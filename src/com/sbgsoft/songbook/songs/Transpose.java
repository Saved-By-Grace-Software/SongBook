package com.sbgsoft.songbook.songs;

import java.util.ListIterator;

import com.sbgsoft.songbook.main.MainStrings;

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
    	
    	// Get the root note index
    	//rootIndex = MainStrings.songKeys.lastIndexOf(root);
    	rootIndex = MainStrings.songKeys.indexOf(root);
    	
    	// Get the index difference
    	//diff = MainStrings.songKeys.lastIndexOf(songKey) - MainStrings.songKeys.lastIndexOf(transposeKey);
    	diff = MainStrings.songKeys.indexOf(songKey) - MainStrings.songKeys.indexOf(transposeKey);
    	
    	// Set the new root note
    	if (diff < 0 && rootIndex - diff > MainStrings.songKeys.size() - 1) //wrap around right
    		newRoot = MainStrings.songKeys.get((rootIndex - diff) - MainStrings.songKeys.size());
    	else if (rootIndex - diff < 0) //wrap around left
			newRoot = MainStrings.songKeys.get(MainStrings.songKeys.size() - (diff - rootIndex));
		else 
			newRoot = MainStrings.songKeys.get(rootIndex - diff);
    	
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
    		
    		// Get the bass note index
    		bassIndex = MainStrings.songKeys.indexOf(bass);
    		
    		// Set the new bass note
        	if (diff < 0 && bassIndex - diff > MainStrings.songKeys.size() - 1) //wrap around right
        		newBass = MainStrings.songKeys.get((bassIndex - diff) - MainStrings.songKeys.size());
        	else if (bassIndex - diff < 0) //wrap around left
        		newBass = MainStrings.songKeys.get(MainStrings.songKeys.size() - (diff - bassIndex));
    		else 
    			newBass = MainStrings.songKeys.get(bassIndex - diff);
    		
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
    	int newCapo = 0;
    	
    	// Get the song key and transpose key locations
    	int songKeyLoc = MainStrings.songKeys.lastIndexOf(songKey);
    	//int tranKeyLoc = MainStrings.songKeys.indexOf(transposeKey);
    	int tranKeyLoc = lastIndexOf(transposeKey, songKeyLoc);
    	
    	//If the current capo is not 0 then change the song key location
    	if (currentCapo != 0) {
    		// Set the new song key location
//    		if (songKeyLoc + currentCapo > MainStrings.songKeys.size())
//    			songKeyLoc = (songKeyLoc + currentCapo) - MainStrings.songKeys.size();
//    		else
    			songKeyLoc = songKeyLoc + currentCapo;
    	}
    	
    	// Determine the capo number
//    	if (songKeyLoc > tranKeyLoc)
    		newCapo = songKeyLoc - tranKeyLoc;
//    	else
//    		newCapo = songKeyLoc + (MainStrings.songKeys.size() - tranKeyLoc);
    	
    	// Check for capo 12
    	if (newCapo == 12)
    		newCapo = 0;
    	
    	return newCapo;
    }
    
    /**
     * Returns the last index of the specified key, starting at the specified position
     * @param indexKey The key to search for
     * @param startAt The position to start at
     * @return
     */
    private static int lastIndexOf(String indexKey, int startAt) {
    	int index = 0;
    	
    	for (ListIterator<String> it = MainStrings.songKeys.listIterator(startAt); it.hasPrevious();) {
    		if (it.previous().equals(indexKey)) {
    			index = it.previousIndex();
    		}
    	}
    	
    	return index;
    }
}
