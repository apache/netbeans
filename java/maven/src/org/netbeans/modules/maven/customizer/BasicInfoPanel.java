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

package org.netbeans.modules.maven.customizer;

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.ReflectionTextComponentUpdater;
import org.netbeans.modules.maven.api.customizer.support.TextComponentUpdater;
import static org.netbeans.modules.maven.customizer.Bundle.*;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;

/**
 *
 * @author  mkleint
 */
public class BasicInfoPanel extends javax.swing.JPanel implements DocumentListener, HelpCtx.Provider {
    private final ModelHandle2 handle;
    private List<TextComponentUpdater> listeners;
    private final Category category;
    private final org.netbeans.api.project.Project prj;
    
    /** Creates new form BasicInfoPanel */
    public BasicInfoPanel(ModelHandle2 handle, Category category, org.netbeans.api.project.Project prj) {
        initComponents();
        this.handle = handle;
        this.category = category;
        this.prj = prj;
        initValues();
    }
    
    private void initValues() {
        Project mdl = handle.getPOMModel().getProject();
        MavenProject project;
        try {
            NbMavenProjectImpl nbi = prj.getLookup().lookup(NbMavenProjectImpl.class);
            assert nbi != null;
            project = nbi.loadParentOf(EmbedderFactory.getProjectEmbedder(), handle.getProject());      
            if (project != null && NbMavenProject.isErrorPlaceholder(project)) {
                project = null;
            }
        } catch (ProjectBuildingException ex) {
            project = null;
        }
        listeners = new ArrayList<TextComponentUpdater>();
        try {
            listeners.add(new ReflectionTextComponentUpdater("getGroupId", mdl, project, txtGroupId, lblGroupId, handle, new ReflectionTextComponentUpdater.Operation() {
                @Override
                public void performOperation(POMModel model) {
                    model.getProject().setGroupId(getNewValue());
                }
            })); //NOI18N
            listeners.add(new ReflectionTextComponentUpdater("getArtifactId",  mdl, project, txtArtifactId, lblArtifactId, handle, new ReflectionTextComponentUpdater.Operation() {
                @Override
                public void performOperation(POMModel model) {
                    model.getProject().setArtifactId(getNewValue());
                }
            })); //NOI18N
            listeners.add(new ReflectionTextComponentUpdater("getVersion",  mdl, project, txtVersion, lblVersion, handle, new ReflectionTextComponentUpdater.Operation() {
                @Override
                public void performOperation(POMModel model) {
                    model.getProject().setVersion(getNewValue());
                }
            })); //NOI18N
            listeners.add(new ReflectionTextComponentUpdater("getName",  mdl, project, txtName, lblName, handle, new ReflectionTextComponentUpdater.Operation() {
                @Override
                public void performOperation(POMModel model) {
                    model.getProject().setName(getNewValue());
                }
            })); //NOI18N
            //in this case referring to parent project for default value is wrong. packaging
            //doesn't get inherited from parent. the default value is jar no matter what the parent has.
            listeners.add(new ReflectionTextComponentUpdater("getPackaging",  mdl, project, txtPackaging, lblPackaging, handle, new ReflectionTextComponentUpdater.Operation() {
                @Override
                public void performOperation(POMModel model) {
                    model.getProject().setPackaging(getNewValue());
                }
            })); //NOI18N
            listeners.add(new ReflectionTextComponentUpdater("getDescription",  mdl, project, taDescription, lblDescription, handle, new ReflectionTextComponentUpdater.Operation() {
                @Override
                public void performOperation(POMModel model) {
                    model.getProject().setDescription(getNewValue());
                }
            })); //NOI18N
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        txtGroupId.getDocument().addDocumentListener(this);
        txtArtifactId.getDocument().addDocumentListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblPackaging = new javax.swing.JLabel();
        txtPackaging = new javax.swing.JTextField();
        lblDescription = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taDescription = new javax.swing.JTextArea();

        lblGroupId.setLabelFor(txtGroupId);
        org.openide.awt.Mnemonics.setLocalizedText(lblGroupId, org.openide.util.NbBundle.getMessage(BasicInfoPanel.class, "LBL_GroupId")); // NOI18N

        lblArtifactId.setLabelFor(txtArtifactId);
        org.openide.awt.Mnemonics.setLocalizedText(lblArtifactId, org.openide.util.NbBundle.getMessage(BasicInfoPanel.class, "LBL_ArtifactId")); // NOI18N

        lblVersion.setLabelFor(txtVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblVersion, org.openide.util.NbBundle.getMessage(BasicInfoPanel.class, "LBL_Version")); // NOI18N

        lblName.setLabelFor(txtName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(BasicInfoPanel.class, "LBL_Name")); // NOI18N

        lblPackaging.setLabelFor(txtPackaging);
        org.openide.awt.Mnemonics.setLocalizedText(lblPackaging, org.openide.util.NbBundle.getMessage(BasicInfoPanel.class, "LBL_Packaging")); // NOI18N

        lblDescription.setLabelFor(taDescription);
        org.openide.awt.Mnemonics.setLocalizedText(lblDescription, org.openide.util.NbBundle.getMessage(BasicInfoPanel.class, "LBL_Description")); // NOI18N

        taDescription.setColumns(20);
        taDescription.setRows(5);
        jScrollPane1.setViewportView(taDescription);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGroupId)
                    .addComponent(lblArtifactId)
                    .addComponent(lblVersion)
                    .addComponent(lblPackaging)
                    .addComponent(lblName)
                    .addComponent(lblDescription))
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(txtVersion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(txtPackaging, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGroupId, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblArtifactId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPackaging, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPackaging))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                    .addComponent(lblDescription))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPackaging;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JTextArea taDescription;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPackaging;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables

    @Override
    public void insertUpdate(DocumentEvent arg0) {
        checkCoords();
    }

    @Override
    public void removeUpdate(DocumentEvent arg0) {
        checkCoords();
    }

    @Override
    public void changedUpdate(DocumentEvent arg0) {
        checkCoords();
    }

    private void checkCoords() {
        boolean isValid = checkCoord(txtGroupId);
        if (isValid) {
            isValid = checkCoord(txtArtifactId);
        }
        category.setValid(isValid);
    }

    @Messages("ERR_Coord_breaks_pom=Error: Group Id or Artifact Id would invalidate Maven POM xml file.")
    private boolean checkCoord(JTextField field) {
        String coord = field.getText();
        boolean result = false;
        try {
            String escaped = XMLUtil.toAttributeValue(coord);
            result = escaped.length() == coord.length() && coord.indexOf(">") == -1
                    && coord.indexOf(" ") == -1;
        } catch (CharConversionException ex) {
            // ignore this one
        }
        if (result) {
            result = !containsMultiByte(coord);
        } else {
            category.setErrorMessage(ERR_Coord_breaks_pom());
        }

        if (result) {
            category.setErrorMessage(null);
        }

        return result;
    }

    @Messages("ERR_multibyte=Error: Multibyte chars forbidden in Group Id and Artifact Id.")
    boolean containsMultiByte (String text) {
        char[] textChars = text.toCharArray();
        for (int i = 0; i < textChars.length; i++) {
            if ((int)textChars[i] > 255) {
                category.setErrorMessage(ERR_multibyte());
                return true;
            }

        }
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return CustomizerProviderImpl.HELP_CTX;
    }
    
}
