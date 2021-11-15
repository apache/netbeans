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
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 60)
public final class DelegateMethodGenerator extends CodeActionsProvider {

    private static final String URI =  "uri";
    private static final String OFFSET =  "offset";
    private static final String TYPE =  "type";
    private static final String FIELDS =  "fields";

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_GenerateDelegateMethod=Generate Delegate Method...",
    })
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Source)) {
            return Collections.emptyList();
        }
        CompilationController info = CompilationController.get(resultIterator.getParserResult());
        if (info == null) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.RESOLVED);
        int offset = getOffset(info, params.getRange().getStart());
        TreePath tp = info.getTreeUtilities().pathFor(offset);
        tp = info.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
        if (tp == null) {
            return Collections.emptyList();
        }
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);
        if (typeElement == null || !typeElement.getKind().isClass()) {
            return Collections.emptyList();
        }
        Elements elements = info.getElements();
        Trees trees = info.getTrees();
        Scope scope = trees.getScope(tp);
        List<QuickPickItem> fields = new ArrayList<>();
        TypeElement cls;
        while (scope != null && (cls = scope.getEnclosingClass()) != null) {
            DeclaredType type = (DeclaredType) cls.asType();
            for (VariableElement field : ElementFilter.fieldsIn(elements.getAllMembers(cls))) {
                TypeMirror fieldType = field.asType();
                if (!ERROR.contentEquals(field.getSimpleName()) && !fieldType.getKind().isPrimitive() && fieldType.getKind() != TypeKind.ARRAY
                        && (fieldType.getKind() != TypeKind.DECLARED || ((DeclaredType)fieldType).asElement() != cls) && trees.isAccessible(scope, field, type)) {
                    QuickPickItem item = new QuickPickItem(createLabel(info, field));
                    item.setUserData(new ElementData(field));
                    fields.add(item);
                }
            }
            scope = scope.getEnclosingScope();
        }
        if (fields.isEmpty()) {
            return Collections.emptyList();
        }
        String uri = Utils.toUri(info.getFileObject());
        QuickPickItem typeItem = new QuickPickItem(createLabel(info, typeElement));
        typeItem.setUserData(new ElementData(typeElement));
        Map<String, Object> data = new HashMap<>();
        data.put(URI, uri);
        data.put(OFFSET, offset);
        data.put(TYPE, typeItem);
        data.put(FIELDS, fields);
        return Collections.singletonList(createCodeAction(Bundle.DN_GenerateDelegateMethod(), CODE_GENERATOR_KIND, data, null));
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectDelegateMethodField=Select target field to generate delegates for",
    })
    public CompletableFuture<CodeAction> resolve(NbCodeLanguageClient client, CodeAction codeAction, Object data) {
        CompletableFuture<CodeAction> future = new CompletableFuture<>();
        try {
            String uri = ((JsonObject) data).getAsJsonPrimitive(URI).getAsString();
            int offset = ((JsonObject) data).getAsJsonPrimitive(OFFSET).getAsInt();
            QuickPickItem type = gson.fromJson(gson.toJson(((JsonObject) data).get(TYPE)), QuickPickItem.class);
            List<QuickPickItem> fields = Arrays.asList(gson.fromJson(((JsonObject) data).get(FIELDS), QuickPickItem[].class));
            if (fields.size() == 1) {
                selectMethods(client, uri, offset, type, fields.get(0)).handle((edit, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                    } else {
                        if (edit != null) {
                            codeAction.setEdit(edit);
                        }
                        future.complete(codeAction);
                    }
                    return null;
                });
            } else {
                client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectDelegateMethodField(), false, fields)).thenAccept(selected -> {
                    try {
                        if (selected != null && !selected.isEmpty()) {
                            selectMethods(client, uri, offset, type, selected.get(0)).handle((edit, ex) -> {
                                if (ex != null) {
                                    future.completeExceptionally(ex);
                                } else {
                                    if (edit != null) {
                                        codeAction.setEdit(edit);
                                    }
                                    future.complete(codeAction);
                                }
                                return null;
                            });
                        } else {
                            future.complete(codeAction);
                        }
                    } catch (IOException | IllegalArgumentException ex) {
                        future.completeExceptionally(ex);
                    }
                });
            }
        } catch (JsonSyntaxException | IOException | IllegalArgumentException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    @NbBundle.Messages({
        "DN_SelectDelegateMethods=Select methods to generate delegates for",
    })
    private CompletableFuture<WorkspaceEdit> selectMethods(NbCodeLanguageClient client, String uri, int offset, QuickPickItem type, QuickPickItem selectedField) throws IOException, IllegalArgumentException {
        CompletableFuture<WorkspaceEdit> future = new CompletableFuture<>();
        FileObject file = Utils.fromUri(uri);
        JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            throw new IOException("Cannot get JavaSource for: " + uri);
        }
        js.runUserActionTask(info -> {
            info.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement origin = (TypeElement) gson.fromJson(gson.toJson(type.getUserData()), ElementData.class).resolve(info);
            VariableElement field = (VariableElement) gson.fromJson(gson.toJson(selectedField.getUserData()), ElementData.class).resolve(info);
            if (origin != null && field != null) {
                final ElementUtilities eu = info.getElementUtilities();
                final Trees trees = info.getTrees();
                final Scope scope = info.getTreeUtilities().scopeFor(offset);
                ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                    @Override
                    public boolean accept(Element e, TypeMirror type) {
                        if (e.getKind() == ElementKind.METHOD && trees.isAccessible(scope, e, (DeclaredType)type)) {
                            Element impl = eu.getImplementationOf((ExecutableElement)e, origin);
                            return impl == null || (!impl.getModifiers().contains(Modifier.FINAL) && impl.getEnclosingElement() != origin);
                        }
                        return false;
                    }
                };
                List<QuickPickItem> methods = new ArrayList<>();
                for (ExecutableElement method : ElementFilter.methodsIn(eu.getMembers(field.asType(), acceptor))) {
                    QuickPickItem item = new QuickPickItem(String.format("%s.%s", field.getSimpleName().toString(), createLabel(info, method)));
                    item.setUserData(new ElementData(method));
                    methods.add(item);
                }
                client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectDelegateMethods(), true, methods)).thenAccept(selected -> {
                    try {
                        if (selected != null && !selected.isEmpty()) {
                            future.complete(generate(uri, offset, selectedField, selected));
                        } else {
                            future.complete(null);
                        }
                    } catch (IOException | IllegalArgumentException ex) {
                        future.completeExceptionally(ex);
                    }
                });
            } else {
                future.complete(null);
            }
        }, true);
        return future;
    }

    private WorkspaceEdit generate(String uri, int offset, QuickPickItem selectedField, List<QuickPickItem> selectedMethods) throws IOException, IllegalArgumentException {
        FileObject file = Utils.fromUri(uri);
        JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            throw new IOException("Cannot get JavaSource for: " + uri);
        }
        List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = wc.getTreeUtilities().pathFor(offset);
            tp = wc.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
            if (tp != null) {
                VariableElement field = (VariableElement) gson.fromJson(gson.toJson(selectedField.getUserData()), ElementData.class).resolve(wc);
                List<ExecutableElement> methods = selectedMethods.stream().map(item -> {
                    ElementData data = gson.fromJson(gson.toJson(item.getUserData()), ElementData.class);
                    return (ExecutableElement)data.resolve(wc);
                }).collect(Collectors.toList());
                org.netbeans.modules.java.editor.codegen.DelegateMethodGenerator.generateDelegatingMethods(wc, tp, field, methods, -1);
            }
        });
        return edits.isEmpty() ? null : new WorkspaceEdit(Collections.singletonMap(uri, edits));
    }
}
