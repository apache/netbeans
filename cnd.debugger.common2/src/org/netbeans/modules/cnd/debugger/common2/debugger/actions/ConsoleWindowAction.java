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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;

public final class ConsoleWindowAction extends CallableSystemAction {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -6814567172958445516L;    

    // interface CallableSystemAction
    @Override
    public void performAction() {
	NativeDebuggerManager.get().enableConsoleWindow();
    }

    // interface SystemAction
    @Override
    public String getName() {
	return Catalog.get("CTL_Cmdio"); // NOI18N
    }

    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("CTL_Cmdio"); // NOI18N
    }

    // interface SystemAction
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/debugger/common2/icons/debugger_console.png"; // NOI18N
    }

    // interface CallableSystemAction
    @Override
    public boolean asynchronous() {
	return false;
    }
}
