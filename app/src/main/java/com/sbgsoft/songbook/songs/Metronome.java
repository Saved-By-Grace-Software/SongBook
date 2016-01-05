package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SamIAm on 12/22/2015.
 */
public class Metronome {
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;
    private int mBeatsPerMinute;
    private int imageOn = -1;
    private int imageOff = -1;
    private int sleepTime;
    private Timer timer;

    public MetronomeList mDots;

    public int getBeatsPerMinute() {
        return mBeatsPerMinute;
    }

    public void setBeatsPerMinute(int beatsPerMinute) {
        this.mBeatsPerMinute = beatsPerMinute;
    }

    public Metronome(Activity _activity) {
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;
        mBeatsPerMinute = 120;
        mDots = new MetronomeList(_activity);

        sleepTime = calculateSleepForBPM(mBeatsPerMinute);
    }

    public void start() {
        // Instantiate the timer
        timer = new Timer("MetronomeTimer", true);

        // Create the timer task
        TimerTask tick = new TimerTask() {
            @Override
            public void run() {
                mDots.tick();
            }
        };

        // Start the task
        timer.scheduleAtFixedRate(tick, sleepTime, sleepTime);
    }

    public void stop() {
        timer.cancel();
        timer.purge();
    }

    private int calculateSleepForBPM(int bpm) {
        int ret;

        // Determine sleep time in milliseconds
        float bps = 60 / (float)bpm;
        ret = (int)(1000 * bps);

        return ret;
    }

    // Sets the on image id
    public void setImageOn(int _imageOn) {
        imageOn = _imageOn;

        // Set the on image in the list
        mDots.setImageOn(imageOn);
    }

    // Sets the off image id
    public void setImageOff(int _imageOff) {
        imageOff = _imageOff;

        // Set the off image in the list
        mDots.setImageOff(imageOff);
    }
}
