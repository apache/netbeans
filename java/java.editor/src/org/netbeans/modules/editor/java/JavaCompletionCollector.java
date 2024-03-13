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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.Command;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.completion.JavaDocumentationTask;
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
        if ((context == null || context.getTriggerKind() != Completion.TriggerKind.TriggerCharacter || context.getTriggerCharacter() == '.')
                && Utilities.isJavaContext(doc, offset, true)) {
            try {
                ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(resultIterator.getSnapshot().getTokenHierarchy(), offset);
                        if (ts.move(offset) == 0 || !ts.moveNext()) {
                            if (!ts.movePrevious()) {
                                ts.moveNext();
                            }
                        }
                        int len = offset - ts.offset();
                        boolean combinedCompletion = context != null && context.getTriggerKind() == Completion.TriggerKind.TriggerForIncompleteCompletions
                                || len > 0 && ts.token().length() >= len && ts.token().id() == JavaTokenId.IDENTIFIER;
                        CompilationController controller = CompilationController.get(resultIterator.getParserResult(ts.offset()));
                        controller.toPhase(JavaSource.Phase.RESOLVED);
                        JavaCompletionTask<Completion> task = JavaCompletionTask.create(offset, new ItemFactoryImpl(controller, offset), combinedCompletion ? EnumSet.of(JavaCompletionTask.Options.COMBINED_COMPLETION) : EnumSet.noneOf(JavaCompletionTask.Options.class), () -> false);
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
        }
        return ret.get();
    }

    public static final Set<String> SUPPORTED_ELEMENT_KINDS = new HashSet<>(Arrays.asList("PACKAGE", "CLASS", "INTERFACE", "ENUM", "ANNOTATION_TYPE", "RECORD", "METHOD", "CONSTRUCTOR", "INSTANCE_INIT", "STATIC_INIT", "FIELD", "ENUM_CONSTANT", "TYPE_PARAMETER", "MODULE"));

    public static Supplier<String> getDocumentation(Document doc, int offset, ElementHandle handle) {
        return () -> {
            try {
                JavaDocumentationTask<Future<String>> task = JavaDocumentationTask.create(offset, handle, new JavaDocumentationTask.DocumentationFactory<Future<String>>() {
                    @Override
                    public Future<String> create(CompilationInfo compilationInfo, Element element, Callable<Boolean> cancel) {
                        ElementJavadoc doc = ElementJavadoc.create(compilationInfo, element, cancel);
                        return ((CompletableFuture<String>) doc.getTextAsync()).thenApplyAsync(content -> {
                            return Utilities.resolveLinks(content, doc);
                        });
                    }
                }, () -> false);
                ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        task.run(resultIterator);
                    }
                });
                return task.getDocumentation().get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static Completion.Kind elementKind2CompletionItemKind(ElementKind kind) {
        switch (kind) {
            case PACKAGE:
                return Completion.Kind.Folder;
            case ENUM:
                return Completion.Kind.Enum;
            case CLASS:
                return Completion.Kind.Class;
            case RECORD:
                return Completion.Kind.Struct;
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

    public static Supplier<List<TextEdit>> addImport(Document doc, int offset, ElementHandle<?> handle) {
        return () -> {
            AtomicReference<String> pkg = new AtomicReference<>();
            List<TextEdit> textEdits = modify2TextEdits(JavaSource.forDocument(doc), copy -> {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                String fqn = SourceUtils.resolveImport(copy, copy.getTreeUtilities().pathFor(offset), handle.getQualifiedName());
                if (fqn != null) {
                    int idx = fqn.lastIndexOf('.');
                    if (idx >= 0) {
                        pkg.set(fqn.substring(0, idx + 1));
                    }
                }
            });
            if (textEdits.isEmpty() && pkg.get() != null) {
                textEdits.add(new TextEdit(offset, offset, pkg.get()));
            }
            return textEdits;
        };
    }

    public static boolean isOfKind(Element e, EnumSet<ElementKind> kinds) {
        if (kinds.contains(e.getKind())) {
            return true;
        }
        for (Element ee : e.getEnclosedElements()) {
            if (isOfKind(ee, kinds)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInDefaultPackage(Element e) {
        while (e != null && e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return e != null && e.getSimpleName().length() == 0;
    }

    private static List<TextEdit> modify2TextEdits(JavaSource js, Task<WorkingCopy> task) {
        List<TextEdit> edits = new ArrayList<>();
        try {
            FileObject[] file = new FileObject[1];
            ModificationResult changes = js.runModificationTask(wc -> {
                task.run(wc);
                file[0] = wc.getFileObject();
            });
            List<? extends ModificationResult.Difference> diffs = changes.getDifferences(file[0]);
            if (diffs != null) {
                for (ModificationResult.Difference diff : diffs) {
                    edits.add(new TextEdit(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset(), diff.getNewText()));
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return edits;
    }

    private static class ItemFactoryImpl implements JavaCompletionTask.TypeCastableItemFactory<Completion>,
            JavaCompletionTask.LambdaItemFactory<Completion>, JavaCompletionTask.ModuleItemFactory<Completion>,
            JavaCompletionTask.RecordPatternItemFactory<Completion> {

        private static final String EMPTY = "";
        private static final String ERROR = "<error>";
        private static final int DEPRECATED = 10;
        private final Document doc;
        private final int offset;
        private final CompilationInfo info;
        private final TreePath treePath;
        private final Scope scope;
        private List<Element> locals = null;

        public ItemFactoryImpl(CompilationInfo info, int offset) throws IOException {
            this.offset = offset;
            this.info = info;
            this.doc = info.getDocument();
            this.treePath = info.getTreeUtilities().pathFor(offset);
            this.scope = info.getTrees().getScope(treePath);
        }

        @Override
        public Completion createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
            return CompletionCollector.newBuilder(kwd)
                    .kind(Completion.Kind.Keyword)
                    .sortText(String.format("%04d%s", smartType ? 670 : 1670, kwd))
                    .insertText(postfix != null ? kwd + postfix : kwd)
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .build();
        }

        @Override
        public Completion createModuleItem(String moduleName, int substitutionOffset) {
            return CompletionCollector.newBuilder(moduleName)
                    .kind(Completion.Kind.Folder)
                    .sortText(String.format("%04d%s", 1950, moduleName))
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .build();
        }

        @Override
        public Completion createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            final String simpleName = pkgFQN.substring(pkgFQN.lastIndexOf('.') + 1);
            return CompletionCollector.newBuilder(simpleName)
                    .kind(Completion.Kind.Folder)
                    .sortText(String.format("%04d%s#%s", 1900, simpleName, pkgFQN))
                    .insertText(simpleName + (inPackageStatement ? EMPTY : "."))
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .build();
        }

        @Override
        public Completion createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
            ElementHandle<TypeElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            return createTypeItem(info, null, handle, elem, type, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType);
        }

        @Override
        public Completion createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends) {
            TypeElement te = handle.resolve(info);
            if (te != null && info.getTrees().isAccessible(scope, te) && isOfKind(te, kinds) && (!afterExtends || !te.getModifiers().contains(Modifier.FINAL)) && (!isInDefaultPackage(te) || isInDefaultPackage(scope.getEnclosingClass()))) {
                return createTypeItem(info, null, handle, te, (DeclaredType) te.asType(), substitutionOffset, referencesCount, info.getElements().isDeprecated(te), insideNew, addTypeVars, false, false);
            }
            return null;
        }

        @Override
        public Completion createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements) {
            StringBuilder label = new StringBuilder();
            StringBuilder insertText = new StringBuilder();
            TypeMirror tm = type;
            int cnt = 1;
            VariableElement inst = instanceOf(info.getTypes().getPrimitiveType(TypeKind.INT), null);
            while(tm.getKind() == TypeKind.ARRAY) {
                tm = ((ArrayType)tm).getComponentType();
                label.append("[]");
                if (inst != null) {
                    insertText.append("[${").append(cnt++).append(":").append(inst.getSimpleName()).append("}]");
                } else {
                    insertText.append("[$").append(cnt++).append("]");
                }
            }
            insertText.append("$0");
            if (tm.getKind().isPrimitive()) {
                String kwd = tm.toString();
                return CompletionCollector.newBuilder(label.insert(0, kwd).toString())
                        .kind(Completion.Kind.Keyword)
                        .sortText(String.format("%04d%s", 670, kwd))
                        .insertText(insertText.insert(0, kwd).toString())
                        .insertTextFormat(Completion.TextFormat.Snippet)
                        .build();
            }
            if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ERROR) {
                DeclaredType dt = (DeclaredType)tm;
                TypeElement elem = (TypeElement)dt.asElement();
                String name = elem.getQualifiedName().toString();
                int idx = name.lastIndexOf('.');
                String pkgName = idx < 0 ? EMPTY : name.substring(0, idx);
                CompletionCollector.Builder builder = CompletionCollector.newBuilder(new StringBuilder(label).insert(0, elem.getSimpleName()).toString())
                        .kind(elementKind2CompletionItemKind(elem.getKind()))
                        .sortText(String.format("%04d%s#%02d#%s", 800, elem.getSimpleName().toString(), Utilities.getImportanceLevel(name), pkgName))
                        .insertText(insertText.insert(0, elem.getSimpleName()).toString())
                        .insertTextFormat(Completion.TextFormat.Snippet)
                        .detail(label.insert(0, elem.getQualifiedName()).toString());
                ElementHandle<TypeElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
                if (handle != null) {
                    builder.documentation(getDocumentation(doc, offset, handle));
                }
                if (elements.isDeprecated(elem)) {
                    builder.addTag(Completion.Tag.Deprecated);
                }
                return builder.build();
            }
            return null;
        }

        @Override
        public Completion createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            return CompletionCollector.newBuilder(elem.getSimpleName().toString())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%04d%s", 1700, elem.getSimpleName().toString()))
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .build();
        }

        @Override
        public Completion createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
            return createTypeCastableVariableItem(info, elem, type, null, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset);
        }

        @Override
        public Completion createTypeCastableVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
            int priority = elem.getKind() == ElementKind.ENUM_CONSTANT || elem.getKind() == ElementKind.FIELD ? smartType ? 300 : 1300 : smartType ? 200 : 1200;
            StringBuilder label = new StringBuilder();
            label.append(elem.getSimpleName());
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label.toString())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%04d%s", priority, elem.getSimpleName().toString()))
                    .insertTextFormat(Completion.TextFormat.PlainText);
            if (type != null) {
                builder.labelDescription(Utilities.getTypeName(info, type, false).toString());
            }
            TextEdit textEdit = null;
            String filter = null;
            if (castType != null) {
                int castStartOffset = assignToVarOffset;
                TreePath tp = info.getTreeUtilities().pathFor(substitutionOffset);
                if (castStartOffset < 0) {
                    if (tp != null && tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                        castStartOffset = (int)info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                    }
                }
                StringBuilder castText = new StringBuilder();
                castText.append("((").append(AutoImport.resolveImport(info, tp, castType)).append(CodeStyle.getDefault(doc).spaceAfterTypeCast() ? ") " : ")");
                int castEndOffset = findCastEndPosition(info.getTokenHierarchy().tokenSequence(JavaTokenId.language()), castStartOffset, substitutionOffset);
                if (castEndOffset >= 0) {
                    castText.append(info.getText().subSequence(castStartOffset, castEndOffset)).append(")");
                    castText.append(info.getText().subSequence(castEndOffset, substitutionOffset)).append(elem.getSimpleName());
                    textEdit = new TextEdit(castStartOffset, offset, castText.toString());
                    filter = info.getText().substring(castStartOffset, substitutionOffset) + elem.getSimpleName().toString();
                }
            }
            if (textEdit != null && filter != null) {
                builder.textEdit(textEdit)
                        .filterText(filter);
            } else {
                builder.insertText(elem.getSimpleName().toString());
            }
            if (type != null && !type.getKind().isPrimitive()) {
                builder.addCommitCharacter('.');
            }
            ElementHandle<VariableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, offset, handle));
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
                    .sortText(String.format("%04d%s", smartType ? 200 : 1200, varName))
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .build();
        }

        @Override
        public Completion createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
            return createExecutableItem(info, elem, type, null, null, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, smartType, assignToVarOffset, memberRef);
        }

        @Override
        public Completion createTypeCastableExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
            return createExecutableItem(info, elem, type, null, castType, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, smartType, assignToVarOffset, memberRef);
        }

        @Override
        public Completion createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
            return createExecutableItem(info, elem, type, name, null, substitutionOffset, null, false, isDeprecated, false, false, false, -1, false);
        }

        @Override
        public Completion createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            Completion item = createExecutableItem(info, elem, type, substitutionOffset, null, false, false, false, false, false, -1, false);
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(item.getLabel())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .labelDetail(String.format("%s - %s", item.getLabelDetail(), implement ? "implement" : "override"))
                    .labelDescription(item.getLabelDescription())
                    .sortText(item.getSortText())
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .textEdit(new TextEdit(substitutionOffset, substitutionOffset, EMPTY))
                    .additionalTextEdits(() -> modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                            wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            TreePath tp = wc.getTreeUtilities().pathFor(substitutionOffset);
                            if (implement) {
                                GeneratorUtils.generateAbstractMethodImplementation(wc, tp, elem, substitutionOffset);
                            } else {
                                GeneratorUtils.generateMethodOverride(wc, tp, elem, substitutionOffset);
                            }
                        }));
            ElementHandle<ExecutableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, offset, handle));
            }
            return builder.build();
        }

        @Override
        public Completion createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
            String typeName = Utilities.getTypeName(info, type, false).toString();
            StringBuilder labelDetail = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            labelDetail.append('(');
            sortParams.append('(');
            if (setter) {
                CodeStyle cs = CodeStyle.getDefault(doc);
                boolean isStatic = elem.getModifiers().contains(Modifier.STATIC);
                String simpleName = CodeStyleUtils.removePrefixSuffix(elem.getSimpleName(),
                    isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                    isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
                String paramName = CodeStyleUtils.addPrefixSuffix(
                        simpleName,
                        cs.getParameterNamePrefix(),
                        cs.getParameterNameSuffix());
                labelDetail.append(typeName).append(' ').append(paramName);
                sortParams.append(typeName);
            }
            labelDetail.append(") - generate");
            Builder builder = CompletionCollector.newBuilder(name)
                    .kind(Completion.Kind.Method)
                    .labelDetail(labelDetail.toString())
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .sortText(String.format("%04d%s#%02d%s", 1500, name, setter ? 1 : 0, sortParams.toString()))
                    .textEdit(new TextEdit(substitutionOffset, substitutionOffset, EMPTY))
                    .additionalTextEdits(() -> modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                        wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TreePath tp = wc.getTreeUtilities().pathFor(substitutionOffset);
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                            if (Utilities.inAnonymousOrLocalClass(tp)) {
                                wc.toPhase(JavaSource.Phase.RESOLVED);
                            }
                            TypeElement te = (TypeElement)wc.getTrees().getElement(tp);
                            if (te != null) {
                                GeneratorUtilities gu = GeneratorUtilities.get(wc);
                                MethodTree method = setter ? gu.createSetter(te, elem) : gu.createGetter(te, elem);
                                ClassTree decl = GeneratorUtils.insertClassMember(wc, (ClassTree)tp.getLeaf(), method, substitutionOffset);
                                wc.rewrite(tp.getLeaf(), decl);
                            }
                        }
                    }));
            if (!setter) {
                builder.labelDescription(typeName);
            }
            return builder.build();
        }

        @Override
        public Completion createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            Builder builder = CompletionCollector.newBuilder(elem.getSimpleName().toString())
                    .kind(Completion.Kind.Constructor)
                    .labelDetail("()")
                    .sortText(String.format("%04d%s#0", smartType ? 650 : 1650, elem.getSimpleName().toString()));
            StringBuilder insertText = new StringBuilder();
            if (substitutionOffset < offset) {
                insertText.append((elem.getSimpleName()));
            }
            insertText.append(CodeStyle.getDefault(doc).spaceBeforeMethodCallParen() ? " ()" : "()");
            if (elem.getModifiers().contains(Modifier.ABSTRACT)) {
                try {
                    if (CodeStyle.getDefault(info.getDocument()).getClassDeclBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                        insertText.append(" {\n$0}");
                    } else {
                        insertText.append("\n{\n$0}");
                    }
                    builder.command(new Command("Complete Abstract Methods", "java.complete.abstract.methods"));
                } catch (IOException ioe) {
                }
                builder.insertTextFormat(Completion.TextFormat.Snippet);
            } else {
                builder.insertTextFormat(Completion.TextFormat.PlainText);
            }
            return builder.insertText(insertText.toString()).build();
        }

        @Override
        public Completion createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
            return null; //TODO: fill
        }

        @Override
        public Completion createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated) {
            ElementHandle<TypeElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            return createTypeItem(info, "@", handle, elem, type, substitutionOffset, referencesCount, isDeprecated, false, false, false, true);
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
                    .sortText(String.format("%04d%s", isDeprecated ? 100 + DEPRECATED : 100, elem.getSimpleName().toString()));
            ElementHandle<ExecutableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, offset, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        @Override
        public Completion createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount) {
            String label = value;
            TextEdit textEdit = null;
            if (value.startsWith("\"")) {
                TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                ts.move(offset);
                if (ts.moveNext() && ts.offset() <= offset) {
                    switch (ts.token().id()) {
                        case STRING_LITERAL:
                            textEdit = new TextEdit(ts.offset(), offset, value);
                            break;
                        case MULTILINE_STRING_LITERAL:
                            String[] tokenLines = ts.token().text().toString().split("\n");
                            String[] lines = value.split("\n");
                            int cnt = 0;
                            for (int i = 0; i < lines.length; i++) {
                                if (i < tokenLines.length) {
                                    if (tokenLines[i].equals(lines[i])) {
                                        cnt += tokenLines[i].length() + 1;
                                    } else if (i == lines.length - 1) {
                                        label = lines[i].trim();
                                        textEdit = new TextEdit(ts.offset() + cnt, offset, lines[i]);
                                    }
                                }
                            }
                            break;
                    }
                }
            }
            Builder builder = CompletionCollector.newBuilder(label)
                    .kind(Completion.Kind.Text)
                    .sortText(value)
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .documentation(documentation);
            if (textEdit != null) {
                builder.textEdit(textEdit);
            }
            return builder.build();
        }

        private static final Object KEY_IMPORT_TEXT_EDITS = new Object();

        @Override
        public Completion createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            //TODO: prefer static imports (but would be much slower?)
            //TODO: should be resolveImport instead of addImports:
            Map<Element, TextEdit> imports = (Map<Element, TextEdit>) info.getCachedValue(KEY_IMPORT_TEXT_EDITS);
            if (imports == null) {
                info.putCachedValue(KEY_IMPORT_TEXT_EDITS, imports = new HashMap<>(), CompilationInfo.CacheClearPolicy.ON_TASK_END);
            }
            TextEdit currentClassImport = imports.computeIfAbsent(type.asElement(), toImport -> {
                List<TextEdit> textEdits = modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                    wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    wc.rewrite(info.getCompilationUnit(), GeneratorUtilities.get(wc).addImports(wc.getCompilationUnit(), new HashSet<>(Arrays.asList(toImport))));
                });
                return textEdits.isEmpty() ? null : textEdits.get(0);
            });
            String label = type.asElement().getSimpleName() + "." + memberElem.getSimpleName();
            String sortText = memberElem.getSimpleName().toString();
            String memberTypeName;
            StringBuilder labelDetail = new StringBuilder();
            StringBuilder insertText = new StringBuilder(label);
            boolean asTemplate = false;
            if (memberElem.getKind().isField()) {
                memberTypeName = Utilities.getTypeName(info, memberType, false).toString();
                sortText += String.format("#%s", Utilities.getTypeName(info, type, false)); //NOI18N
            } else if (memberElem.getKind() == ElementKind.METHOD) {
                CodeStyle cs = CodeStyle.getDefault(doc);
                memberTypeName = Utilities.getTypeName(info, ((ExecutableType) memberType).getReturnType(), false).toString();
                StringBuilder sortParams = new StringBuilder();
                labelDetail.append('(');
                sortParams.append('(');
                insertText.append(cs.spaceBeforeMethodCallParen() ? " (" : "(");
                int cnt = 0;
                Iterator<? extends VariableElement> it = ((ExecutableElement) memberElem).getParameters().iterator();
                Iterator<? extends TypeMirror> tIt = ((ExecutableType) memberType).getParameterTypes().iterator();
                while (it.hasNext() && tIt.hasNext()) {
                    TypeMirror tm = tIt.next();
                    if (tm == null) {
                        break;
                    }
                    String paramTypeName = Utilities.getTypeName(info, tm, false, ((ExecutableElement) memberElem).isVarArgs() && !tIt.hasNext()).toString();
                    String paramName = it.next().getSimpleName().toString();
                    labelDetail.append(paramTypeName).append(' ').append(paramName);
                    sortParams.append(paramTypeName);
                    VariableElement inst = instanceOf(tm, paramName);
                    if (cnt == 0 && cs.spaceWithinMethodCallParens()) {
                        insertText.append(' ');
                    }
                    insertText.append("${").append(cnt).append(":").append(inst != null ? inst.getSimpleName() : paramName).append("}");
                    asTemplate = true;
                    if (tIt.hasNext()) {
                        labelDetail.append(", ");
                        sortParams.append(',');
                        if (cs.spaceBeforeComma()) {
                            insertText.append(' ');
                        }
                        insertText.append(',');
                        if (cs.spaceAfterComma()) {
                            insertText.append(' ');
                        }
                    } else if (cs.spaceWithinMethodCallParens()) {
                        insertText.append(' ');
                    }
                    cnt++;
                }
                labelDetail.append(')');
                sortParams.append(')');
                insertText.append(')');
                sortText += String.format("#%02d#%s#s", cnt, sortParams.toString(), Utilities.getTypeName(info, type, false)); //NOI18N
            } else {
                return null;
            }
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label)
                    .kind(elementKind2CompletionItemKind(memberElem.getKind()))
                    .labelDescription(memberTypeName)
                    .insertText(insertText.toString())
                    .insertTextFormat(asTemplate ? Completion.TextFormat.Snippet : Completion.TextFormat.PlainText)
                    .sortText(String.format("%04d%s", memberElem.getKind().isField() ? 720 : 750, sortText));
            if (labelDetail.length() > 0) {
                builder.labelDetail(labelDetail.toString());
            }
            if (currentClassImport != null) {
                builder.additionalTextEdits(Collections.singletonList(currentClassImport));
            }
            ElementHandle<Element> handle = SUPPORTED_ELEMENT_KINDS.contains(memberElem.getKind().name()) ? ElementHandle.create(memberElem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, offset, handle));
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
            String simpleName = parent.getSimpleName().toString();
            StringBuilder labelDetail = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            labelDetail.append('(');
            sortParams.append('(');
            int cnt = 0;
            if (!isDefault) {
                CodeStyle cs = CodeStyle.getDefault(doc);
                for (VariableElement ve : fields) {
                    if (cnt > 0) {
                        labelDetail.append(", ");
                        sortParams.append(",");
                    }
                    boolean isStatic = ve.getModifiers().contains(Modifier.STATIC);
                    String sName = CodeStyleUtils.removePrefixSuffix(ve.getSimpleName(),
                        isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                        isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
                    sName = CodeStyleUtils.addPrefixSuffix(
                            sName,
                            cs.getParameterNamePrefix(),
                            cs.getParameterNameSuffix());
                    String paramTypeName = Utilities.getTypeName(info, ve.asType(), false).toString();
                    labelDetail.append(paramTypeName).append(' ').append(sName);
                    sortParams.append(paramTypeName);
                    cnt++;
                }
                if (superConstructor != null) {
                    for (VariableElement ve : superConstructor.getParameters()) {
                        if (cnt > 0) {
                            labelDetail.append(", ");
                            sortParams.append(",");
                        }
                        String sName = CodeStyleUtils.removePrefixSuffix(ve.getSimpleName(), cs.getParameterNamePrefix(), cs.getParameterNameSuffix());
                        sName = CodeStyleUtils.addPrefixSuffix(
                                sName,
                                cs.getParameterNamePrefix(),
                                cs.getParameterNameSuffix());
                        String paramTypeName = Utilities.getTypeName(info, ve.asType(), false).toString();
                        labelDetail.append(paramTypeName).append(' ').append(sName);
                        sortParams.append(paramTypeName);
                        cnt++;
                    }
                }
            }
            labelDetail.append(") - generate");
            sortParams.append(')');
            return CompletionCollector.newBuilder(simpleName)
                    .kind(Completion.Kind.Constructor)
                    .labelDetail(labelDetail.toString())
                    .insertTextFormat(Completion.TextFormat.PlainText)
                    .sortText(String.format("%04d%s#%02d%s", 1400, simpleName, cnt, sortParams.toString()))
                    .textEdit(new TextEdit(substitutionOffset, substitutionOffset, EMPTY))
                    .additionalTextEdits(() -> modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                        wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TreePath tp = wc.getTreeUtilities().pathFor(substitutionOffset);
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                            if (parent == wc.getTrees().getElement(tp)) {
                                ArrayList<VariableElement> fieldElements = new ArrayList<>();
                                for (VariableElement fieldElement : fields) {
                                    if (fieldElement != null && fieldElement.getKind().isField()) {
                                        fieldElements.add((VariableElement)fieldElement);
                                    }
                                }
                                ClassTree clazz = (ClassTree) tp.getLeaf();
                                GeneratorUtilities gu = GeneratorUtilities.get(wc);
                                MethodTree ctor = isDefault ? gu.createDefaultConstructor(parent, fieldElements, superConstructor)
                                        : gu.createConstructor(parent, fieldElements, superConstructor);
                                ClassTree decl = GeneratorUtils.insertClassMember(wc, clazz, ctor, substitutionOffset);
                                wc.rewrite(clazz, decl);
                            }
                        }
                    })).build();
        }

        @Override
        public Completion createLambdaItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, boolean expression, boolean addSemicolon) {
            StringBuilder label = new StringBuilder();
            StringBuilder insertText = new StringBuilder();
            StringBuilder sortText = new StringBuilder();
            CodeStyle cs = CodeStyle.getDefault(doc);
            label.append('(');
            insertText.append('(');
            sortText.append('(');
            ExecutableElement desc = info.getElementUtilities().getDescriptorElement(elem);
            ExecutableType descType = (ExecutableType)info.getTypes().asMemberOf(type, desc);
            Iterator<? extends VariableElement> it = desc.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = descType.getParameterTypes().iterator();
            int cnt = 0;
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                if (cnt == 0 && cs.spaceWithinLambdaParens()) {
                    insertText.append(' ');
                }
                cnt++;
                String paramTypeName = Utilities.getTypeName(info, tm, false, desc.isVarArgs() && !tIt.hasNext()).toString();
                VariableElement var = it.next();
                List<String> varNames = Utilities.varNamesSuggestions(tm, var.getKind(), Collections.emptySet(), null, null, info.getTypes(), info.getElements(), Collections.emptyList(), CodeStyle.getDefault(info.getFileObject()));
                String paramName = varNames.isEmpty() ? var.getSimpleName().toString() : varNames.get(0);
                label.append(paramName);
                insertText.append("${").append(cnt).append(":").append(paramName).append("}");
                sortText.append(paramTypeName);
                if (it.hasNext()) {
                    label.append(", ");
                    sortText.append(',');
                    if (cs.spaceBeforeComma()) {
                        insertText.append(' ');
                    }
                    insertText.append(',');
                    if (cs.spaceAfterComma()) {
                        insertText.append(' ');
                    }
                } else if (cs.spaceWithinLambdaParens()) {
                    insertText.append(' ');
                }
            }
            TypeMirror retType = descType.getReturnType();
            label.append(") -> ").append(Utilities.getTypeName(info, retType, false));
            insertText.append(cs.spaceAroundLambdaArrow() ? ") ->" : ")->"); //NOI18N
            if (cs.getOtherBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                insertText.append(cs.spaceAroundLambdaArrow() ? " {\n$0}" : "{\n$0}");
            } else {
                insertText.append("\n{\n$0}");
            }
            if (addSemicolon && retType.getKind() == TypeKind.VOID) {
                insertText.append(';');
            }
            return CompletionCollector.newBuilder(label.toString())
                    .kind(Completion.Kind.Function)
                    .insertText(insertText.toString())
                    .insertTextFormat(Completion.TextFormat.Snippet)
                    .sortText(String.format("%04d#%02d#%s", 50, cnt, sortText.toString()))
                    .build();
        }


        @Override
        public Completion createRecordPatternItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars) {
            String simpleName = elem.getSimpleName().toString();
            Iterator<? extends RecordComponentElement> it = elem.getRecordComponents().iterator();
            StringBuilder labelDetail = new StringBuilder();
            StringBuilder insertText = new StringBuilder(simpleName);
            RecordComponentElement recordComponent;
            int cnt = 1;
            labelDetail.append("(");
            insertText.append("(");
            while (it.hasNext()) {
                recordComponent = it.next();
                CharSequence typeName = Utilities.getTypeName(info, recordComponent.getAccessor().getReturnType(), false);
                labelDetail.append(typeName);
                insertText.append("${").append(cnt++).append(":").append(typeName).append("}");
                labelDetail.append(" ");
                insertText.append(" ");
                labelDetail.append(recordComponent.getSimpleName());
                insertText.append("${").append(cnt++).append(":").append(recordComponent.getSimpleName()).append("}");
                if (it.hasNext()) {
                    labelDetail.append(", ");
                    insertText.append(", ");
                }
            }
            labelDetail.append(")");
            insertText.append(")");
            return CompletionCollector.newBuilder(simpleName)
                    .kind(Completion.Kind.Struct)
                    .labelDetail(labelDetail.toString())
                    .insertText(insertText.toString())
                    .insertTextFormat(Completion.TextFormat.Snippet)
                    .sortText(String.format("%04d%s#", 650, simpleName))
                    .build();
        }

        private Completion createTypeItem(CompilationInfo info, String prefix, ElementHandle<TypeElement> handle, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType) {
            int off = info.getSnapshot().getEmbeddedOffset(substitutionOffset);
            String name = elem.getQualifiedName().toString();
            int idx = name.lastIndexOf('.');
            String pkgName = idx < 0 ? EMPTY : name.substring(0, idx);
            StringBuilder label = new StringBuilder();
            StringBuilder insertText = new StringBuilder();
            if (prefix != null) {
                label.append(prefix);
                insertText.append(prefix);
            }
            label.append(elem.getSimpleName());
            boolean asTemplate = false;
            boolean inImport = false;
            int cnt = 1;
            if (addSimpleName || referencesCount == null) {
                insertText.append(elem.getSimpleName());
            } else if (info.getTreeUtilities().isModuleInfo(info.getCompilationUnit())) {
                insertText.append(elem.getQualifiedName());
            } else {
                TreePath tp = info.getTreeUtilities().pathFor(off);
                if (tp != null && tp.getLeaf().getKind() == Tree.Kind.IMPORT) {
                    insertText.append(elem.getQualifiedName());
                    inImport = true;
                } else {
                    if ((type == null || type.getKind() != TypeKind.ERROR) &&
                            EnumSet.range(ElementKind.PACKAGE, ElementKind.INTERFACE).contains(elem.getEnclosingElement().getKind())) {
                        insertText.append(elem.getSimpleName());
                    } else {
                        insertText.append(elem.getQualifiedName());
                    }
                }
            }
            if (addTypeVars && !inImport) {
                Iterator<? extends TypeMirror> tas = type != null ? type.getTypeArguments().iterator() : null;
                if (tas != null && tas.hasNext()) {
                    insertText.append('<'); //NOI18N
                    if (!insideNew || elem.getModifiers().contains(Modifier.ABSTRACT)
                        || info.getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0
                        || !allowDiamond(info, off, type)) {
                        while (tas.hasNext()) {
                            TypeMirror ta = tas.next();
                            insertText.append("${").append(cnt++).append(":");
                            switch (ta.getKind()) {
                                case TYPEVAR:
                                    TypeVariable tv = (TypeVariable)ta;
                                    if (smartType || elem != tv.asElement().getEnclosingElement()) {
                                        insertText.append(Utilities.getTypeName(info, ta, true));
                                        asTemplate = true;
                                    } else {
                                        insertText.append(Utilities.getTypeName(info, tv.getUpperBound(), false));
                                        if (addTypeVars && SourceVersion.RELEASE_5.compareTo(info.getSourceVersion()) <= 0) {
                                            asTemplate = true;
                                        }
                                    }   break;
                                case WILDCARD:
                                    TypeMirror bound = ((WildcardType)ta).getExtendsBound();
                                    if (bound == null) {
                                        bound = ((WildcardType)ta).getSuperBound();
                                    }   insertText.append(bound != null ? Utilities.getTypeName(info, bound, false) : "Object");
                                    asTemplate = true;
                                    break;
                                case ERROR:
                                    insertText.append(((ErrorType)ta).asElement().getSimpleName());
                                    asTemplate = true;
                                    break;
                                default:
                                    insertText.append(Utilities.getTypeName(info, ta, false));
                                    asTemplate = true;
                                    break;
                            }
                            insertText.append("}");
                            if (tas.hasNext()) {
                                insertText.append(", ");
                            }
                        }
                    }
                    insertText.append('>');
                }
            }
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(label.toString())
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .sortText(String.format("%04d%s#%02d#%s", smartType ? 800 : 1800, elem.getSimpleName().toString(), Utilities.getImportanceLevel(name), pkgName))
                    .insertText(insertText.toString());
            if (asTemplate) {
                builder.insertTextFormat(Completion.TextFormat.Snippet);
            } else {
                builder.insertTextFormat(Completion.TextFormat.Snippet)
                        .addCommitCharacter('.');
            }
            if (pkgName.length() > 0) {
                builder.labelDescription(pkgName);
            }
            if (insideNew) {
                builder.command(new Command("Invoke Completion", "editor.action.triggerSuggest"));
            }
            if (handle != null) {
                builder.documentation(getDocumentation(doc, off, handle));
                if (!addSimpleName && !inImport) {
                    builder.additionalTextEdits(addImport(doc, off, handle));
                }
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        private Completion createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, String name, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
            String simpleName = name != null ? name : (elem.getKind() == ElementKind.METHOD ? elem : elem.getEnclosingElement()).getSimpleName().toString();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            StringBuilder labelDetail = new StringBuilder();
            StringBuilder insertText = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            insertText.append(simpleName);
            labelDetail.append("(");
            CodeStyle cs = CodeStyle.getDefault(doc);
            if (!inImport && !memberRef) {
                insertText.append(cs.spaceBeforeMethodCallParen() ? " (" : "(");
            }
            sortParams.append('(');
            int cnt = 0;
            boolean asTemplate = false;
            Command command = null;
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                if (!inImport && !memberRef && cnt == 0 && cs.spaceWithinMethodCallParens()) {
                    insertText.append(' ');
                }
                cnt++;
                String paramTypeName = Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString();
                String paramName = it.next().getSimpleName().toString();
                labelDetail.append(paramTypeName).append(' ').append(paramName);
                sortParams.append(paramTypeName);
                if (!inImport && !memberRef) {
                    VariableElement inst = instanceOf(tm, paramName);
                    insertText.append("${").append(cnt).append(":").append(inst != null ? inst.getSimpleName() : paramName).append("}");
                    asTemplate = true;
                }
                if (tIt.hasNext()) {
                    labelDetail.append(", ");
                    sortParams.append(',');
                    if (!inImport && !memberRef) {
                        if (cs.spaceBeforeComma()) {
                            insertText.append(' ');
                        }
                        insertText.append(',');
                        if (cs.spaceAfterComma()) {
                            insertText.append(' ');
                        }
                    }
                } else if (!inImport && !memberRef && cs.spaceWithinMethodCallParens()) {
                    insertText.append(' ');
                }
            }
            sortParams.append(')');
            labelDetail.append(')');
            TypeMirror retType = type.getReturnType();
            if (inImport) {
                insertText.append(';');
            } else if (!memberRef) {
                insertText.append(')');
                if (addSemicolon && elem.getKind() == ElementKind.METHOD && retType.getKind() == TypeKind.VOID
                        || "this".equals(name) || "super".equals(name)) {
                    insertText.append(';');
                }
                if (name == null && elem.getKind() == ElementKind.CONSTRUCTOR
                        && (elem.getEnclosingElement().getModifiers().contains(Modifier.ABSTRACT)
                        || elem.getModifiers().contains(Modifier.PROTECTED) && !info.getTrees().isAccessible(scope, elem, (DeclaredType)elem.getEnclosingElement().asType()))) {
                    if (cs.getClassDeclBracePlacement() == CodeStyle.BracePlacement.SAME_LINE) {
                        insertText.append(" {\n$0}");
                    } else {
                        insertText.append("\n{\n$0}");
                    }
                    command = new Command("Complete Abstract Methods", "java.complete.abstract.methods");
                    asTemplate = true;
                } else if (asTemplate) {
                    insertText.append("$0");
                }
            }
            int priority = elem.getKind() == ElementKind.METHOD ? smartType ? 500 : 1500 : smartType ? 650 : name != null ? 1550 : 1650;
            CompletionCollector.Builder builder = CompletionCollector.newBuilder(simpleName)
                    .kind(elementKind2CompletionItemKind(elem.getKind()))
                    .labelDetail(labelDetail.toString())
                    .insertTextFormat(asTemplate ? Completion.TextFormat.Snippet : Completion.TextFormat.PlainText)
                    .sortText(String.format("%04d%s#%02d%s", priority, simpleName, cnt, sortParams.toString()));
            if (elem.getKind() == ElementKind.METHOD) {
                builder.labelDescription(Utilities.getTypeName(info, retType, false).toString());
            }
            TextEdit textEdit = null;
            String filter = null;
            if (castType != null) {
                TreePath tp = info.getTreeUtilities().pathFor(substitutionOffset);
                int castStartOffset = assignToVarOffset;
                if (castStartOffset < 0) {
                    if (tp != null && tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                        castStartOffset = (int)info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                    }
                }
                StringBuilder castText = new StringBuilder();
                castText.append("((").append(AutoImport.resolveImport(info, tp, castType)).append(cs.spaceAfterTypeCast() ? ") " : ")");
                int castEndOffset = findCastEndPosition(info.getTokenHierarchy().tokenSequence(JavaTokenId.language()), castStartOffset, substitutionOffset);
                if (castEndOffset >= 0) {
                    castText.append(info.getText().subSequence(castStartOffset, castEndOffset)).append(")");
                    castText.append(info.getText().subSequence(castEndOffset, substitutionOffset)).append(insertText);
                    textEdit = new TextEdit(castStartOffset, offset, castText.toString());
                    filter = info.getText().substring(castStartOffset, substitutionOffset) + simpleName;
                }
            }
            if (textEdit != null && filter != null) {
                builder.textEdit(textEdit)
                        .filterText(filter);

            } else {
                builder.insertText(insertText.toString());
            }
            ElementHandle<ExecutableElement> handle = SUPPORTED_ELEMENT_KINDS.contains(elem.getKind().name()) ? ElementHandle.create(elem) : null;
            if (handle != null) {
                builder.documentation(getDocumentation(doc, offset, handle));
            }
            if (isDeprecated) {
                builder.addTag(Completion.Tag.Deprecated);
            }
            return builder.build();
        }

        private VariableElement instanceOf(TypeMirror type, String name) {
            VariableElement closest = null;
            int distance = Integer.MAX_VALUE;
            if (type != null) {
                Types types = info.getTypes();
                for (Element e : getLocals()) {
                    if (e instanceof VariableElement && !ERROR.contentEquals(e.getSimpleName())
                            && e.asType().getKind() != TypeKind.ERROR && types.isAssignable(e.asType(), type)) {
                        if (name == null) {
                            return (VariableElement)e;
                        }
                        int d = ElementHeaders.getDistance(e.getSimpleName().toString().toLowerCase(), name.toLowerCase());
                        if (isSameType(e.asType(), type, types)) {
                            d -= 1000;
                        }
                        if (d < distance) {
                            distance = d;
                            closest = (VariableElement)e;
                        }
                    }
                }
            }
            return closest;
        }

        private List<Element> getLocals() {
            if (locals == null) {
                locals = new ArrayList<>();
                Trees trees = info.getTrees();
                SourcePositions sp = trees.getSourcePositions();
                TreeUtilities tu = info.getTreeUtilities();
                TypeElement encl = scope.getEnclosingClass();
                boolean isStatic = encl != null ? tu.isStaticContext(scope) : false;
                if (encl == null) {
                    CompilationUnitTree cut = treePath.getCompilationUnit();
                    Iterator<? extends Tree> it = cut.getTypeDecls().iterator();
                    if (it.hasNext()) {
                        encl = (TypeElement)trees.getElement(TreePath.getPath(cut, it.next()));
                    }
                }
                Collection<? extends Element> illegalForwardRefs = SourceUtils.getForwardReferences(treePath, offset, sp, trees);
                Collection<CharSequence> illegalForwardRefNames = new HashSet<>(illegalForwardRefs.size());
                for (Element element : illegalForwardRefs) {
                    illegalForwardRefNames.add(element.getSimpleName());
                }
                TypeElement enclClass = encl;
                ExecutableElement method = scope.getEnclosingMethod();
                for (Element element : info.getElementUtilities().getLocalMembersAndVars(scope, (e, t) -> {
                    switch (e.getKind()) {
                        case TYPE_PARAMETER:
                            return true;
                        case LOCAL_VARIABLE:
                        case RESOURCE_VARIABLE:
                        case EXCEPTION_PARAMETER:
                        case PARAMETER:
                            return (method == null || method == e.getEnclosingElement() || e.getModifiers().contains(Modifier.FINAL)) &&
                                    !illegalForwardRefNames.contains(e.getSimpleName());
                        case FIELD:
                            if (e.getSimpleName().contentEquals("this")) { //NOI18N
                                return !isStatic && e.asType().getKind() == TypeKind.DECLARED && ((DeclaredType)e.asType()).asElement() == enclClass;
                            }
                            if (e.getSimpleName().contentEquals("super")) { //NOI18N
                                return false;
                            }
                            if (illegalForwardRefNames.contains(e.getSimpleName())) {
                                return false;
                            }
                        default:
                            return (!isStatic || e.getModifiers().contains(Modifier.STATIC)) && tu.isAccessible(scope, e, (DeclaredType)t);
                    }
                })) {
                    switch(element.getKind()) {
                        case TYPE_PARAMETER:
                            break;
                        default:
                            locals.add(element);
                    }
                }

            }
            return locals;
        }

        private static boolean isSameType(TypeMirror t1, TypeMirror t2, Types types) {
            if (types.isSameType(t1, t2)) {
                return true;
            }
            if (t1.getKind().isPrimitive() && types.isSameType(types.boxedClass((PrimitiveType)t1).asType(), t2)) {
                return true;
            }
            return t2.getKind().isPrimitive() && types.isSameType(t1, types.boxedClass((PrimitiveType)t2).asType());
        }

        private static boolean allowDiamond(CompilationInfo info, int offset, DeclaredType type) {
            TreeUtilities tu = info.getTreeUtilities();
            TreePath path = tu.pathFor(offset);
            while (path != null && !(path.getLeaf() instanceof StatementTree)) {
                path = path.getParentPath();
            }
            if (path != null) {
                Trees trees = info.getTrees();
                int pos = (int)trees.getSourcePositions().getStartPosition(path.getCompilationUnit(), path.getLeaf().getKind() == Tree.Kind.VARIABLE ? ((VariableTree)path.getLeaf()).getType() : path.getLeaf());
                if (pos >= 0) {
                    Scope scope = tu.scopeFor(pos);
                    String stmt = info.getText().substring(pos, offset);
                    StringBuilder sb = new StringBuilder();
                    sb.append('{').append(stmt).append(Utilities.getTypeName(info, type, true)).append("();}"); //NOI18N;
                    SourcePositions[] sp = new SourcePositions[1];
                    StatementTree st = tu.parseStatement(sb.toString(), sp);
                    tu.attributeTree(st, scope);
                    TreePath tp = tu.pathFor(new TreePath(path, st), offset - pos, sp[0]);
                    TypeMirror tm = tp != null ? trees.getTypeMirror(tp) : null;
                    sb = new StringBuilder();
                    sb.append('{').append(stmt).append(((TypeElement)type.asElement()).getQualifiedName()).append("<>();}"); //NOI18N
                    st = tu.parseStatement(sb.toString(), sp);
                    tu.attributeTree(st, scope);
                    tp = tu.pathFor(new TreePath(path, st), offset - pos, sp[0]);
                    TypeMirror tmd = tp != null ? trees.getTypeMirror(tp) : null;
                    return tm != null && tmd != null && info.getTypes().isSameType(tm, tmd);
                }
            }
            return false;
        }

        private static int findCastEndPosition(TokenSequence<JavaTokenId> ts, int startPos, int endPos) {
            TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(ts, startPos, endPos);
            if (last != null && last.token().id() == JavaTokenId.DOT) {
                last = findLastNonWhitespaceToken(ts, startPos, last.offset());
                if (last != null) {
                    return last.offset() + last.token().length();
                }
            }
            return -1;
        }

        private static TokenSequence<JavaTokenId> findLastNonWhitespaceToken(TokenSequence<JavaTokenId> ts, int startPos, int endPos) {
            ts.move(endPos);
            while(ts.movePrevious()) {
                int offset = ts.offset();
                if (offset < startPos) {
                    return null;
                }
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case JAVADOC_COMMENT:
                        break;
                    default:
                        return ts;
                }
            }
            return null;
        }
    }
}
