package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by SamIAm on 12/22/2015.
 */
public class Metronome {

    //region Private Class Members
    private Activity mActivity;
    private int mBeatsPerMinute;
    private TimeSignature mTimeSignature;
    private Drawable imageOn;
    private Drawable imageOff;
    private int tickDelay;
    private boolean isRunning = false;
    private boolean inTapTempoMode = false;
    private long previousTimestamp = 0;
    private ArrayList<Integer> tempoTaps;
    private ScheduledThreadPoolExecutor exec;
    //endregion

    //region Public Class Members
    public MetronomeList mDots;
    //endregion

    //region Constructors
    public Metronome() {
        mBeatsPerMinute = 0;
        tickDelay = 0;
        mActivity = null;
        mDots = new MetronomeList(mActivity);
        tempoTaps = new ArrayList<>();
    }

    public Metronome(Activity _activity) {
        mBeatsPerMinute = 0;
        tickDelay = 0;
        mActivity = _activity;
        mDots = new MetronomeList(mActivity);
        tempoTaps = new ArrayList<>();
    }

    public Metronome(Activity _activity, int _beatsPerMinute, TimeSignature _timeSignature) {
        mTimeSignature = _timeSignature;
        mBeatsPerMinute = _beatsPerMinute;

        // Calculate the sleep time
        if (mBeatsPerMinute > 0)
            tickDelay = calculateTickDelayFromBPM(mBeatsPerMinute, mTimeSignature);
        else
            tickDelay = 0;

        mActivity = _activity;
        mDots = new MetronomeList(mActivity);
        tempoTaps = new ArrayList<>();
    }
    //endregion

    //region Public Functions
    /**
     * Starts the metronome ticking
     */
    public void start() {
        // Only start if the sleeptime is set properly
        if (tickDelay > 0) {
            // Restart the metronome list to the start
            mDots.resetToStart();

            Log.d("SONGBOOK", "sleep time = " + tickDelay);

            // Start the task
            exec = new ScheduledThreadPoolExecutor(1);
            exec.scheduleAtFixedRate(new MetronomeTimer(mDots), 0, tickDelay, TimeUnit.MILLISECONDS);

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
        if (exec != null && !exec.isShutdown())
            exec.shutdown();

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
        setImageOn(ContextCompat.getDrawable(mActivity, R.drawable.filled));
        setImageOff(ContextCompat.getDrawable(mActivity, R.drawable.open));

        // Add the dots for the metronome, based on time signature
        for (int i = 0; i < mTimeSignature.beatsPerBar; i++) {
            ImageView icon = new ImageView(mActivity);
            icon.setImageDrawable(imageOff);
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

    // Calculates the sleep time from the given beats per minute
    public int calculateTickDelayFromBPM(int bpm, TimeSignature tSig) {
        int ret = -1;
        float delTime;

        if (tSig != null) {
            // Determine sleep time in milliseconds for major beats
            delTime = (60 / (float) bpm) * 1000;

            // If we are in compound time, calculate for ticks
            switch (tSig.noteOneBeat) {
                case 8:
                    // Divide by 3 for the tick time
                    delTime = delTime / 3;
                    break;
                case 4:
                default:
                    break;
            }

            // Convert the calculation to an integer
            if (delTime > 0)
                ret = Math.round(delTime);
        }

        return ret;
    }

    // Calculates the beats per minute from the sleep time
    public int calculateBPMForSleep(int sleep) {
        int ret;

        // Determine bpm from milliseconds
        ret = 60000 / sleep;

        return ret;
    }
    //endregion

    //region Private Functions
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
    //endregion

    //region Getters & Setters
    // Sets the on image id
    public void setImageOn(Drawable _imageOn) {
        imageOn = _imageOn;

        // Set the on image in the list
        mDots.setImageOn(imageOn);
    }

    // Sets the off image id
    public void setImageOff(Drawable _imageOff) {
        imageOff = _imageOff;

        // Set the off image in the list
        mDots.setImageOff(imageOff);
    }

    // Sets the Metronome's beats per minute
    public void setBeatsPerMinute(int beatsPerMinute) {
        this.mBeatsPerMinute = beatsPerMinute;

        // Calculate the sleep time
        if (mBeatsPerMinute > 0)
            tickDelay = calculateTickDelayFromBPM(mBeatsPerMinute, mTimeSignature);
        else
            tickDelay = 0;
    }

    // Sets the time signature for the metronome
    public void setTimeSignature(TimeSignature mTimeSignature) {
        this.mTimeSignature = mTimeSignature;
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

class MetronomeTimer implements Runnable {
    private MetronomeList mDots;

    public MetronomeTimer(MetronomeList _dots) {
        mDots = _dots;
    }

    @Override
    public void run() {
        //Log.d("SONGBOOK", "tick");
        mDots.tick();
    }
}
