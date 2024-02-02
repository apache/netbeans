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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
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
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.InputService;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.QuickPickStep;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 10)
public final class ConstructorGenerator extends CodeActionsProvider {

    private static final String GENERATE_CONSTRUCTOR = "nbls.java.generate.constructor";
    private static final String URI =  "uri";
    private static final String OFFSET =  "offset";
    private static final String CONSTRUCTORS =  "constructors";
    private static final String FIELDS =  "fields";

    private final Gson gson = new Gson();

    public ConstructorGenerator() {
    }

    @Override
    @NbBundle.Messages({
        "DN_GenerateConstructor=Generate Constructor...",
    })
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.RESOLVED);
        List<String> only = params.getContext().getOnly();
        boolean isSource = only != null && only.contains(CodeActionKind.Source);
        int startOffset = getOffset(info, params.getRange().getStart());
        TreePath tp = info.getTreeUtilities().pathFor(startOffset);
        tp = info.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
        if (tp == null) {
            return Collections.emptyList();
        }
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);
        if (typeElement == null || !typeElement.getKind().isClass() || NestingKind.ANONYMOUS == typeElement.getNestingKind()) {
            return Collections.emptyList();
        }
        final Set<? extends VariableElement> uninitializedFields = info.getTreeUtilities().getUninitializedFields(tp);
        if (!isSource) {
            final Set<VariableElement> selectedFields = new HashSet<>();
            int endOffset = getOffset(info, params.getRange().getEnd());
            for (Tree m : ((ClassTree) tp.getLeaf()).getMembers()) {
                if (m.getKind() != Tree.Kind.VARIABLE) continue;
                int start = (int) info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), m);
                int end   = (int) info.getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), m);
                if (startOffset <= end && endOffset >= start) {
                    VariableElement var = (VariableElement) info.getTrees().getElement(new TreePath(tp, m));
                    if (uninitializedFields.contains(var)) {
                        selectedFields.add(var);
                    }
                }
            }
            if (selectedFields.isEmpty()) {
                return Collections.emptyList();
            }
        }
        final List<ExecutableElement> inheritedConstructors = new ArrayList<>();
        TypeMirror superClassType = typeElement.getSuperclass();
        if (superClassType.getKind() == TypeKind.DECLARED) {
            TypeElement superClass = (TypeElement) ((DeclaredType) superClassType).asElement();
            Elements elements = info.getElements();
            for (ExecutableElement executableElement : ElementFilter.constructorsIn(superClass.getEnclosedElements())) {
                PackageElement currentPackage = elements.getPackageOf(typeElement);
                PackageElement ctorPackage = elements.getPackageOf(executableElement);
                Set<Modifier> ctorMods = executableElement.getModifiers();
                if ((currentPackage != ctorPackage && !(ctorMods.contains(Modifier.PUBLIC) || ctorMods.contains(Modifier.PROTECTED)))
                        || ctorMods.contains(Modifier.PRIVATE)) {
                    continue;
                }
                inheritedConstructors.add(executableElement);
            }
        }
        List<QuickPickItem> constructors;
        if (typeElement.getKind() != ElementKind.ENUM && inheritedConstructors.size() == 1) {
            if (uninitializedFields.isEmpty() && inheritedConstructors.get(0).getParameters().isEmpty()
                    && ElementFilter.constructorsIn(typeElement.getEnclosedElements()).stream().filter(ctor -> ctor.getParameters().isEmpty() && !info.getElementUtilities().isSynthetic(ctor)).count() > 0) {
                constructors = Collections.emptyList();
            } else {
                QuickPickItem item = new QuickPickItem(createLabel(info, inheritedConstructors.get(0)));
                item.setUserData(new ElementData(inheritedConstructors.get(0)));
                constructors = Collections.singletonList(item);
            }
        } else if (inheritedConstructors.size() > 1) {
            constructors = new ArrayList<>(inheritedConstructors.size());
            for (ExecutableElement constructorElement : inheritedConstructors) {
                QuickPickItem item = new QuickPickItem(createLabel(info, constructorElement));
                item.setUserData(new ElementData(constructorElement));
                constructors.add(item);
            }
        } else {
            constructors = Collections.emptyList();
        }
        List<QuickPickItem> fields;
        if (uninitializedFields.isEmpty()) {
            fields = Collections.emptyList();
        } else {
            fields = new ArrayList<>();
            for (VariableElement variableElement : uninitializedFields) {
                QuickPickItem item = new QuickPickItem(createLabel(info, variableElement));
                item.setUserData(new ElementData(variableElement));
                item.setPicked(variableElement.getModifiers().contains(Modifier.FINAL));
                fields.add(item);
            }
        }
        if (constructors.isEmpty() && fields.isEmpty()) {
            return Collections.emptyList();
        }
        fields.sort((f1, f2) -> f1.getLabel().compareTo(f2.getLabel()));
        constructors.sort((c1, c2) -> c1.getLabel().compareTo(c2.getLabel()));
        String uri = Utils.toUri(info.getFileObject());
        Map<String, Object> data = new HashMap<>();
        data.put(URI, uri);
        data.put(OFFSET, startOffset);
        data.put(CONSTRUCTORS, constructors);
        data.put(FIELDS, fields);
        return Collections.singletonList(createCodeAction(client, Bundle.DN_GenerateConstructor(), isSource ? CODE_GENERATOR_KIND : CodeActionKind.QuickFix, null, "nbls.generate.code", GENERATE_CONSTRUCTOR, data));
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GENERATE_CONSTRUCTOR);
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectSuperConstructor=Select super constructor",
        "DN_SelectConstructorFields=Select fields to be initialized by constructor",
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
            List<QuickPickItem> constructors = Arrays.asList(gson.fromJson(data.get(CONSTRUCTORS), QuickPickItem[].class));
            List<QuickPickItem> fields = Arrays.asList(gson.fromJson(data.get(FIELDS), QuickPickItem[].class));
            if (constructors.size() < 2 && fields.isEmpty()) {
                future.complete(generate(client, uri, offset, constructors, fields));
            } else {
                InputService.Registry inputServiceRegistry = Lookup.getDefault().lookup(InputService.Registry.class);
                if (inputServiceRegistry != null) {
                    int totalSteps = constructors.size() > 1 ? 2 : 1;
                    String inputId = inputServiceRegistry.registerInput(params -> {
                        if (params.getStep() < totalSteps) {
                            Either<List<QuickPickItem>, String> constructorData = params.getData().get(CONSTRUCTORS);
                            if (constructorData != null) {
                                List<QuickPickItem> selectedConstructors = constructorData.getLeft();
                                for (QuickPickItem constructor : constructors) {
                                    constructor.setPicked(selectedConstructors.contains(constructor));
                                }
                            }
                            return CompletableFuture.completedFuture(Either.forLeft(new QuickPickStep(totalSteps, CONSTRUCTORS, null, Bundle.DN_SelectSuperConstructor(), true, constructors)));
                        } else if (params.getStep() == totalSteps) {
                            Either<List<QuickPickItem>, String> fieldData = params.getData().get(FIELDS);
                            if (fieldData != null) {
                                List<QuickPickItem> selectedFields = fieldData.getLeft();
                                for (QuickPickItem field : fields) {
                                    field.setPicked(selectedFields.contains(field));
                                }
                            }
                            return CompletableFuture.completedFuture(Either.forLeft(new QuickPickStep(totalSteps, FIELDS, null, Bundle.DN_SelectConstructorFields(), true, fields)));
                        } else {
                            return CompletableFuture.completedFuture(null);
                        }
                    });
                    client.showMultiStepInput(new ShowMutliStepInputParams(inputId, Bundle.DN_GenerateConstructor())).thenAccept(result -> {
                        Either<List<QuickPickItem>, String> selectedConstructors = result.get(CONSTRUCTORS);
                        Either<List<QuickPickItem>, String> selectedFields = result.get(FIELDS);
                        try {
                            future.complete(selectedFields != null ? generate(client, uri, offset, selectedConstructors != null ? selectedConstructors.getLeft() : constructors, selectedFields.getLeft()) : null);
                        } catch (IOException | IllegalArgumentException ex) {
                            future.completeExceptionally(ex);
                        }
                    });
                }
            }
        } catch (JsonSyntaxException | IOException | IllegalArgumentException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    @NbBundle.Messages({
        "DN_ConstructorAlreadyExists=Given constructor already exists",
    })
    private WorkspaceEdit generate(NbCodeLanguageClient client, String uri, int offset, List<QuickPickItem> constructors, List<QuickPickItem> fields) throws IOException, IllegalArgumentException {
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
                    List<ExecutableElement> selectedConstructors = constructors.stream().map(item -> {
                        ElementData data = gson.fromJson(gson.toJson(item.getUserData()), ElementData.class);
                        return (ExecutableElement)data.resolve(wc);
                    }).collect(Collectors.toList());
                    List<VariableElement> selectedFields = fields.stream().map(item -> {
                        ElementData data = gson.fromJson(gson.toJson(item.getUserData()), ElementData.class);
                        return (VariableElement)data.resolve(wc);
                    }).collect(Collectors.toList());
                    GeneratorUtils.generateConstructors(wc, tp, selectedFields, selectedConstructors, -1);
                }
            });
            return edits.isEmpty() ? null : new WorkspaceEdit(Collections.singletonMap(uri, edits));
        } catch (GeneratorUtils.DuplicateMemberException dme) {
            client.showMessage(new MessageParams(MessageType.Info, Bundle.DN_ConstructorAlreadyExists()));
        }
        return null;
    }
}
