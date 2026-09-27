package com.b4lol.assistant.tiles;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class ScreenshotTile extends TileService {
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;

    @Override
    public void onClick() {
        super.onClick();
        if (busy) return;
        busy = true;
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand("cmd statusbar collapse; sleep 0.6; input keyevent 120");
            boolean rootDenied = !result.isSuccess() && !RootUtils.hasRootAccess();
            main.post(() -> {
                busy = false;
                Tile tile = getQsTile();
                if (tile != null) {
                    tile.setState(Tile.STATE_INACTIVE);
                    tile.updateTile();
                }
                Toast.makeText(this, result.isSuccess() ? "Screenshot taken" : rootDenied ? "Root Required" : "Screenshot failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("Screenshot");
            tile.updateTile();
        }
    }
}
