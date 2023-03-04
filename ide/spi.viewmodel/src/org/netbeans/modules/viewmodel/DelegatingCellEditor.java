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
