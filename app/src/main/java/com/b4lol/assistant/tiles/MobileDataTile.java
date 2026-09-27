package com.b4lol.assistant.tiles;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class MobileDataTile extends TileService {
    private static final String ENABLED = "B4_MOBILE_ON";
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;

    @Override
    public void onClick() {
        super.onClick();
        if (busy) return;
        busy = true;
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand(
                    "state=$(settings get global mobile_data) || exit $?; "
                    + "if [ \"$state\" = 1 ]; then svc data disable && echo B4_MOBILE_OFF; "
                    + "else svc data enable && echo " + ENABLED + "; fi");
            boolean rootDenied = !result.isSuccess() && !RootUtils.hasRootAccess();
            main.post(() -> {
                busy = false;
                if (result.isSuccess()) updateState(result.getOutput().contains(ENABLED));
                else Toast.makeText(this, rootDenied ? "Root Required" : "Mobile Data toggle failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateState(boolean enabled) {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel("Mobile Data");
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand("settings get global mobile_data");
            main.post(() -> {
                if (!busy && result.isSuccess()) updateState("1".equals(result.getOutput().trim()));
            });
        });
    }
}
