package com.mobileland.sual.client;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> noticeList;

    public NoticeAdapter(List<Notice> noticeList) {
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.titleText.setText(notice.getTitle());
        holder.dateText.setText(notice.getDate());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), NoticeDetailActivity.class);
            intent.putExtra("title", notice.getTitle());
            intent.putExtra("date", notice.getDate());
            intent.putExtra("aiSummary", notice.getAiSummary());
            intent.putExtra("url", notice.getUrl());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.noticeItemTitle);
            dateText = itemView.findViewById(R.id.noticeItemDate);
        }
    }
}