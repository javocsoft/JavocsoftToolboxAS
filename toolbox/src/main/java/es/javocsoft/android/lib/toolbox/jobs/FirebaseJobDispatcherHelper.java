package es.javocsoft.android.lib.toolbox.jobs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.jobs.service.JobIntentServiceBase;


/**
 * <p>As an a more backwards compatibility Job scheduling, Google presented <b>Firebase Job Scheduler</b>.
 * <u>This class is a helper class</u> for scheduling Job Services with Firebase Job Dispatcher. Use
 * this class to create scheduled recurrent/non-recurrent tasks. For using a common service instead
 * of a Job, see ToolBox {@link JobIntentServiceBase}.</p>
 *
 * <p>Until <b>Firebase Job Scheduler</b>, most of us were familiar with JobScheduler API, see
 * ToolBox {@link JobServiceSchedulerHelper} and Android {@link android.app.job.JobScheduler} which
 * was introduced in API 21. Before JobScheduler, we had AlarmManager API, see {@link android.app.AlarmManager}
 * which was present since Android version 1. Although significant changes and improvements were made
 * to this API from version 19, to save battery of the device and boost the performance, the biggest
 * drawback of Alarm Manager was that it solely works on the basis of time. To overcome this drawback,
 * Google introduced JobScheduler which works on various conditions like availability of network,
 * charging of device.</p>
 *
 * JobScheduler was good news for all the developers whose app have minimum SDK version set to 21 or
 * greater as they can use this API directly. But what about the apps whose minimum SDK is less
 * than 21?. If this is your case, there are a couple of options available to the developers.
 * <ul>
 *     <li><b>GcmNetworkManager</b>. Android bundled this module in Google Play services version 7.5. If
 *     your application is running on Lollipop or above, GcmNetworkManager will use the framework’s
 *     JobScheduler, so there is almost no difference between GcmNetworkManager and JobScheduler.
 *     For those platforms below Lollipop, this class will only work if the Google Play services
 *     is installed on the device and is having a version greater or equal to 7.5.<br>
 *     <u>Note</u>: Google has stopped active development on this module and instead recommends using
 *     Firebase Job Dispatcher for scheduling any task.</li>
 *     <li><b>Firebase Job Dispatcher</b>. This library is a wrapper over GcmNetworkManager. But
 *     there is one big advantage it holds over GcmNetworkManager. If Google Play Services app is not
 *     installed or the app version is less than 7.5, then this library internally uses AlarmManager
 *     to schedule the tasks. This can work up to minimum SDK 9. Similar to GcmNetworkManager, this
 *     library uses the framework’s JobScheduler if the application is running on Lollipop and above.</li>
 * </ul>
 *
 * <p>So this class is a helper to make easier for you the usage of Firebase Job Scheduler. Follow these
 * steps in order to successfully schedule a job:</p>
 *
 * 1.- Add the dependency to Firebase Job Dispatcher:
 *
 * <pre>
 *  implementation 'com.firebase:firebase-jobdispatcher:0.8.5'
 * </pre>
 *
 * 2.- Create your Firebase Dispatcher Job Service, see {@link com.firebase.jobdispatcher.JobService}.
 * Extracted from Google docs:
 * <pre>
 *  import com.firebase.jobdispatcher.JobParameters;
 *  import com.firebase.jobdispatcher.JobService;
 *
 *  public class MyJobService extends com.firebase.jobdispatcher.JobService {
 *      &#64;Override
 *      //When your job is called by the framework, onStartJob() method will be invoked which runs
 *      //on the main thread. This method returns a boolean which tells the framework whether there
 *      //is more work remaining. We should consider offloading the code in onStartJob() to a new
 *      //thread and return true. Returning true here tells the framework that more work is
 *      //remaining. As soon as the job is completed we can call jobFinished() method which will
 *      //indicate that the work for this job cycle is completed. If the code involves only some
 *      //basic operations, then you can complete it in the main thread itself and returns false
 *      //which means no more work is remaining.
 *      public boolean onStartJob(JobParameters job) {
 *          // Do some work here
 *
 *          return false; // Answers the question: "Is there still work going on?"
 *      }
 *      &#64;Override
 *      //This method is called when your job is stopped by the framework. The job can be stopped
 *      //due to various reasons like the running constraints associated with the job are no longer
 *      //satisfied. The method returns a boolean variable which tells whether the job should be
 *      //tried again or not. If returned true, then the framework will put up this job again for
 *      //execution.
 *      public boolean onStopJob(JobParameters job) {
 *          return false; // Answers the question: "Should this job be retried?"
 *      }
 *   }
 * </pre>
 * <p><b>NOTE</b>: When a job had been offloaded to a new thread and onStartJob() method returns true,
 * when the job in the new thread completes, we need to call
 * jobFinished(JobParameters params, boolean needsReschedule) method. Always make sure to inform the
 * framework explicitly that the work for this job has been completed using jobFinished() method .
 * If you fail to do so, the framework will consider the job as still running even if the job is
 * not running.</p>
 *
 * 3.- Add your Job Service to your application AndroidManifest.xml:
 *
 * <pre>{@code
 *  <service
 *      android:exported="false"
 *      android:name=".MyJobService">
 *          <intent-filter>
 *              <action android:name="com.firebase.jobdispatcher.ACTION_EXECUTE"/>
 *          </intent-filter>
 *  </service>
 * }</pre>
 *
 * More info about this library Google Firebase Library on GitHub
 * <a href="https://github.com/firebase/firebase-jobdispatcher-android">GitHub firebase-jobdispatcher-android</a>
 * <br><br>
 * <b>About Android Doze and App Standby</b><br>
 * Doze and App Standby manage the behavior of all apps running on Android 6.0 or higher, regardless
 * whether they are specifically targeting API level 23. See more at
 * <a href="https://developer.android.com/training/monitoring-device-state/doze-standby.html#testing_doze_and_app_standby">Android Doze and App Standby</a>.
 *
 * @author JavocSoft 2018
 * @version 1.0
 */
