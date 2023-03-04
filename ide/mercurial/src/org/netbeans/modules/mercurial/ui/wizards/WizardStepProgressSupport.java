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

package org.netbeans.modules.mercurial.ui.wizards;

import javax.swing.LayoutStyle;
import javax.swing.GroupLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public abstract class WizardStepProgressSupport extends HgProgressSupport implements Runnable, Cancellable {   

    private JComponent progressComponent;
    private JLabel progressLabel;
    private JComponent progressBar;
    private JButton stopButton;
    private JComponent progressLine;
    
    public WizardStepProgressSupport() {
    }

    public abstract void setEditable(boolean bl);

    @Override
    public JComponent getProgressComponent() {
        if (progressComponent == null) {
            progressComponent = createProgressComponent();
        }
        return progressComponent;
    }

    private JComponent createProgressComponent() {
        progressLabel = new JLabel(getDisplayName());

        progressBar = super.getProgressComponent();

        stopButton = new JButton(NbBundle.getMessage(WizardStepProgressSupport.class, "BK2022")); // NOI18N
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);

        progressLine = new JPanel();
        progressLine.add(progressBar);
        progressLine.add(Box.createHorizontalStrut(
                                LayoutStyle.getInstance()
                                .getPreferredGap(progressBar,
                                                 stopButton,
                                                 RELATED,
                                                 SwingConstants.EAST,
                                                 progressLine)));
        progressLine.add(stopButton);

        progressLine.setLayout(new BoxLayout(progressLine, BoxLayout.X_AXIS));
        progressBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                .addComponent(progressLabel)
                .addComponent(progressLine));
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(progressLabel)
                .addPreferredGap(RELATED)
                .addComponent(progressLine));
        panel.setLayout(layout);

        layout.setHonorsVisibility(false);   //hiding should not affect prefsize

        progressLabel.setVisible(false);
        progressLine.setVisible(false);

        return panel;
    }

    public void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressLabel.setVisible(true);
                progressLine.setVisible(true);

                WizardStepProgressSupport.super.startProgress();
            }
        });                                                
    }

    protected void finnishProgress() {        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {                
                WizardStepProgressSupport.super.finnishProgress();

                progressLabel.setVisible(false);
                progressLine.setVisible(false);

                setEditable(true);
            }
        });                
    }

    public void setDisplayName(String displayName) {
        if(progressLabel != null) progressLabel.setText(displayName);
        super.setDisplayName(displayName);
    }
    
    public synchronized boolean cancel() {
        if(stopButton!=null) stopButton.setEnabled(false);
        setDisplayName(org.openide.util.NbBundle.getMessage(WizardStepProgressSupport.class, "MSG_Progress_Terminating"));
        return super.cancel();
    }
        
}
