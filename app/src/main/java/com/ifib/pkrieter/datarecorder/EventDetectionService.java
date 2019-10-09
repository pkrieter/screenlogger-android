package com.ifib.pkrieter.datarecorder;

import android.content.Intent;
import android.support.v4.app.JobIntentService;
import android.util.Log;

public class EventDetectionService extends JobIntentService {
    private static String TAG = "EventDetectionService";
    public EventDetectionService(){super();}

    @Override
    protected void onHandleWork(Intent intent) {
        Log.i(TAG, "STARTED");
        LogManager lManager = new LogManager(getApplicationContext(), getApplicationInfo().loadLabel(getPackageManager()).toString());
        lManager.run();
    }
}
