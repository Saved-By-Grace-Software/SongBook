package com.sbgsoft.tabapp.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sbgsoft.tabapp.R;
import com.sbgsoft.tabapp.main.MainStrings;

public class OpenFile extends ListActivity {
	public static final String RESULT_PATH = "filePath";
	private List<String> item = null;
	private List<String> path = null;
	private String root;
	private TextView myPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_open_file);
        myPath = (TextView)findViewById(R.id.open_file_path);
        
        // Set the SD card as the root directory
        root = Environment.getExternalStorageDirectory().getPath();
        
        // Show the files
        getDir(root);
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
 
    	if(!dirPath.equals(root))
    	{
    		item.add(root);
    		path.add(root);
    		item.add("../");
    		path.add(f.getParent()); 
    	}
 
    	Arrays.sort(files, filecomparator);
 
    	for(int i=0; i < files.length; i++)
    	{
    		File file = files[i];
    		
    		if(!file.isHidden() && file.canRead()){
    			path.add(file.getPath());
    			if(file.isDirectory()){
    				item.add(file.getName() + "/");
    			}else{
    				item.add(file.getName());
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
					return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
				}else{
					return -1;
				}
			}else {
				if (file2.isDirectory()){
					return 1;
				}else{
					return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
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
				getDir(path.get(position));
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
	    	alert.setTitle("Are You Sure?");
	    	alert.setMessage("Are you sure you want to import '" + file.getName() + "'?");
	    	
	    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int whichButton) {
		    		getIntent().putExtra(MainStrings.ACTIVITY_RESPONSE_TYPE, MainStrings.FILE_ACTIVITY);
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
	
}	