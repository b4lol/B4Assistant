package com.b4lol.assistant.tiles;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class LockDeviceTile extends TileService {
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;

    @Override
    public void onClick() {
        super.onClick();
        if (busy) return;
        busy = true;
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand("cmd statusbar collapse; sleep 0.3; input keyevent 26");
            main.post(() -> {
                busy = false;
                update();
                if (!result.isSuccess()) Toast.makeText(this, "Lock failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        update();
    }

    private void update() {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setState(Tile.STATE_INACTIVE);
        tile.setLabel("Lock Device");
        tile.updateTile();
    }
}
