package com.sbgsoft.songbook.songs;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.main.StaticVars;


/**
 * @author sfahnestock
 *
 */
public class ChordProParser {
	public static final ArrayList<String> validDelimeters = new ArrayList<String>(
			Arrays.asList("author", "title", "cc", "lc", "capo", "intro",
                    "single", "comment", "sot", "start_of_tab", "eot", "end_of_tab",
                    "soc", "start_of_chorus", "eoc", "end_of_chorus"));
	
    public static String ParseSongFile(Context context, SongItem songItem, String transposeKey, FileInputStream file, boolean useHtml, boolean winLineFeed) throws IOException {
		StringBuilder parsedOutput = new StringBuilder();
		StringBuilder chordLine = new StringBuilder();
		StringBuilder lyricLine = new StringBuilder();
		String line = "", lineFeed = "";
		boolean inHtml = false, transposeSong = false, addedCapo = false, inTab = false, closingChorus = false;
		int skipCounter = 0;
		int chordColor = context.getResources().getColor(R.color.chordColor);
		
		// Set the line feed to use
    	if(winLineFeed)
    		lineFeed = "\r\n";
    	else
    		lineFeed = StaticVars.EOL;
    	
		// Check to see if the song needs to be transposed
		if(transposeKey != "" && !songItem.getKey().equals(transposeKey)) {
    		// Transpose the song
    		transposeSong = true;
    	}

        // Add the song title
		if (useHtml) {
            // Add HTML tags to output
            parsedOutput.append("<html><body>");

            // Add song title header
            parsedOutput.append("<b><big>");
        }
        parsedOutput.append(songItem.getName());
        parsedOutput.append(" - ");
        if (transposeSong)
            parsedOutput.append(transposeKey);
        else
            parsedOutput.append(songItem.getKey());
        if (useHtml) {
            // Close song title header
            parsedOutput.append("</big></b><br />");
        }
		parsedOutput.append(lineFeed);

        // Add the song link
        if (songItem.getSongLink() != null && !songItem.getSongLink().isEmpty()) {
            if (useHtml) {
                // Add song title header
                parsedOutput.append("<a href='");
            }
            parsedOutput.append(songItem.getSongLink());
            if (useHtml) {
                // Close song title header
                parsedOutput.append("'>" + songItem.getSongLink() + "</a><br />");
            }
            parsedOutput.append(lineFeed);
        }
				
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
			if (lineCharArray.length > 2 && lineCharArray[0] == '{' && lineCharArray[2] != 'c') {
				
				// Go through line character by character
				for (int charLoc = 0; charLoc < lineCharArray.length; charLoc++) {
					char c = lineCharArray[charLoc];
					
					// Check for start of a delimeter
					if (c == '{') {
						// Read until ':'
						StringBuilder delim = new StringBuilder();
						int stopRead = line.indexOf(':', charLoc);

                        // If no ':', read until the end of the delimeter
                        if (stopRead < 0) {
                            stopRead = line.indexOf('}', charLoc);
                        }

						for (int i = charLoc; i < stopRead - 1; i++) {
							// Go to the next character
							charLoc++;
							
							// Add that character to the delimeter
							delim.append(lineCharArray[charLoc]);
						}
						
						// Skip the ':' or '}'
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
								if (newCapo != 0 && !(newCapo >= 12)) {
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
										if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>'))) {
											auth.append(lineCharArray[charLoc]);
										}
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
									lyricLine.append("<font color=\"");
									lyricLine.append(context.getResources().getColor(R.color.titleColor));
									lyricLine.append("\"><b>");
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
										if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>'))) 
											title.append(Character.toUpperCase(lineCharArray[charLoc]));
									}
								}
								
								// Append the title
								lyricLine.append(title.toString());
								
								if (useHtml) {
									// Close the bold
									lyricLine.append("</b></font>");
								}
								
								// Skip the final '}'
								charLoc++;
								
								// Continue to the next character
								continue;
							}
							
							// Intro or Single delimeter
							if (delim.toString().equals("intro") || delim.toString().equals("single")) {
								StringBuilder intro = new StringBuilder();

								// If intro, add the intro text
								if (delim.toString().equals("intro")) {
									if (useHtml) {
										// Add beginning of bold
										intro.append("<b>");
									}

									// Append the Intro text
									intro.append("Intro: ");

									if (useHtml) {
										// Close the bold
										intro.append("</b>");
									}
								}

								// Read to end of the delimeter, until '}'

								stopRead = line.indexOf('}', charLoc);
								for (int i = charLoc; i < stopRead - 1; i++) {
									// Go to the next character
									charLoc++;
									
									// Check for a chord
									if (lineCharArray[charLoc] == '[') {
										if (useHtml) {
											// Add chord html formatting
											intro.append("<b><font color=\"" + chordColor + "\">");
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
											if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>')))
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
										if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>')))
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

                            // Start of Tab delimeter
                            if (delim.toString().equals("sot") || delim.toString().equals("start_of_tab")) {
                                // Set the in tab boolean
                                inTab = true;

                                // Continue to the next character
                                continue;
                            }

                            // End of Tab delimeter
                            if (delim.toString().equals("eot") || delim.toString().equals("end_of_tab")) {
                                // Set the in tab boolean
                                inTab = false;

                                // Nothing else allowed after the eot delimeter
                                break;
                            }

                            // Start of Chorus delimeter
                            if (delim.toString().equals("soc") || delim.toString().equals("start_of_chorus")) {
                                // Add the chorus title
                                if (useHtml) {
                                    // Add the italics
                                    lyricLine.append("<i>");

                                    lyricLine.append("<font color=\"");
                                    lyricLine.append(context.getResources().getColor(R.color.titleColor));
                                    lyricLine.append("\"><b>");
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
                                        if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>')))
                                            title.append(Character.toUpperCase(lineCharArray[charLoc]));
                                    }
                                }

                                // Append the title
                                if (title.toString() != "") {
                                    lyricLine.append(title.toString());
                                } else {
                                    lyricLine.append("CHORUS");
                                }

                                if (useHtml) {
                                    // Close the formatting
                                    lyricLine.append("</b></font>");
                                }

                                // Skip the final '}'
                                charLoc++;

                                // Continue to the next character
                                continue;
                            }

                            // End of Chorus delimeter
                            if (delim.toString().equals("eoc") || delim.toString().equals("end_of_chorus")) {
                                // End the italics
                                if (useHtml) {
                                    lyricLine.append("</i>");
                                }

                                // Set the closing of a chorus boolean
                                closingChorus = true;

                                // Nothing else allowed after the eoc delimeter
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
						if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>')))
							lyricLine.append(c);
					}
				}
				
				// Completed line, append to output
				if (lyricLine.length() > 0) {
					parsedOutput.append(lyricLine);
                    if (closingChorus) {
                        // No line feed when closing chorus, flip the boolean
                        closingChorus = false;
                    } else {
                        if (useHtml) {
                            parsedOutput.append("<br />");
                        } else {
                            parsedOutput.append(lineFeed);
                        }
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
											cc.append("<b><font color=\"" + chordColor + "\">");
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
											if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>'))) 
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
											lc.append("<b><font color=\"" + chordColor + "\">");
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
											if (useHtml || (!useHtml && (!inHtml && lineCharArray[charLoc] != '>'))) 
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
                    // Check for in a tab
                    else if (inTab) {
                        // No formatting in a tab, add the character to the lyric line
                        if (c == ' ') {
                            if (useHtml)
                                lyricLine.append("&nbsp;");
                            else
                                lyricLine.append(" ");
                        } else {
                            lyricLine.append(c);
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
						
						// Append the html
						if (useHtml)
							lyricLine.append(html.toString());
					}
					// Check for chord
					else if (c == '[') {
						if (useHtml) {
							// Add chord html formatting
							chordLine.append("<b><font color=\"" + chordColor + "\">");
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
