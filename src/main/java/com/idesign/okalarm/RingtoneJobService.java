package com.idesign.okalarm;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

public class RingtoneJobService extends JobService {

  public boolean onStartJob(JobParameters params) {
    Intent intent = new Intent(this, RingtoneService.class);

    intent.putExtra(Constants.EXTRA_URI, params.getExtras().getString(Constants.EXTRA_URI));
    intent.putExtra(Constants.EXTRA_VOLUME, params.getExtras().getInt(Constants.EXTRA_VOLUME));
    startService(intent);
    jobFinished(params, false);
    return true;
  }

  public boolean onStopJob(JobParameters params) {

    return false;
  }
}
