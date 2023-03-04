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

package org.netbeans.modules.web.jsf.dialogs;

import java.awt.Color;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** AddDialog.java
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 */
public class AddDialog extends DialogDescriptor {
    private static Color errorLabelColor = javax.swing.UIManager.getDefaults().getColor("ToolBar.dockingForeground"); //NOI18N
    public static final JButton ADD_OPTION = new JButton(NbBundle.getMessage(AddDialog.class,"LBL_Add"));
    static {
        ADD_OPTION.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/dialogs/Bundle").getString("ACSD_Add"));
    }
    private JPanel panel;
    
    /** Creates a new instance of EditDialog */
    public AddDialog(ValidatingPanel panel, String title, HelpCtx helpCtx) {
        super (new InnerPanel((JPanel)panel),getTitle(title),true,
              new Object[]{ADD_OPTION, DialogDescriptor.CANCEL_OPTION},
              DialogDescriptor.OK_OPTION,
              DialogDescriptor.BOTTOM_ALIGN,
              helpCtx,
              null);
        this.panel=(JPanel)panel;
        AbstractButton[] comp = panel.getStateChangeComponents();
        if (comp!=null && comp.length>0) {
            StateChangeListener list = new StateChangeListener(this);
            for (int i=0;i<comp.length;i++) {
                comp[i].addItemListener(list);
            }
        }
        JTextComponent[] textComp = panel.getDocumentChangeComponents();
        if (textComp!=null && textComp.length>0) {
            DocListener list = new DocListener(this);
            for (int i=0;i<textComp.length;i++) {
                textComp[i].getDocument().addDocumentListener(list);
            }
        }
        checkValues();
    }

    private static String getTitle(String title) {
        return NbBundle.getMessage(AddDialog.class,"TTL_ADD",title);
    }
    
    public void disableAdd() {
       ((JButton)getOptions()[0]).setEnabled(false);
    }
    
    public void enableAdd() {
       ((JButton)getOptions()[0]).setEnabled(true);
    }
    
    /** Returns the dialog panel 
    * @return dialog panel
    */
    public final javax.swing.JPanel getDialogPanel() {
        return panel;
    }
    
    /** Calls validation of panel components, displays or removes the error message
    * Should be called from listeners listening to component changes. 
    */
    protected final void checkValues() {
        String errorMessage = validate();
        if (errorMessage==null) {
            enableAdd();
        } else {
            disableAdd();
        }
        javax.swing.JLabel errorLabel = ((InnerPanel)getMessage()).getErrorLabel();
        errorLabel.setText(errorMessage==null?" ":errorMessage);
    }
    
    /** Provides validation for panel components */
    protected String validate() {
        return ((ValidatingPanel)panel).validatePanel();
    }
    
    private static class InnerPanel extends javax.swing.JPanel {
        javax.swing.JLabel errorLabel;
        InnerPanel(JPanel panel) {
            super(new java.awt.BorderLayout());
            errorLabel = new javax.swing.JLabel(" ");
            errorLabel.setBorder(new javax.swing.border.EmptyBorder(12,12,0,0));
            errorLabel.setForeground(errorLabelColor);
            add(panel, java.awt.BorderLayout.CENTER);
            add(errorLabel, java.awt.BorderLayout.SOUTH);
            this.getAccessibleContext().setAccessibleDescription(panel.getAccessibleContext().getAccessibleDescription());
            this.getAccessibleContext().setAccessibleName(panel.getAccessibleContext().getAccessibleName());
        }
        
        void setErrorMessage(String message) {
            errorLabel.setText(message);
        }
        
        javax.swing.JLabel getErrorLabel() {
            return errorLabel;
        }
    }
    
    /** Useful DocumentListener class that can be added to the panel's text compoents */
    private class DocListener implements javax.swing.event.DocumentListener {
        AddDialog dialog;
        
        DocListener(AddDialog dialog) {
            this.dialog=dialog;
        }
        /**
         * Method from DocumentListener
         */
        public void changedUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }

        /**
         * Method from DocumentListener
         */
        public void insertUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }

        /**
         * Method from DocumentListener
         */
        public void removeUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
    }
    
    private class StateChangeListener implements java.awt.event.ItemListener {
        AddDialog dialog;
        StateChangeListener (AddDialog dialog) {
            this.dialog=dialog;
        }
        public void itemStateChanged(java.awt.event.ItemEvent e) {
            dialog.checkValues();
        }
    }
}
