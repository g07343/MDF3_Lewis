package com.matthewlewis.shakit;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

public class MusicWidgetProvider extends AppWidgetProvider {

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
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
