package com.idesign.okalarm.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.media.Ringtone;

import java.util.ArrayList;
import java.util.List;

public class RingtonesViewModel extends ViewModel {

  private MutableLiveData<List<Ringtone>> mRingtones;

  public LiveData<List<Ringtone>> getRingtones() {
    ifNull();
    return mRingtones;
  }

  public void setRingtones(List<Ringtone> ringtones) {
    ifNull();
    this.mRingtones.setValue(ringtones);
  }

  public void postRingtones(List<Ringtone> ringtones) {
    ifNull();
    mRingtones.postValue(ringtones);
  }
  private void ifNull() {
    if (mRingtones == null) {
      mRingtones = new MutableLiveData<>();
      mRingtones.setValue(new ArrayList<>());
    }
  }

  public int index(Ringtone ringtone) {
    return mRingtones.getValue().indexOf(ringtone);
  }
}
