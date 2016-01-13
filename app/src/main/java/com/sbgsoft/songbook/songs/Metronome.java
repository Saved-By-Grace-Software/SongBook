package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sbgsoft.songbook.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SamIAm on 12/22/2015.
 */
public class Metronome {

    //region Private Class Members
    private Activity mActivity;
    private int mBeatsPerMinute;
    private TimeSignature mTimeSignature;
    private int imageOn = -1;
    private int imageOff = -1;
    private int sleepTime;
    private boolean isRunning = false;
    private boolean inTapTempoMode = false;
    private long previousTimestamp = 0;
    private ArrayList<Integer> tempoTaps;
    private Handler mHandler;
    private static int ANDROID_DELAY = 10;
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
        tempoTaps = new ArrayList<Integer>();
        mHandler = new Handler();
    }
    //endregion

    //region Public Functions
    /**
     * Starts the metronome ticking
     */
    public void start() {
        // Only start if the sleeptime is set properly
        if (sleepTime > 0) {
            // Restart the metronome list to the start
            mDots.resetToStart();

            Log.d("SONGBOOK", "sleep time = " + sleepTime);

            // Start the task
            mHandler.removeCallbacks(mCallTick);
            mHandler.postDelayed(mCallTick, sleepTime);

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
        // Remove handler callbacks
        mHandler.removeCallbacks(mCallTick);

        // Clear the is running trigger
        isRunning = false;
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

        // Set the on and off images for the metronome
        setImageOn(R.drawable.filled);
        setImageOff(R.drawable.open);

        // Add the dots for the metronome, based on time signature
        for (int i = 0; i < mTimeSignature.beatsPerBar; i++) {
            ImageView icon = new ImageView(mActivity);
            icon.setImageResource(imageOff);
            icon.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (height * 0.08),
                    (int) (width * 0.1)));

            metronomeBar.addView(icon);


            // Add to the metronome list
            mDots.add(icon);
        }

        // Add the touch listener for the metronome
        metronomeBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        metronomeBar.requestLayout();
    }
    //endregion

    //region Private Functions
    // Calculates the sleep time from the given beats per minute
    private int calculateSleepForBPM(int bpm) {
        int ret;

        // Determine sleep time in milliseconds
        float bps = 60 / (float)bpm;
        ret = (int)(1000 * bps);

        return ret;
    }

    // Calculates the beats per minute from the sleep time
    private int calculateBPMForSleep(int sleep) {
        int ret;

        // Determine bpm from milliseconds
        ret = 60000 / sleep;

        return ret;
    }

    // Calculates the beats per minute from the tap tempo array
    private int calculateBPMFromTapTempoArray() {
        int bpm = 0;

        // Ensure we have enough data to calculate
        if (tempoTaps.size() > 1) {
            // Get the average time gap in the array
            int avgGap = 0;
            for (int gap : tempoTaps) {
                avgGap += gap;
            }
            avgGap = (avgGap / tempoTaps.size());

            // Calculate the BPM from the gap time
            bpm = calculateBPMForSleep(avgGap);
        }

        return bpm;
    }

    // Hides all of the metronome dots
    private void disableMetronomeDisplay() {
        mDots.hideAllIcons();
    }

    // Handles the metronome being touched
    private void touch() {
        // Check that timer exists and we are in normal mode
        if (!inTapTempoMode) {
            if (isRunning) {
                // Timer is running, stop the metronome
                stop();
            } else {
                // Timer is stopped, start the metronome
                start();
            }
        }

        // Check for in tap tempo mode
        if (inTapTempoMode) {
            // Get the current time in milliseconds
            long currTime = System.currentTimeMillis();

            // Check to see if we need to roll the array
            if (tempoTaps.size() > 10) {
                // Reached size limit, remove first and then add
                tempoTaps.remove(0);
            }

            // Add the difference to the array
            if (previousTimestamp > 0) {
                int diff = (int)(currTime - previousTimestamp);
                tempoTaps.add(diff);
                Log.d("SONGBOOK", "   Added Gap: " + diff);
            }
            previousTimestamp = currTime;

            // Calculate the bpm from the current list and adjust current bpm
            if (tempoTaps.size() > 1) {
                setBeatsPerMinute(calculateBPMFromTapTempoArray());
                Log.d("SONGBOOK", "New BPM: " + mBeatsPerMinute);
            }
        }
    }

    // Handles the tap tempo mode (when metronome is long-pressed
    private void tapTempoMode() {
        // Check if we are in tap tempo mode already
        if (inTapTempoMode) {
            // Turn off tap tempo mode
            inTapTempoMode = false;

            // Alert the user of the new bpm
            Toast.makeText(mActivity, "New Tempo: " + mBeatsPerMinute, Toast.LENGTH_LONG).show();

            // Restart the metronome
            mDots.resetToStart();
            start();
        } else {
            // Turn on tap tempo mode
            inTapTempoMode = true;

            // Alert the user that tap tempo mode has been entered
            Toast.makeText(mActivity, "Tap Tempo Mode has been entered!", Toast.LENGTH_LONG).show();

            // Clear the current tap tempo list and previous time stamp
            tempoTaps.clear();
            previousTimestamp = 0;

            // Stop and reset the metronome while in tap tempo mode
            stop();
            mDots.resetToStart();
        }
    }

    // Runnable task for ticking the metronome
    private Runnable mCallTick = new Runnable() {
        public void run() {
            long startTime = SystemClock.elapsedRealtime();

            mDots.tick();

            long timeElapsed = SystemClock.elapsedRealtime() - startTime;
            mHandler.postDelayed(this, sleepTime - timeElapsed - ANDROID_DELAY);
        }
    };
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

    // Sets the Metronome's beats per minute
    public void setBeatsPerMinute(int beatsPerMinute) {
        this.mBeatsPerMinute = beatsPerMinute;

        // Calculate the sleep time
        if (mBeatsPerMinute > 0)
            sleepTime = calculateSleepForBPM(mBeatsPerMinute);
        else
            sleepTime = 0;
    }

    // Sets the time signature for the metronome
    public void setmTimeSignature(TimeSignature mTimeSignature) {
        this.mTimeSignature = mTimeSignature;

        Log.d("SONGBOOK", "Time Signature: " + mTimeSignature.toString());
    }
    //endregion

    //region Gesture Detector
    /**
     * Listens for the long press
     */
    GestureDetector.SimpleOnGestureListener simpleOnGestureListener
            = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public void onLongPress(MotionEvent event) {
            tapTempoMode();
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            touch();
            return true;
        }
    };

    GestureDetector gestureDetector
            = new GestureDetector(mActivity, simpleOnGestureListener);
    //endregion
}
