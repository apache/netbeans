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


package org.netbeans.modules.cnd.debugger.gdb2;

import javax.swing.Action;

import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;

import org.netbeans.modules.cnd.debugger.common2.debugger.WatchModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.VariableModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.WatchVariable;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeWatch;
import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;

class GdbWatch extends GdbVariable implements WatchVariable {

    private volatile NativeWatch nativeWatch;

    public GdbWatch(NativeDebugger debugger, ModelChangeDelegator updater,
		    String name) {
	super((GdbDebuggerImpl)debugger, updater, null, name, null, null, true);
    }

    // interface WatchVariable
    @Override
    public void setNativeWatch(NativeWatch parent) {
	this.nativeWatch = parent;
    }

    // interface WatchVariable
    @Override
    public NativeWatch getNativeWatch() {
	return nativeWatch;
    }

    // interface ListMapItem
    @Override
    public boolean hasKey() {
	return getVariableName() != null;
    }

    // interface ListMapItem
    @Override
    public Object getKey() {
	return getVariableName();
    }

    // interface WatchVariable
    @Override
    public void removeAllDescendantFromOpenList(boolean isLocal) {
    }

    private int routingToken = 0;

    // interface WatchVariable
    @Override
    public int getRoutingToken() {
	if (routingToken == 0)
	    routingToken = RoutingToken.WATCHES.getUniqueRoutingTokenInt();
	return routingToken;
    }

    // interface Variable
    @Override
    public Action[] getActions(boolean isWatch) {

	// There is a bug where ... because gdb reuses vars for identical
	// expressions such that VariableBag may return a GdbWatch when
	// filling locals. SHOULD re-introduce the assert once that bug
	// is fixed.
	// LATER assert isWatch;

	return new Action[] {
            WatchModel.NEW_WATCH_ACTION,
            WatchModel.SHOW_PINNED_WATCHES_ACTION,
	    null,
	    WatchModel.DELETE_ACTION,
	    new WatchModel.DeleteAllAction(),
	    null,
	    // LATER VariableModel.Action_INHERITED_MEMBERS,
	    // LATER VariableModel.Action_DYNAMIC_TYPE,
	    VariableModel.getOutputFormatAction(this),
	    // LATER SystemAction.get(MaxObjectAction.class),
	    null
	};
    }
    
    @Override
    public void createWatch() {
        throw new UnsupportedOperationException("Not supported for watches."); //NOI18N
    }
}
