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

package org.netbeans.modules.php.project.connections.ui.transfer.tree;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

// copied from spi.palette
/**
 * This listener controls click and double click on the CheckNodes. In addition
 * to it provides support for keyboard node checking/unchecking and opening
 * document.
 *
 * todo (#pf): Improve behaviour and comments.
 *
 * @author  Pavel Flaska, S. Aubrecht
 */
final class CheckListener implements MouseListener, KeyListener {

    private final TransferSelectorModel model;

    public CheckListener(TransferSelectorModel model) {
        assert model != null;
        this.model = model;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // todo (#pf): we need to solve problem between click and double
        // click - click should be possible only on the check box area
        // and double click should be bordered by title text.
        // we need a test how to detect where the mouse pointer is
        JTree tree = (JTree) e.getSource();
        Point p = e.getPoint();
        int x = e.getX();
        int y = e.getY();
        int row = tree.getRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);

        // if path exists and mouse is clicked exactly once
        if (path == null) {
            return;
        }

        Node node = Visualizer.findNode(path.getLastPathComponent());
        if (node == null) {
            return;
        }

        Rectangle chRect = CheckRenderer.getCheckBoxRectangle();
        Rectangle rowRect = tree.getPathBounds(path);
        chRect.setLocation(chRect.x + rowRect.x, chRect.y + rowRect.y);
        if (e.getClickCount() == 1 && chRect.contains(p)) {
            boolean isSelected = model.isNodeSelected(node);
            model.setNodeSelected(node, !isSelected);
            tree.repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == ' ') { // NOI18N
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            if (path == null) {
                return;
            }

            Node node = Visualizer.findNode(path.getLastPathComponent());
            if (node == null) {
                return;
            }

            boolean isSelected = model.isNodeSelected(node);
            model.setNodeSelected(node, !isSelected);
            tree.repaint();

            e.consume();
        }
    }
}
