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
package org.netbeans.modules.xml.lib;

import org.netbeans.modules.xml.util.Util;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;

/**
 * @author  Libor Kramolis
 */
public final class GuiUtil {

    private GuiUtil() {}

    /**
     * Perform default action on specified data object.
     */
    public static void performDefaultAction (DataObject dataObject) {
        final Node node = dataObject.getNodeDelegate();
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                callAction(node.getPreferredAction(), node, new ActionEvent (node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            }
        });
    }
    
    /**
     * Try to perform default action on specified file object.
     */
    public static void performDefaultAction (FileObject fo) {
        if (fo == null) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("FileObject can not be null.", new IllegalArgumentException());  // NOI18N
            return;            
        }
        final DataObject obj;
        try {
            obj = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            if (Util.THIS.isLoggable()) {
                Util.THIS.debug("DataObject not found", e); // NOI18N
            }
            return;            
        }
        // All else is GUI, do in EQ.
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                Node node = obj.getNodeDelegate();
                Action a = node.getPreferredAction();
                callAction(a, node, new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            }
        });
    }

    private static void callAction(Action a, Node node, ActionEvent actionEvent) {
        if (a instanceof ContextAwareAction) {
            a = ((ContextAwareAction)a).createContextAwareInstance(node.getLookup());
        }
        if (a == null) {
            return;
        }
        a.actionPerformed(actionEvent);
    }

    public static boolean confirmAction (String message) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation (message, NotifyDescriptor.YES_NO_OPTION);
        Object option = DialogDisplayer.getDefault().notify (nd);
        return ( option == NotifyDescriptor.YES_OPTION );
    }


    public static void setStatusText (String text) {
        StatusDisplayer.getDefault().setStatusText (text);
    }



    /**
     * Notify exception to user. Just shortcut to ErrorManager.
     */
    public static void notifyException (Throwable exc) {
        notifyException (null, exc);
    }


    /**
     * Notify annotated exception to user. Just shortcut to ErrorManager.
     */    
    public static void notifyException (String desc, Throwable ex) {
        ErrorManager err = ErrorManager.getDefault();
        if (desc != null) {
            err.annotate (ex, desc);
        }
        err.notify (err.EXCEPTION, ex);  // show stack trace to user
    }

    /**
     * Thread safe notify message as WARNING_MESSAGE.
     */
    public static void notifyWarning (final String message) {
        // invokeLater??? there had to be some error with DialogDisplyer
        SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    NotifyDescriptor nd = new NotifyDescriptor.Message
                        (message, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault ().notify (nd);
                }
            });
    }

    public static void notifyError (final String message) {
        SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    DialogDisplayer.getDefault().notify
                        (new NotifyDescriptor.Message (message, NotifyDescriptor.ERROR_MESSAGE)
                         );        
                }
            });
    }

}
