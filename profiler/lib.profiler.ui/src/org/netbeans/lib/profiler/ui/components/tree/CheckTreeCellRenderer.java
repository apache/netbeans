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

package org.netbeans.lib.profiler.ui.components.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 *
 * @author Jiri Sedlacek
 */
public class CheckTreeCellRenderer extends JPanel implements TreeCellRendererPersistent {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static Dimension checkBoxDimension = new JCheckBox().getPreferredSize();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JCheckBox checkBox = new JCheckBox();
    private ButtonModel checkBoxModel = checkBox.getModel();
    private Component treeRendererComponent;
    private DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
    private boolean persistentRenderer = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CheckTreeCellRenderer() {
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
        // --- Workaround for #205932 - not sure why, but works fine...
        Font f = UIManager.getFont("Label.font"); // NOI18N
        if (f != null) treeRenderer.setFont(f.deriveFont(f.getStyle()));
        // --- 
        add(checkBox, BorderLayout.WEST);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static Dimension getCheckBoxDimension() {
        return checkBoxDimension;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
                                                  int row, boolean hasFocus) {
        // Get CheckTreeNode from current Node
        CheckTreeNode treeNode = (value instanceof CheckTreeNode) ? (CheckTreeNode) value : null;

        // Update UI
        if (treeRendererComponent != null) {
            remove(treeRendererComponent);
        }

        if (treeNode != null) {
            checkBox.setVisible(!persistentRenderer);
            setupCellRendererIcon((DefaultTreeCellRenderer) treeRenderer, treeNode.getIcon());
        } else {
            checkBox.setVisible(false);
            setupCellRendererIcon((DefaultTreeCellRenderer) treeRenderer, null);
        }

        treeRendererComponent = treeRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        add(treeRendererComponent, BorderLayout.CENTER);

        // Return if no path or not a CheckTreeNode
        if (treeNode == null) {
            return this;
        }

        // If tree model supports checking (uses CheckTreeNodes), setup CheckBox
        if (treeNode.isFullyChecked()) {
            setupCheckBox(Boolean.TRUE);
        } else {
            setupCheckBox(treeNode.isPartiallyChecked() ? null : Boolean.FALSE);
        }

        return this;
    }

    public Component getTreeCellRendererComponentPersistent(JTree tree, Object value, boolean selected, boolean expanded,
                                                            boolean leaf, int row, boolean hasFocus) {
        CheckTreeCellRenderer ctcr = new CheckTreeCellRenderer();
        ctcr.persistentRenderer = true;

        return ctcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    private void setupCellRendererIcon(DefaultTreeCellRenderer renderer, Icon icon) {
        renderer.setLeafIcon(icon);
        renderer.setOpenIcon(icon);
        renderer.setClosedIcon(icon);
    }

    private void setupCheckBox(Boolean state) {
        if (state == Boolean.TRUE) {
            // Fully checked
            checkBoxModel.setArmed(false);
            checkBoxModel.setPressed(false);
            checkBoxModel.setSelected(true);
        } else if (state == Boolean.FALSE) {
            // Fully unchecked
            checkBoxModel.setArmed(false);
            checkBoxModel.setPressed(false);
            checkBoxModel.setSelected(false);
        } else {
            // Partially checked
            checkBoxModel.setArmed(true);
            checkBoxModel.setPressed(true);
            checkBoxModel.setSelected(true);
        }
    }
}
