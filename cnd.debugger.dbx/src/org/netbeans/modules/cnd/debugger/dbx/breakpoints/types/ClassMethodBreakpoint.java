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
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.BooleanProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class ClassMethodBreakpoint extends NativeBreakpoint {

    public StringProperty className =
	new StringProperty(pos, "className", null, false, null); // NOI18N
    public StringProperty qclassName =
	new StringProperty(pos, "qclassName", null, false, null); // NOI18N
    public StringProperty method =
	new StringProperty(pos, "method", null, false, null); // NOI18N
    public StringProperty qmethod =
	new StringProperty(pos, "qmethod", null, false, null); // NOI18N

    // Include methods in base classes?
    public BooleanProperty recurse =
	new BooleanProperty(pos, "recurse", null, false, false); // NOI18N

    public ClassMethodBreakpoint(int flags) {
	super(new ClassMethodBreakpointType(), flags);
    } 

    public void setClassName(String className) {
	this.className.set(className);
    }

    public String getClassName() {
	return className.get();
    } 

    public void setQclassName(String cls) {
	qclassName.set(cls);
    }

    public String getQclassName() {
	return qclassName.get();
    } 

    public void setMethodName(String sc) {
	method.set(sc);
    } 

    public String getMethodName() {
	return method.get();
    } 

    public void setQmethodName(String sc) {
	qmethod.set(sc);
    } 

    public String getQmethodName() {
	return qmethod.get();
    } 

    /** Should we include methods in parent classes? */
    public void setRecurse(boolean r) {
	recurse.set(r);
    }

    /** Should we include methods in parent classes? */
    public boolean isRecurse() {
	return recurse.get();
    }

    protected final String getSummary() {
	return getClassName();
    } 

    protected String getDisplayNameHelp() {
	String summary = null;
	ClassMethodBreakpoint bre = this;
	if (bre.getClassName() != null) {
	    if (bre.getMethodName() != null) {
		// XXX -should- be language sensitive, e.g. "." instead
		// of "::" for Java
		summary = bre.getClassName() + "::" + // NOI18N
		    bre.getMethodName();
	    } else {
		summary = Catalog.format("Handler_AllMeth", bre.getClassName());
	    }
	} else {
	    // we don't allow both to be null
	    summary = Catalog.format("Handler_AllClass", bre.getMethodName());
	}
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
