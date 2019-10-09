package com.ifib.pkrieter.datarecorder;

import android.graphics.Bitmap;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class VideoManager {

    private static FrameGrabber videoGrabber;
    private static final String TAG = "VMANAGER";
    private Frame vFrame = null;
    private AndroidFrameConverter converter;
    private Bitmap currentFrame;

    VideoManager(String videoFilePath){
        videoGrabber = new FFmpegFrameGrabber(videoFilePath);
        converter = new AndroidFrameConverter();
        currentFrame = null;
        try
        {
            videoGrabber.setFormat("mp4");
            videoGrabber.start();
        } catch (FrameGrabber.Exception e)
        {
            Log.e("javacv", "Failed to start grabber" + e);
        }
    }

    public Frame getNextFrame(){
        try
        {
            vFrame = videoGrabber.grabFrame();

        } catch (FrameGrabber.Exception e)
        {
            Log.e("javacv", "video grabFrame failed: "+ e);
            vFrame = null;
        }
        if( vFrame == null ){
            stop();
        }
        return vFrame;
    }

    public void stop(){
        try
        {
            videoGrabber.stop();
        }catch (FrameGrabber.Exception e)
        {
            Log.e("javacv", "failed to stop video grabber", e);
        }
    }

    public boolean checkFrameDifference(Frame frameA, Frame frameB){
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();

        opencv_core.Mat gray1 = new opencv_core.Mat();
        opencv_imgproc.cvtColor(converterToMat.convertToMat(frameA), gray1, opencv_imgproc.COLOR_BGR2GRAY);

        opencv_core.Mat gray2 = new opencv_core.Mat();
        opencv_imgproc.cvtColor(converterToMat.convertToMat(frameB), gray2, opencv_imgproc.COLOR_BGR2GRAY);

        opencv_core.MatExpr diff = opencv_core.subtract(gray1,gray2);
        double[] min_val = new double[2];
        double[] max_val = new double[2];
        opencv_core.Point minLoc = new opencv_core.Point();
        opencv_core.Point maxLoc = new opencv_core.Point();
        opencv_core.minMaxLoc(diff.asMat(),min_val, max_val,minLoc,maxLoc,null);
        Log.i(TAG, "DIFF "+min_val[0] + " " +min_val[1]+max_val[0] + " " +max_val[1]);
        return true;
    }

}
