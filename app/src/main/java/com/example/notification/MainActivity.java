package com.example.notification;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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

    private EditText alertTitle, DueDate;
    private NumberPicker numberPicker;
    private RadioGroup radioGroup;
    private RadioButton radioMinutes, radioDays, radioHours;
    private Button alertButton;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        alertTitle = findViewById(R.id.alertTitle);
        DueDate = findViewById(R.id.DueDate);
        numberPicker = findViewById(R.id.numberPicker);
        radioMinutes = findViewById(R.id.radioMinutes);
        radioDays = findViewById(R.id.radioDays);
        radioHours = findViewById(R.id.radioHours);
        alertButton = findViewById(R.id.alertbutton);

        // Initialize Calendar instance
        calendar = Calendar.getInstance();

        // Set up NumberPicker
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);

        // Set up Date picker dialog
        DueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReminder();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "This is the default channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        showTimePickerDialog();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        // Update DueDate EditText with selected date and time
                        updateDueDateEditText();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDueDateEditText() {
        // Format the selected date and time
        String formattedDate = android.text.format.DateFormat.format("dd-MM-yyyy hh:mm a", calendar).toString();
        // Set the formatted date and time to the DueDate EditText
        DueDate.setText(formattedDate);
    }

    private void setReminder() {
        String title = alertTitle.getText().toString();
        // Get the selected date and time from the calendar
        long dueDateTimeMillis = calendar.getTimeInMillis();
        // Get reminder interval value from number picker
        int reminderInterval = numberPicker.getValue();
        // Get the selected time unit for the reminder interval
        String timeUnit = "";
        if (radioMinutes.isChecked()) {
            timeUnit = "minutes";
        } else if (radioDays.isChecked()) {
            timeUnit = "days";
        } else if (radioHours.isChecked()) {
            timeUnit = "hours";
        }

        // Subtract reminder interval from due date/time
        long reminderTimeMillis = dueDateTimeMillis - getMillisecondsForTimeUnit(reminderInterval, timeUnit);

        // Check if reminder time is in the past
        if (reminderTimeMillis <= System.currentTimeMillis()) {
            // If it's in the past, set it to the current time + reminder interval
            reminderTimeMillis = System.currentTimeMillis() + getMillisecondsForTimeUnit(reminderInterval, timeUnit);
        }


        // Create notification intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Set sound and vibration for the notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {0, 1000, 1000}; // Vibrate pattern: vibrate for 1 second, pause for 1 second, vibrate for 1 second

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.notif) // Replace "notif.png" with the actual name of your icon
                .setContentTitle("Reminder: " + title)
                .setContentText("Due Date: " + DueDate.getText().toString())
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(pattern)
                .setContentIntent(contentIntent);

        /// Show notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

        // Set the alarm
        setAlarm(reminderTimeMillis, title);

        Toast.makeText(this, "Reminder set for " + reminderInterval + " " + timeUnit + " before " + DueDate.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    private void setAlarm(long alarmTimeMillis, String title) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("title", title);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
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
