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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
    public static abstract class FocusablePanel extends JPanel {
        
        public abstract void initializeFocus();
    }
}
