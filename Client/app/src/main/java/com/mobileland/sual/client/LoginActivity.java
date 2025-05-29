package com.mobileland.sual.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.user.model.Profile;

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

                    Account kakaoAccount = user.getKakaoAccount();
                    if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
                        String nickname = kakaoAccount.getProfile().getNickname();
                        Log.i("KakaoLogin", "닉네임: " + nickname);
                    } else {
                        Log.w("KakaoLogin", "닉네임 정보를 가져올 수 없습니다.");
                    }

                    // 홈 화면으로 이동
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.w("KakaoLogin", "user 객체가 null입니다.");
                }
                return null;
            });
        }
        return null;
    };
}
