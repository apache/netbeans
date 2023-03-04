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

package org.netbeans.lib.profiler.ui.components.table;

import org.netbeans.lib.profiler.ui.UIUtils;
import java.awt.*;
import javax.swing.*;


/** Enhanced Table cell rendered that paints text labels using provided text alignment
 *
 * @author Ian Formanek
 */
public class MethodNameTableCellRenderer extends EnhancedTableCellRenderer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JLabel label1;
    private JLabel label2;
    private JLabel label3;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a default table cell renderer with LEADING horizontal alignment showing border when focused. */
    public MethodNameTableCellRenderer() {
        label1 = new JLabel(""); //NOI18N
        label2 = new JLabel(""); //NOI18N
        label3 = new JLabel(""); //NOI18N

        label2.setFont(label1.getFont().deriveFont(Font.BOLD));

        setLayout(new BorderLayout());

        JPanel in = new JPanel();
        in.setOpaque(false);
        in.setLayout(new BorderLayout());
        add(label1, BorderLayout.WEST);
        add(in, BorderLayout.CENTER);
        in.add(label2, BorderLayout.WEST);
        in.add(label3, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                             int row, int column) {
        return new MethodNameTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    protected void setRowForeground(Color c) {
        super.setRowForeground(c);
        label1.setForeground(c);
        label2.setForeground(c);
        label3.setForeground(UIUtils.getDisabledForeground(c));
    }
    
    protected void setValue(JTable table, Object value, int row, int column) {
        if (table != null) {
            setFont(table.getFont());
        }

        if (value != null) {
            String str = value.toString();
            int bracketIndex = str.indexOf('('); //NOI18N
            String text3 = ""; //NOI18N

            if (bracketIndex != -1) {
                text3 = " " + str.substring(bracketIndex); //NOI18N
                str = str.substring(0, bracketIndex);
            }

            int dotIndex = str.lastIndexOf('.'); //NOI18N
            label1.setText(str.substring(0, dotIndex + 1));
            label2.setText(str.substring(dotIndex + 1));
            label3.setText(text3);
        } else {
            label1.setText(""); //NOI18N
            label2.setText(""); //NOI18N
            label3.setText(""); //NOI18N
        }
    }
}
