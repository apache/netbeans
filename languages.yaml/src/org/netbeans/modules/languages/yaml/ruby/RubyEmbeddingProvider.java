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
package org.netbeans.modules.languages.yaml.ruby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.languages.yaml.YamlTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author Petr Hejl
 */
public class RubyEmbeddingProvider extends EmbeddingProvider {

    public static final String RUBY_MIME_TYPE = "text/x-ruby"; // NOI18N
    private static final Logger LOG = Logger.getLogger(RubyEmbeddingProvider.class.getName());

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (YamlTokenId.YAML_MIME_TYPE.equals(snapshot.getMimeType())) {
            List<Embedding> embeddings = translate(snapshot);
            if (embeddings.isEmpty()) {
                return Collections.<Embedding>emptyList();
            } else {
                return Collections.singletonList(Embedding.create(embeddings));
            }
        } else {
            LOG.warning("Unexpected snapshot type: '" + snapshot.getMimeType() + "'; expecting '" + YamlTokenId.YAML_MIME_TYPE + "'"); //NOI18N
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
        BaseDocument d = (BaseDocument) snapshot.getSource().getDocument(false);
        if (d == null) {
            return Collections.emptyList();
        }

        List<Embedding> embeddings = new ArrayList<Embedding>();

        try {
            d.readLock();
            TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get((Document) d);
            TokenSequence<YamlTokenId> tokenSequence = tokenHierarchy.tokenSequence(YamlTokenId.language()); //get top level token sequence

            if (tokenSequence != null) {
                translate(snapshot, tokenHierarchy, tokenSequence, embeddings);
            }
        } finally {
            d.readUnlock();
        }
        return embeddings;
    }

    /**
     * Perform eruby translation
     *
     * @param outputBuffer The buffer to emit the translation to
     * @param tokenHierarchy The token hierarchy for the yaml code
     * @param tokenSequence The token sequence for the yaml code
     */
    private void translate(Snapshot snapshot, TokenHierarchy<Document> tokenHierarchy,
            TokenSequence<? extends YamlTokenId> tokenSequence, List<Embedding> embeddings) {
        // Add a super class such that code completion, goto declaration etc.
        // knows where to pull the various link_to etc. methods from

        // Erubis uses _buf; I've seen eruby using something else (_erbout?)
        embeddings.add(snapshot.create("_buf='';", RUBY_MIME_TYPE));

        boolean skipNewline = false;
        while (tokenSequence.moveNext()) {
            Token<? extends YamlTokenId> token = tokenSequence.token();

            if (token.id() == YamlTokenId.TEXT || token.id() == YamlTokenId.COMMENT) {
                int sourceStart = token.offset(tokenHierarchy);

                String text = token.text().toString();

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

                // avoid creating an embedding for the artificial '\n' at the end (which is now there
                // as a result of the fix for #159502
                // XXX: shouldn't this be handled e.g. in token hiearchy creation??
                if (found && tokenSequence.index() < tokenSequence.tokenCount() - 1) {
                    embeddings.add(snapshot.create(sourceStart, i, RUBY_MIME_TYPE));
                    //buffer.append(text.substring(0, i));
                    text = text.substring(i);
                } else {
                    embeddings.add(snapshot.create(";", RUBY_MIME_TYPE));
                    //buffer.append(';');
                }
                embeddings.add(snapshot.create("_buf << '", RUBY_MIME_TYPE));

                if (skipNewline && text.startsWith("\n")) { // NOI18N
                    text = text.substring(1);
                }
                // Escape 's in the document so they don't escape out of the ruby code
                // I don't have to do this on lines that are in comments... But no big harm
                text = text.replace("'", "\\'");

                // FIXME this escaping should be replaced with
                // proper embeddings from source code
                embeddings.add(snapshot.create(text, RUBY_MIME_TYPE));

                // TODO: This "\n" shouldn't be there if the next "<%" is a "<%-" !
                embeddings.add(snapshot.create("';\n", RUBY_MIME_TYPE));
                //buffer.append("';\n"); // NOI18N

                skipNewline = false;
            } else if (token.id() == YamlTokenId.RUBY) {
                int sourceStart = token.offset(tokenHierarchy);

                String text = token.text().toString();
                skipNewline = false;
                if (text.endsWith("-")) { // NOI18N
                    text = text.substring(0, text.length() - 1);
                    skipNewline = true;
                }

                embeddings.add(snapshot.create(sourceStart, text.length(), RUBY_MIME_TYPE));

                skipNewline = false;
            } else if (token.id() == YamlTokenId.RUBY_EXPR) {
                embeddings.add(snapshot.create("_buf << (", RUBY_MIME_TYPE));

                int sourceStart = token.offset(tokenHierarchy);

                String text = token.text().toString();
                skipNewline = false;
                if (text.endsWith("-")) { // NOI18N
                    text = text.substring(0, text.length() - 1);
                    skipNewline = true;
                }
                embeddings.add(snapshot.create(sourceStart, text.length(), RUBY_MIME_TYPE));

// Make code sanitizing work better:  buffer.append("\n).to_s;"); // NOI18N
                embeddings.add(snapshot.create(").to_s;", RUBY_MIME_TYPE));
            }
        }

        // Close off the class
        // eruby also ends with this statement: _buf.to_s
//        String end = "\nend\n"; // NOI18N
//        buffer.append(end);
//        if (doc != null) {
//            codeBlocks.add(new CodeBlockData(doc.getLength(), doc.getLength(), buffer.length()-end.length(), buffer.length()));
//        }
    }

    public static final class Factory extends TaskFactory {

        public Factory() {
            // no-op
        }

        public @Override
        Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            if (!YamlTokenId.YAML_MIME_TYPE.equals(snapshot.getMimeType())) {
                return Collections.<SchedulerTask>emptyList();
            }

            return Collections.singleton(new RubyEmbeddingProvider());
        }
    }
}
