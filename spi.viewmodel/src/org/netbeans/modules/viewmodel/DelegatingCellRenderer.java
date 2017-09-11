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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
