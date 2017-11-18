package es.javocsoft.android.lib.toolbox.firebase.core.callback;

import android.content.Context;
import android.os.Bundle;

/**
 * This class allows to do something with the received notification.
 *
 * @author JavocSoft 2017
 * @since 2017
 */
public abstract class OnNewNotificationCallback extends Thread implements Runnable {

    private Bundle notificationBundle;
    public Context context;

    public OnNewNotificationCallback() {}

    @Override
    public void run() {
        pre_task();
        task();
        post_task();
    }

    protected abstract void pre_task();
    protected abstract void task();
    protected abstract void post_task();


    public void setNotificationBundle(Bundle notificationBundle) {
        this.notificationBundle = notificationBundle;
    }

    /**
     * Gets the notification extras.
     *
     * @return
     */
    protected Bundle getExtras() {
        return notificationBundle;
    }

    /**
     * Gets the context.
     *
     * @return
     */
    protected Context getContext(){
        return context;
    }
}