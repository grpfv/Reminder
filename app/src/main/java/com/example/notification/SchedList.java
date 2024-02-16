package com.example.notification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.notification.RecyclerView.Adapter;

import java.util.ArrayList;
import java.util.List;

public class SchedList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sched_list);

        // Retrieve the selected day and time from the intent
        String selectedDay = getIntent().getStringExtra("selectedDay");
        String timeIn = getIntent().getStringExtra("timeIn");

        // Add the selected day and time to your schedule data list
        List<String> scheduleData = getData(selectedDay, timeIn);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Adapter adapter = new Adapter(scheduleData);
        recyclerView.setAdapter(adapter);
    }

    // Method to provide schedule data including the selected day and time
    private List<String> getData(String selectedDay, String timeIn) {
        List<String> data = new ArrayList<>();
        // Add the selected day and time to the schedule data list
        data.add(selectedDay + " | " + timeIn);
        // Add other schedule items if needed
        return data;
    }
}