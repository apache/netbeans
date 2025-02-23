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

package org.netbeans.modules.bugtracking.issuetable;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.openide.util.ImageUtilities;

class QueryTableHeaderRenderer extends DefaultTableCellRenderer {

    private final JLabel seenCell = new JLabel();

    private static final Icon seenHeaderIcon = ImageUtilities.loadIcon("org/netbeans/modules/bugtracking/commons/resources/seen-header.png"); // NOI18N
    private final TableCellRenderer delegate;
    private final IssueTable issueTable;
    private boolean isSaved;

     public QueryTableHeaderRenderer(TableCellRenderer delegate, IssueTable issueTable) {
        super();
        this.issueTable = issueTable;
        this.delegate = delegate;
        seenCell.setIcon(seenHeaderIcon);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSaved && column == issueTable.getSeenColumnIdx()) {
            Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            seenCell.setFont(c.getFont());
            seenCell.setForeground(c.getForeground());
            seenCell.setBorder(((JComponent) c).getBorder());
            return seenCell;
        } else {
            return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    void setSaved(boolean saved) {
        isSaved = saved;
    }

}
