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
