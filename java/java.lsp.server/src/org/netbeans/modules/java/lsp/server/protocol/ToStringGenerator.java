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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
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
@ServiceProvider(service = CodeActionsProvider.class, position = 50)
public final class ToStringGenerator extends CodeActionsProvider {

    private static final String GENERATE_TO_STRING = "nbls.java.generate.toString";
    private static final String URI =  "uri";
    private static final String OFFSET =  "offset";
    private static final String FIELDS =  "fields";

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_GenerateToString=Generate toString()...",
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
        tp = info.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
        if (tp == null) {
            return Collections.emptyList();
        }
        TypeElement type = (TypeElement) info.getTrees().getElement(tp);
        if (type == null || !type.getKind().isClass()) {
            return Collections.emptyList();
        }
        List<QuickPickItem> fields = new ArrayList<>();
        for (Element element : type.getEnclosedElements()) {
            switch (element.getKind()) {
                case METHOD:
                    if (element.getSimpleName().contentEquals("toString") && ((ExecutableElement) element).getParameters().isEmpty()) { //NOI18N
                        return Collections.emptyList();
                    }
                    break;
                case FIELD:
                    if (!ERROR.contentEquals(element.getSimpleName()) && !element.getModifiers().contains(Modifier.STATIC)) {
                        QuickPickItem item = new QuickPickItem(createLabel(info, (VariableElement)element));
                        item.setUserData(new ElementData(element));
                        fields.add(item);
                    }
                    break;
            }
        }
        String uri = Utils.toUri(info.getFileObject());
        Map<String, Object> data = new HashMap<>();
        data.put(URI, uri);
        data.put(OFFSET, offset);
        data.put(FIELDS, fields);
        return Collections.singletonList(createCodeAction(client, Bundle.DN_GenerateToString(), CODE_GENERATOR_KIND, null, "nbls.generate.code", GENERATE_TO_STRING, data));
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GENERATE_TO_STRING);
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectToString=Select fields to be included in toString()",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        JsonObject data = (JsonObject) arguments.get(0);
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            String uri = data.getAsJsonPrimitive(URI).getAsString();
            int offset = data.getAsJsonPrimitive(OFFSET).getAsInt();
            List<QuickPickItem> fields = Arrays.asList(gson.fromJson(data.get(FIELDS), QuickPickItem[].class));
            if (fields.isEmpty()) {
                future.complete(generate(uri, offset, fields));
            } else {
                client.showQuickPick(new ShowQuickPickParams(Bundle.DN_GenerateToString(), Bundle.DN_SelectToString(), true, fields)).thenAccept(selected -> {
                    try {
                        future.complete(selected != null ? generate(uri, offset, fields) : null);
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

    private WorkspaceEdit generate(String uri, int offset, List<QuickPickItem> fields) throws IOException, IllegalArgumentException {
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
                ClassTree cls = (ClassTree) tp.getLeaf();
                List<VariableElement> selectedFields = fields.stream().map(item -> {
                    ElementData data = gson.fromJson(gson.toJson(item.getUserData()), ElementData.class);
                    return (VariableElement)data.resolve(wc);
                }).collect(Collectors.toList());
                MethodTree method = org.netbeans.modules.java.editor.codegen.ToStringGenerator.createToStringMethod(wc, selectedFields, cls.getSimpleName().toString(), true);
                wc.rewrite(cls, GeneratorUtilities.get(wc).insertClassMember(cls, method));
            }
        });
        return edits.isEmpty() ? null : new WorkspaceEdit(Collections.singletonMap(uri, edits));
    }
}
