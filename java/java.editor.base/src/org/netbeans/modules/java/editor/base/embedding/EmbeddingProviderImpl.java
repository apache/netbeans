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
package org.netbeans.modules.java.editor.base.embedding;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaMultiLineStringTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserBasedEmbeddingProvider;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class EmbeddingProviderImpl extends ParserBasedEmbeddingProvider<Parser.Result> {

    public EmbeddingProviderImpl() {
        super();
    }

    @Override
    public List<Embedding> getEmbeddings(Parser.Result result) {
        CompilationController info = CompilationController.get(result);
        if (info == null) {
            return Collections.emptyList();
        }
        List<Embedding> embeddings = new ArrayList<>();
        TokenSequence<?> ts = TokenHierarchy.get(info.getSnapshot().getSource().getDocument(true)).tokenSequence(); //XXX: locking!
        ts.moveStart();
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.MULTILINE_STRING_LITERAL && ts.token().partType() == PartType.COMPLETE) {
                String language = findLanguageFromTarget(info, ts.offset() + 1);
                String mimeType = language != null ? mapLanguageToMimeType(language) : null;
                if (mimeType == null) {
                    continue;
                }
                List<Embedding> parts = embeddingsForTextBlockContent(result.getSnapshot(), ts, mimeType);
                if (!parts.isEmpty()) {
                    embeddings.add(Embedding.create(parts));
                }
            }
        }
        return embeddings;
    }

    private static String findLanguageFromTarget(CompilationController info, int pos) {
        try {
            info.toPhase(JavaSource.Phase.RESOLVED); //TODO - improve, avoid resolved if at all possible
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        TreePath tp = info.getTreeUtilities().pathFor(pos);
        if (tp.getLeaf().getKind() != Kind.STRING_LITERAL) {
            return null;
        }
        VariableElement target = null;
        switch (tp.getParentPath().getLeaf().getKind()) {
            case METHOD_INVOCATION: {
                int argPos = ((MethodInvocationTree) tp.getParentPath().getLeaf()).getArguments().indexOf(tp.getLeaf());
                if (argPos == (-1)) {
                    break;
                }
                Element el = info.getTrees().getElement(tp.getParentPath());
                if (el == null || (el.getKind() != ElementKind.METHOD && el.getKind() != ElementKind.CONSTRUCTOR)) {
                    break;
                }
                target = ((ExecutableElement) el).getParameters().get(argPos);
                break;
            }
            case VARIABLE: {
                target = (VariableElement) info.getTrees().getElement(tp.getParentPath());
                break;
            }
        }
        if (target == null) {
            return null;
        }
        for (AnnotationMirror am : target.getAnnotationMirrors()) {
            if (am.getAnnotationType().asElement().getSimpleName().contentEquals("Language")) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (e.getKey().getSimpleName().contentEquals("value")) {
                        return (String) e.getValue().getValue();
                    }
                }
            }
        }
        return target.getSimpleName().toString();
    }

    private static String mapLanguageToMimeType(String language) {
        String candidate = null;
        FileObject editors = FileUtil.getConfigFile("Editors");
        for (FileObject primary : editors.getChildren()) {
            for (FileObject secondary : primary.getChildren()) {
                if (secondary.getNameExt().equalsIgnoreCase(language)) {
                    return primary.getNameExt() + "/" + secondary.getNameExt();
                } else if (secondary.getNameExt().equalsIgnoreCase("x-" + language)) {
                    candidate = primary.getNameExt() + "/" + secondary.getNameExt();
                }
            }
        }
        return candidate;
    }

    private List<Embedding> embeddingsForTextBlockContent(Snapshot snapshot, TokenSequence<?> ts, String mimeType) {
        List<Embedding> result = new ArrayList<>();
        TokenSequence<?> nested = ts.embedded(JavaMultiLineStringTokenId.language());
        while (nested.moveNext()) {
            if (nested.token().id() == JavaMultiLineStringTokenId.INDENT) {
                continue;
            }
            //XXX: escapes!
            result.add(snapshot.create(nested.offset(), nested.token().length(), mimeType));
            nested.createEmbedding(Language.find(mimeType), 0, 0, true);
        }
        return result;
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        //TODO:
    }

    @MimeRegistration(mimeType="text/x-java", service=TaskFactory.class)
    public static final class FactoryImpl extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new EmbeddingProviderImpl());
        }

    }
}
