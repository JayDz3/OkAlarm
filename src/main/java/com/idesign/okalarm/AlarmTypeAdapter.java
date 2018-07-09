package com.idesign.okalarm;

import android.content.Context;
import android.media.Ringtone;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AlarmTypeAdapter extends RecyclerView.Adapter<AlarmTypeAdapter.AlarmViewHolder> {

  private List<Ringtone> mRingtones;
  private OnAlarmTypeListener mListener;
  private Context context;
  private AlarmViewHolder currentSelectedView;
  static class AlarmViewHolder extends RecyclerView.ViewHolder {

    TextView uriView;

    AlarmViewHolder(View view) {
      super(view);
      uriView = view.findViewById(R.id.fragment_alarm_type_text);
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

  @Override
  @NonNull
  public AlarmTypeAdapter.AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_type_item, parent, false);
    return new AlarmViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AlarmTypeAdapter.AlarmViewHolder viewHolder, final int position) {
    final Ringtone ringtone = mRingtones.get(position);
    viewHolder.uriView.setText(ringtone.getTitle(context));
    viewHolder.itemView.setOnClickListener(l -> selectRingtone(ringtone, viewHolder));
  }

  @Override
  public int getItemCount() {
    return mRingtones.size();
  }

  private void selectRingtone(Ringtone ringtone, AlarmViewHolder viewHolder) {
    mListener.onSelectAlarm(ringtone);
    if (currentSelectedView != null) {
      currentSelectedView.itemView.setBackgroundColor(context.getColor(R.color.colorTransparent));
    }
    currentSelectedView = viewHolder;
    viewHolder.itemView.setBackgroundColor(context.getColor(R.color.colorLightGray));
  }

  public interface OnAlarmTypeListener {
    void onSelectAlarm(Ringtone ringtone);
  }
}
