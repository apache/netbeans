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

import org.netbeans.lib.profiler.ui.components.HTMLLabel;
import java.awt.*;
import java.net.URL;
import javax.swing.*;


/** Enhanced Table cell rendered that paints text labels using provided text alignment
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
public class HTMLLabelTableCellRenderer extends EnhancedTableCellRenderer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected HTMLLabel label;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a default table cell renderer with LEADING horizontal alignment showing border when focused. */
    public HTMLLabelTableCellRenderer() {
        this(JLabel.LEADING);
    }

    public HTMLLabelTableCellRenderer(int horizontalAlignment) {
        this(horizontalAlignment, false);
    }

    public HTMLLabelTableCellRenderer(int horizontalAlignment, final boolean persistent) {
        setHorizontalAlignment(horizontalAlignment);
        label = new HTMLLabel() {
                protected void showURL(URL url) {
                    HTMLLabelTableCellRenderer.this.handleLink(url);
                }

                public void setCursor(Cursor cursor) {
                    if (persistent) {
                        super.setCursor(cursor);
                    } else {
                        HTMLLabelTableCellRenderer.this.handleCursor(cursor);
                    }
                }
            };

        setLayout(new BorderLayout());
        add(label,
            ((horizontalAlignment == JLabel.LEADING) || (horizontalAlignment == JLabel.LEFT)) ? BorderLayout.WEST
                                                                                              : BorderLayout.EAST);

        //    setBorder (BorderFactory.createEmptyBorder(1, 3, 1, 3));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                             int row, int column) {
        return new HTMLLabelTableCellRenderer(getHorizontalAlignment(), true).getTableCellRendererComponent(table, value,
                                                                                                            isSelected, hasFocus,
                                                                                                            row, column);
    }

    protected void setRowBackground(Color c) {
        super.setRowBackground(c);
        label.setBackground(c);
    }

    protected void setValue(JTable table, Object value, int row, int column) {
        if (table != null) {
            setFont(table.getFont());
        }

        label.setText((value == null) ? "" : value.toString()); //NOI18N
    }

    protected void handleCursor(Cursor cursor) {
        // override to react to setCursor
    }

    protected void handleLink(URL url) {
        // override to react to URL clicks
    }
}
