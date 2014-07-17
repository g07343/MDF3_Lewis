package com.matthewlewis.shakit;

import java.io.IOException;
import java.util.Arrays;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
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
	BroadcastReceiver pauseReceiver;
	long startTime;
	Integer nowPlaying;
	String[] songPaths;
	String[] songTitles;
	static MediaPlayer musicPlayer;
	boolean isPaused;
	
	public class LocalBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}
	
	public MusicService() {
		super();
		// TODO Auto-generated constructor stub
	} 



	


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (musicPlayer != null) {
			if (intent.getAction().equals("Pause")) {

				if (musicPlayer.isPlaying()) {
					System.out.println("Pause fired from passed intent");
					stopMusic();

				} else {
					musicPlayer.start();
					isPaused = false;
				}
				buildNotification("play/pause");

			} else if (intent.getAction().equals("Next")) {
				int numSongs = URI_ARRAY.length() -1;
				int nextSong = nowPlaying +1;
				
				
				if (nextSong >= numSongs) {
					nextSong = 0;
					playSong(nextSong);
				} else  {
					playSong(nextSong);
				}
				buildNotification("default");
			} else if (intent.getAction().equals("Previous")) {
				int numSongs = URI_ARRAY.length() -1;
				int prevSong = nowPlaying -1;
				System.out.println("Array length total is:  " + numSongs);
				System.out.println("Next Song should be:  " + prevSong);
				
				if (prevSong <= -1) {
					prevSong = URI_ARRAY.length() -2;
					playSong(prevSong);
				} else {
					playSong(prevSong);
				}
				buildNotification("default");
			} else if (intent.getAction().equals("Stop")) {
				if (musicPlayer != null) {
					musicPlayer.stop();
					musicPlayer.reset();
					musicPlayer.release();
					this.stopSelf();
				}

			}
		} else { 
			buildNotification("destroy");
		}
		
		
		return super.onStartCommand(intent, flags, startId);
	}


	public void restoreNotification(Notification notification) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(TXT_NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		System.out.println("Service onDestroy function runs");
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("ON_BIND", "Music service started.");
		// android.os.Debug.waitForDebugger();

		//set up our global music player instance
		musicPlayer = new MediaPlayer();
		
		//set up an onCompletionListener so we know when it finishes and can reset our notification
		musicPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				isPaused = true;
				buildNotification("play/pause");
			}
			
		});
		
		startTime = System.currentTimeMillis();

		pauseReceiver = new PauseReceiver();
        this.registerReceiver(new PauseReceiver(), new IntentFilter("com.matthewlewis.shakit.PauseReceiver"));
		
		// grab our extras data
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(URI_ARRAY)) {
			songPaths = extras.getStringArray(URI_ARRAY);
			songTitles = extras.getStringArray(TITLE_ARRAY);
			int songNumber = extras.getInt("number");
			nowPlaying = songNumber;
			playSong(songNumber);
		}
		
		buildNotification("default");
	
		return serviceBinder;
	}




	public void playSong(int songPlace) {
		String testSongLocation = songPaths[songPlace];
		nowPlaying = songPlace;
		if (musicPlayer != null) {
			musicPlayer.reset();
		}
        
        Uri currentLocation = Uri.parse(testSongLocation);
        try{               	
        	musicPlayer.setDataSource(getApplicationContext(), currentLocation);
        	musicPlayer.prepare();
        	musicPlayer.start();
        	
        } catch (IOException e) {
        	System.out.println("Catch block triggered...");
        	
        	//for some reason, couldn't get the duration, so set to unknown for checking later
        	
        }
        //make sure the music player successfully loaded and began playing from file
        if (musicPlayer.isPlaying()) {
        	//create a notification that a user can use to control playback from
        	//outside app interface
        	
        }
        isPaused = false;
	}
	
	public void stopMusic() {
		long currentTime = System.currentTimeMillis();
		System.out.println("Started was:  " + startTime + "  Current is:  " + currentTime);
		
		if (currentTime - startTime > 1000) {
			if (musicPlayer != null) {
				musicPlayer.pause();
				isPaused = true;
			} else {
				System.out.println("music player was null");
			}
		}		
	}
	
	public void resumeMusic() {
		if (musicPlayer != null) {
			musicPlayer.start();
			isPaused = false;
		}
	}

	public Integer getSavedSpot() {
		SharedPreferences prefs = this.getSharedPreferences("com.matthewlewis.shakit", Context.MODE_PRIVATE);
		if (prefs.contains("song")) {
			nowPlaying = prefs.getInt("song", 0);
		}
		
		if (prefs.contains("currentPosition")) {
			Integer savedSpot = prefs.getInt("currentPosition", (Integer) null);
			return savedSpot;
		} else {
			return null;
		}
	}
	
	public void setCurrentMusic() {
		if (musicPlayer != null) {
			
//			SharedPreferences prefs = this.getSharedPreferences("com.matthewlewis.shakit", Context.MODE_PRIVATE);
//			if (nowPlaying != null) {
//				prefs.edit().putInt("song", nowPlaying).apply();
//				if (musicPlayer.getCurrentPosition() != -1) {
//					int currentPosition = musicPlayer.getCurrentPosition();
//					prefs.edit().putInt("currentPosition", currentPosition);
//				}
//				
//					
//			}
		}
		
	}
	
	public void checkPlayer() {
		if (musicPlayer == null) {
			
		} 
	}
	
	//we use this receiver to be informed of when the user taps the "pause" button from the notification.
	//While MusicService pauses itself, we need to update the notification to use a "play" button if paused, and vice versa
	public static class PauseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			
			
		}
		
	}
	
