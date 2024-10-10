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

import java.lang.ref.WeakReference;
import org.netbeans.modules.lsp.client.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.*;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.OperationAdapter;
import org.openide.loaders.OperationEvent;
import org.openide.modules.OnStart;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class TextDocumentSyncServerCapabilityHandler {

    static {
        DataLoaderPool.getDefault().addOperationListener(new OperationAdapter() {
            @Override
            public void operationRename(OperationEvent.Rename ev) {
                FileObject file = ev.getObject().getPrimaryFile();

                LSPBindings server = LSPBindings.getBindings(file);

                if (server == null) {
                    return; //ignore
                }

                EditorCookie ec = Utils.lookupForFile(file, EditorCookie.class);

                String newUri = Utils.toURI(file);
                String oldUri = Utils.uriReplaceFilename(newUri, ev.getOriginalName());
                reopenFile(server, ec.getDocument(), oldUri, file);
                LSPBindings.scheduleBackgroundTasks(file);
            }

            @Override
            public void operationMove(OperationEvent.Move ev) {
                FileObject originalFile = ev.getOriginalPrimaryFile();
                FileObject newFile = ev.getObject().getPrimaryFile();

                LSPBindings server = LSPBindings.getBindings(newFile);

                if (server == null) {
                    return; //ignore
                }

                EditorCookie ec = Utils.lookupForFile(newFile, EditorCookie.class);
                Document doc = ec.getDocument();

                server.getOpenedFiles().remove(originalFile);
                server.getOpenedFiles().put(newFile, Boolean.TRUE);
                reopenFile(server, doc, Utils.toURI(originalFile), newFile);
                HandlerRegistry hr = (HandlerRegistry) doc.getProperty(HandlerRegistry.class);
                if (hr != null) {
                    hr.forEach(bt -> {
                        LSPBindings.removeBackgroundTask(originalFile, bt);
                        LSPBindings.addBackgroundTask(newFile, bt);
                    });
                }
                LSPBindings.scheduleBackgroundTasks(newFile);
            }
        });
    }

    private final RequestProcessor WORKER = new RequestProcessor(TextDocumentSyncServerCapabilityHandler.class.getName(), 1, false, false);
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
        newClosed.removeAll(currentOpened);
        lastOpened.clear();
        lastOpened.addAll(currentOpened);

        for (JTextComponent opened : newOpened) {
            editorOpened(opened);
        }

        for (JTextComponent closed : newClosed) {
            editorClosed(closed);
        }
    }

    private void ensureOpenedInServer(JTextComponent opened) {
        FileObject file = NbEditorUtilities.getFileObject(opened.getDocument());

        if (file == null)
            return; //ignore

        Document doc = opened.getDocument();
        ensureDidOpenSent(doc);
        registerBackgroundTasks(opened);
    }

    public static void refreshOpenedFilesInServers() {
        SwingUtilities.invokeLater(() -> {
            assert SwingUtilities.isEventDispatchThread();
            for (JTextComponent c : EditorRegistry.componentList()) {
                h.ensureOpenedInServer(c);
            }
        });
    }

    private static final TextDocumentSyncServerCapabilityHandler h = new TextDocumentSyncServerCapabilityHandler();
    @OnStart
    public static class Init implements Runnable {

        @Override
        public void run() {
            EditorRegistry.addPropertyChangeListener(evt -> h.handleChange());
            SwingUtilities.invokeLater(() -> h.handleChange());
        }

    }

    private final Map<Document, Integer> openDocument2PanesCount = new HashMap<>();

    private void documentOpened(Document doc) {

        openDocument2PanesCount.computeIfAbsent(doc, d -> {
            doc.putProperty(TextDocumentSyncServerCapabilityHandler.class, true);
            ensureDidOpenSent(doc);
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
                        FileObject file = NbEditorUtilities.getFileObject(doc);

                        if (file == null)
                            return; //ignore

                        Position startPos = Utils.createPosition(doc, start);
                        Position endPos = Utils.computeEndPositionForRemovedText(startPos, oldText);
                        TextDocumentContentChangeEvent[] event = new TextDocumentContentChangeEvent[1];
                        event[0] = new TextDocumentContentChangeEvent(new Range(startPos,
                                                                             endPos),
                                                                   oldText.length(),
                                                                   newText);

                        boolean typingModification = DocumentUtilities.isTypingModification(doc);
                        long documentVersion = DocumentUtilities.getDocumentVersion(doc);

                        WORKER.post(() -> {
                            LSPBindings server = LSPBindings.getBindings(file);

                            if (server == null)
                                return ; //ignore

                            TextDocumentSyncKind syncKind = TextDocumentSyncKind.None;
                            Either<TextDocumentSyncKind, TextDocumentSyncOptions> sync = server.getInitResult().getCapabilities().getTextDocumentSync();
                            if (sync != null) {
                                if (sync.isLeft()) {
                                    syncKind = sync.getLeft();
                                } else {
                                    TextDocumentSyncKind change = sync.getRight().getChange();
                                    if (change != null)
                                        syncKind = change;
                                }
                            }
                            switch (syncKind) {
                                case None:
                                    return ;
                                case Full:
                                    doc.render(() -> {
                                        try {
                                            event[0] = new TextDocumentContentChangeEvent(doc.getText(0, doc.getLength()));
                                        } catch (BadLocationException ex) {
                                            Exceptions.printStackTrace(ex);
                                            event[0] = new TextDocumentContentChangeEvent("");
                                        }
                                    });
                                    break;
                                case Incremental:
                                    //event already filled
                                    break;
                            }

                            VersionedTextDocumentIdentifier di = new VersionedTextDocumentIdentifier(++version);
                            di.setUri(org.netbeans.modules.lsp.client.Utils.toURI(file));
                            DidChangeTextDocumentParams params = new DidChangeTextDocumentParams(di, Arrays.asList(event));

                            server.getTextDocumentService().didChange(params);

                            if (typingModification && oldText.isEmpty() && event.length == 1) {
                                if (newText.equals("}") || newText.equals("\n")) {
                                    List<TextEdit> edits = new ArrayList<>();
                                    doc.render(() -> {
                                        if (documentVersion != DocumentUtilities.getDocumentVersion(doc))
                                            return ;
                                        edits.addAll(Utils.computeDefaultOnTypeIndent(doc, start, startPos, newText));
                                    });
                                    NbDocument.runAtomic((StyledDocument) doc, () -> {
                                        if (documentVersion == DocumentUtilities.getDocumentVersion(doc)) {
                                            Utils.applyEditsNoLock(doc, edits);
                                        }
                                    });
                                }
                            }
                            LSPBindings.scheduleBackgroundTasks(file);
                        });
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {}
            });
            return 0;
        });
    }

    private synchronized void editorOpened(JTextComponent c) {
        Document doc = c.getDocument();
        FileObject file = NbEditorUtilities.getFileObject(c.getDocument());

        if (file == null)
            return; //ignore

        documentOpened(doc);
        registerBackgroundTasks(c);
        openDocument2PanesCount.compute(doc, (d, count) -> count + 1);
    }

    private synchronized void editorClosed(JTextComponent c) {
        Document doc = c.getDocument();
        Integer count = openDocument2PanesCount.getOrDefault(doc, -1);
        if (count > 0) {
            openDocument2PanesCount.put(doc, --count);
        }
        if (count == 0) {
            //TODO modified!
            WORKER.post(() -> {
                FileObject file = NbEditorUtilities.getFileObject(doc);

                if (file == null)
                    return; //ignore

                LSPBindings server = LSPBindings.getBindings(file);

                if (server == null)
                    return ; //ignore

                TextDocumentIdentifier di = new TextDocumentIdentifier();
                di.setUri(Utils.toURI(file));
                DidCloseTextDocumentParams params = new DidCloseTextDocumentParams(di);

                server.getTextDocumentService().didClose(params);
                server.getOpenedFiles().remove(file);
            });
            openDocument2PanesCount.remove(doc);
        }
    }

    private void ensureDidOpenSent(Document doc) {
        WORKER.post(() -> {
            FileObject file = NbEditorUtilities.getFileObject(doc);

            if (file == null)
                return; //ignore

            LSPBindings server = LSPBindings.getBindings(file);

            if (server == null)
                return ; //ignore

            if (server.getOpenedFiles().put(file, Boolean.TRUE) != null) {
                //already opened:
                return;
            }

            doc.putProperty(HyperlinkProviderImpl.class, true);

            TextDocumentItem textDocumentItem = toTextDocumentItem(doc, file);

            server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(textDocumentItem));
            LSPBindings.scheduleBackgroundTasks(file);
        });
    }

    private static TextDocumentItem toTextDocumentItem(Document doc, FileObject file) {
        String[] text = new String[1];
        String uri = Utils.toURI(file);

        doc.render(() -> {
            try {
                text[0] = doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                text[0] = "";
            }
        });
        TextDocumentItem textDocumentItem = new TextDocumentItem(uri,
                FileUtil.getMIMEType(file),
                0,
                text[0]);
        return textDocumentItem;
    }

    private static void reopenFile(LSPBindings server, Document doc, String oldUri, FileObject newFile) {
        DidCloseTextDocumentParams closeParams = new DidCloseTextDocumentParams();
        closeParams.setTextDocument(new TextDocumentIdentifier(oldUri));
        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams();
        openParams.setTextDocument(toTextDocumentItem(doc, newFile));
        server.getTextDocumentService().didClose(closeParams);
        server.getOpenedFiles().remove(newFile);
        server.getTextDocumentService().didOpen(openParams);
        server.getOpenedFiles().put(newFile, Boolean.TRUE);
    }

    private void registerBackgroundTasks(JTextComponent c) {
        Document doc = c.getDocument();
        WORKER.post(() -> {
            FileObject file = NbEditorUtilities.getFileObject(doc);

            if (file == null)
                return; //ignore

            LSPBindings server = LSPBindings.getBindings(file);

            if (server == null)
                return ; //ignore

            synchronized(HandlerRegistry.class) {
                if(doc.getProperty(HandlerRegistry.class) == null) {
                    doc.putProperty(HandlerRegistry.class, new HandlerRegistry());
                }
            }
            HandlerRegistry hr = (HandlerRegistry) doc.getProperty(HandlerRegistry.class);

            SwingUtilities.invokeLater(() -> {
                if (c.getClientProperty(MarkOccurrences.class) == null) {
                    MarkOccurrences mo = new MarkOccurrences(c);
                    LSPBindings.addBackgroundTask(file, mo);
                    c.putClientProperty(MarkOccurrences.class, mo);
                    hr.add(mo);
                }
                if (c.getClientProperty(BreadcrumbsImpl.class) == null) {
                    BreadcrumbsImpl bi = new BreadcrumbsImpl(c);
                    LSPBindings.addBackgroundTask(file, bi);
                    c.putClientProperty(BreadcrumbsImpl.class, bi);
                    hr.add(bi);
                }
                if (c.getClientProperty(SemanticHighlight.class) == null) {
                    SemanticHighlight sh = new SemanticHighlight(c);
                    LSPBindings.addBackgroundTask(file, sh);
                    c.putClientProperty(SemanticHighlight.class, sh);
                    hr.add(sh);
                }
            });
        });
    }

    private static class HandlerRegistry {
        private final List<WeakReference<LSPBindings.BackgroundTask>> handlers = new ArrayList<>();

        public synchronized void add(LSPBindings.BackgroundTask backgroundTask) {
            handlers.add(new WeakReference<>(backgroundTask));
        }

        public synchronized void forEach(Consumer<LSPBindings.BackgroundTask> consumer) {
            Iterator<WeakReference<LSPBindings.BackgroundTask>> it = handlers.iterator();

            while(it.hasNext()) {
                WeakReference<LSPBindings.BackgroundTask> handlerRef = it.next();
                LSPBindings.BackgroundTask backgroundTask = handlerRef.get();
                if(backgroundTask != null) {
                    consumer.accept(backgroundTask);
                } else {
                    it.remove();
                }
            }
        }
    }
}
