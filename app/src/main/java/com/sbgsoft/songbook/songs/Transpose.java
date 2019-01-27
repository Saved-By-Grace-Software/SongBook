package com.sbgsoft.songbook.songs;

import android.text.StaticLayout;

import java.util.ListIterator;

import com.sbgsoft.songbook.main.StaticVars;

public class Transpose {
    /**
     * Transposes the chord into the specified key
     * @param originalChord The chord to transpose
     * @param transposeKey The key to transpose it into
     * @return The transposed chord
     */
    public static String transposeChord(final String originalChord, String transposeKey, String songKey) {
    	String newChord = "", root = "", newRoot = "", bass = "", newBass = "";
    	int slashIndex = 0;
    	int diff = 0, rootIndex = 0, bassIndex = 0;    	
    	
    	// Get the root note of the chord
    	if (originalChord.length() > 1) {
	    	root = originalChord.substring(0, 1);
	    	if (originalChord.charAt(1) == 35 || originalChord.charAt(1) == 98) {
	    		root += originalChord.charAt(1);
	    	}
    	}
    	else
    		root = originalChord;
    	
    	// Check for key from keymap
    	if (StaticVars.songKeyMap.containsKey(root)) {
			root = StaticVars.songKeyMap.get(root);
		}
    	
    	// Get the root note index
    	rootIndex = StaticVars.songKeys_transpose.lastIndexOf(root);
    	
    	// Get the index difference
    	diff = getCapo(songKey, transposeKey, 0);
    	
    	// Set the new root note
    	newRoot = StaticVars.songKeys_transpose.get(rootIndex - diff);
    	
    	// Check for a bass note
    	if (originalChord.contains("/")) {
    		slashIndex = originalChord.indexOf("/");
    		
    		// Get the bass note
    		bass = originalChord.substring(slashIndex + 1, slashIndex + 2);
    		if (originalChord.length() - (slashIndex + 1) > 1) {
    			if (originalChord.charAt(slashIndex + 2) == 35 || originalChord.charAt(slashIndex + 2) == 98) {
    				bass += originalChord.charAt(slashIndex + 2);
    	    	}
    		}
    		
    		// Check for key from keymap
        	if (StaticVars.songKeyMap.containsKey(bass)) {
        		bass = StaticVars.songKeyMap.get(bass);
    		}
    		
    		// Get the bass note index
    		bassIndex = StaticVars.songKeys_transpose.lastIndexOf(bass);
    		
    		// Set the new bass note
        	newBass = StaticVars.songKeys_transpose.get(bassIndex - diff);
    		
    		// Create the new chord
    		// Replace the root note
    		if (root.length() == 2)
        		newChord = newRoot + originalChord.substring(2, slashIndex);
        	else
        		newChord = newRoot + originalChord.substring(1, slashIndex);
    		
    		// Replace the bass note
    		newChord += "/";
    		if (bass.length() == 2)
    			newChord += newBass + originalChord.substring(slashIndex + 3);
    		else
    			newChord += newBass + originalChord.substring(slashIndex + 2);
    	}
    	else {
    		// Create the new chord
    		// No bass note so only replace the root note
        	if (root.length() == 2)
        		newChord = newRoot + originalChord.substring(2);
        	else
        		newChord = newRoot + originalChord.substring(1);
    	}

    	return newChord;
    }

    /**
     * Gets the capo to play in
     * @param transposeKey The key the song is being transposed into
     * @return The capo number
     */
    public static int getCapo(String songKey, String transposeKey, int currentCapo) {
    	int newCapo;
    	
    	// Get the song key and transpose key locations
    	int songKeyLoc = StaticVars.songKeys_transpose.lastIndexOf(songKey);
    	int tranKeyLoc = lastIndexOf(transposeKey, songKeyLoc);
    	
    	//If the current capo is not 0 then change the song key location
    	if (currentCapo != 0) {
    		// Set the new song key location
    		songKeyLoc = songKeyLoc + currentCapo;
    	}
    	
    	// Determine the capo number
    	newCapo = songKeyLoc - tranKeyLoc;
    	
    	// Check for capo 12
    	if (newCapo == 12)
    		newCapo = 0;
    	else if (newCapo > 12)
    		newCapo -= 12;
    	
    	return newCapo;
    }

    /**
     * Returns the baritone equivalent key for the given key
     * @param key
     * @return
     */
    public static String getBaritoneKey(String key) {
        String ret = "";

        // Find the key in the circle of fifths
        int pos = StaticVars.circleOfFifths.lastIndexOf(key);

        // Move back one step around the circle
        if (pos == 0) {
            ret = StaticVars.circleOfFifths.get(StaticVars.circleOfFifths.size() - 1);
        } else {
            ret = StaticVars.circleOfFifths.get(pos - 1);
        }

        return ret;
    }
    
    /**
     * Returns the last index of the specified key, starting at the specified position
     * @param indexKey The key to search for
     * @param startAt The position to start at
     * @return
     */
    private static int lastIndexOf(String indexKey, int startAt) {
    	int index = 0;

        // Make sure we are starting within the list
        if (startAt < StaticVars.songKeys_transpose.size() && startAt > 0) {
            for (ListIterator<String> it = StaticVars.songKeys_transpose.listIterator(startAt); it.hasPrevious(); ) {
                index = it.previousIndex();
                if (it.previous().equals(indexKey)) {
                    break;
                }
            }
        }
    	
    	return index;
    }
}
