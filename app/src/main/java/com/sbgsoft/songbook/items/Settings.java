package com.sbgsoft.songbook.items;

import com.sbgsoft.songbook.main.StaticVars;

/**
 * Created by SamIAm on 4/11/2016.
 */
public class Settings {
    //region Private Members
    private boolean mShowTransposeInSet;
    private boolean mShowEditInSet;
    private String mMetronomeState;
    //endregion

    //region Constructors
    public Settings() {
        mShowTransposeInSet = true;
        mShowEditInSet = true;
        mMetronomeState = "";
    }

    public Settings(String metronomeState, boolean transposeOn, boolean editOn) {
        mShowTransposeInSet = transposeOn;
        mShowEditInSet = editOn;
        mMetronomeState = metronomeState;
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
    //endregion
}
