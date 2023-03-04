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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.PatternResourcesSetupPanel.Pattern;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author  Nam Nguyen
 */
public class ContainerItemSetupPanelVisual extends javax.swing.JPanel 
    implements AbstractPanel.Settings, SourcePanel 
{
    
    private Project project;
    private List<ChangeListener> listeners;
    
    private boolean resourceClassNameOveridden;
    private boolean containerUriOveridden;
    private boolean containerClassNameOveridden;
    
    public ContainerItemSetupPanelVisual(String name) {
        setName(name);
        this.listeners = new ArrayList<ChangeListener>();
        initComponents();
        packageComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireChange();
            }
        });
        classTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                compareClassNames();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                compareClassNames();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                compareClassNames();
            }
        });
        containerTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                compareClassNames();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                compareClassNames();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                compareClassNames();
            }
        });

        medaTypeComboBox.setModel(new DefaultComboBoxModel(GenericResourceBean.getSupportedMimeTypes()));
        ((JTextComponent) packageComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireChange();
            }
        });
    }

        private void compareClassNames() {
            String className = classTextField.getText().trim();
            String containerClassName = containerTextField.getText().trim();
            if (className.equals(containerClassName)) {
                fireChange();
            }
        }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        classLabel = new javax.swing.JLabel();
        uriLabel = new javax.swing.JLabel();
        uriTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        medaTypeComboBox = new javax.swing.JComboBox();
        mediaTypeLabel = new javax.swing.JLabel();
        contentClassLabel = new javax.swing.JLabel();
        selectClassButton = new javax.swing.JButton();
        representationClassTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        resourceNameLabel = new javax.swing.JLabel();
        resourceNameTextField = new javax.swing.JTextField();
        classTextField = new javax.swing.JTextField();
        containerLabel = new javax.swing.JLabel();
        containerTextField = new javax.swing.JTextField();
        containerUriLabel = new javax.swing.JLabel();
        containerUriTextField = new javax.swing.JTextField();
        contentClassLabel1 = new javax.swing.JLabel();
        containerRepresentationClassTextField = new javax.swing.JTextField();
        selectClassButton1 = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(450, 312));

        classLabel.setLabelFor(classTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(classLabel, bundle.getString("LBL_ClassName")); // NOI18N

        uriLabel.setLabelFor(uriTextField);
        org.openide.awt.Mnemonics.setLocalizedText(uriLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_UriTemplate")); // NOI18N

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N

        locationComboBox.setMinimumSize(new java.awt.Dimension(34, 20));
        locationComboBox.setPreferredSize(new java.awt.Dimension(4, 25));
        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Package")); // NOI18N

        packageComboBox.setEditable(true);
        packageComboBox.setInheritsPopupMenu(true);
        packageComboBox.setMinimumSize(new java.awt.Dimension(133, 20));
        packageComboBox.setPreferredSize(new java.awt.Dimension(4, 25));
        packageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageChanged(evt);
            }
        });
        packageComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                packageComboBoxKeyReleased(evt);
            }
        });

        medaTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        medaTypeComboBox.setMinimumSize(new java.awt.Dimension(69, 20));

        mediaTypeLabel.setLabelFor(medaTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(mediaTypeLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_MimeType")); // NOI18N

        contentClassLabel.setLabelFor(representationClassTextField);
        org.openide.awt.Mnemonics.setLocalizedText(contentClassLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_RepresentationClass")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectClassButton, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Select")); // NOI18N
        selectClassButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClickHandler(evt);
            }
        });
        selectClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectClassButtonActionPerformed(evt);
            }
        });

        representationClassTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                representationClassChanged(evt);
            }
        });

        resourceNameLabel.setLabelFor(resourceNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(resourceNameLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ResourceName")); // NOI18N

        resourceNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resourceNameChanged(evt);
            }
        });

        classTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                classTextFieldKeyReleased(evt);
            }
        });

        containerLabel.setLabelFor(containerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(containerLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ContainerClass")); // NOI18N

        containerTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                containerClassNameChanged(evt);
            }
        });

        containerUriLabel.setLabelFor(containerUriTextField);
        org.openide.awt.Mnemonics.setLocalizedText(containerUriLabel, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ContainerUriTemplate")); // NOI18N

        contentClassLabel1.setLabelFor(containerRepresentationClassTextField);
        org.openide.awt.Mnemonics.setLocalizedText(contentClassLabel1, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ContainerRepresentationClass")); // NOI18N

        containerRepresentationClassTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                containerRepresentationClassChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(selectClassButton1, org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_SelectContainerRepresentationClass")); // NOI18N
        selectClassButton1.setActionCommand(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Select")); // NOI18N
        selectClassButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectClassButtonMouseClickHandler(evt);
            }
        });
        selectClassButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectClassButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(classLabel)
                    .addComponent(locationLabel)
                    .addComponent(projectLabel)
                    .addComponent(containerLabel)
                    .addComponent(resourceNameLabel)
                    .addComponent(packageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resourceNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                    .addComponent(classTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                    .addComponent(projectTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                    .addComponent(locationComboBox, 0, 281, Short.MAX_VALUE)
                    .addComponent(containerTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                    .addComponent(packageComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentClassLabel1)
                    .addComponent(contentClassLabel)
                    .addComponent(uriLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(uriTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                    .addComponent(representationClassTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                    .addComponent(containerRepresentationClassTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                    .addComponent(containerUriTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                    .addComponent(medaTypeComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 141, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(selectClassButton)
                    .addComponent(selectClassButton1)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(containerUriLabel)
                    .addComponent(mediaTypeLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resourceNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resourceNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classLabel)
                    .addComponent(classTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(containerLabel)
                    .addComponent(containerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(packageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uriLabel)
                    .addComponent(uriTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(containerUriLabel)
                    .addComponent(containerUriTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mediaTypeLabel)
                    .addComponent(medaTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectClassButton)
                    .addComponent(contentClassLabel)
                    .addComponent(representationClassTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentClassLabel1)
                    .addComponent(selectClassButton1)
                    .addComponent(containerRepresentationClassTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        classLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ClassName")); // NOI18N
        uriLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_UirTemplate")); // NOI18N
        uriTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "ItemUriTemplate")); // NOI18N
        uriTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_Uri")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_ProjectName")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "Location")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_Location")); // NOI18N
        packageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Package")); // NOI18N
        packageComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "ResourcePackage")); // NOI18N
        packageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_ResourcePackage")); // NOI18N
        medaTypeComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "MimeType")); // NOI18N
        medaTypeComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_MimeType")); // NOI18N
        mediaTypeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_MimeType")); // NOI18N
        contentClassLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_RepresentationClass")); // NOI18N
        selectClassButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "SelectItemResourceRepresentation")); // NOI18N
        selectClassButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_SelectClass")); // NOI18N
        representationClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "ItemResourceRepresentationClass")); // NOI18N
        representationClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_RepresentationClass")); // NOI18N
        resourceNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ResourceName")); // NOI18N
        resourceNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "ResourceName")); // NOI18N
        resourceNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_ResourceName")); // NOI18N
        classTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "ClassName")); // NOI18N
        classTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_ClassName")); // NOI18N
        containerLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ContainerClass")); // NOI18N
        containerTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "ContainerClassName")); // NOI18N
        containerTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_ContainerClassName")); // NOI18N
        containerUriLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ContainerUriTemplate")); // NOI18N
        containerUriTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ContainerUriTemplate")); // NOI18N
        containerUriTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_ContainerUriTemplate")); // NOI18N
        contentClassLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_ContainerRepresentationClass")); // NOI18N
        containerRepresentationClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "ContainerResourceRepresentationClass")); // NOI18N
        containerRepresentationClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_ContainerRepresentationClass")); // NOI18N
        selectClassButton1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "SelectContainerRepresentationClass")); // NOI18N
        selectClassButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "DESC_SelectContainerRepresentationClass")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Specify_Resource_Class")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ContainerItemSetupPanelVisual.class, "LBL_Specify_Resource_Class")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void selectClassButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectClassButton1ActionPerformed
    fireChange();
}//GEN-LAST:event_selectClassButton1ActionPerformed

private void selectClassButtonMouseClickHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectClassButtonMouseClickHandler
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final ElementHandle<TypeElement> handle = TypeElementFinder.find(Util.getClasspathInfo(project), new TypeElementFinder.Customizer() {

                            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {
                                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
                            }

                            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                                return true;
                            }
                        });

                if (handle != null) {
                    containerRepresentationClassTextField.setText(handle.getQualifiedName());
                    fireChange();
                }
            }
        });
}//GEN-LAST:event_selectClassButtonMouseClickHandler

private void containerRepresentationClassChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_containerRepresentationClassChanged
    fireChange();
}//GEN-LAST:event_containerRepresentationClassChanged

    private void resourceNameChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resourceNameChanged
    String newName = resourceNameTextField.getText();
    if (! resourceClassNameOveridden) {
        classTextField.setText(findFreeClassName(newName));
    }
//    if (! uriOveridden) {
//        String currentUri = uriTextField.getText();
//        uriTextField.setText(Util.deriveUri(newName, currentUri));
//    }
    if (! containerClassNameOveridden) {
        containerTextField.setText(findFreeClassName(Util.pluralize(newName)));
    }
    if (! containerUriOveridden) {
        String newContainerName = Util.lowerFirstChar(newName);
        String currentUri = containerUriTextField.getText();
        containerUriTextField.setText(Util.deriveUri(newContainerName, currentUri));
    }
    fireChange();
}//GEN-LAST:event_resourceNameChanged

