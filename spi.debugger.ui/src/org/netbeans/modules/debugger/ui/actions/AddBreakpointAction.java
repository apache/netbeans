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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 * AddBreakpoint action.
 *
 * @author   Jan Jancura
 */
public class AddBreakpointAction extends AbstractAction {

    private static WeakReference<AddBreakpointDialogManager> abdmRef;

    
    public AddBreakpointAction () {
        putValue (
            Action.NAME, 
            NbBundle.getMessage (
                AddBreakpointAction.class, 
                "CTL_AddBreakpoint"
            )
        );
    }

    public void actionPerformed (ActionEvent e) {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            
        if (dm.lookup (null, BreakpointType.class).size () == 0) 
            return; // no breakpoint events...

        // create Add Breakpoint Dialog for it
        AddBreakpointDialogManager abdm = abdmRef != null ? abdmRef.get() : null;
        if (abdm == null) {
            abdm = new AddBreakpointDialogManager ();
            abdmRef = new WeakReference<AddBreakpointDialogManager>(abdm);
        }
        abdm.getDialog ().setVisible (true);
    }
    
    // innerclasses .........................................................................

    /**
    * Dialog manager for adding breakpoints.
    * This class is final only for performance reasons,
    * can be happily unfinaled if desired.
    */
    static final class AddBreakpointDialogManager extends Object
        implements ActionListener, PropertyChangeListener {

        /** true if ok was pressed */
        private boolean okPressed;
        private Dialog dialog;
        private AddBreakpointPanel panel;
        private DialogDescriptor descriptor;
        private NotificationLineSupport notificationSupport;
        private Controller controller;
        private JButton bOk;
        private JButton bCancel;

        /** Accessor for managed dialog instance */
        Dialog getDialog () {
            dialog = createDialog ();
            okPressed = false;
            setValid ();
            startListening ();
            panel.addPropertyChangeListener (this);
            return dialog;
        }

        /** Constructs managed dialog instance using TopManager.createDialog
        * and returnrs it */
        private Dialog createDialog () {
            ResourceBundle bundle = NbBundle.getBundle (AddBreakpointAction.class);

            panel = new AddBreakpointPanel ();
            // create dialog descriptor, create & return the dialog
            descriptor = new DialogDescriptor (
                panel,
                bundle.getString ("CTL_Breakpoint_Title"), // NOI18N
                true,
                this
            );
            descriptor.setOptions (new JButton[] {
                bOk = new JButton (bundle.getString ("CTL_Ok")), // NOI18N
                bCancel = new JButton (bundle.getString ("CTL_Cancel")) // NOI18N
            });
            bOk.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Ok")); // NOI18N
            bCancel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Cancel")); // NOI18N
            descriptor.setClosingOptions (new Object [0]);
            notificationSupport = descriptor.createNotificationLineSupport();
            Dialog d = DialogDisplayer.getDefault ().createDialog (descriptor);
            d.pack ();
            return d;
        }

        /** Called when some dialog button was pressed */
        public void actionPerformed (ActionEvent evt) {
            okPressed = bOk.equals (evt.getSource ());
            Controller controller = panel.getController ();
            boolean close = false;
            if (okPressed)
                close = controller != null && controller.ok ();
            else
                close = controller == null || controller.cancel ();
                
            if (!close) return;
            panel.removePropertyChangeListener (this);
            stopListening ();
            dialog.setVisible (false);
            dialog.dispose ();
            dialog = null;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName () == AddBreakpointPanel.PROP_TYPE) {
                stopListening ();
                setValid ();
                notificationSupport.clearMessages();
                startListening ();
                
            } else
            if (e.getPropertyName () == Controller.PROP_VALID) {
                setValid ();
            } else if (e.getPropertyName() == NotifyDescriptor.PROP_ERROR_NOTIFICATION) {
                Object v = e.getNewValue();
                String msg = (v == null) ? null : v.toString();
                notificationSupport.setErrorMessage(msg);
            } else if (e.getPropertyName() == NotifyDescriptor.PROP_INFO_NOTIFICATION) {
                Object v = e.getNewValue();
                String msg = (v == null) ? null : v.toString();
                notificationSupport.setInformationMessage(msg);
            } else if (e.getPropertyName() == NotifyDescriptor.PROP_WARNING_NOTIFICATION) {
                Object v = e.getNewValue();
                String msg = (v == null) ? null : v.toString();
                notificationSupport.setWarningMessage(msg);
            }
        }
        
        void startListening () {
            controller = panel.getController ();
            if (controller == null) return;
            controller.addPropertyChangeListener (this);
        }
        
        void stopListening () {
            if (controller == null) return;
            controller.removePropertyChangeListener (this);
            controller = null;
        }
        
        void setValid () {
            Controller controller = panel.getController ();
            if (controller == null) {
                bOk.setEnabled (false);
                return;
            }
            if (panel.isNoValidityController()) {
                // Always valid
                bOk.setEnabled (true);
            } else {
                bOk.setEnabled (controller.isValid ());
            }
        }

        /** @return true if OK button was pressed in dialog,
        * false otherwise. */
        public boolean getOKPressed () {
            return okPressed;
        }
    }
}


