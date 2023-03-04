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

package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.GridBagConstraints;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.editor.codegen.ConstructorGenerator;
import org.openide.util.NbBundle;

/**
 *
 * @author  Dusan Balek
 */
public class ConstructorPanel extends JPanel {
    
    private JLabel constructorSelectorLabel;
    private ElementSelectorPanel constructorSelector;
    private JLabel fieldSelectorLabel;    
    private ElementSelectorPanel fieldSelector;
    
    /** Creates new form ConstructorPanel */
    public ConstructorPanel(ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription) {
        initComponents();
        if (constructorDescription != null) {
            constructorSelectorLabel = new javax.swing.JLabel();
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
            add(constructorSelectorLabel, gridBagConstraints);
            constructorSelector = new ElementSelectorPanel(constructorDescription, false);
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
            add(constructorSelector, gridBagConstraints);
            constructorSelectorLabel.setText(NbBundle.getMessage(ConstructorGenerator.class, "LBL_super_constructor_select")); //NOI18N
            constructorSelectorLabel.setLabelFor(constructorSelector);
        }
        if (fieldsDescription != null) {
            fieldSelectorLabel = new javax.swing.JLabel();
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets =new java.awt.Insets(12, constructorDescription != null ? 0 : 12, 6, 12);
            add(fieldSelectorLabel, gridBagConstraints);
            fieldSelector = new ElementSelectorPanel(fieldsDescription, false);
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, constructorDescription != null ? 0 : 12, 0, 12);
            add(fieldSelector, gridBagConstraints);
            fieldSelectorLabel.setText(NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor_select")); //NOI18N
            fieldSelectorLabel.setLabelFor(fieldSelector);
        }
	
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ConstructorGenerator.class, "A11Y_Generate_Constructor"));
    }
    
    public ElementHandle<? extends Element> getInheritedConstructor() {
        if (constructorSelector == null)
            return null;
        List<ElementHandle<? extends Element>> handles = constructorSelector.getSelectedElements();
        return (handles.size() == 1 ? handles.get(0) : null );
    }

    public List<ElementHandle<? extends Element>> getInheritedConstructors() {
        if (constructorSelector == null)
            return Collections.EMPTY_LIST;
        return constructorSelector.getSelectedElements();
    }
    
    public List<ElementHandle<? extends Element>> getVariablesToInitialize() {
        if (fieldSelector == null)
            return null;
        return ((ElementSelectorPanel)fieldSelector).getSelectedElements();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
