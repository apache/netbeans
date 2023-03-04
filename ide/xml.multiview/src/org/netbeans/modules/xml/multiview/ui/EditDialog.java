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

package org.netbeans.modules.xml.multiview.ui;

import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/** EditDialog.java
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 * @author Petr Slechta
 */
public abstract class EditDialog extends DialogDescriptor {
    private javax.swing.JPanel panel;
    private NotificationLineSupport statusLine;

    /** Creates a new instance of EditDialog */
    public EditDialog(javax.swing.JPanel panel, String title, boolean adding) {
        super (panel, getTitle(title, adding), true,
              DialogDescriptor.OK_CANCEL_OPTION,
              DialogDescriptor.OK_OPTION,
              DialogDescriptor.BOTTOM_ALIGN,
              null, null);
        this.panel = panel;
        statusLine = createNotificationLineSupport();
    }
   
    /** Creates a new instance of EditDialog */
    public EditDialog(javax.swing.JPanel panel, String title) {
        this(panel, title,false);
    }
    
    private static String getTitle(String title, boolean adding) {
        return (adding?NbBundle.getMessage(EditDialog.class,"TTL_ADD",title):
                NbBundle.getMessage(EditDialog.class,"TTL_EDIT",title));
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
    public final void checkValues() {
        String errorMessage = validate();
        if (errorMessage == null) {
            statusLine.clearMessages();
            setValid(true);
        } else {
            statusLine.setErrorMessage(errorMessage);
            setValid(false);
        }
    }
    
    /** Provides validation for panel components */
    protected abstract String validate();
    
    /** Useful DocumentListener class that can be added to the panel's text compoents */
    public static class DocListener implements javax.swing.event.DocumentListener {
        EditDialog dialog;
        
        public DocListener(EditDialog dialog) {
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
}
