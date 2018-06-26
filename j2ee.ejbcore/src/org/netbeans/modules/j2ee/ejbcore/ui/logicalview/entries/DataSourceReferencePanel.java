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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport.Action;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport.Context;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.NotificationLineSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Panel for adding data source reference.
 * @author Tomas Mysik
 * @author Petr Slechta
 */
public class DataSourceReferencePanel extends JPanel {
    public static final String IS_VALID = DataSourceReferencePanel.class.getName() + ".IS_VALID"; // NOI18N
    private final J2eeModuleProvider provider;
    private final Set<String> refNames;
    private final Set<Datasource> moduleDatasources;
    private final Set<Datasource> serverDatasources;
    private final boolean isDsApiSupportedByServerPlugin;
    private NotificationLineSupport statusLine;
    
    /** Creates new form DataSourceReferencePanel */
    public DataSourceReferencePanel(J2eeModuleProvider provider, Set<String> refNames, Set<Datasource> moduleDatasources, Set<Datasource> serverDatasources) {
        initComponents();
        this.provider = provider;
        this.refNames = refNames;
        this.moduleDatasources = moduleDatasources;
        this.serverDatasources = serverDatasources;
        isDsApiSupportedByServerPlugin = isDsApiSupportedByServerPlugin();
        
        registerListeners();
        
        setupAddButton();
        setupComboBoxes();
        handleComboBoxes();
        
        populate();
    }

    public void setNotificationLine(NotificationLineSupport statusLine) {
        this.statusLine = statusLine;
        verify();
    }

    /**
     * Get the name of the data source reference.
     * @return the reference name.
     */
    public String getReferenceName() {
        return dsReferenceText.getText().trim();
    }
    
    /**
     * Get the data source.
     * @return selected data source.
     */
    public Datasource getDataSource() {
        if (projectDsRadio.isSelected()) {
            return (Datasource) projectDsCombo.getSelectedItem();
        }
        return (Datasource) serverDsCombo.getSelectedItem();
    }
    
    public boolean copyDataSourceToProject() {
        if (projectDsRadio.isSelected()) {
            return false;
        }
        return dsCopyToProjectCheck.isSelected();
    }
    
    // TODO this method should be reviewed (handle 'Missing server' error)
    private boolean isDsApiSupportedByServerPlugin() {
        return (provider != null
                && provider.isDatasourceCreationSupported()
                && ServerUtil.isValidServerInstance(provider));
    }
    
