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
package org.netbeans.modules.micronaut.completion;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.micronaut.db.Utils;
import org.netbeans.modules.micronaut.expression.MicronautExpressionLanguageUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class)
public class MicronautDataCompletionCollector implements CompletionCollector {

    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        new MicronautDataCompletionTask().query(doc, offset, new MicronautDataCompletionTask.ItemFactory<Completion>() {
            @Override
            public Completion createControllerMethodItem(CompilationInfo info, VariableElement delegateRepository, ExecutableElement delegateMethod, String controllerId, String id, int offset) {
                String delegateMethodName = delegateMethod.getSimpleName().toString();
                String methodName = Utils.getControllerDataEndpointMethodName(delegateMethodName, id);
                TypeMirror delegateRepositoryType = delegateRepository.asType();
                if (delegateRepositoryType.getKind() == TypeKind.DECLARED) {
                    ExecutableType type = (ExecutableType) info.getTypes().asMemberOf((DeclaredType) delegateRepositoryType, delegateMethod);
                    Iterator<? extends VariableElement> it = delegateMethod.getParameters().iterator();
                    Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
                    StringBuilder labelDetail = new StringBuilder();
                    StringBuilder sortParams = new StringBuilder();
                    labelDetail.append("(");
                    sortParams.append('(');
                    int cnt = 0;
                    while(it.hasNext() && tIt.hasNext()) {
                        TypeMirror tm = tIt.next();
                        if (tm == null) {
                            break;
                        }
                        cnt++;
                        String paramTypeName = Utils.getTypeName(info, tm, false, delegateMethod.isVarArgs() && !tIt.hasNext()).toString();
                        String paramName = it.next().getSimpleName().toString();
                        labelDetail.append(paramTypeName).append(' ').append(paramName);
                        sortParams.append(paramTypeName);
                        if (tIt.hasNext()) {
                            labelDetail.append(", ");
                            sortParams.append(',');
                        }
                    }
                    sortParams.append(')');
                    labelDetail.append(')');
                    TypeMirror returnType = Utils.getControllerDataEndpointReturnType(info, delegateMethodName, type);
                    FileObject fo = info.getFileObject();
                    ElementHandle<VariableElement> repositoryHandle = ElementHandle.create(delegateRepository);
                    ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(delegateMethod);
                    return CompletionCollector.newBuilder(methodName)
                            .kind(Completion.Kind.Method)
                            .labelDetail(String.format("%s - generate", labelDetail.toString()))
                            .labelDescription(Utils.getTypeName(info, returnType, false, false).toString())
                            .sortText(String.format("%04d%s#%02d%s", 1500, methodName, cnt, sortParams.toString()))
                            .insertTextFormat(Completion.TextFormat.PlainText)
                            .textEdit(new TextEdit(offset, offset, ""))
                            .additionalTextEdits(() -> modify2TextEdits(JavaSource.forFileObject(fo), wc -> {
                                wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                TreePath tp = wc.getTreeUtilities().pathFor(offset);
                                TypeElement te = TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()) ? (TypeElement) wc.getTrees().getElement(tp) : null;
                                if (te != null) {
                                    ClassTree clazz = (ClassTree) tp.getLeaf();
                                    VariableElement repository = repositoryHandle.resolve(wc);
                                    ExecutableElement method = methodHandle.resolve(wc);
                                    if (repository != null && method != null) {
                                        TypeMirror repositoryType = repository.asType();
                                        if (repositoryType.getKind() == TypeKind.DECLARED) {
                                            MethodTree mt = Utils.createControllerDataEndpointMethod(wc, (DeclaredType) repositoryType, repository.getSimpleName().toString(), method, controllerId, id);
                                            wc.rewrite(clazz, GeneratorUtilities.get(wc).insertClassMember(clazz, mt, offset));
                                        }
                                    }
                                }
                            })).build();
                }
                return null;
            }
            @Override
            public Completion createFinderMethodItem(String name, String returnType, int offset) {
                Builder builder = CompletionCollector.newBuilder(name).kind(Completion.Kind.Method).sortText(String.format("%04d%s", 10, name));
                if (returnType != null) {
                    builder.labelDetail("(...)")
                            .labelDescription(returnType)
                            .insertText(new StringBuilder("${1:").append(returnType).append("} ").append(name).append("$2($0);").toString())
                            .insertTextFormat(Completion.TextFormat.Snippet);
                }
                return builder.build();
            }
            @Override
            public Completion createFinderMethodNameItem(String prefix, String name, int offset) {
                return CompletionCollector.newBuilder(prefix + name).kind(Completion.Kind.Method).sortText(String.format("%04d%s", 10, name)).build();
            }
            @Override
            public Completion createFinderMethodParam(CompilationInfo info, VariableElement variableElement, int offset) {
                String name = variableElement.getSimpleName().toString();
                TypeMirror type = variableElement.asType();
                String typeName = Utils.getTypeName(info, type, false, false).toString();
                Set<ElementHandle<TypeElement>> handles = new HashSet<>();
                StringBuilder sb = new StringBuilder();
                for (TypeElement ann : Utils.getRelevantAnnotations(variableElement)) {
                    sb.append('@').append(ann.getSimpleName()).append(' ');
                    handles.add(ElementHandle.create(ann));
                }
                if (type.getKind() == TypeKind.DECLARED) {
                    handles.add(ElementHandle.create((TypeElement) ((DeclaredType) type).asElement()));
                }
                sb.append(typeName).append(' ').append(name);
                Builder builder = CompletionCollector.newBuilder(name).kind(Completion.Kind.Property).sortText(String.format("%04d%s", 10, name))
                        .insertText(sb.toString()).labelDescription(typeName);
                if (!handles.isEmpty()) {
                    builder.additionalTextEdits(() -> modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), copy -> {
                        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Set<TypeElement> toImport = handles.stream().map(handle -> handle.resolve(copy)).filter(te -> te != null).collect(Collectors.toSet());
                        copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), toImport));
                    }));
                }
                return builder.build();
            }
            @Override
            public Completion createFinderMethodParams(CompilationInfo info, List<VariableElement> variableElements, int offset) {
                StringBuilder label = new StringBuilder();
                StringBuilder insertText = new StringBuilder();
                StringBuilder sortParams = new StringBuilder();
                Set<ElementHandle<TypeElement>> handles = new HashSet<>();
                label.append('(');
                int cnt = 0;
                Iterator<VariableElement> it = variableElements.iterator();
                while (it.hasNext()) {
                    cnt++;
                    VariableElement variableElement = it.next();
                    String name = variableElement.getSimpleName().toString();
                    TypeMirror type = variableElement.asType();
                    String typeName = Utils.getTypeName(info, type, false, false).toString();
                    for (TypeElement ann : Utils.getRelevantAnnotations(variableElement)) {
                        insertText.append('@').append(ann.getSimpleName()).append(' ');
                        handles.add(ElementHandle.create(ann));
                    }
                    if (type.getKind() == TypeKind.DECLARED) {
                        handles.add(ElementHandle.create((TypeElement) ((DeclaredType) type).asElement()));
                    }
                    label.append(typeName).append(' ').append(name);
                    insertText.append(typeName).append(' ').append("${").append(cnt).append(":").append(name).append("}");
                    sortParams.append(typeName);
                    if (it.hasNext()) {
                        label.append(", ");
                        insertText.append(", ");
                        sortParams.append(",");
                    }
                }
                label.append(')');
                Builder builder = CompletionCollector.newBuilder(label.toString()).kind(Completion.Kind.Property).sortText(String.format("%04d#%02d%s", 5, cnt, sortParams.toString()))
                        .insertText(insertText.toString()).insertTextFormat(Completion.TextFormat.Snippet);
                if (!handles.isEmpty()) {
                    builder.additionalTextEdits(() -> modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), copy -> {
                        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Set<TypeElement> toImport = handles.stream().map(handle -> handle.resolve(copy)).filter(te -> te != null).collect(Collectors.toSet());
                        copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), toImport));
                    }));
                }
                return builder.build();
            }
            @Override
            public Completion createSQLItem(CompletionItem item) {
                return CompletionCollector.newBuilder(item.getInsertPrefix().toString())
                        .insertText(item.getInsertPrefix().toString().replace("\"", "\\\""))
                        .kind(Completion.Kind.Property)
                        .sortText(String.format("%010d%s",
                                Long.valueOf(item.getSortPriority()) + Math.abs(Long.valueOf(Integer.MIN_VALUE)),
                                item.getSortText())
                        )
                        .build();
            }
            @Override
            public Completion createKeywordItem(String name, int offset) {
                return CompletionCollector.newBuilder(name).kind(Completion.Kind.Keyword).sortText(String.format("%04d%s", 200, name)).build();
            }
            @Override
            public Completion createBuiltInItem(String name, String parenPair, int offset) {
                return CompletionCollector.newBuilder(name + parenPair.charAt(0) + "..." + parenPair.charAt(1))
                        .kind('(' == parenPair.charAt(0) ? Completion.Kind.Function : Completion.Kind.Variable)
                        .sortText(String.format("%04d%s", 100, name))
                        .insertText(name + parenPair.charAt(0) + "$0" + parenPair.charAt(1))
                        .insertTextFormat(Completion.TextFormat.Snippet)
                        .build();
            }
            @Override
            public Completion createPackageItem(String name, int offset) {
                return CompletionCollector.newBuilder(name)
                        .kind(Completion.Kind.Folder)
                        .sortText(String.format("%04d%s", 400, name))
                        .insertText(name + '.')
                        .build();
            }
            @Override
            public Completion createBeanPropertyItem(String name, String typeName, int offset) {
                return CompletionCollector.newBuilder(name + " : " + typeName)
                        .kind(Completion.Kind.Property)
                        .sortText(String.format("%04d%s", 50, name))
                        .insertText(name)
                        .build();
            }
            @Override
            public Completion createEnvPropertyItem(String name, String documentation, int anchorOffset, int offset) {
                return CompletionCollector.newBuilder(name)
                        .kind(Completion.Kind.Text)
                        .sortText(name)
                        .textEdit(new TextEdit(anchorOffset, offset, name))
                        .documentation(documentation)
                        .build();
            }
            @Override
            public Completion createJavaElementItem(CompilationInfo info, Element element, int offset) {
                String simpleName = element.getSimpleName().toString();
                if (element.getKind() == ElementKind.METHOD) {
                    Iterator<? extends VariableElement> it = ((ExecutableElement)element).getParameters().iterator();
                    Iterator<? extends TypeMirror> tIt = ((ExecutableType) element.asType()).getParameterTypes().iterator();
                    StringBuilder labelDetail = new StringBuilder();
                    StringBuilder insertText = new StringBuilder();
                    StringBuilder sortParams = new StringBuilder();
                    labelDetail.append('(');
                    insertText.append(simpleName).append('(');
                    sortParams.append('(');
                    int cnt = 0;
                    boolean asTemplate = false;
                    while(it.hasNext() && tIt.hasNext()) {
                        TypeMirror tm = tIt.next();
                        if (tm == null) {
                            break;
                        }
                        cnt++;
                        String paramTypeName = Utils.getTypeName(info, tm, false, ((ExecutableElement)element).isVarArgs() && !tIt.hasNext()).toString();
                        String paramName = it.next().getSimpleName().toString();
                        labelDetail.append(paramTypeName).append(' ').append(paramName);
                        sortParams.append(paramTypeName);
                        insertText.append("${").append(cnt).append(':').append(paramName).append('}');
                        asTemplate = true;
                        if (tIt.hasNext()) {
                            labelDetail.append(", ");
                            sortParams.append(',');
                            insertText.append(", ");
                        }
                    }
                    labelDetail.append(')');
                    insertText.append(')');
                    sortParams.append(')');
                    return CompletionCollector.newBuilder(simpleName)
                            .kind(Completion.Kind.Method)
                            .labelDetail(labelDetail.toString())
                            .labelDescription(Utils.getTypeName(info, ((ExecutableElement)element).getReturnType(), false, false).toString())
                            .sortText(String.format("%04d%s#%02d%s", 100, simpleName, cnt, sortParams.toString()))
                            .insertText(insertText.toString())
                            .insertTextFormat(asTemplate ? Completion.TextFormat.Snippet : Completion.TextFormat.PlainText)
                            .documentation(() -> MicronautExpressionLanguageUtilities.getJavadocText(info, element, false, 3))
                            .build();
                }
                Builder builder = CompletionCollector.newBuilder(simpleName);
                switch (element.getKind()) {
                    case ENUM:
                        builder.kind(Completion.Kind.Enum).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    case CLASS:
                        builder.kind(Completion.Kind.Class).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    case RECORD:
                        builder.kind(Completion.Kind.Struct).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    case ANNOTATION_TYPE:
                    case INTERFACE:
                        builder.kind(Completion.Kind.Interface).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected Java element kind: " + element.getKind());
                }
                return builder.documentation(() -> MicronautExpressionLanguageUtilities.getJavadocText(info, element, false, 3)).build();
            }
        }).stream().forEach(consumer);
        return true;
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
}
