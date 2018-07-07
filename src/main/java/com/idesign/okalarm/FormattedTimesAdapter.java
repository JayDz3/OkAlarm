package com.idesign.okalarm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.idesign.okalarm.Interfaces.AlarmItemListener;

import java.util.List;

public class FormattedTimesAdapter extends RecyclerView.Adapter<FormattedTimesAdapter.MyViewHolder> {

  private List<FormattedTime> formattedTimes;
  private AlarmItemListener mListener;

  static class MyViewHolder extends RecyclerView.ViewHolder {
    private TextView _hourView, _minuteView, _am_pm, _dateView;
    private Button _deleteButton;
    private SwitchCompat _switch;

    MyViewHolder(View view) {
      super(view);
      _hourView = view.findViewById(R.id.alarm_item_hour);
      _minuteView = view.findViewById(R.id.alarm_item_minute);
      _am_pm = view.findViewById(R.id.alarm_am_pm);
      _dateView = view.findViewById(R.id.alarm_date);
      _deleteButton = view.findViewById(R.id.alarm_delete_button);
      _switch = view.findViewById(R.id.alarm_switch);
    }
  }

  FormattedTimesAdapter(List<FormattedTime> times, AlarmItemListener listener) {
    this.formattedTimes = times;
    setListener(listener);
  }

  private void setListener(AlarmItemListener listener) {
    if (mListener == null) {
      mListener = listener;
    }
  }

  public void setList(List<FormattedTime> times) {
    this.formattedTimes = times;
    notifyDataSetChanged();
  }

  @Override
  @NonNull
  public FormattedTimesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item, parent, false);
    return new MyViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull FormattedTimesAdapter.MyViewHolder viewHolder, final int position) {
    final FormattedTime formattedTime = formattedTimes.get(position);
    String finalMin = formattedMinute(formattedTime.get_minute());
    viewHolder._hourView.setText(String.valueOf(formattedTime.get_hour()));
    viewHolder._minuteView.setText(finalMin);
    viewHolder._am_pm.setText(formattedTime.get_am_pm());
    viewHolder._dateView.setText(formattedTime.get_date());
    viewHolder._switch.setChecked(formattedTime.getIsActive());
    viewHolder._deleteButton.setOnClickListener(l -> deleteItem(position, formattedTime.get_rawTime()));
    viewHolder._switch.setOnClickListener(l -> toggleSwitch(formattedTime));
  }

  private String formattedMinute(int source) {
    if (source < 10) {
      return "0" + String.valueOf(source);
    } else {
      return String.valueOf(source);
    }
  }

  @Override
  public int getItemCount() {
    return formattedTimes.size();
  }

  private void deleteItem(final int position, long rawTime){
    mListener.onDeleteAlarm(position, rawTime);
    formattedTimes.remove(position);
    notifyItemRemoved(position);
    notifyItemRangeChanged(0, getItemCount());
  }

  private void toggleSwitch(FormattedTime formattedTime) {
    if (formattedTime.getIsActive()) {
      formattedTime.set_isActive(false);
      mListener.onToggleAlarm(false, formattedTime);
    } else {
      formattedTime.set_isActive(true);
      mListener.onToggleAlarm(true, formattedTime);
    }
  }

}
