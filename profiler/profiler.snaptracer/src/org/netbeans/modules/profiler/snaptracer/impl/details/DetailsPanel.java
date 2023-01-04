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

package org.netbeans.modules.profiler.snaptracer.impl.details;

import org.netbeans.modules.profiler.snaptracer.impl.swing.HeaderPanel;
import org.netbeans.modules.profiler.snaptracer.impl.swing.ScrollBar;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.TimelineSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Jiri Sedlacek
 */
public final class DetailsPanel extends JPanel {

    private JPanel noDataContainer;
    private JComponent dataContainer;

    private JScrollBar scrollBar;

    private DetailsTable table;
    private final TimelineSupport support;

    private boolean selectionAdjusting;
    private KeyStroke tableKeyStroke;


    public DetailsPanel(TimelineSupport support) {
        this.support = support;
        initComponents();
        initListeners();
    }


    public void setTableModel(TableModel model) {
        if (model == null) {
            table.clearSelection();
            table.setModel(new DefaultTableModel());
            removeAll();
            add(noDataContainer, BorderLayout.CENTER);
        } else {
            int selectedRow = getSelectedRow();
            table.setModel(model);
            if (selectedRow != -1)
                table.getSelectionModel().setSelectionInterval(selectedRow,
                                                               selectedRow);
            removeAll();
            add(dataContainer, BorderLayout.CENTER);
        }
        
        validate();
        repaint();
    }


    private void initListeners() {
        TableListener tableListener = new TableListener();
        table.getSelectionModel().addListSelectionListener(tableListener);
        table.addKeyListener(tableListener);
        table.addMouseListener(tableListener);
    }

    private int getSelectedRow() {
        int selectedRow = table.getSelectedRow();
        return selectedRow == -1 ? -1 : table.convertRowIndexToModel(selectedRow);
    }

    private boolean isTrackingEnd() {
        if (scrollBar == null) return false;
        return scrollBar.getValue() + scrollBar.getVisibleAmount() >= scrollBar.getMaximum();
    }

    private boolean isSelectionChanging() {
        if (selectionAdjusting) return true;
        if (tableKeyStroke == null) return false;
        return table.getActionForKeyStroke(tableKeyStroke) != null;
    }

    private void initComponents() {        
        table = new DetailsTable();
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JViewport viewport = new Viewport(table);

        final JScrollPane tableScroll = new JScrollPane(
                                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setViewport(viewport);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        tableScroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new HeaderPanel());
        
        scrollBar = new ScrollBar(JScrollBar.VERTICAL) {
            public int getUnitIncrement(int direction) {
                JViewport vp = tableScroll.getViewport();
                Scrollable view = (Scrollable)(vp.getView());
                Rectangle vr = vp.getViewRect();
                return view.getScrollableUnitIncrement(vr, getOrientation(), direction);
            }
            public int getBlockIncrement(int direction) {
                JViewport vp = tableScroll.getViewport();
                Scrollable view = (Scrollable)(vp.getView());
                Rectangle vr = vp.getViewRect();
                return view.getScrollableBlockIncrement(vr, getOrientation(), direction);
            }
            public void setValues(int newValue, int newExtent, int newMin, int newMax) {
                setEnabled(newExtent < newMax);
                if (isEnabled() && !isSelectionChanging() && isTrackingEnd())
                    newValue = newMax - newExtent;
                super.setValues(newValue, newExtent, newMin, newMax);
            }
        };
        tableScroll.setVerticalScrollBar(scrollBar);
        dataContainer = tableScroll;

        JLabel noDataLabel = new JLabel("<No probe selected>", JLabel.CENTER);
        noDataLabel.setEnabled(false);
        noDataContainer = new JPanel(new BorderLayout());
        noDataContainer.setOpaque(false);
        noDataContainer.add(noDataLabel, BorderLayout.CENTER);

        setOpaque(false);
        setLayout(new BorderLayout());
        add(noDataContainer, BorderLayout.CENTER);
    }


    private class TableListener extends MouseAdapter implements
                                ListSelectionListener, KeyListener {
        public void valueChanged(ListSelectionEvent e) {
            selectionAdjusting = e.getValueIsAdjusting();
        }
        public void keyPressed(KeyEvent e) {
            tableKeyStroke = KeyStroke.getKeyStrokeForEvent(e);
        }
        public void keyReleased(KeyEvent e) {
            tableKeyStroke = null;
            if (e.getKeyCode() == KeyEvent.VK_SPACE)
                support.scrollChartToIndex(getSelectedRow());
        }
        public void keyTyped(KeyEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                support.scrollChartToIndex(getSelectedRow());
        }
    }


    private static class Viewport extends JViewport {
        
        private final DetailsTable view;
        private final Color background;

        Viewport(DetailsTable view) {
            super();

            setView(view);
            this.view = view;
            
            setOpaque(true);
            background = view.getBackground();
            setBackground(background);

            view.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                public void columnAdded(TableColumnModelEvent e) { repaint(); }
                public void columnMoved(TableColumnModelEvent e) { repaint(); }
                public void columnRemoved(TableColumnModelEvent e) { repaint(); }
                public void columnMarginChanged(ChangeEvent e) { repaint(); }
                public void columnSelectionChanged(ListSelectionEvent e) {}
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintVerticalLines(g);
        }

        private void paintVerticalLines(Graphics g) {
            int height = getHeight();
            int viewHeight = view.getHeight();
            if (viewHeight >= height) return;

            g.setColor(background);
            g.fillRect(0, viewHeight, getWidth(), getHeight() - viewHeight);

            int cellX = 0;
            int cellWidth;
            TableColumnModel model = view.getColumnModel();
            int columnCount = model.getColumnCount();
            
            g.setColor(DetailsTable.DEFAULT_GRID_COLOR);
            for (int i = 0; i < columnCount; i++) {
                cellWidth = model.getColumn(i).getWidth();
                cellX += cellWidth;
                g.drawLine(cellX - 1, viewHeight, cellX - 1, height);
            }
        }

    }

}
