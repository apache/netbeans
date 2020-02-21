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

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.awt.event.ActionEvent;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Action which shows Hierarchy component.
 */
@ActionID(id = "org.netbeans.modules.cnd.navigation.hierarchy.HierarchyAction", category = "Window")
@ActionRegistration(lazy = true, displayName = "#CTL_HierarchyAction", iconBase=HierarchyTopComponent.ICON_PATH)
public class HierarchyAction extends CallableSystemAction {

    public HierarchyAction() {
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        performAction();
    }

    @Override
    protected String iconResource() {
        return HierarchyTopComponent.ICON_PATH;
    }

    @Override
    public void performAction() {
        if (!CsmUtilities.isAnyNativeProjectOpened()) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(HierarchyAction.class, "CTL_HierarchyAction.warning"), NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        HierarchyTopComponent win = HierarchyTopComponent.findInstance();
        //Preferences ps = NbPreferences.forModule(HierarchyTopComponent.class);
        win.open();
        win.requestActive();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(HierarchyAction.class, "CTL_HierarchyAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    protected boolean asynchronous () {
        return false;
    }
}
