/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.options.editor.onsave;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.WeakListeners;

/**
 * Panel for "On Save" tab in options.
 *
 * @author Miloslav Metelka
 */
@OptionsPanelController.Keywords(keywords = {"#KW_OnSave"}, location = OptionsDisplayer.EDITOR, tabTitle="#CTL_OnSave_DisplayName")
public class OnSaveTabPanel extends JPanel implements PropertyChangeListener {
    
    private OnSaveCommonPanel commonPanel;

    private OnSaveTabSelector selector;

    private PropertyChangeListener weakListener;

    public OnSaveTabPanel() {
        initComponents();
        // Languages combobox renderer
        cboLanguage.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if ((value instanceof String) && selector != null) {
                    value = selector.getLanguageName((String) value);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        
        commonPanel = new OnSaveCommonPanel();
        commonPanelContainer.setLayout(new BorderLayout());
        commonPanelContainer.add(commonPanel, BorderLayout.WEST);
        
        customPanelContainer.setLayout(new BorderLayout());
    }

    private String storedMimeType = null;

    public void setSelector(OnSaveTabSelector selector) {
        if (selector == null) {
            storedMimeType = (String)cboLanguage.getSelectedItem();
        }

        if (this.selector != null) {
            this.selector.removePropertyChangeListener(weakListener);
        }

        this.selector = selector;

        if (this.selector != null) {
            this.weakListener = WeakListeners.propertyChange(this, this.selector);
            this.selector.addPropertyChangeListener(weakListener);
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            String preSelectMimeType = null;
            for (String mimeType : this.selector.getMimeTypes()) {
                model.addElement(mimeType);
                if (mimeType.equals(storedMimeType)) {
                    preSelectMimeType = mimeType;
                }
            }
            cboLanguage.setModel(model);

            // Pre-select a language
            if (preSelectMimeType == null) {
                JTextComponent pane = EditorRegistry.lastFocusedComponent();
                preSelectMimeType = pane != null ? (String)pane.getDocument().getProperty("mimeType") : ""; // NOI18N
            }
            cboLanguage.setSelectedItem(preSelectMimeType);
            if (!preSelectMimeType.equals(cboLanguage.getSelectedItem())) {
                cboLanguage.setSelectedIndex(0);
            }
        } else {
            cboLanguage.setModel(new DefaultComboBoxModel());
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

        lLanguage = new javax.swing.JLabel();
        cboLanguage = new javax.swing.JComboBox();
        commonPanelContainer = new javax.swing.JPanel();
        customPanelContainer = new javax.swing.JPanel();

        lLanguage.setLabelFor(cboLanguage);
        org.openide.awt.Mnemonics.setLocalizedText(lLanguage, org.openide.util.NbBundle.getMessage(OnSaveTabPanel.class, "OnSaveTabPanel.lLanguage.text")); // NOI18N

        cboLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageChanged(evt);
            }
        });

        javax.swing.GroupLayout commonPanelContainerLayout = new javax.swing.GroupLayout(commonPanelContainer);
        commonPanelContainer.setLayout(commonPanelContainerLayout);
        commonPanelContainerLayout.setHorizontalGroup(
            commonPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 241, Short.MAX_VALUE)
        );
        commonPanelContainerLayout.setVerticalGroup(
            commonPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 90, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout customPanelContainerLayout = new javax.swing.GroupLayout(customPanelContainer);
        customPanelContainer.setLayout(customPanelContainerLayout);
        customPanelContainerLayout.setHorizontalGroup(
            customPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        customPanelContainerLayout.setVerticalGroup(
            customPanelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 137, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(customPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(commonPanelContainer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lLanguage)
                        .addGap(3, 3, 3)
                        .addComponent(cboLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lLanguage)
                    .addComponent(cboLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(commonPanelContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(customPanelContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void languageChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageChanged
        selector.setSelectedMimeType((String)cboLanguage.getSelectedItem());
    }//GEN-LAST:event_languageChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboLanguage;
    private javax.swing.JPanel commonPanelContainer;
    private javax.swing.JPanel customPanelContainer;
    private javax.swing.JLabel lLanguage;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String mimeType = selector.getSelectedMimeType();
        Preferences prefs = selector.getPreferences(mimeType);
        Preferences globalPrefs = selector.getPreferences("");
        commonPanel.update(prefs, globalPrefs);

        customPanelContainer.setVisible(false);
        customPanelContainer.removeAll();
        PreferencesCustomizer c = selector.getSelectedCustomizer();
        if (c != null) {
            customPanelContainer.add(c.getComponent(), BorderLayout.WEST);
        }
        customPanelContainer.setVisible(true);
    }

}
