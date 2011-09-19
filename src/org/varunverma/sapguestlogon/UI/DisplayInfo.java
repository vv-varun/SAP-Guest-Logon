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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.varunverma.sapguestlogon.R;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.webkit.WebView;

//	Utility Class to display static HTML File (for Help and About info)
public class DisplayInfo extends Activity {
	
	private String html_text;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        
        WebView my_web_view = (WebView) findViewById(R.id.info);       
        my_web_view.getSettings().setJavaScriptEnabled(true);
        
        int res_id = this.getIntent().getIntExtra("Type", -1);
        if(res_id == -1){
        	return;
        }
        
        // Read HTML file from project source
        get_html_from_file(res_id);
        
        my_web_view.loadData(html_text, "text/html", "utf-8");
		
	}

	private void get_html_from_file(int res_id) {
		// Read HTML file from project source.
		Resources res = getResources();
        InputStream is = res.openRawResource(res_id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        html_text = "";
        String line = "";
        try {
			while((line = reader.readLine()) != null){
				html_text = html_text + line;
			}
		} catch (IOException e) {
			// Exception ! Ignore for time being.
			e.printStackTrace();
		}
	}	
}