package com.idesign.okalarm.AudioPlayer;

import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.Ringtone;

public class AudioChangeHelper implements AudioManager.OnAudioFocusChangeListener {

  private AudioManager mAudioManager;
  private AudioFocusRequest mRequest;
  private RingtonePlayer mRingtonePlayer;

  public AudioChangeHelper(AudioManager audioManager) {
    mAudioManager = audioManager;
  }

  public void setRequest(AudioFocusRequest request) {
    mRequest = request;
  }

  public void assignRingtone(final Ringtone ringtone) {
    if (mRingtonePlayer != null) {
      mRingtonePlayer.stop();
      mRingtonePlayer = null;
    }
    mRingtonePlayer = new RingtonePlayer(ringtone);
  }

  public boolean requestAudioFocus() {
    final int result = mAudioManager.requestAudioFocus(mRequest);
    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  public void abandonAudioFocus()  {
    mAudioManager.abandonAudioFocusRequest(mRequest);
  }

  @Override
  public void onAudioFocusChange(int focusChange) {
    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
      if (mRingtonePlayer.isPlaying()) {
        stop();
      }

    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
      if (mRingtonePlayer.isPlaying()) {
        stop();
      }

    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
      if (mRingtonePlayer.isStopped()) {
        toggleVolume();
        play();
      }

    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
      if (mRingtonePlayer.isStopped()) {
        toggleVolume();
        play();
      }
    }
  }

  public void play() {
    new Thread(mRingtonePlayer).start();
  }

  public void stop() {
    mRingtonePlayer.stop();
  }

  public void setVolume(int volume) {
    if (volume == 0) {
      mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, 1, 0);
    } else {
      mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
    }
  }

  private void toggleVolume() {
    if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) == 0) {
      mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, 1, 0);
    }
  }
}
