package com.idesign.okalarm;

import android.content.Context;
import android.media.Ringtone;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

public class AlarmTypeAdapter extends RecyclerView.Adapter<AlarmTypeAdapter.AlarmViewHolder> {

  private List<Ringtone> mRingtones;
  private OnAlarmTypeListener mListener;
  private Context context;
  private int _activeIndex  = -1;
  static class AlarmViewHolder extends RecyclerView.ViewHolder {

    RadioButton _radioButton;

    AlarmViewHolder(View view) {
      super(view);
      _radioButton = view.findViewById(R.id.fragment_alarm_type_text);
    }
  }

  AlarmTypeAdapter(List<Ringtone> ringtones, OnAlarmTypeListener listener, Context context) {
    this.context = context;
    mRingtones = ringtones;
    setListener(listener);
  }

  private void setListener(OnAlarmTypeListener listener) {
    if (mListener == null) {
      mListener = listener;
    }
  }

  public void setItems(List<Ringtone> ringtones) {
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
    final Ringtone ringtone = mRingtones.get(position);
    viewHolder._radioButton.setText(ringtone.getTitle(context));
    viewHolder._radioButton.setOnClickListener(l -> selectRingtone(ringtone, position));
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

  private void selectRingtone(Ringtone ringtone, final int position) {
    if (_activeIndex == position) {
      _activeIndex = -1;
      ringtone = null;
    } else {
      _activeIndex = position;
    }
    mListener.onSelectAlarm(ringtone, _activeIndex);
    notifyDataSetChanged();
  }

  public interface OnAlarmTypeListener {
    void onSelectAlarm(Ringtone ringtone, final int position);
  }
}
