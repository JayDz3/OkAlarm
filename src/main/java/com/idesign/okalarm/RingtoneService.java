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

public class RingtoneService extends Service {

  private Ringtone ringtone;
  private AudioManager audioManager;
  private RingtoneManager ringtoneManager;

  private AudioAttributes ringtoneAttributes;
  private AudioFocusRequest request;

  private AudioChangeHelper mAudioChangeHelper;
  private boolean granted;

  @Override
  public IBinder onBind(Intent intent){
    return null;
  }

  @Override
  public void onCreate() {
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    ringtoneManager = new RingtoneManager(getBaseContext());
    mAudioChangeHelper = new AudioChangeHelper();

    ringtoneAttributes = new AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ALARM).build();

    final AudioAttributes focusRequestAttributes = new AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_ALARM)
    .build();

    request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
    .setAcceptsDelayedFocusGain(true)
    .setAudioAttributes(focusRequestAttributes)
    .setOnAudioFocusChangeListener(mAudioChangeHelper).build();

    granted = mAudioChangeHelper.requestAudioFocus();

  }

  @Override
  public  int onStartCommand(Intent intent, int flags, int startId) {

    final int _newVolume = intent.getIntExtra(Constants.EXTRA_VOLUME, 0);
    final Uri uri = Uri.parse(intent.getExtras().getString(Constants.EXTRA_URI));

    if (ringtone != null) {
      ringtone.stop();
    }

    int position = ringtoneManager.getRingtonePosition(uri);
    ringtone = ringtoneManager.getRingtone(position);
    ringtone.setAudioAttributes(ringtoneAttributes);
    if (granted) {
      setVolume(_newVolume);
      mAudioChangeHelper.play();
    }
    return START_NOT_STICKY;
  }

  private void setVolume(int volume) {
    if (volume == 0) {
      audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 1, 0);
    } else {
      audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
    }
  }

  @Override
  public void onDestroy() {
    ringtone.stop();
    mAudioChangeHelper.abandonAudioFocus();
  }

  /*========================*
   *  Audio Listener Class  *
   *========================*/
  private final class AudioChangeHelper implements AudioManager.OnAudioFocusChangeListener {

    private boolean requestAudioFocus() {
      final int result = audioManager.requestAudioFocus(request);
      return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus()  {
      audioManager.abandonAudioFocusRequest(request);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        if (ringtone != null && ringtone.isPlaying()) {
          ringtone.stop();
        }
      } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);

      } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);

      } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        if (ringtone != null && !ringtone.isPlaying()) {
          toggleVolume();
          play();
        }
      }
    }

    private void play() {
      new Thread(() -> ringtone.play()).start();
    }

    private void toggleVolume() {
      if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) == 0) {
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 1, 0);
      }
    }
  }

  public void showToast(String message) {
    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
  }

}
