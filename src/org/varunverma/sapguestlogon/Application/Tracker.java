/**
@author Varun Verma (http://varunverma.org)

SAP Guest Logon provides you one touch & automatic logon into SAP's Guest network.
This program is developed by Varun Verma for Android OS
	
Copyright (c) 2011 Varun Verma (http://varunverma.org)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.varunverma.sapguestlogon.Application;

import android.content.Context;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Tracker {

	private GoogleAnalyticsTracker tracker;
	private static Tracker t;
	private boolean enabled;
	
	public static Tracker getInstance(){
		if(t == null){
			t = new Tracker(); 
		}
		return t;
	}
	
	public Tracker(){
		tracker = GoogleAnalyticsTracker.getInstance();
		enabled = Application.get_instance().AnalyticsEnabled;
	}

	public void start(String id, Context context) {
		// Start Tracker
		if(enabled){
			tracker.startNewSession(id, context);
		}
	}

	public void trackEvent(String category, String action, String opt_lable, int opt_value) {
		// Track Event
		if(enabled){
			tracker.trackEvent(category, action, opt_lable, opt_value);
		}
	}

	public void trackPageView(String page) {
		// Track Page View
		if(enabled){
			tracker.trackPageView(page);
		}
	}

	public void dispatch() {
		// Dispatch
		if(enabled){
			tracker.dispatch();
		}
	}

	public void setCustomVar(int index, String name, String value, int scope) {
		// Set custom Variable
		if(enabled){
			tracker.setCustomVar(index, name, value, scope);
		}
	}

	public void stop() {
		// Stop Tracker
		if(enabled){
			tracker.stopSession();
		}
	}
	
}