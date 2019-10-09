package com.ifib.pkrieter.datarecorder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;

/**
 * RecordService Class
 *
 * Background service for recording the device screen.
 * Listens for commands to stop or start recording by the user
 * and by screen locked/unlock events. Notification in the
 * notification center informs user about the running recording
 * service.
 */
public final class RecordService extends Service {

    private ServiceHandler mServiceHandler;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;
    private int resultCode;
    private Intent data;
    private BroadcastReceiver mScreenStateReceiver;

    private static final String TAG = "RECORDERSERVICE";
    private static final String EXTRA_RESULT_CODE = "resultcode";
    private static final String EXTRA_DATA = "data";
    private static final int ONGOING_NOTIFICATION_ID = 23;
    private boolean screenOff;
    private boolean weAreRecording = false;

    private String deviceName;
    private String videoDir;

    /*
     *
     */
    static Intent newIntent(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, RecordService.class);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(EXTRA_DATA, data);
        return intent;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case Intent.ACTION_SCREEN_ON:
                    startRecording(resultCode, data);
                    screenOff = false;
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    screenOff = true;
                    stopRecording();
                    UploadJobService.scheduleJob(context);
                    break;
                case Intent.ACTION_CONFIGURATION_CHANGED:
                    if(!screenOff){
                        stopRecording();
                        startRecording(resultCode, data);
                    }
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    if(isConnected(context)){
                        UploadJobService.scheduleJob(context);
                    }
                    break;
            }
        }
    }

    private static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if (resultCode == RESULT_OK) {
                startRecording(resultCode, data);
            }else{
            }
        }
    }

    @Override
    public void onCreate() {
        // run this service as foreground service to prevent it from getting killed
        // when the main app is being closed
        Intent notificationIntent =  new Intent(this, RecordService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle("ScreenLogger")
                        .setContentText("Your screen is being recorded and analyzed in the background.")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setTicker("Tickertext")
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);

        // register receiver to check if the phone screen is on or off
        mScreenStateReceiver = new MyBroadcastReceiver();
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        screenStateFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mScreenStateReceiver, screenStateFilter);

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        videoDir = MainActivity.getMainDir(this.getApplicationContext()).getAbsolutePath() + "/videos/";
        // get device name
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        deviceName = myDevice.getName().replace(" ", "");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Aufnahme gestartet", Toast.LENGTH_SHORT).show();

        resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        data = intent.getParcelableExtra(EXTRA_DATA);

        if (resultCode == 0 || data == null) {
            throw new IllegalStateException("Result code or data missing.");
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return START_REDELIVER_INTENT;
    }

    private void startRecording(int resultCode, Intent data) {
        if(!weAreRecording) {

            MediaProjectionManager mProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaRecorder = new MediaRecorder();

            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getRealMetrics(metrics);

            int mScreenDensity = metrics.densityDpi;
            int displayWidth = metrics.widthPixels;
            int displayHeight = metrics.heightPixels;

            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setVideoEncodingBitRate(8 * 100 * 1);
            mMediaRecorder.setVideoFrameRate(5);
            mMediaRecorder.setVideoSize(displayWidth, displayHeight);

            Long timestamp = System.currentTimeMillis();

            String orientation = "portrait";
            if (displayWidth > displayHeight) {
                orientation = "landscape";
            }
            String filePathAndName = videoDir + "/" + "time_" + timestamp.toString() + "_mode_" + orientation + ".mp4"; // deviceName
            Log.i(TAG, "PATH: "+filePathAndName);
            mMediaRecorder.setOutputFile(filePathAndName);

            try {
                mMediaRecorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            Surface surface = mMediaRecorder.getSurface();

            mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainActivity",
                    displayWidth, displayHeight, mScreenDensity, VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                    surface, null, null);

            mMediaRecorder.start();

            Log.v(TAG, "Started recording");
            weAreRecording = true;
        }
    }

    private void stopRecording() {
        if(weAreRecording){
            // quick n dirty :-/
            try {
                mMediaRecorder.stop();
                mMediaProjection.stop();
                mMediaRecorder.release();
                mVirtualDisplay.release();
            } catch(RuntimeException e) {
                Log.i(TAG, "Stop Failed");
            }
            weAreRecording = false;
        }
    }

    private static List<File> getListFiles(File parentDir, String format) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file,format));
            } else {
                if(file.getName().endsWith(format)){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        unregisterReceiver(mScreenStateReceiver);
        stopSelf();
        Toast.makeText(this, "Aufnahme gestoppt", Toast.LENGTH_SHORT).show();
    }
}
