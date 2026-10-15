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
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.QuickPick;
import org.openide.NotifyDescriptor.QuickPick.Item;
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
public class MicronautRepository implements TemplateWizard.Iterator {

    public static TemplateWizard.Iterator create() {
        return new MicronautRepository();
    }

    @NbBundle.Messages({
        "MSG_SelectEntities=Select Entity Classes",
        "MSG_NoEntities=No entity class found in {0}",
        "MSG_Repository_Type=Select Repository Type",
        "MSG_CrudRepository=CRUD Repository",
        "DESC_CrudRepository=CrudRepository enables automatic generation of CRUD operations.",
        "MSG_PageableRepository=Pageable Repository",
        "DESC_PageableRepository=PageableRepository extends CrudRepository and adds methods for pagination."
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
                    if (sourceGroup == null) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_NoSourceGroup(folder.getPath()), NotifyDescriptor.ERROR_MESSAGE));
                        return Collections.emptyList();
                    }
                    if (!Utils.isDBSupported(sourceGroup) && !Utils.isJPASupported(sourceGroup)) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_NoDdSupport(folder.getPath()), NotifyDescriptor.ERROR_MESSAGE));
                        return Collections.emptyList();
                    }
                    DatabaseConnection connection = ConnectionManager.getDefault().getPreferredConnection(true);
                    if (connection == null) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_NoDbConn(), NotifyDescriptor.ERROR_MESSAGE));
                        return Collections.emptyList();
                    }
                    final boolean jpaSupported = Utils.isJPASupported(sourceGroup);
                    final Map<String, String> entity2idTypes = getEntityClasses(sourceGroup, jpaSupported);
                    final List<NotifyDescriptor.QuickPick.Item> entities = new ArrayList<>();
                    for (String entityFQN : entity2idTypes.keySet()) {
                        int idx = entityFQN.lastIndexOf('.');
                        if (idx < 0) {
                            entities.add(new NotifyDescriptor.QuickPick.Item(entityFQN, null));
                        } else {
                            entities.add(new NotifyDescriptor.QuickPick.Item(entityFQN.substring(idx + 1), entityFQN.substring(0, idx)));
                        }
                    }
                    if (entities.isEmpty()) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_NoEntities(sourceGroup.getRootFolder().getPath()), NotifyDescriptor.ERROR_MESSAGE));
                        return Collections.emptyList();
                    }
                    List<Item> repoType = new ArrayList<>();
                    Item pageableItem = new Item(Bundle.MSG_PageableRepository(), null /*Bundle.DESC_PageableRepository()*/);
                    repoType.add(new Item(Bundle.MSG_CrudRepository(), null /*Bundle.DESC_CrudRepository()*/));
                    repoType.add(pageableItem);
                    QuickPick qpt = new QuickPick(Bundle.MSG_Repository_Type(), Bundle.MSG_Repository_Type(), repoType, false);
                    if (DialogDescriptor.OK_OPTION != DialogDisplayer.getDefault().notify(qpt)) {
                        return Collections.emptyList();
                    }
                    NotifyDescriptor.QuickPick qp = new NotifyDescriptor.QuickPick(Bundle.MSG_SelectEntities(), Bundle.MSG_SelectEntities(), entities, true);
                    if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(qp)) {
                        String dialect = getDialect(jpaSupported);
                        List<FileObject> generated = new ArrayList<>();
                        for (NotifyDescriptor.QuickPick.Item item : qp.getItems()) {
                            if (item.isSelected()) {
                                String fqn = item.getDescription() != null ? item.getDescription() + '.' + item.getLabel() : item.getLabel();
                                String entityIdType = entity2idTypes.get(fqn);
                                FileObject fo = generate(folder, item.getLabel(), fqn, entityIdType, dialect, pageableItem.isSelected());
                                if (fo != null) {
                                    generated.add(fo);
                                }
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
    private FileObject targetFolder;
    private boolean jpaSupported;

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        String dialect = getDialect(jpaSupported);
        Set<DataObject> generated = new HashSet<>();
        Map<String, String> selectedEntities = (Map<String, String>) wiz.getProperty(ClassesSelectorPanel.PROP_SELECTED_CLASSES);
        for (Map.Entry<String, String> entry : selectedEntities.entrySet()) {
            String fqn = entry.getKey();
            int idx = fqn.lastIndexOf('.');
            String label = idx < 0 ? fqn : fqn.substring(idx + 1);
            FileObject fo = generate(targetFolder, label, fqn, entry.getValue(), dialect, false);
            if (fo != null) {
                generated.add(DataObject.find(fo));
            }
        }
        return generated;
    }

    @Override
    public void initialize(TemplateWizard wiz) {
        wizardDescriptor = wiz;

        panel = new ClassesSelectorPanel.WizardPanel(NbBundle.getMessage(MicronautRepository.class, "Templates/Micronaut/Repository"), "Entities", selectedEntities -> {
            return selectedEntities.isEmpty() ? NbBundle.getMessage(MicronautRepository.class, "ERR_SelectEntities") : null;
        });
        Wizards.mergeSteps(wizardDescriptor, new WizardDescriptor.Panel[] {
            panel
        }, new String[] {
            NbBundle.getMessage(MicronautRepository.class, "LBL_Entities")
        });

        targetFolder = Templates.getTargetFolder(wizardDescriptor);
        Project project = Templates.getProject(wiz);
        SourceGroup sourceGroup = SourceGroups.getFolderSourceGroup(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA), targetFolder);
        if (sourceGroup != null) {
            jpaSupported = Utils.isJPASupported(sourceGroup);
            Map<String, String> entities = getEntityClasses(sourceGroup, jpaSupported);
            wiz.putProperty(ClassesSelectorPanel.PROP_CLASSES, entities);
        }
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

    private static Map<String, String> getEntityClasses(SourceGroup sg, boolean jpaSupported) {
        final Map<String, String> entities = new HashMap<>();
        JavaSource js = JavaSource.create(ClasspathInfo.create(sg.getRootFolder()));
        if (js != null) {
            try {
                js.runWhenScanFinished(cc -> {
                    TypeElement typeElement = cc.getElements().getTypeElement(jpaSupported ? "javax.persistence.Entity" : "io.micronaut.data.annotation.MappedEntity"); //NOI18N
                    if (typeElement != null) {
                        TypeElement[] idTypeElements = new TypeElement[] {
                            cc.getElements().getTypeElement(jpaSupported ? "javax.persistence.Id" : "io.micronaut.data.annotation.Id"), //NOI18N
                            cc.getElements().getTypeElement(jpaSupported ? "javax.persistence.EmbeddedId" : "io.micronaut.data.annotation.EmbeddedId") //NOI18N
                        };
                        Set<ElementHandle<TypeElement>> elementHandles = cc.getClasspathInfo().getClassIndex().getElements(ElementHandle.create(typeElement), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
                        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
                            TypeElement type = elementHandle.resolve(cc);
                            if (type != null) {
                                String fqn = null;
                                String idType = null;
                                for (AnnotationMirror annotationMirror : type.getAnnotationMirrors()) {
                                    if (fqn == null && typeElement == annotationMirror.getAnnotationType().asElement()) {
                                        fqn = type.getQualifiedName().toString();
                                    }
                                }
                                if (fqn != null) {
                                    for (TypeElement idTypeElement : idTypeElements) {
                                        if (idTypeElement != null) {
                                            for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
                                                if (idType == null) {
                                                    for (AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
                                                        if (idType == null && idTypeElement == annotationMirror.getAnnotationType().asElement()) {
                                                            idType = cc.getTypeUtilities().getTypeName(field.asType(), TypeUtilities.TypeNameOptions.PRINT_FQN).toString();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (idType == null) {
                                        idType = "java.lang.Object"; //NOI18N
                                    }
                                    entities.put(fqn, idType);
                                }
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return entities;
    }

    private static String getDialect(boolean jpaSupported) {
        if (!jpaSupported) {
            DatabaseConnection connection = ConnectionManager.getDefault().getPreferredConnection(true);
            if (connection != null) {
                try {
                    ConnectionManager.getDefault().connect(connection);
                    Connection conn = connection.getJDBCConnection();
                    String name = conn.getMetaData().getDatabaseProductName();
                    if (name.matches("(?i).*h2.*")) { //NOI18N
                        return "H2"; //NOI18N
                    } else if (name.matches("(?i).*mysql.*")) { //NOI18N
                        return "MYSQL"; //NOI18N
                    } else if (name.matches("(?i).*oracle.*")) { //NOI18N
                        return "ORACLE"; //NOI18N
                    } else if (name.matches("(?i).*postgresql.*")) { //NOI18N
                        return "POSTGRES"; //NOI18N
                    } else if (name.matches("(?i).*microsoft.*")) { //NOI18N
                        return "SQL_SERVER"; //NOI18N
                    }
                } catch (SQLException | DatabaseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return ""; //NOI18N
        }
        return null;
    }

    private static final String CRUD_REPOSITORY = "io.micronaut.data.repository.CrudRepository";            // NOI18N
    private static final String PAGEABLE_REPOSITORY = "io.micronaut.data.repository.PageableRepository";    // NOI18N

    @NbBundle.Messages({
        "MSG_Repository_Interface=Repository interface {0}\n"
    })
    private static FileObject generate(FileObject folder, String entityName, String entityFQN, String entityIdType, String dialect, boolean pageable) {
        try {
            String name = entityName + "Repository"; // NOI18N
            FileObject fo = GenerationUtils.createInterface(folder, name, Bundle.MSG_Repository_Interface(name));
            if (fo != null) {
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    js.runModificationTask(copy -> {
                        copy.toPhase(JavaSource.Phase.RESOLVED);
                        Tree origTree = copy.getCompilationUnit().getTypeDecls().get(0);
                        if (origTree.getKind() == Tree.Kind.INTERFACE) {
                            GenerationUtils gu = GenerationUtils.newInstance(copy);
                            TreeMaker tm = copy.getTreeMaker();
                            TypeMirror entityIdTM = copy.getTreeUtilities().parseType(entityIdType, (TypeElement) copy.getTrees().getElement(new TreePath(new TreePath(copy.getCompilationUnit()), origTree)));
                            List<ExpressionTree> args = Arrays.asList(tm.QualIdent(entityFQN), entityIdTM != null && entityIdTM.getKind().isPrimitive() ? tm.QualIdent(copy.getTypes().boxedClass((PrimitiveType) entityIdTM)) : tm.QualIdent(entityIdType));
                            String repoFaq = pageable ? PAGEABLE_REPOSITORY : CRUD_REPOSITORY;
                            ParameterizedTypeTree type = tm.ParameterizedType(tm.QualIdent(repoFaq), args);
                            ClassTree cls = tm.addClassImplementsClause((ClassTree) origTree, type);
                            if (dialect == null) {
                                cls = gu.addAnnotation(cls, gu.createAnnotation("io.micronaut.data.annotation.Repository")); //NOI18N
                            } else if (dialect.isEmpty()) {
                                cls = gu.addAnnotation(cls, gu.createAnnotation("io.micronaut.data.jdbc.annotation.JdbcRepository")); //NOI18N
                            } else {
                                List<ExpressionTree> annArgs = Collections.singletonList(gu.createAnnotationArgument("dialect", "io.micronaut.data.model.query.builder.sql.Dialect", dialect)); //NOI18N
                                cls = gu.addAnnotation(cls, gu.createAnnotation("io.micronaut.data.jdbc.annotation.JdbcRepository", annArgs)); //NOI18N
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
