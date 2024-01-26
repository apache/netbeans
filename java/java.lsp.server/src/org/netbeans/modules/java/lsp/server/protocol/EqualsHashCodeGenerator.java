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
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 40)
public final class EqualsHashCodeGenerator extends CodeActionsProvider {

    private static final String GENERATE_EQUALS_HASHCODE = "nbls.java.generate.equals.hashCode";
    private static final String KIND =  "kind";
    private static final String URI =  "uri";
    private static final String OFFSET =  "offset";
    private static final String FIELDS =  "fields";
    private static final int EQUALS_ONLY = 1;
    private static final int HASH_CODE_ONLY = 2;

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_GenerateEquals=Generate equals()...",
        "DN_GenerateHashCode=Generate hashCode()...",
        "DN_GenerateEqualsHashCode=Generate equals() and hashCode()...",
    })
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Source)) {
            return Collections.emptyList();
        }
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.RESOLVED);
        int offset = getOffset(info, params.getRange().getStart());
        TreePath tp = info.getTreeUtilities().pathFor(offset);
        tp = info.getTreeUtilities().getPathElementOfKind(Tree.Kind.CLASS, tp);
        if (tp == null) {
            return Collections.emptyList();
        }
        TypeElement type = (TypeElement) info.getTrees().getElement(tp);
        if (type == null || type.getKind() != ElementKind.CLASS) {
            return Collections.emptyList();
        }
        ExecutableElement[] equalsHashCode = org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator.overridesHashCodeAndEquals(info, type, null);
        if (equalsHashCode[0] != null && equalsHashCode[1] != null) {
            return Collections.emptyList();
        }
        List<QuickPickItem> fields = new ArrayList<>();
        for (VariableElement variableElement : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (!ERROR.contentEquals(variableElement.getSimpleName()) && !variableElement.getModifiers().contains(Modifier.STATIC)) {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                fields.add(item);
            }
        }
        if (fields.isEmpty()) {
            return Collections.emptyList();
        }
        String uri = Utils.toUri(info.getFileObject());
        if (equalsHashCode[0] == null) {
            if (equalsHashCode[1] == null) {
                return Collections.singletonList(createCodeAction(client, Bundle.DN_GenerateEqualsHashCode(), CODE_GENERATOR_KIND, null, "nbls.generate.code", GENERATE_EQUALS_HASHCODE, data(0, uri, offset, fields)));
            }
            return Collections.singletonList(createCodeAction(client, Bundle.DN_GenerateEquals(), CODE_GENERATOR_KIND, null, "nbls.generate.code", GENERATE_EQUALS_HASHCODE, data(EQUALS_ONLY, uri, offset, fields)));
        }
        return Collections.singletonList(createCodeAction(client, Bundle.DN_GenerateHashCode(), CODE_GENERATOR_KIND, null, "nbls.generate.code", GENERATE_EQUALS_HASHCODE, data(HASH_CODE_ONLY, uri, offset, fields)));
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GENERATE_EQUALS_HASHCODE);
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectEquals=Select fields to be included in equals()",
        "DN_SelectHashCode=Select fields to be included in hashCode()",
        "DN_SelectEqualsHashCode=Select fields to be included in equals() and hashCode()",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        JsonObject data = (JsonObject) arguments.get(0);
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            int kind = data.getAsJsonPrimitive(KIND).getAsInt();
            String uri = data.getAsJsonPrimitive(URI).getAsString();
            int offset = data.getAsJsonPrimitive(OFFSET).getAsInt();
            List<QuickPickItem> fields = Arrays.asList(gson.fromJson(data.get(FIELDS), QuickPickItem[].class));
            String title;
            String text;
            boolean generateEquals = HASH_CODE_ONLY != kind;
            boolean generateHashCode = EQUALS_ONLY != kind;
            switch (kind) {
                case EQUALS_ONLY: title = Bundle.DN_GenerateEquals(); text = Bundle.DN_SelectEquals(); break;
                case HASH_CODE_ONLY: title = Bundle.DN_GenerateHashCode(); text = Bundle.DN_SelectHashCode(); break;
                default: title = Bundle.DN_GenerateEqualsHashCode(); text = Bundle.DN_SelectEqualsHashCode(); break;
            }
            client.showQuickPick(new ShowQuickPickParams(title, text, true, fields)).thenAccept(selected -> {
                try {
                    if (selected != null) {
                        FileObject file = Utils.fromUri(uri);
                        JavaSource js = JavaSource.forFileObject(file);
                        if (js == null) {
                            throw new IOException("Cannot get JavaSource for: " + uri);
                        }
                        List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
                            wc.toPhase(JavaSource.Phase.RESOLVED);
                            TreePath tp = wc.getTreeUtilities().pathFor(offset);
                            tp = wc.getTreeUtilities().getPathElementOfKind(Tree.Kind.CLASS, tp);
                            if (tp != null) {
                                List<VariableElement> selectedFields = selected.stream().map(item -> {
                                    ElementData userData = gson.fromJson(gson.toJson(item.getUserData()), ElementData.class);
                                    return (VariableElement) userData.resolve(wc);
                                }).collect(Collectors.toList());
                                org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator.generateEqualsAndHashCode(wc, tp, generateEquals ? selectedFields : null, generateHashCode ? selectedFields : null, -1);
                            }
                        });
                        future.complete(edits.isEmpty() ? null : new WorkspaceEdit(Collections.singletonMap(uri, edits)));
                    } else {
                        future.complete(null);
                    }
                } catch (IOException | IllegalArgumentException ex) {
                    future.completeExceptionally(ex);
                }
            });
        } catch(JsonSyntaxException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    private static Map<String, Object> data(int kind, String uri, int offset, List<QuickPickItem> fields) {
        Map<String, Object> data = new HashMap<>();
        data.put(KIND, kind);
        data.put(URI, uri);
        data.put(OFFSET, offset);
        data.put(FIELDS, fields);
        return data;
    }
}
