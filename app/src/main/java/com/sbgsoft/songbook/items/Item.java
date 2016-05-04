package com.sbgsoft.songbook.items;

import java.util.Comparator;

public interface Item {
	
	// Return the item name
	String getName();

    /**
     * Comparator for Song Items by author
     * @author SamIAm
     *
     */
    class ItemComparableName implements Comparator<Item> {

        public int compare(Item o1, Item o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
