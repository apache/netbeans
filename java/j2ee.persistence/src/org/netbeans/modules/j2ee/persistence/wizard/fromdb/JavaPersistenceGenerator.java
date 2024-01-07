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
package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Table;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel.ColumnData;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.EntityMethodGenerator;
import org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper.State;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Generator of Java Persistence API ORM classes from DB.
 *
 * @author Pavel Buzek, Andrei Badea
 */
public class JavaPersistenceGenerator implements PersistenceGenerator {

    // XXX Javadoc for generated code missing in many places - issue 90302
    // XXX createToStringMethod() could be moved to GenerationUtils
    // XXX init() commented out until annotation model is implemented
    // XXX empty lines in generated hashCode() - issue 90186
    // XXX comments are lost in method body passed as string - issue 89873
    // XXX return 0, 1 in generated equals() - issue 90183
    // XXX empty line in generated equals() - issue 90186
    private final Map<String, String> entityName2TableName = new HashMap<>();
    private Project initProject;
    // options (not currently exposed in UI)
    // field vs. property access
    private static boolean fieldAccess = true;
    // named input params for named queries vs. positional params
    private static boolean genNamedParams = true;
    // should generated Entity Classes implement Serializable?
    private static boolean genSerializableEntities = true;
    private Set<FileObject> result;
    private static HashMap<String, VariableTree> variables;
    private static HashMap<String, MethodTree> setters;
    private static HashMap<String, MethodTree> getters;    /**
     * Specifies whether the generated enties should be added to the first
     * persistence unit found in the project. Note that this setting only
     * applies to non-Java EE 5 projects - for Java EE 5 projects the entities
     * are not added to a PU even if this is true.
     */
    private final boolean addToAutoDiscoveredPU;
    /**
     * The persistence unit to which the generated entities should
     * be added.
     */
    private PersistenceUnit persistenceUnit;

    /**
     * Creates a new instance of JavaPersistenceGenerator. Tries to add the
     * generated entities to the first persistence unit found (only in non-Java EE 5 projects).
     */
    public JavaPersistenceGenerator() {
        this.persistenceUnit = null;
        this.addToAutoDiscoveredPU = true;
    }

    /**
     * Creates a new instance of JavaPersistenceGenerator
     *
     * @param persistenceUnit the persistence unit to which the generated entities
     * should be added. Must exist in the project where the entities are generated.
     * Has no effect in Java EE 5 projects - in those
     * the entities are not added to any persistence unit regardless of this. May
     * be null, in which case the generated entities are not added any persistence unit.
     */
    public JavaPersistenceGenerator(PersistenceUnit persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
        this.addToAutoDiscoveredPU = false;
    }

    @Override
    public void generateBeans(final ProgressPanel progressPanel,
            final RelatedCMPHelper helper,
            final FileObject dbSchemaFile,
            final ProgressContributor handle) throws IOException {
        //as it's "public" and may be called from outside, need to check target package exist and create if necessary, see #220073
        if(helper.getLocation() != null && helper.getPackageName() != null) {
            SourceGroups.getFolderForPackage( helper.getLocation(), helper.getPackageName());
        }
        //
        generateBeans(helper.getBeans(), helper.isGenerateFinderMethods(),
                helper.isGenerateJAXBAnnotations(),
                helper.isGenerateValidationConstraints(),
                helper.isFullyQualifiedTableNames(), helper.isRegenTablesAttrs(),
                helper.isUseDefaults(),
                helper.isGenerateMappedSuperclasses(),
                helper.getFetchType(), helper.getCollectionType(),
                handle, progressPanel, helper.getProject());
    }

             // package private for tests
    void generateBeans(EntityClass[] entityClasses, boolean generateNamedQueries,
            boolean generateJAXBAnnotations,
            boolean generateValidationConstraints,
            boolean fullyQualifiedTableNames, boolean regenTablesAttrs,
            FetchType fetchType, CollectionType collectionType,
            ProgressContributor progressContributor, ProgressPanel panel, Project prj) throws IOException {
        
        generateBeans(entityClasses, generateNamedQueries, generateJAXBAnnotations, 
                generateValidationConstraints, fullyQualifiedTableNames, regenTablesAttrs, 
                false, false, fetchType, collectionType, progressContributor, panel, prj);
        
    }
            

    private void generateBeans(EntityClass[] entityClasses, boolean generateNamedQueries,
            boolean generateJAXBAnnotations,
            boolean generateValidationConstraints,
            boolean fullyQualifiedTableNames, boolean regenTablesAttrs,
            boolean useDefaults,
            boolean generateMappedSC,
            FetchType fetchType, CollectionType collectionType,
            ProgressContributor progressContributor, ProgressPanel panel, Project prj) throws IOException {

        int progressMax = entityClasses.length * 3;
        progressContributor.start(progressMax);
        if (prj != null) {
            ContainerClassPathModifier modifier = prj.getLookup().lookup(ContainerClassPathModifier.class);
            if (modifier != null) {
                progressContributor.progress(NbBundle.getMessage(JavaPersistenceGenerator.class, "LBL_Progress_Adding_Classpath"));
                //TODO not project directory, but source root.
                modifier.extendClasspath(prj.getProjectDirectory(),
                        new String[]{
                            ContainerClassPathModifier.API_ANNOTATION,
                            ContainerClassPathModifier.API_PERSISTENCE,
                            ContainerClassPathModifier.API_TRANSACTION
                        });
            }
        }

        result = new Generator(entityClasses, generateNamedQueries,
                generateJAXBAnnotations,
                generateValidationConstraints,
                fullyQualifiedTableNames, regenTablesAttrs,
                useDefaults,
                generateMappedSC,
                fetchType, collectionType,
                progressContributor, panel, this).run();
        addToPersistenceUnit(result);
        progressContributor.progress(progressMax);
        PersistenceUtils.logUsage(JavaPersistenceGenerator.class, "USG_PERSISTENCE_ENTITY_DB_CREATED", new Integer[]{entityClasses.length});
    }

