package com.b4lol.assistant.tiles;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class DnsTile extends TileService {
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
            String specifier = root && !enabled ? RootUtils.runCommand("settings get global private_dns_specifier").getOutput().trim() : "";
            boolean configured = enabled || (!specifier.isEmpty() && !"null".equals(specifier));
            boolean success = root && configured && RootUtils.runCommand("settings put global private_dns_mode " + (enabled ? "off" : "hostname")).isSuccess();
            main.post(() -> {
                busy = false;
                if (success) updateState(!enabled);
                else Toast.makeText(this, !root ? "Root Required" : !configured ? "No DNS provider configured" : "DNS toggle failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private boolean isEnabled() {
        RootUtils.CommandResult result = RootUtils.runCommand("settings get global private_dns_mode");
        return result.isSuccess() && "hostname".equals(result.getOutput().trim());
    }

    private void updateState(boolean enabled) {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel(enabled ? "DNS ON" : "DNS");
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
