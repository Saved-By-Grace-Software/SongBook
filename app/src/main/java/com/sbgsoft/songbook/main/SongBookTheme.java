package com.sbgsoft.songbook.main;

public class SongBookTheme {

    //region Static Default Values
    private static final int BACKGROUND_TOP = 0xFF6783A1;
    private static final int BACKGROUND_BOTTOM = 0xFFDEEBFF;
    private static final int BACKGROUND_ANGLE = 90;
    private static final int MAIN_FONT = 0xFF000040;
    private static final int MAIN_FONT_SHADOW = 0x77000000;
    private static final int TITLE_FONT = 0x7FFFFFFF;
    private static final int TITLE_FONT_SHADOW = 0x77000000;
    private static final int SPINNER_FONT = 0xFFFFFFFF;
    //endregion

    //region Static Chord Color Values
    private static final int CHORD_COLOR_BLUE = 0xFF006B9F;     // Default
    private static final int CHORD_COLOR_GREEN = 0xFF1AA809;
    private static final int CHORD_COLOR_PURPLE = 0xFF4309A8;
    private static final int CHORD_COLOR_PINK = 0xFFEA1081;
    private static final int CHORD_COLOR_BLACK = 0xFF000000;
    private static final int CHORD_COLOR_ORANGE = 0xFFFD8200;
    //endregion

    //region Private Class Members
    private int mBackgroundTop;
    private int mBackgroundBottom;
    private int mBackgroundAngle;
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
        mBackgroundAngle = BACKGROUND_ANGLE;
        mMainFontColor = MAIN_FONT;
        mMainFontShadowColor = MAIN_FONT_SHADOW;
        mTitleFontColor = TITLE_FONT;
        mTitleFontShadowColor = TITLE_FONT_SHADOW;
        mSpinnerFontColor = SPINNER_FONT;
    }
    //endregion

    //region Getters
    public int getMainFontColor() {
        return mMainFontColor;
    }

    public int getBackgroundBottom() {
        return mBackgroundBottom;
    }

    public int getBackgroundTop() {
        return mBackgroundTop;
    }

    public int getBackgroundAngle() {
        return mBackgroundAngle;
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
    public void setMainFontColor(int mainFontColor) {
        this.mMainFontColor = mainFontColor;
    }

    public void setBackgroundBottom(int backgroundBottom) {
        this.mBackgroundBottom = backgroundBottom;
    }

    public void setBackgroundTop(int backgroundTop) {
        this.mBackgroundTop = backgroundTop;
    }

    public void setBackgroundAngle(int backgroundAngle) {
        this.mBackgroundAngle = backgroundAngle;
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
}
