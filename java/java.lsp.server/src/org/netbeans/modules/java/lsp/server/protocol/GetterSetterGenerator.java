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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
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

    public static final String GENERATE_GETTERS =  "java.generate.getters";
    public static final String GENERATE_SETTERS =  "java.generate.setters";
    public static final String GENERATE_GETTERS_SETTERS =  "java.generate.getters.setters";

    private final Set<String> commands = Collections.unmodifiableSet(new HashSet(Arrays.asList(GENERATE_GETTERS, GENERATE_SETTERS, GENERATE_GETTERS_SETTERS)));
    private final Gson gson = new Gson();

    public GetterSetterGenerator() {
    }

    @Override
    @NbBundle.Messages({
        "DN_GenerateGetters=Generate Getters...",
        "DN_GenerateSetters=Generate Setters...",
        "DN_GenerateGettersSetters=Generate Getters and Setters...",
        "DN_GenerateGetterFor=Generate Getter for \"{0}\"",
        "DN_GenerateSetterFor=Generate Setter for \"{0}\"",
        "DN_GenerateGetterSetterFor=Generate Getter and Setter for \"{0}\"",
    })
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        CompilationController info = CompilationController.get(resultIterator.getParserResult());
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
            result.add(createCodeAction(name, all ? CODE_GENERATOR_KIND : CodeActionKind.QuickFix, GENERATE_GETTERS, uri, offset, all, pair.first().stream().map(variableElement -> {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                return item;
            }).collect(Collectors.toList())));
        }
        if (missingSetters) {
            String name = pair.second().size() == 1 ? Bundle.DN_GenerateSetterFor(pair.second().iterator().next().getSimpleName().toString()) : Bundle.DN_GenerateSetters();
            result.add(createCodeAction(name, all ? CODE_GENERATOR_KIND : CodeActionKind.QuickFix, GENERATE_SETTERS, uri, offset, all, pair.second().stream().map(variableElement -> {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                return item;
            }).collect(Collectors.toList())));
        }
        if (missingGetters && missingSetters) {
            pair.first().retainAll(pair.second());
            String name = pair.first().size() == 1 ? Bundle.DN_GenerateGetterSetterFor(pair.first().iterator().next().getSimpleName().toString()) : Bundle.DN_GenerateGettersSetters();
            result.add(createCodeAction(name, all ? CODE_GENERATOR_KIND : CodeActionKind.QuickFix, GENERATE_GETTERS_SETTERS, uri, offset, all, pair.first().stream().map(variableElement -> {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                return item;
            }).collect(Collectors.toList())));
        }
        return result;
    }

    @Override
    public Set<String> getCommands() {
        return commands;
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectGetters=Select fields to generate getters for",
        "DN_SelectSetters=Select fields to generate setters for",
        "DN_SelectGettersSetters=Select fields to generate getters and setters for",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.size() > 3) {
            String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
            int offset = gson.fromJson(gson.toJson(arguments.get(1)), Integer.class);
            boolean all = gson.fromJson(gson.toJson(arguments.get(2)), boolean.class);
            List<QuickPickItem> fields = Arrays.asList(gson.fromJson(gson.toJson(arguments.get(3)), QuickPickItem[].class));
            int kind;
            String text;
            switch (command) {
                case GENERATE_GETTERS: kind = GeneratorUtils.GETTERS_ONLY; text = Bundle.DN_SelectGetters(); break;
                case GENERATE_SETTERS: kind = GeneratorUtils.SETTERS_ONLY; text = Bundle.DN_SelectSetters(); break;
                default: kind = 0; text = Bundle.DN_SelectGettersSetters(); break;
            }
            if (all && fields.size() > 1) {
                client.showQuickPick(new ShowQuickPickParams(text, true, fields)).thenAccept(selected -> {
                    if (selected != null && !selected.isEmpty()) {
                        generate(client, kind, uri, offset, selected);
                    }
                });
            } else if (fields.size() == 1) {
                generate(client, kind, uri, offset, fields);
            }
        } else {
            client.logMessage(new MessageParams(MessageType.Error, String.format("Illegal number of arguments received for command: %s", command)));
        }
        return CompletableFuture.completedFuture(true);
    }

    private void generate(NbCodeLanguageClient client, int kind, String uri, int offset, List<QuickPickItem> fields) throws IllegalArgumentException {
        try {
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
            client.applyEdit(new ApplyWorkspaceEditParams(new WorkspaceEdit(Collections.singletonMap(uri, edits))));
        } catch (IOException ex) {
            client.logMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
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
}
