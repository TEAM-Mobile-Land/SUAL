package com.mobileland.sual.client;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NoticeDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        ImageButton closeBtn = findViewById(R.id.closeButton);
        closeBtn.setOnClickListener(v -> {
            finish();
        });

        TextView titleText = findViewById(R.id.detailTitle);
        TextView dateText = findViewById(R.id.detailDate);
        TextView aiSummaryText = findViewById(R.id.detailAiSummary);

        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String aiSummary = getIntent().getStringExtra("aiSummary");

        titleText.setText(title);
        dateText.setText(date);
        aiSummaryText.setText(aiSummary);
    }
}