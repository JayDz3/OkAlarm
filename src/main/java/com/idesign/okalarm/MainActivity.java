package com.idesign.okalarm;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlarmManager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.idesign.okalarm.ViewModels.ActiveAlarmsViewModel;
import com.idesign.okalarm.ViewModels.RingtonesViewModel;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnAlarmSet,
PuzzleFragment.OnPuzzleListener,
ActiveAlarmsFragment.ActiveAlarmFragmentListener {

  ActiveAlarmsFragment activeAlarmsFragment;
  AlarmFragment addAlarmFragment;
  PuzzleFragment puzzleFragment;
  EmptyListFragment mEmptyListFragment;
  FloatingActionButton fab;

  private AlarmManager alarmManager;
  private List<Ringtone> mRingtones;
  Ringtone activeRingtone;

  Intent intent;
  PendingIntent pendingIntent;

  AlarmReceiver mAlarmReceiver;

  private List<Long> times;

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
  private Uri _activeUri;

  private List<Uri> uris;
  RingtoneManager ringtoneManager;

  private ActiveAlarmsViewModel model;
  private RingtonesViewModel ringtonesViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    model = ViewModelProviders.of(this).get(ActiveAlarmsViewModel.class);
    ringtonesViewModel = ViewModelProviders.of(this).get(RingtonesViewModel.class);

    fab = findViewById(R.id.main_fab);
    fab.setOnClickListener(l -> setFragment());

    times = new ArrayList<>();
    mRingtones = new ArrayList<>();
    uris = new ArrayList<>();
    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


    ringtoneManager = new RingtoneManager(MainActivity.this);
    Cursor cursor = ringtoneManager.getCursor();

    Intent getIntent = getIntent();

    if (savedInstanceState != null) {
      onInstanceStateNotNull(savedInstanceState);
    } else {
      while (cursor.moveToNext()) {
        int currentPos = cursor.getPosition();
        Uri uri = ringtoneManager.getRingtoneUri(currentPos);
        Ringtone ringtone = ringtoneManager.getRingtone(currentPos);
        mRingtones.add(ringtone);
        uris.add(uri);
      }
      ringtonesViewModel.setRingtones(mRingtones);

      StatusBarNotification[] notifications = getNotifications();
      List<StatusBarNotification> appNotifications = myNotifications(notifications);
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
        _activeUri = Uri.parse(_coldUri);
        showToast(_coldUri);
        onActiveAlarm(appNotifications, _coldUri);
        return;
      }

      // Started from notification tray
      if (getIntent.getStringExtra(Constants.BOOT_TAG) != null) {
        showToast(getIntent.getStringExtra(Constants.BOOT_TAG));
        String _message = (String) getMessageText(getIntent);
        String itemUri = getIntent.getStringExtra(Constants.EXTRA_URI);
        showToast(itemUri);
        _activeUri = Uri.parse(itemUri);
        String answer = getNameOfDay();
        _isCorrect = _message.equalsIgnoreCase(answer);
        if (_isCorrect) {
          showToast("Correct!");
        } else {
          showToast("Sorry, wrong day");
        }
        onActiveAlarm(appNotifications, itemUri);
      }
    }
  }

  public void onActiveAlarm(List<StatusBarNotification> clearNotifications, String uri) {
    broadcastCloseNotificationTray(clearNotifications);
    startRingtoneService(this, uri);
    goToPuzzleFragment();
    disableNotificationService();
  }

  public void onInstanceStateNotNull(Bundle inState) {
      getValuesFromBundle(inState);
      if (mFragment_int == -1) {
        fab.setVisibility(View.VISIBLE);
        toggleEmptyListFragment();
      } else {
        fab.setVisibility(View.GONE);
      }
      // Removing stopRingtoneService from onDestroy fixes this issue
      if (_activeUri != null && mFragment_int == 1) {
       // startRingtoneService(this, _activeUri.toString());
      //  goToPuzzleFragment();
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
      for (StatusBarNotification notification : notifications) {
        notificationManager.cancel(notification.getId());
      }
    }
  }

  private CharSequence getMessageText(Intent intent) {
    String KEY_TEXT_REPLY = "key.text.reply";
    if (intent.getExtras() != null && intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE) != null) {
      String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
      int rawTime = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);
      String itemUri = intent.getStringExtra(Constants.EXTRA_URI);
      activeRingtone = RingtoneManager.getRingtone(this, Uri.parse(itemUri));
    }

    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
    if (remoteInput != null) {
      return remoteInput.getCharSequence(KEY_TEXT_REPLY);
    }
    return null;
  }

  public Uri getUri(Ringtone ringtone) {
    int idx = mRingtones.indexOf(ringtone);
    return uris.get(idx);
  }

  public void broadcastCloseNotificationTray(List<StatusBarNotification> target) {
    clearNotifications(target);
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

  public List<StatusBarNotification> myNotifications(StatusBarNotification[] allNotifications) {
    List<StatusBarNotification> appNotifications = new ArrayList<>();
    for (StatusBarNotification notification : allNotifications) {
      if (notification.getNotification().getChannelId().equalsIgnoreCase(Constants.NOTIFICATION_CHANNEL_ID)) {
        appNotifications.add(notification);
      }
    }
    return appNotifications;
  }

  public void disableNotificationService() {
    ComponentName receiver = new ComponentName(this, AlarmNotification.class);
    PackageManager packageManager = this.getPackageManager();
    packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
  }

  public void showToast(CharSequence message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
    getSupportFragmentManager().beginTransaction()
    .replace(R.id.main_frame_layout, activeAlarmsFragment).commit();
    fab.setVisibility(View.VISIBLE);
  }

  public void setFragment() {
    mFragment_int = 0;
    fab.setVisibility(View.GONE);
    if (addAlarmFragment != null && addAlarmFragment.isVisible()) {
      return;
    }
    addAlarmFragment = AlarmFragment.newInstance();

    attachFragment(addAlarmFragment);
  }

  /*===============================================*
   *  Alarm has gone off, attach Puzzle Fragment   *
   *===============================================*/
  public void setFragment(int hourOfDay, int minute, String am_pm, long millis) {
    // activeRingtone.play();
    fab.setVisibility(View.GONE);
    if (puzzleFragment != null && puzzleFragment.isVisible()) {
      return;
    }
    puzzleFragment = PuzzleFragment.newInstance(hourOfDay, minute, am_pm);
    _hour = hourOfDay;
    _minute = minute;
    _am_pm = am_pm;
    _rawtime = millis;
    attachFragment(puzzleFragment);
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

  public void detachFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
    .detach(fragment).commit();
    fab.setVisibility(View.VISIBLE);
  }

  /*===========================*
   *  AlarmFragment Interface  *
   *===========================*/
  public void onSet(int hourOfDay, int minute, String am_pm, final int position) {
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

      Ringtone ringtone = position == -1 ? null : ringtonesViewModel.selectedRingtone(position);
      String _title = ringtone == null ? null : ringtone.getTitle(this);
      Uri itemUri;
      if (ringtone == null){
        itemUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
      } else {
       itemUri = ringtoneManager.getRingtoneUri(position);
      }

      ActiveAlarm activeAlarm = new ActiveAlarm(calendar.getTimeInMillis(), hour, calendar.get(Calendar.MINUTE), am_pm, _combined, true, false, _title, itemUri.toString());
      _rawtime = activeAlarm.get_rawTime();
      addNewItem(activeAlarm);
      startIntent(activeAlarm);
      attachActiveAlarmsFragment();
    }
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
  public void onCancel() {
    stopRingtoneService();
    toggleEmptyListFragment();
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

  public void addNewItem(ActiveAlarm activeAlarm) {
    List<ActiveAlarm> target = model.getItems().getValue();
    target.add(activeAlarm);
    model.setActiveAlarms(sorted(target));
  }

  public List<ActiveAlarm> sorted(List<ActiveAlarm> source) {
    source.sort((a, b) -> {
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
    return source;
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
    intent = new Intent(this, AlarmReceiver.class);
    intent.putExtra(Constants.EXTRA_RINGTONE_TITLE, activeAlarm.get_title());
    intent.putExtra(Constants.EXTRA_RAW_TIME, time);
    intent.putExtra(Constants.BOOT_TAG, Constants.ALARM_CLASS_TAG);
    intent.putExtra(Constants.EXTRA_URI, activeAlarm.get_itemUri());
    intent.setAction(Constants.ACTION_MANAGE_ALARM);
    pendingIntent = PendingIntent.getBroadcast(this, time, intent, 0);
  }

  public void stopRingtoneService() {
    Intent i = new Intent(this, RingtoneService.class);
    this.stopService(i);
  }

  /*================================================*
   *  From ItemAdapter single item actions          *
   *  AlarmItemListener Interface                   *
   *================================================*/
  public void onDeleteAlarm(final int position, long rawTime) {
    stopRingtoneService();
    times.remove(rawTime);
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

  public boolean alarmIsActive(Uri activeUri) {
    if (activeUri != null) {
      Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(activeUri.toString()));
      return ringtone.isPlaying();
    }
    return false;
  }


  /*==========================*
   *   From Puzzle Fragment   *
   *==========================*/

  public void onAnswer(int answer) {
    stopRingtoneService();
    toggleEmptyListFragment();
  }

  public void onAnswer(String answer) {
    stopRingtoneService();
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
      if (resultCode == RESULT_OK) {
        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_HANDLE_INTENT)) {
          String bootTag = intent.getStringExtra(Constants.BOOT_TAG);
          String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
          String itemUri = intent.getStringExtra(Constants.EXTRA_URI);
          int time = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);
          activeRingtone = RingtoneManager.getRingtone(context, Uri.parse(itemUri));
          _activeUri = Uri.parse(itemUri);
          goToPuzzleFragment();
        }
      }
    }
  };

  public void startRingtoneService(Context context, String itemUri) {
    Intent ringtoneIntent = new Intent(context, RingtoneService.class);
    ringtoneIntent.putExtra(Constants.EXTRA_URI, itemUri);
    context.startService(ringtoneIntent);
  }

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
    // stopRingtoneService();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    String ringtoneTitle = activeRingtone == null ? null : activeRingtone.getTitle(this);
    String itemUri = _activeUri == null ? null : _activeUri.toString();
    outState.putInt(EXTRA_FRAGMENT_INT, mFragment_int);
    outState.putInt(EXTRA_HOUR, _hour);
    outState.putInt(EXTRA_MINUTE, _minute);
    outState.putString(EXTRA_AM_PM, _am_pm);
    outState.putLong(EXTRA_RAW_TIME, _rawtime);
    outState.putString(EXTRA_RINGTONE_TITLE, ringtoneTitle);
    outState.putString(Constants.EXTRA_URI, itemUri);
    super.onSaveInstanceState(outState);
  }

  public void getValuesFromBundle(Bundle savedInstanceState) {
    if (savedInstanceState.keySet().contains(EXTRA_FRAGMENT_INT)) {
      mFragment_int = savedInstanceState.getInt(EXTRA_FRAGMENT_INT);
      _hour = savedInstanceState.getInt(EXTRA_HOUR);
      _minute = savedInstanceState.getInt(EXTRA_MINUTE);
      _am_pm = savedInstanceState.getString(EXTRA_AM_PM);
      _rawtime = savedInstanceState.getLong(EXTRA_RAW_TIME);
      String itemUri = savedInstanceState.getString(Constants.EXTRA_URI);
      String ringTonetitle = savedInstanceState.getString(EXTRA_RINGTONE_TITLE);
      _activeUri = itemUri == null ? null : Uri.parse(itemUri);
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
}