package com.mobileland.sual.client;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScholarshipActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private RecyclerView noticeRecyclerView;
    private NoticeAdapter noticeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarship);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        noticeRecyclerView = findViewById(R.id.noticeRecyclerView);
        noticeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                    noticeAdapter = new NoticeAdapter(notices);
                    noticeRecyclerView.setAdapter(noticeAdapter);
                } else {
                    Toast.makeText(ScholarshipActivity.this, "응답 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notice>> call, Throwable t) {
                Toast.makeText(ScholarshipActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // ActionBar 뒤로가기 버튼 처리
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}