public class FirebaseJobDispatcherHelper {

    private Context context;

    private FirebaseJobDispatcher fcmDispatcher;


    public FirebaseJobDispatcherHelper(Context context){
        this.context = context;

        // Create a new dispatcher using the Google Play driver.
        fcmDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
    };


    /**
     * // start between 0 and 60 seconds from now
     *
     * @param jobService    The job to run. See {@link com.firebase.jobdispatcher.JobService}
     * @param jobId         An unique id of the task to run (the Job)
     * @param replaceJob    Set to TRUE to overwrite an existing job with the same jobId. If set to FALSE,
     *                      then the newly created task will be rejected by the Android framework.
     * @param rebootProof   Set to TRUE to make the job to persists across device boots. If set to TRUE,
     *                      permission RECEIVE_BOOT_COMPLETED must be set in your AndroidManifest.xml.
     * @param runIntervalSeconds  The intervals, in seconds, to run the job
     * @param flexStartSeconds   Seconds in which Job can be started.
     * @param retryStrategy The retry strategy if job is stopped or has problems, see {@link RetryStrategy}
     * @param extras    You can attach some extras to the job.
     * @param constraints   Constraints that need to be satisfied for the job to run, see {@link Constraint}
     * @return
     */
    public boolean scheduleRecurrentJob(Class jobService, String jobId, Boolean replaceJob,
                                        Boolean rebootProof,
                                        Integer runIntervalSeconds,
                                        Integer flexStartSeconds,
                                        RetryStrategy retryStrategy, Bundle extras,
                                        @Constraint.JobConstraint int... constraints){
        boolean res = false;

        Job.Builder jBuilder = fcmDispatcher.newJobBuilder()
                .setService(jobService)
                .setTag(jobId)
                .setLifetime(((rebootProof!=null && rebootProof)?Lifetime.FOREVER:Lifetime.UNTIL_NEXT_BOOT))
                .setRecurring(true)
                .setTrigger(createPeriodicTrigger(runIntervalSeconds,flexStartSeconds))
                .setReplaceCurrent(((replaceJob!=null && replaceJob)?replaceJob:false))
                .setRetryStrategy((retryStrategy!=null?retryStrategy:RetryStrategy.DEFAULT_EXPONENTIAL))
                .setConstraints(constraints);

        if(extras!=null)
            jBuilder.setExtras(extras);

        Job job = jBuilder.build();

        try{
            fcmDispatcher.mustSchedule(job);
            res = true;
        }catch(FirebaseJobDispatcher.ScheduleFailedException e){
            Log.e(ToolBox.TAG, "FirebaseJobDispatcherHelper # Error scheduling one-shoot Job Service with Id: " + jobId, e);
        }

        return res;
    }

