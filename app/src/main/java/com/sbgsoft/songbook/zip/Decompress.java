package com.sbgsoft.songbook.zip;

import android.util.Log;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream; 
 
/** 
 * 
 * @author jon 
 */ 
public class Decompress { 
	private String _zipFile; 
	private File _location;
 
	public Decompress(String zipFile, File location) {
		_zipFile = zipFile; 
		_location = location; 
 
		_dirChecker(""); 
	} 
 
	public boolean unzip() { 
		try  {
			FileInputStream fin = new FileInputStream(_zipFile); 
			ZipInputStream zin = new ZipInputStream(fin); 
			ZipEntry ze = null;
			String folderLocation = _location.getAbsolutePath() + "/";

			while ((ze = zin.getNextEntry()) != null) { 
				Log.v("Decompress", "Unzipping " + ze.getName());

				if(ze.isDirectory()) { 
					_dirChecker(ze.getName()); 
				} else {
					FileOutputStream fout = new FileOutputStream(folderLocation + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) { 
						fout.write(c); 
					}
					zin.closeEntry(); 
					fout.close(); 
				} 
			} 
			zin.close(); 
		} catch(Exception e) { 
			Log.e("Decompress", "unzip", e); 
			return false;
		} 
		
		return true;
	} 
 
	private void _dirChecker(String dir) { 
		File f = new File(_location + dir); 
 
		if(!f.isDirectory()) { 
			f.mkdirs(); 
		} 
	} 
} 