package com.idesign.okalarm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.idesign.okalarm.Interfaces.AlarmItemListener;

import java.util.List;

public class FormattedTimesAdapter extends RecyclerView.Adapter<FormattedTimesAdapter.MyViewHolder> {

  private List<FormattedTime> formattedTimes;
  private AlarmItemListener mListener;

  static class MyViewHolder extends RecyclerView.ViewHolder {
    private TextView _hourView, _minuteView;
    private Button _deleteButton;
    private Switch _switch;

    MyViewHolder(View view) {
      super(view);
      _hourView = view.findViewById(R.id.alarm_item_hour);
      _minuteView = view.findViewById(R.id.alarm_item_minute);
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
    viewHolder._switch.setChecked(formattedTime.getIsActive());
    viewHolder._deleteButton.setOnClickListener(l -> deleteItem(position));
    viewHolder._switch.setOnClickListener(l -> toggleSwitch(formattedTime, position));
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

  private void deleteItem(final int position){
    formattedTimes.remove(position);
    notifyItemRemoved(position);
    notifyItemRangeChanged(0, getItemCount());
    mListener.onDelete(position);
  }

  private void toggleSwitch(FormattedTime formattedTime, int position) {
    if (formattedTime.getIsActive()) {
      formattedTime.set_isActive(false);
      mListener.onToggle(false, position);
    } else {
      formattedTime.set_isActive(true);
      mListener.onToggle(true, position);
    }
  }

}
