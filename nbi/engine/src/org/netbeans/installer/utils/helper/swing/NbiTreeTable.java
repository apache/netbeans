/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
