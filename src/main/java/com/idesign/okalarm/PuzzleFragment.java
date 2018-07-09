package com.idesign.okalarm;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PuzzleFragment extends Fragment {
  private static final String ARG_HOUR_OF_DAY = "hourOfDay";
  private static final String ARG_MINUTE = "minute";
  private static final String ARG_AM_PM = "am_pm";

  private int _hourOfDay;
  private int _minute;
  private String _am_pm;

  private OnPuzzleListener mListener;
  Button submitButton;

  public PuzzleFragment() { }

  public static PuzzleFragment newInstance(int _hourOfDay, int _minute, String _am_pm) {
    PuzzleFragment fragment = new PuzzleFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_HOUR_OF_DAY, _hourOfDay);
    args.putInt(ARG_MINUTE, _minute);
    args.putString(ARG_AM_PM, _am_pm);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    submitButton = view.findViewById(R.id.puzzle_submit_answer);
    submitButton.setOnClickListener(l -> stopRingtone());
  }

  public void stopRingtone() {
    mListener.onAnswer(2);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      _hourOfDay = getArguments().getInt(ARG_HOUR_OF_DAY);
      _minute = getArguments().getInt(ARG_MINUTE);
      _am_pm = getArguments().getString(ARG_AM_PM);
    }
  }

  public int get_hourOfDay() {
    return _hourOfDay;
  }

  public int get_minute() {
    return _minute;
  }

  public String get_am_pm() {
    return _am_pm;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_puzzle, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mListener = (OnPuzzleListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " Must implement OnPuzzleListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnPuzzleListener {
    void onAnswer(int answer);
    void onAnswer(String answer);
  }
}
