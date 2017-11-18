package es.javocsoft.android.lib.toolbox.firebase.core.callback;

import android.content.Context;

import es.javocsoft.android.lib.toolbox.firebase.NotificationModule;

/**
 * This class allows to do something when registration is done.
 *
 * @author JavocSoft 2017.
 * @since 2017
 */
public abstract class OnRegistrationCallback extends Thread implements Runnable {


    protected OnRegistrationCallback() {}

    @Override
    public void run() {
        pre_task();
        task();
        post_task();

    }

    protected abstract void pre_task();
    protected abstract void task();
    protected abstract void post_task();


    /**
     * Gets the Firebase registration token.
     *
     * @return
     */
    protected String getFirebaseRegistrationToken() {
        if(getContext()!=null)
            return NotificationModule.getRegistrationId(getContext());
        else
            return null;
    }

    /**
     * Gets the context.
     *
     * @return
     */
    protected Context getContext(){
        return NotificationModule.APPLICATION_CONTEXT;
    }
}