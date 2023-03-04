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

package org.netbeans.installer.utils.helper.swing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiTreeTable extends JTable {
    private NbiTreeTableModel model;
    
    private NbiTreeTableColumnRenderer treeRenderer;
    
    private boolean mousePressedEventConsumed = false;
    
    public NbiTreeTable(final NbiTreeTableModel model) {
        this.model = model;
        
        setTreeColumnRenderer(new NbiTreeTableColumnRenderer(this));
        model.setTree(treeRenderer);
        
        super.setModel(model);
        
        getColumnModel().getColumn(model.getTreeColumnIndex()).setCellRenderer(treeRenderer);
        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    public void updateUI() {
        super.updateUI();
        
        if (treeRenderer != null) {
            treeRenderer.updateUI();
        }
    }
    
    public void setRowHeight(int height) {
        super.setRowHeight(height);
        
        if (treeRenderer != null) {
            treeRenderer.setRowHeight(height);
        }
    }
    
    public NbiTreeTableModel getModel() {
        return model;
    }
    
    public NbiTreeTableColumnRenderer getTreeColumnRenderer() {
        return treeRenderer;
    }
    
    public void setTreeColumnRenderer(NbiTreeTableColumnRenderer renderer) {
        treeRenderer = renderer;
        
        model.setTree(renderer);
        model.setTreeModel(renderer.getModel());
        
        treeRenderer.setRowHeight(getRowHeight());
    }
    
    public NbiTreeTableColumnCellRenderer getTreeColumnCellRenderer() {
        return treeRenderer.getTreeColumnCellRenderer();
    }
    
    public void setTreeColumnCellRenderer(NbiTreeTableColumnCellRenderer renderer) {
        treeRenderer.setTreeColumnCellRenderer(renderer);
    }
    
    protected void processMouseEvent(MouseEvent event) {
        int column = columnAtPoint(event.getPoint());
        int row = rowAtPoint(event.getPoint());
        
        if ((event.getID() == MouseEvent.MOUSE_RELEASED) && mousePressedEventConsumed) {
            mousePressedEventConsumed = false;
            event.consume();
            return;
        }
        
        if (mouseEventHitTreeHandle(event)) {
            mousePressedEventConsumed = true;
            event.consume();
            sendTreeHandleEvents(event);
            return;
        }
        
        mousePressedEventConsumed = false;
        super.processMouseEvent(event);
    }
    
    protected void processKeyEvent(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_RELEASED) {
            int row = getSelectedRow();
            
            if (row != -1) {
                if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (treeRenderer.isExpanded(row)) {
                        treeRenderer.collapseRow(row);
                    } else {
                        int parentRow = treeRenderer.getRowForPath(treeRenderer.getPathForRow(row).getParentPath());
                        
                        treeRenderer.collapseRow(parentRow);
                        getSelectionModel().setSelectionInterval(parentRow, parentRow);
                    }
                    event.consume();
                    return;
                }
                if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (treeRenderer.isCollapsed(row)) {
                        treeRenderer.expandRow(row);
                    }
                    event.consume();
                    return;
                }
            }
        }
        
        super.processKeyEvent(event);
    }
    
    private boolean mouseEventHitTreeHandle(MouseEvent event) {
        if ((event.getID() != MouseEvent.MOUSE_PRESSED)) {
            return false;
        }
        
        int column = columnAtPoint(event.getPoint());
        int row = rowAtPoint(event.getPoint());
        
        if (column == model.getTreeColumnIndex()) {
            MouseEvent mousePressed = new MouseEvent(treeRenderer,
                    MouseEvent.MOUSE_PRESSED,
                    event.getWhen(),
                    event.getModifiers(),
                    event.getX() - getCellRect(row, column, true).x,
                    event.getY(),
                    event.getClickCount(),
                    event.isPopupTrigger());
            MouseEvent mouseReleased = new MouseEvent(treeRenderer,
                    MouseEvent.MOUSE_RELEASED,
                    event.getWhen(),
                    event.getModifiers(),
                    event.getX() - getCellRect(row, column, true).x,
                    event.getY(),
                    event.getClickCount(),
                    event.isPopupTrigger());
            
            TreePath targetPath = treeRenderer.getPathForRow(row);
            
            boolean currentState = treeRenderer.isExpanded(targetPath);
            
            // dispatch the event and see whether the node changed its state
            model.consumeNextExpansionEvent();
            treeRenderer.dispatchEvent(mousePressed);
            treeRenderer.dispatchEvent(mouseReleased);
            
            if (treeRenderer.isExpanded(targetPath) == currentState) {
                model.cancelConsume();
                return false;
            } else {
                model.consumeNextExpansionEvent();
                treeRenderer.dispatchEvent(mousePressed);
                treeRenderer.dispatchEvent(mouseReleased);
                return true;
            }
        } else {
            return false;
        }
    }
    
    private void sendTreeHandleEvents(MouseEvent event) {
        int column = model.getTreeColumnIndex();
        int row = rowAtPoint(event.getPoint());
        
        MouseEvent mousePressed = new MouseEvent(treeRenderer,
                MouseEvent.MOUSE_PRESSED,
                event.getWhen(),
                event.getModifiers(),
                event.getX() - getCellRect(row, column, true).x,
                event.getY(),
                event.getClickCount(),
                event.isPopupTrigger());
        MouseEvent mouseReleased = new MouseEvent(treeRenderer,
                MouseEvent.MOUSE_RELEASED,
                event.getWhen(),
                event.getModifiers(),
                event.getX() - getCellRect(row, column, true).x,
                event.getY(),
                event.getClickCount(),
                event.isPopupTrigger());
        
        treeRenderer.dispatchEvent(mousePressed);
        treeRenderer.dispatchEvent(mouseReleased);
    }
}
