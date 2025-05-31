package com.mobileland.sual.client;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.widget.TextView;

public class ScholarshipActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private TextView noticeTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarship);

        noticeTitleText = findViewById(R.id.noticeTitleText);
        loadScholarshipNotices();
    }

    private void loadScholarshipNotices() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ScholarshipApi api = retrofit.create(ScholarshipApi.class);

        Call<List<Notice>> call = api.getScholarshipNotices();
        call.enqueue(new Callback<List<Notice>>() {
            @Override
            public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notice> notices = response.body();

                    // 첫 번째 공지 제목을 TextView에 표시
                    if (!notices.isEmpty()) {
                        Notice firstNotice = notices.get(0);
                        noticeTitleText.setText(firstNotice.getTitle());
                    } else {
                        noticeTitleText.setText("공지 없음");
                    }
                } else {
                    noticeTitleText.setText("응답 실패");
                }
            }

            @Override
            public void onFailure(Call<List<Notice>> call, Throwable t) {
                noticeTitleText.setText("네트워크 오류: " + t.getMessage());
            }
        });
    }
}