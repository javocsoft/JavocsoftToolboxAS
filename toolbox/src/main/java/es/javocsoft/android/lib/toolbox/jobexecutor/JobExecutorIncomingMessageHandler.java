package es.javocsoft.android.lib.toolbox.jobexecutor;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.util.Log;

import es.javocsoft.android.lib.toolbox.ToolBox;


/**
 * This message handler allows the communication between {@link JobServiceSchedulerHelper} and the
 * scheduled Job services {@link JobServiceBase}. This message handler will trigger START
 * and STOP ({@link JobServiceSchedulerHelper#MSG_JOB_HANDLER_START} and
 * {@link JobServiceSchedulerHelper#MSG_JOB_HANDLER_START}) events to specified external
 * optional message Handler so user can be aware of every launch and stop of scheduled Job Service.
 *
 * @author JavocSoft 2018
 * @version 1.0
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobExecutorIncomingMessageHandler extends Handler {

    private static final String CTAG = JobExecutorIncomingMessageHandler.class.getSimpleName();

    /**  */
    public static String MESSENGER_INTENT_KEY;


    /** Internal event triggered once a job service starts */
    public static final int MSG_JOB_START = 0;
    /** Internal event triggered once a job service stops */
    public static final int MSG_JOB_STOP = 1;


    private Handler handler;
    private Context context;

    /**
     * A {@link Handler} that allows you to send messages associated with a thread. A {@link Messenger}
     * uses this handler to communicate from {@link JobServiceBase}. It can be also used to make some
     * actions in the caller.
     *
     * @param handler
     */
    public JobExecutorIncomingMessageHandler(Context context, Handler handler) {
        super(context.getMainLooper());

        this.handler = handler;
        this.context = context;

        MESSENGER_INTENT_KEY = context.getApplicationContext().getPackageName() + ".MESSENGER_INTENT_KEY";

        Log.i(ToolBox.TAG, CTAG + " # Initialized (Intent Key: " + MESSENGER_INTENT_KEY + ").");
    }

    @Override
    public void handleMessage(Message msg) {
        if (context==null) {
            return;
        }

        Message m;
        switch (msg.what) {
            case MSG_JOB_START: //Receives callback from the service when a job has started
                Log.i(ToolBox.TAG, CTAG + " # Started Job service (" + (Integer)msg.obj + ")");

                //Trigger handler event if an external handler is present.
                if (handler != null) {
                    m = Message.obtain(handler, JobServiceSchedulerHelper.MSG_JOB_HANDLER_START);
                    m.obj = msg.obj;
                    handler.sendMessage(m);
                }
                break;

            case MSG_JOB_STOP: //Receives callback from the service when a job has stopped
                Log.i(ToolBox.TAG, CTAG + " # Stopped Job service (" + (Integer)msg.obj + ")");

                //Trigger handler event if an external handler is present.
                if (handler != null) {
                    m = Message.obtain(handler, JobServiceSchedulerHelper.MSG_JOB_HANDLER_STOP);
                    m.obj = msg.obj;
                    handler.sendMessage(m);
                }
                break;
        }
    }
}
