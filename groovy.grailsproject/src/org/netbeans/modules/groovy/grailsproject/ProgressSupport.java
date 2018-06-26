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
