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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Preferences Screen.
 */

public class myPreferences extends Activity
						   implements OnClickListener {
	
	private Application application;
	private TextView tv_user, tv_pwd, tv_help;
	private RadioButton rb_int, rb_ext;
	private String help_text_int, help_text_ext;
	
	public myPreferences(){
		help_text_int = "Enter you SAP User Id (I,D,C Number) & SAP Domain Password." +
				"\nDo not add @SAP in the end!";
		
		help_text_ext = "Create a user id for yourself on SAP Corporate Portal." +
		"\nQuick link on corporate portal: /go/gia" +
		"\nEnter the user/password here." +
		"\nNote: After this user is expired, you must enter the new user/password here again.";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		
		this.setTitle("SAP Guest Logon: Preferences");
		application = Application.get_instance();
				
		final Button clear_button = (Button) findViewById(R.id.save);
        clear_button.setOnClickListener(this);
                
        tv_user = (TextView) findViewById(R.id.User);
        tv_pwd = (TextView) findViewById(R.id.Password);
        tv_help = (TextView) findViewById(R.id.Help);
        tv_help.setTextColor(Color.GREEN);
        
        rb_int = (RadioButton) findViewById(R.id.internal);
        rb_ext = (RadioButton) findViewById(R.id.external);
        
        rb_int.setOnClickListener(this);
        rb_ext.setOnClickListener(this);
        
		put_defaults();
		
	}

	private void put_defaults() {
		// Put Default Values.
		String user = application.user;
		String password = application.password;
		
		EditText edittext = (EditText) findViewById(R.id.user);
		edittext.setText(user);
		
		edittext = (EditText) findViewById(R.id.password);
		edittext.setText(password);
		
		if(application.isSAPUserId){
			rb_int.setChecked(true);
		}
		else{
			rb_ext.setChecked(true);
		}
				
		if(rb_int.isChecked()){
			tv_user.setText("Enter SAP User Id");
			tv_pwd.setText("Enter SAP Domain Password");
			tv_help.setText(help_text_int);
		}else{
			tv_user.setText("Enter User Name");
			tv_pwd.setText("Enter Password");
			tv_help.setText(help_text_ext);
		}
		
	}

	public void onClick(View view) {
		// On Click of any View
		switch (view.getId()){
		
		case R.id.internal:
			tv_user.setText("Enter SAP User Id");
			tv_pwd.setText("Enter SAP Domain Password");
			tv_help.setText(help_text_int);
			break;
		
		case R.id.external:
			tv_user.setText("Enter User Name");
			tv_pwd.setText("Enter Password");
			tv_help.setText(help_text_ext);
			break;
			
		case R.id.save:
			save_preferences();
			break;
		
		}
	}

	private void save_preferences() {
		// SAVE Preferences.
		String message; 
		
		SharedPreferences settings = getSharedPreferences(Application.Preferences, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		EditText edittext = (EditText) findViewById(R.id.user);
		String user = edittext.getText().toString();
		if(rb_int.isChecked()){
			user = user.toUpperCase();
		}
		
		edittext = (EditText) findViewById(R.id.password);
		String password = edittext.getText().toString();
		
		if(validate_data(user,password)){ }
		else{
			// Error...
			return;
		}
		
		// Encrypt Password before Saving It.
		try {
			
			if(application.EncryptionEnabled){
				password = SimpleCrypto.encrypt(application.mkey, password);
			}
			
			if(application.SimpleEncryptionEnabled){
				password = SimpleCrypto.encrypt(application.seed, password);
			}
			
			editor.putString("User", user);
			editor.putString("Password", password);
			editor.putBoolean("SAPUserId", rb_int.isChecked());	// X: Internal, '': External
			
			editor.commit();
			application.initialized = false;
			application.read_preferences();
			
			message = "Preferences Saved successfully";
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			
		} catch (Exception e) {
			// Error !
			e.printStackTrace();
			message = "Error while encryption! Redo your action.";
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			return;
		}
		
		// Analytics tracking
		String user_type;
		if(rb_int.isChecked()){ user_type = "SAP User Id"; }
		else{ user_type = "Temporary User Id"; }
		
		application.tracker.setCustomVar(1, "SAPUserId", user_type, 1);
		application.tracker.trackPageView("/Settings");
		// End of analytics tracking.
		
	}
	
	private boolean validate_data(String user, String password) {
		// Validate User Id
		if(user.equals("") || user == null){
			//User is not entered!
			Toast.makeText(getApplicationContext(), "Enter a User Name!", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(password.equals("") || password == null){
			//Password is not entered!
			Toast.makeText(getApplicationContext(), "Enter a Password!", Toast.LENGTH_LONG).show();
			return false;
		}
		
		// Validate a user id.
		if(rb_int.isChecked()){
			//Internal User Id.
			char u = user.charAt(0);
			if(u != 'I' && u != 'D' && u != 'C'){
				Toast.makeText(getApplicationContext(), "This is not a valid SAP User Id!", Toast.LENGTH_LONG).show();
				return false;
			}
			
			if( (user.length() != 7) && (user.length() != 8)){
				Toast.makeText(getApplicationContext(), "This is not a valid SAP User Id!", Toast.LENGTH_LONG).show();
				return false;
			}
		}
		else{
			//External User Id.
		}
		
		return true;
		
	}

	@Override
	protected void onDestroy(){
		application.tracker.dispatch();
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
	
}