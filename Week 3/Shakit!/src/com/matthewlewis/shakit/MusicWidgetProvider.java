package com.matthewlewis.shakit;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;
import android.widget.TextView;

public class MusicWidgetProvider extends AppWidgetProvider {

	RemoteViews remote;
	TextView titleText;
	
	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
		System.out.println("onEnabled runs from provider");	
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		System.out.println("onUpdate runs from provider");
		// TODO Auto-generated method stub
		
		
		
		//set up our reference to our remoteViews
		remote = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		remote.setTextViewText(R.id.widget_songLabel, "BLAH BLAH!");
		
		
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
		
		appWidgetManager.updateAppWidget(appWidgetIds, remote);
		//super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		System.out.println("On receive fires within appwidgetprovider");
		
		String action = intent.getAction();
		System.out.println("Action received was:  " + action);
		if (action.equals("android.appwidget.action.APPWIDGET_ENABLED")) {
			System.out.println("PlayPause button was pressed!!!");
		} else if (action.equals("PlayPause")) {
			
		} else if (action.equals("Next")) {
			
		} else if (action.equals("Previous")) {
			
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		System.out.println("onDeleted runs from provider");
		super.onDeleted(context, appWidgetIds);
	}
}
