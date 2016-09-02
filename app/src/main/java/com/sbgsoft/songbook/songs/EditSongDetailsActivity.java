package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.files.OpenFile;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;
import com.sbgsoft.songbook.sets.CurrentSetTab;
import com.sbgsoft.songbook.sets.SetsTab;

import java.util.Arrays;
import java.util.Locale;

public class EditSongDetailsActivity extends AppCompatActivity {
    private String songName = "";
    private EditText songNameET;
    private EditText authorET;
    private EditText keyET;
    private EditText linkET;
    private EditText bpmET;
    private TextView trackTV;
    private Spinner timeSpin;
    private CoordinatorLayout coordinatorLayout;
    private String fullTrackPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_song_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.editatt_coordinatorLayout);

        // Get the song name from the bundle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            songName = extras.getString(StaticVars.SONG_NAME_KEY);
        }

        // Populate the views
        populateSongDetails();

        // Setup activity return type
        getIntent().putExtra(StaticVars.ACTIVITY_RESPONSE_TYPE, StaticVars.EDIT_SONG_ATT_ACTIVITY);

        // Setup save button
        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.song_edit_att_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save the details and then exit the activity
                boolean success = saveDetails();

                // Set result to OK and finish
                if (success) {
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });

        // Setup cancel button
        FloatingActionButton cancelButton = (FloatingActionButton) findViewById(R.id.song_edit_att_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set result to cancel and finish
                setResult(RESULT_CANCELED, getIntent());
                finish();
            }
        });

        // Setup clear track button
        FloatingActionButton clearTrackButton = (FloatingActionButton) findViewById(R.id.editatt_clear_track);
        clearTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear the song track view
                trackTV.setText("");
                fullTrackPath = "";
            }
        });
    }

    public synchronized void onActivityResult(final int requestCode,
                                              int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            // Get the returned filepath
            fullTrackPath = data.getStringExtra(OpenFile.RESULT_PATH);

            // Set the filepath for song track
            trackTV.setText(getTrackDisplayName(fullTrackPath));
        }
    }

    /**
     * Fills the form with existing data from the database
     */
    private void populateSongDetails() {
        songNameET = (EditText)findViewById(R.id.editatt_song_name);
    	authorET = (EditText)findViewById(R.id.editatt_song_author);
    	keyET = (EditText)findViewById(R.id.editatt_song_key);
        linkET = (EditText)findViewById(R.id.editatt_song_link);
        bpmET = (EditText)findViewById(R.id.editatt_song_bpm);
        timeSpin = (Spinner)findViewById(R.id.editatt_song_time);
        trackTV = (TextView)findViewById(R.id.editatt_song_track);

    	// Populate the text boxes
    	songNameET.setText(songName);
    	authorET.setText(MainActivity.dbAdapter.getSongAuthor(songName));
    	keyET.setText(MainActivity.dbAdapter.getSongKey(songName));
        linkET.setText(MainActivity.dbAdapter.getSongLink(songName));

        // Populate the background track
        fullTrackPath = MainActivity.dbAdapter.getSongTrack(songName);
        trackTV.setText(getTrackDisplayName(fullTrackPath));

        // Get the beats per minute and populate
        int bpm = MainActivity.dbAdapter.getSongBpm(songName);
        if (bpm > 0)
            bpmET.setText(Integer.toString(bpm));

        // Get the time signature and select the spinner
        TimeSignature ts = MainActivity.dbAdapter.getSongTimeSignature(songName);
        if (ts.noteOneBeat > 0 && ts.beatsPerBar > 0) {
            String[] timeSigs = getResources().getStringArray(R.array.time_signatures);
            int loc = Arrays.asList(timeSigs).indexOf(ts.toString());
            if (loc >= 0 && loc < timeSpin.getCount())
                timeSpin.setSelection(loc);
        } else {
            timeSpin.setSelection(3);
        }
    }

    /**
     * Saves the song details to the database
     */
    private boolean saveDetails() {
        String key = keyET.getText().toString();

        // Upper case the key
        if (key.length() > 1)
            key = key.substring(0, 1).toUpperCase(Locale.US) + key.substring(1).trim();
        else if (key.length() > 0)
            key = key.toUpperCase(Locale.US).trim();

        // Check for a correct key
        if (!MainActivity.isValidKey(key)) {
            Snackbar.make(coordinatorLayout, "That is not a valid key!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return false;
        }

        // Check for bpm populated
        int bpm = 0;
        try {
            bpm = Integer.parseInt(bpmET.getText().toString());
        } catch (NumberFormatException nfe) {
        }

        // Update the song in the database
        MainActivity.dbAdapter.updateSongAttributes(songName, songNameET.getText().toString(),
                authorET.getText().toString(), key, String.valueOf(timeSpin.getSelectedItem()),
                linkET.getText().toString(), bpm, fullTrackPath);

        return true;
    }

    /**
     * Click handler for the track edit test
     * @param v
     */
    public void onTrackClick(View v) {
        // Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        intent.putExtra(StaticVars.FILE_ACTIVITY_KEY, StaticVars.EDIT_SONG_ATT_ACTIVITY);
        intent.putExtra(StaticVars.FILE_ACTIVITY_TYPE_KEY, StaticVars.FILE_ACTIVITY_FILE);

        // Start the activity
        startActivityForResult(intent, 1);
    }

    private String getTrackDisplayName(String fullPath) {
        String ret = "";

        if (fullPath != null) {
            ret = fullPath.substring(fullPath.lastIndexOf("/")+1);
        }
        return ret;
    }
}
