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
package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.Completion;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class) //NOI18N
public class GenerateJavadocCollector implements CompletionCollector {

    @NbBundle.Messages({
        "DN_JavadocComment=Javadoc comment",
    })
    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        if (context != null && context.getTriggerKind() == Completion.TriggerKind.TriggerCharacter && context.getTriggerCharacter() == '*') {
            AtomicReference<TokenSequence<JavaTokenId>> ref = new AtomicReference<>();
            doc.render(() -> {
                ref.set(SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset));
            });
            TokenSequence<JavaTokenId> ts = ref.get();
            if (ts != null) {
                ts.move(offset);
                if (ts.moveNext() && ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                    int jdBeginOffset = ts.offset();
                    String text = ts.token().text().toString();
                    if (text.substring(3, text.length() - 2).trim().isEmpty()) {
                        JavaSource js = JavaSource.forDocument(doc);
                        if (js != null) {
                            FileObject file = js.getFileObjects().iterator().next();
                            SourceVersion sv = JavadocUtilities.resolveSourceVersion(file);
                            final JavadocGenerator gen = new JavadocGenerator(sv);
                            gen.updateSettings(file);
                            try {
                                js.runUserActionTask(new Task<CompilationController>() {
                                    @Override
                                    public void run(CompilationController cc) throws Exception {
                                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                        int offsetBehindJavadoc = ts.offset() + ts.token().length();
                                        while (ts.moveNext()) {
                                            JavaTokenId tid = ts.token().id();
                                            if (tid != JavaTokenId.WHITESPACE && tid != JavaTokenId.LINE_COMMENT && tid != JavaTokenId.BLOCK_COMMENT) {
                                                offsetBehindJavadoc = ts.offset() + 1;
                                                break;
                                            }
                                        }
                                        TreePath tp = cc.getTreeUtilities().pathFor(offsetBehindJavadoc);
                                        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())
                                                && tp.getLeaf().getKind() != Tree.Kind.METHOD && tp.getLeaf().getKind() != Tree.Kind.VARIABLE) {
                                            tp = tp.getParentPath();
                                        }
                                        if (tp != null && cc.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf()) >= jdBeginOffset) {
                                            Element el = cc.getTrees().getElement(tp);
                                            if (el != null) {
                                                String javadoc = gen.generateComment(el, cc);
                                                if (javadoc != null) {
                                                    StringBuilder sb = new StringBuilder("\n * ${0}"); //NOI18N
                                                    boolean first = true;
                                                    for (String s : javadoc.split("\n")) { //NOI18N
                                                        if (first && s.isEmpty()) {
                                                            sb.append('\n');
                                                        } else {
                                                            sb.append(" * ").append(s).append('\n'); //NOI18N
                                                        }
                                                        first = false;
                                                    }
                                                    consumer.accept(CompletionCollector.newBuilder(Bundle.DN_JavadocComment())
                                                            .kind(Completion.Kind.Snippet)
                                                            .insertText(sb.toString())
                                                            .insertTextFormat(Completion.TextFormat.Snippet)
                                                            .documentation(javadoc)
                                                            .build());
                                                }
                                            }
                                        }
                                    }
                                }, true);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
