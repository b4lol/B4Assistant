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
    
    private static final String DEFAULT_LABEL = "Caffeine";
    private static final int INFINITE_TIMEOUT = Integer.MAX_VALUE;
    
    private boolean isCaffeineActive = false;
    private int originalTimeout = 30000;
    private BroadcastReceiver screenOffReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        screenOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) && isCaffeineActive) {
                    stopCaffeine();
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenOffReceiver, filter);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (screenOffReceiver != null) {
            try {
                unregisterReceiver(screenOffReceiver);
            } catch (Exception e) { }
        }
        if (isCaffeineActive) {
            restoreOriginalTimeout();
        }
    }
    
    @Override
    public void onClick() {
        super.onClick();
        
        if (!RootUtils.hasRootAccess()) {
            Toast.makeText(this, "Root Required", Toast.LENGTH_LONG).show();
            return;
        }
        
        RootUtils.collapseStatusBar();
        
        if (isCaffeineActive) {
            stopCaffeine();
        } else {
            startCaffeine();
        }
    }
    
    private void startCaffeine() {
        RootUtils.runAsync(new Runnable() {
            public void run() {
                try {
                    String result = RootUtils.runCommand("settings get system screen_off_timeout").getOutput().trim();
                    try {
                        originalTimeout = Integer.parseInt(result);
                        if (originalTimeout <= 0 || originalTimeout > 86400000) {
                            originalTimeout = 30000;
                        }
                    } catch (NumberFormatException e) {
                        originalTimeout = 30000;
                    }
                    
                    RootUtils.runCommand("settings put system screen_off_timeout " + INFINITE_TIMEOUT);
                    isCaffeineActive = true;
                    
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            updateTileState(true);
                            Toast.makeText(CaffeineTile.this, "Caffeine ON", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            Toast.makeText(CaffeineTile.this, "Error", Toast.LENGTH_SHORT).show();
                            updateTileState(false);
                        }
                    });
                }
            }
        });
    }
    
    private void stopCaffeine() {
        isCaffeineActive = false;
        
        RootUtils.runAsync(new Runnable() {
            public void run() {
                restoreOriginalTimeout();
                
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        updateTileState(false);
                        Toast.makeText(CaffeineTile.this, "Caffeine OFF", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void restoreOriginalTimeout() {
        try {
            RootUtils.runCommand("settings put system screen_off_timeout " + originalTimeout);
        } catch (Exception e) {
            RootUtils.runCommand("settings put system screen_off_timeout 30000");
        }
    }
    
    private void updateTileState(boolean active) {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel(active ? "Caffeine ON" : DEFAULT_LABEL);
            tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }
    
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState(isCaffeineActive);
    }
}
