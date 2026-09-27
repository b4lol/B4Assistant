package com.b4lol.assistant.tiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

import java.lang.reflect.Method;

public class MobileDataTile extends TileService {
    
    @Override
    public void onClick() {
        super.onClick();
        
        if (!RootUtils.hasRootAccess()) {
            Toast.makeText(this, "Root Required", Toast.LENGTH_LONG).show();
            return;
        }
        
        toggleMobileData();
    }
    
    @TargetApi(Build.VERSION_CODES.Q)
    @Override
    public void onLongClick() {
        Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    private void toggleMobileData() {
        final boolean isEnabled = isMobileDataEnabled();
        final Context context = this;
        
        RootUtils.collapseStatusBar();
        
        RootUtils.runAsync(new Runnable() {
            public void run() {
                try {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    Method method = cm.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    method.setAccessible(true);
                    method.invoke(cm, !isEnabled);
                } catch (Exception e) {
                    RootUtils.runCommand("svc data " + (isEnabled ? "disable" : "enable"));
                }
                
                final boolean newState = !isEnabled;
                
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        updateTileState(newState);
                        Toast.makeText(context, "Mobile Data " + (newState ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private boolean isMobileDataEnabled() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Method method = cm.getClass().getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(cm);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void updateTileState(boolean enabled) {
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
        updateTileState(isMobileDataEnabled());
    }
}
