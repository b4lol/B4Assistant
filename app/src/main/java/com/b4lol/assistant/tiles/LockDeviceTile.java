package com.b4lol.assistant.tiles;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class LockDeviceTile extends TileService {
    
    @Override
    public void onClick() {
        super.onClick();
        
        final Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
        
        RootUtils.runAsync(new Runnable() {
            public void run() {
                RootUtils.collapseStatusBar();
                
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {}
                
                RootUtils.runCommand("input keyevent 26");
                
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        tile.setState(Tile.STATE_INACTIVE);
                        tile.updateTile();
                    }
                });
            }
        });
    }
    
    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setLabel("Lock Device");
        tile.updateTile();
    }
}
