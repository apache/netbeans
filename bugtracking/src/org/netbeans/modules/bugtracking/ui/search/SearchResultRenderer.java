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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
