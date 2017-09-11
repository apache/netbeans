/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
