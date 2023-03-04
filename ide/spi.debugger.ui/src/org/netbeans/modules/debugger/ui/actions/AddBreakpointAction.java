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


