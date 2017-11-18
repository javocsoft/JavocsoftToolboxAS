package es.javocsoft.android.lib.toolbox.firebase.core.callback;

import android.content.Context;

import es.javocsoft.android.lib.toolbox.firebase.NotificationModule;

/**
 * This class allows to do something when unregistration is done.
 *
 * @author JavocSoft 2013.
 * @since 2013
 */
public abstract class OnUnregistrationCallback extends Thread implements Runnable {


    protected OnUnregistrationCallback() {}

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
     * Gets the context.
     *
     * @return
     */
    protected Context getContext(){
        return NotificationModule.APPLICATION_CONTEXT;
    }
}