    /**
     * Adds the given entities to out persistence unit found in the project.
     */
    private void addToPersistenceUnit(Set<FileObject> entities) {

        if (entities.isEmpty()) {
            return;
        }

        if (persistenceUnit == null && !addToAutoDiscoveredPU) {
            return;
        }
        FileObject fo = entities.iterator().next();

        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null && !(Util.isSupportedJavaEEVersion(project) && Util.isContainerManaged(project)) && ProviderUtil.getDDFile(project, fo) != null) {
            try {
                PUDataObject pudo = ProviderUtil.getPUDataObject(project, fo, null);
                // no persistence unit was provider, we'll try find one
                if (persistenceUnit == null) {
                    PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
                    //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
                    if (pu.length == 1) {
                        persistenceUnit = pu[0];
                    }
                }
                if (persistenceUnit != null) {
                    ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
                    if (classPathProvider != null) {
                        for (FileObject entity : entities) {
                            String entityFQN = classPathProvider.findClassPath(entity, ClassPath.SOURCE).getResourceName(entity, '.', false);
                            pudo.addClass(persistenceUnit, entityFQN, false);
                        }
                        pudo.save();
                    }
                }

            } catch (InvalidPersistenceXmlException ipx) {
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
            }
        }

    }

    @Override
    public void init(WizardDescriptor wiz) {
        // get the table names for all entities in the project
        initProject = Templates.getProject(wiz);
        final MetadataModelReadHelper<EntityMappingsMetadata, Set<Entity>> readHelper;
        EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(initProject.getProjectDirectory());
        if (entityClassScope == null) {
            return;
        }

        MetadataModel<EntityMappingsMetadata> entityMappingsModel = entityClassScope.getEntityMappingsModel(true);
        readHelper = MetadataModelReadHelper.create(entityMappingsModel, (EntityMappingsMetadata metadata) -> {
            Set<Entity> result1 = new HashSet<>();
            result1.addAll(Arrays.asList(metadata.getRoot().getEntity()));
            return result1;
        });

        readHelper.addChangeListener( (ChangeEvent e) -> {
            if (readHelper.getState() == State.FINISHED) {
                try {
                    processEntities(readHelper.getResult());
                } catch (ExecutionException ex) {
                    Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.FINE, "Failed to get entity classes: ", ex); //NOI18N
                }
            }
        });
        readHelper.start();
    }

    private void processEntities(Set<Entity> entityClasses) {
        for (Entity entity : entityClasses) {
            Table entityTable = entity.getTable();
            if (entityTable != null) {
                entityName2TableName.put(entityTable.getName(), entity.getClass2());
            }
        }
    }

    @Override
    public void uninit() {
        initProject = null;
    }

    @Override
    public String getFQClassName(String tableName) {
        return entityName2TableName.get(tableName);
    }

    @Override
    public String generateEntityName(String name) {
        return name;
    }

    @Override
    public Set<FileObject> createdObjects() {
        return result;
    }
    


    /**
     * Encapsulates the whole entity class generation process.
     */
    private static final class Generator {

        private final ProgressPanel progressPanel;
        private final ProgressContributor progressContributor;
        private final Map<String, EntityClass> beanMap = new HashMap<>();
        private final EntityClass[] entityClasses;
        private final boolean generateNamedQueries;
        private final boolean generateJAXBAnnotations;
        private final boolean generateValidationConstraints;
        private final boolean fullyQualifiedTableNames;
        private final boolean regenTablesAttrs, generateMappedSC;
        private final FetchType fetchType;
        private final CollectionType collectionType;
        private final Set<FileObject> generatedEntityFOs;
        private final Set<FileObject> generatedFOs;
        private final PersistenceGenerator persistenceGen;
        private final boolean useDefaults;
        private final HashMap<String, String> replacedNames;//see #228059
        private final HashMap<String, String> replacedTypeNames;

        public Generator(EntityClass[] entityClasses, boolean generateNamedQueries,
                boolean generateJAXBAnnotations,
                boolean generateValidationConstraints,
                boolean fullyQualifiedTableNames, boolean regenTablesAttrs,
                boolean useDefaults,
                boolean generateMappedSC,
                FetchType fetchType, CollectionType collectionType,
                ProgressContributor progressContributor, ProgressPanel progressPanel,
                PersistenceGenerator persistenceGen) {
            this.entityClasses = entityClasses;
            this.generateNamedQueries = generateNamedQueries;
            this.generateJAXBAnnotations = generateJAXBAnnotations;
            this.generateValidationConstraints = generateValidationConstraints;
            this.fullyQualifiedTableNames = fullyQualifiedTableNames;
            this.useDefaults = useDefaults;
            this.generateMappedSC = generateMappedSC;
            this.regenTablesAttrs = regenTablesAttrs;
            this.fetchType = fetchType;
            this.collectionType = collectionType;
            this.progressContributor = progressContributor;
            this.progressPanel = progressPanel;
            generatedFOs = new HashSet<FileObject>();
            generatedEntityFOs = new HashSet<FileObject>();
            this.persistenceGen = persistenceGen;
            replacedNames = new HashMap<String, String>();
            replacedTypeNames = new HashMap<String, String>();
        }

        public Set<FileObject> run() throws IOException {
            try {
                runImpl();
            } catch (IOException e) {
                Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.INFO, "IOException, remove generated."); //NOI18N 
                for (FileObject generatedFO : generatedFOs) {
                    generatedFO.delete();
                }
                throw e;
            } finally {
            }
            return generatedEntityFOs;
        }

        public void runImpl() throws IOException {

            // first generate empty entity classes -- this is needed as
            // in the field and method generation it will be necessary to resolve
            // their types (e.g. entity A has a field of type Collection<B>, thus
            // while generating entity A we must be able to resolve type B).

            beanMap.clear();
            Set<FileObject> generationPackageFOs = new HashSet<>();
            Set<String> generatedEntityClasses = new HashSet<>();

            for (int i = 0; i < entityClasses.length; i++) {
                final EntityClass entityClass = entityClasses[i];
                String entityClassName = entityClass.getClassName();
                FileObject packageFileObject = entityClass.getPackageFileObject();
                beanMap.put(entityClassName, entityClass);

                String progressMsg = NbBundle.getMessage(JavaPersistenceGenerator.class, "TXT_GeneratingClass", entityClassName);
                progressContributor.progress(progressMsg, i);
                if (progressPanel != null) {
                    progressPanel.setText(progressMsg);
                }

                FileObject entity = packageFileObject.getFileObject(entityClassName, "java");

                //NOI18N
                switch (entityClass.getUpdateType()) {
                    case RECREATE:
                        if (entity == null) {//we hit case when old entity position is different from target package
                            String fqn = persistenceGen.getFQClassName(entityClass.getTableName());
                            int ind = fqn.lastIndexOf('.');
                            String pkg = ind > -1 ? fqn.substring(0, ind) : "";
                            String rel = pkg.replaceAll("\\.", "/");
                            FileObject oldPackage = entityClass.getRootFolder().getFileObject(rel);
                            entity = oldPackage.getFileObject(entityClassName, "java");
                        }
                        // NO PK classes for views
                        if (entityClass.isForTable() && !entityClass.isUsePkField()) {
                            String pkClassName = createPKClassName(entityClassName);
                            FileObject pkFO = packageFileObject.getFileObject(pkClassName, "java");
                            if (pkFO == null) { // NOI18N
                                String fqn = persistenceGen.getFQClassName(entityClass.getTableName());
                                int ind = fqn.lastIndexOf('.');
                                String pkg = ind > -1 ? fqn.substring(0, ind) : "";
                                String rel = pkg.replaceAll("\\.", "/");
                                FileObject oldPackage = entityClass.getRootFolder().getFileObject(rel);
                                pkFO = oldPackage.getFileObject(pkClassName, "java");
                            }
                            if (pkFO != null) {
                                pkFO.delete();
                            }
                        }
                        entity.delete();
                        entity = null;
                        //fall through is expected
                    case NEW: {
                        generatedEntityClasses.add(entityClassName);

                        // XXX Javadoc
                        try {
                            String newName = entityClassName;
                            int count = 1;
                            while (packageFileObject.getFileObject(newName, "java") != null && count<1000) {
                                newName = entityClassName + "_" + count;
                            }
                            entity = GenerationUtils.createClass(packageFileObject, newName, NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_Class"));
                            if (!newName.equals(entityClassName)) {
                                replacedNames.put(entityClassName, newName);
                                replacedTypeNames.put(entityClass.getPackage()+"."+entityClassName , entityClass.getPackage()+"."+newName);
                                generatedEntityClasses.remove(entityClassName);
                                generatedEntityClasses.add(newName);
                            }
                        } catch (RuntimeException ex) {
                            Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.WARNING, "Can't create class {0} from template in package {1} with package fileobject validity {2}.", new Object[]{entityClassName, packageFileObject.getPath(), packageFileObject.isValid()});//NOI18N
                            throw ex;
                        }
                        generatedEntityFOs.add(entity);
                        generatedFOs.add(entity);

                        // NO PK classes for views
                        if (entityClass.isForTable() && !entityClass.isUsePkField()) {
                            String pkClassName = createPKClassName(entityClassName);
                            if (packageFileObject.getFileObject(pkClassName, "java") == null) { // NOI18N
                                FileObject pkClass = GenerationUtils.createClass(packageFileObject, pkClassName, NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_PKClass", pkClassName, entityClassName));
                                generatedFOs.add(pkClass);
                            }
                        }
                    }
                    case UPDATE: {
                        generationPackageFOs.add(packageFileObject);
                    }
                }
            }

            // now generate the fields and methods for each entity class
            // and its primary key class


            Set<ClassPath> bootCPs = getAllClassPaths(generationPackageFOs, ClassPath.BOOT);
            Set<ClassPath> compileCPs = getAllClassPaths(generationPackageFOs, ClassPath.COMPILE);
            Set<ClassPath> sourceCPs = getAllClassPaths(generationPackageFOs, ClassPath.SOURCE);
            JPAClassPathHelper cpHelper = new JPAClassPathHelper(bootCPs, compileCPs, sourceCPs)
                    .setModuleBootPaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_BOOT_PATH))
                    .setModuleCompilePaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_COMPILE_PATH))
                    .setModuleClassPaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_CLASS_PATH))
                    .setModuleSourcePaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_SOURCE_PATH));
            //1st just go through to refresh, in some rare cases entities can't be found/parsed, see #213736
            for (int i = 0; i < entityClasses.length; i++) {
                final EntityClass entityClass = entityClasses[i];
                String entityClassName = getClassName(entityClass);

                if (!generatedEntityClasses.contains(entityClassName) && !UpdateType.UPDATE.equals(entityClass.getUpdateType())) {
                    // this entity class already existed, we didn't create it, so we don't want to touch it except Update type
                    progressContributor.progress(entityClasses.length + i);
                    continue;
                }
                String progressMsg = NbBundle.getMessage(JavaPersistenceGenerator.class, "TXT_GeneratingClass", entityClassName);
                progressContributor.progress(progressMsg, entityClasses.length + i);
                if (progressPanel != null) {
                    progressPanel.setText(progressMsg);
                }
                FileObject entityClassPackageFO = entityClass.getPackageFileObject();
                FileObject entityClassFO0 = entityClassPackageFO.getFileObject(entityClassName, "java"); // NOI18N
                if(entityClassFO0 == null){
                    //refresh parent
                    entityClassPackageFO.refresh(true);
                    entityClassFO0 = entityClassPackageFO.getFileObject(entityClassName, "java");
                    if(entityClassFO0 == null){
                        Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.INFO, "Can't resolve fileobject in package {0} for entity {1}, update type {2}, package valid {3}", new Object[]{entityClassPackageFO.getPath(), entityClassName, entityClass.getUpdateType().getName(), entityClassPackageFO.isValid()});//NOI18N
                    }
                }
                final FileObject entityClassFO = entityClassFO0;
                final FileObject pkClassFO = entityClassPackageFO.getFileObject(createPKClassName(entityClassName), "java"); // NOI18N
                try {
                    JavaSource javaSource = (pkClassFO != null && entityClass.getUpdateType() != UpdateType.UPDATE)
                            ? JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO, pkClassFO)
                            : JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO);
                    javaSource.runModificationTask((WorkingCopy copy) -> {
                        copy.toPhase(Phase.RESOLVED);
                    }).commit();
                } catch (IOException e) {
                    String message = e.getMessage();
                    String newMessage = ((message == null)
                            ? NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_GeneratingClass_NoExceptionMessage", entityClassName)
                            : NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_GeneratingClass", entityClassName, message));
                    throw new IOException(newMessage, e);
                }

            }
            //actual generation loop
            Set<String> lowCaseGeneratedEntityClasses = new HashSet<>();
            for(String tmp:generatedEntityClasses) {
                lowCaseGeneratedEntityClasses.add(tmp.toLowerCase());
            }
            for (int i = 0; i < entityClasses.length; i++) {
                final EntityClass entityClass = entityClasses[i];
                String entityClassName = getClassName(entityClass);

                if (!generatedEntityClasses.contains(entityClassName) && !UpdateType.UPDATE.equals(entityClass.getUpdateType())) {
                    // this entity class already existed, we didn't create it, so we don't want to touch it except Update type
                    progressContributor.progress(entityClasses.length + i);
                    continue;
                }
                String progressMsg = NbBundle.getMessage(JavaPersistenceGenerator.class, "TXT_GeneratingClass", entityClassName);
                progressContributor.progress(progressMsg, 2*entityClasses.length + i);
                if (progressPanel != null) {
                    progressPanel.setText(progressMsg);
                }
                FileObject entityClassPackageFO = entityClass.getPackageFileObject();
                FileObject entityClassFO0 = entityClassPackageFO.getFileObject( entityClassName, "java"); // NOI18N
                
                final FileObject entityClassFO = entityClassFO0;
                final FileObject pkClassFO = (entityClass.isForTable() && !entityClass.isUsePkField()) ? entityClassPackageFO.getFileObject(createPKClassName(entityClassName), "java") : null; // NOI18N
                try {
                    JavaSource javaSource = (pkClassFO != null && entityClass.getUpdateType() != UpdateType.UPDATE)
                            ? JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO, pkClassFO)
                            : JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO);
                    javaSource.runModificationTask( (WorkingCopy copy) -> {
                        if (copy.getFileObject().equals(entityClassFO)) {
                            EntityClassGenerator clsGen = new EntityClassGenerator(copy, entityClass);
                            clsGen.run();
                        } else {
                            if (entityClass.getUpdateType() != UpdateType.UPDATE) {
                                new PKClassGenerator(copy, entityClass).run();
                            } else {
                                Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.INFO, "PK Class update isn't supported"); //NOI18N //TODO: implement update
                            }
                        }
                    }).commit();
                } catch (IOException e) {
                    String message = e.getMessage();
                    String newMessage = ((message == null)
                            ? NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_GeneratingClass_NoExceptionMessage", entityClassName)
                            : NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_GeneratingClass", entityClassName, message));
                    throw new IOException(newMessage, e);
                }

            }
        }

        private static String createPKClassName(String entityClassName) {
            return entityClassName + "PK"; // NOI18N
        }

        private static Set<ClassPath> getAllClassPaths(Set<FileObject> fileObjects, String id) {
            Set<ClassPath> classPaths = new HashSet<>();
            for (FileObject fileObject : fileObjects) {
                ClassPath cp = ClassPath.getClassPath(fileObject, id);
                if (cp != null) {
                    classPaths.add(cp);
                }
            }
            return classPaths;
        }
        
        String getClassName(EntityClass entityClass) {
            return replacedNames.containsKey(entityClass.getClassName()) ? replacedNames.get(entityClass.getClassName()) : entityClass.getClassName();
        }
        /**
         * Encapsulates common logic for generating classes (be it
         * entity or primary key classes). Each instance generates a single
         * class.
         */
        private abstract class ClassGenerator {

            protected final WorkingCopy copy;
            protected final GenerationUtils genUtils;
            // the entity class we are generating
            protected final EntityClass entityClass;
            // the mapping of the entity class to the database
            protected final CMPMappingModel dbMappings;
            // true if a primary key class needs to be generated along with the entity class
            protected final boolean needsPKClass;
            // the simple class name of the primary key class
            protected final String pkClassName;
            // the fully-qualified name of the primary key class
            protected final String pkFQClassName;
            // generated properties
            protected final List<Property> properties = new ArrayList<>();
            // generated methods
            protected final List<MethodTree> methods = new ArrayList<>();
            // generated constructors
            protected final List<MethodTree> constructors = new ArrayList<>();
            // generated fields. does not include fields of properties, just plain fields 
            protected final List<VariableTree> fields = new ArrayList<>();
            // the original class tree of the class we are generating
            protected ClassTree originalClassTree;
            // the modified class tree of the class we are generating
            protected ClassTree newClassTree;
            // the TypeElement corresponding to classTree
            protected TypeElement typeElement;
            // classTree's module
            protected ModuleElement moduleElement;
            // the generating type like New, Update etc
            protected UpdateType updateType;

            private boolean decimalCommentExist = false;

            protected final String enterprisePrefix;

            public ClassGenerator(WorkingCopy copy, EntityClass entityClass) throws IOException {
                this.copy = copy;
                copy.toPhase(Phase.RESOLVED);

                this.entityClass = entityClass;
                this.updateType = entityClass.getUpdateType();
                dbMappings = entityClass.getCMPMapping();
                // NO PK for views
                needsPKClass = entityClass.isForTable() && !entityClass.isUsePkField();

                pkClassName = needsPKClass ? createPKClassName(entityClass.getClassName()) : null;
                pkFQClassName = entityClass.getPackage() + "." + pkClassName; // NOI18N

                typeElement = SourceUtils.getPublicTopLevelElement(copy);
                if (typeElement == null) {
                    throw new IllegalStateException("Cannot find a public top-level class named " + entityClass.getClassName() + // NOI18N
                            " in " + FileUtil.getFileDisplayName(copy.getFileObject())); // NOI18N
                }
                moduleElement = copy.getElements().getModuleOf(typeElement);
                originalClassTree = copy.getTrees().getTree(typeElement);
                assert originalClassTree != null;
                newClassTree = originalClassTree;
                genUtils = GenerationUtils.newInstance(copy);

                String enterprisePrefix;
                ClassPath compileClassPath = copy.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                if (compileClassPath.findResource("jakarta/persistence/EntityManager.class") != null) {
                    enterprisePrefix = "jakarta";
                } else if (compileClassPath.findResource("javax/persistence/EntityManager.class") != null) {
                    enterprisePrefix = "javax";
                } else {
                    enterprisePrefix = "jakarta";
                }
                this.enterprisePrefix = enterprisePrefix;
            }

            protected String createFieldName(String capitalizedFieldName) {
                return createFieldNameImpl(capitalizedFieldName, false);
            }

            protected String createCapitalizedFieldName(String fieldName) {
                return createFieldNameImpl(fieldName, true);
            }

            private String createFieldNameImpl(String fieldName, boolean capitalized) {
                StringBuilder sb = new StringBuilder(fieldName);
                char firstChar = sb.charAt(0);
                sb.setCharAt(0, capitalized ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar));
                return sb.toString();
            }

            /**
             * Creates a property for an entity member, that is, is creates
             * a field, a getter and a setter method.
             */
            protected Property createProperty(EntityMember m) throws IOException {
                boolean isPKMember = m.isPrimaryKey();
                List<AnnotationTree> annotations = new ArrayList<>();

                //add @Id() only if not in an embeddable PK class
                if (isPKMember && !needsPKClass) {
                    annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.Id")); // NOI18N
                    if (m.isAutoIncrement()) {
                        // Can only support strategy=GenerationType.IDENTITY.
                        // See issue 76357 - desc 17
                        List<ExpressionTree> annArguments = new ArrayList<>();
                        annArguments.add(genUtils.createAnnotationArgument("strategy", enterprisePrefix + ".persistence.GenerationType", "IDENTITY")); // NOI18N
                        annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.GeneratedValue", annArguments)); //NOI18N
                    }
                }

                // Add @Basic(optional=false) for not nullable columns
                if (!m.isNullable()) {
                    List<ExpressionTree> basicAnnArguments = new ArrayList<>();
                    basicAnnArguments.add(genUtils.createAnnotationArgument("optional", false)); //NOI18N
                    annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.Basic", basicAnnArguments)); //NOI18N
                    //Add @NotNull constraint
                    if (generateValidationConstraints && !m.isAutoIncrement()) {   //NOI18N
                        annotations.add(genUtils.createAnnotation(enterprisePrefix + ".validation.constraints.NotNull")); //NOI18N
                    }
                }

                boolean isLobType = m.isLobType();
                if (isLobType) {
                    annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.Lob")); // NOI18N
                }

                List<ExpressionTree> columnAnnArguments = new ArrayList<>();
                String memberName = m.getMemberName();
                String memberType = getMemberType(m);

                String columnName = dbMappings.getCMPFieldMapping().get(memberName);
                if(!useDefaults || !memberName.equalsIgnoreCase(columnName)){
                    columnAnnArguments.add(genUtils.createAnnotationArgument("name", columnName)); //NOI18N
                }

                if (regenTablesAttrs && !m.isNullable()) {
                    columnAnnArguments.add(genUtils.createAnnotationArgument("nullable", false)); //NOI18N
                }
                Integer length = m.getLength();
                Integer precision = m.getPrecision();
                Integer scale = m.getScale();

                Comment comment = null;
                if (length != null && isCharacterType(memberType)) {
                    if (generateValidationConstraints) {
                        if (memberName.equalsIgnoreCase("email")) { //NOI18N
                            String regexpString = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\\\."    //NOI18N
                                                   +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"  //NOI18N
                                                   +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";   //NOI18N
                            String commentString = NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_ANNOTATION_EMAIL_COMMENT");
                            comment = Comment.create(Comment.Style.LINE, "@Pattern(regexp=\""+regexpString+"\", " +
                                                                        "message=\""+ NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_INVALID_EMAIL")+"\")" +
                                                                        commentString);
                        } else if (memberName.equalsIgnoreCase("phone") || memberName.equalsIgnoreCase("fax")) { //NOI18N
                            //Pattern for phone in the form (xxx) xxx–xxxx.
                            String regexpString = "^\\\\(?(\\\\d{3})\\\\)?[- ]?(\\\\d{3})[- ]?(\\\\d{4})$";   //NOI18N
                            String commentString = NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_ANNOTATION_PHONE_COMMENT");
                            comment = Comment.create(Comment.Style.LINE,  "@Pattern(regexp=\""+regexpString+"\", " +
                                                                        "message=\""+ NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_INVALID_PHONE")+"\")" +
                                                                        commentString);
                        }
                        List <ExpressionTree> sizeAnnArguments = new ArrayList<>();
                        if (!m.isNullable()) {
                            sizeAnnArguments.add(genUtils.createAnnotationArgument("min", 1));  //NOI18N
                        }
                        sizeAnnArguments.add(genUtils.createAnnotationArgument("max", length)); //NOI18N
                        annotations.add(genUtils.createAnnotation(enterprisePrefix + ".validation.constraints.Size", sizeAnnArguments));   //NOI18N
                    }
                }
                if (isDecimalType(memberType) && !decimalCommentExist) {
                    comment = Comment.create(Comment.Style.LINE, "@Max(value=?)  @Min(value=?)"+NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_ANNOTATION_COMMENT_DECIMAL"));
                    decimalCommentExist = true;
                }

                if (regenTablesAttrs) {
                    if (length != null && isCharacterType(memberType)) {
                        columnAnnArguments.add(genUtils.createAnnotationArgument("length", length)); // NOI18N
                    }
                    if (precision != null && isDecimalType(memberType)) {
                        columnAnnArguments.add(genUtils.createAnnotationArgument("precision", precision)); // NOI18N
                    }
                    if (scale != null && isDecimalType(memberType)) {
                        columnAnnArguments.add(genUtils.createAnnotationArgument("scale", scale)); // NOI18N
                    }
                }

                if(useDefaults && (columnAnnArguments == null || columnAnnArguments.isEmpty())){
                    //skip default
                } else {
                    annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.Column", columnAnnArguments)); //NOI18N
                }

                String temporalType = getMemberTemporalType(m);
                if (temporalType != null) {
                    ExpressionTree temporalAnnValueArgument = genUtils.createAnnotationArgument(null, enterprisePrefix + ".persistence.TemporalType", temporalType); //NOI18N
                    annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.Temporal", Collections.singletonList(temporalAnnValueArgument)));
                }

                return new Property(Modifier.PRIVATE, annotations, comment, memberType, memberName);
            }

            /**
             * Like {@link #createProperty}, but it only creates a variable
             * with no modififers and no annotations. Useful to pass in
             * a parameter list when creating a method or constructor.
             */
            protected VariableTree createVariable(EntityMember m) {
                return genUtils.createVariable(typeElement, m.getMemberName(), getMemberType(m));
            }

            String getMemberType(EntityMember m) {
                String memberType = m.getMemberType();
                if ("java.sql.Date".equals(memberType)) { //NOI18N
                    memberType = "java.util.Date";
                } else if ("java.sql.Time".equals(memberType)) { //NOI18N
                    memberType = "java.util.Date";
                } else if ("java.sql.Timestamp".equals(memberType)) { //NOI18N
                    memberType = "java.util.Date";
                }
                return memberType;
            }

            private boolean isCharacterType(String type) {
                // XXX also need to check for char[] and Character[]
                // (better to use TypeMirror)
                return "java.lang.String".equals(type); // NOI18N
            }

            private boolean isDecimalType(String type) {
                return "java.lang.Double".equals(type) || // NOI18N
                        "java.lang.Float".equals(type) || // NOI18N
                        "java.math.BigDecimal".equals(type); // NOI18N
            }

            private String getMemberTemporalType(EntityMember m) {
                String memberType = m.getMemberType();
                String temporalType = null;
                if ("java.sql.Date".equals(memberType)) { //NOI18N
                    temporalType = "DATE";
                } else if ("java.sql.Time".equals(memberType)) { //NOI18N
                    temporalType = "TIME";
                } else if ("java.sql.Timestamp".equals(memberType)) { //NOI18N
                    temporalType = "TIMESTAMP";
                }
                return temporalType;
            }

            public void run() throws IOException {
                initialize();
                for (Object object : entityClass.getFields()) {
                    generateMember((EntityMember) object);
                }
                afterMembersGenerated();
                for (RelationshipRole roleObject : entityClass.getRoles()) {
                    generateRelationship(roleObject);
                }
                finish();

                // add the generated members
                TreeMaker make = copy.getTreeMaker();
                int position = 0;
                for (VariableTree field : fields) {
                    newClassTree = make.insertClassMember(newClassTree, position, field);
                    position++;
                }
                for (Property property : properties) {
                    if(property.getOldField()!=null){
                        newClassTree = make.removeClassMember(newClassTree, property.getOldField());//just replace
                    }
                    newClassTree = make.insertClassMember(newClassTree, position, property.getField());
                    position++;
                }
                for (MethodTree constructor : constructors) {
                    newClassTree = make.addClassMember(newClassTree, constructor);
                }
                for (Property property : properties) {
                    if(property.getOldGetter()!=null){
                       newClassTree = make.removeClassMember(newClassTree, property.getOldGetter());//just replace for now, need either to replace declaration only or add comment with old body
                    }
                    newClassTree = make.addClassMember(newClassTree, property.getGetter());
                    if(property.getOldSetter()!=null){
                       newClassTree = make.removeClassMember(newClassTree, property.getOldSetter());//just replace for now, need either to replace declaration only or add comment with old body
                    }
                    newClassTree = make.addClassMember(newClassTree, property.getSetter());
                }
                for (MethodTree method : methods) {
                    newClassTree = make.addClassMember(newClassTree, method);
                }
                Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.FINE, "Rewrite entity tree with name: {0}", entityClass.getTableName()); //NOI18N
                Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.FINE, "Rewrite entity tree with annotations: length = {0}, annotations = {1}", new Object[]{newClassTree.getModifiers().getAnnotations().size(),  newClassTree.getModifiers().getAnnotations()}); //NOI18N
                copy.rewrite(originalClassTree, newClassTree);
            }

            /**
             * Called at the beginning of the generation process.
             */
            protected abstract void initialize() throws IOException;

            /**
             * Called for each entity class member.
             */
            protected abstract void generateMember(EntityMember m) throws IOException;

            /**
             * Called after all members have been generated.
             */
            protected abstract void afterMembersGenerated() throws IOException;

            /**
             * Called for each relationship.
             */
            protected abstract void generateRelationship(RelationshipRole role) throws IOException;

            /**
             * Called at the end of the generation process.
             */
            protected abstract void finish() throws IOException;

            /**
             * Encapsulates a generated property, that is, its field, getter
             * and setter method.
             */
            protected final class Property {

                private final VariableTree field;
                private final MethodTree getter;
                private final MethodTree setter;
                private VariableTree existingFieldTree;//used in update/refctoring to point to previosly generated field
                private MethodTree existingGetterTree;//used in update/refctoring to point to previosly generated field
                private MethodTree existingSetterTree;//used in update/refctoring to point to previosly generated field

                public Property(Modifier modifier, List<AnnotationTree> annotations, Comment comment, String type, String name) throws IOException {
                    this(modifier, annotations, comment, genUtils.createType(type, typeElement), name, false);
                }

                public Property(Modifier modifier, List<AnnotationTree> annotations, Comment comment, TypeMirror type, String name) throws IOException {
                    this(modifier, annotations, comment, copy.getTreeMaker().Type(type), name, false);
                }

                private Property(Modifier modifier, List<AnnotationTree> annotations, Comment comment, TypeMirror type, String name, boolean xmlTransient) throws IOException {
                    this(modifier, annotations, comment, copy.getTreeMaker().Type(type), name, xmlTransient);
                }

                private Property(Modifier modifier, List<AnnotationTree> annotations, Comment comment, Tree typeTree, String name, boolean xmlTransient) throws IOException {
                    TreeMaker make = copy.getTreeMaker();
                    field = make.Variable(
                            make.Modifiers(EnumSet.of(modifier), fieldAccess ? annotations : Collections.<AnnotationTree>emptyList()),
                            name,
                            typeTree,
                            null);
                    if (comment != null) {
                        make.addComment(field, comment, true);
                    }
                    if (xmlTransient) {
                        AnnotationTree xmlTransientAn = genUtils.createAnnotation(
                                enterprisePrefix + ".xml.bind.annotation.XmlTransient"); //NOI18N
                        TypeElement jsonIgnore = moduleElement != null
                                ? copy.getElements().getTypeElement(
                                        moduleElement,
                                        "org.codehaus.jackson.annotate.JsonIgnore") // NOI18N
                                : copy.getElements().getTypeElement(
                                        "org.codehaus.jackson.annotate.JsonIgnore"); // NOI18N
                        List<AnnotationTree> annotationTrees;
                        if ( jsonIgnore == null ){
                            annotationTrees = Collections.singletonList(xmlTransientAn);
                        }
                        else {
                            AnnotationTree jsonIgnoreAn = genUtils.createAnnotation(
                                jsonIgnore.getQualifiedName().toString());
                            annotationTrees = new ArrayList<AnnotationTree>(2);
                            annotationTrees.add( xmlTransientAn);
                            annotationTrees.add(jsonIgnoreAn);
                        }
                        getter = genUtils.createPropertyGetterMethod(
                                make.Modifiers(EnumSet.of(Modifier.PUBLIC), annotationTrees ),
                                name,
                                typeTree);
                    } else {
                        getter = genUtils.createPropertyGetterMethod(
                                make.Modifiers(EnumSet.of(Modifier.PUBLIC), fieldAccess ? Collections.<AnnotationTree>emptyList() : annotations),
                                name,
                                typeTree);
                    }

                    setter = genUtils.createPropertySetterMethod(
                            genUtils.createModifiers(Modifier.PUBLIC),
                            name,
                            typeTree);
                }

                public VariableTree getField() {
                    return field;
                }

                public MethodTree getGetter() {
                    return getter;
                }

                public MethodTree getSetter() {
                    return setter;
                }

                public VariableTree getOldField() {
                    return existingFieldTree;
                }

                public MethodTree getOldGetter() {
                    return existingGetterTree;
                }

                public MethodTree getOldSetter() {
                    return existingSetterTree;
                }

                private void setOldField(VariableTree existingTree) {
                    this.existingFieldTree = existingTree;
                }
                private void setOldGetter(MethodTree existingTree) {
                    this.existingGetterTree = existingTree;
                }
                private void setOldSetter(MethodTree existingTree) {
                    this.existingSetterTree = existingTree;
                }
            }
        }

        /**
         * An implementation of ClassGenerator which generates entity classes.
         */
        private final class EntityClassGenerator extends ClassGenerator {

            // the simple name of the entity class
            private final String entityClassName;
            // the fully-qualified name of the entity class
            private final String entityFQClassName;
            // the non-nullable properties (not including the primary key ones)
            private final List<Property> nonNullableProps = new ArrayList<>();
            // the names of the primary key columns
            private final List<String> pkColumnNames = new ArrayList<>();
            // variables correspoding to the fields in the primary key classs (or empty if no primary key class)
            private final List<VariableTree> pkClassVariables = new ArrayList<>();
            // the list of @NamedQuery annotations which will be added to the entity class
            private final List<ExpressionTree> namedQueryAnnotations = new ArrayList<>();
            // the property for the primary key (or the primary key class)
            private Property pkProperty;
            // the prefix or all named queries ("select ... ")
            private String namedQueryPrefix;
            private HashMap<String, Tree> existingColumns = new HashMap<>();
            private String existingEmbeddedId = null;
            private HashMap<String, Tree> existingJoinColumns = new HashMap<>();
            private HashMap<TypeMirror, ArrayList<String>> existingJoinColumnss = new HashMap<>();
            private HashMap<String, ArrayList<String>> existingJoinTables = new HashMap<>();
            private HashMap<String, Tree> existingMappings = new HashMap<>();
            private final boolean useDefaults;
            private final boolean jaxbOrder = false;//need to be enum, like "alphavetical, as in code, as in table, undefined".
            
            public EntityClassGenerator(WorkingCopy copy, EntityClass entityClass) throws IOException {
                super(copy, entityClass);
                entityClassName = getClassName(entityClass);
                assert typeElement.getSimpleName().contentEquals(entityClassName);
                entityFQClassName = entityClass.getPackage() + "." + entityClassName;
                this.useDefaults = entityClass.getUseDefaults();
            }

            @Override
            protected void initialize() throws IOException {
                newClassTree = genUtils.ensureNoArgConstructor(newClassTree);
                if (genSerializableEntities && !UpdateType.UPDATE.equals(updateType)) {
                    newClassTree = genUtils.addImplementsClause(newClassTree, "java.io.Serializable"); // NOI18N
                }
                if (needsPKClass && existingEmbeddedId == null) {
                    String pkFieldName = createFieldName(pkClassName);
                    pkProperty = new Property(
                            Modifier.PROTECTED,
                            Collections.singletonList(genUtils.createAnnotation(enterprisePrefix + ".persistence.EmbeddedId")),
                            null,
                            pkFQClassName,
                            pkFieldName);
                    properties.add(pkProperty);
                }

                if (UpdateType.UPDATE.equals(updateType)) {
                    collectExistingColumns();
                } else {
                    newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation(generateMappedSC ? enterprisePrefix + ".persistence.MappedSuperclass" : enterprisePrefix + ".persistence.Entity")); // NOI18N
                    List<ExpressionTree> tableAnnArgs = new ArrayList<>();
                    if(useDefaults && entityClassName.equalsIgnoreCase(dbMappings.getTableName())){
                        //skip
                    } else if(dbMappings.getTableName() != null) {
                        tableAnnArgs.add(genUtils.createAnnotationArgument("name", dbMappings.getTableName())); // NOI18N
                    }
                    if (fullyQualifiedTableNames) {
                        String schemaName = entityClass.getSchemaName();
                        String catalogName = entityClass.getCatalogName();
                        if (catalogName != null) {
                            tableAnnArgs.add(genUtils.createAnnotationArgument("catalog", catalogName)); // NOI18N
                        }
                        if (schemaName != null) {
                            tableAnnArgs.add(genUtils.createAnnotationArgument("schema", schemaName)); // NOI18N
                        }
                    }

                    // UniqueConstraint annotations for the table
                    if (regenTablesAttrs && entityClass.getUniqueConstraints() != null
                            && !entityClass.getUniqueConstraints().isEmpty()) {
                        List<ExpressionTree> uniqueConstraintAnnotations = new ArrayList<>();
                        for (List<String> constraintCols : entityClass.getUniqueConstraints()) {

                            List<ExpressionTree> colArgs = new ArrayList<>();
                            for (String colName : constraintCols) {
                                colArgs.add(genUtils.createAnnotationArgument(null, colName));
                            }
                            ExpressionTree columnNamesArg = genUtils.createAnnotationArgument("columnNames", colArgs); // NOI18N
                            uniqueConstraintAnnotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.UniqueConstraint",
                                    Collections.singletonList(columnNamesArg))); //NOI18N
                        }

                        tableAnnArgs.add(genUtils.createAnnotationArgument("uniqueConstraints", uniqueConstraintAnnotations)); // NOI18N
                    }

                    if(!useDefaults || !tableAnnArgs.isEmpty()) {
                        newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation(enterprisePrefix + ".persistence.Table", tableAnnArgs));//NOI18N
                    }

                    if (generateJAXBAnnotations) {
                        newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation(enterprisePrefix + ".xml.bind.annotation.XmlRootElement"));//NOI18N
                        /**
                         * see #228733
                         * if(jaxbOrder) {
                            //ArrayList <ExpressionTree> fL = new ArrayList<ExpressionTree> ();
                            //fL.add(genUtils.createAnnotation);
                            //newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.xml.bind.annotation.XmlAccessorType", null);//NOI18N
                            ArrayList ls = new ArrayList();
                            List<ExpressionTree> nms = new ArrayList<ExpressionTree>();
                            nms.add(genUtils.createAnnotationArgument(null,pkProperty.getField().getName()));
                            for (EntityMember mem : entityClass.getFields()) {
                                if(!mem.isPrimaryKey()) {
                                    nms.add(genUtils.createAnnotationArgument(null,mem.getMemberName()));
                                }
                            }
                            for (RelationshipRole mem : entityClass.getRoles()) {
                                nms.add(genUtils.createAnnotationArgument(null,mem.getFieldName()));
                            }
                            ls.add(genUtils.createAnnotationArgument("propOrder", nms));//NOI18N
                            newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.xml.bind.annotation.XmlType",ls));//NOI18N
                        }**/
                    }

                }

                //TODO: javadoc - generate or fake in test mode
                //        b.setCommentDataAuthor(authorOverride);
                //        b.setCommentDataDate(dateOverride);
            }

            private void collectExistingColumns() {
                variables = new HashMap<String, VariableTree>();
                setters = new HashMap<String, MethodTree>();
                getters = new HashMap<String, MethodTree>();
                for (Tree member : originalClassTree.getMembers()) {
                    List<? extends AnnotationTree> annotations = null;
                    Tree memberType = null;
                    if (Kind.VARIABLE == member.getKind()) {
                        VariableTree variable = (VariableTree) member;
                        annotations = variable.getModifiers().getAnnotations();
                        memberType = variable.getType();
                        variables.put(variable.getName().toString(), variable);
                    } else if (Kind.METHOD == member.getKind()) {
                        MethodTree method = (MethodTree) member;
                        annotations = method.getModifiers().getAnnotations();
                        memberType = method.getReturnType();
                        String tmp = method.getName().toString();
                        if(tmp.length()>3 && tmp.startsWith("get")){//NOI18N
                            getters.put((tmp.substring(3,4).toLowerCase() + (tmp.length()>4 ? tmp.substring(4):"")).toUpperCase(), method);
                        } else if(tmp.length()>3 && tmp.startsWith("set")){//NOI18N
                            setters.put((tmp.substring(3,4).toLowerCase() + (tmp.length()>4 ? tmp.substring(4):"")).toUpperCase(), method);
                        } else if(tmp.length()>2 && tmp.startsWith("is")) {//NOI18N
                            getters.put((tmp.substring(2,3).toLowerCase() + (tmp.length()>3 ? tmp.substring(3):"")).toUpperCase(), method);
                        }
                    }
                    if (annotations != null) {
                        for (AnnotationTree annTree : annotations) {
                            if(annTree.getAnnotationType() instanceof IdentifierTree) {
                                Name nm = ((IdentifierTree) annTree.getAnnotationType()).getName();
                                if (nm.contentEquals("Column")) {//NOI18N
                                    for (ExpressionTree exTree : annTree.getArguments()) {
                                        AssignmentTree aTree = (AssignmentTree) exTree;
                                        if (((IdentifierTree) (aTree).getVariable()).getName().contentEquals("name")) {//NOI18N
                                            existingColumns.put((String) ((LiteralTree) aTree.getExpression()).getValue(), member);
                                            break;
                                        }
                                    }
                                } else if (nm.contentEquals("EmbeddedId")) {//NOI18N
                                    TypeMirror tm = this.copy.getTrees().getTypeMirror(TreePath.getPath(copy.getCompilationUnit(), memberType));
                                    existingEmbeddedId = tm.toString();
                                    if (pkProperty != null) {
                                        //currently unsupported update
                                        properties.remove(pkProperty);
                                    }
                                } else if (nm.contentEquals("JoinTable")) {//NOI18N
                                    ArrayList<String> columns = new ArrayList<>();
                                    String tableName = null;
                                    for (ExpressionTree exTree : annTree.getArguments()) {
                                        AssignmentTree aTree = (AssignmentTree) exTree;
                                        Name nm2 = ((IdentifierTree) (aTree).getVariable()).getName();
                                        ExpressionTree value = aTree.getExpression();
                                        if (nm2.contentEquals("joinColumns")) {//NOI18N
                                            List<? extends ExpressionTree> inis;
                                            if(value instanceof NewArrayTree){
                                                NewArrayTree columnsArrayTree = (NewArrayTree) value;
                                                //---TODO: this code is duplicated in this class.
                                                inis = columnsArrayTree.getInitializers();
                                            }  else {
                                                ArrayList<ExpressionTree> one = new ArrayList<>();
                                                one.add(value);
                                                inis = one;
                                            }
                                            for (ExpressionTree eT : inis) {
                                                if (eT instanceof AnnotationTree) {
                                                    AnnotationTree aT = (AnnotationTree) eT;
                                                    Name aN = ((IdentifierTree) aT.getAnnotationType()).getName();
                                                    if (aN.contentEquals("JoinColumn")) {//NOI18N
                                                        for (ExpressionTree expTree : aT.getArguments()) {
                                                            AssignmentTree asTree = (AssignmentTree) expTree;
                                                            if (((IdentifierTree) (asTree).getVariable()).getName().contentEquals("name")) {//NOI18N
                                                                columns.add((String) ((LiteralTree) asTree.getExpression()).getValue());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Collections.sort(columns);
                                            //--- END TODO.
                                        } else if (nm2.contentEquals("name")) {//NOI18N
                                            tableName = ((LiteralTree) value).getValue().toString();
                                        }
                                    }
                                    existingJoinTables.put(tableName, columns);
                                } else if (nm.contentEquals("JoinColumns")) {//NOI18N
                                    TypeMirror tm = this.copy.getTrees().getTypeMirror(TreePath.getPath(copy.getCompilationUnit(), memberType));
                                    ArrayList<String> columns = new ArrayList<>();
                                    for (ExpressionTree exTree : annTree.getArguments()) {
                                        AssignmentTree aTree = (AssignmentTree) exTree;
                                        ExpressionTree value = aTree.getExpression();
                                        if (value instanceof NewArrayTree) {
                                            NewArrayTree arrTree = (NewArrayTree) value;
                                            List<? extends ExpressionTree> inis = arrTree.getInitializers();
                                            for (ExpressionTree eT : inis) {
                                                if (eT instanceof AnnotationTree) {
                                                    AnnotationTree aT = (AnnotationTree) eT;
                                                    Name aN = ((IdentifierTree) aT.getAnnotationType()).getName();
                                                    if (aN.contentEquals("JoinColumn")) {//NOI18N
                                                        for (ExpressionTree expTree : aT.getArguments()) {
                                                            AssignmentTree asTree = (AssignmentTree) expTree;
                                                            if (((IdentifierTree) (asTree).getVariable()).getName().contentEquals("name")) {//NOI18N
                                                                columns.add((String) ((LiteralTree) asTree.getExpression()).getValue());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Collections.sort(columns);

                                            break;
                                        }
                                    }
                                    existingJoinColumnss.put(tm, columns);
                                } else if (nm.contentEquals("JoinColumn")) {//NOI18N
                                    for (ExpressionTree exTree : annTree.getArguments()) {
                                        AssignmentTree aTree = (AssignmentTree) exTree;
                                        if (((IdentifierTree) (aTree).getVariable()).getName().contentEquals("name")) {//NOI18N
                                            existingJoinColumns.put((String) ((LiteralTree) aTree.getExpression()).getValue(), annTree);
                                            break;
                                        }
                                    }
                                } else if (nm.contentEquals("OneToOne") || nm.contentEquals("OneToMany") || nm.contentEquals("ManyToMany")) {//NOI18
                                    //may be relation with mappedTo
                                    for (ExpressionTree expression : annTree.getArguments()) {
                                        if (expression instanceof AssignmentTree) {
                                            AssignmentTree aTree = (AssignmentTree) expression;
                                            if (aTree.getVariable().toString().equals("mappedBy")) {//NOI18N
                                                TypeMirror tm = this.copy.getTrees().getTypeMirror(TreePath.getPath(copy.getCompilationUnit(), memberType));
                                                existingMappings.put(tm.toString(), expression);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
            
            private <T> void wildcardListAdd(List<T> l, T add){
                l.add(add);
            }

            @Override
            protected void generateMember(EntityMember m) throws IOException {
                //skip generating already exist members for UPDATE type
                Tree existingTree = UpdateType.UPDATE.equals(updateType) ? existingColumns.get(m.getColumnName()) : null;//don't need to care if it's not update
                String memberName = m.getMemberName();
                boolean isPKMember = m.isPrimaryKey();
                Property property = null;
                if (isPKMember) {
                    //do not support pk class/pk class member update yet
                    if(existingTree!=null) {
                        return;
                    }
                    //
                    if (needsPKClass) {
                        if (!UpdateType.UPDATE.equals(updateType)) {
                            pkClassVariables.add(createVariable(m));
                        } else {
                            //TODO: support update for pk
                        }
                    } else {
                        pkProperty = property = createProperty(m);
                    }
                    String pkColumnName = dbMappings.getCMPFieldMapping().get(memberName);
                    pkColumnNames.add(pkColumnName);
                } else {
                    //check type
                    property = createProperty(m);
                    if(existingTree !=null){
                        Tree exMemberType = null;
                        if (Kind.VARIABLE == existingTree.getKind()) {
                            VariableTree variable = (VariableTree) existingTree;
                            exMemberType = variable.getType();
                            //need to find accessors
                            property.setOldField(variable);
                            property.setOldGetter(getters.get(m.getColumnName().toUpperCase()));
                            property.setOldSetter(setters.get(m.getColumnName().toUpperCase()));
                        } else if (Kind.METHOD == existingTree.getKind()) {
                            MethodTree method = (MethodTree) existingTree;
                            exMemberType = method.getReturnType();
                            //need to find setter and variable
                            property.setOldGetter(method);
                            property.setOldSetter(setters.get(m.getColumnName().toUpperCase()));
                            property.setOldField(variables.get(m.getColumnName().toUpperCase()));
                        }
                        TypeMirror exTm = this.copy.getTrees().getTypeMirror(TreePath.getPath(copy.getCompilationUnit(), exMemberType));
                        String newType = getMemberType(m);
                        //first if type is the same, just return and keep all as is
                        if(exTm.toString().equals(newType)){
                            return;//nothing is changed
                        } else {
                            //found no refactoring for type change, need manually found variable/setter/getter/constructor, other parts user may need to update himself
                            //do nothing here, shhould be done based on "existing" in properties
                        }
                    }
                    //
                    if (!m.isNullable()) {
                        nonNullableProps.add(property);
                    }
                }
                // we don't create the property only if the current member is
                // part of a primary key, in which case it will be put in the primary key class
                assert (property != null) || (property == null && isPKMember && needsPKClass);
                if (property != null) {
                    properties.add(property);
                }

                // generate equivalent of finder methods - named query annotations
                if (generateNamedQueries && !m.isLobType()) {
                    List<ExpressionTree> namedQueryAnnArguments = new ArrayList<>();
                    namedQueryAnnArguments.add(genUtils.createAnnotationArgument("name", entityClassName + ".findBy" + createCapitalizedFieldName(memberName))); //NOI18N

                    if (namedQueryPrefix == null) {
                        char firstLetter = entityClassName.toLowerCase().charAt(0);
                        namedQueryPrefix = "SELECT " + firstLetter + " FROM " + entityClassName + " " + firstLetter + " WHERE " + firstLetter + "."; // NOI18N
                    }
                    // need a prefix of "pk_field_name." if this is part of a composite pk
                    String memberAccessString = ((needsPKClass && isPKMember) ? (pkProperty.getField().getName().toString() + "." + memberName) : memberName); // NOI18N
                    namedQueryAnnArguments.add(genUtils.createAnnotationArgument(
                            "query", namedQueryPrefix + //NOI18N
                            memberAccessString + ((genNamedParams) ? (" = :" + memberName) : "= ?1"))); //NOI18N
                    namedQueryAnnotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.NamedQuery", namedQueryAnnArguments)); //NOI18N
                }
            }

            protected void addFindAllNamedQueryAnnotation() {
                // Add NamedQuery findAll here
                List<ExpressionTree> namedQueryAnnArguments = new ArrayList<>();
                namedQueryAnnArguments.add(genUtils.createAnnotationArgument("name", entityClassName + ".findAll")); // NOI18N

                char firstLetter = entityClassName.toLowerCase().charAt(0);
                String queryString = "SELECT " + firstLetter + " FROM " + entityClassName + " " + firstLetter; // NOI18N
                namedQueryAnnArguments.add(genUtils.createAnnotationArgument("query", queryString)); // NOI18N

                // Have the findAll as the first NameQuery
                namedQueryAnnotations.add(0, genUtils.createAnnotation(enterprisePrefix + ".persistence.NamedQuery", namedQueryAnnArguments)); //NOI18N
            }

            @Override
            protected void afterMembersGenerated() {
                if (!UpdateType.UPDATE.equals(updateType) && !generateMappedSC) {
                    addFindAllNamedQueryAnnotation();

                    newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation(enterprisePrefix + ".persistence.NamedQueries", // NOI18N
                            Collections.singletonList(genUtils.createAnnotationArgument(null, namedQueryAnnotations))));
                }
            }

            @Override
            protected void generateRelationship(RelationshipRole role) throws IOException {
                String memberName = role.getFieldName();

                if (existingColumns.get(memberName)!=null) {
                    return;
                }

                // XXX getRelationshipFieldType() does not work well when entity classes
                // are not all generated to the same package - fixed in issue 139804
                String typeName = getRelationshipFieldType(role, entityClass.getPackage());
                if(replacedTypeNames.containsKey(typeName)) {
                    typeName = replacedTypeNames.get(typeName);
                }
                TypeElement typeEl = moduleElement != null
                        ? copy.getElements().getTypeElement(moduleElement, typeName)
                        : copy.getElements().getTypeElement(typeName);
                //need some extended logging if null, see issue # 217461
                if(typeEl == null) {
                     Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.WARNING, "Null typeelement for {0}", typeName); //NOI18N
                    //1: need to know if it was generated
                    for(FileObject fo : generatedFOs){
                        Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.WARNING, "Next FileObject was generated: {0}, valid: {1}, can read: {2}, locked: {3}", new String[]{fo!=null?fo.getName():"null", (fo!=null?fo.isValid():"null")+"", (fo!=null?fo.canRead():"null")+"", (fo!=null?fo.isLocked():"null")+""}); //NOI18N
                    }
                    //2: 
                     Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.WARNING, "Member name {0}", memberName); //NOI18N
                     Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.WARNING, "Table name {0}", entityClass.getTableName()); //NOI18N
                     Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.WARNING, "Update type {0}", entityClass.getUpdateType()); //NOI18N
                }
                //
                assert typeEl != null : "null TypeElement for \"" + typeName + "\"";
                TypeMirror fieldType = typeEl.asType();
                if (role.isToMany()) {
                    // Use the collection type the user wants
                    TypeElement collectionTypeElem = moduleElement != null
                            ? copy.getElements().getTypeElement(moduleElement, collectionType.className())
                            : copy.getElements().getTypeElement(collectionType.className());
                    fieldType = copy.getTypes().getDeclaredType(collectionTypeElem, fieldType);
                }

                List<AnnotationTree> annotations = new ArrayList<>();
                List<ExpressionTree> annArguments = new ArrayList<>();
                if (role.isCascade()) {
                    annArguments.add(genUtils.createAnnotationArgument("cascade", enterprisePrefix + ".persistence.CascadeType", "ALL")); // NOI18N
                }
                if (role.equals(role.getParent().getRoleB())) { // Role B
                    String fName = role.getParent().getRoleA().getFieldName();
                    String fieldTypeStr = fieldType.toString();
                    AssignmentTree aTree = (AssignmentTree) existingMappings.get(fieldTypeStr);
                    if(aTree == null && role.isToMany()){//try to find other possible collection types
                        String inType = fieldTypeStr.substring(collectionType.className().length());
                        for(CollectionType ct:CollectionType.values()){
                            aTree = (AssignmentTree) existingMappings.get(ct.className()+inType);
                            if(aTree != null) {
                                break;
                            }
                        }                        
                    }
                    if (aTree != null) {
                        ExpressionTree expr = aTree.getExpression();
                        if (expr instanceof LiteralTree) {
                            LiteralTree literal = (LiteralTree) expr;
                            String value = literal.getValue().toString();
                            if (value != null && value.length() > 0) {
                                return;
                            }
                        }
                    }
                    annArguments.add(genUtils.createAnnotationArgument("mappedBy", fName)); // NOI18N
                } else {  // Role A
                    if (role.isMany() && role.isToMany()) { // ManyToMany
                        List<ExpressionTree> joinTableAnnArguments = new ArrayList<>();
                        String jTN =  dbMappings.getJoinTableMapping().get(role.getFieldName());

                        if(existingJoinTables.get(jTN) != null){
                            //update isn't supported yet, just return if same join table already exists
                            return;
                        }

                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("name", jTN)); //NOI18N

                        CMPMappingModel.JoinTableColumnMapping joinColumnMap = dbMappings.getJoinTableColumnMppings().get(role.getFieldName());

                        List<AnnotationTree> joinCols = new ArrayList<>();
                        ColumnData[] columns = joinColumnMap.getColumns();
                        ColumnData[] refColumns = joinColumnMap.getReferencedColumns();
                        for (int colIndex = 0; colIndex < columns.length; colIndex++) {
                            List<ExpressionTree> attrs = new ArrayList<>();
                            attrs.add(genUtils.createAnnotationArgument("name", columns[colIndex].getColumnName())); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", refColumns[colIndex].getColumnName())); //NOI18N
                            if (regenTablesAttrs && !columns[colIndex].isNullable()) {
                                attrs.add(genUtils.createAnnotationArgument("nullable", false)); //NOI18N
                            }
                            joinCols.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.JoinColumn", attrs)); //NOI18N
                        }
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("joinColumns", joinCols)); // NOI18N

                        List<AnnotationTree> inverseCols = new ArrayList<>();
                        ColumnData[] invColumns = joinColumnMap.getInverseColumns();
                        ColumnData[] refInvColumns = joinColumnMap.getReferencedInverseColumns();
                        for (int colIndex = 0; colIndex < invColumns.length; colIndex++) {
                            List<ExpressionTree> attrs = new ArrayList<>();
                            attrs.add(genUtils.createAnnotationArgument("name", invColumns[colIndex].getColumnName())); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", refInvColumns[colIndex].getColumnName())); //NOI18N
                            if (regenTablesAttrs && !invColumns[colIndex].isNullable()) {
                                attrs.add(genUtils.createAnnotationArgument("nullable", false)); //NOI18N
                            }
                            inverseCols.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.JoinColumn", attrs)); // NOI18N
                        }
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("inverseJoinColumns", inverseCols)); // NOI18N

                        annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.JoinTable", joinTableAnnArguments)); // NOI18N
                    } else { // ManyToOne, OneToMany, OneToOne
                        ColumnData[] columns = dbMappings.getCmrFieldMapping().get(role.getFieldName());
                        CMPMappingModel relatedMappings = beanMap.get(role.getParent().getRoleB().getEntityName()).getCMPMapping();
                        ColumnData[] invColumns = relatedMappings.getCmrFieldMapping().get(role.getParent().getRoleB().getFieldName());
                        if (columns.length == 1) {
                            if (existingJoinColumns.get(columns[0].getColumnName()) != null) {
                                return;
                            }
                            List<ExpressionTree> attrs = new ArrayList<>();
                            attrs.add(genUtils.createAnnotationArgument("name", columns[0].getColumnName())); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", invColumns[0].getColumnName())); //NOI18N
                            if (regenTablesAttrs && !columns[0].isNullable()) {
                                attrs.add(genUtils.createAnnotationArgument("nullable", false));
                            }
                            makeReadOnlyIfNecessary(pkColumnNames, columns[0].getColumnName(), attrs);
                            annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.JoinColumn", attrs)); //NOI18N
                        } else {
                            if(existingJoinColumnss.get(fieldType) != null){
                                return; //currently do not overgenerate and do not update etc existing JoinColumns
                            }

                            List<AnnotationTree> joinCols = new ArrayList<>();
                            for (int colIndex = 0; colIndex < columns.length; colIndex++) {
                                List<ExpressionTree> attrs = new ArrayList<>();
                                attrs.add(genUtils.createAnnotationArgument("name", columns[colIndex].getColumnName())); //NOI18N
                                attrs.add(genUtils.createAnnotationArgument("referencedColumnName", invColumns[colIndex].getColumnName())); //NOI18N
                                if (regenTablesAttrs && !columns[colIndex].isNullable()) {
                                    attrs.add(genUtils.createAnnotationArgument("nullable", false));
                                }
                                makeReadOnlyIfNecessary(pkColumnNames, columns[colIndex].getColumnName(), attrs);
                                joinCols.add(genUtils.createAnnotation(enterprisePrefix + ".persistence.JoinColumn", attrs)); // NOI18N
                            }
                            ExpressionTree joinColumnsNameAttrValue = genUtils.createAnnotationArgument(null, joinCols);
                            AnnotationTree joinColumnsAnnotation = genUtils.createAnnotation(enterprisePrefix + ".persistence.JoinColumns", Collections.singletonList(joinColumnsNameAttrValue)); //NOI18N
                            annotations.add(joinColumnsAnnotation);
                        }
                    }
                }
                String relationAnn;
                if (role.isMany() && role.isToMany()) {
                    relationAnn = "ManyToMany"; //NOI18N
                } else if (role.isMany()) {
                    relationAnn = "ManyToOne"; //NOI18N
                } else if (role.isToMany()) {
                    relationAnn = "OneToMany"; //NOI18N
                } else {
                    relationAnn = "OneToOne";  //NOI18N
                }

                if (!role.isToMany()) { // meaning ManyToOne or OneToOne
                    // Add optional=false on @ManyToOne or the owning side of @OneToOne
                    // if the relationship is non-optional (or non-nuallable in other words)  
                    if (!role.isOptional() && (role.isMany() || role.equals(role.getParent().getRoleA()))) {
                        annArguments.add(genUtils.createAnnotationArgument("optional", false)); // NOI18N
                    }
                }

                //FetchType
                if (fetchType.equals(FetchType.LAZY)) {
                    annArguments.add(genUtils.createAnnotationArgument("fetch", enterprisePrefix + ".persistence.FetchType", "LAZY")); // NOI18N
                } else if (fetchType.equals(FetchType.EAGER)) {
                    annArguments.add(genUtils.createAnnotationArgument("fetch", enterprisePrefix + ".persistence.FetchType", "EAGER")); // NOI18N
                }

                // Create the relationship annotation 
                annotations.add(genUtils.createAnnotation(enterprisePrefix + ".persistence." + relationAnn, annArguments)); // NOI18N

                if (generateJAXBAnnotations && role.isToMany()) {
                    properties.add(new Property(Modifier.PRIVATE, annotations, null, fieldType, memberName, true));
                } else {
                    properties.add(new Property(Modifier.PRIVATE, annotations, null, fieldType, memberName));
                }
            }

            /**
             * Creates the <code>serialVersionUID</code> field with
             * the initial value of <code>1L</code>.
             * 
             * @return the created field.
             */
            private VariableTree createSerialVersionUID() {
                Set<Modifier> serialVersionUIDModifiers = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

                TreeMaker make = copy.getTreeMaker();
                VariableTree serialVersionUID = make.Variable(make.Modifiers(serialVersionUIDModifiers),
                        "serialVersionUID", genUtils.createType("long", typeElement), make.Literal(Long.valueOf("1"))); //NOI18N

                return serialVersionUID;
            }

            @Override
            protected void finish() {

                if (needsPKClass && UpdateType.UPDATE.equals(updateType)) {
                    return;//do not support yet updates with pk class
                }
                if (pkProperty != null) {
                    // create a constructor which takes the primary key field as argument    
                    VariableTree pkFieldParam = genUtils.removeModifiers(pkProperty.getField());
                    List<VariableTree> pkFieldParams = Collections.singletonList(pkFieldParam);
                    constructors.add(genUtils.createAssignmentConstructor(genUtils.createModifiers(Modifier.PUBLIC), entityClassName, pkFieldParams));

                    // if different than pk fields constructor, add constructor
                    // which takes all non-nullable non-relationship fields as args
                    if (!nonNullableProps.isEmpty()) {
                        List<VariableTree> nonNullableParams = new ArrayList<>(nonNullableProps.size() + 1);
                        nonNullableParams.add(pkFieldParam);
                        for (Property property : nonNullableProps) {
                            nonNullableParams.add(genUtils.removeModifiers(property.getField()));
                        }
                        constructors.add(genUtils.createAssignmentConstructor(genUtils.createModifiers(Modifier.PUBLIC), entityClassName, nonNullableParams));
                    }

                    // create a constructor which takes the fields of the primary key class as arguments
                    if (!pkClassVariables.isEmpty()) {
                        StringBuilder body = new StringBuilder(30 + 30 * pkClassVariables.size());
                        body.append("{"); // NOI18N
                        body.append("this." + pkProperty.getField().getName() + " = new " + pkClassName + "("); // NOI18N
                        for (Iterator<VariableTree> i = pkClassVariables.iterator(); i.hasNext();) {
                            body.append(i.next().getName());
                            body.append(i.hasNext() ? ", " : ");"); // NOI18N
                        }
                        body.append("}"); // NOI18N
                        TreeMaker make = copy.getTreeMaker();
                        constructors.add(make.Constructor(
                                make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.<AnnotationTree>emptyList()),
                                Collections.<TypeParameterTree>emptyList(),
                                pkClassVariables,
                                Collections.<ExpressionTree>emptyList(),
                                body.toString()));
                    }


                    // add equals and hashCode methods
                    EntityMethodGenerator methodGenerator = new EntityMethodGenerator(copy, genUtils, typeElement);
                    methods.add(methodGenerator.createHashCodeMethod(pkFieldParams));
                    methods.add(methodGenerator.createEqualsMethod(entityClassName, pkFieldParams));
                    methods.add(methodGenerator.createToStringMethod(entityFQClassName, pkFieldParams));
                }

                // add the serialVersionUID field
                if (!UpdateType.UPDATE.equals(updateType)) {
                    fields.add(createSerialVersionUID());
                }
            }

            private String getRelationshipFieldType(RelationshipRole role, String pkg) {
                RelationshipRole rA = role.getParent().getRoleA();
                RelationshipRole rB = role.getParent().getRoleB();
                RelationshipRole otherRole = role.equals(rA) ? rB : rA;

                // To address issue 139804
                // First, check if the entity package name is set in the role.
                // If yes, then that's the package
                // If no, then default to the passed in pkg
                if (role.getEntityPkgName() != null) {
                    return otherRole.getEntityPkgName() + "." + otherRole.getEntityName(); // NOI18N
                } else {
                    return pkg.length() == 0 ? otherRole.getEntityName() : pkg + "." + otherRole.getEntityName(); // NOI18N
                }
            }

            private void makeReadOnlyIfNecessary(List<String> pkColumnNames, String testColumnName, List<ExpressionTree> attrs) {
                // if the join column is a pk column, add insertable = false, updatable = false
                if (pkColumnNames.contains(testColumnName)) {
                    attrs.add(genUtils.createAnnotationArgument("insertable", false)); //NOI18N
                    attrs.add(genUtils.createAnnotationArgument("updatable", false)); //NOI18N
                }
            }
        }

        /**
         * An implementation of ClassGenerator which generates primary key
         * classes.
         */
        private final class PKClassGenerator extends ClassGenerator {

            public PKClassGenerator(WorkingCopy copy, EntityClass entityClass) throws IOException {
                super(copy, entityClass);
            }

            @Override
            protected void initialize() throws IOException {
                newClassTree = genUtils.ensureNoArgConstructor(newClassTree);
                // primary key class must be serializable and @Embeddable
                newClassTree = genUtils.addImplementsClause(newClassTree, "java.io.Serializable"); //NOI18N
                newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation(enterprisePrefix + ".persistence.Embeddable")); // NOI18N
            }

            @Override
            protected void generateMember(EntityMember m) throws IOException {
                if (!m.isPrimaryKey()) {
                    return;
                }
                Property property = createProperty(m);
                properties.add(property);
            }

            @Override
            protected void afterMembersGenerated() {
            }

            @Override
            protected void generateRelationship(RelationshipRole relationship) {
            }

            @Override
            protected void finish() {
                // add a constructor which takes the fields of the primary key class as arguments
                List<VariableTree> parameters = new ArrayList<>(properties.size());
                for (Property property : properties) {
                    parameters.add(genUtils.removeModifiers(property.getField()));
                }
                constructors.add(genUtils.createAssignmentConstructor(genUtils.createModifiers(Modifier.PUBLIC), pkClassName, parameters));

                // add equals and hashCode methods
                EntityMethodGenerator methodGenerator = new EntityMethodGenerator(copy, genUtils, typeElement);
                methods.add(methodGenerator.createHashCodeMethod(parameters));
                methods.add(methodGenerator.createEqualsMethod(pkClassName, parameters));
                methods.add(methodGenerator.createToStringMethod(pkFQClassName, parameters));
            }
        }
    }
}
