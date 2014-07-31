/*
 * Author Matthew Lewis
 * 
 * Project GameHoard
 * 
 * Package com.matthewlewis.gamehoard
 * 
 * File RunningTask.java
 * 
 * Purpose This is an extended version of the Runnable class that allows us to manually kill the thread, since we are basically using
 * it to control the visibility of the webview 'preview' objects, and not anything too intense.
 * 
 */
package com.matthewlewis.gamehoard;

public class RunningTask implements Runnable{
	
	private volatile boolean isRunning = true;
	
	public void run() {
		while (isRunning) {
			
		}
	}
	
	public void kill() {
		isRunning = false;
	}
}
