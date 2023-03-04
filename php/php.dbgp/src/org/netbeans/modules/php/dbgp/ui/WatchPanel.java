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
package org.netbeans.modules.php.dbgp.ui;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A GUI panel for customizing a Watch. Stolen from
 * org.netbeans.modules.debugger.ui.WatchPanel
 *
 * @author ads
 */
public class WatchPanel {
    private static final String A11_WATCH_NAME = "ACSD_CTL_Watch_Name"; // NOI18N
    private static final String CENTER = "Center"; // NOI18N
    private static final String WEST = "West"; // NOI18N
    private static final String WATCH_NAME = "CTL_Watch_Name"; // NOI18N
    private static final String WATCH_PANEL = "ACSD_WatchPanel"; // NOI18N
    private JPanel myPanel;
    private JTextField myTextField;
    private String myExpression;

    public WatchPanel(String expression) {
        myExpression = expression;
    }

    public JComponent getPanel() {
        if (myPanel != null) {
            return myPanel;
        }
        myPanel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);
        myPanel.getAccessibleContext().setAccessibleDescription(bundle.getString(WATCH_PANEL));
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString(WATCH_NAME));
        textLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        myPanel.setLayout(new BorderLayout());
        myPanel.setBorder(new EmptyBorder(11, 12, 1, 11));
        myPanel.add(WEST, textLabel);
        myTextField = new JTextField(25);
        myPanel.add(CENTER, myTextField);
        myTextField.getAccessibleContext().setAccessibleDescription(bundle.getString(A11_WATCH_NAME));
        myTextField.setBorder(new CompoundBorder(myTextField.getBorder(), new EmptyBorder(2, 0, 2, 0)));
        myTextField.setText(myExpression);
        myTextField.selectAll();
        textLabel.setLabelFor(myTextField);
        myTextField.requestFocus();
        return myPanel;
    }

    public String getExpression() {
        return myTextField.getText().trim();
    }

}