private void containerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_containerTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_containerTextFieldActionPerformed

private void containerClassNameChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_containerClassNameChanged
    containerClassNameOveridden = true;
    fireChange();
}//GEN-LAST:event_containerClassNameChanged

private void representationClassChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_representationClassChanged
    fireChange();
}//GEN-LAST:event_representationClassChanged

    private void selectClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectClassButtonActionPerformed
    fireChange();
    
}//GEN-LAST:event_selectClassButtonActionPerformed

    private void mouseClickHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClickHandler

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final ElementHandle<TypeElement> handle = TypeElementFinder.find(Util.getClasspathInfo(project), new TypeElementFinder.Customizer() {

                            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {
                                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
                            }

                            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                                return true;
                            }
                        });

                if (handle != null) {
                    representationClassTextField.setText(handle.getQualifiedName());
                    fireChange();
                }
            }
        });
}//GEN-LAST:event_mouseClickHandler

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed

    private void classTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_classTextFieldKeyReleased
        resourceClassNameOveridden = true;
        fireChange();
    }//GEN-LAST:event_classTextFieldKeyReleased

    private void packageComboBoxKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_packageComboBoxKeyReleased
        fireChange();
    }//GEN-LAST:event_packageComboBoxKeyReleased

    private void packageChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageChanged
        fireChange();
    }//GEN-LAST:event_packageChanged
                
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classTextField;
    private javax.swing.JLabel containerLabel;
    private javax.swing.JTextField containerRepresentationClassTextField;
    private javax.swing.JTextField containerTextField;
    private javax.swing.JLabel containerUriLabel;
    private javax.swing.JTextField containerUriTextField;
    private javax.swing.JLabel contentClassLabel;
    private javax.swing.JLabel contentClassLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox medaTypeComboBox;
    private javax.swing.JLabel mediaTypeLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JTextField representationClassTextField;
    private javax.swing.JLabel resourceNameLabel;
    private javax.swing.JTextField resourceNameTextField;
    private javax.swing.JButton selectClassButton;
    private javax.swing.JButton selectClassButton1;
    private javax.swing.JLabel uriLabel;
    private javax.swing.JTextField uriTextField;
    // End of variables declaration//GEN-END:variables
    
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    public void fireChange() {
        ChangeEvent event =  new ChangeEvent(this);
        
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
    
    public boolean valid(WizardDescriptor wizard) {
        AbstractPanel.clearErrorMessage(wizard);
        String resourceName = resourceNameTextField.getText().trim();
        String packageName = getPackage();
        String className = classTextField.getText().trim();
        String containerName = containerTextField.getText().trim();
        SourceGroup[] groups = SourceGroupSupport.getJavaSourceGroups(project);
        
        if (groups == null || groups.length < 1) {
            AbstractPanel.setErrorMessage(wizard, "MSG_NoJavaSourceRoots");
            return false;
        } else if (resourceName.length() == 0 || ! Utilities.isJavaIdentifier(resourceName)) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidResourceName");
            return false;
        } else if (className.equals(containerName)) {
            AbstractPanel.setErrorMessage(wizard, "MSG_ClassNameEqualsContainerClassName");
            return false;
        }  else if (className.length() == 0 || ! Utilities.isJavaIdentifier(className)) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidResourceClassName");
            return false;
        } else if (containerName.length() == 0 || ! Utilities.isJavaIdentifier(containerName)) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidContainerResourceClassName");
            return false;
        } else if (uriTextField.getText().trim().length() == 0) {
            AbstractPanel.setErrorMessage(wizard, "MSG_EmptyUriTemplate");
            return false;
        } else if (!Util.isValidUri(uriTextField.getText().trim())) {
            AbstractPanel.setErrorMessage(wizard, "MSG_IncorrectUriTemplate");
            return false;
        } else if (containerUriTextField.getText().trim().length() == 0) {
            AbstractPanel.setErrorMessage(wizard, "MSG_EmptyContainerUriTemplate");
            return false;
        } else if (!Util.isValidUri(containerUriTextField.getText().trim())) {
            AbstractPanel.setErrorMessage(wizard, "MSG_IncorrectUriTemplate");
            return false;
        } else if (! Util.isValidPackageName(packageName)) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidPackageName");
            return false;
        } else if (getResourceClassFile() != null) {
            AbstractPanel.setErrorMessage(wizard, "MSG_ExistingClass");
            return false;
        } else if (getContainerClassFile() != null) {
            AbstractPanel.setErrorMessage(wizard, "MSG_ExistingClass");
            return false;
        }
        return true;
    }

    public SourceGroup getLocationValue() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    public String getPackage() {
        return ((JTextComponent)packageComboBox.getEditor().getEditorComponent()).getText();
    }

    private void locationChanged() {
        updateSourceGroupPackages();
        fireChange();
    }
    
    private String getResourceName() {
        return resourceNameTextField.getText();
    }
    
    private String getResourceClassName() {
        return classTextField.getText();
    }
    
    private FileObject getResourceClassFile() {
        FileObject folder = null;
        try {
            folder = SourceGroupSupport.getFolderForPackage(getLocationValue(), getPackage());
            if (folder != null) {
                return folder.getFileObject(getResourceClassName(), Constants.JAVA_EXT);
            }
        } catch(IOException ex) {
            //OK just return null
        }
        return null;
    }
    
    private String getContainerClassName() {
        return containerTextField.getText();
    }
    
    private FileObject getContainerClassFile() {
        FileObject folder = null;
        try {
            folder = SourceGroupSupport.getFolderForPackage(getLocationValue(), getPackage());
            if (folder != null) {
                return folder.getFileObject(getContainerClassName(), Constants.JAVA_EXT);
            }
        } catch(IOException ex) {
            //OK just return null
        }
        return null;
    }
    
    public static final String DEFAULT_RESOURCE_NAME = "Item";
    
    public void read(WizardDescriptor settings) {
        project = Templates.getProject(settings);
        FileObject targetFolder = Templates.getTargetFolder(settings);
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        SourceGroupUISupport.connect(locationComboBox, sourceGroups);

        packageComboBox.setRenderer(PackageView.listRenderer());

        updateSourceGroupPackages();

        // set default source group and package cf. targetFolder
        if (targetFolder != null) {
            SourceGroup targetSourceGroup = SourceGroupSupport.findSourceGroupForFile(sourceGroups, targetFolder);
            if (targetSourceGroup != null) {
                locationComboBox.setSelectedItem(targetSourceGroup);
                String targetPackage = SourceGroupSupport.getPackageForFolder(targetSourceGroup, targetFolder);
                if (targetPackage != null) {
                    ((JTextComponent)packageComboBox.getEditor().getEditorComponent()).setText(targetPackage);
               }
            }
        } else {
            String targetPackage = (String) settings.getProperty(WizardProperties.TARGET_PACKAGE);
            if (targetPackage != null) {
                ((JTextComponent) packageComboBox.getEditor().getEditorComponent()).setText(targetPackage);
            }
        }
        
           String value = (String) settings.getProperty(WizardProperties.ITEM_RESOURCE_NAME);
        if (value == null || value.trim().length() == 0) { // first time
            resourceNameTextField.setText(DEFAULT_RESOURCE_NAME);
            String containerName = Util.pluralize(getResourceName());

            classTextField.setText(findFreeClassName(getResourceName()));
            if (isClientControlledPattern(settings)) {
                uriTextField.setText("{name}"); //NOI18N
            } else {
                uriTextField.setText("{id}"); //NOI18N
            }
            containerTextField.setText(findFreeClassName(containerName));
            containerUriTextField.setText("/"+containerName);
            representationClassTextField.setText(GenericResourceBean.
                getDefaultRepresetationClass((MimeType)medaTypeComboBox.getSelectedItem()));
            containerRepresentationClassTextField.setText(GenericResourceBean.
                getDefaultRepresetationClass((MimeType)medaTypeComboBox.getSelectedItem()));
            uriTextField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    fireChange();
                }
                public void insertUpdate(DocumentEvent e) {
                    fireChange();
                }
                public void removeUpdate(DocumentEvent e) {
                    fireChange();
                }
            });
            containerUriTextField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    containerUriOveridden = true;
                    fireChange();
                }
                public void insertUpdate(DocumentEvent e) {
                    containerUriOveridden = true;
                    fireChange();
                }
                public void removeUpdate(DocumentEvent e) {
                    containerUriOveridden = true;
                    fireChange();
                }
            });
        } else {
            resourceNameTextField.setText(value);
            classTextField.setText((String) settings.getProperty(WizardProperties.ITEM_RESOURCE_CLASS));
            uriTextField.setText((String) settings.getProperty(WizardProperties.ITEM_RESOURCE_URI));
            medaTypeComboBox.setSelectedItem(((MimeType[]) settings.getProperty(WizardProperties.ITEM_MIME_TYPES))[0]);
            String[] types = (String[]) settings.getProperty(WizardProperties.ITEM_REPRESENTATION_TYPES);
            if (types != null && types.length > 0) {
                representationClassTextField.setText(types[0]);
            }
            containerTextField.setText((String) settings.getProperty(WizardProperties.CONTAINER_RESOURCE_CLASS));
            containerUriTextField.setText((String) settings.getProperty(WizardProperties.CONTAINER_RESOURCE_URI));
            types = (String[]) settings.getProperty(WizardProperties.CONTAINER_REPRESENTATION_TYPES);
            if (types != null && types.length > 0) {
                containerRepresentationClassTextField.setText(types[0]);
            }
        }
        
    }
    
    public void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.RESOURCE_PACKAGE, getPackage());
        settings.putProperty(WizardProperties.ITEM_RESOURCE_NAME, resourceNameTextField.getText());
        settings.putProperty(WizardProperties.ITEM_RESOURCE_CLASS, classTextField.getText());
        settings.putProperty(WizardProperties.ITEM_RESOURCE_URI, uriTextField.getText());
        settings.putProperty(WizardProperties.CONTAINER_RESOURCE_CLASS, containerTextField.getText());
        settings.putProperty(WizardProperties.CONTAINER_RESOURCE_URI, containerUriTextField.getText());
        settings.putProperty(WizardProperties.ITEM_MIME_TYPES, new MimeType[] { (MimeType) medaTypeComboBox.getSelectedItem() });
        settings.putProperty(WizardProperties.SOURCE_GROUP, getLocationValue());


        String type = representationClassTextField.getText();
        if (type != null && type.length() > 0) {
            settings.putProperty(WizardProperties.ITEM_REPRESENTATION_TYPES, new String[] { representationClassTextField.getText()} );
        }
        type = containerRepresentationClassTextField.getText();
        if (type != null && type.length() > 0) {
            settings.putProperty(WizardProperties.CONTAINER_REPRESENTATION_TYPES, new String[] { containerRepresentationClassTextField.getText()} );
        }
        
        try {            
            FileObject packageFO = SourceGroupSupport.getFolderForPackage(getLocationValue(), getPackage(), false);

            if (packageFO != null) {
                Templates.setTargetFolder(settings, packageFO);
            } else {
                Templates.setTargetFolder(settings, null);
                settings.putProperty(WizardProperties.TARGET_PACKAGE, getPackage());
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public SourceGroup getSourceGroup() {
        return (SourceGroup) locationComboBox.getSelectedItem();
    }
    
    @Override
    public String getPackageName() {
        return ((JTextComponent) packageComboBox.getEditor().getEditorComponent()).getText();
    }

    public double getRenderedHeight(){
        return selectClassButton1.getLocation().getY()+
                selectClassButton1.getSize().getHeight()+getGap();
    }
    
    private double getGap(){
        double gap = containerRepresentationClassTextField.getLocation().getY();
        gap = gap - (representationClassTextField.getLocation().getY() +representationClassTextField.getHeight());
        return gap;
    }

    private void updateSourceGroupPackages() {
        SourceGroup sourceGroup = (SourceGroup)locationComboBox.getSelectedItem();
        if (sourceGroup != null) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSelectedItem()!= null && model.getSelectedItem().toString().startsWith("META-INF")
                    && model.getSize() > 1) { // NOI18N
                model.setSelectedItem(model.getElementAt(1));
            }
            packageComboBox.setModel(model);
        }
    }
    
    private String findFreeClassName(String uri) {
        try {
            FileObject folder = SourceGroupSupport.getFolderForPackage(getLocationValue(), getPackage());
            if (folder != null) {
                return FileUtil.findFreeFileName(folder, Util.deriveResourceClassName(uri), Constants.JAVA_EXT);
            }
        } catch (IOException ex) {
            //OK just return null
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    static boolean isClientControlledPattern(WizardDescriptor settings) {
        Pattern p = (Pattern) settings.getProperty(WizardProperties.PATTERN_SELECTION);
        return p == Pattern.CLIENTCONTROLLED;
    }
}
