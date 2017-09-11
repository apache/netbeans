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
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;


/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class InnerToOuterPanel extends JPanel implements CustomRefactoringPanel {
    // refactoring this panel provides parameters for
    private final InnerToOuterRefactoring refactoring;
    private final ChangeListener parent;
    private boolean disableDeclareFields;
    private boolean initialized = false;
    
    /** Creates new form InnerToOuterPanel
     * @param refactoring The refactoring this panel provides parameters for.
     */
    public InnerToOuterPanel(InnerToOuterRefactoring refactoring, final ChangeListener parent, boolean disableDeclareFields) {
        this.refactoring = refactoring;
        this.parent = parent;
        initComponents();
        setPreferredSize(new Dimension(300, 130));
        this.disableDeclareFields = disableDeclareFields;
    }
    
    @Override
    public Component getComponent() {
        return this;
    }

    /** Initialization of the panel (called by the parent window).
     */
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                classNameField.setText(refactoring.getClassName());
                if (disableDeclareFields) {
                    fieldCheckBox.setEnabled(false);
                } else if (refactoring.getReferenceName() != null) {
                    fieldNameField.setText(refactoring.getReferenceName());
                    fieldCheckBox.setSelected(true);
                }
                DocumentListener dl = new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent event) {
                        parent.stateChanged(null);
                    }
                    @Override
                    public void insertUpdate(DocumentEvent event) {
                        parent.stateChanged(null);
                    }
                    @Override
                    public void removeUpdate(DocumentEvent event) {
                        parent.stateChanged(null);
                    }
                };
                classNameField.getDocument().addDocumentListener(dl);
                fieldNameField.getDocument().addDocumentListener(dl);
                classNameField.selectAll();
                classNameField.requestFocusInWindow();
            }
        });
        initialized = true;
    }
    
    // --- GETTERS FOR REFACTORING PARAMETERS ----------------------------------
    
    /** Getter used by the refactoring UI to get value
     * of target type.
     * @return Target type.
     */
    public String getClassName() {
        return classNameField.getText();
    }
    
    public String getReferenceName() {
        if (fieldCheckBox.isSelected()) {
            return fieldNameField.getText();
        } else {
            return null;
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        classNameField.requestFocusInWindow();
        return true;
    }
    
    // --- GENERATED CODE ------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataPanel = new javax.swing.JPanel();
        classNameLabel = new javax.swing.JLabel();
        classNameField = new javax.swing.JTextField();
        fieldPanel = new javax.swing.JPanel();
        fieldCheckBox = new javax.swing.JCheckBox();
        fieldNamePanel = new javax.swing.JPanel();
        fieldNameLabel = new javax.swing.JLabel();
        fieldNameField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.BorderLayout());

        dataPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        dataPanel.setLayout(new java.awt.BorderLayout(12, 0));

        classNameLabel.setLabelFor(classNameField);
        org.openide.awt.Mnemonics.setLocalizedText(classNameLabel, org.openide.util.NbBundle.getBundle(InnerToOuterPanel.class).getString("LBL_InnerToOuter_ClassName")); // NOI18N
        dataPanel.add(classNameLabel, java.awt.BorderLayout.WEST);
        dataPanel.add(classNameField, java.awt.BorderLayout.CENTER);
        classNameField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "ACSD_nameField")); // NOI18N
        classNameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "ACSD_nameField")); // NOI18N

        fieldPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        fieldPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(fieldCheckBox, org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "LBL_InnerToOuter_DeclareField")); // NOI18N
        fieldCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 4, 0));
        fieldCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fieldCheckBoxItemStateChanged(evt);
            }
        });
        fieldPanel.add(fieldCheckBox, java.awt.BorderLayout.NORTH);
        fieldCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "ACSD_DeclareFieldName")); // NOI18N
        fieldCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "ACSD_DeclareFieldDescription")); // NOI18N

        fieldNamePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 32, 0, 0));
        fieldNamePanel.setLayout(new java.awt.BorderLayout(12, 0));

        fieldNameLabel.setLabelFor(fieldNameField);
        org.openide.awt.Mnemonics.setLocalizedText(fieldNameLabel, org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "LBL_InnerToOuter_FieldName")); // NOI18N
        fieldNameLabel.setEnabled(false);
        fieldNamePanel.add(fieldNameLabel, java.awt.BorderLayout.WEST);
        fieldNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "ACSD_DeclareFieldName")); // NOI18N

        fieldNameField.setEnabled(false);
        fieldNamePanel.add(fieldNameField, java.awt.BorderLayout.CENTER);
        fieldNameField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "ACSD_DeclareFieldName")); // NOI18N
        fieldNameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InnerToOuterPanel.class, "ACSD_DeclareFieldDescription")); // NOI18N

        fieldPanel.add(fieldNamePanel, java.awt.BorderLayout.SOUTH);

        dataPanel.add(fieldPanel, java.awt.BorderLayout.SOUTH);

        add(dataPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void fieldCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fieldCheckBoxItemStateChanged
        boolean enable = evt.getStateChange() == ItemEvent.SELECTED;
        fieldNameField.setEnabled(enable);
        fieldNameLabel.setEnabled(enable);
        parent.stateChanged(null);
    }//GEN-LAST:event_fieldCheckBoxItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classNameField;
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JCheckBox fieldCheckBox;
    private javax.swing.JTextField fieldNameField;
    private javax.swing.JLabel fieldNameLabel;
    private javax.swing.JPanel fieldNamePanel;
    private javax.swing.JPanel fieldPanel;
    // End of variables declaration//GEN-END:variables
    
}
