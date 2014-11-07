package com.sbgsoft.songbook.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.main.MainStrings;

public class OpenFile extends ListActivity {
	public static final String RESULT_PATH = "filePath";
	private ArrayList<String> extensions;
	private boolean allFiles = false;
	private List<String> item = null;
	private List<String> path = null;
	private String root, activityType, fileType;
	private TextView myPath;
	private String currentDir;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_open_file);
        myPath = (TextView)findViewById(R.id.open_file_path);
        
        // Get Extras
        activityType = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	// Get the activity type
        	activityType = extras.getString(MainStrings.FILE_ACTIVITY_KEY);
        	
        	// Get the file type
        	fileType = extras.getString(MainStrings.FILE_ACTIVITY_TYPE_KEY);
        }
        
        // Change buttons and title for folder type
        if (fileType.equals(MainStrings.FILE_ACTIVITY_FOLDER)) {
        	// Remove the file type views
        	findViewById(R.id.file_type_spinner).setVisibility(View.GONE);
        	
        	// Show the select folder button
        	findViewById(R.id.open_file_select_folder).setVisibility(View.VISIBLE);
        	
        	// Set the title
        	setTitle(getResources().getString(R.string.title_activity_open_folder));
        }
        
        // Set the SD card as the root directory
        root = Environment.getExternalStorageDirectory().getPath();
        currentDir = root;
        
        // Fill the file type spinner
        if (activityType.equals(MainStrings.IMPORT_DB_ACTIVITY)) {
        	fillDBFileTypeSpinner();
        } else {
        	fillSongFileTypeSpinner();
        }
        
        
        // Show the files
        getDir(root);
    }
	
	private void fillSongFileTypeSpinner() {
		// Set the extensions
		extensions = new ArrayList<String>(Arrays.asList("txt", "pro", "chordpro", "chopro"));
		
		// Create the spinner adapter
		ArrayList<String> fileTypes = new ArrayList<String>(Arrays.asList("SongBook Files", "All Files"));
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.file_spinner_item, fileTypes);
    	adapter.setDropDownViewResource( R.layout.file_spinner_dropdown_item );
    	final Spinner fileSpinner = (Spinner) findViewById(R.id.file_type_spinner);
    	
    	// Set the on click listener for each item
    	fileSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	if (((String)a.getItemAtPosition(position)).equals("All Files")) {
	            	allFiles = true;
	            	getDir(currentDir);
            	}
            	else {
            		allFiles = false;
            		getDir(currentDir);
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	
    	// Set the adapter
    	fileSpinner.setAdapter(adapter);
	}
	
	private void fillDBFileTypeSpinner() {
		// Set the extensions
		extensions = new ArrayList<String>(Arrays.asList("bak"));
				
		// Create the spinner adapter
		ArrayList<String> fileTypes = new ArrayList<String>(Arrays.asList("SongBook Backup Files", "All Files"));
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.file_spinner_item, fileTypes);
    	adapter.setDropDownViewResource( R.layout.file_spinner_dropdown_item );
    	final Spinner fileSpinner = (Spinner) findViewById(R.id.file_type_spinner);
    	
    	// Set the on click listener for each item
    	fileSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int position, long row) {
            	if (((String)a.getItemAtPosition(position)).equals("All Files")) {
	            	allFiles = true;
	            	getDir(currentDir);
            	}
            	else {
            		allFiles = false;
            		getDir(currentDir);
            	}
            }
            
            public void onNothingSelected(AdapterView<?> arg0) {
            	// Nothing was clicked so ignore it
            }
        });
    	
    	// Set the adapter
    	fileSpinner.setAdapter(adapter);
	}
    
	/**
	 * Show the files
	 * @param dirPath The path to show
	 */
    private void getDir(String dirPath)
    {
    	myPath.setText("Location: " + dirPath);
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
 
    	// Add directories to the list
    	if(!dirPath.equals("/"))
    	{
    		item.add("./");
    		path.add("/");
    		item.add("../");
    		path.add(f.getParent()); 
    	}
 
    	Arrays.sort(files, filecomparator);
 
    	// Add files to the list
    	for(int i=0; i < files.length; i++)
    	{
    		File file = files[i];
    		int temp = file.getAbsolutePath().lastIndexOf(".");
    		String extension = file.getAbsolutePath().substring(temp + 1);
    		
    		if(!file.isHidden() && file.canRead()){
    			if(file.isDirectory()){
    				path.add(file.getPath());
    				item.add(file.getName() + "/");
    			}else{
    				// Only show selected file type
    	    		if (extensions.contains(extension) || allFiles) {
    	    			path.add(file.getPath());
    	    			item.add(file.getName());
    	    		}
    			}
    		} 
    	}

    	setListAdapter(new FileArrayAdapter(this, item)); 
    }
    
    /**
     * Comparator to sort files and directories in alphabetical order
     */
	Comparator<? super File> filecomparator = new Comparator<File>(){
		public int compare(File file1, File file2) {
			if(file1.isDirectory()){
				if (file2.isDirectory()){
					return String.valueOf(file1.getName().toLowerCase(Locale.ENGLISH)).compareTo(file2.getName().toLowerCase());
				}else{
					return -1;
				}
			}else {
				if (file2.isDirectory()){
					return 1;
				}else{
					return String.valueOf(file1.getName().toLowerCase(Locale.ENGLISH)).compareTo(file2.getName().toLowerCase());
				}
			}
		}  
	};
    	
	/**
	 * Click listener
	 */
	@Override
	protected void onListItemClick(ListView l, final View v, int position, long id) {
		final File file = new File(path.get(position));
		
		if (file.isDirectory())
		{
			if(file.canRead()){
				currentDir = path.get(position);
				getDir(currentDir);
			}else{
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", null).show(); 
			} 
		}else {
			// Create the dialog to choose which group to add the song to
	    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

	    	alert.setIcon(R.drawable.ic_launcher);
	    	alert.setTitle("Correct File?");
	    	alert.setMessage("Is '" + file.getName() + "' the correct file?");
	    	
	    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int whichButton) {
		    		getIntent().putExtra(MainStrings.ACTIVITY_RESPONSE_TYPE, activityType);
					getIntent().putExtra(RESULT_PATH, file.getAbsolutePath());
					setResult(RESULT_OK, getIntent());
					finish();
		    	}
		    });
	    	
	    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int whichButton) {
		    		// Do nothing, wrong file chosen
		    	}
		    });
	    	
	    	alert.show();
    	}
    }
	
	/**
	 * Cancel button was clicked
	 * @param v
	 */
	public void onCancelClick(View v) {
		getIntent().putExtra(MainStrings.ACTIVITY_RESPONSE_TYPE, activityType);
		setResult(RESULT_CANCELED, getIntent());
		finish();
	}
	
	/**
	 * Select folder was clicked
	 * @param v
	 */
	public void onSelectFolderClick(View v) {
		getIntent().putExtra(MainStrings.ACTIVITY_RESPONSE_TYPE, activityType);
		getIntent().putExtra(RESULT_PATH, currentDir);
		setResult(RESULT_OK, getIntent());
		finish();
	}
}	