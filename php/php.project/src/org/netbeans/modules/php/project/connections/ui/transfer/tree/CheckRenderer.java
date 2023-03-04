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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

// copied from spi.palette
/**
 * @author Pavel Flaska, S. Aubrecht
 */
final class CheckRenderer extends JPanel implements TreeCellRenderer {
    private static final long serialVersionUID = 945937593475L;

    private static final Dimension CHECK_DIM;

    private final NodeRenderer nodeRenderer;
    private final TransferSelectorModel model;
    private final JCheckBox check = new JCheckBox();

    private static Rectangle checkBounds;
    /** The component returned by HtmlRenderer.Renderer.getTreeCellRendererComponent() */
    private Component stringDisplayer = new JLabel(" "); // NOI18N

    static {
        Dimension old = new JCheckBox().getPreferredSize();
        CHECK_DIM = new Dimension(old.width, old.height - 5);
    }

    public CheckRenderer(TransferSelectorModel model) {
        assert model != null;

        this.model = model;
        nodeRenderer = new NodeRenderer();

        setLayout(null);
        add(check);
        check.setOpaque(false);
        check.setPreferredSize(CHECK_DIM);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        stringDisplayer = nodeRenderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

        TreePath path = tree.getPathForRow(row);
        if (path != null && path.getPathCount() == 1) {
            // do not show checkbox for the root node
            return stringDisplayer;
        }

        if (stringDisplayer instanceof JComponent) {
            setToolTipText(((JComponent) stringDisplayer).getToolTipText());
        }

        // HtmlRenderer does not tolerate null colors - real ones are needed to
        // ensure fg/bg always diverge enough to be readable
        if (stringDisplayer.getBackground() == null) {
            stringDisplayer.setBackground(tree.getBackground());
        }
        if (stringDisplayer.getForeground() == null) {
            stringDisplayer.setForeground(tree.getForeground());
        }

        Node node;
        if (value instanceof Node) {
            node = (Node) value;
        } else {
            node = Visualizer.findNode(value);
        }
        check.setSelected(node == null || model.isNodeSelected(node)); // node.isSelected());
        boolean partiallySelected = node != null && model.isNodePartiallySelected(node);
        check.getModel().setPressed(partiallySelected);
        check.getModel().setArmed(partiallySelected);
        check.setEnabled(true); // !node.isDisabled());
        return this;
    }

    @Override
    public void paintComponent(Graphics g) {
        Dimension dCheck = check.getSize();
        Dimension dLabel = stringDisplayer.getPreferredSize();

        int yLabel = 0;
        if (dCheck.height >= dLabel.height) {
            yLabel = (dCheck.height - dLabel.height) / 2;
        }
        check.setBounds(0, 0, dCheck.width, dCheck.height);
        check.paint(g);
        int y = yLabel - 2;
        stringDisplayer.setBounds(dCheck.width, y, dLabel.width, getHeight() - 1);
        g.translate(dCheck.width, yLabel);
        stringDisplayer.paint(g);
        g.translate(-dCheck.width, -yLabel);
    }

    @Override
    public Dimension getPreferredSize() {
        stringDisplayer.setFont(getFont());
        Dimension dCheck = check.getPreferredSize();
        Dimension dLabel = stringDisplayer.getPreferredSize();

        return new Dimension(dCheck.width  + dLabel.width, (dCheck.height < dLabel.height ? dLabel.height : dCheck.height));
    }

    @Override
    public void doLayout() {
        Dimension dCheck = check.getPreferredSize();
        Dimension dLabel = stringDisplayer.getPreferredSize();
        int yCheck = 0;

        if (dCheck.height < dLabel.height) {
            yCheck = (dLabel.height - dCheck.height) / 2;
        }

        check.setLocation(0, yCheck);
        check.setBounds(0, yCheck, dCheck.width, dCheck.height);
        if (checkBounds == null) {
            checkBounds = check.getBounds();
        }
    }

    public static Rectangle getCheckBoxRectangle() {
        if (checkBounds == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        return (Rectangle) checkBounds.clone();
    }
}
