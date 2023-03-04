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

import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.undo.UndoableEdit;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.MetaComponentCreator;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 */
public class DuplicateAction  extends NodeAction {

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        List comps = FormUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && getParent(comps) != null);
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(AlignAction.class, "ACT_Duplicate"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void performAction(Node[] nodes) {
        duplicate(nodes, -1, -1);
    }

    public static void performAction(Node[] nodes, int keyCode) {
        int dimension = (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
                ? LayoutConstants.HORIZONTAL : LayoutConstants.VERTICAL;
        int direction = (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_UP)
                ? LayoutConstants.LEADING : LayoutConstants.TRAILING;
        duplicate(nodes, dimension, direction);
    }

    private static void duplicate(Node[] nodes, int dimension, int direction) {
        List<RADComponent> comps = FormUtils.getSelectedLayoutComponents(nodes);
        RADVisualContainer parent = (comps != null) ? getParent(comps) : null;
        if (parent != null) {
            FormModel formModel = parent.getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            Object layoutUndoMark = layoutModel.getChangeMark();
            UndoableEdit layoutEdit = layoutModel.getUndoableEdit();
            boolean autoUndo = true; // in case of unexpected error, for robustness

            String[] sourceIds = new String[comps.size()];
            String[] targetIds = new String[comps.size()];
            int i = 0;
            MetaComponentCreator creator = formModel.getComponentCreator();
            try {
                for (RADComponent comp : comps) {
                    RADComponent copiedComp = creator.copyComponent(comp, parent);
                    if (copiedComp == null) {
                        return; // copy failed...
                    }
                    sourceIds[i] = comp.getId();
                    targetIds[i] = copiedComp.getId();
                    i++;
                }
                FormEditor.getFormDesigner(formModel).getLayoutDesigner()
                        .duplicateLayout(sourceIds, targetIds, dimension, direction);
                autoUndo = false;
            } finally {
                if (layoutUndoMark != null && !layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    formModel.addUndoableEdit(layoutEdit);
                }
                if (autoUndo) {
                    formModel.forceUndoOfCompoundEdit();
                }
            }
        }
    }

    private static RADVisualContainer getParent(List components) {
        RADVisualContainer commonParent = null;
        for (Object comp : components) {
            if (comp instanceof RADVisualComponent) {
                RADVisualContainer parent = ((RADVisualComponent)comp).getParentContainer();
                if (parent == null || (commonParent != null && parent != commonParent)) {
                    return null;
                }
                if (commonParent == null) {
                    commonParent = parent;
                }
            } else {
                return null;
            }
        }
        return commonParent != null && commonParent.getLayoutSupport() == null
                ? commonParent : null;
    }
}
