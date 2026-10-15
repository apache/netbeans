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
package org.netbeans.modules.java.editor.semantic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lsp.InlayHint;
import org.netbeans.api.lsp.Position;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase;
import org.netbeans.spi.lsp.InlayHintsProvider;
import org.openide.util.Pair;

@MimeRegistration(mimeType="text/x-java", service=InlayHintsProvider.class)
public class InlayHintsProviderImpl implements InlayHintsProvider {

    private static final Set<String> SUPPORTED_HINT_TYPES = Set.of(
        "parameter", "chained", "var"
    );

    @Override
    public Set<String> supportedHintTypes() {
        return SUPPORTED_HINT_TYPES;
    }

    @Override
    public CompletableFuture<List<? extends InlayHint>> inlayHints(Context context) {
        CompletableFuture<List<? extends InlayHint>> result = new CompletableFuture<>();
        JavaSource js = JavaSource.forFileObject(context.getFile());

        if (js != null) {
            List<InlayHint> hints = new ArrayList<>();
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    SemanticHighlighterBase.Settings settings =
                            new SemanticHighlighterBase.Settings(context.getRequestedHintTypes().contains("parameter"),
                                                                 context.getRequestedHintTypes().contains("chained"),
                                                                 context.getRequestedHintTypes().contains("var"));
                    Document doc = cc.getSnapshot().getSource().getDocument(true);
                    int start = context.getRange().getStartOffset();
                    int end = context.getRange().getEndOffset();
                    new SemanticHighlighterBase() {
                        @Override
                        protected boolean process(CompilationInfo info, Document doc) {
                            process(info, doc, settings, new SemanticHighlighterBase.ErrorDescriptionSetter() {
                                @Override
                                public void setHighlights(Document doc, Collection<Pair<int[], ColoringAttributes.Coloring>> highlights, Map<int[], String> preText) {
                                    for (Map.Entry<int[], String> e : preText.entrySet()) {
                                        if (e.getKey()[0] >= start && e.getKey()[0] <= end) {
                                            InlayHint hint = new InlayHint(new PositionImpl(e.getKey()[0]), e.getValue());
                                            hints.add(hint);
                                        }
                                    }
                                }

                                @Override
                                public void setColorings(Document doc, Map<Token, ColoringAttributes.Coloring> colorings) {
                                    //...nothing
                                }
                            });
                            return true;
                        }
                    }.process(cc, doc);
                }, true);
                result.complete(hints);
            } catch (IOException ex) {
                result.completeExceptionally(ex);
            }
        }
        return result;
    }

    private static final class PositionImpl implements Position {
        private final int offset;

        public PositionImpl(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }
    }
}
