/*
 * Author Matthew Lewis
 * 
 * Project PhotoSet
 * 
 * Package com.matthewlewis.photoset
 * 
 * File MainActivity.java
 * 
 * Purpose MainActivity is the main interface for the entire app.  It displays an images the user has opted to "share" to the activity, and allows
 * them to select a "decorate" tab, which will load a subsequent activity to add image overlays.  Intents sharing images sometimes are formatted differently,
 * depending on the app creating the intent.  For example, 
 * 
 */
package com.matthewlewis.photoset;

import java.util.ArrayList;
import com.matthewlewis.photomail.R;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends Activity {

	public static LinearLayout activityLayout;
	LayoutParams imageParams;
	int screenHeight;
	Button decorateBtn;
	ImageView logo;
	Context context;
	int[] idArray;
	ImageView imageHolder;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      //get rid of that pesky title bar and set to fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        //grab a reference to our application context
        context = this.getApplicationContext();
        
        //lock our orientation to portrait, which keeps our images from stretching and keeps the size correct for a background image
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        setContentView(R.layout.activity_main);
        
        //grab reference to our logo imageView
        logo = (ImageView) findViewById(R.id.logo);
        
        //grab a reference to our default imageView
        imageHolder = (ImageView) findViewById(R.id.image_holder);
        
        //grab a reference to our decorate button
        decorateBtn = (Button) findViewById(R.id.decorate_btn);
        
        //grab the containing layout for the images so we can dynamically add more images if necessary
        activityLayout = (LinearLayout) findViewById(R.id.activity_holder);
        
        //crate layoutParams for any images we need to dynamically add
        imageParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        //Get our intent to see if our app was started by another app
        Intent intent = getIntent();
        
        String receivedAction = intent.getAction();
    	
    	String receivedType = intent.getType();
    	
    	//grab our screen displayMetrics so we can determine the height of the display
    	DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	
    	//set our height variable, which helps us to determine the height of images if there are more than one
    	screenHeight = metrics.heightPixels;
    	
    	//add click listener to our decorate button
    	decorateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				System.out.println("user tapped decorate!");
				
				//get rid of this activity's interface, since we will potentially be taking a screenshot within the next activity
				logo.setVisibility(View.GONE);
				decorateBtn.setVisibility(View.GONE);
				
				//load in our transparent "decorate" activity
				Intent decorateIntent = new Intent(context, com.matthewlewis.photoset.DecorateActivity.class);
				
				startActivityForResult(decorateIntent, 0);
			}
    		
    	});
    	
    	//if we have a valid "type" contained within intent and its starts with "image/"...
    	if (receivedType != null && receivedType.startsWith("image/")) {
    		if (receivedAction.equals("android.intent.action.SEND")) {
    			System.out.println("One image passed!");
        			
        		//grab our single passed image URI from the intent
        		Uri receivedUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        		
        		//apply the image located at the Uri to our imageView
        		imageHolder.setImageURI(receivedUri);
        		
        		//check if the user instead opted to "share" more than one image
    		} else if (receivedAction.equals("android.intent.action.SEND_MULTIPLE")) {
    			System.out.println("Multiple images sent!");
    			//get the contained bundle so we can figure out what "key" was used to store any parcelables
    			Bundle extras = intent.getExtras();
    			
    			//set our default image to "GONE" so that it isn't occupying the screen anymore
    			imageHolder.setVisibility(View.GONE);
    			System.out.println("Keys in intent were:  " + extras.keySet().size());
    			
    			//for each key contained, attempt to get an arraylist of parcelables, which will hold image Uris 
    			for (String key : extras.keySet()) {
    				System.out.println("Key was:  " + key);
    				
    				//create and assign our arraylist using our created key 
    				ArrayList<Parcelable> array = (ArrayList<Parcelable>) intent.getParcelableArrayListExtra(key);
    				
    				//check to make sure we have a valid arrayList
    				if (array != null) {
    					System.out.println("Object within the key was:  " + array);
        				
    					int maxHeight;
    					
    					//use our screen's pixel height to dynamically divide the height of the images passed to us (even height)
    					//depending on if there are more than 3 images passed, apply a certain height
    					if (array.size() > 3) {
    						maxHeight = screenHeight/3;
    						idArray = new int[3];
    					} else {
    						maxHeight = screenHeight/array.size();
    						idArray = new int[array.size()];
    					}
    					
    					//if more than 3 images were passed, alert user that only 3 will be used (avoid memory problems)
    					if (array.size() > 3) {
    						Toast.makeText(getApplicationContext(), "Unfortunately, PhotoSet only supports up to 3 images at once.  Using the first three.",
    								   Toast.LENGTH_LONG).show();
    					}  					
    					
    					
    					
    					//for the length of the array, create a new imageView and assign the image from the uri
        				for (int i = 0; i < array.size(); i++) {
        					ImageView receivedHolder = new ImageView(this);
        					Uri receivedUri = (Uri) array.get(i);
        					
        					receivedHolder.setImageURI(receivedUri);
        					receivedHolder.setLayoutParams(imageParams);
        					receivedHolder.setScaleType(ImageView.ScaleType.FIT_XY);
        					
        					
        					//manually set id for dynamic view to store it
        					int incrementer = i+1;
        					int newId = 837498576 / incrementer;
        					receivedHolder.setId(newId);
        					
        					//grab the id of each dynamic view so we can save it 
        					int id = receivedHolder.getId();
        					idArray[i] = id;
        					
        					receivedHolder.getLayoutParams().height = maxHeight;
        					System.out.println("Height assigned was:  " + maxHeight);
        					activityLayout.addView(receivedHolder);
        					
        					//to avoid OOM errors, limit to three images
        					if (i == 2) {
        						break;
        					}
        				}
    				}				
    				activityLayout.requestLayout();
    			}
    		}
    		
    	} else {
    		//manually set background color, because for some reason, the layout isn't doing it
    		View rootView = activityLayout.getRootView();
    		rootView.setBackgroundColor(Color.BLACK);
    		
    		//intent was the default, meaning the activity was launched manually by user
    		Drawable defaultBg = getResources().getDrawable(R.drawable.noneselected);
    		activityLayout.setBackground(defaultBg);
    		decorateBtn.setVisibility(View.GONE);
    	}
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		super.onActivityResult(requestCode, resultCode, data);
		logo.setVisibility(View.VISIBLE);
		decorateBtn.setVisibility(View.VISIBLE);
		
		//grab the returned boolean so we know if the user updated their wallpaper
		Boolean didUpdate = data.getBooleanExtra("wasUpdated", false);
		
		if (didUpdate) {
			//if the user DID update their wallpaper, let's grab it and set it here to match
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.getApplicationContext());
			Drawable currentWallpaper = wallpaperManager.getFastDrawable();
			
			if (idArray != null) {
				//make sure to get rid of the old images that were being displayed
				//this is needed when the user imported multiple images instead of one
				for (int i = 0; i < idArray.length; i++) {
					
					int id = idArray[i];
					ImageView oldImage = (ImageView) findViewById(id);
					activityLayout.removeView(oldImage);
				}
			} else {
				//idArray was never used since the user only shared one image
				imageHolder.setVisibility(View.GONE);
			}			
			
			//set new background
			
			activityLayout.setBackground(currentWallpaper);
		}
	}   
}
