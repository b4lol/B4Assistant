package com.b4lol.assistant.tiles;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class ScreenshotTile extends TileService {
    
    @Override
    public void onClick() {
        super.onClick();
        
        if (!RootUtils.hasRootAccess()) {
            Toast.makeText(this, "Root Required", Toast.LENGTH_LONG).show();
            return;
        }
        
        final Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
        
        RootUtils.runAsync(new Runnable() {
            public void run() {
                RootUtils.collapseStatusBar();
                
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {}
                
                RootUtils.runCommand("input keyevent 120");
                
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        tile.setState(Tile.STATE_INACTIVE);
                        tile.updateTile();
                        Toast.makeText(ScreenshotTile.this, "Screenshot taken", Toast.LENGTH_SHORT).show();
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
        tile.setLabel("Screenshot");
        tile.updateTile();
    }
}
