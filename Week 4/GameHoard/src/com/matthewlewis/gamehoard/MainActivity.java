/*
 * Author Matthew Lewis
 * 
 * Project GameHoard
 * 
 * Package com.matthewlewis.gamehoard
 * 
 * File MainActivity.java
 * 
 * Purpose MainActivity contains both the native UI ("preview" webviews) and the main WebView responsible for containing
 * the HTML interface.
 * 
 */
package com.matthewlewis.gamehoard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class MainActivity extends Activity {

	WebView searchView;
	WebView preview1;
	WebView preview2;
	WebView preview3;
	WebView preview4;
	String enteredUrl;
	String[] saved = new String[4];
	
	
    @SuppressLint({ "SetJavaScriptEnabled", "ClickableViewAccessibility" }) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //force no title and portrait
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        setContentView(R.layout.activity_main);
        
        //grab a reference to our webviews 
        searchView = (WebView) findViewById(R.id.searchWebView);
        preview1 = (WebView) findViewById(R.id.webPreview_1);
        preview2 = (WebView) findViewById(R.id.webPreview_2);
        preview3 = (WebView) findViewById(R.id.webPreview_3);
        preview4 = (WebView) findViewById(R.id.webPreview_4);
        
        //set all of our preview webViews to not be clickable
        preview1.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		preview2.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
        
		preview3.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
        
		preview4.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
        
        //create default zoom/viewport settings for each of our preview webViews (reeeeaaaalllllyyyy small!)
        preview1.getSettings().setLoadWithOverviewMode(true);
        preview1.getSettings().setUseWideViewPort(true);
        preview1.setInitialScale(1);
        
        preview2.getSettings().setLoadWithOverviewMode(true);
        preview2.getSettings().setUseWideViewPort(true);
        preview2.setInitialScale(1);
        
        preview3.getSettings().setLoadWithOverviewMode(true);
        preview3.getSettings().setUseWideViewPort(true);
        preview3.setInitialScale(1);
        
        preview4.getSettings().setLoadWithOverviewMode(true);
        preview4.getSettings().setUseWideViewPort(true);
        preview4.setInitialScale(1);
        
        //by default, hide all of the preview webviews, which if enabled, will show themselves later
        preview1.setVisibility(View.GONE);
        preview2.setVisibility(View.GONE);
        preview3.setVisibility(View.GONE);
        preview4.setVisibility(View.GONE);
        
        //preview1.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        preview1.setWebChromeClient(new WebChromeClient());
        preview1.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
    		System.out.println("JAVASCRIPT_LOG:  " + string);
    	}
    	
    	//if the entered url is valid, this function will update the "preview" tile that displays a tiny
    	//thumbnail for the user to see and launch the intent to the website
    	@JavascriptInterface
    	public void showPreview(final String url, int previewPane) {
    		
    		//determine which preview pane to update according to the passed int
    		switch(previewPane) {
    		case 1:
    			
    			//create a runnable since this was called on the "JavaBridge" thread
    			preview1.post(new Runnable() {
					@Override
					public void run() {						
						//set our custom WebViewClient class to this particular webView
						preview1.setWebViewClient(new MyWebViewClient());
						preview1.loadUrl(url);
					}   				
    			});
    			
    			break;
    			
    		case 2:
    			
    			preview2.post(new Runnable() {
					@Override
					public void run() {						
						//set our custom WebViewClient class to this particular webView
						preview2.setWebViewClient(new MyWebViewClient());
						preview2.loadUrl(url);
					}   				
    			});
    			
    			break;
    			
    		case 3:
    			
    			preview3.post(new Runnable() {
					@Override
					public void run() {						
						//set our custom WebViewClient class to this particular webView
						preview3.setWebViewClient(new MyWebViewClient());
						preview3.loadUrl(url);
					}   				
    			});
    			
    			break;
    			
    		case 4:
    			
    			preview4.post(new Runnable() {
					@Override
					public void run() {						
						//set our custom WebViewClient class to this particular webView
						preview4.setWebViewClient(new MyWebViewClient());
						preview4.loadUrl(url);
					}   				
    			});
    			
    			break;
    		}
    	}   
    	
    	//this is the native method that is called when the user chooses to save the websites
    	@JavascriptInterface
    	public void saveToDevice(String[] array) {
    		for (int i = 0; i < array.length; i++) {
    			SharedPreferences prefs = _context.getSharedPreferences("com.matthewlewis.gamehoard", MODE_PRIVATE);
    			Integer sync = i + 1;
    			String convertedSync = sync.toString();
    			
    			Editor editor = prefs.edit();
    			System.out.println("Key to be saved is:  " + convertedSync + "  and value is:  " + array[i]);
    			editor.putString(convertedSync, array[i]).apply();
    			
    			//display confirmation that favorites were saved
    			Toast.makeText(_context, "Favorites saved!", Toast.LENGTH_LONG).show();
    			
    		}
    		
    	}
    	
    	//this method grabs individual saved url strings one by one, accepting a string as the key
    	//since trying to pass anything but strings between javascript and java is a nightmare
    	@JavascriptInterface
    	public String returnSaved (String idString) {
    		SharedPreferences prefs = _context.getSharedPreferences("com.matthewlewis.gamehoard", MODE_PRIVATE);
    		
    		if (prefs.contains(idString)) {
    			String saved = prefs.getString(idString, "null");
    			return saved;
    		} else {
    			return "null";
    		}
    	}
    	
    	//this method toggles the visibility of the 'preview' webviews
    	@JavascriptInterface
    	public void togglePreview(String previewNum, final String intention) {
    		WebView local = null;
    		
    		if (previewNum.equals("1")) {
    			local = preview1;
    		} else if (previewNum.equals("2")) {
    			local = preview2;
    		} else if (previewNum.equals("3")) {
    			local = preview3;
    		} else {
    			local = preview4;
    		}
    		
    		System.out.println("~~~~~~~~~Intention was:  " + intention + "  and number was:  " + previewNum + "~~~~~~~~~~~");
    		
    		//whatever the local webview was set to, convert to a finalized form and use a runnable to toggle it's visibility
    		final WebView finalizedLocal = local;
    		
    		//create a different runnable depending on the 'intention' string passed
    		//need to create two separate ones because checking the 'intention' never allows the else statement to run for some reason
    		if (intention.equals("VISIBLE")) {
    			finalizedLocal.post(new RunningTask() {
    				private volatile boolean isRunning = true;
    				
    				@Override
    				public void run() {
    					Thread thisThread = Thread.currentThread();
    					while (isRunning) {
    						// set the view to visible
        					finalizedLocal.setVisibility(View.VISIBLE);
        					this.kill(); 	
    					} 
    					thisThread.interrupt();
    				}   
    				
    				@Override
    				public void kill() {
    					isRunning = false;
    					System.out.println("***VISIBILITY THREAD KILLED***");
    					
    				}
    				
        		});
    		} else {
    			finalizedLocal.post(new RunningTask() {
    				private volatile boolean isRunning = true;
					@Override
					public void run() {
						Thread thisThread = Thread.currentThread();
						while (isRunning) {
							// set view to invisibile
							finalizedLocal.setVisibility(View.GONE);
							this.kill();
						}  
						thisThread.interrupt();										
					}
					
					@Override
					public void kill() {
						isRunning = false;
						System.out.println("***INVISIBLE THREAD KILLED***");
					}
    			});
    		}   		
    	}
    	
    	//this method is called from javascript when the user taps an invisible overlay sitting over
    	//the 'preview' panes
    	@JavascriptInterface
    	public void launchIntent(String url) {
    		Intent webIntent = new Intent(Intent.ACTION_VIEW);
    		webIntent.setData(Uri.parse(url));
    		startActivity(webIntent);
    	}
    	
    	private class MyWebViewClient extends WebViewClient {
    		@Override
    		public boolean shouldOverrideUrlLoading(WebView view, String url) {
    			System.out.println("Internal urlString is: " + url + "  and global string is:  " + enteredUrl);
    			
    			//return false so Android doesn't create an intent
    			return false;
    		}
    	}
    }
}
