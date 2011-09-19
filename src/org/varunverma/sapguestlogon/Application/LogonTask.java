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

import android.os.AsyncTask;

public class LogonTask extends AsyncTask<Void, String, Boolean> {
	
	private LogonTaskProgressUpdate caller;
	private Application application;
	
	public LogonTask(Application application, LogonTaskProgressUpdate caller) {
		this.application = application;
		this.caller = caller;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// Start the job...
		Boolean success;
		
		try {
			publishProgress("Checking Internet Connection...\n");
			// Check if internet is on... 
			application.connection_manager.IsInternetOn();
			
			// Yes, it is :-)
			publishProgress("Internet is already on...\n");
			publishProgress("Happy Surfing :-)\n");
			success = true;
			
		} catch (NoWiFi e) {
			// No Wifi !
			publishProgress(e.getMessage() + "\n");
			success = false;

		} catch (URLRedirected e) {
			// URL Redirected ! Looks like we have to logon ...
			try {
				// Perform Logon.
				publishProgress("Logging to SAP Guest Network...\n");
				String message = application.perform_logon(e.get_redirected_url());
				message = message + "\n";
				publishProgress("Security Certificates validated...\n");
				publishProgress(message);
				success = true;
				
			} catch (Exception e1) {
				// Error while Logon !
				String error = "Following error occured while logon: " + e1.getMessage() + "\n";
				publishProgress(error);
				success = false;
				
			}
			
		} catch (NoInternetConnection e) {
			// No, internet connection available !
			publishProgress(e.getMessage() + "\n");
			success = false;
		}
		
		return success;
	}
	
	protected void onProgressUpdate(String... progress){
		// Update UI with Message.
		caller.UpdateTextView(progress[0]);
	}
	
	protected void onPostExecute(Boolean result) {
		
		if(result){
			// Show Success
			caller.UpdateTextView("Done... Bye.\n");
		}
		else{
			// Show Failure
			caller.UpdateTextView("Please try again... \n");
		}
		caller.TaskFinished(result);
	}

}