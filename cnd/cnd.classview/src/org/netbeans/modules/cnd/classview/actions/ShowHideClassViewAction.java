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

package org.netbeans.modules.cnd.classview.actions;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.classview.ClassViewTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.*;

/**
 * Shows/Hides class view pane
 */
@ActionID(id = "org.netbeans.modules.cnd.classview.ClassViewAction", category = "View")
@ActionRegistration(lazy = true, displayName = "#CTL_ClassViewAction", iconBase=ClassViewTopComponent.ICON_PATH)
@ActionReferences(value = {
    @ActionReference(path = "Shortcuts", name = "D-9"),
    @ActionReference(path = "Menu/Window", position = 300)})
public class ShowHideClassViewAction extends CallableSystemAction {

    public ShowHideClassViewAction() {
        putValue(NAME, NbBundle.getMessage(ShowHideClassViewAction.class, "CTL_ClassViewAction")); // NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ShowHideClassViewAction.class, "HINT_ClassViewAction")); // NOI18N
    }

    @Override
    public String getName() {
        return (String) getValue(NAME);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        performAction();
    }

    @Override
    public void performAction() {
        TopComponent tc = ClassViewTopComponent.findDefault();
        if (!tc.isOpened()) {
            tc.open();
            Preferences ps = NbPreferences.forModule(ClassViewTopComponent.class);
            ps.putBoolean(ClassViewTopComponent.OPENED_PREFERENCE, true); // NOI18N
        }
        tc.requestActive();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected String iconResource() {
        return ClassViewTopComponent.ICON_PATH;
    }
}
