/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
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
                    NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine(Bundle.MSG_SelectControllerName(), Bundle.MSG_SelectControllerName());
                    if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(inputLine)) {
                        List<FileObject> generated = new ArrayList<>();
                        String name = inputLine.getInputText();
                        if (!name.isEmpty()) {
                            String controllerName = name.substring(0, 1).toUpperCase() + name.substring(1) + "Controller"; // NOI18N
                            FileObject fo = generate(desc.getTarget(), controllerName);
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

    private WizardDescriptor.Panel panel;
    private WizardDescriptor wizardDescriptor;

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        Set<DataObject> generated = new HashSet<>();
        FileObject targetFolder = Templates.getTargetFolder(wiz);
        String targetName = Templates.getTargetName(wiz);
        if (targetFolder != null && targetName != null && !targetName.isEmpty()) {
            FileObject fo = generate(targetFolder, targetName);
            if (fo != null) {
                generated.add(DataObject.find(fo));
            }
        }
        return generated;
    }

    @Override
    public void initialize(TemplateWizard wiz) {
        wizardDescriptor = wiz;

        Project project = Templates.getProject(wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        if(sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            panel = Templates.buildSimpleTargetChooser(project, sourceGroups).create();
        } else {
            panel = JavaTemplates.createPackageChooser(project, sourceGroups);
        }

        Wizards.mergeSteps(wiz, new WizardDescriptor.Panel[] {panel}, null);
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panel;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void nextPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public void previousPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @NbBundle.Messages({
        "MSG_ControllerClass=Controller class {0}\n"
    })
    private static FileObject generate(FileObject folder, String controllerName) {
        try {
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
                            String name = controllerName.toLowerCase();
                            if (name.endsWith("controller")) {
                                name = name.substring(0, name.length() - 10);
                            }
                            List<ExpressionTree> annArgs = Collections.singletonList(gu.createAnnotationArgument(null, "/" + name.toLowerCase())); //NOI18N
                            ClassTree cls = gu.addAnnotation((ClassTree) origTree, gu.createAnnotation("io.micronaut.http.annotation.Controller", annArgs));
                            List<ExpressionTree> getAnnArgs = Arrays.asList(gu.createAnnotationArgument("uri", "/"), gu.createAnnotationArgument("produces", "text/plain")); //NOI18N
                            ModifiersTree mods = tm.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.singletonList(gu.createAnnotation("io.micronaut.http.annotation.Get", getAnnArgs))); //NOI18N
                            MethodTree indexMethod = tm.Method(mods, "index", tm.QualIdent("java.lang.String"), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{return \"Example Response\";}", null); //NOI18N
                            cls = tm.addClassMember(cls, indexMethod);
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
