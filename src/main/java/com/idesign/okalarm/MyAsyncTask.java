package com.idesign.okalarm;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
  private RingtoneManager manager;
  private Cursor cursor;
  private List<Ringtone> ringtones;
  private WeakReference<MainActivity> weakReference;

  MyAsyncTask(MainActivity activity, RingtoneManager ringtoneManager, Cursor cursor, List<Ringtone> ringtones) {
    this.weakReference = new WeakReference<>(activity);
    this.manager = ringtoneManager;
    this.cursor = cursor;
    this.ringtones = ringtones;
  }
  protected Void doInBackground(Void... params) {
    while (cursor.moveToNext()) {
      int currentPos = cursor.getPosition();
      Ringtone ringtone = manager.getRingtone(currentPos);
      ringtones.add(ringtone);
    }
    return null;
  }

  protected void onPostExecute(Void result) {
    MainActivity reference = weakReference.get();
    Toast.makeText(reference, "DONE", Toast.LENGTH_SHORT).show();;
  }

}
