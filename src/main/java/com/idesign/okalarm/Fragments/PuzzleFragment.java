package com.idesign.okalarm.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.idesign.okalarm.R;

public class PuzzleFragment extends Fragment {

  private OnPuzzleListener mListener;
  Button submitButton;

  public PuzzleFragment() { }

  public static PuzzleFragment newInstance() {
    return new PuzzleFragment();

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {

    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_puzzle, container, false);
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
