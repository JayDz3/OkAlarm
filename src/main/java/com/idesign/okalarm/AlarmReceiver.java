package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.support.v4.content.LocalBroadcastManager;

public class AlarmReceiver extends BroadcastReceiver {

  private static int isActivated = 0;
  private static int isRegistered = 0;
  public AlarmReceiver() {}

  public AlarmReceiver(int setToOne) {
    isActivated = setToOne;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getExtras() != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_MANAGE_ALARM)) {
      if (isActivated == 0) {
         if (isRegistered == 0) {
          registerNotificationReceiver(context);
        }

        String finalTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE) == null ? Constants.NO_RINGTONE : intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
        String itemUri = intent.getStringExtra(Constants.EXTRA_URI);
        int volume = intent.getIntExtra(Constants.EXTRA_VOLUME, 0);

        Intent broadcastIntent = new Intent(Constants.ACTION_HANDLE_NOTIFICATION);
        broadcastIntent.putExtra(Constants.BOOT_TAG, Constants.NOTIFICATION_CLASS_TAG);
        broadcastIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, finalTitle);
        broadcastIntent.putExtra(Constants.EXTRA_URI, itemUri);
        broadcastIntent.putExtra(Constants.EXTRA_RAW_TIME, intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0));
        broadcastIntent.putExtra(Constants.EXTRA_VOLUME, volume);

        sendBroadCastNotification(context, broadcastIntent);
        isRegistered = 1;
        return;
      }
        String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE) == null ? Constants.NO_RINGTONE : intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
        String itemUri = intent.getStringExtra(Constants.EXTRA_URI);
        int rawTime = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);
        int volume = intent.getIntExtra(Constants.EXTRA_VOLUME, 0);
        Intent uiIntent = new Intent(Constants.ACTION_RECEIVE_ALARM);
        uiIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, ringtoneTitle);
        uiIntent.putExtra(Constants.EXTRA_RAW_TIME, rawTime);
        uiIntent.putExtra(Constants.EXTRA_URI, itemUri);
        uiIntent.putExtra(Constants.BOOT_TAG, Constants.RECEIVE_ALARM_TAG);
        uiIntent.putExtra(Constants.EXTRA_VOLUME, volume);

        startRingtoneService(context, itemUri, volume);
        startUiService(context, uiIntent);
    }
  }

  public void startRingtoneService(Context context, String itemUri, int volume) {
    Intent ringtoneIntent = new Intent(context, RingtoneService.class);
    ringtoneIntent.putExtra(Constants.EXTRA_URI, itemUri);
    ringtoneIntent.putExtra(Constants.EXTRA_VOLUME, volume);
    context.startService(ringtoneIntent);
  }

   public void startUiService(Context context, Intent inboundIntent) {
    Intent updateUiIntent = new Intent(context, HandleBroadcastIntentService.class);
    updateUiIntent.putExtra(Constants.BOOT_TAG, Constants.RECEIVE_ALARM_TAG);
    updateUiIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, inboundIntent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE));
    updateUiIntent.putExtra(Constants.EXTRA_URI, inboundIntent.getStringExtra(Constants.EXTRA_URI));
    updateUiIntent.putExtra(Constants.EXTRA_RAW_TIME, inboundIntent.getIntExtra(Constants.EXTRA_RAW_TIME, 0));
    updateUiIntent.putExtra(Constants.EXTRA_VOLUME, inboundIntent.getIntExtra(Constants.EXTRA_VOLUME, 0));
    context.startService(updateUiIntent);
  }

  private void registerNotificationReceiver(Context context) {
    AlarmNotification alarmNotification = new AlarmNotification();
    IntentFilter intentFilter = new IntentFilter(Constants.ACTION_HANDLE_NOTIFICATION);
    context.getApplicationContext().registerReceiver(alarmNotification, intentFilter);
  }

  private void sendBroadCastNotification(Context context, Intent intent) {
    context.sendBroadcast(intent);
  }

}