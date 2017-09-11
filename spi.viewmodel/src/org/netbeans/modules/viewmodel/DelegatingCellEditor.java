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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.netbeans.spi.viewmodel.TableRendererModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
class DelegatingCellEditor implements TableCellEditor {

    private String columnID;
    private TableCellEditor defaultEditor;
    private TableCellEditor currentEditor;
    private Reference<TableCellEditor> canceledEditorRef;

    public DelegatingCellEditor(String columnID, TableCellEditor defaultEditor) {
        this.columnID = columnID;
        this.defaultEditor = defaultEditor;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Outline outline = (Outline) table;
        Node n = DelegatingCellRenderer.getNodeAt(outline, row);
        if (n instanceof TreeModelNode) {
            TreeModelNode tmn = (TreeModelNode) n;
            TableRendererModel trm = tmn.getModel();
            try {
                if (trm.canEditCell(tmn.getObject(), columnID)) {
                    TableCellEditor editor = trm.getCellEditor(tmn.getObject(), columnID);
                    if (editor != null) {
                        currentEditor = editor;
                        return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
                    }
                }
            } catch (UnknownTypeException ex) {
            }
        }
        // No specific editor
        currentEditor = defaultEditor;
        return defaultEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        if (currentEditor != null) {
            return currentEditor.getCellEditorValue();
        }
        if (canceledEditorRef != null) {
            TableCellEditor canceledEditor = canceledEditorRef.get();
            if (canceledEditor != null) {
                return canceledEditor.getCellEditorValue();
            }
        }
        Exceptions.printStackTrace(new IllegalStateException("No current editor."));
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (!(anEvent.getSource() instanceof Outline)) {
            return false;
        }
        Outline outline = (Outline) anEvent.getSource();
        int row;
        if (anEvent instanceof MouseEvent) {
            MouseEvent event = (MouseEvent) anEvent;
            Point p = event.getPoint();
            // Locate the editor under the event location
            //int column = outline.columnAtPoint(p);
            row = outline.rowAtPoint(p);
        } else {
            row = outline.getSelectedRow();
        }
        Node n = DelegatingCellRenderer.getNodeAt(outline, row);
        if (n instanceof TreeModelNode) {
            TreeModelNode tmn = (TreeModelNode) n;
            TableRendererModel trm = tmn.getModel();
            try {
                boolean canEdit = trm.canEditCell(tmn.getObject(), columnID);
                if (canEdit) {
                    TableCellEditor tce = trm.getCellEditor(tmn.getObject(), columnID);
                    canEdit = tce.isCellEditable(anEvent);
                    return canEdit;
                }
            } catch (UnknownTypeException ex) {
            }
        }
        return defaultEditor.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        if (!(anEvent.getSource() instanceof Outline)) {
            return false;
        }
        Outline outline = (Outline) anEvent.getSource();
        if (!(anEvent instanceof MouseEvent)) {
            return false;
        }
        MouseEvent event = (MouseEvent) anEvent;
        Point p = event.getPoint();

        // Locate the editor under the event location
        //int column = outline.columnAtPoint(p);
        int row = outline.rowAtPoint(p);
        Node n = DelegatingCellRenderer.getNodeAt(outline, row);
        if (n instanceof TreeModelNode) {
            TreeModelNode tmn = (TreeModelNode) n;
            TableRendererModel trm = tmn.getModel();
            try {
                if (trm.canEditCell(tmn.getObject(), columnID)) {
                    TableCellEditor editor = trm.getCellEditor(tmn.getObject(), columnID);
                    if (editor != null) {
                        return editor.shouldSelectCell(anEvent);
                    }
                }
            } catch (UnknownTypeException ex) {
            }
        }
        return defaultEditor.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        if (currentEditor != null) {
            boolean status = currentEditor.stopCellEditing();
            if (status) {
                canceledEditorRef = new WeakReference<TableCellEditor>(currentEditor);
                currentEditor = null;
            }
            return status;
        }
        return true;
    }

    @Override
    public void cancelCellEditing() {
        if (currentEditor != null) {
            currentEditor.cancelCellEditing();
            canceledEditorRef = new WeakReference<TableCellEditor>(currentEditor);
            currentEditor = null;
            return ;
        }
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        currentEditor.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        TableCellEditor editor = currentEditor;
        if (editor == null && canceledEditorRef != null) {
            editor = canceledEditorRef.get();
        }
        if (editor != null) {
            editor.removeCellEditorListener(l);
        }
    }

}