public void buildNotification (String type) {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		
		
		//create a basic intent to pause
    	Intent basicPause = new Intent(this, MusicService.class);
    	basicPause.setAction("Pause");
    	
    	//basic intent to skip
    	Intent basicNext = new Intent(this, MusicService.class);
    	basicNext.setAction("Next");
    	
    	//basic intent to return to previous song
    	Intent basicPrevious = new Intent(this, MusicService.class);
    	basicPrevious.setAction("Previous");
    	
    	//basic intent for when the user clicks the notification itself, which we want to open MainActivity
    	Intent basicOpen = new Intent(this, MainActivity.class);
    	
    	//create an intent to close out the notification and stop music playback
    	Intent basicClose = new Intent(this, MusicService.class);
    	basicClose.setAction("Stop");
    	
    	//create three different pending intents to apply to our control buttons
    	PendingIntent pausePending = PendingIntent.getService(this, 0, basicPause, 0);
    	PendingIntent nextPending = PendingIntent.getService(this, 0, basicNext, 0);
    	PendingIntent previousPending = PendingIntent.getService(this, 0, basicPrevious, 0);
    	//PendingIntent openActivityPending = PendingIntent.getActivity(this, 0, basicOpen, 0);
    	PendingIntent closePending = PendingIntent.getService(this, 0, basicClose, 0);
    	
    	if (notification == null) {
    		notification = new Notification();
    	}
    	
    	
    	Notification.Builder notificationBuilder = new Notification.Builder(this);
    	notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
    	notificationBuilder.setContentTitle(songTitles[nowPlaying]);
    	//notificationBuilder.setOngoing(true);
    	
    	//need to create an intent for below that will "relaunch" MainActivity
    	//notificationBuilder.setContentIntent(pIntent);
    	
    	
    	
		
		
		
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
			notificationBuilder.addAction(R.drawable.pause_small, "", pausePending);
			
		}
		
		
    	
    	notificationBuilder.addAction(R.drawable.next_small, "", nextPending);
    	
    	//notificationBuilder.setContentIntent(openActivityPending);
    	notificationBuilder.setDeleteIntent(closePending);
    	notification = notificationBuilder.build();  	
    	nm.notify(TXT_NOTIFICATION_ID, notification);
		
    	
    	
    	
	}
}
