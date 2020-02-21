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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makefile.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.cnd.makefile.editor.ShellEmbeddingHighlightContainer.HighlightItem;
import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.modules.cnd.api.script.ShTokenId;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.Exceptions;

/**
 * Provides shell fragments embedded into makefile.
 *
 */
public class ShellEmbeddingProvider extends EmbeddingProvider {

    private volatile boolean cancelled;

    /*package*/ ShellEmbeddingProvider() {
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        cancelled = false;
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        if (tokenHierarchy != null) {
            TokenSequence<MakefileTokenId> tokenSequence = tokenHierarchy.tokenSequence(MakefileTokenId.language());
            if (tokenSequence != null) {
                List<Embedding> embeddings = getEmbeddings(snapshot, tokenSequence);
                Document doc = snapshot.getSource().getDocument(false);
                if (doc != null && !cancelled) {
                    List<HighlightItem> highlights = getHighlights(doc, embeddings);
                    if (!cancelled) {
                        ShellEmbeddingHighlightContainer.get(doc).setHighlights(highlights);
                    }
                }
                return embeddings;
            }
        }
        return Collections.emptyList();
    }

    private List<Embedding> getEmbeddings(Snapshot snapshot, TokenSequence<MakefileTokenId> ts) {
        List<Embedding> allEmbeddings = new ArrayList<Embedding>();
        List<Embedding> localEmbeddings = new ArrayList<Embedding>();
        boolean inShell = false;
        while (ts.moveNext() && !cancelled) {
            Token<MakefileTokenId> token = ts.token();
            switch (token.id()) {
                case TAB:
                    inShell = true;
                    break;

                case SHELL:
                    localEmbeddings.add(snapshot.create(ts.offset(), token.length(), MIMENames.SHELL_MIME_TYPE));
                    inShell = true;
                    break;

                case MACRO:
                    if (inShell) {
                        localEmbeddings.add(snapshot.create(evaluateMacro(token.text()), MIMENames.SHELL_MIME_TYPE)); // NOI18N
                    }
                    break;

                default:
                    if (!localEmbeddings.isEmpty()) {
                        allEmbeddings.add(Embedding.create(localEmbeddings));
                        localEmbeddings.clear();
                    }
                    inShell = false;
            }
        }

        if (!localEmbeddings.isEmpty() && !cancelled) {
            allEmbeddings.add(Embedding.create(localEmbeddings));
        }

        return allEmbeddings;
    }

    private List<HighlightItem> getHighlights(Document doc, List<Embedding> embeddings) {
        List<HighlightItem> highlights = new ArrayList<HighlightItem>();
        for (Embedding embedding : embeddings) {
            if (cancelled) {
                break;
            }

            Snapshot snapshot = embedding.getSnapshot();
            TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
            if (tokenHierarchy != null) {
                TokenSequence<ShTokenId> tokenSequence = tokenHierarchy.tokenSequence(ShTokenId.language());
                if (tokenSequence != null) {
                    addHighlights(snapshot, doc, tokenSequence, highlights);
                }
            }
        }
        return highlights;
    }

    private static void addHighlights(Snapshot snapshot, Document doc, TokenSequence<ShTokenId> tokenSequence, List<HighlightItem> highlights) {
        while (tokenSequence.moveNext()) {
            Token<ShTokenId> token = tokenSequence.token();
            int startOffset = snapshot.getOriginalOffset(tokenSequence.offset());
            int endOffset = snapshot.getOriginalOffset(tokenSequence.offset() + token.length());
            for (int i = 1; (startOffset < 0 || endOffset < 0) && i <= token.length(); ++i) {
                int offset = snapshot.getOriginalOffset(tokenSequence.offset() + i);
                if (0 <= offset) {
                    if (startOffset < 0) {
                        startOffset = offset;
                    }
                    if (endOffset < 0 || endOffset < offset) {
                        endOffset = offset;
                    }
                }
            }
            if (0 <= startOffset && 0 <= endOffset) {
                try {
                    highlights.add(new HighlightItem(
                            doc.createPosition(startOffset),
                            doc.createPosition(endOffset),
                            token.id().primaryCategory()));
                } catch (BadLocationException ex) {
                    // Can't add
                }
            }
        }
    }

    private CharSequence evaluateMacro(CharSequence macro) {
        // no real macro evaluation yet
        if (TokenUtilities.textEquals(macro, "$$")) { // NOI18N
            return "$"; // NOI18N
        } else {
            return ""; // NOI18N
        }
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new ShellEmbeddingProvider());
        }
    }
}
