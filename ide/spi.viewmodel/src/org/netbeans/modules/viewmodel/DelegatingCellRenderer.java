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

package org.netbeans.modules.viewmodel;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.spi.viewmodel.TableRendererModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
class DelegatingCellRenderer implements TableCellRenderer {

    private String columnID;
    private TableCellRenderer defaultRenderer;

    public DelegatingCellRenderer(String columnID, TableCellRenderer defaultRenderer) {
        this.columnID = columnID;
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Outline outline = (Outline) table;
        Node n = getNodeAt(outline, row);
        if (n instanceof TreeModelNode) {
            TreeModelNode tmn = (TreeModelNode) n;
            TableRendererModel trm = tmn.getModel();
            try {
                if (trm.canRenderCell(tmn.getObject(), columnID)) {
                    TableCellRenderer renderer = trm.getCellRenderer(tmn.getObject(), columnID);
                    if (renderer != null) {
                        return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                }
            } catch (UnknownTypeException ex) {
            }
        }
        // No specific renderer
        return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    static final Node getNodeAt(Outline outline, int rowInUI) {
        Node result = null;
        OutlineModel om = (OutlineModel) outline.getModel();
        int row = outline.convertRowIndexToModel(rowInUI);
        TreePath path = om.getLayout().getPathForRow(row);
        if (path != null) {
            result = Visualizer.findNode(path.getLastPathComponent());
        }
        return result;
    }
}
