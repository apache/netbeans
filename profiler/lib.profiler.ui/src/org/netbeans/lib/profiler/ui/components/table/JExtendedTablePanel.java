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

package org.netbeans.lib.profiler.ui.components.table;

import org.netbeans.lib.profiler.ui.UIConstants;
import org.netbeans.lib.profiler.ui.components.JExtendedTable;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.JTableHeader;


/**
 * A subclass of JPanel that provides additional fuctionality for displaying JExtendedTable.
 * JExtendedTablePanel provides JScrollPane for displaying JExtendedTable and customized Viewport.
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
public class JExtendedTablePanel extends JPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    // Custom ExtendedTable Viewport
    private class CustomExtendedTableViewport extends JViewport {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private JTableHeader tableHeader;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CustomExtendedTableViewport(JExtendedTable extendedTable) {
            super();
            setView(extendedTable);
            updateBackgroundColor();
            this.tableHeader = extendedTable.getTableHeader();
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            if (UIConstants.SHOW_TABLE_VERTICAL_GRID) {
                paintVerticalLines(g);
            }
        }

        private int getEmptySpaceY() {
            if (getView() == null) {
                return 0;
            }

            return getView().getHeight();
        }

        private void paintVerticalLines(Graphics g) {
            int emptySpaceY = getEmptySpaceY();

            if (emptySpaceY > 0) {
                g.setColor(UIConstants.TABLE_VERTICAL_GRID_COLOR);

                int cellX = 0;
                int cellWidth;

                for (int i = 0; i < extendedTable.getColumnModel().getColumnCount(); i++) {
                    cellWidth = extendedTable.getColumnModel().getColumn(i).getWidth();
                    g.drawLine((cellX + cellWidth) - 1, emptySpaceY, (cellX + cellWidth) - 1, getHeight() - 1);
                    cellX += cellWidth;
                }
            }
        }

        private void updateBackgroundColor() {
            setBackground(extendedTable.isEnabled() ? extendedTable.getBackground()
                                                    : UIManager.getColor("TextField.inactiveBackground")); // NOI18N
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    CustomExtendedTableViewport extendedTableViewport;
    protected JExtendedTable extendedTable;
    protected JScrollPane extendedTableScrollPane;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of JExtendedTablePanel */
    public JExtendedTablePanel(JExtendedTable extendedTable) {
        super(new BorderLayout());
        this.extendedTable = extendedTable;

        initComponents();
        hookHeaderColumnResize();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void clearBorders() {
        extendedTableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        extendedTableScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    }
    
    public void setCorner(String key, java.awt.Component corner) {
        extendedTableScrollPane.setCorner(key, corner);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateVerticalScrollbar();
        extendedTable.setEnabled(enabled);
        extendedTableViewport.updateBackgroundColor();
    }

    public JScrollPane getScrollPane() {
        return extendedTableScrollPane;
    }

    private void hookHeaderColumnResize() {
        if (extendedTable.getTableHeader() != null) {
            extendedTable.getTableHeader().getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                    public void columnAdded(TableColumnModelEvent e) {
                        extendedTableViewport.repaint();
                    }

                    public void columnMoved(TableColumnModelEvent e) {
                        extendedTableViewport.repaint();
                    }

                    public void columnRemoved(TableColumnModelEvent e) {
                        extendedTableViewport.repaint();
                    }

                    public void columnMarginChanged(ChangeEvent e) {
                        extendedTableViewport.repaint();
                    }

                    public void columnSelectionChanged(ListSelectionEvent e) {
                    } // Ignored
                });

        }
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder());

        extendedTableScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        extendedTableViewport = new CustomExtendedTableViewport(extendedTable);
        extendedTableScrollPane.setViewport(extendedTableViewport);
        extendedTableScrollPane.addMouseWheelListener(extendedTable);
        // Enable vertical scrollbar only if needed
        JScrollBar vScrollbar = extendedTableScrollPane.getVerticalScrollBar();
        vScrollbar.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateVerticalScrollbar();
            }
        });

        add(extendedTableScrollPane, BorderLayout.CENTER);
    }

    private void updateVerticalScrollbar() {
        JScrollBar vScrollbar = extendedTableScrollPane.getVerticalScrollBar();
        vScrollbar.setEnabled(JExtendedTablePanel.this.isEnabled() &&
                              vScrollbar.getVisibleAmount() < vScrollbar.getMaximum());
    }
    
}
