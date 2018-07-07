package com.idesign.okalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmCompleted extends BroadcastReceiver {

  public void onReceive(Context context, Intent intent) {
    Toast.makeText(context, "Alarm set", Toast.LENGTH_SHORT).show();
  }
}
