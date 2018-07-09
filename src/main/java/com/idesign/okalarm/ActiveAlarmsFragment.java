package com.idesign.okalarm;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ActiveAlarmsFragment extends Fragment {

  RecyclerView mRecyclerView;
  private OnActiveAlarmsListener mListener;

  public ActiveAlarmsFragment() {}

  public static ActiveAlarmsFragment newInstance() {
    return new ActiveAlarmsFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_active_alarms, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mListener = (OnActiveAlarmsListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " Must implement OnActiveAlarmsListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnActiveAlarmsListener {
    void onDeleteAlarm();
    void onCancelAlarm();
    void onEditAlarm();
    void onSetAlarm();
  }

}
