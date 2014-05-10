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
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main Activity of the Leap Project on Android App.
 * @author Zhen Li
 * @version 2.0 May 10th 2014
 */
public class MainActivity extends Activity {

	/** Set the range of the valid time interval */
	/*final double		MIN_TIME = 1.0;
	final double		MAX_TIME = 10.0;
	final double		DEFAULT_TIME = 5.0;*/
	
	final int			MAX_RECORD_NUM = 100000;
	
	/** Objects on the Screen */
	LinearLayout		layout;
	TextView			tvSDMem;
	TextView			tvAccNo, tvGravityNo;
	/*EditText			etTimeInterval;
	Button				buttonStart, buttonEnd;*/
	
	/** Sensor Manager and Listener */
	SensorManager		sManager = null;
	SensorEventListener	mySensorListener;
	
	/** Files */
	File				sdCardDir = null;
	FileWriter			fWriter = null;
	long				startTime = 0;
/*	double				timeInterval;
	double				dataX, dataY, dataZ;
	
	Handler				sensorHandler = null;
	Runnable			runnable;
	Intent			intent = null;
	PendingIntent		pIntent = null;
	AlarmManager		alarm = null;*/
	String				openTimeFilePrefix;
	int					fileNo;
	
	/** Record the Data */
	boolean				isRecording;
	int					recordAccNo, recordGravityNo;
	double[][]			recordAccData, recordGravityData;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detectSDCard();
        
        layout = (LinearLayout) findViewById(R.id.layoutTotal);
        // etTimeInterval = (EditText) findViewById(R.id.editTextTime);
        tvAccNo = (TextView) findViewById(R.id.textViewAccNo);
        tvGravityNo = (TextView) findViewById(R.id.textViewGravityNo);
        
        openTimeFilePrefix = Calendar.getInstance().getTime().toString();
        fileNo = 0;       
        isRecording = false;
        recordAccNo = 0;
        recordGravityNo = 0;
        //recordAccData = new double [MAX_RECORD_NUM] [4];
        
        setSensorManager();
        setTouchListener();
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
		/*if (sensorHandler != null)
		{
			sensorHandler.removeCallbacks(runnable);
		}*/
/*		if (alarm != null)
		{
			alarm.cancel(pIntent);
		}*/
		if (sManager != null)
		{
			sManager.unregisterListener(mySensorListener);
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
    	mySensorListener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION && isRecording)
				{
					if (recordAccNo == MAX_RECORD_NUM)
					{
						Log.d("RECORD", "Acc: Memory Limit Exceeded");
						return ;
					}
					/** Mark down the Acceleration X,Y,Z data */
					recordAccData[recordAccNo][0] = Calendar.getInstance().getTimeInMillis() - startTime;
					for (int i = 0; i < 3; ++i)
					{
						recordAccData[recordAccNo][i + 1] = event.values[i];
					}
					++recordAccNo;
					if (recordAccNo % 10 == 0)
						tvAccNo.setText(String.valueOf(recordAccNo));
/*					dataX = event.values[0];
					dataY = event.values[1];
					dataZ = event.values[2];*/
				}
				else if (event.sensor.getType() == Sensor.TYPE_GRAVITY && isRecording)
				{
					if (recordGravityNo == MAX_RECORD_NUM)
					{
						Log.d("RECORD", "Gravity: Memory Limit Exceeded");
						return ;
					}
					/** Mark down the Gravity Sensor X,Y,Z data */
					recordGravityData[recordGravityNo][0] = Calendar.getInstance().getTimeInMillis() - startTime;
					for (int i = 0; i < 3; ++i)
					{
						recordGravityData[recordGravityNo][i + 1] = event.values[i];
					}
					++recordGravityNo;
					if (recordGravityNo % 10 == 0)
						tvGravityNo.setText(String.valueOf(recordGravityNo));
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				Log.d("SENSOR", "Accuracy Changed");
			}
		};
		sManager.registerListener(mySensorListener, sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
		sManager.registerListener(mySensorListener, sManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    /**
     * Set Listener of the Start / End Button.
     * Check the range of the Time Interval.
     */
    private void setTouchListener()
    {
    	layout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction())
				{
				/** Start recording */
				case MotionEvent.ACTION_DOWN:
					if (!isRecording)
					{
						Toast.makeText(getApplicationContext(), "Start!", Toast.LENGTH_SHORT).show();
						startTime = Calendar.getInstance().getTimeInMillis();
						
						recordAccNo = 0;
						recordAccData = new double [MAX_RECORD_NUM] [4];
						
						recordGravityNo = 0;
						recordGravityData = new double [MAX_RECORD_NUM] [4];
						
						isRecording = true;
					}
					break;
				/** End recording and write into file. */
				case MotionEvent.ACTION_UP:
					if (isRecording)
					{
						writeRecord();
						isRecording = false;
					}
					break;
				}
				return true;
			}
		});
