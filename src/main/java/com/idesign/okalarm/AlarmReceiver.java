package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

  private static int isActivated = 0;

  public AlarmReceiver() {}

  public AlarmReceiver(int setToOne) {
    isActivated = setToOne;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getExtras() != null) {
      if (isActivated == 0) {
        Intent broadcastIntent = new Intent();
        String finalTitle;
        if (intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE) == null) {
          finalTitle = Constants.NO_RINGTONE;
        } else {
          finalTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
        }
        broadcastIntent.putExtra(Constants.BOOT_TAG, Constants.NOTIFICATION_CLASS_TAG);
        broadcastIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, finalTitle);
        broadcastIntent.putExtra(Constants.EXTRA_RAW_TIME, intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0));
        broadCastNotification(context, broadcastIntent);
        isActivated = 1;
        return;
      }
      if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_MANAGE_ALARM)) {
        String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
        int rawTime = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);
        if (ringtoneTitle == null) {
          ringtoneTitle = Constants.NO_RINGTONE;
        }
        Intent sendIntent = new Intent(Constants.ACTION_RECEIVE_ALARM);
        sendIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, ringtoneTitle);
        sendIntent.putExtra(Constants.EXTRA_RAW_TIME, rawTime);
        sendIntent.putExtra(Constants.BOOT_TAG, Constants.RECEIVE_ALARM_TAG);
        onStartService(context, sendIntent);
      }
    }
  }

   public void onStartService(Context context, Intent inboundIntent) {
    Intent i = new Intent(context, HandleBroadcastIntentService.class);
    i.putExtra(Constants.BOOT_TAG, Constants.RECEIVE_ALARM_TAG);
    i.putExtra(Constants.EXTRA_RINGTONE_TITLE, inboundIntent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE));
    i.putExtra(Constants.EXTRA_RAW_TIME, inboundIntent.getIntExtra(Constants.EXTRA_RAW_TIME, 0));
    context.startService(i);
  }

  private void broadCastNotification(Context context, Intent intent) {
    AlarmNotification mNotification = new AlarmNotification();
    mNotification.createNotificationChannel(context, intent);
  }

}