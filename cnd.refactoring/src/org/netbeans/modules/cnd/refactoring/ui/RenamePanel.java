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
package org.netbeans.modules.cnd.refactoring.ui;
import java.awt.Component;
import java.awt.event.ItemEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.RefactoringModule;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;


/**
 * Rename refactoring parameters panel
 *
 */
public class RenamePanel extends JPanel implements CustomRefactoringPanel {

    private final transient String oldName;
    private final transient ChangeListener parent;
    private final transient CsmObject origObject;
    
    /** Creates new form RenamePanelName */
    public RenamePanel(CsmObject origObject, String oldName, ChangeListener parent, String name, boolean editable, boolean showUpdateReferences) {
        setName(name);
        this.oldName = oldName;
        this.parent = parent;
        this.origObject = origObject;
        initComponents();
//        String labelText = "<html><font style=\"color: red\"><br>&nbsp;WARNING: This feature is in development and inaccurate!<br>" + //NOI18N
//                "&nbsp;Use Preview to check renamed objects</font></html>"; // NOI18N
//        jPanel1.add(new JLabel(labelText));
        updateReferencesCheckBox.setVisible(showUpdateReferences);
        nameField.setEnabled(editable);
        //parent.setPreviewEnabled(false);
        nameField.requestFocus();
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
                RenamePanel.this.parent.stateChanged(null);
            }
            @Override
            public void insertUpdate(DocumentEvent event) {
                RenamePanel.this.parent.stateChanged(null);
            }
            @Override
            public void removeUpdate(DocumentEvent event) {
                RenamePanel.this.parent.stateChanged(null);
            }
        });
    }
    
    @Override
    public boolean requestFocusInWindow() {
        nameField.requestFocusInWindow();
        return true;
    }
    
    private boolean initialized = false;
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        //put initialization code here
        initialized = true;
        try {
            CsmCacheManager.enter();
            CsmObject resolvedObject = CsmRefactoringUtils.getReferencedElement(this.origObject);
            final String objKindStr = getObjectKind(resolvedObject);   
            final String title = NbBundle.getMessage(RenamePanel.class, "LBL_RenamePanelTitle", objKindStr, oldName); // NOI18N

            final RenamePanel panel = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setName(title);
                }            
            });
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    private static String getObjectKind(CsmObject obj) {
        String kindName = "";
        if (obj != null) {
            if (CsmKindUtilities.isClass(obj)) {
                CsmDeclaration.Kind kind = ((CsmClass)obj).getKind();
                if (kind == CsmDeclaration.Kind.STRUCT) {
                    kindName = getString("LBL_Struct"); // NOI18N
                } else if (kind == CsmDeclaration.Kind.UNION) {
                    kindName = getString("LBL_Union"); // NOI18N
                } else {
                    assert kind == CsmDeclaration.Kind.CLASS : "unexpected kind " + kind;
                    kindName = getString("LBL_Class"); // NOI18N
                }
            } else if (CsmKindUtilities.isEnum(obj)) {
                kindName = getString("LBL_Enum"); // NOI18N
            } else if (CsmKindUtilities.isMacro(obj)) {
                kindName = getString("LBL_Macro"); // NOI18N
            } else if (CsmKindUtilities.isFile(obj)) {
                kindName = getString("LBL_File"); // NOI18N
            } else if (CsmKindUtilities.isField(obj)) {
                kindName = getString("LBL_Field"); // NOI18N
            } else if (CsmKindUtilities.isParamVariable(obj)) {
                kindName = getString("LBL_Parameter"); // NOI18N
            } else if (CsmKindUtilities.isEnumerator(obj)) {
                kindName = getString("LBL_Enumerator"); // NOI18N
            } else if (CsmKindUtilities.isVariable(obj)) {
                kindName = getString("LBL_Variable"); // NOI18N
            } else if (CsmKindUtilities.isNamespace(obj)) {
                kindName = getString("LBL_Namespace"); // NOI18N
            } else if (CsmKindUtilities.isConstructor(obj)) {
                kindName = getString("LBL_Constructor"); // NOI18N
            } else if (CsmKindUtilities.isMethod(obj)) {
                kindName = getString("LBL_Method"); // NOI18N
            } else if (CsmKindUtilities.isFunction(obj)) {
                kindName = getString("LBL_Function"); // NOI18N
            } else if (CsmKindUtilities.isTypedef(obj)) {
                kindName = getString("LBL_Typedef"); // NOI18N
            } else if (CsmKindUtilities.isTypeAlias(obj)) {
                kindName = getString("LBL_TypeAlias"); // NOI18N
            }
        }
        return kindName;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringUI.class, key);
    }
    
    @Override
    public void requestFocus() {
        nameField.requestFocus();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        textCheckBox = new javax.swing.JCheckBox();
        updateReferencesCheckBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(RenamePanel.class, "LBL_NewName")); // NOI18N
        add(label, new java.awt.GridBagConstraints());

        nameField.setText(oldName);
        nameField.selectAll();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(nameField, gridBagConstraints);
        nameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSD_nameField")); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        textCheckBox.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.rename", // NOI18N
            Boolean.FALSE)).booleanValue());
org.openide.awt.Mnemonics.setLocalizedText(textCheckBox, org.openide.util.NbBundle.getBundle(RenamePanel.class).getString("LBL_RenameComments")); // NOI18N
textCheckBox.setEnabled(false);
textCheckBox.addItemListener(new java.awt.event.ItemListener() {
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        textCheckBoxItemStateChanged(evt);
    }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(textCheckBox, gridBagConstraints);
    textCheckBox.getAccessibleContext().setAccessibleDescription(textCheckBox.getText());

    org.openide.awt.Mnemonics.setLocalizedText(updateReferencesCheckBox, org.openide.util.NbBundle.getBundle(RenamePanel.class).getString("LBL_RenameWithoutRefactoring")); // NOI18N
    updateReferencesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 0, 4));
    updateReferencesCheckBox.setMargin(new java.awt.Insets(2, 2, 0, 2));
    updateReferencesCheckBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            updateReferencesCheckBoxActionPerformed(evt);
        }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(updateReferencesCheckBox, gridBagConstraints);
    updateReferencesCheckBox.getAccessibleContext().setAccessibleDescription(updateReferencesCheckBox.getText());
    }// </editor-fold>//GEN-END:initComponents

    private void updateReferencesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateReferencesCheckBoxActionPerformed
//        textCheckBox.setEnabled(!updateReferencesCheckBox.isSelected());
        parent.stateChanged(null);
    }//GEN-LAST:event_updateReferencesCheckBoxActionPerformed

    private void textCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_textCheckBoxItemStateChanged
        // used for change default value for searchInComments check-box.                                                  
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption("searchInComments.rename", b); // NOI18N
    }//GEN-LAST:event_textCheckBoxItemStateChanged
                                                             
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel label;
    private javax.swing.JTextField nameField;
    private javax.swing.JCheckBox textCheckBox;
    private javax.swing.JCheckBox updateReferencesCheckBox;
    // End of variables declaration//GEN-END:variables

    public String getNameValue() {
        return nameField.getText();
    }
    
    public boolean searchInComments() {
        return textCheckBox.isSelected();
    }
    
    public boolean isUpdateReferences() {
        if (updateReferencesCheckBox.isVisible() && updateReferencesCheckBox.isSelected()) {
            return false;
        }
        return true;
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
