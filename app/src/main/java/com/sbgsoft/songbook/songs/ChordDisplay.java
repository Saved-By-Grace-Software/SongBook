package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.ChordClickableSpan;
import com.sbgsoft.songbook.main.CustomAlertDialogBuilder;
import com.sbgsoft.songbook.main.StaticVars;

/**
 * Created by SamIAm on 3/25/2016.
 */
public class ChordDisplay {
    //region Private Members
    private Activity mActivity;
    //endregion

    //region Constructors
    public ChordDisplay(Activity _activity) {
        mActivity = _activity;
    }
    //endregion

    //region Public Functions
    /**
     * Shows the specified chord diagram
     * @param chordName The chord to display
     */
    public void showChord(String chordName) {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(mActivity);

        // Get the dialog view to gather user input
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.chord_display, (ViewGroup) mActivity.findViewById(R.id.chord_display_root));

        // Get the chord box
        ImageView chordBox = (ImageView)dialoglayout.findViewById(R.id.chord_display);

        // Find the image corresponding to the chord name
        if (!chordName.isEmpty()) {
            // Create the string representation of the chord
            String chord = "chord_" + chordName.toLowerCase();

            // Attempt to find the id for the chord
            int resID = mActivity.getResources().getIdentifier(chord, "drawable", mActivity.getPackageName());

            // Check to see if we found an id
            if (resID != 0) {
                // Chord chart found, display the chord
                chordBox.setImageResource(resID);

                // Set title as chord chart
                alert.setTitle("Chord Chart");
            } else {
                // No chord chart found, set title as cannot find chord
                alert.setTitle("'" + chordName + "' Chord Not Available");
            }
        } else {
            // No chord given, set title as cannot find chord
            alert.setTitle("Chord Not Available");
        }

        // Set the dialog layout
        alert.setView(dialoglayout);

        // Set tempo button
        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        alert.setCanceledOnTouchOutside(true);

        alert.show();
    }

    /**
     * Makes all of the chords clickable to show their diagrams
     * @param spannable The spannable song text
     */
    public void applySpan(SpannableString spannable) {
        // Get a string to search
        final String spannableString = spannable.toString();

        // TODO: Try some sort of regex?
        spannableString.matches("^[A-G][\\s#bmad1-9/suA-G]*[^h-ln-rtv-zH-Z]");

        // Find the start chord markup
        int start = spannableString.indexOf(StaticVars.chordMarkupStart);

        // Make sure we have found a chord
        if (start > 0) {
            start += StaticVars.chordMarkupStart.length();

            // Find the end of the chord markup
            int end = start + spannableString.indexOf(StaticVars.chordMarkupEnd, start);

            // Get the chord text
            String chordText = spannableString.substring(start, end);

            // Add the clickable span
            ChordClickableSpan span = new ChordClickableSpan(this, chordText);
            spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Make it bold
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            spannable.setSpan(boldSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the color
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(mActivity.getResources().getColor(R.color.chordColor));
            spannable.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    //endregion
}
