package com.sbgsoft.songbook.items;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.main.MainActivity;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class SetItem implements Item, Parcelable {
	private String name;
	private String date;
    private String link;
	
	public ArrayList<SongItem> songs;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeString(date);
        out.writeString(link);
		out.writeSerializable(songs);
	}
	
	public static final Parcelable.Creator<SetItem> CREATOR = new Creator<SetItem>() {
		@SuppressWarnings("unchecked")
		public SetItem createFromParcel(Parcel source) {
			SetItem setItem = new SetItem();
			setItem.name = source.readString();
			setItem.date = source.readString();
            setItem.link = source.readString();
			setItem.songs = (ArrayList<SongItem>)source.readSerializable();
			return setItem;
		}
		
		public SetItem[] newArray(int size) {
			return new SetItem[size];
		}
	};

    public SetItem(String setName, String setDate, String setLink) {
        setName(setName);
        setDate(setDate);
        setLink(setLink);
        songs = new ArrayList<>();
    }
	
	public SetItem() {
		setName("");
		setDate("");
        setLink("");
		songs = new ArrayList<>();
	}

	/**
	 * Returns the song name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the song author
	 */
	public String getDate() {
		return date;
	}

    public String getShortDate() {
        String ret = "";

        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date tmp = df.parse(date);

            SimpleDateFormat dfO = new SimpleDateFormat("MMM yy");
            ret = dfO.format(tmp);
        } catch (ParseException e) {

        }

        return ret;
    }

    public String getLink() {
        return link;
    }

    public String getSongs() {
        StringBuilder sb = new StringBuilder();

        // Loop through each song in the set
        for(SongItem s : songs) {
            // Add the song name to the string builder
            sb.append(s.getName());

            // Check to make sure we aren't on the last song
            if (songs.indexOf(s) < songs.size() - 1)
                sb.append(", ");
        }

        return sb.toString();
    }
	
	public void setName(String name) {
		this.name = name;
	}

	public void setDate(String date) {
		this.date = date.replaceAll("\\s", "");
	}

    public void setLink(String link) {
        this.link = link;
    }
	
	public void selfPopulateSongsList() {
		// Clear current songs list
		songs.clear();
		
    	Cursor c = MainActivity.dbAdapter.getSetSongs(name);
    	c.moveToFirst();
    	
    	// Populate the ArrayList
    	while (!c.isAfterLast()) {
    		// Get the strings from the cursor
        	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
        	String songAuthor = c.getString(c.getColumnIndex(DBStrings.TBLSONG_AUTHOR));
        	String setKey = c.getString(c.getColumnIndex(DBStrings.TBLSONG_KEY));
        	String songFile = c.getString(c.getColumnIndex(DBStrings.TBLSONG_FILE));
        	String songKey = MainActivity.dbAdapter.getSongKey(songName);
            String songLink = c.getString(c.getColumnIndex(DBStrings.TBLSONG_LINK));
            int songBpm = c.getInt(c.getColumnIndex(DBStrings.TBLSONG_BPM));
            String songTime = c.getString(c.getColumnIndex(DBStrings.TBLSONG_TIME));

        	// Add the song item
        	songs.add(new SongItem(songName, songAuthor, songKey, songFile, setKey, songBpm, songTime, songLink));
        	
        	// Move to the next song
        	c.moveToNext();
    	}
    	
    	// Close the cursor
    	c.close();
	}

    /**
     * Comparator for Set Items by date
     * Oldest date is first
     * @author SamIAm
     *
     */
    public static class SetItemComparableDate implements Comparator<Item> {

        public int compare(Item o1, Item o2) {
            int ret = -1;

            String[] split1 = ((SetItem)o1).getDate().split("/");
            String[] split2 = ((SetItem)o2).getDate().split("/");

            if (split1.length == 3 && split2.length == 3)
            {
                try
                {
                    // Parse date 1
                    int month1 = Integer.parseInt(split1[0].trim());
                    int day1 = Integer.parseInt(split1[1].trim());
                    int year1 = Integer.parseInt(split1[2].trim());

                    // Parse date 2
                    int month2 = Integer.parseInt(split2[0].trim());
                    int day2 = Integer.parseInt(split2[1].trim());
                    int year2 = Integer.parseInt(split2[2].trim());

                    // Compare years
                    if (year1 < year2)
                    {
                        // o1 is earlier than o2
                        ret = -1;
                    }
                    else if (year1 > year2)
                    {
                        // o1 is later than o2
                        ret = 1;
                    }
                    else
                    {
                        // Years are the same, compare months
                        if (month1 < month2)
                        {
                            // o1 is earlier than o2
                            ret = -1;
                        }
                        else if (month1 > month2)
                        {
                            // o1 is later than o2
                            ret = 1;
                        }
                        else
                        {
                            // Years & Months are the same, compare days
                            if (day1 < day2)
                            {
                                // o1 is less than o2
                                ret = -1;
                            }
                            else if (day1 > day2)
                            {
                                // o1 is later than o2
                                ret = 1;
                            }
                            else
                            {
                                // o1 and o2 are the same
                                ret = 0;
                            }
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    // Could not parse date correctly
                    ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
                }
            }
            else
            {
                // Could not parse date correctly
                ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
            }

            return ret;
        }
    }

    /**
     * Comparator for Set Items by date
     * Most recent date is first
     * @author SamIAm
     *
     */
    public static class SetItemComparableDateReverse implements Comparator<Item>{

        public int compare(Item o1, Item o2) {
            int ret = -1;

            String[] split1 = ((SetItem)o1).getDate().split("/");
            String[] split2 = ((SetItem)o2).getDate().split("/");

            if (split1.length == 3 && split2.length == 3)
            {
                try
                {
                    // Parse date 1
                    int month1 = Integer.parseInt(split1[0].trim());
                    int day1 = Integer.parseInt(split1[1].trim());
                    int year1 = Integer.parseInt(split1[2].trim());

                    // Parse date 2
                    int month2 = Integer.parseInt(split2[0].trim());
                    int day2 = Integer.parseInt(split2[1].trim());
                    int year2 = Integer.parseInt(split2[2].trim());

                    // Compare years
                    if (year1 < year2)
                    {
                        // o1 is earlier than o2
                        ret = 1;
                    }
                    else if (year1 > year2)
                    {
                        // o1 is later than o2
                        ret = -1;
                    }
                    else
                    {
                        // Years are the same, compare months
                        if (month1 < month2)
                        {
                            // o1 is earlier than o2
                            ret = 1;
                        }
                        else if (month1 > month2)
                        {
                            // o1 is later than o2
                            ret = -1;
                        }
                        else
                        {
                            // Years & Months are the same, compare days
                            if (day1 < day2)
                            {
                                // o1 is less than o2
                                ret = 1;
                            }
                            else if (day1 > day2)
                            {
                                // o1 is later than o2
                                ret = -1;
                            }
                            else
                            {
                                // o1 and o2 are the same
                                ret = 0;
                            }
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    // Could not parse date correctly
                    ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
                }
            }
            else
            {
                // Could not parse date correctly
                ret = ((SetItem)o1).getDate().compareToIgnoreCase(((SetItem)o2).getDate());
            }

            return ret;
        }
    }
}
