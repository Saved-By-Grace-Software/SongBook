package com.sbgsoft.tabapp.sets;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.db.DBStrings;
import com.sbgsoft.tabapp.main.MainActivity;
import com.sbgsoft.tabapp.songs.SongsTab;

public class CreateSetActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_set);
        
        fillSongsList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_create_set, menu);
        return true;
    }
    
    private void fillSongsList() {
    	Cursor c = MainActivity.dbAdapter.getSongNames(SongsTab.ALL_SONGS_LABEL);
    	
    	final String[] songNames = new String[c.getCount()];
    	final boolean[] songsChecked = new boolean[c.getCount()];
    	int counter = 0;
    	
    	// Add songs to list view
    	while(c.moveToNext()) {
    		songsChecked[counter] = false;
    		songNames[counter++] = c.getString(c.getColumnIndexOrThrow(DBStrings.TBLSONG_NAME));
    	}
    	
    	// Set up the list view and adapter
        ListView lv = ((ListView)findViewById(R.id.create_set_song_list));
        lv.setEmptyView(findViewById(R.id.empty_songs));
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        // Set the on click listener for each item
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long row) {
//            	// Get the set to show
//            	String setName = setsList.get(position).getName();
//            	
//            	// Set the current set and show it
//            	dbAdapter.setCurrentSet(setName);
//            	mViewPager.setCurrentItem(0, true);
//            	fillCurrentSetListView();
            }
        });

        // Register the context menu and set the adapter
        registerForContextMenu(lv);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, songNames));
    }
    
    public void onCancelClick(View v) {
    	// Canceled do nothing
    	finish();
    }
    
    public void onOKClick(View v) {
    	
    	
    	// Close the activity
    	finish();
    }
}
