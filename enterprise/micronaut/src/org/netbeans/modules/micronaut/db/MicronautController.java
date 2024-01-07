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
package org.netbeans.modules.micronaut.db;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class MicronautController implements TemplateWizard.Iterator {

    public static TemplateWizard.Iterator create() {
        return new MicronautController();
    }

    @NbBundle.Messages({
        "MSG_SelectRepository=Select Data Repository Interfaces",
        "MSG_SelectRepository_Prompt=Repositories to be called from Controllers",
        "MSG_SelectControllerName=Controller Name"
    })
    public static CreateFromTemplateHandler handler() {
        return new CreateFromTemplateHandler() {
            @Override
            protected boolean accept(CreateDescriptor desc) {
                return true;
            }

            @Override
            protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
                try {
                    final FileObject folder = desc.getTarget();
                    final Project project = FileOwnerQuery.getOwner(folder);
                    if (project == null) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_NoProject(folder.getPath()), NotifyDescriptor.ERROR_MESSAGE));
                        return Collections.emptyList();
                    }
                    final SourceGroup sourceGroup = SourceGroups.getFolderSourceGroup(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA), folder);
                    if (sourceGroup != null) {
                        Set<ElementHandle<TypeElement>> repositoryClasses = getRepositoryClasses(sourceGroup);
                        if (!repositoryClasses.isEmpty()) {
                            List<NotifyDescriptor.QuickPick.Item> items = repositoryClasses.stream().map(handle -> {
                                String fqn = handle.getQualifiedName();
                                int idx = fqn.lastIndexOf('.');
                                return idx < 0 ? new NotifyDescriptor.QuickPick.Item(fqn, null) : new NotifyDescriptor.QuickPick.Item(fqn.substring(idx + 1), fqn.substring(0, idx));
                            }).collect(Collectors.toList());
                            NotifyDescriptor.QuickPick qpt = new NotifyDescriptor.QuickPick(Bundle.MSG_SelectRepository(), Bundle.MSG_SelectRepository_Prompt(), items, true);
                            if (DialogDescriptor.OK_OPTION != DialogDisplayer.getDefault().notify(qpt)) {
                                return Collections.emptyList();
                            }
                            List<FileObject> generated = new ArrayList<>();
                            boolean hasSelectedItem = false;
                            for (NotifyDescriptor.QuickPick.Item item : qpt.getItems()) {
                                if (item.isSelected()) {
                                    hasSelectedItem = true;
                                    String label = item.getLabel();
                                    if (label.toLowerCase().endsWith(("repository"))) { //NOI18N
                                        label = label.substring(0, label.length() - 10);
                                    }
                                    FileObject fo = generate(folder, label, item.getDescription() != null ? item.getDescription() + '.' + item.getLabel() : item.getLabel());
                                    if (fo != null) {
                                        generated.add(fo);
                                    }
                                }
                            }
                            if (hasSelectedItem) {
                                return generated;
                            }
                        }
                    }
                    NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine(Bundle.MSG_SelectControllerName(), Bundle.MSG_SelectControllerName());
                    if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(inputLine)) {
                        List<FileObject> generated = new ArrayList<>();
                        String name = inputLine.getInputText();
                        if (!name.isEmpty()) {
                            if (name.toLowerCase().endsWith(("controller"))) { //NOI18N
                                name = name.substring(0, name.length() - 10);
                            }
                            FileObject fo = generate(desc.getTarget(), name, null);
                            if (fo != null) {
                                generated.add(fo);
                            }
                        }
                        return generated;
                    }
                } catch (Exception ex) {
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                }
                return Collections.emptyList();
            }
        };
    }

    private WizardDescriptor.Panel[] panels;
    private int index;
    private WizardDescriptor wizardDescriptor;
    private FileObject targetFolder;

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        Set<DataObject> generated = new HashSet<>();
        Map<String, ElementHandle<TypeElement>> selectedRepositories = (Map<String, ElementHandle<TypeElement>>) wiz.getProperty(ClassesSelectorPanel.PROP_SELECTED_CLASSES);
        for (String fqn : selectedRepositories.keySet()) {
            int idx = fqn.lastIndexOf('.');
            String label = idx < 0 ? fqn : fqn.substring(idx + 1);
            if (label.toLowerCase().endsWith(("repository"))) { //NOI18N
                label = label.substring(0, label.length() - 10);
            }
            FileObject fo = generate(targetFolder, label, fqn);
            if (fo != null) {
                generated.add(DataObject.find(fo));
            }
        }
        if (generated.isEmpty()) {
            String targetName = Templates.getTargetName(wiz);
            if (targetName != null && !targetName.isEmpty()) {
                FileObject fo = generate(targetFolder, targetName, null);
                if (fo != null) {
                    generated.add(DataObject.find(fo));
                }
            }
        }
        return generated;
    }

    @Override
    public void initialize(TemplateWizard wiz) {
        wizardDescriptor = wiz;

        targetFolder = Templates.getTargetFolder(wizardDescriptor);
        Project project = Templates.getProject(wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        if(sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            panels = new WizardDescriptor.Panel[] {
                Templates.buildSimpleTargetChooser(project, sourceGroups).create()
            };
        } else {
            List<WizardDescriptor.Panel> p = new ArrayList<>();
            SourceGroup sourceGroup = SourceGroups.getFolderSourceGroup(sourceGroups, targetFolder);
            if (sourceGroup != null) {
                Set<ElementHandle<TypeElement>> repositoryClasses = getRepositoryClasses(sourceGroup);
                if (!repositoryClasses.isEmpty()) {
                    Map<String, ElementHandle<TypeElement>> repositories = new HashMap<>();
                    for (ElementHandle<TypeElement> handle : repositoryClasses) {
                        repositories.put(handle.getQualifiedName(), handle);
                    }
                    wiz.putProperty(ClassesSelectorPanel.PROP_CLASSES, repositories);
                    p.add(new ClassesSelectorPanel.WizardPanel(NbBundle.getMessage(MicronautController.class, "Templates/Micronaut/Controller"), "Repositories", s -> null)); //NOI18N
                }
            }
            p.add(JavaTemplates.createPackageChooser(project, sourceGroups));
            panels = p.toArray(new WizardDescriptor.Panel[0]);
        }

        Wizards.mergeSteps(wiz, panels, null);
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return index < (panels.length - 1) && !(current() instanceof WizardDescriptor.FinishablePanel && ((WizardDescriptor.FinishablePanel) current()).isFinishPanel());
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if ((index + 1) == panels.length) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (index == 0) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private static Set<ElementHandle<TypeElement>> getRepositoryClasses(final SourceGroup sourceGroup) throws IllegalArgumentException {
        ClasspathInfo cpInfo = ClasspathInfo.create(sourceGroup.getRootFolder());
        Set<ElementHandle<TypeElement>> repositoryClasses = new HashSet<>();
        LinkedList<ElementHandle<TypeElement>> bases = new LinkedList<>();
        bases.add(ElementHandle.createTypeElementHandle(ElementKind.INTERFACE, "io.micronaut.data.repository.CrudRepository")); //NOI18N
        while (!bases.isEmpty()) {
            ElementHandle<TypeElement> base = bases.removeFirst();
            bases.addAll(cpInfo.getClassIndex().getElements(base, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES)));
            Set<ElementHandle<TypeElement>> srcElements = cpInfo.getClassIndex().getElements(base, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            bases.addAll(srcElements);
            repositoryClasses.addAll(srcElements);
        }
        return repositoryClasses;
    }

    @NbBundle.Messages({
        "MSG_ControllerClass=Controller class {0}\n"
    })
    private static FileObject generate(FileObject folder, String name, String repositoryFQN) {
        try {
            String controllerName = name.substring(0, 1).toUpperCase() + name.substring(1) + "Controller"; // NOI18N
            FileObject fo = GenerationUtils.createClass(folder, controllerName, Bundle.MSG_ControllerClass(controllerName));
            if (fo != null) {
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    js.runModificationTask(copy -> {
                        copy.toPhase(JavaSource.Phase.RESOLVED);
                        Tree origTree = copy.getCompilationUnit().getTypeDecls().get(0);
                        if (origTree.getKind() == Tree.Kind.CLASS) {
                            GenerationUtils gu = GenerationUtils.newInstance(copy);
                            TreeMaker tm = copy.getTreeMaker();
                            List<ExpressionTree> annArgs = Collections.singletonList(gu.createAnnotationArgument(null, "/" + name.toLowerCase())); //NOI18N
                            ClassTree cls = gu.addAnnotation((ClassTree) origTree, gu.createAnnotation("io.micronaut.http.annotation.Controller", annArgs)); //NOI18N
                            if (repositoryFQN != null) {
                                String repositoryFieldName = name.substring(0, 1).toLowerCase() + name.substring(1) + "Repository"; //NOI18N
                                VariableTree repositoryField = tm.Variable(tm.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)), repositoryFieldName, tm.QualIdent(repositoryFQN), null);
                                cls = tm.addClassMember(cls, repositoryField);
                                cls = tm.addClassMember(cls, GeneratorUtilities.get(copy).createConstructor(cls, Collections.singleton(repositoryField)));
                                TypeElement te = copy.getElements().getTypeElement(repositoryFQN);
                                MethodTree mt = te != null ? Utils.createControllerDataEndpointMethod(copy, te, repositoryFieldName, "findAll", null) : null;
                                if (mt != null) {
                                    cls = tm.addClassMember(cls, mt);
                                }
                            } else {
                                List<ExpressionTree> getAnnArgs = Arrays.asList(gu.createAnnotationArgument("uri", "/"), gu.createAnnotationArgument("produces", "text/plain")); //NOI18N
                                ModifiersTree mods = tm.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.singletonList(gu.createAnnotation("io.micronaut.http.annotation.Get", getAnnArgs))); //NOI18N
                                MethodTree indexMethod = tm.Method(mods, "index", tm.QualIdent("java.lang.String"), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{return \"Example Response\";}", null); //NOI18N
                                cls = tm.addClassMember(cls, indexMethod);
                            }
                            copy.rewrite(origTree, cls);
                        }
                    }).commit();
                }
            }
            return fo;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
