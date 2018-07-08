package com.idesign.okalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;


public class AlarmNotification extends BroadcastReceiver {
  private static final String TAG = "MyAlarmNotification";
  public static final String DESCRIPTION = "alert alarms";
  public static final String CHANNEL_ID = "101";
  public static final int ALARM_REQUEST_CODE = 101;
  public static final String EXTRA_COLD_BOOT = "extra.cold";
  public static final String KEY_TEXT_REPLY = "key.text.reply";


  public AlarmNotification() { }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null && intent.getExtras() != null) {
      Bundle extras = intent.getExtras();
      boolean isColdBoot = extras.getBoolean(EXTRA_COLD_BOOT);
      if (isColdBoot) {
        createNotificationChannel(context);
      }
    }
  }

  public void createNotificationChannel(Context context) {
    int importance = NotificationManager.IMPORTANCE_DEFAULT;
    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, TAG, importance);
    channel.setDescription(DESCRIPTION);
    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);

    Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
    intent.putExtra(EXTRA_COLD_BOOT, true);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

    String replyLabel = context.getResources().getString(R.string.reply_label);
    RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
    .setLabel(replyLabel)
    .build();

    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_add_alarm_black_24dp, TAG, pendingIntent)
    .addRemoteInput(remoteInput)
    .build();

    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String _title = "Good Morning!";
    String _content = "What day is it?";
    Notification notification = getNotificationAction(context, alarmSound, _title, _content, action);

    NotificationManagerCompat manager = NotificationManagerCompat.from(context.getApplicationContext());
    manager.notify(ALARM_REQUEST_CODE, notification);
  }

  public Notification getNotificationAction(Context context, Uri alarmSound, String _title, String _content, NotificationCompat.Action action) {
    return new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
    .setContentTitle(_title)
    .setContentText(_content)
    .setSound(alarmSound)
    .setSmallIcon(R.mipmap.ic_launcher_round)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .addAction(action).build();
  }

}
