package com.sbgsoft.songbook.items;

import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.main.StaticVars;

/**
 * Created by SamIAm on 4/11/2016.
 */
public class Settings {
    //region Private Members
    private boolean mShowTransposeInSet;
    private boolean mShowEditInSet;
    private String mMetronomeState;
    private boolean mUseBrightMetronome;
    private SongBookTheme mSongBookTheme;
    private String mChordColor;
    private boolean mAutoplayTrack;
    //endregion

    //region Constructors
    public Settings() {
        mShowTransposeInSet = true;
        mShowEditInSet = true;
        mMetronomeState = "";
        mSongBookTheme = new SongBookTheme();
        mAutoplayTrack = false;
    }

    public Settings(String metronomeState, boolean transposeOn, boolean editOn) {
        mShowTransposeInSet = transposeOn;
        mShowEditInSet = editOn;
        mMetronomeState = metronomeState;
        mSongBookTheme = new SongBookTheme();
        mAutoplayTrack = false;
    }

    public Settings(String metronomeState, boolean transposeOn, boolean editOn, boolean useBrightMetronome) {
        mShowTransposeInSet = transposeOn;
        mShowEditInSet = editOn;
        mMetronomeState = metronomeState;
        mUseBrightMetronome = useBrightMetronome;
        mSongBookTheme = new SongBookTheme();
        mAutoplayTrack = false;
    }

    public Settings(String metronomeState, String transposeOn, String editOn) {
        // Set transpose option
        if (transposeOn.equals(StaticVars.SETTINGS_SET_TRANSPOSE_OFF)) {
            mShowTransposeInSet = false;
        } else {
            // We default to true
            mShowTransposeInSet = true;
        }

        // Set edit option
        if (editOn.equals(StaticVars.SETTINGS_SET_EDIT_OFF)) {
            mShowEditInSet = false;
        } else {
            // We default to true
            mShowEditInSet = true;
        }

        // Metronome status
        mMetronomeState = metronomeState;

        mSongBookTheme = new SongBookTheme();
        mAutoplayTrack = false;
    }

    public Settings(String metronomeState, String transposeOn, String editOn, int useBrightMetronome) {
        // Set transpose option
        if (transposeOn.equals(StaticVars.SETTINGS_SET_TRANSPOSE_OFF)) {
            mShowTransposeInSet = false;
        } else {
            // We default to true
            mShowTransposeInSet = true;
        }

        // Set edit option
        if (editOn.equals(StaticVars.SETTINGS_SET_EDIT_OFF)) {
            mShowEditInSet = false;
        } else {
            // We default to true
            mShowEditInSet = true;
        }

        // Set the metronome type
        setUseBrightMetronome(useBrightMetronome);

        // Metronome status
        mMetronomeState = metronomeState;

        mSongBookTheme = new SongBookTheme();
        mAutoplayTrack = false;
    }
    //endregion

    //region Getters and Setters
    public boolean getShowTransposeInSet() {
        return mShowTransposeInSet;
    }

    public String getShowTransposeInSetString() {
        if (mShowTransposeInSet)
            return StaticVars.SETTINGS_SET_TRANSPOSE_ON;
        else
            return StaticVars.SETTINGS_SET_TRANSPOSE_OFF;
    }

    public void setShowTransposeInSet(boolean showTransposeInSet) {
        this.mShowTransposeInSet = showTransposeInSet;
    }

    public boolean getShowEditInSet() {
        return mShowEditInSet;
    }

    public String getShowEditInSetString() {
        if (mShowEditInSet)
            return StaticVars.SETTINGS_SET_EDIT_ON;
        else
            return StaticVars.SETTINGS_SET_EDIT_OFF;
    }

    public void setShowEditInSet(boolean showEditInSet) {
        this.mShowEditInSet = showEditInSet;
    }

    public String getMetronomeState() {
        return mMetronomeState;
    }

    public void setMetronomeState(String mMetronomeState) {
        this.mMetronomeState = mMetronomeState;
    }

    public boolean getUseBrightMetronome() {
        return mUseBrightMetronome;
    }

    public int getUseBrightMetronomeInt() {
        if (mUseBrightMetronome)
            return StaticVars.SETTINGS_BRIGHT_METRONOME;
        else
            return StaticVars.SETTINGS_STANDARD_METRONOME;
    }

    public void setUseBrightMetronome(boolean useBrightMetronome) {
        this.mUseBrightMetronome = useBrightMetronome;
    }

    public void setUseBrightMetronome(int useBrightMetronome) {
        if (useBrightMetronome == StaticVars.SETTINGS_BRIGHT_METRONOME)
            this.mUseBrightMetronome = true;
        else
            this.mUseBrightMetronome = false;
    }

    public SongBookTheme getSongBookTheme() {
        return mSongBookTheme;
    }

    public void setSongBookTheme(SongBookTheme themeColor) {
        this.mSongBookTheme = themeColor;
    }

    public String getChordColor() {
        return mChordColor;
    }

    public void setChordColor(String chordColor) {
        this.mChordColor = chordColor;
    }

    public boolean getAutoplayTrack() {
        return mAutoplayTrack;
    }

    public void setAutoplayTrack(boolean autoplayTrack) {
        this.mAutoplayTrack = autoplayTrack;
    }
    //endregion
}
