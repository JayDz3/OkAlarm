package com.idesign.okalarm.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.media.Ringtone;

import java.util.ArrayList;
import java.util.List;

public class RingtonesViewModel extends ViewModel {

  private MutableLiveData<List<Ringtone>> mRingtones;
  private int _selectedItemIndex = -1;

  public LiveData<List<Ringtone>> getRingtones() {
    if (mRingtones == null) {
      mRingtones = new MutableLiveData<>();
      mRingtones.setValue(new ArrayList<>());
    }
    return mRingtones;
  }

  public void setRingtones(List<Ringtone> ringtones) {
    if (mRingtones == null) {
      mRingtones = new MutableLiveData<>();
    }
    this.mRingtones.setValue(ringtones);
  }

  public void set_selectedItemIndex(int position) {
    _selectedItemIndex = position;
  }

  public Ringtone selectedRingtone(int position) {
    return mRingtones.getValue().get(position);
  }

  public int index(Ringtone ringtone) {
    return mRingtones.getValue().indexOf(ringtone);
  }

  public int get_selectedItemIndex() {
    return _selectedItemIndex;
  }
}
