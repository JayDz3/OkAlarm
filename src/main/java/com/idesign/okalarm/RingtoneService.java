package com.idesign.okalarm;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

public class RingtoneService extends Service {

  private Ringtone ringtone;

  @Override
  public IBinder onBind(Intent intent){
    return null;
  }

  @Override
  public  int onStartCommand(Intent intent, int flags, int startId) {
    Uri uri = Uri.parse(intent.getExtras().getString(Constants.EXTRA_URI));
    this.ringtone = RingtoneManager.getRingtone(this, uri);
    ringtone.play();
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    ringtone.stop();
  }
}
