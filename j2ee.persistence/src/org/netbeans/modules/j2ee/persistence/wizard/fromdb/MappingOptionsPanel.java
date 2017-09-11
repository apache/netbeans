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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * 
 * @author  Dongmei Cao
 */
public class MappingOptionsPanel extends javax.swing.JPanel {
    
    public MappingOptionsPanel() {
        initComponents();
        fetchComboBox.setModel(new DefaultComboBoxModel(
                new String[]{NbBundle.getMessage(MappingOptionsPanel.class, "LBL_FETCH_DEFAULT"),
                    NbBundle.getMessage(MappingOptionsPanel.class, "LBL_FETCH_EAGER"),
                    NbBundle.getMessage(MappingOptionsPanel.class, "LBL_FETCH_LAZY")
                }));
        fetchComboBox.setSelectedIndex(0);

        collectionTypeComboBox.setModel(new DefaultComboBoxModel(
                new String[]{"java.util.Collection", "java.util.List", "java.util.Set"})); // NOI18N
        collectionTypeComboBox.setSelectedIndex(0);
    }
    
    public void initialize(CollectionType collectionType, FetchType fetchType, boolean fullyQualifiedTblName, boolean regenSchemaAttrs, boolean useColumnNamesInRelationships) {
        
        switch(fetchType) {
            case EAGER:
                fetchComboBox.setSelectedIndex(1);
                break;
            case LAZY:
                fetchComboBox.setSelectedIndex(2);
                break;
            case DEFAULT:
            default:
                fetchComboBox.setSelectedIndex(0);
        }
        
        switch(collectionType) {
            case LIST:
                collectionTypeComboBox.setSelectedIndex(1);
                break;
            case SET:
                collectionTypeComboBox.setSelectedIndex(2);
                break;
            case COLLECTION:
            default:
                collectionTypeComboBox.setSelectedIndex(0);
        }
        
        tableNameCheckBox.setSelected(fullyQualifiedTblName);
        regenTablesCheckBox.setSelected(regenSchemaAttrs);
        relationshipColumnNamesCheckBox.setSelected(useColumnNamesInRelationships);
    }
    
    public FetchType getFetchType() {
        int selected = fetchComboBox.getSelectedIndex();
        if(selected == 0 ) {
            return FetchType.DEFAULT;
        } else if(selected == 1 ) {
            return FetchType.EAGER;
        } else {
            return FetchType.LAZY;
        }
    }
    
    public CollectionType getCollectionType() {
        int selected = collectionTypeComboBox.getSelectedIndex();
        if(selected == 0 ) {
            return CollectionType.COLLECTION;
        } else if(selected == 1 ) {
            return CollectionType.LIST;
        } else {
            return CollectionType.SET;
        }
    }
    
    public boolean isFullyQualifiedTableName() {
        return tableNameCheckBox.isSelected();
    }
    
    public boolean isRegenSchemaAttributes() {
        return regenTablesCheckBox.isSelected();
    }

    public boolean isUseColumnNamesInRelationships() {
        return relationshipColumnNamesCheckBox.isSelected();
    }
    public boolean isUseDefaults() {
        return defaultsCheckBox.isSelected();
    }
    public boolean isGenerateUnresolved() {
        return relationshipsUnresolvedCheckBox.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fetchLabel = new javax.swing.JLabel();
        fetchComboBox = new javax.swing.JComboBox();
        tableNameCheckBox = new javax.swing.JCheckBox();
        regenTablesCheckBox = new javax.swing.JCheckBox();
        paddingPanel = new javax.swing.JPanel();
        descLabel = new javax.swing.JLabel();
        collectionTypeLabel = new javax.swing.JLabel();
        collectionTypeComboBox = new javax.swing.JComboBox();
        relationshipColumnNamesCheckBox = new javax.swing.JCheckBox();
        defaultsCheckBox = new javax.swing.JCheckBox();
        relationshipsUnresolvedCheckBox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "LBL_MappingOptions")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        fetchLabel.setLabelFor(fetchComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(fetchLabel, org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "LBL_FETCH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(fetchLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(fetchComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(tableNameCheckBox, org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "LBL_TABLE_NAME")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(tableNameCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(regenTablesCheckBox, org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "LBL_REGENERATE_TABLES")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(regenTablesCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(paddingPanel, gridBagConstraints);

        descLabel.setText(org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "LBL_TABLE_MAPPING_DESC")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(descLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(collectionTypeLabel, org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "LBL_COLLECTOIN_TYPE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(collectionTypeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(collectionTypeComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(relationshipColumnNamesCheckBox, org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "MappingOptionsPanel.relationshipColumnNamesCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(relationshipColumnNamesCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(defaultsCheckBox, org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "MappingOptionsPanel.defaultsCheckBox.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(defaultsCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(relationshipsUnresolvedCheckBox, org.openide.util.NbBundle.getMessage(MappingOptionsPanel.class, "MappingOptionsPanel.relationshipsUnresolvedCheckBox.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(relationshipsUnresolvedCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox collectionTypeComboBox;
    private javax.swing.JLabel collectionTypeLabel;
    private javax.swing.JCheckBox defaultsCheckBox;
    private javax.swing.JLabel descLabel;
    private javax.swing.JComboBox fetchComboBox;
    private javax.swing.JLabel fetchLabel;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JCheckBox regenTablesCheckBox;
    private javax.swing.JCheckBox relationshipColumnNamesCheckBox;
    private javax.swing.JCheckBox relationshipsUnresolvedCheckBox;
    private javax.swing.JCheckBox tableNameCheckBox;
    // End of variables declaration//GEN-END:variables

    public static final class WizardPanel implements WizardDescriptor.Panel<WizardDescriptor>  {

        private MappingOptionsPanel component;
        private boolean componentInitialized;
        private WizardDescriptor wizardDescriptor;

        public MappingOptionsPanel getComponent() {
            if (component == null) {
                component = new MappingOptionsPanel();
            }

            return component;
        }

        public HelpCtx getHelp() {
                return new HelpCtx(MappingOptionsPanel.class);
        }

        public void readSettings(WizardDescriptor settings) {
            wizardDescriptor = settings;
            
            if (!componentInitialized) {
                componentInitialized = true;

                RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);
                FetchType fetchType = helper.getFetchType();
                boolean fullTblName = helper.isFullyQualifiedTableNames();
                boolean regenSchema = helper.isRegenTablesAttrs();
                boolean useColumnNamesInRelationships = helper.isUseColumnNamesInRelationships();
                CollectionType clcType = helper.getCollectionType();
                getComponent().initialize(clcType, fetchType, fullTblName, regenSchema, useColumnNamesInRelationships);
            }
        }

        public boolean isValid() {
            return true;
        }

        public void storeSettings(WizardDescriptor settings) {
            RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);
            MappingOptionsPanel mPanel = getComponent();
            helper.setFetchType(mPanel.getFetchType());
            helper.setFullyQualifiedTableNames(mPanel.isFullyQualifiedTableName());
            helper.setRegenTablesAttrs(mPanel.isRegenSchemaAttributes());
            helper.setUseColumnNamesInRelationships(mPanel.isUseColumnNamesInRelationships());
            helper.setCollectionType(mPanel.getCollectionType());
            helper.setUseDefaults(mPanel.isUseDefaults());
            helper.setGenerateUnresolvedRelationships(mPanel.isGenerateUnresolved());
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }
}
