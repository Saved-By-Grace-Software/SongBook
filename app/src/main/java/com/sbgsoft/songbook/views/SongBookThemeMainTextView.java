package com.sbgsoft.songbook.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sbgsoft.songbook.main.SongBookTheme;

public class SongBookThemeMainTextView extends TextView {
    public SongBookThemeMainTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public SongBookThemeMainTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public SongBookThemeMainTextView(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        // TODO: Get current theme from database
        SongBookTheme songBookTheme = new SongBookTheme();

        // Set the color to the current theme color
        setTextColor(songBookTheme.getMainFontColor());

        if (attrs != null) {
            float shadowRad = attrs.getAttributeFloatValue(android.R.attr.shadowRadius, 0.0f);

            if (shadowRad > 0) {
                // Set shadow color
                setShadowLayer(1.5f, 3, 3, songBookTheme.getMainFontShadowColor());
            }
        }
    }
}
