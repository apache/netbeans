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
