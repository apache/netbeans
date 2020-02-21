/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.capture;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ProjectSupport.Model;

public final class CaptureInfo {
    public final String executable;
    public final int argc;
    public final String argv[];
    public final String workingDirectory;
    public final int pid;
    public final Model model;
    public final String hostName;

    public CaptureInfo(String executable,
	       int argc, String argv[],
	       String wd,
	       int pid,
	       Model model,
	       String hostName) {
	this.executable = executable;
	this.argc = argc;
	this.argv = argv;
	this.workingDirectory = wd;
	this.pid = pid;
	this.model = model;
	this.hostName = hostName;
    }

    public String quotedArgvString() {
	String string = "";			// NOI18N
	for (int cx = 1; cx < argc; cx++)
	    string += IpeUtils.quoteIfNecessary(argv[cx]) + " ";// NOI18N
	string = string.trim();
	return string;
    } 

    public String argvString() {

	// concatenate 'argv' array into 'string'
	// skip argv[0]

	String string = "";			// NOI18N
	for (int cx = 1; cx < argc; cx++) {
	    string += argv[cx] + " ";	// NOI18N
	} 
	string = string.trim();

	// truncate if too long
	String sub = string;
	try {
	    sub = string.substring(0, 50);
	} catch (StringIndexOutOfBoundsException x) {
	    return sub;
	}
	sub += " ...";			// NOI18N
	return sub;
	
    }

    /**
     * Truncate on the left and if truncated replace with ...'s
     */
    public String truncate(String str) {
	String sub = str;
	try {
	    int len = str.length();
	    sub = str.substring(len-50, len);
	} catch (StringIndexOutOfBoundsException x) {
	    return sub;
	}
	return "... " + sub;	// NOI18N
    } 

    public String messageString() {
	/* OLD
	// the HTML doesn't really seem to work for some reason
	String msg = "";		// NOI18N
	// LATER msg += "<html>";
	msg += "ss_attach is about to execute\n";
	msg += "\t" + truncate(executable) + " " + argvString() + "\n";
	msg += "in\n";
	msg += "\t" + truncate(workingDirectory) + "\n";
	msg += "Would you like to attach to it?\n";
	// LATER msg += "</html>";
	*/

	String msg = Catalog.format("FMT_CaughtMessage",	// NOI18N
				    truncate(executable),
				    argvString(),
				    truncate(workingDirectory));
	return msg;
    }

}
