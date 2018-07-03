package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class AlarmReceiver extends BroadcastReceiver {
private static Ringtone ringtone;
  @Override
  public void onReceive(Context context, Intent intent) {
    Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    ringtone = RingtoneManager.getRingtone(context, alarmUri);
    ringtone.play();
  }

  public static Ringtone getRingtone() {
    return ringtone;
  }
}
