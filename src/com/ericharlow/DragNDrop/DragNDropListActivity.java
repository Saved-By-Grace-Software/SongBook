/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ericharlow.DragNDrop;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainStrings;

public class DragNDropListActivity extends ListActivity {
	//private static String[] mListContent={"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7"};
	private static String[] mListContent;
	private String setName = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dragndroplistview);
        
        // Get songs and add them to the page adapter
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String[] songs = extras.getStringArray(MainStrings.SET_SONGS_KEY);
            setName = extras.getString(MainStrings.SET_NAME_KEY);
            mListContent = songs;
        }
        
        ArrayList<String> content = new ArrayList<String>(mListContent.length);
        for (int i=0; i < mListContent.length; i++) {
        	content.add(mListContent[i]);
        }
        
        setListAdapter(new DragNDropAdapter(this, new int[]{R.layout.dragitem}, new int[]{R.id.TextView01}, content));//new DragNDropAdapter(this,content)
        ListView listView = getListView();
        
        if (listView instanceof DragNDropListView) {
        	((DragNDropListView) listView).setDropListener(mDropListener);
        	((DragNDropListView) listView).setRemoveListener(mRemoveListener);
        	((DragNDropListView) listView).setDragListener(mDragListener);
        }
    }
    
    /**
     * Called when the save button is clicked
     * @param v
     */
    public void onSaveClick(View v) {
    	// Get the new set order
    	String[] newOrder = new String[getListView().getCount()];
    	for (int i = 0; i < getListView().getCount(); i++) {
    		newOrder[i] = (String)getListView().getItemAtPosition(i);
    	}
    	
    	// Send the new order back to the main activity
    	getIntent().putExtra(MainStrings.ACTIVITY_RESPONSE_TYPE, MainStrings.REORDER_ACTIVITY);
    	getIntent().putExtra(MainStrings.SET_SONGS_KEY, newOrder);
    	getIntent().putExtra(MainStrings.SET_NAME_KEY, setName);
		setResult(RESULT_OK, getIntent());    	
    	
    	// Finish the activity
    	finish();
    }
    
    /**
     * Called when the cancel button is clicked
     * @param v
     */
    public void onCancelClick(View v) {
    	// Don't save, finish the activity
    	finish();
    }

	private DropListener mDropListener = 
		new DropListener() {
        public void onDrop(int from, int to) {
        	ListAdapter adapter = getListAdapter();
        	if (adapter instanceof DragNDropAdapter) {
        		((DragNDropAdapter)adapter).onDrop(from, to);
        		getListView().invalidateViews();
        	}
        }
    };
    
    private RemoveListener mRemoveListener =
        new RemoveListener() {
        public void onRemove(int which) {
        	ListAdapter adapter = getListAdapter();
        	if (adapter instanceof DragNDropAdapter) {
        		((DragNDropAdapter)adapter).onRemove(which);
        		getListView().invalidateViews();
        	}
        }
    };
    
    private DragListener mDragListener =
    	new DragListener() {

    	int backgroundColor = 0xe0103010;
    	int defaultBackgroundColor;
    	
			public void onDrag(int x, int y, ListView listView) {
				// TODO Auto-generated method stub
			}

			public void onStartDrag(View itemView) {
				itemView.setVisibility(View.INVISIBLE);
				defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
				itemView.setBackgroundColor(backgroundColor);
				ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
				if (iv != null) iv.setVisibility(View.INVISIBLE);
			}

			public void onStopDrag(View itemView) {
				itemView.setVisibility(View.VISIBLE);
				itemView.setBackgroundColor(defaultBackgroundColor);
				ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
				if (iv != null) iv.setVisibility(View.VISIBLE);
			}
    	
    };
    
    
}