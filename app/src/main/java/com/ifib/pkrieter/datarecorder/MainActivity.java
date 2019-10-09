package com.ifib.pkrieter.datarecorder;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DATARECORDER";
    private static final int PERMISSION_CODE = 1;
    private MediaProjectionManager mProjectionManager;
    private Switch mToggleButton;
    private boolean justUsedTheSwitch;
    private SharedPreferences prefs = null;
    private static File mainDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mProjectionManager = (MediaProjectionManager) getSystemService (Context.MEDIA_PROJECTION_SERVICE);
        mToggleButton = (Switch) findViewById(R.id.simpleSwitch);
        justUsedTheSwitch = false;

        mainDir = getMainDir(this.getApplicationContext());

        // prefs
        String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
        prefs = getSharedPreferences(appName, MODE_PRIVATE);

        // for testing
        prefs.edit().putBoolean("privacy", true).commit();

        if (prefs.getBoolean("firstrun", true)) {
            // create main dir
            if (!mainDir.exists()) {
                mainDir.mkdirs();
            }
            // create sub dirs
            String[] dirs = {"events","logfiles","debugscreens","videos","temp","tessdata"};

            for(int i = 0; i < dirs.length; i++){

                File dir = new File(mainDir + "/" + dirs[i]+"/");
                if (!dir.exists()) { dir.mkdirs(); }
            }
            // copy event screens
            copyEventscreens();

            // generate random string as pseudonym for video files
            prefs.edit().putString("alias", UUID.randomUUID().toString() ).commit();

            //privacy setting, hash private parts of logmessages (name etc.)
            prefs.edit().putBoolean("privacy", true).commit();

            // privacy secret to be added to strings
            prefs.edit().putString("secret", UUID.randomUUID().toString() ).commit();

            prefs.edit().putBoolean("firstrun", false).commit();
        }

        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleScreenShare(v);
                justUsedTheSwitch = true;
            }
        });

        if(isServiceRunning(RecordService.class)){
            mToggleButton.setChecked(true);
        }else{
            mToggleButton.setChecked(false);
        }

    }

    public static File getMainDir(Context context){

        File[] Dirs = ContextCompat.getExternalFilesDirs(context, null);
        int exStorIndex;
        // if no SD card it's 0, otherwise use SD card 1
        if(Dirs.length < 2){
            exStorIndex = 0;
        }else{
            exStorIndex = 1;
        }
        File mainDir = new File(Dirs[exStorIndex], "datarecorder");

        return mainDir;
    }

    private void copyEventscreens(){
        Field[] eventFiles = R.raw.class.getFields();
        //R.drawable.class.getFields();
        List<Integer> fileIds = new ArrayList<>();
        for (Field f : eventFiles) {
            int id = getResources().getIdentifier(f.getName(), "raw", getPackageName());
            if(id>0){
                fileIds.add( id);
            }
        }
        try{
            createFile(this.getBaseContext(), fileIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createFile(final Context context, final List<Integer> inputRawResources)throws IOException {

        final String outPutFilePathEvents = mainDir.getAbsolutePath() + "/events/";
        final String outputFileTessdata = mainDir.getAbsolutePath() + "/tessdata/";

        final Resources resources = context.getResources();
        final byte[] largeBuffer = new byte[1024 * 4];
        int totalBytes = 0;
        int bytesRead = 0;

        for (Integer resource : inputRawResources) {
            String fName = resources.getResourceEntryName(resource.intValue());
            File outFile = null;
            if(fName.contains("tessdata_")){
                fName = fName.substring(9).replace("_",".").replace("0","-");
                outFile = new File(outputFileTessdata, fName);
            }else{
                outFile = new File(outPutFilePathEvents, resources.getResourceEntryName(resource.intValue()) + ".jpg");
            }
            final OutputStream outputStream = new FileOutputStream(outFile);
            final InputStream inputStream = resources.openRawResource(resource.intValue());
            while ((bytesRead = inputStream.read(largeBuffer)) > 0) {
                if (largeBuffer.length == bytesRead) {
                    outputStream.write(largeBuffer);
                } else {
                    final byte[] shortBuffer = new byte[bytesRead];
                    System.arraycopy(largeBuffer, 0, shortBuffer, 0, bytesRead);
                    outputStream.write(shortBuffer);
                }
                totalBytes += bytesRead;
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if( !justUsedTheSwitch ){
            if( isServiceRunning(RecordService.class)  ){
                mToggleButton.setChecked(true);
            }else{
                mToggleButton.setChecked(false);
            }
        }
        justUsedTheSwitch = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode == RESULT_OK) {
            startRecordingService(resultCode, data);
        }else{
            Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);
            return;
        }
    }

    public void onToggleScreenShare(View view) {
        if ( ((Switch)view).isChecked() ) {
            // ask for permission to capture screen and act on result after
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
            Log.v(TAG, "onToggleScreenShare");
        } else {
            Log.v(TAG, "onToggleScreenShare: Recording Stopped");
            stopRecordingService();
        }
    }

    private void startRecordingService(int resultCode, Intent data){
        Intent intent = RecordService.newIntent(this, resultCode, data);
        startService(intent);
    }

    private void stopRecordingService(){
        Intent intent = new Intent(this, RecordService.class);
        stopService(intent);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
