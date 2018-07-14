package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class IntentManager extends BroadcastReceiver {
  private static final int OFF = 0;
  private static int APP_STATUS = 0;

  public IntentManager() {}

  public IntentManager(int ACTIVATED) {
    APP_STATUS = ACTIVATED;
  }
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getExtras() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_MANAGE_ALARM)) {
      if (APP_STATUS == OFF) {
        activateNotificationService(context);
        createNotification(context, intent);
        return;
      }
      startRingtoneService(context, intent);
      startUiService(context);
    }
  }

  public void activateNotificationService(Context context) {
    NotificationService notificationService = new NotificationService();
    IntentFilter intentFilter = new IntentFilter(Constants.ACTION_HANDLE_NOTIFICATION);
    context.getApplicationContext().registerReceiver(notificationService, intentFilter);
  }

  public void createNotification(Context context, Intent incomingIntent) {
    String ringTonetitle = getTitle(incomingIntent);
    Intent notificationIntent = new Intent(Constants.ACTION_HANDLE_NOTIFICATION);
    notificationIntent.putExtra(Constants.BOOT_TAG, Constants.NOTIFICATION_CLASS_TAG);
    notificationIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, ringTonetitle);
    notificationIntent.putExtra(Constants.EXTRA_URI, incomingIntent.getStringExtra(Constants.EXTRA_URI));
    notificationIntent.putExtra(Constants.EXTRA_RAW_TIME, incomingIntent.getIntExtra(Constants.EXTRA_RAW_TIME, 0));
    notificationIntent.putExtra(Constants.EXTRA_VOLUME, incomingIntent.getIntExtra(Constants.EXTRA_VOLUME, 0));
    context.sendBroadcast(notificationIntent);
  }

  public void startRingtoneService(Context context, Intent incoming) {
    Intent ringtoneIntent = new Intent(context, RingtoneService.class);
    ringtoneIntent.putExtra(Constants.EXTRA_URI, incoming.getStringExtra(Constants.EXTRA_URI));
    ringtoneIntent.putExtra(Constants.EXTRA_VOLUME, incoming.getIntExtra(Constants.EXTRA_VOLUME, 0));
    context.startService(ringtoneIntent);
  }

  public void startUiService(Context context) {
    Intent updateUiIntent = new Intent(context, HandleBroadcastIntentService.class);
    context.startService(updateUiIntent);
  }

  public String getTitle(Intent intent) {
    return intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE) == null ? Constants.NO_RINGTONE : intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
  }

}
