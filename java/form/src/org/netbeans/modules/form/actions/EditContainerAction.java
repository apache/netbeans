/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.actions;

import org.openide.nodes.Node;
import org.openide.util.actions.*;
import org.openide.util.*;

import org.netbeans.modules.form.*;


/** Action that focuses selected container to be edited in FormDesigner.
 */
public class EditContainerAction extends NodeAction {

    private static String name;

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (isEditableComponent(metacomp)) {
                FormDesigner designer = FormEditor.getFormDesigner(metacomp.getFormModel());
                if (designer != null) {
                    designer.setTopDesignComponent((RADVisualComponent)metacomp, true);
                    designer.requestActive();

                    // same node keeps selected, but the state changed
                    reenable0(activatedNodes);
                    DesignParentAction.reenable(activatedNodes);
                    EditFormAction.reenable(activatedNodes);
                }
            }
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (isEditableComponent(metacomp)) {
                FormDesigner designer = FormEditor.getFormDesigner(metacomp.getFormModel());
                if (designer != null && metacomp != designer.getTopDesignComponent()) {
                    return true;
                }
            }
        }
        return false;
    }

    static void reenable(Node[] nodes) {
        SystemAction.get(EditContainerAction.class).reenable0(nodes);
    }

    private void reenable0(Node[] nodes) {
        setEnabled(enable(nodes));
    }

    @Override
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(EditContainerAction.class)
                     .getString("ACT_EditContainer"); // NOI18N
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.containers.designing"); // NOI18N
    }

    public static boolean isEditableComponent(RADComponent metacomp) {
        if (metacomp instanceof RADVisualComponent) {
            RADVisualComponent visComp = (RADVisualComponent) metacomp;
            RADVisualContainer parent = visComp.getParentContainer();
            // can design visual container, or a visual component with no parent
            // can't design menus except the entire menu bar
            return parent == null
                   || (visComp instanceof RADVisualContainer
                       && (!visComp.isMenuComponent() || parent.getContainerMenu() == visComp));
        }
        return false;
    }

}
