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

package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.swing.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;

/*
 * UseSuperTypePanel.java
 *
 * Created on June 20, 2005
 *
 * @author  Bharath Ravi Kumar
 */

/**
 * The panel for the use super type refactoring
 */
public class UseSuperTypePanel extends JPanel implements CustomRefactoringPanel {
    
    private final UseSuperTypeRefactoring refactoring;
    /**
     * Creates new form UseSuperTypePanel
     * @param refactoring The use super type refactoring that is
     * used by this panel
     * @param className  
     */
    public UseSuperTypePanel(UseSuperTypeRefactoring refactoring, String className) {
        this.refactoring = refactoring;
        initComponents();
        TreePathHandle subType = (TreePathHandle) refactoring.getTypeElement();
        String title = null;
        title = NbBundle.getMessage(UseSuperTypePanel.class, "LBL_UseSyperTypeTitle", className);
        setName(title);
        superTypeList.setCellRenderer(new DefaultListCellRenderer() {
            
            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                
                super.getListCellRendererComponent(list,
                        ((ElementHandle)value).getBinaryName(), index,
                        isSelected, cellHasFocus);
                
                if (value instanceof ElementHandle) {
                    Icon i = ElementIcons.getElementIcon(((ElementHandle) value).getKind(), Collections.singleton(Modifier.PUBLIC));
                    setIcon(i);
                }
                return this;
                
            }
        });
        superTypeList.setModel(new DefaultComboBoxModel(refactoring.getCandidateSuperTypes()));
        superTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        superTypeList.setSelectedIndex(0);
    }

    @Override
    public boolean requestFocusInWindow() {
        superTypeList.requestFocusInWindow();
        return true;
    }
    
    @Override
    public void initialize() {
    }
    
    @Override
    public Component getComponent(){
        return this;
    }
    /**
     * Returns the target super type to be used
     * @return The target super type Object
     */
    public ElementHandle getSuperType(){
        return (ElementHandle) superTypeList.getSelectedValue();
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
        listScrollPane = new javax.swing.JScrollPane();
        superTypeList = new javax.swing.JList();

        setMaximumSize(new java.awt.Dimension(600, 500));
        setPreferredSize(new java.awt.Dimension(300, 200));
        setLayout(new java.awt.GridBagLayout());

        label.setLabelFor(superTypeList);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getBundle(UseSuperTypePanel.class).getString("LBL_UseSuperType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(label, gridBagConstraints);
        label.getAccessibleContext().setAccessibleDescription("N/A");

        superTypeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listScrollPane.setViewportView(superTypeList);
        superTypeList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UseSuperTypePanel.class, "ACSD_SupertypeToUse")); // NOI18N
        superTypeList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseSuperTypePanel.class, "ACSD_SupertypeToUseDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(listScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JList superTypeList;
    // End of variables declaration//GEN-END:variables
    
}
