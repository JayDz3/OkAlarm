package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;

public class AlarmReceiver extends BroadcastReceiver {

  private static int isActivated = 0;
  private static int isRegistered = 0;
  public AlarmReceiver() {}

  public AlarmReceiver(int setToOne) {
    isActivated = setToOne;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getExtras() != null) {
      if (isActivated == 0) {
        if (isRegistered == 0) {
          registerNotificationReceiver(context);
        }
        Intent broadcastIntent = new Intent();
        String finalTitle;
        String itemUri = intent.getStringExtra(Constants.EXTRA_URI);
        if (intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE) == null) {
          finalTitle = Constants.NO_RINGTONE;
        } else {
          finalTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
        }
        broadcastIntent.setAction(Constants.ACTION_HANDLE_NOTIFICATION);
        broadcastIntent.putExtra(Constants.BOOT_TAG, Constants.NOTIFICATION_CLASS_TAG);
        broadcastIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, finalTitle);
        broadcastIntent.putExtra(Constants.EXTRA_URI, itemUri);
        broadcastIntent.putExtra(Constants.EXTRA_RAW_TIME, intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0));
        broadCastNotification(context, broadcastIntent);
        isRegistered = 1;
        return;
      }
      if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_MANAGE_ALARM)) {
        String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
        String itemUri = intent.getStringExtra(Constants.EXTRA_URI);
        int rawTime = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);
        if (ringtoneTitle == null) {
          ringtoneTitle = Constants.NO_RINGTONE;
        }
        Intent sendIntent = new Intent(Constants.ACTION_RECEIVE_ALARM);
        sendIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, ringtoneTitle);
        sendIntent.putExtra(Constants.EXTRA_RAW_TIME, rawTime);
        sendIntent.putExtra(Constants.EXTRA_URI, itemUri);
        sendIntent.putExtra(Constants.BOOT_TAG, Constants.RECEIVE_ALARM_TAG);

        startRingtoneService(context, itemUri);
        startUiService(context, sendIntent);
      }
    }
  }

  public void startRingtoneService(Context context, String itemUri) {
    Intent ringtoneIntent = new Intent(context, RingtoneService.class);
    ringtoneIntent.putExtra(Constants.EXTRA_URI, itemUri);
    context.startService(ringtoneIntent);
  }

   public void startUiService(Context context, Intent inboundIntent) {
    Intent i = new Intent(context, HandleBroadcastIntentService.class);
    i.putExtra(Constants.BOOT_TAG, Constants.RECEIVE_ALARM_TAG);
    i.putExtra(Constants.EXTRA_RINGTONE_TITLE, inboundIntent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE));
    i.putExtra(Constants.EXTRA_URI, inboundIntent.getStringExtra(Constants.EXTRA_URI));
    i.putExtra(Constants.EXTRA_RAW_TIME, inboundIntent.getIntExtra(Constants.EXTRA_RAW_TIME, 0));
    context.startService(i);
  }

  private void registerNotificationReceiver(Context context) {
    AlarmNotification alarmNotification = new AlarmNotification();
    IntentFilter intentFilter = new IntentFilter(Constants.ACTION_HANDLE_NOTIFICATION);
    context.getApplicationContext().registerReceiver(alarmNotification, intentFilter);
  }

  private void broadCastNotification(Context context, Intent intent) {
    context.getApplicationContext().sendBroadcast(intent);
  }

}