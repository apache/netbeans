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


package org.netbeans.modules.properties;


import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Customizer for locale node (<code>PropertiesLocaleNode</code>).
 *
 * @author  Peter Zavadsky
 * @see PropertiesLocaleNode
 */
public class LocaleNodeCustomizer extends JPanel {

    /** Customized properties file entry which represents one properties file. */
    private PropertiesFileEntry entry;

    /** Creates new <code>LocaleNodeCustomizer</code>. */
    public LocaleNodeCustomizer(PropertiesFileEntry entry) {
        this.entry = entry;
    
        initComponents();
        initAccessibility();
        
        Locale locale = getLocale(entry);
        
        if(new Locale("", "").equals(locale)) { // NOI18N
            changeNameButton.setEnabled(false);
            nameText.setText(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LAB_defaultLanguage"));//NOI18N
        } else {
            nameText.setText(locale.toString());
        }
        
        removeKeyButton.setEnabled(false);
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_EDITLOCALE);
    }

    /** Updates name of the <code>entry</code>. */
    private void updateName(Locale locale) {
        // Don't rename to "Default" locale node or to the same one.
        if (locale.equals(new Locale("", "")) || locale.equals(getLocale(entry)) ) {
            return;
        }

        String newName = Util.assembleName(
            entry.getDataObject().getPrimaryFile().getName(),
            locale.toString()
        );

        entry.getNodeDelegate().setName(newName);
        
        nameText.setText(locale.toString());
    }

    /** Utility method. Gets icon for key item in key list. */    
    private static Icon getKeyIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/properties/propertiesKey.gif", false); // NOI18N
    }
    
    /** Gets locale which represents the entry. Utility method.
     * @param entry entry which <code>Locale</code> to get */
    static Locale getLocale(PropertiesFileEntry entry) {
        String localeSuffix = Util.getLocaleSuffix(entry);
        String languageCode = Util.getLanguage(localeSuffix);
        
        if (languageCode == null) {
            return new Locale("", ""); // NOI18N
        }
        
        String countryCode = Util.getCountry(localeSuffix);
        
        if (countryCode == null) {
            return new Locale(languageCode, ""); // NOI18N
        }
        
        String variant = Util.getVariant(localeSuffix);
        
        if (variant == null) {
            return new Locale(languageCode, countryCode);
        }
        
        return new Locale(languageCode, countryCode, variant);
    }

    /** Retrievs keys in entry. Utility method.
     * @param entry entry which keys to get */
    private static String[] retrieveKeys(PropertiesFileEntry entry) {
        List<String> keysList = new ArrayList<String>();
        
        if(entry == null) {
            return new String[0];
        }
        
        for (Iterator<Element.ItemElem> it = entry.getHandler().getStructure().allItems(); it.hasNext(); ) {
            String key = it.next().getKey();
            if (key != null && !(keysList.contains(key))) {
                keysList.add(key);
            }
        }
        
        String[] keys = new String[keysList.size()];
        keysList.toArray(keys);
        return keys;
    }
    
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_LocaleNodeCustomizer"));
                
        nameText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_LocaleName"));
        addKeyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_AddKey"));
        changeNameButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_ChangeNameButton"));
        removeKeyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_RemoveKey"));
        keyList.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_KeyList"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        keyLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        keyList = new JList(retrieveKeys(entry));
        addKeyButton = new javax.swing.JButton();
        removeKeyButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        nameText = new javax.swing.JTextField();
        changeNameButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setLabelFor(nameText);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        keyLabel.setLabelFor(keyList);
        org.openide.awt.Mnemonics.setLocalizedText(keyLabel, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Keys")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(keyLabel, gridBagConstraints);

        keyList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                label.setText(value.toString());

                label.setIcon(getKeyIcon());

                return label;
            }
        });
        keyList.setPrototypeCellValue("0123456789012345678901234567890123456789");
        keyList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                keyListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(keyList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 5, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addKeyButton, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_AddKey")); // NOI18N
        addKeyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addKeyButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(addKeyButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeKeyButton, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_RemoveKey")); // NOI18N
        removeKeyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeKeyButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 5, 11);
        add(removeKeyButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        nameText.setEditable(false);
        nameText.selectAll();
        nameText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameTextFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(nameText, gridBagConstraints);

        changeNameButton.setText("...");
        changeNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeNameButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(changeNameButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void nameTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFocusGained
        // Accessibility
        nameText.selectAll();
    }//GEN-LAST:event_nameTextFocusGained

    private void changeNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeNameButtonActionPerformed
        final Dialog[] dialog = new Dialog[1];
        final LocalePanel panel = new LocalePanel(getLocale(entry));

        DialogDescriptor dd = new DialogDescriptor(
            panel,
            NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleTitle"),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent evt2) {
                    // OK pressed
                    if (evt2.getSource() == DialogDescriptor.OK_OPTION) {
                        dialog[0].setVisible(false);
                        dialog[0].dispose();

                        updateName(panel.getLocale());
                    // Cancel pressed
                    } else if (evt2.getSource() == DialogDescriptor.CANCEL_OPTION) {
                        dialog[0].setVisible(false);
                        dialog[0].dispose();
                    }
                }
            }
        );
        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialog[0].setVisible(true);
    }//GEN-LAST:event_changeNameButtonActionPerformed

    private void keyListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_keyListValueChanged
        if (keyList.isSelectionEmpty()) {
            removeKeyButton.setEnabled(false);
        } else {
            removeKeyButton.setEnabled(true);
        }
    }//GEN-LAST:event_keyListValueChanged

    private void removeKeyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeKeyButtonActionPerformed
        Object[] selectedValues = keyList.getSelectedValues();

        PropertiesStructure ps = entry.getHandler().getStructure();
        
        for(int i=0; i<selectedValues.length; i++) {
            ps.deleteItem((String)selectedValues[i]);
        }
        
        updateKeyList();
    }//GEN-LAST:event_removeKeyButtonActionPerformed

    private void addKeyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeyButtonActionPerformed
        try {
            entry.getNodeDelegate().getNewTypes()[0].create();

            updateKeyList();
        } catch(IOException ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ioe);
        }
    }//GEN-LAST:event_addKeyButtonActionPerformed

    /** Updates keys. Utility method. */
    private void updateKeyList() {
        // Very ugly, but now there is incosistency gap in the properties structure.
        // REmove threads when changed parsing.
        PropertiesRequestProcessor.getInstance().post(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // Update keys.
                        keyList.setListData(retrieveKeys(entry));
                    }
                });
            }
        });
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addKeyButton;
    private javax.swing.JButton changeNameButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JList keyList;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameText;
    private javax.swing.JButton removeKeyButton;
    // End of variables declaration//GEN-END:variables

}
