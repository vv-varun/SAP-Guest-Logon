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

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

//	This is the main application class. Will be a singleton.

public class Application {
	
	// Static Constants.
	public static final String TAG = "SAPGuestLogon";
	static final String success = "Logon successful. Enjoy SAP Guest Network connection :-)";
	public static final String Preferences = "SAPGuestLogonPreferences";
	public static final int EULA = 1;
	public static int current_version;
	public static String Version;
	
	// Google Analytics Tracker.
	public Tracker tracker;
	
	// Logon Data
	String logonURL;						// Logon URL.
	public String user, password;			// In un-encrypted format.
	public String EPassword;				// Encrypted Logon Data.
	public String mkey;						// Master Key -- In case of Extreme Encryption.
	public String seed;						// Seed used in case of simple Encryption.
	
	// Preferences.
	public boolean isSAPUserId;					// SAP User Id or External User Id.
	public boolean AnalyticsEnabled;			// Analytics is enabled.
	public boolean EncryptionEnabled;			// Extreme Encryption is Enabled.
	public boolean SimpleEncryptionEnabled;		// Simple Encryption Enabled.
	public boolean AutomaticLogon;				// Automatic Logon Enaled.
	public boolean EULAAccepted;				// EULA Accepted.
	
	// To ensure Global State.
	public Context context;
	private static Application application;
	
	// Other Attributes.
	int CertificateId;
	public int old_version;
	public int seedkey;
	ConnectionManager connection_manager;
	public boolean initialized;
	
	public static Application get_instance(){
		// Application is a Singleton class.
		if(application == null){
			application = new Application();
		}
		return application;
	}
	
	private Application(){
		// Nothing to do in constructor.
		initialized = false;
		logonURL = "";
	}
	
	public void initialize(){
		// Initialize only if not initialized yet.
		
		// This is to deceive the general public... 
		// The real seed in the published application is different ;-)
		seed = (String) context.getResources().getText(seedkey);
		read_preferences();
		connection_manager = ConnectionManager.get_instance(context);
		
		try {
			current_version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			Version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			current_version = 0;
			Version = "1.0";
			Log.e(TAG, e.getMessage(), e);
		}
		
	}
	
	public String perform_logon() throws Exception {
		
		/*///
		We will trust only our own certificate...
		*////
		
		// Check Logon URL.
		if(logonURL.contentEquals("")){
			// URL is blank ! WTF !
			throw new Exception("Logon URL could not be determined. " +
					"Login is not possible. Please inform the developer.");
		}
		
        String user;
        if(password == null || password.contentEquals("")){
        	throw new Exception("Password not maintained.");
        }
        
		// Create a new HttpClient and Post Header 
        Log.i(Application.TAG, "Logging(HttpPost) with URL:" + logonURL);
        HttpClient httpclient = new myHTTPClient(context);
        HttpPost httppost = new HttpPost(logonURL);
        try {
        	// Try to Login
        	if(isSAPUserId){
        		user = this.user + "@SAP";
        	}
        	else{
        		user = this.user;
        	}
        	
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
        	nameValuePairs.add(new BasicNameValuePair("user", user));  
        	nameValuePairs.add(new BasicNameValuePair("password", password));
        	nameValuePairs.add(new BasicNameValuePair("visitor_accept_terms", "1"));
        	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  

        	//Execute HTTP Post Request  
        	@SuppressWarnings("unused")
        	HttpResponse response = httpclient.execute(httppost);  
        	
        	// Check if Internet Connection is available now or not !
        	connection_manager.IsInternetOn();
        	return success;

        } catch (SSLException e){
        	// Security Certificate was not validated !
        	tracker.trackPageView("/Failed");
        	throw e;
        }
        catch (Exception e) {
			// Any Generic Exception...
        	tracker.trackPageView("/Failed");
        	throw e;
		}
        
	}

	public void setEULAResult(boolean accepted) {
		// Set EULA Result
		SharedPreferences settings = context.getSharedPreferences(Application.Preferences, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putBoolean("EULA", accepted);		
		editor.commit();
		EULAAccepted = accepted;
	}
	
	public void read_preferences() {
		// Read the preferences --> Like the user, password and Location Id.
		SharedPreferences preferences = context.getSharedPreferences(Preferences, 0);
		
		// EULA Accepted.
		EULAAccepted = preferences.getBoolean("EULA",false);
		
		// Encryption Settings.
		EncryptionEnabled = preferences.getBoolean("Encryption",false);
		
		// Automatic Logon Settings.
		AutomaticLogon = preferences.getBoolean("AutomaticLogon",false);
		SimpleEncryptionEnabled = AutomaticLogon;
		
		// Read Logon Data
		ReadLogonData();
		
		// User Type?
		isSAPUserId = preferences.getBoolean("SAPUserId", true);
		// Analytics Settings.
		AnalyticsEnabled = preferences.getBoolean("Analytics", true);
		// Old Version.
		old_version = preferences.getInt("Version", 1);
		
	}

	private void ReadLogonData() {
		
		// Read the Logon Data from Settings / Preferences.
		
		if(initialized){
			return;
		}
		
		// Read LogonData from Preferences.
		SharedPreferences preferences = context.getSharedPreferences(Preferences, 0);
		
		// User Password.
		user = preferences.getString("User", "");
		if(EncryptionEnabled || SimpleEncryptionEnabled){
			EPassword = preferences.getString("Password", "");
		}
		else{
			password = preferences.getString("Password", "");
		}
		
		if(user.contentEquals("")){ }
		else {
			initialized = true;
		}
		
		// If Simple Encryption, then convert the password.
		if(SimpleEncryptionEnabled){
			try {
				password = SimpleCrypto.decrypt(seed, EPassword);
			} catch (Exception e) {
				// Should not happen.
				initialized = false;
			}
		}
	}

	public void updatePassword() throws EncryptionError {
		/*/
		*	I am not documenting this method for security reasons ;-)
		*	Anyways, not called from anywhere at the moment..
		/*/
		
		SharedPreferences settings = context.getSharedPreferences(Application.Preferences, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		try {
			
			EPassword = SimpleCrypto.encrypt(seed, application.password);
			editor.putString("Password", application.EPassword);
			editor.commit();
			
		} catch (Exception e) {
			// OMG !!! Shit happens in life... 
			// Sorry...
			EncryptionError error = new EncryptionError();
			throw error;
		}
		
	}

}