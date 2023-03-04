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
package org.netbeans.modules.java.hints.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

/**
 * Generic help utilitie for Panel Swing UI.
 *
 * @author sdedic
 */
public class InnerPanelSupport {
    private static final String PROP_BULK_ENABLE = InnerPanelSupport.class.getName() + ".BulkEnable"; // NOI18N
    
    /**
     * Returns all children (recursive) of the parent. Children are enumerated
     * top-down, depth-first. Parent is not returned in the list.
     * 
     * @param parent parent to start the enumeration.
     * @return all (sub)components.
     */
    public static List<Component> getAllComponents(JComponent parent) {
        List<Component> result = new ArrayList<>(5);
        result.addAll(Arrays.asList(parent.getComponents()));
        for (int index = 0; index < result.size(); index++) {
            Component c = result.get(index);
            if (c instanceof Container) {
                result.addAll(Arrays.asList(((Container)c).getComponents()));
            }
        }
        return result;
    }
    
    /**
     * Enables or disables UI within the panel.
     * The panel should be constructed initially enabled with Components enabled as
     * if the panel should be operational. Then {@link #enablePanel}(panel, false) must be
     * called to disable the panel. 
     * <p/>
     * The call will disable all visible and currently enabled Components. The list of
     * disabled components is remembered for further enable operation. When enable is called,
     * the exact set of components will be enabled, in the reverse order. This means that the
     * containing JPanels will be enabled after all their children and may miake additional
     * adjustments without this {@link enablePanel} interfering.
     * <p/>
     * If any of the enabled {@link Container} components implements {@link Runnable}
     * interface, it will be run to update the internal state after <b>all components</b>
     * are enabled. Runnables are run in down-top order (from children to parents).
     * 
     * @param panel the root of enable/disable operation
     * @param enable enable flag.
     */
    public static void enablePanel(JPanel panel, boolean enable) {
        panel.setEnabled(enable);
        List<Component> contents = getAllComponents(panel);
        List<Component> enabled = new ArrayList<>(contents.size() / 2);
        
        if (enable) {
            List<Runnable> reCheck = new ArrayList<>(3);
            enabled = (List<Component>)panel.getClientProperty(PROP_BULK_ENABLE);
            if (enabled == null) {
                return;
            }
            for (int i = enabled.size() - 1; i >= 0; i--) {
                Component c = enabled.get(i);
                c.setEnabled(true);
                if (c instanceof JPanel && c instanceof Runnable) {
                    reCheck.add((Runnable)c);
                }
            }
            for (Runnable c : reCheck) {
                c.run();
            }
            panel.setEnabled(enable);
            if (panel instanceof Runnable) {
                // update after enable
                ((Runnable)panel).run();
            }
        } else {
            for (Component c : contents) {
                if (c.isEnabled() && c.isVisible()) {
                    enabled.add(c);
                    c.setEnabled(false);
                    
                    if (c instanceof JList) {
                        ((JList)c).setSelectedIndex(-1);
                    } else if (c instanceof JTable) {
                        ((JTable)c).getSelectionModel().clearSelection();
                    } else if (c instanceof JTextField) {
                        ((JTextField)c).select(0, 0);
                    }
                }
            }
            
            panel.putClientProperty(PROP_BULK_ENABLE, enabled);
        }
    }

    /**
     * Tooltip component, which paints a Image previously
     * painted by original CellRenderer
     */
    private static class RenderedImage extends JComponent {
        private BufferedImage image;

        public RenderedImage() {
        }

        public void setImage(BufferedImage image) {
            this.image = image;
            invalidate();
            if (isVisible()) {
                repaint();
            }
        }
        
