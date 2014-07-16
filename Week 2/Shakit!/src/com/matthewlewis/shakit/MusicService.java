package com.matthewlewis.shakit;

import java.io.IOException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
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
	long startTime;
	
	String[] songPaths;
	String[] songTitles;
	MediaPlayer musicPlayer;
	
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
		
		if (intent.getAction().equals("Pause")) {
			System.out.println("Pause fired from passed intent");
			stopMusic();
		} else if (intent.getAction().equals("Next")) {
			System.out.println("User wants to skip to next song!");
		} else if (intent.getAction().equals("Previous")) {
			System.out.println("User wants to go to previous song!");
		}
		return super.onStartCommand(intent, flags, startId);
	}






	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("ON_BIND", "Music service started.");
		// android.os.Debug.waitForDebugger();

		startTime = System.currentTimeMillis();

		
		// grab our extras data
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(URI_ARRAY)) {
			songPaths = extras.getStringArray(URI_ARRAY);
			songTitles = extras.getStringArray(TITLE_ARRAY);
			playSong(0);
		}

		if (intent.hasExtra(PAUSE)) {
			System.out.println("Pause was sent");
			stopMusic();
		}

		if (intent.hasExtra(NOTIFICATION_ACTION)) {
			System.out.println("ACTION RECEIVED FROM NOTIFICATION!!!");
		}
		return serviceBinder;
	}

	public void playSong(int songPlace) {
		String testSongLocation = songPaths[songPlace];
		
		musicPlayer = new MediaPlayer();
        
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
        	
        	//create a basic intent to pause
        	Intent basicPause = new Intent(this, MusicService.class);
        	basicPause.setAction("Pause");
        	
        	//basic intent to skip
        	Intent basicNext = new Intent(this, MusicService.class);
        	basicNext.setAction("Next");
        	
        	//basic intent to return to previous song
        	Intent basicPrevious = new Intent(this, MusicService.class);
        	basicPrevious.setAction("Previous");
        	
        	//create three different pending intents to apply to our control buttons
        	PendingIntent pausePending = PendingIntent.getService(this, 0, basicPause, 0);
        	PendingIntent nextPending = PendingIntent.getService(this, 0, basicNext, 0);
        	PendingIntent previousPending = PendingIntent.getService(this, 0, basicPrevious, 0);
        	
        	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        	Notification notification = new Notification();
        	Notification.Builder notificationBuilder = new Notification.Builder(this);
        	notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        	notificationBuilder.setContentTitle(songTitles[songPlace]);
        	notificationBuilder.setOngoing(true);
        	
        	//need to create an intent for below that will "relaunch" MainActivity
        	//notificationBuilder.setContentIntent(pIntent);
        	notificationBuilder.addAction(R.drawable.ic_launcher, "", previousPending);
        	notificationBuilder.addAction(R.drawable.ic_launcher, "", pausePending);
        	notificationBuilder.addAction(R.drawable.ic_launcher, "", nextPending);
        	
        	
        	notification = notificationBuilder.build();
        	

        	BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action_name = intent.getAction();
                    if (action_name.equals("notificationReceiver")) {
                        // call your method here and do what ever you want.
                    	System.out.println("Broadcast received!");
                    }
                };
            };
            Context context = getApplicationContext();
           context.registerReceiver(notificationReceiver, new IntentFilter("notificationReceiver"));
        	
        	
        	nm.notify(TXT_NOTIFICATION_ID, notification);
        }
	}
	
	public void stopMusic() {
		long currentTime = System.currentTimeMillis();
		System.out.println("Started was:  " + startTime + "  Current is:  " + currentTime);
		
		if (currentTime - startTime > 1000) {
			if (musicPlayer != null) {
				musicPlayer.pause();
			} else {
				System.out.println("music player was null");
			}
		}
		
	}

	public void testFunction() {
		System.out.println("testFunction runs!!!!!!!!!!");
	}
}
