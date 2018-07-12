package com.idesign.okalarm;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class RingtoneService extends Service {

  private Ringtone ringtone;
  private int _volumeBefore;
  private AudioManager audioManager;

  @Override
  public IBinder onBind(Intent intent){
    return null;
  }

  @Override
  public  int onStartCommand(Intent intent, int flags, int startId) {

    final int _volumeNow = intent.getIntExtra(Constants.EXTRA_VOLUME, 0);
    Uri uri = Uri.parse(intent.getExtras().getString(Constants.EXTRA_URI));

    /*=======================*
     *  Using Audio Manager  *
     *=======================*/
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    if (ringtone == null) {
      _volumeBefore = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
    } else {
      ringtone.stop();
    }

     ringtone = RingtoneManager.getRingtone(this, uri);
     audioManager.setStreamVolume(AudioManager.STREAM_ALARM, _volumeNow, 0);
     ringtone.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
     ringtone.play();
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, _volumeBefore, 0);
    ringtone.stop();
  }
}
