package com.mobileland.sual.client;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import java.security.MessageDigest;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


public class LoginActivity extends AppCompatActivity {

    private Button kakaoLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        kakaoLoginBtn = findViewById(R.id.kakao_login_button);

        kakaoLoginBtn.setOnClickListener(v -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(this, callback);
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(this, callback);
            }
        });
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES
            );
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("KeyHash", "🔥 실제 사용 중인 키 해시: " + keyHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
