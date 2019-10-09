package com.ifib.pkrieter.datarecorder;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class LogManager {

    private static final String TAG = "LOGMANAGER";
    private VideoManager vManager;
    private EventManager eManager;
    private OCRManager ocrManager;
    private static File root;
    private List<LogEntry> tempLogFile;
    private List<File> videoFiles;
    private List<File> logFiles;
    private boolean run;
    private Context context;
    private static SharedPreferences prefs;
    private boolean privacyPref;
    private String secret;

    LogManager(Context context, String appName){
        this.context =context;
        eManager = new EventManager(context);
        ocrManager = new OCRManager(context);
        root = MainActivity.getMainDir(context);
        run = false;
        prefs = context.getSharedPreferences(appName, MODE_PRIVATE);

        privacyPref = prefs.getBoolean("privacy",true);
        secret = prefs.getString("secret","nosecret");
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

    public void run(){

        // get list of videos and files
        videoFiles = getListFiles(new File(root + "/videos/"),".mp4");

        if (videoFiles != null && !videoFiles.isEmpty() && videoFiles.size() > 1){
            // keep the latest screen recording
            int delete = 0;
            long latest = videoFiles.get(0).lastModified();
            for(int i = 0; i < videoFiles.size(); i++){
                if( videoFiles.get(i).lastModified() > latest){
                    latest = videoFiles.get(i).lastModified();
                    delete = i;
                }
            }
            videoFiles.remove(delete);

            int i = 0;
            while(i < videoFiles.size()){

                startEventDetection( videoFiles.get(i) );
                AsyncTask.execute(new Runnable() { public void run() { uploadLogFiles(); }});

                i++;
            }
        }

    }

    private void startEventDetection(File videoFilePath) {
        // update privacy field
        privacyPref = prefs.getBoolean("privacy",true);

        // right now only process videos in portait mode
        if(videoFilePath.getAbsolutePath().contains("portrait")) {

            vManager = new VideoManager(videoFilePath.getAbsolutePath());
            tempLogFile = new ArrayList<>();

            final List<EventDefinition> myEvents = eManager.getEventList();
            Frame currentFrame = vManager.getNextFrame();

            int fcounter = 0;

            // frame by frame
            while (currentFrame != null) {

                tempLogFile.add(checkFrameForEvents(currentFrame, myEvents, fcounter));
                fcounter++;
                currentFrame = vManager.getNextFrame();
            }

            Log.i(TAG,"LOGFILESIZE: " + tempLogFile.size() );
            writeLogFile(tempLogFile, videoFilePath.getName());
        }
        // delete processed video file
        if(videoFilePath.exists()){
            videoFilePath.delete();
        }
    }

    private void writeLogFile(List<LogEntry> logList, String videoFileName){
        Log.i(TAG, videoFileName);
        String fileName = videoFileName.substring(0, videoFileName.length() - 4) + ".txt";

        // must be in format like this: time_1513241649555_mode_portrait
        Long videoTimeStamp = Long.parseLong( fileName.substring( 5, fileName.indexOf("_mode") ) );
        String user = prefs.getString("alias", "random-string");
        File file = new File(root+"/logfiles/", user+fileName);
        if(!file.exists()) {
            try {
                FileWriter out = new FileWriter(file);
                for (int i = 0; i < logList.size(); i++) {
                    Long frameTimestamp = videoTimeStamp + (logList.get(i).timeStamp / 1000);
                    for (int j = 0; j < logList.get(i).foundEvents.size(); j++) {
                        // 16;1513260215379.6833;2017-12-14 15:03:35.379683;True;NOTHING FOUND
                        out.write(logList.get(i).frameCounter + ";" + frameTimestamp + ";" + getTimeAndDate(frameTimestamp) + ";" + logList.get(i).orientation + ";" + logList.get(i).foundEvents.get(j) + System.lineSeparator());
                    }
                }
                out.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public static String getTimeAndDate(Long timestamp){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = formatter.format(new Date(timestamp));
        return dateString;
    }

    private LogEntry checkFrameForEvents(Frame currentFrame, List<EventDefinition> myEvents, int fcounter) {
        // converters
        AndroidFrameConverter converter = new AndroidFrameConverter();
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();

        Bitmap currentFrameBitm = converter.convert(currentFrame);
        opencv_core.Mat currentFrameMat = converterToMat.convertToMat(converter.convert(currentFrameBitm));

        boolean isPortrait = currentFrameBitm.getHeight() > currentFrameBitm.getWidth();

        // log entry for this frame
        LogEntry logEntry = new LogEntry(fcounter, currentFrame.timestamp, isPortrait, false);

        // for now only portrait
        if(isPortrait){

            EventDefinition eDef;

            // check frame against eventlist
            for (int i = 0; i < myEvents.size(); i++) {

                eDef = myEvents.get(i);
                boolean isEvent = true;
                String appendToLogEntry = "";

                // Search for fixed positions of current event first
                if (eDef.getFixedPostions().size() > 0) {
                    boolean wasTrueAtLeastOneTime = false;
                    EventDefinition.FixedPostionDefintion eDefFixedPos;
                    for (int j = 0; j < eDef.getFixedPostions().size(); j++) {
                        if (isEvent == true){
                            eDefFixedPos = eDef.getFixedPostions().get(j);

                            Bitmap sliced1 = MatchingManager.sliceImage(currentFrameBitm, eDefFixedPos.left, eDefFixedPos.top, eDefFixedPos.right, eDefFixedPos.bottom);

                            int isSimilar = 0;
                            if(eDefFixedPos.useEdges){
                                isSimilar = MatchingManager.areImagesSimilar(MatchingManager.canny(sliced1), eDefFixedPos.pHash);
                            }else{
                                isSimilar = MatchingManager.areImagesSimilar(sliced1, eDefFixedPos.pHash);
                            }

                            if (isSimilar < 3) {
                                isEvent = true;
                                wasTrueAtLeastOneTime = true;
                            } else {
                                isEvent = false;
                            }
                        }
                    }
                    if(eDef.isOR() && wasTrueAtLeastOneTime){
                        isEvent = true;
                    }
                }

                // search for text
                if (isEvent == true && eDef.getSearchForTextList().size() > 0) {
                    EventDefinition.SearchForTextDefintion eDefSearchFortext;
                    for (int j = 0; j < eDef.getSearchForTextList().size(); j++) {
                        eDefSearchFortext = eDef.getSearchForTextList().get(j);
                        Bitmap sliced = MatchingManager.sliceImage(currentFrameBitm, eDefSearchFortext.left, eDefSearchFortext.top, eDefSearchFortext.right, eDefSearchFortext.bottom);
                        if (eDefSearchFortext.searchTerm != null && !eDefSearchFortext.searchTerm.isEmpty()) {
                            isEvent = ocrManager.searchForString(eDefSearchFortext.searchTerm, sliced);
                        } else {
                            appendToLogEntry = ocrManager.getStringFromImage(sliced).replaceAll("\r", "").replaceAll("\n", "");
                            // anonymize
                            if(eDefSearchFortext.keepPrivate && privacyPref){
                                appendToLogEntry = md5(appendToLogEntry + secret);
                            }
                        }
                    }
                }

                // search for image in image using opencv's template matching
                if (isEvent == true && eDef.getSearchForImages().size() > 0) {
                    EventDefinition.SearchForImageDefintion eDefSearchForImage;
                    for(int j = 0; j < eDef.getSearchForImages().size(); j++){
                        eDefSearchForImage = eDef.getSearchForImages().get(j);
                        isEvent = MatchingManager.isInImage(currentFrameMat, eDefSearchForImage.screenshotPartAsMat);
                    }
                }

                // add event's log message to log entry
                if (isEvent) {
                    logEntry.addEvent(eDef.getLogMessage() + " " + appendToLogEntry);
                }
            }

        }

        // if we found no events, add nothing found log message
        if(logEntry.foundEvents.size() == 0){
            logEntry.addEvent("NOTHING FOUND");
        }

        currentFrameBitm.recycle();

        return logEntry;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void uploadLogFiles() {
        Log.i(TAG, "UPLOADFILES CALLED" );
        // get device name
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName().replace(" ", "");

        //String videoDir = Environment.getExternalStorageDirectory() + "/datarecorder/";
        String videoDir = root.getAbsolutePath()+"/logfiles/";

        List<File> logFileList = getListFiles(new File(videoDir),".txt");

        if (logFileList != null && !logFileList.isEmpty() && logFileList.size() > 1) {

            // keep the latest screen recording
            int delete = 0;
            long latest = logFileList.get(0).lastModified();
            for(int i = 0; i < logFileList.size(); i++){
                if( logFileList.get(i).lastModified() > latest){
                    latest = logFileList.get(i).lastModified();
                    delete = i;
                }
            }
            logFileList.remove(delete);

            int i = 0;

            // replace with your server data
            FtpUpload uploader = new FtpUpload("serveradress", "username", "password", deviceName);

            int failCounter = 0;
            while( i < logFileList.size() && failCounter < 110 ){

                Log.i(TAG, "SIZE OF FILELIST: "+logFileList.size() );

                Log.i(TAG, "TRY: " + logFileList.get(i).getName());
                Boolean successUpload = uploader.uploadFile( logFileList.get(i).getAbsolutePath(), logFileList.get(i).getName() );

                if(successUpload){
                    logFileList.get(i).delete();
                    i++;
                }else{
                    failCounter++;
                }
            }

            // we're done
            uploader.unregisterConnection();
        }

    }

}
