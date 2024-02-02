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
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 30)
public final class GetterSetterGenerator extends CodeActionsProvider {

    private static final String GENERATE_GETTER_SETTER = "nbls.java.generate.getter.setter";
    private static final String KIND =  "kind";
    private static final String URI =  "uri";
    private static final String OFFSET =  "offset";
    private static final String ALL =  "all";
    private static final String FIELDS =  "fields";

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_GenerateGetters=Generate Getters...",
        "DN_GenerateSetters=Generate Setters...",
        "DN_GenerateGettersSetters=Generate Getters and Setters...",
        "DN_GenerateGetterFor=Generate Getter for \"{0}\"",
        "DN_GenerateSetterFor=Generate Setter for \"{0}\"",
        "DN_GenerateGetterSetterFor=Generate Getter and Setter for \"{0}\"",
    })
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.RESOLVED);
        List<String> only = params.getContext().getOnly();
        boolean all = only != null && only.contains(CodeActionKind.Source);
        Pair<Set<VariableElement>, Set<VariableElement>> pair = findMissingGettersSetters(info, params.getRange(), all);
        boolean missingGetters = !pair.first().isEmpty();
        boolean missingSetters = !pair.second().isEmpty();
        String uri = Utils.toUri(info.getFileObject());
        int offset = getOffset(info, params.getRange().getStart());
        List<CodeAction> result = new ArrayList<>();
        if (missingGetters) {
            String name = pair.first().size() == 1 ? Bundle.DN_GenerateGetterFor(pair.first().iterator().next().getSimpleName().toString()) : Bundle.DN_GenerateGetters();
            result.add(createCodeAction(client, name, all ? CODE_GENERATOR_KIND : CodeActionKind.QuickFix, null, "nbls.generate.code", GENERATE_GETTER_SETTER, data(GeneratorUtils.GETTERS_ONLY, uri, offset, all, pair.first().stream().map(variableElement -> {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                return item;
            }).collect(Collectors.toList()))));
        }
        if (missingSetters) {
            String name = pair.second().size() == 1 ? Bundle.DN_GenerateSetterFor(pair.second().iterator().next().getSimpleName().toString()) : Bundle.DN_GenerateSetters();
            result.add(createCodeAction(client, name, all ? CODE_GENERATOR_KIND : CodeActionKind.QuickFix, null, "nbls.generate.code", GENERATE_GETTER_SETTER, data(GeneratorUtils.SETTERS_ONLY, uri, offset, all, pair.second().stream().map(variableElement -> {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                return item;
            }).collect(Collectors.toList()))));
        }
        if (missingGetters && missingSetters) {
            pair.first().retainAll(pair.second());
            String name = pair.first().size() == 1 ? Bundle.DN_GenerateGetterSetterFor(pair.first().iterator().next().getSimpleName().toString()) : Bundle.DN_GenerateGettersSetters();
            result.add(createCodeAction(client, name, all ? CODE_GENERATOR_KIND : CodeActionKind.QuickFix, null, "nbls.generate.code", GENERATE_GETTER_SETTER, data(0, uri, offset, all, pair.first().stream().map(variableElement -> {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                return item;
            }).collect(Collectors.toList()))));
        }
        return result;
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GENERATE_GETTER_SETTER);
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectGetters=Select fields to generate getters for",
        "DN_SelectSetters=Select fields to generate setters for",
        "DN_SelectGettersSetters=Select fields to generate getters and setters for",
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
            boolean all = data.getAsJsonPrimitive(ALL).getAsBoolean();
            List<QuickPickItem> fields = Arrays.asList(gson.fromJson(data.get(FIELDS), QuickPickItem[].class));
            String title;
            String text;
            switch (kind) {
                case GeneratorUtils.GETTERS_ONLY: title = Bundle.DN_GenerateGetters(); text = Bundle.DN_SelectGetters(); break;
                case GeneratorUtils.SETTERS_ONLY: title = Bundle.DN_GenerateSetters(); text = Bundle.DN_SelectSetters(); break;
                default: title = Bundle.DN_GenerateGettersSetters(); text = Bundle.DN_SelectGettersSetters(); break;
            }
            if (all && fields.size() > 1) {
                client.showQuickPick(new ShowQuickPickParams(title, text, true, fields)).thenAccept(selected -> {
                    try {
                        future.complete(selected != null && !selected.isEmpty() ? generate(kind, uri, offset, selected) : null);
                    } catch (IOException | IllegalArgumentException ex) {
                        future.completeExceptionally(ex);
                    }
                });
            } else {
                future.complete(generate(kind, uri, offset, fields));
            }
        } catch(JsonSyntaxException | IOException | IllegalArgumentException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    private WorkspaceEdit generate(int kind, String uri, int offset, List<QuickPickItem> fields) throws IOException, IllegalArgumentException {
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
                List<VariableElement> variableElements = fields.stream().map(item -> {
                    ElementData data = gson.fromJson(gson.toJson(item.getUserData()), ElementData.class);
                    return (VariableElement) data.resolve(wc);
                }).collect(Collectors.toList());
                GeneratorUtils.generateGettersAndSetters(wc, tp, variableElements, kind, -1);
            }
        });
        return edits.isEmpty() ? null : new WorkspaceEdit(Collections.singletonMap(uri, edits));
    }

    private static Pair<Set<VariableElement>, Set<VariableElement>> findMissingGettersSetters(CompilationInfo info, Range range, boolean all) {
        TreePath tp = info.getTreeUtilities().pathFor(getOffset(info, range.getStart()));
        tp = info.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
        if (tp == null) {
            return Pair.of(Collections.emptySet(), Collections.emptySet());
        }

        TypeElement type = (TypeElement) info.getTrees().getElement(tp);
        if (type == null) {
            return Pair.of(Collections.emptySet(), Collections.emptySet());
        }

        int selectionStart = getOffset(info, range.getStart());
        int selectionEnd   = getOffset(info, range.getEnd());

        ClassTree clazz = (ClassTree) tp.getLeaf();
        Set<VariableElement> selectedFields = new HashSet<>();

        for (Tree m : clazz.getMembers()) {
            if (m.getKind() != Tree.Kind.VARIABLE) continue;
            int start = (int) info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), m);
            int end   = (int) info.getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), m);

            if (all || intersects(start, end, selectionStart, selectionEnd)) {
                selectedFields.add((VariableElement) info.getTrees().getElement(new TreePath(tp, m)));
            }
        }

        Pair<Set<VariableElement>, Set<VariableElement>> pair = findMissingGettersSetters(info, type);

        pair.first().retainAll(selectedFields);
        pair.second().retainAll(selectedFields);

        return pair;
    }

    private static boolean intersects(int fieldStart, int fieldEnd, int selectionStart, int selectionEnd) {
        return selectionStart <= fieldEnd && selectionEnd >= fieldStart;
    }

    private static Pair<Set<VariableElement>, Set<VariableElement>> findMissingGettersSetters(CompilationInfo info, TypeElement type) {
        Set<VariableElement> missingGetters = new LinkedHashSet<>();
        Set<VariableElement> missingSetters = new LinkedHashSet<>();
        ElementUtilities eu = info.getElementUtilities();
        CodeStyle codeStyle = CodeStyle.getDefault(info.getFileObject());

        for (VariableElement variableElement : ElementFilter.fieldsIn(info.getElements().getAllMembers(type))) {
            if (ERROR.contentEquals(variableElement.getSimpleName())) {
                continue;
            }
            boolean hasGetter = eu.hasGetter(type, variableElement, codeStyle);
            boolean hasSetter = variableElement.getModifiers().contains(Modifier.FINAL) ||
                                eu.hasSetter(type, variableElement, codeStyle);
            if (!hasGetter) {
                missingGetters.add(variableElement);
            }
            if (!hasSetter) {
                missingSetters.add(variableElement);
            }
        }

        return Pair.of(missingGetters, missingSetters);
    }

    private static Map<String, Object> data(int kind, String uri, int offset, boolean all, List<QuickPickItem> fields) {
        Map<String, Object> data = new HashMap<>();
        data.put(KIND, kind);
        data.put(URI, uri);
        data.put(OFFSET, offset);
        data.put(ALL, all);
        data.put(FIELDS, fields);
        return data;
    }
}
