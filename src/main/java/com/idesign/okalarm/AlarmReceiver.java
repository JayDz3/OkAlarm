package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

  private static OnAlarmReceiver mListener;

  public AlarmReceiver() {}

  public AlarmReceiver(OnAlarmReceiver listener) {
    setListener(listener);
  }

  public void setListener(OnAlarmReceiver listener) {
    if (mListener == null) {
      mListener = listener;
    }
  }
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getExtras() != null) {
      if (mListener == null) {
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
        if (context instanceof OnAlarmReceiver) {
          mListener = (OnAlarmReceiver) context;
        }
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
        mListener.onAction(sendIntent);
      }
    }
  }


  private void broadCastNotification(Context context, Intent intent) {
    AlarmNotification mNotification = new AlarmNotification();
    mNotification.createNotificationChannel(context, intent);
  }

  public void setListenerToNull() {
    mListener = null;
  }

  public interface OnAlarmReceiver {
    void onAction(Intent intent);
  }

}