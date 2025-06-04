package com.mobileland.sual.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final List<String> scheduleList;

    public ScheduleAdapter(List<String> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView scheduleItem;

        public ViewHolder(View view) {
            super(view);
            scheduleItem = view.findViewById(R.id.schedule_item);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.scheduleItem.setText(scheduleList.get(position));
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }
}
