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
public class LabelBracketTableCellRenderer extends EnhancedTableCellRenderer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JLabel label1;
    private JLabel label2;
    private int digitsWidth = -1;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a default table cell renderer with LEADING horizontal alignment showing border when focused. */
    public LabelBracketTableCellRenderer() {
        this(JLabel.LEADING);
    }

    public LabelBracketTableCellRenderer(int horizontalAlignment) {
        this(horizontalAlignment, "(99.9%)"); //NOI18N
    }

    public LabelBracketTableCellRenderer(int horizontalAlignment, final String widestBracketText) {
        setHorizontalAlignment(horizontalAlignment);
        label1 = new JLabel("", horizontalAlignment); //NOI18N
        label2 = new JLabel("", horizontalAlignment) { //NOI18N
                public Dimension getPreferredSize() {
                    Dimension d = super.getPreferredSize();

                    if (digitsWidth == -1) {
                        digitsWidth = getFontMetrics(getFont()).stringWidth(widestBracketText);
                    }

                    if (d.width < digitsWidth) {
                        return new Dimension(digitsWidth, d.height);
                    } else {
                        return d;
                    }
                }
            };

        Font f = label2.getFont();
        label2.setFont(new Font(f.getName(), f.getStyle(), f.getSize() - 1));

        setLayout(new BorderLayout());

        if (horizontalAlignment == JLabel.LEADING) {
            add(label1, BorderLayout.WEST);
            add(label2, BorderLayout.CENTER);
        } else {
            add(label1, BorderLayout.CENTER);
            add(label2, BorderLayout.EAST);
        }

        setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                             int row, int column) {
        return new LabelBracketTableCellRenderer(label1.getHorizontalAlignment()).getTableCellRendererComponent(table, value,
                                                                                                                isSelected,
                                                                                                                hasFocus, row,
                                                                                                                column);
    }

    protected void setRowForeground(Color c) {
        super.setRowForeground(c);
        label1.setForeground(c);
        label2.setForeground(UIUtils.getDisabledForeground(c));
    }

    protected void setValue(JTable table, Object value, int row, int column) {
        if (table != null) {
            setFont(table.getFont());
        }

        if (value != null) {
            String str = value.toString();
            int bracketIdx = str.lastIndexOf('('); // NOI18N

            if (bracketIdx != -1) {
                label1.setText(str.substring(0, bracketIdx));
                label2.setText(str.substring(bracketIdx));
            } else {
                label1.setText(str);
                label2.setText(""); // NOI18N
            }
        } else {
            label1.setText(""); // NOI18N
            label2.setText(""); // NOI18N
        }
    }
}
