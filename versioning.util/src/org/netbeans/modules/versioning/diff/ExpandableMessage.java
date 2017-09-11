/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
