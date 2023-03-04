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

package org.netbeans.modules.groovy.grailsproject;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class ProgressSupport {

    public static ProgressDialogDescriptor createProgressDialog(String title, ProgressHandle handle,
            ActionListener cancelationListener) {
        
        assert SwingUtilities.isEventDispatchThread();

        Component progress = ProgressHandleFactory.createProgressComponent(handle);
        JLabel label = ProgressHandleFactory.createDetailLabelComponent(handle);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new java.awt.Dimension(450, 50));

        GridBagConstraints constraintsLabel = new GridBagConstraints();
        constraintsLabel.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        constraintsLabel.fill = java.awt.GridBagConstraints.HORIZONTAL;
        constraintsLabel.anchor = java.awt.GridBagConstraints.WEST;
        constraintsLabel.weightx = 1.0;
        constraintsLabel.insets = new java.awt.Insets(8, 8, 0, 8);
        constraintsLabel.gridx = 0;
        constraintsLabel.gridy = 0;
        panel.add(label, constraintsLabel);

        GridBagConstraints constraintsProgress = new java.awt.GridBagConstraints();
        constraintsProgress.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        constraintsProgress.gridheight = java.awt.GridBagConstraints.REMAINDER;
        constraintsProgress.fill = java.awt.GridBagConstraints.HORIZONTAL;
        constraintsProgress.anchor = java.awt.GridBagConstraints.NORTHWEST;
        constraintsProgress.weightx = 1.0;
        constraintsProgress.insets = new java.awt.Insets(6, 8, 8, 8);
        constraintsProgress.gridx = 0;
        constraintsProgress.gridy = 1;      
        panel.add(progress, constraintsProgress);

        JButton cancel = new JButton(NbBundle.getMessage(ProgressSupport.class, "LBL_Cancel"));
        ProgressDialogDescriptor descriptor = new ProgressDialogDescriptor(panel, title, true,
                new JButton[] {cancel}, cancel, DialogDescriptor.DEFAULT_ALIGN, null, cancelationListener);

        return descriptor;
    }

    public static class ProgressDialogDescriptor extends DialogDescriptor {

        private final JButton cancelButton;

        private ProgressDialogDescriptor(Object innerPane, String title, boolean modal,
                JButton[] options, JButton initialValue, int optionsAlign, HelpCtx helpCtx, ActionListener bl) {

            super(innerPane, title, modal, options, initialValue, optionsAlign, helpCtx, bl);

            assert options.length == 1;
            assert options[0] == initialValue;

            cancelButton = initialValue;
        }

        public void addCancelListener(ActionListener listener) {
            cancelButton.addActionListener(listener);
        }

        public void removeCancelListener(ActionListener listener) {
            cancelButton.removeActionListener(listener);
        }
    }
}
