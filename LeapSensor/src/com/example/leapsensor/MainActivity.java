package com.example.leapsensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main Activity of the Leap Project on Android App.
 * @author Zhen Li
 * @version 1.0 May 1st 2014
 */
public class MainActivity extends Activity {

	TextView		tvSDMem;
	EditText		etTimeInterval;
	Button			buttonStart, buttonEnd;
	
	File			sdCardDir = null;
	FileWriter		fWriter = null;
	long			startTime = 0;
	double			timeInterval;
	Handler			sensorHandler = null;
	Runnable		runnable;
	
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
    
    
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (fWriter != null)
		{
			try {
				fWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (sensorHandler != null)
		{
			sensorHandler.removeCallbacks(runnable);
		}
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
        	Toast.makeText(getApplicationContext(), "SD Card Not Found", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Set Listener of the Start / End Button.
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
					Toast.makeText(getApplicationContext(), "Time Interval too small", Toast.LENGTH_SHORT).show();
					etTimeInterval.setText(String.valueOf(DEFAULT_TIME));
				}
				else if (timeInterval > MAX_TIME)
				{
					Toast.makeText(getApplicationContext(), "Time Interval too big", Toast.LENGTH_SHORT).show();
					etTimeInterval.setText(String.valueOf(DEFAULT_TIME));
				}
				else
				{
					/** Start the real work */
					Toast.makeText(getApplicationContext(), "Start!", Toast.LENGTH_SHORT).show();
					
					recordSensor();
				}
			}
		});
    	
    	buttonEnd = (Button) findViewById(R.id.buttonEnd);
    	buttonEnd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (fWriter == null || sensorHandler == null)
				{
					Toast.makeText(getApplicationContext(), "Not Started", Toast.LENGTH_SHORT).show();
				}
				else
				{
					/** Close the file writer and handler */
					try {
						fWriter.close();
						sensorHandler.removeCallbacks(runnable);
						Toast.makeText(getApplicationContext(), "Write Succeed", Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		});
    }
    
    /**
     * Record the Sensor Data and write them on a file in the SD Card.
     */
    private void recordSensor()
    {
    	if (sdCardDir == null)
    	{
    		/** SD Card not available */
        	Toast.makeText(getApplicationContext(), "SD Card Not Found", Toast.LENGTH_SHORT).show();
        	return;
    	}
    	
    	/** Store the data in '/sdcard0/LeapData/[LocalCalendarTime].csv' */
    	String path = sdCardDir + "/LeapData";
    	String name = "/" + Calendar.getInstance().getTime().toString() + ".csv";
    	
    	File fPath = new File(path);
    	File fName = new File(path + name);
    	
    	if (!fPath.exists())
    	{
    		fPath.mkdir();
    	}
    	
    	try {
    		fName.createNewFile();
    		fWriter = new FileWriter(fName);
    		
    		/** Write for each timeInterval ms */
    		sensorHandler = new Handler();
    		runnable = new Runnable() {
				
				@Override
				public void run() {
					/** Get data from the acceleration sensors */
					try {
						fWriter.write(String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime) + ",");
						fWriter.write("\r\n");
					} catch (IOException e) {
						Log.d("IO", "Write Failed");
						e.printStackTrace();
					}
					
					sensorHandler.postDelayed(this, (long)timeInterval);
				}
			};
			startTime = Calendar.getInstance().getTimeInMillis();
    		sensorHandler.postDelayed(runnable, (long)timeInterval);
			
    		/*FileWriter fw = new FileWriter(fName);
    		fw.write("1.0, 1, 2, 3");
    		fw.write("\r\n");
    		fw.write("2.0, 4, 5, 6");
    		fw.write("\r\n");
    		fw.close();
    		Toast.makeText(getApplicationContext(), "Write Succeed", Toast.LENGTH_SHORT).show();
    		*/
    	} catch (FileNotFoundException e) {
    		Toast.makeText(getApplicationContext(), "File Not Found", Toast.LENGTH_SHORT).show();
    	} catch (IOException e) {
    		Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
    	}
    }
}
