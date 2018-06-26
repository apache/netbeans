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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.ui.transfer;

import java.awt.BorderLayout;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public final class TransferFilesChooserVisual extends JPanel {
    private static final long serialVersionUID = 8975634564231321L;

    @StaticResource
    private static final String INFO_ICON = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N

    private final TransferFilesChooserPanel filesChooserPanel;
    private final TransferFilesChooser.TransferType transferType;

    public TransferFilesChooserVisual(TransferFilesChooserPanel filesChooserPanel, TransferFilesChooser.TransferType transferType) {
        assert filesChooserPanel != null;
        assert transferType != null;

        this.filesChooserPanel = filesChooserPanel;
        this.transferType = transferType;

        initComponents();

        innerPanel.add(filesChooserPanel.getEmbeddablePanel(), BorderLayout.CENTER);

        filesChooserPanel.addChangeListener(new TransferFilesChooserPanel.TransferFilesChangeListener() {
            @Override
            public void selectedFilesChanged() {
                updateSelectedFilesInfo();
            }

            @Override
            public void filterChanged() {
            }
        });
        updateSelectedFilesInfo();
    }

    public TransferFilesChooserPanel getEmbeddablePanel() {
        return (TransferFilesChooserPanel) outerPanel;
    }

    void updateSelectedFilesInfo() {
        String msg;
        int size = filesChooserPanel.getSelectedFiles().size();
        if (size == 0) {
            msg = NbBundle.getMessage(TransferFilesChooserVisual.class, "LBL_ZeroFilesSelected"); // NOI18N
        } else {
            // lazy download/upload
            msg = NbBundle.getMessage(TransferFilesChooserVisual.class, "LBL_FilesOrMoreSelected", size); // NOI18N
        }
        selectedFilesInfoLabel.setText(msg);
        updateWarning();
    }

    void updateWarning() {
        int size = filesChooserPanel.getSelectedFiles().size();
        if (size == 0) {
            warningLabel.setIcon(null);
            warningLabel.setText(" "); // NOI18N
        } else {
            String msgKey = null;
            switch (transferType) {
                case DOWNLOAD:
                    msgKey = "LBL_WarnDownload";
                    break;
                case UPLOAD:
                    msgKey = "LBL_WarnUpload";
                    break;
                default:
                    throw new IllegalStateException("Unknown transfer type: " + transferType);
            }
            warningLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(INFO_ICON, false)));
            warningLabel.setText(NbBundle.getMessage(TransferFilesChooserVisual.class, msgKey));
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectFilesLabel = new JLabel();
        outerPanel = new EmbeddablePanel(filesChooserPanel);
        innerPanel = new JPanel();
        selectedFilesInfoLabel = new JLabel();
        warningLabel = new JLabel();

        selectFilesLabel.setLabelFor(outerPanel);



        Mnemonics.setLocalizedText(selectFilesLabel, NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.selectFilesLabel.text"));
        innerPanel.setLayout(new BorderLayout());
        Mnemonics.setLocalizedText(selectedFilesInfoLabel, "DUMMY");
        Mnemonics.setLocalizedText(warningLabel, "DUMMY");
        GroupLayout outerPanelLayout = new GroupLayout(outerPanel);
        outerPanel.setLayout(outerPanelLayout);

        outerPanelLayout.setHorizontalGroup(
            outerPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(outerPanelLayout.createSequentialGroup()
                .addComponent(selectedFilesInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED, 402, Short.MAX_VALUE)
                .addComponent(warningLabel))
            .addComponent(innerPanel, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        outerPanelLayout.setVerticalGroup(
            outerPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, outerPanelLayout.createSequentialGroup()
                .addComponent(innerPanel, GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(outerPanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(selectedFilesInfoLabel)
                    .addComponent(warningLabel)))
        );

        innerPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.innerPanel.AccessibleContext.accessibleName")); // NOI18N
        innerPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.innerPanel.AccessibleContext.accessibleDescription")); // NOI18N
        selectedFilesInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.selectedFilesInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        selectedFilesInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.selectedFilesInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        warningLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.warningLabel.AccessibleContext.accessibleName")); // NOI18N
        warningLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.warningLabel.AccessibleContext.accessibleDescription")); // NOI18N
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(outerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(selectFilesLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectFilesLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(outerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        selectFilesLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.selectFilesLabel.AccessibleContext.accessibleName")); // NOI18N
        selectFilesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.selectFilesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        outerPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.outerPanel.AccessibleContext.accessibleName")); // NOI18N
        outerPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.outerPanel.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferFilesChooserVisual.class, "TransferFilesChooserVisual.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel innerPanel;
    private JPanel outerPanel;
    private JLabel selectFilesLabel;
    private JLabel selectedFilesInfoLabel;
    private JLabel warningLabel;
    // End of variables declaration//GEN-END:variables

    private static final class EmbeddablePanel extends TransferFilesChooserPanel {
        private static final long serialVersionUID = 646546111000L;

        private final TransferFilesChooserPanel delegate;

        public EmbeddablePanel(TransferFilesChooserPanel delegate) {
            assert delegate != null;

            this.delegate = delegate;
        }

        @Override
        public void addChangeListener(TransferFilesChangeListener listener) {
            delegate.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(TransferFilesChangeListener listener) {
            delegate.removeChangeListener(listener);
        }

        @Override
        public Set<TransferFile> getSelectedFiles() {
            return delegate.getSelectedFiles();
        }

        @Override
        public TransferFilesChooserPanel getEmbeddablePanel() {
            throw new IllegalStateException();
        }

        @Override
        public boolean hasAnyTransferableFiles() {
            return delegate.hasAnyTransferableFiles();
        }
    }
}
