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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.EntityMethodGenerator;
import org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * A wizard for creating entity classes.
 *
 * @author Martin Adamek
 * @author Erno Mononen
 */

public final class EntityWizard implements WizardDescriptor.InstantiatingIterator {

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private EntityWizardDescriptor ejbPanel;
    private PersistenceUnitWizardDescriptor puPanel;
    private WizardDescriptor wiz;
    private SourceGroup[] sourceGroups;
    
    public static EntityWizard create() {
        return new EntityWizard();
    }
    
    @Override
    public String name() {
        return NbBundle.getMessage(EntityWizard.class, "LBL_EntityEJBWizardTitle");
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
    }
    
    @Override
    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        Sources sources = ProjectUtils.getSources(project);
        sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        ejbPanel = new EntityWizardDescriptor();
        WizardDescriptor.Panel targetChooserPanel;
        // Need to check if there are any Java Source group. See issue 139851
        if(sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups( Sources.TYPE_GENERIC ); 
            targetChooserPanel = new ValidatingPanel(Templates.buildSimpleTargetChooser(project, sourceGroups).bottomPanel(ejbPanel).create(  ));
        } else {
            targetChooserPanel = new ValidatingPanel(JavaTemplates.createPackageChooser(project,sourceGroups, ejbPanel, true));
        }
        //
        boolean noPuNeeded = true;
        try {
            noPuNeeded = ProviderUtil.persistenceExists(project, Templates.getTargetFolder(wiz)) || !ProviderUtil.isValidServerInstanceOrNone(project);
        } catch (InvalidPersistenceXmlException | RuntimeException ex){
            Logger.getLogger(EntityWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml"); //NOI18N
        }
        if(noPuNeeded){
            panels =  new WizardDescriptor.Panel[] {targetChooserPanel};
        } else {
            puPanel = new PersistenceUnitWizardDescriptor(project);
            panels = new WizardDescriptor.Panel[] {targetChooserPanel, puPanel};
        }
        
        Wizards.mergeSteps(wiz, panels, null);
    }
    
