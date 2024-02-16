package com.example.notification.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notification.R;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ScheduleViewHolder> {

    private List<String> scheduleList;

    public Adapter(List<String> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use parent.getContext() to get the correct context for inflating the layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        String scheduleItem = scheduleList.get(position);
        holder.scheduleTextView.setText(scheduleItem);
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView scheduleTextView;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleTextView = itemView.findViewById(R.id.scheduleItemText);
        }
    }
}
