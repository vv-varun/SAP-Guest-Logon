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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Utility Class to check internet connection is on or not !
 */

/**
 * @author Varun Verma (http://varunverma.org)
 *
 */
public class ConnectionManager {
	
	public Context context;
	private static final String google = "http://www.google.com";
	private static ConnectionManager instance;
	
	public static ConnectionManager get_instance(Context context){
		if(instance == null){
			instance = new ConnectionManager(context);
		}
		return instance;
	}
	
	private ConnectionManager(Context context){
		this.context = context;
	}
	
	public boolean isWifiOn() throws NoWiFi{
		//ConnectivityManager is used to check available network(s)
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null){
        	//no network is available
		    NoWiFi e = new NoWiFi();
		    throw e;
		}
        else {
        	//at least one type of network is available
		    return true;
        }
	}
	
	public boolean IsInternetOn() throws NoWiFi, URLRedirected, NoInternetConnection{
		
		if(isWifiOn() == false){
			return false;
		}
		else{
			// is the server reachable?
            try {
            		URL url = new URL(google);
            		HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            		urlc.setConnectTimeout(1000 * 5); // Timeout is in seconds
            		urlc.connect();
            		if (urlc.getResponseCode() == 200) {
            			//http response is OK
            			urlc.disconnect();
            			return true;
            		}
            		else{
            			@SuppressWarnings("unused")
						InputStream in = new BufferedInputStream(urlc.getInputStream());
            		    if (!url.getHost().equals(urlc.getURL().getHost())) {
            	    	// We are redirected to somewhere else !
            		    // This is where we need to post our logon data.
            		    	String redirected_url = urlc.getURL().toString();
							throw new URLRedirected(redirected_url);
            		    }
            			urlc.disconnect();
            			throw new NoInternetConnection();
            		}
                }
            catch (MalformedURLException e) {
            	throw new NoInternetConnection(e);
            }
			catch (IOException e) {
				throw new NoInternetConnection(e);
			}
		}
	}

}