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

    String bootTag = intent.getStringExtra(Constants.BOOT_TAG);
    String ringtoneTitle = intent.getStringExtra(Constants.EXTRA_RINGTONE_TITLE);
    String itemUri = intent.getStringExtra(Constants.EXTRA_URI);

    int time = intent.getIntExtra(Constants.EXTRA_RAW_TIME, 0);

    Intent outIntent = new Intent(Constants.ACTION_HANDLE_INTENT);
    outIntent.putExtra("resultCode", Activity.RESULT_OK);
    outIntent.putExtra(Constants.BOOT_TAG, bootTag);
    outIntent.putExtra(Constants.EXTRA_RINGTONE_TITLE, ringtoneTitle);
    outIntent.putExtra(Constants.EXTRA_URI, itemUri);
    outIntent.putExtra(Constants.EXTRA_RAW_TIME, time);
    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(outIntent);
  }
}
