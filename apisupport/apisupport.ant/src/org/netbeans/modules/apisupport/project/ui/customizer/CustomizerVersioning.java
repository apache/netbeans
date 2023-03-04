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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Versioning</em> panel in Netbeans Module customizer.
 *
 * @author Martin Krauskopf
 */
final class CustomizerVersioning extends NbPropertyPanel.Single {
    
    private static final int CHECKBOX_WIDTH = new JCheckBox().getWidth();
    
    private boolean lastAppImplChecked;
    
    private Dimension lastSize;
    BasicCustomizer.SubCategoryProvider provider;
    
    
    /** Creates new form CustomizerVersioning */
    CustomizerVersioning(SingleModuleProperties props, ProjectCustomizer.Category cat, BasicCustomizer.SubCategoryProvider prov) {
        super(props, CustomizerVersioning.class, cat);
        initComponents();
        initAccesibility();
        initPublicPackageTable();
        refresh();
        attachListeners();
        checkValidity();
        provider = prov;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (provider != null) {
            showSubCategory(provider);
            //do preselection on first showing the panel only..
            provider = null;
        }
    }
    
    protected void refresh() {
        ApisupportAntUIUtils.setText(majorRelVerValue, getProperties().getMajorReleaseVersion());
        ApisupportAntUIUtils.setText(tokensValue, getProperties().getProvidedTokens());
        String specVersion = getProperties().getSpecificationVersion();
        if (getProperties().isOSGi()) {
            ApisupportAntUIUtils.setText(specificationVerValue, specVersion);
            appendImpl.setEnabled(false);
            implVer.setEnabled(false);
            implVerValue.setEnabled(false);
        } else {
        if (null == specVersion || "".equals(specVersion)) { // NOI18N
            appendImpl.setSelected(true);
            ApisupportAntUIUtils.setText(specificationVerValue, getProperty(SingleModuleProperties.SPEC_VERSION_BASE));
        } else {
            ApisupportAntUIUtils.setText(specificationVerValue, specVersion);
        }
        ApisupportAntUIUtils.setText(implVerValue, getProperties().getImplementationVersion());
        }
        friendsList.setModel(getProperties().getFriendListModel());
        ApisupportAntUIUtils.setText(cnbValue, getProperties().getCodeNameBase());
        regularMod.setSelected(true);
        autoloadMod.setSelected(getBooleanProperty(SingleModuleProperties.IS_AUTOLOAD));
        eagerMod.setSelected(getBooleanProperty(SingleModuleProperties.IS_EAGER));
        removeFriendButton.setEnabled(false);
        getProperties().getDependenciesListModelInBg(new Runnable() {
            public void run() {
                updateAppendImpl();
            }
        });
    }
    
