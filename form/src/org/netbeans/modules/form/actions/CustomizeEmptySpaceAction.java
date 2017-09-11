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
