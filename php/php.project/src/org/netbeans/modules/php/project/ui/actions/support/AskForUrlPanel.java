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

package org.netbeans.modules.php.project.ui.actions.support;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FocusTraversalPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class AskForUrlPanel extends JPanel {
    private static final long serialVersionUID = 9261149997804215L;

    private final PhpProject project;
    private final JTextField urlComboBoxEditor;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport notificationLineSupport;

    AskForUrlPanel(PhpProject project, URL defaultUrl) {
        assert project != null;
        assert defaultUrl != null;

        this.project = project;

        initComponents();

        for (String s : ProjectSettings.getDebugUrls(project)) {
            urlComboBox.addItem(s);
        }
        if (urlComboBox.getItemCount() == 0) {
            // add default url
            urlComboBox.addItem(defaultUrl.toExternalForm());
        }

        urlComboBoxEditor = (JTextField) urlComboBox.getEditor().getEditorComponent();
        urlComboBoxEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateFields();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateFields();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateFields();
            }
        });
    }

    public boolean open() {
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(AskForUrlPanel.class, "LBL_SpecifyUrl"),
                true,
                null);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            validateFields();
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        boolean ok = descriptor.getValue() == NotifyDescriptor.OK_OPTION;
        if (ok) {
            Set<String> urls = new LinkedHashSet<>();
            urls.add((String) urlComboBox.getSelectedItem());
            for (int i = 0; i < urlComboBox.getItemCount(); ++i) {
                urls.add(urlComboBox.getItemAt(i));
            }
            ProjectSettings.setDebugUrls(project, new ArrayList<>(urls));
        }
        return ok;
    }

    public URL getUrl() {
        try {
            return new URL((String) urlComboBox.getSelectedItem());
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("The URL must already be valid!", ex);
        }
    }

    void validateFields() {
        assert descriptor != null;
        assert notificationLineSupport != null;

        String url = urlComboBoxEditor.getText();
        if (!Utils.isValidUrl(url)) {
            notificationLineSupport.setErrorMessage(NbBundle.getMessage(AskForUrlPanel.class, "MSG_InvalidUrl"));
            descriptor.setValid(false);
            return;
        }

        notificationLineSupport.clearMessages();
        descriptor.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        urlLabel = new JLabel();
        urlComboBox = new JComboBox<String>();

        urlLabel.setLabelFor(urlComboBox);
        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.urlLabel.text")); // NOI18N

        urlComboBox.setEditable(true);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(urlLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(urlComboBox, 0, 341, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.urlComboBox.AccessibleContext.accessibleName")); // NOI18N
        urlComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.urlComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> urlComboBox;
    private JLabel urlLabel;
    // End of variables declaration//GEN-END:variables

}
