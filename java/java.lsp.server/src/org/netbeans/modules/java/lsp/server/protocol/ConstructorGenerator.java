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
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeGenerator.class, position = 10)
public final class ConstructorGenerator extends CodeGenerator {

    public static final String GENERATE_CONSTRUCTOR =  "java.generate.constructor";

    private final Set<String> commands = Collections.singleton(GENERATE_CONSTRUCTOR);
    private final Gson gson = new Gson();

    public ConstructorGenerator() {
    }

    @Override
    @NbBundle.Messages({
        "DN_GenerateConstructor=Generate Constructor...",
    })
    public List<CodeAction> getCodeActions(CompilationInfo info, CodeActionParams params) {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Source)) {
            return Collections.emptyList();
        }
        int offset = getOffset(info, params.getRange().getStart());
        TreePath tp = info.getTreeUtilities().pathFor(offset);
        tp = info.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
        if (tp == null) {
            return Collections.emptyList();
        }
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);
        if (typeElement == null || !typeElement.getKind().isClass() || NestingKind.ANONYMOUS.equals(typeElement.getNestingKind())) {
            return Collections.emptyList();
        }
        final Set<? extends VariableElement> uninitializedFields = info.getTreeUtilities().getUninitializedFields(tp);
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
                fields.add(item);
            }
        }
        if (constructors.isEmpty() && fields.isEmpty()) {
            return Collections.emptyList();
        }
        String uri = Utils.toUri(info.getFileObject());
        return Collections.singletonList(createCodeAction(Bundle.DN_GenerateConstructor(), CODE_GENERATOR_KIND, GENERATE_CONSTRUCTOR, uri, offset, constructors, fields));
    }

    @Override
    public Set<String> getCommands() {
        return commands;
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectSuperConstructor=Select super constructor",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.size() > 3) {
            String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
            int offset = gson.fromJson(gson.toJson(arguments.get(1)), Integer.class);
            List<QuickPickItem> constructors = Arrays.asList(gson.fromJson(gson.toJson(arguments.get(2)), QuickPickItem[].class));
            List<QuickPickItem> fields = Arrays.asList(gson.fromJson(gson.toJson(arguments.get(3)), QuickPickItem[].class));
            if (constructors.size() < 2 && fields.isEmpty()) {
                generate(client, uri, offset, constructors, fields);
            } else {
                if (constructors.size() > 1) {
                    client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectSuperConstructor(), true, constructors)).thenAccept(selected -> {
                        if (selected != null) {
                            selectFields(client, uri, offset, selected, fields);
                        }
                    });
                } else {
                    selectFields(client, uri, offset, constructors, fields);
                }
            }
        } else {
            client.logMessage(new MessageParams(MessageType.Error, String.format("Illegal number of arguments received for command: %s", command)));
        }
        return CompletableFuture.completedFuture(true);
    }

    @NbBundle.Messages({
        "DN_SelectConstructorFields=Select fields to be initialized by constructor",
    })
    private void selectFields(NbCodeLanguageClient client, String uri, int offset, List<QuickPickItem> constructors, List<QuickPickItem> fields) {
        if (!fields.isEmpty()) {
            client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectConstructorFields(), true, fields)).thenAccept(selected -> {
                if (selected != null) {
                    generate(client, uri, offset, constructors, selected);
                }
            });
        } else {
            generate(client, uri, offset, constructors, fields);
        }
    }

    @NbBundle.Messages({
        "DN_ConstructorAlreadyExists=Given constructor already exists",
    })
    private void generate(NbCodeLanguageClient client, String uri, int offset, List<QuickPickItem> constructors, List<QuickPickItem> fields) {
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
            client.applyEdit(new ApplyWorkspaceEditParams(new WorkspaceEdit(Collections.singletonMap(uri, edits))));
        } catch (GeneratorUtils.DuplicateMemberException dme) {
            client.showMessage(new MessageParams(MessageType.Info, Bundle.DN_ConstructorAlreadyExists()));
        } catch (IOException | IllegalArgumentException ex) {
            client.logMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
    }
}
