package com.sbgsoft.songbook.songs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.StaticVars;

public class EditSongTextActivity extends Activity {
	private String songFile = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        setContentView(R.layout.activity_edit_song);
        
        // Get the song name from the bundle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            songFile = extras.getString(StaticVars.SONG_FILE_KEY);
            String songText = extras.getString(StaticVars.SONG_TEXT_KEY);
            String songName = extras.getString(StaticVars.SONG_NAME_KEY);
            
            // Set activity title
            setTitle("Edit Song - " + songName);
            
            // Fill the edit text
            EditText et = (EditText)findViewById(R.id.song_edit_text);
        	et.setText(songText);
        }
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
    	// Save the song
    	saveSong();
    	
    	// Close the activity
    	finish();
    }
    
    /**
     * Called when the cancel button is clicked
     * @param v
     */
    public void onCancelClick(View v) {
    	// Cancel clicked so don't save the file
    	finish();
    }

    /**
     * Saves the song from text format back to songbook format
     */
    private void saveSong() {
    	// Save the file
    	try {
	    	// Get the edited text
	    	EditText et = (EditText)findViewById(R.id.song_edit_text);
	    	String songText = et.getText().toString();
	    	
	    	// Convert the songText into songbook format
	    	BufferedReader br = new BufferedReader(new StringReader(songText));
	    	StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        boolean startedSong = false;
	        
	     // Read each line of the file
	        while (line != null) {
	        	// Check for only white spaces
	        	if(line.trim().length() <= 0) {
	        		// Add system line break
	            	sb.append(StaticVars.EOL);
	            	
	            	// Read the next line and continue
	            	line = br.readLine();
	            	continue;
	        	}
	        	
	        	// Check for song part tags
	        	if(StaticVars.songParts.contains(line.split("[^A-Za-z\\-]")[0].toLowerCase(Locale.US))) {
	        		sb.append("{title:");
	        		sb.append(line);
	            	sb.append("}");
	            	startedSong = true;
	        	}
	        	// Process the intro line
	        	else if (line.toLowerCase(Locale.US).contains("intro")) {
	        		sb.append("{intro:");
	        		boolean chordStart = false;
	        		boolean inChord = false;
	        		
	        		// Escape the chords from the line
	        		for (int i = 0; i < line.length(); i++) {
	        			char c = line.charAt(i);
	        			
	        			if (c == ':') {
	        				sb.append(c);
	        				chordStart = true;
	        			}
	        			else if (chordStart && !inChord) {
	        				if (c >= 65 && c <= 71) {
	        					sb.append("[");
	        					inChord = true;
	        				}
	        				sb.append(c);
	        			}
	        			// In a chord
	        			else if (inChord) {
	        				// If the letter is a new chord and the previous character is not a '/'
	        				if (c >= 65 && c <= 71 && line.charAt(i - 1) != 47) {
	        					sb.append("][");
	        				}
	        				// Else if the letter is not part of a chord
	        				else if (!((c >= 49 && c <= 57) || (c >= 65 && c <= 71) || c == 35 || c == 98 || c == 109 || c == 97 || c == 100 || c == 47)) {
	        					sb.append(']');
	        					inChord = false;
	        				}
	        				sb.append(c);
	        			}
	        			else {
	        				sb.append(c);
	        			}
	        		}
	        		
	        		// If still in a chord, close the chord out
	        		if (inChord)
	        			sb.append("]");
	        		
	        		// End the intro tag
	            	sb.append("}");
	        	}
	        	else if (!startedSong && line.length() > 0) {
	        		sb.append("{comment:");
	        		sb.append(line);
	            	sb.append("}");
	        	}
	        	else if (startedSong) {
	        		// Check to see if the line has chords
	        		if (line.length() > 0) {
	        			// If the line starts with a space or a chord character followed by a non chord item
	        			if (line.charAt(0) == 32 || (line.matches("^[A-G][\\s#bmad1-9/suA-G]*[^h-ln-rtv-zH-Z]") || (line.matches("^[A-G]")))) { 
		        			// Read the next two lines, chord and lyrics
		            		String chords = line;
		            		String lyrics = br.readLine();
		            		int chordOffset = 0;
		            		
		            		// Check to see if we are still in the song
		            		if (chords.length() == 0 && lyrics.length() == 0) {
		            			startedSong = false;
		            		}
		            		else if (chords.length() == 0 && (StaticVars.songParts.contains(lyrics.split("\\W+")[0].toLowerCase(Locale.US)))) {
		            			sb.append(StaticVars.EOL);
		            			sb.append("{title:");
		                		sb.append(lyrics);
		                    	sb.append("}");
		            		}
		            		else {
		            			int len = 0;
		            			
		            			// Set the length for the for loop
		            			if (lyrics.length() > chords.length())
		            				len = lyrics.length();
		            			else
		            				len = chords.length();
		            			
		            			// Cycle through the characters in the lines
		    	        		for (int i = 0; i < len; i++) {
		    	        			
		    	        			// Decrement the chordOffset
		    	        			if (chordOffset > 0)
		    	        				chordOffset--;
		    	        			
		    	        			// Add chords to the line
		    	        			if (i < chords.length() && chordOffset <= 0) {
		    	        				char c = chords.charAt(i);
		    	        				
		    	        				// If this is a new chord
		    	        				if (c >= 65 && c <= 71) {
		    	        					// Append an open bracket and the chord
		    	        					sb.append("[");
		    	        					sb.append(c);
		    	        					chordOffset++;
		    	        					
		    	        					// Cycle forward through the chord characters
		    	        					for (int j = i+1; j < chords.length(); j++) {
		    	        						c = chords.charAt(j);
		    	        						
		    	        						// If the next character is a space end the chord
		    	        						if (c == 32) {
		    	        							break;
		    	        						}
		    	        						// If the next character is a new chord, start new chord
		    	        						else if (c >= 65 && c <= 71 && chords.charAt(j - 1) != 47) {
		    	        							sb.append("][");
		    	        						}
		    	        						sb.append(c);
		            							chordOffset++;
		    	        					}
		    	        					sb.append("]");
		    	        				}
		    	        				// If it is not a space or chord character
		    	        				else if (c != 32) {
		    	        					// Append an open bracket and the chord
		    	        					sb.append("{cc:");
		    	        					sb.append(c);
		    	        					chordOffset++;
		    	        					
		    	        					// Cycle forward through the chord characters
		    	        					for (int j = i+1; j < chords.length(); j++) {
		    	        						c = chords.charAt(j);
		    	        						
		    	        						// If the next character is a new chord, break
		    	        						if (c >= 65 && c <= 71) {
		    	        							break;
		    	        						}
		    	        						sb.append(c);
		            							chordOffset++;
		    	        					}
		    	        					
		    	        					// End the cc
		    	        					sb.append("}");
		    	        				}
		    	        			}
		    	        			
		    	        			// Add the lyrics to the line
		    	        			if (i < lyrics.length())
		    	        				sb.append(lyrics.charAt(i));
		    	        		}
		            		}
		        		}
	        			else {
	        				sb.append("{comment:");
		            		sb.append(line);
		                	sb.append("}");
	        			}
	        		}
	        	}
	        	else {
	            	sb.append(line);
	        	}
	        	
	        	// Add system line break
	        	sb.append(StaticVars.EOL);
	        	
	        	// Read the next line
	        	line = br.readLine();
	        }
	        
	        // Close the buffered reader
	        br.close();
	        
	        // Write the output file
	        OutputStream out = openFileOutput(songFile, Context.MODE_PRIVATE);
	        PrintStream ps = new PrintStream(out);
	        ps.print(sb);
	    } catch (FileNotFoundException fnf) {
	    	Toast.makeText(getApplicationContext(), "Could not save song file!", Toast.LENGTH_LONG).show();
    	} catch (IOException e) {
    		Toast.makeText(getApplicationContext(), "Could not save song file!", Toast.LENGTH_LONG).show();
		}
    }
}