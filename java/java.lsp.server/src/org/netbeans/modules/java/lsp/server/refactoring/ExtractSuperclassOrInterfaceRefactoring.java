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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 170)
public final class ExtractSuperclassOrInterfaceRefactoring extends CodeRefactoring {

    private static final String EXTRACT_SUPERCLASS_REFACTORING_COMMAND =  "nbls.java.refactor.extract.superclass";
    private static final String EXTRACT_INTERFACE_REFACTORING_COMMAND =  "nbls.java.refactor.extract.interface";
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);

    private final Set<String> commands = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(EXTRACT_INTERFACE_REFACTORING_COMMAND, EXTRACT_SUPERCLASS_REFACTORING_COMMAND)));
    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_ExtractSuperclass=Extract Superclass...",
        "DN_ExtractInterface=Extract Interface...",
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
        String uri = Utils.toUri(info.getFileObject());
        Trees trees = info.getTrees();
        TreeUtilities treeUtilities = info.getTreeUtilities();
        TreePath path = treeUtilities.pathFor(offset);
        path = JavaRefactoringUtils.findEnclosingClass(info, path, true, true, true, true, false);
        if (path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            List<? extends Tree> decls = info.getCompilationUnit().getTypeDecls();
            if (!decls.isEmpty()) {
                path = TreePath.getPath(info.getCompilationUnit(), decls.get(0));
            } else {
                return Collections.emptyList();
            }
        }
        TypeElement type = (TypeElement) trees.getElement(path);
        if (type == null) {
            return Collections.emptyList();
        }
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        List<QuickPickItem> members = new ArrayList<>();
        List<QuickPickItem> allMembers = new ArrayList<>();
        ClassTree sourceTree = (ClassTree) path.getLeaf();
        for (Tree member : sourceTree.getMembers()) {
            TreePath memberTreePath = new TreePath(path, member);
            if (!treeUtilities.isSynthetic(memberTreePath)) {
                Element memberElm = trees.getElement(memberTreePath);
                if (memberElm != null) {
                    long startMember = sourcePositions.getStartPosition(info.getCompilationUnit(), member);
                    long endMember = sourcePositions.getEndPosition(info.getCompilationUnit(), member);
                    boolean selected = offset > startMember && offset < endMember;
                    Set<Modifier> mods = memberElm.getModifiers();
                    if (memberElm.getKind() == ElementKind.FIELD) {
                        QuickPickItem memberItem = new QuickPickItem(createLabel(info, memberElm), null, null, selected, new ElementData(memberElm));
                        allMembers.add(memberItem);
                        if (mods.contains(Modifier.PUBLIC) && mods.contains(Modifier.STATIC) && mods.contains(Modifier.FINAL) && ((VariableTree) member).getInitializer() != null) {
                            members.add(memberItem);
                        }
                    } else if (memberElm.getKind() == ElementKind.METHOD) {
                        QuickPickItem memberItem = new QuickPickItem(createLabel(info, memberElm), null, null, selected, new ElementData(memberElm));
                        allMembers.add(memberItem);
                        if (mods.contains(Modifier.PUBLIC) && !mods.contains(Modifier.STATIC)) {
                            members.add(memberItem);
                        }
                    }
                }
            }
        }
        List<CodeAction> result = new ArrayList<>();
        if (!allMembers.isEmpty()) {
            QuickPickItem elementItem = new QuickPickItem(createLabel(info, type));
            elementItem.setUserData(new ElementData(type));
            if (!type.getKind().isInterface()) {
                result.add(createCodeAction(client, Bundle.DN_ExtractSuperclass(), CodeActionKind.RefactorExtract, null, EXTRACT_SUPERCLASS_REFACTORING_COMMAND, uri, elementItem, allMembers));
            }
            if (!members.isEmpty()) {
                result.add(createCodeAction(client, Bundle.DN_ExtractInterface(), CodeActionKind.RefactorExtract, null, EXTRACT_INTERFACE_REFACTORING_COMMAND, uri, elementItem, members));
            }
        }
        return result;
    }

    @Override
    public Set<String> getCommands() {
        return commands;
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectMembersToExtract=Select members to extract",
        "DN_SelectClassName=Select class name",
        "DN_SelectInterfaceName=Select interface name",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.size() > 2) {
            String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
            QuickPickItem type = gson.fromJson(gson.toJson(arguments.get(1)), QuickPickItem.class);
            List<QuickPickItem> members = Arrays.asList(gson.fromJson(gson.toJson(arguments.get(2)), QuickPickItem[].class));
            client.showQuickPick(new ShowQuickPickParams(null, Bundle.DN_SelectMembersToExtract(), true, members)).thenAccept(selected -> {
                if (selected != null && !selected.isEmpty()) {
                    String label = EXTRACT_SUPERCLASS_REFACTORING_COMMAND.equals(command) ? Bundle.DN_SelectClassName() : Bundle.DN_SelectInterfaceName();
                    String value = EXTRACT_SUPERCLASS_REFACTORING_COMMAND.equals(command) ? "NewClass" : "NewInterface";
                    client.showInputBox(new ShowInputBoxParams(label, value)).thenAccept(name -> {
                        if (name != null && !name.isEmpty()) {
                            extract(client, uri, command, type, selected, name);
                        }
                    });
                }
            });
        } else {
            client.showMessage(new MessageParams(MessageType.Error, String.format("Illegal number of arguments received for command: %s", command)));
        }
        return CompletableFuture.completedFuture(true);
    }

    private void extract(NbCodeLanguageClient client, String uri, String command, QuickPickItem source, List<QuickPickItem> members, String name) {
        try {
            FileObject file = Utils.fromUri(uri);
            ClasspathInfo info = ClasspathInfo.create(file);
            ElementHandle handle = gson.fromJson(gson.toJson(source.getUserData()), ElementData.class).toHandle();
            AbstractRefactoring refactoring;
            if (EXTRACT_SUPERCLASS_REFACTORING_COMMAND.equals(command)) {
                List<MemberInfo<ElementHandle<Element>>> memberHandles = new ArrayList<>();
                JavaSource js = JavaSource.forFileObject(file);
                if (js == null) {
                    throw new IOException("Cannot get JavaSource for: " + uri);
                }
                js.runUserActionTask(ci -> {
                    ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (QuickPickItem member : members) {
                        Element el = gson.fromJson(gson.toJson(member.getUserData()), ElementData.class).resolve(ci);
                        memberHandles.add(MemberInfo.create(el, ci));
                    }
                }, true);
                ExtractSuperclassRefactoring r = new ExtractSuperclassRefactoring(TreePathHandle.from(handle, info));
                r.setMembers(memberHandles.toArray(new MemberInfo[0]));
                r.setSuperClassName(name);
                refactoring = r;
            } else {
                List<ElementHandle<VariableElement>> fields = new ArrayList<>();
                List<ElementHandle<ExecutableElement>> methods = new ArrayList<>();
                for (QuickPickItem member : members) {
                    ElementHandle memberHandle = gson.fromJson(gson.toJson(member.getUserData()), ElementData.class).toHandle();
                    switch (memberHandle.getKind()) {
                        case FIELD:
                            fields.add(memberHandle);
                            break;
                        case METHOD:
                            methods.add(memberHandle);
                            break;
                    }
                }
                ExtractInterfaceRefactoring r = new ExtractInterfaceRefactoring(TreePathHandle.from(handle, info));
                r.setFields(fields);
                r.setMethods(methods);
                r.setInterfaceName(name);
                refactoring = r;
            }
            refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file));
            client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, EXTRACT_SUPERCLASS_REFACTORING_COMMAND.equals(command) ? "Extract Superclass" : "Extract Interface")));
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
    }
}
