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
package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * A panel containing the session-factory property data 
 * 
 * @author Dongmei Cao
 */
public class PropertiesTablePanel extends DefaultTablePanel {

    private PropertiesTableModel model;
    private PUDataObject configDataObject;
    private PropertiesPanel.PropertiesParamHolder propParam;

    public PropertiesTablePanel(final PUDataObject dObj, final PropertiesPanel.PropertiesParamHolder propParam, final PropertiesTableModel model) {
        super(model);
        this.model = model;
        this.configDataObject = dObj;
        this.propParam = propParam;

        removeButton.addActionListener( (java.awt.event.ActionEvent evt) -> {
            configDataObject.modelUpdatedFromUI();
            int row = getTable().getSelectedRow();
            model.removeRow(row);
            enableAddButton();
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));

        enableAddButton();
    }

    /**
     * Enable or disable the Add button depending if there are any more properties to be defined
     */
    private void enableAddButton() {
        // Add button is disabled if all properties in the specified category are defined
        
        // TODO: Waiting for the setAddButton() method from DefaultTalbePanel class
//        if (Util.getAvailPropNames(propParam.getProvider(), propParam.getPU()).size()>0) {
//            super.setAddButton(true);
//        } else {
//            super.setAddButton(false);
//        }
    }

    /**
     * Listener for the Add and Edit buttons
     */
    private class TableActionListener implements ActionListener {

        private boolean add;

        TableActionListener(boolean add) {
            this.add = add;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add ? -1 : getTable().getSelectedRow());

            String propName = null;
            String propValue = null;
            if (!add) {
                propName = (String) model.getValueAt(row, 0);
                propValue = (String) model.getValueAt(row, 1);
            }
            
            final PropertyPanel dialogPanel = new PropertyPanel(propParam, add, 
                    propName, propValue);

            EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(PropertiesTablePanel.class, "LBL_Property"), add) {

                @Override
                protected String validate() {
                    String propValue = dialogPanel.getPropertyValue();

                    if (propValue.length() == 0) {
                        return NbBundle.getMessage(PropertiesTablePanel.class, "TXT_Prop_Value_Empty");
                    }
                    return null;
                }
            };

            if( dialogPanel.getPropertyValue().length() == 0 ) {
                // disable OK button
                dialog.setValid(false);
            } else {
                dialog.setValid( true );
            }

            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getValueTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getValueComboBoxTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.addNameComboBoxListener(new PropertyPanelListner(dialog));
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getValueTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getValueComboBoxTextField().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                configDataObject.modelUpdatedFromUI();

                String name = dialogPanel.getPropertyName();
                String value = dialogPanel.getPropertyValue();

                if (add) {
                    model.addRow(name, value);
                    enableAddButton();
                } else {
                    model.editRow(row, value);
                }
            }
        }
    }
    
    private static class PropertyPanelListner implements ActionListener {
        
        private EditDialog dialog;
        
        public PropertyPanelListner(EditDialog dialog){
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if( ((PropertyPanel)dialog.getDialogPanel()).getPropertyValue().length() == 0 ) {
                // disable OK button
                dialog.setValid(false);
            } else {
                dialog.setValid( true );
            }
            
            dialog.checkValues();
        }
        
    }
}
