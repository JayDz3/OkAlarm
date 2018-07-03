package com.idesign.okalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.idesign.okalarm.Interfaces.AlarmItemListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnAlarmSet, AlarmItemListener {

  AlarmFragment newFragment;
  ToggleButton toggleButton;
  FloatingActionButton fab;

  private RecyclerView recyclerView;
  private FormattedTimesAdapter adapter;

  private AlarmManager alarmManager;
  private Intent intent;
  private PendingIntent pendingIntent;

  private List<Long> times;
  private List<FormattedTime> formattedTimes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toggleButton = findViewById(R.id.main_toggle);
    fab = findViewById(R.id.main_fab);

    times = new ArrayList<>();
    formattedTimes = new ArrayList<>();
    adapter = new FormattedTimesAdapter(formattedTimes, MainActivity.this);
    recyclerView = findViewById(R.id.main_recycler_view);

    DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(itemDecoration);
    recyclerView.setAdapter(adapter);

    toggleButton.setOnClickListener(l -> onToggle());
    fab.setOnClickListener(l -> attachAlarmFragment());
    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
  }

  public void attachAlarmFragment() {
    if (newFragment != null && !isAttached()) {
      return;
    }
    showToast("attaching");
    newFragment = AlarmFragment.newInstance();
    getSupportFragmentManager().beginTransaction()
    .replace(R.id.main_frame_layout, newFragment).commit();
  }

  public boolean isAttached() {
    return newFragment.isDetached();
  }

  public void onSet(int hourOfDay, int minute, String am_pm) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    if (!times.contains(calendar.getTimeInMillis())) {
      times.add(calendar.getTimeInMillis());
      int hour = calendar.get(Calendar.HOUR);
      int min = calendar.get(Calendar.MINUTE);
      addNewItem(hour, min);
      intent = new Intent(MainActivity.this, AlarmReceiver.class);
      pendingIntent = PendingIntent.getBroadcast(this, 100, intent, 0);
      if (alarmManager != null) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
      }
    }
  }

  public void addNewItem(int hour, int min) {
    FormattedTime formattedTime = new FormattedTime(hour, min, true);
    formattedTimes.add(formattedTime);
    adapter.notifyItemInserted(formattedTimes.size());
  }

  public void logMessage(String message) {
    Log.d("Main", message);
  }

  public void onCancel() {
    if (AlarmReceiver.getRingtone() != null) {
      AlarmReceiver.getRingtone().stop();
    }
    intent = new Intent(MainActivity.this, AlarmReceiver.class);
    pendingIntent = PendingIntent.getBroadcast(this, 100, intent, 0);
    if (alarmManager != null) {
      alarmManager.cancel(pendingIntent);
    }
  }

  public void onToggle() {
    Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    logMessage(pendingIntent.toString());
    pendingIntent.cancel();
  }

  public void onDelete(final int position) {

  }

  public void onToggle(boolean isToggled, int position) {
    if (!isToggled) {
      logMessage("not toggled: " + isToggled);
    } else {
      logMessage("toggled: " + isToggled);
    }
  }

  public void showToast(CharSequence message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }
}
