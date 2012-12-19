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

package org.varunverma.sapguestlogon.ui;

import org.varunverma.sapguestlogon.R;
import org.varunverma.sapguestlogon.Application.Application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * This is the broad cast receiver. 
 * The method on Receive get triggered when the registered Intent is fired!
 */

public class StartApplication extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// This is called when WiFi State Changes.
		Application application = Application.get_instance();
		application.context = context;
		application.seedkey = R.string.seed;
		application.initialize();
		Log.v(Application.TAG, "Application Initialized from BroadCast Reciever.");
			
		if(!application.EULAAccepted){
			Log.w(Application.TAG, "Application stopped from BroadCast Reciever becuase EULA not accepted.");
			return;
		}
		
		if(application.EncryptionEnabled){
			// In case of Extreme Encryption, the Automatic Logon is not enabled.
			Log.w(Application.TAG, "Application stopped from BroadCast Reciever becuase Encryption is enabled!");
			return;
		}
		
		if(application.user.contentEquals("") || application.password.contentEquals("")){
			Log.w(Application.TAG, "Application stopped from BroadCast Reciever becuase LogonData not maintained!");
			return;
		}
		
		// Get Wifi Manager.
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wi = wm.getConnectionInfo();
		String ssid = wi.getSSID();
		if(ssid == null){
			Intent i = new Intent(context, LogonService.class);
			context.stopService(i);
			Log.w(Application.TAG, "Service STOPPED rom BroadCase Reciever");
			return;
		}
		if(ssid.contains("SAP") && ssid.contains("Guest")){
			// Start Service
			Intent i = new Intent(context, LogonService.class);
			long delay = 15 * 1000;		// 15 sec in milli seconds
			i.putExtra("Delay", delay);
			context.startService(i);
			Log.i(Application.TAG, "Service started rom BroadCase Reciever");
		}
		else{
			// Stop service in other cases also.
			Intent i = new Intent(context, LogonService.class);
			context.stopService(i);
			Log.w(Application.TAG, "Service STOPPED rom BroadCase Reciever");
		}
	}
}