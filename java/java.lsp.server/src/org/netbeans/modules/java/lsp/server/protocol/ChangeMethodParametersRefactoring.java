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
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 200)
public final class ChangeMethodParametersRefactoring extends CodeRefactoring {

    private static final String CHANGE_METHOD_PARAMS_REFACTORING_KIND = "refactor.change.method.params";
    private static final String CHANGE_METHOD_PARAMS_REFACTORING_COMMAND =  "java.refactor.change.method.params";

    private final Set<String> commands = Collections.singleton(CHANGE_METHOD_PARAMS_REFACTORING_COMMAND);
    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_ChangeMethodParams=Change Method Parameters...",
    })
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Refactor)) {
            return Collections.emptyList();
        }
        CompilationController info = CompilationController.get(resultIterator.getParserResult());
        if (info == null || !JavaRefactoringUtils.isRefactorable(info.getFileObject())) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        int offset = getOffset(info, params.getRange().getStart());
        Trees trees = info.getTrees();
        TreePath path = info.getTreeUtilities().pathFor(offset);
        Tree.Kind kind = null;
        while (path != null && (kind = path.getLeaf().getKind()) != Tree.Kind.METHOD && kind != Tree.Kind.METHOD_INVOCATION && kind != Tree.Kind.NEW_CLASS && kind != Tree.Kind.MEMBER_REFERENCE) {
            path = path.getParentPath();
        }
        Element element = null;
        FileObject elementSource = null;
        if (kind == Tree.Kind.METHOD_INVOCATION || kind == Tree.Kind.NEW_CLASS || kind == Tree.Kind.MEMBER_REFERENCE) {
            element = trees.getElement(path);
            if (element == null || element.asType().getKind() == TypeKind.ERROR) {
                return Collections.emptyList();
            }
            elementSource = SourceUtils.getFile(ElementHandle.create(element), info.getClasspathInfo());
        }
        if (elementSource == null) {
            return Collections.emptyList();
        }
        QuickPickItem elementItem = new QuickPickItem(createLabel(info, element, true));
        elementItem.setUserData(new ElementData(element));
        return Collections.singletonList(createCodeAction(Bundle.DN_ChangeMethodParams(), CHANGE_METHOD_PARAMS_REFACTORING_KIND, null, CHANGE_METHOD_PARAMS_REFACTORING_COMMAND, Utils.toUri(elementSource), elementItem));
    }

    @Override
    public Set<String> getCommands() {
        return commands;
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
                String label = sourceItem.getLabel();
                int idx = label.indexOf('(');
                client.showInputBox(new ShowInputBoxParams(Bundle.DN_ChangeMethodSignature(), label.substring(idx))).thenAccept(signature -> {
                    if (signature != null && !signature.isEmpty()) {
                        changeMethodParams(client, uri, sourceItem, signature);
                    }
                });
            } else {
                throw new IllegalArgumentException(String.format("Illegal number of arguments received for command: %s", command));
            }
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
        return CompletableFuture.completedFuture(true);
    }

    private void changeMethodParams(NbCodeLanguageClient client, String uri, QuickPickItem source, String signature) {
        try {
            FileObject file = Utils.fromUri(uri);
            ClasspathInfo info = ClasspathInfo.create(file);
            JavaSource js = JavaSource.forFileObject(file);
            if (js == null) {
                throw new IOException("Cannot get JavaSource for: " + uri);
            }
            ElementHandle handle = gson.fromJson(gson.toJson(source.getUserData()), ElementData.class).toHandle();
            AtomicReference<ChangeParametersRefactoring.ParameterInfo[]> params = new AtomicReference<>();
            StringBuilder ret = new StringBuilder();
            js.runUserActionTask(ci -> {
                ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = (ExecutableElement) handle.resolve(ci);
                if (method != null) {
                    TreePath path = ci.getTrees().getPath(method);
                    if (path != null) {
                        ExecutableElement el = fromSignature(ci, signature, path);
                        if (el != null) {
                            if (method.getReturnType() != el.getReturnType()) {
                                ret.append(Utilities.getTypeName(ci, el.getReturnType(), true));
                            }
                            params.set(matchParameters(ci, el.getParameters(), method.getParameters()));
                        }
                    }
                }
            }, true);
            if (params.get() == null) {
                throw new IllegalArgumentException("Error while parsing new method signature.");
            }
            ChangeParametersRefactoring refactoring = new ChangeParametersRefactoring(TreePathHandle.from(handle, info));
            refactoring.setReturnType(ret.length() > 0 ? ret.toString() : null);
            refactoring.setParameterInfo(params.get());
            refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file));
            client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, "ChangeMethodParameters")));
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
    }

    private static ExecutableElement fromSignature(CompilationInfo info, String signature, TreePath path) {
        int idx = signature.lastIndexOf(':');
        StringBuilder toParse = new StringBuilder("{class _X { public ");
        if (idx < 0) {
            toParse.append("_X").append(signature);
        } else {
            toParse.append(signature.substring(idx + 1)).append(" m").append(signature.substring(0, idx));
        }
        toParse.append("{}}}");
        SourcePositions[] sp = new SourcePositions[1];
        TreeUtilities treeUtilities = info.getTreeUtilities();
        StatementTree stmt = treeUtilities.parseStatement(toParse.toString(), sp);
        if (stmt != null && stmt.getKind() == Tree.Kind.BLOCK) {
            treeUtilities.attributeTree(stmt, info.getTrees().getScope(path));
            List<? extends StatementTree> stmts = ((BlockTree) stmt).getStatements();
            if (!stmts.isEmpty() && stmts.get(0).getKind() == Tree.Kind.CLASS) {
                ClassTree ct = (ClassTree) stmts.get(0);
                for (Tree member : ct.getMembers()) {
                    TreePath memberPath = new TreePath(path, member);
                    if (!treeUtilities.isSynthetic(memberPath) && member.getKind() == Tree.Kind.METHOD) {
                        Element element = info.getTrees().getElement(memberPath);
                        if (element != null && (element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.CONSTRUCTOR)) {
                            return (ExecutableElement) element;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static ChangeParametersRefactoring.ParameterInfo[] matchParameters(CompilationInfo info, List<? extends VariableElement> params, List<? extends VariableElement> orig) {
        ChangeParametersRefactoring.ParameterInfo[] result = new ChangeParametersRefactoring.ParameterInfo[params.size()];
        boolean[] used = new boolean[orig.size()];
        for (int idx = 0; idx < params.size(); idx++) {
            VariableElement param = params.get(idx);
            for (int i = 0; i < orig.size(); i++) {
                if (!used[i]) {
                    VariableElement origParam = orig.get(i);
                    if (origParam.getSimpleName().contentEquals(param.getSimpleName())) {
                        result[idx] = new ChangeParametersRefactoring.ParameterInfo(i, param.getSimpleName().toString(), Utilities.getTypeName(info, param.asType(), true).toString(), null);
                        used[i] = true;
                    }
                }
            }
        }
        for (int idx = 0; idx < params.size(); idx++) {
            if (result[idx] == null) {
                VariableElement param = params.get(idx);
                for (int i = 0; i < orig.size(); i++) {
                    if (!used[i]) {
                        VariableElement origParam = orig.get(i);
                        if (origParam.asType() == param.asType()) {
                            result[idx] = new ChangeParametersRefactoring.ParameterInfo(i, param.getSimpleName().toString(), Utilities.getTypeName(info, param.asType(), true).toString(), null);
                            used[i] = true;
                        }
                    }
                }
            }
        }
        for (int idx = 0; idx < params.size(); idx++) {
            if (result[idx] == null) {
                VariableElement param = params.get(idx);
                result[idx] = idx >= orig.size() || used[idx]
                        ? new ChangeParametersRefactoring.ParameterInfo(-1, param.getSimpleName().toString(), Utilities.getTypeName(info, param.asType(), true).toString(), defaultValue(param))
                        : new ChangeParametersRefactoring.ParameterInfo(idx, param.getSimpleName().toString(), Utilities.getTypeName(info, param.asType(), true).toString(), null);
            }
        }
        return result;
    }

    private static String defaultValue(VariableElement param) {
        switch(param.asType().getKind()) {
            case ARRAY:
            case DECLARED:
                return "null";
            case BOOLEAN:
                return "false";
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return "0";
        }
        return null;
    }
}
