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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;

/**
 * Context for breakpoints.
 * <br>
 * Top-level bpts get converted to midlevel bpts in a particular context.
 * <br>
 * In the global regime a context is used to restore context-specific bpts,
 * that is, if they are differentiated. It is only made up of the executable.
 * <br>
 * In per-target bpts the context plays a much more important role and becomes
 * a proxy for DebugTarget. That is why it includes the hostname.
 * <br>
 * This context is shown in the session column and is used for
 * differentiating bpts.
 */

public class Context {
    private final String executable;
    private final String hostname;

    public Context(String executable, String hostname) {
	if (! NativeDebuggerManager.isPerTargetBpts())
	    hostname = null;
	this.executable = executable == null? "": executable;
	this.hostname = (hostname == null)? "": 
			(hostname.equals("localhost"))? "": // NOI18N
			hostname;
    } 

    public static Context parse(String s) {
        String executable;
        String hostname;
        int slashX = s.indexOf('@');

        if (slashX == -1) {
	    // No "@<hostname>"
            executable = s;
            hostname = null;

        } else if (slashX == 0) {
	    // _Only_ "@<hostname>"
            executable = null;          // no executable?
            hostname = s.substring(slashX);

        } else {
            executable = s.substring(0, slashX);	// excludes s[slashX]
            hostname = s.substring(slashX+1);
        }
	if (! NativeDebuggerManager.isPerTargetBpts())
	    hostname = null;
        Context c = new Context(executable, hostname);
	return c;
    }

    // copy constructor/cloner
    public Context(Context that) {
	this.executable = that.executable;
	this.hostname = that.hostname;
    }

    // interface Object
    @Override
    public String toString() {
	if (IpeUtils.isEmpty(hostname))
	    return executable;
	else
	    return executable + "@" + hostname; // NOI18N
    }

    public boolean matches(Context that) {
	if (this == that)
	    return true;
	return IpeUtils.sameString(this.executable, 
				   that.executable) &&
	       IpeUtils.sameString(this.hostname, 
				   that.hostname)
				   ;
    }

    // interface Object
    @Override
    public boolean equals(Object o) {
	if (! (o instanceof Context))
	    return false;
	Context that = (Context) o;
	if (this == that)
	    return true;
	return IpeUtils.sameString(this.executable, 
				   that.executable) &&
	       IpeUtils.sameString(this.hostname, 
				   that.hostname);
    }

    // interface Object
    @Override
    public int hashCode() {
	int result = 7632;
	if (executable != null)
	    result = 5 * result + executable.hashCode();
	if (hostname != null)
	    result = 7 * result + hostname.hashCode();
	return result;
    }
}

