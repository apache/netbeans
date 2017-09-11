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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.modules.j2ee.persistence.provider.Provider;

/**
 * Panel for adding new PU property
 * 
 * @author  psb
 */
public class PropertyPanel extends javax.swing.JPanel implements ActionListener {

    private JTextField valueTextField = null;
    private JComboBox valueComboBox = null;
    private Provider provider;
    
    /** Creates new form PropertyPanel */
    public PropertyPanel(PropertiesPanel.PropertiesParamHolder propParam, boolean add, String propName, String propValue) {
        initComponents();
        provider = propParam.getProvider();
        // The comb box only contains the property names that are not defined yet when adding
        if (add) {
            nameComboBox.setModel(new DefaultComboBoxModel(Util.getAvailPropNames(provider, propParam.getPU()).toArray(new String[]{})));
        } else {
            nameComboBox.setModel(new DefaultComboBoxModel(Util.getPropsNamesExceptGeneral(provider).toArray(new String[]{})));
            nameComboBox.setSelectedItem(propName);
        }

        valueTextField = new JTextField();
        valueComboBox = new JComboBox();

        // Add the appropriate component for the value 
        String selectedPropName = (String) nameComboBox.getSelectedItem();
        addValueComponent(selectedPropName, propValue);

        nameComboBox.addActionListener((ActionListener) this);

        // Disable the name combo box for editing
        nameComboBox.setEnabled(add);
    }
    
    public void addNameComboBoxListener(ActionListener listener) {
        nameComboBox.addActionListener(listener);
    }

    public void addValueComponent(String propName, String propValue) {
        valuePanel.removeAll();
        Object possibleValue = PersistenceCfgProperties.getPossiblePropertyValue(provider, propName);
        if (possibleValue == null) {
         
            valuePanel.add(valueTextField, java.awt.BorderLayout.CENTER);
            valueTextField.setText(propValue);
            
        } else if (possibleValue instanceof String[]) {
            
            valueComboBox.setModel( new DefaultComboBoxModel((String[]) possibleValue));
            valueComboBox.setEditable(true);

            valuePanel.add(valueComboBox, java.awt.BorderLayout.CENTER);
            
            if (propValue != null) {
                valueComboBox.setSelectedItem(propValue);
            } else {
                valueComboBox.setSelectedIndex(0);
            }
        }

        this.revalidate();
        this.repaint();
    }

    public JTextField getValueTextField() {
        return this.valueTextField;
    }
    
    public JTextField getValueComboBoxTextField() {
        return (JTextField)this.valueComboBox.getEditor().getEditorComponent();
    }

    public String getPropertyName() {
        return (String) this.nameComboBox.getSelectedItem();
    }

    public String getPropertyValue() {
        Object possibleValue = PersistenceCfgProperties.getPossiblePropertyValue(provider, getPropertyName());
        if(possibleValue == null) {
            return getValueTextField().getText().trim();
        } else {
            return getValueComboBoxTextField().getText().trim();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        String propName = (String) cb.getSelectedItem();
        addValueComponent(propName, null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        nameComboBox = new javax.swing.JComboBox();
        valuePanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(PropertyPanel.class, "PropertyPanel.nameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(valueLabel, org.openide.util.NbBundle.getMessage(PropertyPanel.class, "PropertyPanel.valueLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(valueLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(nameComboBox, gridBagConstraints);

        valuePanel.setPreferredSize(new java.awt.Dimension(27, 22));
        valuePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(valuePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox nameComboBox;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JPanel valuePanel;
    // End of variables declaration//GEN-END:variables

}
