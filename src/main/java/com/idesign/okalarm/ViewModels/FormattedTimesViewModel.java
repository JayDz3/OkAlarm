package com.idesign.okalarm.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.okalarm.FormattedTime;

import java.util.ArrayList;
import java.util.List;

public class FormattedTimesViewModel extends ViewModel {
  private MutableLiveData<List<FormattedTime>> formattedTimes;

  public LiveData<List<FormattedTime>> getItems() {
    if (formattedTimes == null) {
      formattedTimes = new MutableLiveData<>();
      formattedTimes.setValue(new ArrayList<>());
    }
    return formattedTimes;
  }

  public void setFormattedTimes(List<FormattedTime> times) {
    if (formattedTimes == null) {
      formattedTimes = new MutableLiveData<>();
    }
    this.formattedTimes.setValue(times);
  }
}
