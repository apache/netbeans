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

import java.awt.Component;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Panel with on-save items common for all languages: reformat and remove-trailing-whitespace.
 *
 * @author Miloslav Metelka
 */
@OptionsPanelController.Keywords(keywords = {"#KW_OnSave"}, location = OptionsDisplayer.EDITOR, tabTitle="#CTL_OnSave_DisplayName")
public class OnSaveCommonPanel extends javax.swing.JPanel {
    
    private static final String[] LINE_OPTIONS = new String[] { "never", "always", "modified-lines" }; // NOI18N

    private boolean allLanguages;

    private Preferences preferences;

    public OnSaveCommonPanel() {
        initComponents();
        loc(cbUseGlobalSettings, "Use_Global_Settings");
        loc(lRemoveTrailingWhitespace, "Remove_Trailing_Whitespace");
        loc(cboRemoveTrailingWhitespace, "Remove_Trailing_Whitespace");
        loc(lReformat, "Reformat");
        loc(cboReformat, "Reformat");
        cboRemoveTrailingWhitespace.setModel(new DefaultComboBoxModel(LINE_OPTIONS));
        cboRemoveTrailingWhitespace.setRenderer(new RemoveTrailingWhitespaceRenderer(cboRemoveTrailingWhitespace.getRenderer()));
        cboReformat.setModel(new DefaultComboBoxModel(LINE_OPTIONS));
        cboReformat.setRenderer(new RemoveTrailingWhitespaceRenderer(cboReformat.getRenderer()));
    }

    void update(Preferences preferences, Preferences globalPreferences) {
        this.allLanguages = (preferences == globalPreferences);
        this.preferences = preferences;

        boolean useGlobalSettings = !allLanguages && preferences.getBoolean(SimpleValueNames.ON_SAVE_USE_GLOBAL_SETTINGS, Boolean.TRUE);
        if (useGlobalSettings) {
            preferences = globalPreferences;
        }
        cbUseGlobalSettings.setSelected(useGlobalSettings);
        cbUseGlobalSettings.setVisible(!allLanguages);
        String rtw = preferences.get(SimpleValueNames.ON_SAVE_REMOVE_TRAILING_WHITESPACE, "never");
        cboRemoveTrailingWhitespace.setSelectedItem(rtw);
        String reformat = preferences.get(SimpleValueNames.ON_SAVE_REFORMAT, "never");
        cboReformat.setSelectedItem(reformat);
        updateEnabled();
    }

    void updateEnabled() {
        boolean enabled = !cbUseGlobalSettings.isSelected();
        cboRemoveTrailingWhitespace.setEnabled(enabled);
        cboReformat.setEnabled(enabled);
    }

    private static String loc (String key) {
        return NbBundle.getMessage(OnSaveCommonPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else if (c instanceof JLabel) {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
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
        java.awt.GridBagConstraints gridBagConstraints;

        cbUseGlobalSettings = new javax.swing.JCheckBox();
        lReformat = new javax.swing.JLabel();
        cboReformat = new javax.swing.JComboBox();
        lRemoveTrailingWhitespace = new javax.swing.JLabel();
        cboRemoveTrailingWhitespace = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        cbUseGlobalSettings.setText("Use All Languages Settings");
        cbUseGlobalSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbUseGlobalSettingsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        add(cbUseGlobalSettings, gridBagConstraints);

        lReformat.setLabelFor(cboReformat);
        lReformat.setText("Reformat:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 30, 0, 0);
        add(lReformat, gridBagConstraints);

        cboReformat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboReformatActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
        add(cboReformat, gridBagConstraints);

        lRemoveTrailingWhitespace.setLabelFor(cboRemoveTrailingWhitespace);
        lRemoveTrailingWhitespace.setText("Remove Trailing Whitespace From:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 30, 0, 0);
        add(lRemoveTrailingWhitespace, gridBagConstraints);

        cboRemoveTrailingWhitespace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboRemoveTrailingWhitespaceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
        add(cboRemoveTrailingWhitespace, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cboRemoveTrailingWhitespaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboRemoveTrailingWhitespaceActionPerformed
        if (preferences != null) {
            preferences.put(SimpleValueNames.ON_SAVE_REMOVE_TRAILING_WHITESPACE,
                    (String) cboRemoveTrailingWhitespace.getSelectedItem());
        }
    }//GEN-LAST:event_cboRemoveTrailingWhitespaceActionPerformed

    private void cboReformatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboReformatActionPerformed
        if (preferences != null) {
            preferences.put(SimpleValueNames.ON_SAVE_REFORMAT, (String) cboReformat.getSelectedItem());
        }
    }//GEN-LAST:event_cboReformatActionPerformed

    private void cbUseGlobalSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbUseGlobalSettingsActionPerformed
        if (preferences != null) {
            preferences.putBoolean(SimpleValueNames.ON_SAVE_USE_GLOBAL_SETTINGS, cbUseGlobalSettings.isSelected());
            updateEnabled();
        }
    }//GEN-LAST:event_cbUseGlobalSettingsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbUseGlobalSettings;
    private javax.swing.JComboBox cboReformat;
    private javax.swing.JComboBox cboRemoveTrailingWhitespace;
    private javax.swing.JLabel lReformat;
    private javax.swing.JLabel lRemoveTrailingWhitespace;
    // End of variables declaration//GEN-END:variables

    private static final class RemoveTrailingWhitespaceRenderer implements ListCellRenderer {

        private final ListCellRenderer defaultRenderer;

        public RemoveTrailingWhitespaceRenderer(ListCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return defaultRenderer.getListCellRendererComponent(
                    list,
                    NbBundle.getMessage(OnSaveCommonPanel.class, "LINE_OPTION_" + value), //NOI18N
                    index,
                    isSelected,
                    cellHasFocus);
        }

    } // End of RemoveTrailingWhitespaceRendererRenderer class
    
}
