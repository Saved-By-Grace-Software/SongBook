package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.CustomAlertDialogBuilder;

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
    //endregion

    //region Private Functions
    /**
     * Gets the height for the chord display box
     * @return The height to set the chord display box
     */
    private int getChordBoxHeight() {
        int ret;

        // Get the height to fit it to
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        ret = size.y / 4;

        return ret;
    }
    //endregion
}
