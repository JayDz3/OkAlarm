package com.idesign.okalarm.Interfaces;

import com.idesign.okalarm.FormattedTimesAdapter;

public interface AlarmItemListener {
  void onDelete(int position);
  void onToggle(boolean isToggled, final int position);
}
