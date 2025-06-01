package com.mobileland.sual.client;

import com.mobileland.sual.client.api.AcademicApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AcademicActivity extends BaseNoticeActivity {

    @Override
    protected Call<List<Notice>> getNoticeCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AcademicApi api = retrofit.create(AcademicApi.class);
        return api.getAcademicNotices();
    }
}