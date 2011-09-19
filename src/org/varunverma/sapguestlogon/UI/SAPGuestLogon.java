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

import org.varunverma.sapguestlogon.R;
import org.varunverma.sapguestlogon.Application.Application;
import org.varunverma.sapguestlogon.Application.LogonTask;
import org.varunverma.sapguestlogon.Application.LogonTaskProgressUpdate;
import org.varunverma.sapguestlogon.Application.SimpleCrypto;
import org.varunverma.sapguestlogon.Application.Tracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// This is the main activity.
public class SAPGuestLogon extends Activity implements LogonTaskProgressUpdate, OnClickListener {
	
	private Tracker tracker;
	private TextView tv;
	private Application application;
	private EditText master_key;
	private Dialog dialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	
    	// Initialize Application.
    	application = Application.get_instance();
    	application.context = getApplicationContext();
    	application.seedkey = R.string.seed;
    	application.initialize();

    	// Track via Google Analytics.
    	tracker = Tracker.getInstance();
    	tracker.start("UA-5272745-4", this);
    	application.tracker = tracker;
    	tracker.trackEvent("Version", "Version", "6.7", 0);
    	tracker.trackPageView("/Main");
    	
    	setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.tv);
        
        if(application.EULAAccepted){
        	StartMainActivity();
        }
        else{
        	// Show EULA.
        	Intent eula = new Intent(SAPGuestLogon.this, Eula.class);
			SAPGuestLogon.this.startActivityForResult(eula, Application.EULA);
        }
        
    }
	
	private void StartMainActivity() {
		// This is Main Activity.
		//Show what's new in this version
		show_whats_new();
        
        tv.setText("Welcome...\n");

        if(application.user.equals("")){
        	tv.append("Logon data not found!... Redirecting to preferences screen...\n");
        	Toast.makeText(application.context, "Maintain Logon Data first!", Toast.LENGTH_LONG).show();
			Intent my_preferences = new Intent(SAPGuestLogon.this, myPreferences.class);
			SAPGuestLogon.this.startActivity(my_preferences);
			tv.append("Relaunch the application\n");
        }
        else{
        	// Check if encryption is enabled or not.
        	if(application.EncryptionEnabled){
        		// Ask the user to enter the master key used for Encryption.
        		// This will be asked in a dialog box.
        		tv.append("Encryption is enabled...Requesting the user to enter the master key.\n");
        		/*/
        		//Only for testing. Remove this.
        		String info;
        		info = "User: " + application.user + "\n";
        		tv.append(info);
        		info = "Pwd: " + application.password + "\n";
        		tv.append(info);
        		info = "Encrypted Pwd: " + application.EPassword + "\n";
        		tv.append(info);
        		//*/
        		retrieveMasterKey();
        	}
        	else{
        		// Encryption not enabled. So no need to recover the password.
        		//Perform direct Logon.
        		performLogon();
        	}
        }
	}

	private void performLogon() {
		// Perform Logon.
		/*/
		//Only for testing. Remove this.
		String info;
		info = "User: " + application.user + "\n";
		tv.append(info);
		info = "Pwd: " + application.password + "\n";
		tv.append(info);
		info = "Encrypted Pwd: " + application.EPassword + "\n";
		tv.append(info);
		//*/
    	LogonTask logon = new LogonTask(application, this);
		logon.execute();
	}

	private void show_whats_new() {
		
		int curr_version = 12;
		int old_version = application.old_version;
		
		if(curr_version > old_version){
					
			Intent info = new Intent(SAPGuestLogon.this, DisplayInfo.class);
			info.putExtra("Type", R.raw.new_features);
			SAPGuestLogon.this.startActivity(info);
			
			SharedPreferences settings = getSharedPreferences(Application.Preferences, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("Version", curr_version);
			editor.commit();
		}
	}

	private void start_service() {
		// Start Service
		Intent i = new Intent(this, LogonService.class);
		long delay = 2 * 60 * 1000;		// 2 min in milli seconds
		i.putExtra("Delay", delay);
		//Start Service.
		startService(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		  
		 switch (item.getItemId()){
		 
		 case R.id.settings:
			 
			 Intent my_settings = new Intent(SAPGuestLogon.this, myPreferences.class);
			 SAPGuestLogon.this.startActivity(my_settings);
			 return true;

		 case R.id.Help:
			 
			 tracker.trackPageView("/Help");
			 Intent help = new Intent(SAPGuestLogon.this, DisplayInfo.class);
			 help.putExtra("Type", R.raw.help);
			 SAPGuestLogon.this.startActivity(help);
		     return true;
		 
		 case R.id.About:
			 
			 tracker.trackPageView("/About");
			 Intent about = new Intent(SAPGuestLogon.this, DisplayInfo.class);
			 about.putExtra("Type", R.raw.about);
			 SAPGuestLogon.this.startActivity(about);
		     return true;
		     
		 case R.id.New:
			 
			 tracker.trackPageView("/NewFeatures");
			 Intent info = new Intent(SAPGuestLogon.this, DisplayInfo.class);
			 info.putExtra("Type", R.raw.new_features);
			 SAPGuestLogon.this.startActivity(info);
		     return true;
		     
		 case R.id.EULA:
			 
			 Intent eula = new Intent(SAPGuestLogon.this, Eula.class);
			 SAPGuestLogon.this.startActivityForResult(eula, Application.EULA);
			 return true;
		 
		 case R.id.ExtraSettings:
			 
			 Intent extra_settings = new Intent(SAPGuestLogon.this, ExtraSettings.class);
			 SAPGuestLogon.this.startActivity(extra_settings);
			 return true;

		 }

		 return super.onOptionsItemSelected(item);
	 }

	@Override
	protected void onDestroy  (){
		tracker.dispatch();
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}

	public void UpdateTextView(String message) {
		// Update Text View
		tv.append(message);
	}

	public void TaskFinished(boolean result) {
        // Start a service to keep the connection on...
        start_service();
	}
	
	private void retrieveMasterKey(){
		
		String help_text = "Enter your private Key.\n" +
				"If you enter this key wrong, your password cannot be retrieved.\n" +
				"If you cannot remember this key, then un-install and install the app again.\n" +
				"NOTE: Your logon data will be wiped off when you un-install this app.";
		
		// Create Dialog box and ask user for the key.
		dialog = new Dialog(SAPGuestLogon.this);
		// Set Layout.
		dialog.setContentView(R.layout.master_key);
		// Set Title.
		dialog.setTitle("Enter your Encryption Key");
		// Set Texts.
		TextView help = (TextView) dialog.findViewById(R.id.EncryptionHelp);
		help.setText(help_text);
		// Get the Edit Text View.
		master_key = (EditText) dialog.findViewById(R.id.MasterKey);
		// Set the Button Click Listener.
		Button retrieve = (Button) dialog.findViewById(R.id.go);
		retrieve.setText("  Decrypt  ");
		retrieve.setOnClickListener(this);
		Button cancel = (Button) dialog.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
		// Now show the dialog.
		dialog.show();
	}

	public void onClick(View v) {
		// On Click of any View
		switch(v.getId()){
			
		case R.id.go:
			decryptPassword();
			break;
		
		case R.id.cancel:
			finish();
			break;
			
		default:
			break;
		}
	}

	private void decryptPassword() {
		// Decrypt Password.
		String mkey = master_key.getText().toString();
		try {
			application.password = SimpleCrypto.decrypt(mkey, application.EPassword);
			dialog.dismiss();
			application.mkey = mkey;
			// Now continue with logon.
			performLogon();
		} catch (Exception e) {
			// Error. Do not Exit the dialog box.
			String error = "Error occured while decrypting your password.\n" +
					"This can be due to wrong master key. Please enter a correct master Key.";
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Handle the result of an Activity.
		switch (requestCode) {
		
		case Application.EULA:
			
			if(application.EULAAccepted){
				// If EULA was accepted, Start Main Activity.
				StartMainActivity();
			}
			else{
				// EULA was rejected. So Finish Activity.
				finish();
			}
			break;
		
		}
	}
}