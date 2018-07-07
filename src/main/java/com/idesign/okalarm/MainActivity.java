package com.idesign.okalarm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlarmManager;

import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewPropertyAnimator;

import com.idesign.okalarm.Interfaces.AlarmItemListener;
import com.idesign.okalarm.Interfaces.OnAlarmRing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnAlarmSet, AlarmItemListener, OnAlarmRing, PuzzleFragment.OnPuzzleListener {

  FragmentManager mFragmentManager;
  AlarmFragment newFragment;
  PuzzleFragment puzzleFragment;
  FloatingActionButton fab;

  private RecyclerView recyclerView;
  private FormattedTimesAdapter adapter;

  private AlarmManager alarmManager;
  private List<Ringtone> mRingtones;
  Ringtone mRingtone;
  Intent intent;
  PendingIntent pendingIntent;

  private AlarmReceiver mAlarmReceiver;

  private List<Long> times;
  private List<FormattedTime> formattedTimes;
  private FormattedTimesViewModel viewModel;

  public static final String EXTRA_FRAGMENT_INT = "extra.fragment.integer";
  public static final String EXTRA_HOUR = "extra.hour";
  public static final String EXTRA_MINUTE = "extra.minute";
  public static final String EXTRA_AM_PM = "extra.ampm";
  public static final String EXTRA_RAW_TIME = "extra.rawtime";
  private int mFragment_int = -1;
  private int _hour;
  private int _minute;
  private String _am_pm;
  private long _rawtime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    recyclerView = findViewById(R.id.main_recycler_view);
    fab = findViewById(R.id.main_fab);
    fab.setOnClickListener(l -> setFragment());

    formattedTimes = new ArrayList<>();
    times = new ArrayList<>();
    mRingtones = new ArrayList<>();

    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    mFragmentManager = getSupportFragmentManager();

    adapter = new FormattedTimesAdapter(formattedTimes, MainActivity.this);
    DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(itemDecoration);
    recyclerView.setAdapter(adapter);

    RingtoneManager ringtoneManager = new RingtoneManager(MainActivity.this);
    Cursor cursor = ringtoneManager.getCursor();

    while (cursor.moveToNext()) {
      int currentPos = cursor.getPosition();
      mRingtones.add(ringtoneManager.getRingtone(currentPos));
    }

    viewModel = ViewModelProviders.of(this).get(FormattedTimesViewModel.class);
    final Observer<List<FormattedTime>> itemObserver = items -> MainActivity.this.setViewModel(adapter, items);
    viewModel.getItems().observe(this, itemObserver);
    mAlarmReceiver = new AlarmReceiver(MainActivity.this);

    if (savedInstanceState != null) {
      getValuesFromBundle(savedInstanceState);
    }
    if (alarmIsActive()) {
      goToPuzzleFragment();
      return;
    }
    toggleView();
  }

  public void toggleView() {
    switch (mFragment_int) {
      case -1:
        showRecyclerView();
        break;
      case 0:
        setFragment();
        break;
      case 1:
        setFragment(_hour, _minute, _am_pm, _rawtime);
        break;
      default:
        break;
    }
  }

  public void setViewModel(FormattedTimesAdapter adapter, List<FormattedTime> newTimes) {
    if (newTimes != null) {
      formattedTimes = newTimes;
      adapter.setList(formattedTimes);
    }
  }
  /*=======================================*
   *  Add AlarmFragment to set new alarm   *
   *=======================================*/
  public void setFragment() {
    mFragment_int = 0;
    if (newFragment != null && newFragment.isVisible()) {
      return;
    }
    animator(recyclerView, 0.0f, 300)
    .setListener(listenerAdapter());
  }

  /*===============================================*
   *  Alarm has gone off, attach Puzzle Fragment   *
   *===============================================*/
  public void setFragment(int hourOfDay, int minute, String am_pm, long millis) {
    if (puzzleFragment != null && puzzleFragment.isVisible()) {
      return;
    }
    animator(recyclerView, 0.0f, 300)
    .setListener(listenerAdapter(hourOfDay, minute, am_pm, millis));
  }

  public ViewPropertyAnimator animator(View view, float alphaTime, int duration) {
    return view.animate()
    .alpha(alphaTime)
    .setDuration(duration);
  }

  /*=======================*
   *  For puzzle fragment  *
   *=======================*/
  public AnimatorListenerAdapter listenerAdapter(int hourOfDay, int minute, String am_pm, long millis) {
    return new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        recyclerView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        puzzleFragment = PuzzleFragment.newInstance(hourOfDay, minute, am_pm);
        _hour = hourOfDay;
        _minute = minute;
        _am_pm = am_pm;
        _rawtime = millis;
        attachFragment(puzzleFragment);
      }
    };
  }

  /*======================*
   *  for Alarm Fragment  *
   *======================*/
  public AnimatorListenerAdapter listenerAdapter() {
    return new AnimatorListenerAdapter() {
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        newFragment = AlarmFragment.newInstance();
        recyclerView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        attachFragment(newFragment);
      }
    };
  }

  public void attachFragment(Fragment fragment) {
    mFragmentManager.beginTransaction()
    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    .replace(R.id.main_frame_layout, fragment).commit();
  }

  public void goToPuzzleFragment() {
    Calendar now = Calendar.getInstance();
    String am_pm_string = now.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
    setFragment(now.get(Calendar.HOUR), now.get(Calendar.MINUTE), am_pm_string, now.getTimeInMillis());
  }

  public void showRecyclerView() {
    mFragment_int = -1;
    if (newFragment != null && newFragment.isVisible()) {
      detachFragment(newFragment);
      return;
    }
    if (puzzleFragment != null && puzzleFragment.isVisible()) {
      detachFragment(puzzleFragment);
      return;
    }
    fab.setVisibility(View.VISIBLE);
  }

  public void detachFragment(Fragment fragment) {
    mFragmentManager.beginTransaction()
    .detach(fragment).commit();
    animator(recyclerView, 1.0f, 300)
    .setListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        recyclerView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
      }
    });
  }

  /*===========================*
   *  AlarmFragment Interface  *
   *===========================*/
  public void onSet(int hourOfDay, int minute, String am_pm, Ringtone ringtone) {
    Calendar calendar = Calendar.getInstance();
    setCalendarValues(calendar, hourOfDay, minute);
    Calendar today = Calendar.getInstance();
    long calMilliseconds = calendar.getTimeInMillis();

    if (am_pm.equals("AM") && today.get(Calendar.AM_PM) != Calendar.AM || today.getTimeInMillis() > calMilliseconds) {
      calMilliseconds += (1000 * 60 * 60 * 24);
      calendar.setTimeInMillis(calMilliseconds);
    }

    if (!times.contains(calendar.getTimeInMillis())) {
      mRingtone = ringtone;
      times.add(calendar.getTimeInMillis());
      int baseHour = am_pm.equals("AM") ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);
      int hour = getHourByInteger(baseHour);
      int _monthInt = calendar.get(Calendar.MONTH);
      String _month = getMonth(_monthInt);
      String _date = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
      String _combined = _month + " " + _date;
      String _title = mRingtone == null ? null : mRingtone.getTitle(this);

      FormattedTime formattedTime = new FormattedTime(calendar.getTimeInMillis(), hour, calendar.get(Calendar.MINUTE), am_pm, _combined, true, false, _title);
      _rawtime = formattedTime.get_rawTime();
      addNewItem(formattedTime);
      startIntent(formattedTime);
      if (alarmManager != null) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, formattedTime.get_rawTime(), pendingIntent);
      }
    }
    showRecyclerView();
  }

  public void setCalendarValues(Calendar calendar, int hourOfDay, int minute) {
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  public int getHourByInteger(int hourOfDay) {
    if (hourOfDay == 0) {
      return 12;
    } else {
      return hourOfDay;
    }
  }

  /*================================================*
   *  From AlarmFragment: cancel setting new alarm  *
   *================================================*/
  public void onCancel() {
    silenceAlarm();
    showRecyclerView();
  }
  // END AlarmFragment Interface //

  /*=============================================*
   *  Return string representation of month int  *
   *=============================================*/
  public String getMonth(int _month) {
    switch (_month) {
      case 0:
        return "Jan";
      case 1:
        return "Feb";
      case 2:
        return "Mar";
      case 3:
        return "Apr";
      case 4:
        return "May";
      case 5:
        return "Jun";
      case 6:
        return "Jul";
      case 7:
        return "Aug";
      case 8:
        return "Sep";
      case 9:
        return "Oct";
      case 10:
        return "Nov";
      case 11:
        return "Dec";
      default:
        return "None";
    }
  }

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
    viewModel.setFormattedTimes(formattedTimes);
  }

  /*=======================*
   *  Set new Alarm intent *
   *=======================*/
  public void startIntent(FormattedTime formattedTime) {
    mAlarmReceiver.setRingtone(this, mRingtone);
    int time = (int) formattedTime.get_rawTime();
    intent = new Intent(this, AlarmReceiver.class);
    pendingIntent = PendingIntent.getBroadcast(this, time, intent, 0);
  }

  public Ringtone findRingtoneByTitle(FormattedTime formattedTime) {
   Ringtone ringtone = null;
   boolean isFound = false;
   for (Ringtone tone : mRingtones) {
     if (!isFound && tone.getTitle(this).equals(formattedTime.get_title())) {
       isFound = true;
       ringtone = tone;
     }
   }
   return ringtone;
  }

  public void cancelIntent(FormattedTime formattedTime) {
    int time = (int) formattedTime.get_rawTime();
    intent = new Intent(this, AlarmReceiver.class);
    pendingIntent = PendingIntent.getBroadcast(this, time, intent, 0);
  }


  public void silenceAlarm() {
    if (mAlarmReceiver.getRingtone() != null) {
      mAlarmReceiver.getRingtone().stop();
    }
  }

  /*================================================*
   *  From ItemAdapter single item actions          *
   *  AlarmItemListener Interface                   *
   *================================================*/
  public void onDeleteAlarm(final int position, long rawTime) {
    silenceAlarm();
    times.remove(rawTime);
    FormattedTime formattedTime = formattedTimes.get(position);
    cancelIntent(formattedTime);
    if (alarmManager != null) {
      alarmManager.cancel(pendingIntent);
    }
    viewModel.setFormattedTimes(formattedTimes);
  }

  public void onReady() {
    if (newFragment == null) {
      return;
    }
    newFragment.setRingtones(mRingtones);
  }

  public void onToggleAlarm(boolean isToggled, FormattedTime formattedTime) {
    if (!isToggled) {
      silenceAlarm();
      cancelIntent(formattedTime);
      if (alarmManager != null) {
        alarmManager.cancel(pendingIntent);
      }
    } else {
      startIntent(formattedTime);
      alarmManager.set(AlarmManager.RTC_WAKEUP, formattedTime.get_rawTime(), pendingIntent);
    }
  }

  public boolean alarmIsActive() {
    return mAlarmReceiver.getRingtone() != null && mAlarmReceiver.getRingtone().isPlaying();
  }

  /*=========================*
   * From Broadcast Receiver *
   *=========================*/
  public void onRing(Ringtone ringtone) {
    boolean isFound = false;
    if (formattedTimes == null || formattedTimes.size() == 0) {
      setDefaultAlarm();
    } else {
      for (FormattedTime formattedTime : formattedTimes) {
        if (!isFound && formattedTime.getIsActive() && !formattedTime.getHasPlayed()) {
          isFound = true;
          screenTitleForNullValue(formattedTime);
          formattedTime.set_hasPlayed(true);
        }
      }
    }
    mAlarmReceiver.getRingtone().play();
    goToPuzzleFragment();
  }

  public void screenTitleForNullValue(FormattedTime formattedTime) {
    if (formattedTime.get_title() == null) {
      setDefaultAlarm();
    } else {
      mAlarmReceiver.setRingtone(MainActivity.this, findRingtoneByTitle(formattedTime));
    }
  }

  public void setDefaultAlarm() {
    mAlarmReceiver.setRingtone(MainActivity.this,null);
  }

  /*==========================*
   *   From Puzzle Fragment   *
   *==========================*/

  public void onAnswer(int answer) {
    silenceAlarm();
    showRecyclerView();
  }

  public void onAnswer(String answer) {
    silenceAlarm();
    showRecyclerView();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt(EXTRA_FRAGMENT_INT, mFragment_int);
    outState.putInt(EXTRA_HOUR, _hour);
    outState.putInt(EXTRA_MINUTE, _minute);
    outState.putString(EXTRA_AM_PM, _am_pm);
    outState.putLong(EXTRA_RAW_TIME, _rawtime);
    super.onSaveInstanceState(outState);
  }

  public void getValuesFromBundle(Bundle savedInstanceState) {
    if (savedInstanceState.keySet().contains(EXTRA_FRAGMENT_INT)) {
      mFragment_int = savedInstanceState.getInt(EXTRA_FRAGMENT_INT);
      _hour = savedInstanceState.getInt(EXTRA_HOUR);
      _minute = savedInstanceState.getInt(EXTRA_MINUTE);
      _am_pm = savedInstanceState.getString(EXTRA_AM_PM);
      _rawtime = savedInstanceState.getLong(EXTRA_RAW_TIME);
    }
  }

  @Override
  public void onBackPressed() {
    if (puzzleFragment != null && puzzleFragment.isVisible()) {
      showRecyclerView();
      return;
    }
    super.onBackPressed();
  }
}
