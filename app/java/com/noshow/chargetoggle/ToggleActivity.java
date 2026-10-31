package com.noshow.chargetoggle;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class ToggleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BatteryProtect.hasPermission(this)) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        int state;
        try {
            state = BatteryProtect.toggle(this);
        } catch (SecurityException e) {
            state = BatteryProtect.UNKNOWN;
        }

        if (state == BatteryProtect.UNKNOWN) {
            startActivity(new Intent(this, SetupActivity.class));
        } else {
            Toast.makeText(this, BatteryProtect.describe(state), Toast.LENGTH_SHORT).show();
            TileService.requestListeningState(this, new ComponentName(this, ToggleTileService.class));
        }
        finish();
    }
}