        @Override
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(), image.getHeight());
        }
    }
    
    /**
     * Support which manages tooltip display on long cells.
     * Attaches as Mouse(Move)Listener to the underlying Table, and also to the
     * tooltip (if it is displayed). Propagates mouse events from the tooltip to the
     * underlying table.
     * <p/>
     * Displays tooltip above the table's cell whenever the mouse hovers over a cell,
     * which is not fully displayed to the right; the cell must be entirely vertically
     * visible, otherwise the tooltip will not be shown.
     * <p/>
     * The tooltip is drawn using the JTable's own CellRenderer and saved as a BufferedImage.
     * The image is updated whenever table selection changes to mimic the original
     * table cell's graphics which is overlaid with the tooltip.
     * <p/>
     */
    private static class CellExtensionSupport implements MouseListener, MouseMotionListener, ListSelectionListener  {
        private JTable  listClasses;
        private final CellRendererPane rendererPane = new CellRendererPane();
        private int currentRow = -1;
        private int currentCol = -1;
        private Popup currentPopup;
        private RenderedImage popupContents;

        private CellExtensionSupport(JTable listClasses) {
            this.listClasses = listClasses;
        }
        
        
        private void hidePopup() {
            if (currentPopup != null) {
                currentPopup.hide();
                currentPopup = null;
                popupContents = null;
                currentRow = -1;
                currentCol = -1;
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (popupContents != null) {
                updateTooltipImage(popupContents, currentRow, currentCol);
            }
        }
        
        private boolean updateTooltipImage(RenderedImage contents, int row, int col) {
            TableCellRenderer renderer = listClasses.getCellRenderer(row, col); 
            Component component = listClasses.prepareRenderer(renderer, row, col);
            
            if (!(component instanceof JComponent)) {
                // sorry.
                return false;
            }
            Rectangle cellRect = listClasses.getCellRect(row, col, false);
            Dimension size = component.getPreferredSize();
            Rectangle visibleRect = listClasses.getVisibleRect();
            
            // The visible region is wide enough, hide the tooltip
            if (cellRect.width >= size.width) {
                hidePopup();
                return false;
            }           
            // Hide if the cell does not vertically fit
            if (cellRect.y + size.height > visibleRect.y + visibleRect.height + 1) {
                hidePopup();
                return false;
            }
            BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            Graphics g = img.getGraphics();
            g.setClip(null);
            g.setColor(listClasses.getBackground());
            g.fillRect(0, 0, size.width, size.height);
            g.translate(-cellRect.x, -cellRect.y);
//            g.translate(-visibleRect.x, -visibleRect.y);
            
            cellRect.width = size.width;
            // prevent some repaing issues, see javadoc for CellRendererPane.
            rendererPane.paintComponent(g, component, listClasses, cellRect);
            
            // if table displays lines, frame the cell's display using lines.
            if (listClasses.getShowHorizontalLines()) {
                int rightX = size.width - 1;
                g.translate(cellRect.x, cellRect.y);
                g.setColor(listClasses.getForeground());
                g.drawLine(0, 0, rightX, 0);
                g.drawLine(rightX, 0, rightX, size.height);
                g.drawLine(rightX, size.height - 1, 0, size.height - 1);
            }
            g.dispose();
            rendererPane.remove(component);
            contents.setImage(img);
            return true;
        }

        @Override
        public void mouseDragged(MouseEvent e) {}
        
        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            int col = listClasses.columnAtPoint(p);
            int row = listClasses.rowAtPoint(p);
            
            if (col < 0 || row < 0) {
                hidePopup();
                return;
            }
            if (col == currentCol && row == currentRow) {
                // the tooltip is (probably) shown, do not create again
                return;
            }
            Rectangle cellRect = listClasses.getCellRect(row, col, false);
            Point pt = cellRect.getLocation();
            SwingUtilities.convertPointToScreen(pt, listClasses);
            
            RenderedImage ri = new RenderedImage();
            if (!updateTooltipImage(ri, row, col)) {
                return;
            }
            ri.addMouseListener(this);
            
            Popup popup = PopupFactory.getSharedInstance().getPopup(listClasses, ri, pt.x, pt.y);
            popupContents = ri;
            currentPopup = popup;
            currentCol = col;
            currentRow = row;
            popup.show();
            System.err.println("Hello");
        }
        
        public void redispatchMouseEvent(MouseEvent e) {
            if (!(e.getComponent() instanceof RenderedImage)) {
                return;
            }
            MouseEvent delegate = SwingUtilities.convertMouseEvent(e.getComponent(), e, listClasses);
            listClasses.dispatchEvent(delegate);
            // if the table started editing, remove the popup:
            if (listClasses.isEditing()) {
                hidePopup();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
//            hidePopup();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (e.getSource() == listClasses && currentPopup != null) {
                // check if hovering above the popup -> do not dismiss.
                // the mouse exits the table, when it crosses the boudary to the
                // tooltip.
                Point screen = e.getLocationOnScreen();
                Rectangle visibleRec = listClasses.getVisibleRect();
                Point pt = visibleRec.getLocation();
                SwingUtilities.convertPointToScreen(pt, listClasses);
                visibleRec.setLocation(pt);
                if (visibleRec.contains(screen)) {
                    return;
                }
                
                hidePopup();
            } else if (e.getSource() == this.popupContents) {
                // exit from the popup
                hidePopup();
            }
        }
        
    }

    /**
     * Enhances a JTable, so it displays tooltips for cells which are cut
     * at the right side.
     * This feature is useful for tables with no horizontal scrollbar, since
     * the user may see full cell's contents when hovering above the row/cell.
     * <p/>
     * A tooltip is displayed above table, which hides itself whenever the user
     * moves mouse out of component or the tooltip or starts editing (if the table
     * is cell-editable).
     * <p/>
     * The tooltip's contents is painted by the configured CellRenderer.
     * 
     * @param table table to enhance
     * @return the table; just use as part of new expression.
     */
    public static JTable displayExtendedCells(JTable table) {
        CellExtensionSupport supp = new CellExtensionSupport(table);
        table.addMouseListener(supp);
        table.addMouseMotionListener(supp);
        table.getSelectionModel().addListSelectionListener(supp);
        
        return table;
    }

}
