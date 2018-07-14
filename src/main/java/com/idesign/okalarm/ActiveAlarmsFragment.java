package com.idesign.okalarm;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idesign.okalarm.Interfaces.ActiveAlarmsFragmentListener;
import com.idesign.okalarm.ViewModels.ActiveAlarmsViewModel;

public class ActiveAlarmsFragment extends Fragment implements ActiveAlarmsAdapter.OnActiveAlarmAdapterListener {

  RecyclerView mRecyclerView;
  private ActiveAlarmsAdapter mActiveAlarmsAdapter;
  private ActiveAlarmsFragmentListener mListener;

  ActiveAlarmsViewModel model;

  public ActiveAlarmsFragment() {}

  public static ActiveAlarmsFragment newInstance() {
    return new ActiveAlarmsFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    model = ViewModelProviders.of(getActivity()).get(ActiveAlarmsViewModel.class);
    model.getItems().observe(this, items -> {
      mActiveAlarmsAdapter.setList(items);
    });
    mActiveAlarmsAdapter = new ActiveAlarmsAdapter(model.getItems().getValue(),ActiveAlarmsFragment.this);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_active_alarms, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRecyclerView = view.findViewById(R.id.active_alarms_recycler_view);

    DividerItemDecoration itemDecoration = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setAdapter(mActiveAlarmsAdapter);
  }

  public void onToggleAlarm(boolean isToggled, final int position) {
    mListener.onToggleAlarm(isToggled, position);
  }

  public void onDeleteAlarm(final int position, long rawTime) {
    mListener.onDeleteAlarm(position, rawTime);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mListener = (ActiveAlarmsFragmentListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " Must implement OnActiveAlarmsListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

}
