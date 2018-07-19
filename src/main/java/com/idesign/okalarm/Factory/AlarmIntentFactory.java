package com.idesign.okalarm.Factory;

import android.net.Uri;
import java.util.Calendar;

public class AlarmIntentFactory {

 public AlarmIntentFactory() { }

  public final ActiveAlarm activeAlarm(int hourOfDay, int minute, int volume, String am_pm, String _title, Uri itemUri) {
    final Calendar calendar = Calendar.getInstance();
    final Calendar today = Calendar.getInstance();

    setCalendarValues(calendar, hourOfDay, minute);
    long calMilliseconds = calendar.getTimeInMillis();

    if (am_pm.equals("AM") && today.get(Calendar.AM_PM) != Calendar.AM || today.getTimeInMillis() > calMilliseconds) {
      calMilliseconds += (1000 * 60 * 60 * 24);
      calendar.setTimeInMillis(calMilliseconds);
    }

    final int baseHour = am_pm.equals("AM") ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);
    final int hour = getHourByInteger(baseHour);
    final String _month = getMonth(calendar.get(Calendar.MONTH));
    final String _date = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    final String _combined = _month + " " + _date;
    return new ActiveAlarm(calendar.getTimeInMillis(), hour, calendar.get(Calendar.MINUTE), volume, am_pm, _combined, true, false, _title, itemUri.toString());
  }


  private void setCalendarValues(Calendar calendar, int hourOfDay, int minute) {
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  private int getHourByInteger(int hourOfDay) {
    return hourOfDay == 0 ? 12 : hourOfDay;
  }

  private String getMonth(int _month) {
    switch (_month) {
      case 0:
        return "Jan";
      case 1:
        return "Feb";
      case 2:
        return "Mar";
      case 3:
        return "Apr";
      case 4:
        return "May";
      case 5:
        return "Jun";
      case 6:
        return "Jul";
      case 7:
        return "Aug";
      case 8:
        return "Sep";
      case 9:
        return "Oct";
      case 10:
        return "Nov";
      case 11:
        return "Dec";
      default:
        return "None";
    }
  }
}
