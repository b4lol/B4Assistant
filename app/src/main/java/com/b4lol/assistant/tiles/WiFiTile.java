package com.b4lol.assistant.tiles;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class WiFiTile extends TileService {
    
    @Override
    public void onClick() {
        super.onClick();
        toggleWiFi();
    }
    
    @TargetApi(Build.VERSION_CODES.Q)
    @Override
    public void onLongClick() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    private void toggleWiFi() {
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        final boolean isEnabled = wifiManager.isWifiEnabled();
        final WiFiTile context = this;
        
        RootUtils.collapseStatusBar();
        
        RootUtils.runAsync(new Runnable() {
            public void run() {
                boolean success = false;
                
                try {
                    success = wifiManager.setWifiEnabled(!isEnabled);
                } catch (Exception e) {
                    success = false;
                }
                
                if (!success && RootUtils.hasRootAccess()) {
                    RootUtils.runCommand("svc wifi " + (isEnabled ? "disable" : "enable"));
                    success = true;
                }
                
                final boolean newState = !isEnabled;
                final boolean finalSuccess = success;
                
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if (finalSuccess) {
                            updateTileState(newState);
                            Toast.makeText(context, "WiFi " + (newState ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "WiFi toggle failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    
    private void updateTileState(boolean enabled) {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel("WiFi");
            tile.updateTile();
        }
    }
    
    @Override
    public void onStartListening() {
        super.onStartListening();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        updateTileState(wifiManager.isWifiEnabled());
    }
}
