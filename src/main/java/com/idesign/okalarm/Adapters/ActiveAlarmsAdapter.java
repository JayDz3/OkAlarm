package com.idesign.okalarm.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.idesign.okalarm.Factory.ActiveAlarm;
import com.idesign.okalarm.R;

import java.util.List;

public class ActiveAlarmsAdapter extends RecyclerView.Adapter<ActiveAlarmsAdapter.MyViewHolder> {

  private List<ActiveAlarm> activeAlarms;
  private OnActiveAlarmAdapterListener mListener;

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

   public ActiveAlarmsAdapter(List<ActiveAlarm> times, OnActiveAlarmAdapterListener listener) {
    this.activeAlarms = times;
    setListener(listener);
  }

  private void setListener(OnActiveAlarmAdapterListener listener) {
    if (mListener == null) {
      mListener = listener;
    }
  }

  public void setList(List<ActiveAlarm> times) {
    this.activeAlarms = times;
    notifyDataSetChanged();
  }

  @Override
  @NonNull
  public ActiveAlarmsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item, parent, false);
    return new ActiveAlarmsAdapter.MyViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ActiveAlarmsAdapter.MyViewHolder viewHolder, final int position) {
    final ActiveAlarm activeAlarm = activeAlarms.get(position);
    String finalMin = formattedMinute(activeAlarm.get_minute());
    viewHolder._hourView.setText(String.valueOf(activeAlarm.get_hour()));
    viewHolder._minuteView.setText(finalMin);
    viewHolder._am_pm.setText(activeAlarm.get_am_pm());
    viewHolder._dateView.setText(activeAlarm.get_date());
    viewHolder._switch.setChecked(activeAlarm.getIsActive());
    viewHolder._deleteButton.setOnClickListener(l -> deleteItem(position, activeAlarm.get_rawTime()));
    viewHolder._switch.setOnClickListener(l -> toggleSwitch(activeAlarm, position));
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
    return activeAlarms.size();
  }

  private void deleteItem(final int position, long rawTime){
    mListener.onDeleteAlarm(position, rawTime);
    notifyItemRemoved(position);
    notifyItemRangeChanged(0, getItemCount());
  }

  private void toggleSwitch(ActiveAlarm activeAlarm, final int position) {
    if (activeAlarm.getIsActive()) {
      activeAlarm.set_isActive(false);
      mListener.onToggleAlarm(false, position);
    } else {
      activeAlarm.set_isActive(true);
      mListener.onToggleAlarm(true, position);
    }
  }

  public interface OnActiveAlarmAdapterListener {
    void onDeleteAlarm(int position, long rawtime);
    void onToggleAlarm(boolean isToggled, final int position);
  }
}
