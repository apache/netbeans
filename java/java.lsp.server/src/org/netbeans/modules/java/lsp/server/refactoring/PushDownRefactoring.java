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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
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
@ServiceProvider(service = CodeActionsProvider.class, position = 190)
public final class PushDownRefactoring extends CodeRefactoring {

    private static final String PUSH_DOWN_REFACTORING_KIND = "refactor.push.down";
    private static final String PUSH_DOWN_REFACTORING_COMMAND =  "nbls.java.refactor.push.down";

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_PushDown=Push Down...",
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
        SourcePositions sourcePositions = trees.getSourcePositions();
        TreeUtilities treeUtilities = info.getTreeUtilities();
        Types types = info.getTypes();
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
        ElementHandle<TypeElement> eh = ElementHandle.create((TypeElement) element);
        Set<FileObject> resources = info.getClasspathInfo().getClassIndex().getResources(eh, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
        if (resources.isEmpty()) {
            return Collections.emptyList();
        }
        List<QuickPickItem> members = new ArrayList<>();
        for (Element m: element.getEnclosedElements()) {
            if (m.getKind() == ElementKind.CONSTRUCTOR || m.getKind() == ElementKind.STATIC_INIT || m.getKind() == ElementKind.INSTANCE_INIT) {
                continue;
            }
            if (m instanceof TypeElement && types.isSubtype(m.asType(), element.asType())) {
                continue;
            }
            TreePath mPath = trees.getPath(m);
            long startMember = mPath != null ? sourcePositions.getStartPosition(mPath.getCompilationUnit(), mPath.getLeaf()) : -1;
            long endMember = mPath != null ? sourcePositions.getEndPosition(mPath.getCompilationUnit(), mPath.getLeaf()) : -1;
            boolean selected = offset > startMember && offset < endMember;
            members.add(new QuickPickItem(createLabel(info, m), null, null, selected, new ElementData(m)));
        }
        if (members.isEmpty()) {
            return Collections.emptyList();
        }
        QuickPickItem elementItem = new QuickPickItem(createLabel(info, element));
        elementItem.setUserData(new ElementData(element));
        return Collections.singletonList(createCodeAction(client, Bundle.DN_PushDown(), PUSH_DOWN_REFACTORING_KIND, null, PUSH_DOWN_REFACTORING_COMMAND, uri, elementItem, members));
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(PUSH_DOWN_REFACTORING_COMMAND);
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectMembersToPushDown=Select members to push down",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        try {
            if (arguments.size() > 2) {
                String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
                QuickPickItem sourceItem = gson.fromJson(gson.toJson(arguments.get(1)), QuickPickItem.class);
                List<QuickPickItem> members = Arrays.asList(gson.fromJson(gson.toJson(arguments.get(2)), QuickPickItem[].class));
                client.showQuickPick(new ShowQuickPickParams(null, Bundle.DN_SelectMembersToPushDown(), true, members)).thenAccept(selected -> {
                    if (selected != null && !selected.isEmpty()) {
                        pushDown(client, uri, sourceItem, selected);
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

    private void pushDown(NbCodeLanguageClient client, String uri, QuickPickItem source, List<QuickPickItem> members) {
        try {
            FileObject file = Utils.fromUri(uri);
            ClasspathInfo info = ClasspathInfo.create(file);
            JavaSource js = JavaSource.forFileObject(file);
            if (js == null) {
                throw new IOException("Cannot get JavaSource for: " + uri);
            }
            ElementHandle sourceHandle = gson.fromJson(gson.toJson(source.getUserData()), ElementData.class).toHandle();
            List<MemberInfo<ElementHandle<Element>>> memberHandles = new ArrayList<>();
            js.runUserActionTask(ci -> {
                ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (QuickPickItem member : members) {
                    Element el = gson.fromJson(gson.toJson(member.getUserData()), ElementData.class).resolve(ci);
                    MemberInfo<ElementHandle<Element>> memberInfo = MemberInfo.create(el, ci);
                    memberHandles.add(memberInfo);
                }
            }, true);
            org.netbeans.modules.refactoring.java.api.PushDownRefactoring refactoring = new org.netbeans.modules.refactoring.java.api.PushDownRefactoring(TreePathHandle.from(sourceHandle, info));
            refactoring.setMembers(memberHandles.toArray(new MemberInfo[0]));
            refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file));
            client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, "PushDown")));
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
