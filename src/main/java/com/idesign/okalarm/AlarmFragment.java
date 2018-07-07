package com.idesign.okalarm;

import android.app.TimePickerDialog;
import android.content.Context;
import android.media.Ringtone;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, AlarmTypeAdapter.OnAlarmTypeListener {

  RecyclerView recyclerView;
  Button submitButton;
  Button cancelButton;
  private TimePicker timePicker;
  private int hourOfDay;
  private int minute;
  private String am_pm;

 AlarmTypeAdapter alarmTypeAdapter;
 private List<Ringtone> ringtones;
 private Ringtone ringtone;

  private OnAlarmSet mListener;

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
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
   recyclerView = view.findViewById(R.id.fragment_alarm_list);
    DividerItemDecoration itemDecoration = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    recyclerView.addItemDecoration(itemDecoration);

    ringtones = new ArrayList<>();
    mListener.onReady();
    alarmTypeAdapter = new AlarmTypeAdapter(ringtones,AlarmFragment.this, this.getContext());
    recyclerView.setAdapter(alarmTypeAdapter);
    submitButton = view.findViewById(R.id.fragment_confirm_button);
    cancelButton = view.findViewById(R.id.fragment_cancel);
    timePicker = view.findViewById(R.id.fragment_time_picker);
    submitButton.setOnClickListener(l -> onTimeSet(timePicker, hourOfDay, minute));
    cancelButton.setOnClickListener(l -> mListener.onCancel());
    setInitialTime();
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

  public void setRingtones(List<Ringtone> tones) {
    ringtones = tones;
  }

  @Override
  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    this.hourOfDay = timePicker.getHour();
    this.minute = timePicker.getMinute();
    Calendar dateTime = Calendar.getInstance();

    dateTime.set(Calendar.HOUR_OF_DAY, this.hourOfDay);
    dateTime.set(Calendar.MINUTE, this.minute);
    if (dateTime.get(Calendar.AM_PM) == Calendar.AM) {
      this.am_pm = "AM";
    } else if (dateTime.get(Calendar.AM_PM) == Calendar.PM) {
      this.am_pm = "PM";
    }
    mListener.onSet(this.hourOfDay, this.minute, this.am_pm, ringtone);
  }

  public void onSelectAlarm(Ringtone ringtone) {
    this.ringtone = ringtone;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnAlarmSet) {
      if (mListener == null) {
        mListener = (OnAlarmSet) context;
      }
    } else {
      throw new RuntimeException(context.toString()
      + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnAlarmSet {
    void onReady();
    void onSet(int hourOfDay, int minute, String am_pm, Ringtone ringtone);
    void onCancel();
  }

}
