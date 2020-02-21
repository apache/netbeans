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


package org.netbeans.modules.cnd.debugger.dbx;

import javax.swing.Action;

import org.openide.util.actions.SystemAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MaxObjectAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.WatchModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.VariableModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.WatchVariable;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeWatch;
import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;

/*
 * NOTE: Only top-level entries in the watch view are DbxWatches!
 */

class DbxWatch extends DbxVariable implements WatchVariable {
    private final int id;
    private final int originalId;
    private final boolean restricted;
    private NativeWatch nativeWatch;
    
    public DbxWatch(DbxDebuggerImpl debugger, ModelChangeDelegator updater,
		    int id, boolean restricted,
		    String name, String type) {
	this(debugger, updater, id, restricted, name, type, -1);
    }
   
    public DbxWatch(DbxDebuggerImpl debugger, ModelChangeDelegator updater,
		    int id, boolean restricted,
		    String name, String type, int originalId) {
	super(debugger, updater, null, name, name, type, type, null, true);
	this.id = id;
	this.restricted = restricted;
        this.originalId = originalId;
    }

    // interface WatchVariable
    public void setNativeWatch(NativeWatch nativeWatch) {
	this.nativeWatch = nativeWatch;
    }

    // interface WatchVariable
    public NativeWatch getNativeWatch() {
	return nativeWatch;
    }

    public int getOriginalId() {
        return originalId;
    }

    // interface ListMapItem
    public boolean hasKey() {
	return id > 0;
    }

    // interface ListMapItem
    public Object getKey() {
	return id;
    }

    // override Variable
    @Override
    protected void update() {
	if (nativeWatch != null)
	    nativeWatch.update();
	else
	    super.update();
    }

    public int getId() {
	return id;
    } 

    public boolean isRestricted() {
	return restricted;
    }

    private int routingToken = 0;

    public int getRoutingToken() {
	if (routingToken == 0)
	    routingToken = RoutingToken.WATCHES.getUniqueRoutingTokenInt();
	return routingToken;
    }

    // interface Variable
    @Override
    public String getDebugInfo() {
	String info = "";
	info += super.getDebugInfo();
	info += "<code>"; // NOI18N
	info += "<hr>" + // NOI18N
	    "<b>expr</b> " + nativeWatch.getExpression() + "<br>"; // NOI18N
	if (nativeWatch.watch() == null) {
	    info += 
		"<b>qexpr</b> " + nativeWatch.getQualifiedExpression() + "<br>" + // NOI18N
		"<b>scope</b> " + nativeWatch.getScope() + "<br>" + // NOI18N
		"<b>restricted</b> " + nativeWatch.isRestricted() + "<br>"; // NOI18N
	}
	info += "</code>"; // NOI18N
	return info;
    }

    // interface Variable
    @Override
    public Action[] getActions(boolean isWatch) {
	assert isWatch;
        
	return new Action[] {
            WatchModel.NEW_WATCH_ACTION,
	    null,
	    WatchModel.DELETE_ACTION,
	    new WatchModel.DeleteAllAction(),
	    null,
	    VariableModel.Action_INHERITED_MEMBERS,
	    VariableModel.Action_DYNAMIC_TYPE,
	    VariableModel.Action_STATIC_MEMBERS,
	    VariableModel.Action_PRETTY_PRINT,
	    VariableModel.getOutputFormatAction(this),
	    SystemAction.get(MaxObjectAction.class),
	    null,
	};
    }
    
    @Override
    public void createWatch() {
        throw new UnsupportedOperationException("Not supported for watches."); //NOI18N
    }
}
