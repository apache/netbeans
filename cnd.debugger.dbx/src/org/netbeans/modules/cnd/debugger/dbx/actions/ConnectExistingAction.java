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
