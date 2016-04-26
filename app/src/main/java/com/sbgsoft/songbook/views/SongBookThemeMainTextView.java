package com.sbgsoft.songbook.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.SongBookTheme;

public class SongBookThemeMainTextView extends TextView {
    private static final float SHADOW_RADIUS = 1.5f;
    private static final int SHADOW_DX = 3;
    private static final int SHADOW_DY = 3;

    public SongBookThemeMainTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SongBookThemeMainTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public SongBookThemeMainTextView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        // TODO: Get current theme from database
        SongBookTheme songBookTheme = new SongBookTheme();

        // Set the color to the current theme color
        setTextColor(songBookTheme.getMainFontColor());

        // Get the use shadow field
        if (context != null && attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SongBookThemeMainTextView, 0, 0);
            boolean useShadow = a.getBoolean(R.styleable.SongBookThemeMainTextView_useShadow, false);

            if (useShadow) {
                // Set shadow color
                setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, songBookTheme.getMainFontShadowColor());
            }
        }
    }
}
