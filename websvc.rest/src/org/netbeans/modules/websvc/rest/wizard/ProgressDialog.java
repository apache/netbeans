/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
        assert SwingUtilities.isEventDispatchThread();
        createDialog(title);
    }

    public void open() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                while (dialog != null) {
                    dialog.setVisible(true);
                }
            }
        });
    }

    public void close() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    Dialog oldDialog = dialog;
                    dialog = null;
                    oldDialog.setVisible(false);
                    oldDialog.dispose();
                }
            }
        });
    }

    public ProgressHandle getProgressHandle() {
        return pHandle;
    }

    private void createDialog(String title) {
        pHandle = ProgressHandleFactory.createHandle(title);
        JPanel panel = new ProgressPanel(pHandle);

        DialogDescriptor descriptor = new DialogDescriptor(
                panel, title); // NOI18N

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
        int dialogX = (int) (windowWidth / 2.0) - (int) (dialogWidth / 2.0);
        int dialogY = (int) (windowHeight / 2.0) - (int) (dialogHeight / 2.0);

        dialog.setLocation(dialogX, dialogY);
    }

    private static class ProgressPanel extends JPanel {

        private JLabel messageLabel;
        private JComponent progressBar;

        public ProgressPanel(ProgressHandle pHandle) {
            messageLabel = ProgressHandleFactory.createDetailLabelComponent(pHandle);
            messageLabel.setText(NbBundle.getMessage(ProgressDialog.class,
                    "MSG_StartingProgress"));
            progressBar = ProgressHandleFactory.createProgressComponent(pHandle);

            initComponents();
        }

        private void initComponents() {
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE).addComponent(messageLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)).addContainerGap()));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(messageLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        }

        public Dimension getPreferredSize() {
            Dimension orig = super.getPreferredSize();
            return new Dimension(500, orig.height);
        }
    }
}
