package com.mobileland.sual.client;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 공지를 불러오는 클래스들의 부모 클래스
public abstract class BaseNoticeActivity extends AppCompatActivity {

    private RecyclerView noticeRecyclerView;
    private NoticeAdapter noticeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarship); // 재사용 가능하면 이 레이아웃 그대로 사용

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        noticeRecyclerView = findViewById(R.id.noticeRecyclerView);
        noticeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNotices();
    }

    private void loadNotices() {
        getNoticeCall().enqueue(new Callback<List<Notice>>() {
            @Override
            public void onResponse(Call<List<Notice>> call, Response<List<Notice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notice> notices = response.body();

                    notices = notices.stream()
                            .filter(n -> n.getDate() != null && !n.getDate().trim().isEmpty())
                            .collect(Collectors.toList());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
                    Collections.sort(notices, (o1, o2) -> {
                        try {
                            return sdf.parse(o2.getDate().trim()).compareTo(sdf.parse(o1.getDate().trim()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });

                    noticeAdapter = new NoticeAdapter(notices);
                    noticeRecyclerView.setAdapter(noticeAdapter);
                } else {
                    Toast.makeText(BaseNoticeActivity.this, "응답 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notice>> call, Throwable t) {
                Toast.makeText(BaseNoticeActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 각 자식 Activity에서 구현해야 할 부분: 어떤 API를 호출할지
    protected abstract Call<List<Notice>> getNoticeCall();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}