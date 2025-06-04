package com.mobileland.sual.client;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobileland.sual.client.database.NoticeDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SaveFragment extends Fragment {

    private RecyclerView recyclerView;
    private NoticeAdapter adapter;
    private List<Notice> savedNotices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save, container, false);
        recyclerView = view.findViewById(R.id.saveRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadNoticesFromDB();

        return view;
    }

    private void loadNoticesFromDB() {
        savedNotices = new ArrayList<>();
        NoticeDatabaseHelper dbHelper = new NoticeDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(NoticeDatabaseHelper.TABLE_NAME, null, null, null, null, null, "date DESC");

        while (cursor.moveToNext()) {
            savedNotices.add(new Notice(
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("aiSummary")),
                    cursor.getString(cursor.getColumnIndexOrThrow("url"))
            ));

        }
        cursor.close();
        db.close();

        adapter = new NoticeAdapter(savedNotices);
        recyclerView.setAdapter(adapter);
    }
}
