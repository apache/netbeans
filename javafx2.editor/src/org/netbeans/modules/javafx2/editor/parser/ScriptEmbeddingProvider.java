/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.EventHandler;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.FxScriptFragment;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.Exceptions;

/**
 * Creates embeddings for javascript event handlers. Because of FXML spec not clear on the extensibility,
 * only "javascript" langauge, with MIME "text/javascript" is supported.
 * <p/>
 * The class will define both Parser API embeddings and Lexer embeddings, if the
 * source comes from a Document.
 * <p/>
 * There is no support for character entities YET.
 *
 * @author sdedic
 */
public class ScriptEmbeddingProvider extends EmbeddingProvider {
    private static final String JAVASCRIPT_MIME = "text/javascript"; // NOI18N
    private static final String JAVASCRIPT_LANG = "javascript"; // NOI81N
    
    public ScriptEmbeddingProvider() {
    }

    @Override
    public List<Embedding> getEmbeddings(final Snapshot snapshot) {
        if (!JavaFXEditorUtils.FXML_MIME_TYPE.equals(snapshot.getMimeType())) {
            return Collections.emptyList();
        }

        final ScriptFinder f = new ScriptFinder(snapshot);
        try {
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    final FxmlParserResult res = FxmlParserResult.get(resultIterator.getParserResult());
                    if (res == null ||
                        res.getSourceModel().getLanguage() == null ||
                        !JAVASCRIPT_LANG.equals(res.getSourceModel().getLanguage().getLanguage())) {
                        return;
                    }
                    Document doc = snapshot.getSource().getDocument(false);
                    if (doc != null) {
                        doc.render(new Runnable() {
                            public void run() {
                                f.setFxResult(res);
                                res.getSourceModel().accept(f);
                            }
                        });
                    } else {
                        f.setFxResult(res);
                        res.getSourceModel().accept(f);
                    }
                }
                
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return f.embeddings;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void cancel() {
        // nop
    }
    
    private class ScriptFinder extends FxNodeVisitor.ModelTreeTraversal {
        private FxmlParserResult fxResult;
        private List<Embedding> embeddings = new ArrayList<Embedding>();
        /**
         * This should be the snapshot from the EmbeddingProvider's invocation
         */
        private Snapshot snapshot;
        private Document doc;
        /**
         * Contains either document-based (r/w) TokenSequence or r/o Snapshot
         * tokenSequence if document was not foudn. Always check doc != null before
         * creating an embedding.
         */
        private TokenSequence<XMLTokenId> ts;
        
        public ScriptFinder(Snapshot snapshot) {
            this.snapshot = snapshot;
        }

        public void setFxResult(FxmlParserResult fxResult) {
            this.fxResult = fxResult;

            // process tokens from the start to the end, join all TEXT and CDATA contents
            // and present that as an embedding
            doc = snapshot.getSource().getDocument(false);
            if (doc == null) {
                ts = (TokenSequence<XMLTokenId>)fxResult.getTokenHierarchy().tokenSequence();
            } else {
                ts = (TokenSequence<XMLTokenId>)TokenHierarchy.get(doc).tokenSequence();
            }
        }

        @Override
        public void visitScript(FxScriptFragment script) {
            TextPositions pos = fxResult.getTreeUtilities().positions(script);
            createEmbedding(pos);
            super.visitScript(script); 
        }
        
        private void createEmbedding(TextPositions pos) {
            int skip = ts.move(pos.getContentStart());
            if (skip != 0) {
                System.err.println("");
            }

            List<Embedding> content = new LinkedList<Embedding>();

            while (ts.moveNext() && ts.offset() < pos.getContentEnd()) {
                Token<XMLTokenId> token = ts.token();
                XMLTokenId id = token.id();

                switch (id) {
                    case CHARACTER:
                        // uh-oh, not supported yet; treat as text
                    case TEXT:
                        content.add(
                            snapshot.create(
                                ts.offset(), token.length(), JAVASCRIPT_MIME
                            )
                        );
                        if (doc != null) {
                            ts.createEmbedding(Language.find(JAVASCRIPT_MIME), 0, 0, true);
                        }
                        break;

                    case CDATA_SECTION: {
                        int start = ts.offset();
                        int startPad = 0;
                        int endPad = 0;
                        int len = token.length();
                        CharSequence text = token.text();

                        if (len > 9 && text.subSequence(0, 9).toString().equals("<![CDATA[")) {
                            startPad = 9;
                        }
                        if (len > 3 && text.subSequence(len - 3, len).toString().equals("]]>")) {
                            endPad = 3;
                        }
                        content.add(
                            snapshot.create(
                                start + startPad, len - startPad - endPad, 
                                JAVASCRIPT_MIME
                            )
                        );
                        if (doc != null) {
                            ts.createEmbedding(Language.find(JAVASCRIPT_MIME), startPad, endPad);
                        }
                        break;
                    }
                }
                skip = 0;
            }
            if (content.isEmpty()) {
                return;
            }
            if (content.size() == 1) {
                embeddings.add(content.get(0));
            } else {
                embeddings.add(Embedding.create(content));
            }
        }
        
        @Override
        public void visitEvent(EventHandler eh) {
            if (!eh.isScript() || !eh.hasContent()) {
                return;
            }
            
            TextPositions pos = fxResult.getTreeUtilities().positions(eh);
            boolean attribute = fxResult.getTreeUtilities().isAttribute(eh);

            if (attribute) {
                // FIXME - no CHARACTER handling !!
                embeddings.add(
                    snapshot.create(
                        pos.getContentStart(), 
                        (pos.getContentEnd() - pos.getContentStart()),
                        JAVASCRIPT_MIME
                    )
                );
                if (doc != null) {
                    int skip = ts.move(pos.getContentStart());
                    ts.moveNext();
                    ts.createEmbedding(Language.find(JAVASCRIPT_MIME), skip, skip, true);
                }
            } else {
                createEmbedding(pos);
            }
        }
    }
    
    @MimeRegistration(
            mimeType=JavaFXEditorUtils.FXML_MIME_TYPE,
            service=TaskFactory.class
    )
    public static class Factory extends TaskFactory {
        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            if (Language.find(JAVASCRIPT_MIME) == null) {
                return Collections.emptyList();
            }
            return Collections.<SchedulerTask>singleton(new ScriptEmbeddingProvider());
        }
    }
}
