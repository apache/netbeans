/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.editor.javadoc;

import com.sun.source.tree.Scope;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.editor.java.JavaCompletionCollector;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.base.javadoc.JavadocCompletionUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class)
public class JavadocCompletionCollector implements CompletionCollector {

    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        AtomicBoolean ret = new AtomicBoolean(true);
        if ((context == null || context.getTriggerKind() != Completion.TriggerKind.TriggerCharacter || context.getTriggerCharacter() == '#' || context.getTriggerCharacter() == '@')
                && JavadocCompletionUtils.isJavadocContext(doc, offset)) {
            try {
                ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        JavadocCompletionTask<Completion> task = JavadocCompletionTask.create(offset, new ItemFactoryImpl(controller, offset),
                                context != null && context.getTriggerKind() == Completion.TriggerKind.TriggerForIncompleteCompletions, () -> false);
                        task.run(resultIterator);
                        List<Completion> results = task.getResults();
                        if (results != null) {
                            for (Iterator<Completion> it = results.iterator(); it.hasNext();) {
                                Completion item = it.next();
                                if (item == null) {
                                    it.remove();
                                }
                            }
                            results.forEach(consumer);
                        }
                        if (task.hasAdditionalItems()) {
                            ret.set(false);
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return ret.get();
    }

    private static class ItemFactoryImpl implements JavadocCompletionTask.ItemFactory<Completion> {

        private final CompilationInfo info;
        private final Document doc;
        private final int offset;
        private final Scope scope;

        public ItemFactoryImpl(CompilationInfo info, int offset) throws IOException {
            this.info = info;
            this.doc = info.getDocument();
            this.offset = offset;
            this.scope = getScope();
        }

        @Override
        public Completion createTagItem(String name, int startOffset) {
            Builder builder = CompletionCollector.newBuilder(name)
                    .kind(Completion.Kind.Keyword)
                    .sortText(String.format("%04d%s", 1500, name))
                    .insertTextFormat(Completion.TextFormat.PlainText);
            if (startOffset < offset) {
                builder.textEdit(new TextEdit(startOffset, offset, name + ' '));
            } else {
                builder.insertText(name + ' ');
            }
            return builder.build();
        }

        @Override
        public Completion createNameItem(String name, int startOffset) {
            return CompletionCollector.newBuilder(name)
                    .kind(name.charAt(0) == '<' ? Completion.Kind.TypeParameter : Completion.Kind.Variable)
                    .insertText(name + ' ')
                    .sortText(String.format("%04d%s", 1100, name))
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .build();
        }

        @Override
        public Completion createJavadocExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int startOffset, boolean isInherited, boolean isDeprecated) {
            String simpleName = (elem.getKind() == ElementKind.METHOD ? elem : elem.getEnclosingElement()).getSimpleName().toString();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            StringBuilder label = new StringBuilder();
            StringBuilder insertText = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            label.append(simpleName).append('(');
            insertText.append(simpleName).append('(');
            sortParams.append('(');
            int cnt = 0;
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                cnt++;
                String paramTypeName = Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString();
                String paramName = it.next().getSimpleName().toString();
                label.append(paramTypeName).append(' ').append(paramName);
                sortParams.append(paramTypeName);
                insertText.append(paramTypeName);
                if (tIt.hasNext()) {
                    label.append(", ");
                    sortParams.append(',');
                    insertText.append(", ");
                }
            }
            sortParams.append(')');
            label.append(')');
            insertText.append(')');
            TypeMirror retType = type.getReturnType();
            if (elem.getKind() == ElementKind.METHOD) {
                label.append(" : ").append(Utilities.getTypeName(info, retType, false).toString());
            }
            int priority = elem.getKind() == ElementKind.METHOD ? 1500 : 1650;
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label.toString())
                    .kind(JavaCompletionCollector.elementKind2CompletionItemKind(elem.getKind()))
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .sortText(String.format("%04d%s#%02d%s", priority, simpleName, cnt, sortParams.toString()))
                    .insertText(insertText.toString());

            ElementHandle<ExecutableElement> handle = JavaCompletionCollector.SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(JavaCompletionCollector.getDocumentation(doc, offset, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createJavadocTypeItem(CompilationInfo info, TypeElement elem, int startOffset, boolean isDeprecated) {
            ElementHandle<TypeElement> handle = JavaCompletionCollector.SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            return createTypeItem(handle, elem, (DeclaredType) elem.asType(), '#', null, isDeprecated, false);
        }

        @Override
        public Completion createJavaTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int startOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean smartType) {
            ElementHandle<TypeElement> handle = JavaCompletionCollector.SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            return createTypeItem(handle, elem, type, (char) -1, referencesCount, isDeprecated, false);
        }

        @Override
        public Completion createLazyTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int startOffset, ReferencesCount referencesCount, Source source) {
            TypeElement te = handle.resolve(info);
            if (te != null && (scope == null || info.getTrees().isAccessible(scope, te)) && JavaCompletionCollector.isOfKind(te, kinds) && (!JavaCompletionCollector.isInDefaultPackage(te) || JavaCompletionCollector.isInDefaultPackage(scope.getEnclosingClass()))) {
                return createTypeItem(handle, te, (DeclaredType) te.asType(), (char) -1, referencesCount, info.getElements().isDeprecated(te), false);
            }
            return null;
        }

        @Override
        public Completion createJavaVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int startOffset, boolean isInherited, boolean isDeprecated) {
            int priority = elem.getKind() == ElementKind.ENUM_CONSTANT || elem.getKind() == ElementKind.FIELD ? 1300 : 1200;
            StringBuilder label = new StringBuilder();
            label.append(elem.getSimpleName());
            if (type != null) {
                label.append(" : ").append(Utilities.getTypeName(info, type, false));
            }
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label.toString())
                    .kind(JavaCompletionCollector.elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%04d%s", priority, elem.getSimpleName().toString()))
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .insertText(elem.getSimpleName().toString());
            if (type != null && !type.getKind().isPrimitive()) {
                builder.addCommitCharacter('.');
            }
            ElementHandle<VariableElement> handle = JavaCompletionCollector.SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(JavaCompletionCollector.getDocumentation(doc, offset, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createPackageItem(String pkgFQN, int startOffset) {
            final String simpleName = pkgFQN.substring(pkgFQN.lastIndexOf('.') + 1);
            return CompletionCollector.newBuilder(simpleName)
                    .kind(Completion.Kind.Folder)
                    .sortText(String.format("%04d%s#%s", 1900, simpleName, pkgFQN))
                    .insertText(simpleName + '.')
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .build();
        }

        private Completion createTypeItem(ElementHandle<TypeElement> handle, TypeElement elem, DeclaredType type, char commitChar, ReferencesCount referencesCount, boolean isDeprecated, boolean smartType) {
            String name = elem.getQualifiedName().toString();
            int idx = name.lastIndexOf('.');
            String pkgName = idx < 0 ? "" : name.substring(0, idx);
            StringBuilder label = new StringBuilder();
            StringBuilder insertText = new StringBuilder();
            label.append(elem.getSimpleName());
            if (pkgName.length() > 0) {
                label.append(" (").append(pkgName).append(')');
            }
            if (referencesCount == null) {
                insertText.append(elem.getSimpleName());
            } else {
                if ((type == null || type.getKind() != TypeKind.ERROR) &&
                        EnumSet.range(ElementKind.PACKAGE, ElementKind.INTERFACE).contains(elem.getEnclosingElement().getKind())) {
                    insertText.append(elem.getSimpleName());
                } else {
                    insertText.append(elem.getQualifiedName());
                }
            }
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label.toString())
                    .kind(JavaCompletionCollector.elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%04d%s#%02d#%s", smartType ? 800 : 1800, elem.getSimpleName().toString(), Utilities.getImportanceLevel(name), pkgName))
                    .insertText(insertText.toString())
                    .insertTextFormat(Completion.TextFormat.PlainText);
            if (commitChar != -1) {
                builder.addCommitCharacter('#');
            }
            if (handle != null) {
                builder.documentation(JavaCompletionCollector.getDocumentation(doc, offset, handle));
                builder.additionalTextEdits(JavaCompletionCollector.addImport(doc, offset, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        private Scope getScope() {
            try {
                return info.getTrees().getScope(info.getTreeUtilities().pathFor(offset));
            } catch (Exception e) {}
            return null;
        }
    }
}
