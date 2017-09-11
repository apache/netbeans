/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.tasks;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import org.netbeans.modules.bugtracking.tasks.dashboard.CategoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.team.commons.treelist.TreeList;
import org.netbeans.modules.team.commons.treelist.TreeListModel;

/**
 *
 * @author jpeska
 */
public class DashboardTransferHandler extends TransferHandler {

    private final DataFlavor taskFlavor = DashboardTransferable.taskFlavor;

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        if (!info.isDataFlavorSupported(taskFlavor)) {
            return false;
        }
        Category category = getTargetCategory(info);
        return category != null;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof JList) {
            JList list = (JList) c;
            Object[] values = list.getSelectedValues();
            if (values == null || values.length == 0) {
                return null;
            }
            List<TaskNode> nodes = new ArrayList<TaskNode>(values.length);
            for (int i = 0; i < values.length; i++) {
                Object val = values[i];
                if (val instanceof TaskNode) {
                    nodes.add((TaskNode) val);
                } else {
                    return null;
                }
            }
            return new DashboardTransferable(nodes.toArray(new TaskNode[nodes.size()]));
        }
        return null;
    }

    /**
     * We support copy
     */
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        Category category = getTargetCategory(info);
        if (category == null) {
            return false;
        }

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        TaskNode[] data;
        try {
            data = (TaskNode[]) t.getTransferData(taskFlavor);
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        // Perform the actual import.
        DashboardViewer.getInstance().addTaskToCategory(category, data);
        return true;
    }

    /*
     * Returns CategoryNode if it is a target of a drop, null otherwise
     */
    private Category getTargetCategory(TransferHandler.TransferSupport info) {
        TreeList list = (TreeList) info.getComponent();
        TreeListModel listModel = (TreeListModel) list.getModel();
        JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
        int index = dl.getIndex();
        Object elementAt = listModel.getElementAt(index);
        if (dl.isInsert()) {
            if (elementAt instanceof TaskNode) {
                Category category = ((TaskNode) elementAt).getCategory();
                return category != null && category.persist() ? category : null;
            }
            Object elementAtPrevious = listModel.getElementAt(index - 1);
            if (elementAtPrevious instanceof TaskNode) {
                Category category = ((TaskNode) elementAtPrevious).getCategory();
                return category != null && category.persist() ? category : null;
            }
            return null;
        }
        if (!(elementAt instanceof CategoryNode)) {
            return null;
        }
        Category category = ((CategoryNode) elementAt).getCategory();
        return category != null && category.persist() ? category : null;
    }
}