/*    	buttonStart = (Button) findViewById(R.id.buttonStart);
    	buttonStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				*//** Check the Time Interval *//*
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
				else if (fWriter != null)
				{
					Toast.makeText(getApplicationContext(), "Already started", Toast.LENGTH_SHORT).show();
				}
				else
				{
					*//** Start the real work *//*
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
				//if (fWriter == null || sManager == null || alarm == null)
				{
					Toast.makeText(getApplicationContext(), "Not Started", Toast.LENGTH_SHORT).show();
				}
				else
				{
					*//** Close the file writer and handler *//*
					try {
						fWriter.close();
						sensorHandler.removeCallbacks(runnable);
						sManager.unregisterListener(mySensorListener);
						//alarm.cancel(pIntent);
						
						fWriter = null;
						//alarm = null;
						sensorHandler = null;
						Toast.makeText(getApplicationContext(), "Write Succeed", Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		});*/
    }
    
    /**
     * Write the data on a file in the SD Card.
     */
    private void writeRecord()
    {
    	if (sdCardDir == null)
    	{
    		/** SD Card not available */
        	Toast.makeText(getApplicationContext(), "SD Card Not Found", Toast.LENGTH_SHORT).show();
        	return;
    	}
    	
    	/** Store the data in '/sdcard0/LeapData/[CreateTime]-[fileNo].csv' */
    	String path = sdCardDir + "/LeapData";
    	String name = "/" + openTimeFilePrefix + "-" + String.valueOf(fileNo) + ".csv";
    	
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
    		Log.d("WRITE", "Create files: " + String.valueOf(fileNo));
    		
    		fWriter.write("AccTime, AccX, AccY, AccZ, GravityTime, GravityX, GravityY, GravityZ\r\n");
    		
    		int minRecord = Math.min(recordAccNo, recordGravityNo);
    		for (int i = 0; i < minRecord; ++i)
    		{
    			for (int j = 0; j < 4; ++j)
    			{
    				fWriter.write(String.valueOf(recordAccData[i][j]) + ", ");
    			}
    			
    			for (int j = 0; j < 3; ++j)
    			{
    				fWriter.write(String.valueOf(recordGravityData[i][j]) + ", ");
    			}
    			fWriter.write(String.valueOf(recordGravityData[i][3]) + "\r\n");
    		}
    		
    		if (minRecord < recordAccNo)
    		{
    			for (int i = minRecord; i < recordAccNo; ++i)
        		{
        			for (int j = 0; j < 4; ++j)
        			{
        				fWriter.write(String.valueOf(recordAccData[i][j]) + ", ");
        			}
        			
        			fWriter.write("-1, -1, -1, -1\r\n");
        		}
    		}
    		if (minRecord < recordGravityNo)
    		{
    			for (int i = minRecord; i < recordGravityNo; ++i)
        		{
        			fWriter.write("-1, -1, -1, -1, ");
        			
        			for (int j = 0; j < 3; ++j)
        			{
        				fWriter.write(String.valueOf(recordGravityData[i][j]) + ", ");
        			}
        			fWriter.write(String.valueOf(recordGravityData[i][3]) + "\r\n");
        		}
    		}
    		
    		fWriter.close();
    		Toast.makeText(getApplicationContext(), "Write Succeed", Toast.LENGTH_SHORT).show();
    		
    		/** Write for each timeInterval ms */
/*    		intent = new Intent(this, MyBroadcastReceiver.class);
    		pIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
    		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
    		*/
/*    		sensorHandler = new Handler();
    		runnable = new Runnable() {
				
				@Override
				public void run() {
					*//** Get data from the acceleration sensors *//*
					try {
						*//** Write them to file *//*
						fWriter.write(String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime) + ", " + String.valueOf(dataX) + ", "
								+ String.valueOf(dataY) + ", " + String.valueOf(dataZ));
						fWriter.write("\r\n");
						
						*//** Display them on the screen *//*
						tvAccX.setText(String.valueOf(dataX));
						tvAccY.setText(String.valueOf(dataY));
						tvAccZ.setText(String.valueOf(dataZ));
					} catch (IOException e) {
						Log.d("IO", "Write Failed");
						e.printStackTrace();
					}
					
					sensorHandler.postDelayed(this, (long)timeInterval);
				}
			};
			startTime = Calendar.getInstance().getTimeInMillis();
			sManager.registerListener(mySensorListener, sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    		sensorHandler.postDelayed(runnable, (long)timeInterval);*/
			//alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (long)timeInterval, pIntent);
			
    	} catch (FileNotFoundException e) {
    		Toast.makeText(getApplicationContext(), "File Not Found", Toast.LENGTH_SHORT).show();
    	} catch (IOException e) {
    		Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
    	}
    }
    
/*    public class MyBroadcastReceiver extends BroadcastReceiver
    {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				*//** Write them to file *//*
				fWriter.write(String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime) + ", " + String.valueOf(dataX) + ", "
						+ String.valueOf(dataY) + ", " + String.valueOf(dataZ));
				fWriter.write("\r\n");
				
				*//** Display them on the screen *//*
				tvX.setText(String.valueOf(dataX));
				tvY.setText(String.valueOf(dataY));
				tvZ.setText(String.valueOf(dataZ));
			} catch (IOException e) {
				Log.d("IO", "Write Failed");
				e.printStackTrace();
			}
		}
    	
    }*/
}
