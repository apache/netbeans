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

package org.netbeans.modules.languages.antlr.refactoring;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.antlr.v3.Antlr3Language;
import org.netbeans.modules.languages.antlr.v4.Antlr4Language;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


@NbBundle.Messages({"WARN_CannotPerformHere=Cannot perform rename here."})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position=150)
public class RefactoringActionsProvider extends ActionsImplementationProvider{

    @Override
    @Messages("NM_Unknown=Unknown")
    public void doFindUsages(Lookup lookup) {
        Runnable start = () -> {
            EditorCookie ec = lookup.lookup(EditorCookie.class);

            if (isFromEditor(ec)) {
                JEditorPane c = ec.getOpenedPanes()[0];
                Document doc = c.getDocument();
                AbstractDocument abstractDoc = (doc instanceof AbstractDocument) ? ((AbstractDocument) doc) : null;
                FileObject file = NbEditorUtilities.getFileObject(doc);
                int caretPos = c.getCaretPosition();

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

                SymbolInformation si = new SymbolInformation(file, name);
                UI.openRefactoringUI(new WhereUsedRefactoringUIImpl(si),
                                     TopComponent.getRegistry().getActivated());
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
        boolean ret = false;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null) {
            Document doc = ec.getDocument();
            if(doc == null) return ret;
            FileObject file = NbEditorUtilities.getFileObject(doc);
            ret = Antlr3Language.MIME_TYPE.equals(file.getMIMEType())
                    || Antlr4Language.MIME_TYPE.equals(file.getMIMEType());
        }
        return ret;
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
