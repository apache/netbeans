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
package org.netbeans.modules.websvc.saas.codegen.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Peter Liu
 */
public class ProgressDialog {
    private ProgressHandle pHandle;
    private Dialog dialog;
    
    public ProgressDialog(String title) {
        createDialog(title);
    }
    
    public void open() {
        while (dialog != null) {
            dialog.setVisible(true);
        }
    }
    
    public void close() {
        if (dialog != null) {
            Dialog oldDialog = dialog;
            dialog = null;
            oldDialog.setVisible(false);
            oldDialog.dispose();
        }
    }
    
    public ProgressHandle getProgressHandle() {
        return pHandle;
    }
    
    private void createDialog(String title) {
        pHandle = ProgressHandleFactory.createHandle(title);
        JPanel panel = new ProgressPanel(pHandle);
        
        DialogDescriptor descriptor = new DialogDescriptor(
                panel, title
        );
        
        final Object[] OPTIONS = new Object[0];
        descriptor.setOptions(OPTIONS);
        descriptor.setClosingOptions(OPTIONS);
        descriptor.setModal(true);
        descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        int windowWidth = mainWindow.getWidth();
        int windowHeight = mainWindow.getHeight();
        int dialogWidth = dialog.getWidth();
        int dialogHeight = dialog.getHeight();
        int dialogX = (int)(windowWidth/2.0) - (int)(dialogWidth/2.0);
        int dialogY = (int)(windowHeight/2.0) - (int)(dialogHeight/2.0);
        
        dialog.setLocation(dialogX, dialogY);
    }
    
    private class ProgressPanel extends JPanel {
        private final JLabel messageLabel;
        private final JComponent progressBar;
        
        public ProgressPanel(ProgressHandle pHandle) {
            messageLabel = ProgressHandleFactory.createDetailLabelComponent(pHandle);
            messageLabel.setText(NbBundle.getMessage(ProgressDialog.class,
                    "MSG_StartingProgress")); // NOI18N
            progressBar = ProgressHandleFactory.createProgressComponent(pHandle);
            
            initComponents();
        }
        
        private void initComponents() {
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .addComponent(messageLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE))
                    .addContainerGap())
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(messageLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
        }
        
        
        @Override
        public Dimension getPreferredSize() {
            Dimension orig = super.getPreferredSize();
            return new Dimension(500, orig.height);
        }
        
    }
    
}
