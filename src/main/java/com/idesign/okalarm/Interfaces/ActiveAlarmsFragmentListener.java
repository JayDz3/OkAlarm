package com.idesign.okalarm.Interfaces;

public interface ActiveAlarmsFragmentListener {
  void onDeleteAlarm(final int position, long rawTime);
  void onToggleAlarm(boolean isToggled, final int position);
}