    @Override
    public Set instantiate() throws IOException {
        Project project = Templates.getProject(wiz);

        ClassPath compileCP = ClassPath.getClassPath(sourceGroups[0].getRootFolder(), ClassPath.COMPILE);

        String enterprisePrefix = "jakarta";
        if (compileCP != null && compileCP.findResource("javax/persistence/Entity.class") != null) {
            enterprisePrefix = "javax";
        }
        
        FileObject result = generateEntity(
                Templates.getTargetFolder(wiz),
                Templates.getTargetName(wiz),
                ejbPanel.getPrimaryKeyClassName(),
                true, // setting field access type by default
                enterprisePrefix
                );
        
        try{
            boolean isCreatePU = ejbPanel.isCreatePU();
            if(isCreatePU)
            {
                PersistenceUnit punit = Util.buildPersistenceUnitUsingData(project, puPanel.getPersistenceUnitName(), puPanel.getPersistenceConnection()!=null ? puPanel.getPersistenceConnection().getName() : puPanel.getDatasource(), TableGeneration.NONE, puPanel.getSelectedProvider());
                ProviderUtil.setTableGeneration(punit, puPanel.getTableGeneration(), puPanel.getSelectedProvider());
                if (punit != null){
                    Util.addPersistenceUnitToProjectRoot( project, result, punit);
                }
            } else {
                Util.addPersistenceUnitToProject( project );
            }

            addEntityToPersistenceUnit(result);
            PersistenceUtils.logUsage(EntityWizard.class, "USG_PERSISTENCE_ENTITY", null);
        } catch (InvalidPersistenceXmlException ipx){
            // just log for debugging purposes, at this point the user has
            // already been warned about an invalid persistence.xml
            Logger.getLogger(EntityWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
        }
        
        return Collections.singleton(result);
    }
    
    /**
     * Adds the given entity to the persistence unit defined in the project in which this wizard
     * was invoked.
     * @param entity the entity to be added.
     */
    private void addEntityToPersistenceUnit(FileObject entity) throws InvalidPersistenceXmlException{
        
        Project project = Templates.getProject(wiz);
        if(project != null) {
            String entityFQN = "";
            ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
            if (classPathProvider != null) {
                ClassPath clsPath = classPathProvider.findClassPath(entity, ClassPath.SOURCE);
                if(clsPath == null ) {
                   return;
                }
                entityFQN = clsPath.getResourceName(entity, '.', false);
            }

            if (!(Util.isSupportedJavaEEVersion(project) && Util.isContainerManaged(project)) && ProviderUtil.getDDFile(project, entity) != null) {
                PUDataObject pudo = ProviderUtil.getPUDataObject(project, entity, null);
                try {
                    PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
                    //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
                    if (pu.length == 1) {
                        pudo.addClass(pu[0], entityFQN, false);
                    }
                } catch (RuntimeException ex) {
                    Logger.getLogger(EntityWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml"); //NOI18N
                }
            }
        }
    }
    
    
    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }
    
    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }
    
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.length - 1 && ejbPanel.isCreatePU();
    }
    
    @Override
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    @Override
    public void previousPanel() {
        if (! hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    /**
     * Generates an entity class.
     *
     * @param targetFolder the target folder for the entity.
     * @param targetName the target name of the entity.
     * @param primaryKeyClassName the name of the primary key class, needs to be
     *  resolvable in the generated entity's scope.
     * @param isAccessProperty defines the access strategy for the id field.
     * @return a FileObject representing the generated entity.
     */
    public static FileObject generateEntity(final FileObject targetFolder, final String targetName,
            final String primaryKeyClassName, final boolean isAccessProperty, final String enterprisePrefix) throws IOException {
        
        FileObject entityFo = GenerationUtils.createClass(targetFolder, targetName, null);
        ClassPath boot = ClassPath.getClassPath(targetFolder, ClassPath.BOOT);
        ClassPath moduleBoot = ClassPath.getClassPath(targetFolder, JavaClassPathConstants.MODULE_BOOT_PATH);
        ClassPath compile = ClassPath.getClassPath(targetFolder, ClassPath.COMPILE);
        ClassPath moduleCompile = ClassPath.getClassPath(targetFolder, JavaClassPathConstants.MODULE_COMPILE_PATH);
        ClassPath moduleClass = ClassPath.getClassPath(targetFolder, JavaClassPathConstants.MODULE_CLASS_PATH);
        ClassPath source = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
        ClassPath moduleSource = ClassPath.getClassPath(targetFolder, JavaClassPathConstants.MODULE_SOURCE_PATH);
        
        JPAClassPathHelper cpHelper = new JPAClassPathHelper(
                Collections.<ClassPath>singleton(boot), 
                Collections.<ClassPath>singleton(compile), 
                Collections.<ClassPath>singleton(source))
                .setModuleBootPaths(moduleBoot != null ? Collections.<ClassPath>singleton(moduleBoot) : null)
                .setModuleCompilePaths(moduleCompile != null ? Collections.<ClassPath>singleton(moduleCompile) : null)
                .setModuleClassPaths(moduleClass != null ? Collections.<ClassPath>singleton(moduleClass) : null)
                .setModuleSourcePaths(moduleSource != null ? Collections.<ClassPath>singleton(moduleSource) : null);
        

        JavaSource targetSource = JavaSource.create(cpHelper.createClasspathInfo(), entityFo);
        Task<WorkingCopy> task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
            assert typeElement != null;
            ClassTree clazz = workingCopy.getTrees().getTree(typeElement);
            GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
            ClassTree modifiedClazz = genUtils.ensureNoArgConstructor(clazz);
            TreeMaker make = workingCopy.getTreeMaker();
            
            String idFieldName = "id"; // NOI18N
            TypeMirror type = workingCopy.getTreeUtilities().parseType(primaryKeyClassName, typeElement);
            Tree typeTree = make.Type(type);
            
            Set<Modifier> serialVersionUIDModifiers = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            VariableTree serialVersionUID = make.Variable(make.Modifiers(serialVersionUIDModifiers), "serialVersionUID", genUtils.createType("long", typeElement), make.Literal(Long.valueOf("1"))); //NOI18N
            VariableTree idField = make.Variable(genUtils.createModifiers(Modifier.PRIVATE), idFieldName, typeTree, null);
            ModifiersTree idMethodModifiers = genUtils.createModifiers(Modifier.PUBLIC);
            MethodTree idGetter = genUtils.createPropertyGetterMethod(idMethodModifiers, idFieldName, typeTree);
            MethodTree idSetter = genUtils.createPropertySetterMethod(idMethodModifiers, idFieldName, typeTree);
            AnnotationTree idAnnotation = genUtils.createAnnotation(enterprisePrefix + ".persistence.Id"); //NOI18N
            ExpressionTree generationStrategy = genUtils.createAnnotationArgument("strategy", enterprisePrefix + ".persistence.GenerationType", "AUTO"); //NOI18N
            AnnotationTree generatedValueAnnotation = genUtils.createAnnotation(enterprisePrefix + ".persistence.GeneratedValue", Collections.singletonList(generationStrategy)); //NOI18N
            
            if (isAccessProperty){
                idField = genUtils.addAnnotation(idField, idAnnotation);
                idField = genUtils.addAnnotation(idField, generatedValueAnnotation);
            } else {
                idGetter = genUtils.addAnnotation(idGetter, idAnnotation);
                idGetter = genUtils.addAnnotation(idGetter, generatedValueAnnotation);
            }
            
            List<VariableTree> classFields = new ArrayList<>();
            classFields.add(serialVersionUID);
            classFields.add(idField);
            modifiedClazz = genUtils.addClassFields(clazz, classFields);
            modifiedClazz = make.addClassMember(modifiedClazz, idGetter);
            modifiedClazz = make.addClassMember(modifiedClazz, idSetter);
            modifiedClazz = genUtils.addImplementsClause(modifiedClazz, "java.io.Serializable");
            modifiedClazz = genUtils.addAnnotation(modifiedClazz, genUtils.createAnnotation(enterprisePrefix + ".persistence.Entity"));
            
            String entityClassFqn = typeElement.getQualifiedName().toString();
            EntityMethodGenerator methodGenerator = new EntityMethodGenerator(workingCopy, genUtils, typeElement);
            List<VariableTree> fieldsForEquals = Collections.<VariableTree>singletonList(idField);
            modifiedClazz = make.addClassMember(modifiedClazz, methodGenerator.createHashCodeMethod(fieldsForEquals));
            modifiedClazz = make.addClassMember(modifiedClazz, methodGenerator.createEqualsMethod(targetName, fieldsForEquals));
            modifiedClazz = make.addClassMember(modifiedClazz, methodGenerator.createToStringMethod(entityClassFqn, fieldsForEquals));
            workingCopy.rewrite(clazz, modifiedClazz);
        };
        
        if(targetSource == null) {
            //need some logging to investigate possible npes
            Logger.getLogger(EntityWizard.class.getName()).log(Level.WARNING, "Classpaths compile, boot, source: {0},{1},{2}; target folder is valid, entity fo is valid: {3},{4}", new Object[]{compile, boot, source, targetFolder.isValid(), entityFo.isValid()});
            return entityFo;//NO need to get NPE below
        }
        
        targetSource.runModificationTask(task).commit();
        
        return entityFo;
    }
    
    /**
     * A panel which checks whether the target project has a valid server set,
     * otherwise it delegates to the real panel.
     */
    private static final class ValidatingPanel extends DelegatingWizardDescriptorPanel {
        
        public ValidatingPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }
        
        @Override
        public boolean isValid() {
            boolean valid = super.isValid();
            if (!ProviderUtil.isValidServerInstanceOrNone(getProject())) {
                getWizardDescriptor().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_MissingServer")); // NOI18N
            }
            return valid;
        }

    }
}
