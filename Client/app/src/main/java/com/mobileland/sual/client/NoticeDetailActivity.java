package com.mobileland.sual.client;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoticeDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        ImageButton closeBtn = findViewById(R.id.closeButton);
        closeBtn.setOnClickListener(v -> finish());

        TextView titleText = findViewById(R.id.detailTitle);
        TextView dateText = findViewById(R.id.detailDate);
        TextView aiSummaryText = findViewById(R.id.detailAiSummary);

        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String aiSummary = getIntent().getStringExtra("aiSummary");

        titleText.setText(title);
        dateText.setText(date);
        aiSummaryText.setText(highlightSummary(aiSummary));
        aiSummaryText.setAutoLinkMask(Linkify.WEB_URLS);
        aiSummaryText.setLinkTextColor(Color.parseColor("#2196F3"));  // 링크 색 고정
        aiSummaryText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableStringBuilder highlightSummary(String input) {
        SpannableStringBuilder builder = new SpannableStringBuilder(input);

        // 꺽새 <내용> 처리 (하이라이트)
        Pattern anglePattern = Pattern.compile("<(.*?)>");
        Matcher angleMatcher = anglePattern.matcher(builder);
        while (angleMatcher.find()) {
            String highlightText = angleMatcher.group(1);
            int start = angleMatcher.start();
            int end = angleMatcher.end();

            // 꺽새 제거
            builder.replace(start, end, highlightText);
            end = start + highlightText.length();

            builder.setSpan(
                    new BackgroundColorSpan(ContextCompat.getColor(this, R.color.primary300)), // 파란 배경
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            builder.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary900)),
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            builder.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            angleMatcher = anglePattern.matcher(builder); // 갱신
        }

        // 별표 **내용** 처리 (볼드)
        Pattern starPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher starMatcher = starPattern.matcher(builder);
        while (starMatcher.find()) {
            String boldText = starMatcher.group(1);
            int start = starMatcher.start();
            int end = starMatcher.end();

            // 별표 제거
            builder.replace(start, end, boldText);
            end = start + boldText.length();

            builder.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            starMatcher = starPattern.matcher(builder); // 갱신
        }

        return builder;
    }
}