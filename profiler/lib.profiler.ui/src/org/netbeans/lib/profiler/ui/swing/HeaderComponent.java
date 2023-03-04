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
package org.netbeans.lib.profiler.ui.swing;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * JComponent with TableHeader appearance.
 *
 * @author Jiri Sedlacek
 */
class HeaderComponent extends JComponent {
    
    private boolean isPressed;
    private boolean isSelected;
    
    HeaderComponent(final ActionListener listener) {
        if (listener != null) addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                isSelected = true;
                repaint();
            }
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                if (isSelected) {
                    repaint();
                    fireActionEvent(e);
                }
            }
            public void mouseEntered(MouseEvent e) {
                if (isPressed || !isButtonDown(e)) {
                    isSelected = true;
                    repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if (isSelected) {
                    isSelected = false;
                    repaint();
                }
            }
            private void fireActionEvent(MouseEvent e) {
                ActionEvent ae = new ActionEvent(e.getSource(), e.getID(),
                                                 e.paramString(), e.getWhen(),
                                                 e.getModifiers());
                listener.actionPerformed(ae);
            }
            private boolean isButtonDown(MouseEvent e) {
                return SwingUtilities.isLeftMouseButton(e) ||
                       SwingUtilities.isMiddleMouseButton(e) ||
                       SwingUtilities.isRightMouseButton(e);
            }
        });
    }
    
    protected void paintComponent(Graphics g) {
        JTableHeader header = getHeader();
        setupHeader(header);
        TableCellRenderer renderer = header.getDefaultRenderer();
        JComponent component = (JComponent)renderer.getTableCellRendererComponent(
                               getTable(), "", isSelected && isPressed, isFocusOwner(), -1, 0); // NOI18N
        
        int height = header.getPreferredSize().height;
        component.setBounds(0, 0, getWidth(), height);
        component.setOpaque(false);
        getPainter().paintComponent(g, component, null, 0, 0, getWidth(), height, false);
    }
    
    private void setupHeader(JTableHeader h) {
        h.setDraggedColumn(isSelected && isPressed ? getColumn() : null);
        
        MouseEvent e = isSelected && !isPressed ?
                new MouseEvent(h, MouseEvent.MOUSE_ENTERED, 1, 0, 1, 1, 0, false) :
                new MouseEvent(h, MouseEvent.MOUSE_EXITED, 1, 0, 0, 0, 0, false);
        h.dispatchEvent(e);
    }
    
    
    private static CellRendererPane PAINTER;
    private static CellRendererPane getPainter() {
        if (PAINTER == null) PAINTER = new CellRendererPane();
        return PAINTER;
    }
    
    private static JTable REF_TABLE;
    private static JTable getTable() {
        if (REF_TABLE == null) REF_TABLE = new JTable(new Object[][] {{}},
                                                      new Object[] { " " }) { // NOI18N
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public boolean contains(int x, int y) { return x == 1 && y == 1; }
                };
            }
        };
        return REF_TABLE;
    }
    
    private static JTableHeader getHeader() {
        return getTable().getTableHeader();
    }
    
    private static TableColumn getColumn() {
        return getHeader().getColumnModel().getColumn(0);
    }
    
}
