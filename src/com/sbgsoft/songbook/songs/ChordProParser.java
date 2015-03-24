package com.sbgsoft.songbook.songs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.widget.Toast;

import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.MainStrings;


/**
 * @author sfahnestock
 *
 */
public class ChordProParser {
	public static final ArrayList<String> validDelimeters = new ArrayList<String>(
			Arrays.asList("author", "title", "cc", "lc", "capo", "intro", "single", "comment"));
	
	
	/**
	 * Parses the input file stream and returns HTML formatted text
	 * @param file - the input file stream
	 * @return HTML formatted text
	 * @throws IOException
	 */
	public static String ParseSongFile(SongItem songItem, String transposeKey, FileInputStream file) throws IOException {
		StringBuilder parsedOutput = new StringBuilder();
		StringBuilder chordLine = new StringBuilder();
		StringBuilder lyricLine = new StringBuilder();
		String line = "";
		boolean inHtml = false, transposeSong = false, addedCapo = false;
		int skipCounter = 0;
		
		// Check to see if the song needs to be transposed
		if(!songItem.getKey().equals(transposeKey)) {
    		// Transpose the song
    		transposeSong = true;
    	}
		
		// Add HTML tags to output
		parsedOutput.append("<html><body>");
		
		// Add song title
		parsedOutput.append("<h2>");
		parsedOutput.append(songItem.getName());
		parsedOutput.append(" - ");
		parsedOutput.append(transposeKey);
		parsedOutput.append("</h2>");
				
    	// Begin reading the file
    	DataInputStream in = new DataInputStream(file);
    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
		line = br.readLine();
		
		while (line != null) {
			// Reset variables
			skipCounter = 0;
			inHtml = false;
			
			// Read line character by character
			char[] lineCharArray = line.toCharArray();
						
			// Check for a delimeter line (other than cc, lc)
			if (lineCharArray.length > 5 && lineCharArray[0] == '{' && lineCharArray[2] != 'c') {
				
				// Go through line character by character
				for (int charLoc = 0; charLoc < lineCharArray.length; charLoc++) {
					char c = lineCharArray[charLoc];
					
					// Check for start of a delimeter
					if (c == '{') {
						// Read until ':'
						StringBuilder delim = new StringBuilder();
						int stopRead = line.indexOf(':', charLoc);
						for (int i = charLoc; i < stopRead - 1; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the delimeter
							delim.append(lineCharArray[charLoc]);
						}
						
						// Skip the ':'
						charLoc++;
						
						// Check for valid delimeter
						if (validDelimeters.contains(delim.toString())) {
							
							// Capo delimeter
							if (delim.toString().equals("capo")) {
								// Read to end of the delimeter, until '}'
								StringBuilder capo = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Add that character to the capo
									capo.append(lineCharArray[charLoc]);
								}
								
								// Parse capo to integer
								int currentCapo = 0;
								try {
									currentCapo = Integer.parseInt(capo.toString());
								} catch (Exception e) { }
								
								// Get the new capo
								int newCapo = Transpose.getCapo(songItem.getKey(), transposeKey, currentCapo);
								
								// Append the capo
								if (newCapo != 0) {
									// Add beginning of italics 
									lyricLine.append("<i>");
									
									// Append the capo
									lyricLine.append("Capo ");
									lyricLine.append(newCapo);
									
									// Close the italics
									lyricLine.append("</i>");
								}
								
								addedCapo = true;
								
								// Nothing else allowed after the capo delimeter
								break;
							}
							
							// Author delimeter
							if (delim.toString().equals("author")) {
								// Add beginning of bold 
								lyricLine.append("<b>");
								
								// Read to end of the delimeter, until '}'
								StringBuilder auth = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for html
									if (lineCharArray[charLoc] == '<')
										inHtml = true;
									else if (lineCharArray[charLoc] == '>')
										inHtml = false;
									
									// Add that character to the author
									if (lineCharArray[charLoc] == ' ' && !inHtml)
										auth.append("&nbsp;");
									else
										auth.append(lineCharArray[charLoc]);
								}
								
								// Append the author
								lyricLine.append(auth.toString());
								
								// Close the bold
								lyricLine.append("</b>");
								
								// Nothing else allowed after the author delimeter
								break;
							}
							
							// Title delimeter
							if (delim.toString().equals("title")) {
								// Add beginning of bold 
								lyricLine.append("<b>");
								
								// Read to end of the delimeter, until '}'
								StringBuilder title = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for html
									if (lineCharArray[charLoc] == '<')
										inHtml = true;
									else if (lineCharArray[charLoc] == '>')
										inHtml = false;
									
									// Add that character to the title
									if (lineCharArray[charLoc] == ' ' && !inHtml)
										title.append("&nbsp;");
									else
										title.append(lineCharArray[charLoc]);
								}
								
								// Append the title
								lyricLine.append(title.toString());
								
								// Close the bold
								lyricLine.append("</b>");
								
								// Skip the final '}'
								charLoc++;
								
								// Continue to the next character
								continue;
							}
							
							// Intro delimeter
							if (delim.toString().equals("intro") || delim.toString().equals("single")) {
								// Read to end of the delimeter, until '}'
								StringBuilder intro = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for a chord
									if (lineCharArray[charLoc] == '[') {
										// Add chord html formatting
										intro.append("<b><font color=\"#006B9F\">");
										
										// Read to the end of the chord
										StringBuilder chord = new StringBuilder();
										int tmp = line.indexOf(']', charLoc);
										for(int j = charLoc; j < tmp - 1; j++) {
											// Go to the next character
											charLoc++;
											i++;
											
											// Add that character to the chord
											chord.append(lineCharArray[charLoc]);
										}
										
										// Append the chord
										if (transposeSong) 
											intro.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
										else
											intro.append(chord.toString());
										
										// Skip the final ']'
										charLoc++;
										i++;
										
										// Close chord html formatting
										intro.append("</font></b>");
									}
									else {
										// Check for html
										if (lineCharArray[charLoc] == '<')
											inHtml = true;
										else if (lineCharArray[charLoc] == '>')
											inHtml = false;
										
										// Add that character to the intro
										if (lineCharArray[charLoc] == ' ' && !inHtml)
											intro.append("&nbsp;");
										else
											intro.append(lineCharArray[charLoc]);
									}
								}
								
								// Append the intro
								lyricLine.append(intro.toString());
								
								// Skip the final '}'
								charLoc++;
								
								// Nothing else allowed after the intro delimeter
								break;
							}
							
							// Comment delimeter
							if (delim.toString().equals("comment")) {
								// Read to end of the delimeter, until '}'
								StringBuilder comment = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for html
									if (lineCharArray[charLoc] == '<')
										inHtml = true;
									else if (lineCharArray[charLoc] == '>')
										inHtml = false;
									
									// Add that character to the title
									if (lineCharArray[charLoc] == ' ' && !inHtml)
										comment.append("&nbsp;");
									else
										comment.append(lineCharArray[charLoc]);
								}
								
								// Append the title
								lyricLine.append(comment.toString());
								
								// Skip the final '}'
								charLoc++;
								
								// Nothing else allowed after the comment delimeter
								break;
							}
							
						}
					}
					
					// Check for html
					if (c == '<')
						inHtml = true;
					else if (c == '>')
						inHtml = false;
					
					// Add the character
					if (c == ' ' && !inHtml)
						lyricLine.append("&nbsp;");
					else
						lyricLine.append(c);
				}
				
				// Completed line, append to output
				if (lyricLine.length() > 0) {
					parsedOutput.append(lyricLine);
					parsedOutput.append("<br />");
				}
			} 
			else if (line.length() > 0) {
				// Go through line character by character
				for (int charLoc = 0; charLoc < lineCharArray.length; charLoc++) {
					char c = lineCharArray[charLoc];
					
					// Check for delimeter
					if (c == '{') {
						// Read until ':'
						StringBuilder delim = new StringBuilder();
						int stopRead = line.indexOf(':', charLoc);
						for (int i = charLoc; i < stopRead - 1; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the delimeter
							delim.append(lineCharArray[charLoc]);
						}
						
						// Skip the ':'
						charLoc++;
						
						// Check for valid delimeter
						if (validDelimeters.contains(delim.toString())) {
														
							// Chord comment
							if (delim.toString().equals("cc")) {
								// Read to end of the delimeter, until '}'
								StringBuilder cc = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for chord
									if (lineCharArray[charLoc] == '[') {
										// Add chord html formatting
										cc.append("<b><font color=\"#006B9F\">");
										
										// Read to the end of the chord
										StringBuilder chord = new StringBuilder();
										int tmp = line.indexOf(']', charLoc);
										for(int j = charLoc; j < tmp - 1; j++) {
											// Go to the next character
											charLoc++;
											i++;
											
											// Add that character to the chord
											chord.append(lineCharArray[charLoc]);
											
											// Increment the skip counter
											skipCounter++;
										}
										
										// Append the chord
										if (transposeSong) 
											cc.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
										else
											cc.append(chord.toString());
										
										// Skip the final ']'
										charLoc++;
										i++;
										
										// Close chord html formatting
										cc.append("</font></b>");
									}
									else {
										// Check for html
										if (lineCharArray[charLoc] == '<')
											inHtml = true;
										else if (lineCharArray[charLoc] == '>')
											inHtml = false;
										
										// Add that character to the title
										if (lineCharArray[charLoc] == ' ' && !inHtml)
											cc.append("&nbsp;");
										else
											cc.append(lineCharArray[charLoc]);
										
										// Increment the skip counter, if not html
										if (!inHtml && lineCharArray[charLoc] != '>')
											skipCounter++;
									}
								}
								
								// Append the chord comment
								chordLine.append(cc.toString());
								
								// Skip the final '}'
								charLoc++;
							}
							
							// Lyric comment
							if (delim.toString().equals("lc")) {
								// Read to end of the delimeter, until '}'
								StringBuilder lc = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for a chord
									if (lineCharArray[charLoc] == '[') {
										// Add chord html formatting
										lc.append("<b><font color=\"#006B9F\">");
										
										// Read to the end of the chord
										StringBuilder chord = new StringBuilder();
										int tmp = line.indexOf(']', charLoc);
										for(int j = charLoc; j < tmp - 1; j++) {
											// Go to the next character
											charLoc++;
											i++;
											
											// Add that character to the chord
											chord.append(lineCharArray[charLoc]);
										}
										
										// Append the chord
										if (transposeSong) 
											lc.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
										else
											lc.append(chord.toString());
										
										// Skip the final ']'
										charLoc++;
										i++;
										
										// Close chord html formatting
										lc.append("</font></b>");
									}
									else {
										// Check for html
										if (lineCharArray[charLoc] == '<')
											inHtml = true;
										else if (lineCharArray[charLoc] == '>')
											inHtml = false;
										
										// Add that character to the intro
										if (lineCharArray[charLoc] == ' ' && !inHtml)
											lc.append("&nbsp;");
										else
											lc.append(lineCharArray[charLoc]);
									}
								}
								
								// Append the intro
								lyricLine.append(lc.toString());
								
								// Skip the final '}'
								charLoc++;
							}
						}
					}
					// Check for html
					else if (c == '<') {
						// Read to the end of the html
						StringBuilder html = new StringBuilder();
						int tmp = line.indexOf('>', charLoc);
						html.append(lineCharArray[charLoc]);
						for(int i = charLoc; i < tmp; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the html
							html.append(lineCharArray[charLoc]);
						}
						
						// Append the chord
						lyricLine.append(html.toString());
					}
					// Check for chord
					else if (c == '[') {
						// Add chord html formatting
						chordLine.append("<b><font color=\"#006B9F\">");
						
						// Read to the end of the chord
						StringBuilder chord = new StringBuilder();
						int tmp = line.indexOf(']', charLoc);
						for(int i = charLoc; i < tmp - 1; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the chord
							chord.append(lineCharArray[charLoc]);
							
							// Increment the skip counter
							skipCounter++;
						}
						
						// Append the chord
						if (transposeSong) 
							chordLine.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
						else
							chordLine.append(chord.toString());
						
						// Skip the final ']'
						charLoc++;
						
						// Close chord html formatting
						chordLine.append("</font></b>");
					}
					else {
						// Check skip counter to see if we need to add a space to the chord line
						if (skipCounter > 0)
							skipCounter--;
						else {
							chordLine.append("&nbsp;");
						}
						
						// Add the character to the lyric line
						if (c == ' ')
							lyricLine.append("&nbsp;");
						else
							lyricLine.append(c);						
						
					}
				}
				
				// Completed line, append to output
				if (chordLine.length() > 0) {
					parsedOutput.append(chordLine);
					parsedOutput.append("<br />");
				}
				if (lyricLine.length() > 0) {
					parsedOutput.append(lyricLine);
					parsedOutput.append("<br />");
				}
			}
			else {
				// Empty line, add a line break
				parsedOutput.append("<br />");
			}
			
			// Clear chord and lyric lines
			chordLine = new StringBuilder();
			lyricLine = new StringBuilder();
			
			// Read the next line
			line = br.readLine();
		}
		
		// Close HTML tags in output
		parsedOutput.append("</body></html>");
		
		// Check to see if capo needs added
		if (transposeSong && !addedCapo) {
			int newCapo = Transpose.getCapo(songItem.getKey(), transposeKey, 0);
			if (newCapo != 0) {
				// Find the end of the title
				int eot = parsedOutput.toString().indexOf("</h2>") + 5;
				
				// Build the capo string
				String capoString = "<i>Capo " + newCapo + "</i><br />";
				
				// Insert the new capo
				parsedOutput.insert(eot, capoString);
			}
		}
        
        // Close the input stream and reader
    	in.close();
    	br.close();
		
		return parsedOutput.toString();
	}

	/**
     * Creates a monospace text string of the song
     * @param songName The song name to create the text for
     * @param transposeKey The key to transpose the song into
     * @return The monospace text string
     */
    public static String createSongPlainText(SongItem songItem, String transposeKey, boolean includeTitle, boolean winLineFeed, FileInputStream fis) {
    	StringBuilder sb = new StringBuilder();
    	String songText = "", chordLine = "", lyricLine = "", currentChord = "", newChord = "", authorLine = "";
    	String songKey = songItem.getKey();
    	String lineFeed = "";
    	boolean transposeSong = false, addCapo = true;
    	Pattern regex;
    	Matcher matcher;
    	int currentCapo = 0, newCapo = 0;
    	
    	// Set the line feed to use
    	if(winLineFeed)
    		lineFeed = "\r\n";
    	else
    		lineFeed = MainStrings.EOL;
    	
    	// Add the song title
    	if(includeTitle)
    		sb.append(songItem.getName() + lineFeed);
    			
    	try {
    		// Check to see if the song needs to be transposed
        	if(!transposeKey.isEmpty() && !songKey.equals(transposeKey)) {
        		// Transpose the song
        		transposeSong = true;
        	}
        	
        	// Compile the regex to look for a capo
            regex = Pattern.compile("([Cc][Aa][Pp][Oo])\\D*(\\d+)");
        	
        	DataInputStream in = new DataInputStream(fis);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();

            // Read each line of the file
            while (line != null) {
            	boolean inChord = false;
            	boolean inDelimiter = false;
            	int skipCounter = 0, charCounter = 0, commentLoc = 0;
            	String delimiter = "";  
            	
            	// Check for capo and adjust if necessary
            	if (transposeSong && addCapo) {
	        		matcher = regex.matcher(line);
	            	if (matcher.find()) {
	            		currentCapo = Integer.parseInt(matcher.group(2));
	            		newCapo = Transpose.getCapo(songKey, transposeKey, currentCapo);
	            		// If capo 0 remove the capo line
	            		if (newCapo == 0) {
	            			// Clear the chord and lyric lines
	                        chordLine = "";
	                        lyricLine = "";
	                        
	                        // Read the next line
	                        line = br.readLine();
	            		}
	            		else
	            			line = line.substring(0, matcher.start()) + "Capo " + newCapo + line.substring(matcher.end());
	            		
	            		addCapo = false;
            			continue;
	            	}
            	}
            	
            	// For intro add the line with chord formatting but all on the same line
        		if (line.startsWith("{intro:") || line.startsWith("{single:")) {
        			char[] tmp = line.substring(line.indexOf(':') + 1, line.length() - 1).toCharArray();
        			
        			for (char c : tmp) {
        				if (c == '[') {
        					inChord = true;
        					continue;
        				}
        				if (c == ']') {
        					inChord = false;
        					
        					// Transpose the chord
        					if (transposeSong)
        						newChord = Transpose.transposeChord(currentChord, transposeKey, songKey);
        					else
        						newChord = currentChord;
                			
                			// Add the chord to the line
        					lyricLine += newChord;
        					currentChord = "";
        					continue;
        				}
        				
        				// If in a chord fill the currentChord, else add to lyric line
        				if (inChord)
        					currentChord += c;
        				else
        					lyricLine += c;
        			}
        			
        			if (!lyricLine.isEmpty()) {
		                sb.append(lyricLine);
		                sb.append(lineFeed);
	            	}
        		} else {
        			// Step through each character in the line
                	for (char c : line.toCharArray()) {
                		// Increment the character counter
                		charCounter++;
                		
                		// If the character is an open bracket set inChord true and continue
                		if (c == '[' && !delimiter.equals("lc")) {
                			inChord = true;
                			continue;
                		}
                		
                		// If the character is a closed bracket set inChord false and continue
                		if (c == ']' && !delimiter.equals("lc")) {
                			inChord = false;
                			
                			// Transpose the chord
                			if (transposeSong)
        						newChord = Transpose.transposeChord(currentChord, transposeKey, songKey);
                			else
                				newChord = currentChord;
                			
                			// Check for different sized chords
                			if (newChord.length() > currentChord.length())
                				skipCounter++;
                			else if (newChord.length() < currentChord.length())
                				skipCounter--;
                			
                			// Add the chord to the line
        					chordLine += newChord;
        					currentChord = "";
                			continue;
                		}
                		
                		// If the character is an open { set inComment true and continue
                		if (c == '{') {
                			inDelimiter = true;
                			
                			// Set the comment type
                			commentLoc = line.indexOf(":", charCounter);
                			delimiter = line.substring(charCounter, commentLoc);
                			
                			continue;
                		}
                		
                		// If the character is a closed } set inComment false and continue
                		if (c == '}') {
                			inDelimiter = false;
                    		
                			delimiter = "";
                			commentLoc = 0;
                			continue;
                		}
                		
                		// If in a comment
                		if (inDelimiter) {
                			// A chord comment type
                			if (delimiter.equals("cc")) {
                				if (charCounter > commentLoc + 1) {
                					skipCounter++;
                					if (inChord)
                						currentChord += c;
                					else
                						chordLine += c;
                				}
                			}
                			
                			// A lyric chord type
                			if (delimiter.equals("lc")) {
                				if (charCounter > commentLoc + 1) {
                					if (c == '[') {
                						inChord = true;
                					}
                					else if (c == ']') {
                						inChord = false;
                						
                						// Transpose the chord
                    					if (transposeSong)
                    						newChord = Transpose.transposeChord(currentChord, transposeKey, songKey);
                    					else 
                    						newChord = currentChord;
                            			
                            			// Add the chord to the line
                    					lyricLine += newChord;
                    					currentChord = "";
                					}
                					else {
                						if (inChord)
                        					currentChord += c;
                        				else
                        					lyricLine += c;
                					}
                				}
                			}
                			
                			// For comments just add the line with no formatting
                    		if (delimiter.equals("comment") || delimiter.equals("title") || delimiter.equals("author")) {
                    			//sb.append(line.substring(i + 1, line.length() - 1) + "<br/>");
                    			if (charCounter > commentLoc + 1) {
                    				lyricLine += c;
                    				if (delimiter.equals("author")) 
                    					authorLine += c;
                    			}
                    		}
                    	
                    		continue;
                		}
                		
                		// If in a chord, add the chord to the chord line
                		if (inChord) {
                			currentChord += c;
                			skipCounter++;
                		} else {
                			if (skipCounter > 0)
                				skipCounter--;
                			else
                				chordLine += " ";
                			lyricLine += c;
                		}
                	}
    	            	
                	// Add the chord and lyric lines to the overall string builder
                	if (!chordLine.isEmpty()) {
    	                sb.append(chordLine);
    	                sb.append(lineFeed);
                	}
                	if (!lyricLine.isEmpty()) {
    	                sb.append(lyricLine);
    	                sb.append(lineFeed);
                	}
                	if (chordLine.isEmpty() && lyricLine.isEmpty())
                		sb.append(lineFeed);
        		}
        		
        		// Clear the chord and lyric lines
                chordLine = "";
                lyricLine = "";
                
                // Read the next line
                line = br.readLine();
            }
            
            // Set the song text
            songText = sb.toString();
            
            // If a capo was not added and a capo is needed, add one
            if (addCapo && transposeSong) {
            	// Search for the author line
        		regex = Pattern.compile(authorLine);
        		matcher = regex.matcher(songText);
    	    	if (matcher.find()) {
    	    		newCapo = Transpose.getCapo(songKey, transposeKey, 0);
    	    		if (newCapo != 0)
    	    			songText = songText.substring(0, matcher.end()) + lineFeed + "Capo " + newCapo + lineFeed + songText.substring(matcher.end());
    	    	}	
            }
            
            br.close();
        } catch (Exception e) {
    		//Toast.makeText(getApplicationContext(), "Failed to create file attachment!", Toast.LENGTH_LONG).show();
        }     	
    	
    	
    	return songText.toString();	
    }

    public static String ParseSongFile(SongItem songItem, String transposeKey, FileInputStream file, boolean useHtml, boolean winLineFeed) throws IOException {
		StringBuilder parsedOutput = new StringBuilder();
		StringBuilder chordLine = new StringBuilder();
		StringBuilder lyricLine = new StringBuilder();
		String line = "", lineFeed = "";
		boolean inHtml = false, transposeSong = false, addedCapo = false;
		int skipCounter = 0;
		
		// Set the line feed to use
    	if(winLineFeed)
    		lineFeed = "\r\n";
    	else
    		lineFeed = MainStrings.EOL;
    	
		// Check to see if the song needs to be transposed
		if(!songItem.getKey().equals(transposeKey)) {
    		// Transpose the song
    		transposeSong = true;
    	}
		
		if (useHtml) {
			// Add HTML tags to output
			parsedOutput.append("<html><body>");
			
			// Add song title
			parsedOutput.append("<h2>");
			parsedOutput.append(songItem.getName());
			parsedOutput.append(" - ");
			parsedOutput.append(transposeKey);
			parsedOutput.append("</h2>");
		} else {
			parsedOutput.append(songItem.getName());
			parsedOutput.append(" - ");
			parsedOutput.append(transposeKey);
		}
		parsedOutput.append(lineFeed);
				
    	// Begin reading the file
    	DataInputStream in = new DataInputStream(file);
    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
		line = br.readLine();
		
		while (line != null) {
			// Reset variables
			skipCounter = 0;
			inHtml = false;
			
			// Read line character by character
			char[] lineCharArray = line.toCharArray();
						
			// Check for a delimeter line (other than cc, lc)
			if (lineCharArray.length > 5 && lineCharArray[0] == '{' && lineCharArray[2] != 'c') {
				
				// Go through line character by character
				for (int charLoc = 0; charLoc < lineCharArray.length; charLoc++) {
					char c = lineCharArray[charLoc];
					
					// Check for start of a delimeter
					if (c == '{') {
						// Read until ':'
						StringBuilder delim = new StringBuilder();
						int stopRead = line.indexOf(':', charLoc);
						for (int i = charLoc; i < stopRead - 1; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the delimeter
							delim.append(lineCharArray[charLoc]);
						}
						
						// Skip the ':'
						charLoc++;
						
						// Check for valid delimeter
						if (validDelimeters.contains(delim.toString())) {
							
							// Capo delimeter
							if (delim.toString().equals("capo")) {
								// Read to end of the delimeter, until '}'
								StringBuilder capo = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Add that character to the capo
									capo.append(lineCharArray[charLoc]);
								}
								
								// Parse capo to integer
								int currentCapo = 0;
								try {
									currentCapo = Integer.parseInt(capo.toString());
								} catch (Exception e) { }
								
								// Get the new capo
								int newCapo = Transpose.getCapo(songItem.getKey(), transposeKey, currentCapo);
								
								// Append the capo
								if (newCapo != 0) {
									if (useHtml) {
										// Add beginning of italics 
										lyricLine.append("<i>");
									}
									
									// Append the capo
									lyricLine.append("Capo ");
									lyricLine.append(newCapo);
									
									if (useHtml) {
										// Close the italics
										lyricLine.append("</i>");
									}
								}
								
								addedCapo = true;
								
								// Nothing else allowed after the capo delimeter
								break;
							}
							
							// Author delimeter
							if (delim.toString().equals("author")) {
								if (useHtml) {
									// Add beginning of bold 
									lyricLine.append("<b>");
								}
								
								// Read to end of the delimeter, until '}'
								StringBuilder auth = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for html
									if (lineCharArray[charLoc] == '<')
										inHtml = true;
									else if (lineCharArray[charLoc] == '>')
										inHtml = false;
									
									// Add that character to the author
									if (lineCharArray[charLoc] == ' ' && !inHtml) {
										if (useHtml)
											auth.append("&nbsp;");
										else
											auth.append(" ");
									} else {
										auth.append(lineCharArray[charLoc]);
									}
								}
								
								// Append the author
								lyricLine.append(auth.toString());
								
								if (useHtml) {
									// Close the bold
									lyricLine.append("</b>");
								}
								
								// Nothing else allowed after the author delimeter
								break;
							}
							
							// Title delimeter
							if (delim.toString().equals("title")) {
								if (useHtml) {
									// Add beginning of bold 
									lyricLine.append("<b>");
								}
								
								// Read to end of the delimeter, until '}'
								StringBuilder title = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for html
									if (lineCharArray[charLoc] == '<')
										inHtml = true;
									else if (lineCharArray[charLoc] == '>')
										inHtml = false;
									
									// Add that character to the title
									if (lineCharArray[charLoc] == ' ' && !inHtml) {
										if (useHtml)
											title.append("&nbsp;");
										else
											title.append(" ");
									} else {
										title.append(lineCharArray[charLoc]);
									}
								}
								
								// Append the title
								lyricLine.append(title.toString());
								
								if (useHtml) {
									// Close the bold
									lyricLine.append("</b>");
								}
								
								// Skip the final '}'
								charLoc++;
								
								// Continue to the next character
								continue;
							}
							
							// Intro delimeter
							if (delim.toString().equals("intro") || delim.toString().equals("single")) {
								// Read to end of the delimeter, until '}'
								StringBuilder intro = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for a chord
									if (lineCharArray[charLoc] == '[') {
										if (useHtml) {
											// Add chord html formatting
											intro.append("<b><font color=\"#006B9F\">");
										}
										
										// Read to the end of the chord
										StringBuilder chord = new StringBuilder();
										int tmp = line.indexOf(']', charLoc);
										for(int j = charLoc; j < tmp - 1; j++) {
											// Go to the next character
											charLoc++;
											i++;
											
											// Add that character to the chord
											chord.append(lineCharArray[charLoc]);
										}
										
										// Append the chord
										if (transposeSong) 
											intro.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
										else
											intro.append(chord.toString());
										
										// Skip the final ']'
										charLoc++;
										i++;
										
										if (useHtml) {
											// Close chord html formatting
											intro.append("</font></b>");
										}
									}
									else {
										// Check for html
										if (lineCharArray[charLoc] == '<')
											inHtml = true;
										else if (lineCharArray[charLoc] == '>')
											inHtml = false;
										
										// Add that character to the intro
										if (lineCharArray[charLoc] == ' ' && !inHtml) {
											if (useHtml)
												intro.append("&nbsp;");
											else
												intro.append(" ");
										} else {
											intro.append(lineCharArray[charLoc]);
										}
									}
								}
								
								// Append the intro
								lyricLine.append(intro.toString());
								
								// Skip the final '}'
								charLoc++;
								
								// Nothing else allowed after the intro delimeter
								break;
							}
							
							// Comment delimeter
							if (delim.toString().equals("comment")) {
								// Read to end of the delimeter, until '}'
								StringBuilder comment = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for html
									if (lineCharArray[charLoc] == '<')
										inHtml = true;
									else if (lineCharArray[charLoc] == '>')
										inHtml = false;
									
									// Add that character to the title
									if (lineCharArray[charLoc] == ' ' && !inHtml) {
										if (useHtml)
											comment.append("&nbsp;");
										else
											comment.append(" ");
									} else {
										comment.append(lineCharArray[charLoc]);
									}
								}
								
								// Append the title
								lyricLine.append(comment.toString());
								
								// Skip the final '}'
								charLoc++;
								
								// Nothing else allowed after the comment delimeter
								break;
							}
							
						}
					}
					
					// Check for html
					if (c == '<')
						inHtml = true;
					else if (c == '>')
						inHtml = false;
					
					// Add the character
					if (c == ' ' && !inHtml) {
						if (useHtml)
							lyricLine.append("&nbsp;");
						else
							lyricLine.append(" ");
					} else {
						lyricLine.append(c);
					}
				}
				
				// Completed line, append to output
				if (lyricLine.length() > 0) {
					parsedOutput.append(lyricLine);
					if (useHtml) {
						parsedOutput.append("<br />");
					} else {
						parsedOutput.append(lineFeed);
					}
				}
			} 
			else if (line.length() > 0) {
				// Go through line character by character
				for (int charLoc = 0; charLoc < lineCharArray.length; charLoc++) {
					char c = lineCharArray[charLoc];
					
					// Check for delimeter
					if (c == '{') {
						// Read until ':'
						StringBuilder delim = new StringBuilder();
						int stopRead = line.indexOf(':', charLoc);
						for (int i = charLoc; i < stopRead - 1; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the delimeter
							delim.append(lineCharArray[charLoc]);
						}
						
						// Skip the ':'
						charLoc++;
						
						// Check for valid delimeter
						if (validDelimeters.contains(delim.toString())) {
														
							// Chord comment
							if (delim.toString().equals("cc")) {
								// Read to end of the delimeter, until '}'
								StringBuilder cc = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for chord
									if (lineCharArray[charLoc] == '[') {
										if (useHtml) {
											// Add chord html formatting
											cc.append("<b><font color=\"#006B9F\">");
										}
										
										// Read to the end of the chord
										StringBuilder chord = new StringBuilder();
										int tmp = line.indexOf(']', charLoc);
										for(int j = charLoc; j < tmp - 1; j++) {
											// Go to the next character
											charLoc++;
											i++;
											
											// Add that character to the chord
											chord.append(lineCharArray[charLoc]);
											
											// Increment the skip counter
											skipCounter++;
										}
										
										// Append the chord
										if (transposeSong) 
											cc.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
										else
											cc.append(chord.toString());
										
										// Skip the final ']'
										charLoc++;
										i++;
										
										if (useHtml) {
											// Close chord html formatting
											cc.append("</font></b>");
										}
									}
									else {
										// Check for html
										if (lineCharArray[charLoc] == '<')
											inHtml = true;
										else if (lineCharArray[charLoc] == '>')
											inHtml = false;
										
										// Add that character to the title
										if (lineCharArray[charLoc] == ' ' && !inHtml) {
											if (useHtml)
												cc.append("&nbsp;");
											else
												cc.append(" ");
										} else {
											cc.append(lineCharArray[charLoc]);
										}
										
										// Increment the skip counter, if not html
										if (!inHtml && lineCharArray[charLoc] != '>')
											skipCounter++;
									}
								}
								
								// Append the chord comment
								chordLine.append(cc.toString());
								
								// Skip the final '}'
								charLoc++;
							}
							
							// Lyric comment
							if (delim.toString().equals("lc")) {
								// Read to end of the delimeter, until '}'
								StringBuilder lc = new StringBuilder();
								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for a chord
									if (lineCharArray[charLoc] == '[') {
										if (useHtml) {
											// Add chord html formatting
											lc.append("<b><font color=\"#006B9F\">");
										}
										
										// Read to the end of the chord
										StringBuilder chord = new StringBuilder();
										int tmp = line.indexOf(']', charLoc);
										for(int j = charLoc; j < tmp - 1; j++) {
											// Go to the next character
											charLoc++;
											i++;
											
											// Add that character to the chord
											chord.append(lineCharArray[charLoc]);
										}
										
										// Append the chord
										if (transposeSong) 
											lc.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
										else
											lc.append(chord.toString());
										
										// Skip the final ']'
										charLoc++;
										i++;
										
										if (useHtml) {
											// Close chord html formatting
											lc.append("</font></b>");
										}
									}
									else {
										// Check for html
										if (lineCharArray[charLoc] == '<')
											inHtml = true;
										else if (lineCharArray[charLoc] == '>')
											inHtml = false;
										
										// Add that character to the intro
										if (lineCharArray[charLoc] == ' ' && !inHtml) {
											if (useHtml)
												lc.append("&nbsp;");
											else
												lc.append(" ");
										} else {
											lc.append(lineCharArray[charLoc]);
										}
									}
								}
								
								// Append the intro
								lyricLine.append(lc.toString());
								
								// Skip the final '}'
								charLoc++;
							}
						}
					}
					// Check for html
					else if (c == '<') {
						// Read to the end of the html
						StringBuilder html = new StringBuilder();
						int tmp = line.indexOf('>', charLoc);
						html.append(lineCharArray[charLoc]);
						for(int i = charLoc; i < tmp; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the html
							html.append(lineCharArray[charLoc]);
						}
						
						// Append the chord
						lyricLine.append(html.toString());
					}
					// Check for chord
					else if (c == '[') {
						if (useHtml) {
							// Add chord html formatting
							chordLine.append("<b><font color=\"#006B9F\">");
						}
						
						// Read to the end of the chord
						StringBuilder chord = new StringBuilder();
						int tmp = line.indexOf(']', charLoc);
						for(int i = charLoc; i < tmp - 1; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the chord
							chord.append(lineCharArray[charLoc]);
							
							// Increment the skip counter
							skipCounter++;
						}
						
						// Append the chord
						if (transposeSong) 
							chordLine.append(Transpose.transposeChord(chord.toString(), transposeKey, songItem.getKey()));
						else
							chordLine.append(chord.toString());
						
						// Skip the final ']'
						charLoc++;
						
						if (useHtml) {
							// Close chord html formatting
							chordLine.append("</font></b>");
						}
					}
					else {
						// Check skip counter to see if we need to add a space to the chord line
						if (skipCounter > 0)
							skipCounter--;
						else {
							if (useHtml)
								chordLine.append("&nbsp;");
							else
								chordLine.append(" ");
						}
						
						// Add the character to the lyric line
						if (c == ' ') {
							if (useHtml)
								lyricLine.append("&nbsp;");
							else
								lyricLine.append(" ");
						} else {
							lyricLine.append(c);
						}
						
					}
				}
				
				// Completed line, append to output
				if (chordLine.length() > 0) {
					parsedOutput.append(chordLine);
					if (useHtml) {
						parsedOutput.append("<br />");
					} else {
						parsedOutput.append(lineFeed);
					}
				}
				if (lyricLine.length() > 0) {
					parsedOutput.append(lyricLine);
					if (useHtml) {
						parsedOutput.append("<br />");
					} else {
						parsedOutput.append(lineFeed);
					}
				}
			}
			else {
				// Empty line, add a line break
				if (useHtml) {
					parsedOutput.append("<br />");
				} else {
					parsedOutput.append(lineFeed);
				}
			}
			
			// Clear chord and lyric lines
			chordLine = new StringBuilder();
			lyricLine = new StringBuilder();
			
			// Read the next line
			line = br.readLine();
		}
		
		// Close HTML tags in output
		if (useHtml)
			parsedOutput.append("</body></html>");
		
		// Check to see if capo needs added
		if (transposeSong && !addedCapo) {
			int newCapo = Transpose.getCapo(songItem.getKey(), transposeKey, 0);
			if (newCapo != 0) {
				// Find the end of the title
				int eot = parsedOutput.toString().indexOf("</h2>") + 5;
				
				// Build the capo string
				String capoString = "<i>Capo " + newCapo + "</i><br />";
				
				// Insert the new capo
				parsedOutput.insert(eot, capoString);
			}
		}
        
        // Close the input stream and reader
    	in.close();
    	br.close();
		
		return parsedOutput.toString();
	}
}
