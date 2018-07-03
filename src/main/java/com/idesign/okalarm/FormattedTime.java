package com.idesign.okalarm;

public class FormattedTime {
  private long _rawTime;
  private int _hour;
  private int _minute;
  private String _am_pm;
  private boolean _isActive;

  FormattedTime(long _rawTime, int _hour, int _minute, String _am_pm, boolean _isActive) {
    this._rawTime = _rawTime;
    this._hour = _hour;
    this._minute = _minute;
    this._am_pm = _am_pm;
    this._isActive = _isActive;
  }

  public void set_rawTime(long _rawTime) {
    this._rawTime = _rawTime;
  }

  public void set_hour(int _hour) {
    this._hour = _hour;
  }

  public void set_minute(int _minute) {
    this._minute = _minute;
  }

  public void set_am_pm(String _am_pm) {
    this._am_pm = _am_pm;
  }

  public void set_isActive(boolean _isActive) {
    this._isActive = _isActive;
  }

  public long get_rawTime() {
    return _rawTime;
  }

  public int get_hour() {
    return _hour;
  }

  public int get_minute() {
    return _minute;
  }

  public String get_am_pm() {
    return _am_pm;
  }

  public boolean getIsActive() {
    return _isActive;
  }
}
