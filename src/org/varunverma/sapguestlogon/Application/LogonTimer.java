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

import java.util.TimerTask;

import org.varunverma.sapguestlogon.R;
import org.varunverma.sapguestlogon.UI.LogonService;
import org.varunverma.sapguestlogon.UI.SAPGuestLogon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Timer Task 
 */

/**
 * @author Varun Verma (http://varunverma.org)
 *
 */

public class LogonTimer extends TimerTask {
	
	private LogonService service;
	private Application application;
	
	public LogonTimer(LogonService service){
		this.service = service;
		application = Application.get_instance();
	}
	
	@Override
	public void run() {
		// Perform Logon.
		try {
			Log.i(Application.TAG, "Time RUN started...");
			// Check if Internet is on.
			application.connection_manager.IsInternetOn();
			
		} catch (BADURLRedirection e){
			
			// We are re-directed to a wrong URL !!
			String message = "WARNING: Redirected to an un-expected URL! :" + e.get_redirected_url() + 
					"Automatic Logon will not be Performed, becuase this is a BAD URL !";
			Log.w(Application.TAG, message);
			Log.w(Application.TAG, "Manual Logon is required!");
			
			notifyBadUrlManualLogon();
			
			
		} catch (URLRedirected e) {
			// No, we are redirected to some URL !
			if(application.logonURL.contentEquals("")){
				application.logonURL = e.get_redirected_url();
			}
			// So Logon.
			try {
				// Logging.
				application.perform_logon();
				// Success :-)
				service.successful_logon();
				
			} catch (Exception e1) {
				// Oops !
				Log.e(Application.TAG, "Error in timer RUN", e1);
			}
			
		} catch (Exception e) {
			// Oops !
			Log.e(Application.TAG, "Error in timer RUN", e);
		}      
	}

	private void notifyBadUrlManualLogon() {
		// Notify Bad URL Logon
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) application.context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent notificationIntent = new Intent(application.context, SAPGuestLogon.class);
		PendingIntent contentIntent = PendingIntent.getActivity(application.context, 0, notificationIntent, 0);
		
		String tickerText = "Manual Logon Required...";
		String contentText = "Redirected to an un-expected URL. Hence manual logon is required..";
		
		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(R.drawable.icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		
		notification.setLatestEventInfo(application.context, "Attention!", contentText, contentIntent);
		notificationManager.notify(1, notification);
		
	}

}