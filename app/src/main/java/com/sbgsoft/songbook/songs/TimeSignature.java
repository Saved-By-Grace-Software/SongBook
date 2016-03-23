package com.sbgsoft.songbook.songs;

import com.sbgsoft.songbook.main.StaticVars;

/**
 * Created by SamIAm on 1/6/2016.
 */
public final class TimeSignature {
    public int beatsPerBar;
    public int noteOneBeat;

    // Constructor
    public TimeSignature() {
        beatsPerBar = 0;
        noteOneBeat = 0;
    }

    // Constructor, convert string to time signature
    public TimeSignature(String timeStr) {
        if (timeStr != null && timeStr != "") {
            // Trim the string of whitespace
            String tmp = timeStr.trim();

            // Split the string by /
            String[] split = tmp.split("/");

            // Make sure we have a valid string
            if (split.length == 2) {
                // Try to convert each item into the appropriate variable
                try {
                    beatsPerBar = Integer.parseInt(split[0]);
                    noteOneBeat = Integer.parseInt(split[1]);
                } catch (NumberFormatException nfe) {
                    // Failed to parse string
                    beatsPerBar = 0;
                    noteOneBeat = 0;
                }
            } else {
                // Invalid string
                beatsPerBar = 0;
                noteOneBeat = 0;
            }
        } else {
            // Invalid string
            beatsPerBar = 0;
            noteOneBeat = 0;
        }
    }

    @Override
    public String toString() {
        return beatsPerBar + "/" + noteOneBeat;
    }
}
