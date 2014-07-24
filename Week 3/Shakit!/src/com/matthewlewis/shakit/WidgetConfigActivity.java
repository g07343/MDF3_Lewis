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
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewAnimator;

public class WidgetConfigActivity  extends Activity {

	ViewAnimator viewAnimator;	
	Animation slide_in_left, slide_in_right, slide_out_right, slide_out_left;
	GestureDetector gestureDetector;
	int currentScreen;
	ImageView pageIcons;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		//force portrait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.widget_config_layout);
		super.onCreate(savedInstanceState);
		
		
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
		
		
	}

	@Override
	public boolean onTouchEvent (MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	//create a cutom gesture listener class to implement
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		
		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
		
		@Override
		public boolean onDown(MotionEvent event) {
			System.out.println("onDOWN!");
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

}
