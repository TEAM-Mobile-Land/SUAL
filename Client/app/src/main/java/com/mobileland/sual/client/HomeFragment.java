package com.mobileland.sual.client;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // layout 파일은 나중에 res/layout/fragment_home.xml 로 자동 생성됨
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}