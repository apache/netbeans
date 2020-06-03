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
package org.netbeans.modules.cnd.refactoring.codegen.ui;

import java.awt.GridBagConstraints;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.codegen.ConstructorGenerator;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 */
public class ConstructorPanel extends JPanel {

    private JLabel constructorSelectorLabel;
    private SuperConstructorSelectorPanel constructorSelector;
    private JLabel fieldSelectorLabel;
    private ElementSelectorPanel fieldSelector;

    /** Creates new form ConstructorPanel */
    public ConstructorPanel(ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription) {
        initComponents();
        if (fieldsDescription != null) {
            fieldSelectorLabel = new javax.swing.JLabel();
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
            add(fieldSelectorLabel, gridBagConstraints);
            fieldSelector = new ElementSelectorPanel(fieldsDescription, false, true, false);
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
            add(fieldSelector, gridBagConstraints);
            Mnemonics.setLocalizedText(fieldSelectorLabel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor_select")); //NOI18N
            fieldSelectorLabel.setLabelFor(fieldSelector);
            fieldSelector.doInitialExpansion(1);
        }
        if (constructorDescription != null) {
            constructorSelectorLabel = new javax.swing.JLabel();
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(12, fieldsDescription != null ? 0 : 12, 6, 12);
            add(constructorSelectorLabel, gridBagConstraints);
            constructorSelector = new SuperConstructorSelectorPanel(constructorDescription);
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, fieldsDescription != null ? 0 : 12, 0, 12);
            add(constructorSelector, gridBagConstraints);
            Mnemonics.setLocalizedText(constructorSelectorLabel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_super_constructor_select")); //NOI18N
            constructorSelectorLabel.setLabelFor(constructorSelector);
        }
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ConstructorGenerator.class, "A11Y_Generate_Constructor"));
    }

    public CsmConstructor getInheritedConstructor() {
        if (constructorSelector == null) {
            return null;
        }
        List<CsmDeclaration> handles = constructorSelector.getSelectedElements();
        return (handles.size() == 1 ? (CsmConstructor) handles.get(0) : null);
    }

    public List<CsmConstructor> getInheritedConstructors() {
        if (constructorSelector == null) {
            return Collections.<CsmConstructor>emptyList();
        }
        List<?> decls = constructorSelector.getSelectedElements();
        // we know that list contains only constructors
        @SuppressWarnings("unchecked")
        List<CsmConstructor> ctrs = (List<CsmConstructor>) decls;
        return ctrs;
    }

    public List<CsmField> getVariablesToInitialize() {
        if (fieldSelector == null) {
            return Collections.<CsmField>emptyList();
        }
        List<?> decls = fieldSelector.getSelectedElements();
        // we know that list contains only fields
        @SuppressWarnings("unchecked")
        List<CsmField> fields = (List<CsmField>) decls;
        return fields;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
