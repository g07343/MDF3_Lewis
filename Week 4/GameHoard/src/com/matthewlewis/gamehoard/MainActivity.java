package com.matthewlewis.gamehoard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class MainActivity extends Activity {

	WebView searchView;
	
    @SuppressLint("SetJavaScriptEnabled") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //force no title and portrait
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        setContentView(R.layout.activity_main);
        
        //grab a reference to our webview used for searching
        searchView = (WebView) findViewById(R.id.searchWebView);
        
        //grab the webview's settings
        WebSettings webSettings = searchView.getSettings();
        
        //enable javascript for the webview
        webSettings.setJavaScriptEnabled(true);
        
        searchView.setWebChromeClient(new WebChromeClient());
        
        //load the local web page from assets
        searchView.loadUrl("file:///android_asset/search.html");
        
        //set our javascript interface for the search webview to the below "SearchInterface" class
        searchView.addJavascriptInterface(new SearchInterface(this), "Native");
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
    
    public class SearchInterface {
    	
    	Context _context;
    	
    	SearchInterface(Context context) {
    		_context = context;
    	}
    	
    	@JavascriptInterface
    	public void logString(String string) {
    		System.out.println("User wants to search:  " + string);
    	}
    	
    }
}
