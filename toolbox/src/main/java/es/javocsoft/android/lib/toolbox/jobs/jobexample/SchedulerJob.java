package es.javocsoft.android.lib.toolbox.jobs.jobexample;

import android.app.job.JobParameters;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import es.javocsoft.android.lib.toolbox.jobs.scheduler.JobServiceBase;


/**
 * @author JavocSoft 2018
 * @version 1.0
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SchedulerJob extends JobServiceBase {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("SchedulerJob", "CREATED");
    }

    @Override
    public void onStart(JobParameters params) {
        Log.i("SchedulerJob", "START");

        jobFinished(params, true);
    }

    @Override
    public boolean onStop() {
        Log.i("SchedulerJob", "STOP");

        return false;
    }
}
