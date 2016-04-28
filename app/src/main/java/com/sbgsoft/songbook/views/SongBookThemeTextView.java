package com.sbgsoft.songbook.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainActivity;
import com.sbgsoft.songbook.main.SongBookTheme;

public class SongBookThemeTextView extends TextView {
    private static final float SHADOW_RADIUS = 1.5f;
    private static final int SHADOW_DX = 3;
    private static final int SHADOW_DY = 3;

    public SongBookThemeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SongBookThemeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public SongBookThemeTextView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        // Get current theme from database
        SongBookTheme songBookTheme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Read the xml attributes
        if (context != null && attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SongBookThemeTextView, 0, 0);

            // Check for using shadow
            boolean useShadow = a.getBoolean(R.styleable.SongBookThemeTextView_useShadow, false);
            if (useShadow) {
                // Set shadow color
                setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, songBookTheme.getMainFontShadowColor());
            }

            // Check for a title or spinner
            boolean isTitle = a.getBoolean(R.styleable.SongBookThemeTextView_isTitle, false);
            boolean isSpinner = a.getBoolean(R.styleable.SongBookThemeTextView_isSpinner, false);

            if (isTitle) {
                // Set the color to the current theme color
                setTextColor(songBookTheme.getTitleFontColor());
                setLinkTextColor(songBookTheme.getTitleFontColor());
            } else if (isSpinner) {
                // Set the color to the current theme color
                setTextColor(songBookTheme.getSpinnerFontColor());
            } else {
                // Set the color to the current theme color
                setTextColor(songBookTheme.getMainFontColor());
                setLinkTextColor(songBookTheme.getMainFontColor());
            }
        }
    }
}
