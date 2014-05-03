package com.example.leapsensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

	/** Objects on the Screen */
	TextView			tvSDMem;
	TextView			tvX, tvY, tvZ;
	EditText			etTimeInterval;
	Button				buttonStart, buttonEnd;
	
	SensorManager		sManager = null;
	SensorEventListener	myAccListener;
	File				sdCardDir = null;
	FileWriter			fWriter = null;
	long				startTime = 0;
	double				timeInterval;
	double				dataX, dataY, dataZ;
	Handler				sensorHandler = null;
	Runnable			runnable;
	int					fileNo;
	
	/** Set the range of the valid time interval */
	final double		MIN_TIME = 1.0;
	final double		MAX_TIME = 10.0;
	final double		DEFAULT_TIME = 5.0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fileNo = 0;
        
        detectSDCard();
        etTimeInterval = (EditText) findViewById(R.id.editTextTime);
        tvX = (TextView) findViewById(R.id.textViewSensorX);
        tvY = (TextView) findViewById(R.id.textViewSensorY);
        tvZ = (TextView) findViewById(R.id.textViewSensorZ);
        setSensorManager();
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
		if (sManager != null)
		{
			sManager.unregisterListener(myAccListener);
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
     * Set sensor manager and register listener
     */
    private void setSensorManager()
    {
    	sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	myAccListener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
				{
					/** Mark down the X,Y,Z data */
					dataX = event.values[0];
					dataY = event.values[1];
					dataZ = event.values[2];
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				Log.d("SENSOR", "Accuracy Changed");
			}
		};
    	// sManager.registerListener(myAccListener, sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
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
				if (fWriter == null || sensorHandler == null || sManager == null)
				{
					Toast.makeText(getApplicationContext(), "Not Started", Toast.LENGTH_SHORT).show();
				}
				else
				{
					/** Close the file writer and handler */
					try {
						fWriter.close();
						sensorHandler.removeCallbacks(runnable);
						sManager.unregisterListener(myAccListener);
						
						fWriter = null;
						sensorHandler = null;
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
    	String name = "/" + Calendar.getInstance().getTime().toString() + "-" + String.valueOf(fileNo) + ".csv";
    	
    	File fPath = new File(path);
    	File fName = new File(path + name);
    	
    	if (!fPath.exists())
    	{
    		fPath.mkdir();
    	}
    	
    	try {
    		fName.createNewFile();
    		fWriter = new FileWriter(fName);
    		++fileNo;
    		Log.d("SENSOR", String.valueOf(fileNo));
    		
    		/** Write for each timeInterval ms */
    		sensorHandler = new Handler();
    		runnable = new Runnable() {
				
				@Override
				public void run() {
					/** Get data from the acceleration sensors */
					try {
						/** Write them to file */
						fWriter.write(String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime) + ", " + String.valueOf(dataX) + ", "
								+ String.valueOf(dataY) + ", " + String.valueOf(dataZ));
						fWriter.write("\r\n");
						
						/** Display them on the screen */
						tvX.setText(String.valueOf(dataX));
						tvY.setText(String.valueOf(dataY));
						tvZ.setText(String.valueOf(dataZ));
					} catch (IOException e) {
						Log.d("IO", "Write Failed");
						e.printStackTrace();
					}
					
					sensorHandler.postDelayed(this, (long)timeInterval);
				}
			};
			startTime = Calendar.getInstance().getTimeInMillis();
			sManager.registerListener(myAccListener, sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
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
