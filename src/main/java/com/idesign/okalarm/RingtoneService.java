package com.idesign.okalarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import com.idesign.okalarm.AudioPlayer.AudioChangeHelper;

public class RingtoneService extends Service {

  private RingtoneManager ringtoneManager;
  private AudioAttributes ringtoneAttributes;
  private AudioChangeHelper mAudioChangeHelper;
  private boolean granted;

  @Override
  public IBinder onBind(Intent intent){
    return null;
  }

  @Override
  public void onCreate() {
    final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    ringtoneManager = new RingtoneManager(getBaseContext());

    mAudioChangeHelper = new AudioChangeHelper(audioManager);

    final AudioAttributes focusRequestAttributes = new AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ALARM)
    .build();

    final AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
    .setAcceptsDelayedFocusGain(true)
    .setAudioAttributes(focusRequestAttributes)
    .setOnAudioFocusChangeListener(mAudioChangeHelper).build();

    mAudioChangeHelper.setRequest(request);

    ringtoneAttributes = new AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ALARM).build();
    granted = mAudioChangeHelper.requestAudioFocus();

  }

  @Override
  public  int onStartCommand(Intent intent, int flags, int startId) {
    final int _newVolume = intent.getIntExtra(Constants.EXTRA_VOLUME, 0);
    final Uri uri = Uri.parse(intent.getStringExtra(Constants.EXTRA_URI));

    final int position = ringtoneManager.getRingtonePosition(uri);
    final Ringtone ringtone = ringtoneManager.getRingtone(position);
    ringtone.setAudioAttributes(ringtoneAttributes);
    mAudioChangeHelper.assignRingtone(ringtone);

    if (granted) {
      mAudioChangeHelper.setVolume(_newVolume);
      mAudioChangeHelper.play();
    }
    return START_NOT_STICKY;
  }


  @Override
  public void onDestroy() {
    mAudioChangeHelper.stop();
    mAudioChangeHelper.abandonAudioFocus();
    mAudioChangeHelper = null;
  }

  public void showToast(String message) {
    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
  }

}
