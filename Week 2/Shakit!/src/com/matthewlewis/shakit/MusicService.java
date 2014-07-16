package com.matthewlewis.shakit;

import java.io.IOException;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
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
	public static final String PAUSE = "pause";
	public static final String PLAY = "play";
	public static final String NEXT = "next";
	public static final String BACK = "back";
	long startTime;
	
	String[] songPaths;
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
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("ON_BIND", "Music service started.");
		// android.os.Debug.waitForDebugger();

		startTime = System.currentTimeMillis();

		Log.i("ON_HANDLE_INTENT", "Music service started.");
		System.out.println("Service started!");
		// grab our extras data
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(URI_ARRAY)) {
			songPaths = extras.getStringArray(URI_ARRAY);
			playSong(0);
		}

		if (intent.hasExtra(PAUSE)) {
			System.out.println("Pause was sent");
			stopMusic();
		}

		return serviceBinder;
	}
	
	
	
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		//android.os.Debug.waitForDebugger();
		
		startTime = System.currentTimeMillis();
		
		Log.i("ON_HANDLE_INTENT", "Music service started.");
		System.out.println("Service started!");
		//grab our extras data
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(URI_ARRAY)) {
			songPaths = extras.getStringArray(URI_ARRAY);
			playSong(0);
		} 
		
		if (intent.hasExtra(PAUSE)) {
			System.out.println("Pause was sent");
			stopMusic();
		}
		
		
		
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
	}
	
	public void stopMusic() {
		long currentTime = System.currentTimeMillis();
		System.out.println("Started was:  " + startTime + "  Current is:  " + currentTime);
		
		if (currentTime - startTime > 1000) {
			if (musicPlayer != null) {
				System.out.println("music player was not null..");
				System.out.println("stopMusic called...");
				musicPlayer.pause();
			}
		}
		
	}

	
}
