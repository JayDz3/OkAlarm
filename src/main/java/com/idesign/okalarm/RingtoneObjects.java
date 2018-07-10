package com.idesign.okalarm;

import android.media.Ringtone;

import java.util.List;

public class RingtoneObjects {

  private List<Ringtone> ringtones;

  public RingtoneObjects() {}

  public void setRingtones(List<Ringtone> ringtones) {
    this.ringtones = ringtones;
  }

  public List<Ringtone> getRingtones() {
    return ringtones;
  }

  public Ringtone selectedRingtone(int position) {
    return ringtones.get(position);
  }
}
