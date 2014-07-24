/*
 * Author Matthew Lewis
 * 
 * Project Shakit!
 * 
 * Package com.matthewlewis.shakit
 * 
 * File MusicWidgetProvider.java
 * 
 * Purpose The MusicWidgetProvider class is the main class responsible for updating and maintaining any widgets the user decides
 * to create.  It also allows for controlling music playback via the widget's interface.
 * 
 */
package com.matthewlewis.shakit;

import java.io.IOException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;
import android.widget.TextView;

public class MusicWidgetProvider extends AppWidgetProvider {

	RemoteViews remote;
	TextView titleText;
	String[] songNames;
	String[] songLocations;
	int[] widgetIds;
	AppWidgetManager widgetManager;
	private final String BUTTON_KEY = "buttonStatus";
	private final String BUTTON_PLAY = "play";
	private final String BUTTON_PAUSE = "pause";
	
	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		System.out.println("onEnabled runs from provider");	
		super.onEnabled(context);	
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		System.out.println("onUpdate runs from provider");
		// TODO Auto-generated method stub
		
		//write a default state for our play/pause button to shared prefs, since data is not retained between updates
		saveToPrefs(BUTTON_KEY, BUTTON_PLAY, context);
		
		//create all of our intents for each button contained within the widget
		Intent playPauseIntent = new Intent(context, MusicWidgetProvider.class);
		Intent nextIntent = new Intent(context, MusicWidgetProvider.class);
		Intent previousIntent = new Intent(context, MusicWidgetProvider.class);
		
		//set up actions for each intent, so we know which was pressed
		playPauseIntent.setAction("PlayPause");
		nextIntent.setAction("Next");
		previousIntent.setAction("Previous");
		
