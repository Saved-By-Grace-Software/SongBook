package com.sbgsoft.songbook.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;

/**
 * Created by SamIAm on 4/30/2016.
 */
public class SongBookThemeButton extends Button {
    private Context mContext;
    private AttributeSet mAttrs;

    public SongBookThemeButton(Context context) {
        super(context);
        mContext = context;
        mAttrs = null;
        init();
    }

    public SongBookThemeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        init();
    }

    public SongBookThemeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mAttrs = attrs;
        init();
    }

    private void init() {
        // Get current theme from database
        SongBookTheme songBookTheme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Set the button text color to match the theme
        setTextColor(songBookTheme.getMainFontColor());
    }
}
