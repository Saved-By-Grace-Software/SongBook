package com.sbgsoft.songbook.views;

import java.io.BufferedReader;
import java.io.StringReader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

public class AutoFitTextView extends TextView {
	
	static final String TAG = "AutoFitTextView";
	
	float minimumTextSizePixels = 4.0f;
	float textDecrement = 1.0f;
	boolean fit = true;
    Context mContext;
	
	public AutoFitTextView(Context context) {
		super(context);
        mContext = context;
	}

	public AutoFitTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mContext = context;
	}

	public AutoFitTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        mContext = context;
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
		
		currentTextSize = this.getTextSize();
	}
	
	protected void shrinkToFitWidth() {
        // Get the width to fit it to
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
		//int width = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
		
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
		    		Paint textPaint = this.getPaint();
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
		int height = this.getHeight();
		
		// Get the current text size in pixels
		float currentTextSize = this.getTextSize();
		
		// Check to make sure we aren't already at the minimum size
		if (!(currentTextSize <= minimumTextSizePixels)) {
			int widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.getWidth(), MeasureSpec.AT_MOST);
			int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			this.measure(widthMeasureSpec, heightMeasureSpec);
			int textHeight = this.getMeasuredHeight();
			
			// Check the overall height
			if (textHeight > height && currentTextSize > 2.0f) {
				// Set the text size in pixels
				this.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize - textDecrement);
				shrinkToFitHeight();
			}	
		}
	}
}
