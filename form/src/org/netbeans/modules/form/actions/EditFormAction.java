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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
