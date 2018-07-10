package com.idesign.okalarm;

import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.media.Ringtone;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import com.idesign.okalarm.ViewModels.RingtonesViewModel;

import java.util.Calendar;

public class AlarmFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, AlarmTypeAdapter.OnAlarmTypeListener {

  RecyclerView recyclerView;
  Button submitButton;
  Button cancelButton;
  private TimePicker timePicker;
  private int hourOfDay;
  private int minute;
  private String am_pm;

  AlarmTypeAdapter alarmTypeAdapter;
  private Ringtone ringtone;

  private OnAlarmSet mListener;
  private RingtonesViewModel ringtonesViewModel;

  private String EXTRA_IDX = "extra.index";
  private int _activeIndex = -1;

  public AlarmFragment() { }

  public static AlarmFragment newInstance() {
    return  new AlarmFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.fragment_alarm, container, false);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ringtonesViewModel = ViewModelProviders.of(getActivity()).get(RingtonesViewModel.class);
    ringtonesViewModel.getRingtones().observe(this, items -> {
      alarmTypeAdapter.setItems(items);
    });
    alarmTypeAdapter = new AlarmTypeAdapter(ringtonesViewModel.getRingtones().getValue(),AlarmFragment.this, this.getContext());
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

    recyclerView = view.findViewById(R.id.fragment_alarm_list);
    DividerItemDecoration itemDecoration = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    recyclerView.addItemDecoration(itemDecoration);
    recyclerView.setAdapter(alarmTypeAdapter);
    submitButton = view.findViewById(R.id.fragment_confirm_button);
    cancelButton = view.findViewById(R.id.fragment_cancel);
    timePicker = view.findViewById(R.id.fragment_time_picker);
    submitButton.setOnClickListener(l -> onTimeSet(timePicker, hourOfDay, minute));
    cancelButton.setOnClickListener(l -> mListener.onCancel());
    setInitialTime();
    if (savedInstanceState != null) {
      _activeIndex = savedInstanceState.getInt(EXTRA_IDX);
      Log.d("INDEX", "idx: " + _activeIndex);
    }
    alarmTypeAdapter.setSelectedIndex(_activeIndex);
  }

  public void setInitialTime() {
    Calendar c = Calendar.getInstance();
    this.hourOfDay = c.get(Calendar.HOUR_OF_DAY);
    this.minute = c.get(Calendar.MINUTE);
    if (c.get(Calendar.AM_PM) == Calendar.AM) {
      this.am_pm = "AM";
    } else if (c.get(Calendar.AM_PM) == Calendar.PM) {
      this.am_pm = "PM";
    }
  }


  @Override
  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    _activeIndex = -1;
    this.hourOfDay = timePicker.getHour();
    this.minute = timePicker.getMinute();
    Calendar dateTime = Calendar.getInstance();

    dateTime.set(Calendar.HOUR_OF_DAY, this.hourOfDay);
    dateTime.set(Calendar.MINUTE, this.minute);
    this.am_pm = dateTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
    int position = this.ringtone == null ? -1 : ringtonesViewModel.index(this.ringtone);
    mListener.onSet(this.hourOfDay, this.minute, this.am_pm, position);
  }

  public void onSelectAlarm(Ringtone ringtone, final int position) {
    this.ringtone = ringtone;
    _activeIndex = position;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mListener = (OnAlarmSet) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + "Must implement OnAlarmSet");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    outState.putInt(EXTRA_IDX, _activeIndex);
    super.onSaveInstanceState(outState);
  }

  public interface OnAlarmSet {
    void onSet(int hourOfDay, int minute, String am_pm, final int position);
    void onCancel();
  }

}
