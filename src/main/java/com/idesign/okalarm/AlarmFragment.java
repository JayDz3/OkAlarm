package com.idesign.okalarm;

import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TimePicker;

import com.idesign.okalarm.ViewModels.ActiveAlarmsViewModel;
import com.idesign.okalarm.ViewModels.RingtonesViewModel;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class AlarmFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, AlarmTypeAdapter.OnAlarmTypeListener {

  RecyclerView recyclerView;
  ImageView muteImage;
  Button submitButton;
  Button cancelButton;
  private TimePicker timePicker;
  SeekBar seekBar;
  private int hourOfDay;
  private int minute;
  private String am_pm;

  AlarmTypeAdapter alarmTypeAdapter;
  private Ringtone ringtone;

  private OnAlarmSet mListener;
  private RingtonesViewModel ringtonesViewModel;
  private ActiveAlarmsViewModel activeAlarmsViewModel;

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
    activeAlarmsViewModel = ViewModelProviders.of(getActivity()).get(ActiveAlarmsViewModel.class);
    ringtonesViewModel = ViewModelProviders.of(getActivity()).get(RingtonesViewModel.class);
    ringtonesViewModel.getRingtones().observe(this, items -> {
      alarmTypeAdapter.setItems(items);
    });
    alarmTypeAdapter = new AlarmTypeAdapter(ringtonesViewModel.getRingtones().getValue(),AlarmFragment.this, this.getContext());
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

    seekBar = view.findViewById(R.id.fragment_volume_slider);
    muteImage = view.findViewById(R.id.fragment_mute_icon);
    submitButton = view.findViewById(R.id.fragment_confirm_button);
    cancelButton = view.findViewById(R.id.fragment_cancel);
    timePicker = view.findViewById(R.id.fragment_time_picker);
    submitButton.setOnClickListener(l -> onTimeSet(timePicker, hourOfDay, minute));
    cancelButton.setOnClickListener(l -> mListener.onCancel());

    AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

    seekBar.setProgress(0);
    seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
    seekBar.setOnSeekBarChangeListener(seekbarListener);

    recyclerView = view.findViewById(R.id.fragment_alarm_list);
    DividerItemDecoration itemDecoration = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    recyclerView.addItemDecoration(itemDecoration);
    recyclerView.setAdapter(alarmTypeAdapter);

    setInitialTime();
    if (savedInstanceState != null) {
      _activeIndex = savedInstanceState.getInt(EXTRA_IDX);
    }
    alarmTypeAdapter.setSelectedIndex(_activeIndex);
    updateLayout(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    updateLayout(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
  }

  private void updateLayout(boolean isLandscape) {
    if (isLandscape) {
      timePicker.setVisibility(View.GONE);
    } else {
      timePicker.setVisibility(View.VISIBLE);
    }
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
    int volume = seekBar.getProgress();

    this.hourOfDay = timePicker.getHour();
    this.minute = timePicker.getMinute();

    Calendar calendar = Calendar.getInstance();

    calendar.set(Calendar.HOUR_OF_DAY, this.hourOfDay);
    calendar.set(Calendar.MINUTE, this.minute);
    this.am_pm = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

    // check active alarms index of this time //
    int idx = checkIndex(calendar.get(Calendar.HOUR), timePicker.getMinute(), calendar.get(Calendar.AM_PM));
    if (idx > -1) {
      mListener.onCancel();
      return;
    }
    int position = this.ringtone == null ? -1 : ringtonesViewModel.index(this.ringtone);
    mListener.onSet(this.hourOfDay, this.minute, volume, this.am_pm, position);
  }

  private SeekBar.OnSeekBarChangeListener seekbarListener = new SeekBar.OnSeekBarChangeListener() {
    public void onStartTrackingTouch(SeekBar seekBar) {}
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      updateView(progress);
    }
    public void onStopTrackingTouch(SeekBar seekBar) { }
  };

  private void updateView(int progress) {
    if (progress == 0) {
      muteImage.setVisibility(View.VISIBLE);
    } else {
      muteImage.setVisibility(View.INVISIBLE);
    }
  }

  public int checkIndex(int hour, int minute, int calendarAmPm) {
    int idx = -1;
    int formattedHour = hour == 0 ? 12 : hour;
    Calendar calendar = Calendar.getInstance();
    List<ActiveAlarm> filtered = activeAlarmsViewModel.getItems().getValue().stream()
    .filter(i -> i.get_hour() == hour && i.get_minute() == minute)
    .collect(Collectors.toList());

    for (ActiveAlarm alarm : filtered) {
      calendar.setTimeInMillis(alarm.get_rawTime());
      if (alarm.get_hour() == formattedHour && alarm.get_minute() == minute && calendar.get(Calendar.AM_PM) == calendarAmPm) {
          idx = 0;
      }
    }
    return idx;
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
    void onSet(int hourOfDay, int minute, int volume, String am_pm, final int position);
    void onCancel();
  }

}
