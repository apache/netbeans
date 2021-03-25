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
package org.netbeans.modules.editor.java;

import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class)
public class JavaCompletionCollector implements CompletionCollector {

    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        AtomicBoolean ret = new AtomicBoolean(true);
        try {
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    TokenSequence<JavaTokenId> ts = resultIterator.getSnapshot().getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    if (ts.move(offset) == 0 || !ts.moveNext()) {
                        if (!ts.movePrevious()) {
                            ts.moveNext();
                        }
                    }
                    int len = offset - ts.offset();
                    boolean allCompletion = context != null && context.getTriggerKind() == Completion.TriggerKind.TriggerForIncompleteCompletions
                            || len > 0 && ts.token().length() >= len && ts.token().id() == JavaTokenId.IDENTIFIER;
                    CompilationController controller = CompilationController.get(resultIterator.getParserResult(ts.offset()));
                    controller.toPhase(JavaSource.Phase.RESOLVED);
                    JavaCompletionTask<Completion> task = JavaCompletionTask.create(offset, new ItemFactoryImpl(controller, ts.offset()), allCompletion ? EnumSet.of(JavaCompletionTask.Options.ALL_COMPLETION) : EnumSet.noneOf(JavaCompletionTask.Options.class), () -> false);
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
                    if (task.hasAdditionalClasses() || task.hasAdditionalMembers()) {
                        ret.set(false);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret.get();
    }

    private static class ItemFactoryImpl implements JavaCompletionTask.ItemFactory<Completion> {

        private static final Set<String> SUPPORTED_ELEMENT_KINDS = new HashSet<>(Arrays.asList("PACKAGE", "CLASS", "INTERFACE", "ENUM", "ANNOTATION_TYPE", "METHOD", "CONSTRUCTOR", "INSTANCE_INIT", "STATIC_INIT", "FIELD", "ENUM_CONSTANT", "TYPE_PARAMETER", "MODULE"));
        private static final int DEPRECATED = 10;
        private final Document doc;
        private final int offset;
        private final CompilationInfo info;
        private final Scope scope;

        public ItemFactoryImpl(CompilationInfo info, int offset) throws IOException {
            this.offset = offset;
            this.info = info;
            this.doc = info.getDocument();
            this.scope = info.getTrees().getScope(info.getTreeUtilities().pathFor(offset));
        }

        @Override
        public Completion createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
            return CompletionCollector.newBuilder(kwd)
                    .kind(Completion.Kind.Keyword)
                    .sortText(String.format("%4d%s", smartType ? 670 : 1670, kwd))
                    .insertText(kwd + postfix)
                    .build();
        }

        @Override
        public Completion createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            final String simpleName = pkgFQN.substring(pkgFQN.lastIndexOf('.') + 1);
            return CompletionCollector.newBuilder(simpleName)
                    .kind(Completion.Kind.Folder)
                    .sortText(String.format("%4d%s#%s", 1900, simpleName, pkgFQN))
                    .insertText(simpleName + (inPackageStatement ? "" : "."))
                    .build();
        }

