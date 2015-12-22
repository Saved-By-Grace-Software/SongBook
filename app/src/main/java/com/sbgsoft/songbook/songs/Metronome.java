package com.sbgsoft.songbook.songs;

import android.util.Log;

/**
 * Created by SamIAm on 12/22/2015.
 */
public class Metronome implements Runnable {
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;
    private int mBeatsPerMinute;

    public int getBeatsPerMinute() {
        return mBeatsPerMinute;
    }

    public void setBeatsPerMinute(int beatsPerMinute) {
        this.mBeatsPerMinute = beatsPerMinute;
    }

    public Metronome() {
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;
        mBeatsPerMinute = 102;
    }

    @Override
    public void run() {
        int counter = 1;
        int sleepTime = calculateSleepForBPM(mBeatsPerMinute);

        while (!mFinished) {
            // Tick the metronome
            Log.d("SONGBOOK", "Tick " + counter);
            if (counter >= 4)
                counter = 1;
            else
                counter++;

            // Wait between ticks
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Log.d("SONGBOOK", "Stopping Metronome");
                mFinished = true;
            }

            // Check for an interrupt
            if (Thread.interrupted()) {
                Log.d("SONGBOOK", "Stopping Metronome");
            }

            // Check for a thread pause
            synchronized (mPauseLock) {
                while (mPaused) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) { }
                }
            }
        }
    }

    /**
     * Call this on pause.
     */
    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    /**
     * Call this on resume.
     */
    public void onResume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    private int calculateSleepForBPM(int bpm) {
        int ret;

        // Determine sleep time in milliseconds
        float bps = 60 / (float)bpm;
        ret = (int)(1000 * bps);

        return ret;
    }
}
