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

import java.awt.*;
import javax.swing.*;

import org.openide.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.nodes.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutdesign.*;

/**
 * Customize empty space action.
 *
 * @author Jan Stola
 */
public class CustomizeEmptySpaceAction extends CookieAction {
    private static String name;
    private static Dialog dialog;

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * Human presentable name of the action.
     *
     * @return the name of the action
     */
    @Override
    public String getName() {
        if (name == null) {
            name = NbBundle.getMessage(CustomizeEmptySpaceAction.class, "ACT_CustomizeEmptySpace"); // NOI18N
        }
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        perform(activatedNodes, false);
    }

    private static void perform(Node[] nodes, boolean onlySingleGap) {
        RADVisualComponent metacomp = getMetaComponent(nodes);
        LayoutDesigner.EditableGap[] editableGaps = getEditableGaps(metacomp, onlySingleGap);
        if (editableGaps == null) {
            return; // [beep?]
        }
        FormModel formModel = metacomp.getFormModel();
        LayoutDesigner layoutDesigner = FormEditor.getFormDesigner(formModel).getLayoutDesigner();
        final EditLayoutSpacePanel customizer = new EditLayoutSpacePanel(editableGaps, metacomp.getName());

        DialogDescriptor dd = new DialogDescriptor(
            customizer,
            NbBundle.getMessage(CustomizeEmptySpaceAction.class, "TITLE_EditLayoutSpace"), // NOI18N
            true,
            new java.awt.event.ActionListener() {
            @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (evt.getSource() == NotifyDescriptor.OK_OPTION) {
                        if (customizer.applyValues()) {
                            dialog.dispose();
                        }
                    }
                }
            });
        dd.setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizeEmptySpaceAction.class, "ACSD_EditLayoutSpace")); // NOI18N
        // setting the help id on the customizer after creating the dialog will avoid the help button
        HelpCtx.setHelpIDString(customizer, "f1_gui_layout_space_html"); // NOI18N
        dialog.setVisible(true);
        dialog = null;
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            LayoutModel layoutModel = formModel.getLayoutModel();
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            try {
                layoutDesigner.applyEditedGaps(editableGaps);
                autoUndo = false;
            } finally {
                formModel.fireContainerLayoutChanged(editableGaps.length == 1 && metacomp instanceof RADVisualContainer ?
                        (RADVisualContainer)metacomp : metacomp.getParentContainer(), null, null, null);
                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    formModel.addUndoableEdit(ue);
                }
                if (autoUndo) {
                    formModel.forceUndoOfCompoundEdit();
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return super.enable(activatedNodes) && enable(activatedNodes, false);
    }

    private static boolean enable(Node[] nodes, boolean onlySingleGap) {
        RADVisualComponent metacomp = getMetaComponent(nodes);
        if (metacomp != null && !metacomp.isReadOnly()) {
            return getEditableGaps(metacomp, onlySingleGap) != null;
        }
        return false;
    }

    private static LayoutDesigner.EditableGap[] getEditableGaps(RADVisualComponent metacomp, boolean onlySingleGap) {
        if (metacomp != null) {
            FormDesigner formDesigner = FormEditor.getFormDesigner(metacomp.getFormModel());
            if (formDesigner != null) {
                LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
                if (formDesigner.isInDesigner(metacomp) && layoutDesigner != null) {
                    LayoutDesigner.EditableGap[] editableGaps = layoutDesigner.getEditableGaps();
                    if (editableGaps != null) {
                        boolean oneGap = (editableGaps.length == 1);
                        if (oneGap || (!onlySingleGap && metacomp != formDesigner.getTopDesignComponent())) {
                            return editableGaps;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static RADVisualComponent getMetaComponent(Node[] nodes) {
        if (nodes.length == 1) {
            RADComponentCookie radCookie = nodes[0].getLookup().lookup(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if (metacomp instanceof RADVisualComponent) {
                    return (RADVisualComponent) metacomp;
                }
            }
        }
        return null;
    }

    // This action is used for double-clicking a gap in the designer.
    public static class EditSingleGapAction extends NodeAction {
        @Override
        public boolean isEnabled() {
            setEnabled(false);
            return FocusManager.getCurrentManager().getFocusOwner() instanceof HandleLayer
                    && super.isEnabled();
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return CustomizeEmptySpaceAction.enable(activatedNodes, true);
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            CustomizeEmptySpaceAction.perform(activatedNodes, true);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(CustomizeEmptySpaceAction.class, "ACT_CustomizeEmptySpace"); // NOI18N
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
}
