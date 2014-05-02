package com.example.leapsensor;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	File			sdCardDir = null;
	TextView		tvSDMem;
	EditText		etTimeInterval;
	Button			buttonStart;
	
	double			timeInterval;
	
	/** Set the range of the valid time interval */
	final double	MIN_TIME = 1.0;
	final double	MAX_TIME = 10.0;
	final double	DEFAULT_TIME = 5.0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        detectSDCard();
        etTimeInterval = (EditText) findViewById(R.id.editTextTime);
        setButtonListener();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * Get Empty SD Memory Size and Display.
     */
    private void detectSDCard()
    {
    	if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
        	sdCardDir = Environment.getExternalStorageDirectory();
        	StatFs	fs = new StatFs(sdCardDir.getPath());
        	int blockSize, emptyBlock, emptySize;
        	blockSize = fs.getBlockSize();
        	emptyBlock = fs.getAvailableBlocks();
        	emptySize = (blockSize * emptyBlock) >> 20;
        	
        	tvSDMem = (TextView) findViewById(R.id.textViewSDMem);
        	tvSDMem.setText(String.valueOf(emptySize));
        }
        else
        {
        	/** SD Card not available */
        	Toast errSD = Toast.makeText(getApplicationContext(), "SD Card Not Found", Toast.LENGTH_SHORT);
        	errSD.show();
        }
    }
    
    /**
     * Set Listener of the Start Button.
     * Check the range of the Time Interval.
     */
    private void setButtonListener()
    {
    	buttonStart = (Button) findViewById(R.id.buttonStart);
    	buttonStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/** Check the Time Interval */
				timeInterval = Double.valueOf(etTimeInterval.getText().toString());
				if (timeInterval < MIN_TIME)
				{
					Toast minTime = Toast.makeText(getApplicationContext(), "Time Interval too small", Toast.LENGTH_SHORT);
					minTime.show();
					etTimeInterval.setText(String.valueOf(DEFAULT_TIME));
				}
				else if (timeInterval > MAX_TIME)
				{
					Toast maxTime = Toast.makeText(getApplicationContext(), "Time Interval too big", Toast.LENGTH_SHORT);
					maxTime.show();
					etTimeInterval.setText(String.valueOf(DEFAULT_TIME));
				}
				else
				{
					Toast start = Toast.makeText(getApplicationContext(), "Start!", Toast.LENGTH_SHORT);
					start.show();
				}
			}
		});
    }
}
