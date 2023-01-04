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
package org.netbeans.lib.profiler.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import org.netbeans.lib.profiler.ui.AppearanceController;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class ProfilerTableContainer extends JPanel {
    
    private static final String PROP_COLUMN = "column"; // NOI18N

    private ProfilerTable table;
    private JScrollPane tableScroll;
    private JPanel scrollersPanel;

    public ProfilerTableContainer(final ProfilerTable table, boolean decorated,
                                  ColumnChangeAdapter adapter) {
        super(new BorderLayout());
        AppearanceController.getDefault().customizeProfilerTableContainer(this);
        
        this.table = table;
        
        tableScroll = new JScrollPane(table) {
            @Override
            protected JViewport createViewport() {
                if (getViewport() == null) return customViewport(table);
                else return super.createViewport();
            }
        };
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureVerticalScrollBar(tableScroll.getVerticalScrollBar());
        if (!decorated) {
            tableScroll.setBorder(BorderFactory.createEmptyBorder());
            tableScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        }
        add(tableScroll, BorderLayout.CENTER);
        
        final ProfilerColumnModel cModel = table._getColumnModel();
        
        final Set<Integer> scrollableColumns = table.getScrollableColumns();
        if (scrollableColumns != null && !scrollableColumns.isEmpty()) {
            scrollersPanel = new JPanel(null) {
                @Override
                public void doLayout() {
                    int height = getHeight();
                    if (height > 0) for (Component component : getComponents()) {
                        JScrollBar scroller = (JScrollBar)component;
                        int column = table.convertColumnIndexToView(getColumn(scroller));
                        Rectangle rect = table.getTableHeader().getHeaderRect(column);
                        scroller.setBounds(rect.x, 0, rect.width, height);
                        scroller.doLayout();
                    }
                }

                @Override
                public Dimension getPreferredSize() {
                    Dimension d = super.getPreferredSize();
                    d.height = 0;
                    for (Component component : getComponents()) {
                        JScrollBar scroller = (JScrollBar)component;
                        if (cModel.isColumnVisible(getColumn(scroller)))
                            d.height = Math.max(d.height, scroller.getPreferredSize().height);
                    }
                    return d;
                }
            };
            scrollersPanel.setOpaque(true);
            scrollersPanel.setBackground(UIUtils.getProfilerResultsBackground());

            for (final Integer column : scrollableColumns) {
                final JScrollBar scroller = new JScrollBar(JScrollBar.HORIZONTAL) {
                    private boolean adjusting = false;
                    {
                        putClientProperty(PROP_COLUMN, column);
                    }

                    @Override
                    public void setValue(int value) {
                        value = checkedValue(value);
                        super.setValue(value);
                        updateColumnOffset(value);
                    }

                    @Override
                    public void setValues(int value, int extent, int min, int max) {
                        if (adjusting) return;
                        value = checkedValue(value);
                        setEnabled(extent < max);
                        if (trackEnd()) value = max - extent;
                        super.setValues(value, extent, min, max);
                        updateColumnOffset(value);
                    }

                    @Override
                    public void setValueIsAdjusting(boolean b) {
                        adjusting = b;
                        super.setValueIsAdjusting(b);
                        if (!adjusting) updateHorizontalScrollBars(table, column, false);
                    }

                    @Override
                    public int getUnitIncrement() {
                        return 20;
                    }

                    @Override
                    public int getUnitIncrement(int direction) {
                        return getUnitIncrement();
                    }

                    @Override
                    public int getBlockIncrement() {
                        return (int)(getVisibleAmount() * 0.9f);
                    }

                    @Override
                    public int getBlockIncrement(int direction) {
                        return getBlockIncrement();
                    }
                    private void updateColumnOffset(int value) {
                        table.setColumnOffset(column, value);
                    }
                    private boolean trackEnd() {
                        if (!isEnabled()) return false;
                        int visible = getVisibleAmount();
                        if (visible > 0) return getValue() + visible >= getMaximum();
                        return !table.isLeadingAlign(column);
                    }
                    private int checkedValue(int value) {
                        value = Math.max(0, value);
                        value = Math.min(getMaximum() - getVisibleAmount(), value);
                        return value;
                    }
                };
                scroller.addMouseWheelListener(new MouseWheelListener() {
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        scroll(scroller, e);
                    }
                });
                scrollersPanel.add(scroller);
            }
            
            MouseWheelListener[] listeners = tableScroll.getMouseWheelListeners();
            if (listeners != null && listeners.length == 1) {
                final MouseWheelListener listener = listeners[0];
                tableScroll.removeMouseWheelListener(listener);
                tableScroll.addMouseWheelListener(new MouseWheelListener() {
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        if (onlyShift(e)) {
                            int c = table.columnAtPoint(e.getPoint());
                            int _c = c == -1 ? -1 : table.convertColumnIndexToModel(c);
                            if (_c != -1 && table.isScrollableColumn(_c)) {
                                JScrollBar scroller = getScroller(_c);
                                if (scroller != null) scroll(scroller, e);
                                return;
                            }
                        }
                        listener.mouseWheelMoved(e);
                    }
                });
            }
            
            cModel.addColumnChangeListener(new ColumnChangeAdapter() {
                @Override
                public void columnWidthChanged(int column, int oldWidth, int newWidth) {
                    if (table.isScrollableColumn(column))
                        updateHorizontalScrollBars(table, column, true);
                }

                @Override
                public void columnPreferredWidthChanged(int column, int oldWidth, int newWidth) {
                    if (table.isScrollableColumn(column))
                        updateHorizontalScrollBars(table, column, false);
                }
            });
            
            cModel.addColumnModelListener(new TableColumnModelListener() {
                public void columnAdded(TableColumnModelEvent e) {}
                public void columnRemoved(TableColumnModelEvent e) {}
                public void columnMoved(TableColumnModelEvent e) { moved(e); }
                public void columnMarginChanged(ChangeEvent e) { margin(e); }
                public void columnSelectionChanged(ListSelectionEvent e) {}
                private void moved(TableColumnModelEvent e) {
                    if (e.getFromIndex() != e.getToIndex())
                        updateHorizontalScrollBars(table, -1, true);
                }
                private void margin(ChangeEvent e) {
                    // Invoke later to let the columnWidthChanged perform first
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            for (Integer column : scrollableColumns) {
                            int _column = table.convertColumnIndexToView(column);
                                Rectangle rect = table.getTableHeader().getHeaderRect(_column);
                                Rectangle scroll = getScroller(column).getBounds();
                                // Column position changed without changing size
                                if (rect.x != scroll.x || rect.width != scroll.width) {
                                    updateHorizontalScrollBars(table, -1, true);
                                    break;
                                }
                            }
                        }
                    });
                }
            });

            add(scrollersPanel, BorderLayout.SOUTH);
        }
        
        if (adapter != null) cModel.addColumnChangeListener(adapter);
    }
    
    private static void scroll(JScrollBar scroller, MouseWheelEvent event) {
        if (event.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int direction = event.getUnitsToScroll() < 0 ? -1 : 1;
            int increment = scroller.getUnitIncrement(direction);
            int amount = event.getScrollAmount();
            int oldValue = scroller.getValue();
            int newValue = oldValue + increment * amount * direction;
            if (oldValue != newValue) scroller.setValue(newValue);
            event.consume();
        }
    }
    
    private static boolean onlyShift(MouseEvent e) {
        return e.isShiftDown() && !(e.isAltDown() || e.isAltGraphDown() ||
                                    e.isControlDown() || e.isMetaDown());
    }
    
    public boolean tableNeedsScrolling() {
        return tableScroll.getVerticalScrollBar().isEnabled();
    }
    
    public BufferedImage createTableScreenshot(boolean onlyVisibleArea) {
        return onlyVisibleArea ? UIUtils.createScreenshot(tableScroll) :
                                 UIUtils.createScreenshot(table);
    }
    
    private int getColumn(JScrollBar scroller) {
        return (Integer)scroller.getClientProperty(PROP_COLUMN);
    }
    
    private JScrollBar getScroller(int column) {
        for (Component component : scrollersPanel.getComponents()) {
            JScrollBar scroller = (JScrollBar)component;
            if (getColumn(scroller) == column) return scroller;
        }
        return null;
    }
    
    private JViewport customViewport(final JTable table) {
        return new JViewport() {
            {
                setBackground(table.getBackground());
                table.addPropertyChangeListener("enabled", new PropertyChangeListener() { // NOI18N
                    public void propertyChange(PropertyChangeEvent evt) { setBackground(table.getBackground()); }
                });
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                
                Component view = getView();
                if (view == table) {
                    if (!listening) hookColumns();
                    
                    int height = getHeight();
                    int viewHeight = view.getHeight();
                    if (height > viewHeight) {
                        g.setColor(table.getGridColor());
                        JTableHeader header = table.getTableHeader();
                        if (header != null) {
                            for (int i = 0; i < table.getColumnCount(); i++) {
                                Rectangle rect = header.getHeaderRect(i);
                                if (rect.width > 0) g.drawLine(rect.x + rect.width - 1, viewHeight,
                                                               rect.x + rect.width - 1, height - 1);
                            }
                        } else if (table.getRowCount() > 0) {
                            for (int i = 0; i < table.getColumnCount(); i++) {
                                Rectangle rect = table.getCellRect(0, i, true);
                                if (rect.width > 0) g.drawLine(rect.x + rect.width - 1, viewHeight,
                                                               rect.x + rect.width - 1, height - 1);
                            }
                        }
                    }
                }
            }
            private boolean listening;
            private void hookColumns() {
                table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                    public void columnAdded(TableColumnModelEvent e) { repaint(); }
                    public void columnRemoved(TableColumnModelEvent e) { repaint(); }
                    public void columnMoved(TableColumnModelEvent e) { repaint(); }
                    public void columnMarginChanged(ChangeEvent e) { repaint(); }
                    public void columnSelectionChanged(ListSelectionEvent e) { repaint(); }
                });
                listening = true;
            }
        };
    }
    
    private void configureVerticalScrollBar(final JScrollBar scrollBar) {
        scrollBar.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                scrollBar.setEnabled(ProfilerTableContainer.this.isEnabled() &&
                          scrollBar.getVisibleAmount() < scrollBar.getMaximum());
            }
        });
    }
    
    private void updateHorizontalScrollBars(ProfilerTable table, int column, boolean layout) {
        if (column != -1) {
            JScrollBar scroll = getScroller(column);
            int offset = table.getColumnOffset(column);
            int columnPref = table.getColumnPreferredWidth(column);
            int _column = table.convertColumnIndexToView(column);
            int columnAct = table.getTableHeader().getHeaderRect(_column).width;
            if (columnPref > columnAct) {
                int value = Math.min(offset, columnPref - columnAct);
                scroll.setValues(value, columnAct, 0, columnPref);
            } else {
                scroll.setValues(0, 0, 0, 0);
            }
        }
        
        if (layout) {
            doLayout();
            scrollersPanel.doLayout();
            repaint();
        }
    }
    
    
    public static class ColumnChangeAdapter implements ProfilerColumnModel.Listener {
        
        public void columnOffsetChanged(int column, int oldOffset, int newOffset) {}
        
        public void columnWidthChanged(int column, int oldWidth, int newWidth) {}
        
        public void columnPreferredWidthChanged(int column, int oldWidth, int newWidth) {}
        
    }
    
}
