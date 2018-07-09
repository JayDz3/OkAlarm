package com.idesign.okalarm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlarmManager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Toast;

import com.idesign.okalarm.Interfaces.AlarmItemListener;
import com.idesign.okalarm.ViewModels.FormattedTimesViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnAlarmSet,
AlarmItemListener,
PuzzleFragment.OnPuzzleListener {

  AlarmFragment newFragment;
  PuzzleFragment puzzleFragment;
  FloatingActionButton fab;

  private RecyclerView recyclerView;
  private FormattedTimesAdapter adapter;

  private AlarmManager alarmManager;
  private List<Ringtone> mRingtones;
  Ringtone activeRingtone;

  Intent intent;
  PendingIntent pendingIntent;

  AlarmReceiver mAlarmReceiver;

  private List<Long> times;
  private List<FormattedTime> formattedTimes;
  private FormattedTimesViewModel viewModel;

  public static final String EXTRA_FRAGMENT_INT = "extra.fragment.integer";
  public static final String EXTRA_HOUR = "extra.hour";
  public static final String EXTRA_MINUTE = "extra.minute";
  public static final String EXTRA_AM_PM = "extra.ampm";
  public static final String EXTRA_RAW_TIME = "extra.rawtime";
  public static final String EXTRA_RINGTONE_TITLE = "ringtone.title";

  private int mFragment_int = -1;
  private int _hour;
  private int _minute;
  private String _am_pm;
  private long _rawtime;
  boolean _isCorrect = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent getIntent = getIntent();
    recyclerView = findViewById(R.id.main_recycler_view);

    fab = findViewById(R.id.main_fab);
    fab.setOnClickListener(l -> setFragment());

    formattedTimes = new ArrayList<>();
    times = new ArrayList<>();
    mRingtones = new ArrayList<>();

    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

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

    if (savedInstanceState != null) {
      getValuesFromBundle(savedInstanceState);
      if (alarmIsActive()) {
        goToPuzzleFragment();
        return;
      }
      toggleView();
    } else {
      StatusBarNotification[] notifications = getNotifications();
      if (notifications.length > 0) {
        showToast("Active notifications");
      }
      String _message = (String) getMessageText(getIntent);
      String answer = "saturday";
      boolean doReturn = screenMessage(_message, answer);
      if (!doReturn) {
        toggleView();
      }
    }
    disableNotificationService();
  }

  private CharSequence getMessageText(Intent intent) {
    String KEY_TEXT_REPLY = "key.text.reply";
    if (intent.getExtras() != null) {
      if (intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE) != null) {
        String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
        int rawTime = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);

        if (ringtoneTitle.equalsIgnoreCase(Constants.NO_RINGTONE)) {
          Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
          activeRingtone = RingtoneManager.getRingtone(this, alarmUri);
        } else {
          activeRingtone = findRingtoneByTitle(ringtoneTitle);
        }
      }
    }

    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
    if (remoteInput != null) {
       return remoteInput.getCharSequence(KEY_TEXT_REPLY);
    }
    return null;
  }

  public boolean screenMessage(String message, String answer) {
    if (message != null) {
      if (message.equalsIgnoreCase(answer)) {
        _isCorrect = true;
        showToast("Correct!");
      } else {
        showToast("Sorry, wrong day");
      }
      broadcastCloseNotificationTray();
      goToPuzzleFragment();
      return true;
    }
    return false;
  }

  public void broadcastCloseNotificationTray() {
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    StatusBarNotification[] notifications = getNotifications();
    if (notificationManager != null) {
      for (StatusBarNotification notification : notifications) {
        if (notification.getNotification().getChannelId().equalsIgnoreCase(Constants.NOTIFICATION_CHANNEL_ID)) {
           notificationManager.cancel(notification.getId());
        }
      }
    }
    Intent closeIntent = new Intent(Constants.ACTION_CLOSE_DIALOGS);
    sendBroadcast(closeIntent);
  }

  public StatusBarNotification[] getNotifications() {
    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager != null) {
      return notificationManager.getActiveNotifications();
    } else {
      return new StatusBarNotification[0];
    }
  }

  public void disableNotificationService() {
    ComponentName receiver = new ComponentName(this, AlarmNotification.class);
    PackageManager packageManager = this.getPackageManager();
    packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
  }

  public void showToast(CharSequence message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
    activeRingtone.play();
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
    getSupportFragmentManager().beginTransaction()
    .replace(R.id.main_frame_layout, fragment).commit();
  }

  public void goToPuzzleFragment() {
    Calendar now = Calendar.getInstance();
    String am_pm_string = now.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
    mFragment_int = 1;
    setFragment(now.get(Calendar.HOUR), now.get(Calendar.MINUTE), am_pm_string, now.getTimeInMillis());
  }

  public void showRecyclerView() {
    mFragment_int = - 1;
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
    getSupportFragmentManager().beginTransaction()
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
      times.add(calendar.getTimeInMillis());
      int baseHour = am_pm.equals("AM") ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);
      int hour = getHourByInteger(baseHour);
      int _monthInt = calendar.get(Calendar.MONTH);
      String _month = getMonth(_monthInt);
      String _date = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
      String _combined = _month + " " + _date;
      String _title = ringtone == null ? null : ringtone.getTitle(this);

      FormattedTime formattedTime = new FormattedTime(calendar.getTimeInMillis(), hour, calendar.get(Calendar.MINUTE), am_pm, _combined, true, false, _title);
      _rawtime = formattedTime.get_rawTime();
      addNewItem(formattedTime);
      startIntent(formattedTime);
      if (alarmManager != null) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, formattedTime.get_rawTime(), pendingIntent);
      }
      showRecyclerView();
    }
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
    int time = (int) formattedTime.get_rawTime();
    intent = new Intent(this, AlarmReceiver.class);
    intent.putExtra(Constants.EXTRA_RINGTONE_TITLE, formattedTime.get_title());
    intent.putExtra(Constants.EXTRA_RAW_TIME, time);
    intent.putExtra(Constants.BOOT_TAG, Constants.ALARM_CLASS_TAG);
    intent.setAction(Constants.ACTION_MANAGE_ALARM);
    pendingIntent = PendingIntent.getBroadcast(this, time, intent, 0);
  }

  public Ringtone findRingtoneByTitle(String title) {
    Ringtone ringtone = null;
    boolean isFound = false;
    for (Ringtone tone : mRingtones) {
      if (!isFound && tone.getTitle(this).equals(title)) {
        isFound = true;
        ringtone = tone;
      }
    }
    return ringtone;
  }

  public void cancelIntent(FormattedTime formattedTime) {
    int time = (int) formattedTime.get_rawTime();
    intent = new Intent(this, AlarmReceiver.class);
    intent.putExtra(Constants.EXTRA_RINGTONE_TITLE, formattedTime.get_title());
    intent.putExtra(Constants.EXTRA_RAW_TIME, time);
    intent.putExtra(Constants.BOOT_TAG, Constants.ALARM_CLASS_TAG);
    intent.setAction(Constants.ACTION_MANAGE_ALARM);
    pendingIntent = PendingIntent.getBroadcast(this, time, intent, 0);
    if (alarmManager != null) {
      alarmManager.cancel(pendingIntent);
    }
  }

  public void silenceAlarm() {
    if (activeRingtone != null && activeRingtone.isPlaying()) {
      activeRingtone.stop();
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
    } else {
      startIntent(formattedTime);
      alarmManager.set(AlarmManager.RTC_WAKEUP, formattedTime.get_rawTime(), pendingIntent);
    }
  }

  public boolean alarmIsActive() {
    return activeRingtone != null && activeRingtone.isPlaying();
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

  /*======================================*
   *  From Intent Service                 *
   *  Handles Intent from Alarm Service   *
   *======================================*/
  private BroadcastReceiver myReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
      if (resultCode == RESULT_OK) {
        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_HANDLE_INTENT)) {
          String bootTag = intent.getStringExtra(Constants.BOOT_TAG);
          String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
          int time = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);

          if (ringtoneTitle.equalsIgnoreCase(Constants.NO_RINGTONE)) {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            activeRingtone = RingtoneManager.getRingtone(context, alarmUri);
          } else {
            activeRingtone = findRingtoneByTitle(ringtoneTitle);
          }
          goToPuzzleFragment();
        }
      }
    }
  };

  @Override
  protected void onStart() {
    super.onStart();
    mAlarmReceiver = new AlarmReceiver(1);
    IntentFilter filter = new IntentFilter(Constants.ACTION_HANDLE_INTENT);
    LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, filter);
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onStop() {
    super.onStop();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    String ringtoneTitle = activeRingtone == null ? null : activeRingtone.getTitle(this);
    if (activeRingtone != null) {
      activeRingtone.stop();
      activeRingtone = null;
    }
    outState.putInt(EXTRA_FRAGMENT_INT, mFragment_int);
    outState.putInt(EXTRA_HOUR, _hour);
    outState.putInt(EXTRA_MINUTE, _minute);
    outState.putString(EXTRA_AM_PM, _am_pm);
    outState.putLong(EXTRA_RAW_TIME, _rawtime);
    outState.putString(EXTRA_RINGTONE_TITLE, ringtoneTitle);
    super.onSaveInstanceState(outState);
  }

  public void getValuesFromBundle(Bundle savedInstanceState) {
    if (savedInstanceState.keySet().contains(EXTRA_FRAGMENT_INT)) {
      mFragment_int = savedInstanceState.getInt(EXTRA_FRAGMENT_INT);
      _hour = savedInstanceState.getInt(EXTRA_HOUR);
      _minute = savedInstanceState.getInt(EXTRA_MINUTE);
      _am_pm = savedInstanceState.getString(EXTRA_AM_PM);
      _rawtime = savedInstanceState.getLong(EXTRA_RAW_TIME);
      String ringtoneTitle = savedInstanceState.getString(EXTRA_RINGTONE_TITLE);
      if (ringtoneTitle != null) {
        activeRingtone = findRingtoneByTitle(ringtoneTitle);
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (puzzleFragment != null && puzzleFragment.isVisible()) {
      showRecyclerView();
      return;
    }
    if (mFragment_int == 0) {
      showRecyclerView();
      silenceAlarm();
      return;
    }
    super.onBackPressed();
  }
}