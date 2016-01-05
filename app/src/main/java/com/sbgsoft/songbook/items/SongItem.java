package com.sbgsoft.songbook.items;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

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
	}
	
	@Override
	public String toString() {
		return "SongItem [name=" + name + ", author=" + author + ", key=" + key +
                ", file=" + file + ", text=" + text + ", capo=" + capo +
                ", setKey=" + setKey + ", bpm=" + bpm + ", timeSig=" + timeSignature + "]";
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
}