    private void attachListeners() {
        friendsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    removeFriendButton.setEnabled(friendsList.getSelectedIndex() != -1);
                }
            }
        });
        majorRelVerValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
        implVerValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateAppendImpl();
                checkValidity();
            }
        });
        specificationVerValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
        publicPkgsTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = publicPkgsTable.getSelectedRow();
                if (row == -1) {
                    // Nothing selected; e.g. user has tabbed into the table but not pressed Down key.
                    return;
                }
                Boolean b = (Boolean) publicPkgsTable.
                        getValueAt(row, 0);
                if(!b && !selectAllPkgsButton.isEnabled())
                    selectAllPkgsButton.setEnabled(true);
                if(b && !deselectAllPkgsButton.isEnabled())
                    deselectAllPkgsButton.setEnabled(true);
                checkPublicPackagesSelectionButtons();
                checkValidity();
            }
        });
    }
    
    boolean isCustomizerValid() {
        return checkMajorReleaseVersion();
    }
    
    private boolean checkMajorReleaseVersion() {
        boolean valid;
        String mrv = majorRelVerValue.getText().trim();
        try {
            valid = (mrv.length() == 0 || Integer.parseInt(mrv) >= 0);
        } catch (NumberFormatException nfe) {
            valid = false;
        }
        return valid;
    }
    
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    protected void checkValidity() {
        exportOnlyToFriend.setSelected(getFriendModel().getSize() > 0);
        // check major release version
        if (!checkMajorReleaseVersion()) {
            category.setErrorMessage(getMessage("MSG_MajorReleaseVersionIsInvalid")); // NOI18N
            category.setValid(false);
        } else if (exportOnlyToFriend.isSelected() && getPublicPackagesModel().getSelectedPackages().size() < 1) {
            category.setErrorMessage(getMessage("MSG_PublicPackageMustBeSelected"));
            category.setValid(false);
        } else if (implVerValue.getText().matches(".*[^0-9].*")) { // NOI18N
            category.setErrorMessage(getMessage("MSG_integer_impl_version_recommended"));
            category.setValid(true);
        } else {
            boolean ok = true;
            String text = specificationVerValue.getText();
            if (text != null && !text.isEmpty() && !text.matches(/* nb-module-project3.xsd#specificationVersionType */"(0|[1-9][0-9]*)([.](0|[1-9][0-9]*))*")) {
                ok = false;
            }
            if (ok) {
                category.setErrorMessage(null);
                category.setValid(true);
            } else {
                category.setErrorMessage(getMessage("MSG_invalid_spec_version"));
                category.setValid(false);
            }
        }
    }
    
    private void initPublicPackageTable() {
        publicPkgsTable.setModel(getProperties().getPublicPackagesModel());
        publicPkgsTable.getColumnModel().getColumn(0).setMaxWidth(CHECKBOX_WIDTH + 20);
        publicPkgsTable.setRowHeight(publicPkgsTable.getFontMetrics(publicPkgsTable.getFont()).getHeight() +
                (2 * publicPkgsTable.getRowMargin()));
        publicPkgsTable.setTableHeader(null);
        publicPkgsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        publicPkgsSP.getViewport().setBackground(publicPkgsTable.getBackground());
        final Action switchAction = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int row = publicPkgsTable.getSelectedRow();
                if (row == -1) {
                    // Nothing selected; e.g. user has tabbed into the table but not pressed Down key.
                    return;
                }
                Boolean b = (Boolean) publicPkgsTable.
                        getValueAt(row, 0);
                publicPkgsTable.setValueAt(Boolean.valueOf(!b.booleanValue()),
                        row, 0);
                checkValidity();
            }
        };
        publicPkgsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                switchAction.actionPerformed(null);
            }
        });
        publicPkgsTable.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startEditing"); // NOI18N
        publicPkgsTable.getActionMap().put("startEditing", switchAction); // NOI18N
        
        checkPublicPackagesSelectionButtons();
    }
    
    private void updateAppendImpl() {
        boolean isImplVerFiled = !"".equals(implVerValue.getText().trim()); // NOI18N
        boolean shouldEnable = isImplVerFiled || getProperties().dependingOnImplDependency();
        if (shouldEnable && !appendImpl.isEnabled()) {
            appendImpl.setEnabled(true);
            appendImpl.setSelected(lastAppImplChecked);
        } else if (!shouldEnable && appendImpl.isEnabled()) {
            appendImpl.setEnabled(false);
            lastAppImplChecked = appendImpl.isSelected();
            appendImpl.setSelected(false);
        }
    }
    
    @Override
    public void store() {
        getProperties().setMajorReleaseVersion(majorRelVerValue.getText().trim());
        String specVer = specificationVerValue.getText().trim();
        if (appendImpl.isSelected()) {
            getProperties().setSpecificationVersion(""); // NOI18N
            setProperty(SingleModuleProperties.SPEC_VERSION_BASE, specVer);
        } else {
            getProperties().setSpecificationVersion(specVer);
            setProperty(SingleModuleProperties.SPEC_VERSION_BASE, ""); // NOI18N
        }
        getProperties().setImplementationVersion(implVerValue.getText().trim());
        getProperties().setProvidedTokens(tokensValue.getText().trim());
        setBooleanProperty(SingleModuleProperties.IS_AUTOLOAD, autoloadMod.isSelected());
        setBooleanProperty(SingleModuleProperties.IS_EAGER, eagerMod.isSelected());
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String pName = evt.getPropertyName();
        if (SingleModuleProperties.DEPENDENCIES_PROPERTY == pName) {
            updateAppendImpl();
        } else if (ModuleProperties.PROPERTIES_REFRESHED == pName) {
            refresh();
            checkValidity();
        }
    }
    
    private CustomizerComponentFactory.FriendListModel getFriendModel() {
        return (CustomizerComponentFactory.FriendListModel) friendsList.getModel();
    }
    
    private CustomizerComponentFactory.PublicPackagesTableModel getPublicPackagesModel() {
        return (CustomizerComponentFactory.PublicPackagesTableModel) publicPkgsTable.getModel();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        moduleTypeGroup = new javax.swing.ButtonGroup();
        cnb = new javax.swing.JLabel();
        cnbValue = new javax.swing.JTextField();
        majorRelVer = new javax.swing.JLabel();
        majorRelVerValue = new javax.swing.JTextField();
        specificationVer = new javax.swing.JLabel();
        specificationVerValue = new javax.swing.JTextField();
        implVer = new javax.swing.JLabel();
        implVerValue = new javax.swing.JTextField();
        tokens = new javax.swing.JLabel();
        tokensValue = new javax.swing.JTextField();
        appendImpl = new javax.swing.JCheckBox();
        publicPkgs = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        buttonPanel1 = new javax.swing.JPanel();
        addFriendButton = new javax.swing.JButton();
        removeFriendButton = new javax.swing.JButton();
        filler1 = new javax.swing.JLabel();
        friendsSP = new javax.swing.JScrollPane();
        friendsList = new javax.swing.JList();
        exportOnlyToFriend = new javax.swing.JCheckBox();
        typePanel = new javax.swing.JPanel();
        regularMod = new javax.swing.JRadioButton();
        autoloadMod = new javax.swing.JRadioButton();
        eagerMod = new javax.swing.JRadioButton();
        typeTxt = new javax.swing.JLabel();
        publicPackagesPanel = new javax.swing.JPanel();
        publicPkgsSP = new javax.swing.JScrollPane();
        publicPkgsTable = new javax.swing.JTable();
        Dimension tableDim = publicPkgsTable.getPreferredScrollableViewportSize();
        publicPkgsTable.setPreferredScrollableViewportSize(new Dimension(tableDim.width, 100));
        buttonPanel3 = new javax.swing.JPanel();
        selectAllPkgsButton = new javax.swing.JButton();
        deselectAllPkgsButton = new javax.swing.JButton();
        filler3 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        cnb.setLabelFor(cnbValue);
        org.openide.awt.Mnemonics.setLocalizedText(cnb, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_CNB")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(cnb, gridBagConstraints);

        cnbValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(cnbValue, gridBagConstraints);

        majorRelVer.setLabelFor(majorRelVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(majorRelVer, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_MajorReleaseVersion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(majorRelVer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(majorRelVerValue, gridBagConstraints);

        specificationVer.setLabelFor(specificationVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(specificationVer, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_SpecificationVersion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 12);
        add(specificationVer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(specificationVerValue, gridBagConstraints);

        implVer.setLabelFor(implVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(implVer, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_ImplementationVersion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(implVer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(implVerValue, gridBagConstraints);

        tokens.setLabelFor(tokensValue);
        org.openide.awt.Mnemonics.setLocalizedText(tokens, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_ProvidedTokens")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(tokens, gridBagConstraints);

        tokensValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tokensValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(tokensValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(appendImpl, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AppendImplementation")); // NOI18N
        appendImpl.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(appendImpl, gridBagConstraints);

        publicPkgs.setLabelFor(publicPkgsTable);
        org.openide.awt.Mnemonics.setLocalizedText(publicPkgs, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_PublicPackages")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 12);
        add(publicPkgs, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.GridBagLayout());

        buttonPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addFriendButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AddButton")); // NOI18N
        addFriendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFriend(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel1.add(addFriendButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeFriendButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_RemoveButton")); // NOI18N
        removeFriendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFriend(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        buttonPanel1.add(removeFriendButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 1.0;
        buttonPanel1.add(filler1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        bottomPanel.add(buttonPanel1, gridBagConstraints);

        friendsList.setVisibleRowCount(3);
        friendsSP.setViewportView(friendsList);
        friendsList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "ACS_FriendsList")); // NOI18N
        friendsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "ACSD_FriendsList")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        bottomPanel.add(friendsSP, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(bottomPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(exportOnlyToFriend, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_ExportOnlyToFriends")); // NOI18N
        exportOnlyToFriend.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exportOnlyToFriend.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(exportOnlyToFriend, gridBagConstraints);

        typePanel.setLayout(new java.awt.GridBagLayout());

        moduleTypeGroup.add(regularMod);
        regularMod.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(regularMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_RegularModule")); // NOI18N
        regularMod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        typePanel.add(regularMod, gridBagConstraints);

        moduleTypeGroup.add(autoloadMod);
        org.openide.awt.Mnemonics.setLocalizedText(autoloadMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AutoloadModule")); // NOI18N
        autoloadMod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        typePanel.add(autoloadMod, gridBagConstraints);

        moduleTypeGroup.add(eagerMod);
        org.openide.awt.Mnemonics.setLocalizedText(eagerMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_EagerModule")); // NOI18N
        eagerMod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        typePanel.add(eagerMod, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(typeTxt, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_ModuleType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typePanel.add(typeTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(typePanel, gridBagConstraints);

        publicPackagesPanel.setMinimumSize(new java.awt.Dimension(112, 62));
        publicPackagesPanel.setPreferredSize(new java.awt.Dimension(347, 62));
        publicPackagesPanel.setLayout(new java.awt.GridBagLayout());

        publicPkgsTable.setShowHorizontalLines(false);
        publicPkgsSP.setViewportView(publicPkgsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        publicPackagesPanel.add(publicPkgsSP, gridBagConstraints);

        buttonPanel3.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(selectAllPkgsButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_SelectAll"));
        selectAllPkgsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllPkgsButtonaddFriend(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel3.add(selectAllPkgsButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(deselectAllPkgsButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_DeselectAll"));
        deselectAllPkgsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllPkgsButtonremoveFriend(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        buttonPanel3.add(deselectAllPkgsButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 1.0;
        buttonPanel3.add(filler3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        publicPackagesPanel.add(buttonPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.8;
        add(publicPackagesPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void removeFriend(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFriend
        getFriendModel().removeFriend((String) friendsList.getSelectedValue());
        if (getFriendModel().getSize() > 0) {
            friendsList.setSelectedIndex(0);
        }
        friendsList.requestFocus();
        checkValidity();
    }//GEN-LAST:event_removeFriend
    
    private void addFriend(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFriend
        AddFriendPanel addFriend = new AddFriendPanel(getProperties());
        DialogDescriptor descriptor = new DialogDescriptor(addFriend, getMessage("CTL_AddNewFriend_Title"));
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.AddFriendPanel"));
        final JButton okButton = new JButton(getMessage("CTL_OK"));
        JButton cancel = new JButton(getMessage("CTL_Cancel"));
        okButton.setEnabled(false);
        Object[] options = new Object[] { okButton , cancel };
        descriptor.setOptions(options);
        descriptor.setClosingOptions(options);
        final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        addFriend.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName() == AddFriendPanel.VALID_PROPERTY) {
                    okButton.setEnabled(((Boolean) pce.getNewValue()).booleanValue());
                }
            }
        });
        d.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_AddNewFriend_Title"));
        okButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_OK"));
        cancel.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_Cancel"));
        if (lastSize != null) {
            d.setSize(lastSize);
        } else {
            d.pack();
        }
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        lastSize = d.getSize();
        if (descriptor.getValue().equals(okButton)) {
            String newFriendCNB = addFriend.getFriendCNB();
            getFriendModel().addFriend(newFriendCNB);
            friendsList.setSelectedValue(newFriendCNB, true);
            checkValidity();
        }
        d.dispose();
        friendsList.requestFocus();
    }//GEN-LAST:event_addFriend

private void tokensValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tokensValueActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_tokensValueActionPerformed

    private void selectAllPkgsButtonaddFriend(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllPkgsButtonaddFriend
        selectOrDeselectAllPackages(true);
    }//GEN-LAST:event_selectAllPkgsButtonaddFriend

    private void deselectAllPkgsButtonremoveFriend(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllPkgsButtonremoveFriend
        selectOrDeselectAllPackages(false);
    }//GEN-LAST:event_deselectAllPkgsButtonremoveFriend
    
    private String getMessage(String key) {
        return NbBundle.getMessage(CustomizerVersioning.class, key);
    }
    
    public void showSubCategory(BasicCustomizer.SubCategoryProvider prov) {
        if (CustomizerProviderImpl.CATEGORY_VERSIONING.equals(prov.getCategory()) &&
            CustomizerProviderImpl.SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES.equals(prov.getSubcategory())) {
            publicPkgsTable.requestFocus();
            /* XXX does not work quite right under Ocean; have to press TAB once; this does not help:
            if (publicPkgsTable.getModel().getRowCount() > 0) {
                publicPkgsTable.setEditingRow(0);
                publicPkgsTable.setEditingColumn(1);
            }
             */
        }
    }
    
    private void selectOrDeselectAllPackages(boolean check)
    {
        int rowCount = publicPkgsTable.getRowCount();
        if(rowCount > 0)
        {
            for(int i = 0; i < rowCount; i++)
            {
                publicPkgsTable.setValueAt(check,
                        i, 0);
            }
        }
        selectAllPkgsButton.setEnabled(!check);
        deselectAllPkgsButton.setEnabled(check);
        checkValidity();
    }
    
    private void checkPublicPackagesSelectionButtons()
    {
        boolean enableSelectAllPkgsButton = false;
        boolean enableDeselectAllPkgsButton = false;
        int rowCount = publicPkgsTable.getRowCount();
        if(rowCount > 0)
        {
            for(int i = 0; i < rowCount; i++)
            {
                if(!(Boolean) publicPkgsTable.
                        getValueAt(i, 0))
                {
                    enableSelectAllPkgsButton = true;
                }
                else if((Boolean) publicPkgsTable.
                        getValueAt(i, 0))
                {
                    enableDeselectAllPkgsButton = true;
                }
            }
        }
        selectAllPkgsButton.setEnabled(enableSelectAllPkgsButton);
        deselectAllPkgsButton.setEnabled(enableDeselectAllPkgsButton);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFriendButton;
    private javax.swing.JCheckBox appendImpl;
    private javax.swing.JRadioButton autoloadMod;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel buttonPanel1;
    private javax.swing.JPanel buttonPanel3;
    private javax.swing.JLabel cnb;
    private javax.swing.JTextField cnbValue;
    private javax.swing.JButton deselectAllPkgsButton;
    private javax.swing.JRadioButton eagerMod;
    private javax.swing.JCheckBox exportOnlyToFriend;
    private javax.swing.JLabel filler1;
    private javax.swing.JLabel filler3;
    private javax.swing.JList friendsList;
    private javax.swing.JScrollPane friendsSP;
    private javax.swing.JLabel implVer;
    private javax.swing.JTextField implVerValue;
    private javax.swing.JLabel majorRelVer;
    private javax.swing.JTextField majorRelVerValue;
    private javax.swing.ButtonGroup moduleTypeGroup;
    private javax.swing.JPanel publicPackagesPanel;
    private javax.swing.JLabel publicPkgs;
    private javax.swing.JScrollPane publicPkgsSP;
    private javax.swing.JTable publicPkgsTable;
    private javax.swing.JRadioButton regularMod;
    private javax.swing.JButton removeFriendButton;
    private javax.swing.JButton selectAllPkgsButton;
    private javax.swing.JLabel specificationVer;
    private javax.swing.JTextField specificationVerValue;
    private javax.swing.JLabel tokens;
    private javax.swing.JTextField tokensValue;
    private javax.swing.JPanel typePanel;
    private javax.swing.JLabel typeTxt;
    // End of variables declaration//GEN-END:variables
    
    private void initAccesibility() {
        addFriendButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AddFriendButton"));
        removeFriendButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RemoveFriendButton"));
        cnbValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CnbValue"));
        majorRelVerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_MajorRelVerValue"));
        specificationVerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_SpecificationVerValuea"));
        appendImpl.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AppendImpl"));
        implVerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ImplVerValue"));
        regularMod.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RegularMod"));
        autoloadMod.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AutoloadMod"));
        eagerMod.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_EagerMod"));
        publicPkgsTable.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_PublicPkgsTable"));
        tokensValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_TokensValue"));
    }
    
}
