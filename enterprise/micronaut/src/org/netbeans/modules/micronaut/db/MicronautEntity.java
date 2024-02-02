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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.api.entity.generator.EntitiesFromDBGenerator;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation;
import org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole;
import org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.JavaPersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGeneratorProvider;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPWizard;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class MicronautEntity extends RelatedCMPWizard {

    private static final String TYPE_MICRONAUT = "micronaut"; // NOI18N

    public static MicronautEntity create() {
        return new MicronautEntity(TYPE_MICRONAUT);
    }

    @NbBundle.Messages({
        "MSG_NoDbConn=No database connection found",
        "MSG_NoDdSupport=No database support libraries found for {0}",
        "MSG_NoProject=No project found for {0}",
        "MSG_NoSourceGroup=No source group found for {0}",
        "MSG_SelectTables=Select Database Tables",
        "MSG_NoDbTables=No database table found for {0}",
        "MSG_CreatingEntities=Entity Classes from Database",
        "MSG_ReadingTables=Reading Table Definitions",
        "MSG_CreatingClasses=Creating Classes"
    })
    public static CreateFromTemplateHandler handler() {
        return new CreateFromTemplateHandler() {
            @Override
            protected boolean accept(CreateDescriptor desc) {
                return true;
            }

            @Override
            protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
                ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_CreatingEntities());
                handle.start();
                try {
                    handle.progress(Bundle.MSG_ReadingTables());
                    FileObject folder = desc.getTarget();
                    Project project = FileOwnerQuery.getOwner(folder);
                    if (project == null) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_NoProject(folder.getPath()), NotifyDescriptor.ERROR_MESSAGE));
                        return Collections.emptyList();
                    }
                    SourceGroup sourceGroup = SourceGroups.getFolderSourceGroup(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA), folder);
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
                    ConnectionManager.getDefault().connect(connection);
                    Connection conn = connection.getJDBCConnection();
                    ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), conn.getSchema(), "%", new String[]{"TABLE", "VIEW"}); //NOI18N
                    List<NotifyDescriptor.QuickPick.Item> dbItems = new ArrayList<>();
                    while (rs.next()) {
                        dbItems.add(new NotifyDescriptor.QuickPick.Item(rs.getString("TABLE_NAME"), null)); //NOI18N
                    }
                    if (dbItems.isEmpty()) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_NoDbTables(connection.getDisplayName()), NotifyDescriptor.ERROR_MESSAGE));
                        return Collections.emptyList();
                    }
                    NotifyDescriptor.QuickPick qp = new NotifyDescriptor.QuickPick(Bundle.MSG_SelectTables(), Bundle.MSG_SelectTables(), dbItems, true);
                    if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(qp)) {
                        List<String> selectedItems = qp.getItems().stream().filter(item -> item.isSelected()).map(item -> item.getLabel()).collect(Collectors.toList());
                        handle.progress(Bundle.MSG_CreatingClasses());
                        EntitiesFromDBGenerator generator = new EntitiesFromDBGenerator(selectedItems, false, false, false,
                                EntityRelation.FetchType.DEFAULT, EntityRelation.CollectionType.COLLECTION,
                                SourceGroups.getPackageForFolder(sourceGroup, folder), sourceGroup, connection, project, null, new Generator());
                        ProgressContributor pc = BasicAggregateProgressFactory.createProgressContributor("entity"); //NOI18N\
                        List<FileObject> generated = new ArrayList<>();
                        for (FileObject fo : generator.generate(pc)) {
                            if (fo != null) {
                                generated.add(fo);
                            }
                        }
                        return generated;
                    }
                } catch (IOException | SQLException | DatabaseException ex) {
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                } finally {
                    handle.finish();
                }
                return Collections.emptyList();
            }
        };
    }

    private MicronautEntity(String type) {
        super(type);
    }

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        String wizardTitle = NbBundle.getMessage(MicronautEntity.class, "Templates/Micronaut/Entity");
            wiz.putProperty("NewFileWizard_Title", wizardTitle); // NOI18N
            return super.instantiate(wiz);
        }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGeneratorProvider.class)
    public static class GeneratorProvider implements PersistenceGeneratorProvider {

        @Override
        public String getGeneratorType() {
            return TYPE_MICRONAUT;
        }

        @Override
        public PersistenceGenerator createGenerator() {
            return new Generator();
        }
    }

    private static class Generator extends JavaPersistenceGenerator {

        private final Set<FileObject> generatedEntityFOs = new HashSet<>();
        private final Set<FileObject> generatedFOs = new HashSet<>();
        private final Map<String, String> replacedNames = new HashMap<>();
        private final Map<String, String> replacedTypeNames = new HashMap<>();
        private final Map<String, EntityClass> beanMap = new HashMap<>();

        @Override
        public Set<FileObject> createdObjects() {
            return generatedEntityFOs;
        }

        @Override
        public void generateBeans(ProgressPanel progressPanel, RelatedCMPHelper helper, FileObject dbschemaFile, ProgressContributor progressContributor) throws IOException {
            try {
                doGenerateBeans(progressPanel, helper, progressContributor);
            } catch (IOException e) {
                Logger.getLogger(Generator.class.getName()).log(Level.INFO, "IOException, remove generated."); //NOI18N
                for (FileObject generatedFO : generatedFOs) {
                    try {
                        generatedFO.delete();
                    } catch (IOException ioe) {}
                }
                throw e;
            }
        }

        private void doGenerateBeans(ProgressPanel progressPanel, RelatedCMPHelper helper, ProgressContributor progressContributor) throws IOException {
            if (helper.getLocation() != null && helper.getPackageName() != null) {
                SourceGroups.getFolderForPackage(helper.getLocation(), helper.getPackageName());
            }

            final boolean jpaSupported = Utils.isJPASupported(helper.getLocation());
            final boolean beanValidationSupported = isBeanValidationSupported(helper.getLocation());

            EntityClass[] entityClasses = helper.getBeans();
            int progressMax = entityClasses.length * 2;
            progressContributor.start(progressMax);

            beanMap.clear();
            Set<FileObject> generationPackageFOs = new HashSet<>();
            Set<String> generatedEntityClasses = new HashSet<>();

            for (int i = 0; i < entityClasses.length; i++) {
                final EntityClass entityClass = entityClasses[i];
                String entityClassName = entityClass.getClassName();
                FileObject packageFileObject = entityClass.getPackageFileObject();
                beanMap.put(entityClassName, entityClass);

                String progressMsg = NbBundle.getMessage(Generator.class, "TXT_GeneratingClass", entityClassName);
                progressContributor.progress(progressMsg, i);
                if (progressPanel != null) {
                    progressPanel.setText(progressMsg);
                }

                FileObject entity = packageFileObject.getFileObject(entityClassName, "java"); //NOI18N
                switch (entityClass.getUpdateType()) {
                    case RECREATE:
                    case UPDATE:
                        if (entity == null) {//we hit case when old entity position is different from target package
                            String fqn = this.getFQClassName(entityClass.getTableName());
                            int ind = fqn.lastIndexOf(".");
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
                                String fqn = this.getFQClassName(entityClass.getTableName());
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
                    case NEW:
                        generatedEntityClasses.add(entityClassName);
                        try {
                            String pkMemberName = getPkMemberName(entityClass);
                            String newName = pkMemberName != null ? Character.toUpperCase(pkMemberName.charAt(0)) + pkMemberName.substring(1) : entityClassName;
                            int count = 1;
                            while (packageFileObject.getFileObject(newName, "java") != null && count < 1000) {
                                newName = entityClassName + "_" + count;
                                count++;
                            }
                            entity = GenerationUtils.createClass(packageFileObject, newName, NbBundle.getMessage(Generator.class, "MSG_Entity_Class", newName));
                            if (!newName.equals(entityClassName)) {
                                replacedNames.put(entityClassName, newName);
                                String pkg = entityClass.getPackage();
                                String entityClassFQN = pkg == null || pkg.isEmpty() ? entityClassName : pkg + "." + entityClassName;
                                String newFQN = pkg == null || pkg.isEmpty() ? newName : pkg + "." + newName;
                                replacedTypeNames.put(entityClassFQN, newFQN);
                                generatedEntityClasses.remove(entityClassName);
                                generatedEntityClasses.add(newName);
                            }
                        } catch (RuntimeException ex) {
                            Logger.getLogger(Generator.class.getName()).log(Level.WARNING, "Can't create class {0} from template in package {1} with package fileobject validity {2}.", new Object[]{entityClassName, packageFileObject.getPath(), packageFileObject.isValid()});//NOI18N
                            throw ex;
                        }
                        generatedEntityFOs.add(entity);
                        generatedFOs.add(entity);
                        generationPackageFOs.add(packageFileObject);
                        // NO PK classes for views
                        if (entityClass.isForTable() && !entityClass.isUsePkField()) {
                            String pkClassName = createPKClassName(entityClassName);
                            if (packageFileObject.getFileObject(pkClassName, "java") == null) { // NOI18N
                                FileObject pkClass = GenerationUtils.createClass(packageFileObject, pkClassName, NbBundle.getMessage(Generator.class, "MSG_PK_Class", pkClassName, entityClassName));
                                generatedFOs.add(pkClass);
                            }
                        }
                }
            }

            Set<ClassPath> bootCPs = getAllClassPaths(generationPackageFOs, ClassPath.BOOT);
            Set<ClassPath> compileCPs = getAllClassPaths(generationPackageFOs, ClassPath.COMPILE);
            Set<ClassPath> sourceCPs = getAllClassPaths(generationPackageFOs, ClassPath.SOURCE);
            JPAClassPathHelper cpHelper = new JPAClassPathHelper(bootCPs, compileCPs, sourceCPs)
                    .setModuleBootPaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_BOOT_PATH))
                    .setModuleCompilePaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_COMPILE_PATH))
                    .setModuleClassPaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_CLASS_PATH))
                    .setModuleSourcePaths(getAllClassPaths(generationPackageFOs, JavaClassPathConstants.MODULE_SOURCE_PATH));

            for (int i = 0; i < entityClasses.length; i++) {
                final EntityClass entityClass = entityClasses[i];
                String entityClassName = getClassName(entityClass);

                if (!generatedEntityClasses.contains(entityClassName) && !UpdateType.UPDATE.equals(entityClass.getUpdateType())) {
                    // this entity class already existed, we didn't create it, so we don't want to touch it except Update type
                    progressContributor.progress(entityClasses.length + i);
                    continue;
                }
                String progressMsg = NbBundle.getMessage(Generator.class, "TXT_GeneratingClass", entityClassName);
                progressContributor.progress(progressMsg, entityClasses.length + i);
                if (progressPanel != null) {
                    progressPanel.setText(progressMsg);
                }
                final FileObject entityClassPackageFO = entityClass.getPackageFileObject();
                final FileObject entityClassFO = entityClassPackageFO.getFileObject( entityClassName, "java"); // NOI18N
                final FileObject pkClassFO = (entityClass.isForTable() && !entityClass.isUsePkField()) ? entityClassPackageFO.getFileObject(createPKClassName(entityClassName), "java") : null; // NOI18N
                try {
                    JavaSource javaSource = (pkClassFO != null && entityClass.getUpdateType() != UpdateType.UPDATE)
                            ? JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO, pkClassFO)
                            : JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO);
                    javaSource.runModificationTask(copy -> {
                        if (copy.getFileObject().equals(entityClassFO)) {
                            new EntityClassGenerator(copy, entityClass, jpaSupported, beanValidationSupported).run();
                        } else {
                            if (entityClass.getUpdateType() != UpdateType.UPDATE) {
                                new PKClassGenerator(copy, entityClass, jpaSupported, beanValidationSupported).run();
                            } else {
                                Logger.getLogger(Generator.class.getName()).log(Level.INFO, "PK Class update isn't supported"); //NOI18N
                            }
                        }
                    }).commit();
                } catch (IOException e) {
                    String message = e.getMessage();
                    String newMessage = ((message == null)
                            ? NbBundle.getMessage(Generator.class, "ERR_GeneratingClass_NoExceptionMessage", entityClassName)
                            : NbBundle.getMessage(Generator.class, "ERR_GeneratingClass", entityClassName, message));
                    throw new IOException(newMessage, e);
                }
            }

            progressContributor.progress(progressMax);
            PersistenceUtils.logUsage(Generator.class, "USG_PERSISTENCE_ENTITY_DB_CREATED", new Integer[]{entityClasses.length});
        }

        private static String getPkMemberName(EntityClass entityClass) {
            if (entityClass != null) {
                List<EntityMember> pks = entityClass.getFields().stream().filter(f -> f.isPrimaryKey()).collect(Collectors.toList());
                if (pks.size() == 1) {
                    String memberName = pks.get(0).getMemberName();
                    if (memberName.length() > 2 && memberName.endsWith("Id")) {
                        return memberName.substring(0, memberName.length() - 2);
                    }
                }
            }
            return null;
        }

        private static String createPKClassName(String entityClassName) {
            return entityClassName + "PK"; // NOI18N
        }

        private String getClassName(EntityClass entityClass) {
            return replacedNames.containsKey(entityClass.getClassName()) ? replacedNames.get(entityClass.getClassName()) : entityClass.getClassName();
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

        private static boolean isBeanValidationSupported(SourceGroup sg) {
            if (sg == null) {
                return false;
            }
            ClassPath compile = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE);
            if (compile == null) {
                return false;
            }
            final String notNullAnnotation = "javax.validation.constraints.NotNull"; //NOI18N
            return compile.findResource(notNullAnnotation.replace('.', '/') + ".class") != null; //NOI18N
        }

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

            protected final boolean generateJPA;
            protected final boolean generateValidationConstraints;
            protected final boolean generateSerdeable;

            private ClassGenerator(WorkingCopy copy, EntityClass entityClass, boolean jpaSupported, boolean beanValidationSupported) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                this.copy = copy;
                this.entityClass = entityClass;
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
                generateJPA = jpaSupported;
                generateValidationConstraints = beanValidationSupported;
                generateSerdeable = copy.getElements().getTypeElement("io.micronaut.serde.annotation.Serdeable") != null;
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
                    annotations.add(genUtils.createAnnotation(generateJPA ? "javax.persistence.Id" : "io.micronaut.data.annotation.Id")); // NOI18N
                    if (m.isAutoIncrement()) {
                        annotations.add(genUtils.createAnnotation(generateJPA ? "javax.persistence.GeneratedValue" : "io.micronaut.data.annotation.GeneratedValue")); //NOI18N
                    }
                }

                if (!m.isNullable()) {
                    //Add @NotNull constraint
                    if (generateValidationConstraints && !m.isAutoIncrement()) {   //NOI18N
                        annotations.add(genUtils.createAnnotation("javax.validation.constraints.NotNull")); //NOI18N
                    }
                }

                boolean isLobType = m.isLobType();
                if (isLobType && generateJPA) {
                    annotations.add(genUtils.createAnnotation("javax.persistence.Lob")); // NOI18N
                }

                List<ExpressionTree> columnAnnArguments = new ArrayList<>();
                String memberName = m.getMemberName();
                String memberType = m.getMemberType();

                String columnName = dbMappings.getCMPFieldMapping().get(memberName);
                if (!memberName.equalsIgnoreCase(columnName)){
                    columnAnnArguments.add(genUtils.createAnnotationArgument(generateJPA ? "name" : null, columnName)); //NOI18N
                }

                Integer length = m.getLength();
                if (length != null && isCharacterType(memberType)) {
                    if (generateValidationConstraints) {
                        List <ExpressionTree> sizeAnnArguments = new ArrayList<>();
                        if (!m.isNullable()) {
                            sizeAnnArguments.add(genUtils.createAnnotationArgument("min", 1));  //NOI18N
                        }
                        sizeAnnArguments.add(genUtils.createAnnotationArgument("max", length)); //NOI18N
                        annotations.add(genUtils.createAnnotation("javax.validation.constraints.Size", sizeAnnArguments));   //NOI18N
                    }
                }

                if (!columnAnnArguments.isEmpty()) {
                    if (generateJPA) {
                        annotations.add(genUtils.createAnnotation("javax.persistence.Column", columnAnnArguments)); //NOI18N
                    } else if (isPKMember && needsPKClass) {
                        annotations.add(genUtils.createAnnotation("io.micronaut.data.annotation.MappedProperty", columnAnnArguments)); //NOI18N
                    }
                }

                String temporalType = getMemberTemporalType(m);
                if (temporalType != null && generateJPA) {
                    ExpressionTree temporalAnnValueArgument = genUtils.createAnnotationArgument(null, "javax.persistence.TemporalType", temporalType); //NOI18N
                    annotations.add(genUtils.createAnnotation("javax.persistence.Temporal", Collections.singletonList(temporalAnnValueArgument)));
                }

                return new Property(Modifier.PRIVATE, annotations, memberType, memberName);
            }

            protected VariableTree createVariable(EntityMember m) {
                return genUtils.createVariable(typeElement, m.getMemberName(), m.getMemberType());
            }

            private boolean isCharacterType(String type) {
                return "java.lang.String".equals(type); //NOI18N
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
                    newClassTree = make.insertClassMember(newClassTree, position, property.getField());
                    position++;
                }
                for (MethodTree constructor : constructors) {
                    newClassTree = make.addClassMember(newClassTree, constructor);
                }
                for (Property property : properties) {
                    newClassTree = make.addClassMember(newClassTree, property.getGetter());
                    newClassTree = make.addClassMember(newClassTree, property.getSetter());
                }
                for (MethodTree method : methods) {
                    newClassTree = make.addClassMember(newClassTree, method);
                }
                Logger.getLogger(Generator.class.getName()).log(Level.FINE, "Rewrite entity tree with name: {0}", entityClass.getTableName()); //NOI18N
                Logger.getLogger(Generator.class.getName()).log(Level.FINE, "Rewrite entity tree with annotations: length = {0}, annotations = {1}", new Object[]{newClassTree.getModifiers().getAnnotations().size(),  newClassTree.getModifiers().getAnnotations()}); //NOI18N
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

                public Property(Modifier modifier, List<AnnotationTree> annotations, String type, String name) throws IOException {
                    this(modifier, annotations, genUtils.createType(type, typeElement), name);
                }

                public Property(Modifier modifier, List<AnnotationTree> annotations, TypeMirror type, String name) throws IOException {
                    this(modifier, annotations, copy.getTreeMaker().Type(type), name);
                }

                private Property(Modifier modifier, List<AnnotationTree> annotations, Tree typeTree, String name) throws IOException {
                    TreeMaker make = copy.getTreeMaker();
                    field = make.Variable(
                            make.Modifiers(EnumSet.of(modifier), annotations),
                            name,
                            typeTree,
                            null);
                    getter = genUtils.createPropertyGetterMethod(
                            make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.<AnnotationTree>emptyList()),
                            name,
                            typeTree);
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
            }
        }

        private class EntityClassGenerator extends ClassGenerator {

            // the simple name of the entity class
            private final String entityClassName;
            // the non-nullable properties (not including the primary key ones)
            private final List<Property> nonNullableProps = new ArrayList<>();
            // the names of the primary key columns
            private final List<String> pkColumnNames = new ArrayList<>();
            // variables correspoding to the fields in the primary key classs (or empty if no primary key class)
            private final List<VariableTree> pkClassVariables = new ArrayList<>();
            private Property pkProperty;
            private boolean pkGenerated;

            public EntityClassGenerator(WorkingCopy copy, EntityClass entityClass, boolean jpaSupported, boolean beanValidationSupported) throws IOException {
                super(copy, entityClass, jpaSupported, beanValidationSupported);
                entityClassName = getClassName(entityClass);
                assert typeElement.getSimpleName().contentEquals(entityClassName);
            }

            @Override
            protected void initialize() throws IOException {
                newClassTree = genUtils.ensureNoArgConstructor(newClassTree);
                if (needsPKClass) {
                    String pkFieldName = createFieldName(pkClassName);
                    pkProperty = new Property(
                            Modifier.PROTECTED,
                            Collections.singletonList(genUtils.createAnnotation(generateJPA ? "javax.persistence.EmbeddedId" : "io.micronaut.data.annotation.EmbeddedId")), // NOI18N
                            pkFQClassName,
                            pkFieldName);
                    properties.add(pkProperty);
                }
                if (generateSerdeable) {
                    newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("io.micronaut.serde.annotation.Serdeable")); //NOI18N
                }
                if (generateJPA) {
                    newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.persistence.Entity")); //NOI18N
                    if (dbMappings.getTableName() != null && !entityClassName.equalsIgnoreCase(dbMappings.getTableName())) {
                        List<ExpressionTree> tableAnnArgs = Collections.singletonList(genUtils.createAnnotationArgument("name", dbMappings.getTableName())); //NOI18N
                        newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.persistence.Table", tableAnnArgs)); //NOI18N
                    }
                } else if (dbMappings.getTableName() != null && !entityClassName.equalsIgnoreCase(dbMappings.getTableName())) {
                    List<ExpressionTree> tableAnnArgs = Collections.singletonList(genUtils.createAnnotationArgument(null, dbMappings.getTableName())); //NOI18N
                    newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("io.micronaut.data.annotation.MappedEntity", tableAnnArgs)); //NOI18N
                } else {
                    newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("io.micronaut.data.annotation.MappedEntity")); //NOI18N
                }
            }

            @Override
            protected void generateMember(EntityMember m) throws IOException {
                //skip generating already exist members for UPDATE type
                String memberName = m.getMemberName();
                boolean isPKMember = m.isPrimaryKey();
                Property property = null;
                if (isPKMember) {
                    if (needsPKClass) {
                        pkClassVariables.add(createVariable(m));
                    } else {
                        pkProperty = property = createProperty(m);
                    }
                    String pkColumnName = dbMappings.getCMPFieldMapping().get(memberName);
                    pkColumnNames.add(pkColumnName);
                    pkGenerated = m.isAutoIncrement();
                } else {
                    property = createProperty(m);
                    if (!m.isNullable()) {
                        nonNullableProps.add(property);
                    }
                }
                assert (property != null) || (property == null && isPKMember && needsPKClass);
                if (property != null) {
                    properties.add(property);
                }
            }

            @Override
            protected void generateRelationship(RelationshipRole role) throws IOException {
                String memberName = role.getFieldName();
                if (role.isMany() && !role.isToMany()) {
                    String roleName = role.getRoleName();
                    if (roleName.endsWith("Id")) {
                        roleName = roleName.substring(0, roleName.length() - 2);
                    }
                    if (!roleName.isEmpty()) {
                        memberName = Character.toLowerCase(roleName.charAt(0)) + roleName.substring(1);
                    }
                }
                String typeName = getRelationshipFieldType(role, entityClass.getPackage());
                if(replacedTypeNames.containsKey(typeName)) {
                    typeName = replacedTypeNames.get(typeName);
                }
                TypeElement typeEl = moduleElement != null
                        ? copy.getElements().getTypeElement(moduleElement, typeName)
                        : copy.getElements().getTypeElement(typeName);
                assert typeEl != null : "null TypeElement for \"" + typeName + "\"";
                String collectionType = "java.util.Set"; //NOI18N
                TypeMirror fieldType = typeEl.asType();
                if (role.isToMany()) {
                    TypeElement collectionTypeElem = moduleElement != null
                            ? copy.getElements().getTypeElement(moduleElement, collectionType)
                            : copy.getElements().getTypeElement(collectionType);
                    fieldType = copy.getTypes().getDeclaredType(collectionTypeElem, fieldType);
                }
                List<AnnotationTree> annotations = new ArrayList<>();
                List<ExpressionTree> annArguments = new ArrayList<>();
                if (role.isCascade()) {
                    annArguments.add(genUtils.createAnnotationArgument("cascade", generateJPA ? "javax.persistence.CascadeType" : "io.micronaut.data.annotation.Relation.Cascade", "ALL")); // NOI18N
                }
                if (role.equals(role.getParent().getRoleB())) { // Role B
                    String fName = role.getParent().getRoleA().getFieldName();
                    annArguments.add(genUtils.createAnnotationArgument("mappedBy", fName)); // NOI18N
                } else if (generateJPA) {  // Role A
                    if (role.isMany() && role.isToMany()) { // ManyToMany
                        List<ExpressionTree> joinTableAnnArguments = new ArrayList<>();
                        String jTN = dbMappings.getJoinTableMapping().get(role.getFieldName());
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("name", jTN)); //NOI18N
                        CMPMappingModel.JoinTableColumnMapping joinColumnMap = dbMappings.getJoinTableColumnMppings().get(role.getFieldName());
                        CMPMappingModel.ColumnData[] columns = joinColumnMap.getColumns();
                        CMPMappingModel.ColumnData[] refColumns = joinColumnMap.getReferencedColumns();
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("joinColumns", createJoinColumnAnnotations(columns, refColumns, null))); // NOI18N
                        CMPMappingModel.ColumnData[] invColumns = joinColumnMap.getInverseColumns();
                        CMPMappingModel.ColumnData[] refInvColumns = joinColumnMap.getReferencedInverseColumns();
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("inverseJoinColumns", createJoinColumnAnnotations(invColumns, refInvColumns, null))); // NOI18N
                        annotations.add(genUtils.createAnnotation("javax.persistence.JoinTable", joinTableAnnArguments)); // NOI18N
                    } else { // ManyToOne, OneToMany, OneToOne
                        CMPMappingModel.ColumnData[] columns = dbMappings.getCmrFieldMapping().get(role.getFieldName());
                        CMPMappingModel relatedMappings = beanMap.get(role.getParent().getRoleB().getEntityName()).getCMPMapping();
                        CMPMappingModel.ColumnData[] invColumns = relatedMappings.getCmrFieldMapping().get(role.getParent().getRoleB().getFieldName());
                        if (columns.length == 1) {
                            List<ExpressionTree> attrs = new ArrayList<>();
                            attrs.add(genUtils.createAnnotationArgument("name", columns[0].getColumnName())); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", invColumns[0].getColumnName())); //NOI18N
                            makeReadOnlyIfNecessary(pkColumnNames, columns[0].getColumnName(), attrs);
                            annotations.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); //NOI18N
                        } else {
                            ExpressionTree joinColumnsNameAttrValue = genUtils.createAnnotationArgument(null, createJoinColumnAnnotations(columns, invColumns, pkColumnNames));
                            AnnotationTree joinColumnsAnnotation = genUtils.createAnnotation("javax.persistence.JoinColumns", Collections.singletonList(joinColumnsNameAttrValue)); //NOI18N
                            annotations.add(joinColumnsAnnotation);
                        }
                    }
                }

                if (generateJPA && !role.isToMany()) { // meaning ManyToOne or OneToOne
                    // Add optional=false on @ManyToOne or the owning side of @OneToOne
                    // if the relationship is non-optional (or non-nuallable in other words)
                    if (!role.isOptional() && (role.isMany() || role.equals(role.getParent().getRoleA()))) {
                        annArguments.add(genUtils.createAnnotationArgument("optional", false)); // NOI18N
                    }
                }

                // Create the relationship annotation
                if (generateJPA) {
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
                    annotations.add(genUtils.createAnnotation("javax.persistence." + relationAnn, annArguments)); // NOI18N
                } else {
                    String relationAnn;
                    if (role.isMany() && role.isToMany()) {
                        relationAnn = "MANY_TO_MANY"; //NOI18N
                    } else if (role.isMany()) {
                        relationAnn = "MANY_TO_ONE"; //NOI18N
                    } else if (role.isToMany()) {
                        relationAnn = "ONE_TO_MANY"; //NOI18N
                    } else {
                        relationAnn = "ONE_TO_ONE";  //NOI18N
                    }
                    annArguments.add(0, genUtils.createAnnotationArgument("value", "io.micronaut.data.annotation.Relation.Kind", relationAnn)); // NOI18N
                    annotations.add(genUtils.createAnnotation("io.micronaut.data.annotation.Relation", annArguments)); // NOI18N
                }

                properties.add(new Property(Modifier.PRIVATE, annotations, fieldType, memberName));
            }

            @Override
            protected void finish() {
                if (pkProperty != null && !pkGenerated) {
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
                }
            }

            private List<AnnotationTree> createJoinColumnAnnotations(CMPMappingModel.ColumnData[] columns, CMPMappingModel.ColumnData[] refColumns, List<String> pkcNames) {
                List<AnnotationTree> joinCols = new ArrayList<>();
                for (int colIndex = 0; colIndex < columns.length; colIndex++) {
                    List<ExpressionTree> attrs = new ArrayList<>();
                    attrs.add(genUtils.createAnnotationArgument("name", columns[colIndex].getColumnName())); //NOI18N
                    attrs.add(genUtils.createAnnotationArgument("referencedColumnName", refColumns[colIndex].getColumnName())); //NOI18N
                    if (pkcNames != null) {
                        makeReadOnlyIfNecessary(pkcNames, columns[colIndex].getColumnName(), attrs);
                    }
                    joinCols.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); //NOI18N
                }
                return joinCols;
            }

            private String getRelationshipFieldType(RelationshipRole role, String pkg) {
                RelationshipRole rA = role.getParent().getRoleA();
                RelationshipRole rB = role.getParent().getRoleB();
                RelationshipRole otherRole = role.equals(rA) ? rB : rA;

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

        private final class PKClassGenerator extends ClassGenerator {

            public PKClassGenerator(WorkingCopy copy, EntityClass entityClass, boolean jpaSupported, boolean beanValidationSupported) throws IOException {
                super(copy, entityClass, jpaSupported, beanValidationSupported);
            }

            @Override
            protected void initialize() throws IOException {
                newClassTree = genUtils.ensureNoArgConstructor(newClassTree);
                if (generateSerdeable) {
                    newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("io.micronaut.serde.annotation.Serdeable")); //NOI18N
                }
                newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation(generateJPA ? "javax.persistence.Embeddable" : "io.micronaut.data.annotation.Embeddable")); // NOI18N
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
            }
        }

    }
}
