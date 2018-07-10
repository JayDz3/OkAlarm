package com.idesign.okalarm.Interfaces;


import com.idesign.okalarm.ActiveAlarm;

public interface AlarmItemListener {
  void onDeleteAlarm(int position, long rawTime);
  void onToggleAlarm(boolean isToggled, int position);
  void onAdapter();
}
