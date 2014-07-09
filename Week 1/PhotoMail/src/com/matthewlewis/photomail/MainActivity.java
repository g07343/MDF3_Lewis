package com.matthewlewis.photomail;


import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.provider.MediaStore;

public class MainActivity extends Activity {

	LinearLayout activityLayout;
	LayoutParams imageParams;
	int screenHeight;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageHolder = (ImageView) findViewById(R.id.image_holder);
        
        activityLayout = (LinearLayout) findViewById(R.id.activity_holder);
        imageParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        //Get our intent to see if our app was started by another app
        Intent intent = getIntent();
        
        String receivedAction = intent.getAction();
    	
    	String receivedType = intent.getType();
    	
    	DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	
    	screenHeight = metrics.heightPixels;
    	
    	
    	
    	if (receivedType != null && receivedType.startsWith("image/")) {
    		if (receivedAction.equals("android.intent.action.SEND")) {
    			
        		
        		
        		
        		
        		
        		Uri receivedUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        		
        		imageHolder.setImageURI(receivedUri);
        		
        		
        		
        		//imageParams = imageHolder.getLayoutParams();
        		
        		
        		
        		
        		
        		
        		
    		} else if (receivedAction.equals("android.intent.action.SEND_MULTIPLE")) {
    			System.out.println("Multiple images sent!");
    			Bundle extras = intent.getExtras();
    			imageHolder.setVisibility(View.GONE);
    			System.out.println("Keys in intent were:  " + extras.keySet().size());
    			
    			for (String key : extras.keySet()) {
    				System.out.println("Key was:  " + key);
    				ArrayList<Parcelable> array = (ArrayList<Parcelable>) intent.getParcelableArrayListExtra(key);
    				System.out.println("Object within the key was:  " + array);
    				
    				for (int i =0; i < array.size(); i++) {
    					ImageView receivedHolder = new ImageView(this);
    					Uri receivedUri = (Uri) array.get(i);
    					System.out.println("Looping through...Uri is:  " + receivedUri);
    					receivedHolder.setImageURI(receivedUri);
    					receivedHolder.setLayoutParams(imageParams);
    					receivedHolder.setScaleType(ImageView.ScaleType.FIT_XY);
    					int maxHeight = screenHeight/array.size();
    					receivedHolder.getLayoutParams().height = maxHeight;
    					activityLayout.addView(receivedHolder);
    				}
    				activityLayout.requestLayout();
    			}
    		}
    		
    	}
        
        
        
        //create a string array to hold columns of images so we can go through them one by one
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media._ID;
        //Stores all the images from the gallery in Cursor
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
        //Total number of images
        int count = cursor.getCount();

        //Create an array to store path to all the images
        String[] arrPath = new String[count];

        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            //Store the path of the image
            arrPath[i]= cursor.getString(dataColumnIndex);
            //Log.i("PATH", arrPath[i]);
        }  
        cursor.close();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
