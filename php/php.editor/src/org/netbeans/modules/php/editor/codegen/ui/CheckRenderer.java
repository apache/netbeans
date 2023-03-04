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

package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Andrei Badea
 */
public class CheckRenderer extends JPanel implements ListCellRenderer {

    private static final JList LIST_FOR_COLORS = new JList();

    private final JCheckBox checkBox;
    private final JLabel label;

    public CheckRenderer() {
        setLayout(new BorderLayout());
        setOpaque(true);

        this.checkBox = new JCheckBox();
        this.label = new JLabel();

        add(checkBox, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);

        checkBox.setOpaque(false);
        label.setOpaque(false);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String text;
        boolean checked = false;
        if (value instanceof Selectable) {
            Selectable selectable = ((Selectable) value);
            checked = selectable.isSelected();
            text = selectable.getDisplayName();
        } else {
            text = value.toString();
        }
        checkBox.setSelected(checked);
        label.setText(text);
        if (isSelected) {
            label.setForeground(LIST_FOR_COLORS.getSelectionForeground());
            setOpaque(true);
            setBackground(LIST_FOR_COLORS.getSelectionBackground());
        } else {
            label.setForeground(list.getForeground());
            setOpaque(false);
        }
        return this;
    }
}
