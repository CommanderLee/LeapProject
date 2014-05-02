package com.example.leapsensor;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	File		sdCardDir = null;
	TextView	tvSDMem;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /** Get Empty SD Memory Size */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
        	sdCardDir = Environment.getExternalStorageDirectory();
        	StatFs	fs = new StatFs(sdCardDir.getPath());
        	int blockSize, emptyBlock, emptySize;
        	blockSize = fs.getBlockSize();
        	emptyBlock = fs.getAvailableBlocks();
        	emptySize = blockSize * emptyBlock;
        	
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
