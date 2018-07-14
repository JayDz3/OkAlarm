package com.idesign.okalarm;

import android.app.AlarmManager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import android.service.notification.StatusBarNotification;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import com.idesign.okalarm.Interfaces.ActiveAlarmsFragmentListener;
import com.idesign.okalarm.ViewModels.ActiveAlarmsViewModel;
import com.idesign.okalarm.ViewModels.RingtonesViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnAlarmSet,
PuzzleFragment.OnPuzzleListener,
ActiveAlarmsFragmentListener {

  ActiveAlarmsFragment activeAlarmsFragment;
  AlarmFragment addAlarmFragment;
  PuzzleFragment puzzleFragment;
  EmptyListFragment mEmptyListFragment;
  FloatingActionButton fab;

  IntentManager intentManager;
  Intent intent;
  PendingIntent pendingIntent;

  private RingtoneManager ringtoneManager;
  private AlarmManager alarmManager;

  private ActiveAlarmsViewModel model;
  private RingtonesViewModel ringtonesViewModel;
  private int mFragment_int = -1;

  public static final String EXTRA_FRAGMENT_INT = "extra.fragment.integer";

  AudioManager audioManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    model = ViewModelProviders.of(this).get(ActiveAlarmsViewModel.class);
    ringtonesViewModel = ViewModelProviders.of(this).get(RingtonesViewModel.class);
    fab = findViewById(R.id.main_fab);
    fab.setOnClickListener(l -> setFragment());

    List<Ringtone> mRingtones = new ArrayList<>();

    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    ringtoneManager = new RingtoneManager(MainActivity.this);
    Cursor cursor = ringtoneManager.getCursor();

    Intent getIntent = getIntent();

    if (savedInstanceState != null) {
      onInstanceStateNotNull(savedInstanceState);
    } else {
      while (cursor.moveToNext()) {
        int currentPos = cursor.getPosition();
        Ringtone ringtone = ringtoneManager.getRingtone(currentPos);
        mRingtones.add(ringtone);
      }
      ringtonesViewModel.setRingtones(mRingtones);

      List<StatusBarNotification> appNotifications = myNotifications();
      boolean hasPendingNotifications = appNotifications.size() > 0;

      // Started from icon and no logged messages
      if (getIntent.getStringExtra(Constants.BOOT_TAG) == null && !hasPendingNotifications) {
        disableNotificationService();
        toggleEmptyListFragment();
        return;
      }

      // Started from icon but has logged messages
      if (getIntent.getStringExtra(Constants.BOOT_TAG) == null && hasPendingNotifications) {
        Bundle _coldNotificationBundle = appNotifications.get(0).getNotification().extras;
        String _coldUri = _coldNotificationBundle.getString(Constants.EXTRA_URI);
        int volume = _coldNotificationBundle.getInt(Constants.EXTRA_VOLUME);
        onActiveAlarm(appNotifications, _coldUri, volume);
        return;
      }

      // Started from notification tray
      if (getIntent.getStringExtra(Constants.BOOT_TAG) != null) {
        String _message = (String) getMessageText(getIntent);
        String answer = getNameOfDay();
        String itemUri = getIntent.getStringExtra(Constants.EXTRA_URI);
        int volume = getIntent.getIntExtra(Constants.EXTRA_VOLUME, 0);
        String result = _message.equalsIgnoreCase(answer) ? "Correct!" : "Sorry, wrong day";
        showToast(result);
        onActiveAlarm(appNotifications, itemUri, volume);
      }
    }
  }

  /*=============================*
   *  ID is for JobService only  *
   *=============================*/
  public void onActiveAlarm(List<StatusBarNotification> notifications, String uri, int volume) {
    clearNotifications(notifications);
    closeNotificationTray();
    startRingtoneService(this, uri, volume);
    goToPuzzleFragment();
    disableNotificationService();
  }

  public void disableNotificationService() {
    ComponentName receiver = new ComponentName(this, NotificationService.class);
    PackageManager packageManager = this.getPackageManager();
    packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
  }

  public void onInstanceStateNotNull(Bundle inState) {
      getValuesFromBundle(inState);
      if (mFragment_int == -1) {
        fab.setVisibility(View.VISIBLE);
        toggleEmptyListFragment();
      } else {
        fab.setVisibility(View.GONE);
      }
  }

  public String getNameOfDay() {
    Calendar calendar = Calendar.getInstance();
    Date date = calendar.getTime();
    return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
  }

  public void clearNotifications(List<StatusBarNotification> notifications) {
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager != null) {
      notifications.forEach(i -> notificationManager.cancel(i.getId()));
    }
  }

  private CharSequence getMessageText(Intent intent) {
    String KEY_TEXT_REPLY = "key.text.reply";
    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
    if (remoteInput != null) {
      return remoteInput.getCharSequence(KEY_TEXT_REPLY);
    } else {
      return "";
    }
  }

  public void closeNotificationTray() {
    Intent closeIntent = new Intent(Constants.ACTION_CLOSE_DIALOGS);
    sendBroadcast(closeIntent);
  }

  public List<StatusBarNotification> myNotifications() {
    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    List<StatusBarNotification> appNotifications = new ArrayList<>();
    if (notificationManager == null) {
      return appNotifications;
    }

    StatusBarNotification[] all = notificationManager.getActiveNotifications();
    for (StatusBarNotification notification : all) {
      if (notification.getNotification().getChannelId().equalsIgnoreCase(Constants.NOTIFICATION_CHANNEL_ID)) {
        appNotifications.add(notification);
      }
    }
    return appNotifications;
  }

  public void toggleEmptyListFragment() {
    if (model.getItems().getValue() != null && model.getItems().getValue().size() == 0) {
      attachEmptyListFragment();
    } else {
      attachActiveAlarmsFragment();
    }
  }

  /*=======================================*
   *  Add AlarmFragment to set new alarm   *
   *=======================================*/
  public void attachEmptyListFragment() {
    mFragment_int = -1;
    if (mEmptyListFragment != null && mEmptyListFragment.isVisible()) {
      return;
    }
    mEmptyListFragment = EmptyListFragment.newInstance();
    getSupportFragmentManager().beginTransaction()
    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    .replace(R.id.main_frame_layout, mEmptyListFragment).commit();
    fab.setVisibility(View.VISIBLE);
  }

  public void attachActiveAlarmsFragment() {
    mFragment_int = -1;
    if (activeAlarmsFragment != null && activeAlarmsFragment.isVisible()) {
      return;
    }
    activeAlarmsFragment = ActiveAlarmsFragment.newInstance();
    replaceFragment(activeAlarmsFragment);
    fab.setVisibility(View.VISIBLE);
  }

  public void setFragment() {
    mFragment_int = 0;
    fab.setVisibility(View.GONE);
    if (addAlarmFragment != null && addAlarmFragment.isVisible()) {
      return;
    }
    addAlarmFragment = AlarmFragment.newInstance();
    replaceFragment(addAlarmFragment);
  }

  /*===============================================*
   *  Alarm has gone off, attach Puzzle Fragment   *
   *===============================================*/

  public void replaceFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
    .replace(R.id.main_frame_layout, fragment).commit();
  }

  public void goToPuzzleFragment() {
    mFragment_int = 1;
    if (puzzleFragment != null && puzzleFragment.isVisible()) {
      return;
    }

    Calendar now = Calendar.getInstance();
    String am_pm_string = now.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
    fab.setVisibility(View.GONE);
    puzzleFragment = PuzzleFragment.newInstance(now.get(Calendar.HOUR), now.get(Calendar.MINUTE), am_pm_string);
    replaceFragment(puzzleFragment);
  }

  /*===========================*
   *  AlarmFragment Interface  *
   *===========================*/
  public void onSet(int hourOfDay, int minute, int volume, String am_pm, final int position) {
    Calendar calendar = Calendar.getInstance();
    setCalendarValues(calendar, hourOfDay, minute);
    long calMilliseconds = calendar.getTimeInMillis();

    Calendar today = Calendar.getInstance();
    if (am_pm.equals("AM") && today.get(Calendar.AM_PM) != Calendar.AM || today.getTimeInMillis() > calMilliseconds) {
      calMilliseconds += (1000 * 60 * 60 * 24);
      calendar.setTimeInMillis(calMilliseconds);
    }

    int baseHour = am_pm.equals("AM") ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);
    int hour = getHourByInteger(baseHour);
    int _monthInt = calendar.get(Calendar.MONTH);
    String _month = getMonth(_monthInt);
    String _date = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    String _combined = _month + " " + _date;

    Uri itemUri;
    String _title;

    if (position == -1) {
      itemUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
      _title = null;
    } else {
      _title = ringtonesViewModel.selectedRingtone(position).getTitle(this);
      itemUri = ringtoneManager.getRingtoneUri(position);
    }

    ActiveAlarm activeAlarm = new ActiveAlarm(calendar.getTimeInMillis(), hour, calendar.get(Calendar.MINUTE), volume, am_pm, _combined, true, false, _title, itemUri.toString());
    addNewItem(activeAlarm);
    startIntent(activeAlarm);
    attachActiveAlarmsFragment();
  }

  public void setCalendarValues(Calendar calendar, int hourOfDay, int minute) {
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  public int getHourByInteger(int hourOfDay) {
    return hourOfDay == 0 ? 12 : hourOfDay;
  }

  /*================================================*
   *  From AlarmFragment: cancel setting new alarm  *
   *================================================*/

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

  public void addNewItem(ActiveAlarm activeAlarm) {
    model.getItems().getValue().add(activeAlarm);
    sortActiveAlarms();
  }

  public void sortActiveAlarms() {
    model.getItems().getValue().sort((a, b) -> Long.compare(a.get_rawTime(), b.get_rawTime()));
  }

  /*=======================*
   *  Set new Alarm intent *
   *=======================*/
  public void startIntent(ActiveAlarm activeAlarm) {
    populateAlarmIntent(activeAlarm);
    alarmManager.set(AlarmManager.RTC_WAKEUP, activeAlarm.get_rawTime(), pendingIntent);
  }

  public void cancelIntent(ActiveAlarm activeAlarm) {
    populateAlarmIntent(activeAlarm);
    alarmManager.cancel(pendingIntent);
  }

  public void populateAlarmIntent(ActiveAlarm activeAlarm) {
    int time = (int) activeAlarm.get_rawTime();
    intent = new Intent(this, IntentManager.class);
    intent.putExtra(Constants.EXTRA_RINGTONE_TITLE, activeAlarm.get_title());
    intent.putExtra(Constants.EXTRA_RAW_TIME, time);
    intent.putExtra(Constants.BOOT_TAG, Constants.ALARM_CLASS_TAG);
    intent.putExtra(Constants.EXTRA_URI, activeAlarm.get_itemUri());
    intent.putExtra(Constants.EXTRA_VOLUME, activeAlarm.get_volume());
    intent.setAction(Constants.ACTION_MANAGE_ALARM);
    pendingIntent = PendingIntent.getBroadcast(this, time, intent, 0);
  }

  /*================================================*
   *  From ItemAdapter single item actions          *
   *  AlarmItemListener Interface                   *
   *================================================*/
  public void onDeleteAlarm(final int position, long rawTime) {
    stopRingtoneService();
    ActiveAlarm activeAlarm = model.selectedAlarm(position);
    cancelIntent(activeAlarm);
    model.removeAlarm(position);
    toggleEmptyListFragment();
  }

  public void onToggleAlarm(boolean isToggled, final int position) {
    ActiveAlarm alarm = model.selectedAlarm(position);
    if (!isToggled) {
      stopRingtoneService();
      cancelIntent(alarm);
    } else {
      startIntent(alarm);
    }
  }

  /*==========================*
   *   From Puzzle Fragment   *
   *==========================*/

  public void onAnswer(int answer) {
    toggleEmptyListFragment();
    stopRingtoneService();
  }

  public void onAnswer(String answer) {
    toggleEmptyListFragment();
    stopRingtoneService();
  }

  public void onCancel() {
    stopRingtoneService();
    sortActiveAlarms();
    toggleEmptyListFragment();
  }

  /*======================================*
   *  From Intent Service                 *
   *  Handles Intent from Alarm Service   *
   *======================================*/
  private BroadcastReceiver myReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
      if (resultCode == RESULT_OK && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_HANDLE_INTENT)) {
        goToPuzzleFragment();
      }
    }
  };

  public void startRingtoneService(Context context, String itemUri, int volume) {
    Intent ringtoneIntent = new Intent(context, RingtoneService.class);
    ringtoneIntent.putExtra(Constants.EXTRA_URI, itemUri);
    ringtoneIntent.putExtra(Constants.EXTRA_VOLUME, volume);
    context.startService(ringtoneIntent);
  }

  public void stopRingtoneService() {
    Intent i = new Intent(this, RingtoneService.class);
    stopService(i);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    intentManager = new IntentManager(1);
    IntentFilter filter = new IntentFilter(Constants.ACTION_HANDLE_INTENT);
    LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, filter);
  }

  @Override
  protected void onPause() {
    super.onPause();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt(EXTRA_FRAGMENT_INT, mFragment_int);
    super.onSaveInstanceState(outState);
  }

  public void getValuesFromBundle(Bundle savedInstanceState) {
    if (savedInstanceState.keySet().contains(EXTRA_FRAGMENT_INT)) {
      mFragment_int = savedInstanceState.getInt(EXTRA_FRAGMENT_INT);
    }
  }

  @Override
  public void onBackPressed() {
    if (mFragment_int == 1) {
      toggleEmptyListFragment();
      return;
    }
    if (mFragment_int == 0) {
      toggleEmptyListFragment();
      stopRingtoneService();
      return;
    }
    super.onBackPressed();
  }

  public void showToast(CharSequence message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}