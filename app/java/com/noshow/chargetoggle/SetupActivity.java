package com.noshow.chargetoggle;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** 권한 안내 + 배터리 관련 시스템 설정 키 목록(진단). */
public class SetupActivity extends Activity {
    private static final String GRANT_CMD =
            "adb shell pm grant com.noshow.chargetoggle android.permission.WRITE_SECURE_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        boolean granted = BatteryProtect.hasPermission(this);
        int state = BatteryProtect.read(this);

        StringBuilder sb = new StringBuilder();
        sb.append("권한: ").append(granted ? "허용됨 ✅" : "없음 ❌").append("\n");
        sb.append("현재 상태: ").append(BatteryProtect.describe(state)).append("\n\n");
        if (!granted) {
            sb.append("PC에 USB로 연결(USB 디버깅 ON)한 뒤 아래 명령을 한 번만 실행하세요:\n\n")
              .append(GRANT_CMD).append("\n\n");
        }
        sb.append("── 배터리 관련 설정 키 ──\n").append(dumpKeys());
        final String text = sb.toString();

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextIsSelectable(true);
        tv.setTextSize(13);

        Button copy = new Button(this);
        copy.setText("전체 복사");
        copy.setOnClickListener(v -> {
            ClipboardManager cm = getSystemService(ClipboardManager.class);
            cm.setPrimaryClip(ClipData.newPlainText("diag", text));
            Toast.makeText(this, "복사됨", Toast.LENGTH_SHORT).show();
        });

        root.addView(copy);
        root.addView(tv);
        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }

    private String dumpKeys() {
        StringBuilder out = new StringBuilder();
        for (String table : new String[] {"global", "system", "secure"}) {
            out.append("[").append(table).append("]\n");
            try (Cursor c = getContentResolver().query(
                    Uri.parse("content://settings/" + table),
                    new String[] {"name", "value"}, null, null, null)) {
                if (c == null) { out.append("  (읽기 불가)\n"); continue; }
                while (c.moveToNext()) {
                    String name = c.getString(0);
                    if (name == null) continue;
                    String n = name.toLowerCase();
                    if (n.contains("protect") || n.contains("battery") || n.contains("charg")) {
                        out.append("  ").append(name).append(" = ").append(c.getString(1)).append("\n");
                    }
                }
            } catch (Exception e) {
                out.append("  (오류: ").append(e.getClass().getSimpleName()).append(")\n");
            }
        }
        return out.toString();
    }
}
