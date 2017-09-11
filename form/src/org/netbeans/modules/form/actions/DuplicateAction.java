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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
