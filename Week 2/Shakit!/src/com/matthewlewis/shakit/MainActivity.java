package com.matthewlewis.shakit;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity implements SensorEventListener{

	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor proximitySensor;
	private Sensor linearAcceleration;
	String[] songTitles;
	String[] songPaths;
	String[] songLengths;
	private float firstDistance;
	private float newDistance;
	private long accelTime = 0;
	private long accelElapsed = 0;
	private float accelOriginalX = 0;
	private float accelNewX = 0;
	private float accelOriginalY = 0;
	private float accelNewY = 0;
	private long lastUpdate = -1;
	private float last_x, last_y, last_z;
	private boolean isBusy;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //force fullscreen and portrait so we can correctly detect shake movements
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        setContentView(R.layout.activity_main);
        
        //set up our boolean, which keeps double shake gestures from being recognized
        isBusy = false;
        
        //grab all of the audio on the device, so we can set up our interface
        getAllAudio();
        
        //get access to our sensor manager/sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        
        //check to ensure the user's device has the above sensors
        if (proximitySensor == null) {
        	System.out.println("No proximity sensor detected!");
        } else {
        	//register a listener to capture data from this sensor
        	sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        if (accelerometerSensor == null) {
        	System.out.println("No accelerometer sensor detected!");
        } else {
        	//register listener to capture data from this sensor
        	sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        if (linearAcceleration == null) {
        	System.out.println("No linear acceleration sensor detected!");
        } else {
        	sensorManager.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    //this method is called within the onCreate() function, and basically gathers a list of music that is contained on the user's device
    private void getAllAudio() {
        Cursor musicCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA }, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        //grab the number of found items so we can create arrays of appropriate size
        int numSongs = musicCursor.getCount();

        //set up two string arrays, one to hold the name of each found audio clip, and another to contain the filepaths
        songTitles = new String[numSongs];
        songPaths = new String[numSongs];
        songLengths = new String[numSongs];
        
        int i = 0;
        //grab all of our music files and and store within arrays
        if (musicCursor.moveToFirst()) {
            do {
            	
                songTitles[i] = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                songPaths[i] = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                
                //since our cursor is apparently unable to get the duration of the media it finds, load each into a
                //"temporary" mediaPlayer, in order to grab the duration
                MediaPlayer testPlayer = new MediaPlayer();
                String musicLocation = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                Uri currentLocation = Uri.parse(musicLocation);
                try{               	
                	testPlayer.setDataSource(getApplicationContext(), currentLocation);
                	testPlayer.prepare();
                	
                	float duration = testPlayer.getDuration() / 1000;
                	songLengths[i] = Float.toString(duration);
                	System.out.println("Duration of audio is:  " + duration);
                } catch (IOException e) {
                	System.out.println("Catch block triggered...");
                	
                	//for some reason, couldn't get the duration, so set to unknown for checking later
                	songLengths[i] = "unknown";
                }
                //get rid of our temporary media player object so we aren't holding onto multiple instances needlessly
                testPlayer.reset();
                testPlayer.release();
                i++;
                
                //move our cursor object until we get to a new audio file
            } while (musicCursor.moveToNext());
        }   

        //close our cursor object
        musicCursor.close();
    }


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	//default method provided for when our sensor data is updated
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor == proximitySensor) {
			if (firstDistance == 0.0f) {
				firstDistance = event.values[0];
			} else {
				newDistance = event.values[0];
			}
			if (newDistance != firstDistance) {
				System.out.println("User has covered proximity sensor!");
			}
			
		} else if (event.sensor == accelerometerSensor) {
			long currentTime = System.currentTimeMillis();
			int threshHold = 9250000;
			
			if ((currentTime - lastUpdate) > 190) {
				
				if (isBusy == false) {
					long diffTime = (currentTime - lastUpdate);
					lastUpdate = currentTime;
					
					float x = event.values[0];
					float y = event.values[1];
					float z = event.values[2];
					
					float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
					
					
					
					float calcFloat = (float)Math.pow(10, 4);
					x = x * calcFloat;
					float temp = Math.round(x);
					
					
					
					if (speed > threshHold && temp>10.0000) {
						System.out.println("LEFT");
						isBusy = true;
					} else if (speed > threshHold && temp<-10.0000) {
						System.out.println("RIGHT");
						isBusy = true;
					}
					
					last_x = x;
		            last_y = y;
		            last_z = z;
				} else {
					isBusy = false;
				}
			}
				
			
			
		} else if (event.sensor == linearAcceleration) {
			//System.out.println("Acceleration sensor updated!");
			
			float xValue = event.values[0];
			//System.out.println("Linear accel value of X is:  " + xValue);
		}
	}
}
