package com.b4lol.assistant.tiles;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class RecoveryTile extends TileService {
    
    @Override
    public void onClick() {
        super.onClick();
        
        if (!RootUtils.hasRootAccess()) {
            Toast.makeText(this, "Root Required", Toast.LENGTH_LONG).show();
            return;
        }
        
        showIOSDialog();
    }
    
    private void showIOSDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("Reboot to Recovery");
        builder.setMessage("Are you sure you want to reboot to recovery?");
        
        builder.setPositiveButton("Reboot", new android.content.DialogInterface.OnClickListener() {
            public void onClick(android.content.DialogInterface dialog, int which) {
                RootUtils.runAsync(new Runnable() {
                    public void run() {
                        RootUtils.runCommand("reboot recovery");
                    }
                });
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
                    positive.setTextColor(Color.parseColor("#FF3B30"));
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
    
    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setLabel("Recovery");
        tile.updateTile();
    }
}
