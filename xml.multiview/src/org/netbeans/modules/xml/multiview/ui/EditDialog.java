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
