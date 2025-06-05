package com.mobileland.sual.client;

import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import com.mobileland.sual.client.utils.ThemeUtils;
import android.content.Intent;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnGoHome = findViewById(R.id.btnGoHome);
        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish(); // 설정화면 종료
        });

        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setChecked(ThemeUtils.isDark(this));

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeUtils.saveTheme(this, isChecked);
            recreate();
        });
    }
}