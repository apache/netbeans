/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
