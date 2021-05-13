package com.sbgsoft.songbook.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.db.DBAdapter;
import com.sbgsoft.songbook.db.DBStrings;
import com.sbgsoft.songbook.files.OpenFile;
import com.sbgsoft.songbook.items.SetItem;
import com.sbgsoft.songbook.items.SetSearchCriteria;
import com.sbgsoft.songbook.items.Settings;
import com.sbgsoft.songbook.items.SongItem;
import com.sbgsoft.songbook.items.SongSearchCriteria;
import com.sbgsoft.songbook.main.StaticVars.SongFileType;
import com.sbgsoft.songbook.sets.CurrentSetTab;
import com.sbgsoft.songbook.sets.SetActivity;
import com.sbgsoft.songbook.sets.SetGroupArrayAdapter;
import com.sbgsoft.songbook.sets.SetsTab;
import com.sbgsoft.songbook.songs.ChordDisplay;
import com.sbgsoft.songbook.songs.ChordProParser;
import com.sbgsoft.songbook.songs.EditSongDetailsActivity;
import com.sbgsoft.songbook.songs.SongActivity;
import com.sbgsoft.songbook.songs.SongsTab;
import com.sbgsoft.songbook.songs.TextFileImporter;
import com.sbgsoft.songbook.songs.Transpose;
import com.sbgsoft.songbook.views.AutoFitTextView;
import com.sbgsoft.songbook.views.SongBookThemeTextView;
import com.sbgsoft.songbook.zip.Compress;
import com.sbgsoft.songbook.zip.Decompress;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //region Class Variables
	// *****************************************************************************
	// * 
	// * Class Variables
	// * 
	// *****************************************************************************
	public static DBAdapter dbAdapter;
	static ViewPager mViewPager;
	public Fragment currSetFragment;
	public Fragment setsFragment;
	public Fragment songsFragment;
    static TabLayout tabLayout;
    static Toolbar toolbar;

	private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ArrayList<NavDrawerItem> mNavDrawerItems;
    private NavDrawerListAdapter mNavDrawerAdapter;
    private ViewGroup.LayoutParams tabLayoutLP;

	private String importFilePath = "";
	
	private Map<String, Boolean> addSongsDialogMap = new HashMap<String, Boolean>();
	private ArrayList<String> addSongsDialogList = new ArrayList<String>();
	
	private Map<String, Boolean> addSetsDialogMap = new HashMap<String, Boolean>();
	private ArrayList<String> addSetsDialogList = new ArrayList<String>();
	
	private ProgressDialog progressDialog;

    private SetItem setToExport;
	//endregion


    //region Class Functions
	// *****************************************************************************
    // * 
    // * Class Functions
    // * 
    // *****************************************************************************
    /**
     *  Called when the activity is first created. 
     **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the database
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        // Set the main view
        setContentView(R.layout.activity_main);

        // Get the current theme from the database
        SongBookTheme theme = dbAdapter.getCurrentSettings().getSongBookTheme();

        // Apply the background color
        View layout = findViewById(R.id.drawer_layout);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {theme.getBackgroundTop(),theme.getBackgroundBottom()});
        gd.setCornerRadius(0f);
        layout.setBackground(gd);

        // Set up the toolbar
        toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        //toolbar.setLogo(R.drawable.ic_launcher);
        toolbar.setBackgroundColor(theme.getToolbarColor());
        setSupportActionBar(toolbar);

        // Set up the view pager
        mViewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        setupViewPager(mViewPager);

        // Create the tab layout
        tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setBackgroundColor(theme.getToolbarColor());
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                // Enable changing tab on click
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayoutLP = tabLayout.getLayoutParams();

        // Set up the navigation drawer
        setupNavDrawer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;

        // Get the item that was selected
        int id = item.getItemId();

        // Check for which option was selected
        if (id == R.id.action_settings) {
            showSettingsPage();
            ret = true;
        } else if (id == R.id.action_search) {
            // Get the current page
            int currPage = mViewPager.getCurrentItem();

            switch (currPage) {
                case 1:
                    // Sets page
                    findSetDialog();
                    break;
                case 2:
                    // Current set page
                    findSongDialog();
                    mViewPager.setCurrentItem(0);
                    break;
                case 0:
                default:
                    // Default to songs search
                    findSongDialog();
                    break;
            }

            ret = true;
        }

        if (ret)
            return ret;
        else
            return super.onOptionsItemSelected(item);
    }
    
    /**
     * Called when the activity is destroyed
     */
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        dbAdapter.close();
    }
    
    /**
     * Get the return from the file dialog activity
     */
    public synchronized void onActivityResult(final int requestCode,
        int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {
        	String activityType = data.getStringExtra(StaticVars.ACTIVITY_RESPONSE_TYPE);
        	
        	// If returning from an import song activity
        	if (activityType.equals(StaticVars.IMPORT_SONG_ACTIVITY)) {
	            importFilePath = data.getStringExtra(OpenFile.RESULT_PATH);
	            createSong();
        	}
        	// If returning from an import database activity
        	else if (activityType.equals(StaticVars.IMPORT_DB_ACTIVITY)) {
        		String filePath = data.getStringExtra(OpenFile.RESULT_PATH);
        		importFile(filePath, true, "This will erase all data currently in your database.  Do you want to continue?");
        	}
            // If returning from an import set activity
            else if (activityType.equals(StaticVars.IMPORT_SET_ACTIVITY)) {
                String filePath = data.getStringExtra(OpenFile.RESULT_PATH);
                importFile(filePath, false, "Are you sure you want to import \"" + filePath + "\"");
            }
        	// If returning from an export database activity
        	else if (activityType.equals(StaticVars.EXPORT_DB_ACTIVITY)) {
        		String folder = data.getStringExtra(OpenFile.RESULT_PATH);
        		exportAll(folder);
        	}
            // If returning from an export set activity
            else if (activityType.equals(StaticVars.EXPORT_SET_ACTIVITY)) {
                String folder = data.getStringExtra(OpenFile.RESULT_PATH);
                exportSet(folder);
            }
            // If returning from an edit song details activity
            else if (activityType.equals(StaticVars.EDIT_SONG_ATT_ACTIVITY)) {
                // Refresh lists
                ((SongsTab)songsFragment).refillSongsList();
                ((SetsTab)setsFragment).refillSetsList();
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
            }

        } 

    }
    
    /**
     * Makes the gradient show smoothly
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case StaticVars.PERMISSIONS_BACKUP_IMPORT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    executePermReqFunction(requestCode);
                } else {
                    // Permission Denied
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Must have access to External Storage for this function!", Snackbar.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //endregion


    //region Other Functions
    /**
     * Sets up the view pager
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        // Create the tab objects
        setsFragment = new SetsTab();
        songsFragment = new SongsTab();
        currSetFragment = new CurrentSetTab();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(songsFragment, "Songs");
        adapter.addFrag(setsFragment, "Sets");
        adapter.addFrag(currSetFragment, "Current Set");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    /**
     * Create and setup the navigation drawer
     */
    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name)
        {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                // Reset the lists
                setMainNavDrawerItems();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the navigation drawer icon
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Add items to the drawer
        mNavDrawerItems = new ArrayList<>();
        setMainNavDrawerItems();

        // Set up the drawer list
        mNavDrawerAdapter = new NavDrawerListAdapter(getApplicationContext(), mNavDrawerItems);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.nav_drawer_header, null, false);
        mDrawerList.addHeaderView(listHeaderView);
        mDrawerList.setAdapter(mNavDrawerAdapter);

        // Add listener for clicks
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                navMenuItemClicked(position);
            }
        });
    }

    /**
     * Sets the initial items for the nav drawer
     */
    private void setNavDrawerItems(int stringArrayId, int iconArrayId) {
        // Make sure we aren't null
        if (mNavDrawerItems != null) {
            // Clear the current list
            mNavDrawerItems.clear();

            // Get the list items
            String[] options = getResources().getStringArray(stringArrayId);
            TypedArray navMenuIcons = getResources().obtainTypedArray(iconArrayId);

            // Add them to the list
            for (int i = 0; i < options.length; i++) {
                mNavDrawerItems.add(new NavDrawerItem(options[i], navMenuIcons.getResourceId(i, -1)));
            }

            // Notify the adapter of changes
            if (mNavDrawerAdapter != null)
                mNavDrawerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets the main nav drawer items
     */
    private void setMainNavDrawerItems() {
        setNavDrawerItems(R.array.main_nav_menu, R.array.main_nav_icons);
    }

    /**
     * Handles a click on the navigation drawer menu
     * @param position
     */
    public void navMenuItemClicked(int position) {
        // Get the item clicked on
        if (position == 0) {
            // Close the drawer
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else if (position > 0 && position < mNavDrawerItems.size() + 1) {
            String item = mNavDrawerItems.get(position - 1).getTitle();

            // Decide what to do with each menu item
            switch (item) {
                //region Songs Menu
                case "Songs\u2026":
                    // Show the songs submenu
                    setNavDrawerItems(R.array.songs_nav_menu, R.array.songs_nav_icons);
                    break;
                case "Create Song":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    createSong();
                    break;
                case "Import Song":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    permissionRequiredFunction(StaticVars.PERMISSIONS_SONG_IMPORT);
                    break;
                case "Find Song":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    findSongDialog();
                    break;
                case "Delete All Songs":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    deleteAllSongs();
                    break;
                //endregion

                //region Sets Menu
                case "Sets\u2026":
                    // Show the songs submenu
                    setNavDrawerItems(R.array.sets_nav_menu, R.array.sets_nav_icons);
                    break;
                case "Create Set":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    createSet();
                    break;
                case "Import Set":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    permissionRequiredFunction(StaticVars.PERMISSIONS_SET_IMPORT);
                    break;
                case "Find Set":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    findSetDialog();
                    break;
                case "Delete All Sets":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    deleteAllSets();
                    break;
                //endregion

                //region Song Groups Menu
                case "Song Groups\u2026":
                    // Show the song groups submenu
                    setNavDrawerItems(R.array.songgrp_nav_menu, R.array.songgrp_nav_icons);
                    break;
                case "Create Song Group":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    createSongGroup();
                    break;
                case "Delete Song Group":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    deleteSongGroup();
                    break;
                case "Delete All Song Groups":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    deleteAllSongGroups();
                    break;
                //endregion

                //region Set Groups Menu
                case "Set Groups\u2026":
                    // Show the set groups submenu
                    setNavDrawerItems(R.array.setgrp_nav_menu, R.array.setgrp_nav_icons);
                    break;
                case "Create Set Group":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    createSetGroup();
                    break;
                case "Delete Set Group":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    deleteSetGroup();
                    break;
                case "Delete All Set Groups":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    deleteAllSetGroups();
                    break;
                //endregion

                //region Import Export Menu
                case "Upload/Download\u2026":
                    // Show the import/export submenu
                    setNavDrawerItems(R.array.impexp_nav_menu, R.array.impexp_nav_icons);
                    break;
                case "Download from Cloud":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    permissionRequiredFunction(StaticVars.PERMISSIONS_BACKUP_IMPORT);
                    break;
                case "Upload to Cloud":
                    // Reset the nav drawer items
                    setMainNavDrawerItems();

                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    permissionRequiredFunction(StaticVars.PERMISSIONS_BACKUP_EXPORT);
                    break;
                //endregion

                //region Other Menus
                case "Settings":
                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    showSettingsPage();
                    break;
                case "How To\u2026":
                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    showHowTos();
                    break;
                case "About SongBook":
                    // Close the app drawer before taking action
                    mDrawerLayout.closeDrawer(Gravity.LEFT);

                    showAboutBox();
                    break;
                case "Back":
                    // Go back to the main menu
                    setMainNavDrawerItems();
                    break;
                //endregion

                default:
                    break;
            }
        }
    }

    /**
     * Shows the about box with app information
     */
    public void showAboutBox() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

        // Set the dialog view to show the about message
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.simple_text_dialog, (ViewGroup) findViewById(R.id.simple_dialog_root));
        alert.setView(dialoglayout);
        final TextView tv = (TextView)dialoglayout.findViewById(R.id.simple_dialog_text);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

    	// Create the dialog
    	alert.setTitle("About " + getString(R.string.app_name));
    	
    	// Build the message
    	int start, end, startTitle = 0, endTitle;
    	SpannableStringBuilder message = new SpannableStringBuilder();
    	StyleSpan italics;
    	RelativeSizeSpan smallFont;

    	try {
			message.append(getString(R.string.full_app_name) + " v" + 
					getPackageManager().getPackageInfo(getPackageName(), 0).versionName +
					StaticVars.EOL);
		} catch (NameNotFoundException e) {
			message.append(getString(R.string.full_app_name) + StaticVars.EOL);
		}
    	message.append("Database Version " + DBStrings.DATABASE_VERSION + StaticVars.EOL);
        endTitle = message.length();
    	message.append(StaticVars.EOL);
    	message.append("Saved By Grace Software" + StaticVars.EOL);
    	message.append("Saxonburg, PA" + StaticVars.EOL);
    	message.append("https://fahnfam.com" + StaticVars.EOL);
        message.append("savedbygracesoft@gmail.com" + StaticVars.EOL);
    	message.append(StaticVars.EOL);
    	message.append("Virtual SongBook is designed to allow you to carry all of your guitar music with you wherever " + 
    			"you go on your Android phone or tablet. It also allows you to create sets of songs for performances, gigs " +
    			"or worship. If you have any problems or questions please send us an email.  God Bless!!" + StaticVars.EOL);
    	message.append(StaticVars.EOL);
    	start = message.length();
    	message.append("\"For by grace you have been saved through faith. And this is not your own doing; it is the gift of God, " +
    			"not a result of works so that no one may boast." +
                StaticVars.EOL + "-Ephesians 2:8-9");
        end = message.length();

        // Set the spans
        italics = new StyleSpan(Typeface.ITALIC);
        smallFont = new RelativeSizeSpan(0.75f);
        message.setSpan(italics, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        message.setSpan(smallFont, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        message.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), startTitle, endTitle,  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        message.setSpan(new StyleSpan(Typeface.BOLD), startTitle, endTitle, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // Make the links clickable
        Linkify.addLinks(message, Linkify.ALL);

        // Display information
        tv.setText(message);
    	
    	// Add an OK button
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog and make the links clickable
        alert.show();
    }

    /**
     * Shows the how to instructions
     */
    public void showHowTos() {
        // Create the options array
        final CharSequence[] options = getResources().getStringArray(R.array.how_tos);

        // Create the options dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("How To What?");

        // Set the items
        alert.setItems(options, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichItem) {
                StringBuilder message = new StringBuilder();
                ArrayList<String> instructions = new ArrayList<String>();
                int counter = 1;

                // Create the instructions dialog
                AlertDialog.Builder instrAlert = new AlertDialog.Builder(MainActivity.this);
                instrAlert.setTitle("How To " + options[whichItem]);

                // Make the text for instructions small
                message.append("<small>");

                // Build the instructions to show
                switch (whichItem) {
                    case 0:     // Create a set
                        instructions = StaticVars.howToCreateSet;
                        break;
                    case 1:     // Add songs to a set
                        // Add the special note to the message
                        message.append("<i>*If you don't have a song in your list already you will need to import it</i><br /><br />");
                        instructions = StaticVars.howToAddSongToSet;
                        break;
                    case 2:     // Import a song
                        instructions = StaticVars.howToImportSong;
                        break;
                    case 3:     // Change the order of songs in a set
                        instructions = StaticVars.howToOrderSongs;
                        break;
                    case 4:     // Change the key a song uses in a set
                        instructions = StaticVars.howToChangeSetKey;
                        break;
                    case 5:     // Use the metronome
                        instructions = StaticVars.howToUseMetronome;
                        break;
                    case 6:
                        instructions = StaticVars.howToUsePageFlip;
                        break;
                }

                // Build the how to message string
                for (String i : instructions) {
                    // Add the step number
                    message.append(counter + ")  ");

                    // Add the instruction
                    //message.append("<i>" + i + "</i><br /><br />");
                    message.append(i + "<br /><br />");

                    // Increment the counter
                    counter++;
                }

                // Trim the last line breaks and close the small tag
                message.delete(message.length() - 12, message.length());
                message.append("</small>");

                // Add the instructions to the dialog
                instrAlert.setMessage(Html.fromHtml(message.toString()));

                // Add an OK button
                instrAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                // Add a back button
                instrAlert.setNeutralButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show the how to options again
                        showHowTos();
                    }
                });

                // Show the instructions
                instrAlert.show();
            }
        });

        // Show the dialog
        alert.show();
    }

    /**
     * Checks for and requests access to files
     */
    public void permissionRequiredFunction (int permissionRequestType) {
        // Only request runtime permissions on 23 or higher
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request permissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        permissionRequestType);

            } else {
                // We already have permissions, run the function
                executePermReqFunction(permissionRequestType);
            }/**/
        } else {
            // Below SDK 23, don't need runtime permissions
            executePermReqFunction(permissionRequestType);
        }
    }

    /**
     * Executes the specified function now that we have permissions
     * @param permissionRequestType The switch for which function to execute
     */
    private void executePermReqFunction (int permissionRequestType) {
        switch (permissionRequestType) {
            case StaticVars.PERMISSIONS_BACKUP_IMPORT:
                //selectImportFile(StaticVars.IMPORT_DB_ACTIVITY);
                importFile("/storage/emulated/0/Download/sbgvsb_05-05-21.bak", true, "This will erase all data currently in your database.  Do you want to continue?");
                break;
            case StaticVars.PERMISSIONS_BACKUP_EXPORT:
                selectExportFolder(StaticVars.EXPORT_DB_ACTIVITY);
                break;
            case StaticVars.PERMISSIONS_SONG_IMPORT:
                importSong();
                break;
            case StaticVars.PERMISSIONS_SET_EXPORT:
                selectExportFolder(StaticVars.EXPORT_SET_ACTIVITY);
                break;
            case StaticVars.PERMISSIONS_SET_IMPORT:
                selectImportFile(StaticVars.IMPORT_SET_ACTIVITY);
                break;
            default:
                break;
        }
    }

    /**
     * Shows the settings page
     */
    public void showSettingsPage() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

        // Set the dialog view to gather user input
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.settings, (ViewGroup) findViewById(R.id.settings_root));
        alert.setView(dialoglayout);

        // Get the current settings
        Settings settings = dbAdapter.getCurrentSettings();

        // Get the options views
        final CheckBox transposeOnCB = (CheckBox)dialoglayout.findViewById(R.id.settings_transpose_show);
        final CheckBox editOnCB = (CheckBox)dialoglayout.findViewById(R.id.settings_edit_show);
        final CheckBox autoplayOnCB = (CheckBox)dialoglayout.findViewById(R.id.settings_autoplay);
        final RadioGroup metronomeStateRG = (RadioGroup)dialoglayout.findViewById(R.id.settings_metronome_radio);
        final RadioGroup metronomeTypeRG = (RadioGroup)dialoglayout.findViewById(R.id.settings_metronome_type_radio);
        final Spinner themeColorSpin = (Spinner)dialoglayout.findViewById(R.id.settings_theme_color_spinner);
        final Spinner chordColorSpin = (Spinner)dialoglayout.findViewById(R.id.settings_chord_color_spinner);

        // Update the views with the current settings
        transposeOnCB.setChecked(settings.getShowTransposeInSet());
        editOnCB.setChecked(settings.getShowEditInSet());
        autoplayOnCB.setChecked(settings.getAutoplayTrack());
        if (settings.getMetronomeState().equals(StaticVars.SETTINGS_METRONOME_STATE_ON))
            metronomeStateRG.check(R.id.settings_metronome_on);
        else if (settings.getMetronomeState().equals(StaticVars.SETTINGS_METRONOME_STATE_OFF))
            metronomeStateRG.check(R.id.settings_metronome_off);
        if (settings.getUseBrightMetronomeInt() == StaticVars.SETTINGS_BRIGHT_METRONOME)
            metronomeTypeRG.check(R.id.settings_bright_metronome);

        // Update the theme color spinner
        if (settings.getSongBookTheme().getThemeName() != null && settings.getSongBookTheme().getThemeName() != "") {
            String[] themeColors = getResources().getStringArray(R.array.theme_colors);
            int loc = Arrays.asList(themeColors).indexOf(settings.getSongBookTheme().getThemeName());
            if (loc >= 0 && loc < themeColorSpin.getCount())
                themeColorSpin.setSelection(loc);
        } else {
            themeColorSpin.setSelection(0);
        }

        // Update the chord color spinner
        if (settings.getChordColor() != null && settings.getChordColor() != "") {
            String[] chordColors = getResources().getStringArray(R.array.chord_colors);
            int loc = Arrays.asList(chordColors).indexOf(settings.getChordColor());
            if (loc >= 0 && loc < chordColorSpin.getCount())
                chordColorSpin.setSelection(loc);
        } else {
            chordColorSpin.setSelection(0);
        }

        // Add the dialog title
        alert.setTitle("SongBook Settings");

        // Set the OK button
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get the settings strings
                String metronomeState;
                int metStId = metronomeStateRG.getCheckedRadioButtonId();
                if (metStId == R.id.settings_metronome_on)
                    metronomeState = StaticVars.SETTINGS_METRONOME_STATE_ON;
                else if (metStId == R.id.settings_metronome_off)
                    metronomeState = StaticVars.SETTINGS_METRONOME_STATE_OFF;
                else
                    metronomeState = StaticVars.SETTINGS_METRONOME_STATE_WITHBPM;

                boolean metronomeType = false;
                int metTpId = metronomeTypeRG.getCheckedRadioButtonId();
                if (metTpId == R.id.settings_bright_metronome)
                    metronomeType = true;

                // Create the settings object to save
                Settings settings = new Settings(metronomeState, transposeOnCB.isChecked(), editOnCB.isChecked(), metronomeType);
                settings.setAutoplayTrack(autoplayOnCB.isChecked());

                // Set the color options
                settings.setSongBookTheme(new SongBookTheme(String.valueOf(themeColorSpin.getSelectedItem())));
                settings.setChordColor(String.valueOf(chordColorSpin.getSelectedItem()));

                // Save the options to the database
                dbAdapter.setCurrentSettings(settings);

                // Load the current theme
                loadTheme();

                // Close the dialog
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", null);
        alert.setCanceledOnTouchOutside(true);

        alert.show();
    }

    /**
     * Loads the current theme from the database and displays it
     */
    public void loadTheme() {
        // Get the current theme from the database
        SongBookTheme theme = dbAdapter.getCurrentSettings().getSongBookTheme();

        // Apply the background color
        View layout = findViewById(R.id.drawer_layout);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {theme.getBackgroundTop(),theme.getBackgroundBottom()});
        gd.setCornerRadius(0f);
        layout.setBackground(gd);

        // Set titlebar background
        toolbar.setBackgroundColor(theme.getToolbarColor());
        tabLayout.setBackgroundColor(theme.getToolbarColor());

        // Apply the list font colors
        ((SetsTab)setsFragment).refillSetsList(true);
        ((CurrentSetTab)currSetFragment).refillCurrentSetList(true);
        ((SongsTab)songsFragment).refillSongsList(true);

        // Apply spinner color
        ((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0, true);
        ((SetsTab)setsFragment).fillSetSortSpinner();
        ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0, true);
        ((SongsTab)songsFragment).fillSongSortSpinner();

        // Update the set tab spinner labels
        SongBookThemeTextView setSortByLabel = ((SongBookThemeTextView)findViewById(R.id.set_sort_label));
        if (setSortByLabel != null)
            setSortByLabel.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());
        SongBookThemeTextView setGroupLabel = ((SongBookThemeTextView)findViewById(R.id.set_group_label));
        if (setGroupLabel != null)
            setGroupLabel.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());

        // Update the song tab spinner labels
        SongBookThemeTextView songSortByLabel = ((SongBookThemeTextView)findViewById(R.id.song_sort_label));
        if (songSortByLabel != null)
            songSortByLabel.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());
        SongBookThemeTextView songGroupLabel = ((SongBookThemeTextView)findViewById(R.id.song_group_label));
        if (songGroupLabel != null)
            songGroupLabel.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());

        // Apply title color for sets tab
        SongBookThemeTextView setsTitle = ((SongBookThemeTextView)findViewById(R.id.sets_tab_title));
        setsTitle.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());

        // Apply title color for current set tab
        SongBookThemeTextView currSetTitle = ((SongBookThemeTextView)findViewById(R.id.current_set_tab_title));
        currSetTitle.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());

        // Apply title color for songs tab
        SongBookThemeTextView songsTitle = ((SongBookThemeTextView)findViewById(R.id.songs_tab_title));
        songsTitle.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());

        // Apply title color for current set link
        SongBookThemeTextView currSetLinkTitle = ((SongBookThemeTextView)findViewById(R.id.current_set_tab_link));
        currSetLinkTitle.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());

        // Update the separator bar color
        ((SetsTab)setsFragment).reColorSeparatorBar();
        ((SongsTab)songsFragment).reColorSeparatorBar();
        ((CurrentSetTab)currSetFragment).reColorSeparatorBar();

        // Update the fast scroller color
        ((SetsTab)setsFragment).reColorFastScroll();
        ((SongsTab)songsFragment).reColorFastScroll();
        ((CurrentSetTab)currSetFragment).reColorFastScroll();

        // Update the navigation drawer list colors
        mNavDrawerAdapter.resetTheme();
        setMainNavDrawerItems();

        // Update the navigation drawer header colors
        SongBookThemeTextView headerTitle = (SongBookThemeTextView)mDrawerList.findViewById(R.id.nav_drawer_header_text);
        headerTitle.setCustomText(theme.getTitleFontColor(), true, theme.getTitleFontShadowColor());
    }
    //endregion


    //region Set Functions
    // *****************************************************************************
    // * 
    // * Set Functions
    // * 
    // *****************************************************************************
    /**
     * Prompts the user for a name and creates the set
     */
    private void createSet() {
    	// Create the alert dialog
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);
    	alert.setTitle("Create Set");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	final View dialoglayout = inflater.inflate(R.layout.add_set, (ViewGroup) findViewById(R.id.add_set_root));
    	alert.setView(dialoglayout);
    	final EditText setNameET = (EditText)dialoglayout.findViewById(R.id.add_set_name);
        final EditText setLinkET = (EditText)dialoglayout.findViewById(R.id.add_set_link);
    	final DatePicker setDateDP = (DatePicker)dialoglayout.findViewById(R.id.add_set_date);

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Get the date and set name
	    		String setName = setNameET.getText().toString();
	    		String setDate = setDateDP.getYear() + "-" + String.format("%02d", (setDateDP.getMonth() + 1)) + "-" + String.format("%02d", setDateDP.getDayOfMonth());
                String setLink = setLinkET.getText().toString();

                if (setLink == null)
                    setLink = "";
	    		
	    		if (setName.length() > 0) {
                    dialog.dismiss();
                    selectSetSongs(setName, setDate, setLink);
	    		}
	    		else {
                    Snackbar.make(dialoglayout, "Cannot create a set with no name!", Snackbar.LENGTH_LONG).show();
                    return;
                }
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	    // Canceled.
                dialog.dismiss();
	    	}
    	});

        alert.show();
    }
    
    /**
     * Selects the songs for the set
     */
    private void selectSetSongs(final String setName, final String setDate, final String setLink) {
    	Cursor c = dbAdapter.getSongs(SongsTab.ALL_SONGS_LABEL);
    	
    	// Clear the previous song lists
    	addSongsDialogList.clear();
    	addSongsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		addSongsDialogList.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)));
    		addSongsDialogMap.put(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)), false);
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());
    	
    	// Create the alert dialog and set the title
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Select Songs");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner songGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView songsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> songsAD;
    	
    	// Fill the list view
    	songsLV.setEmptyView(findViewById(R.id.empty_songs));
    	songsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	songsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String song = addSongsDialogList.get(position);
            	addSongsDialogMap.put(song, !addSongsDialogMap.get(song));
            }
        });
    	songsAD = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, addSongsDialogList);
    	songsLV.setAdapter(songsAD);

        // Fill the group spinner
        ArrayList<String> songGroups = ((SongsTab)songsFragment).getSongGroupsList(false);
        ArrayAdapter<String> songsGroupsAdapter = new SetGroupArrayAdapter(this, songGroups);
    	songGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)songGroupSP.getSelectedItem();

            	// Fill the new songs list
            	Cursor c = dbAdapter.getSongs(groupName);
            	addSongsDialogList.clear();

            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
                	addSongsDialogList.add(songName);
            	}
            	c.close();
            	Collections.sort(addSongsDialogList, new SortIgnoreCase());

            	// Update list view
            	songsAD.notifyDataSetChanged();

            	// Set the list view checked properties
            	for(int i = 0; i < songsLV.getCount(); i++) {
            		songsLV.setItemChecked(i, addSongsDialogMap.get(songsLV.getItemAtPosition(i)));
            	}
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	songGroupSP.setAdapter(songsGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Set all selected items to the songs for the set	    		
	    		ArrayList<String> setSongs = new ArrayList<String>();
	    		for(String s : addSongsDialogMap.keySet()) {
	    			if(addSongsDialogMap.get(s))
	    				setSongs.add(s);
	    		}
	    		
	    		// Create the set and refresh the list
	    		if(!dbAdapter.createSet(setName, setSongs, setDate + " ", setLink))
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Failed to create set!", Snackbar.LENGTH_LONG).show();
	    		else {
                    // Refresh set and current set list
					((SetsTab)setsFragment).refillSetsList();
                    ((CurrentSetTab)currSetFragment).refillCurrentSetList();
                }
	        	
	        	// Add the set to a group
	        	addSetToGroup(setName);
			}
    	});

    	// Set negative button of the dialog
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Do Nothing
	    	}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
    }
    
    /**
     * Adds the song to a group
     * @param setName The song to add
     */
    public void addSetToGroup(final String setName) {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSetGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	final boolean[] checkedGroupNames = new boolean[c.getCount() - 1];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
    		if (!groupName.equals(SetsTab.ALL_SETS_LABEL)) {
                checkedGroupNames[counter] = false;
                groupNames[counter++] = groupName;
            }
    	}
    	c.close();
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Set to Group");
    	
    	alert.setMultiChoiceItems(groupNames, checkedGroupNames, new DialogInterface.OnMultiChoiceClickListener() {

            public void onClick(DialogInterface dialog, int which, boolean checked) {
                checkedGroupNames[which] = checked;
            }
        });
    	
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the song to the selected groups
                for (int i = 0; i < groupNames.length; i++) {
                    if (!groupNames[i].equals("No Group") && checkedGroupNames[i])
                        dbAdapter.addSetToGroup(setName, groupNames[i].toString());
                }

                // Refresh sets list
				((SetsTab)setsFragment).refillSetsList();
				((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0, true);
            }
        });

    	alert.show();
    }
    
    /**
     * Removes the set from the specified group
     * @param setName The set to remove
     * @param groupName The group to remove the song from
     */
    public void removeSetFromGroup(final String setName, final String groupName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Remove Set From Group?!");
    	alert.setMessage("Are you sure you want to remove '" + setName + "' from '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Remove song from the group
                dbAdapter.removeSetFromGroup(setName, groupName);

                // Update set list view
				((SetsTab)setsFragment).refillSetsList();
            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do Nothing
            }
        });

    	alert.show();
    }
    
    /**
     * Updates the songs for the set
     */
    public void updateSetSongs(final String setName) {
    	Cursor c = dbAdapter.getSongs(SongsTab.ALL_SONGS_LABEL);
    	
    	// Clear the previous song lists
    	addSongsDialogList.clear();
    	addSongsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		String songName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
    		addSongsDialogList.add(songName);
    		addSongsDialogMap.put(songName, dbAdapter.isSongInSet(songName, setName));
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());

    	// Create the alert dialog and set the title
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Select Songs");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner songGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView songsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> songsAD;
    	
    	// Fill the list view
    	songsLV.setEmptyView(findViewById(R.id.empty_songs));
    	songsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	songsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String song = addSongsDialogList.get(position);
            	addSongsDialogMap.put(song, !addSongsDialogMap.get(song));
            }
        });
    	songsAD = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, addSongsDialogList);
    	songsLV.setAdapter(songsAD);
    	
    	// Fill the group spinner
        ArrayList<String> songGroups = ((SongsTab)songsFragment).getSongGroupsList(false);
        ArrayAdapter<String> songsGroupsAdapter = new SetGroupArrayAdapter(this, songGroups);
    	songGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)songGroupSP.getSelectedItem();
            	
            	// Fill the new songs list
            	Cursor c = dbAdapter.getSongs(groupName);
            	addSongsDialogList.clear();
            	
            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
                	addSongsDialogList.add(songName);
            	}
            	c.close();
            	Collections.sort(addSongsDialogList, new SortIgnoreCase());
            	
            	// Update list view
            	songsAD.notifyDataSetChanged();
            	
            	// Set the list view checked properties
            	for(int i = 0; i < songsLV.getCount(); i++) {
            		songsLV.setItemChecked(i, addSongsDialogMap.get(songsLV.getItemAtPosition(i)));
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	songGroupSP.setAdapter(songsGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Set all selected items to the songs for the set	    		
	    		ArrayList<String> setSongs = new ArrayList<String>();
	    		for(String s : addSongsDialogMap.keySet()) {
	    			if(addSongsDialogMap.get(s))
	    				setSongs.add(s);
	    		}
	    		
	    		// Create the set and refresh the list
	    		if(!dbAdapter.updateSet(setName, setSongs.toArray(new String[setSongs.size()])))
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Failed to update set!", Snackbar.LENGTH_LONG).show();
	    		else {
	    			// Update current set list
                    ((CurrentSetTab)currSetFragment).refillCurrentSetList();
	    		}
			}
    	});

    	// Set negative button of the dialog
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Do Nothing
	    	}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
    }
    
    /**
     * Prompts the user to confirm then deletes all sets
     */
    private void deleteAllSets() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All?!");
    	alert.setMessage("Are you sure you want to delete ALL sets???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		dbAdapter.deleteAllSets();
	    		
	    		// Refresh song, set and current set lists
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
				((SetsTab)setsFragment).refillSetsList();
				((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0);
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Do Nothing
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Prompts the user to confirm then deletes the specified set
     */
    public void deleteSet(final String setName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete Set?!");
    	alert.setMessage("Are you sure you want to delete '" + setName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Delete set from database
                dbAdapter.deleteSet(setName);

                // Refresh set and current set list
				((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0, true);
				((SetsTab)setsFragment).refillSetsList();
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do Nothing
            }
        });

    	alert.show();
    }
    
    /**
     * Edits the set name and date
     * @param setName The set to edit
     */
    public void editSetAtt(final String setName) {
    	// Create the alert dialog
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);
    	alert.setTitle("Edit Set");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	final View dialoglayout = inflater.inflate(R.layout.add_set, (ViewGroup) findViewById(R.id.add_set_root));
    	alert.setView(dialoglayout);
    	final EditText setNameET = (EditText)dialoglayout.findViewById(R.id.add_set_name);
        final EditText setLinkET = (EditText)dialoglayout.findViewById(R.id.add_set_link);
    	final DatePicker setDateDP = (DatePicker)dialoglayout.findViewById(R.id.add_set_date);
    	
    	// Populate the set fields
    	setNameET.setText(setName);
        setLinkET.setText(dbAdapter.getSetLink(setName));
    	String temp[] = dbAdapter.getSetDate(setName).split("-");
    	if (temp.length >= 2)
    		setDateDP.updateDate(Integer.parseInt(temp[0].trim()), Integer.parseInt(temp[1].trim()) - 1, Integer.parseInt(temp[2].trim()));

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get the date and set name
                String newSetName = setNameET.getText().toString();
                String setDate = setDateDP.getYear() + "-" + String.format("%02d", (setDateDP.getMonth() + 1)) + "-" + String.format("%02d", setDateDP.getDayOfMonth());
                String setLink = setLinkET.getText().toString();

                if (setLink == null)
                    setLink = "";

                if (newSetName.length() > 0) {
                    dbAdapter.updateSetAttributes(setName, newSetName, setDate, setLink);

                    // Refresh set and current set list
					((SetsTab)setsFragment).refillSetsList();
                    ((CurrentSetTab)currSetFragment).refillCurrentSetList();

                    dialog.dismiss();
                } else
                    Snackbar.make(dialoglayout, "Cannot create a set with no name!", Snackbar.LENGTH_LONG).show();
            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.dismiss();
            }
        });

    	alert.show();
    }
    
    /**
     * Emails the set
     * @param setItem The set to email
     * @param songFileType The type of sharing, (txt, pro, pdf)
     */
    private void emailSet(SetItem setItem, StaticVars.SongFileType songFileType) {
		String setDate = setItem.getDate();
        String setLink = setItem.getLink();
		ArrayList<Uri> uris = new ArrayList<Uri>();
		
		// Start the output string
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>" + setItem.getName() + "</h2>");

        if (setItem.getLink() != null && !setItem.getLink().isEmpty())
            sb.append("<a href='" + setLink + "'>" + setLink + "</a><br/>");

		sb.append("<i>" + setDate + "</i><br/><br/>");
		
		for (SongItem songItem : setItem.songs) {
			File att;
			
			// Add the attachment
			switch (songFileType) {
				case plainText:		
				case chordPro:
					// Create the attachment file
					att = saveSong(songItem, songFileType, songItem.getSetKey(), false);
					att.deleteOnExit();
					
					// Add the file as an attachment
					uris.add(Uri.fromFile(att));	
					
					break;				
				case PDF:
					// Save the song as a PDF
					att = saveSongAsPdf(songItem, songItem.getSetKey(), false);
					att.deleteOnExit();
					
					// Add the file as an attachment
					uris.add(Uri.fromFile(att));	
				default:
					break;
			}

            // Add song and key
			if (songItem.getSetKey() == "") 
				sb.append("<b>" + songItem.getName() + "</b> - " + songItem.getKey() + "<br/>");
			else
				sb.append("<b>" + songItem.getName() + "</b> - " + songItem.getSetKey() + "<br/>");

            // Add song link if it exists
            if (songItem.getSongLink() != null && !songItem.getSongLink().isEmpty()) {
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                sb.append("<a href=\"" + songItem.getSongLink() + "\">");
                sb.append(songItem.getSongLink());
                sb.append("</a><br/>");
            }

            // Add bpm and time signature if it exists
            if (songItem.getBpm() > 0) {
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                sb.append(songItem.getBpm() + " BPM <i>in</i> " + songItem.getTimeSignature() + "<br/>");
            }

            // Add a line break between songs
            sb.append("<br/>");
		}
		
		// Create the email intent
		Intent i = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
		i.setType("text/html");
		
		// Add the subject, body and attachments
		i.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris);
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, "SBGSoft Virtual SongBook - " + setItem.getName());
		i.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(sb.toString()));
		
		startActivity(Intent.createChooser(i, "Send song email via:"));  
    }
    
    /**
     * Saves the set
     * @param setItem The set to save
     * @param songFileType The type of sharing, (txt, pro, pdf)
     */
    private void saveSet(final SetItem setItem, final StaticVars.SongFileType songFileType) {
    	// For each song in the set
    	for (SongItem songItem : setItem.songs) {
			saveSong(songItem, songFileType, songItem.getSetKey(), false);
    	}

        Snackbar.make(getWindow().getDecorView().getRootView(), "Saved set files to: " + Environment.getExternalStorageDirectory() + "!", Snackbar.LENGTH_LONG).show();
    }
     
    /**
     * Emails the set with the songs as attachments
     * @param setItem The set item object
     */
    public void shareSet(String setName) {
		// Get the set item
		final SetItem setItem = ((SetsTab)setsFragment).getSetItem(setName);

    	// Create the options array
    	final CharSequence[] options;
    	
    	if (Build.VERSION.SDK_INT >= 19) {
	    	options = new CharSequence[] {getString(R.string.cmenu_sets_share_email), 
	    			getString(R.string.cmenu_sets_share_email_cp),
	    			getString(R.string.cmenu_sets_share_email_pdf),
	    			getString(R.string.cmenu_sets_share_save), 
	    			getString(R.string.cmenu_sets_share_save_cp),
	    			getString(R.string.cmenu_sets_share_save_pdf),
                    getString(R.string.cmenu_sets_share_export)};
    	} else {
    		options = new CharSequence[] {getString(R.string.cmenu_sets_share_email), 
	    			getString(R.string.cmenu_sets_share_email_cp),
	    			getString(R.string.cmenu_sets_share_save), 
	    			getString(R.string.cmenu_sets_share_save_cp),
                    getString(R.string.cmenu_sets_share_export)};
    	}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Share How?");
    	alert.setItems(options, new OnClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem) {	    		
	    		// Email, plain text
	    		if (options[whichItem] == getString(R.string.cmenu_sets_share_email)) {
	    			emailSet(setItem, SongFileType.plainText);
	    		}
	    		// Email, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_email_cp)) {
	    			emailSet(setItem, SongFileType.chordPro);
	    		}
	    		// Email, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_email_pdf)) {
	    			emailSet(setItem, SongFileType.PDF);
	    		}
	    		// Save, plain text
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_save)) {
	    			saveSet(setItem, SongFileType.plainText);
	    		}
	    		// Save, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_save_cp)) {
	    			saveSet(setItem, SongFileType.chordPro);
	    		}
	    		// Save, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_sets_share_save_pdf)) {
	    			saveSet(setItem, SongFileType.PDF);
	    		}
                // Export
                else if (options[whichItem] == getString(R.string.cmenu_sets_share_export)) {
                    setToExport = setItem;
                    permissionRequiredFunction(StaticVars.PERMISSIONS_SET_EXPORT);
                }
    		}
    	});
    	
    	alert.show();
    }

    /**
     * Enables the user to find sets
     */
    private void findSetDialog() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

        // Set the dialog view to gather user input
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.search_dialog, (ViewGroup) findViewById(R.id.search_dialog_root));
        alert.setView(dialoglayout);
        final EditText setNameSearch = (EditText)dialoglayout.findViewById(R.id.search_dialog_text);

        // Add the dialog title
        alert.setTitle("Find Set");

        // Set the OK button
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Make sure there is some text to search
                String searchText = setNameSearch.getText().toString();

                if (searchText.isEmpty()) {
                    Snackbar.make(dialoglayout, "You must enter text to search. Please try again.", Snackbar.LENGTH_LONG).show();
                } else {
                    // Create the song search object
                    SetSearchCriteria setSearch = new SetSearchCriteria();
                    setSearch.setNameSearchText = searchText;

                    // Fill the songs tab with the search data
                    int numResults = ((SetsTab)setsFragment).refillSetsList(setSearch);
                    ((SetsTab)setsFragment).fillSetGroupsSpinner(true, numResults);

                    // Close the dialog
                    dialog.dismiss();
                }
            }
        });

        alert.setNegativeButton("Cancel", null);
        alert.setCanceledOnTouchOutside(true);

        alert.show();
    }
    //endregion


    //region Song Functions
    // *****************************************************************************
    // * 
    // * Song Functions
    // * 
    // *****************************************************************************
    /**
     * Prompts the user for a name and creates the set
     */
    public void createSong() {
    	CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);
    	String[] pathSplit = importFilePath.split("/");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	final View dialoglayout = inflater.inflate(R.layout.add_song, (ViewGroup) findViewById(R.id.add_song_root));
    	alert.setView(dialoglayout);
    	final EditText songNameET = (EditText)dialoglayout.findViewById(R.id.add_song_name);
    	final EditText authorET = (EditText)dialoglayout.findViewById(R.id.add_song_author);
    	final EditText keyET = (EditText)dialoglayout.findViewById(R.id.add_song_key);
        final EditText linkET = (EditText)dialoglayout.findViewById(R.id.add_song_link);
        final EditText bpmET = (EditText)dialoglayout.findViewById(R.id.add_song_bpm);
        final Spinner timeSpin = (Spinner)dialoglayout.findViewById(R.id.add_song_time);
        timeSpin.setSelection(3);
    	
    	// Add the dialog title
    	if (importFilePath != "") {
    		alert.setTitle("Add Song - " + pathSplit[pathSplit.length - 1]);
    		
    		// Populate the song name with the file name
        	songNameET.setText(pathSplit[pathSplit.length - 1].substring(0, pathSplit[pathSplit.length - 1].lastIndexOf(".")));
    	}
    	else
    		alert.setTitle("Add Song");

    	// Set the OK button
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get the user inputs
                String songName = songNameET.getText().toString();
                String songAuthor = StaticVars.UNKNOWN;
                String songKey = "";
                String songTime = String.valueOf(timeSpin.getSelectedItem());
                String songLink = linkET.getText().toString();

                if (authorET.getText().length() > 0)
                    songAuthor = authorET.getText().toString().trim();
                if (keyET.getText().length() > 1)
                    songKey = keyET.getText().toString().substring(0, 1).toUpperCase(Locale.US) + keyET.getText().toString().substring(1).trim();
                else if (keyET.getText().length() > 0)
                    songKey = keyET.getText().toString().toUpperCase(Locale.US).trim();

                // Check for bpm populated
                int bpm = 0;
                try {
                    bpm = Integer.parseInt(bpmET.getText().toString());
                } catch (NumberFormatException nfe) {
                }

                // Check for a correct key
                if (!isValidKey(songKey)) {
                    Snackbar.make(dialoglayout, "That is not a valid key!" +
                            StaticVars.EOL + "Please enter a valid key and try again.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // Create the song
                if (songName.length() > 0) {
                    String songFile = songName + ".txt";
                    if (!dbAdapter.createSong(songName, songFile, songAuthor, songKey, songTime, songLink, bpm)) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Failed to create song!", Snackbar.LENGTH_LONG).show();
                    }
                    else {
                        // If a file is waiting to be imported
                        if (importFilePath != "") {
                            // Copy the file into the tabapp songs directory
                            try {
                                if (importFilePath.substring(importFilePath.length() - 3).equals("txt")) {
                                    TextFileImporter.importTextFile(importFilePath, songFile, songAuthor, getApplicationContext());
                                } else {
                                    InputStream in = new FileInputStream(importFilePath);
                                    //OutputStream out = new FileOutputStream(songFile);
                                    OutputStream out = openFileOutput(songFile, Context.MODE_PRIVATE);
                                    byte[] buf = new byte[1024];
                                    int len;
                                    while ((len = in.read(buf)) > 0) {
                                        out.write(buf, 0, len);
                                    }
                                    in.close();
                                    out.close();
                                }
                            } catch (Exception e) {
                                // Delete the song since the file could not be imported
                                dbAdapter.deleteSong(songName);

                                // Alert that the song failed
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Could not import file, Song deleted.", Snackbar.LENGTH_LONG).show();
                            }

                            // Clear the import file path
                            importFilePath = "";
                        } else {
                            try {
                                OutputStream out = openFileOutput(songFile, Context.MODE_PRIVATE);
                                out.close();
                            } catch (IOException e) {
                                // Delete the song since the file could not be imported
                                dbAdapter.deleteSong(songName);

                                // Alert that the song failed
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Could not create song file, Song deleted.", Snackbar.LENGTH_LONG).show();
                            }

                        }

                        // Add the song to a group
                        addSongToGroup(songName);
                    }
                } else {
                    Snackbar.make(dialoglayout, "Cannot create a song with no name!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // Close the dialog
                dialog.dismiss();
            }
        });

    	alert.setNegativeButton("Cancel", null);
    	alert.setCanceledOnTouchOutside(true);

    	alert.show();
    }
    
    /**
     * Adds the song to a group
     * @param songName The song to add
     */
    public void addSongToGroup(final String songName) {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSongGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	final boolean[] checkedGroupNames = new boolean[c.getCount() - 1];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
    		if (!groupName.equals(SongsTab.ALL_SONGS_LABEL)) {
                checkedGroupNames[counter] = false;
                groupNames[counter++] = groupName;
            }
    	}
    	
    	// Close the cursor
    	c.close();
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Add Song to Group");
    	alert.setMultiChoiceItems(groupNames, checkedGroupNames, new DialogInterface.OnMultiChoiceClickListener() {

            public void onClick(DialogInterface dialog, int which, boolean checked) {
                checkedGroupNames[which] = checked;
            }
        });
    	
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the song to the selected groups
                for (int i = 0; i < groupNames.length; i++) {
                    if (!groupNames[i].equals("No Group") && checkedGroupNames[i])
                        dbAdapter.addSongToGroup(songName, groupNames[i].toString());
                }

                // Refresh song group list
                ((SongsTab)songsFragment).refillSongsList();
                ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0, true);
            }
        });

    	alert.show();
    }
    
    /**
     * Adds the song to a set
     * @param songName The song to add
     */
    public void addSongToSet(final String songName) {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSets(SetsTab.ALL_SETS_LABEL);
    	
    	// Clear the previous song lists
    	addSetsDialogList.clear();
    	addSetsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		String setName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
    		addSetsDialogList.add(setName);
    		addSetsDialogMap.put(setName, dbAdapter.isSongInSet(songName, setName));
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());
    	
    	// Create the dialog to choose which group to add the song to
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Add Song to Which Set?");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner setGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView setsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> setsAD;
    	
    	// Fill the list view
    	setsLV.setEmptyView(findViewById(R.id.empty_songs));
    	setsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	setsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String set = addSetsDialogList.get(position);
            	addSetsDialogMap.put(set, !addSetsDialogMap.get(set));
            }
        });
    	setsAD = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, addSetsDialogList);
    	setsLV.setAdapter(setsAD);
    	
    	// Fill the group spinner
        ArrayList<String> setGroups = ((SetsTab)setsFragment).getSetGroupsList(false);
        ArrayAdapter<String> setGroupsAdapter;
        setGroupsAdapter = new SetGroupArrayAdapter(this, setGroups);
    	setGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)setGroupSP.getSelectedItem();
            	
            	// Fill the new songs list
            	Cursor c = dbAdapter.getSets(groupName);
            	addSetsDialogList.clear();
            	
            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String setName = c.getString(c.getColumnIndex(DBStrings.TBLSETS_NAME));
                	addSetsDialogList.add(setName);
            	}
            	c.close();
            	Collections.sort(addSetsDialogList, new SortIgnoreCase());
            	
            	// Update list view
            	setsAD.notifyDataSetChanged();
            	
            	// Set the list view checked properties
            	for(int i = 0; i < setsLV.getCount(); i++) {
            		setsLV.setItemChecked(i, addSetsDialogMap.get(setsLV.getItemAtPosition(i)));
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	setGroupSP.setAdapter(setGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		
				// Set all selected items to the set	    	
	    		for(String s : addSetsDialogMap.keySet()) {
	    			if(addSetsDialogMap.get(s))
	    				dbAdapter.addSongToSet(s, songName);
	    		}
	    		
	    		// Refresh the set lists
                ((SetsTab)setsFragment).refillSetsList();
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
			}
    	});

    	// Set negative button of the dialog
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {	}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
    }
    
    /**
     * Adds the song to a set
     * @param songName The song to add
     */
    public void addSongToCurrentSet(final String songName) {
    	// Get the current set
    	final String currentSet = dbAdapter.getCurrentSetName();
   
		AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		alert.setTitle("Add Song to Set?");
		alert.setMessage("Do you want to add '" + songName + "' to '" + currentSet + "'?");
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		dbAdapter.addSongToSet(currentSet, songName);
				
	    		// Refresh the current set and set list
                ((SetsTab)setsFragment).refillSetsList();
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
			}
    	});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) { 	}
    	});

		alert.show();
    }
    
    /**
     * Removes the song from the specified group
     * @param songName The song to remove
     * @param groupName The group to remove the song from
     */
    public void removeSongFromGroup(final String songName, final String groupName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Remove Song From Group?!");
    	alert.setMessage("Are you sure you want to remove '" + songName + "' from '" + groupName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Remove song from the group
	    		dbAdapter.removeSongFromGroup(songName, groupName);
	    		
	    		// Refresh the song lists
                ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0, true);
                ((SongsTab)songsFragment).refillSongsList();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Do Nothing
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Prompts the user to confirm then deletes all songs
     */
    private void deleteAllSongs() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All?!");
    	alert.setMessage("Are you sure you want to delete ALL songs???" + StaticVars.EOL +
    			"This will delete all sets as well...");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		if(dbAdapter.deleteAllSongs()) {
	    			// Get a list of all internal files
	    			String[] files = fileList();
	    			
	    			// Delete each file
	    			for (String file : files) {
	    				deleteFile(file);
	    			}
	    		}
	    		
	    		// Refresh song, set and current set lists
                ((SongsTab)songsFragment).refillSongsList();
                ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0);
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
                ((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0);
                ((SetsTab)setsFragment).refillSetsList();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Do Nothing
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Prompts the user to confirm then deletes the specified song
     */
    public void deleteSong(final String songName) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete Song?!");
    	alert.setMessage("Are you sure you want to delete '" + songName + "'???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get song file
                String fileToDelete = dbAdapter.getSongFile(songName);
                if (fileToDelete != "") {
                    // Delete song file
                    deleteFile(fileToDelete);

                    // Delete song from database
                    dbAdapter.deleteSong(songName);
                }

                // Refresh the song and current set view
                ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0, true);
                ((SongsTab)songsFragment).refillSongsList();
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do Nothing
            }
        });

    	alert.show();
    }
        
    /**
     * Imports a song text file into the db
     */
    private void importSong() {
        // Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        intent.putExtra(StaticVars.FILE_ACTIVITY_KEY, StaticVars.IMPORT_SONG_ACTIVITY);
        intent.putExtra(StaticVars.FILE_ACTIVITY_TYPE_KEY, StaticVars.FILE_ACTIVITY_FILE);
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    /**
     * Edits the song name, author and key
     * @param songName The song to edit
     */
    public void editSongAtt(final String songName) {
        // Create the edit activity intent
        Intent i = new Intent(this, EditSongDetailsActivity.class);
        i.putExtra(StaticVars.SONG_NAME_KEY, songName);

        // Start the activity
        startActivityForResult(i, StaticVars.EDIT_SONG_ATT);
    }

    /**
     * Emails the song
     * @param songName The song to email
     */
    private void emailSong(SongItem songItem, StaticVars.SongFileType songFileType, String newSongKey) {
		// Create the email intent
    	Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/Message");
		
		File att;
		
		// Add the attachment
		switch (songFileType) {
			case plainText:		
			case chordPro:
				// Create the attachment file
				att = saveSong(songItem, songFileType, newSongKey, false);
				att.deleteOnExit();
				
				// Add the file as an attachment
				i.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(att));	
				
				break;				
			case PDF:
				// Save the song as a PDF
				att = saveSongAsPdf(songItem, newSongKey, false);
				att.deleteOnExit();
				
				// Add the file as an attachment
				i.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(att));
			default:
				break;
		}
		
		// Set the songkey for the email
		String tmpkey;
		if (newSongKey == "")
			tmpkey = songItem.getKey();
		else
			tmpkey = newSongKey;

        // Build the email string
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>SBGSoft Virtual SongBook</h2>");
        sb.append("<b>Song Name:</b>&nbsp;&nbsp;" + songItem.getName() + "<br/>");
        sb.append("<b>Song Key:</b>&nbsp;&nbsp;" + tmpkey + "<br/>");

        if ((songItem.getSongLink() != null && !songItem.getSongLink().isEmpty() || songItem.getBpm() > 0))
            sb.append("<b>Song Details:</b><br/>");

        if (songItem.getSongLink() != null && !songItem.getSongLink().isEmpty()) {
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append("<a href=\"" + songItem.getSongLink() + "\">");
            sb.append(songItem.getSongLink());
            sb.append("</a><br/>");
        }

        if (songItem.getBpm() > 0) {
            sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append(songItem.getBpm() + " BPM <i>in</i> " + songItem.getTimeSignature() + "<br/>");
        }

        sb.append("<br/>");
        sb.append("The music for this song has been attached to this email as a file.");
        sb.append("<br/>");
		
		// Add the subject and body
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, "SBGSoft Virtual SongBook - " + songItem.getName());
		//i.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<h2>" + songName + "</h2>" + getSongText(songI.getSongFile())));
		i.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(sb.toString()));

		startActivity(Intent.createChooser(i, "Send Song Email Via:"));
    }
    
    /**
     * Saves the song
     * @param songName The song to save
     */
    private File saveSong(final SongItem songItem, final StaticVars.SongFileType songFileType, String newSongKey, boolean showMessage) {
		// Craft the file name
		String fileName = songItem.getName() + " - " + songItem.getAuthor();
		if (newSongKey == "" || songFileType == SongFileType.chordPro)
			fileName += " (" + songItem.getKey() + ")";
		else
			fileName += " (" + newSongKey + ")";
		
		File att = null;
		
		// Save the file
		switch (songFileType) {
			case plainText:
				// Add the file extension
				fileName += ".txt";
				
				try {
					// Open the file and translate it
    				FileInputStream fis = openFileInput(songItem.getFile());
    				String temp = ChordProParser.ParseSongFile(getApplicationContext(), songItem, newSongKey, fis, false, true);
    				
    				// Write the file
    				att = new File(Environment.getExternalStorageDirectory(), fileName);
    				FileOutputStream out = new FileOutputStream(att);
    		    	out.write(temp.getBytes());
    				
    		    	// Close the files
    		    	fis.close();
    		    	out.close();
    				
    			} catch (Exception e) {
    				if (showMessage)
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Unable to save text file!", Snackbar.LENGTH_LONG).show();
    			}
				
				if (showMessage)
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Text file saved to: " + Environment.getExternalStorageDirectory() + "/" + fileName + "!", Snackbar.LENGTH_LONG).show();
				
				break;
			case chordPro:
				// Add the file extension
				fileName += ".pro";
				
				try {	        					
					// Open the input file
    				FileInputStream fis = openFileInput(songItem.getFile());
    				
    				// Open the output file
    				att = new File(Environment.getExternalStorageDirectory(), fileName);
    				FileOutputStream out = new FileOutputStream(att);
    		    	
    				// Copy the file
    				byte[] buffer = new byte[1024];
    				int read;
    				while ((read = fis.read(buffer)) != -1) {
    					out.write(buffer, 0, read);
    				}
    				
    				// Close the files
    		    	fis.close();
    		    	out.close();
    				
    			} catch (Exception e) {
    				if (showMessage)
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Unable to save ChordPro file!", Snackbar.LENGTH_LONG).show();
    			}
				
				if (showMessage)
                    Snackbar.make(getWindow().getDecorView().getRootView(), "ChordPro file saved to: " + Environment.getExternalStorageDirectory() + "/" + fileName + "!", Snackbar.LENGTH_LONG).show();
				
				break;
			case PDF:
				// Add the file extension
				fileName += ".pdf";
				
				// Save the songs as a PDF
				att = saveSongAsPdf(songItem, newSongKey, showMessage);
				
			default:
				break;
		}
		
		return att;
    }
        
    /**
     * Shares the song via email or saving it
     * @param songItem The SongItem
     * @param songName The song name
     */
    public void shareSong(String songName) {
        final SongItem songItem;
        songItem = dbAdapter.getSong(songName);

    	// Create the options array
    	final CharSequence[] options;
    	
    	if (Build.VERSION.SDK_INT >= 19) {
	    	options = new CharSequence[] {getString(R.string.cmenu_songs_share_email), 
	    			getString(R.string.cmenu_songs_share_email_cp),
	    			getString(R.string.cmenu_songs_share_email_pdf),
	    			getString(R.string.cmenu_songs_share_save), 
	    			getString(R.string.cmenu_songs_share_save_cp),
	    			getString(R.string.cmenu_songs_share_save_pdf)};
    	} else {
    		options = new CharSequence[] {getString(R.string.cmenu_songs_share_email), 
	    			getString(R.string.cmenu_songs_share_email_cp),
	    			getString(R.string.cmenu_songs_share_save), 
	    			getString(R.string.cmenu_songs_share_save_cp)};
    	}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Share How?");
    	alert.setItems(options, new OnClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem) {
    			// Dismiss the current dialog
    			dialog.dismiss();
    			
    			// Create the key array
	    		CharSequence[] keys = StaticVars.songKeys.toArray(new CharSequence[StaticVars.songKeys.size() + 1]);
	    		keys[StaticVars.songKeys.size()] = "Original Key";
	    	
	    		AlertDialog.Builder keysAlert;
	    		
	    		// Email, plain text
	    		if (options[whichItem] == getString(R.string.cmenu_songs_share_email)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    		    	
    	    		keysAlert = new AlertDialog.Builder(MainActivity.this);

    	    		keysAlert.setTitle("Email Song in Which Key?");
    	    		keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		emailSong(songItem, StaticVars.SongFileType.plainText, newSongKey);
    	        		}
    	        	});
    	        	
    	    		keysAlert.show();
	    		}
	    		// Email, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_email_cp)) {
	    			emailSong(songItem, StaticVars.SongFileType.chordPro, "");
	    		}
	    		// Email, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_email_pdf)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    		    	
    	    		keysAlert = new AlertDialog.Builder(MainActivity.this);

    	    		keysAlert.setTitle("Email Song in Which Key?");
    	    		keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		emailSong(songItem, StaticVars.SongFileType.PDF, newSongKey);
    	        		}
    	        	});
    	        	
    	    		keysAlert.show();
	    		}
	    		// Save, plain text
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_save)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    	    		
    		    	keysAlert = new AlertDialog.Builder(MainActivity.this);

    		    	keysAlert.setTitle("Save Song in Which Key?");
    		    	keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		saveSong(songItem, StaticVars.SongFileType.plainText, newSongKey, true);
    	        		}
    	        	});
    	        	
    		    	keysAlert.show();
	    		}
	    		// Save, chordpro
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_save_cp)) {
	    			saveSong(songItem, StaticVars.SongFileType.chordPro, "", true);
	    		}
	    		// Save, PDF
	    		else if (options[whichItem] == getString(R.string.cmenu_songs_share_save_pdf)) {
	    			// Check for a special key
    		    	if (StaticVars.songKeyMap.containsKey(songItem.getKey())) {
    		    		// Set the song key to the associated key
    		    		songItem.setKey(StaticVars.songKeyMap.get(songItem.getKey()));
    		    	}
    	    		
    		    	keysAlert = new AlertDialog.Builder(MainActivity.this);

    		    	keysAlert.setTitle("Save Song in Which Key?");
    		    	keysAlert.setItems(keys, new OnClickListener() {
    	        		public void onClick (DialogInterface dialog, int whichItem) {
    	        			// Set the new song key
    	        			String newSongKey = "";
    	        			if (whichItem < StaticVars.songKeys.size()) {
    	        				newSongKey = StaticVars.songKeys.get(whichItem);
    	        			}
    	        			
    	        			// Check to make sure the song has a proper key
    	        	    	if (StaticVars.songKeys.contains(songItem.getKey()))
    	        	    		saveSong(songItem, StaticVars.SongFileType.PDF, newSongKey, true);
    	        		}
    	        	});
    	        	
    		    	keysAlert.show();
	    		}
    		}
    	});
    	
    	alert.show();
    }
    
    /**
     * Saves the song as a PDF file
     * @param songItem The song to save
     * @return The created file
     */
    @TargetApi(19)
    public File saveSongAsPdf(SongItem songItem, String songKey, boolean showMessage) {
    	int pageWidth = 450;
    	int pageHeight = 700;
    	int padding = 30;
    	final float densityMultiplier = getResources().getDisplayMetrics().density;
    	float defaultTextSize = 6.0f;
    	File att = null;
    	
    	// Craft the file name
		String fileName = songItem.getName() + " - " + songItem.getAuthor();
		if (songKey == "")
			fileName += " (" + songItem.getKey() + ").pdf";
		else
			fileName += " (" + songKey + ").pdf";
    	
    	// Create a new PDF document
    	PdfDocument document = new PdfDocument();
    	
    	try {
	    	// Create a page description
	    	PageInfo pageInfo = new PageInfo.Builder(pageWidth, pageHeight, 1).create();
	    	
	    	// Create a new page from the page info
	    	Page page = document.startPage(pageInfo);
	    	
	    	// Create the text view to add to the page
	    	AutoFitTextView tv = new AutoFitTextView(MainActivity.this);
	    	tv.setTypeface(Typeface.MONOSPACE);
	    	tv.setTextColor(Color.BLACK);
	    	tv.setPadding(padding, padding, padding, padding);
	    	tv.layout(0, 0, pageWidth, pageHeight);	 
	    	tv.setTextSize(defaultTextSize);
	    	tv.setTextDecrement(0.25f);
	    	tv.setMinimumTextSizePixels(2.0f * densityMultiplier);
	    	
	    	// Get the fitted text size
	    	FileInputStream fis = openFileInput(dbAdapter.getSongFile(songItem.getName()));
	    	String songText = ChordProParser.ParseSongFile(getApplicationContext(), songItem, songKey, fis, true, false);
	    	
	    	// Add the song text to the text view
            ChordDisplay disp = new ChordDisplay(this);
            tv.setText(disp.setChordClickableText(songText), TextView.BufferType.SPANNABLE);
	    	
	    	// Force the text to shrink
	    	tv.shrinkToFit();
	    	
	    	// Add the song to the page
	    	tv.draw(page.getCanvas());
	    	
	    	// Finish the page
	    	document.finishPage(page);
    	
	    	// Write the document
	    	att = new File(Environment.getExternalStorageDirectory(), fileName);
			FileOutputStream out = new FileOutputStream(att);
	    	document.writeTo(out);
    	} catch (Exception e) {
    		if (showMessage)
                Snackbar.make(getWindow().getDecorView().getRootView(),
                        "Failed to save \"" + songItem.getName() + "\" to \"" + Environment.getExternalStorageDirectory() + "/" + fileName,
                        Snackbar.LENGTH_LONG).show();
    	}
    	
    	// Close the document
    	document.close();
    	
    	// Alert on success
    	if (showMessage)
            Snackbar.make(getWindow().getDecorView().getRootView(),
                    "Saved \"" + songItem.getName() + "\" to \"" + Environment.getExternalStorageDirectory() + "/" + fileName,
                    Snackbar.LENGTH_LONG).show();
    	
    	return att;
    }
    
    /**
     * Sets the song key for the set
     */
    public void setSongKeyForSet(final String setName, String songName) {
        // Get the song item
        final SongItem songItem = dbAdapter.getSong(songName);

    	// Create the key array
		CharSequence[] keys = StaticVars.songKeys.toArray(new CharSequence[StaticVars.songKeys.size() + 2]);
		keys[StaticVars.songKeys.size()] = "Original Key";
        keys[StaticVars.songKeys.size() + 1] = "Baritone";
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Which Key?");
    	alert.setItems(keys, new OnClickListener() {
    		public void onClick (DialogInterface dialog, int whichItem) {
    			// Set the new song key for the set
    			if (whichItem < StaticVars.songKeys.size()) {   // Named key
                    dbAdapter.setSongKeyForSet(setName, songItem.getName(), StaticVars.songKeys.get(whichItem));
                } else if (whichItem == StaticVars.songKeys.size()) {   // Original key
                    dbAdapter.setSongKeyForSet(setName, songItem.getName(), songItem.getKey());
                } else if (whichItem == StaticVars.songKeys.size() + 1) {   // Baritone key
                    String newKey = Transpose.getBaritoneKey(songItem.getKey());
                    dbAdapter.setSongKeyForSet(setName, songItem.getName(), newKey);
                }

    			
    			// Refresh current set list
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();

                // Refresh sets list
                ((SetsTab)setsFragment).refillSetsList();
    		}
    	});
    	
    	alert.show();
    }
    
    /**
     * Shows the song statistics dialog
     * @param songName The song to give stats for
     */
    public void showSongStats(String songName) {
    	// Create the dialog
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("'" + songName + "' Statistics");
    	
    	// Build the message
    	StringBuilder message = new StringBuilder();
    	
    	// Show last 5 uses
    	message.append("Last 5 Uses: ");
    	message.append(StaticVars.EOL);
    	
    	Cursor c = dbAdapter.getSongLastFive(songName);
    	c.moveToFirst(); 
    	while(!c.isAfterLast()) {
    		// Get the set name and date
    		String setName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_NAME));
    		String setDate = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETS_DATE));
    		String[] datesplit = setDate.split("-");
    		setDate = datesplit[1] + "/" + datesplit[2] + "/" + datesplit[0];
    		
    		message.append("\t" + setName + ", " + setDate);
        	message.append(StaticVars.EOL);
        	c.moveToNext();
    	}
    	c.close();
    	message.append(StaticVars.EOL);
    	
    	// Show total usage
    	message.append("Total Usage in Sets: ");
    	float percent = dbAdapter.getSongTotalUsage(songName);
    	message.append(String.format("%.2f", percent) + "%");
    	message.append(StaticVars.EOL);
    	message.append(StaticVars.EOL);
    	
    	// Show group membership
    	message.append("Member of Song Groups: ");
    	
    	c = dbAdapter.getSongGroups(songName);
    	if(c.getCount() > 0) {
	    	c.moveToFirst();
	    	while(!c.isAfterLast()) {
	    		String songGroupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));
	    		message.append(StaticVars.EOL);
	    		message.append("\t" + songGroupName);
	        	c.moveToNext();
	    	}
	    	c.close();
    	} else {
    		message.append(StaticVars.EOL);
    		message.append("\tNo Groups");
    	}
    	
    	// Display information
    	alert.setMessage(message);
    	
    	// Add an OK button
    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});

    	alert.show();
	}

    /**
     * Determines if the specified key is valid
     * @param songKey The key
     * @return True if valid, false if invalid
     */
    public static boolean isValidKey(String songKey) {
        boolean ret = true;

        // Check for a correct key
        if (songKey.isEmpty() || (!StaticVars.songKeyMap.containsKey(songKey) && !StaticVars.songKeys.contains(songKey))) {
            ret = false;
        }

        return ret;
    }

    /**
     * Enables the user to find songs
     */
    private void findSongDialog() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

        // Set the dialog view to gather user input
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.search_dialog, (ViewGroup) findViewById(R.id.search_dialog_root));
        alert.setView(dialoglayout);
        final EditText songNameSearch = (EditText)dialoglayout.findViewById(R.id.search_dialog_text);

        // Add the dialog title
        alert.setTitle("Find Song");

        // Set the OK button
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Make sure there is some text to search
                String searchText = songNameSearch.getText().toString();

                if (searchText.isEmpty()) {
                    Snackbar.make(dialoglayout, "You must enter text to search. Please try again.", Snackbar.LENGTH_LONG).show();
                } else {
                    // Create the song search object
                    SongSearchCriteria songSearch = new SongSearchCriteria();
                    songSearch.songNameSearchText = searchText;

                    // Fill the songs tab with the search data
                    int numResults = ((SongsTab)songsFragment).refillSongsList(songSearch);
                    ((SongsTab)songsFragment).fillSongGroupsSpinner(true, numResults);

                    // Close the dialog
                    dialog.dismiss();
                }
            }
        });

        alert.setNegativeButton("Cancel", null);
        alert.setCanceledOnTouchOutside(true);

        alert.show();
    }

    /**
     * Shows the specified song
     * @param songName
     */
    public void showSong(String songName) {
        // Get the song to show
        SongItem song = dbAdapter.getSong(songName);

        try {
            FileInputStream fis = openFileInput(dbAdapter.getSongFile(song.getName()));
            song.setText(ChordProParser.ParseSongFile(getApplicationContext(), song, song.getKey(), fis, true, false));

            // Show the song activity
            SongActivity songA = new SongActivity();
            Intent showSong = new Intent(getApplicationContext(), songA.getClass());
            showSong.putExtra(StaticVars.SONG_ITEM_KEY, (Parcelable) song);
            startActivity(showSong);

        } catch (FileNotFoundException e) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Could not open song file!", Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Could not open song file!", Snackbar.LENGTH_LONG).show();
        }
    }
    //endregion


    //region Current Set Functions
    // *****************************************************************************
    // * 
    // * Current Set Functions
    // * 
    // *****************************************************************************
	/**
	 * Shows the current set in the current set tab
	 * @param setName The set to show
     */
    public void showCurrentSet(String setName) {
        // Set the current set and show it
        dbAdapter.setCurrentSet(setName);
        ((CurrentSetTab)currSetFragment).refillCurrentSetList();
        mViewPager.setCurrentItem(2, true);
    }

    /**
     * Loads the current set songs to be shown
     * @param startSongName Name of the song to start showing
     */
    public void viewCurrentSet(String startSongName) {
        int position = 0;

        // Create a new SetItem to pass
        SetItem setItem = new SetItem();

        // Get the current set songs
        ArrayList<SongItem> currSetList = ((CurrentSetTab)currSetFragment).getCurrentSetList();

        // Loop through each song in the current set and add it to the array
        for (int i = 0; i < currSetList.size(); i++) {
            SongItem currSong = currSetList.get(i);

            // Get the updated time information
            currSong.setBpm(dbAdapter.getSongBpm(currSong.getName()));
            currSong.setTimeSignature(dbAdapter.getSongTimeSignature(currSong.getName()).toString());

            // Check for start position
            if (currSong.getName().equals(startSongName))
                position = i;

            // Set song text
            try {
                FileInputStream fis = openFileInput(dbAdapter.getSongFile(currSong.getName()));
                currSong.setKey(dbAdapter.getSongKey(currSong.getName()));
                currSong.setText(ChordProParser.ParseSongFile(getApplicationContext(), currSong, dbAdapter.getSongKeyForSet(dbAdapter.getCurrentSetName(), currSong.getName()), fis, true, false));

                setItem.songs.add(currSong);
            } catch (FileNotFoundException e) {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Could not open one of the song files!", Snackbar.LENGTH_LONG).show();
                return;
            } catch (IOException e) {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Could not open song file!", Snackbar.LENGTH_LONG).show();
            }
        }

        // Show the set activity
        SetActivity set = new SetActivity();
        Intent showSet = new Intent(this, set.getClass());
        showSet.putExtra(StaticVars.CURRENT_SONG_KEY, position);
        showSet.putExtra(StaticVars.SET_SONGS_KEY, setItem);
        startActivity(showSet);
    }

    /**
     * Removes the specified song from the specified set
     * @param songName
     * @param setName
     */
    public void removeSongFromSet(final String songName, final String setName, final int songOrder) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Remove Song?!");
        alert.setMessage("Are you sure you want to remove '" + songName + "' from the set '" + setName + "'?");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Remove the song from the set
                dbAdapter.removeSongFromSet(setName, songName, songOrder);

                // Refresh the current set list
                ((CurrentSetTab)currSetFragment).refillCurrentSetList();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Do nothing
            }
        });

        alert.show();
    }
    //endregion


    //region Song Group Functions
    // *****************************************************************************
    // * 
    // * Song Group Functions
    // * 
    // *****************************************************************************
    /**
     * Creates a new song group
     */
    private void createSongGroup() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

    	alert.setTitle("Create Song Group");
    	alert.setMessage("Please enter the name of the song group (must be unique)");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String groupName = input.getText().toString();
	    		if (groupName.length() > 0) {
	    			if(!dbAdapter.createSongGroup(groupName))
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Failed to create song group!", Snackbar.LENGTH_LONG).show();
	    			else
	    				addSongsToGroup(groupName);

                    dialog.dismiss();
	    		}
	    		else {
                    Snackbar.make(input, "Cannot create a song group with no name!", Snackbar.LENGTH_LONG).show();
                    return;
                }
	    		
	    		// Refresh the song list and song group spinner
                ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0, true);
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	    // Canceled.
                dialog.dismiss();
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Shows a dialog to select songs to add to the group
     */
    private void addSongsToGroup(final String groupName) {
    	Cursor c = dbAdapter.getSongs(SongsTab.ALL_SONGS_LABEL);
    	
    	// Clear the previous song lists
    	addSongsDialogList.clear();
    	addSongsDialogMap.clear();
    	
    	// Populate the songs lists
    	while(c.moveToNext()) {
    		addSongsDialogList.add(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)));
    		addSongsDialogMap.put(c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME)), false);
    	}
    	c.close();
    	Collections.sort(addSongsDialogList, new SortIgnoreCase());
    	
    	// Create the alert dialog and set the title
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Select Songs");
    	
    	// Set the dialog view to gather user input
    	LayoutInflater inflater = getLayoutInflater();
    	View dialoglayout = inflater.inflate(R.layout.add_set_songs, (ViewGroup) findViewById(R.id.add_set_songs_root));
    	alert.setView(dialoglayout);
    	
    	// Get the views
    	final Spinner songGroupSP = (Spinner)dialoglayout.findViewById(R.id.add_set_songs_spinner);
    	final ListView songsLV = (ListView)dialoglayout.findViewById(R.id.add_set_songs_list);
    	final ArrayAdapter<String> songsAD;
    	
    	// Fill the list view
    	songsLV.setEmptyView(findViewById(R.id.empty_songs));
    	songsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	songsLV.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
            	String song = addSongsDialogList.get(position);
            	addSongsDialogMap.put(song, !addSongsDialogMap.get(song));
            }
        });
    	songsAD = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, addSongsDialogList);
    	songsLV.setAdapter(songsAD);
    	
    	// Fill the group spinner
        ArrayList<String> songGroups = ((SongsTab)songsFragment).getSongGroupsList(false);
        ArrayAdapter<String> songsGroupsAdapter = new SetGroupArrayAdapter(this, songGroups);
    	songGroupSP.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	// Get the selected group
            	String groupName = (String)songGroupSP.getSelectedItem();
            	
            	// Fill the new songs list
            	Cursor c = dbAdapter.getSongs(groupName);
            	addSongsDialogList.clear();
            	
            	// Populate the ArrayList
            	while (c.moveToNext()) {
            		// Get the strings from the cursor
                	String songName = c.getString(c.getColumnIndex(DBStrings.TBLSONG_NAME));
                	addSongsDialogList.add(songName);
            	}
            	c.close();
            	Collections.sort(addSongsDialogList, new SortIgnoreCase());
            	
            	// Update list view
            	songsAD.notifyDataSetChanged();
            	
            	// Set the list view checked properties
            	for(int i = 0; i < songsLV.getCount(); i++) {
            		songsLV.setItemChecked(i, addSongsDialogMap.get(songsLV.getItemAtPosition(i)));
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	songGroupSP.setAdapter(songsGroupsAdapter);
    	
    	// Set positive button of the dialog
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Add all the selected songs to the group   
	    		for(String s : addSongsDialogMap.keySet()) {
	    			if(addSongsDialogMap.get(s)) {
	    				dbAdapter.addSongToGroup(s, groupName);
	    			}	
	    		}
			}
    	});

    	// Show the dialog
    	AlertDialog a = alert.create();
    	a.show();
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics); 
    	int height = metrics.heightPixels;
    	height = (int) (height / 1.5);
    	a.getWindow().setLayout(LayoutParams.WRAP_CONTENT, height);
    }
    
    /**
     * Deletes the specified group
     * @param groupName The group to delete
     */
    private void deleteSongGroup() {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSongGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	int counter = 0;
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
            String groupName = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONGGROUPS_NAME));

            // Make sure we don't show the All Songs group to delete
            if (!groupName.equals("All Songs")) {
                groupNames[counter++] = groupName;
            }
    	}
    	c.close();
    	
    	// Create the dialog to choose which group to delete
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Choose Group to Delete");
    	alert.setItems(groupNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String groupName = groupNames[which].toString();
				if (groupName.equals(SongsTab.ALL_SONGS_LABEL))
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Cannot Delete the '" + SongsTab.ALL_SONGS_LABEL + "' group!", Snackbar.LENGTH_LONG).show();
				else {
					// Confirm they want to delete the group
					AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this);
					confirm.setTitle("Delete Group?!");
					confirm.setMessage("Are you sure you want to delete '" + groupName + "'?");
					
					confirm.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) {
				    		dbAdapter.deleteSongGroup(groupName);
							
				    		// Refresh the song list and song group spinner
                            ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0);
                            ((SongsTab)songsFragment).refillSongsList();
						}
			    	});

					confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) { 	}
			    	});

					confirm.show();
				}
			}
		});

    	alert.show();
    }
    
    /**
     * Deletes all groups
     */
    private void deleteAllSongGroups() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All Groups?!");
    	alert.setMessage("Are you sure you want to delete ALL groups???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Delete song from database
		    	dbAdapter.deleteAllSongGroups();
	    		
		    	// Refresh the song list and song group spinner
                ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0);
                ((SongsTab)songsFragment).refillSongsList();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Do Nothing
	    	}
    	});

    	alert.show();
    }
    //endregion


    //region Set Group Functions
    // *****************************************************************************
    // * 
    // * Set Group Functions
    // * 
    // *****************************************************************************
    /**
     * Creates a new set group
     */
    private void createSetGroup() {
        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(this);

    	alert.setTitle("Create Set Group");
    	alert.setMessage("Please enter the name of the set group (must be unique)");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		String value = input.getText().toString();
	    		if (value.length() > 0) {
	    			if(!dbAdapter.createSetGroup(value))
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Failed to create set group!", Snackbar.LENGTH_LONG).show();
	    		}
	    		else {
                    Snackbar.make(input, "Cannot create a set group with no name!", Snackbar.LENGTH_LONG).show();
                    return;
                }
	    		
	    		// Refresh the set group spinner and set list
                ((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0);
                ((SetsTab)setsFragment).refillSetsList();

                dialog.dismiss();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	    // Canceled.
                dialog.dismiss();
	    	}
    	});

    	alert.show();
    }
    
    /**
     * Deletes the specified set group
     * @param groupName The group to delete
     */
    private void deleteSetGroup() {
    	// Get the list of group names
    	Cursor c = dbAdapter.getSetGroupNames();
    	
    	final CharSequence[] groupNames = new CharSequence[c.getCount() - 1];
    	int counter = 0;
    	
    	// Don't show the all songs group
    	c.moveToFirst();
    	
    	// Add groups to list view
    	while(c.moveToNext()) {
    		groupNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSETGROUPS_NAME));
    	}
    	c.close();
    	
    	// Create the dialog to choose which group to delete
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Choose Group to Delete");
    	alert.setItems(groupNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String groupName = groupNames[which].toString();
				if (groupName.equals(SetsTab.ALL_SETS_LABEL))
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Cannot Delete the '" + SetsTab.ALL_SETS_LABEL + "' group!", Snackbar.LENGTH_LONG).show();
				else {
					// Confirm they want to delete the group
					AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this);
					confirm.setTitle("Delete Group?!");
					confirm.setMessage("Are you sure you want to delete '" + groupName + "'?");
					
					confirm.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) {
				    		dbAdapter.deleteSetGroup(groupName);
							
				    		// Refresh the set group spinner and set list
                            ((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0);
                            ((SetsTab)setsFragment).refillSetsList();
						}
			    	});

					confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int whichButton) { 	}
			    	});

					confirm.show();
				}
			}
		});

    	alert.show();
    }
    
    /**
     * Deletes all groups
     */
    private void deleteAllSetGroups() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Delete All Set Groups?!");
    	alert.setMessage("Are you sure you want to delete ALL set groups???");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
				// Delete song from database
		    	dbAdapter.deleteAllSetGroups();
	    		
		    	// Refresh the set group spinner and set list
                ((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0);
                ((SetsTab)setsFragment).refillSetsList();
			}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		// Do Nothing
	    	}
    	});

    	alert.show();
    }
    //endregion


    //region Import / Export functions
    // *****************************************************************************
    // * 
    // * Import / Export Functions
    // * 
    // *****************************************************************************
    /**
     * Selects the folder to export the backup file to
     */
    private void selectExportFolder(String activityKey) {
    	// Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        intent.putExtra(StaticVars.FILE_ACTIVITY_KEY, activityKey);
        intent.putExtra(StaticVars.FILE_ACTIVITY_TYPE_KEY, StaticVars.FILE_ACTIVITY_FOLDER);
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    /**
     * Exports all songbook files and db
     */
    private void exportAll(String folder) {
    	final String exportZipLocation = folder + "/" + StaticVars.EXPORT_ZIP_FILE;
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Export");
    	alert.setMessage("Are you sure you want to export your data to '" + exportZipLocation + "'?");

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Create the db backup sql script
                String exportSQLData = dbAdapter.exportDBData();

                try {
                    // Store the backup script in the app files folder
                    FileOutputStream out = openFileOutput(StaticVars.EXPORT_SQL_FILE, Context.MODE_PRIVATE);
                    out.write(exportSQLData.getBytes());
                    out.close();

                    // Get a list of all the files in the app files folder
                    String[] tmpFiles = fileList();
                    ArrayList<String> filesAL = new ArrayList<>();
                    for (int i = 0; i < tmpFiles.length; i++) {
                        // Check for .txt or .sql file
                        String fileName = tmpFiles[i];
                        if (fileName.equals(StaticVars.EXPORT_SQL_FILE) ||
                                fileName.substring(fileName.length() - 4).equals(".txt"))
                            filesAL.add(getFilesDir() + "/" + fileName);
                    }

                    // Convert to string array
                    String[] files = new String[filesAL.size()];
                    files = filesAL.toArray(files);

                    // Zip the files and save to the external storage
                    Compress newZip = new Compress(files, exportZipLocation);
                    if (newZip.zip())
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Your data has been successfully saved to: " + exportZipLocation, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(getWindow().getDecorView().getRootView(), "There was an error backing up your data. Please try again.", Snackbar.LENGTH_LONG).show();

                    // Delete the backup script
                    deleteFile(StaticVars.EXPORT_SQL_FILE);
                } catch (Exception e) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Could not write db file!", Snackbar.LENGTH_LONG).show();
                }


            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled, do not import
            }
        });

    	alert.show();
    }

    /**
     * Exports the set stored in setToExport
     * @param folder The folder to export the set to
     */
    private void exportSet(String folder) {
        // Make sure we have a valid set to export
        if (setToExport != null) {
            // Set the export location
            String filename = setToExport.getName() + ".bak";
            String exportZipLocation = folder + "/" + filename;

            // Create the db backup sql script
            String exportSQLData = dbAdapter.exportSetDBData(setToExport.getName());

            try {
                // Store the backup script in the app files folder
                FileOutputStream out = openFileOutput(StaticVars.EXPORT_SQL_FILE, Context.MODE_PRIVATE);
                out.write(exportSQLData.getBytes());
                out.close();

                // Get a list of all the files in the app files folder
                String[] allFiles = fileList();
                ArrayList<String> files = new ArrayList<>();
                for (int i = 0; i < allFiles.length; i++) {
                    // Only add the songs that are part of this set
                    for (SongItem s : setToExport.songs) {
                        String tmp = s.getName();
                        if (allFiles[i].contains(tmp)) {
                            // Song is in the set, add it to the list
                            files.add(getFilesDir() + "/" + allFiles[i]);
                        }
                    }
                }

                // Add the sql file to the list
                files.add(getFilesDir() + "/" + StaticVars.EXPORT_SQL_FILE);

                // Zip the files and save to the external storage
                String[] filesToZip = files.toArray(new String[0]);
                Compress newZip = new Compress(filesToZip, exportZipLocation);
                if (newZip.zip()) {
                    // Alert the user the set has been exported
                    Snackbar.make(getWindow().getDecorView().getRootView(), "\"" + setToExport.getName() + "\" has been exported to " + exportZipLocation, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "There was an error exporting your set. Please try again.", Snackbar.LENGTH_LONG).show();
                }

                // Delete the backup script
                deleteFile(filename);
            } catch (Exception e) {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Could not write export file!", Snackbar.LENGTH_LONG).show();
            }



            // Clear the set to export
            setToExport = null;
        }
    }
    
    /**
     * Opens the dialog to select the file to import
     */
    private void selectImportFile(String activityKey) {
    	// Create the open file intent
        Intent intent = new Intent(getBaseContext(), OpenFile.class);
        intent.putExtra(StaticVars.FILE_ACTIVITY_KEY, activityKey);
        intent.putExtra(StaticVars.FILE_ACTIVITY_TYPE_KEY, StaticVars.FILE_ACTIVITY_FILE);
        
        // Start the activity
        startActivityForResult(intent, 1);
    }
    
    /**
     * Imports songbook files and data from the specified file
     * @param filePath The compressed file to import
     */
    private void importFile(final String filePath, final boolean clearDB, String warningMessage) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

        Log.v("Import", "File Name: " + filePath);

        alert.setTitle("Download From Cloud");
    	alert.setMessage(warningMessage);
    	
    	final ImportDatabase importDBTask = new ImportDatabase();

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Configure progress dialog
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Importing Data. This may take a few minutes." + System.getProperty("line.separator") + "Please wait...");
                progressDialog.setTitle("Please Wait!");
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener) null);

                progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // Update the progress dialog text
                                progressDialog.setMessage("Please wait while we stop the import..." + System.getProperty("line.separator") + "This may take a few minutes.");

                                // Hide the cancel button
                                progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);

                                // Cancel the import task
                                importDBTask.cancel(true);
                            }
                        });
                    }
                });


                // Show progress dialog
                progressDialog.show();

                // Start the import task
                ImportDBParams params = new ImportDBParams(filePath, clearDB);
                importDBTask.execute(new ImportDBParams[]{params});
            }
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled, do not import
            }
        });

    	alert.show();
    }
    //endregion


    //region Classes
    // *****************************************************************************
    // * 
    // * Classes
    // * 
    // *****************************************************************************
    public class ImportDBParams {

        public ImportDBParams() {
            filePath = "";
            clearDB = false;
            result = "";
        }

        public ImportDBParams(String _filepath, boolean _clearDB) {
            filePath = _filepath;
            clearDB = _clearDB;
            result = "";
        }

        public ImportDBParams(String _filepath, boolean _clearDB, String _result) {
            filePath = _filepath;
            clearDB = _clearDB;
            result = _result;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String filePath;

        public boolean getClearDB() {
            return clearDB;
        }

        public void setClearDB(boolean clearDB) {
            this.clearDB = clearDB;
        }

        public boolean clearDB;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String result;
    }

    public class ImportDatabase extends AsyncTask<ImportDBParams, Void, ImportDBParams> {
    	@Override
    	protected ImportDBParams doInBackground(ImportDBParams... params) {
            // Setup return
            ImportDBParams ret = new ImportDBParams();
    		ret.setResult("");

            // Get the parameters
            String filePath = params[0].getFilePath();
            boolean clearDB = params[0].getClearDB();
    		
    		// Decompress the backup file
    		String unzipFolder = "sbg_unzipped";
        	String unzipLocation = Environment.getExternalStorageDirectory() + "/" + unzipFolder + "/"; 
        	 
        	Decompress d = new Decompress(filePath, unzipLocation);
        	if (!d.unzip()) {
                ret.setResult("There was an error decompressing your backup file. Please try again.");
        	}
        	else {
            	// Run the sql script to import songs
            	try {
            		// Open and read the export file
        	    	InputStream fis = new FileInputStream(unzipLocation + "/" + StaticVars.EXPORT_SQL_FILE);
        	    	DataInputStream din = new DataInputStream(fis);
        	    	BufferedReader br = new BufferedReader(new InputStreamReader(din));
        	    	StringBuilder sb = new StringBuilder();
        	        String line = br.readLine();
        	        
        	        // Cycle through each line in the sql file and add it to the string builder
        	        while(line != null) {
        	        	sb.append(line + StaticVars.EOL);
        	        	line = br.readLine();
        	        }
        	        
        	        br.close();
        	        
        	        // Check for cancel
                	if (isCancelled()) {
                		return ret;
                	}
                	
                	// Clear the database and files
                    if (clearDB) {
                        dbAdapter.clearDB();
                        String[] files = fileList();
                        for (int i = 0; i < files.length; i++) {
                            deleteFile(files[i]);
                        }
                    }
                	
                	// Check for cancel
                	if (isCancelled()) {
                		return ret;
                	}
        	        
        	        // Execute the SQL file
        	        if (!dbAdapter.importDBData(sb.toString())) {
                        // Failed to import the database data properly
                        ret.setResult("Failed to import the database file. Import aborted.");
                        return ret;
                    }
        	        
        	        // Check for cancel
                	if (isCancelled()) {
                		return ret;
                	}
        	        
        	        // Add the song files to the files directory
                	File dir = new File(unzipLocation);
                	for (File child : dir.listFiles()) {
                		// Try to add the song file
                		try {
            	    		InputStream in = new FileInputStream(child);
            	    		OutputStream out = openFileOutput(child.getName(), Context.MODE_PRIVATE);
            	    		byte[] buf = new byte[1024];
            	    		int len;
            	    		while ((len = in.read(buf)) > 0) {
            	    		   out.write(buf, 0, len);
            	    		}
            	    		in.close();
            	    		out.close(); 
                		}
                		catch (Exception e) {
                			// If the song file failed, remove the song from the DB
                            if (clearDB) {
                                String songName = child.getName();
                                songName = songName.substring(0, songName.lastIndexOf("."));
                                dbAdapter.deleteSong(songName);

                                if (ret.equals("")) {
                                    ret.setResult("Import complete, some songs failed: " + songName);
                                } else {
                                    ret.setResult(ret.getResult() + ", " + songName );
                                }
                            }
                		}
                		
                		// Check for cancel
                    	if (isCancelled()) {
                    		return ret;
                    	}
                	}
            	}
            	catch (Exception e) {
            		// Add the default values back into the db
                    if (clearDB) {
                        dbAdapter.addDBDefaults();
                    }
                    ret.setResult("Could not import database file. Import aborted.");
            	}
            	
            	// Delete the unzipped temp files
            	File dir = new File(Environment.getExternalStorageDirectory(), unzipFolder);
            	if (dir.isDirectory()) {
            		String filesList[] = dir.list();
            		for (String f : filesList) {
            			new File(dir, f).delete();
            		}
            	}
        	}
        	
        	// Set return value
        	if (ret.getResult().equals("")) {
                ret.setResult("Successfully imported your data!");
        	}
        	
        	return ret;
    	}
    	
    	@Override
    	protected void onPostExecute(ImportDBParams result) {
    		// Refresh all the lists
            ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0);
            ((SongsTab)songsFragment).fillSongSortSpinner();
            ((SongsTab)songsFragment).refillSongsList();
			((SetsTab)setsFragment).refillSetsList();
			((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0);
            ((CurrentSetTab)currSetFragment).refillCurrentSetList();
        	
        	// Close the progress dialog
        	progressDialog.dismiss();
        	
        	// Show success message
            Snackbar.make(getWindow().getDecorView().getRootView(), result.getResult(), Snackbar.LENGTH_LONG).show();
    	}
    	
    	@Override
    	protected void onCancelled(ImportDBParams result) {
    		// Clear the database and files
            if (result.getClearDB()) {
                dbAdapter.clearDB();
                String[] files = fileList();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }

                // Add the default values back into the db
                dbAdapter.addDBDefaults();
            }
    		
    		// Refresh all the lists
            ((SongsTab)songsFragment).fillSongGroupsSpinner(false, 0);
            ((SongsTab)songsFragment).fillSongSortSpinner();
            ((SongsTab)songsFragment).refillSongsList();
            ((SetsTab)setsFragment).fillSetGroupsSpinner(false, 0);
            ((SetsTab)setsFragment).refillSetsList();
            ((CurrentSetTab)currSetFragment).refillCurrentSetList();
        	
        	// Close the progress dialog
        	progressDialog.dismiss();
        	
        	// Show success message
            Snackbar.make(getWindow().getDecorView().getRootView(), "Your import was cancelled!", Snackbar.LENGTH_LONG).show();
    	}
    }

    /**
     * Comparator for case insensitive sorting
     * @author SamIAm
     *
     */
    @SuppressLint("DefaultLocale")
	public static class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            int ret;

            String s1 = (String) o1;
            String s2 = (String) o2;

            // Check for special case
            if (s1.equals(SongsTab.ALL_SONGS_LABEL) || s1.equals(SetsTab.ALL_SETS_LABEL)) {
                ret = -10;
            } else if (s2.equals(SongsTab.ALL_SONGS_LABEL) || s2.equals(SetsTab.ALL_SETS_LABEL)) {
                ret = 10;
            } else {
                ret = s1.toLowerCase(Locale.ENGLISH).compareTo(s2.toLowerCase());
            }

            return ret;
        }
    }
    //endregion
}

