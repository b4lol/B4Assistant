package com.b4lol.assistant.tiles;

import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class WiFiTile extends TileService {
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;

    private WifiManager wifiManager() {
        return (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    @Override
    public void onClick() {
        super.onClick();
        if (busy) return;
        busy = true;
        boolean enabled = wifiManager().isWifiEnabled();
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand("cmd statusbar collapse; svc wifi " + (enabled ? "disable" : "enable"));
            main.post(() -> {
                busy = false;
                if (!result.isSuccess()) Toast.makeText(this, "Wi-Fi toggle failed", Toast.LENGTH_SHORT).show();
                updateState(result.isSuccess() ? !enabled : wifiManager().isWifiEnabled());
            });
        });
    }

    private void updateState(boolean enabled) {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel("Wi-Fi");
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateState(wifiManager().isWifiEnabled());
    }
}
