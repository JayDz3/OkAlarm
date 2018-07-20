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

public class NotificationService extends BroadcastReceiver {

  public NotificationService() {}

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null && intent.getExtras() != null) {
      final Bundle extras = intent.getExtras();
      final String bootTag = extras.getString(Constants.BOOT_TAG);
      if (bootTag != null && bootTag.equalsIgnoreCase(Constants.NOTIFICATION_CLASS_TAG)) {
        createNotificationChannel(context, intent);
      }
    }
  }

  public void createNotificationChannel(Context context, Intent receivedIntent) {
    final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());

    final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    addChannel(notificationManager);

    final String ringtoneTitle = receivedIntent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
    final String itemUri = receivedIntent.getStringExtra(Constants.EXTRA_URI);
    final int volume = receivedIntent.getIntExtra(Constants.EXTRA_VOLUME, 0);

    final String replyLabel = context.getResources().getString(R.string.reply_label);
    final RemoteInput remoteInput = new RemoteInput.Builder(Constants.NOTIFICATION_KEY_TEXT_REPLY)
    .setLabel(replyLabel).build();

    final int rawTime = receivedIntent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);
    final int notificationId = 1;

    final Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
    final Bundle notificationBundle = new Bundle();

    intent.putExtra(Constants.BOOT_TAG, Constants.NOTIFICATION_CLASS_TAG);
    intent.putExtra(Constants.EXTRA_RINGTONE_TITLE, ringtoneTitle);
    intent.putExtra(Constants.EXTRA_URI, itemUri);
    intent.putExtra(Constants.EXTRA_RAW_TIME, rawTime);
    intent.putExtra(Constants.EXTRA_VOLUME, volume);
    intent.setFlags(Constants.FLAG_NEW_TASK | Constants.FLAG_CLEAR_TASK);

    notificationBundle.putString(Constants.EXTRA_URI, itemUri);
    notificationBundle.putInt(Constants.EXTRA_VOLUME, volume);
    notificationBundle.putInt(Constants.EXTRA_RAW_TIME, rawTime);

    final PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), Constants.NOTIFICATION_ALARM_REQUEST_CODE, intent, Constants.FLAG_UPDATE_CURRENT);

    final NotificationCompat.Action action = new NotificationCompat.Action.Builder(
      R.drawable.ic_add_alarm_black_24dp,
      Constants.NOTIFICATION_CLASS_TAG,
      pendingIntent)
    .addRemoteInput(remoteInput)
    .build();

    final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    final String _title = "Good Morning!";
    final String _content = "What day is it?";


    final Notification notification = new NotificationCompat.Builder(context.getApplicationContext(), Constants.NOTIFICATION_CHANNEL_ID)
    .setContentTitle(_title)
    .setContentText(_content)
    .setSound(alarmSound)
    .setExtras(notificationBundle)
    .setSmallIcon(R.mipmap.ic_launcher_round)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .addAction(action).build();

    notificationManagerCompat.notify(notificationId, notification);
    /*=========================================*
     *  Unregister after getting notification  *
     *=========================================*/
    context.unregisterReceiver(this);
  }

  public void addChannel(NotificationManager notificationManager) {
    if (notificationManager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID) == null) {
      final NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CLASS_TAG, NotificationManager.IMPORTANCE_DEFAULT);
      channel.setDescription(Constants.NOTIFICATION_DESCRIPTION);
      notificationManager.createNotificationChannel(channel);
    }
  }
}
