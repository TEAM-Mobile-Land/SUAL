package com.mobileland.sual.client;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.sdk.user.UserApiClient;

import android.Manifest;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        Log.d("Firebase", "✅ FirebaseApp initialized 상태: " + !FirebaseApp.getApps(this).isEmpty());


        //알림 권한 런타임 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 이상
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent); // 사용자에게 권한 요청 화면 보여줌
                Toast.makeText(this, "앱을 다시 실행해주세요.", Toast.LENGTH_LONG).show();
                return;
            }
        }



        // 🔹 FCM 토픽 구독
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "✅ all 토픽 구독 성공");
                    } else {
                        Log.e("FCM", "❌ all 토픽 구독 실패", task.getException());
                    }
                });

        // 🔹 카카오 사용자 정보 가져오기
        UserApiClient.getInstance().me((user, error) -> {
            if (error != null) {
                Log.e("HomeActivity", "사용자 정보 요청 실패", error);
            } else if (user != null) {
                Long kakaoId = user.getId();  // 고유 ID
                String nickname = null;

                if (user.getKakaoAccount() != null && user.getKakaoAccount().getProfile() != null) {
                    nickname = user.getKakaoAccount().getProfile().getNickname();  // 닉네임
                }

                Log.i("HomeActivity", "✅ 카카오 ID: " + kakaoId + ", 닉네임: " + nickname);

                // TODO: 여기에 서버에 전송하거나 로컬 저장 등의 로직 추가 가능
            }
            return null;
        });

        // 🔹 하단 네비게이션 프래그먼트 처리
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.menu_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.menu_schedule) {
                selectedFragment = new ScheduleFragment();
            } else if (itemId == R.id.menu_save) {
                selectedFragment = new SaveFragment();
            } else if (itemId == R.id.menu_my) {
                selectedFragment = new MyFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // 🔹 기본 프래그먼트 설정
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.menu_home);
        }
    }
}