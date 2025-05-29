package com.mobileland.sual.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button kakaoLoginBtn = findViewById(R.id.kakao_login_button);

        kakaoLoginBtn.setOnClickListener(v -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(this, callback);
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(this, callback);
            }
        });
    }

    final Function2<OAuthToken, Throwable, Unit> callback = (token, error) -> {
        if (error != null) {
            Log.e("KakaoLogin", "로그인 실패", error);
        } else if (token != null) {
            Log.i("KakaoLogin", "로그인 성공: " + token.getAccessToken());

            UserApiClient.getInstance().me((user, meError) -> {
                if (meError != null) {
                    Log.e("KakaoLogin", "사용자 정보 요청 실패", meError);
                } else if (user != null) {
                    Long kakaoId = user.getId();
                    Log.i("KakaoLogin", "카카오 ID: " + kakaoId);

                    if (user.getKakaoAccount() != null && user.getKakaoAccount().getProfile() != null) {
                        String nickname = user.getKakaoAccount().getProfile().getNickname();
                        Log.i("KakaoLogin", "닉네임: " + nickname);
                    } else {
                        Log.w("KakaoLogin", "KakaoAccount 또는 Profile 정보 없음");
                    }

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.w("KakaoLogin", "사용자 정보가 null입니다.");
                }
                return null;
            });
        }
        return null;
    };
}
