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

package org.netbeans.modules.subversion.remote.ui.status;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import org.netbeans.modules.subversion.remote.Subversion;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Open the Subversion view. It focuses recently opened
 * view unless it's not initialized yet. For uninitialized
 * view it behaves like StatusProjectsAction without
 * on-open refresh.
 *
 * 
 */
@ActionID(id = "org.netbeans.modules.subversion.remote.ui.status.OpenVersioningAction", category = "SubversionRemote")
@ActionRegistration(displayName = "#CTL_MenuItem_OpenVersioning", iconBase=OpenVersioningAction.ICON_BASE)
@ActionReferences({
   @ActionReference(path="OptionsDialog/Actions/SubversionRemote")
})
public class OpenVersioningAction extends ShowAllChangesAction {
    
    public static final String ICON_BASE = "org/netbeans/modules/subversion/remote/resources/icons/versioning-view.png"; //NOI18N
    
    public OpenVersioningAction() {
        putValue("noIconInMenu", Boolean.FALSE); // NOI18N
        setIcon(ImageUtilities.loadImageIcon(ICON_BASE, false)); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenVersioningAction.class, "CTL_MenuItem_OpenVersioning"); // NOI18N
    }

    /**
     * Window/Versioning should be always enabled.
     * 
     * @return true
     */ 
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenVersioningAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
        if (stc.hasContext() == false) {
            super.actionPerformed(e);
        } else {
            if(!Subversion.getInstance().checkClientAvailable(stc.getContext())) {
                return;
            }
            stc.open();
            stc.requestActive();
        }
    }

    @Override
    protected boolean shouldPostRefresh() {
        return false;
    }
}
