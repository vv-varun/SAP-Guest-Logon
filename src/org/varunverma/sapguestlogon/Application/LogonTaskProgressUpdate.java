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

public interface LogonTaskProgressUpdate {
	
	public void UpdateTextView(String message);
	public void TaskFinished(LogonResult result);

}