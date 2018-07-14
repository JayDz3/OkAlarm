package com.idesign.okalarm;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.media.Ringtone;
import android.support.v4.content.LocalBroadcastManager;

public class HandleBroadcastIntentService extends IntentService {

  public HandleBroadcastIntentService() {
    super("HandleBroadcastIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Intent outIntent = new Intent(Constants.ACTION_HANDLE_INTENT);
    outIntent.putExtra("resultCode", Activity.RESULT_OK);
    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(outIntent);
  }
}
