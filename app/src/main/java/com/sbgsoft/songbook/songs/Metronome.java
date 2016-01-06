package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sbgsoft.songbook.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SamIAm on 12/22/2015.
 */
public class Metronome {

    //region Private Class Members
    private Activity mActivity;
    private int mBeatsPerMinute;
    private int imageOn = -1;
    private int imageOff = -1;
    private int sleepTime;
    private Timer timer;
    private boolean isRunning = false;
    //endregion

    //region Public Class Members
    public MetronomeList mDots;
    //endregion

    //region Constructor
    public Metronome(Activity _activity) {
        mBeatsPerMinute = 0;
        sleepTime = 0;
        mActivity = _activity;
        mDots = new MetronomeList(mActivity);
    }
    //endregion

    //region Public Functions
    /**
     * Starts the metronome ticking
     */
    public void start() {
        // Only start if the sleeptime is set properly
        if (sleepTime > 0) {
            // Instantiate the timer
            timer = new Timer("MetronomeTimer", true);

            // Restart the metronome list to the start
            mDots.resetToStart();

            // Create the timer task
            TimerTask tick = new TimerTask() {
                @Override
                public void run() {
                    mDots.tick();
                }
            };

            // Start the task
            timer.scheduleAtFixedRate(tick, sleepTime, sleepTime);

            // Set the is running trigger
            isRunning = true;
        } else {
            // Sleep time not set, disable the buttons
            disableMetronomeDisplay();
        }
    }

    /**
     * Stops the metronome clicking
     */
    public void stop() {
        if (timer != null) {
            // Cancel and purge the timer
            timer.cancel();
            timer.purge();

            // Clear the is running trigger
            isRunning = false;
        }
    }

    /**
     * Initializes the metronome
     */
    public void initialize(LinearLayout metronomeBar) {
        // Get the height to fit it to
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;

        // Get the metronome bar layout
        int children = metronomeBar.getChildCount();

        // Resize all icons in the bar and add them to the metronome
        for (int i = 0; i < children; i++) {
            ImageView icon = (ImageView)metronomeBar.getChildAt(i);

            // Adjust the metronome size
            icon.getLayoutParams().height = (int)(height * 0.1);
            icon.getLayoutParams().width = (int)(width * 0.12);
            icon.requestLayout();

            // Add to the metronome list
            mDots.add(icon);

            // Add the touch listener for the dot
            icon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                        touch();
                    return true;
                }
            });
        }

        // Set the on and off images for the metronome
        setImageOn(R.drawable.filled);
        setImageOff(R.drawable.open);
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

    // Hides all of the metronome dots
    private void disableMetronomeDisplay() {
        mDots.hideAllIcons();
    }

    // Handles the metronome being touched
    private void touch() {
        Log.d("SONGBOOK", "Metronome Touched!");
        // Check that timer exists
        if (timer != null) {
            if (isRunning) {
                // Timer is running, stop the metronome
                stop();
            } else {
                // Timer is stopped, start the metronome
                start();
            }
        }
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

    // Gets the Metronome's beats per minute
    public int getBeatsPerMinute() {
        return mBeatsPerMinute;
    }

    // Sets the Metronome's beats per minute
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
