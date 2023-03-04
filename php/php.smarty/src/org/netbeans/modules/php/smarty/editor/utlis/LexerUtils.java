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
package org.netbeans.modules.php.smarty.editor.utlis;

import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.smarty.editor.TplKit;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Martin Fousek
 */
public final class LexerUtils {

    private LexerUtils() {
    }

    public static boolean isVariablePart(int character) {
        return Character.isJavaIdentifierPart(character);
    }

    public static boolean isWS(int character) {
        return Character.isWhitespace(character);
    }

    public static void relexerOpenedTpls() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TopComponent[] topComponents = WindowManager.getDefault().getOpenedTopComponents(WindowManager.getDefault().findMode("editor")); //NOI18N
                for (TopComponent topComponent : topComponents) {
                    if (topComponent instanceof CloneableEditorSupport.Pane) {
                        JEditorPane cesEditorPane = ((CloneableEditorSupport.Pane) topComponent).getEditorPane();
                        EditorKit editorKit = cesEditorPane.getEditorKit();

                        final Document doc = cesEditorPane.getDocument();
                        if (editorKit instanceof TplKit) {
                            ((TplKit) (editorKit)).initLexerColoringListener(doc);
                        }
                        NbEditorDocument nbdoc = (NbEditorDocument) doc;
                        nbdoc.runAtomic(new Runnable() {

                            @Override
                            public void run() {
                                MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                                if (mti != null) {
                                    mti.tokenHierarchyControl().rebuild();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public static TokenSequence<TplTopTokenId> getTplTopTokenSequence(Document doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        return (TokenSequence<TplTopTokenId>) getTokenSequence(th, offset, TplTopTokenId.language());
    }

    public static TokenSequence<TplTopTokenId> getTplTopTokenSequence(Snapshot snapshot, int offset) {
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        return (TokenSequence<TplTopTokenId>) getTokenSequence(th, offset, TplTopTokenId.language());
    }

    public static TokenSequence<TplTopTokenId> getTplTopTokenSequence(TokenHierarchy th, int offset) {
        return (TokenSequence<TplTopTokenId>) getTokenSequence(th, offset, TplTopTokenId.language());
    }

    public static TokenSequence<TplTokenId> getTplTokenSequence(TokenHierarchy th, int offset) {
        return (TokenSequence<TplTokenId>) getTokenSequence(th, offset, TplTokenId.language());
    }

    public static <K> TokenSequence<? extends K> getTokenSequence(TokenHierarchy th,
            int offset, Language<? extends K> language) {
        TokenSequence ts = th.tokenSequence(language);

        if (ts == null) {
            // Possibly an embedding scenario such as an HTML file
            // First try with backward bias true
            List<TokenSequence> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == language) {
                    ts = t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == language) {
                        ts = t;

                        break;
                    }
                }
            }
        }

        return ts;
    }

}