    private void registerListeners() {
        // text field
        dsReferenceText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void insertUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                verify();
            }
        });
        
        // radio buttons
        projectDsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
                handleComboBoxes();
            }
        });
        serverDsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
                handleComboBoxes();
            }
        });
        
        // combo boxes
        projectDsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
            }
        });
        serverDsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
            }
        });

        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                verify();
            }
            public void ancestorRemoved(AncestorEvent event) {
                verify();
            }
            public void ancestorMoved(AncestorEvent event) {
                verify();
            }
        });
    }
    
    private void setupComboBoxes() {
        projectDsCombo.setPrototypeDisplayValue(SelectDatabasePanel.PROTOTYPE_VALUE);
        projectDsCombo.setRenderer(DatasourceUIHelper.createDatasourceListCellRenderer());
        serverDsCombo.setRenderer(DatasourceUIHelper.createDatasourceListCellRenderer());
    }
    
    private void handleComboBoxes() {
        projectDsCombo.setEnabled(projectDsRadio.isSelected());
        serverDsCombo.setEnabled(serverDsRadio.isSelected());
        dsCopyToProjectCheck.setEnabled(serverDsRadio.isSelected());
    }
    
    private void populate() {
        populateDataSources(moduleDatasources, projectDsCombo);
        populateDataSources(serverDatasources, serverDsCombo);
    }
    
    private static void populateDataSources(final Set<Datasource> datasources, final JComboBox comboBox) {
        assert datasources != null && comboBox != null;
        
        List<Datasource> sortedDatasources = new ArrayList<Datasource>(datasources);
        Collections.sort(sortedDatasources, DatasourceUIHelper.createDatasourceComparator());
        
        comboBox.removeAllItems();
        for (Datasource ds : sortedDatasources) {
            comboBox.addItem(ds);
        }
    }
    
    private void setupAddButton() {
        addButton.setEnabled(isDsApiSupportedByServerPlugin);
        warningLabel.setVisible(!isDsApiSupportedByServerPlugin);
        projectDsRadio.setEnabled(isDsApiSupportedByServerPlugin);
        projectDsRadio.setSelected(isDsApiSupportedByServerPlugin);
        serverDsRadio.setSelected(!isDsApiSupportedByServerPlugin);
    }
    
    public void verify() {
        boolean isValid = verifyComponents();
        firePropertyChange(IS_VALID, !isValid, isValid);
    }
    
    private boolean verifyComponents() {
        // reference name
        String refName = dsReferenceText.getText();
        if (refName == null || refName.trim().length() == 0) {
            setInfo("ERR_NO_REFNAME"); // NOI18N
            return false;
        } else {
            refName = refName.trim();
            if (!Utilities.isJavaIdentifier(refName)){
                setError("ERR_INVALID_REFNAME"); // NOI18N
                return false;
            }
            if (refNames.contains(refName)) {
                setError("ERR_DUPLICATE_REFNAME"); // NOI18N
                return false;
            }
        }
        
        // data sources (radio + combo)
        if (dsGroup.getSelection() == null) {
            setInfo("ERR_NO_DATASOURCE_SELECTED"); // NOI18N
            return false;
        } else if (projectDsRadio.isSelected()) {
            if (projectDsCombo.getItemCount() == 0
                    || projectDsCombo.getSelectedIndex() == -1) {
                setInfo("ERR_NO_DATASOURCE_SELECTED"); // NOI18N
                return false;
            }
        } else if (serverDsRadio.isSelected()) {
            if (serverDsCombo.getItemCount() == 0
                    || serverDsCombo.getSelectedIndex() == -1) {
                setInfo("ERR_NO_DATASOURCE_SELECTED"); // NOI18N
                return false;
            }
        }

        if (!isDsApiSupportedByServerPlugin) {
            // DS API is not supported by the server plugin
            statusLine.setWarningMessage(NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_DSC_Warning"));
            return true;
        }
        
        // no errors
        statusLine.clearMessages();
        return true;
    }
    
    private void setError(String key) {
        if (statusLine != null) {
            statusLine.setErrorMessage(NbBundle.getMessage(DataSourceReferencePanel.class, key));
        }
    }
    
    private void setInfo(String key) {
        if (statusLine != null) {
            statusLine.setInformationMessage(NbBundle.getMessage(DataSourceReferencePanel.class, key));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dsGroup = new javax.swing.ButtonGroup();
        dsReferenceLabel = new javax.swing.JLabel();
        dsReferenceText = new javax.swing.JTextField();
        projectDsRadio = new javax.swing.JRadioButton();
        serverDsRadio = new javax.swing.JRadioButton();
        projectDsCombo = new javax.swing.JComboBox();
        serverDsCombo = new javax.swing.JComboBox();
        dsCopyToProjectCheck = new javax.swing.JCheckBox();
        addButton = new javax.swing.JButton();
        warningLabel = new javax.swing.JLabel();

        dsReferenceLabel.setLabelFor(dsReferenceText);
        org.openide.awt.Mnemonics.setLocalizedText(dsReferenceLabel, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_DsReferenceName")); // NOI18N

        dsGroup.add(projectDsRadio);
        projectDsRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(projectDsRadio, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_ProjectDs")); // NOI18N
        projectDsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectDsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        dsGroup.add(serverDsRadio);
        org.openide.awt.Mnemonics.setLocalizedText(serverDsRadio, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_ServerDs")); // NOI18N
        serverDsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverDsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(dsCopyToProjectCheck, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_DsCopyToProject")); // NOI18N
        dsCopyToProjectCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dsCopyToProjectCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(warningLabel, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_DSC_Warning")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(dsCopyToProjectCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE))
                            .addComponent(serverDsRadio)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 516, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectDsRadio)
                                    .addComponent(dsReferenceLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(serverDsCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 351, Short.MAX_VALUE)
                                    .addComponent(dsReferenceText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                    .addComponent(projectDsCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 351, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dsReferenceLabel)
                    .addComponent(dsReferenceText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectDsRadio)
                    .addComponent(projectDsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(warningLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverDsRadio)
                    .addComponent(serverDsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dsCopyToProjectCheck)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dsReferenceLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "ACSD_ReferenceName")); // NOI18N
        projectDsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "ACSD_ProjectDataSource")); // NOI18N
        serverDsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "ACSD_ServerDataSource")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "ACSD_AddDataSource")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "ACSD_AddDataSourceRef")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "ACSD_AddDataSourceRef")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Datasource datasource = handleDataSourceCustomizer();
        if (datasource != null) {
            moduleDatasources.add(datasource);
            populateDataSources(moduleDatasources, projectDsCombo);
            projectDsCombo.setSelectedItem(datasource);
        }
}//GEN-LAST:event_addButtonActionPerformed
    
    private Datasource handleDataSourceCustomizer() {
        Datasource datasource = null;
        Set<Datasource> datasources = new HashSet<Datasource>(moduleDatasources);
        datasources.addAll(serverDatasources);
        DatasourceComboBoxCustomizer dsc = new DatasourceComboBoxCustomizer(datasources);
        if (dsc.showDialog()) {
            datasource = createDataSource(dsc);
        }
        
        return datasource;
    }
    
    private Datasource createDataSource(DatasourceComboBoxCustomizer dsc) {
        // if provider is able to create it, we will use it
        if (isDsApiSupportedByServerPlugin) {
            return createServerDataSource(dsc);
        }
        return createProjectDataSource(dsc);
    }
    
    private Datasource createServerDataSource(DatasourceComboBoxCustomizer dsc) {
        final Datasource[] ds = new Datasource[1];

        // creating datasources asynchronously
        final String password = dsc.getPassword();
        final String jndiName = dsc.getJndiName();
        final String url = dsc.getUrl();
        final String username = dsc.getUsername();
        final String driverClassName = dsc.getDriverClassName();
        
        Action action = new ProgressSupport.BackgroundAction() {
            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(DatasourceUIHelper.class, "MSG_creatingDS");
                actionContext.progress(msg);
                try {
                    ds[0] = provider.createDatasource(jndiName, url, username, password, driverClassName);
                } catch (DatasourceAlreadyExistsException daee) {
                    // it should not occur bcs it should be already handled in DatasourceCustomizer
                    StringBuilder sb = new StringBuilder();
                    for (Object conflict : daee.getDatasources()) {
                        sb.append(conflict.toString() + "\n"); // NOI18N
                    }
                    String message = NbBundle.getMessage(DatasourceUIHelper.class, "ERR_DsConflict", sb.toString());
                    Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(daee, message));
                } catch (ConfigurationException ce) {
                    Exceptions.printStackTrace(ce);
                }
            }
            
            @Override
            public boolean isEnabled() {
                return password != null;
            }
        };
        
        // invoke action
        Collection<Action> actions = Collections.singleton(action);
        ProgressSupport.invoke(actions);
        
        return ds[0];
    }
    
    private Datasource createProjectDataSource(DatasourceComboBoxCustomizer dsc) {
        return new DatasourceImpl(
                        dsc.getJndiName(),
                        dsc.getUrl(),
                        dsc.getUsername(),
                        dsc.getPassword(),
                        dsc.getDriverClassName());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JCheckBox dsCopyToProjectCheck;
    private javax.swing.ButtonGroup dsGroup;
    private javax.swing.JLabel dsReferenceLabel;
    private javax.swing.JTextField dsReferenceText;
    private javax.swing.JComboBox projectDsCombo;
    private javax.swing.JRadioButton projectDsRadio;
    private javax.swing.JComboBox serverDsCombo;
    private javax.swing.JRadioButton serverDsRadio;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
    
    private static class DatasourceImpl implements Datasource {
        
        private final String jndiName;
        private final String url;
        private final String username;
        private final String password;
        private final String driverClassName;
        private String displayName;
        
        public DatasourceImpl(String jndiName, String url, String username, String password, String driverClassName) {
            this.jndiName = jndiName;
            this.url = url;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }
        
        public String getJndiName() {
            return jndiName;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public String getDriverClassName() {
            return driverClassName;
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                displayName = getJndiName() + " [" + getUrl() + "]"; // NOI18N
            }
            return displayName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }
}
