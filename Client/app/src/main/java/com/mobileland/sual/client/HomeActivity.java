package com.mobileland.sual.client;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.sdk.user.UserApiClient;

import android.Manifest;
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
                if (user.getKakaoAccount() != null) {
                    String email = user.getKakaoAccount().getEmail();
                    if (email != null) {
                        Log.i("HomeActivity", "이메일: " + email);
                    } else {
                        Log.w("HomeActivity", "이메일 정보 없음 (사용자 동의 X)");
                    }
                } else {
                    Log.w("HomeActivity", "KakaoAccount 정보 없음");
                }
            } else {
                Log.w("HomeActivity", "user 객체가 null입니다.");
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
