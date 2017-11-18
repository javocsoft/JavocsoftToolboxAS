package es.javocsoft.android.lib.toolbox.firebase.core.callback;

import android.content.Context;
import android.os.Bundle;

import es.javocsoft.android.lib.toolbox.firebase.NotificationModule;

/**
 * This class allows to do something when user opens the notification.
 *
 * @author JavocSoft 2017.
 * @since 2017
 */
public abstract class OnOpenAckCallback extends Thread implements Runnable {

    private Bundle notificationBundle;

    public OnOpenAckCallback() {}

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
        return NotificationModule.APPLICATION_CONTEXT;
    }
}
