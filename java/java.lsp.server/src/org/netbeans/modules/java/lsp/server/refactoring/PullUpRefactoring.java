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
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 180)
public final class PullUpRefactoring extends CodeRefactoring {

    private static final String PULL_UP_REFACTORING_KIND = "refactor.pull.up";
    private static final String PULL_UP_REFACTORING_COMMAND =  "nbls.java.refactor.pull.up";

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_PullUp=Pull Up...",
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
        TreePath path = findSelectedClassMemberDeclaration(treeUtilities.pathFor(offset), info);
        if (path == null) {
            return Collections.emptyList();
        }
        Element element = trees.getElement(path);
        if (!(element instanceof TypeElement)) {
            element = info.getElementUtilities().enclosingTypeElement(element);
        }
        if (!(element instanceof TypeElement)) {
            return Collections.emptyList();
        }
        Collection<TypeElement> supertypes = JavaRefactoringUtils.getSuperTypes((TypeElement)element, info, true);
        if (supertypes.isEmpty()) {
            return Collections.emptyList();
        }
        final List<QuickPickItem> supertypeItems = new ArrayList<>(supertypes.size());
        for (TypeElement e: supertypes) {
            QuickPickItem supertypeItem = new QuickPickItem(createLabel(info, e), null, null, false, new ElementData(e));
            supertypeItems.add(supertypeItem);
        }
        QuickPickItem elementItem = new QuickPickItem(createLabel(info, element));
        elementItem.setUserData(new ElementData(element));
        return Collections.singletonList(createCodeAction(client, Bundle.DN_PullUp(), PULL_UP_REFACTORING_KIND, null, PULL_UP_REFACTORING_COMMAND, uri, offset, elementItem, supertypeItems));
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(PULL_UP_REFACTORING_COMMAND);
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectTargetSupertype=Select target supertype",
        "DN_SelectMembersToPullUp=Select members to pull up",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        try {
            if (arguments.size() > 3) {
                String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
                int offset = gson.fromJson(gson.toJson(arguments.get(1)), Integer.class);
                QuickPickItem sourceItem = gson.fromJson(gson.toJson(arguments.get(2)), QuickPickItem.class);
                List<QuickPickItem> superclasses = Arrays.asList(gson.fromJson(gson.toJson(arguments.get(3)), QuickPickItem[].class));
                if (superclasses.size() > 1) {
                    client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectTargetSupertype(), superclasses)).thenAccept(selected -> {
                        if (selected != null && !selected.isEmpty()) {
                            QuickPickItem targetItem = selected.get(0);
                            List<QuickPickItem> members = getMembers(client, uri, offset, sourceItem, targetItem);
                            if (!members.isEmpty()) {
                                client.showQuickPick(new ShowQuickPickParams(null, Bundle.DN_SelectMembersToPullUp(), true, members)).thenAccept(selectedMembers -> {
                                    if (selectedMembers != null && !selectedMembers.isEmpty()) {
                                        pullUp(client, uri, sourceItem, targetItem, selectedMembers);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    QuickPickItem targetItem = superclasses.get(0);
                    List<QuickPickItem> members = getMembers(client, uri, offset, sourceItem, targetItem);
                    if (!members.isEmpty()) {
                        client.showQuickPick(new ShowQuickPickParams(null, Bundle.DN_SelectMembersToPullUp(), true, members)).thenAccept(selectedMembers -> {
                            if (selectedMembers != null && !selectedMembers.isEmpty()) {
                                pullUp(client, uri, sourceItem, targetItem, selectedMembers);
                            }
                        });
                    }
                }
            } else {
                throw new IllegalArgumentException(String.format("Illegal number of arguments received for command: %s", command));
            }
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
        return CompletableFuture.completedFuture(true);
    }

    private List<QuickPickItem> getMembers(NbCodeLanguageClient client, String uri, int offset, QuickPickItem source, QuickPickItem target) {
        List<QuickPickItem> members = new ArrayList<>();
        try {
            FileObject file = Utils.fromUri(uri);
            JavaSource js = JavaSource.forFileObject(file);
            if (js == null) {
                throw new IOException("Cannot get JavaSource for: " + uri);
            }
            js.runUserActionTask(info -> {
                Trees trees = info.getTrees();
                SourcePositions sourcePositions = trees.getSourcePositions();
                ElementUtilities eu = info.getElementUtilities();
                Types types = info.getTypes();
                TypeElement sourceElement = (TypeElement) gson.fromJson(gson.toJson(source.getUserData()), ElementData.class).toHandle().resolve(info);
                TypeMirror sourceType = sourceElement.asType();
                TypeElement targetElement = (TypeElement) gson.fromJson(gson.toJson(target.getUserData()), ElementData.class).toHandle().resolve(info);
                for (Element e : sourceElement.getEnclosedElements()) {
                    switch (e.getKind()) {
                        case CONSTRUCTOR:
                        case STATIC_INIT:
                        case INSTANCE_INIT:
                            continue;
                        case METHOD:
                            if (eu.alreadyDefinedIn(e.getSimpleName(), (ExecutableType) types.asMemberOf((DeclaredType) sourceType, e), targetElement)) {
                                break;
                            }
                        default: {
                            TreePath path = trees.getPath(e);
                            long startMember = path != null ? sourcePositions.getStartPosition(path.getCompilationUnit(), path.getLeaf()) : -1;
                            long endMember = path != null ? sourcePositions.getEndPosition(path.getCompilationUnit(), path.getLeaf()) : -1;
                            boolean selected = offset > startMember && offset < endMember;
                            members.add(new QuickPickItem(createLabel(info, e), null, null, selected, new ElementData(e)));
                        }
                    }
                }
            }, true);
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
        return members;
    }

    private void pullUp(NbCodeLanguageClient client, String uri, QuickPickItem source, QuickPickItem target, List<QuickPickItem> members) {
        try {
            FileObject file = Utils.fromUri(uri);
            ClasspathInfo info = ClasspathInfo.create(file);
            JavaSource js = JavaSource.forFileObject(file);
            if (js == null) {
                throw new IOException("Cannot get JavaSource for: " + uri);
            }
            ElementHandle sourceHandle = gson.fromJson(gson.toJson(source.getUserData()), ElementData.class).toHandle();
            ElementHandle targetHandle = gson.fromJson(gson.toJson(target.getUserData()), ElementData.class).toHandle();
            List<MemberInfo<ElementHandle<Element>>> memberHandles = new ArrayList<>();
            js.runUserActionTask(ci -> {
                ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                boolean isAbstract = targetHandle.resolve(ci).getModifiers().contains(Modifier.ABSTRACT);
                for (QuickPickItem member : members) {
                    Element el = gson.fromJson(gson.toJson(member.getUserData()), ElementData.class).resolve(ci);
                    MemberInfo<ElementHandle<Element>> memberInfo = MemberInfo.create(el, ci);
                    memberInfo.setMakeAbstract(isAbstract && el.getKind() == ElementKind.METHOD);
                    memberHandles.add(memberInfo);
                }
            }, true);
            org.netbeans.modules.refactoring.java.api.PullUpRefactoring refactoring = new org.netbeans.modules.refactoring.java.api.PullUpRefactoring(TreePathHandle.from(sourceHandle, info));
            refactoring.setTargetType(targetHandle);
            refactoring.setMembers(memberHandles.toArray(new MemberInfo[0]));
            refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file));
            client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, "PullUp")));
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
    }

    private static TreePath findSelectedClassMemberDeclaration(final TreePath path, final CompilationInfo javac) {
        TreePath currentPath = path;
        TreePath selection = null;
        while (currentPath != null && selection == null) {
            switch (currentPath.getLeaf().getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case NEW_CLASS:
                case METHOD:
                    selection = currentPath;
                    break;
                case VARIABLE:
                    Element elm = javac.getTrees().getElement(currentPath);
                    if (elm != null && elm.getKind().isField()) {
                        selection = currentPath;
                    }
                    break;
            }
            if (selection != null && javac.getTreeUtilities().isSynthetic(selection)) {
                selection = null;
            }
            if (selection == null) {
                currentPath = currentPath.getParentPath();
            }
        }
        if (selection == null && path != null) {
            List<? extends Tree> typeDecls = path.getCompilationUnit().getTypeDecls();
            if (!typeDecls.isEmpty() && typeDecls.get(0).getKind().asInterface() == ClassTree.class) {
                selection = TreePath.getPath(path.getCompilationUnit(), typeDecls.get(0));
            }
        }
        return selection;
    }
}
