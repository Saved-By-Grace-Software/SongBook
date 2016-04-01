package com.sbgsoft.songbook.songs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.StaticVars;

public class EditSongRawActivity extends Activity {
	private String songName = "";
	private String songFile = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        setContentView(R.layout.activity_edit_song);
        
        // Get the song name from the bundle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            songName = extras.getString(StaticVars.SONG_NAME_KEY);
            songFile = extras.getString(StaticVars.SONG_FILE_KEY);
        }
        
        // Fill the edit text with the song file
        loadSong();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_edit_song, menu);
        return true;
    }
    
    /**
     * Called when the save button is clicked
     * @param v
     */
    public void onSaveClick(View v) {
    	// Save the file
    	try {
    		// Open file to output
	    	OutputStream os = openFileOutput(songFile, Context.MODE_PRIVATE);
	    	PrintStream printStream = new PrintStream(os);
	    	
	    	// Get the edited text
	    	EditText et = (EditText)findViewById(R.id.song_edit_text);
	    	String songText = et.getText().toString();
	    	
	    	printStream.print(songText);
	    	printStream.close();
	    } catch (FileNotFoundException fnf) {
	    		Toast.makeText(getApplicationContext(), "Could not save song file!", Toast.LENGTH_LONG).show();
    	}
    	
    	// Close the activity
        setResult(Activity.RESULT_OK);
    	finish();
    }
    
    /**
     * Called when the cancel button is clicked
     * @param v
     */
    public void onCancelClick(View v) {
    	// Cancel clicked so don't save the file
        setResult(Activity.RESULT_CANCELED);
    	finish();
    }
    
    /**
     * Loads the song file and populates the edit text
     */
    private void loadSong() {
    	String songText = "";
    	String fileName = MainActivity.dbAdapter.getSongFile(songName);
    	// Copy the file into the tabapp songs directory
    	try {
        	FileInputStream fis = openFileInput(fileName);
        	DataInputStream in = new DataInputStream(fis);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
                line = br.readLine();
            }
            songText = sb.toString();
            br.close();
        } catch (Exception e) {
    		Toast.makeText(getApplicationContext(), "Could not open song file!", Toast.LENGTH_LONG).show();
        }  
    	
    	EditText et = (EditText)findViewById(R.id.song_edit_text);
    	et.setText(songText);
    }
}
