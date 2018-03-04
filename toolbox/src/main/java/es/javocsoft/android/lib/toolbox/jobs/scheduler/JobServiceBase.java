package es.javocsoft.android.lib.toolbox.jobs.scheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.jobs.scheduler.JobServiceBase;
import es.javocsoft.android.lib.toolbox.jobs.JobServiceSchedulerHelper;

/**
 * Job Service base class. Whenever you need to use an scheduled job service
 * , implement this class and customize methods "onStart" and "onStop". There is an internal
 * communication between these job services and {@link JobServiceSchedulerHelper} so you will aware
 * when the job service starts and stops.
 * <br><br>
 * Create your Job Service, an example is: *
 *
 * <pre>{@code
 *   public class JobWork extends JobServiceBase {
 *      "@Override"
 *       public void onCreate() {
 *           super.onCreate();
 *           Log.i("JobWork", "CREATED");
 *       }
 *
 *       "@Override"
 *       public void onStart(JobParameters params) {
 *           Log.i("JobWork", "START");
 *
 *           //It is important to tell to the Job Scheduler when you have finished your work.
 *           jobFinished(params, true);
 *       }
 *
 *       "@Override"
 *       public boolean onStop() {
 *           Log.i("JobWork", "STOP");
 *
 *           //Check if you need to reschedule or not. Return TRUE if you need to re-schedule.
 *           return false;
 *       }
 *   }
 * }</pre>
 *
 * <p>Once created the JobService, declare it in your manifest with the permission
 *  android.permission.BIND_JOB_SERVICE:</p>
 *  <pre>{@code
 *      <service android:name=".JobWork"
 *               android:permission="android.permission.BIND_JOB_SERVICE" />
 *  }</pre>
 *  In pre-Oreo devices, you will have to add WAKE_LOCK permission in your manifest.xml:
 *
 *  <pre>{@code
 *      <uses-permission android:name="android.permission.WAKE_LOCK"/>
 *  }</pre>
 *
 *  Have in consideration that:
 *  <ul>
 *      <li>Job Scheduler Android service is available since Android 21+ so, if you need backward
 *      compatibility with older version, use {@link JobIntentService} instead JobServiceBase.</li>
 *      <li>When running on anything less than Android Oreo (Android v21 to v25), the service will
 *      start almost instantly. On Android Oreo (v26+) it will be subject to JobScheduler policies,
 *      in other words, it will not run while the device is dozing and may get delayed more than an
 *      usual service when the system is under heavy load.</li>
 *      <li>On pre Android Oreo, the service can run indefinitely but on Android Oreo it will adhere
 *      to the usual JobService execution type limits. At which point it will stop (not the process)
 *      and continue execution at a later time.</li>
 *  </ul>
 *
 *
 * @author JavocSoft 2018
 * @version 1.0
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class JobServiceBase extends JobService {

    private static final String CTAG = JobServiceBase.class.getSimpleName();

    /** Inner handler to comunicate from job services with the JobServiceSchedulerHelper */
    private Messenger helperMessenger;


    @Override
    public void onCreate() {
        super.onCreate();

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Job Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Job Service destroyed");
    }

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Initializing internal messenger helper with intent JobExecutorIncomingMessageHandler.MESSENGER_INTENT_KEY: " + JobExecutorIncomingMessageHandler.MESSENGER_INTENT_KEY);

        helperMessenger = intent.getParcelableExtra(JobExecutorIncomingMessageHandler.MESSENGER_INTENT_KEY);

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # Job Service initialized");

        return START_NOT_STICKY;
    }



    @Override
    public boolean onStartJob(JobParameters params) {

        sendMessage(JobExecutorIncomingMessageHandler.MSG_JOB_START, params.getJobId());

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # onStartJob: " + params.getJobId());

        onStart(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        sendMessage(JobExecutorIncomingMessageHandler.MSG_JOB_STOP, params.getJobId());

        if(ToolBox.LOG_ENABLE)
            Log.d(ToolBox.TAG, CTAG + " # onStopJob: " + params.getJobId());

        return onStop();
    }

    /**
     * Add your logic to the job. Remember to use {@link JobService#jobFinished(JobParameters, boolean)}
     * once you have finished your stuff.
     *
     * @param params
     */
    public abstract void onStart(JobParameters params);

    /**
     * <p>Invoked by the system if it needs to be canceled before the job is finished. Call to this
     * method doesn’t mean your work is failed, so you have to check progress of works to resume
     * and complete it quickly at next chance.</p>
     * <ul>
     *     <li>By returning true, you can re-schedule the job that are currently running. it will be run
     *     on the next chance with little more latency</li>
     *     <li>return false to prevent this job from being scheduled again.</li>
     * </ul>
     * @return
     */
    public abstract boolean onStop();


    //AUXILIAR

    private void sendMessage(int messageID, @Nullable Object params) {
        // If this service is launched by the JobScheduler, there's no callback Messenger. It
        // only exists when the MainActivity calls startService() with the callback in the Intent.
        if (helperMessenger == null) {
            Log.d(ToolBox.TAG, CTAG + " # Job Service is bound, not started. There's no callback to send a message to.");
            return;
        }

        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;

        try {
            helperMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(ToolBox.TAG, CTAG + " # Error passing service object back to job scheduler helper.");
        }
    }
}
