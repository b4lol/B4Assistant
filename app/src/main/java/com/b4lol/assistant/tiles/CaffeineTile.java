package com.b4lol.assistant.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class CaffeineTile extends TileService {
    private static final int MAX_TIMEOUT = Integer.MAX_VALUE;
    private static final int DEFAULT_TIMEOUT = 30_000;
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean active;
    private boolean busy;
    private volatile boolean destroyed;
    private int originalTimeout = DEFAULT_TIMEOUT;
    private BroadcastReceiver screenOffReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        screenOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) && active && !busy) stopCaffeine();
            }
        };
        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        if (screenOffReceiver != null) unregisterReceiver(screenOffReceiver);
        if (active) RootUtils.runAsync(() -> restoreTimeout(originalTimeout));
        super.onDestroy();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (busy) return;
        if (active) stopCaffeine();
        else startCaffeine();
    }

    private void startCaffeine() {
        busy = true;
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand(
                    "old=$(settings get system screen_off_timeout) || exit $?; "
                    + "settings put system screen_off_timeout " + MAX_TIMEOUT + " && echo \"$old\"");
            int timeout = DEFAULT_TIMEOUT;
            if (result.isSuccess()) {
                try {
                    int parsed = Integer.parseInt(result.getOutput().trim());
                    if (parsed > 0 && parsed <= 86_400_000) timeout = parsed;
                } catch (NumberFormatException ignored) { }
            }
            final int savedTimeout = timeout;
            main.post(() -> {
                busy = false;
                if (result.isSuccess() && !destroyed) {
                    originalTimeout = savedTimeout;
                    active = true;
                    updateState();
                    Toast.makeText(this, "Caffeine ON", Toast.LENGTH_SHORT).show();
                } else {
                    if (result.isSuccess()) RootUtils.runAsync(() -> restoreTimeout(savedTimeout));
                    else Toast.makeText(this, "Caffeine failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void stopCaffeine() {
        busy = true;
        int timeout = originalTimeout;
        RootUtils.runAsync(() -> {
            boolean success = restoreTimeout(timeout);
            main.post(() -> {
                busy = false;
                if (success) active = false;
                updateState();
                if (!destroyed) Toast.makeText(this, success ? "Caffeine OFF" : "Failed to restore timeout", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private boolean restoreTimeout(int timeout) {
        return RootUtils.runCommand("settings put system screen_off_timeout " + timeout).isSuccess();
    }

    private void updateState() {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setLabel(active ? "Caffeine ON" : "Caffeine");
        tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateState();
    }
}
