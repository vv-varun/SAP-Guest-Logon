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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

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
	private ConnectivityManager cm;
	
	public static ConnectionManager get_instance(Context context){
		if(instance == null){
			instance = new ConnectionManager(context);
		}
		return instance;
	}
	
	private ConnectionManager(Context context){
		this.context = context;
		//ConnectivityManager is used to check available network(s)
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	}
	
	public boolean isWifiOn() throws NoWiFi{
		
		Log.i(Application.TAG, "Checking if WiFi is ON.");
		if (cm.getActiveNetworkInfo() == null){
        	//no network is available
		    NoWiFi e = new NoWiFi();
		    Log.i(Application.TAG, "No WiFi available!");
		    throw e;
		}
        else {
        	//at least one type of network is available
        	Log.i(Application.TAG, "WiFi is ON :)");
		    return true;
        }
	}
	
	public boolean IsInternetOn() throws NoWiFi, URLRedirected, NoInternetConnection{
		
		Log.i(Application.TAG, "Checking if Internet is ON!");
		Application application = Application.get_instance();
		
		if(isWifiOn() == false){
			return false;
		}
		else{
			// is the server reachable?
            try {
            		String redirected_url = "";
            		URL url = new URL(google);
            		HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            		
            		// Force -- Don't use Cache..
            		urlc.setDefaultUseCaches(false);
            		urlc.setUseCaches(false);
            		
            		urlc.setConnectTimeout(5000 * 10); // Timeout is in seconds
            		urlc.connect();
            		
            		boolean isReachable = false;
            		
            		try{
            			// Check connection using HttpGet.
            			
            			String queryURL = "http://www.google.com/search?q=" + String.valueOf(System.currentTimeMillis());
            			Log.i(Application.TAG, "Tryng to Query:" + queryURL + " via HttpGet.");
            			
            			HttpClient httpclient = new DefaultHttpClient();
                		HttpGet httpGet = new HttpGet(queryURL);
                		HttpResponse response = httpclient.execute(httpGet);
                		
                		// Open Stream for Reading.
                		InputStream is = response.getEntity().getContent();
                		if(is.read() == -1){
                			isReachable = false;
                			Log.i(Application.TAG, "HttpGet tells not reachable!");                			
                		}
                		else{
                			isReachable = true;
                			Log.i(Application.TAG, "HttpGet tells reachable :)");
                		}
                		
            		} catch (Exception e){
            			isReachable = false;
            			Log.i(Application.TAG, "Exception in HttpGet connection test", e);
            		}
        			
            		@SuppressWarnings("unused")
            		InputStream in = new BufferedInputStream(urlc.getInputStream());
        		    int response_code = urlc.getResponseCode();
        		    Log.i(Application.TAG, "URL-Connection response code:" + String.valueOf(response_code));
        		    
        		    if(!isReachable || response_code!= 200){
        		    	// Not Reachable! OR Response was not OK !
            			// So get the Redirected URL.
        		    	
        		    	// Do we already have the redirected URL ?
        		    	if(application.logonURL.contentEquals("")){
        		    		// No, we don't have !!
        		    		redirected_url = urlc.getURL().toString();
                			Log.i(Application.TAG, "Internet Not available. URLC tells redirected URL=" + redirected_url);
                			
                			if(redirected_url.contentEquals(google)){
                			//	WTF .. how can I be redirected to myself !
                				Log.i(Application.TAG, "As per URLC, redirected URL is same as google. So setting it to SPACE");
                				redirected_url = "";
                			}
                			
                			if (redirected_url.contentEquals("")) {
                				// Hard code the redirected URL !
                				redirected_url = "https://wlan.sap.com/cgi-bin/login";
                				Log.i(Application.TAG, "Redirected URL manually set to wlan.sap.com");
                				redirected_url = validateURL(redirected_url);
                			}
                			
            		    }
            		    else{
            		    	redirected_url = application.logonURL;
            		    }           			

            		}
        		    else{
        		    	// HTTP response is OK && HttpGet returned something !
            			urlc.disconnect();
            			Log.i(Application.TAG, "Response code is 200 and HttpGet returned something !");
            			return true;
        		    }

        		    if (!redirected_url.contentEquals("")){
            			// Is the URL correct ?
            			if(redirected_url.contains("wlan.sap.com")){
            				throw new URLRedirected(redirected_url);
            			}
            			else{
            				Log.w(Application.TAG, "Oops! we have BAD URL: " + redirected_url);
            				throw new BADURLRedirection(redirected_url);
            			}          			
            		}
                }
            catch (MalformedURLException e) {
            	Log.e(Application.TAG, "Something wrong with URL!", e);
            	throw new NoInternetConnection(e);
            }
			catch (IOException e) {
				Log.e(Application.TAG, "Something wrong with IO!", e);
				throw new NoInternetConnection(e);
			}
		}
		
		return false;
	}

	private String validateURL(String redirected_url) throws MalformedURLException {
		// Validate if this URL is correct !
		URL url;
		HttpURLConnection urlc;
		Log.i(Application.TAG, "Validating wlan.sap.com becuase sometimes it could be wrong!");
		
		try {
			
			url = new URL(redirected_url);
			
			urlc = (HttpURLConnection) url.openConnection();
			// Force -- Don't use Cache..
			urlc.setDefaultUseCaches(false);
			urlc.setUseCaches(false);
			
			urlc.setConnectTimeout(5000 * 10); // Timeout is in seconds
			urlc.connect();
			
			@SuppressWarnings("unused")
			InputStream in = new BufferedInputStream(urlc.getInputStream());
		    int response_code = urlc.getResponseCode();
		    if(response_code != 200){
		    	redirected_url = "https://securelogin.arubanetworks.com/cgi-bin/login";
		    	Log.w(Application.TAG, "Could not connect to wlan.sap.com, so defaulting it to:" + redirected_url);
		    }
			
		} catch (IOException e) {
			redirected_url = "https://securelogin.arubanetworks.com/cgi-bin/login";
			Log.w(Application.TAG, "Exception occured while validating wlan.sap.com, so defaulting it to:" + redirected_url, e);
		}
		
		return redirected_url;
	}

}