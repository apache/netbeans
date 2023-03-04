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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator;
import org.openide.util.NbBundle;

/**
 *
 * @author  Dusan Balek
 */
public class EqualsHashCodePanel extends JPanel {

    private ElementSelectorPanel equalsSelector;
    private ElementSelectorPanel hashCodeSelector;
    
    private JLabel equalsLabel;
    private JLabel hashCodeLabel;

    /** Creates new form EqualsHashCodePanel */
    public EqualsHashCodePanel(ElementNode.Description description, boolean generateEquals, boolean generateHashCode) {
        assert generateEquals || generateHashCode;
        
        initComponents();
        
        GridBagConstraints gridBagConstraints;

        PropertyChangeListener pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        };

        if( generateEquals ) {
            equalsLabel = new JLabel(NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_equals_select")); //NOI18N

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
            add(equalsLabel, gridBagConstraints);

            equalsSelector = new ElementSelectorPanel(description, false);
            equalsSelector.getExplorerManager().addPropertyChangeListener(pcl);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
            add(equalsSelector, gridBagConstraints);
        
            equalsLabel.setLabelFor(equalsSelector);
        }

        if( generateHashCode ) {
            hashCodeLabel = new JLabel(NbBundle.getMessage(EqualsHashCodeGenerator.class, "LBL_hashcode_select")); //NOI18N

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.insets = new java.awt.Insets(12, generateEquals ? 0 : 12, 6, 12);
            add(hashCodeLabel, gridBagConstraints);

            hashCodeSelector = new ElementSelectorPanel( ElementNode.Description.deepCopy(description), false);
            hashCodeSelector.getExplorerManager().addPropertyChangeListener(pcl);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, generateEquals ? 0 : 12, 0, 12);
            add(hashCodeSelector, gridBagConstraints);
        
            hashCodeLabel.setLabelFor(hashCodeSelector);
        }
	
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EqualsHashCodeGenerator.class, "A11Y_Generate_EqualsHashCode"));
    }
    
    
    public List<ElementHandle<? extends Element>> getEqualsVariables() {
        if( null == equalsSelector )
            return null;
        return ((ElementSelectorPanel)equalsSelector).getSelectedElements();
    }

    public List<ElementHandle<? extends Element>> getHashCodeVariables() {
        if( null == hashCodeSelector )
            return null;
        return ((ElementSelectorPanel)hashCodeSelector).getSelectedElements();
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
