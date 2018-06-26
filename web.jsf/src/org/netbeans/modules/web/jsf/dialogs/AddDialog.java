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
