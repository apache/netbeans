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

package org.netbeans.modules.cnd.navigation.macroview;

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
 * Action which shows Declaration component.
 *
 */
@ActionID(id = "org.netbeans.modules.cnd.navigation.macroview.MacroExpansionAction", category = "Window")
@ActionRegistration(lazy = true, displayName = "#CTL_MacroExpansionAction", iconBase=MacroExpansionTopComponent.ICON_PATH)
public class MacroExpansionAction extends CallableSystemAction {

    public MacroExpansionAction() {
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        performAction();
    }

    @Override
    protected String iconResource() {
        return MacroExpansionTopComponent.ICON_PATH;
    }

    @Override
    public void performAction() {
        if (!CsmUtilities.isAnyNativeProjectOpened()) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(MacroExpansionAction.class, "CTL_MacroExpansionAction.warning"), NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        MacroExpansionTopComponent win = MacroExpansionTopComponent.findInstance();
        win.open();
        win.requestActive();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MacroExpansionAction.class, "CTL_MacroExpansionAction"); // NOI18N
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
