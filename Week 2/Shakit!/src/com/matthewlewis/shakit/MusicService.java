package com.matthewlewis.shakit;

import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class MusicService extends IntentService{

	public static final String MESSENGER_KEY = "messenger";
	public static final String URI_ARRAY = "uriList";
	String[] songPaths;
	
	public MusicService() {
		super("MusicService");
		// TODO Auto-generated constructor stub
		System.out.println("music service.........");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		//android.os.Debug.waitForDebugger();
		
		Log.i("ON_HANDLE_INTENT", "Music service started.");
		System.out.println("Service started!");
		//grab our extras data
		Bundle extras = intent.getExtras();
		String[] songPaths = extras.getStringArray(URI_ARRAY);
		
		String testSongLocation = songPaths[0];
		
		MediaPlayer testPlayer = new MediaPlayer();
        
        Uri currentLocation = Uri.parse(testSongLocation);
        try{               	
        	testPlayer.setDataSource(getApplicationContext(), currentLocation);
        	testPlayer.prepare();
        	testPlayer.start();
        	
        } catch (IOException e) {
        	System.out.println("Catch block triggered...");
        	
        	//for some reason, couldn't get the duration, so set to unknown for checking later
        	
        }
	}

	public void nextSong() {
		
	}
}
