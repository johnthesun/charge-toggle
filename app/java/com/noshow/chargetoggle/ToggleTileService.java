package com.noshow.chargetoggle;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class ToggleTileService extends TileService {
    @Override
    public void onStartListening() {
        refresh();
    }

    @Override
    public void onClick() {
        if (!BatteryProtect.hasPermission(this)) {
            Intent i = new Intent(this, SetupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityAndCollapse(i);
            return;
        }
        int state = BatteryProtect.toggle(this);
        Toast.makeText(this, BatteryProtect.describe(state), Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void refresh() {
        Tile tile = getQsTile();
        if (tile == null) return;
        int state = BatteryProtect.read(this);
        tile.setState(state == BatteryProtect.ON ? Tile.STATE_ACTIVE
                : state == BatteryProtect.OFF ? Tile.STATE_INACTIVE : Tile.STATE_UNAVAILABLE);
        tile.setLabel(state == BatteryProtect.ON ? "충전 85%" : state == BatteryProtect.OFF ? "충전 100%" : "충전 제한");
        tile.updateTile();
    }
}
