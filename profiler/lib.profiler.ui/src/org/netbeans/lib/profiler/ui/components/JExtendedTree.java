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

package org.netbeans.lib.profiler.ui.components;

import org.netbeans.lib.profiler.ui.UIConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.tree.TreeCellRendererPersistent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;


/**
 *
 * @author Jiri Sedlacek
 */
public class JExtendedTree extends JTree implements CellTipAware {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class PrivateComponentListener implements MouseListener, MouseMotionListener {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
            // --- CellTip support ------------------
            CellTipManager.sharedInstance().setEnabled(false);
        }

        public void mouseExited(MouseEvent e) {
            // --- CellTip support ------------------
            // Return if mouseExit occured because of showing heavyweight celltip
            if (contains(e.getPoint()) && cellTip.isShowing()) {
                return;
            }

            CellTipManager.sharedInstance().setEnabled(false);
            lastTreePath = null;
        }

        public void mouseMoved(MouseEvent e) {
            // --- CellTip support ------------------
            processCellTipMouseMove(e);
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected JToolTip cellTip;
    protected Rectangle rendererRect;
    protected TreePath lastTreePath = null;
    private PrivateComponentListener componentListener = new PrivateComponentListener();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of JExtendedTree */
    public JExtendedTree() {
        addMouseListener(componentListener);
        addMouseMotionListener(componentListener);

        setRowHeight(UIUtils.getDefaultRowHeight()); // celltips require to have row height initialized!

        // --- CellTip support ------------------
        cellTip = createCellTip();
        cellTip.setBackground(getBackground());
        cellTip.setBorder(BorderFactory.createLineBorder(UIConstants.TABLE_VERTICAL_GRID_COLOR));
        cellTip.setLayout(new BorderLayout());

        CellTipManager.sharedInstance().registerComponent(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public JToolTip getCellTip() {
        return cellTip;
    }

    public Point getCellTipLocation() {
        if (rendererRect == null) {
            return null;
        }

        return new Point(rendererRect.getLocation().x - 1, rendererRect.getLocation().y - 1);
    }

    public void processMouseEvent(MouseEvent e) {
        if (e instanceof MouseWheelEvent) {
            Component target = JExtendedTree.this.getParent();
            if (!(target instanceof JViewport))
                target = JExtendedTree.this;
            MouseEvent mwe = SwingUtilities.convertMouseEvent(
                    JExtendedTree.this, (MouseWheelEvent)e, target);
            target.dispatchEvent((MouseWheelEvent)mwe);
        } else {
            super.processMouseEvent((MouseEvent)e);
        }
    }

    protected JToolTip createCellTip() {
        return new JToolTip();
    }

    protected void processCellTipMouseMove(MouseEvent e) {
        // Identify treetable row and column at cursor
        TreePath currentTreePath = getPathForLocation(e.getX(), e.getY());

        // Return if treetable cell is the same as in previous event
        if (currentTreePath == lastTreePath) {
            return;
        }

        lastTreePath = currentTreePath;

        // Return if cursor isn't at any cell
        if (lastTreePath == null) {
            CellTipManager.sharedInstance().setEnabled(false);

            return;
        }

        Component cellRenderer;
        Component cellRendererPersistent;
        int row = getRowForPath(lastTreePath);

        TreeCellRenderer treeCellRenderer = getCellRenderer();

        if (!(treeCellRenderer instanceof TreeCellRendererPersistent)) {
            return;
        }

        cellRenderer = treeCellRenderer.getTreeCellRendererComponent(JExtendedTree.this, lastTreePath.getLastPathComponent(),
                                                                     false, isExpanded(row),
                                                                     getModel().isLeaf(lastTreePath.getLastPathComponent()), row,
                                                                     false);
        cellRendererPersistent = ((TreeCellRendererPersistent) treeCellRenderer).getTreeCellRendererComponentPersistent(JExtendedTree.this,
                                                                                                                        lastTreePath
                                                                                                                        .getLastPathComponent(),
                                                                                                                        false,
                                                                                                                        isExpanded(row),
                                                                                                                        getModel()
                                                                                                                            .isLeaf(lastTreePath
                                                                                                                                    .getLastPathComponent()),
                                                                                                                        row, false);

        // Return if celltip is not supported for the cell
        if (cellRenderer == null) {
            CellTipManager.sharedInstance().setEnabled(false);

            return;
        }

        Point cellStart = getPathBounds(lastTreePath).getLocation();
        rendererRect = new Rectangle(cellStart.x, cellStart.y, cellRenderer.getPreferredSize().width,
                                     cellRenderer.getPreferredSize().height + 2);

        if (!rendererRect.contains(e.getPoint())) {
            CellTipManager.sharedInstance().setEnabled(false);

            return;
        }

        // Return if cell contents is fully visible
        Rectangle visibleRect = getVisibleRect();

        if ((rendererRect.x >= visibleRect.x) && ((rendererRect.x + rendererRect.width) <= (visibleRect.x + visibleRect.width))) {
            CellTipManager.sharedInstance().setEnabled(false);

            return;
        }

        while (cellTip.getComponentCount() > 0) {
            cellTip.remove(0);
        }

        cellTip.add(cellRendererPersistent, BorderLayout.CENTER);
        cellTip.setPreferredSize(new Dimension(cellRendererPersistent.getPreferredSize().width + 2, getRowHeight() + 2));

        CellTipManager.sharedInstance().setEnabled(true);
    }
}
