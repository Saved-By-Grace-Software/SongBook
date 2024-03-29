package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.ChordClickableSpan;
import com.sbgsoft.songbook.main.CustomAlertDialogBuilder;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;
import com.sbgsoft.songbook.main.StaticVars;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SamIAm on 3/25/2016.
 */
public class ChordDisplay {
    //region Private Members
    private Activity mActivity;
    private int chordFontColor;
    //endregion

    //region Constructors
    public ChordDisplay(Activity _activity) {
        mActivity = _activity;

        // Get the current theme from the database
        chordFontColor = SongBookTheme.getChordColorCode(MainActivity.dbAdapter.getCurrentSettings().getChordColor());
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
            // Translate any special keys
            if (!chordName.contains("/")) {
                if (StaticVars.chordKeyMap.containsKey(chordName))
                    chordName = StaticVars.chordKeyMap.get(chordName);
            } else {
                String[] tmp = chordName.split("/");
                if (tmp.length > 1) {
                    if (StaticVars.chordKeyMap.containsKey(tmp[0]))
                        tmp[0] = StaticVars.chordKeyMap.get(tmp[0]);
                    if (StaticVars.chordKeyMap.containsKey(tmp[1]))
                        tmp[1] = StaticVars.chordKeyMap.get(tmp[1]);
                }
                chordName = tmp[0] + "/" + tmp[1];
            }

            // Create the string representation of the chord
            String chord = "chord_" + chordName.toLowerCase();

            // Replace any sharps as # is not valid for file names
            if (chord.contains("#")) {
                chord = chord.replaceAll("#", "sp");
            }

            // Replace any slashes as / is not valid for file names
            if (chord.contains("/")) {
                chord = chord.replaceAll("/", "_");
            }

            // Common naming changes
            if (chord.endsWith("maj")) {
                chord = chord.replaceAll("maj", "");
            }
            if (chord.contains("sus2")) {
                chord = chord.replaceAll("sus2", "2");
            }
            if (chord.contains("min")) {
                chord = chord.replaceAll("min", "m");
            }
            if (chord.contains("sus4")) {
                chord = chord.replaceAll("sus4", "sus");
            }
            if (chord.contains("dom7")) {
                chord = chord.replaceAll("dom", "");
            } else if (chord.contains("dom")) {
                chord = chord.replaceAll("dom", "7");
            }

            // Attempt to find the id for the chord
            int resID = mActivity.getResources().getIdentifier(chord, "drawable", mActivity.getPackageName());

            // Check to see if we found an id
            if (resID != 0) {
                // Chord chart found, display the chord
                chordBox.setImageResource(resID);

                // Set title as chord chart
                alert.setTitle("'" + chordName + "' Chord Chart");
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
     * @param songText The song text
     */
    public CharSequence setChordClickableText(String songText) {
        SpannableStringBuilder spannable =
                new SpannableStringBuilder(SpannableString.valueOf(Html.fromHtml(songText)));

        // Build the regex to find the chords
        Pattern p = Pattern.compile(StaticVars.chordMarkupRegex);
        Matcher m = p.matcher(spannable);

        // Find the matches and make them clickable
        while(m.find()) {
            int start = m.start();
            int end = m.end();

            // Make sure we have found a chord
            if (start > 0 && end > 0) {
                // Get the chord text
                //String chordText = m.group().substring(1, m.group().length() - 1);
                String chordText = spannable.subSequence(start + 1, end - 1).toString();

                // Add the clickable span
                ChordClickableSpan span = new ChordClickableSpan(this, chordText);
                spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Make it bold
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                spannable.setSpan(boldSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Set the color
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(chordFontColor);
                spannable.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // Remove the chord delimeters
        int loc = 0;
        while (loc < spannable.length()) {
            if (spannable.charAt(loc) == StaticVars.chordMarkupStart.charAt(0) ||
                    spannable.charAt(loc) == StaticVars.chordMarkupEnd.charAt(0)) {
                spannable.delete(loc, loc + 1);
            } else {
                loc++;
            }
        }

        // Hide the hack
        int st = spannable.toString().indexOf(StaticVars.chordClickHackFix);
        spannable.setSpan(new RelativeSizeSpan(0.0f), st, st + StaticVars.chordClickHackFix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }
    //endregion
}
