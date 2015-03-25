package com.sbgsoft.songbook.views;

import java.io.BufferedReader;
import java.io.StringReader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoFitTextView extends TextView {
	
	static final String TAG = "AutoFitTextView";
	
	float minimumTextSizePixels = 4.0f;
	float textDecrement = 1.0f;
	boolean fit = true;
	
	public AutoFitTextView(Context context) {
		super(context);
	}

	public AutoFitTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoFitTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public float getMinimumTextSizePixels() {
		return minimumTextSizePixels;
	}

	public void setMinimumTextSizePixels(float minimumTextSizePixels) {
		this.minimumTextSizePixels = minimumTextSizePixels;
	}
	
	public float getTextDecrement() {
		return textDecrement;
	}

	public void setTextDecrement(float textDecrement) {
		this.textDecrement = textDecrement;
	}

	public void setFitTextToBox( Boolean fit ) {
		this.fit = fit;
	}
	
	public boolean getFitTextToBox() {
		return this.fit;
	}
	
	protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
		if (fit) shrinkToFit();
	}
	
	public void shrinkToFit() {
		float currentTextSize = this.getTextSize();
		
		// Check to make sure we aren't already at the minimum size
		if (!(currentTextSize <= minimumTextSizePixels)) {
			// Shrink the width to fit any long lines
			shrinkToFitWidth();
		}
		
		// Check to make sure we aren't already at the minimum size
		currentTextSize = this.getTextSize();
		if (!(currentTextSize <= minimumTextSizePixels)) {
			// Shrink the height to fit any extra lines
			shrinkToFitHeight();
		}
	}
	
	protected void shrinkToFitWidth() {
		int width = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
		
		// Get the current text size in pixels
		float currentTextSize = this.getTextSize();
		
		// Check to make sure we aren't already at the minimum size
		if (!(currentTextSize <= minimumTextSizePixels)) {
			// Loop through each line of the string to check if width needs adjusted
	    	try {
		    	BufferedReader bufReader = new BufferedReader(new StringReader(this.getText().toString()));
		    	String line = null;
		    	while ((line = bufReader.readLine()) != null) {
		    		// Get the width of the text
		    		Rect bounds = new Rect();
		    		Paint textPaint = this.getPaint();
//		    		textPaint.getTextBounds(line, 0, line.length(), bounds);
//		    		int lineWidth1 = bounds.width();
		    		float lineWidth = textPaint.measureText(line);
		    		
		    		// Check to see if the text size needs decremented
		    		if (lineWidth >= (float)width) {
		    			// Decrement the text size
		    			if (currentTextSize - textDecrement >= minimumTextSizePixels) {
		    				this.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize - textDecrement);
		    			}
		    			else {
		    				this.setTextSize(TypedValue.COMPLEX_UNIT_PX, minimumTextSizePixels);
		    			}
	
		    			// Check all the lines again
		    			shrinkToFitWidth();
		    		}
		    	}
	    	} catch (Exception e) { }
		}
	}
	
	protected void shrinkToFitHeight() {
		int height = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
		
		// Get the current text size in pixels
		float currentTextSize = this.getTextSize();
		
		// Check to make sure we aren't already at the minimum size
		if (!(currentTextSize <= minimumTextSizePixels)) {
			int lines = this.getLineCount();
			
			Rect r = new Rect();
			int y1 = this.getLineBounds(0, r);
			int y2 = this.getLineBounds(lines-1, r);
			
			float size = this.getTextSize();
			if ((y2 + y1) > height && size > 2.0f) {
				// Set the text size in pixels
				this.setTextSize(TypedValue.COMPLEX_UNIT_PX, size - 1.0f);
				shrinkToFitHeight();
			}	
		}
	}
}
