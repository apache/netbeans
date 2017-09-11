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
