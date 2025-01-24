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
package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author kuba
 */
public class CheckBoxTreeRenderer extends JPanel implements TreeCellRenderer {

    protected JCheckBox check;
    protected JLabel label;
    private static final JList LIST_FOR_COLORS = new JList();

    public CheckBoxTreeRenderer() {
        setLayout(new BorderLayout());
        setOpaque(true);
        this.check = new JCheckBox();
        this.label = new JLabel();
        add(check, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
        check.setOpaque(false);
        label.setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, isSelected,
                expanded, leaf, row, hasFocus);
        setEnabled(tree.isEnabled());
        if (value instanceof CheckNode) {
            CheckNode n = (CheckNode) value;
            check.setSelected(n.isSelected());
            label.setIcon(ImageUtilities.image2Icon(n.getIcon())); // XXX Ask description directly
        }
        if (isSelected) {
            label.setForeground(LIST_FOR_COLORS.getSelectionForeground());
            setOpaque(true);
            setBackground(LIST_FOR_COLORS.getSelectionBackground());
        } else {
            label.setForeground(tree.getForeground());
            setOpaque(false);
        }
        label.setText(stringValue);
        return this;
    }
}
