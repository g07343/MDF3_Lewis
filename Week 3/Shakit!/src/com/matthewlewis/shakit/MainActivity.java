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
import java.util.Random;

import com.matthewlewis.shakit.MusicService.LocalBinder;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor proximitySensor;
	static String[] songTitles;
	static String[] songPaths;
	String[] songLengths;
	int[] bgColors;
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
	static int playingInt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// force fullscreen and portrait so we can correctly detect shake
		// movements
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(R.layout.activity_main);

		// set up our text views
		nextTrack = (TextView) findViewById(R.id.nextTrack);
		previousTrack = (TextView) findViewById(R.id.previousTrack);
		currentlyPlaying = (TextView) findViewById(R.id.currentPlaying);

		// grab our buttons
		playPause = (ImageButton) findViewById(R.id.playPause_btn);
		next = (ImageButton) findViewById(R.id.next_btn);
		previous = (ImageButton) findViewById(R.id.previous_btn);

		isActive = false;

		// grab global context variable
		context = this;
		
		//grab our linearLayouts in MainActivity so that we can style them when the user interacts with them
		final LinearLayout playPauseView = (LinearLayout) findViewById(R.id.playPauseView);
		final LinearLayout previousView = (LinearLayout) findViewById(R.id.previousView);
		final LinearLayout nextView = (LinearLayout) findViewById(R.id.nextView);
		
		// grab all of the audio on the device, so we can set up our interface
		getAllAudio();

		// set up interface to display music found
		setSongDetails(0);

		// store the two arrays of information to shared preferences because
		// sometimes the service seems to "lose" it for some reason
		SharedPreferences prefs = context.getSharedPreferences(
				"com.matthewlewis.shakit", Context.MODE_PRIVATE);
		Editor editor = prefs.edit();

		try {
			editor.putString("songTitles",
					ObjectSerializer.serialize(songTitles));
			editor.putString("songPaths", ObjectSerializer.serialize(songPaths));
			editor.putInt("numSongs", songPaths.length);
			System.out
					.println("Data saved to shared preferences successfully!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		editor.commit();

		pauseReceiver = new PauseReceiver();
		this.registerReceiver(pauseReceiver, new IntentFilter(
				"com.matthewlewis.shakit.PauseReceiver"));

		notificationReceiver = new NotificationReceiver();
		this.registerReceiver(notificationReceiver, new IntentFilter(
				"com.matthewlewis.shakit.NotificationReceiver"));

		// get access to our sensor manager/sensors
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// check to ensure the user's device has the above sensors
		if (proximitySensor == null) {
			System.out.println("No proximity sensor detected!");
		} else {
			// register a listener to capture data from this sensor
			sensorManager.registerListener(this, proximitySensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		if (accelerometerSensor == null) {
			System.out.println("No accelerometer sensor detected!");
		} else {
			// register listener to capture data from this sensor
			sensorManager.registerListener(this, accelerometerSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
		// set an onclick listener for our play/pause button
		playPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Drawable currentImage = playPause.getDrawable();
				setRandomColor(playPauseView);
				
				if (currentImage.getConstantState().equals(
						getResources().getDrawable(R.drawable.play)
								.getConstantState())) {
					// the button was "play" so set to pause and start music
					// playback
					if (mService.nowPlaying == null) {
						mService.playSong(playingInt);
						System.out.println("playSong was called in service!");
					} else {
						mService.resumeMusic();
						System.out.println("resumeMusic was called in service!");
					}

					mService.buildNotification("play/pause");
					playPause.setImageResource(R.drawable.pause);
				} else {
					// the button was "pause" so set to play and pause current
					// playback
					mService.stopMusic();
					mService.buildNotification("play/pause");
					playPause.setImageResource(R.drawable.play);
				}
				//make sure to update widget when the play/pause button is tapped
				updateWidget();
			}
		});
		// set onclicklistener for our next button
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				playingInt++;
				mService.nextSong();
				Drawable currentIcon = playPause.getDrawable();
				//make sure to update our play button's icon if appropriate
				if (mService.musicPlayer.isPlaying()
						&& currentIcon.getConstantState().equals(
								getResources().getDrawable(R.drawable.play)
										.getConstantState())) {
					updatePlayBtn();
				}

				setSongDetails(playingInt);
				mService.buildNotification("play/pause");
				setRandomColor(nextView);
				
				//make sure to update widget when the next button is tapped
				updateWidget();
			}
		});
		// set onclicklistener for our previous button
		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				playingInt--;
				mService.previousSong();
				Drawable currentIcon = playPause.getDrawable();
				//make sure to update our play button's icon if appropriate
				if (mService.musicPlayer.isPlaying()
						&& currentIcon.getConstantState().equals(
								getResources().getDrawable(R.drawable.play)
										.getConstantState())) {
					updatePlayBtn();
				}
				setSongDetails(playingInt);
				mService.buildNotification("play/pause");
				setRandomColor(previousView);
				
				//make sure to update widget when the previous button is tapped
				updateWidget();
			}

		});
		
		//this seems to ensure that the device launcher is sure to update it's widgets section.
		//was having issues getting the widget to appear without a device reboot before
		sendBroadcast(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
		
	}

	//this function randomly picks a color and assigns it to the view passed
	public void setRandomColor (View view) {
		
		//set up our array of possible colors for styling our UI randomly when the user changes playback
				int[] bgColors = {Color.parseColor("#5041D600"), Color.parseColor("#50002BF8"), Color.parseColor("#50FFDF00"), Color.parseColor("#50FE2406"), 
				                  Color.parseColor("#50FFFFFF"), Color.parseColor("#50000000"), Color.parseColor("#50E98700"), Color.parseColor("#508200CF"), Color.parseColor("#50FBC4E3")};
				
		
		LinearLayout thisView = (LinearLayout) view;
		int randomNum = new Random().nextInt(bgColors.length);
		
		int randomColor = bgColors[randomNum];
		thisView.setBackgroundColor(randomColor);
	}
	
	//this function keeps our song tracking updated for the user interface (where it displays which song is playing out of how many)
	public void updateCount (int number) {
		int actualTrack = number +1;
		int totalSongs = songPaths.length;
		TextView songCounter = (TextView) findViewById(R.id.songCounter);
		songCounter.setText("Song " + actualTrack + " / " + totalSongs);
	}
	
	// this function keeps our app's interface data updated according to which
	// song is playing
	public void setSongDetails(int songNumber) {
		System.out.println("Song details called!");
		int arraySync = songTitles.length - 1;
		playingInt = songNumber;

		if (songNumber <= -1) {
			songNumber = songTitles.length - 1;
			playingInt = songNumber;
		} else if (songNumber > arraySync) {
			playingInt = 0;
		}

		int previousSong = playingInt - 1;
		int nextSong = playingInt + 1;

		if (previousSong <= -1) {
			previousSong = songTitles.length - 1;
		}

		if (nextSong == songTitles.length) {
			nextSong = 0;
		}
		currentlyPlaying.setText(songTitles[playingInt]);
		previousTrack.setText(songTitles[previousSong]);
		nextTrack.setText(songTitles[nextSong]);
		updateCount(playingInt);
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

	// this method is called within the onCreate() function, and basically
	// gathers a list of music that is contained on the user's device
	private void getAllAudio() {
		Cursor musicCursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.DATA }, null, null,
				"LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

		// grab the number of found items so we can create arrays of appropriate
		// size
		int numSongs = musicCursor.getCount();

		// if no music on device, supply the built in songs and info (just for
		// now)
		if (numSongs == 0) {
			songPaths = new String[3];
			songTitles = new String[3];

			songPaths[0] = "android.resource://" + getPackageName() + "/"
					+ R.raw.boogiewoogiebed;
			songPaths[1] = "android.resource://" + getPackageName() + "/"
					+ R.raw.sidewayssamba;
			songPaths[2] = "android.resource://" + getPackageName() + "/"
					+ R.raw.sk8board;

			songTitles[0] = "BoogieWoogieBed";
			songTitles[1] = "SidewaysSamba";
			songTitles[2] = "Sk8board";

		} else {
			// set up two string arrays, one to hold the name of each found
			// audio clip, and another to contain the filepaths
			songTitles = new String[numSongs];
			songPaths = new String[numSongs];
			songLengths = new String[numSongs];
		}

		int i = 0;
		// grab all of our music files and and store within arrays
		if (musicCursor.moveToFirst()) {
			do {

				songTitles[i] = musicCursor
						.getString(musicCursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
				songPaths[i] = musicCursor.getString(musicCursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

				// since our cursor is apparently unable to get the duration of
				// the media it finds, load each into a
				// "temporary" mediaPlayer, in order to grab the duration
				MediaPlayer testPlayer = new MediaPlayer();
				String musicLocation = musicCursor.getString(musicCursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				Uri currentLocation = Uri.parse(musicLocation);
				try {
					testPlayer.setDataSource(getApplicationContext(),
							currentLocation);
					testPlayer.prepare();

					float duration = testPlayer.getDuration() / 1000;
					songLengths[i] = Float.toString(duration);
					// System.out.println("Duration of audio is:  " + duration);
				} catch (IOException e) {
					System.out.println("Catch block triggered...");

					// for some reason, couldn't get the duration, so set to
					// unknown for checking later
					songLengths[i] = "unknown";
				}
				// get rid of our temporary media player object so we aren't
				// holding onto multiple instances needlessly
				testPlayer.reset();
				testPlayer.release();
				i++;

				// move our cursor object until we get to a new audio file
			} while (musicCursor.moveToNext());
		}

		// close our cursor object
		musicCursor.close();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	// default method provided for when our sensor data is updated
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
					
					//update our widget to match current state
					updateWidget();
					
					// need to grab our icon to ensure we toggle this only when
					// our button displayed
					// is the "pause" version and not play
					Drawable currentIcon = playPause.getDrawable();
					if (currentIcon.getConstantState().equals(
							getResources().getDrawable(R.drawable.pause)
									.getConstantState())) {
						updatePlayBtn();
					}
				}
			}
			// the below code attempts to detect when the user quickly turn the
			// phone left and right and will skip/or go back the current track
		} else if (event.sensor == accelerometerSensor) {
			long currentTime = System.currentTimeMillis();
			int threshHold = 9000000;

			if ((currentTime - lastUpdate) > 190) {

				long diffTime = (currentTime - lastUpdate);
				lastUpdate = currentTime;

				float x = event.values[0];
				float y = event.values[1];
				float z = event.values[2];

				float speed = Math.abs(x + y + z - last_x - last_y - last_z)
						/ diffTime * 10000;

				if (speed > threshHold && x > 5.8 && y < 5.7) {
					System.out.println("LEFT???");
					previous.performClick();
				} else if (speed > threshHold && x < -5.8 && y < 5.7) {
					System.out.println("RIGHT??");
					next.performClick();
				}

				last_x = x;
				last_y = y;
				last_z = z;

			}

		}
	}

	// this method changes the selected audio clip depending upon which
	// "direction" string it is passed.
	// when done, it reenables the sensor listeners
	public void changeTrack(String direction) {
		if (direction.equals("forward")) {
			// user shook device to the right, so skip to next audio file

		} else if (direction.equals("back")) {
			// user shook device to the left, so go to previous audio file

		} else {
			// random was activiated
		}
		// now that we've handled the gesture, re-add the listener to capture
		// future requests
		sensorManager.registerListener(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// from Google documentation, use this to bind our activity to our
			// service so we can communicate and access it's functions
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
		// get ready to start our service to handle music playback
		MusicService musicService = new MusicService();

		// now that the app has started, register listeners to the sensors we
		// need for gestures
		sensorManager.registerListener(this, proximitySensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		if (mService != null) {

			Intent startMusicService = new Intent(context,
					musicService.getClass());
			context.bindService(startMusicService, mConnection, 0);
			if (mService.notification == null) {
				mService.buildNotification("default");
			}

			// mBound = true;
		} else {
			//start up and bind to our service on start (no music without the service!)

			// create an intent to send to the service
			Intent startMusicService = new Intent(context,
					musicService.getClass());

			// add our list of file locations to the service via intent
			startMusicService.putExtra(MusicService.URI_ARRAY, songPaths);

			// add list of titles to the intent as well
			startMusicService.putExtra(MusicService.TITLE_ARRAY, songTitles);

			// start the service by binding to it
			context.bindService(startMusicService, mConnection,
					Context.BIND_AUTO_CREATE);

			System.out.println("Service was null");
			// mBound = true;
		}

	}
	
	//override the super method to (at least try) detect when the app is closed
	//Note: this isn't guaranteed to get called and only happens maybe 1 in 4 times
	@Override
	protected void onDestroy() {
		System.out.println("MainActivity Destroyed!");
		mService.buildNotification("destroy");
		if (mBound && mConnection != null) {
			try {
				mService.unbindService(mConnection);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			System.out.println("Attempting to unregister the receiver");
			this.unregisterReceiver(pauseReceiver);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.onDestroy();
	}

	//app has left the foreground, go ahead an get rid of receiver and update widget
	@Override
	protected void onPause() {
		if (mBound) {
			mService.songPaths = songPaths;
			mService.songTitles = songTitles;
		}
		if (pauseReceiver != null) {
			try {
				System.out.println("Attempting to unregister the receiver");
				this.unregisterReceiver(pauseReceiver);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateWidget();
		super.onPause();
	}

	//app is back, re-register receiver and update our interface to match MusicService
	@Override
	protected void onResume() {
		isActive = true;
		if (mBound) {
			if (mService.musicPlayer.isPlaying()) {
				playPause.setImageResource(R.drawable.pause);
			} else {
				playPause.setImageResource(R.drawable.play);
			}
			setSongDetails(mService.nowPlaying);
		}
		
		if (pauseReceiver != null) {
			try {
				System.out.println("Attempting to register the receiver");
				this.registerReceiver(pauseReceiver, new IntentFilter(
						"com.matthewlewis.shakit.PauseReceiver"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("ONRESUME RUNS");
		updateWidget();
		super.onResume();
	}

	@Override
	protected void onStop() {
		Intent pauseIntent = new Intent("com.matthewlewis.shakit.PauseReceiver");
		sendBroadcast(pauseIntent);

		if (this.isFinishing()) {
			System.out.println("Activity was finishing!");

		}

		// Unbind from the service
		if (mBound) {
			if (mConnection != null && mService != null) {
				// unbindService(mConnection);
				// mBound = false;

				// remove our listener for when the user covers proximity, since
				// the activity is no longer active
				sensorManager.unregisterListener(this, proximitySensor);

				// remove listener for motion gestures since activity is no
				// longer active
				sensorManager.unregisterListener(this, accelerometerSensor);
			}
		}

		super.onStop();
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

	//this function is called whenever the user interacts with the notification, and keeps
	//our UI's "play" button updated as appropriate
	public void updatePlayBtn() {
		
		//check if music player is currently playing
		if (mService.musicPlayer.isPlaying()) {
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

			Bundle extras = intent.getExtras();
			if (extras != null) {
				if (extras.containsKey("playing")) {
					
					//grab the int that was passed so we can update the UI to match the notification
					int newSong = extras.getInt("playing");
					main.updatePlayBtn();
					main.setSongDetails(newSong);
				} else if (extras.containsKey("notificationAction")) {
					main.updatePlayBtn();
				}
			}

		}

	}
	
	//this method is called whenever the user interacts with the UI and is just needed to keep our widget(s) updated
	public void updateWidget() {
		//grab widgetManager so we can be sure to keep our widget in sync with playback
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
				
		//grab MusicWidgetProvider 
		ComponentName widgetProvider = new ComponentName(getApplicationContext(), MusicWidgetProvider.class);
				
		//grab the array of widgetIds associated with our app to update them since the user changed our notification
		int[] widgetIds = widgetManager.getAppWidgetIds(widgetProvider);
				
				
		Intent updateWidget = new Intent(context, MusicWidgetProvider.class);
		updateWidget.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		sendBroadcast(updateWidget);
	}
}
