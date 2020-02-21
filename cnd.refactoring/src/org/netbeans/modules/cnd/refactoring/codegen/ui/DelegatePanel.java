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
package org.netbeans.modules.cnd.refactoring.codegen.ui;

import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.refactoring.codegen.DelegateMethodGenerator;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbBundle;

/**
 *
 */
public class DelegatePanel extends javax.swing.JPanel implements PropertyChangeListener {

    private JTextComponent component;
    private ElementSelectorPanel delegateSelector;
    private ElementSelectorPanel methodSelector;

    /** Creates new form DelegatePanel */
    public DelegatePanel(JTextComponent component, ElementNode.Description description) {
        this.component = component;
        initComponents();
        delegateSelector = new ElementSelectorPanel(description, false, true, true);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(delegateSelector, gridBagConstraints);
        delegateSelector.getExplorerManager().addPropertyChangeListener(DelegatePanel.this);
        
        methodSelector = new ElementSelectorPanel(null, false, true, true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(methodSelector, gridBagConstraints);
        
        delegateLabel.setText(NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_field_select")); //NOI18N
        delegateLabel.setLabelFor(delegateSelector);
        methodLabel.setText(NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_method_select")); //NOI18N
        methodLabel.setLabelFor(methodSelector);
        
        
        delegateSelector.doInitialExpansion(1);
	
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DelegateMethodGenerator.class, "A11Y_Generate_Delegate"));
    }

    public CsmField getDelegateField() {
        List<CsmDeclaration> handles = delegateSelector.getTreeSelectedElements();
        return handles.size() == 1 ? (CsmField)handles.get(0) : null;
    }

    public List<CsmDeclaration> getDelegateMethods() {
        return methodSelector.getSelectedElements();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        delegateLabel = new javax.swing.JLabel();
        methodLabel = new javax.swing.JLabel();

        setFocusable(false);
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(delegateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 12);
        add(methodLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ( ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ) {
            SwingUtilities.invokeLater(new Runnable() {                 
                @Override
                public void run() {
                    CsmField handle = getDelegateField();
                    methodSelector.setRootElement(handle == null ? null : DelegateMethodGenerator.getAvailableMethods(component, handle), false, true);
                    methodSelector.doInitialExpansion(-1);            
                }
            });
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel delegateLabel;
    public javax.swing.JLabel methodLabel;
    // End of variables declaration//GEN-END:variables
}
