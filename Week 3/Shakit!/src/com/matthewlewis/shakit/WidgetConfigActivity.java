/*
 * Author Matthew Lewis
 * 
 * Project Shakit!
 * 
 * Package com.matthewlewis.shakit
 * 
 * File WidgetConfigActivity.java
 * 
 * Purpose The widget config activity allows the user to customize the look of their widget as they place it onto
 * their home screen.  Customized choices (when finished) are then sent to the widget provider to update the remote view
 * 
 */
package com.matthewlewis.shakit;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RemoteViews;
import android.widget.ViewAnimator;

public class WidgetConfigActivity  extends Activity {

	ViewAnimator viewAnimator;	
	Animation slide_in_left, slide_in_right, slide_out_right, slide_out_left;
	GestureDetector gestureDetector;
	int currentScreen;
	ImageView pageIcons;
	String speakerColor;
	String buttonColor;
	String tintColor;
	ImageView playPause;
	ImageView playPauseBg;
	ImageView nextBtn;
	ImageView previousBtn;
	LinearLayout tintedBg;
	RadioGroup speakerGroup;
	RadioGroup buttonGroup;
	RadioGroup tintGroup;
	Button doneButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		//force portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.widget_config_layout);
		super.onCreate(savedInstanceState);
		
		//set up the default selected colors for the widget
		speakerColor = "Yellow";
		buttonColor = "White";
		tintColor = "Dark";
		
		//grab the views from the "demo" widget that is displayed
		playPause = (ImageView) findViewById(R.id.widget_playPause);
		playPauseBg = (ImageView) findViewById(R.id.widget_playPauseBg);
		nextBtn = (ImageView) findViewById(R.id.widget_next);
		previousBtn = (ImageView) findViewById(R.id.widget_previous);
		tintedBg = (LinearLayout) findViewById(R.id.widget_tintedBg);
		speakerGroup = (RadioGroup) findViewById(R.id.widgetConfig_speakerGroup);
		buttonGroup = (RadioGroup) findViewById(R.id.widgetConfig_buttonGroup);
		tintGroup = (RadioGroup) findViewById(R.id.widgetConfig_backgroundGroup);
		doneButton = (Button) findViewById(R.id.widgetConfig_button);
		
		//set up which screen were on, which is used to tell our view animator which child to dynamically show
		currentScreen = 0;
		
		//grab our view animator
		viewAnimator = (ViewAnimator) findViewById(R.id.widgetConfig_animator);
		
		//grab our imageView that shows the user which page they're on
		pageIcons = (ImageView) findViewById(R.id.widgetConfig_pagination);
		
		//set up our animations
		slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
		slide_in_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
		slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
		slide_out_left = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
		
		//set our animations to the animator
		viewAnimator.setAnimation(slide_in_left);
		viewAnimator.setAnimation(slide_out_right);
		
		//set up our gesture detector
		gestureDetector = new GestureDetector(this, new MyGestureListener());
		
