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

    //region Private Class Members
    private int mBeatsPerMinute;
    private int imageOn = -1;
    private int imageOff = -1;
    private int sleepTime;
    private Timer timer;
    //endregion

    //region Public Class Members
    public MetronomeList mDots;
    //endregion

    //region Constructor
    public Metronome(Activity _activity) {
        mBeatsPerMinute = 0;
        sleepTime = 0;
        mDots = new MetronomeList(_activity);
    }
    //endregion

    //region Public Functions
    public void start() {
        // Only start if the sleeptime is set properly
        if (sleepTime > 0) {
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
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
    //endregion

    //region Private Functions
    private int calculateSleepForBPM(int bpm) {
        int ret;

        // Determine sleep time in milliseconds
        float bps = 60 / (float)bpm;
        ret = (int)(1000 * bps);

        return ret;
    }
    //endregion

    //region Getters & Setters
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

    public int getBeatsPerMinute() {
        return mBeatsPerMinute;
    }

    public void setBeatsPerMinute(int beatsPerMinute) {
        this.mBeatsPerMinute = beatsPerMinute;

        // Calculate the sleep time
        if (mBeatsPerMinute > 0)
            sleepTime = calculateSleepForBPM(mBeatsPerMinute);
        else
            sleepTime = 0;
    }
    //endregion
}
