package es.javocsoft.android.lib.toolbox.jobs.jobexample;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * @author JavocSoft 2018
 * @version 1.0
 */
public class FirebaseDispatcherJob extends JobService {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("FirebaseDispatcherJob", "CREATED");
    }

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        Log.i("FirebaseDispatcherJob", "START");

        completeJob(job);

        return true;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        Log.i("FirebaseDispatcherJob", "STOP");

        return false;
    }


    //AUXILIAR

    public void completeJob(final JobParameters parameters) {
        try {
            //This task takes 2 seconds to complete.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //Tell the framework that the job has completed and does not needs to be reschedule
            jobFinished(parameters, true);
        }
    }
}
