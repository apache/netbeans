/*
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

package org.netbeans.modules.lsp.client.bindings.refactoring;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameOptions;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/** Based on RefactoringActionsProvider, ContextAnalyzer, RefactoringUtils, UIUtilities from refactoring.java.
 *
 * @author Jan Becicka
 */
@NbBundle.Messages({"WARN_CannotPerformHere=Cannot perform rename here."})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position=100)
public class RefactoringActionsProvider extends ActionsImplementationProvider{

    @Override
    @Messages("NM_Unknown=Unknown")
    public void doFindUsages(Lookup lookup) {
        Runnable start = () -> {
            EditorCookie ec = lookup.lookup(EditorCookie.class);

            if (isFromEditor(ec)) {
                try {
                    JEditorPane c = ec.getOpenedPanes()[0];
                    Document doc = c.getDocument();
                    AbstractDocument abstractDoc = (doc instanceof AbstractDocument) ? ((AbstractDocument) doc) : null;
                    FileObject file = NbEditorUtilities.getFileObject(doc);
                    LSPBindings bindings = LSPBindings.getBindings(file);
                    Caret caret = c.getCaret();
                    if(caret == null) {
                        return;
                    }
                    int caretPos = caret.getDot();
                    Position pos = Utils.createPosition(doc, caretPos);
                    ReferenceParams params = new ReferenceParams();
                    params.setTextDocument(new TextDocumentIdentifier(Utils.toURI(file)));
                    params.setPosition(pos);
                    params.setContext(new ReferenceContext(false)); //(could be an option?)

                    String name = Bundle.NM_Unknown();

                    if (abstractDoc != null) {
                        abstractDoc.readLock();
                    }
                    try {
                        TokenSequence<?> ts = TokenHierarchy.get(doc).tokenSequence();
                        if (ts != null) {
                            ts.move(caretPos);
                            if (ts.moveNext()) {
                                name = ts.token().text().toString();
                            }
                        }
                    } finally {
                        if (abstractDoc != null) {
                            abstractDoc.readUnlock();
                        }
                    }

                    UI.openRefactoringUI(new WhereUsedRefactoringUIImpl(bindings, params, name),
                                         TopComponent.getRegistry().getActivated());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        SwingUtilities.invokeLater(start);
    }

    @Override
    public void doRename(Lookup lookup) {
        Runnable start = () -> {
            EditorCookie ec = lookup.lookup(EditorCookie.class);

            if (isFromEditor(ec)) {
                try {
                    JEditorPane c = ec.getOpenedPanes()[0];
                    Document doc = c.getDocument();
                    AbstractDocument abstractDoc = (doc instanceof AbstractDocument) ? ((AbstractDocument) doc) : null;
                    FileObject file = NbEditorUtilities.getFileObject(doc);
                    LSPBindings bindings = LSPBindings.getBindings(file);
                    Caret caret = c.getCaret();
                    if(caret == null) {
                        return;
                    }
                    int caretPos = caret.getDot();
                    Position pos = Utils.createPosition(doc, caretPos);
                    String name;
                    if(abstractDoc != null) {
                        abstractDoc.readLock();
                    }
                    try {
                        TokenSequence<?> ts = TokenHierarchy.get(doc).tokenSequence();
                        name = "";
                        if (ts != null) {
                            ts.move(caretPos);
                            if (ts.moveNext()) {
                                name = ts.token().text().toString();
                            }
                        }
                    } finally {
                        if (abstractDoc != null) {
                            abstractDoc.readUnlock();
                        }
                    }

                    UI.openRefactoringUI(new RenameRefactoringUIImpl(bindings, file, pos, name),
                                         TopComponent.getRegistry().getActivated());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        SwingUtilities.invokeLater(start);
    }

    static String getActionName(Action action) {
        String arg = (String) action.getValue(Action.NAME);
        arg = arg.replace("&", ""); // NOI18N
        return arg.replace("...", ""); // NOI18N
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        LSPBindings bindings = getBindings(lookup);
        if (bindings == null) {
            return false;
        }
        return Utils.isEnabled(bindings.getInitResult().getCapabilities().getReferencesProvider());
    }

    @Override
    public boolean canRename(Lookup lookup) {
        LSPBindings bindings = getBindings(lookup);
        if (bindings == null) {
            return false;
        }
        Either<Boolean, RenameOptions> hasRename = bindings.getInitResult().getCapabilities().getRenameProvider();
        return hasRename != null && ((hasRename.isLeft() && Utils.isTrue(hasRename.getLeft())) || hasRename.isRight());
    }

    private LSPBindings getBindings(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            JEditorPane c = ec.getOpenedPanes()[0];
            Document doc = c.getDocument();
            FileObject file = NbEditorUtilities.getFileObject(doc);
            LSPBindings bindings = file != null ? LSPBindings.getBindings(file, false) : null;
            return bindings;
        }
        return null;
    }

    public static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && NbDocument.findRecentEditorPane(ec) != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }

}
