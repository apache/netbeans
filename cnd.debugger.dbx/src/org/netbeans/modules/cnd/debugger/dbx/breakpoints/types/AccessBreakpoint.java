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
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.AccessBAProperty;
import org.netbeans.modules.cnd.debugger.common2.values.AccessBA;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.BooleanProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class AccessBreakpoint extends NativeBreakpoint {

    public StringProperty address = 
	new StringProperty(pos, "address", null, false, null); // NOI18N
    public StringProperty size = 
	new StringProperty(pos, "size", null, false, null); // NOI18N
    public BooleanProperty read = 
	new BooleanProperty(pos, "read", null, false, false); // NOI18N
    public BooleanProperty write = 
	new BooleanProperty(pos, "write", null, false, true); // NOI18N
    public BooleanProperty execute = 
	new BooleanProperty(pos, "execute", null, false, false); // NOI18N
    public AccessBAProperty when =
	new AccessBAProperty(pos, "when", null, false, AccessBA.BEFORE); // NOI18N

    public AccessBreakpoint(int flags) {
	super(new AccessBreakpointType(), flags);
    } 

    public String getAddress() {
	return address.get();
    }

    public void setAddress(String newAddress) {
	address.set(newAddress);
    }

    public String getSize() {
	return size.get();
    }

    public void setSize(String newSize) {
	size.set(newSize);
    }

    public boolean isRead() {
	return read.get();
    }

    public void setRead(boolean newRead) {
	read.set(newRead);
    }
    
    public boolean isWrite() {
	return write.get();
    }

    public void setWrite(boolean newWrite) {
	write.set(newWrite);
    }

    public boolean isExecute() {
	return execute.get();
    }

    public void setExecute(boolean newExecute) {
	execute.set(newExecute);
    }

    public AccessBA getWhen() {
        return when.get();
    }

    public void setWhen(AccessBA ba) {
	when.set(ba);
    }

    public String getSummary() {
	return address.toString();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	AccessBreakpoint bre = this;
	StringBuffer sb = new StringBuffer(30);
	if (bre.getWhen() == AccessBA.BEFORE) {
	    sb.append(Catalog.get("Handler_Before")); // NOI18N
	} else {
	    sb.append(Catalog.get("Handler_After")); // NOI18N
	}
	if (bre.isRead()) {
	    sb.append(' ');
	    sb.append(Catalog.get("Handler_read")); // NOI18N
	}
	if (bre.isWrite()) {
	    sb.append(' ');
	    sb.append(Catalog.get("Handler_write")); // NOI18N
	}
	if (bre.isExecute()) {
	    sb.append(' ');
	    sb.append(Catalog.get("Handler_execute")); // NOI18N
	}
	sb.append(' ');
	sb.append(bre.getAddress());
	if (bre.getSize() != null) {
	    sb.append(',');
	    sb.append(bre.getSize());
	}
	summary = sb.toString();
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
