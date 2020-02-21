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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.awt.Color;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;

/**
 * A panel which puts up some informative messages and allows the user to
 * turn off all such further messages.
 */
public class InfoPanel extends JPanel {
    private JTextArea textArea;
    private JCheckBox doNotShowCheckBox;
    private String msg1 = null;

    public InfoPanel(String msg) {
	initComponents(msg);
    }

    public InfoPanel(String msg, String msg1) {
	this.msg1 = msg1;
	initComponents(msg);
    }

    private void initComponents(String msg) {
	setLayout(new GridBagLayout());
	GridBagConstraints gbc;

        // See JLF-II p66
        // But we deviate because we're embedded in a JOptionPane.
        final int dialogMargin = 0;     // 11;
        final int labelSpace = 11;
        final int titleSpace = 12;      // p74
        final int bottomMargin = 12;

	final int rows = 4;
	final int cols = 60;

        textArea = new JTextArea(msg, // NOI18N
				 rows, cols);
	textArea.setEditable(false);
	textArea.setLineWrap(true);
	textArea.setWrapStyleWord(true);

	Color bgColor;
	bgColor = (Color) UIManager.getDefaults().
	    get("Label.background");    // NOI18N
	textArea.setBackground(bgColor);
	gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 1.0;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.insets = new Insets(dialogMargin, dialogMargin, 12, dialogMargin);
	add(textArea, gbc);

	if (msg1 == null)
	    msg1 = Catalog.get("LBL_doNotShowAgain"); // NOI18N
	doNotShowCheckBox = new JCheckBox(msg1);	// NOI18N
	Catalog.setAccessibleDescription(doNotShowCheckBox,
					 "ACSD_doNotShowAgain");// NOI18N
	doNotShowCheckBox.
	    setMnemonic(Catalog.getMnemonic("MNEM_doNotShowAgain"));// NOI18N

	gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbc.weightx = 1.0;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.insets = new Insets(dialogMargin, dialogMargin, bottomMargin, dialogMargin);
	add(doNotShowCheckBox, gbc);
    }

    public boolean dontShowAgain() {
	return doNotShowCheckBox.isSelected();
    }
}
