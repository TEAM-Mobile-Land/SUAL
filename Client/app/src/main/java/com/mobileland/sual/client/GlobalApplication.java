package com.mobileland.sual.client;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;
public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "f52007b16768c0a9fef5e27fab5ec66f");
    }
}
