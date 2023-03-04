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

package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.*;
import javax.swing.*;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.table.EnhancedTableCellRenderer;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode;


/**
 *
 * @author Jiri Sedlacek
 */
final class FieldTableCellRenderer extends EnhancedTableCellRenderer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final JLabel label1;
    private final JLabel label2;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public FieldTableCellRenderer() {
        setHorizontalAlignment(JLabel.TRAILING);
        
        label1 = new JLabel(""); //NOI18N
        label2 = new JLabel(""); //NOI18N
        
        setLayout(null);
        add(label1);
        add(label2);

        setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
    }
    
    public Dimension getPreferredSize() {
        Dimension d1 = label1.getPreferredSize();
        Dimension d2 = label2.getPreferredSize();
        d1.width += d2.width;
        d1.height = Math.max(d1.height, d2.height);
        Insets i = getInsets();
        d1.width += i.left + i.right;
        d1.height += i.top + i.bottom;
        return d1;
    }
    
    public Dimension getMaximumSize() {
	return getPreferredSize();
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }
    
    public void doLayout() {
        Insets i = getInsets();
        int w = getWidth() - i.left - i.right;
        int h = getHeight() - i.top - i.bottom;
        Dimension d1 = label1.getPreferredSize();
        Dimension d2 = label2.getPreferredSize();
        if (d1.width >= w) { // d1 partially displayed, d2 not displayed
            label1.setBounds(i.left, i.top, w, h);
            label2.setBounds(0, 0, 0, 0);
        } else if (d1.width + d2.width <= w) { // d1 and d2 fully displayed
            label1.setBounds(getWidth() - i.right - d1.width - d2.width, i.top, d1.width, h);
            label2.setBounds(getWidth() - i.right - d2.width, i.top, d2.width, h);
        } else { // d1 fully displayed, d2 partially displayed
            label1.setBounds(i.left, i.top, d1.width, h);
            label2.setBounds(i.left + d1.width, i.top, w - d1.width, h);
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                             int row, int column) {
        return new FieldTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    protected void setRowForeground(Color c) {
        super.setRowForeground(c);
        label1.setForeground(c);
        label2.setForeground(UIUtils.getDisabledForeground(c));
    }

    protected void setValue(JTable table, Object value, int row, int column) {
        if (value instanceof HeapWalkerNode) {
            HeapWalkerNode node = (HeapWalkerNode)value;
            label1.setText(node.getValue());
            String details = node.getDetails();
            label2.setText(details == null || details.isEmpty() ? "" : "  " + details); //NOI18N
        } else {
            label1.setText(""); //NOI18N
            label2.setText(""); //NOI18N
        }
    }
}
