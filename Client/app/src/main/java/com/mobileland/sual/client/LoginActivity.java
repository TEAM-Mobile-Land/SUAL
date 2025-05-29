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
                    if (kakaoAccount != null) {
                        Profile profile = kakaoAccount.getProfile();
                        if (profile != null) {
                            String nickname = profile.getNickname();
                            Log.i("KakaoLogin", "닉네임: " + nickname);
                        } else {
                            Log.w("KakaoLogin", "프로필 정보 없음");
                        }

                        String email = kakaoAccount.getEmail();
                        if (email != null) {
                            Log.i("KakaoLogin", "이메일: " + email);
                        } else {
                            Log.w("KakaoLogin", "이메일 정보 없음 또는 동의하지 않음");
                        }
                    } else {
                        Log.w("KakaoLogin", "KakaoAccount 정보 없음");
                    }


                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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
