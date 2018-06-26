/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
