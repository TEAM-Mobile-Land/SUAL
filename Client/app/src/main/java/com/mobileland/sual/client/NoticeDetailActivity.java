package com.mobileland.sual.client;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mobileland.sual.client.database.NoticeDatabaseHelper;

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
        aiSummaryText.setLinkTextColor(Color.parseColor("#2196F3"));
        aiSummaryText.setMovementMethod(LinkMovementMethod.getInstance());

        Button deadlineButton = findViewById(R.id.deadlineButton);
        deadlineButton.setOnClickListener(v -> {
            Intent intent = getIntent();
            String content = intent.getStringExtra("content");
            String url = intent.getStringExtra("url");
            String type = intent.getStringExtra("type");

            String deadline = extractDeadlineFromText(aiSummary);

            NoticeDatabaseHelper dbHelper = new NoticeDatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("content", content);
            values.put("date", date);
            values.put("url", url);
            values.put("type", type);
            values.put("aiSummary", aiSummary);
            values.put("deadline", deadline);

            try {
                db.insertOrThrow(NoticeDatabaseHelper.TABLE_NAME, null, values);
                Toast.makeText(this, "마감일 저장 및 알림이 예약되었습니다.", Toast.LENGTH_SHORT).show();

                if (deadline != null) {
                    scheduleNotification(this, title, deadline);
                    Log.d("NoticeAlarm", "⏰ 알림 예약됨: " + deadline);
                }
                Log.d("NoticeSave", "✅ 공지 저장됨: " + title);

            } catch (Exception e) {
                Toast.makeText(this, "이미 저장된 공지입니다.", Toast.LENGTH_SHORT).show();
                Log.w("NoticeSave", "⚠️ 중복 저장 차단됨: " + e.getMessage());
            } finally {
                db.close();
            }

        });
    }

    private SpannableStringBuilder highlightSummary(String input) {
        SpannableStringBuilder builder = new SpannableStringBuilder(input);

        Pattern anglePattern = Pattern.compile("<(.*?)>");
        Matcher angleMatcher = anglePattern.matcher(builder);
        while (angleMatcher.find()) {
            String highlightText = angleMatcher.group(1);
            int start = angleMatcher.start();
            int end = angleMatcher.end();

            builder.replace(start, end, highlightText);
            end = start + highlightText.length();

            builder.setSpan(new BackgroundColorSpan(ContextCompat.getColor(this, R.color.primary300)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary900)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            angleMatcher = anglePattern.matcher(builder);
        }

        Pattern starPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher starMatcher = starPattern.matcher(builder);
        while (starMatcher.find()) {
            String boldText = starMatcher.group(1);
            int start = starMatcher.start();
            int end = starMatcher.end();

            builder.replace(start, end, boldText);
            end = start + boldText.length();

            builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            starMatcher = starPattern.matcher(builder);
        }

        return builder;
    }

    private String extractDeadlineFromText(String summary) {
        if (summary == null) return null;

        // 1. "6월 30일" 또는 "6월30일까지"
        Pattern pattern1 = Pattern.compile("(\\d{1,2})월\\s*(\\d{1,2})일.*?까지");
        Matcher matcher1 = pattern1.matcher(summary);
        if (matcher1.find()) {
            String month = matcher1.group(1);
            String day = matcher1.group(2);
            return String.format("2025-%02d-%02d", Integer.parseInt(month), Integer.parseInt(day));
        }

        // 2. "6. 30." 또는 "6.30일까지"
        Pattern pattern2 = Pattern.compile("(\\d{1,2})\\.\\s*(\\d{1,2})\\.(일|\\(.*?\\))?.*?까지");
        Matcher matcher2 = pattern2.matcher(summary);
        if (matcher2.find()) {
            String month = matcher2.group(1);
            String day = matcher2.group(2);
            return String.format("2025-%02d-%02d", Integer.parseInt(month), Integer.parseInt(day));
        }

        return null;
    }


    private void scheduleNotification(Context context, String title, String deadline) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 3); // 테스트용 즉시 알림 (3초 후)

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", "📌 마감 알림");
        intent.putExtra("content", title + " 마감일이 다가옵니다!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, title.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}