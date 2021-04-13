package com.eme22.animeparseres.Util;

import android.os.Handler;
import android.os.Looper;

public class DelayedAction {

    private Handler _handler;
    private Runnable _runnable;

    /**
     * Constructor
     * @param runnable The runnable
     * @param delay The delay (in milli sec) to wait before running the runnable
     */
    public DelayedAction(Runnable runnable, long delay) {
        _handler = new Handler(Looper.getMainLooper());
        _runnable = runnable;
        _handler.postDelayed(_runnable, delay);
    }

    /**
     * Cancel a runnable
     */
    public void cancel() {
        if ( _handler == null || _runnable == null ) {
            return;
        }
        _handler.removeCallbacks(_runnable);
    }}