		//set up listeners on our three radio groups to get the user's selections
		speakerGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int option) {
				// TODO Auto-generated method stub
				
				//grab the reference ids to the three radio buttons to compare against the passed integer
				int red = R.id.widgetConfig_speakerRed;
				int yellow = R.id.widgetConfig_speakerYellow;
				int blue = R.id.widgetConfig_speakerBlue;
				
				if (option == red) {
					playPauseBg.setImageDrawable(getResources().getDrawable(R.drawable.speaker_red));
					speakerColor = "Red";
				} else if (option == yellow) {
					playPauseBg.setImageDrawable(getResources().getDrawable(R.drawable.speaker_yellow));
					speakerColor = "Yellow";
				} else if (option == blue) {
					playPauseBg.setImageDrawable(getResources().getDrawable(R.drawable.speaker_blue));
					speakerColor = "Blue";
				}
			}			
		});
		
		buttonGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// grab references to each possible radio button contained in the group
				int white = R.id.widgetConfig_buttonWhite;
				int black = R.id.widgetConfig_buttonBlack;
				int yellow = R.id.widgetConfig_buttonYellow;
				int red = R.id.widgetConfig_buttonRed;
				
				if (checkedId == white) {
					buttonColor = "White";
				} else if (checkedId == black) {
					buttonColor = "Black";
				} else if (checkedId == yellow) {
					buttonColor = "Yellow";
				} else if (checkedId == red) {
					buttonColor = "Red";					
				}
				//call a method to update the preview widget's icons
				updatePreviewIcons();
			}
			
		});
		
		tintGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// grab references to each of the radio buttons within the group
				int dark = R.id.widgetConfig_tintDark;
				int light = R.id.widgetConfig_tintLight;
				int red = R.id.widgetConfig_tintRed;
				int blue = R.id.widgetConfig_tintBlue;
				
				if (checkedId == dark) {
					tintColor = "Dark";
					tintedBg.setBackgroundColor(Color.parseColor("#50000000"));
				} else if (checkedId == light) {
					tintColor = "Light";
					tintedBg.setBackgroundColor(Color.parseColor("#50FFFFFF"));
				} else if (checkedId == red) {
					tintColor = "Red";
					tintedBg.setBackgroundColor(Color.parseColor("#50ff0000"));
				} else if (checkedId == blue) {
					tintColor = "Blue";
					tintedBg.setBackgroundColor(Color.parseColor("#500018ff"));
				}
				
			}
			
		});
		
		//set up onClickListener for our "done" button so we can build the widget and place it on the home screen
		doneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// user is done customizing their widget, so save out to prefs (in case provider needs to access these)
				SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.matthewlewis.shakit", Context.MODE_PRIVATE);
				prefs.edit().putString("speakerColor", speakerColor).apply();
				prefs.edit().putString("buttonColor", buttonColor).apply();
				prefs.edit().putString("tintColor", tintColor);
				
				//go ahead and apply to the created widget now
				RemoteViews remote = new RemoteViews(getPackageName(), R.layout.widget_layout);
				
				//grab our intent/bundle so we can access the widget's id
				Bundle extras = getIntent().getExtras();
				
				if (extras != null) {
					int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
					
					//check to make sure that the widget id is valid
					if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
						int speakerDrawable;
						int playBtnDrawable;
						int nextBtnDrawable;
						int previousBtnDrawable;
						
						//check each of the user defined colors and apply via the remoteView
						if (speakerColor.equals("Red")) {
							speakerDrawable = R.drawable.speaker_red;
							remote.setImageViewResource(R.id.widget_playPauseBg, speakerDrawable);
						} else if (speakerColor.equals("Blue")) {
							speakerDrawable = R.drawable.speaker_blue;
							remote.setImageViewResource(R.id.widget_playPauseBg, speakerDrawable);
						} else if (speakerColor.equals("Yellow")) {
							speakerDrawable = R.drawable.speaker_yellow;
							remote.setImageViewResource(R.id.widget_playPauseBg, speakerDrawable);
						}
						
						//set our button colors
						if (buttonColor.equals("White")) {
							playBtnDrawable = R.drawable.play_small;
							nextBtnDrawable = R.drawable.next_small;
							previousBtnDrawable = R.drawable.back_small;
							remote.setImageViewResource(R.id.widget_playPause, playBtnDrawable);
							remote.setImageViewResource(R.id.widget_next, nextBtnDrawable);
							remote.setImageViewResource(R.id.widget_previous, previousBtnDrawable);
						} else if (buttonColor.equals("Black")) {
							playBtnDrawable = R.drawable.play_small_black;
							nextBtnDrawable = R.drawable.next_small_black;
							previousBtnDrawable = R.drawable.back_small_black;
							remote.setImageViewResource(R.id.widget_playPause, playBtnDrawable);
							remote.setImageViewResource(R.id.widget_next, nextBtnDrawable);
							remote.setImageViewResource(R.id.widget_previous, previousBtnDrawable);
						} else if (buttonColor.equals("Yellow")) {
							playBtnDrawable = R.drawable.play_small_yellow;
							nextBtnDrawable = R.drawable.next_small_yellow;
							previousBtnDrawable = R.drawable.back_small_yellow;
							remote.setImageViewResource(R.id.widget_playPause, playBtnDrawable);
							remote.setImageViewResource(R.id.widget_next, nextBtnDrawable);
							remote.setImageViewResource(R.id.widget_previous, previousBtnDrawable);
						} else if (buttonColor.equals("Red")) {
							playBtnDrawable = R.drawable.play_small_red;
							nextBtnDrawable = R.drawable.next_small_red;
							previousBtnDrawable = R.drawable.back_small_red;
							remote.setImageViewResource(R.id.widget_playPause, playBtnDrawable);
							remote.setImageViewResource(R.id.widget_next, nextBtnDrawable);
							remote.setImageViewResource(R.id.widget_previous, previousBtnDrawable);
						}
						
						//set the widget background tint
						if (tintColor.equals("Dark")) {
							remote.setInt(R.id.widget_tintedBg, "setBackgroundColor", Color.parseColor("#50000000"));
						} else if (tintColor.equals("Light")) {
							remote.setInt(R.id.widget_tintedBg, "setBackgroundColor", Color.parseColor("#50FFFFFF"));
						} else if (tintColor.equals("Red")) {
							remote.setInt(R.id.widget_tintedBg, "setBackgroundColor", Color.parseColor("#50ff0000"));
						} else if (tintColor.equals("Blue")) {
							remote.setInt(R.id.widget_tintedBg, "setBackgroundColor", Color.parseColor("#500018ff"));
						}
					}
					
					//tell widgetManager to update our widget
					AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(widgetId, remote);
					
					//unfortunately, we need to set our intents here, adding even more logic to this poor onClickListener
					
					//create all of our intents for each button contained within the widget
					Intent playPauseIntent = new Intent(getApplicationContext(), MusicWidgetProvider.class);
					Intent nextIntent = new Intent(getApplicationContext(), MusicWidgetProvider.class);
					Intent previousIntent = new Intent(getApplicationContext(), MusicWidgetProvider.class);
					
					//set up actions for each intent, so we know which was pressed
					playPauseIntent.setAction("PlayPause");
					nextIntent.setAction("Next");
					previousIntent.setAction("Previous");
					
					//create our pending intents for each button
					PendingIntent playPausePending = PendingIntent.getBroadcast(getApplicationContext(), 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					PendingIntent nextPending = PendingIntent.getBroadcast(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					PendingIntent previousPending = PendingIntent.getBroadcast(getApplicationContext(), 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					
					//add our onClickPendingIntent 'listeners' to each of our widget's buttons
					remote.setOnClickPendingIntent(R.id.widget_playPause, playPausePending);
					remote.setOnClickPendingIntent(R.id.widget_next, nextPending);
					remote.setOnClickPendingIntent(R.id.widget_previous, previousPending);
					
					Intent result = new Intent();
					result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
					setResult(RESULT_OK, result);
					finish();
				}
			}
			
		});
	}

	@Override
	public boolean onTouchEvent (MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	//create a cutom gesture listener class to listen for left/right swipes
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		
		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
		
		@Override
		public boolean onDown(MotionEvent event) {
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			
			try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    currentScreen ++;
                    if (currentScreen > 2) {
                    	currentScreen = 2;
                    } else {
                    	viewAnimator.setInAnimation(slide_in_right);
                    	viewAnimator.setOutAnimation(slide_out_left);
                    	viewAnimator.showNext();;
                    	updatePageIcon();
                    }
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	currentScreen --;
                	if (currentScreen < 0) {
                		currentScreen = 0;
                	} else {
                		//viewAnimator.setDisplayedChild(currentScreen);
                		viewAnimator.setInAnimation(slide_in_left);
                    	viewAnimator.setOutAnimation(slide_out_right);
                		viewAnimator.showPrevious();
                		updatePageIcon();
                	}               	
                }
            } catch (Exception e) {
                // nothing
            }
			
			return true;
		}
	}
	
	public void updatePageIcon () {
		if (currentScreen == 0) {
			pageIcons.setImageDrawable(getResources().getDrawable(R.drawable.page1));
		} else if (currentScreen == 1) {
			pageIcons.setImageDrawable(getResources().getDrawable(R.drawable.page2));
		} else if (currentScreen == 2) {
			pageIcons.setImageDrawable(getResources().getDrawable(R.drawable.page3));
		}
	}

	//this method basically updates the icons within the "preview" widget as the user configures it
	//This is it's own method to keep onCreate less cluttered
	public void updatePreviewIcons () {
		if (buttonColor.equals("White")) {
			playPause.setImageDrawable(getResources().getDrawable(R.drawable.play_small));
			nextBtn.setImageDrawable(getResources().getDrawable(R.drawable.next_small));
			previousBtn.setImageDrawable(getResources().getDrawable(R.drawable.back_small));
		} else if (buttonColor.equals("Black")) {
			playPause.setImageDrawable(getResources().getDrawable(R.drawable.play_small_black));
			nextBtn.setImageDrawable(getResources().getDrawable(R.drawable.next_small_black));
			previousBtn.setImageDrawable(getResources().getDrawable(R.drawable.back_small_black));
		} else if (buttonColor.equals("Yellow")) {
			playPause.setImageDrawable(getResources().getDrawable(R.drawable.play_small_yellow));
			nextBtn.setImageDrawable(getResources().getDrawable(R.drawable.next_small_yellow));
			previousBtn.setImageDrawable(getResources().getDrawable(R.drawable.back_small_yellow));
		} else if (buttonColor.equals("Red")) {
			playPause.setImageDrawable(getResources().getDrawable(R.drawable.play_small_red));
			nextBtn.setImageDrawable(getResources().getDrawable(R.drawable.next_small_red));
			previousBtn.setImageDrawable(getResources().getDrawable(R.drawable.back_small_red));
		}
	}
}
