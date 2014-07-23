/*
 * Author Matthew Lewis
 * 
 * Project Shakit!
 * 
 * Package com.matthewlewis.shakit
 * 
 * File MusicService.java
 * 
 * Purpose MusicService is basically in charge of music playback and keeping a notification up to date within the notification pane.  
 * It communicates back and forth with MainActivity to keep information updated, so the user can control playback from the notification,
 * or from within the app.
 * 
 */
package com.matthewlewis.shakit;

import java.io.IOException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service{
	
	private final IBinder serviceBinder = new LocalBinder();
	public static final String MESSENGER_KEY = "messenger";
	public static final String URI_ARRAY = "uriList";
	public static final String TITLE_ARRAY = "titleList";
	public static final String PAUSE = "pause";
	public static final String PLAY = "play";
	public static final String NEXT = "next";
	public static final String NOTIFICATION_ACTION = "action";
	public static final String BACK = "back";
	private final int TXT_NOTIFICATION_ID = 1;
	
	Notification notification;
	NotificationManager nm;
	long startTime;
	Integer nowPlaying;
	String[] songPaths;
	String[] songTitles;
	MediaPlayer musicPlayer;
	boolean isPaused;
	boolean activityAlive;
	boolean timerToggle;
	
	public class LocalBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}
	
	public MusicService() {
		super();
		// TODO Auto-generated constructor stub
	} 

	//this method receives actions from anything the user does with our notification, and responds to keep
	//MusicService updated, as well as communicating to MainActivity to keep our interface updated as well.
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		//create an intent for our notification broadcast, which communicates to MainActivity
		Intent notificationIntent = new Intent("com.matthewlewis.shakit.NotificationReceiver");

		//the below logic checks what action was sent via the intent and then updates the Service accordingly
		if (musicPlayer != null) {
			if (intent.getAction().equals("Pause")) {
				//if music player is currently playing, pause it, otherwise resume playback
				if (musicPlayer.isPlaying()) {					
					stopMusic();	
					isPaused = true;
				} else {
					resumeMusic();
					isPaused = false;
				}
				notificationIntent.putExtra("notificationAction", "Pause");
				buildNotification("play/pause");

			} else if (intent.getAction().equals("Next")) {
				nextSong();
				System.out.println("NEXT");
				buildNotification("play/pause");
				notificationIntent.putExtra("playing", nowPlaying);
			} else if (intent.getAction().equals("Previous")) {
				previousSong();
				buildNotification("play/pause");
				notificationIntent.putExtra("playing", nowPlaying);
			} else if (intent.getAction().equals("Stop")) {
				if (musicPlayer != null) {
					if (musicPlayer.isPlaying()) {
						notificationIntent.putExtra("notificationAction", "Pause");
						musicPlayer.stop();
					}
					
					
					notification = null;
					this.stopSelf();
				}

			}
		} else { 
			//buildNotification("destroy");
		}
		//send our broadcast to MainActivity to let it know what was done
		sendBroadcast(notificationIntent);
		return super.onStartCommand(intent, flags, startId);
	}

	//this function skips to the next song in our array after checking to make sure we
	//are within the bounds of our array
	public void nextSong () {
		int numSongs = songPaths.length -1;
		int nextSong = nowPlaying +1;
		
		System.out.println("NEXT Array length total is:  " + numSongs);
		System.out.println("NEXT Song should be:  " + nextSong);
		
		if (nextSong > numSongs) {
			nextSong = 0;
			playSong(nextSong);
		} else  {
			playSong(nextSong);
		}
	}

	//this function goes to the previous song in our array after checking to make sure we
		//are within the bounds of our array
	public void previousSong() {
		int numSongs = songPaths.length -1;
		int prevSong = nowPlaying -1;
		System.out.println("PREV Array length total is:  " + numSongs);
		System.out.println("PREV Song should be:  " + prevSong);
		
		if (prevSong <= -1) {
			prevSong = songPaths.length -1;
			playSong(prevSong);
		} else {
			playSong(prevSong);
		}
	}
	
	//this function recreates our array if it was destroyed somehow
	public void restoreNotification(Notification notification) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(TXT_NOTIFICATION_ID, notification);
	}

	//this function is what binds our service to MainActivity
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("ON_BIND", "Music service started.");
		// android.os.Debug.waitForDebugger();

		//set up our boolean, which we set back and forth to true/false allowing us to determine if MainActivity
		//is for some reason detroyed
		activityAlive = true;
		
		//set up toggle boolean to give broadcast more time to be set
		timerToggle = false;
		
		//set up our global music player instance
		musicPlayer = new MediaPlayer();
		
		//set paused boolean to true by default
		isPaused = true;
		
		//set up an onCompletionListener so we know when it finishes and can reset our notification
		musicPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				isPaused = true;
				buildNotification("play/pause");
				Intent notificationIntent = new Intent("com.matthewlewis.shakit.NotificationReceiver");
				notificationIntent.putExtra("notificationAction", "Pause");
				sendBroadcast(notificationIntent);
			}
			
		});
		//grab the current time so we don't accidentally turn off the sound at launch 
		//(for some reason, the app's onPause fires when it is first launched"
		startTime = System.currentTimeMillis();

		// grab our extras data
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(URI_ARRAY)) {
			songPaths = extras.getStringArray(URI_ARRAY);
			songTitles = extras.getStringArray(TITLE_ARRAY);
			int songNumber = extras.getInt("number");
			nowPlaying = songNumber;
		}

		// set the default data source for mediaplayer, just in case the user
		// wants to start playback from notification
		Uri defaultSong = Uri.parse(songPaths[0]);
		try {
			musicPlayer.setDataSource(getApplicationContext(), defaultSong);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create the notification default
		buildNotification("default");

		return serviceBinder;
	}

	//this function is what handles sound playback based on an integer passed
	public void playSong(int songPlace) {
		//in rare almost random cases, our data is lost when user leaves MainActivity, so grab it from shared preferences
		if (songPaths == null) {
			SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.matthewlewis.shakit", Context.MODE_PRIVATE);
	        
			try {
				int numSongs = prefs.getInt("numSongs", 0);
				songPaths = (String[]) ObjectSerializer.deserialize(prefs.getString("songPaths", ObjectSerializer.serialize(new String[numSongs])));
				songTitles = (String[]) ObjectSerializer.deserialize(prefs.getString("songTitles", ObjectSerializer.serialize(new String[numSongs])));
				System.out.println("Data retrieved from shared preferences successfully!");
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		//grab the location of the file in reference to the int passed
		String testSongLocation = songPaths[songPlace];
		
		//set our global int to match what was passed
		nowPlaying = songPlace;
		System.out.println("NOW PLAYING IS:  " + nowPlaying);
		
		//if we already had a musis player, reset it in preperation for playing a new clip
		if (musicPlayer != null) {
			musicPlayer.reset();
		} else {
			musicPlayer = new MediaPlayer();
		}
        
		//parse the uri string into a URI
        Uri currentLocation = Uri.parse(testSongLocation);
        try{
        	//attempt to set the player's data source and play
        	musicPlayer.setDataSource(getApplicationContext(), currentLocation);
        	musicPlayer.prepare();
        	musicPlayer.start();
        	
        } catch (IOException e) {
        	System.out.println("Catch block triggered...");
        	
        }
        //make sure the music player successfully loaded and began playing from file
        if (musicPlayer.isPlaying()) {
        	//create a notification that a user can use to control playback from
        	//outside app interface
        	
        }
        //set our playing boolean to false, since musicPlayer is now playing
        isPaused = false;
	}
	
	//this function handles stopping the playback of music
	public void stopMusic() {
		long currentTime = System.currentTimeMillis();
		
		//only pause if we have been running for a certain amount of time
		//this keeps the proximity sensor "mute" function from running immediately at startup
		if (currentTime - startTime > 1000) {
			if (musicPlayer != null && notification != null) {
				musicPlayer.pause();
				isPaused = true;
			} else {
				System.out.println("music player was null");
			}
		}		
	}
	
	//this function simply resumes music playback
	public void resumeMusic() {
		System.out.println("track is:  " + nowPlaying);
		//check to make sure musicplayer is valid
		if (musicPlayer != null && nowPlaying != null ) {
			try {
				musicPlayer.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			musicPlayer.start();
			isPaused = false;
		} else {
			nowPlaying = 0;
			playSong(0);
		}
	}

	//this function serves to create and constantly update our notification when the user interacts with the app's interface,
	//or the old notification instance itself
	public void buildNotification (String type) {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		if (songTitles != null) {
			
		}
		
		//create a basic intent to pause
    	Intent basicPause = new Intent(this, MusicService.class);
    	basicPause.setAction("Pause");
    	
    	//basic intent to skip
    	Intent basicNext = new Intent(this, MusicService.class);
    	basicNext.setAction("Next");
    	
    	//basic intent to return to previous song
    	Intent basicPrevious = new Intent(this, MusicService.class);
    	basicPrevious.setAction("Previous");
    	
    	//create an intent to close out the notification and stop music playback
    	Intent basicClose = new Intent(this, MusicService.class);
    	basicClose.setAction("Stop");
    	
    	//create different pending intents to apply to our control buttons
    	PendingIntent pausePending = PendingIntent.getService(this, 0, basicPause, 0);
    	PendingIntent nextPending = PendingIntent.getService(this, 0, basicNext, 0);
    	PendingIntent previousPending = PendingIntent.getService(this, 0, basicPrevious, 0);
    	PendingIntent closePending = PendingIntent.getService(this, 0, basicClose, 0);
 
    	if (notification == null) {
    		notification = new Notification();
    	}
    	
    	//create a notification builder
    	Notification.Builder notificationBuilder = new Notification.Builder(this);
    	notificationBuilder.setSmallIcon(R.drawable.app_icon);
    	notificationBuilder.setContentTitle(songTitles[nowPlaying]);		
    	notificationBuilder.addAction(R.drawable.back_small, "", previousPending);
		
		
		//service is being destroyed likely because app was closed, so get rid of persistent notification
		if (type.equals("destroy")) {
			nm.cancel(TXT_NOTIFICATION_ID);
		} else if (type.equals("play/pause")) {
			if (isPaused == true) {
				//music is currently paused, so set icon to play instead
				notificationBuilder.addAction(R.drawable.play_small, "", pausePending);
				
			} else {
				//music is currently playing, so set icon to pause
				notificationBuilder.addAction(R.drawable.pause_small, "", pausePending);
				
			}
		} else if (type.equals("default")) {
			notificationBuilder.addAction(R.drawable.play_small, "", pausePending);			
		}
		
		//add this one last since builder puts icons in order they were added	
    	notificationBuilder.addAction(R.drawable.next_small, "", nextPending);
    	
    	//notificationBuilder.setContentIntent(openActivityPending);
    	notificationBuilder.setDeleteIntent(closePending);
    	notification = notificationBuilder.build();  
    	if (!(type.equals("destroy"))) {
    		nm.notify(TXT_NOTIFICATION_ID, notification);
    	}    	
	}
}
