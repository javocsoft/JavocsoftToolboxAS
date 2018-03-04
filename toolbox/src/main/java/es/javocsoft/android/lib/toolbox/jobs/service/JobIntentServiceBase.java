package es.javocsoft.android.lib.toolbox.jobs.service;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import es.javocsoft.android.lib.toolbox.ToolBox;

/**
 *  <p>This class implements <b>{@link JobIntentService}</b>, JobIntentService works in the same way
 *  as a Service however, it enqueues the work into the {@link JobScheduler} on compatible Android
 *  targets, handling the compatibility for you if is not present.</p>
 *
 *  <p>If you want to avoid issues with background limitations since Android Oreo (v26+) with your
 *  current services, see {@link android.app.Service}, granting compatibility between new and older
 *  versions, do the following:</p>
 *
 *  1.- Add dependencies. {@link JobIntentService} needs Google Maven repository so you have to add
 *  it in your project build.gradle:
 *
 *  <pre>{@code
 *   allprojects {
 *       repositories {
 *           jcenter ()
 *           maven {
 *               url "https://maven.google.com"
 *           }
 *       }
 *   }
 *  }</pre>
 *
 *  and in your dependencies, at least v26 of compatibility support library is required:
 *  <pre>{@code
 *      compile 'com.android.support:support-compat:26.0.0'
 *  }</pre>
 *
 *  2.- Create/Modifiy your service, extending now {@link JobIntentServiceBase}
 *  and implements the methods {@link JobIntentServiceBase#onStart()}
 *  and {@link JobIntentServiceBase#onStop()} to put in
 *  them your service stuff.<br><br>
 *
 *  3.- Declare your service in your AndroidManifest.xml setting in it the permission
 *  "android.permission.BIND_JOB_SERVICE":
 *  <pre>{@code
 *      <service android:name=".ExampleJobIntentService"
 *               android:permission="android.permission.BIND_JOB_SERVICE" />
 *  }</pre>
 *
 *  4.- For pre-Oreo devices, you will have to add WAKE_LOCK permission in your manifest.xml: *
 *  <pre>{@code
 *      <uses-permission android:name="android.permission.WAKE_LOCK"/>
 *  }</pre>
 *
 *  Now you can start service as usual, Android will decide how to run the service for you. Have in
 *  consideration that:
 *  <ul>
 *      <li>When running on anything less than Android Oreo, the service will start almost instantly.
 *      On Android Oreo it will be subject to JobScheduler policies, in other words, it will not run
 *      while the device is dozing and may get delayed more than an usual service when the system
 *      is under heavy load.</li>
 *      <li>On pre Android Oreo, the service can run indefinitely but on Android Oreo it will adhere
 *      to the usual JobService execution type limits. At which point it will stop (not the process)
 *      and continue execution at a later time.</li>
 *  </ul>
 *
 *  More info at <a href="https://developer.android.com/reference/android/support/v4/app/JobIntentService.html">JobIntentService</a>
 *
 *  @author JavocSoft 2018
 *  @version 1.0
 */
public abstract class JobIntentServiceBase extends JobIntentService {

    private static final String CTAG = JobIntentServiceBase.class.getSimpleName();

    protected int jobId;
    protected Context context;

    public JobIntentServiceBase(int jobId, Class jobIntentServiceClass, Context context, Intent work) {
        this.jobId = jobId;
        this.context = context;

        enqueueWork(context, jobIntentServiceClass, jobId, work);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Job Intent Service created");

        onInitialize();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Job Intent Service started");

        onStart();
    }

    @Override
    public boolean onStopCurrentWork() {
        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Job Intent Service stopped");

        return onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Job Intent Service destroyed");

        onDestroyed();
    }


    public abstract void onInitialize();

    /**
     * This will be called if the JobScheduler has decided to stop this job.  The job for
     * this service does not have any constraints specified, so this will only generally happen
     * if the service exceeds the job's maximum execution time.
     *
     * @return True to indicate to the JobManager whether you'd like to reschedule this work,
     * false to drop this and all following work. Regardless of the value returned, your service
     * must stop executing or the system will ultimately kill it.  The default implementation
     * returns true, and that is most likely what you want to return as well (so no work gets
     * lost).
     */
    public abstract boolean onStop();

    public abstract void onStart();

    public abstract void onDestroyed();

}