		//create our pending intents for each button
		PendingIntent playPausePending = PendingIntent.getBroadcast(context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent nextPending = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent previousPending = PendingIntent.getBroadcast(context, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		//add our onClickPendingIntent 'listeners' to each of our widget's buttons
		remote.setOnClickPendingIntent(R.id.widget_playPause, playPausePending);
		remote.setOnClickPendingIntent(R.id.widget_next, nextPending);
		remote.setOnClickPendingIntent(R.id.widget_previous, previousPending);
		
		if (MainActivity.mService != null) {
			if (MainActivity.mService.musicPlayer != null) {
				//set our displayed song name to whatever music service is now showing
				remote.setTextViewText(R.id.widget_songLabel, MainActivity.mService.songTitles[MainActivity.mService.nowPlaying]);
					
				//set image of pause/play button according to if MusicService's musicPlayer is currently playing
				if (MainActivity.mService.musicPlayer.isPlaying()) {
					remote.setImageViewResource(R.id.widget_playPause, R.drawable.pause_small);
				} else {
					remote.setImageViewResource(R.id.widget_playPause, R.drawable.play_small);
				}
				
			}			
		}
		
		appWidgetManager.updateAppWidget(appWidgetIds, remote);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		//grab the action contained within the intent to a string so we know what has occurred
		String action = intent.getAction();
		
		//grab our ids and widgetManager in case we need to update the widget
		ComponentName widget = new ComponentName(context, MusicWidgetProvider.class);
		AppWidgetManager wm = AppWidgetManager.getInstance(context);
		int[] ids = wm.getAppWidgetIds(widget);
		
		//grab our remote view if we need to do something to its interface
		remote = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		//System.out.println("Action received was:  " + action);
		
		if (action.equals("android.appwidget.action.APPWIDGET_ENABLED")) {
			//this is the very first thing that is run when the user first selects the widget from the launcher,
			//so grab any data we need for its functionality
			if (MainActivity.songTitles != null) {
				songNames = MainActivity.songTitles;
				songLocations = MainActivity.songPaths;
			} else {
				//for whatever reason, MainActivity is either currently not running, or it's data is null,
				//so grab it from shared prefs (which should have data since MainActivity has to have run at least once)
				SharedPreferences prefs = context.getSharedPreferences("com.matthewlewis.shakit", Context.MODE_PRIVATE);
		        
				try {
					int numSongs = prefs.getInt("numSongs", 0);
					songLocations = (String[]) ObjectSerializer.deserialize(prefs.getString("songPaths", ObjectSerializer.serialize(new String[numSongs])));
					songNames = (String[]) ObjectSerializer.deserialize(prefs.getString("songTitles", ObjectSerializer.serialize(new String[numSongs])));
					System.out.println("Data retrieved from shared preferences successfully!");
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
			
		} else if (action.equals("PlayPause")) {
			
			
			
			//grab a boolean, which tells us if MainActivity currently has a reference to MusicService
			boolean serviceExists;
			if (MainActivity.mService != null) {
				serviceExists = true;
			} else {
				serviceExists = false;
			}
			
			//read from prefs to get which icon is currently being displayed
			String currentIcon = readPrefs(BUTTON_KEY, context);
			if (currentIcon.equals(BUTTON_PLAY)) {
				
				
				//since the user clicked the "play" icon, play whichever song is currently displayed
				if (serviceExists == true) {
					//service is "active" so go ahead an play
					System.out.println("Is it playing??!");
					MainActivity.mService.resumeMusic();
					MainActivity.mService.buildNotification("play/pause");
					remote.setImageViewResource(R.id.widget_playPause, R.drawable.pause_small);
					saveToPrefs(BUTTON_KEY, BUTTON_PAUSE, context);
				} else {
					//service either wasn't started, or is not running so start it
					System.out.println("Can't play because Music Service is not currently running!");
					Intent musicIntent = new Intent(context, MainActivity.class);
					musicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(musicIntent);
										
				}
				
			} else {
				remote.setImageViewResource(R.id.widget_playPause, R.drawable.play_small);
				saveToPrefs(BUTTON_KEY, BUTTON_PLAY, context);
				
				if (serviceExists == true) {
					//pause the currently playing music
					MainActivity.mService.stopMusic();
					MainActivity.mService.buildNotification("play/pause");
				} else {
					System.out.println("Can't pause because Music Service is not currently running!");
					
				}
				
			}		
			wm.partiallyUpdateAppWidget(ids, remote);
			
			System.out.println("PlayPause");
		} else if (action.equals("Next")) {
			if (MainActivity.mService != null) {
				System.out.println("Next");
				MainActivity.mService.nextSong();
				MainActivity.mService.buildNotification("play/pause");
				remote.setImageViewResource(R.id.widget_playPause, R.drawable.pause_small);
				remote.setTextViewText(R.id.widget_songLabel, MainActivity.mService.songTitles[MainActivity.mService.nowPlaying]);
				wm.partiallyUpdateAppWidget(ids, remote);
			}
			
		} else if (action.equals("Previous")) {
			if (MainActivity.mService != null) {
				System.out.println("Previous");
				MainActivity.mService.previousSong();
				MainActivity.mService.buildNotification("play/pause");
				remote.setImageViewResource(R.id.widget_playPause, R.drawable.pause_small);
				remote.setTextViewText(R.id.widget_songLabel, MainActivity.mService.songTitles[MainActivity.mService.nowPlaying]);
				wm.partiallyUpdateAppWidget(ids, remote);
			}
			
		} else if (action.equals("android.appwidget.action.APPWIDGET_APDATE")) {
			//this is a broadcast sent from the MusicService class, so check to make
			//sure everything is valid, then update our widget to match MusicService notification
			if (MainActivity.mService != null) {
				if (MainActivity.mService.musicPlayer != null) {
					if (MainActivity.mService.musicPlayer != null) {
						//set our displayed song name to whatever music service is now showing
						remote.setTextViewText(R.id.widget_songLabel, MainActivity.mService.songTitles[MainActivity.mService.nowPlaying]);
						
						//set image of pause/play button according to if MusicService's musicPlayer is currently playing
						if (MainActivity.mService.musicPlayer.isPlaying()) {
							remote.setImageViewResource(R.id.widget_playPause, R.drawable.pause_small);
						} else {
							remote.setImageViewResource(R.id.widget_playPause, R.drawable.play_small);
						}
						wm.partiallyUpdateAppWidget(ids, remote);
					}
				}			
			}
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		System.out.println("onDeleted runs from provider");
		super.onDeleted(context, appWidgetIds);
	}
	
	//since data is not retained between widget updates (looks like Android kills off widgetProviders...), we need to save any data to shared preferences. 
	public void saveToPrefs (String key, String value, Context context) {
		SharedPreferences prefs = context.getSharedPreferences("com.matthewlewis.shakit", Context.MODE_PRIVATE);
		prefs.edit().putString(key, value).apply();
	}
	
	//this method is used to retrieve a value from shared preferences
	public String readPrefs(String key, Context context) {
		SharedPreferences prefs = context.getSharedPreferences("com.matthewlewis.shakit", Context.MODE_PRIVATE);
		String val = prefs.getString(key, null);
		return val;
	}
	
}
