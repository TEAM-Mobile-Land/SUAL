package com.mobileland.sual.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.user.UserApiClient;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Locale locale = new Locale("ko");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());


        UserApiClient.getInstance().me((user, error) -> {
            if (error != null) {
                Log.e("MainActivity", "사용자 정보 요청 실패", error);
            } else if (user != null) {
                if (user.getKakaoAccount() != null) {
                    String email = user.getKakaoAccount().getEmail();
                    if (email != null) {
                        Log.i("MainActivity", "이메일: " + email);
                    } else {
                        Log.w("MainActivity", "이메일 정보 없음 (사용자 동의 X)");
                    }
                } else {
                    Log.w("MainActivity", "KakaoAccount 정보 없음");
                }
            } else {
                Log.w("MainActivity", "user 객체가 null입니다.");
            }
            return null;
        });

    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
