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


import java.io.IOException;
import com.matthewlewis.photomail.R;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DecorateActivity extends Activity{
	
	LinearLayout iconDrawer;
	Button finishBtn;
	Integer currentSelected;
	int[] imageIdArray;
	String savedPath;
	RelativeLayout rootView;
	int[] idArray;
	Boolean wallpaperSet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		System.out.println("ONCREATE FUNCTION RUNS IN DECORATEACTIVITY");
		
		//set up our boolean, which will be returned to MainAcitivity to communicate if the user set the wallpaper
		wallpaperSet = false;
		
		//grab the root content holder of the activity
		rootView = (RelativeLayout) findViewById(R.id.decorate_rootView);
		
		//rootView.setBackground(converted);
		
		//set our view to the xml layout
		setContentView(R.layout.activity_decorate);
		
		//set up our drawer so the user has things to choose from
		populateDrawer();
		
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
		
		//set onClickListener for the "Set!" button, which applys the image as the device wallpaper
		finishBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				setWallpaper();
			}
			
		});
		
		//create an array of ids for our various "addIcons" so we can keep this (mostly) dynamic.  
		//Don't want to keep track of 9 different image views..
		idArray = new int[9];
		
		for (int i = 1; i < 10; i++) {
			String buttonLabel = "imageView" + i;
			
			//use this to determine where to insert values into the array, since we're starting at one
			int arraySync = i-1;
			
			//use our dyanamic string above to grab the id of the item imageView it finds
			int resID = getResources().getIdentifier(buttonLabel, "id", DecorateActivity.this.getPackageName());
			
			//add the id to our array
			idArray[arraySync] = resID;
		}
		System.out.println("Finalized array is:  " + idArray.toString());
		
		//now that we have all of the ids, dynamically apply onClickListeners to each of them
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
	
	//detect when the user taps the "back" button
    @Override
    public void onBackPressed() {
    	finish();
    }
	
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		Intent result = new Intent();
		
		//return our boolean so MainActivity knows if it needs to update it's own background to match
		result.putExtra("wasUpdated", wallpaperSet);
		
		setResult(Activity.RESULT_OK, result);
		super.finish();
	}

	//this method is responsible for displaying the drawer containing the icons the user can choose from.
	//it receives the raw id of which imageView so we can dynamically do things in code, rather than keep 
	//track of 9 different image views (plus 6 more for the stickers)
	public void enableDrawer(int Id) {
		
		currentSelected = Id;
		System.out.println("ID pressed was:  " + Id);
		finishBtn.setVisibility(View.GONE);
		iconDrawer.setVisibility(View.VISIBLE);
	}
	
	//this method is called to hide the drawer when the user chooses something
	public void hideDrawer() {
		iconDrawer.setVisibility(View.GONE);
		finishBtn.setVisibility(View.VISIBLE);
	}


	//this method runs when the activity loads, and is responsible for dynamically creating items within the drawer
	private void populateDrawer() {
		LinearLayout stickerHolder = (LinearLayout) findViewById(R.id.decorate_stickerHolder);
		
		//create an array to hold the drawable ids
		imageIdArray = new int[6];
		
		int rowCounter = 0;
		
		//for now, hardcode the number of items so we can keep things dynamic (currently we have 6 items)
		for (int i = 1; i < 7; i++) {
			
			//dynamic string to reference our specific drawable
			String stickerName = "sticker" + i;
			
			System.out.println("Sticker being search is:  " + stickerName);
			
			//like in the onCreate method, we need a seperate counter to keep our data in sync with array
			int arraySync = i-1;
			
			if (rowCounter == 2) {
				//reset our row counter so we only have 3 items per row
				rowCounter = 0;
			}
			
			//get the actual integer id of each image to store in array
			final int resID = getResources().getIdentifier(stickerName, "drawable", DecorateActivity.this.getPackageName());
			
			//add the id to our array for when we dynamically assign onClickListeners
			imageIdArray[arraySync] = resID;
			
			//create a reuseable imageView each time to hold each object
			ImageView stickerItem = new ImageView(this);
			
			//get our image
			Drawable stickerImage = getResources().getDrawable(resID);
			
			//set to the imageView
			stickerItem.setImageDrawable(stickerImage);
			
			//add the imageView to the drawer for display
			stickerHolder.addView(stickerItem);
			
			//increment our row counter to maintain correct number of items per row
			rowCounter ++;
			
			//dynamically add onClickListeners to each, which can use the global "currentSelected" variable to ensure 
			//we're always targeting the correct quadrant that the user selected, since it is updated when the drawer opens
			stickerItem.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// user selected an item, so close the drawer
					hideDrawer();
					System.out.println("Item in drawer clicked!");
					
					//grab the item that was selected using the created id (above)
					Drawable selected = getResources().getDrawable(resID);
					
					//grab the imageView that corresponds with the one the user wants to edit
					ImageView selectedTile = (ImageView) findViewById(currentSelected);
					
					//set the selected image to the user-selected tile
					selectedTile.setImageDrawable(selected);				
				}
				
			});
		}
	}
	
	//this method is run when the user taps the "Set!" button, and dynamically removes parts of the interface
	//that the user didn't modify in order to take a screenshot, and subsequently set it as the device wallpaper.
	private void setWallpaper() {
		//remove the "helper" label at the top so we don't save it as part of the screenshot
		TextView helperText = (TextView) findViewById(R.id.decorate_HelperText);
		helperText.setVisibility(View.GONE);
		
		//hide our button so we don't save it in the screenshot
		finishBtn.setVisibility(View.GONE);
		
		//check and remove any "unused" add icons on the screen so they aren't included in the image
		for (int i = 0; i < idArray.length; i++) {
			//create a reuseable imageView that we can then check the image it contains
			ImageView imageHolder = (ImageView) findViewById(idArray[i]);
			Drawable current = imageHolder.getDrawable();
			Drawable defaultImage = getResources().getDrawable(R.drawable.add_tile);
			if (current.getConstantState().equals(defaultImage.getConstantState())) {
				imageHolder.setVisibility(View.GONE);
			}
		}
		
		//set up bitmap object to hold original background that was shared
		Bitmap bitmap;
		
		//grab our first activity's background
		LinearLayout priorRoot = MainActivity.activityLayout;
		
		//make sure we're getting the root view
		View root = priorRoot.getRootView();
		
		//open up the drawing cache so we can use it
		root.setDrawingCacheEnabled(true);
		
		//create a bitmap object from the drawing cache
		bitmap = Bitmap.createBitmap(root.getDrawingCache());
		
		//close the drawing cache now that we're done
		root.setDrawingCacheEnabled(false);
		
		
		//grab this activity's background 
		Bitmap thisScreen;
		
		//grab our root view of this activity
		RelativeLayout thisView = (RelativeLayout) findViewById(R.id.decorate_rootView);
		
		//enable the drawing cache
		thisView.setDrawingCacheEnabled(true);
		
		//create a new bitmap of this activity from the cache
		thisScreen = Bitmap.createBitmap(thisView.getDrawingCache());
		
		//close the cache
		thisView.setDrawingCacheEnabled(false);
		
		//now combine the two, overlaying decorate activity's bitmap over the other to create our wallpaper
		Bitmap overlay = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
		Canvas canvas = new Canvas(overlay);
		canvas.drawBitmap(bitmap, new Matrix(), null);
		canvas.drawBitmap(thisScreen, 0, 0, null);
		
		//now set the saved image as the device's wallpaper
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.getApplicationContext());
		
		//determine what size the device wants the wallpaper to be.
		//this is where things get weird - on devices with "wallpaper scrolling" turned on,
		//this will not report the correct values, and results in a stretched image.
		int width = wallpaperManager.getDesiredMinimumWidth();
		int height = wallpaperManager.getDesiredMinimumHeight();
		
		try {
			//attempt to set the wallpaper using our wallpaperManager
			wallpaperManager.setBitmap(Bitmap.createScaledBitmap(overlay, width, height, false));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//restore visibility to interface
		helperText.setVisibility(View.VISIBLE);
		finishBtn.setVisibility(View.VISIBLE);
		
		//alert the user the wallpaper was set successfully
		Toast.makeText(getApplicationContext(), "Wallpaper set.  If wallpaper scrolling is enabled, stretching may appear.",
				   Toast.LENGTH_LONG).show();
		
		//return all "add" views to visible in case the user decides to further edit
		for (int i = 0; i < idArray.length; i++) {
			//create a reuseable imageView that we can then check the image it contains
			ImageView imageHolder = (ImageView) findViewById(idArray[i]);
			imageHolder.setVisibility(View.VISIBLE);
		}
		
		//set our boolean to true so MainActivity knows we updated our wallpaper
		wallpaperSet = true;
	}
	
}
