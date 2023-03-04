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

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author jhavlin
 */
public class LinkButtonPanel extends JPanel {

    private JButton button;
    private JLabel leftParenthesis;
    private JLabel rightParenthesis;
    private String enabledText;
    private String disabledText;

    public LinkButtonPanel(JButton button) {
        this.button = button;
        initTexts();
        init();
    }

    private void init() {
        this.setLayout(new FlowLayout(
                FlowLayout.LEADING, 0, 0));
        setLinkLikeButton(button);
        leftParenthesis = new JLabel("(");                              //NOI18N
        rightParenthesis = new JLabel(")");                             //NOI18N
        add(leftParenthesis);
        add(button);
        add(rightParenthesis);
        MouseListener ml = createLabelMouseListener();
        leftParenthesis.addMouseListener(ml);
        rightParenthesis.addMouseListener(ml);
        button.setEnabled(false);
        this.setMaximumSize(
                this.getPreferredSize());
    }

    /**
     * Init values of enabled and disabled button texts.
     */
    private void initTexts() {
        enabledText = button.getText();
        if (enabledText.startsWith(UiUtils.HTML_LINK_PREFIX)
                && enabledText.endsWith(UiUtils.HTML_LINK_SUFFIX)) {
            disabledText = enabledText.substring(
                    UiUtils.HTML_LINK_PREFIX.length(),
                    enabledText.length() - UiUtils.HTML_LINK_SUFFIX.length());
        } else {
            disabledText = enabledText;
        }
    }

    /**
     * Create listener that delegates mouse clicks on parenthesis to the button.
     */
    private MouseListener createLabelMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (button.isEnabled()) {
                    for (ActionListener al : button.getActionListeners()) {
                        al.actionPerformed(null);
                    }
                }
            }
        };
    }

    /**
     * Set button border and background to look like a label with link.
     */
    private void setLinkLikeButton(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
    }

    /**
     * Enable button and parentheses around it.
     */
    public void enableButton() {
        button.setText(enabledText);
        button.setEnabled(true);
        leftParenthesis.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightParenthesis.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftParenthesis.setEnabled(true);
        rightParenthesis.setEnabled(true);
        this.setMinimumSize(this.getPreferredSize());
    }

    /**
     * Disable button and parentheses around it.
     */
    public void disableButton() {
        button.setText(disabledText);
        button.setEnabled(false);
        leftParenthesis.setCursor(Cursor.getDefaultCursor());
        rightParenthesis.setCursor(Cursor.getDefaultCursor());
        leftParenthesis.setEnabled(false);
        rightParenthesis.setEnabled(false);
        this.setMinimumSize(this.getPreferredSize());
    }

    public void setButtonEnabled(boolean enabled) {
        if (enabled) {
            enableButton();
        } else {
            disableButton();
        }
    }
}
