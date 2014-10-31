package com.sbgsoft.songbook.songs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.sbgsoft.songbook.items.SongItem;


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
		boolean inHtml = false;
		int skipCounter = 0;
		
		// Add HTML tags to output
		parsedOutput.append("<html><body>");
		
		// Add song title
		parsedOutput.append("<h2>");
		parsedOutput.append(songItem.getName());
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
											if (lineCharArray[charLoc] == ' ')
												chord.append("&nbsp;");
											else
												chord.append(lineCharArray[charLoc]);
										}
										
										// Append the chord
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
				parsedOutput.append(lyricLine);
				parsedOutput.append("<br />");
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
											if (lineCharArray[charLoc] == ' ')
												chord.append("&nbsp;");
											else
												chord.append(lineCharArray[charLoc]);
											
											// Increment the skip counter
											skipCounter++;
										}
										
										// Append the chord
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
											if (lineCharArray[charLoc] == ' ')
												chord.append("&nbsp;");
											else
												chord.append(lineCharArray[charLoc]);
										}
										
										// Append the chord
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
							if (lineCharArray[charLoc] == ' ')
								chord.append("&nbsp;");
							else
								chord.append(lineCharArray[charLoc]);
							
							// Increment the skip counter
							skipCounter++;
						}
						
						// Append the chord
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
				parsedOutput.append(chordLine);
				parsedOutput.append("<br />");
				parsedOutput.append(lyricLine);
				parsedOutput.append("<br />");
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
		
		return parsedOutput.toString();
	}
}
