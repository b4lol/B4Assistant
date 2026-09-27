package com.b4lol.assistant.tiles;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class MobileDataTile extends TileService {
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;

    @Override
    public void onClick() {
        super.onClick();
        if (busy) return;
        busy = true;
        RootUtils.runAsync(() -> {
            boolean root = RootUtils.hasRootAccess();
            boolean enabled = root && isEnabled();
            boolean success = root && RootUtils.runCommand("svc data " + (enabled ? "disable" : "enable")).isSuccess();
            main.post(() -> {
                busy = false;
                if (success) updateState(!enabled);
                else Toast.makeText(this, root ? "Mobile Data toggle failed" : "Root Required", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private boolean isEnabled() {
        RootUtils.CommandResult result = RootUtils.runCommand("settings get global mobile_data");
        return result.isSuccess() && "1".equals(result.getOutput().trim());
    }

    private void updateState(boolean enabled) {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel("Mobile Data");
            tile.updateTile();
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        RootUtils.runAsync(() -> {
            boolean enabled = isEnabled();
            main.post(() -> updateState(enabled));
        });
    }
}
