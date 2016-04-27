package com.sbgsoft.songbook.main;

public class SongBookTheme {

    //region Static Chord Color Values
    private static final int CHORD_COLOR_BLUE = 0xFF006B9F;     // Default
    private static final int CHORD_COLOR_GREEN = 0xFF1AA809;
    private static final int CHORD_COLOR_PURPLE = 0xFF4309A8;
    private static final int CHORD_COLOR_PINK = 0xFFEA1081;
    private static final int CHORD_COLOR_BLACK = 0xFF000000;
    private static final int CHORD_COLOR_ORANGE = 0xFFFD8200;
    //endregion

    //region Private Class Members
    private String mThemeName;
    private int mBackgroundTop;
    private int mBackgroundBottom;
    private int mMainFontColor;
    private int mMainFontShadowColor;
    private int mTitleFontColor;
    private int mTitleFontShadowColor;
    private int mSpinnerFontColor;
    //endregion

    //region Constructors
    public SongBookTheme() {
        // By default we use the blue theme
        mBackgroundTop = BACKGROUND_TOP;
        mBackgroundBottom = BACKGROUND_BOTTOM;
        mMainFontColor = MAIN_FONT;
        mMainFontShadowColor = MAIN_FONT_SHADOW;
        mTitleFontColor = TITLE_FONT;
        mTitleFontShadowColor = TITLE_FONT_SHADOW;
        mSpinnerFontColor = SPINNER_FONT;
        mThemeName = THEME_NAME;
    }

    public SongBookTheme(String themeColor) {
        // TODO: Select colors based upon themeColor

        // By default we use the blue theme
        mBackgroundTop = GREEN_BACKGROUND_TOP;
        mBackgroundBottom = GREEN_BACKGROUND_BOTTOM;
        mMainFontColor = GREEN_MAIN_FONT;
        mMainFontShadowColor = GREEN_MAIN_FONT_SHADOW;
        mTitleFontColor = GREEN_TITLE_FONT;
        mTitleFontShadowColor = GREEN_TITLE_FONT_SHADOW;
        mSpinnerFontColor = GREEN_SPINNER_FONT;
        mThemeName = themeColor;
    }
    //endregion

    //region Getters
    public String getThemeName() {
        return mThemeName;
    }

    public int getMainFontColor() {
        return mMainFontColor;
    }

    public int getBackgroundBottom() {
        return mBackgroundBottom;
    }

    public int getBackgroundTop() {
        return mBackgroundTop;
    }

    public int getTitleFontColor() {
        return mTitleFontColor;
    }

    public int getMainFontShadowColor() {
        return mMainFontShadowColor;
    }

    public int getTitleFontShadowColor() {
        return mTitleFontShadowColor;
    }

    public int getSpinnerFontColor() {
        return mSpinnerFontColor;
    }

    public static int getChordColorCode(String colorName) {
        int ret;

        switch(colorName) {
            case "Green":
                ret = CHORD_COLOR_GREEN;
                break;
            case "Purple":
                ret = CHORD_COLOR_PURPLE;
                break;
            case "Pink":
                ret = CHORD_COLOR_PINK;
                break;
            case "Black":
                ret = CHORD_COLOR_BLACK;
                break;
            case "Orange":
                ret = CHORD_COLOR_ORANGE;
                break;
            case "Default (Blue)":
            default:
                ret = CHORD_COLOR_BLUE;
        }

        return ret;
    }
    //endregion

    //region Setters
    public void setThemeName(String themeName) {
        this.mThemeName = themeName;
    }

    public void setMainFontColor(int mainFontColor) {
        this.mMainFontColor = mainFontColor;
    }

    public void setBackgroundBottom(int backgroundBottom) {
        this.mBackgroundBottom = backgroundBottom;
    }

    public void setBackgroundTop(int backgroundTop) {
        this.mBackgroundTop = backgroundTop;
    }

    public void setTitleFontColor(int titleFontColot) {
        this.mTitleFontColor = titleFontColot;
    }

    public void setMainFontShadowColor(int mainFontShadowColor) {
        this.mMainFontShadowColor = mainFontShadowColor;
    }

    public void setTitleFontShadowColor(int titleFontShadowColor) {
        this.mTitleFontShadowColor = titleFontShadowColor;
    }

    public void setSpinnerFontColor(int spinnerFontColor) {
        this.mSpinnerFontColor = spinnerFontColor;
    }
    //endregion

    //region Theme Color Values
    //region Static Default Values
    private static final String THEME_NAME = StaticVars.SETTINGS_DEFAULT_THEME_COLOR;
    private static final int BACKGROUND_TOP = 0xFF6783A1;
    private static final int BACKGROUND_BOTTOM = 0xFFDEEBFF;
    private static final int MAIN_FONT = 0xFF000040;
    private static final int MAIN_FONT_SHADOW = 0x77000000;
    private static final int TITLE_FONT = 0x7FFFFFFF;
    private static final int TITLE_FONT_SHADOW = 0x77000000;
    private static final int SPINNER_FONT = 0xFFFFFFFF;
    //endregion

    //region Static Default Values
    private static final String GREEN_THEME_NAME = StaticVars.SETTINGS_DEFAULT_THEME_COLOR;
    private static final int GREEN_BACKGROUND_TOP = 0xFF325231;
    private static final int GREEN_BACKGROUND_BOTTOM = 0xFFA6E7A4;
    private static final int GREEN_MAIN_FONT = 0xFF000040;
    private static final int GREEN_MAIN_FONT_SHADOW = 0x77000000;
    private static final int GREEN_TITLE_FONT = 0x7FFFFFFF;
    private static final int GREEN_TITLE_FONT_SHADOW = 0x77000000;
    private static final int GREEN_SPINNER_FONT = 0xFFFFFFFF;
    //endregion
    //endregion
}
