package com.example.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText alertTitle, Timein, Timeout, Day;
    private NumberPicker numberPicker;
    private RadioGroup radioGroup;
    private RadioButton radioMinutes, radioDays, radioHours;
    private Button alertButton;
    private Calendar calendar;

    public static final String CHANNEL_ID = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        alertTitle = findViewById(R.id.alertTitle);
        Timein = findViewById(R.id.Timein);
        Timeout = findViewById(R.id.Timeout);
        Day = findViewById(R.id.Day);
        numberPicker = findViewById(R.id.numberPicker);
        radioGroup = findViewById(R.id.radioGroup);
        radioMinutes = findViewById(R.id.radioMinutes);
        radioDays = findViewById(R.id.radioDays);
        radioHours = findViewById(R.id.radioHours);
        alertButton = findViewById(R.id.alertbutton);

        // Initialize Calendar instance
        calendar = Calendar.getInstance();

        // Set up NumberPicker
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);


        // Set up "ADD SCHEDULE" button click listener
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReminder();
            }
        });

        // Set up Time picker dialog for Timein EditText
        Timein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(Timein);
            }
        });

        // Set up Time picker dialog for Timeout EditText
        Timeout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(Timeout);
            }
        });


    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showTimePickerDialog(final EditText editText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        // Format the selected time
                        String formattedTime = String.format("%02d:%02d %s", hourOfDay % 12 == 0 ? 12 : hourOfDay % 12, minute, hourOfDay < 12 ? "AM" : "PM");

                        // Set the formatted time to the EditText
                        editText.setText(formattedTime);

                        // Update the calendar object with the selected time
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void setReminder() {
        String title = alertTitle.getText().toString();
        String selectedDay = Day.getText().toString();
        String timeIn = Timein.getText().toString();

        // Check for empty fields
        if (title.isEmpty() || selectedDay.isEmpty() || timeIn.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int reminderInterval = numberPicker.getValue();
        String timeUnit = "";

        // Determine the selected time unit
        if (radioMinutes.isChecked()) {
            timeUnit = "minutes";
        } else if (radioDays.isChecked()) {
            timeUnit = "days";
        } else if (radioHours.isChecked()) {
            timeUnit = "hours";
        }

        // Convert reminder interval to milliseconds
        long reminderIntervalMillis = getMillisecondsForTimeUnit(reminderInterval, timeUnit);

        // Calculate alarm time based on selected day, time in, and reminder interval
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTime(calendar.getTime());

        // Set the alarm time to the selected time in EditText
        int hourOfDay = Integer.parseInt(timeIn.substring(0, 2));
        int minute = Integer.parseInt(timeIn.substring(3, 5));
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);

        // Adjust alarm time based on reminder interval
        switch (timeUnit) {
            case "minutes":
                alarmTime.add(Calendar.MINUTE, -reminderInterval);
                break;
            case "hours":
                alarmTime.add(Calendar.HOUR_OF_DAY, -reminderInterval);
                break;
            case "days":
                alarmTime.add(Calendar.DAY_OF_MONTH, -reminderInterval);
                break;
        }

        // Set the alarm for the specified day
        int selectedDayOfWeek = getDayOfWeek(selectedDay);
        int currentDayOfWeek = alarmTime.get(Calendar.DAY_OF_WEEK);
        if (selectedDayOfWeek != -1) {
            // Adjust the alarm time if necessary to match the selected day
            int daysToAdd = (selectedDayOfWeek - currentDayOfWeek + 7) % 7;
            alarmTime.add(Calendar.DAY_OF_WEEK, daysToAdd);
        }

        // Create the intent for the notification
        Intent notificationIntent = new Intent(this, AlarmReceiver.class);
        notificationIntent.putExtra("title", "You have class in " + title);
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the alarm for the reminder time
        setAlarm(alarmTime.getTimeInMillis(), contentIntent);

        // Notify the user about when the alarm will be displayed
        Toast.makeText(this, "Alarm in:" + alarmTime.getTime(), Toast.LENGTH_SHORT).show();

        // Create an intent to start the SchedList activity
        Intent intent = new Intent(MainActivity.this, SchedList.class);
        // Pass the selected day and time to the SchedList activity
        intent.putExtra("selectedDay", selectedDay);
        intent.putExtra("timeIn", timeIn);
        // Start the SchedList activity
        startActivity(intent);
    }

    private int getDayOfWeek(String selectedDay) {
        switch (selectedDay.toLowerCase()) {
            case "monday":
                return Calendar.MONDAY;
            case "tuesday":
                return Calendar.TUESDAY;
            case "wednesday":
                return Calendar.WEDNESDAY;
            case "thursday":
                return Calendar.THURSDAY;
            case "friday":
                return Calendar.FRIDAY;
            case "saturday":
                return Calendar.SATURDAY;
            case "sunday":
                return Calendar.SUNDAY;
            default:
                return -1;
        }
    }

    private void setAlarm(long alarmTimeMillis, PendingIntent contentIntent) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to trigger at the reminder time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, contentIntent);
    }

    private long getMillisecondsForTimeUnit(int interval, String timeUnit) {
        switch (timeUnit) {
            case "minutes":
                return interval * 60 * 1000;
            case "hours":
                return interval * 60 * 60 * 1000;
            case "days":
                return interval * 24 * 60 * 60 * 1000;
            default:
                return 0;
        }
    }
}
