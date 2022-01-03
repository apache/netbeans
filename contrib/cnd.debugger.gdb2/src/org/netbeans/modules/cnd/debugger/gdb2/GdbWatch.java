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
