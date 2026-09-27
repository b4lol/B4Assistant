package com.b4lol.assistant.tiles;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.b4lol.assistant.utils.RootUtils;

public class DnsTile extends TileService {
    private static final String ENABLED = "B4_DNS_ON";
    private static final String UNCONFIGURED = "B4_DNS_UNCONFIGURED";
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean busy;

    @Override
    public void onClick() {
        super.onClick();
        if (busy) return;
        busy = true;
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand(
                    "mode=$(settings get global private_dns_mode) || exit $?; "
                    + "if [ \"$mode\" = hostname ]; then "
                    + "settings put global private_dns_mode off && echo B4_DNS_OFF; "
                    + "else provider=$(settings get global private_dns_specifier) || exit $?; "
                    + "if [ -z \"$provider\" ] || [ \"$provider\" = null ]; then echo " + UNCONFIGURED + "; exit 67; fi; "
                    + "settings put global private_dns_mode hostname && echo " + ENABLED + "; fi");
            boolean unconfigured = result.getExitCode() == 67 && result.getOutput().contains(UNCONFIGURED);
            boolean rootDenied = !result.isSuccess() && !unconfigured && !RootUtils.hasRootAccess();
            main.post(() -> {
                busy = false;
                if (result.isSuccess()) updateState(result.getOutput().contains(ENABLED));
                else Toast.makeText(this, unconfigured ? "No DNS provider configured"
                        : rootDenied ? "Root Required" : "DNS toggle failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateState(boolean enabled) {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(enabled ? "DNS ON" : "DNS");
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        RootUtils.runAsync(() -> {
            RootUtils.CommandResult result = RootUtils.runCommand("settings get global private_dns_mode");
            main.post(() -> {
                if (!busy && result.isSuccess()) updateState("hostname".equals(result.getOutput().trim()));
            });
        });
    }
}
