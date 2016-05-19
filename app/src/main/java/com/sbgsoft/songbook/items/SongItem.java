package com.sbgsoft.songbook.items;

import java.io.Serializable;
import java.util.Comparator;

import android.os.Parcel;
import android.os.Parcelable;

import com.sbgsoft.songbook.main.StaticVars;

public class SongItem implements Item, Parcelable, Serializable {
	private static final long serialVersionUID = -152686405814171985L;
	
	private String name;
	private String author;
	private String key;
	private String file;
	private String text;
	private int capo;
	private String setKey;
    private int bpm;
    private String timeSignature;
    private String songLink;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeString(author);
		out.writeString(key);
		out.writeString(file);
		out.writeString(text);
		out.writeInt(capo);
		out.writeString(setKey);
        out.writeInt(bpm);
        out.writeString(timeSignature);
        out.writeString(songLink);
	}
	
	@Override
	public String toString() {
		return "SongItem [name=" + name + ", author=" + author + ", key=" + key +
                ", file=" + file + ", text=" + text + ", capo=" + capo +
                ", setKey=" + setKey + ", bpm=" + bpm + ", timeSig=" + timeSignature +
                ", songLink=" + songLink + "]";
	}
	
	public static final Parcelable.Creator<SongItem> CREATOR = new Creator<SongItem>() {
		public SongItem createFromParcel(Parcel source) {
			SongItem songItem = new SongItem();
			songItem.name = source.readString();
			songItem.author = source.readString();
			songItem.key = source.readString();
			songItem.file = source.readString();
			songItem.text = source.readString();
			songItem.capo = source.readInt();
			songItem.setKey = source.readString();
            songItem.bpm = source.readInt();
            songItem.timeSignature = source.readString();
            songItem.songLink = source.readString();
			return songItem;
		}
		
		public SongItem[] newArray(int size) {
			return new SongItem[size];
		}
	};
	
	/**
	 * Constructor
	 * @param songName
	 */
	public SongItem(String songName, String songAuthor, String songKey, String songFile) {
		name = songName;
		author = songAuthor;
		key = songKey;
		file = songFile;
		setText("");
		setCapo(0);
		setSetKey("");
        setBpm(0);
        setTimeSignature("");
        setSongLink("");
	}
	
	public SongItem(String songName, String songAuthor, String songKey, String songFile, String setKey) {
		name = songName;
		author = songAuthor;
		key = songKey;
		file = songFile;
		setText("");
		setCapo(0);
		setSetKey(setKey);
        setBpm(0);
        setTimeSignature("");
        setSongLink("");
	}

    public SongItem(String songName, String songAuthor, String songKey, String songFile, String setKey, int songBpm, String songTime, String songLink) {
        name = songName;
        author = songAuthor;
        key = songKey;
        file = songFile;
        setText("");
        setCapo(0);
        setSetKey(setKey);
        setBpm(songBpm);
        setTimeSignature(songTime);
        setSongLink(songLink);
    }
	
	public SongItem() {
		setName("");
		setAuthor("");
		setKey("");
		setFile("");
		setText("");
		setCapo(0);
		setSetKey("");
        setBpm(0);
        setTimeSignature("");
        setSongLink("");
	}

	/**
	 * Returns the song name
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the song author
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * Returns the song key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Returns the song file
	 */
	public String getSongFile() {
		return file;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}

	public int getCapo() {
		return capo;
	}

	public void setCapo(int capo) {
		this.capo = capo;
	}

	public String getSetKey() {
		return setKey;
	}

	public void setSetKey(String setKey) {
		this.setKey = setKey;
	}

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public String getTimeSignature() {
        return timeSignature;
    }

    public void setTimeSignature(String timeSignature) {
        this.timeSignature = timeSignature;
    }

    public String getSongLink() { return songLink; }

    public void setSongLink(String songLink) { this.songLink = songLink; }

    /**
     * Comparator for Song Items by author
     * @author SamIAm
     *
     */
    public static class SongItemComparableAuthor implements Comparator<Item> {

        public int compare(Item o1, Item o2) {
            return ((SongItem)o1).getAuthor().compareToIgnoreCase(((SongItem)o2).getAuthor());
        }
    }

    /**
     * Comparator for Song Items by key
     * @author SamIAm
     *
     */
    public static class SongItemComparableKey implements Comparator<Item>{

        public int compare(Item o1, Item o2) {
            // Get the song keys
            String key1 = ((SongItem)o1).getKey();
            String key2 = ((SongItem)o2).getKey();

            // Translate any special keys
            if (StaticVars.songKeyMap.containsKey(key1))
                key1 = StaticVars.songKeyMap.get(key1);
            if (StaticVars.songKeyMap.containsKey(key2))
                key2 = StaticVars.songKeyMap.get(key2);

            // Do a special compare for 'unknown'
            if (key1.equals(StaticVars.UNKNOWN) && key2.equals(StaticVars.UNKNOWN))
                return 0;
            else if (key1.equals(StaticVars.UNKNOWN) && !key2.equals(StaticVars.UNKNOWN))
                return 1;
            else if (!key1.equals(StaticVars.UNKNOWN) && key2.equals(StaticVars.UNKNOWN))
                return -1;

            // Compare the keys
            if (StaticVars.songKeys.indexOf(key1) > StaticVars.songKeys.indexOf(key2))
                return 1;
            else if (StaticVars.songKeys.indexOf(key1) == StaticVars.songKeys.indexOf(key2))
                return 0;
            else
                return -1;
        }
    }
}
