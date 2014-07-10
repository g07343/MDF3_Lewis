/*
 * Author Matthew Lewis
 * 
 * Project PhotoSet
 * 
 * Package com.matthewlewis.photoset
 * 
 * File DecorateActivity.java
 * 
 * Purpose DecorateActivity allows the user to overlay small icons/images over there image and then set it as their device's background.  
 * We use a transparent background so that the user can actually see how it will look before saving it.
 * 
 */
package com.matthewlewis.photoset;

import java.util.ArrayList;

import com.matthewlewis.photomail.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class DecorateActivity extends Activity{
	
	LinearLayout iconDrawer;
	Button finishBtn;
	Integer currentSelected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//set our view to the xml layout
		setContentView(R.layout.activity_decorate);
		
		//grab references to our views that we may need
		finishBtn = (Button) findViewById(R.id.decorate_setBtn);
		iconDrawer = (LinearLayout) findViewById(R.id.decorate_drawerHolder);
		
		iconDrawer.setVisibility(View.GONE);
		
		//this onClickListener is needed so that the buttons underneath the view don't receive tap events, since 
		//Android delegates tap events to them otherwise.
		iconDrawer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//do nothing, we just need this to keep other onClickEvents from firing.
				hideDrawer();
			}
			
		});
		
		//create an array of ids for our various "addIcons" so we can keep this (mostly) dynamic.  
		//Don't want to keep track of 9 different image views..
		final int[] idArray = new int[9];
		
		for (int i = 1; i < 10; i++) {
			String buttonLabel = "imageView" + i;
			int arraySync = i-1;
			
			int resID = getResources().getIdentifier(buttonLabel, "id", DecorateActivity.this.getPackageName());
			idArray[arraySync] = resID;
		}
		System.out.println("Finalized array is:  " + idArray.toString());
		
		for (int i = 0; i < idArray.length; i++) {
			ImageView current = (ImageView) findViewById(idArray[i]);
			final int temp = i;
			current.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub					
					enableDrawer(idArray[temp]);
				}
				
			});
		}
	}
	

    @Override
    public void onBackPressed() {
    	finish();
    }
	
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		Intent result = new Intent();
		setResult(Activity.RESULT_OK, result);
		super.finish();
	}

	//this method is responsible for displaying the drawer containing the icons the user can choose from.
	//it receives the raw id of which imageView so we can dynamically do things in code, rather than keep 
	//track of 9 different image views
	public void enableDrawer(int Id) {
		
		currentSelected = Id;
		System.out.println("ID pressed was:  " + Id);
		finishBtn.setVisibility(View.GONE);
		iconDrawer.setVisibility(View.VISIBLE);
	}
	
	public void hideDrawer() {
		iconDrawer.setVisibility(View.GONE);
		finishBtn.setVisibility(View.VISIBLE);
		removeListeners();
	}

	//this method removes all previously assigned clicklisteners so we aren't added images randomly
	private void removeListeners() {
		// TODO Auto-generated method stub
		
	}

}
