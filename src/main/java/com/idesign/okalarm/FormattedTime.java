package com.idesign.okalarm;

public class FormattedTime {
  private long _rawTime;
  private int _hour;
  private int _minute;
  private String _am_pm;
  private String _date;
  private boolean _isActive;
  private boolean _hasPlayed;
  private String _title;
  private String _itemUri;

  FormattedTime(long _rawTime, int _hour, int _minute, String _am_pm, String _date, boolean _isActive, boolean _hasPlayed, String _title, String _itemUri) {
    this._rawTime = _rawTime;
    this._hour = _hour;
    this._minute = _minute;
    this._am_pm = _am_pm;
    this._date = _date;
    this._isActive = _isActive;
    this._hasPlayed = _hasPlayed;
    this._title = _title;
    this._itemUri = _itemUri;
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

  public void set_date(String _date) {this._date = _date; };

  public void set_isActive(boolean _isActive) {
    this._isActive = _isActive;
  }

  public void set_hasPlayed(boolean _hasPlayed) { this._hasPlayed = _hasPlayed; }

  public void set_title(String _title) { this._title = _title; }

  public void set_itemUri(String _itemUri) { this._itemUri = _itemUri; }

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

  public String get_date() {
    return _date;
  }

  public boolean getIsActive() {
    return _isActive;
  }

  public boolean getHasPlayed() { return _hasPlayed; }

  public String get_title() {
    return _title;
  }

  public String get_itemUri() {
    return _itemUri;
  }
}
