/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
