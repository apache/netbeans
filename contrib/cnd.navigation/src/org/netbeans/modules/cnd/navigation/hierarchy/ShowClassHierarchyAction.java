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

import org.netbeans.modules.cnd.navigation.hierarchy.HierarchyTopComponent.TypeContextFinder;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

@ActionID(id = "org.netbeans.modules.cnd.navigation.hierarchy.ShowClassHierarchyAction", category = "Edit")
@ActionRegistration(lazy = true, displayName = "#CTL_ShowHierarchyAction")
@ActionReferences(value = {
    @ActionReference(path = "Editors/text/x-h/Popup/goto", position = 1200),
    @ActionReference(path = "Editors/text/x-c++/Popup/goto", position = 1200)})
public final class ShowClassHierarchyAction extends CookieAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        HierarchyTopComponent view = HierarchyTopComponent.findInstance();
        if (!view.isOpened()) {
            view.open();
        }
        view.setClass(new TypeContextFinder(activatedNodes), false);
        view.requestActive();
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ShowHierarchyAction"); // NOI18N
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class<?>[]{DataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

