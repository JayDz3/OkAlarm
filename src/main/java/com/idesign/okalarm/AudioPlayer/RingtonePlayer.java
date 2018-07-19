package com.idesign.okalarm.AudioPlayer;

import android.media.Ringtone;

 final class RingtonePlayer implements Runnable {
  private final Ringtone mRingtone;

  RingtonePlayer(final Ringtone ringtone) {
    mRingtone = ringtone;
  }

  final boolean isPlaying() {
    return mRingtone != null && mRingtone.isPlaying();
  }

  final boolean isStopped() {
    return mRingtone != null && !mRingtone.isPlaying();
  }

  public final void run() {
    mRingtone.play();
  }

  public final void stop() {
    if (mRingtone != null && mRingtone.isPlaying()) {
      mRingtone.stop();
    }
  }
}
