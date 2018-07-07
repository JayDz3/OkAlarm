package com.idesign.okalarm.Interfaces;


import com.idesign.okalarm.FormattedTime;

public interface AlarmItemListener {
  void onDeleteAlarm(int position, long rawTime);
  void onToggleAlarm(boolean isToggled, FormattedTime formattedTime);
}
