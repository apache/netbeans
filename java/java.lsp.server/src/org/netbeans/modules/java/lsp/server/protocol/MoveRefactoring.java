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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 160)
public final class MoveRefactoring extends CodeRefactoring {

    private static final String MOVE_REFACTORING_KIND = "refactor.move";
    private static final String MOVE_REFACTORING_COMMAND =  "java.refactor.move";
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);

    private final Set<String> commands = Collections.singleton(MOVE_REFACTORING_COMMAND);
    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_Move=Move...",
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
        String uri = Utils.toUri(info.getFileObject());
        Element element = elementForOffset(info, offset);
        if (element != null) {
            QuickPickItem elementItem = new QuickPickItem(createLabel(info, element));
            elementItem.setUserData(new ElementData(element));
            return Collections.singletonList(createCodeAction(Bundle.DN_Move(), MOVE_REFACTORING_KIND, null, MOVE_REFACTORING_COMMAND, uri, elementItem));
        } else {
            return Collections.singletonList(createCodeAction(Bundle.DN_Move(), MOVE_REFACTORING_KIND, null, MOVE_REFACTORING_COMMAND, uri));
        }
    }

    @Override
    public Set<String> getCommands() {
        return commands;
    }

    @Override
    @NbBundle.Messages({
        "DN_DefaultPackage=<default package>",
        "DN_SelectTargetPackage=Select target package",
        "DN_CreateNewClass=<create new class>",
        "DN_SelectTargetClass=Select target class",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        try {
            if (arguments.size() > 0) {
                String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
                QuickPickItem elementItem = arguments.size() > 1 ? gson.fromJson(gson.toJson(arguments.get(1)), QuickPickItem.class) : null;
                FileObject file = Utils.fromUri(uri);
                Project project = FileOwnerQuery.getOwner(file);
                HashSet<QuickPickItem> items = new HashSet<>();
                if (project != null) {
                    for(SourceGroup sourceGroup : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                        String name = sourceGroup.getDisplayName();
                        FileObject rootFolder = sourceGroup.getRootFolder();
                        if (elementItem == null) {
                            items.add(new QuickPickItem(Bundle.DN_DefaultPackage(), name, null, false, Utils.toUri(rootFolder)));
                        }
                        for (String packageName : ClasspathInfo.create(rootFolder).getClassIndex().getPackageNames("", false, EnumSet.of(ClassIndex.SearchScope.SOURCE))) {
                            if (elementItem == null) {
                                String pkg = "";
                                for (String part : packageName.split("\\.")) {
                                    if (!part.isEmpty()) {
                                        pkg += pkg.length() == 0 ? part : "." + part;
                                        items.add(new QuickPickItem(pkg, name, null, false, Utils.toUri(rootFolder.getFileObject(pkg.replace('.', '/')))));
                                    }
                                }
                            } else {
                                items.add(new QuickPickItem(packageName, name, null, false, Utils.toUri(rootFolder.getFileObject(packageName.replace('.', '/')))));
                            }
                        }
                    }
                }
                ArrayList<QuickPickItem> packages = new ArrayList<>(items);
                Collections.sort(packages, (item1, item2) -> {
                    int i = item1.getDescription().compareTo(item2.getDescription());
                    return i == 0 ? item1.getLabel().compareTo(item2.getLabel()) : i;
                });
                Consumer<List<QuickPickItem>> f = selectedPackage -> {
                    if (selectedPackage != null && !selectedPackage.isEmpty()) {
                        ClasspathInfo info = ClasspathInfo.create(file);
                        TreePathHandle tph = elementItem != null ? TreePathHandle.from(gson.fromJson(gson.toJson(elementItem.getUserData()), ElementData.class).toHandle(), info) : null;
                        List<QuickPickItem> classes = packageClasses(selectedPackage.get(0), tph == null || tph.getKind() == Tree.Kind.CLASS);
                        if (classes.isEmpty()) {
                            if (tph == null) {
                                move(client, uri, selectedPackage.get(0), ClasspathInfo.create(file));
                            } else {
                                throw new IllegalArgumentException(String.format("No target class found in selected package"));
                            }
                        } else {
                            client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectTargetClass(), false, classes)).thenAccept(selectedClass -> {
                                if (selectedClass != null && !selectedClass.isEmpty()) {
                                    QuickPickItem selected = Bundle.DN_CreateNewClass().equals(selectedClass.get(0).getLabel()) ? selectedPackage.get(0) : selectedClass.get(0);
                                    move(client, tph != null ? tph : uri, selected, info);
                                }
                            });
                        }
                    }
                };
                if (packages.size() == 1) {
                    f.accept(packages);
                } else {
                    client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectTargetPackage(), false, packages)).thenAccept(f);
                }
            } else {
                throw new IllegalArgumentException(String.format("Illegal number of arguments received for command: %s", command));
            }
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
        return CompletableFuture.completedFuture(true);
    }

    private void move(NbCodeLanguageClient client, Object source, QuickPickItem target, ClasspathInfo info) {
        try {
            org.netbeans.modules.refactoring.api.MoveRefactoring refactoring;
            if (source instanceof String) {
                FileObject file = Utils.fromUri((String) source);
                refactoring = new org.netbeans.modules.refactoring.api.MoveRefactoring(Lookups.fixed(file));
                refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file));
            } else {
                TreePathHandle tph = (TreePathHandle) source;
                refactoring = new org.netbeans.modules.refactoring.api.MoveRefactoring(Lookups.fixed(tph));
                refactoring.getContext().add(tph.getKind() == Tree.Kind.CLASS ? JavaRefactoringUtils.getClasspathInfoFor(tph.getFileObject()) : new JavaMoveMembersProperties(tph));
            }
            if (target.getDescription() != null) {
                refactoring.setTarget(Lookups.singleton(new URL((String) target.getUserData())));
            } else {
                ElementHandle handle = gson.fromJson(gson.toJson(target.getUserData()), ElementData.class).toHandle();
                refactoring.setTarget(Lookups.singleton(TreePathHandle.from(handle, info)));
            }
            client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, "Move")));
        } catch (Exception ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
    }

    private static Element elementForOffset(CompilationInfo info, int offset) throws RuntimeException {
        List<? extends TypeElement> topLevelElements = info.getTopLevelElements();
        Trees trees = info.getTrees();
        SourcePositions sourcePositions = trees.getSourcePositions();
        CompilationUnitTree compilationUnit = info.getCompilationUnit();
        for (TypeElement typeElement : topLevelElements) {
            ClassTree topLevelClass = trees.getTree(typeElement);
            long startPosition = sourcePositions.getStartPosition(compilationUnit, topLevelClass);
            long endPosition = sourcePositions.getEndPosition(compilationUnit, topLevelClass);
            if (offset > startPosition && offset < endPosition) {
                for (Element element : typeElement.getEnclosedElements()) {
                    Tree member = trees.getTree(element);
                    long startMember = sourcePositions.getStartPosition(compilationUnit, member);
                    long endMember = sourcePositions.getEndPosition(compilationUnit, member);
                    if (offset > startMember && offset < endMember) {
                        return element;
                    }
                }
                return topLevelElements.size() > 1 ? typeElement : null;
            }
        }
        return null;
    }

    private static List<QuickPickItem> packageClasses(QuickPickItem targetPackage, boolean proposeNew) {
        try {
            FileObject fo = Utils.fromUri((String) targetPackage.getUserData());
            ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            final ClasspathInfo info = ClasspathInfo.create(EMPTY_PATH, EMPTY_PATH, sourcePath);
            Set<ClassIndex.SearchScopeType> searchScopeType = new HashSet<>(1);
            String packageName = Bundle.DN_DefaultPackage().equals(targetPackage.getLabel()) ? "" : targetPackage.getLabel();
            final Set<String> packageSet = Collections.singleton(packageName);
            searchScopeType.add(new ClassIndex.SearchScopeType() {
                @Override
                public Set<? extends String> getPackages() {
                    return packageSet;
                }

                @Override
                public boolean isSources() {
                    return true;
                }

                @Override
                public boolean isDependencies() {
                    return false;
                }
            });
            final Set<ElementHandle<TypeElement>> result = info.getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.PREFIX, searchScopeType);
            if (result != null && !result.isEmpty()) {
                List<QuickPickItem> ret = new ArrayList<>(result.size() + 1);
                if (proposeNew) {
                    ret.add(new QuickPickItem(Bundle.DN_CreateNewClass()));
                }
                for (ElementHandle<TypeElement> elementHandle : result) {
                    String qualifiedName = elementHandle.getQualifiedName();
                    if (qualifiedName.startsWith(packageName)) {
                        String shortName = qualifiedName.substring(packageName.length() + 1);
                        int idx = shortName.indexOf('.');
                        if (fo.getFileObject(idx < 0 ? shortName : shortName.substring(0, idx), "java") != null) {
                            ret.add(new QuickPickItem(shortName, null, null, false, new ElementData(elementHandle)));
                        }
                    }
                }
                return ret;
            }
        } catch (Exception ex) {}
        return Collections.emptyList();
    }
}
