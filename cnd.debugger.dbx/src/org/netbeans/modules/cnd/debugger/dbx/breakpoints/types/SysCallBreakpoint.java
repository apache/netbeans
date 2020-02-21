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

package org.netbeans.modules.cnd.debugger.dbx.breakpoints.types;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.SysCallEEProperty;
import org.netbeans.modules.cnd.debugger.common2.values.SysCallEE;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * @deprecated Use the same class from common instead
 */
@Deprecated
public final class SysCallBreakpoint extends NativeBreakpoint {

    public StringProperty sysCall =
	new StringProperty(pos, "sysCall", null, false, null); // NOI18N
    public SysCallEEProperty entryExit =
	new SysCallEEProperty(pos, "entryExit", null, false, SysCallEE.ENTRY); // NOI18N

    public SysCallBreakpoint(int flags) {
	super(new SysCallBreakpointType(), flags);
    } 

    public String getSysCall() {
	return sysCall.get();
    }

    public void setSysCall(String newSysCall) {
	sysCall.set(newSysCall);
    }

    public SysCallEE getEntryExit() {
        return entryExit.get();
    }

    public void setEntryExit(SysCallEE ee) {
        entryExit.set(ee);
    }
    
    public String getSummary() {
	return sysCall.get();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	SysCallBreakpoint bre = this;
	String call = bre.getSysCall();
	if (call == null) {
	    if (bre.getEntryExit() == SysCallEE.EXIT) {
		summary = Catalog.get("Handler_SysoutAny");
	    } else {
		summary = Catalog.get("Handler_SysinAny");
	    }
	} else {
	    if (bre.getEntryExit() == SysCallEE.EXIT) {
		summary = Catalog.format("Handler_Sysout", call);
	    } else {
		summary = Catalog.format("Handler_Sysin", call);
	    }
	}
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
