package com.mobileland.sual.client;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.kakao.sdk.common.KakaoSdk;
public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:298596866657:android:08a3d2ec3d12a8c23ae820")
                .setApiKey("AIzaSyCLDLWde1u8A9VaMte60ljQTRinpPgCcrg")
                .setProjectId("sual-notice")
                .build();

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options);
        }
        KakaoSdk.init(this, "f52007b16768c0a9fef5e27fab5ec66f");
    }
}
