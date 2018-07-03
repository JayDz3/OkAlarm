package com.idesign.okalarm;

public class FormattedTime {
  private int _hour;
  private int _minute;
  private boolean _isActive;

  FormattedTime(int _hour, int _minute, boolean _isActive) {
    this._hour = _hour;
    this._minute = _minute;
    this._isActive = _isActive;
  }

  public void set_hour(int _hour) {
    this._hour = _hour;
  }

  public void set_minute(int _minute) {
    this._minute = _minute;
  }

  public void set_isActive(boolean _isActive) {
    this._isActive = _isActive;
  }

  public int get_hour() {
    return _hour;
  }

  public int get_minute() {
    return _minute;
  }

  public boolean getIsActive() {
    return _isActive;
  }
}
