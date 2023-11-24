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
package org.netbeans.modules.java.lsp.server.refactoring;

import com.google.gson.Gson;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 200)
public final class ChangeMethodParametersRefactoring extends CodeRefactoring {

    private static final String CHANGE_METHOD_PARAMS_REFACTORING_KIND = "refactor.change.method.params";
    private static final String CHANGE_METHOD_PARAMS_REFACTORING_COMMAND =  "nbls.java.refactor.change.method.params";

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_ChangeMethodParams=Change Method Parameters...",
    })
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Refactor)) {
            return Collections.emptyList();
        }
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null || !JavaRefactoringUtils.isRefactorable(info.getFileObject())) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        int offset = getOffset(info, params.getRange().getStart());
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        ts.move(offset);
        if (ts.moveNext() && ts.token().id() != JavaTokenId.WHITESPACE && ts.offset() == offset) {
            offset += 1;
        }
        Trees trees = info.getTrees();
        TreePath path = info.getTreeUtilities().pathFor(offset);
        Tree.Kind kind = null;
        while (path != null && (kind = path.getLeaf().getKind()) != Tree.Kind.METHOD && kind != Tree.Kind.METHOD_INVOCATION && kind != Tree.Kind.NEW_CLASS && kind != Tree.Kind.MEMBER_REFERENCE) {
            path = path.getParentPath();
        }
        Element element = null;
        FileObject elementSource = null;
        if (kind == Tree.Kind.METHOD || kind == Tree.Kind.METHOD_INVOCATION || kind == Tree.Kind.NEW_CLASS || kind == Tree.Kind.MEMBER_REFERENCE) {
            element = trees.getElement(path);
            if (element == null || element.asType().getKind() == TypeKind.ERROR) {
                return Collections.emptyList();
            }
            ElementHandle<Element> handle = ElementHandle.create(element);
            if (JavaRefactoringUtils.isFromLibrary(handle, info.getClasspathInfo())) {
                return Collections.emptyList();
            }
            elementSource = SourceUtils.getFile(handle, info.getClasspathInfo());
        }
        if (elementSource == null) {
            return Collections.emptyList();
        }
        QuickPickItem elementItem = new QuickPickItem(createLabel(info, element, true));
        elementItem.setUserData(new ElementData(element));
        return Collections.singletonList(createCodeAction(client, Bundle.DN_ChangeMethodParams(), CHANGE_METHOD_PARAMS_REFACTORING_KIND, null, CHANGE_METHOD_PARAMS_REFACTORING_COMMAND, Utils.toUri(elementSource), elementItem));
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(CHANGE_METHOD_PARAMS_REFACTORING_COMMAND);
    }

    @Override
    @NbBundle.Messages({
        "DN_ChangeMethodSignature=Change method signature",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        try {
            if (arguments.size() > 1) {
                String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
                QuickPickItem sourceItem = gson.fromJson(gson.toJson(arguments.get(1)), QuickPickItem.class);
                ElementHandle handle = gson.fromJson(gson.toJson(sourceItem.getUserData()), ElementData.class).toHandle();
                FileObject file = Utils.fromUri(uri);
                JavaSource js = JavaSource.forFileObject(file);
                if (js != null) {
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            js.runUserActionTask(ci -> {
                                ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                ExecutableElement method = (ExecutableElement) handle.resolve(ci);
                                if (method != null) {
                                    Pages.showChangeMethodParametersUI(ci, client, file, handle, method);
                                }
                            }, true);
                            return null;
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }, RequestProcessor.getDefault());
                }
            } else {
                throw new IllegalArgumentException(String.format("Illegal number of arguments received for command: %s", command));
            }
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
        return CompletableFuture.completedFuture(true);
    }

    private static String defaultValue(String type) {
        switch(type) {
            case "boolean":
                return "false";
            case "byte":
            case "char":
            case "double":
            case "float":
            case "int":
            case "long":
            case "short":
                return "0";
        }
        return "null";
    }

    @HTMLDialog(url = "ui/ChangeMethodParameters.html", resources = {"refactoring.css"})
    static HTMLDialog.OnSubmit showChangeMethodParametersUI(
        CompilationController ci,
        NbCodeLanguageClient client,
        FileObject file,
        ElementHandle handle,
        ExecutableElement method
    ) {
        ParameterUI[] params = new ParameterUI[method.getParameters().size()];
        for (int i = 0; i < method.getParameters().size(); i++) {
            VariableElement param = method.getParameters().get(i);
            ChangeParametersRefactoring.ParameterInfo info = new ChangeParametersRefactoring.ParameterInfo(i, param.getSimpleName().toString(), Utilities.getTypeName(ci, param.asType(), true).toString(), null);
            params[i] = new ParameterUI(info.getType(), info.getName());
            params[i].assignInfo(info);
        }
        Modifier mod;
        if (method.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC)) {
            mod = Modifier.PUBLIC;
        } else if (method.getModifiers().contains(javax.lang.model.element.Modifier.PROTECTED)) {
            mod = Modifier.PROTECTED;
        } else if (method.getModifiers().contains(javax.lang.model.element.Modifier.PRIVATE)) {
            mod = Modifier.PRIVATE;
        } else {
            mod = Modifier.PACKAGE_PRIVATE;
        }
        ChangeMethodParameterUI model = new ChangeMethodParameterUI();
        model.withName(method.getSimpleName().toString())
                .withReturnType(Utilities.getTypeName(ci, method.getReturnType(), true).toString())
                .withSelectedModifier(mod)
                .withIsStatic(method.getModifiers().contains(javax.lang.model.element.Modifier.STATIC))
                .withParameters(params)
                .assignData(client, file, TreePathHandle.from(handle, ClasspathInfo.create(file)));
        model.applyBindings();
        return (id) -> {
            if ("accept".equals(id)) {
                model.doRefactoring();
            }
            return true; // return false, if validation fails
        };
    }

    @Model(className = "ChangeMethodParameterUI", targetId = "", instance = true, builder = "with", properties = {
        @Property(name = "selectedModifier", type = Modifier.class),
        @Property(name = "isStatic", type = boolean.class),
        @Property(name = "name", type = String.class),
        @Property(name = "returnType", type = String.class),
        @Property(name = "parameters", type = ParameterUI.class, array = true)
    })
    static final class MethodControl {

        private NbCodeLanguageClient client;
        private FileObject file;
        private TreePathHandle handle;

        @ModelOperation
        void assignData(ChangeMethodParameterUI ui, NbCodeLanguageClient client, FileObject file, TreePathHandle handle) {
            this.client = client;
            this.file = file;
            this.handle = handle;
        }

        @ModelOperation
        @Function
        void doRefactoring(ChangeMethodParameterUI ui) {
            try {
                ChangeParametersRefactoring refactoring = new ChangeParametersRefactoring(handle);
                Modifier selectedModifier = ui.getSelectedModifier();
                if (selectedModifier != null) {
                    Set<javax.lang.model.element.Modifier> modifiers = EnumSet.noneOf(javax.lang.model.element.Modifier.class);
                    switch (selectedModifier) {
                        case PRIVATE: modifiers.add(javax.lang.model.element.Modifier.PRIVATE);break;
                        case PACKAGE_PRIVATE: break; /* no modifier */
                        case PROTECTED: modifiers.add(javax.lang.model.element.Modifier.PROTECTED); break;
                        case PUBLIC: modifiers.add(javax.lang.model.element.Modifier.PUBLIC); break;
                    }
                    refactoring.setModifiers(modifiers);
                }
                String returnType = ui.getReturnType();
                refactoring.setReturnType(returnType.length() > 0 ? returnType : null);
                String name = ui.getName();
                refactoring.setMethodName(name.length() > 0 ? name : null);
                List<ParameterUI> parameters = ui.getParameters();
                ChangeParametersRefactoring.ParameterInfo[] params = new ChangeParametersRefactoring.ParameterInfo[parameters.size()];
                for (int i = 0; i < parameters.size(); i++) {
                    ParameterUI parameter = parameters.get(i);
                    parameter.getInfo(i, (idx, info) -> {
                        if (info != null) {
                            params[idx] = new ChangeParametersRefactoring.ParameterInfo(info.getOriginalIndex(), parameter.getName(), parameter.getType(), null);
                        } else {
                            params[idx] = new ChangeParametersRefactoring.ParameterInfo(-1, parameter.getName(), parameter.getType(), defaultValue(parameter.getType()));
                        }
                    });
                }
                refactoring.setParameterInfo(params);
                refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file));
                client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, "ChangeMethodParameters")));
            } catch (Exception ex) {
                if (client == null) {
                    Exceptions.printStackTrace(
                        Exceptions.attachSeverity(ex, Level.SEVERE)
                    );
                } else {
                    client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
                }
            }
        }

        @Function
        void moveUpParameter(ChangeMethodParameterUI ui, ParameterUI data) {
            final List<ParameterUI> arr = ui.getParameters();
            int index = arr.indexOf(data);
            if (index > 0) {
                ParameterUI other = arr.get(index - 1);
                arr.set(index, other);
                arr.set(index - 1, data);
            }
        }

        @Function
        void moveDownParameter(ChangeMethodParameterUI ui, ParameterUI data) {
            final List<ParameterUI> arr = ui.getParameters();
            int index = arr.indexOf(data);
            if (index != -1 && index + 1 < arr.size()) {
                ParameterUI other = arr.get(index + 1);
                arr.set(index, other);
                arr.set(index + 1, data);
            }
        }

        @Function
        void addParameter(ChangeMethodParameterUI ui) {
            ui.getParameters().add(new ParameterUI());
        }

        @Function
        void removeParameter(ChangeMethodParameterUI ui, ParameterUI data) {
            ui.getParameters().remove(data);
        }

        @ComputedProperty
        static List<Modifier> availableModifiers() {
            return Arrays.asList(Modifier.values());
        }

        @ComputedProperty
        static String preview(
            Modifier selectedModifier, boolean isStatic, String returnType, String name, List<ParameterUI> parameters
        ) {
            StringBuilder sb = new StringBuilder();
            sb.append(selectedModifier != null ? selectedModifier.javaName : "").append(" ");
            if (isStatic) {
                sb.append("static ");
            }
            sb.append(returnType);
            sb.append(" ").append(name).append("(");
            String sep = "";
            for (ParameterUI p : parameters) {
                sb.append(sep);
                sb.append(p.getType() != null ? p.getType() : "").append(" ").append(p.getName() != null ? p.getName() : "");
                sep = ", ";
            }
            sb.append(")");
            return sb.toString();
        }
    }

    @Model(className = "ParameterUI", instance = true, properties = {
        @Property(name = "type", type = String.class),
        @Property(name = "name", type = String.class)
    })
    static final class ParamControl {
        private ChangeParametersRefactoring.ParameterInfo info;

        @ModelOperation
        void assignInfo(ParameterUI model, ChangeParametersRefactoring.ParameterInfo info) {
            this.info = info;
        }

        @ModelOperation
        void getInfo(ParameterUI model, int idx, BiConsumer<Integer, ChangeParametersRefactoring.ParameterInfo> consumer) {
            consumer.accept(idx, info);
        }
    }

    public static enum Modifier {
        PUBLIC("public"), PROTECTED("protected"), PACKAGE_PRIVATE("", "package private"), PRIVATE("private");

        final String javaName;
        final String humanName;

        Modifier(String javaName) {
            this(javaName, null);
        }

        Modifier(String javaName, String humanName) {
            this.javaName = javaName;
            this.humanName = humanName;
        }

        @Override
        public String toString() {
            return humanName == null ? javaName : humanName;
        }
    }
}
