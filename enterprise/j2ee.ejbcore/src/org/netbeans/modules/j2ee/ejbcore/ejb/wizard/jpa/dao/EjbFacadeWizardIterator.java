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
package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.SessionGenerator;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.action.EntityManagerGenerator;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.WizardProperties;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Generates EJB facades for entity classes.
 *
 * @author Martin Adamek, Erno Mononen
 */
    public final class EjbFacadeWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private static final Logger LOGGER = Logger.getLogger(EjbFacadeWizardIterator.class.getName());

    private static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N

    private static final String FACADE_ABSTRACT = "AbstractFacade"; //NOI18N
    private static final String FACADE_SUFFIX = "Facade"; //NOI18N
    private static final String FACADE_REMOTE_SUFFIX = FACADE_SUFFIX + "Remote"; //NOI18N
    private static final String FACADE_LOCAL_SUFFIX = FACADE_SUFFIX + "Local"; //NOI18N
    private static final String EJB_LOCAL = "javax.ejb.Local"; //NOI18N
    private static final String EJB_LOCAL_JAKARTA = "jakarta.ejb.Local"; //NOI18N
    private static final String EJB_REMOTE = "javax.ejb.Remote"; //NOI18N
    private static final String EJB_REMOTE_JAKARTA = "jakarta.ejb.Remote"; //NOI18N
    protected static final String EJB_STATELESS = "javax.ejb.Stateless"; //NOI18N
    protected static final String EJB_STATELESS_JAKARTA = "jakarta.ejb.Stateless"; //NOI18N

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String[] steps;
    private int stepsStartPos;
    private Project project;
    /**
     * Contains the names of the entities. Key the FQN class name,
     * value the name of the entity.
     */
    private final Map<String, String> entityNames = new HashMap<String, String>();

    private static final String EJB30_STATELESS_EJBCLASS = "Templates/J2EE/EJB30/StatelessEjbClass.java"; // NOI18N

    private WizardDescriptor.Panel[] getPanels() {
        return panels;
    }

    @Override
    public Set instantiate() throws IOException {
        assert true : "should never be called, instantiate(ProgressHandle) should be called instead";
            return null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            EjbFacadeWizardPanel2.afterFinishAction.set(true);
            return instantiateWProgress(handle);
        } finally {
            handle.finish();
        }
    }

    private Set instantiateWProgress(ProgressHandle handle) throws IOException {
        this.project = Templates.getProject(wizard);
        initEntityNames();
        @SuppressWarnings("unchecked")
        List<String> entities = (List<String>) wizard.getProperty(WizardProperties.ENTITY_CLASS);

        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final EjbFacadeWizardPanel2 panel = (EjbFacadeWizardPanel2) panels[1];
        String pkg = panel.getPackage();

        boolean createPersistenceUnit = (Boolean) wizard.getProperty(WizardProperties.CREATE_PERSISTENCE_UNIT);
        int stepsCount = entities.size() + (createPersistenceUnit ? 1 : 0);
        int step = 0;
        handle.start(stepsCount);

        if (createPersistenceUnit) {
            PersistenceUnitWizardDescriptor puPanel = (PersistenceUnitWizardDescriptor) (panels[panels.length - 1] instanceof PersistenceUnitWizardDescriptor ? panels[panels.length - 1] : null);
            if(puPanel!=null){
                    handle.progress(NbBundle.getMessage(EjbFacadeWizardIterator.class, "MSG_AddPU"), step++);
                    PersistenceUnit punit = Util.buildPersistenceUnitUsingData(project, puPanel.getPersistenceUnitName(), puPanel.getPersistenceConnection()!=null ? puPanel.getPersistenceConnection().getName() : puPanel.getDatasource(), TableGeneration.NONE, puPanel.getSelectedProvider());
                    ProviderUtil.setTableGeneration(punit, puPanel.getTableGeneration(), puPanel.getSelectedProvider());
                    if (punit != null){
                        Util.addPersistenceUnitToProject( project, punit);
                    }
            }
        }

        for (String entity : entities) {
            handle.progress(NbBundle.getMessage(EjbFacadeWizardIterator.class, "MSG_GenSessionBean", entity), step++);
            createdFiles.addAll(generate(project, targetFolder, entity, pkg, panel.isRemote(), panel.isLocal(), panel.getRemoteInterfaceProject(), panel.getEntityProject(), false));
        }

        PersistenceUtils.logUsage(EjbFacadeWizardIterator.class, "USG_PERSISTENCE_SESSIONBEAN", new Integer[]{entities.size()});

        return createdFiles;
    }

    /**
     * Generates the facade and the loca/remote interface(s) for the given
     * entity class.
     * @param targetFolder the folder where the facade and interfaces are generated.
     * @param entityClass the FQN of the entity class for which the facade is generated.
     * @param pkg the package prefix for the generated facede.
     * @param hasRemote specifies whether a remote interface is generated.
     * @param hasLocal specifies whether a local interface is generated.
     *
     * @return a set containing the generated files.
     */
    private Set<FileObject> generate(final Project project,final FileObject targetFolder, final String entityClass, String pkg, final boolean hasRemote, final boolean hasLocal, Project remoteProject, Project entityProject, boolean overrideExisting) throws IOException {
        return generate(project, targetFolder, entityClass, pkg, hasRemote, hasLocal, remoteProject, entityProject, ContainerManagedJTAInjectableInEJB.class, overrideExisting);
    }


    /**
     * Generates the facade and the loca/remote interface(s) for thhe given
     * entity class.
     * <i>Package private visibility for tests</i>.
     * @param targetFolder the folder where the facade and interfaces are generated.
     * @param entityClass the FQN of the entity class for which the facade is generated.
     * @param pkg the package prefix for the generated facede.
     * @param hasRemote specifies whether a remote interface is generated.
     * @param hasLocal specifies whether a local interface is generated.
     * @param strategyClass the entity manager lookup strategy.
     *
     * @return a set containing the generated files.
     */
    Set<FileObject> generate(final Project project, final FileObject targetFolder, final String entityFQN,
            final String pkg, final boolean hasRemote, final boolean hasLocal,
            final Project remoteProject,
            final Project entityProject,
            final Class<? extends EntityManagerGenerationStrategy> strategyClass,
            boolean overrideExisting) throws IOException {

        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final String entitySimpleName = JavaIdentifiers.unqualify(entityFQN);
        final String variableName = entitySimpleName.toLowerCase().charAt(0) + entitySimpleName.substring(1);

        final boolean jakartaVariant;
        final ClassPath targetClassPath = ClassPath.getClassPath(targetFolder, ClassPath.COMPILE);
        if (targetClassPath != null) {
            final FileObject javaxStatelessFo = targetClassPath.findResource(EJB_STATELESS.replace(".", "/") + ".class");
            final FileObject jakartaStatelessFo = targetClassPath.findResource(EJB_STATELESS_JAKARTA.replace(".", "/") + ".class");
            jakartaVariant = javaxStatelessFo == null || jakartaStatelessFo != null;
        } else {
            jakartaVariant = true;
        }

        //create the abstract facade class
        Task<CompilationController> waiter = null;
        final String afName = pkg.isEmpty() ? FACADE_ABSTRACT : pkg + "." + FACADE_ABSTRACT; //NOI18N
        FileObject afFO = targetFolder.getFileObject(FACADE_ABSTRACT, "java");
        if (afFO == null){
            afFO = GenerationUtils.createClass(targetFolder, FACADE_ABSTRACT, null);
            createdFiles.add(afFO);

            JavaSource source = JavaSource.forFileObject(afFO);
            source.runModificationTask(new Task<WorkingCopy>(){
                @Override
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(Phase.RESOLVED);
                    ClassTree classTree = SourceUtils.getPublicTopLevelTree(workingCopy);
                    assert classTree != null;
                    TreeMaker maker = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    TreePath classTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree);
                    TypeElement classElement = (TypeElement)workingCopy.getTrees().getElement(classTreePath);

                    String genericsTypeName = "T";      //NOI18N
                    List<GenerationOptions> methodOptions = getAbstractFacadeMethodOptions(genericsTypeName, "entity", jakartaVariant); //NOI18N
                    List<Tree> members = new ArrayList<Tree>();
                    String entityClassVar = "entityClass";                                              //NOI18N
                    Tree classObjectTree = genUtils.createType("java.lang.Class<" + genericsTypeName + ">", classElement);     //NOI18N
                    members.add(maker.Variable(genUtils.createModifiers(Modifier.PRIVATE),entityClassVar,classObjectTree,null));
                    members.add(maker.Constructor(
                            genUtils.createModifiers(Modifier.PUBLIC),
                            Collections.<TypeParameterTree>emptyList(),
                            Arrays.asList(new VariableTree[]{genUtils.createVariable(entityClassVar,classObjectTree)}),
                            Collections.<ExpressionTree>emptyList(),
                            "{this." + entityClassVar + " = " + entityClassVar + ";}"));    //NOI18N
                    for(GenerationOptions option: methodOptions){
                        Tree returnType = (option.getReturnType() == null || option.getReturnType().equals("void"))?  //NOI18N
                                                maker.PrimitiveType(TypeKind.VOID):
                                                genUtils.createType(option.getReturnType(), classElement);
                        List<VariableTree> vars = option.getParameterName() == null ? Collections.<VariableTree>emptyList() :
                            Arrays.asList(new VariableTree[]{
                            genUtils.createVariable(
                                    option.getParameterName(),
                                    genUtils.createType(option.getParameterType(), classElement)
                                    )
                        });

                        if (option.getOperation() == null){
                            members.add(maker.Method(
                                    maker.Modifiers(option.getModifiers()),
                                    option.getMethodName(),
                                    returnType,
                                    Collections.<TypeParameterTree>emptyList(),
                                    vars,
                                    Collections.<ExpressionTree>emptyList(),
                                    (BlockTree)null,
                                 null));
                        } else {
                            members.add(maker.Method(
                                    maker.Modifiers(option.getModifiers()),
                                    option.getMethodName(),
                                    returnType,
                                    Collections.<TypeParameterTree>emptyList(),
                                    vars,
                                    Collections.<ExpressionTree>emptyList(),
                                    "{" + option.getCallLines("getEntityManager()", entityClassVar, project!=null ? PersistenceUtils.getJPAVersion(project) : Persistence.VERSION_1_0) + "}", //NOI18N
                                    null));
                    }
                    }

                    ClassTree newClassTree = maker.Class(
                            maker.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT)),
                            classTree.getSimpleName(),
                            Arrays.asList(maker.TypeParameter(genericsTypeName, Collections.<ExpressionTree>emptyList())),
                            null,
                            Collections.<Tree>emptyList(),
                            members);

                    workingCopy.rewrite(classTree, newClassTree);
                }
            }).commit();

            waiter = new Task<CompilationController>(){
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                }
            };
        }

        // create the facade
        FileObject existingFO = targetFolder.getFileObject(entitySimpleName + FACADE_SUFFIX, "java");
        if (existingFO != null) {
            if (overrideExisting) {
                existingFO.delete();
            } else {
                throw new IOException("File already exists exception: " + existingFO.getPath());
            }
        }
        final FileObject facade = GenerationUtils.createClass(targetFolder, entitySimpleName + FACADE_SUFFIX, null);
        createdFiles.add(facade);

        // generate methods for the facade
        EntityManagerGenerator generator = new EntityManagerGenerator(facade, entityFQN);
        List<GenerationOptions> methodOptions = getMethodOptions(entityFQN, variableName, jakartaVariant);
        for (GenerationOptions each : methodOptions){
            generator.generate(each, strategyClass);
        }

        // create the interfaces
        final String localInterfaceFQN = pkg + "." + getUniqueClassName(entitySimpleName + FACADE_LOCAL_SUFFIX, targetFolder);
        final String remoteInterfaceFQN = pkg + "." + getUniqueClassName(entitySimpleName + FACADE_REMOTE_SUFFIX, targetFolder);

        List<GenerationOptions> intfOptions = getAbstractFacadeMethodOptions(entityFQN, variableName, jakartaVariant);
        if (hasLocal) {
            final SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String simpleName = JavaIdentifiers.unqualify(localInterfaceFQN);
            if (!interfaceExists(groups, pkg, simpleName)) {
                FileObject local = createInterface(simpleName, jakartaVariant ? EJB_LOCAL_JAKARTA : EJB_LOCAL, targetFolder);
                addMethodToInterface(intfOptions, local);
                createdFiles.add(local);
            }
        }
        if (hasRemote) {
            final SourceGroup[] groups = ProjectUtils.getSources(remoteProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String simpleName = JavaIdentifiers.unqualify(remoteInterfaceFQN);
            if (!interfaceExists(groups, pkg, simpleName)) {
                FileObject remotePackage = SessionGenerator.createRemoteInterfacePackage(remoteProject, pkg, targetFolder);
                FileObject remote = createInterface(simpleName, jakartaVariant ? EJB_REMOTE_JAKARTA : EJB_REMOTE, remotePackage);
                addMethodToInterface(intfOptions, remote);
                createdFiles.add(remote);
                if (entityProject != null && !entityProject.getProjectDirectory().equals(remoteProject.getProjectDirectory())) {
                    if (groups != null && groups.length > 0) {
                        FileObject fo = groups[0].getRootFolder();
                        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                        if (cp != null) {
                            try {
                                ProjectClassPathModifier.addProjects(new Project[]{entityProject}, fo, ClassPath.COMPILE);
                            } catch (IOException | UnsupportedOperationException e) {
                                NotifyDescriptor d = new NotifyDescriptor.Message(
                                        NbBundle.getMessage(EjbFacadeWizardIterator.class, "WARN_UpdateClassPath",
                                        ProjectUtils.getInformation(remoteProject).getDisplayName(),
                                        ProjectUtils.getInformation(entityProject).getDisplayName()),
                                        NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(d);
                            }
                        }
                    }
                }
            }
        }

        final FileObject abstractFacadeFO = afFO;
        // add the @stateless annotation
        // add implements and extends clauses to the facade
        Task<WorkingCopy> modificationTask = new Task<WorkingCopy>(){
            @Override
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(Phase.RESOLVED);
                String fqn = pkg.isEmpty() ? entitySimpleName + FACADE_SUFFIX : pkg + "." + entitySimpleName + FACADE_SUFFIX;
                TypeElement classElement = wc.getElements().getTypeElement(fqn);
                ClassTree classTree = wc.getTrees().getTree(classElement);
                if (classTree == null) {
                    StringBuilder message = new StringBuilder();
                    message.append("Facade fileObject: {0} [valid={1}]").append("\n")           //NOI18N
                            .append("Facade source package: {2}, entityName: {3}").append("\n") //NOI18N
                            .append("ClassElement: {4}").append("\n");                          //NOI18N
                    String loggingMessage = MessageFormat.format(message.toString(),
                            new Object[]{facade, facade != null ? facade.isValid() : "null", pkg, entitySimpleName, classElement}); //NOI18N
                    LOGGER.severe(loggingMessage);
                }
                GenerationUtils genUtils = GenerationUtils.newInstance(wc);
                TreeMaker maker = wc.getTreeMaker();

                List<Tree> implementsClause = new ArrayList<Tree>(classTree.getImplementsClause());
                if (hasLocal)
                    implementsClause.add(genUtils.createType(localInterfaceFQN, classElement));
                if (hasRemote)
                    implementsClause.add(genUtils.createType(remoteInterfaceFQN, classElement));

                List<Tree> members = new ArrayList<Tree>(classTree.getMembers());
                MethodTree constructor = maker.Constructor(
                        genUtils.createModifiers(Modifier.PUBLIC),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{super(" + entitySimpleName + ".class);}");            //NOI18N
                members.add(constructor);

                TypeElement abstactFacadeElement = wc.getElements().getTypeElement(afName);
                TypeElement entityElement = wc.getElements().getTypeElement(entityFQN);
                if (abstactFacadeElement == null) {
                    LOGGER.log(Level.SEVERE, "TypeElement not found for {0}", afName);
                    LOGGER.log(Level.SEVERE, "AbstractFacade:path={0},valid={1},canRead={2},", new Object[]{
                        abstractFacadeFO.getPath(), abstractFacadeFO.isValid(), abstractFacadeFO.canRead()});
                } else if (entityElement == null) {
                    LOGGER.log(Level.SEVERE, "TypeElement not found for {0}", entityFQN);
                }

                TypeMirror eeType = entityElement.asType();
                LOGGER.log(Level.INFO, "Entity element type:kind={0},type={1}", new Object[]{eeType.getKind().toString(), eeType.toString()});

                DeclaredType declaredType = wc.getTypes().getDeclaredType(abstactFacadeElement, entityElement.asType());
                Tree extendsClause = maker.Type(declaredType);
                ClassTree newClassTree = maker.Class(
                        maker.addModifiersAnnotation(classTree.getModifiers(), genUtils.createAnnotation(jakartaVariant ? EJB_STATELESS_JAKARTA : EJB_STATELESS)),
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        extendsClause,
                        implementsClause,
                        members);

                wc.rewrite(classTree, newClassTree);
            }
        };

        if (waiter != null){
            try {
                JavaSource.forFileObject(afFO).runWhenScanFinished(waiter, true).get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        JavaSource facadeJS = JavaSource.forFileObject(facade);
        if (facadeJS == null) {
            ClassPath classPath = ClassPath.getClassPath(facade, ClassPath.SOURCE);
            if (classPath == null) {
                LOGGER.log(Level.WARNING, "facade FO: valid={0}, sourceCP=null", facade.isValid());
            } else {
                LOGGER.log(
                        Level.WARNING,
                        "facade FO: valid={0}, onSourceCP={1}",
                        new Object[]{facade.isValid(), classPath.contains(facade)});
            }
        }
        facadeJS.runModificationTask(modificationTask).commit();

        return createdFiles;
    }

    /**
     * @return the options representing the methods for a facade, i.e. create/edit/
     * find/remove/findAll.
     */
    private List<GenerationOptions> getMethodOptions(String entityFQN, String variableName, boolean jakartaVariant){

        GenerationOptions getEMOptions = new GenerationOptions();
        getEMOptions.setAnnotation("java.lang.Override"); //NOI18N
        getEMOptions.setMethodName("getEntityManager"); //NOI18N
        getEMOptions.setOperation(GenerationOptions.Operation.GET_EM);
        getEMOptions.setReturnType(jakartaVariant ? "jakarta.persistence.EntityManager" : "javax.persistence.EntityManager");//NOI18N
        getEMOptions.setModifiers(EnumSet.of(Modifier.PROTECTED));

        return Arrays.<GenerationOptions>asList(getEMOptions);
    }

    private List<GenerationOptions> getAbstractFacadeMethodOptions(String entityFQN, String variableName, boolean jakartaVariant){
        //abstract methods

        GenerationOptions getEMOptions = new GenerationOptions();
        getEMOptions.setMethodName("getEntityManager"); //NOI18N
        getEMOptions.setReturnType(jakartaVariant ? "jakarta.persistence.EntityManager" : "javax.persistence.EntityManager");//NOI18N
        getEMOptions.setModifiers(EnumSet.of(Modifier.PROTECTED, Modifier.ABSTRACT));

        //implemented methods
        GenerationOptions createOptions = new GenerationOptions();
        createOptions.setMethodName("create"); //NOI18N
        createOptions.setOperation(GenerationOptions.Operation.PERSIST);
        createOptions.setReturnType("void");//NOI18N
        createOptions.setParameterName(variableName);
        createOptions.setParameterType(entityFQN);

        GenerationOptions editOptions = new GenerationOptions();
        editOptions.setMethodName("edit");//NOI18N
        editOptions.setOperation(GenerationOptions.Operation.MERGE);
        editOptions.setReturnType("void");//NOI18N
        editOptions.setParameterName(variableName);
        editOptions.setParameterType(entityFQN);

        GenerationOptions destroyOptions = new GenerationOptions();
        destroyOptions.setMethodName("remove");//NOI18N
        destroyOptions.setOperation(GenerationOptions.Operation.REMOVE);
        destroyOptions.setReturnType("void");//NOI18N
        destroyOptions.setParameterName(variableName);
        destroyOptions.setParameterType(entityFQN);

        GenerationOptions findOptions = new GenerationOptions();
        findOptions.setMethodName("find");//NOI18N
        findOptions.setOperation(GenerationOptions.Operation.FIND);
        findOptions.setReturnType(entityFQN);//NOI18N
        findOptions.setParameterName("id");//NOI18N
        findOptions.setParameterType("Object");//NOI18N

        GenerationOptions findAllOptions = new GenerationOptions();
        findAllOptions.setMethodName("findAll");//NOI18N
        findAllOptions.setOperation(GenerationOptions.Operation.FIND_ALL);
        findAllOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findAllOptions.setQueryAttribute(getEntityName(entityFQN));

        GenerationOptions findSubOptions = new GenerationOptions();
        findSubOptions.setMethodName("findRange");//NOI18N
        findSubOptions.setOperation(GenerationOptions.Operation.FIND_SUBSET);
        findSubOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findSubOptions.setQueryAttribute(getEntityName(entityFQN));
        findSubOptions.setParameterName("range");//NOI18N
        findSubOptions.setParameterType("int[]");//NOI18N

        GenerationOptions countOptions = new GenerationOptions();
        countOptions.setMethodName("count");//NOI18N
        countOptions.setOperation(GenerationOptions.Operation.COUNT);
        countOptions.setReturnType("int");//NOI18N
        countOptions.setQueryAttribute(getEntityName(entityFQN));

        return Arrays.<GenerationOptions>asList(getEMOptions, createOptions, editOptions, destroyOptions, findOptions, findAllOptions, findSubOptions, countOptions);
    }

    /**
     *@return the name for the given <code>entityFQN</code>.
     */
    private String getEntityName(String entityFQN){
        String result = entityNames.get(entityFQN);
        return result != null ? result : JavaIdentifiers.unqualify(entityFQN);
    }

    /**
     * Initializes the {@link #entityNames} map.
     */
    private void initEntityNames() throws IOException{
        if (project == null){
            // just to facilitate testing, avoids the need to provide a project (together with the getEntityName method)
            return;
        }
        //XXX should probably be using MetadataModelReadHelper. needs a progress indicator as well (#113874).
        try {
            EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(project.getProjectDirectory());
            MetadataModel<EntityMappingsMetadata> entityMappingsModel = entityClassScope.getEntityMappingsModel(true);
            Future<Void> result = entityMappingsModel.runReadActionWhenReady(new MetadataModelAction<EntityMappingsMetadata, Void>() {

                @Override
                public Void run(EntityMappingsMetadata metadata) throws Exception {
                    for (Entity entity : metadata.getRoot().getEntity()) {
                        entityNames.put(entity.getClass2(), entity.getName());
                    }
                    return null;
                }
            });
            result.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    String getUniqueClassName(String candidateName, FileObject targetFolder){
        return FileUtil.findFreeFileName(targetFolder, candidateName, "java"); //NOI18N
    }

    /**
     * Creates an interface with the given <code>name</code>, annotated with an annotation
     * of the given <code>annotationType</code>. <i>Package private visibility just because of tests</i>.
     *
     * @param name the name for the interface
     * @param annotationType the FQN of the annotation
     * @param targetFolder the folder to which the interface is generated
     *
     * @return the generated interface.
     */
    FileObject createInterface(String name, final String annotationType, FileObject targetFolder) throws IOException {
        FileObject sourceFile = GenerationUtils.createInterface(targetFolder, name, null);
        JavaSource source = JavaSource.forFileObject(sourceFile);
        if (source == null) {
            LOGGER.log(Level.SEVERE, "JavaSource not created for FileObject: path={0}, valid={1}, mime-type={2}",
                    new Object[]{sourceFile.getPath(), sourceFile.isValid(), sourceFile.getMIMEType()});
        }
        ModificationResult result = source.runModificationTask(new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = SourceUtils.getPublicTopLevelTree(workingCopy);
                assert clazz != null;
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                TreeMaker make = workingCopy.getTreeMaker();
                AnnotationTree annotations = genUtils.createAnnotation(annotationType);
                ModifiersTree modifiers = make.Modifiers(clazz.getModifiers(), Collections.<AnnotationTree>singletonList(annotations));
                ClassTree modifiedClass = make.Class(modifiers, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList());
                workingCopy.rewrite(clazz, modifiedClass);
            }
        });
        result.commit();
        return source.getFileObjects().iterator().next();
    }

    /**
     * Adds a method to the given interface.
     *
     * @param name the name of the method.
     * @param returnType the return type of the method.
     * @param parameterName the name of the parameter for the method.
     * @param parameterType the FQN type of the parameter.
     * @param target the target interface.
     */
    void addMethodToInterface(final List<GenerationOptions> options, final FileObject target) throws IOException {

        JavaSource source = JavaSource.forFileObject(target);
        ModificationResult result = source.runModificationTask(new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                GenerationUtils utils = GenerationUtils.newInstance(copy);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(copy);
                assert typeElement != null;
                ClassTree original = copy.getTrees().getTree(typeElement);
                ClassTree modifiedClass = original;
                TreeMaker make = copy.getTreeMaker();
                for (GenerationOptions each : options) {
                    if (each.getModifiers().size() == 1 && each.getModifiers().contains(Modifier.PUBLIC)){
                        MethodTree method = make.Method(make.Modifiers(Collections.<Modifier>emptySet()),
                            each.getMethodName(), utils.createType(each.getReturnType(), typeElement),
                            Collections.<TypeParameterTree>emptyList(), getParameterList(each, make, utils, typeElement),
                            Collections.<ExpressionTree>emptyList(), (BlockTree) null, null);
                        modifiedClass = make.addClassMember(modifiedClass, method);
                    }
                }
                copy.rewrite(original, modifiedClass);
            }
        });
        result.commit();
    }

    private List<VariableTree> getParameterList(GenerationOptions options, TreeMaker make, GenerationUtils utils, TypeElement scope){
        if (options.getParameterName() == null){
            return Collections.<VariableTree>emptyList();
        }
        VariableTree vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                options.getParameterName(), utils.createType(options.getParameterType(), scope), null);
        return Collections.<VariableTree>singletonList(vt);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizard.putProperty("NewFileWizard_Title", NbBundle.getMessage(EjbFacadeWizardIterator.class, "Templates/Persistence/ejbFacade"));
        project = Templates.getProject(wizard);

        // http://www.netbeans.org/issues/show_bug.cgi?id=126642
        //if (Templates.getTargetFolder(wizard) == null) {
        //    Templates.setTargetFolder(wizard, project.getProjectDirectory());
        //}

        if (panels == null) {
            boolean noPuNeeded = true;
            try {
                noPuNeeded = ProviderUtil.persistenceExists(project) || !ProviderUtil.isValidServerInstanceOrNone(project);
            } catch (InvalidPersistenceXmlException ex) {
                Logger.getLogger(EjbFacadeWizardIterator.class.getName()).log(Level.FINE, "Invalid persistence.xml: "+ ex.getPath()); //NOI18N
            }
            String names[]=null;

            WizardDescriptor.Panel persistenceClientEntitySelectionPanel = new PersistenceClientEntitySelection(NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"), new HelpCtx(EjbFacadeWizardIterator.class.getName() + "$PersistenceClientEntitySelection"), wizard);
            // persistence panel improved about server validation
            WizardDescriptor.Panel validatedPersistenceClientEntitySelectionPanel = new AppServerValidationPanel(persistenceClientEntitySelectionPanel);
            EjbFacadeWizardPanel2 ejbFacadeWizardPanel2 = new EjbFacadeWizardPanel2(project, wizard);

            if (noPuNeeded) {
                panels = new WizardDescriptor.Panel[]{
                    validatedPersistenceClientEntitySelectionPanel,
                    ejbFacadeWizardPanel2};
                names = new String[]{
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"),
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_GeneratedSessionBeans")};
            } else {
                panels = new WizardDescriptor.Panel[]{
                    validatedPersistenceClientEntitySelectionPanel,
                    ejbFacadeWizardPanel2,
                    new PersistenceUnitWizardDescriptor(project)};
                names = new String[]{
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"),
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_GeneratedSessionBeans"),
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_PersistenceUnitSetup")};
            }


            if (steps == null) {
                mergeSteps(names);
            }
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (c instanceof JComponent) {
                    // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    //set accessible description
                    jc.getAccessibleContext().setAccessibleDescription( (i+stepsStartPos)<steps.length ? steps[i+stepsStartPos] : jc.getName() );
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_FacadeWizardTitle");
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {}
    @Override
    public void removeChangeListener(ChangeListener l) {}

    private void mergeSteps(String[] thisSteps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;

        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            stepsStartPos = beforeSteps.length;
            if (stepsStartPos > 0 && ("...".equals(beforeSteps[stepsStartPos - 1]))) { // NOI18N
                stepsStartPos--;
            }
        } else {
            beforeSteps = null;
            stepsStartPos = 0;
        }

        steps = new String[stepsStartPos + thisSteps.length];
        System.arraycopy(beforeSteps, 0, steps, 0, stepsStartPos);
        System.arraycopy(thisSteps, 0, steps, stepsStartPos, thisSteps.length);
    }

    public static FileObject[] generateSessionBeans(ProgressContributor progressContributor, ProgressPanel progressPanel, List<String> entities, Project project, String jpaControllerPackage, FileObject jpaControllerPackageFileObject, boolean local, boolean remote, Project remoteProject, Project entityProject) throws IOException {
        return generateSessionBeans(progressContributor, progressPanel, entities, project, jpaControllerPackage, jpaControllerPackageFileObject, local, remote, remoteProject, entityProject, false);
    }

    public static FileObject[] generateSessionBeans(ProgressContributor progressContributor, ProgressPanel progressPanel, List<String> entities, Project project, String jpaControllerPackage, FileObject jpaControllerPackageFileObject, boolean local, boolean remote, Project remoteProject, Project entityProject, boolean overrideExisting) throws IOException {
        int progressIndex = 0;
        String progressMsg =  NbBundle.getMessage(EjbFacadeWizardIterator.class, "MSG_Progress_SessionBean_Pre"); //NOI18N;
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);

        int[] nameAttemptIndices = null;

        EjbFacadeWizardIterator iterator=new EjbFacadeWizardIterator();

        FileObject[] sbFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            final String entitySimpleName = JavaIdentifiers.unqualify(entities.get(i));

            progressMsg = NbBundle.getMessage(EjbFacadeWizardIterator.class, "MSG_Progress_SessionBean_Now_Generating", entitySimpleName + FACADE_SUFFIX + ".java");//NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);
            sbFileObjects[i]=iterator.generate(project, jpaControllerPackageFileObject, entities.get(i), jpaControllerPackage, local, remote, remoteProject, entityProject, overrideExisting).iterator().next();
        }

        PersistenceUtils.logUsage(EjbFacadeWizardIterator.class, "USG_PERSISTENCE_SESSIONBEAN", new Integer[]{entities.size()});

        return sbFileObjects;
    }

    public static  int getProgressStepCount(int numEntites)
    {
        return numEntites;
    }

    private static boolean interfaceExists(SourceGroup[] groups, String pkg, String simpleName) {
        String path = pkg.replace(".", "/"); //NOIN18N
        for (SourceGroup sourceGroup : groups) {
            FileObject pkgFO = sourceGroup.getRootFolder().getFileObject(path);
            if (pkgFO != null) {
                if (pkgFO.getFileObject(simpleName + ".java") != null) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }
}
