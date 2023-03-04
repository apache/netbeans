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

package org.netbeans.modules.versioning.diff;

import javax.swing.LayoutStyle;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static javax.swing.JComponent.LEFT_ALIGNMENT;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

/**
 *
 * @author Marian Petras
 */
class ExpandableMessage extends JPanel implements ActionListener {

    private final JLabel lblTopMsg;
    private final JLabel lblBotMsg;
    private final Collection<String> messages;
    private final JButton toggleButton;
    private boolean showingMore = false;
    private JComponent extraInfo;

    ExpandableMessage(String topMsgKey,
                      Collection<String> messages,
                      String bottomMsgKey,
                      JButton toggleButton) {
        super(null);
        this.messages = messages;
        this.toggleButton = toggleButton;
        lblTopMsg = new JLabel(getMessage(topMsgKey));
        lblBotMsg = (bottomMsgKey != null)
                    ? new JLabel(getMessage(bottomMsgKey))
                    : null;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(lblTopMsg);
        lblTopMsg.setAlignmentX(LEFT_ALIGNMENT);

        if (lblBotMsg != null) {
            add(makeVerticalStrut(lblTopMsg, lblBotMsg));
            add(lblBotMsg);
            lblBotMsg.setAlignmentX(LEFT_ALIGNMENT);
        }

        Mnemonics.setLocalizedText(toggleButton,
                                   getMessage("LBL_ShowMoreInformation")); //NOI18N
        toggleButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (showingMore) {
            showLess();
        } else {
            showMore();
        }
    }

    private void showMore() {
        if (extraInfo == null) {
            extraInfo = makeMessagesComponent(messages);
            extraInfo.setAlignmentX(LEFT_ALIGNMENT);
        }
        if (lblBotMsg != null) {
            remove(1);//remove vertical strut between the top msg and bottom msg
        }
        add(makeVerticalStrut(lblTopMsg, extraInfo), 1);
        add(extraInfo, 2);
        if (lblBotMsg != null) {
            add(makeVerticalStrut(extraInfo, lblBotMsg), 3);
        }
        Mnemonics.setLocalizedText(
                toggleButton,
                getMessage("LBL_ShowLessInformation"));                 //NOI18N
        showingMore = true;

        SwingUtilities.getWindowAncestor(this).pack();
    }

    private void showLess() {
        if (lblBotMsg != null) {
            remove(3);//remove vertical strut between extra msg and bottom msg
        }
        remove(2);      //remove the extra msg
        remove(1);      //remove vertical strut between top msg and extra msg
        if (lblBotMsg != null) {
            add(makeVerticalStrut(lblTopMsg, lblBotMsg), 1);
        }
        Mnemonics.setLocalizedText(
                toggleButton,
                getMessage("LBL_ShowMoreInformation"));                 //NOI18N
        showingMore = false;

        SwingUtilities.getWindowAncestor(this).pack();
    }

    private static JComponent makeMessagesComponent(Collection<String> messages) {
        if (messages == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("empty");                //NOI18N
        }
        return new JScrollPane(new ScrollableMessagesList(messages));
    }

    private static Component makeVerticalStrut(JComponent compA,
                                               JComponent compB) {
        LayoutStyle layoutStyle = LayoutStyle.getInstance();
        return Box.createVerticalStrut(
                layoutStyle.getPreferredGap(compA,
                                            compB,
                                            UNRELATED,
                                            SOUTH,
                                            compA.getParent()));
    }

    private static String getMessage(String msgKey, Object... params) {
        return NbBundle.getMessage(FilesModifiedConfirmation.class,
                                   msgKey,
                                   params);
    }

}
