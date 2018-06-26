/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.jstesting;

import java.awt.EventQueue;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

public final class SelectProviderPanel extends JPanel {

    // @GuardedBy("EDT")
    private DialogDescriptor dialogDescriptor;
    // @GuardedBy("EDT")
    private NotificationLineSupport notificationLineSupport;


    private SelectProviderPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();
        init();
    }

    @CheckForNull
    public static JsTestingProvider open() {
        return Mutex.EVENT.readAccess(new Mutex.Action<JsTestingProvider>() {
            @Override
            public JsTestingProvider run() {
                return openInternal();
            }
        });
    }

    @NbBundle.Messages("SelectProviderPanel.title=Select Testing Provider")
    static JsTestingProvider openInternal() {
        assert EventQueue.isDispatchThread();
        final SelectProviderPanel panel = new SelectProviderPanel();
        panel.dialogDescriptor = new DialogDescriptor(
                panel,
                Bundle.SelectProviderPanel_title(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        panel.notificationLineSupport = panel.dialogDescriptor.createNotificationLineSupport();
        panel.validateSelection();
        if (DialogDisplayer.getDefault().notify(panel.dialogDescriptor) == DialogDescriptor.OK_OPTION) {
            return panel.getSelectedProvider();
        }
        return null;
    }

    private void init() {
        for (JsTestingProvider provider : JsTestingProviders.getDefault().getJsTestingProviders()) {
            providerComboBox.addItem(provider);
        }
        providerComboBox.setRenderer(new JsTestingProviderRenderer());
    }

    private JsTestingProvider getSelectedProvider() {
        assert EventQueue.isDispatchThread();
        return (JsTestingProvider) providerComboBox.getSelectedItem();
    }

    @NbBundle.Messages("SelectProviderPanel.noneSelected=No provider selected.")
    void validateSelection() {
        assert EventQueue.isDispatchThread();
        assert dialogDescriptor != null;
        assert notificationLineSupport != null;

        if (getSelectedProvider() == null) {
            notificationLineSupport.setErrorMessage(Bundle.SelectProviderPanel_noneSelected());
            dialogDescriptor.setValid(false);
            return;
        }
        notificationLineSupport.clearMessages();
        dialogDescriptor.setValid(true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providerLabel = new JLabel();
        providerComboBox = new JComboBox<JsTestingProvider>();

        providerLabel.setLabelFor(providerComboBox);
        Mnemonics.setLocalizedText(providerLabel, NbBundle.getMessage(SelectProviderPanel.class, "SelectProviderPanel.providerLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(providerLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(providerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(providerLabel)
                    .addComponent(providerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<JsTestingProvider> providerComboBox;
    private JLabel providerLabel;
    // End of variables declaration//GEN-END:variables

}
