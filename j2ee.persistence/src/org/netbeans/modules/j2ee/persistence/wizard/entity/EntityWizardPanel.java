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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import java.awt.Dimension;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class EntityWizardPanel extends javax.swing.JPanel {
    
    private Project project;
    private FileObject targetFolder;
    private ChangeListener listener;
    //private PersistenceUnit persistenceUnit;
    
    static final String IS_VALID = "EntityWizardPanel_isValid"; //NOI18N
    private boolean createPU;
    
    public EntityWizardPanel(ChangeListener changeListener) {
        this.setProject(project);
        this.listener = changeListener;
        initComponents();
        createPUCheckbox.setSelected(true);
        
        primaryKeyTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
        });
        
    }
    
    public String getPrimaryKeyClassName() {
        return primaryKeyTextField.getText();
    }
    

    void setPersistenceUnitButtonVisibility(boolean visible) {
        createPUCheckbox.setVisible(visible);
        //createPUCheckBox.setSelected(visible);
        updateWarning();
    }

    private void updateWarning(){
        String warning=null;
        try {
            if(createPUCheckbox.isVisible() && !createPU && !(ProviderUtil.persistenceExists(project, targetFolder) || !ProviderUtil.isValidServerInstanceOrNone(project))){
                warning = NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_NoPersistenceUnit");
            }
        } catch (InvalidPersistenceXmlException ex) {
            warning = NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_InvalidPersistenceXml", ex.getPath());//NOI18N
        } catch (RuntimeException ex) {
            warning = NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_InvalidPersistenceXml", ex.getMessage());//NOI18N
        }
        Icon icon = null;
        if (warning != null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/j2ee/persistence/ui/resources/warning.gif", false);//NOI18N
        } else {
            warning = " ";
        }
        createPUWarningLabel.setIcon(icon);
        createPUWarningLabel.setText(warning);
        createPUWarningLabel.setToolTipText(warning);
    }
    
    void setProject(Project project) {
        this.project = project;
    }

    void setTargetFolder(FileObject folder) {
        this.targetFolder = folder;
    }

//    public PersistenceUnit getPersistenceUnit() {
//        return persistenceUnit;
//    }

    public boolean isCreatePU(){
        return createPUCheckbox.isVisible() && createPU;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        persistenceGroup = new javax.swing.ButtonGroup();
        accessTypeGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        primaryKeyTextField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        createPUWarningLabel = new ShyLabel();
        createPUCheckbox = new javax.swing.JCheckBox();

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/entity/Bundle").getString("MN_PrimaryKeyType").charAt(0));
        jLabel1.setLabelFor(primaryKeyTextField);
        jLabel1.setText(org.openide.util.NbBundle.getBundle(EntityWizardPanel.class).getString("LBL_PrimaryKeyClass")); // NOI18N

        primaryKeyTextField.setText("Long");

        searchButton.setText("...");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        createPUWarningLabel.setText(" ");

        org.openide.awt.Mnemonics.setLocalizedText(createPUCheckbox, org.openide.util.NbBundle.getMessage(EntityWizardPanel.class, "LBL_CreatePersistenceUnit")); // NOI18N
        createPUCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createPUCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createPUCheckboxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(primaryKeyTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchButton))
            .addComponent(createPUWarningLabel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(createPUCheckbox)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(searchButton)
                    .addComponent(primaryKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addComponent(createPUWarningLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createPUCheckbox))
        );

        primaryKeyTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityWizardPanel.class, "LBL_PrimaryKeyClass")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ElementHandle<TypeElement> handle = TypeElementFinder.find(null, new TypeElementFinder.Customizer() {

                    @Override
                            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {
                                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
                            }

                    @Override
                            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                                //XXX not all types are supported as identifiers by the jpa spec, but 
                                // leaving unrestricted for now since different persistence providers 
                                // might support more types
                                return true;
                            }
                        });

                if (handle != null) {
                    primaryKeyTextField.setText(handle.getQualifiedName());
                }
            }
        });
    }//GEN-LAST:event_searchButtonActionPerformed
    
    private void createPUCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createPUCheckboxItemStateChanged
        this.createPU = createPUCheckbox.isVisible() && createPUCheckbox.isSelected();
        updateWarning();
        listener.stateChanged(null);
    }//GEN-LAST:event_createPUCheckboxItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessTypeGroup;
    private javax.swing.JCheckBox createPUCheckbox;
    private javax.swing.JLabel createPUWarningLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.ButtonGroup persistenceGroup;
    private javax.swing.JTextField primaryKeyTextField;
    private javax.swing.JButton searchButton;
    // End of variables declaration//GEN-END:variables
    
    /**
     * A crude attempt at a label which doesn't expand its parent.
     */
    private static final class ShyLabel extends JLabel {

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width = 0;
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            size.width = 0;
            return size;
        }
    }
}
