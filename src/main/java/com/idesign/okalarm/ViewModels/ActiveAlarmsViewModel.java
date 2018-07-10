package com.idesign.okalarm.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.okalarm.ActiveAlarm;

import java.util.ArrayList;
import java.util.List;

public class ActiveAlarmsViewModel extends ViewModel {
  private MutableLiveData<List<ActiveAlarm>> activeAlarms;

  public LiveData<List<ActiveAlarm>> getItems() {
    if (activeAlarms == null) {
      activeAlarms = new MutableLiveData<>();
      activeAlarms.setValue(new ArrayList<>());
    }
    return activeAlarms;
  }

  public ActiveAlarm selectedAlarm(int position) {
    return activeAlarms.getValue().get(position);
  }

  public void removeAlarm(int position) {
    activeAlarms.getValue().remove(position);
  }

  public void setActiveAlarms(List<ActiveAlarm> alarms) {
    if (activeAlarms == null) {
      activeAlarms = new MutableLiveData<>();
    }
    this.activeAlarms.setValue(alarms);
  }
}
