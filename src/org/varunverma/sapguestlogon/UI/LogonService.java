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

package org.varunverma.sapguestlogon.UI;

import java.util.Timer;

import org.varunverma.sapguestlogon.Application.Application;
import org.varunverma.sapguestlogon.Application.LogonTimer;
import org.varunverma.sapguestlogon.Application.Tracker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service Class to Keep the itnernet connection ON!
 */

public class LogonService extends Service {
	
	private Timer timer;
	private Application application;
	private long delay;
	
	public LogonService() {
		// Constructor
		application = Application.get_instance();
		delay = 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// I don't know what to do here.
		return null;
	}

	@Override
	public void onCreate() {
		
		super.onCreate();
				
		if(application.context == null){
			application.context = getApplicationContext();
			Log.v(Application.TAG, "Application Initialized from Service.");
		}
		
		if(application.tracker == null){
			// Track via Google Analytics.
			Tracker tracker = Tracker.getInstance();
	    	tracker.start("UA-5272745-4", this);
	    	application.tracker = tracker;
	    	Log.v(Application.TAG, "Tracker Initialized from Service.");
		}
		
		// Nothing wrong in initialization.
		// Passwords are not read again !
		application.initialize();
		
	}
	
	private void start_timer() {
		// Create a timer task to run at interval of 5 minutes and check if connection is lost or not.
		// If lost, post again.
		long period;
		
		period = 5 * 60 * 1000; // 5 min in milli seconds
		
		timer = new Timer();
		LogonTimer logon_timer = new LogonTimer(this);
		
		timer.scheduleAtFixedRate(logon_timer, delay, period);
	}

	@Override
	public void onDestroy() {
		application.tracker.dispatch();
		application.tracker.stop();
		timer.cancel();
		timer.purge();
		Log.i(Application.TAG, "Service Stoped!");
		super.onDestroy();
	}
	
	public int onStartCommand(Intent intent, int flags, int startid) {
		
		if(intent == null){
			delay = 30000;
		}
		else{
			delay = intent.getLongExtra("Delay", 30000);
		}
		
		start_timer();
		return START_STICKY;
	}

	public void successful_logon() {
		// After successful logon, restart the timer.
		timer.cancel();
		timer.purge();
		start_timer();
	}
}