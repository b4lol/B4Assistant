package com.b4lol.assistant.tiles;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public abstract class BaseTile extends TileService {
    
    protected abstract String getScriptPath();
    protected abstract String getModuleUrl();
    protected abstract String getModuleName();
    
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private boolean running;

    @Override
    public void onClick() {
        super.onClick();
        if (running) return;
        running = true;
        final Tile tile = getQsTile();
        final String label = getModuleName();
        if (tile != null) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("Running...");
            tile.updateTile();
        }
        RootUtils.runAsync(() -> {
            boolean root = RootUtils.hasRootAccess();
            boolean exists = root && RootUtils.fileExists(getScriptPath());
            RootUtils.CommandResult result = exists
                    ? RootUtils.runCommand("sh " + RootUtils.shellQuote(getScriptPath())) : null;
            MAIN.post(() -> {
                running = false;
                if (tile != null) {
                    tile.setState(result != null && result.isSuccess() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
                    tile.setLabel(label);
                    tile.updateTile();
                }
                if (!root) Toast.makeText(this, "Root Required", Toast.LENGTH_LONG).show();
                else if (!exists) showModuleMissingDialog();
                else if (!result.isSuccess()) Toast.makeText(this, label + " failed", Toast.LENGTH_SHORT).show();
                else if (tile != null) MAIN.postDelayed(() -> {
                    tile.setState(Tile.STATE_INACTIVE);
                    tile.updateTile();
                }, 1000);
            });
        });
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null && !running) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel(getModuleName());
            tile.updateTile();
        }
    }
    
    private void showModuleMissingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(8));
        
        TextView title = new TextView(this);
        title.setText(getModuleName() + " Not Installed");
        title.setTextColor(Color.BLACK);
        title.setTextSize(17);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        layout.addView(title);
        
        TextView message = new TextView(this);
        message.setText("The required module is not installed. Would you like to download it?");
        message.setTextColor(Color.parseColor("#8E8E93"));
        message.setTextSize(13);
        message.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        message.setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(16));
        layout.addView(message);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Download", new android.content.DialogInterface.OnClickListener() {
            public void onClick(android.content.DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getModuleUrl()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityAndCollapse(intent);
            }
        });
        
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        final AlertDialog dialog = builder.create();
        
        dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
            public void onShow(android.content.DialogInterface d) {
                Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                
                if (positive != null) {
                    positive.setTextColor(Color.parseColor("#007AFF"));
                    positive.setAllCaps(false);
                    positive.setTextSize(16);
                }
                if (negative != null) {
                    negative.setTextColor(Color.parseColor("#007AFF"));
                    negative.setAllCaps(false);
                    negative.setTextSize(16);
                }
                
                Window window = dialog.getWindow();
                if (window != null) {
                    android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                    bg.setCornerRadius(dpToPx(14));
                    bg.setColor(Color.parseColor("#F2F2F7"));
                    window.setBackgroundDrawable(bg);
                }
            }
        });
        
        dialog.show();
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
