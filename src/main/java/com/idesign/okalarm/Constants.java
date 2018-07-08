package com.idesign.okalarm;

import android.app.PendingIntent;
import android.content.Intent;

public class Constants {
  public static final String NOTIFICATION_CLASS_TAG = "MyAlarmNotification";
  public static final String ALARM_CLASS_TAG = "Alarm Receiver";
  public static final String RECEIVE_ALARM_TAG = "Receive Alarm";

  public static final String NOTIFICATION_DESCRIPTION = "alert alarms";
  public static final String NOTIFICATION_CHANNEL_ID = "101";
  public static final int NOTIFICATION_ALARM_REQUEST_CODE = 101;
  public static final String NOTIFICATION_KEY_TEXT_REPLY = "key.text.reply";

  public static final String ALARM_CHANNEL_ID = "102";
  public static final int ALARM_REQUEST_CODE = 102;

  public static final String BOOT_TAG = "boot.tag";

  // Intent Flag integers //
  public static final int FLAG_NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK;
  public static final int FLAG_CLEAR_TASK = Intent.FLAG_ACTIVITY_CLEAR_TASK;

  // Pending intent Flag Integers //
  public static final int FLAG_UPDATE_CURRENT = PendingIntent.FLAG_UPDATE_CURRENT;

  // Action Strings //
  public static final String ACTION_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;
  public static final String ACTION_CLOSE_DIALOGS = Intent.ACTION_CLOSE_SYSTEM_DIALOGS;
  public static final String ACTION_MANAGE_ALARM = "com.idesign.okalarm.ManageAlarm";
  public static final String ACTION_RECEIVE_ALARM = "com.idesign.okalarm.ReceiveAlarm";
  public static final String ACTION_HANDLE_INTENT = "com.idesign.okalarm.HandleIntent";

  public static final String EXTRA_RINGTONE_TITLE = "ringtone.title";
  public static final String EXTRA_RAW_TIME = "raw.time";

  public static final String NO_RINGTONE = "None";

}
