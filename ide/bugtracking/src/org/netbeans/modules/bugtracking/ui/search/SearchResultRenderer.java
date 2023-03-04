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
package org.netbeans.modules.bugtracking.ui.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.ui.search.PopupItem.IssueItem;
import org.netbeans.modules.bugtracking.commons.TextUtils;

/**
 * ListCellRenderer for SearchResults
 * @author Jan Becicka
 */
class SearchResultRenderer extends JLabel implements ListCellRenderer {

    private QuickSearchPopup popup;
    private QuickSearchComboBar combo;
    private JPanel rendererComponent;
    private JPanel dividerLine;
    private JLabel resultLabel;

    private JPanel itemPanel;

    public SearchResultRenderer (QuickSearchComboBar combo, QuickSearchPopup popup) {
        super();
        this.popup = popup;
        this.combo = combo;
        configRenderer();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (!(value instanceof PopupItem)) {
            BugtrackingManager.LOG.log(Level.WARNING, "wrong search list item {0}", value); // NOI18N
            return new JLabel();
        }

        PopupItem item = (PopupItem) value;
        String txt;
        if(item instanceof PopupItem.IssueItem) {
            txt = ((IssueItem) item).highlite(combo.getText(), item.getDisplayText());
        } else {
            txt = item.getDisplayText();
        }
        resultLabel.setText(txt);

        itemPanel.setPreferredSize(new Dimension(popup.getResultWidth(),
        itemPanel.getPreferredSize().height));

        if (isSelected) {
            resultLabel.setBackground(list.getSelectionBackground());
            resultLabel.setForeground(list.getSelectionForeground());
        } else {
            resultLabel.setBackground(QuickSearchComboBar.getResultBackground());
            resultLabel.setForeground(list.getForeground());
        }

        if(index == list.getModel().getSize() - 1 ) {
            rendererComponent.add(dividerLine, BorderLayout.NORTH);
        } else {
            rendererComponent.remove(dividerLine);
        }

        return rendererComponent;
    }

    private void configRenderer () {
        resultLabel = new JLabel();
        resultLabel.setOpaque(true);
        resultLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        itemPanel = new JPanel();
        itemPanel.setBackground(QuickSearchComboBar.getResultBackground());
        itemPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 3));
        itemPanel.setLayout(new BorderLayout());
        itemPanel.add(resultLabel, BorderLayout.CENTER);

        rendererComponent = new JPanel();
        rendererComponent.setLayout(new BorderLayout());
        rendererComponent.add(itemPanel, BorderLayout.CENTER);
        
        dividerLine = new JPanel();
        dividerLine.setBackground(QuickSearchComboBar.getPopupBorderColor());
        dividerLine.setPreferredSize(new Dimension(dividerLine.getPreferredSize().width, 1));
    }
}
