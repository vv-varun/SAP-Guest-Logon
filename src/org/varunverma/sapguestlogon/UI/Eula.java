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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.varunverma.sapguestlogon.R;
import org.varunverma.sapguestlogon.Application.Application;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

// Shows End User License Agreement.
public class Eula extends Activity implements OnClickListener {
	
	private Application application;
	private String EULA_Text;
	private TextView EULA;
	private Button accept, decline, SAPEULA;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eula);
		setTitle("End-User License Agreement");
		
		EULA = (TextView) findViewById(R.id.EULA);
		//EULA.setBackgroundResource(R.color.white);
		//EULA.setTextColor(getResources().getColor(R.color.black));
		EULA.setTextColor(getResources().getColor(R.color.white));
		
		accept = (Button) findViewById(R.id.Accept);
		accept.setOnClickListener(this);
		
		decline = (Button) findViewById(R.id.Reject);
		decline.setOnClickListener(this);
		
		SAPEULA = (Button) findViewById(R.id.SAPEULA);
		SAPEULA.setOnClickListener(this);
		
		application = Application.get_instance();
		
		// Read EULA text from the file.
		readEULA();
		
		// Now display this EULA.
		Spannable eula = (Spannable) Html.fromHtml(EULA_Text);
		EULA.setText(eula);
		
	}

	private void readEULA() {
		// Read EULA from Raw Resources.
		Resources res = getResources();
        InputStream is = res.openRawResource(R.raw.eula);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        EULA_Text = "";
        String line = "";
        try {
			while((line = reader.readLine()) != null){
				EULA_Text = EULA_Text + line;
			}
		} catch (IOException e) {
			// OOps... So let's pretend that user did not accept the EULA.
			EULADeclined();
		}
	}
	
	private void EULAAccepted(){
		
		application.setEULAResult(true);
		setResult(RESULT_OK);       
		this.finish();
	}
	
	private void EULADeclined(){
		
		application.setEULAResult(false);
		setResult(RESULT_OK);       
		this.finish();
	}

	public void onClick(View v) {
		// Handle Button click
		switch(v.getId()){
		
		case R.id.Accept:
			EULAAccepted();
			break;
			
		case R.id.Reject:
			EULADeclined();
			break;
			
		case R.id.SAPEULA:
			String url = "https://apj-guest.wlan.sap.com/public/acceptableusepolicy.htm";
			final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
			startActivity(intent);
			break;
		
		}		
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
}