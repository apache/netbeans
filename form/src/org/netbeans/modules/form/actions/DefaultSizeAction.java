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
import java.util.HashSet;
import java.util.Set;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.actions.NodeAction;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

public class DefaultSizeAction extends NodeAction {

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        for (int i=0; i < nodes.length; i++) {
            if (getValidComponent(nodes[i]) == null)
                return false; // all components must be valid
        }
        return true;
    }

    @Override
    protected void performAction(Node[] nodes) {
        FormModel formModel = null;
        Set<RADVisualContainer> changedContainers = null;
        FormDesigner formDesigner = null;
        LayoutDesigner layoutDesigner = null;
        LayoutModel layoutModel = null;
        Object layoutUndoMark = null;
        javax.swing.undo.UndoableEdit layoutUE = null;
        boolean autoUndo = true;

        try {
            for (int i=0; i < nodes.length; i++) {
                RADVisualComponent metacomp = getValidComponent(nodes[i]);
                if (metacomp == null)
                    return; // all components must be valid

                if (layoutDesigner == null) {
                    formModel = metacomp.getFormModel();
                    changedContainers = new HashSet<RADVisualContainer>();
                    formDesigner = FormEditor.getFormDesigner(formModel);
                    layoutDesigner = formDesigner.getLayoutDesigner();
                    layoutModel = formModel.getLayoutModel();
                    layoutUndoMark = layoutModel.getChangeMark();
                    layoutUE = layoutModel.getUndoableEdit();
                }
                layoutDesigner.setDefaultSize(metacomp.getId());
                if (metacomp instanceof RADVisualContainer) {
                    fireContainerChange((RADVisualContainer)metacomp, changedContainers);
                }
                if (metacomp != formDesigner.getTopDesignComponent()) {
                    formModel.fireContainerLayoutChanged(metacomp.getParentContainer(), null, null, null);
                }
            }
            autoUndo = false;
        } finally  {
            if (layoutUE != null && !layoutUndoMark.equals(layoutModel.getChangeMark())) {
                formModel.addUndoableEdit(layoutUE);
            }
            if (autoUndo) {
                formModel.forceUndoOfCompoundEdit();
            }
        }
    }

    private static void fireContainerChange(RADVisualContainer metacont, Set<RADVisualContainer> alreadyFired) {
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            if (metacomp instanceof RADVisualContainer) {
                fireContainerChange((RADVisualContainer)metacomp, alreadyFired);
            }
        }
        if (!alreadyFired.contains(metacont)) {
            alreadyFired.add(metacont);
            metacont.getFormModel().fireContainerLayoutChanged(metacont, null, null, null);
        }
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getBundle(DefaultSizeAction.class)
                .getString("ACT_DefaultSize"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private static RADVisualComponent getValidComponent(Node node) {
        RADComponentCookie radCookie = node.getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualComponent) {
                RADVisualComponent visualMetaComp = (RADVisualComponent)metacomp;
                RADVisualContainer parent = visualMetaComp.getParentContainer();
                if ((parent != null) && javax.swing.JScrollPane.class.isAssignableFrom(parent.getBeanInstance().getClass())) {
                    visualMetaComp = parent;
                    parent = parent.getParentContainer();
                }
                if (FormUtils.isVisualInDesigner(metacomp) &&
                    ((parent != null && parent.getLayoutSupport() == null
                      && !visualMetaComp.isMenuComponent())
                    || (visualMetaComp instanceof RADVisualContainer
                        && ((RADVisualContainer)visualMetaComp).getLayoutSupport() == null
                        && (!(visualMetaComp instanceof RADVisualFormContainer)
                            || ((RADVisualFormContainer)visualMetaComp).getFormSizePolicy()
                                 != RADVisualFormContainer.GEN_BOUNDS))))
                    return visualMetaComp;
            }
        }
        return null;
    }
}
