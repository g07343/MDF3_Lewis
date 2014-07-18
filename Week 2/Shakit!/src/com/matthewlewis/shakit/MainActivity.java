/*
 * Author Matthew Lewis
 * 
 * Project Shakit!
 * 
 * Package com.matthewlewis.shakit
 * 
 * File MainActivity.java
 * 
 * Purpose MainActivity is responsible for displaying the playing tracks to the user and giving them controls to pause, play, advance to next
 * track, and return to the previous track.  It communicates back and forth with the MusicService service to maintain an updated notification within
 * the notification pane.
 * 
 */
package com.matthewlewis.shakit;

import java.io.IOException;
import com.matthewlewis.shakit.MusicService.LocalBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener{

	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor proximitySensor;
	String[] songTitles;
	String[] songPaths;
	String[] songLengths;
	private float firstDistance;
	private float newDistance;
	private long lastUpdate = -1;
	private float last_x, last_y, last_z;
	TextView xField;
	TextView yField;
	TextView zField;
	Context context;
	static MusicService mService;
	boolean mBound = false;
	boolean isActive;
	PauseReceiver pauseReceiver;
	NotificationReceiver notificationReceiver;
	TextView nextTrack;
	TextView previousTrack;
	TextView currentlyPlaying;
	ImageButton playPause;
	ImageButton next;
	ImageButton previous;
	int playingInt;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //force fullscreen and portrait so we can correctly detect shake movements
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        
        setContentView(R.layout.activity_main);
        
        //set up our text views
        nextTrack = (TextView) findViewById(R.id.nextTrack);
        previousTrack = (TextView) findViewById(R.id.previousTrack);
        currentlyPlaying = (TextView) findViewById(R.id.currentPlaying);
        
        //grab our buttons
        playPause = (ImageButton) findViewById(R.id.playPause_btn);
        next = (ImageButton) findViewById(R.id.next_btn);
        previous = (ImageButton) findViewById(R.id.previous_btn);        
        
        isActive = true;
               
        //grab global context variable
        context = this;       
                
        //grab all of the audio on the device, so we can set up our interface
        getAllAudio();
             
        //set up interface to display music found
        setSongDetails(0);
        
        
        pauseReceiver = new PauseReceiver();
        this.registerReceiver(pauseReceiver, new IntentFilter("com.matthewlewis.shakit.PauseReceiver"));
        
        notificationReceiver = new NotificationReceiver();
        this.registerReceiver(notificationReceiver, new IntentFilter("com.matthewlewis.shakit.NotificationReceiver"));
        
        //get access to our sensor manager/sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
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
        //set an onclick listener for our play/pause button
        playPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Drawable currentImage = playPause.getDrawable();
				
				if (currentImage.getConstantState().equals(getResources().getDrawable(R.drawable.play).getConstantState())) {
					//the button was "play" so set to pause and start music playback
					mService.playSong(playingInt);
					mService.buildNotification("play/pause");
					playPause.setImageResource(R.drawable.pause);
				} else {
					//the button was "pause" so set to play and pause current playback
					mService.stopMusic();
					mService.buildNotification("play/pause");
					playPause.setImageResource(R.drawable.play);
				}				
			}
        	
        });
        //set onclicklistener for our next button
        next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				playingInt ++;
				mService.nextSong();
				setSongDetails(playingInt);
			}
        	
        }); 
        //set onclicklistener for our previous button
        previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				playingInt --;
				mService.previousSong();
				setSongDetails(playingInt);
			}
        	
        }); 
    }

    
    //this function keeps our app's interface data updated according to which song is playing
    public void setSongDetails (int songNumber) {
    	System.out.println("Song details called!");
    	int arraySync = songTitles.length -1;
    	playingInt = songNumber;
    	
    	if (songNumber <= -1) {
    		songNumber = songTitles.length -1;
    		playingInt = songNumber;
    	} else if (songNumber > arraySync) {
    		playingInt = 0;
    	}
     	
    	int previousSong = playingInt -1;
    	int nextSong = playingInt +1;
    	    	
    	if (previousSong <= -1) {
    		previousSong = songTitles.length -1;
    	}
    	
    	if (nextSong == songTitles.length) {
    		nextSong = 0;
    	}
    	currentlyPlaying.setText(songTitles[playingInt]);
    	previousTrack.setText(songTitles[previousSong]);
    	nextTrack.setText(songTitles[nextSong]);
    System.out.println();	
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
        
        

       
        
        //if no music on device, supply the build in songs and info (just for now)
        if (numSongs == 0) {
        	songPaths = new String[3];
        	songTitles = new String[3];
        	
        	songPaths[0] = "android.resource://" + getPackageName() + "/" + R.raw.boogiewoogiebed;
        	songPaths[1] = "android.resource://" + getPackageName() + "/" + R.raw.sidewayssamba;
        	songPaths[2] = "android.resource://" + getPackageName() + "/" + R.raw.sk8board;
        	
        	songTitles[0] = "BoogieWoogieBed";
        	songTitles[1] = "SidewaysSamba";
        	songTitles[2] = "Sk8board";
        	
        } else {
        	 //set up two string arrays, one to hold the name of each found audio clip, and another to contain the filepaths
            songTitles = new String[numSongs];
            songPaths = new String[numSongs];
            songLengths = new String[numSongs];
        }
        
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
                	//System.out.println("Duration of audio is:  " + duration);
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
				if (mBound && mService != null) {
					mService.stopMusic();
					mService.buildNotification("play/pause");
				}
			}
			//the below code attempts to detect when the user quickly turn the phone left and right and will skip/or go back the current track
		} else if (event.sensor == accelerometerSensor) {
			long currentTime = System.currentTimeMillis();
			int threshHold = 9000000;
			
			if ((currentTime - lastUpdate) > 190) {
				
					long diffTime = (currentTime - lastUpdate);
					lastUpdate = currentTime;
					
					float x = event.values[0];
					float y = event.values[1];
					float z = event.values[2];
					
					
					float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
					
					if (speed > threshHold && x > 5.8 && y < 5.7) {
						System.out.println("LEFT???");
						System.out.println("X was:  " + x + " and Y was:  " + y);
					} else if (speed > threshHold && x < -5.8 && y < 5.7) {
						System.out.println("RIGHT??");
						System.out.println("X was:  " + x + " and Y was:  " + y);
					}
					
					float calcFloat = (float)Math.pow(10, 4);
					x = x * calcFloat;
					float temp = Math.round(x);
					
					//System.out.println("Temp was:  " + temp);
					
					if (speed > threshHold && temp>10.0000) {
						//System.out.println("LEFT");
			        	sensorManager.unregisterListener(this, accelerometerSensor);
			        	changeTrack("back");
			        	
			
					} else if (speed > threshHold && temp<-10.0000) {
						//System.out.println("RIGHT");
			        	sensorManager.unregisterListener(this, accelerometerSensor);
			        	changeTrack("forward");
			        	
					}
					
					last_x = x;
		            last_y = y;
		            last_z = z;
		            
			}
				
			
			
		} 
	}
	
	//this method changes the selected audio clip depending upon which "direction" string it is passed.  
	//when done, it reenables the sensor listeners
	public void changeTrack(String direction) {
		if (direction.equals("forward")) {
			//user shook device to the right, so skip to next audio file
			
		} else if (direction.equals("back")) {
			//user shook device to the left, so go to previous audio file
			
		} else {
			//random was activiated
		}
		//now that we've handled the gesture, re-add the listener to capture future requests
		sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			//from Google documentation, use this to bind our activity to our service so we can communicate and access it's functions
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	@Override
    protected void onStart() {
        super.onStart();
      //get ready to start our service to handle music playback
        MusicService musicService = new MusicService();
        
        //now that the app has started, register listeners to the sensors we need for gestures
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (mService != null) {
        	
        	Intent startMusicService = new Intent(context, musicService.getClass());
        	context.bindService(startMusicService, mConnection, 0);
        	if (mService.notification == null) {
        		mService.buildNotification("default");
        	}
        	
        	mBound = true;
        } else {
        	//this all needs to be moved once the interface has been built out, since this basically runs automatically when the app launches (BAD!)
            
            
            //create an intent to send to the service
            Intent startMusicService = new Intent(context, musicService.getClass());       
            
            //add our list of file locations to the service via intent
            startMusicService.putExtra(MusicService.URI_ARRAY, songPaths);
            
            //add list of titles to the intent as well
            startMusicService.putExtra(MusicService.TITLE_ARRAY, songTitles);
            
            //add a track to play
            //startMusicService.putExtra("number", 0);
            
            //start the service by binding to it          
            context.bindService(startMusicService, mConnection, Context.BIND_AUTO_CREATE);
            
            System.out.println("Service was null");
            mBound = true;
        }
        
    }
	
	
	@Override
	protected void onDestroy() {
		System.out.println("MainActivity Destroyed!");
		mService.buildNotification("destroy");
		if (mBound) {
			mService.unbindService(mConnection);
		}
		
		try {
			this.unregisterReceiver(pauseReceiver);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		System.out.println("OnPause called.................");

		super.onPause();
	}

	@Override
	protected void onStop() {
		Intent pauseIntent = new Intent("com.matthewlewis.shakit.PauseReceiver");
		sendBroadcast(pauseIntent);

		super.onStop();

		if (this.isFinishing()) {
			System.out.println("Activity was finishing!");

		}

		// Unbind from the service
		if (mBound) {
			if (mConnection != null && mService != null) {
				unbindService(mConnection);
				mBound = false;

				// remove our listener for when the user covers proximity, since
				// the activity is no longer active
				sensorManager.unregisterListener(this, proximitySensor);

				// remove listener for motion gestures since activity is no
				// longer active
				sensorManager.unregisterListener(this, accelerometerSensor);
			}
		}
	}

	// we use this receiver to be informed of when the user taps the "pause"
	// button from the notification.
	// While MusicService pauses itself, we need to update the notification to
	// use a "play" button if paused, and vice versa
	public static class PauseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("onReceive");
			SharedPreferences prefs = context.getSharedPreferences(
					"com.matthewlewis.shakit", Context.MODE_PRIVATE);
			prefs.edit().putBoolean("isAlive", true).apply();

		}

	}

	public void updatePlayBtn() {
		Drawable current = playPause.getDrawable();
		if (current.getConstantState().equals(
				getResources().getDrawable(R.drawable.play).getConstantState())) {
			playPause.setImageResource(R.drawable.pause);
		} else {
			playPause.setImageResource(R.drawable.play);
		}
	}

	// we use this to receive information from MusicClass as to when and how the
	// user interacts with the notification
	public static class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			MainActivity main = ((MainActivity) context);
			// TODO Auto-generated method stub
			System.out.println("NOTIFICATION RECEIVER");
			Bundle extras = intent.getExtras();
			if (extras.containsKey("playing")) {
				// check passed string so we know what the user did
				int newSong = extras.getInt("playing");

				main.setSongDetails(newSong);
			} else if (extras.containsKey("notificationAction")) {
				main.updatePlayBtn();
			}
		}

	}

}
