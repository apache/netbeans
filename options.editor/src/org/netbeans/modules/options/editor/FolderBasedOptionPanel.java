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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.options.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.spi.options.OptionsPanelController;

/**
 *
 * @author Dusan Balek
 */
@OptionsPanelController.KeywordsRegistration({
    @OptionsPanelController.Keywords(keywords = {"#KW_Hints"}, location = OptionsDisplayer.EDITOR, tabTitle="#CTL_Hints_DisplayName"),
    @OptionsPanelController.Keywords(keywords = {"#KW_Mark"}, location = OptionsDisplayer.EDITOR, tabTitle="#CTL_MarkOccurences_DisplayName")
})
public final class FolderBasedOptionPanel extends JPanel implements ActionListener {
    
    private final FolderBasedController controller;
    private String lastSelectedMimeType = null;
    
    /** Creates new form FolderBasedOptionPanel */
    FolderBasedOptionPanel(FolderBasedController controller, Document filterDocument, boolean allowFiltering) {
        this.controller = controller;

        initComponents();

        filter.setDocument(filterDocument);

        if (!allowFiltering) {
            filter.setVisible(false);
            filterLabel.setVisible(false);
        }
        
        ListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String)
                    value = EditorSettings.getDefault().getLanguageName((String)value);
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        };
        languageCombo.setRenderer(renderer);
        languageCombo.addActionListener(this);

        update();
    }

    void update () {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String mimeType : controller.getMimeTypes()) {
            model.addElement(mimeType);
        }
        languageCombo.setModel(model);
        
        lastSelectedMimeType = controller.getSavedSelectedLanguage();        
        if (lastSelectedMimeType != null && model.getSize() > 0) {
            languageCombo.setSelectedItem(lastSelectedMimeType);
            return;
        }

        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        String preSelectMimeType = pane != null ? (String)pane.getDocument().getProperty("mimeType") : ""; // NOI18N
        languageCombo.setSelectedItem(preSelectMimeType);
        if (!preSelectMimeType.equals (languageCombo.getSelectedItem()) && model.getSize() > 0) {
            languageCombo.setSelectedIndex(0);
        }
    }
    
    String getSelectedLanguage() {
        return (String)languageCombo.getSelectedItem();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languageLabel = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();
        optionsPanel = new javax.swing.JPanel();
        filter = new javax.swing.JTextField();
        filterLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        languageLabel.setLabelFor(languageCombo);
        org.openide.awt.Mnemonics.setLocalizedText(languageLabel, org.openide.util.NbBundle.getMessage(FolderBasedOptionPanel.class, "LBL_Language")); // NOI18N

        languageCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new java.awt.BorderLayout());

        filter.setColumns(10);
        filter.setText(org.openide.util.NbBundle.getMessage(FolderBasedOptionPanel.class, "FolderBasedOptionPanel.filter.text")); // NOI18N

        filterLabel.setLabelFor(filter);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(FolderBasedOptionPanel.class, "FolderBasedOptionPanel.filterLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(languageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(filterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filter, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(optionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(languageLabel)
                    .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filter;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JPanel optionsPanel;
    // End of variables declaration//GEN-END:variables
 
    // Change in the combos
    public void actionPerformed(ActionEvent e) {
        optionsPanel.setVisible(false);
        optionsPanel.removeAll();
        String mimeType = (String)languageCombo.getSelectedItem();
        if (mimeType != null) {
            OptionsPanelController opc = controller.getController(mimeType);
            if (opc != null) {
                JComponent component = opc.getComponent(controller.getLookup());
                optionsPanel.add(component, BorderLayout.CENTER);
                optionsPanel.setVisible(true);
            }
        }

        searchEnableDisable();
        if (isShowing()) { //remember the last selected option only when panel is visible
            lastSelectedMimeType = mimeType;
        }
    }

    void setCurrentMimeType(String key) {
        languageCombo.setSelectedItem(key);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        filter.setText("");
        lastSelectedMimeType = null;
    }

    void searchEnableDisable() {
        String mimeType = (String)languageCombo.getSelectedItem();
        
        filter.setEnabled(mimeType != null ? controller.supportsFilter(mimeType) : false);
    }
}
