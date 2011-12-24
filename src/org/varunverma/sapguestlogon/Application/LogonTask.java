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

import javax.net.ssl.SSLException;

import org.varunverma.sapguestlogon.R;

import android.os.AsyncTask;
import android.util.Log;

public class LogonTask extends AsyncTask<Void, String, LogonResult> {
	
	private LogonTaskProgressUpdate caller;
	private Application application;
	
	public LogonTask(Application application, LogonTaskProgressUpdate caller) {
		this.application = application;
		this.caller = caller;
	}

	@Override
	protected LogonResult doInBackground(Void... params) {
		// Start the job...
		LogonResult result;
		Log.i(Application.TAG, "Logon Task started.");
		
		try {
			publishProgress("Checking Internet Connection...\n");
			// Check if Internet is on... 
			application.connection_manager.IsInternetOn();
			
			// Yes, it is :-)
			publishProgress("Internet is already on...\n");
			publishProgress("Happy Surfing :-)\n");
			Log.i(Application.TAG, "Internet is already ON!");
			
			result = new LogonResult(0);
			result.success = true;
			return result;
			
		} catch (NoWiFi e) {
			// No Wifi !
			publishProgress(e.getMessage() + "\n");
			result = new LogonResult(2);
			result.success = false;
			return result;

		} catch (BADURLRedirection e){
			// We are re-directed to a wrong URL !!
			publishProgress("WARNING: Redirected to an un-expected URL! :" + e.get_redirected_url() + "\n");
			
			result = new LogonResult(1);
			
			// Use Aruba Networks Certificate in this case.
			application.CertificateId = R.raw.arubanetworks;
			application.logonURL = result.badURL = e.get_redirected_url();

			result.success = false;
			
			return result;
			
		} catch (URLRedirected e) {
			// URL Redirected ! Looks like we have to logon ...
			// Set the Logon URL.
			application.logonURL = e.get_redirected_url();
			
			result = performLogon();
			return result;
			
		} catch (NoInternetConnection e) {
			// No, Internet connection available !
			publishProgress(e.getMessage() + "\n");
			result = new LogonResult(100);
			result.success = false;
			return result;
		}
		
	}
	
	protected void onProgressUpdate(String... progress){
		// Update UI with Message.
		caller.UpdateTextView(progress[0]);
		Log.i(Application.TAG, progress[0]);
	}
	
	protected void onPostExecute(LogonResult result) {
		// Send the result back.
		caller.TaskFinished(result);
	}

	private LogonResult performLogon(){
		
		LogonResult result;
		Log.i(Application.TAG, "Performing Logon from Logon Task....");
		
		try {
			// By-Default use the new certificate.
			application.CertificateId = R.raw.sapwlan_new;
			
			// Perform Logon.
			publishProgress("Logging to SAP Guest Network...\n");
			String message = application.perform_logon();
			message = message + "\n";
			publishProgress("Security Certificates validated...\n");
			publishProgress(message);
			
			result = new LogonResult(0);
			result.success = true;
			return result;
			
		} catch (SSLException se){
			
			String message = "";
			try {
				
				publishProgress("The new Security Certificate was not validated !...\n");
				publishProgress("This could be becuase you are in a location where the certificate is not updated yet!\n");
				publishProgress("Let's try again with old certificate...\n");
			
				// Now, try to use the old certificate
				application.CertificateId = R.raw.sapwlan;
			
				// Try to Perform Logon again.
				publishProgress("Re-attempt to Login to SAP Guest Network...\n");
				message = application.perform_logon();
				
				message = message + "\n";
				publishProgress("Security Certificates validated...\n");
				publishProgress(message);
				
				result = new LogonResult(0);
				result.success = true;
				return result;
				
			} catch (Exception e1) {
				// The 2nd attempt with old certificate also failed !
				String error = "Following error occured while logon: " + e1.getMessage() + "\n";
				publishProgress(error);
				Log.e(Application.TAG, error, e1);
				result = new LogonResult(100);
				result.success = false;
				return result;
			}
			
		} catch (Exception e1) {
			// Error while Logon !
			String error = "Following error occured while logon: " + e1.getMessage() + "\n";
			publishProgress(error);
			Log.e(Application.TAG, error, e1);
			result = new LogonResult(100);
			result.success = false;
			return result;
		}
		
	}
	
}