package com.idesign.okalarm.Factory;

public class SystemAlarm {

  private String _title;
  private String _itemUri;

  public SystemAlarm(String _title, String _itemUri) {
    this._title = _title;
    this._itemUri = _itemUri;
  }

  public String get_title() {
    return _title;
  }

  public String get_itemUri() {
    return _itemUri;
  }
}
