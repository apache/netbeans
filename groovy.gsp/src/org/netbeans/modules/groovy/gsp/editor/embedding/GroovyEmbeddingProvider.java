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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.gsp.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.gsp.GspLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author Petr Hejl
 * @author Martin Janicek
 */
public class GroovyEmbeddingProvider extends EmbeddingProvider {

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (GspLanguage.GSP_MIME_TYPE.equals(snapshot.getMimeType())) {
            return Collections.singletonList(Embedding.create(translate(snapshot)));
        } else {
            return Collections.<Embedding>emptyList();
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void cancel() {
        // FIXME parsing API
    }

    private List<Embedding> translate(Snapshot snapshot) {
        final Language<GspTokenId> gspLanguage = GspLexerLanguage.getLanguage();
        final TokenHierarchy<CharSequence> tokenHierarchy = TokenHierarchy.create(snapshot.getText(), gspLanguage);
        final TokenSequence<GspTokenId> tokenSequence = tokenHierarchy.tokenSequence(gspLanguage);

        if (tokenSequence != null) {
            return translate(snapshot, tokenSequence);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Perform groovy translation.
     *
     * @param outputBuffer The buffer to emit the translation to
     * @param tokenHierarchy The token hierarchy for the RHTML code
     * @param tokenSequence  The token sequence for the RHTML code
     */
    private List<Embedding> translate(Snapshot snapshot, TokenSequence<GspTokenId> tokenSequence) {
        final List<Embedding> embeddings = new ArrayList<Embedding>();
        embeddings.add(snapshot.create("def _buf ='';", GroovyTokenId.GROOVY_MIME_TYPE));

        boolean skipNewline = false;
        while (tokenSequence.moveNext()) {
            Token<GspTokenId> token = tokenSequence.token();

            int sourceStart = tokenSequence.offset();
            int sourceEnd = sourceStart + token.length();
            String text = token.text().toString();

            switch (token.id()) {
                case HTML:
                    // If there is leading whitespace in this token followed by a newline,
                    // emit it directly first, then insert my buffer append. Otherwise,
                    // insert a semicolon if we're on the same line as the previous output.
                    boolean found = false;
                    int i = 0;
                    for (; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (c == '\n') {
                            i++; // include it
                            found = true;
                            break;
                        } else if (!Character.isWhitespace(c)) {
                            break;
                        }
                    }

                    if (found) {
                        embeddings.add(snapshot.create(sourceStart, i, GroovyTokenId.GROOVY_MIME_TYPE));
                        text = text.substring(i);
                    }

                    embeddings.add(snapshot.create("_buf += \"\"\"", GroovyTokenId.GROOVY_MIME_TYPE));
                    if (skipNewline && text.startsWith("\n")) { // NOI18N
                        text = text.substring(1);
                        sourceEnd--;
                    }
                    embeddings.add(snapshot.create(text.replace("\"", "\\\""), GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create("\"\"\";", GroovyTokenId.GROOVY_MIME_TYPE));

                    skipNewline = false;
                    break;
                case COMMENT_HTML_STYLE_CONTENT:
                case COMMENT_GSP_STYLE_CONTENT:
                case COMMENT_JSP_STYLE_CONTENT:
                    translateComment(snapshot, embeddings, sourceStart, text);
                    break;
                case GSTRING_CONTENT:
                case SCRIPTLET_CONTENT:
                    embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create(";", GroovyTokenId.GROOVY_MIME_TYPE));

                    skipNewline = false;
                    break;
                case SCRIPTLET_OUTPUT_VALUE_CONTENT:
                    embeddings.add(snapshot.create("_buf += (", GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create(";)", GroovyTokenId.GROOVY_MIME_TYPE));

                    skipNewline = false;
                    break;
                default:
                    break;
            }
        }
        return embeddings;
    }

    private void translateComment(Snapshot snapshot, List<Embedding> embeddings, int sourceStart, String text) {
        embeddings.add(snapshot.create("/*", GroovyTokenId.GROOVY_MIME_TYPE));
        embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
        embeddings.add(snapshot.create("*/", GroovyTokenId.GROOVY_MIME_TYPE));
    }

    public static final class Factory extends TaskFactory {

        public Factory() {
        }

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            if (GspLanguage.GSP_MIME_TYPE.equals(snapshot.getMimeType())) {
                return Collections.singleton(new GroovyEmbeddingProvider());
            } else {
                return Collections.<SchedulerTask>emptyList();
            }
        }
    }
}
