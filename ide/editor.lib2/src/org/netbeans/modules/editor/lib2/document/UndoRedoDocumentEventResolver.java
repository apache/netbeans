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
package org.netbeans.modules.editor.lib2.document;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.undo.UndoManager;

/**
 * Resolver for whether the given document event is currently being undone/redone.
 *
 * @author Miloslav Metelka
 */
public abstract class UndoRedoDocumentEventResolver {
    
    private static UndoRedoDocumentEventResolver resolverChain;
    
    public static synchronized boolean isUndoRedoEvent(DocumentEvent evt) {
        UndoRedoDocumentEventResolver resolver = resolverChain;
        while (resolver != null) {
            if (resolver.isUndoRedo(evt)) {
                return true;
            }
            resolver = resolver.next;
        }
        return false;
    }
    
    public static synchronized void register(UndoRedoDocumentEventResolver resolver) {
        resolver.next = resolverChain;
        resolverChain = resolver;
    }
    
    static {
        // Register default resolver for swing actions
        register(new SwingUndoRedoResolver());
    }
    
    private UndoRedoDocumentEventResolver next;
    
    public UndoRedoDocumentEventResolver() {
    }
    
    public abstract boolean isUndoRedo(DocumentEvent evt);
    
    
    private static final class SwingUndoRedoResolver extends UndoRedoDocumentEventResolver {
        
        private final Class swingUndoRedoDocEventClass;

        SwingUndoRedoResolver() {
            PlainDocument doc = new PlainDocument();
            final Class[] urCls = new Class[1];
            try {
                UndoManager um = new UndoManager();
                doc.addUndoableEditListener(um);
                doc.insertString(0, "a", null);
                doc.addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        // Swing impls wrap the original events into an extra pkg-private AbstractDocument.UndoRedoDocumentEvent class
                        urCls[0] = e.getClass();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });
                um.undo();
                
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
            swingUndoRedoDocEventClass = urCls[0];
            assert (swingUndoRedoDocEventClass != null);
        }

        @Override
        public boolean isUndoRedo(DocumentEvent evt) {
            return (evt.getClass() == swingUndoRedoDocEventClass);
        }
    }
    
}
