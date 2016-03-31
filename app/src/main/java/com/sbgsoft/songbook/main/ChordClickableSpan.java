package com.sbgsoft.songbook.main;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.sbgsoft.songbook.songs.ChordDisplay;

/**
 * Created by SamIAm on 3/25/2016.
 */
public class ChordClickableSpan extends ClickableSpan {
    private ChordDisplay mDisplay;
    private String mChord;

    public ChordClickableSpan(ChordDisplay _display, String _chord) {
        super();
        mDisplay = _display;
        mChord = _chord;
    }

    public void onClick(View tv) {
        mDisplay.showChord(mChord);
    }

    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
    }
}
