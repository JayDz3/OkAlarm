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
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.widget.Toast;

public class AlarmNotification extends BroadcastReceiver {

  public AlarmNotification() { }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null && intent.getExtras() != null) {
      Bundle extras = intent.getExtras();
      String bootTag = extras.getString(Constants.BOOT_TAG);
      if (bootTag != null) {
        showToast(context, bootTag);
      if (bootTag.equalsIgnoreCase(Constants.NOTIFICATION_CLASS_TAG)) {
        createNotificationChannel(context, intent);
      }
      }
    }
  }

  public void showToast(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  public void createNotificationChannel(Context context, Intent receivedIntent) {
    String ringtoneTitle = receivedIntent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
    String itemUri = receivedIntent.getStringExtra(Constants.EXTRA_URI);
    String replyLabel = context.getResources().getString(R.string.reply_label);
    int rawTime = receivedIntent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);
    int notificationId = 1;

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());
    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CLASS_TAG, NotificationManager.IMPORTANCE_DEFAULT);

    channel.setDescription(Constants.NOTIFICATION_DESCRIPTION);
    notificationManager.createNotificationChannel(channel);

    RemoteInput remoteInput = new RemoteInput.Builder(Constants.NOTIFICATION_KEY_TEXT_REPLY)
    .setLabel(replyLabel)
    .build();

    Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
    intent.putExtra(Constants.BOOT_TAG, Constants.NOTIFICATION_CLASS_TAG);
    intent.putExtra(Constants.EXTRA_RINGTONE_TITLE, ringtoneTitle);
    intent.putExtra(Constants.EXTRA_URI, itemUri);
    intent.putExtra(Constants.EXTRA_RAW_TIME, rawTime);
    intent.setFlags(Constants.FLAG_NEW_TASK| Constants.FLAG_CLEAR_TASK);

    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), Constants.NOTIFICATION_ALARM_REQUEST_CODE, intent, Constants.FLAG_UPDATE_CURRENT);

    NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_add_alarm_black_24dp, Constants.NOTIFICATION_CLASS_TAG, pendingIntent)
    .addRemoteInput(remoteInput)
    .build();

    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String _title = "Good Morning!";
    String _content = "What day is it?";
    Bundle extras = new Bundle();
    extras.putString(Constants.EXTRA_URI, itemUri);
    Notification notification = getNotificationAction(context, alarmSound, _title, _content, action, extras);

    StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
    for (StatusBarNotification statusBarNotification : notifications) {
      if (statusBarNotification.getNotification().getChannelId().equalsIgnoreCase(Constants.NOTIFICATION_CHANNEL_ID) && statusBarNotification.getId() == notificationId) {
        notificationId += 1;
      }
    }

    notificationManagerCompat.notify(notificationId, notification);
  }

  public Notification getNotificationAction(Context context, Uri alarmSound, String _title, String _content, NotificationCompat.Action action, Bundle bundle) {
    return new NotificationCompat.Builder(context.getApplicationContext(), Constants.NOTIFICATION_CHANNEL_ID)
    .setContentTitle(_title)
    .setContentText(_content)
    .setSound(alarmSound)
    .setExtras(bundle)
    .setSmallIcon(R.mipmap.ic_launcher_round)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .addAction(action).build();
  }
}
