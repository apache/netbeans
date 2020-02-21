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

package org.netbeans.modules.cnd.debugger.common2.debugger;

public class Frame {
    protected NativeDebugger debugger;
    protected boolean range_of_hidden;
    protected boolean current;
    protected String func;
    protected String loadobj;
    protected String loadobj_base;
    protected String args;
    protected String source;
    protected String lineno;
    protected String frameno;
    protected String pc;
    protected boolean optimized;
    protected boolean attr_user_call;
    protected int attr_sig;
    protected String attr_signame;
    public boolean more;
    private String signal = null;
    /*private*/protected Thread thread = null;
    
    public Frame(NativeDebugger debugger, Thread thread) {
	this.debugger = debugger;
        this.thread = thread;
    }

    public boolean isSpecial() {
	return range_of_hidden ||
	       attr_user_call ||
	       more ||
	       attr_sig != 0;
    }

    public boolean isSignalHandler() {
	return attr_sig != 0;
    }

    public boolean isUserCall() {
	return attr_user_call;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean b) {
	current = b;
    }

    public boolean isHidden() {
        return range_of_hidden;
    }

    public boolean hasDebuggingInfo() {
	if (source == null || source.length() == 0)
	    return false;
        else
	    return true;
    }

    public void setFunc(String f) {
	func = f;
    }

    public String getFunc() {
	return func;
    }

    public String getLineNo() {
	return lineno;
    }

    public String getSource() {
	return source;
    }

    public String getLocationName() {

        // SHOULD add some indication that there are more frames and
        // an action to get them.

        if (range_of_hidden) {
            return "---- " +  Catalog.get("MSG_HIDDDEN_FRAMES") + " ----"; // NOI18N
        } else if (attr_user_call) {
            return "---- " +  Catalog.get("MSG_DEBUGGER_CALL") + " ----"; // NOI18N
        } else if (attr_sig != 0) {
            return "---- " +    // NOI18N
                   Catalog.format("MSG_SIGNAL_HANDLER", // NOI18N
				  attr_signame, attr_sig) +
                   " ----";     // NOI18N

        } else if (func == null) {
            return "?()";       // NOI18N
        } else if (args == null) {
            if (more)
                return func;  // NOI18N
            else
                return func + "(?)";  // NOI18N

        } else {
            return func + args;
        }
    }
    
    public String getFullPath() {
        return null;
    }

    public String getLocation() {
        if (isSpecial())
            return  "";                  // NOI18N
        if (!hasDebuggingInfo())
            return "";                  // NOI18N
        else
            return source + ":" + lineno;   // NOI18N
    }

    public String getNumber() {
        if (isHidden()) {
            return "";                  // NOI18N
        }
        if (isSpecial())
            return "";                  // NOI18N
	return frameno;
    }

    public String getOptimized() {
        if (isSpecial())
            return "";                  // NOI18N
        if (hasDebuggingInfo()) {
            if (optimized)
                return "-g/-O";         // NOI18N
            else
                return "-g";            // NOI18N
        } else {
            return "no -g"; // NOI18N
        }
    }

    public String getLoadObj() {
        if (isSpecial()) {
	    if (more)
		return Catalog.get("MSG_MORE_FRAME");                          // NOI18N
            else
		return "";                          // NOI18N
	}
	return loadobj;
    }

    public String getLoadObjBase() {
        if (isSpecial())
            return "";                          // NOI18N
	return loadobj_base;
    }

    public String getCurrentPC() {
        if (isSpecial())
            return "";                          // NOI18N
	return pc;
    }

    /**
     * Return the signals for the stack frame
     */
    public String getSignal() {
        if (signal == null) {
            if (attr_signame != null) {
                signal = attr_signame; // XXX signal no (attr_sig?)
            } else {
                signal = ""; //NOI18N
            }
        }
        return signal;
    }

    public Thread getThread() {
        return thread;
    }
}
