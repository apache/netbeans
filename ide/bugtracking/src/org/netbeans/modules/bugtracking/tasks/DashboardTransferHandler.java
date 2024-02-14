/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            return new DashboardTransferable(nodes.toArray(new TaskNode[0]));
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
