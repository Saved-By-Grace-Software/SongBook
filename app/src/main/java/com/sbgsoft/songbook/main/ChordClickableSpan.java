package com.sbgsoft.songbook.main;

import android.app.Activity;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sbgsoft.songbook.songs.ChordDisplay;

/**
 * Created by SamIAm on 3/25/2016.
 */
public class ChordClickableSpan extends ClickableSpan {
    public ChordClickableSpan() {
        super();
    }

    public void onClick(View tv) {
        Log.d("SONGBOOK", "clicked");
        ChordDisplay disp = new ChordDisplay((Activity)tv.getContext());
        disp.showChord("C/E");
    }

    public void updateDrawState(TextPaint ds) {// override updateDrawState
        ds.setUnderlineText(false); // set to false to remove underline
    }
}