        @Override
        public Completion createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
            String name = elem.getQualifiedName().toString();
            int idx = name.lastIndexOf('.');
            String pkgName = idx < 0 ? "" : name.substring(0, idx);
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(elem.getSimpleName().toString())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%4d%s#%2d#%s", smartType ? 800 : 1800, elem.getSimpleName().toString(), Utilities.getImportanceLevel(name), pkgName))
                    .detail(CompletableFuture.completedFuture(name));
            ElementHandle<TypeElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, handle))
                        .additionalTextEdits(addImport(doc, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends) {
            TypeElement te = handle.resolve(info);
            if (te != null && info.getTrees().isAccessible(scope, te)) {
                String name = handle.getQualifiedName();
                int idx = name.lastIndexOf('.');
                String pkgName = idx < 0 ? "" : name.substring(0, idx);
                return CompletionCollector.newBuilder(te.getSimpleName().toString())
                        .kind(elementKind2CompletionItemKind(handle.getKind()))
                        .sortText(String.format("%4d%s#%2d#%s", 1800, te.getSimpleName().toString(), Utilities.getImportanceLevel(name), pkgName))
                        .detail(CompletableFuture.completedFuture(name))
                        .documentation(getDocumentation(doc, handle))
                        .additionalTextEdits(addImport(doc, handle))
                        .build();
            }
            return null;
        }

        @Override
        public Completion createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements) {
            return null; //TODO: fill
        }

        @Override
        public Completion createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            return CompletionCollector.newBuilder(elem.getSimpleName().toString())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%4d%s", 1700, elem.getSimpleName().toString()))
                    .build();
        }

        @Override
        public Completion createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
            int priority = elem.getKind() == ElementKind.ENUM_CONSTANT || elem.getKind() == ElementKind.FIELD ? smartType ? 300 : 1300 : smartType ? 200 : 1200;
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(elem.getSimpleName().toString())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%4d%s", priority, elem.getSimpleName().toString()));
            ElementHandle<VariableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType) {
            return CompletionCollector.newBuilder(varName)
                    .kind(Completion.Kind.Variable)
                    .sortText(String.format("%4d%s", smartType ? 200 : 1200, varName))
                    .build();
        }

        @Override
        public Completion createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            StringBuilder label = new StringBuilder();
            String sep = "";
            label.append(elem.getSimpleName());
            label.append("(");
            StringBuilder sortParams = new StringBuilder();
            sortParams.append('(');
            int cnt = 0;
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                label.append(sep);
                String paramTypeName = Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString();
                label.append(paramTypeName);
                label.append(' ');
                label.append(it.next().getSimpleName());
                sep = ", ";
                sortParams.append(paramTypeName);
                if (tIt.hasNext()) {
                    sortParams.append(',');
                }
                cnt++;
            }
            label.append(") : ");
            sortParams.append(')');
            TypeMirror retType = type.getReturnType();
            label.append(Utilities.getTypeName(info, retType, false).toString());
            StringBuilder insertText = new StringBuilder();
            insertText.append(elem.getSimpleName());
            insertText.append("(");
            if (elem.getParameters().isEmpty()) {
                insertText.append(")");
            }
            int priority = elem.getKind() == ElementKind.METHOD ? smartType ? 500 : 1500 : smartType ? 650 : 1650;
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label.toString())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .insertText(insertText.toString())
                    .sortText(String.format("%4d%s#%2d%s", priority, elem.getSimpleName().toString(), cnt, sortParams));
            ElementHandle<ExecutableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
            Completion item = createExecutableItem(info, elem, type, substitutionOffset, null, false, isDeprecated, false, false, false, -1, false);
            String simpleName = name != null ? name : elem.getEnclosingElement().getSimpleName().toString();
            int idx = item.getLabel().indexOf('(');
            String label = simpleName + item.getLabel().substring(idx);
            idx = item.getInsertText().indexOf('(');
            String insertText = simpleName + item.getInsertText().substring(idx);
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label)
                    .kind(Completion.Kind.Constructor)
                    .insertText(insertText)
                    .sortText(String.format("%4d%s", name != null ? 1550 : 1650, item.getSortText().substring(4)));
            ElementHandle<ExecutableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, handle));
            }
            return builder.build();
        }

        @Override
        public Completion createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            Completion item = createExecutableItem(info, elem, type, substitutionOffset, null, false, false, false, false, false, -1, false);
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(String.format("%s - %s", item.getLabel(), implement ? "implement" : "override"))
                    .kind(elementKind2CompletionItemKind(elem.getKind()));
            ElementHandle<ExecutableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, handle));
            }
            try {
                List<TextEdit> textEdits = modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                    wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TreePath tp = wc.getTreeUtilities().pathFor(offset);
                    if (implement) {
                        GeneratorUtils.generateAbstractMethodImplementation(wc, tp, elem, offset);
                    } else {
                        GeneratorUtils.generateMethodOverride(wc, tp, elem, offset);
                    }
                });
                if (!textEdits.isEmpty()) {
                    builder.textEdit(textEdits.get(0));
                }
            } catch (IOException ex) {
            }
            return builder.build();
        }

        @Override
        public Completion createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
            return null; //TODO: fill
        }

        @Override
        public Completion createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            return null; //TODO: fill
        }

        @Override
        public Completion createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
            return null; //TODO: fill
        }

        @Override
        public Completion createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated) {
            return null; //TODO: fill
        }

        @Override
        public Completion createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            StringBuilder insertText = new StringBuilder();
            insertText.append(elem.getSimpleName());
            insertText.append("=");
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(elem.getSimpleName().toString())
                    .kind(Completion.Kind.Property)
                    .insertText(insertText.toString())
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .sortText(String.format("%4d%s", isDeprecated ? 100 + DEPRECATED : 100, elem.getSimpleName().toString()));
            ElementHandle<ExecutableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount) {
            return CompletionCollector.newBuilder(value)
                    .kind(Completion.Kind.Text)
                    .sortText(value)
                    .documentation(CompletableFuture.completedFuture(documentation))
                    .build();
        }

        private static final Object KEY_IMPORT_TEXT_EDITS = new Object();

        @Override
        public Completion createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            //TODO: prefer static imports (but would be much slower?)
            //TODO: should be resolveImport instead of addImports:
            Map<Element, List<TextEdit>> imports = (Map<Element, List<TextEdit>>) info.getCachedValue(KEY_IMPORT_TEXT_EDITS);
            if (imports == null) {
                info.putCachedValue(KEY_IMPORT_TEXT_EDITS, imports = new HashMap<>(), CompilationInfo.CacheClearPolicy.ON_TASK_END);
            }
            List<TextEdit> currentClassImport = imports.computeIfAbsent(type.asElement(), toImport -> {
                try {
                    return modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                        wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        wc.rewrite(info.getCompilationUnit(), GeneratorUtilities.get(wc).addImports(wc.getCompilationUnit(), new HashSet<>(Arrays.asList(toImport))));
                    });
                } catch (IOException ex) {
                    return Collections.emptyList();
                }
            });
            String label = type.asElement().getSimpleName() + "." + memberElem.getSimpleName();
            String sortText = memberElem.getSimpleName().toString();
            if (memberElem.getKind().isField()) {
                sortText += String.format("#%s", Utilities.getTypeName(info, type, false)); //NOI18N
            } else {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                while(tIt.hasNext()) {
                    TypeMirror tm = tIt.next();
                    if (tm == null) {
                        break;
                    }
                    sortParams.append(Utilities.getTypeName(info, tm, false, ((ExecutableElement)memberElem).isVarArgs() && !tIt.hasNext()).toString());
                    if (tIt.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText += String.format("#%2d#%s#s", cnt, sortParams.toString(), Utilities.getTypeName(info, type, false)); //NOI18N
            }
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label)
                    .kind(elementKind2CompletionItemKind(memberElem.getKind()))
                    .insertText(label)
                    .additionalTextEdits(CompletableFuture.completedFuture(currentClassImport))
                    .sortText(String.format("%4d%s", memberElem.getKind().isField() ? 720 : 750, sortText));
            ElementHandle<Element> handle = SUPPORTED_ELEMENT_KINDS.contains(memberElem.getKind().name()) ? ElementHandle.create(memberElem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source) {
            return null; //TODO: fill
        }

        @Override
        public Completion createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            return null; //TODO: fill
        }

        @Override
        public Completion createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
            return null; //TODO: fill
        }

        private static CompletableFuture<String> getDocumentation(Document doc, ElementHandle<?> handle) {
            return new LazyFuture(() -> {
                String[] ret = new String[1];
                ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                    public void run (ResultIterator resultIterator) throws Exception {
                        CompilationInfo info = CompilationInfo.get(resultIterator.getParserResult());
                        if (info != null) {
                            Element element = handle.resolve(info);
                            if (element != null) {
                                ret[0] = ElementJavadoc.create(info, element, null).getText();
                            }
                        }
                    }
                });
                return ret[0];
            });
        }

        private static CompletableFuture<List<TextEdit>> addImport(Document doc, ElementHandle<?> handle) {
            return new LazyFuture(() -> {
                return modify2TextEdits(JavaSource.forDocument(doc), copy -> {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    Element e = handle.resolve(copy);
                    if (e != null) {
                        copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), Collections.singleton(e)));
                    }
                });
            });
        }

        private static Completion.Kind elementKind2CompletionItemKind(ElementKind kind) {
            switch (kind) {
                case PACKAGE:
                    return Completion.Kind.Folder;
                case ENUM:
                    return Completion.Kind.Enum;
                case CLASS:
                    return Completion.Kind.Class;
                case ANNOTATION_TYPE:
                    return Completion.Kind.Interface;
                case INTERFACE:
                    return Completion.Kind.Interface;
                case ENUM_CONSTANT:
                    return Completion.Kind.EnumMember;
                case FIELD:
                    return Completion.Kind.Field;
                case PARAMETER:
                    return Completion.Kind.Variable;
                case LOCAL_VARIABLE:
                    return Completion.Kind.Variable;
                case EXCEPTION_PARAMETER:
                    return Completion.Kind.Variable;
                case METHOD:
                    return Completion.Kind.Method;
                case CONSTRUCTOR:
                    return Completion.Kind.Constructor;
                case TYPE_PARAMETER:
                    return Completion.Kind.TypeParameter;
                case RESOURCE_VARIABLE:
                    return Completion.Kind.Variable;
                case MODULE:
                    return Completion.Kind.Module;
                case STATIC_INIT:
                case INSTANCE_INIT:
                case OTHER:
                default:
                    return Completion.Kind.Text;
            }
        }

        private static List<TextEdit> modify2TextEdits(JavaSource js, Task<WorkingCopy> task) throws IOException {
            FileObject[] file = new FileObject[1];
            ModificationResult changes = js.runModificationTask(wc -> {
                task.run(wc);
                file[0] = wc.getFileObject();
            });
            List<? extends ModificationResult.Difference> diffs = changes.getDifferences(file[0]);
            if (diffs == null) {
                return Collections.emptyList();
            }
            return diffs.stream().map(diff -> {
                String newText = diff.getNewText();
                return new TextEdit(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset(), newText != null ? newText : "");
            }).collect(Collectors.toList());
        }

        private static class LazyFuture<T> extends CompletableFuture<T> {
            private final Callable<T> callable;

            private LazyFuture(Callable<T> callable) {
                this.callable = callable;
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                try {
                    this.complete(callable.call());
                } catch (Exception ex) {
                    this.completeExceptionally(ex);
                }
                return super.get();
            }
        }
    }
}
