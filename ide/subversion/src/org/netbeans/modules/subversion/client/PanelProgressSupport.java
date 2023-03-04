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

package org.netbeans.modules.subversion.client;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 *
 * @author Tomas Stupka
 */
public abstract class PanelProgressSupport extends SvnProgressSupport implements Runnable {

    private JPanel progressComponent;
    private JLabel progressLabel;
    private JPanel panel;

    /**
     *
     * @param panel if null, progress will be displayed in the progress bar
     */
    public PanelProgressSupport(JPanel panel) {
        this.panel = panel;        
    }

    @Override
    public void startProgress() {
        if (panel == null) {
            super.startProgress();
        } else
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProgressHandle progress = getProgressHandle(); // NOI18N
                JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
                progressComponent = new JPanel();
                progressComponent.setLayout(new BorderLayout(6, 0));
                progressLabel = new JLabel();
                progressLabel.setText(getDisplayName());
                progressComponent.add(progressLabel, BorderLayout.NORTH);
                progressComponent.add(bar, BorderLayout.CENTER);
                PanelProgressSupport.super.startProgress();
                panel.setVisible(true);
                panel.add(progressComponent);
                panel.revalidate();
            }
        });
    }

    @Override
    protected void finnishProgress() {
        if (panel == null) {
            super.finnishProgress();
        } else
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PanelProgressSupport.super.finnishProgress();
                panel.remove(progressComponent);
                panel.revalidate();
                panel.repaint();
                panel.setVisible(false);
            }
        });                
    }

    @Override
    public void setDisplayName(String displayName) {
        if (progressLabel != null) progressLabel.setText(displayName);
        super.setDisplayName(displayName);
    }
}
