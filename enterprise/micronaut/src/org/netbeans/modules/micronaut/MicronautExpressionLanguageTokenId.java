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
package org.netbeans.modules.micronaut;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.textmate.lexer.api.GrammarRegistration;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dusan Balek
 */
@GrammarRegistration(mimeType = "text/x-micronaut-el", grammar = "./resources/mexp.tmLanguage.json")
public enum MicronautExpressionLanguageTokenId implements TokenId {

    EXPRESSION_LANGUAGE,
    STRING;

    @Override
    public String primaryCategory() {
        return "string";
    }

    private static final Language<?> micronautExpressionLanguage = Language.find("text/x-micronaut-el");
    private static final Language<?> javaString = Language.find("text/x-java-string");
    private static final Language<MicronautExpressionLanguageTokenId> language = new LanguageHierarchy<MicronautExpressionLanguageTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-micronaut-el-wrapper";
        }

        @Override
        protected Collection<MicronautExpressionLanguageTokenId> createTokenIds() {
            return EnumSet.allOf(MicronautExpressionLanguageTokenId.class);
        }

        @Override
        protected Lexer<MicronautExpressionLanguageTokenId> createLexer(LexerRestartInfo<MicronautExpressionLanguageTokenId> info) {
            return new MicronautExpressionLanguageLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<MicronautExpressionLanguageTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case EXPRESSION_LANGUAGE:
                    return micronautExpressionLanguage != null ? LanguageEmbedding.create(micronautExpressionLanguage, 0, 0) : null;
                case STRING:
                    return javaString != null ? LanguageEmbedding.create(javaString, 0, 0) : null;
            }
            return null;
        }
    }.language();

    public static Language<MicronautExpressionLanguageTokenId> language() {
        return language;
    }

    @MimeRegistration(mimeType = "text/x-java", service = TaskFactory.class)
    public static final class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new MicronautExpressionLanguageEmbeddingProvider());
        }
    }

    private static class MicronautExpressionLanguageLexer implements Lexer {

        private final LexerRestartInfo<MicronautExpressionLanguageTokenId> info;
        private boolean insideEL;

        private MicronautExpressionLanguageLexer(LexerRestartInfo<MicronautExpressionLanguageTokenId> info) {
            this.info = info;
        }

        @Override
        public Token nextToken() {
            int c;
            while ((c = info.input().read()) != LexerInput.EOF) {
                if (insideEL && c == '}') {
                    insideEL = false;
                    return info.tokenFactory().createToken(MicronautExpressionLanguageTokenId.EXPRESSION_LANGUAGE, info.input().readLength() - 1);
                } else if (info.input().readText().toString().endsWith("#{")) {
                    insideEL = true;
                    return info.tokenFactory().createToken(MicronautExpressionLanguageTokenId.STRING, info.input().readLength());
                }
            }
            if (info.input().readLength() == 0) {
                return null;
            }
            return info.tokenFactory().createToken(MicronautExpressionLanguageTokenId.STRING);
        }

        @Override
        public Object state() {
            return insideEL;
        }

        @Override
        public void release() {
        }
    }

    private static class MicronautExpressionLanguageEmbeddingProvider extends JavaParserResultTask<Parser.Result> {

        private static final String EXPRESSION_CLASS = "io.micronaut.core.expressions.EvaluatedExpression";
        private final AtomicBoolean canceled = new AtomicBoolean();

        private MicronautExpressionLanguageEmbeddingProvider() {
            super(JavaSource.Phase.ELEMENTS_RESOLVED);
        }

        @Override
        public void run(Parser.Result result, SchedulerEvent event) {
            final CompilationInfo ci = CompilationInfo.get(result);
            if (ci != null && !canceled.get()) {
                TypeElement expression = ci.getElements().getTypeElement(EXPRESSION_CLASS);
                if (expression != null) {
                    final List<Pair<Integer, Integer>> literalBounds = new ArrayList<>();
                    final SourcePositions sp = ci.getTrees().getSourcePositions();
                    new TreePathScanner<Void, List<Pair<Integer, Integer>>>() {
                        @Override
                        public Void visitLiteral(LiteralTree node, List<Pair<Integer, Integer>> p) {
                            if (!canceled.get() && node.getValue() instanceof String) {
                                TreePath path = this.getCurrentPath();
                                if (path.getParentPath() != null && path.getParentPath().getLeaf().getKind() == Tree.Kind.ASSIGNMENT
                                        && path.getParentPath().getParentPath() != null && path.getParentPath().getParentPath().getLeaf().getKind() == Tree.Kind.ANNOTATION) {
                                    p.add(Pair.of((int) sp.getStartPosition(path.getCompilationUnit(), node), (int) sp.getEndPosition(path.getCompilationUnit(), node)));
                                }
                            }
                            return null;
                        }
                    }.scan(ci.getCompilationUnit(), literalBounds);
                    if (!literalBounds.isEmpty()) {
                        try {
                            final Document doc = ci.getDocument();
                            if (doc != null) {
                                RequestProcessor.getDefault().post(() -> {
                                    Runnable runn = () -> {
                                        final TokenHierarchy<Document> hierarchy = TokenHierarchy.get(doc);
                                        final Language<?> java = Language.find("text/x-java");
                                        if (java != null && micronautExpressionLanguage != null) {
                                            final TokenSequence<?> ts = hierarchy.tokenSequence(java);
                                            if (ts != null) {
                                                for (Pair<Integer, Integer> bound : literalBounds) {
                                                    ts.move(bound.first());
                                                    while (ts.moveNext() && ts.offset() < bound.second()) {
                                                        TokenSequence<?> embedded = ts.embedded();
                                                        if (embedded != null) {
                                                            if ("text/x-java-string".equals(embedded.language().mimeType())) {
                                                                ts.removeEmbedding(embedded.language());
                                                                ts.createEmbedding(language(), 1, 1, true);
                                                            }
                                                        } else {
                                                            ts.createEmbedding(language(), 1, 1, true);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    };
                                    LineDocumentUtils.asRequired(doc, AtomicLockDocument.class).runAtomic(runn);
                                });
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }

        @Override
        public int getPriority() {
            return 999;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            canceled.set(true);
        }
    }
}
