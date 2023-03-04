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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.OverlayLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Jiri Sedlacek
 */
public class HeaderPanel extends JPanel {

    private JPanel clientContainer;
    private Header header;

    public HeaderPanel() {
        initComponents();
    }


    protected Object getRendererValue() { return null; }

    protected void setupRenderer(Component renderer) {}

    protected boolean isSelected() { return false; }

    protected boolean processMouseEvents() { return false; }


    public JPanel getClientContainer() {
        if (clientContainer == null) {
            clientContainer = Spacer.create();
            add(clientContainer, 0);
        }
        return clientContainer;
    }

    public boolean isOptimizedDrawingEnabled() {
        return clientContainer == null;
    }
    
    protected void processMouseEvent(MouseEvent e) {
        if (processMouseEvents()) header.processMouseEvent(e);
        if (!e.isConsumed()) super.processMouseEvent(e);
    }

    private void initComponents() {
        JTable impl = new JTable(new DefaultTableModel(new Object[] { "" }, 0)); // NOI18N
        TableColumnModel colMod = impl.getColumnModel();
        final TableColumn col = colMod.getColumn(0);
        impl.setFocusable(false);
        header = new Header(colMod);
        impl.setTableHeader(header);
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);

        final TableCellRenderer renderer = header.getDefaultRenderer();
        header.setDefaultRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {

                Component component = renderer.getTableCellRendererComponent(
                        table, getRendererValue(), isSelected(),
                        isSelected(), row, processMouseEvents() ? 0 : 1);

                setupRenderer(component);

                col.setWidth(header.getWidth());
                return component;
            }
        });

        JScrollPane scroll = new JScrollPane(impl, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                                   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            public Dimension getPreferredSize() { return header.getPreferredSize(); }
            public void reshape(int x, int y, int width, int height) {
                header.setPreferredSize(new Dimension(width, height));
                super.reshape(x, y, width, height);
            }
        };
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());

        setLayout(new OverlayLayout(this));
        add(scroll);
    }

    private static class Header extends JTableHeader {
        Header(TableColumnModel model) { super(model); };
        public void processMouseEvent(MouseEvent e) { super.processMouseEvent(e); }
    }

}
