package com.mobileland.sual.client;

import com.mobileland.sual.client.api.EventApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventActivity extends BaseNoticeActivity {

    @Override
    protected Call<List<Notice>> getNoticeCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventApi api = retrofit.create(EventApi.class);
        return api.getEventNotices();
    }
}