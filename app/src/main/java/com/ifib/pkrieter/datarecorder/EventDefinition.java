package com.ifib.pkrieter.datarecorder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.util.ArrayList;
import java.util.List;

public class EventDefinition {
    private String logMessage;
    private List<FixedPostionDefintion> fixedPostions;
    private List<SearchForImageDefintion> searchForImages;
    private List<SearchForTextDefintion> searchForTextList;
    private boolean isOR;
    private Context context;
    private String eventPath;

    EventDefinition(String msg, boolean isOR, Context context){
        this.context = context;
        this.logMessage = msg;
        this.isOR = isOR; // only for fixed positions right now
        this.fixedPostions = new ArrayList<FixedPostionDefintion>();
        this.searchForImages = new ArrayList<SearchForImageDefintion>();
        this.searchForTextList = new ArrayList<SearchForTextDefintion>();
        this.eventPath = MainActivity.getMainDir(context).getAbsolutePath() + "/events/";
    }

    public String getLogMessage() {
        return logMessage;
    }

    public boolean isOR() {
        return isOR;
    }

    public List<FixedPostionDefintion> getFixedPostions(){
        return fixedPostions;
    }

    public List<SearchForImageDefintion> getSearchForImages(){ return searchForImages; }

    public List<SearchForTextDefintion> getSearchForTextList(){
        return searchForTextList;
    }

    public void addFixedPosition(String screenshot, int left, int top, int right, int bottom, boolean useEdges, boolean condition){
        FixedPostionDefintion position = new FixedPostionDefintion(screenshot, left, top, right, bottom, useEdges, condition, this.eventPath);
        this.fixedPostions.add(position);
    }

    public void addSearchForImage(String screenshot, int left, int top, int right, int bottom){
        SearchForImageDefintion position = new SearchForImageDefintion(screenshot, left, top, right, bottom, this.eventPath);
        this.searchForImages.add(position);
    }

    public void addsearchForText( int left, int top, int right, int bottom, String searchTerm, boolean onlyDigits, boolean keepPrivate){
        SearchForTextDefintion searchForText = new SearchForTextDefintion(left, top, right, bottom, searchTerm, onlyDigits, keepPrivate);
        this.searchForTextList.add(searchForText);
    }

    // helper class
    public class FixedPostionDefintion{
        public String screenshotName;
        public int left;
        public int top;
        public int right;
        public int bottom;
        public long pHash;
        public boolean useEdges;
        FixedPostionDefintion(String screenshotName, int left, int top, int right, int bottom, boolean useEdges, boolean condition, String path){
            this.screenshotName = screenshotName;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.useEdges = useEdges;

            Bitmap event = BitmapFactory.decodeFile(path +screenshotName, null);
            Bitmap sliced1 = MatchingManager.sliceImage(event, left, top, right, bottom);
            if(useEdges){
                this.pHash = MatchingManager.getPHash(MatchingManager.canny(sliced1) );
            }else{
                this.pHash = MatchingManager.getPHash(sliced1);
            }
        }
    }

    // helper class
    public class SearchForImageDefintion{
        public String screenshotName;
        public int left;
        public int top;
        public int right;
        public int bottom;
        public opencv_core.Mat screenshotPartAsMat;

        SearchForImageDefintion(String screenshotName, int left, int top, int right, int bottom, String path){
            this.screenshotName = screenshotName;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;

            Bitmap event = BitmapFactory.decodeFile(path + screenshotName, null);
            Bitmap sliced1 = MatchingManager.sliceImage(event, left, top, right, bottom);
            AndroidFrameConverter converter = new AndroidFrameConverter();
            OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
            Frame backtoframe = converter.convert(sliced1);
            this.screenshotPartAsMat = converterToMat.convertToMat(backtoframe);
            Log.i("EDEF","height " + this.screenshotPartAsMat.rows() + " wi " + this.screenshotPartAsMat.cols() );
        }
    }

    // helper class
    public class SearchForTextDefintion{
        public String searchTerm;
        public int left;
        public int top;
        public int right;
        public int bottom;
        public boolean onlyDigits;
        public boolean keepPrivate;

        SearchForTextDefintion(int left, int top, int right, int bottom, String searchTerm, boolean onlyDigits, boolean keepPrivate){
            this.searchTerm = searchTerm; // if empty, just grab text you find in this area
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.onlyDigits = onlyDigits;
            this.keepPrivate = keepPrivate;
        }
    }
}