    /**
     * // start between 0 and 60 seconds from now
     *
     * @param jobService    The job to run. See {@link com.firebase.jobdispatcher.JobService}
     * @param jobId         An unique id of the task to run (the Job)
     * @param replaceJob    Set to TRUE to overwrite an existing job with the same jobId. If set to FALSE,
     *                      then the newly created task will be rejected by the Android framework.
     * @param rebootProof   Set to TRUE to make the job to persists across device boots. If set to TRUE,
     *                      permission RECEIVE_BOOT_COMPLETED must be set in your AndroidManifest.xml
     * @param delaySecondsFromNow   Delay, in seconds, before starting the job.
     * @param flexibleStartSeconds  A flexible number of seconds to start the job
     * @param retryStrategy The retry strategy if job is stopped or has problems, see {@link RetryStrategy}
     * @param extras    You can attach some extras to the job.
     * @param constraints   Constraints that need to be satisfied for the job to run, see {@link Constraint}
     * @return  TRUE if operation was done without errors, otherwise FALSE.
     */
    public boolean scheduleOneShootJob(Class jobService, String jobId, Boolean replaceJob,
                                       Boolean rebootProof,
                                       Integer delaySecondsFromNow,
                                       Integer flexibleStartSeconds,
                                       RetryStrategy retryStrategy, Bundle extras,
                                       @Constraint.JobConstraint int... constraints){
        boolean res = false;

        Job.Builder jBuilder = fcmDispatcher.newJobBuilder()
                .setService(jobService)
                .setTag(jobId)
                .setLifetime(((rebootProof!=null && rebootProof)?Lifetime.FOREVER:Lifetime.UNTIL_NEXT_BOOT))
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow((delaySecondsFromNow!=null?delaySecondsFromNow:0), (flexibleStartSeconds!=null?flexibleStartSeconds:0)))
                .setReplaceCurrent(((replaceJob!=null && replaceJob)?replaceJob:false))
                .setRetryStrategy((retryStrategy!=null?retryStrategy:RetryStrategy.DEFAULT_EXPONENTIAL))
                .setConstraints(constraints);

        if(extras!=null)
            jBuilder.setExtras(extras);

        Job job = jBuilder.build();

        try{
            fcmDispatcher.mustSchedule(job);
            res = true;
        }catch(FirebaseJobDispatcher.ScheduleFailedException e){
            Log.e(ToolBox.TAG, "FirebaseJobDispatcherHelper # Error scheduling one-shoot Job Service with Id: " + jobId, e);
        }

        return res;
    }

    /**
     * Cancels the specified Job
     *
     * @param jobId An unique id of the task to run (the Job)
     * @return  TRUE if operation was done without errors, otherwise FALSE.
     */
    public boolean cancelScheduledJob (String jobId) {
        if(fcmDispatcher.cancel(jobId)!=FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS)
            return false;
        else
            return true;
    }

    /**
     * Cancels al scheduled jobs.
     *
     * @return  TRUE if operation was done without errors, otherwise FALSE.
     */
    public boolean cancelAll() {
        if(fcmDispatcher.cancelAll()!=FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS)
            return false;
        else
            return true;
    }


    //AuXiliar

    /**
     * Internaly, in dispatcher:
     *
     * <pre>{@code
     *      b.putLong(REQUEST_PARAM_TRIGGER_WINDOW_PERIOD, trigger.getWindowEnd());
     *      b.putLong(REQUEST_PARAM_TRIGGER_WINDOW_FLEX, trigger.getWindowEnd() - trigger.getWindowStart());
     * }</pre>
     *
     * So period should be provided in windowEnd and period-flex should be provided in windowStart.
     * Therefore the correct call would be Trigger.executionWindow(period-flex, period).
     *
     * @param period
     * @param flex
     * @return
     */
    public static JobTrigger createPeriodicTrigger(int period, Integer flex) {
        return Trigger.executionWindow((flex!=null?(period-flex):0), period);
    }
}