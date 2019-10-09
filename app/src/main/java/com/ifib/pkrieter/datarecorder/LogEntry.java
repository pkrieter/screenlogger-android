package com.ifib.pkrieter.datarecorder;

import java.util.ArrayList;

public class LogEntry {
    public int frameCounter;
    public long timeStamp;
    public boolean orientation;
    public boolean copy;
    public ArrayList<String> foundEvents;

    public LogEntry(int frameCounter, long timeStamp, boolean orientation, boolean copy){
        this.frameCounter = frameCounter;
        this.timeStamp = timeStamp;
        this.orientation = orientation;
        this.copy = copy;
        foundEvents = new ArrayList<String>();
    }

    public void addEvent(String eventMsg){
        this.foundEvents.add(eventMsg);
    }
}
