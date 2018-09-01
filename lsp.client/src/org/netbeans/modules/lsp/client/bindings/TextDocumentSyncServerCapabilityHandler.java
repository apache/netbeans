/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.lsp.client.bindings;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.modules.editor.*;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;

/** TODO: asynchronous
 *  TODO: follow the synchronization options
 *  TODO: close
 *
 * @author lahvac
 */
public class TextDocumentSyncServerCapabilityHandler {

    private final Set<JTextComponent> lastOpened = Collections.newSetFromMap(new IdentityHashMap<>());

    private void handleChange() {
        assert SwingUtilities.isEventDispatchThread();
        Set<JTextComponent> currentOpened = Collections.newSetFromMap(new IdentityHashMap<>());
        currentOpened.addAll(EditorRegistry.componentList());
        Set<JTextComponent> newOpened = Collections.newSetFromMap(new IdentityHashMap<>());
        newOpened.addAll(currentOpened);
        newOpened.removeAll(lastOpened);
        Set<JTextComponent> newClosed = Collections.newSetFromMap(new IdentityHashMap<>());
        newClosed.addAll(lastOpened);
        newClosed.removeAll(newOpened);
        lastOpened.removeAll(newClosed);
        lastOpened.addAll(newOpened);

        for (JTextComponent opened : newOpened) {
            FileObject file = NbEditorUtilities.getFileObject(opened.getDocument());

            if (file == null)
                continue; //ignore

            LSPBindings server = LSPBindings.getBindings(file);

            if (server == null)
                continue; //ignore

            try {
                //XXX: should construct events outside of AWT
                TextDocumentItem textDocumentItem = new TextDocumentItem(file.toURI().toString(),
                                                                         FileUtil.getMIMEType(file),
                                                                         0,
                                                                         opened.getDocument().getText(0, opened.getDocument().getLength())); //XXX: should do in render!

                server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(textDocumentItem));
                server.scheduleBackgroundTasks(file);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            Document doc = opened.getDocument();

            doc.addDocumentListener(new DocumentListener() { //XXX: listener
                int version; //XXX: proper versioning!
                @Override
                public void insertUpdate(DocumentEvent e) {
                    try {
                        fireEvent(e.getOffset(), e.getDocument().getText(e.getOffset(), e.getLength()), "");
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    fireEvent(e.getOffset(), "", ((BaseDocumentEvent) e).getText());
                }
                private void fireEvent(int start, String newText, String oldText) {
                    try {
                        Position startPos = Utils.createPosition(doc, start);
                        int additionalLines = 0;
                        int additionalChars = 0;
                        for (char c : oldText.toCharArray()) {
                            if (c == '\n') {
                                additionalLines++;
                                additionalChars = 0;
                            } else {
                                additionalChars++;
                            }
                        }
                        Position endPos = new Position(startPos.getLine() + additionalLines,
                                                       startPos.getCharacter() + additionalChars);
                        TextDocumentContentChangeEvent event;
                        event = new TextDocumentContentChangeEvent(new Range(startPos,
                                                                             endPos),
                                                                   oldText.length(),
                                                                   newText);
                        VersionedTextDocumentIdentifier di = new VersionedTextDocumentIdentifier(++version);
                        di.setUri(file.toURI().toString());
                        server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(di, Arrays.asList(event)));
                        server.scheduleBackgroundTasks(file);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {}
            });
        }
    }

    @OnStart
    public static class Init implements Runnable {

        @Override
        public void run() {
            TextDocumentSyncServerCapabilityHandler h = new TextDocumentSyncServerCapabilityHandler();
            EditorRegistry.addPropertyChangeListener(evt -> h.handleChange());
            SwingUtilities.invokeLater(() -> h.handleChange());
        }
        
    }
}
