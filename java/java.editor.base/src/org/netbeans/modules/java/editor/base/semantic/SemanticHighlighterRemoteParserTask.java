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
package org.netbeans.modules.java.editor.base.semantic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterRemoteParserTask.HighlightData;
import org.netbeans.modules.java.source.remote.api.RemoteParserTask;
import org.openide.cookies.EditorCookie;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=RemoteParserTask.class)
public class SemanticHighlighterRemoteParserTask implements RemoteParserTask<HighlightData, CompilationInfo, Void> {

    @Override
    public Future<HighlightData> computeResult(CompilationInfo info, Void unused) throws IOException {
        SemanticHighlighterBase shb = new SemanticHighlighterBase() {
            @Override
            protected boolean process(CompilationInfo info, Document doc) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        return new SynchronousFuture<HighlightData>(() -> {
            EditorCookie ec = info.getFileObject().getLookup().lookup(EditorCookie.class);
            Map<Token, Coloring> semanticHighlights = new HashMap<>();
            StyledDocument doc = ec.openDocument();
            TokenSequence<?> ts = TokenHierarchy.get(doc).tokenSequence(); //XXX: embedding!
            shb.doCompute(info, doc, new SemanticHighlighterBase.ErrorDescriptionSetter() {
                @Override
                public void setHighlights(Document doc, Collection<int[]> highlights) {
                    //TODO: ????
                }
                @Override
                public void setColorings(Document doc, Map<Token, Coloring> colorings) {
                    semanticHighlights.putAll(colorings);
                }
            });

            return colorTokens(ts, semanticHighlights);
        }, () -> shb.cancel());
    }

    static HighlightData colorTokens(TokenSequence<?> ts, Map<Token, Coloring> semanticHighlights) throws IOException {
        List<Long> spans = new ArrayList<Long>(ts.tokenCount());
        List<String> cats  = new ArrayList<String>(ts.tokenCount());
        long currentOffset = 0;

        ts.moveStart();

        while (ts.moveNext()) {
            long endOffset = ts.offset() + ts.token().length();
            spans.add(endOffset - currentOffset);
            String category = "";
            Coloring coloring = semanticHighlights.get(ts.token());

            if (coloring != null) {
                for (ColoringAttributes ca : coloring) {
                    if (!category.isEmpty()) category += " ";
                    category += ca.name().toLowerCase();
                }
            }

            cats.add(category);

            currentOffset = endOffset;
        }

        return new HighlightData(cats, spans);
    }

    public static final class HighlightData {
        List<String> categories;
        List<Long> spans;

        public HighlightData() {
        }

        public HighlightData(List<String> cats, List<Long> spans) {
            this.categories = cats;
            this.spans = spans;
        }
    }

}
