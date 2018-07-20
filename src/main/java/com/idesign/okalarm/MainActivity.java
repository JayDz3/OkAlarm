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

import android.media.RingtoneManager;

import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.idesign.okalarm.Factory.SystemAlarm;
import com.idesign.okalarm.Fragments.ActiveAlarmsFragment;
import com.idesign.okalarm.Fragments.AlarmFragment;
import com.idesign.okalarm.Fragments.EmptyListFragment;
import com.idesign.okalarm.Factory.ActiveAlarm;
import com.idesign.okalarm.Factory.AlarmIntentFactory;
import com.idesign.okalarm.Fragments.PuzzleFragment;
import com.idesign.okalarm.Interfaces.ActiveAlarmsFragmentListener;
import com.idesign.okalarm.ViewModels.ActiveAlarmsViewModel;
import com.idesign.okalarm.ViewModels.SystemAlarmsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements AlarmFragment.OnAlarmSet,
PuzzleFragment.OnPuzzleListener,
ActiveAlarmsFragmentListener {

  private ActiveAlarmsFragment activeAlarmsFragment;
  private AlarmFragment addAlarmFragment;
  private PuzzleFragment puzzleFragment;
  private EmptyListFragment mEmptyListFragment;
  private FloatingActionButton fab;

  IntentManager intentManager;

  private PendingIntent pendingIntent;

  private RingtoneManager ringtoneManager;
  private AlarmManager alarmManager;
  private AlarmIntentFactory alarmIntentFactory;

  private ActiveAlarmsViewModel activeAlarmsViewModel;

  private SystemAlarmsViewModel systemAlarmsViewModel;

  private int mFragment_int = -1;

  private List<SystemAlarm> mSystemAlarms;

  private Disposable observable;
  private Cursor mCursor;

  // private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  public static final String EXTRA_FRAGMENT_INT = "extra.fragment.integer";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSystemAlarms = new ArrayList<>();
    ringtoneManager = new RingtoneManager(MainActivity.this);
    mCursor = ringtoneManager.getCursor();

    activeAlarmsViewModel = ViewModelProviders.of(this).get(ActiveAlarmsViewModel.class);

    systemAlarmsViewModel = ViewModelProviders.of(this).get(SystemAlarmsViewModel.class);
    systemAlarmsViewModel.getItems().observe(this, items -> mSystemAlarms = items);

    fab = findViewById(R.id.main_fab);
    fab.setOnClickListener(l -> setFragment());

    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmIntentFactory = new AlarmIntentFactory();
    Intent getIntent = getIntent();

    observeToCursor();

    if (savedInstanceState != null) {
      onInstanceStateNotNull(savedInstanceState);
    } else {
      onInstanceStateIsNull(getIntent);
    }
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

  public void onInstanceStateIsNull(Intent getIntent) {
    final List<StatusBarNotification> notificationList = myNotifications();
    final boolean hasPendingNotifications = notificationList.size() > 0;

    /*
     *  Started from notification tray
     */
    if (getIntent.getStringExtra(Constants.BOOT_TAG) != null) {
      final String message = (String) getMessageText(getIntent);
      final String answer = getNameOfDay();
      final String itemUri = getIntent.getStringExtra(Constants.EXTRA_URI);
      final int volume = getIntent.getIntExtra(Constants.EXTRA_VOLUME, 0);
      final String result = message.equalsIgnoreCase(answer) ? "Correct!" : "Sorry, wrong day";
      toast(result);
      onActiveAlarm(notificationList, itemUri, volume);

    } else if (getIntent.getStringExtra(Constants.BOOT_TAG) == null && hasPendingNotifications) {

      /*
       *  Started from icon but has logged messages
       */
      final Bundle _coldNotificationBundle = notificationList.get(0).getNotification().extras;
      final String itemUri = _coldNotificationBundle.getString(Constants.EXTRA_URI);
      final int volume = _coldNotificationBundle.getInt(Constants.EXTRA_VOLUME);
      onActiveAlarm(notificationList, itemUri, volume);

    } else if (getIntent.getStringExtra(Constants.BOOT_TAG) == null && !hasPendingNotifications) {

      /*
       *  Started from icon and no logged messages
       */
      disableNotificationService();
      toggleEmptyListFragment();
    }
  }

  public void observeToCursor() {
    final List<SystemAlarm> alarms = new ArrayList<>();
    /*=========================================================================*
     *  Working on background thread not throwing error on orientation change  *
     *=========================================================================*/
    if (observable == null || observable.isDisposed()) {
      observable = Observable.just(mCursor)
      .observeOn(Schedulers.io())
      .map((c) -> {

        while (c.moveToNext()) {
          final int pos = c.getPosition();
          SystemAlarm systemAlarm = new SystemAlarm(ringtoneManager.getRingtone(pos).getTitle(getBaseContext()), ringtoneManager.getRingtoneUri(pos).toString());
          alarms.add(systemAlarm);
        }
        return alarms;
      })
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(result -> systemAlarmsViewModel.setSystemAlarms(alarms),
       e -> toast("error: " + e.getMessage()),
      () -> Log.d("MAIN ACTIVITY", "SUBSCRIPTION IS COMPLETE"));
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

  public final List<StatusBarNotification> myNotifications() {
    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    List<StatusBarNotification> appNotifications = new ArrayList<>();
    if (notificationManager == null) {
      return appNotifications;
    }

    for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
      if (notification.getNotification().getChannelId().equalsIgnoreCase(Constants.NOTIFICATION_CHANNEL_ID)) {
        appNotifications.add(notification);
      }
    }
    return appNotifications;
  }

  public void toggleEmptyListFragment() {
    if (activeAlarmsViewModel.getItems().getValue() != null && activeAlarmsViewModel.getItems().getValue().size() == 0) {
      attachEmptyListFragment();
    } else {
      attachActiveAlarmsFragment();
    }
  }

  public void goToPuzzleFragment() {
    mFragment_int = 1;
    if (puzzleFragment != null && puzzleFragment.isVisible()) {
      return;
    }
    if (puzzleFragment == null) {
      puzzleFragment = PuzzleFragment.newInstance();
    }
    fab.setVisibility(View.GONE);
    replaceFragment(puzzleFragment);
  }

  /*=======================================*
   *  Add AlarmFragment to set new alarm   *
   *=======================================*/
  public void attachEmptyListFragment() {
    mFragment_int = -1;
    if (mEmptyListFragment != null && mEmptyListFragment.isVisible()) {
      return;
    }
    if (mEmptyListFragment == null) {
      mEmptyListFragment = EmptyListFragment.newInstance();
    }
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
    if (activeAlarmsFragment == null) {
      activeAlarmsFragment = ActiveAlarmsFragment.newInstance();
    }
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

  /*===========================*
   *  AlarmFragment Interface  *
   *===========================*/
  public void onSet(int hourOfDay, int minute, int volume, String am_pm, final int position) {
    final String itemUri;
    final String _title;
    if (position == -1) {
      itemUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM).toString();
      _title = null;
    } else {
      _title = mSystemAlarms.get(position).get_title();
      itemUri = mSystemAlarms.get(position).get_itemUri();
    }

    final ActiveAlarm activeAlarm = alarmIntentFactory.activeAlarm(hourOfDay, minute, volume, am_pm,  _title, itemUri);
    activeAlarmsViewModel.addActiveAlarm(activeAlarm);
    sortActiveAlarms();
    startIntent(activeAlarm);
    attachActiveAlarmsFragment();
  }

  public void sortActiveAlarms() {
    if (activeAlarmsViewModel.getItems().getValue() != null) {
      activeAlarmsViewModel.getItems().getValue().sort(Comparator.comparingLong(ActiveAlarm::get_rawTime));
    }
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
    final int time = (int) activeAlarm.get_rawTime();
    final Intent intent = new Intent(this, IntentManager.class);
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
    final ActiveAlarm activeAlarm = activeAlarmsViewModel.selectedAlarm(position);
    cancelIntent(activeAlarm);
    activeAlarmsViewModel.removeAlarm(position);
    toggleEmptyListFragment();
  }

  public void onToggleAlarm(boolean isToggled, final int position) {
    ActiveAlarm alarm = activeAlarmsViewModel.selectedAlarm(position);
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
      final int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
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
    if (observable != null && !observable.isDisposed()) {
      observable.dispose();
    }
    mCursor.close();
    observable = null;
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

  final Handler mHandler = new Handler();

  void toast(String message) {
    mHandler.post(() -> Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show());
  }
}