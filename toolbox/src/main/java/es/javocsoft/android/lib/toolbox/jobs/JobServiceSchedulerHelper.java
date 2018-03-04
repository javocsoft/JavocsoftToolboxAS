package es.javocsoft.android.lib.toolbox.jobs;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import java.util.List;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.jobs.service.JobIntentServiceBase;
import es.javocsoft.android.lib.toolbox.jobs.scheduler.JobServiceBase;
import es.javocsoft.android.lib.toolbox.jobs.scheduler.JobExecutorIncomingMessageHandler;

/**
 *  <p><b>Job Service scheduler helper</b> make easier thr usage of scheduled job services, see
 * {@link android.app.job.JobService}. You only have to implement {@link JobServiceBase},
 * customize the methods "onStart" and "onStop" to add your logic into the job and modify your
 * AndroidManifest.xml. You can schedule recurrent jobs and/or one shoot jobs. Also, you can cancel
 * all pending jobs or cancel an specific job.</p>
 *
 * <p>An example of usage:</p>
 *
 * 1.- Prepare our custom scheduled Job Service events receiver handler. Initialize the Job Service
 * Scheduler Helper.
 * <pre>{@code
 *  Handler jeh = new Handler(getApplicationContext().getMainLooper()){
 *       &#64;Override
 *       public void handleMessage(Message msg) {
 *           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
 *               switch (msg.what) {
 *                  case JobServiceSchedulerHelper.MSG_JOB_HANDLER_START:
 *                       Log.i("Job", "Custom Handler.START (jobId: " + (Integer)msg.obj + ")");
 *                       break;
 *                   case JobServiceSchedulerHelper.MSG_JOB_HANDLER_STOP:
 *                       Log.i("Job", "Custom Handler.STOP (jobId: " + (Integer)msg.obj + ")");
 *                       break;
 *                   }
 *               }
 *       }
 *   };
 *
 *   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
 *      //Initialize the Job Executor Helper
 *      jobExecutorHelper = new JobServiceSchedulerHelper(getApplicationContext(), jeh);
 *   }
 * }</pre>
 *
 * 2.- Set your Job Service, extending {@link JobServiceBase} into your AndroidManifest.xml, in our
 * example, "JobWork":
 * <pre>{@code
 *  <service android:name=".JobWork"
 *               android:permission="android.permission.BIND_JOB_SERVICE" />
 * }</pre>
 *
 * 3.- Add the WAKE_LOCK permission (for older Android devices) also in our AndroidManifest.xml:
 * <pre>{@code
 *      <uses-permission android:name="android.permission.WAKE_LOCK"/>
 * }</pre>
 *
 * 4.- Now, you can start scheduling your Job Services, always implementing {@link JobServiceBase}:
 * <pre>{@code
 *  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
 *       ApplicationBase.jobExecutorHelper.scheduleJob(1, null, null, null, null, null, null, null,
 *                                   null, null, JobWork.class, 10);
 *  }
 * }</pre>
 *
 * And that is all! Your first Job Service scheduled for Android to run some background stuff in all
 * Android devices!<br>
 *
 * <h3>Why to use Job Scheduler?</h3>
 *
 * <p>Since <b>Android Oreo</b> (26+), what can be done and how is done in background has changed and there
 * are limitations. As a resume:
 *  <ul>
 *      <li><b>Background Service Limitations</b>: While an app is idle, there are limits to its use of background
 *      services. This does not apply to foreground services, which are more noticeable to the user.</li>
 *      <li><b>Broadcast Limitations</b>: With limited exceptions, apps cannot use their manifest to register
 *      for implicit broadcasts. They can still register for these broadcasts at runtime, and they
 *      can use the manifest to register for explicit broadcasts targeted specifically at their app.</li>
 *  </ul>
 *
 *  In summary you can only start background Services from something that is considered to be in the foreground
 *  - foreground is defined in the link below. These backgound execution limits do not affect Bound
 *  Services. If you declare a Bound Service, other components can bind to that service whether or not
 *  your app is in the foreground<br><br>
 *
 *  These restrictions only apply to apps that target Android 8.0 (API level 26) or higher. However, users
 *  can enable most of these restrictions for any app from the Settings screen, even if the app targets
 *  an API level lower than 26.
 *
 *  <p>In most cases, apps can work around these limitations by using <b>JobScheduler jobs</b>, available
 *  since Android 21+. This approach lets an app arrange to perform work when the app isn't actively
 *  running, but still gives the system the leeway to schedule these jobs in a way that doesn't affect
 *  the user experience</p>
 *  <br>
 *  <b>Android JobScheduler</b> allows you to use background executions with less impact on system resources
 *  such as battery or memory, but this is not all. Before you use it, keep the followings in mind:
 *  <ul>
 *      <li>By default, JobService runs on the main thread. Therefore, if you need a relatively long
 *      runtime or complex operation, you must create a new thread or some other way to implement
 *      asynchronous behavior.</li>
 *      <li>id is a important key for identifying your job. Whether you use a static id or a dynamic
 *      id, it depends on the scenario, but in most cases a static id is enough.</li>
 *      <li>Changing any of the execution conditions would stop current Job.</li>
 *      <li>More complex conditions of a job are make job harder to be executed and maintained . It
 *      is important to determine the appropriate scenario between the consumption of the battery
 *      and the frequency of its execution.</li>
 *      <li>In addition, you should have a clear pause / resume scenarios to implement functionality
 *      like a transaction, or to be able to resume at the last point in progress, in case the job
 *      is canceled suddenly.</li>
 *      <li>Job execution shares their life with Wakelock. The number of WakeLock and the time are
 *      critical value to calculate the battery drain for that app. This can result in a negative
 *      experience in the battery consumption that is monitored by the user if the job is
 *      eventually triggered too often</li>
 *  </ul>
 *
 *  <p>So, as a good practice, you should start using <b>Job Services</b>, by using this helper,
 *  instead usual background services for devices with Android 21+ but, what happens with Android
 *  minor than API level 21? Do we have to create different code for older Android versions? No, To
 *  ensure backwards compatibility (older than Android 21), you should use {@link JobIntentService}
 *  instead usual Service or IntentService. See {@link JobIntentServiceBase}.
 *  </p>
 *
 *  <p>More info at see <a href="https://developer.android.com/about/versions/oreo/background.html">Background Execution Limits</a></p>
 *
 *  <br><br>
 * <b>About Android Doze and App Standby</b><br>
 * Doze and App Standby manage the behavior of all apps running on Android 6.0 or higher, regardless
 * whether they are specifically targeting API level 23. See more at
 * <a href="https://developer.android.com/training/monitoring-device-state/doze-standby.html#testing_doze_and_app_standby">Android Doze and App Standby</a>.
 *
 * @author JavocSoft 2018
 * @version 1.0
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobServiceSchedulerHelper {

    private static final String CTAG = JobServiceSchedulerHelper.class.getSimpleName();

    /** Message sent to external message handler along with the Job Service Id in the "obj" parameter. */
    public static final int MSG_JOB_HANDLER_START = 10;
    /** Message sent to external message handler along with the Job Service Id in the "obj" parameter. */
    public static final int MSG_JOB_HANDLER_STOP = 11;


    /** Internal message channel between scheduled job services and this helper. */
    private JobExecutorIncomingMessageHandler helperHandler;

    private Context context;
    private Handler handler;

    /**
     *  Job Scheduler helper.
     *
     * @param context   The context from is being used the Job Service helper.
     * @param handler   A handler that is going to receive events of your programmed job
     *                  services. See {@link JobServiceSchedulerHelper#MSG_JOB_HANDLER_START} and
     *                  {@link JobServiceSchedulerHelper#MSG_JOB_HANDLER_STOP}
     */
    public JobServiceSchedulerHelper(Context context, Handler handler){
        this.context = context;
        this.handler = handler;

        initialize();
    }


    //AUXILIAR

    private void initialize() {
        helperHandler = new JobExecutorIncomingMessageHandler(context, handler);
    }


    /**
     * Schedules a Job Service.
     *
     * @param jobId A unique job identifier. JobId can be used to cancel the job once is scheduled.
     * @param delay Specify that this job should be delayed by the provided amount of time. Not used if
     *              parameter "executionInterval" is set.
     * @param maxExecutionDelay Set deadline which is the maximum scheduling latency. Not used if
     *                          parameter "executionInterval" is set.
     * @param networkType   See {@link JobInfo} NETWORK_TYPE_*. If not set {@link JobInfo#NETWORK_TYPE_NONE} is set.
     * @param requiresIdle  Specify that to run, the job needs the device to be in idle mode. This defaults to false.
     * @param requiresCharging  Specify that to run this job, the device needs to be plugged in. This defaults to false.
     * @param extraInfo A mapping from String keys to values of various types. The set of types supported by this
     *                  parameter is purposefully restricted to simple objects that can safely be persisted
     *                  to and restored from disk.
     * @param rebootProof   Set whether or not to persist this job across device reboots. <b>Requires permission
     *                      android.Manifest.permission.RECEIVE_BOOT_COMPLETED</b>. Set to true to indicate
     *                      that the job will be written to disk and loaded at boot.
     * @param batteryNotLow Specify that to run this job, the device's battery level must not be low. This
     *                      defaults to false.  If true, the job will only run when the battery level
     *                      is not low, which is generally the point where the user is given a "low battery"
     *                      warning. Only since Android Oreo (26+)
     * @param storageNotLow Specify that to run this job, the device's available storage must not be low.
     *                      This defaults to false.  If true, the job will only run when the device is not
     *                      in a low storage state, which is generally the point where the user is given a
     *                      "low storage" warning. Only since Android Oreo (26+)
     * @param jobServiceBaseClass   A Job Service class. See {@link JobServiceBase}.
     * @param executionInterval Set a value to schedule the job to be run in specified intervals.
     * @return TRUE if job is successfully scheduled, otherwise FALSE.
     * */
    @SuppressLint("MissingPermission")
    public boolean scheduleJob(int jobId, Integer delay, Integer maxExecutionDelay, Integer networkType,
                               Boolean requiresIdle, Boolean requiresCharging, PersistableBundle extraInfo,
                               Boolean rebootProof, Boolean batteryNotLow, Boolean storageNotLow,
                               Class jobServiceBaseClass, Integer executionInterval) {

        boolean result = false;

        //1
        Messenger messengerIncoming = new Messenger(helperHandler);
        Intent startServiceIntent = new Intent(context, jobServiceBaseClass);
        startServiceIntent.putExtra(JobExecutorIncomingMessageHandler.MESSENGER_INTENT_KEY, messengerIncoming);
        context.startService(startServiceIntent);

        //2
        ComponentName mServiceComponent = new ComponentName(context, jobServiceBaseClass);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, mServiceComponent);

        if(executionInterval!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if( (Long.valueOf(executionInterval) * 1000) < JobInfo.getMinPeriodMillis() ){
                    Log.i(ToolBox.TAG, CTAG + " # Specified 'executionInterval' is minor than minimum period time.");
                }
            }
            builder.setPeriodic(Long.valueOf(executionInterval) * 1000);
        }else{
            if (delay!=null) {
                builder.setMinimumLatency(Long.valueOf(delay) * 1000);
            }
            if (maxExecutionDelay!=null) {
                builder.setOverrideDeadline(Long.valueOf(maxExecutionDelay) * 1000);
            }
        }

        if (networkType!=null) {
            builder.setRequiredNetworkType(networkType);
        }
        if(requiresIdle!=null) {
            builder.setRequiresDeviceIdle(requiresIdle);
        }
        if(requiresCharging!=null) {
            builder.setRequiresCharging(requiresCharging);
        }
        if(rebootProof!=null){
            builder.setPersisted(rebootProof);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(batteryNotLow!=null){
                builder.setRequiresBatteryNotLow(batteryNotLow);
            }
            if(storageNotLow!=null){

                builder.setRequiresStorageNotLow(storageNotLow);
            }
        }

        if(extraInfo!=null) {
            builder.setExtras(extraInfo);
        }

        // Schedules the job
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int res = tm.schedule(builder.build());
        if (res == JobScheduler.RESULT_SUCCESS) {
            Log.d(ToolBox.TAG, CTAG + " # Job Service scheduled successfully");
            result = true;
        } else {
            Log.d(ToolBox.TAG, CTAG + " # Job Service scheduling failed");
        }

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Scheduled (" + (executionInterval!=null?"recurrent: every " + executionInterval + " seconds":"one-shoot") + ") job service with id: " + jobId);

        return result;
    }

    /**
     * Cancels all pending jobs.
     */
    public void cancelAllJobs() {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # cancelled all pending (" + jobScheduler.getAllPendingJobs().size() + ") jobs.");
    }

    /**
     * Finished a pending job.
     *
     * @param jobId The job Id of the job service you want to cancel.
     */
    public void cancelJob(int jobId) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
        for(JobInfo j:allPendingJobs){
            if(j.getId()==jobId){
                jobScheduler.cancel(jobId);
                if(ToolBox.LOG_ENABLE)
                    Log.d(ToolBox.TAG, CTAG + " # cancelled JobId: " + jobId);
            }
        }
    }

}
