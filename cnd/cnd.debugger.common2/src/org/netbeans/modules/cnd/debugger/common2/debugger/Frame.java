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
