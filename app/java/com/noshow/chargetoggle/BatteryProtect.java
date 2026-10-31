package com.noshow.chargetoggle;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

/** 삼성 '배터리 보호' 스위치(Settings.Global protect_battery)를 켜고 끈다. */
final class BatteryProtect {
    static final String KEY = "protect_battery";

    static final int OFF = 0;
    static final int ON = 1;
    static final int UNKNOWN = -1;

    private BatteryProtect() {}

    static boolean hasPermission(Context ctx) {
        return ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    static int read(Context ctx) {
        return Settings.Global.getInt(ctx.getContentResolver(), KEY, UNKNOWN);
    }

    /** 토글 후 새 상태를 돌려준다. 키가 없으면 UNKNOWN. */
    static int toggle(Context ctx) {
        ContentResolver cr = ctx.getContentResolver();
        int cur = read(ctx);
        if (cur == UNKNOWN) return UNKNOWN;
        int next = (cur == OFF) ? ON : OFF;
        Settings.Global.putInt(cr, KEY, next);
        return read(ctx);
    }

    static String describe(int state) {
        if (state == ON) return "배터리 보호 ON (85% 제한)";
        if (state == OFF) return "배터리 보호 OFF (100% 충전)";
        return "배터리 보호 설정 키를 찾지 못함";
    }
}
