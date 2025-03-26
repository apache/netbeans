/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cloud.amazon.ui.serverplugin;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.amazon.AmazonInstanceManager;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEInstance;
import org.openide.util.NbBundle;

/**
 *
 */
public class AmazonJ2EEServerWizardComponent extends javax.swing.JPanel implements DocumentListener {

    private AmazonJ2EEServerWizardPanel wizardPanel;
    private String suggestedName;
    private Map<String, List<String>> templates;
    
    private static final String SUFFIX = "-dev-env";
    private final String CONTAINER_FILER = "Tomcat"; // NOI18N
    
    /** Creates new form AmazonJ2EEServerWizardComponent */
    public AmazonJ2EEServerWizardComponent(AmazonJ2EEServerWizardPanel wizardPanel, String suggestedName, AmazonJ2EEInstance aji) {
        this.wizardPanel = wizardPanel;
        this.suggestedName = suggestedName;
        initComponents();
        setName(NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.name"));
        if (suggestedName != null) {
            if (suggestedName.length() > (23-SUFFIX.length())) {
                envNameTextField.setText(suggestedName.substring(0, 23-SUFFIX.length()) +SUFFIX);
            } else {
                envNameTextField.setText(suggestedName+SUFFIX);
            }
            envURLTextField.setText(suggestedName+SUFFIX);
            envFullURLLabel.setText("<html>"+envURLTextField.getText()+".elasticbeanstalk.com"); // NOI18N
        }
        if (aji != null) {
            accountComboBox.setEditable(false);
            accountComboBox.getModel().setSelectedItem(aji.getAmazonInstance().getName());
            ((JTextField)(appNameComboBox.getEditor().getEditorComponent())).setText(aji.getApplicationName());
            appNameComboBox.setEditable(false);
            envNameTextField.setText(aji.getEnvironmentName());
            envNameTextField.setEditable(false);
            templateComboBox.setVisible(false);
            templateLabel.setVisible(false);
            envURLTextField.setVisible(false);
            envURLLabel.setVisible(false);
            envFullURLLabel.setVisible(false);
            containerComboBox.getModel().setSelectedItem(aji.getContainerType());
            containerComboBox.setEditable(false);
        }
    }
    
    void init() {
        initAccounts();
        initApplications();
        enableApplicationComponent(hasAccount());
        hookEnvironmentURL();
        
        if (hasAccount()) {
            initContainersModel();
            accountComboBox.setSelectedIndex(0);
            reloadApplications();
        }
    }
    
    private void enableApplicationComponent(boolean enable) {
        appNameComboBox.setEnabled(enable);
        appNameLabel.setEnabled(enable);
        containerComboBox.setEnabled(enable);
        containerLabel.setEnabled(enable);
        endNameLabel.setEnabled(enable);
        envURLLabel.setEnabled(enable);
        envFullURLLabel.setEnabled(enable);
        envNameTextField.setEnabled(enable);
        envURLTextField.setEnabled(enable);
    }

    
    public String getApplicationName() {
        return ((JTextField)(appNameComboBox.getEditor().getEditorComponent())).getText();
    }
    
    public String getTemplateName() {
        if (!templateComboBox.isEnabled()) {
            return null;
        }
        String templ = (String)templateComboBox.getModel().getSelectedItem();
        if (templ != null && templ.length() > 0) {
            return templ;
        }
        return null;
    }
    
    public String getEnvironmentName() {
        return envNameTextField.getText();
    }
    
    public String getURL() {
        return envURLTextField.getText();
    }
    
    public AmazonInstance getAmazonInstance() {
        return (AmazonInstance)accountComboBox.getSelectedItem();
    }

    public String getContainerType() {
        return (String)containerComboBox.getSelectedItem();
    }
    
    public boolean hasAccount() {
        return accountComboBox.getModel().getSize() > 0;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        accountLavel = new javax.swing.JLabel();
        accountComboBox = new javax.swing.JComboBox();
        appNameLabel = new javax.swing.JLabel();
        appNameComboBox = new javax.swing.JComboBox();
        endNameLabel = new javax.swing.JLabel();
        envNameTextField = new javax.swing.JTextField();
        envURLLabel = new javax.swing.JLabel();
        envURLTextField = new javax.swing.JTextField();
        envFullURLLabel = new javax.swing.JLabel();
        containerLabel = new javax.swing.JLabel();
        containerComboBox = new javax.swing.JComboBox();
        templateLabel = new javax.swing.JLabel();
        templateComboBox = new javax.swing.JComboBox();

        accountLavel.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.accountLavel.text")); // NOI18N

        appNameLabel.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.appNameLabel.text")); // NOI18N

        appNameComboBox.setEditable(true);

        endNameLabel.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.endNameLabel.text")); // NOI18N

        envNameTextField.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.envNameTextField.text")); // NOI18N

        envURLLabel.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.envURLLabel.text")); // NOI18N

        envFullURLLabel.setFont(envFullURLLabel.getFont().deriveFont(envFullURLLabel.getFont().getSize()-2f));
        envFullURLLabel.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.envFullURLLabel.text")); // NOI18N

        containerLabel.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.containerLabel.text")); // NOI18N

        templateLabel.setText(org.openide.util.NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.templateLabel.text")); // NOI18N
        templateLabel.setEnabled(false);

        templateComboBox.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountLavel)
                    .addComponent(appNameLabel)
                    .addComponent(templateLabel)
                    .addComponent(endNameLabel)
                    .addComponent(envURLLabel)
                    .addComponent(containerLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(envFullURLLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .addComponent(containerComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 251, Short.MAX_VALUE)
                    .addComponent(envURLTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .addComponent(envNameTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .addComponent(templateComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 251, Short.MAX_VALUE)
                    .addComponent(accountComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 251, Short.MAX_VALUE)
                    .addComponent(appNameComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 251, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accountLavel)
                    .addComponent(accountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(appNameLabel)
                    .addComponent(appNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(templateLabel)
                    .addComponent(templateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endNameLabel)
                    .addComponent(envNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(envURLLabel)
                    .addComponent(envURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(envFullURLLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(containerLabel)
                    .addComponent(containerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    /*
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.enterNewAppName"),
                NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.enterNewAppNameTitle"),
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
        if (DialogDisplayer.getDefault().notify(il) != NotifyDescriptor.OK_OPTION) {
            return;
        }
*/    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox accountComboBox;
    private javax.swing.JLabel accountLavel;
    private javax.swing.JComboBox appNameComboBox;
    private javax.swing.JLabel appNameLabel;
    private javax.swing.JComboBox containerComboBox;
    private javax.swing.JLabel containerLabel;
    private javax.swing.JLabel endNameLabel;
    private javax.swing.JLabel envFullURLLabel;
    private javax.swing.JTextField envNameTextField;
    private javax.swing.JLabel envURLLabel;
    private javax.swing.JTextField envURLTextField;
    private javax.swing.JComboBox templateComboBox;
    private javax.swing.JLabel templateLabel;
    // End of variables declaration//GEN-END:variables

    private void initAccounts() {
        List<AmazonInstance> l = AmazonInstanceManager.getDefault().getInstances();
        DefaultComboBoxModel model = new DefaultComboBoxModel(l.toArray(new AmazonInstance[0]));
        accountComboBox.setModel(model);
        accountComboBox.setRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String s;
                if (value instanceof AmazonInstance) {
                    s = ((AmazonInstance)value).getName();
                } else {
                    s = (String)value;
                }
                return new JLabel(s);
            }
        });
        
        accountComboBox.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                reloadApplications();
            }
        });
    }
    
    private void initApplications() {
        JTextField tf = (JTextField)(appNameComboBox.getEditor().getEditorComponent());
        tf.getDocument().addDocumentListener(this);
        DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{suggestedName == null ? "" : suggestedName});
        appNameComboBox.setModel(model);
        appNameComboBox.setRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String) {
                    return new JLabel((String)value);
                } else {
                    assert false;
                    return null;
                }
            }
        });
        
        ((JTextField)(appNameComboBox.getEditor().getEditorComponent())).getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadTemplates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadTemplates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadTemplates();
            }
        });
    }
    
    private void reloadTemplates() {
        String appName = getApplicationName();
        if (appName != null) {
            List<String> templateNames = templates.get(appName);
            if (templateNames != null && templateNames.size() > 0) {
                templateNames = new ArrayList<String>(templateNames);
                templateNames.add(0, "");
                templateComboBox.setModel(new DefaultComboBoxModel(templateNames.toArray(new String[0])));
                templateComboBox.setSelectedIndex(0);
                templateComboBox.setEnabled(true);
                templateLabel.setEnabled(true);
                return;
            }
        }
        templateComboBox.setModel(new DefaultComboBoxModel());
        templateComboBox.setEnabled(false);
        templateLabel.setEnabled(false);
    }
    
    private void reloadApplications() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{suggestedName == null ? "" : suggestedName, NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.loadingApplications")});
        appNameComboBox.setModel(model);
        final AmazonInstance ai = (AmazonInstance)accountComboBox.getSelectedItem();
        
        AmazonInstance.runAsynchronously(new Callable<Void>() {
            @Override
            public Void call() {
                templates = ai.readApplicationTemplates();
                final List<String> apps = new ArrayList<String>(templates.keySet());
                apps.add(0, suggestedName == null ? "" : suggestedName);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        appNameComboBox.setModel(new DefaultComboBoxModel(apps.toArray(new String[0])));
                        appNameComboBox.setSelectedIndex(0);
                    }
                });
                return null;
            }
        }, ai);
    }

    private void hookEnvironmentURL() {
        envNameTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                suggestNewURL();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                suggestNewURL();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                suggestNewURL();
            }
        });
        envURLTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFullURL();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFullURL();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFullURL();
            }
        });
    }
    
    private void suggestNewURL() {
        envURLTextField.setText(envNameTextField.getText());
        if (wizardPanel != null) {
            wizardPanel.fireChange();
        }
    }

    private void updateFullURL() {
        envFullURLLabel.setText(envURLTextField.getText().length() > 0 ? 
                "<html>"+envURLTextField.getText()+".elasticbeanstalk.com" : " "); // NOI18N
        if (wizardPanel != null) {
            wizardPanel.fireChange();
        }
    }

    private void initContainersModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{NbBundle.getMessage(AmazonJ2EEServerWizardComponent.class, "AmazonJ2EEServerWizardComponent.loadingApplications")});
        final AmazonInstance ai = (AmazonInstance)accountComboBox.getSelectedItem();
        containerComboBox.setModel(model);
        AmazonInstance.runAsynchronously(new Callable<Void>() {
            @Override
            public Void call() {
                final List<String> containers = ai.readContainerTypes();
                containers.removeIf(c -> !c.contains(CONTAINER_FILER));
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        containerComboBox.setModel(new DefaultComboBoxModel(containers.toArray(new String[0])));
                    }
                });
                return null;
            }
        }, ai);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateState();
    }

    private void updateState() {
        if (wizardPanel != null) {
            wizardPanel.fireChange();
        }
    }

    public void attachSingleListener(ChangeListener changeListener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
