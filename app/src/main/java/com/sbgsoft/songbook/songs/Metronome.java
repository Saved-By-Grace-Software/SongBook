package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.CustomAlertDialogBuilder;
import com.sbgsoft.songbook.main.MainActivity;

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
    private Drawable imageTempoMode;
    private int tickDelay;
    private boolean isRunning = false;
    private boolean inTapTempoMode = false;
    private long previousTimestamp = 0;
    private ArrayList<Integer> tempoTaps;
    private ScheduledThreadPoolExecutor exec;
    private static int startDelay = 0;
    private static int maxTapTempoTapsToRemember = 12;
    private String mCurrentSongName;
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

    public Metronome(Activity _activity, int _beatsPerMinute, TimeSignature _timeSignature, String _currentSongName) {
        mTimeSignature = _timeSignature;
        mBeatsPerMinute = _beatsPerMinute;
        mCurrentSongName = _currentSongName;

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

            // Start the task
            exec = new ScheduledThreadPoolExecutor(1);
            exec.scheduleAtFixedRate(new MetronomeTimer(mDots), startDelay, tickDelay, TimeUnit.MILLISECONDS);

            // Set the is running trigger
            isRunning = true;
        } else {
            // Sleep time not set, disable the buttons
            disableMetronomeDisplay();
        }
    }

    public void start(int _startDelay) {
        // Set the new start delay
        int tmp = startDelay;
        startDelay = _startDelay;

        // Start the metronome
        start();

        // Reset the start delay so subsequent starts are not delayed
        startDelay = tmp;
    }

    /**
     * Stops the metronome clicking
     */
    public void stop() {
        // Stop the thread execution
        if (exec != null && !exec.isShutdown())
            exec.shutdown();

        // Reset the dots
        mDots.resetToStart();

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
        setImageTempoMode(ContextCompat.getDrawable(mActivity, R.drawable.mid));

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
                gestureDetector.setIsLongpressEnabled(true);
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
            delTime = 60000 / (float) bpm;

            // If we are in compound time, calculate for ticks
            switch (tSig.noteOneBeat) {
                case 8:
                    // Divide by 3 for the tick time if top number is multiple of 3
                    if (tSig.beatsPerBar % 3 == 0)
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
    public int calculateBPMFromTickDelay(int _tickDelay, TimeSignature tSig) {
        int ret = -1;
        float bpm = -1;

        if (tSig != null) {
            // Determine our time signature
            switch (tSig.noteOneBeat) {
                case 4:
                    // Determine bpm from milliseconds
                    bpm = 60000 / (float) _tickDelay;
                    break;
                case 8:
                    // Determine bpm from milliseconds
                    if (tSig.beatsPerBar % 3 == 0)
                        bpm = 60000 / ((float) _tickDelay * 3);
                    break;
                default:
                    break;
            }

            // Convert the calculation to an integer
            if (bpm > 0)
                ret = Math.round(bpm);
        }

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

            //Log.d("SONGBOOK", "AvgGap = " + avgGap);

            // Calculate the BPM from the gap time
            bpm = calculateBPMFromTickDelay(avgGap, mTimeSignature);
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
    }

    // Handles the tap tempo mode (when metronome is long-pressed
    private void enterTapTempoMode() {
        // Check if we are in tap tempo mode already
        if (!inTapTempoMode) {
            // Turn on tap tempo mode
            inTapTempoMode = true;

            // Clear the current tap tempo list and previous time stamp
            tempoTaps.clear();
            previousTimestamp = 0;

            // Stop and reset the metronome while in tap tempo mode
            stop();
            mDots.setDotsToTapTempo();

            // Show the tap tempo pad
            showTapTempoPad();
        }
    }

    // Shows the tap tempo dialog
    private void showTapTempoPad() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(mActivity);

        // Get the dialog view to gather user input
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.tap_tempo, (ViewGroup) mActivity.findViewById(R.id.tap_tempo_root));

        // Set the height of the tap box
        int height = getTapBoxHeight();
        if (height > 0) {
            View tapBox = dialoglayout.findViewById(R.id.tap_box);
            ViewGroup.LayoutParams params = tapBox.getLayoutParams();
            params.height = height;
            tapBox.setLayoutParams(params);
        }

        // Set the click listener for the tap box
        View tapBox = dialoglayout.findViewById(R.id.tap_box);
        tapBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Find the tempo display
                TextView tempoText = (TextView)dialoglayout.findViewById(R.id.tempo_text);

                // Process the click
                tapTempoClick(tempoText);
            }
        });

        // Set the dialog layout
        alert.setView(dialoglayout);

        // Add the dialog title
        alert.setTitle("Tap Here...");

        // Set tempo button
        alert.setPositiveButton("Set Tempo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Turn off tap tempo mode
                inTapTempoMode = false;

                // Calculate the new tempo
                setBeatsPerMinute(calculateBPMFromTapTempoArray());

                // Restart the metronome
                mDots.resetToStart();
                start();

                // Close the dialog
                dialog.dismiss();

                // Check to set as default
                setTempoAsSongDefaultDialog();
            }
        });

        // Cancel button
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Turn off tap tempo mode
                inTapTempoMode = false;

                // Restart the metronome
                mDots.resetToStart();
                start();

                // Close the dialog
                dialog.dismiss();
            }
        });


        alert.setCanceledOnTouchOutside(true);

        alert.show();
    }

    /**
     * Gets the height for the tap tempo box
     * @return The height to set the tap tempo box
     */
    private int getTapBoxHeight() {
        int ret;

        // Get the height to fit it to
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        ret = size.y / 4;

        return ret;
    }

    // Click handler for a tap tempo click
    private void tapTempoClick(TextView tempoText) {
        // Get the current time in milliseconds
        long currTime = System.currentTimeMillis();

        // Check to see if we need to roll the array
        if (tempoTaps.size() > maxTapTempoTapsToRemember) {
            // Reached size limit, remove first and then add
            tempoTaps.remove(0);
        }

        // Add the difference to the array
        if (previousTimestamp > 0) {
            int diff = (int)(currTime - previousTimestamp);
            tempoTaps.add(diff);
            //Log.d("SONGBOOK", "   Added Gap: " + diff);
        }
        previousTimestamp = currTime;

        // Set the tempo text
        int tmp = calculateBPMFromTapTempoArray();
        tempoText.setText(String.format("%02d", tmp));
    }

    private void setTempoAsSongDefaultDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);

        alert.setTitle("Set As Default?");
        alert.setMessage("Do you want to set this tempo as the song default tempo?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (mCurrentSongName != null && !mCurrentSongName.isEmpty()) {
                    // Set the new bpm as default
                    boolean ret = MainActivity.dbAdapter.setSongBpm(mCurrentSongName, mBeatsPerMinute);

                    if (ret) {
                        // Alert the user of the new bpm
                        Toast.makeText(mActivity, "New Tempo: " + mBeatsPerMinute + "\nSet as song default", Toast.LENGTH_LONG).show();
                    } else {
                        // Alert the user of the new bpm
                        Toast.makeText(mActivity, "New Tempo: " + mBeatsPerMinute + "\nFailed to set tempo as default!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Alert the user of the new bpm
                    Toast.makeText(mActivity, "New Tempo: " + mBeatsPerMinute + "\nFailed to set tempo as default!", Toast.LENGTH_LONG).show();
                }
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Alert the user of the new bpm
                Toast.makeText(mActivity, "New Tempo: " + mBeatsPerMinute, Toast.LENGTH_LONG).show();
            }
        });

        alert.show();
    }
    //endregion

    //region Getters & Setters
    // Sets the on image
    public void setImageOn(Drawable _imageOn) {
        imageOn = _imageOn;

        // Set the on image in the list
        mDots.setImageOn(imageOn);
    }

    // Sets the off image
    public void setImageOff(Drawable _imageOff) {
        imageOff = _imageOff;

        // Set the off image in the list
        mDots.setImageOff(imageOff);
    }

    // Sets the tempo mode image
    public void setImageTempoMode(Drawable _imageTempoMode) {
        imageTempoMode = _imageTempoMode;

        // Set the off image in the list
        mDots.setImageTempoMode(imageTempoMode);
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

//        @Override
//        public void onLongPress(MotionEvent event) {
//            Log.d("SONGBOOK", "Long-press");
//            enterTapTempoMode();
//        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
//            Log.d("SONGBOOK", "Single tap up");
            touch();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
//            Log.d("SONGBOOK", "Double tap");
            enterTapTempoMode();
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
