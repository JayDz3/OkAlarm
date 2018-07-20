package com.idesign.okalarm.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.idesign.okalarm.Factory.SystemAlarm;
import com.idesign.okalarm.R;

import java.util.List;

public class AlarmTypeAdapter extends RecyclerView.Adapter<AlarmTypeAdapter.AlarmViewHolder> {

  private List<SystemAlarm> mRingtones;
  private OnAlarmTypeListener mListener;
  private int _activeIndex  = -1;

  static class AlarmViewHolder extends RecyclerView.ViewHolder {

    private RadioButton _radioButton;

    AlarmViewHolder(View view) {
      super(view);
      _radioButton = view.findViewById(R.id.fragment_alarm_type_text);
    }
  }

  public AlarmTypeAdapter(List<SystemAlarm> ringtones, OnAlarmTypeListener listener) {
    mRingtones = ringtones;
    setListener(listener);
  }

  private void setListener(OnAlarmTypeListener listener) {
    if (mListener == null) {
      mListener = listener;
    }
  }

  public void setItems(List<SystemAlarm> ringtones) {
    mRingtones = ringtones;
    notifyDataSetChanged();
  }

  public void setSelectedIndex(int index) {
    _activeIndex = index;
  }

  @Override
  @NonNull
  public AlarmTypeAdapter.AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_type_item, parent, false);
    return new AlarmViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AlarmTypeAdapter.AlarmViewHolder viewHolder, final int position) {
    final SystemAlarm systemAlarm = mRingtones.get(position);
    viewHolder._radioButton.setText(systemAlarm.get_title());
    viewHolder._radioButton.setOnClickListener(l -> selectRingtone(systemAlarm, position));
    if (_activeIndex == position) {
      viewHolder._radioButton.setChecked(true);
    } else {
      viewHolder._radioButton.setChecked(false);
    }
  }

  @Override
  public int getItemCount() {
    return mRingtones.size();
  }

  private void selectRingtone(SystemAlarm systemAlarm, final int position) {
    if (_activeIndex == position) {
      _activeIndex = -1;
      systemAlarm = null;
    } else {
      _activeIndex = position;
    }
    mListener.onSelectAlarm(systemAlarm, _activeIndex);
    notifyDataSetChanged();
  }

  public interface OnAlarmTypeListener {
    void onSelectAlarm(SystemAlarm systemAlarm, final int position);
  }
}
