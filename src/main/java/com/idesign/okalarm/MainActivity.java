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
import android.view.View;
import android.widget.Toast;

import com.idesign.okalarm.Interfaces.AlarmItemListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnAlarmSet, AlarmItemListener {

  AlarmFragment newFragment;
  FloatingActionButton fab;

  private RecyclerView recyclerView;
  private FormattedTimesAdapter adapter;

  private AlarmManager alarmManager;
  Intent intent;
  PendingIntent pendingIntent;

  private List<Long> times;
  private List<FormattedTime> formattedTimes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    fab = findViewById(R.id.main_fab);

    times = new ArrayList<>();
    formattedTimes = new ArrayList<>();
    adapter = new FormattedTimesAdapter(formattedTimes, MainActivity.this);
    recyclerView = findViewById(R.id.main_recycler_view);

    DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(itemDecoration);
    recyclerView.setAdapter(adapter);

    fab.setOnClickListener(l -> attachAlarmFragment());
    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
  }

  public void attachAlarmFragment() {
    if (newFragment != null && !isAttached()) {
      return;
    }
    newFragment = AlarmFragment.newInstance();
    getSupportFragmentManager().beginTransaction()
    .replace(R.id.main_frame_layout, newFragment).commit();
    recyclerView.setVisibility(View.GONE);
  }

  public void removeAlarmFragment() {
    getSupportFragmentManager().beginTransaction()
    .detach(newFragment).commit();
    recyclerView.setVisibility(View.VISIBLE);
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
      FormattedTime formattedTime = new FormattedTime(calendar.getTimeInMillis(), hour, min, am_pm, true);
      addNewItem(formattedTime);
      setIntents(formattedTime);
      if (alarmManager != null) {
        setAlarm(calendar.getTimeInMillis(), pendingIntent);
      }
    }
    removeAlarmFragment();
  }

  public void setAlarm(long milliseconds, PendingIntent pendingIntent) {
    alarmManager.set(AlarmManager.RTC_WAKEUP, milliseconds, pendingIntent);

  }

  /*==================================*
   * AlarmListener from AlarmFragment *
   *==================================*/
  public void addNewItem(FormattedTime formattedTime) {
    formattedTimes.add(formattedTime);
    formattedTimes.sort((a, b) -> {
      if (a.get_rawTime() < b.get_rawTime()) {
        return -1;
      }
      if (a.get_rawTime() > b.get_rawTime()) {
        return 1;
      }
      if (a.get_rawTime() == b.get_rawTime()) {
        return 0;
      }
      return 0;
    });
    adapter.notifyDataSetChanged();
  }

  public void onCancel() {
    silenceAlarm();
    removeAlarmFragment();
  }

  /*=======================*
   *  Set new Alarm intent *
   *=======================*/
  public void setIntents(FormattedTime formattedTime) {
    intent = new Intent(MainActivity.this, AlarmReceiver.class);
    pendingIntent = PendingIntent.getBroadcast(this, ((int)formattedTime.get_rawTime()), intent, 0);
  }


  public void silenceAlarm() {
    if (AlarmReceiver.getRingtone() != null) {
      AlarmReceiver.getRingtone().stop();
    }
  }

  /*================================================*
   *  From ItemAdapter single item action interface *
   *================================================*/
  public void onDelete(final int position) {
    silenceAlarm();
    FormattedTime formattedTime = formattedTimes.get(position);
    long rawTime = formattedTime.get_rawTime();
    int idx = times.indexOf(rawTime);
    setIntents(formattedTime);
    if (alarmManager != null) {
      alarmManager.cancel(pendingIntent);
    }
    if (idx > -1) {
      times.remove(rawTime);
    }
  }

  public void onToggle(boolean isToggled, int position) {
    if (!isToggled) {
      silenceAlarm();
      FormattedTime formattedTime = formattedTimes.get(position);
      setIntents(formattedTime);
      if (alarmManager != null) {
        alarmManager.cancel(pendingIntent);
      }
    } else {
      FormattedTime formattedTime = formattedTimes.get(position);
      setIntents(formattedTime);
      setAlarm(((int)formattedTime.get_rawTime()), pendingIntent);
    }
  }

  /*=====================*
   *  Utility functions  *
   *=====================*/
  public void showToast(CharSequence message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  public void logMessage(String message) {
    Log.d("Main", message);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }
}
