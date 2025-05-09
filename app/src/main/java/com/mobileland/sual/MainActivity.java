package com.mobileland.sual;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    // 1. 삼육대학교 공지사항 접속 후 데이터 가져오기.
    // 2. 가져온 데이터가 이미 가져온 것이면, 무시.
    // 3. 내용 저장
    // 4. 다음 url 시도.

    public void crawling() {
        String url = "https://www.syu.ac.kr/academic/academic-notice/";

        try {
            //url에서 HTML 문서 가져오기

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}