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

import com.matthewlewis.photomail.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DecorateActivity extends Activity{
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//set our view to the xml layout
		setContentView(R.layout.activity_decorate);
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

	

}
