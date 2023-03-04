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
package org.netbeans.modules.search.ui;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Panel for a checkbox and a button that is enbled if and only if the checkbox
 * is selected.
 */
public class CheckBoxWithButtonPanel extends JPanel
        implements ItemListener {

    private JCheckBox checkbox;
    private LinkButtonPanel buttonPanel;

    /**
     * Constructor.
     *
     *  * The text of the button must be already set.
     *
     * @param checkbox
     * @param button
     */
    public CheckBoxWithButtonPanel(JCheckBox checkbox, JButton button) {
        this.checkbox = checkbox;
        this.buttonPanel = new LinkButtonPanel(button);
        init();
    }

    /**
     * Init panel and helper elements.
     */
    private void init() {
        this.setLayout(new FlowLayout(
                FlowLayout.LEADING, 0, 0));
        this.add(checkbox);
        this.add(buttonPanel);

        this.setMaximumSize(
                this.getMinimumSize());
        checkbox.addItemListener(this);
        if (checkbox.isSelected()) {
            buttonPanel.enableButton();
        } else {
            buttonPanel.disableButton();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (checkbox.isSelected()) {
            buttonPanel.enableButton();
        } else {
            buttonPanel.disableButton();
        }
        this.setMinimumSize(this.getPreferredSize()); // #214745
    }
}
