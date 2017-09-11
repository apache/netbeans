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

        removeButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDataObject.modelUpdatedFromUI();
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                enableAddButton();
            }
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
