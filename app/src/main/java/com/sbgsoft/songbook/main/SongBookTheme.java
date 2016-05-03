package com.sbgsoft.songbook.main;

public class SongBookTheme {

    //region Static Chord Color Values
    private static final int CHORD_COLOR_BLUE = 0xFF006B9F;     // Default
    private static final int CHORD_COLOR_GREEN = 0xFF1AA809;
    private static final int CHORD_COLOR_PURPLE = 0xFF4309A8;
    private static final int CHORD_COLOR_PINK = 0xFFEA1081;
    private static final int CHORD_COLOR_BLACK = 0xFF000000;
    private static final int CHORD_COLOR_ORANGE = 0xFFFD8200;
    private static final int CHORD_COLOR_RED = 0xFFD50000;
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
    private int mSeparatorBarColor;
    private int mSectionHeaderColor;
    private int mToolbarColor;
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
        mSeparatorBarColor = SEPARATOR_BAR;
        mToolbarColor = TOOLBAR;
        mThemeName = THEME_NAME;
    }

    public SongBookTheme(String themeColor) {
        // Select colors based upon themeColor
        switch(themeColor) {
            case "Blue":
                mBackgroundTop = BLUE_BACKGROUND_TOP;
                mBackgroundBottom = BLUE_BACKGROUND_BOTTOM;
                mMainFontColor = BLUE_MAIN_FONT;
                mMainFontShadowColor = BLUE_MAIN_FONT_SHADOW;
                mTitleFontColor = BLUE_TITLE_FONT;
                mTitleFontShadowColor = BLUE_TITLE_FONT_SHADOW;
                mSpinnerFontColor = BLUE_SPINNER_FONT;
                mSeparatorBarColor = BLUE_SEPARATOR_BAR;
                mSectionHeaderColor = BLUE_SECTION_HEADER;
                mToolbarColor = BLUE_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Green":
                mBackgroundTop = GREEN_BACKGROUND_TOP;
                mBackgroundBottom = GREEN_BACKGROUND_BOTTOM;
                mMainFontColor = GREEN_MAIN_FONT;
                mMainFontShadowColor = GREEN_MAIN_FONT_SHADOW;
                mTitleFontColor = GREEN_TITLE_FONT;
                mTitleFontShadowColor = GREEN_TITLE_FONT_SHADOW;
                mSpinnerFontColor = GREEN_SPINNER_FONT;
                mSeparatorBarColor = GREEN_SEPARATOR_BAR;
                mSectionHeaderColor = GREEN_SECTION_HEADER;
                mToolbarColor = GREEN_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Teal":
                mBackgroundTop = TEAL_BACKGROUND_TOP;
                mBackgroundBottom = TEAL_BACKGROUND_BOTTOM;
                mMainFontColor = TEAL_MAIN_FONT;
                mMainFontShadowColor = TEAL_MAIN_FONT_SHADOW;
                mTitleFontColor = TEAL_TITLE_FONT;
                mTitleFontShadowColor = TEAL_TITLE_FONT_SHADOW;
                mSpinnerFontColor = TEAL_SPINNER_FONT;
                mSeparatorBarColor = TEAL_SEPARATOR_BAR;
                mSectionHeaderColor = TEAL_SECTION_HEADER;
                mToolbarColor = TEAL_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Purple":
                mBackgroundTop = PURPLE_BACKGROUND_TOP;
                mBackgroundBottom = PURPLE_BACKGROUND_BOTTOM;
                mMainFontColor = PURPLE_MAIN_FONT;
                mMainFontShadowColor = PURPLE_MAIN_FONT_SHADOW;
                mTitleFontColor = PURPLE_TITLE_FONT;
                mTitleFontShadowColor = PURPLE_TITLE_FONT_SHADOW;
                mSpinnerFontColor = PURPLE_SPINNER_FONT;
                mSeparatorBarColor = PURPLE_SEPARATOR_BAR;
                mSectionHeaderColor = PURPLE_SECTION_HEADER;
                mToolbarColor = PURPLE_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Pink":
                mBackgroundTop = PINK_BACKGROUND_TOP;
                mBackgroundBottom = PINK_BACKGROUND_BOTTOM;
                mMainFontColor = PINK_MAIN_FONT;
                mMainFontShadowColor = PINK_MAIN_FONT_SHADOW;
                mTitleFontColor = PINK_TITLE_FONT;
                mTitleFontShadowColor = PINK_TITLE_FONT_SHADOW;
                mSpinnerFontColor = PINK_SPINNER_FONT;
                mSeparatorBarColor = PINK_SEPARATOR_BAR;
                mSectionHeaderColor = PINK_SECTION_HEADER;
                mToolbarColor = PINK_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Grey":
                mBackgroundTop = GREY_BACKGROUND_TOP;
                mBackgroundBottom = GREY_BACKGROUND_BOTTOM;
                mMainFontColor = GREY_MAIN_FONT;
                mMainFontShadowColor = GREY_MAIN_FONT_SHADOW;
                mTitleFontColor = GREY_TITLE_FONT;
                mTitleFontShadowColor = GREY_TITLE_FONT_SHADOW;
                mSpinnerFontColor = GREY_SPINNER_FONT;
                mSeparatorBarColor = GREY_SEPARATOR_BAR;
                mSectionHeaderColor = GREY_SECTION_HEADER;
                mToolbarColor = GREY_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Yellow":
                mBackgroundTop = YELLOW_BACKGROUND_TOP;
                mBackgroundBottom = YELLOW_BACKGROUND_BOTTOM;
                mMainFontColor = YELLOW_MAIN_FONT;
                mMainFontShadowColor = YELLOW_MAIN_FONT_SHADOW;
                mTitleFontColor = YELLOW_TITLE_FONT;
                mTitleFontShadowColor = YELLOW_TITLE_FONT_SHADOW;
                mSpinnerFontColor = YELLOW_SPINNER_FONT;
                mSeparatorBarColor = YELLOW_SEPARATOR_BAR;
                mSectionHeaderColor = YELLOW_SECTION_HEADER;
                mToolbarColor = YELLOW_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Orange":
                mBackgroundTop = ORANGE_BACKGROUND_TOP;
                mBackgroundBottom = ORANGE_BACKGROUND_BOTTOM;
                mMainFontColor = ORANGE_MAIN_FONT;
                mMainFontShadowColor = ORANGE_MAIN_FONT_SHADOW;
                mTitleFontColor = ORANGE_TITLE_FONT;
                mTitleFontShadowColor = ORANGE_TITLE_FONT_SHADOW;
                mSpinnerFontColor = ORANGE_SPINNER_FONT;
                mSeparatorBarColor = ORANGE_SEPARATOR_BAR;
                mSectionHeaderColor = ORANGE_SECTION_HEADER;
                mToolbarColor = ORANGE_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Red":
                mBackgroundTop = RED_BACKGROUND_TOP;
                mBackgroundBottom = RED_BACKGROUND_BOTTOM;
                mMainFontColor = RED_MAIN_FONT;
                mMainFontShadowColor = RED_MAIN_FONT_SHADOW;
                mTitleFontColor = RED_TITLE_FONT;
                mTitleFontShadowColor = RED_TITLE_FONT_SHADOW;
                mSpinnerFontColor = RED_SPINNER_FONT;
                mSeparatorBarColor = RED_SEPARATOR_BAR;
                mSectionHeaderColor = RED_SECTION_HEADER;
                mToolbarColor = RED_TOOLBAR;
                mThemeName = themeColor;
                break;
            case "Default (Slate)":
            default:
                mBackgroundTop = BACKGROUND_TOP;
                mBackgroundBottom = BACKGROUND_BOTTOM;
                mMainFontColor = MAIN_FONT;
                mMainFontShadowColor = MAIN_FONT_SHADOW;
                mTitleFontColor = TITLE_FONT;
                mTitleFontShadowColor = TITLE_FONT_SHADOW;
                mSpinnerFontColor = SPINNER_FONT;
                mSeparatorBarColor = SEPARATOR_BAR;
                mSectionHeaderColor = SECTION_HEADER;
                mToolbarColor = TOOLBAR;
                mThemeName = THEME_NAME;
        }
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
            case "Red":
                ret = CHORD_COLOR_RED;
                break;
            case "Default (Blue)":
            default:
                ret = CHORD_COLOR_BLUE;
        }

        return ret;
    }

    public int getSeparatorBarColor() {
        return mSeparatorBarColor;
    }

    public int getSectionHeaderColor() {
        return mSectionHeaderColor;
    }

    public int getToolbarColor() {
        return mToolbarColor;
    }
    //endregion

    //region Theme Color Values

    // https://www.google.com/design/spec/style/color.html#color-color-palette
    // http://paletton.com/#uid=1000u0kllllaFw0g0qFqFg0w0aF
    //   Use Background Top as base

    //region Default Slate Theme Color Values
    private static final String THEME_NAME = StaticVars.SETTINGS_DEFAULT_THEME_COLOR;
    private static final int BACKGROUND_TOP = 0xFF607D8B;           // 500-700
    private static final int BACKGROUND_BOTTOM = 0xFFECEFF1;        // 50
    private static final int MAIN_FONT = 0xFF011722;                // Paletton Deep Base
    private static final int MAIN_FONT_SHADOW = 0xFF023049;         // Paletton Deep UL
    private static final int TITLE_FONT = 0xFFB0BEC5;               // 200
    private static final int TITLE_FONT_SHADOW = 0xFF263238;        // 900
    private static final int SPINNER_FONT = 0xFFECEFF1;             // 50
    private static final int SEPARATOR_BAR = 0xFF011722;            // Paletton Deep Base
    private static final int SECTION_HEADER = 0x7737474F;           // 800
    private static final int TOOLBAR = 0x7737474F;                  // 800
    //endregion

    //region Blue Theme Color Values
    private static final int BLUE_BACKGROUND_TOP = 0xFF2962FF;
    private static final int BLUE_BACKGROUND_BOTTOM = 0xFFE3F2FD;
    private static final int BLUE_MAIN_FONT = 0xFF011423;
    private static final int BLUE_MAIN_FONT_SHADOW = 0xFF022B4A;
    private static final int BLUE_TITLE_FONT = 0xFF90CAF9;
    private static final int BLUE_TITLE_FONT_SHADOW = 0xFF0D47A1;
    private static final int BLUE_SPINNER_FONT = 0xFFE3F2FD;
    private static final int BLUE_SEPARATOR_BAR = 0xFF011423;
    private static final int BLUE_SECTION_HEADER = 0x771565C0;
    private static final int BLUE_TOOLBAR = 0x771565C0;
    //endregion

    //region Green Theme Color Values
    private static final int GREEN_BACKGROUND_TOP = 0xFF388E3C;
    private static final int GREEN_BACKGROUND_BOTTOM = 0xFFE8F5E9;
    private static final int GREEN_MAIN_FONT = 0xFF002A03;
    private static final int GREEN_MAIN_FONT_SHADOW = 0xFF005A06;
    private static final int GREEN_TITLE_FONT = 0xFFA5D6A7;
    private static final int GREEN_TITLE_FONT_SHADOW = 0xFF1B5E20;
    private static final int GREEN_SPINNER_FONT = 0xFFE8F5E9;
    private static final int GREEN_SEPARATOR_BAR = 0xFF002A03;
    private static final int GREEN_SECTION_HEADER = 0x772E7D32;
    private static final int GREEN_TOOLBAR = 0x772E7D32;
    //endregion

    //region Teal Theme Color Values
    private static final int TEAL_BACKGROUND_TOP = 0xFF009688;
    private static final int TEAL_BACKGROUND_BOTTOM = 0xFFE0F2F1;
    private static final int TEAL_MAIN_FONT = 0xFF00211D;
    private static final int TEAL_MAIN_FONT_SHADOW = 0xFF00473F;
    private static final int TEAL_TITLE_FONT = 0xFF80CBC4;
    private static final int TEAL_TITLE_FONT_SHADOW = 0xFF004D40;
    private static final int TEAL_SPINNER_FONT = 0xFFE0F2F1;
    private static final int TEAL_SEPARATOR_BAR = 0xFF00211D;
    private static final int TEAL_SECTION_HEADER = 0x7700695C;
    private static final int TEAL_TOOLBAR = 0x7700695C;
    //endregion

    //region Purple Theme Color Values (Deep Purple)
    private static final int PURPLE_BACKGROUND_TOP = 0xFF651FFF;
    private static final int PURPLE_BACKGROUND_BOTTOM = 0xFFEDE7F6;
    private static final int PURPLE_MAIN_FONT = 0xFF0F0225;
    private static final int PURPLE_MAIN_FONT_SHADOW = 0xFF1F034E;
    private static final int PURPLE_TITLE_FONT = 0xFFB39DDB;
    private static final int PURPLE_TITLE_FONT_SHADOW = 0xFF311B92;
    private static final int PURPLE_SPINNER_FONT = 0xFFEDE7F6;
    private static final int PURPLE_SEPARATOR_BAR = 0xFF0F0225;
    private static final int PURPLE_SECTION_HEADER = 0x774527A0;
    private static final int PURPLE_TOOLBAR = 0x774527A0;
    //endregion

    //region Pink Theme Color Values
    private static final int PINK_BACKGROUND_TOP = 0xFFE91E63;
    private static final int PINK_BACKGROUND_BOTTOM = 0xFFFCE4EC;
    private static final int PINK_MAIN_FONT = 0xFF2F0010;
    private static final int PINK_MAIN_FONT_SHADOW = 0xFF650022;
    private static final int PINK_TITLE_FONT = 0xFFF48FB1;
    private static final int PINK_TITLE_FONT_SHADOW = 0xFF880E4F;
    private static final int PINK_SPINNER_FONT = 0xFFFCE4EC;
    private static final int PINK_SEPARATOR_BAR = 0xFF2F0010;
    private static final int PINK_SECTION_HEADER = 0x77AD1457;
    private static final int PINK_TOOLBAR = 0x77AD1457;
    //endregion

    //region Grey Theme Color Values
    private static final int GREY_BACKGROUND_TOP = 0xFF616161;
    private static final int GREY_BACKGROUND_BOTTOM = 0xFFFAFAFA;
    private static final int GREY_MAIN_FONT = 0xFF000000;
    private static final int GREY_MAIN_FONT_SHADOW = 0xFF212121;
    private static final int GREY_TITLE_FONT = 0xFFEEEEEE;
    private static final int GREY_TITLE_FONT_SHADOW = 0xFF212121;
    private static final int GREY_SPINNER_FONT = 0xFFFAFAFA;
    private static final int GREY_SEPARATOR_BAR = 0xFF000000;
    private static final int GREY_SECTION_HEADER = 0x77424242;
    private static final int GREY_TOOLBAR = 0x77424242;
    //endregion

    //region Yellow Theme Color Values (Special)
    private static final int YELLOW_BACKGROUND_TOP = 0xFFFFFF00;
    private static final int YELLOW_BACKGROUND_BOTTOM = 0xFFFFFFD3;
    private static final int YELLOW_MAIN_FONT = 0xFF000000;
    private static final int YELLOW_MAIN_FONT_SHADOW = 0xFF424242;
    private static final int YELLOW_TITLE_FONT = 0xFF000000;
    private static final int YELLOW_TITLE_FONT_SHADOW = 0x779E9E9E;
    private static final int YELLOW_SPINNER_FONT = 0xFF424242;
    private static final int YELLOW_SEPARATOR_BAR = 0xFF000000;
    private static final int YELLOW_SECTION_HEADER = 0x77424242;
    private static final int YELLOW_TOOLBAR = 0x77424242;
    //endregion

    //region Orange Theme Color Values (Deep Orange)
    private static final int ORANGE_BACKGROUND_TOP = 0xFFFF3D00;
    private static final int ORANGE_BACKGROUND_BOTTOM = 0xFFFBE9E7;
    private static final int ORANGE_MAIN_FONT = 0xFF411102;
    private static final int ORANGE_MAIN_FONT_SHADOW = 0xFF731C00;
    private static final int ORANGE_TITLE_FONT = 0xFFFFAB91;
    private static final int ORANGE_TITLE_FONT_SHADOW = 0xFFBF360C;
    private static final int ORANGE_SPINNER_FONT = 0xFFFBE9E7;
    private static final int ORANGE_SEPARATOR_BAR = 0xFF360D00;
    private static final int ORANGE_SECTION_HEADER = 0x77D84315;
    private static final int ORANGE_TOOLBAR = 0xFFDD2C00;
    //endregion

    //region Red Theme Color Values (Special)
    private static final int RED_BACKGROUND_TOP = 0xFF670000;
    private static final int RED_BACKGROUND_BOTTOM = 0xFFFFCDD2;
    private static final int RED_MAIN_FONT = 0xFF360000;
    private static final int RED_MAIN_FONT_SHADOW = 0xFF460303;
    private static final int RED_TITLE_FONT = 0xFFFFA4A4;
    private static final int RED_TITLE_FONT_SHADOW = 0xFFA70000;
    private static final int RED_SPINNER_FONT = 0xFFFFEBEE;
    private static final int RED_SEPARATOR_BAR = 0xFF360000;
    private static final int RED_SECTION_HEADER = 0x776B0000;
    private static final int RED_TOOLBAR = 0xFF6B0000;
    //endregion
    //endregion
}
