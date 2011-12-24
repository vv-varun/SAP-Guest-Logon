package org.varunverma.sapguestlogon.Application;

public class LogonResult {
	
	/*//
	0	= Success :)
	1	= BAD URL !
	2	= No WiFi !
	
	100 = Unknown error !
	//*/
	
	boolean success;
	int resultCode;
	String badURL;
	
	public LogonResult(int code){
		resultCode = code;
	}
	
	public int getResultCode(){
		return resultCode;
	}

	public String getBadURL() {
		return badURL;
	}

	public boolean isTaskSuccessful(){
		return success;
	}
	
}