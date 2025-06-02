package com.mobileland.sual.client;

import android.os.Bundle;
import android.widget.TextView;

import com.mobileland.sual.client.api.ScholarshipApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScholarshipActivity extends BaseNoticeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView titleTextView = findViewById(R.id.noticeTitle);
        titleTextView.setText("장학 공지");
    }
    @Override
    protected Call<List<Notice>> getNoticeCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ScholarshipApi api = retrofit.create(ScholarshipApi.class);
        return api.getScholarshipNotices();
    }
}