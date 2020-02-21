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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.FunctionSubEventProperty;
import org.netbeans.modules.cnd.debugger.common2.values.FunctionSubEvent;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class FunctionBreakpoint extends NativeBreakpoint {

    public StringProperty function =
	new StringProperty(pos, "function", null, false, null); // NOI18N
    public StringProperty qfunction =
	new StringProperty(pos, "qfunction", null, false, null); // NOI18N
    /* TMP
    public StringProperty qfunction =
	new StringProperty.Tracking(pos, "qfunction", null, false, null);
    */

    public FunctionSubEventProperty subEvent =
	new FunctionSubEventProperty(pos, "subEvent", null, false, FunctionSubEvent.IN); // NOI18N

    public FunctionBreakpoint(int flags) {
	super(new FunctionBreakpointType(), flags);
    } 

    public void setFunction(String function) {
        if (function == null) {
            function = "<func>"; // NOI18N
        }
	this.function.set(function);
    } 

    public String getFunction() {
	return function.get();
    } 

    public void setQfunction(String qfunc) {
	qfunction.set(qfunc);
    }

    public String getQfunction() {
	return qfunction.get();
    }

    public void setSubEvent(FunctionSubEvent se) {
	subEvent.set(se);
    }

    public FunctionSubEvent getSubEvent() {
	return subEvent.get();
    }

    @Override
    protected final String getSummary() {
	return getFunction();
    } 

    @Override
    protected String getDisplayNameHelp() {
	String summary = null;
	FunctionBreakpoint fb = this;
	FunctionSubEvent se = fb.getSubEvent();
	if (se.equals(FunctionSubEvent.IN)) {
	    summary = fb.getFunction();
	} else if (se.equals(FunctionSubEvent.INFUNCTION)) {
	    summary = Catalog.format("Handler_AllFunc", fb.getFunction()); // NOI18N
	} else if (se.equals(FunctionSubEvent.RETURNS)) {
	    summary = Catalog.format("Handler_ReturnFrom", fb.getFunction()); // NOI18N
	} else {
	    summary = fb.getFunction();
	}
	return summary;
    }

    @Override
    protected void processOriginalEventspec(String oeventspec) {
	assert !IpeUtils.isEmpty(oeventspec);
	this.function.set(oeventspec);
    }
}
