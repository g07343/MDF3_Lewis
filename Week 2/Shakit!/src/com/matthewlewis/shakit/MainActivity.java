package com.matthewlewis.shakit;

import java.io.IOException;

import android.app.Activity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

	String[] songTitles;
	String[] songPaths;
	String[] songLengths;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //grab all of the audio on the device, so we can set up our interface
        getAllAudio();
        
        
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
    
    
    //this method is called within the onCreate() function, and basically gathers a list of music that is contained on the user's device
    private void getAllAudio() {
        Cursor musicCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA }, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        //grab the number of found items so we can create arrays of appropriate size
        int numSongs = musicCursor.getCount();

        //set up two string arrays, one to hold the name of each found audio clip, and another to contain the filepaths
        songTitles = new String[numSongs];
        songPaths = new String[numSongs];
        songLengths = new String[numSongs];
        
        int i = 0;
        //grab all of our music files and and store within arrays
        if (musicCursor.moveToFirst()) {
            do {
            	
                songTitles[i] = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                songPaths[i] = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                
                //since our cursor is apparently unable to get the duration of the media it finds, load each into a
                //"temporary" mediaPlayer, in order to grab the duration
                MediaPlayer testPlayer = new MediaPlayer();
                String musicLocation = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                Uri currentLocation = Uri.parse(musicLocation);
                try{               	
                	testPlayer.setDataSource(getApplicationContext(), currentLocation);
                	testPlayer.prepareAsync();
                	
                	float duration = testPlayer.getDuration() / 1000;
                	songLengths[i] = Float.toString(duration);
                	System.out.println("Duration of audio is:  " + duration);
                } catch (IOException e) {
                	System.out.println("Catch block triggered...");
                	
                	//for some reason, couldn't get the duration, so set to unknown for checking later
                	songLengths[i] = "unknown";
                }
                //get rid of our temporary media player object so we aren't holding onto multiple instances needlessly
                testPlayer.reset();
                testPlayer.release();
                i++;
                
                //move our cursor object until we get to a new audio file
            } while (musicCursor.moveToNext());
        }   

        //close our cursor object
        musicCursor.close();
    }
}
