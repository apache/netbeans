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
package org.netbeans.modules.notifications.center;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.notifications.Utils;
import org.openide.util.ImageUtilities;

/**
 * @author Jan Becicka
 * @author jpeska
 */
public class SearchField extends JPanel {

    private JLabel leftIcon;
    private JPanel panel;
    private SearchTextField txtSearch;
    private JSeparator separator;

    public SearchField() {
        super();
        initComponents();
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panel = new JPanel();
        leftIcon = new JLabel();
        txtSearch = new SearchTextField();
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (e.getSource() == txtSearch) {
                    // make sure nothing is selected
                    int n = txtSearch.getText().length();
                    txtSearch.select(n, n);
                }
            }
        });
        separator = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        panel.setBackground(Utils.getTextBackground());
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(Utils.getComboBorderColor()));
        panel.setLayout(new java.awt.GridBagLayout());

        leftIcon.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/notifications/resources/find.png", true));//NOI18N
        leftIcon.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 1);
        panel.add(leftIcon, gridBagConstraints);

        txtSearch.setBorder(null);
        txtSearch.setMinimumSize(new java.awt.Dimension(100, 18));
        txtSearch.setPreferredSize(new java.awt.Dimension(150, 18));

        separator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        panel.add(separator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        panel.add(txtSearch, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panel, gridBagConstraints);
    }

    @Override
    public void requestFocus() {
        txtSearch.requestFocus(); //To change body of generated methods, choose Tools | Templates.
    }

    public String getText() {
        return txtSearch.getText().trim();
    }

    public void setText(String text) {
        txtSearch.setText(text);
    }

    public boolean isEmpty() {
        return txtSearch.getText().trim().isEmpty();
    }

    public void clear() {
        txtSearch.setText("");
    }

    void setCaretPosition(int position) {
        txtSearch.setCaretPosition(position);
    }

    void processSearchKeyEvent(KeyEvent e) {
        txtSearch.processKeyEvent(e);
    }

    public void addDocumentListener(DocumentListener listener) {
        txtSearch.getDocument().addDocumentListener(listener);
    }

    public void removeDocumentListener(DocumentListener listener) {
        txtSearch.getDocument().removeDocumentListener(listener);
    }

    public void addSearchKeyListener(KeyListener listener) {
        txtSearch.addKeyListener(listener);
    }

    public void removeSearchKeyListener(KeyListener listener) {
        txtSearch.removeKeyListener(listener);
    }

    private static class SearchTextField extends JTextField {

        @Override
        protected void processKeyEvent(KeyEvent e) {
            super.processKeyEvent(e);
        }
    }
}
