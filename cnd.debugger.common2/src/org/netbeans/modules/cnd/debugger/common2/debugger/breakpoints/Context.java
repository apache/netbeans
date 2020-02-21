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

