package com.idesign.okalarm;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

public class AlarmFragment extends Fragment implements TimePickerDialog.OnTimeSetListener {

  Button submitButton;
  Button cancelButton;
  private TimePicker timePicker;
  private int hourOfDay;
  private int minute;
  private String am_pm;

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
    mListener.onSet(this.hourOfDay, this.minute, this.am_pm);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnAlarmSet) {
      mListener = (OnAlarmSet) context;
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
    void onSet(int hourOfDay, int minute, String am_pm);
    void onCancel();
  }

}
