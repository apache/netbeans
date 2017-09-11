/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.tag;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.ui.UiUtils;
import org.netbeans.modules.docker.ui.Validations;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class TagPanel extends javax.swing.JPanel {

    private final JButton actionButton;

    private NotificationLineSupport messageLine;

    /**
     * Creates new form TagPanel
     */
    public TagPanel(DockerInstance instance, JButton actionButton) {
        initComponents();

        this.actionButton = actionButton;

        DefaultDocumentListener listener = new DefaultDocumentListener();
        ((JTextComponent) repositoryComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(listener);
        tagTextField.getDocument().addDocumentListener(listener);

        UiUtils.loadRepositories(instance, repositoryComboBox);
    }

    public void setMessageLine(NotificationLineSupport messageLine) {
        this.messageLine = messageLine;
        validateInput();
    }

    @NbBundle.Messages({
        "MSG_EmptyRepository=The repository must not be empty."
    })
    private void validateInput() {
        if (messageLine == null) {
            return;
        }

        messageLine.clearMessages();
        actionButton.setEnabled(true);
        if (getRepository() == null) {
            messageLine.setErrorMessage(Bundle.MSG_EmptyRepository());
            actionButton.setEnabled(false);
            return;
        }
        String repository = getRepository();
        if (repository != null) {
            String message = Validations.validateRepository(repository);
            if (message != null) {
                messageLine.setErrorMessage(message);
                actionButton.setEnabled(false);
                return;
            }
        }
        String tag = getTag();
        if (tag != null) {
            String message = Validations.validateTag(tag);
            if (message != null) {
                messageLine.setErrorMessage(message);
                actionButton.setEnabled(false);
                return;
            }
        }
    }

    private class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            validateInput();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateInput();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateInput();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        repositoryLabel = new javax.swing.JLabel();
        repositoryComboBox = new javax.swing.JComboBox<>();
        tagLabel = new javax.swing.JLabel();
        tagTextField = new javax.swing.JTextField();
        forceCheckBox = new javax.swing.JCheckBox();

        repositoryLabel.setLabelFor(repositoryComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(repositoryLabel, org.openide.util.NbBundle.getMessage(TagPanel.class, "TagPanel.repositoryLabel.text")); // NOI18N

        repositoryComboBox.setEditable(true);

        tagLabel.setLabelFor(tagTextField);
        org.openide.awt.Mnemonics.setLocalizedText(tagLabel, org.openide.util.NbBundle.getMessage(TagPanel.class, "TagPanel.tagLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(forceCheckBox, org.openide.util.NbBundle.getMessage(TagPanel.class, "TagPanel.forceCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(forceCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(repositoryLabel)
                            .addComponent(tagLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(repositoryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tagTextField))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(repositoryLabel)
                    .addComponent(repositoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tagLabel)
                    .addComponent(tagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(forceCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public String getRepository() {
        return UiUtils.getValue(repositoryComboBox);
    }

    public String getTag() {
        return UiUtils.getValue(tagTextField);
    }

    public boolean isForce() {
        return forceCheckBox.isSelected();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox forceCheckBox;
    private javax.swing.JComboBox<String> repositoryComboBox;
    private javax.swing.JLabel repositoryLabel;
    private javax.swing.JLabel tagLabel;
    private javax.swing.JTextField tagTextField;
    // End of variables declaration//GEN-END:variables
}
