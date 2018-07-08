package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.idesign.okalarm.Interfaces.OnAlarmRing;

public class AlarmReceiver extends BroadcastReceiver {

  private static OnAlarmRing mListener;
  private static Ringtone ringtone;
  private static final String EXTRA_COLD_BOOT = "extra.cold";

  public AlarmReceiver() {}

  public AlarmReceiver(OnAlarmRing listener) {
    setListener(listener);
  }
  private void setListener(OnAlarmRing listener) {
    if (mListener == null) {
      mListener = listener;
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (mListener != null) {
      mListener.onRing(ringtone);
    } else {
      broadCastNotification(context);
    }
  }

  /* private void broadCastColdIntent(Context context) {
      Intent mainIntent = new Intent(context.getApplicationContext(), MainActivity.class);
      mainIntent.putExtra(EXTRA_COLD_BOOT, true);
      mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      context.startActivity(mainIntent);
  } */

  private void broadCastNotification(Context context) {
    AlarmNotification mNotification = new AlarmNotification();
    mNotification.createNotificationChannel(context);
  }

  public void setRingtone(Context context, Ringtone tone) {
    if (tone == null) {
      Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
      ringtone = RingtoneManager.getRingtone(context, alarmUri);
    } else {
      ringtone = tone;
    }
  }

  public Ringtone getRingtone() {
    return ringtone;
  }

  public void setListenerToNull() {
    mListener = null;
  }
}