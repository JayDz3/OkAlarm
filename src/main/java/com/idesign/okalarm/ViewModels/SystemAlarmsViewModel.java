package com.idesign.okalarm.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.okalarm.Factory.SystemAlarm;

import java.util.ArrayList;
import java.util.List;

public class SystemAlarmsViewModel extends ViewModel {
  private MutableLiveData<List<SystemAlarm>> mSystemAlarms;

  public LiveData<List<SystemAlarm>> getItems() {
    ifNull();
    return mSystemAlarms;
  }

  public void setSystemAlarms(List<SystemAlarm> systemAlarms) {
    ifNull();
    mSystemAlarms.setValue(systemAlarms);
  }

  public int index(SystemAlarm systemAlarm) {
    return mSystemAlarms.getValue().indexOf(systemAlarm);
  }

  private void ifNull() {
    if (mSystemAlarms == null) {
      mSystemAlarms = new MutableLiveData<>();
      mSystemAlarms.setValue(new ArrayList<>());
    }
  }
}
