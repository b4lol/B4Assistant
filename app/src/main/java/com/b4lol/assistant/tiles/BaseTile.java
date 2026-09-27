package com.b4lol.assistant.tiles;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public abstract class BaseTile extends TileService {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private boolean running;

    protected abstract String getScriptPath();
    protected abstract String getModuleUrl();
    protected abstract String getModuleName();

    @Override
    public void onClick() {
        super.onClick();
        if (running) return;
        running = true;
        update(Tile.STATE_UNAVAILABLE, "Running...");
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runScript(getScriptPath());
            boolean missing = RootUtils.isModuleMissing(result);
            boolean rootDenied = !result.isSuccess() && !missing && !RootUtils.hasRootAccess();
            MAIN.post(() -> {
                running = false;
                update(result.isSuccess() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE, getModuleName());
                if (rootDenied) {
                    Toast.makeText(this, "Root Required", Toast.LENGTH_LONG).show();
                } else if (missing) {
                    if (isLocked()) unlockAndRun(this::showModuleMissingDialog);
                    else showModuleMissingDialog();
                } else if (!result.isSuccess()) {
                    Toast.makeText(this, getModuleName() + " failed", Toast.LENGTH_SHORT).show();
                } else {
                    MAIN.postDelayed(() -> update(Tile.STATE_INACTIVE, getModuleName()), 1000);
                }
            });
        });
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        if (!running) update(Tile.STATE_INACTIVE, getModuleName());
    }

    private void update(int state, String label) {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setState(state);
        tile.setLabel(label);
        tile.updateTile();
    }

    private void showModuleMissingDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getModuleName() + " not installed")
                .setMessage("The required module is unavailable. Open its releases page?")
                .setPositiveButton("Open releases", (ignored, which) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getModuleUrl()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= 34) {
                        PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
                                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        startActivityAndCollapse(pending);
                    } else {
                        startActivityAndCollapse(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        showDialog(dialog);
    }
}
