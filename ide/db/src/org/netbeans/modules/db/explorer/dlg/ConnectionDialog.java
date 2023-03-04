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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class ConnectionDialog {

    private transient ConnectionDialogMediator mediator;
    private transient Exception storedExp;
    
    final DialogDescriptor descriptor;
    final Dialog dialog;
    final JButton cancelButton;
    
    public ConnectionDialog(ConnectionDialogMediator mediator, FocusablePanel basePane, String dlgTitle, HelpCtx helpCtx, ActionListener actionListener) {
        this.cancelButton = new JButton(NbBundle.getMessage(ConnectionDialog.class, "ConnectionDlg.CancelOption"));
        this.mediator = mediator;
        ConnectionProgressListener progressListener = new ConnectionProgressListener() {
            @Override
            public void connectionStarted() {
                descriptor.setValid(false);
            }
            
            @Override
            public void connectionStep(String step) {
            }

            @Override
            public void connectionFinished() {
                descriptor.setValid(true);
            }

            @Override
            public void connectionFailed() {
                descriptor.setValid(true);
            }
        };
        mediator.addConnectionProgressListener(progressListener);
        
        PropertyChangeListener propChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (propertyName == null || propertyName.equals(ConnectionDialogMediator.PROP_VALID)) {
                    updateValid();
                }
            }
        };
        mediator.addPropertyChangeListener(propChangeListener);

        basePane.getAccessibleContext().setAccessibleName(NbBundle.getMessage (ConnectionDialog.class, "ACS_ConnectDialogA11yName"));
        basePane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ConnectionDialog.class, "ACS_ConnectDialogA11yDesc"));

        descriptor = new DialogDescriptor(basePane, dlgTitle, true, new Object[] {DialogDescriptor.OK_OPTION, cancelButton},
                     DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, helpCtx, actionListener);
        
        // Valid in this case means: connection is not establishing
        // when the connection is created the process can't be canceled anymore
        descriptor.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if("valid".equals(e.getPropertyName())) {
                    cancelButton.setEnabled((Boolean) e.getNewValue());
                }
            }
        });

        // disable automatic closing
        descriptor.setClosingOptions(new Object[0]);
        updateValid();
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        
        // Explicitly close dialog on cancelbutton
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        
        // needed for issue 82787, allows the panel to request the focus
        // to the password text field
        basePane.initializeFocus();
        dialog.setVisible(false);
    }
    
    public Window getWindow() {
        return dialog;
    }
    
    public void close() {
        // dialog is closed after successfully create connection
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    public void setVisible(boolean mode) {
        dialog.setVisible(mode);
    }
    
    public void setException(Exception e) {
        storedExp = e;
    }
    
    public boolean isException() {
        return (storedExp != null);
    }        
    
    private void updateValid() {
        boolean valid = mediator.getValid();
        descriptor.setValid(valid);
    }
    
    /**
     * A {@link JPanel} with an {@link #initializeFocus} method whose implementation
     * can call {@link JComponent#requestFocusInWindow} on a children component.
     * Needed because <code>requestFocusInWindow</code> must be called 
     * after a component was <code>pack()</code>-ed, but before it is displayed, and
     * the <code>JPanel</code>, which is displayed using <code>DialogDescriptor</code>
     * does not know when this happens.
     */
    public abstract static class FocusablePanel extends JPanel {
        
        public abstract void initializeFocus();
    }
}
