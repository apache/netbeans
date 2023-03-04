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


/**
 * Makes the entire form selected in designer - if the current designed
 * container is a sub-container and also the current selected node.
 * This action is not presented visually anywhere, it is used as one of the
 * component default actions ensuring that when a designed container is double
 * clicked, the whole form is brought back to design.
 * 
 * @author Tomas Pavek
 */
public class EditFormAction extends NodeAction {

    @Override
    protected boolean enable(Node[] nodes) {
        boolean ret = false;
        if (nodes != null && nodes.length == 1) {
            RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
            RADComponent comp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (comp != null) {
                RADComponent topComp = comp.getFormModel().getTopRADComponent();
                if (comp != topComp && EditContainerAction.isEditableComponent(topComp)) {
                    FormDesigner designer = getDesigner(comp);
                    if (designer != null && comp == designer.getTopDesignComponent()) {
                        ret = true;
                    }
                }
            }
        }
        return ret;
    }

    static void reenable(Node[] nodes) {
        SystemAction.get(EditFormAction.class).reenable0(nodes);
    }

    private void reenable0(Node[] nodes) {
        setEnabled(enable(nodes));
    }

    @Override
    protected void performAction(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
            RADComponent comp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (comp != null) {
                RADComponent topComp = comp.getFormModel().getTopRADComponent();
                if (topComp != comp && EditContainerAction.isEditableComponent(topComp)) {
                    FormDesigner designer = getDesigner(topComp);
                    if (designer != null && topComp != designer.getTopDesignComponent()) {
                        designer.setTopDesignComponent((RADVisualComponent)topComp, true);
                        designer.requestActive();

                        // NodeAction is quite unreliable in enabling, do it ourselves for sure
                        Node[] n = new Node[] { topComp.getNodeReference() };
                        if (n[0] != null) {
                            EditContainerAction.reenable(n);
                            DesignParentAction.reenable(n);
                            EditFormAction.reenable(n);
                        }
                    }
                }
            }
        }
    }

    private static FormDesigner getDesigner(RADComponent comp) {
        return FormEditor.getFormDesigner(comp.getFormModel());
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return ""; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
