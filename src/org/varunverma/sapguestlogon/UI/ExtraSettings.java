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
import org.varunverma.sapguestlogon.Application.SimpleCrypto;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Advanced Preferences Screen.
 */

public class ExtraSettings extends Activity
						   implements OnClickListener {
	
	private Application application;
	private TextView tv_help;
	private CheckBox analytics, AutoLogon, encrypt;
	private String analytics_help, autologon_help, encryption_help;
	private Dialog dialog;
	private EditText master_key;
	private boolean encrypted, decrypted;
	
	public ExtraSettings(){

		analytics_help = "We do not track any personal information about the user.\n" +
					"Only the application usage statistics and crash reports are reported to the developer.\n";
		
		autologon_help = "Uses medium level encryption & enables Automatic Logon.\n" +
				"If Auto Logon is enabled, the application automatically performs logon whenever" +
				" you enter the SAP Guest Network range.\n" +
				"If you activate high level encryption using your own pass phrase, " +
				"this feature cannot be used.";
		
		encryption_help = "Encrypt the logon data using your own Passphrase.\n" +
				"This passphrase is not stored anywhere. Only you know this passphrase.\n" +
				"Without this passphrase, the logon data cannot be decrypted.\n" +
				"If this feature is activated, automatic logon will not work. You must enter this " +
				"passpharse everytime you launch the application.\n" +
				"NOTE: There is no way this passphrase can be recovered. So you must remember this.";
		
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.extra_preferences);
		
		String text = "";
		this.setTitle("SAP Guest Logon: Extra Preferences");
		application = Application.get_instance();
				
        tv_help = (TextView) findViewById(R.id.Help);
        tv_help.setTextColor(Color.GREEN);
        tv_help.setText(analytics_help);
        
        analytics = (CheckBox) findViewById(R.id.analytics);
        analytics.setText("Help make this app better by automatically sending usage statistics");
        
        text = "Use medium level Encryption & Logon Automatically when in SAP-Guest Network range.";
        AutoLogon = (CheckBox) findViewById(R.id.autologon);
        AutoLogon.setText(text);
        AutoLogon.setOnClickListener(this);
        
        encrypt = (CheckBox) findViewById(R.id.encrypt);
        encrypt.setText("Activate high level encryption using my own passphrase.");
        encrypt.setOnClickListener(this);
        
		put_defaults();
		
	}

	private void put_defaults() {
		// Put Default Values.
		analytics.setChecked(application.AnalyticsEnabled);
		encrypt.setChecked(application.EncryptionEnabled);
		AutoLogon.setChecked(application.AutomaticLogon);
		if(application.EncryptionEnabled){
			encrypted = true;
			decrypted = false;
		}
		else{
			encrypted = false;
			decrypted = true;
		}
		
	}

	public void onClick(View view) {
		// On Click of any View.
		switch (view.getId()){
		
		case R.id.analytics:
			tv_help.setText(analytics_help);
			break;
		
		case R.id.encrypt:
			tv_help.setText(encryption_help);
			AutoLogon.setChecked(false);
			encryptionToggled(true);
			break;
					
		case R.id.autologon:
			tv_help.setText(autologon_help);
			if(encrypt.isChecked()){
				AutoLogon.setChecked(false);
			}
			break;
					
		case R.id.go:
			if(application.EncryptionEnabled){
				decryptPassword();
			}
			else{
				encryptPassword();
			}
			break;
		
		case R.id.cancel:
			setEncryptedCheckBox();
			dialog.dismiss();
			
		default: break;
		
		}
	}
	
	private void setEncryptedCheckBox() {
		if(encrypt.isChecked()){
			if(!encrypted){
				encrypt.setChecked(false);
			}
		}else{
			if(!decrypted){
				encrypt.setChecked(true);
			}
		}
	}

	private void save_preferences() {
		// SAVE Preferences
		setEncryptedCheckBox();
		
		SharedPreferences settings = getSharedPreferences(Application.Preferences, 0);
		SharedPreferences.Editor editor = settings.edit();
				
		editor.putBoolean("Analytics", analytics.isChecked());
		editor.putBoolean("Encryption", encrypt.isChecked());
		editor.putBoolean("AutomaticLogon", AutoLogon.isChecked());
		
		editor.commit();
		
		// Reload Preferences after Save.
		application.read_preferences();
		
		if(application.SimpleEncryptionEnabled){
			try {
				application.EPassword = SimpleCrypto.encrypt(application.seed, application.password);
				editor.putString("Password", application.EPassword);
				editor.commit();
			} catch (Exception e) {
				// Oops !
				e.printStackTrace();
			}
		}
		
		Toast.makeText(getApplicationContext(), "Preferences Saved successfully", Toast.LENGTH_LONG).show();
		
		// Analytics tracking
		String info;
		if(this.analytics.isChecked()){	info = "Enabled";	}
		else{	info = "Disabled";	}
		application.tracker.setCustomVar(1, "Analytics_Active", info, 3);
		
		if(this.encrypt.isChecked()){	info = "Enabled";	}
		else{	info = "Disabled";	}
		application.tracker.setCustomVar(1, "Encryption_Enabled", info, 3);
		
		if(this.AutoLogon.isChecked()){	info = "Enabled";	}
		else{	info = "Disabled";	}
		application.tracker.setCustomVar(1, "Auto_Logon", info, 3);
		
		application.tracker.trackPageView("/ExtraSettings");
		// End of analytics tracking.
		
	}
	
	@Override
	protected void onDestroy(){
		save_preferences();
		application.tracker.dispatch();
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
	
	private void encryptionToggled(boolean Encryption){
		
		if(Encryption){	// Mean Extreme Encruption.
			// Encryption is enabled now. 
			// So we need to prompt for master key.
			String help_text = "Enter your private Key.\n" +
			"Remember this Private Key. This key will be used to Encrypt/Decrypt your Logon Data.\n" +
			"This key cannot be retrieved in any manner becuase only you know this key.\n" +
			"If you cannot remember this key, then un-install and install the app again. " +
			"Your logon data will be wiped off when you un-install this app.";
	
			// Create Dialog box and ask user for the key.
			dialog = new Dialog(ExtraSettings.this);
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
			Button save = (Button) dialog.findViewById(R.id.go);
			save.setText("  Encrypt  ");
			save.setOnClickListener(this);
			Button cancel = (Button) dialog.findViewById(R.id.cancel);
			cancel.setOnClickListener(this);

			// Now show the dialog.
			dialog.setCancelable(true);
			dialog.show();
		}
		else{
			// Encryption is disabled now.
			if(application.EncryptionEnabled){
				// Encryption was originally enabled.
				// So we will ask for the key again to verify.
				String help_text = "Enter your private Key.\n" +
				"If you enter this key wrong, you password cannot be retrieved.";
		
				// Create Dialog box and ask user for the key.
				dialog = new Dialog(ExtraSettings.this);
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
				Button save = (Button) dialog.findViewById(R.id.go);
				save.setText("  Decrypt  ");
				save.setOnClickListener(this);
				Button cancel = (Button) dialog.findViewById(R.id.cancel);
				cancel.setOnClickListener(this);
				// Now show the dialog.
				dialog.show();
			}
		}
		
	}
	
	private void encryptPassword(){
		
		String error;
		String mkey = master_key.getText().toString();
		
		if(mkey.length() < 4){
			error = "Lengt of Master key is too short !";
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			return;
		}
		try {
			String success = "Logon data encrypted successfully.";
			application.EPassword = SimpleCrypto.encrypt(mkey, application.password);
			SharedPreferences settings = getSharedPreferences(Application.Preferences, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("Password", application.EPassword);
			editor.commit();
			application.EncryptionEnabled = encrypted = true;
			decrypted = false;
			dialog.dismiss();
			Toast.makeText(this, success, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			// Error !
			encrypted = false;
			decrypted = true;
			error = "Error occured while Encrypting! Redo your operation.";
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	private void decryptPassword(){
		// Decrypt Password.
		String mkey = master_key.getText().toString();
		try {
			application.password = SimpleCrypto.decrypt(mkey, application.EPassword);
			SharedPreferences settings = getSharedPreferences(Application.Preferences, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("Password", application.password);
			editor.commit();
			decrypted = true;
			encrypted = false;
			application.EncryptionEnabled = false;
			dialog.dismiss();
		} catch (Exception e) {
			// Error. Do not Exit the dialog box.
			decrypted = false;
			encrypted = true;
			String error = "Error occured while decrypting your password.\n" +
					"This can be due to wrong master key. Please enter a correct master Key.";
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
}