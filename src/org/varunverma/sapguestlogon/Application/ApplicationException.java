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

// The main application Exception class. Other exceptions inherit from this..
public class ApplicationException extends Exception {

	private static final long serialVersionUID = -5921841610370377652L;
	
	protected Exception e;
	
	public ApplicationException(Exception e) {
		this.e = e;
	}
	
	@Override
	public String getMessage(){
		return e.getMessage();
	}
	
}

@SuppressWarnings("serial")
class CertificateException extends ApplicationException {
	
	public CertificateException(){
		super(new Exception("Certificate(s) could not be validated!"));
	}
	
	public CertificateException(String message){
		super(new Exception(message));
	}
	
}

@SuppressWarnings("serial")
class NoWiFi extends ApplicationException {

	public NoWiFi() {
		super(new Exception("No Wifi Networks available!"));
	}
	
}

@SuppressWarnings("serial")
class NoInternetConnection extends ApplicationException {
	
	public NoInternetConnection(Exception e){
		super(e);
	}
	
	public NoInternetConnection() {
		super(new Exception("No Internet Connection available!"));
	}
	
}

@SuppressWarnings("serial")
class URLRedirected extends ApplicationException {
	
	private String redirected_url;
	
	public URLRedirected(String url) {
		super(new Exception("URL has been redirected."));
		redirected_url = url;
	}
	
	public String get_redirected_url(){
		return redirected_url;
	}
	
}