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
    private Context mContext;
    private AttributeSet mAttrs;
    private boolean useShadow;
    private boolean isTitle;
    private boolean isSpinner;
    private boolean isSection;
    private SongBookTheme songBookTheme;

    public SongBookThemeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mAttrs = attrs;
        init();
    }

    public SongBookThemeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        init();

    }

    public SongBookThemeTextView(Context context) {
        super(context);
        mContext = context;
        mAttrs = null;
        init();
    }

    private void init() {
        // Get current theme from database
        songBookTheme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();

        // Read the xml attributes
        if (mContext != null && mAttrs != null) {
            TypedArray a = mContext.obtainStyledAttributes(mAttrs, R.styleable.SongBookThemeTextView, 0, 0);

            // Get the custom attributes
            useShadow = a.getBoolean(R.styleable.SongBookThemeTextView_useShadow, false);
            isTitle = a.getBoolean(R.styleable.SongBookThemeTextView_isTitle, false);
            isSpinner = a.getBoolean(R.styleable.SongBookThemeTextView_isSpinner, false);
            isSection = a.getBoolean(R.styleable.SongBookThemeTextView_isSection, false);

            recolorText();
        }
    }

    public void setCustomText(int color, boolean useShadow, int shadowColor) {
        setTextColor(color);
        setLinkTextColor(color);
        if (useShadow)
            setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, shadowColor);
    }

    public void recolorText() {
        if (isTitle) {
            // Set the color to the current theme color
            setTextColor(songBookTheme.getTitleFontColor());
            setLinkTextColor(songBookTheme.getTitleFontColor());

            // Check for shadow
            if (useShadow) {
                // Set shadow color
                setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, songBookTheme.getTitleFontShadowColor());
            }
        } else if (isSpinner) {
            // Set the color to the current theme color
            setTextColor(songBookTheme.getSpinnerFontColor());

            // Check for shadow
            if (useShadow) {
                // Set shadow color
                setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, songBookTheme.getTitleFontShadowColor());
            }
        } else if (isSection) {
            setBackgroundColor(songBookTheme.getSectionHeaderColor());
            setTextColor(songBookTheme.getTitleFontColor());
        } else {
            // Set the color to the current theme color
            setTextColor(songBookTheme.getMainFontColor());
            setLinkTextColor(songBookTheme.getMainFontColor());

            // Check for shadow
            if (useShadow) {
                // Set shadow color
                setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, songBookTheme.getMainFontShadowColor());
            }
        }
    }
}
