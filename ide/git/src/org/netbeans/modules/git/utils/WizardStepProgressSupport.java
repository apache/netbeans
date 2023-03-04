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

package org.netbeans.modules.git.utils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.output.OutputLogger;

/**
 *
 * @author Tomas Stupka
 */
public abstract class WizardStepProgressSupport extends GitProgressSupport {   

    private OutputLogger logger;
    private JPanel progressComponent;
    private JLabel progressLabel;
    private JPanel panel;
    private JButton stopButton;
    private boolean noOutputLogging;
    
    public WizardStepProgressSupport(JPanel panel, boolean noOutputLogging) {
        this(panel);
        this.noOutputLogging = noOutputLogging;
    }
    
    public WizardStepProgressSupport(JPanel panel) {
        this.panel = panel;        
    }

    public abstract void setEnabled(boolean bl);

    @Override
    public void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProgressHandle progress = getProgressHandle(); // NOI18N
                JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
                stopButton = new JButton(org.openide.util.NbBundle.getMessage(WizardStepProgressSupport.class, "BK2022")); // NOI18N
                stopButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancel();
                    }
                });
                
                panel.setLayout(new BorderLayout());
                progressComponent = new JPanel();
                progressComponent.setLayout(new BorderLayout(6, 0));
                progressLabel = new JLabel();
                progressLabel.setText(WizardStepProgressSupport.super.getDisplayName());
                progressComponent.add(progressLabel, BorderLayout.NORTH);
                progressComponent.add(bar, BorderLayout.CENTER);
                progressComponent.add(stopButton, BorderLayout.LINE_END);
                WizardStepProgressSupport.super.startProgress();
                
                panel.setVisible(true);
                panel.add(progressComponent);
                panel.revalidate();
            }
        });                                                
    }

    @Override
    protected void finishProgress() {        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {                
                WizardStepProgressSupport.super.finishProgress();
                panel.remove(progressComponent);
                panel.revalidate();
                panel.repaint();
                panel.setVisible(false);
                setEnabled(true);
            }
        });                
    }

    @Override
    public void setDisplayName(String displayName) {
        if(progressLabel != null) progressLabel.setText(displayName);
        super.setDisplayName(displayName);
    }
    
    @Override
    public synchronized boolean cancel() {
        if(stopButton!=null) stopButton.setEnabled(false);
        setDisplayName(org.openide.util.NbBundle.getMessage(WizardStepProgressSupport.class, "MSG_Progress_Terminating"));
        return super.cancel();
    }
    
    @Override
    public final OutputLogger getLogger () {
        if(noOutputLogging) {
            if (logger == null) {
                logger = OutputLogger.getLogger(null);
            }
            return logger;
        }
        return super.getLogger();
    }
        
}
