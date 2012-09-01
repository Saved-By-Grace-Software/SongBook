package com.sbgsoft.tabapp.sets;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainActivity;

public class SetActivity extends Activity {
	TextView song;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Hide status bar of Android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set);
        
        // Get the song textview
        song = (TextView)findViewById(R.id.set_text);
        song.setMovementMethod(new ScrollingMovementMethod());
        
        // Populate it with the song text
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String songName = extras.getString(MainActivity.SONG_NAME_KEY);
            String songText = extras.getString(MainActivity.SONG_TEXT_KEY);

            if (songName != "") {
                song.setText(songName);
            }
            if (songText != "") {
                song.setText(songName + System.getProperty("line.separator") + songText);
            }
        }
        
     // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	// Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override 
    public void onPause() {
    	super.onPause();
    	
    	// Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	// Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_set, menu);
        return true;
    }
    
    /**
     * Increases the font size on the text view by 1
     * @param v
     */
    public void incFontSize(View v) {
    	song.setTextSize(song.getTextSize() + 1);
    }
    
    /**
     * Decreases the font size on the text view by 1
     * @param v
     */
    public void decFontSize(View v) {
    	song.setTextSize(song.getTextSize() - 1);
    }
    
    /**
     * Populates the text view with the song text
     * @param songText The song text to add to the view
     */
    public void populateSongText(String songText) {
    	song.setText(songText);
    }
}
