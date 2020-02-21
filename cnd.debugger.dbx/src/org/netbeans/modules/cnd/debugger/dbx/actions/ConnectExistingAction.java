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

package org.netbeans.modules.cnd.debugger.dbx.actions;

import java.awt.event.ActionEvent;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.dbx.DbxDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;

/**
 * Connect to an existing dbx
 */

public class ConnectExistingAction extends CallableSystemAction {

    // interface SystemAction
    @Override
    public boolean isEnabled() {
	return true;
    }

    // interface SystemAction
    public String getName () {
        return Catalog.get("LBL_ConnectExisting"); // NOI18N

    }

    /* @return the action's help context */
    // interface SystemAction
    public HelpCtx getHelpCtx() {
        return new HelpCtx("Debugging_load"); // NOI18N
    }

    /* @return the action's icon */
    /* LATER
    protected String iconResource() {
        return "org/netbeans/modules/cnd/debugger/common2/icons/debug_executable.png"; // NOI18N
    }
    */

    // interface SystemAction
    @Override
    public void actionPerformed(ActionEvent ev) {
	performAction();
    }

    // interface CallableSystemAction
    public void performAction() {
        DebugTarget dt = new DebugTarget();	// dummy DT
        Configuration conf = dt.getConfig();

        DbxDebuggerInfo ddi = DbxDebuggerInfo.create();
        ddi.setHostName(Host.localhost);
        ddi.setProfile(runProfile(conf));
        ddi.setConfiguration(conf);
        ddi.setAction(NativeDebuggerManager.CONNECT);

        NativeDebuggerManager.get().debugNoAsk(ddi);
    }
    
    private static RunProfile runProfile(Configuration conf) {
        return (RunProfile) conf.getAuxObject(RunProfile.PROFILE_ID);
    }


}
