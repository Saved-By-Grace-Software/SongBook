package com.sbgsoft.songbook.items;

import com.sbgsoft.songbook.main.StaticVars;

/**
 * Created by SamIAm on 4/11/2016.
 */
public class Settings {
    //region Private Members
    private boolean mShowTransposeInSet;
    private boolean mShowEditInSet;
    //endregion

    //region Constructors
    public Settings() {
        mShowTransposeInSet = true;
        mShowEditInSet = true;
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
    }
    //endregion

    //region Getters and Setters
    public boolean getShowTransposeInSet() {
        return mShowTransposeInSet;
    }

    public void setShowTransposeInSet(boolean showTransposeInSet) {
        this.mShowTransposeInSet = showTransposeInSet;
    }

    public boolean getShowEditInSet() {
        return mShowEditInSet;
    }

    public void setShowEditInSet(boolean showEditInSet) {
        this.mShowEditInSet = showEditInSet;
    }
    //endregion
}
