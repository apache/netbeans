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
import com.google.gson.JsonSyntaxException;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 160)
public final class MoveRefactoring extends CodeRefactoring {

    private static final String MOVE_REFACTORING_KIND = "refactor.move";
    private static final String MOVE_REFACTORING_COMMAND =  "nbls.java.refactor.move";
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_Move=Move...",
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
        Element element = elementForOffset(info, offset);
        if (element != null) {
            return Collections.singletonList(createCodeAction(client, Bundle.DN_Move(), MOVE_REFACTORING_KIND, null, MOVE_REFACTORING_COMMAND, uri, new ElementData(element)));
        } else {
            return Collections.singletonList(createCodeAction(client, Bundle.DN_Move(), MOVE_REFACTORING_KIND, null, MOVE_REFACTORING_COMMAND, uri));
        }
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(MOVE_REFACTORING_COMMAND);
    }

    @Override
    @NbBundle.Messages({
        "DN_DefaultPackage=<default package>",
        "DN_CreateNewClass=<create new class>",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        try {
            if (arguments.size() > 0) {
                String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
                FileObject file = Utils.fromUri(uri);
                JavaSource js = JavaSource.forFileObject(file);
                if (js != null) {
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            js.runUserActionTask(ci -> {
                                ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                if (arguments.size() > 1) {
                                    Element element = gson.fromJson(gson.toJson(arguments.get(1)), ElementData.class).resolve(ci);
                                    if (element != null) {
                                        if (element.getKind().isClass() || element.getKind().isInterface()) {
                                            Pages.showMoveClassUI(ci, client, file, element);
                                        } else {
                                            Pages.showMoveMembersUI(ci, client, file, element);
                                        }
                                    }
                                } else {
                                    Pages.showMoveClassUI(ci, client, file, null);
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
        } catch (JsonSyntaxException | IllegalArgumentException | MalformedURLException ex) {
            client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
        }
        return CompletableFuture.completedFuture(true);
    }

    @HTMLDialog(url = "ui/MoveClass.html", resources = {"refactoring.css"})
    static HTMLDialog.OnSubmit showMoveClassUI(
        CompilationController ci,
        NbCodeLanguageClient client,
        FileObject file,
        Element element
    ) {
        MoveElementUI model = new MoveElementUI();
        model.withMoveClass(true)
                .withFrom(file.getName())
                .assignData(client, file, element != null ? TreePathHandle.create(element, ci) : null);
        model.applyBindings();
        return (id) -> {
            if ("accept".equals(id)) {
                model.doRefactoring();
            }
            return true; // return false, if validation fails
        };
    }

    @HTMLDialog(url = "ui/MoveMembers.html", resources = {"refactoring.css"})
    static HTMLDialog.OnSubmit showMoveMembersUI(
        CompilationController ci,
        NbCodeLanguageClient client,
        FileObject file,
        Element element
    ) {
        ElementUtilities eu = ci.getElementUtilities();
        Element enclosingElement = element.getEnclosingElement();
        String parentName = createLabel(ci, enclosingElement);
        ElementUI[] members = enclosingElement.getEnclosedElements().stream()
                .filter(memberElement -> (memberElement.getKind().isField() || memberElement.getKind() == ElementKind.METHOD) && !eu.isSynthetic(memberElement))
                .map(memberElement -> {
                    String label = createLabel(ci, memberElement);
                    ElementData data = new ElementData(memberElement);
                    ElementUI memberElementUI = new ElementUI(memberElement == element, label, memberElement.getKind().name(), data.getSignature());
                    return memberElementUI;
                }).toArray(ElementUI[]::new);
        MoveElementUI model = new MoveElementUI();
        model.withMoveClass(false)
                .withFrom(parentName)
                .withMembers(members)
                .withKeepMethodSelected(false)
                .withDeprecateMethodSelected(true)
                .assignData(client, file, TreePathHandle.create(element, ci));
        model.applyBindings();
        return (id) -> {
            if ("accept".equals(id)) {
                model.doRefactoring();
            }
            return true; // return false, if validation fails
        };
    }

    @Model(className = "MoveElementUI", targetId = "", instance = true, builder = "with", properties = {
        @Property(name = "moveClass", type = boolean.class),
        @Property(name = "from", type = String.class),
        @Property(name = "selectedProject", type = NamedPath.class),
        @Property(name = "selectedRoot", type = NamedPath.class),
        @Property(name = "selectedPackage", type = String.class),
        @Property(name = "selectedClass", type = ElementUI.class),
        @Property(name = "selectedVisibility", type = Visibility.class),
        @Property(name = "selectedJavaDoc", type = JavaDoc.class),
        @Property(name = "members", type = ElementUI.class, array = true),
        @Property(name = "keepMethodSelected", type = boolean.class),
        @Property(name = "deprecateMethodSelected", type = boolean.class)
    })
    static final class MoveElementControl {

        private NbCodeLanguageClient client;
        private FileObject file;
        private TreePathHandle handle;

        @ModelOperation
        void assignData(MoveElementUI ui, NbCodeLanguageClient client, FileObject file, TreePathHandle handle) {
            this.client = client;
            this.file = file;
            this.handle = handle;
        }

        @ComputedProperty
        static List<NamedPath> availableProjects() {
            Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
            List<NamedPath> projectNames = new ArrayList<>(openProjects.length);
            for (int i = 0; i < openProjects.length; i++) {
                projectNames.add(new NamedPath(ProjectUtils.getInformation(openProjects[i]).getDisplayName(), Utils.toUri(openProjects[i].getProjectDirectory())));
            }
            return projectNames;
        }

        @ComputedProperty
        static List<NamedPath> availableRoots(NamedPath selectedProject) {
            Project project = getSelectedProject(selectedProject);
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                List<NamedPath> projectRoots = new ArrayList<>(groups.length);
                for (int i = 0; i < groups.length; i++) {
                    projectRoots.add(new NamedPath(groups[i].getDisplayName(), Utils.toUri(groups[i].getRootFolder())));
                }
                return projectRoots;
            }
            return Collections.emptyList();
        }

        @ComputedProperty
        static List<String> availablePackages(boolean moveClass, NamedPath selectedRoot) {
            FileObject rootFolder = getSelectedRoot(selectedRoot);
            if (rootFolder != null) {
                List<String> packages;
                if (moveClass) {
                    packages = new ArrayList<>();
                    packages.add(Bundle.DN_DefaultPackage());
                    Enumeration<? extends FileObject> children = rootFolder.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject child = children.nextElement();
                        if (child.isFolder()) {
                            packages.add(FileUtil.getRelativePath(rootFolder, child).replace('/', '.'));
                        }
                    }
                } else {
                    packages = ClasspathInfo.create(rootFolder).getClassIndex().getPackageNames("", false, EnumSet.of(ClassIndex.SearchScope.SOURCE)).stream().collect(Collectors.toList());
                }
                packages.sort((s1, s2) -> s1.compareTo(s2));
                return packages;
            }
            return Collections.emptyList();
        }

        @ComputedProperty
        static List<ElementUI> availableClasses(boolean moveClass, NamedPath selectedRoot, String selectedPackage) {
            FileObject rootFolder = getSelectedRoot(selectedRoot);
            if (rootFolder != null && selectedPackage != null) {
                FileObject fo = rootFolder.getFileObject(selectedPackage.replace('.', '/'));
                ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                final ClasspathInfo info = ClasspathInfo.create(EMPTY_PATH, EMPTY_PATH, sourcePath);
                Set<ClassIndex.SearchScopeType> searchScopeType = new HashSet<>(1);
                final Set<String> packageSet = Collections.singleton(selectedPackage);
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
                List<ElementUI> ret = new ArrayList<>();
                if (moveClass) {
                    ret.add(new ElementUI(false, Bundle.DN_CreateNewClass(), null));
                }
                for (ElementHandle<TypeElement> eh : info.getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.PREFIX, searchScopeType)) {
                    ElementData data = new ElementData(eh);
                    String shortName = eh.getQualifiedName().substring(selectedPackage.length() + 1);
                    int idx = shortName.indexOf('.');
                    if (fo.getFileObject(idx < 0 ? shortName : shortName.substring(0, idx), "java") != null) {
                        ret.add(new ElementUI(false, shortName, data.getKind(), data.getSignature()));
                    }
                }
                ret.sort((e1, e2) -> e1.getLabel().compareTo(e2.getLabel()));
                return ret;
            }
            return Collections.emptyList();
        }

        @ComputedProperty
        static List<Visibility> availableVisibilities() {
            return Arrays.asList(Visibility.values());
        }

        @ComputedProperty
        static List<JavaDoc> availableJavaDoc() {
            return Arrays.asList(JavaDoc.values());
        }

        @ModelOperation
        @Function
        void doRefactoring(MoveElementUI ui) {
            try {
                org.netbeans.modules.refactoring.api.MoveRefactoring refactoring;
                if (handle == null) {
                    refactoring = new org.netbeans.modules.refactoring.api.MoveRefactoring(Lookups.fixed(file));
                    refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file));
                } else {
                    InstanceContent ic = new InstanceContent();
                    refactoring = new org.netbeans.modules.refactoring.api.MoveRefactoring(new AbstractLookup(ic));
                    List<TreePathHandle> selectedElements = ui.getMembers().stream().filter(member -> member.isSelected()).map(selectedMember -> {
                        ElementHandle memberHandle = ElementHandleAccessor.getInstance().create(ElementKind.valueOf(selectedMember.getKind()), selectedMember.getSignature().toArray(new String[selectedMember.getSignature().size()]));
                        return TreePathHandle.from(memberHandle, ClasspathInfo.create(file));
                    }).collect(Collectors.toList());
                    ic.set(selectedElements, null);
                    if (handle.getKind() == Tree.Kind.CLASS) {
                        refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(handle.getFileObject()));
                    } else {
                        JavaMoveMembersProperties properties = new JavaMoveMembersProperties(selectedElements.toArray(new TreePathHandle[0]));
                        properties.setVisibility(JavaMoveMembersProperties.Visibility.valueOf(ui.getSelectedVisibility().name()));
                        properties.setDelegate(ui.isKeepMethodSelected());
                        properties.setUpdateJavaDoc(ui.getSelectedJavaDoc() == JavaDoc.UPDATE);
                        properties.setAddDeprecated(ui.isDeprecateMethodSelected());
                        refactoring.getContext().add(properties);
                    }
                }
                ElementUI selectedClass = ui.getSelectedClass();
                if (selectedClass != null) {
                    if (selectedClass.getKind() != null && selectedClass.getSignature() != null) {
                        ElementHandle eh = ElementHandleAccessor.getInstance().create(ElementKind.valueOf(selectedClass.getKind()), selectedClass.getSignature().toArray(new String[selectedClass.getSignature().size()]));
                        refactoring.setTarget(Lookups.singleton(TreePathHandle.from(eh, ClasspathInfo.create(file))));
                    } else {
                        FileObject rootFolder = getSelectedRoot(ui.getSelectedRoot());
                        if (rootFolder != null && ui.getSelectedPackage()!= null) {
                            refactoring.setTarget(Lookups.singleton(rootFolder.getFileObject(ui.getSelectedPackage().replace('.', '/')).toURL()));
                        } else {
                            refactoring.setTarget(Lookup.EMPTY);
                        }
                    }
                } else {
                    refactoring.setTarget(Lookup.EMPTY);
                }
                client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, "Move")));
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

        private static Project getSelectedProject(NamedPath selectedProject) {
            try {
                String path = selectedProject.getPath();
                return path != null ? FileOwnerQuery.getOwner(Utils.fromUri(path)) : null;
            } catch (MalformedURLException ex) {
                return null;
            }
        }

        private static FileObject getSelectedRoot(NamedPath selectedRoot) {
            try {
                String path = selectedRoot.getPath();
                return path != null ? Utils.fromUri(path) : null;
            } catch (MalformedURLException ex) {
                return null;
            }
        }
    }

    @Model(className = "ElementUI", instance = true, properties = {
        @Property(name = "selected", type = boolean.class),
        @Property(name = "label", type = String.class),
        @Property(name = "kind", type = String.class),
        @Property(name = "signature", type = String.class, array = true)
    })
    static final class ElementControl {
    }

    @Model(className = "NamedPath", instance = true, properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "path", type = String.class)
    })
    static final class NamedPathControl {
    }

    public static enum Visibility {

        ESCALATE("Escalate"),
        ASIS("As is"),
        PUBLIC("Public"),
        PROTECTED("Protected"),
        PACKAGE_PRIVATE("Package private"),
        PRIVATE("Private");

        final String humanName;

        Visibility(String humanName) {
            this.humanName = humanName;
        }

        @Override
        public String toString() {
            return humanName;
        }
    }

    public static enum JavaDoc {

        ASIS("As is"),
        UPDATE("Update");

        final String humanName;

        JavaDoc(String humanName) {
            this.humanName = humanName;
        }

        @Override
        public String toString() {
            return humanName;
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
}
