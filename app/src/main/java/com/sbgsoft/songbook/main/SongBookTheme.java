package com.sbgsoft.songbook.main;

import android.graphics.Color;

public class SongBookTheme {

    //region Static Default Values
    private static final int BACKGROUND_TOP = 0xFF6783A1;
    private static final int BACKGROUND_BOTTOM = 0xFFDEEBFF;
    private static final int BACKGROUND_ANGLE = 90;
    private static final int MAIN_FONT = 0xFF000040;
    private static final int CHORD_FONT = 0xFF006B9F;
    //endregion

    //region Private Class Members
    private int mBackgroundTop;
    private int mBackgroundBottom;
    private int mBackgroundAngle;
    private int mMainFontColor;
    private int mChordFontColor;
    //endregion

    //region Constructors
    public SongBookTheme() {
        // By default we use the blue theme
        mBackgroundTop = BACKGROUND_TOP;
        mBackgroundBottom = BACKGROUND_BOTTOM;
        mBackgroundAngle = BACKGROUND_ANGLE;
        mMainFontColor = MAIN_FONT;
        mChordFontColor = CHORD_FONT;
    }
    //endregion

    //region Getters
    public int getChordFontColor() {
        return mChordFontColor;
    }

    public int getMainFontColor() {
        return mMainFontColor;
    }

    public int getBackgroundBottom() {
        return mBackgroundBottom;
    }

    public int getBackgroundTop() {
        return mBackgroundTop;
    }

    public int getBackgroundAngle() {
        return mBackgroundAngle;
    }
    //endregion

    //region Setters
    public void setChordFontColor(int chordFontColor) {
        this.mChordFontColor = chordFontColor;
    }

    public void setMainFontColor(int mainFontColor) {
        this.mMainFontColor = mainFontColor;
    }

    public void setBackgroundBottom(int backgroundBottom) {
        this.mBackgroundBottom = backgroundBottom;
    }

    public void setBackgroundTop(int backgroundTop) {
        this.mBackgroundTop = backgroundTop;
    }

    public void setBackgroundAngle(int backgroundAngle) {
        this.mBackgroundAngle = backgroundAngle;
    }
    //endregion
}
