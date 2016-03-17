package com.sbgsoft.songbook.songs;

import android.content.Context;

import com.sbgsoft.songbook.main.MainStrings;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Created by SamIAm on 3/17/2016.
 */
public class TextFileImporter {

    /**
     * Imports a straight text file into chord pro format
     * @param inputFilePath The song text file
     * @param outputFileName The chord pro output file
     * @throws Exception IO exception
     */
    public static void importTextFile(String inputFilePath, String outputFileName, String songAuthor, Context context) throws IOException {
        InputStream fis = new FileInputStream(inputFilePath);
        DataInputStream in = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        boolean startedSong = false;

        // Add author to song
        sb.append("{author:" + songAuthor + "}");
        sb.append(MainStrings.EOL);

        // Read each line of the file
        while (line != null) {
            // Check for only white spaces
            if(line.trim().length() <= 0) {
                // Add system line break
                sb.append(MainStrings.EOL);

                // Read the next line and continue
                line = br.readLine();
                continue;
            }

            // Check for song part tags
            if(MainStrings.songParts.contains(line.split("[^A-Za-z\\-]")[0].toLowerCase(Locale.US))) {
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
                        else if (chords.length() == 0 && (MainStrings.songParts.contains(lyrics.split("\\W+")[0].toLowerCase(Locale.US)))) {
                            sb.append(MainStrings.EOL);
                            sb.append("{title:");
                            sb.append(lyrics);
                            sb.append("}");
                        }
                        else {
                            int len = 0;

                            // Set the length for the for loop
                            //if (lyrics.length() > chords.length())
                                len = lyrics.length();
                            //else
                                //len = chords.length();

                            // Cycle through the characters in the lines
                            for (int i = 0; i < len; i++) {

                                // Decrement the chordOffset
                                if (chordOffset > 0) {
                                    chordOffset--;
                                }

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

                            // Append the extra chords after the lyric line
                            for (int i = lyrics.length(); i < chords.length(); i++) {
                                // Decrement the chordOffset
                                if (chordOffset > 0) {
                                    sb.append(" ");
                                    chordOffset--;
                                }

                                // Add chords to the line
                                if (chordOffset <= 0) {
                                    char c = chords.charAt(i);

                                    // If this is a new chord
                                    if (c >= 65 && c <= 71) {
                                        // Append an open bracket and the chord
                                        sb.append("[");
                                        sb.append(c);
                                        chordOffset++;

                                        // Cycle forward through the chord characters
                                        for (int j = i + 1; j < chords.length(); j++) {
                                            c = chords.charAt(j);

                                            // If the next character is a space end the chord
                                            if (c == 32) {
                                                break;
                                            }
                                            // If the next character is a new chord, start new chord
                                            else if (c >= 65 && c <= 71 && chords.charAt(j - 1) != 47) {
                                                sb.append("] [");
                                            }
                                            sb.append(c);
                                            chordOffset++;
                                        }
                                        sb.append("] ");
                                    }

                                    // If it is a space, append the space
                                    else if (c == 32) {
                                        sb.append(" ");
                                    }
                                }
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
            sb.append(MainStrings.EOL);

            // Read the next line
            line = br.readLine();
        }

        // Close the buffered reader
        br.close();

        // Write the output file
        OutputStream out = context.openFileOutput(outputFileName, Context.MODE_PRIVATE);
        PrintStream ps = new PrintStream(out);
        ps.print(sb);
    }
}
