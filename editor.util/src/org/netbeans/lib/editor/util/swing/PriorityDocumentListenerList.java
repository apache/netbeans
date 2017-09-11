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

package org.netbeans.lib.editor.util.swing;

import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.PriorityListenerList;

/**
 * Priority listener list that acts as DocumentListener itself
 * firing all added document listeners according to their priority.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class PriorityDocumentListenerList extends PriorityListenerList<DocumentListener> implements DocumentListener {
    
    // -J-Dorg.netbeans.lib.editor.util.swing.PriorityDocumentListenerList.level=FINE
    private static final Logger LOG = Logger.getLogger(PriorityDocumentListenerList.class.getName());
    
    /**
     * Implementation of DocumentListener's method fires all the added
     * listeners according to their priority.
     */
    public void insertUpdate(DocumentEvent evt) {
        logEvent(evt, "insertUpdate");
        // Fire the prioritized listeners
        EventListener[][] listenersArray = getListenersArray();
        // Attempt to fire to all listeners catching possible exception(s) and report first fired then
        RuntimeException runtimeException = null;
        for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
            logPriority(priority);
            EventListener[] listeners = listenersArray[priority];
            for (int i = listeners.length - 1; i >= 0; i--) {
                DocumentListener l = (DocumentListener) listeners[i];
                logListener(l);
                try {
                    l.insertUpdate(evt);
                } catch (RuntimeException ex) {
                    if (runtimeException == null) { // Only record first thrown
                        runtimeException = ex;
                    }
                }
            }
        }
        if (runtimeException != null) {
            throw runtimeException; // Re-throw remembered exception
        }
        logEventEnd("insertUpdate");
    }

    /**
     * Implementation of DocumentListener's method fires all the added
     * listeners according to their priority.
     */
    public void removeUpdate(DocumentEvent evt) {
        logEvent(evt, "removeUpdate");
        // Fire the prioritized listeners
        EventListener[][] listenersArray = getListenersArray();
        // Attempt to fire to all listeners catching possible exception(s) and report first fired then
        RuntimeException runtimeException = null;
        for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
            logPriority(priority);
            EventListener[] listeners = listenersArray[priority];
            for (int i = listeners.length - 1; i >= 0; i--) {
                DocumentListener l = (DocumentListener) listeners[i];
                logListener(l);
                try {
                    l.removeUpdate(evt);
                } catch (RuntimeException ex) {
                    if (runtimeException == null) { // Only record first thrown
                        runtimeException = ex;
                    }
                }
            }
        }
        if (runtimeException != null) {
            throw runtimeException; // Re-throw remembered exception
        }
        logEventEnd("removeUpdate");
    }

    /**
     * Implementation of DocumentListener's method fires all the added
     * listeners according to their priority.
     */
    public void changedUpdate(DocumentEvent evt) {
        logEvent(evt, "changedUpdate");
        // Fire the prioritized listeners
        EventListener[][] listenersArray = getListenersArray();
        // Attempt to fire to all listeners catching possible exception(s) and report first fired then
        RuntimeException runtimeException = null;
        for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
            logPriority(priority);
            EventListener[] listeners = listenersArray[priority];
            for (int i = listeners.length - 1; i >= 0; i--) {
                DocumentListener l = (DocumentListener) listeners[i];
                logListener(l);
                try {
                    l.changedUpdate(evt);
                } catch (RuntimeException ex) {
                    if (runtimeException == null) { // Only record first thrown
                        runtimeException = ex;
                    }
                }
            }
        }
        if (runtimeException != null) {
            throw runtimeException; // Re-throw remembered exception
        }
        logEventEnd("changedUpdate");
    }

    private static void logEvent(DocumentEvent evt, String methodName) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("FIRING PriorityDocumentListenerList." + methodName + // NOI18N
                    "() evt: type=" + evt.getType() + ", off=" + evt.getOffset() + // NOI18N
                    ", len=" + evt.getLength() + "-----------------\n" + // NOI18N
                    "doc: " + evt.getDocument() // NOI18N
            );
        }
    }
    
    private static void logPriority(int priority) {
        if (LOG.isLoggable(Level.FINE)) {
            String prioMsg = (priority < DocumentListenerPriority.PRIORITIES.length)
                    ? DocumentListenerPriority.PRIORITIES[priority].getDescription()
                    : String.valueOf(priority);
            LOG.fine("  " + prioMsg + ":\n"); // NOI18N
        }
    }
    
    private static void logEventEnd(String methodName) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("END-FIRING of " + methodName + "() ------------------------------------------------\n"); // NOI18N
        }
    }
    
    private static void logListener(DocumentListener l) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("    " + l.getClass() + '\n');
        }
    }

}
