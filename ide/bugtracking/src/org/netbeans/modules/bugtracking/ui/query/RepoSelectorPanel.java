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

package org.netbeans.modules.bugtracking.ui.query;

import javax.swing.GroupLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 * Part of the {@code QueryTopComponent}. Contains the combo-box for selection
 * of a bug-tracking repository, plus accessories (label, button &quot;New&quot).
 *
 * @author Marian Petras
 */
public class RepoSelectorPanel extends JPanel implements FocusListener {

    RepoSelectorPanel(JComponent repoSelector,
                      JComponent newRepoButton) {
        super(null);
        JLabel repoSelectorLabel = new JLabel();

        repoSelectorLabel.setLabelFor(repoSelector);
        repoSelectorLabel.setFocusCycleRoot(true);

        Mnemonics.setLocalizedText(
               repoSelectorLabel,
               NbBundle.getMessage(getClass(),
                                   "QueryTopComponent.repoLabel.text"));//NOI18N

        setOpaque(false);

        newRepoButton.addFocusListener(this);
        repoSelector.addFocusListener(this);

        GroupLayout layout;
        setLayout(layout = new GroupLayout(this));
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(repoSelectorLabel)
                        .addPreferredGap(RELATED)
                        .addComponent(repoSelector)
                        .addPreferredGap(RELATED)
                        .addComponent(newRepoButton));
        layout.setVerticalGroup(
                layout.createParallelGroup(BASELINE)
                        .addComponent(repoSelectorLabel)
                        .addComponent(repoSelector, DEFAULT_SIZE,
                                           DEFAULT_SIZE,
                                           PREFERRED_SIZE)
                        .addComponent(newRepoButton));
    }

    @Override
    public void focusGained(FocusEvent e) {
        final Component c = e.getComponent();
        if (c instanceof JComponent) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    RepoSelectorPanel.this.scrollRectToVisible(c.getBounds());
                }
            });
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        //do nothing
    }

}
