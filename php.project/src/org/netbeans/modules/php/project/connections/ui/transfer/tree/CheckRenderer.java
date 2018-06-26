/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.php.project.connections.ui.transfer.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
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
        Color c = UIManager.getColor("Tree.textBackground"); // NOI18N
        if (c == null) {
            // may be null on GTK L&F
            c = Color.WHITE;
        }
        check.setBackground(c);
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
