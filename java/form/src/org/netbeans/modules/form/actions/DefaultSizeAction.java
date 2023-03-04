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
