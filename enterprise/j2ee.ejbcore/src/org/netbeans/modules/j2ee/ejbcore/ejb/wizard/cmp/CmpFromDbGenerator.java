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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp;

import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.action.CmFieldGenerator;
import org.netbeans.modules.j2ee.ejbcore.action.FinderMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EntityGenerator;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.modules.j2ee.ejbcore.spi.ProjectPropertiesSupport;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation;
import org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class CmpFromDbGenerator {
    
    private static final String FINDER_EXCEPTION_CLASS = "javax.ejb.FinderException"; //NOI18N
    
    private final Project project;
    private final FileObject ddFileObject;
    private final EjbJar ejbJar;
    private final EJBNameOptions ejbnames;

    public CmpFromDbGenerator(Project project, FileObject ddFileObject) throws IOException {
        this.project = project;
        this.ddFileObject = ddFileObject;
        this.ejbJar = DDProvider.getDefault().getDDRoot(ddFileObject); // EJB 2.1
        this.ejbnames = new EJBNameOptions();
    }
    
    public void generateBeans(RelatedCMPHelper helper, FileObject dbschemaFile, ProgressNotifier progressNotifier) throws IOException {
        
        disableSunCmpMappingsExclusion();
        J2eeModuleProvider pwm = project.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        
        if (ejbJar.getEnterpriseBeans()==null) {
            EnterpriseBeans eBeans = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(eBeans);
        }
        
        int entityClassIndex = 0;
        int max = 2 * helper.getBeans().length + 4;
        progressNotifier.switchToDeterminate(max);
        OriginalCMPMapping[] mappings = new OriginalCMPMapping[helper.getBeans().length];

        for (EntityClass entityClass : helper.getBeans()) {
            progressNotifier.progress(NbBundle.getMessage(CmpFromDbGenerator.class, "TXT_GeneratingClasses", entityClass.getClassName()));
            String pkClassName = null;
            List<EntityMember> primaryKeys = new ArrayList<EntityMember>();
            for (EntityMember entityMember : entityClass.getFields()) {
                if (entityMember.isPrimaryKey()) {
                    pkClassName = entityMember.getMemberType();
                    primaryKeys.add(entityMember);
                }
            }
            if (primaryKeys.size() > 1) {
                String pkFieldName = ejbnames.getEntityPkClassPrefix() + entityClass.getClassName() + ejbnames.getEntityPkClassSuffix();
                entityClass.setPkFieldName(pkFieldName);
                pkClassName = Character.toUpperCase(pkFieldName.charAt(0)) + pkFieldName.substring(1);
                GenerationUtils.createClass(
                        "Templates/J2EE/EJB21/CmpPrimaryKey.java",
                        entityClass.getPackageFileObject(),
                        pkClassName,
                        null,
                        Collections.singletonMap("seq", primaryKeys)
                        );
            } else if (primaryKeys.size() == 1) {
                entityClass.setPkFieldName(primaryKeys.get(0).getMemberName());
            }
            String wizardTargetName = entityClass.getClassName();
            
            EntityGenerator generator = EntityGenerator.create(
                    wizardTargetName,
                    entityClass.getPackageFileObject(),
                    false,
                    true,
                    true,
                    pkClassName,
                    entityClass.getPkFieldName()
                    );
            FileObject ejbClassFileObject = generator.generate();
            
            String packageNameWithDot = EjbGenerationUtil.getSelectedPackageName(entityClass.getPackageFileObject()) + ".";

            progressNotifier.progress(2*entityClassIndex+3);
            String ejbClassName = packageNameWithDot + ejbnames.getEntityEjbClassPrefix() + wizardTargetName + ejbnames.getEntityEjbClassSuffix();
            Entity entity = findEntityForEjbClass(ejbClassName);
            FinderMethodGenerator finderGenerator = FinderMethodGenerator.create(ejbClassName, ejbClassFileObject);
            //            if (helper.isGenerateFinderMethods()) { // is it possible to have CMP with finder method in impl class?
            progressNotifier.progress(NbBundle.getMessage(CmpFromDbGenerator.class, "TXT_GeneratingFinderMethods", wizardTargetName));
            addFinderMethods(finderGenerator, entity, entityClass.getPackageFileObject(), entityClass, helper.isGenerateMappedSuperclasses());
            //            }
            
            addCmpFields(ejbClassName, entityClass);
            populateEntity(entityClass, entity, wizardTargetName);
            
            DatabaseConnection dbconn = helper.getDatabaseConnection();
            if(dbconn != null) {
                entity.setDescription(dbconn.getName());
            }
            progressNotifier.progress(NbBundle.getMessage(CmpFromDbGenerator.class, "TXT_PersistingOriginalMapping", entityClass.getClassName()));
            mappings[entityClassIndex] = new CMPMapping(entity.getEjbName(), entityClass.getCMPMapping(), dbschemaFile);
            progressNotifier.progress(2*entityClassIndex+4);
            entityClassIndex++;
        }
        
        progressNotifier.progress(NbBundle.getMessage(CmpFromDbGenerator.class, "TXT_GeneratingRelationships"));
        // again going through all entities, it must be done after all classes are generated,
        // because we will resolve the type of relationship fields
        for (EntityClass entityClass : helper.getBeans()) {
            String packageNameWithDot = EjbGenerationUtil.getSelectedPackageName(entityClass.getPackageFileObject()) + ".";
            String wizardTargetName = entityClass.getClassName();
            String ejbClassName = packageNameWithDot + ejbnames.getEntityEjbClassPrefix() + wizardTargetName + ejbnames.getEntityEjbClassSuffix();
            addRelationshipFields(ejbClassName, entityClass);
        }
        EntityRelation[] relation = helper.getRelations();
        if (ejbJar.getSingleRelationships() == null && relation.length > 0) {
            ejbJar.setRelationships(ejbJar.newRelationships());
        }
        Relationships rels = ejbJar.getSingleRelationships();
        for (int i = 0; i < relation.length; i++) {
            EjbRelation ejbRel = rels.newEjbRelation();
            populateRelation(ejbRel, relation[i]);
            rels.addEjbRelation(ejbRel);
        }
        progressNotifier.progress(max - 1);
        progressNotifier.progress(NbBundle.getMessage(CmpFromDbGenerator.class, "TXT_SavingDeploymentDescriptor"));

        // Push mapping information
        // !PW should this really be called before ejb-jar.xml changes are saved to disk?
        if (pwm != null) {
            try {
                pwm.getConfigSupport().setCMPMappingInfo(mappings);
            } catch(ConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        ejbJar.write(ddFileObject);
        
        if (pwm != null) {
            for (EntityClass entityClass : helper.getBeans()) {
                if (helper.getTableSource().getType() == TableSource.Type.DATA_SOURCE) {
		    try {
                        String ejbClassName = entityClass.getPackage() + '.' + 
                                ejbnames.getEntityEjbClassPrefix() + 
                                entityClass.getClassName() + 
                                ejbnames.getEntityEjbClassSuffix();
                        Entity entity = findEntityForEjbClass(ejbClassName);
                        pwm.getConfigSupport().setCMPResource(entity.getEjbName(), helper.getTableSource().getName());
		    } catch (ConfigurationException ex) {
                        Exceptions.printStackTrace(ex);
		    }
                }
            }
        }
        progressNotifier.progress(max);
    }
    
    private void addFinderMethods(FinderMethodGenerator generator, Entity entity, FileObject pkg, EntityClass genData, boolean generateLocal) throws IOException {
        FileObject ejbClassFO = pkg.getFileObject(EjbGenerationUtil.getBaseName(entity.getEjbClass()), "java"); // NOI18N
        assert ejbClassFO != null: "interface class "+ entity.getLocalHome() + " not found in " + pkg;
        
        Iterator<EntityMember> iterator = genData.getFields().iterator();
        while (iterator.hasNext()) {
            EntityMember entityMember = iterator.next();
            if (entityMember.supportsFinder()) { // TODO consider not generating for primary key
                String methodName = "findBy" + EntityMember.makeClassName(entityMember.getMemberName()); // NOI18N
                MethodModel.Variable parameter = MethodModel.Variable.create(entityMember.getMemberType(), entityMember.getMemberName());
                MethodModel methodModel = MethodModel.create(
                        methodName,
                        "java.util.Collection",
                        null,
                        Collections.singletonList(parameter),
                        Collections.singletonList(FINDER_EXCEPTION_CLASS),
                        Collections.<Modifier>emptySet()
                        );
                String ejbql  = MessageFormat.format(
                        "SELECT OBJECT({0}) " +
                        "FROM {1} AS {0} " + // abstract schema name
                        "WHERE {0}.{2} = ?1", // cmp field
                        new Object[] {
                    Character.toLowerCase(entity.getAbstractSchemaName().charAt(0)) + "",
                    entity.getAbstractSchemaName(),
                    entityMember.getMemberName()
                }
                );
                generator.generate(methodModel, generateLocal, false, false, ejbql);
            }
        }
        
    }
    
    private void populateEntity(EntityClass bean, Entity entity, String wizardTargetName) {
        if (bean.isUsePkField()) {
            entity.setPrimkeyField(bean.getPkFieldName());
        }
        entity.setAbstractSchemaName(wizardTargetName);
    }
    
    private void populateRelation(EjbRelation ejbR, EntityRelation entityRelation) {
        RelationshipRole roleA = entityRelation.getRoleA();
        RelationshipRole roleB = entityRelation.getRoleB();
        
        EjbRelationshipRole ejbRoleA = ejbR.newEjbRelationshipRole();
        EjbRelationshipRole ejbRoleB = ejbR.newEjbRelationshipRole();
        
        populateRole(ejbRoleA, roleA);
        populateRole(ejbRoleB, roleB);
        
        ejbR.setEjbRelationName(entityRelation.getRelationName());
        ejbR.setEjbRelationshipRole(ejbRoleA);
        ejbR.setEjbRelationshipRole2(ejbRoleB);
    }
    
    private void populateRole(EjbRelationshipRole ejbR, RelationshipRole role) {
        ejbR.setCascadeDelete(role.isCascade());
        RelationshipRoleSource source = ejbR.newRelationshipRoleSource();
        source.setEjbName(ejbnames.getEntityEjbNamePrefix() + role.getEntityName() + ejbnames.getEntityEjbNameSuffix());
        ejbR.setRelationshipRoleSource(source);
        CmrField cmrField = ejbR.newCmrField();
        cmrField.setCmrFieldName(role.getFieldName());
        if (role.isMany()) {
            ejbR.setMultiplicity(ejbR.MULTIPLICITY_MANY);
        } else {
            ejbR.setMultiplicity(ejbR.MULTIPLICITY_ONE);
        }
        if (role.isToMany()) {
            cmrField.setCmrFieldType(java.util.Collection.class.getName());
        }
        ejbR.setCmrField(cmrField);
        ejbR.setEjbRelationshipRoleName(role.getEntityName());
    }
    
    private String getCmrFieldType(RelationshipRole role, String pkg) {
        if (role.isToMany()) {
            return java.util.Collection.class.getName();
        } else {
            RelationshipRole roleA = role.getParent().getRoleA();
            RelationshipRole roleB = role.getParent().getRoleB();
            RelationshipRole otherRole = role.equals(roleA) ? roleB : roleA;
            EJBNameOptions ejbNames = new EJBNameOptions();
            String ejbClassName = pkg + "." + ejbNames.getEntityEjbClassPrefix() + otherRole.getEntityName() + ejbNames.getEntityEjbClassSuffix();
            
            Entity entity = (Entity) ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, ejbClassName);
            return entity.getLocal();
        }
    }
    
    /**
     * Temporary fix for #53475. By default the sun-cmp-mappings.xml file is
     * excluded from the JAR. It is again included when the user goes through this
     * wizard.
     */
    private void disableSunCmpMappingsExclusion() {
        if (org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project).length == 0) {
            return;
        }
        ProjectPropertiesSupport projectPropertiesSupport = project.getLookup().lookup(ProjectPropertiesSupport.class);
        if (projectPropertiesSupport != null) {
            projectPropertiesSupport.disableSunCmpMappingExclusion();
        }
    }
    
    private void addCmpFields(String ejbClass, EntityClass entityClass) throws IOException {
        EJBNameOptions ejbNames = new EJBNameOptions();
        String className = ejbNames.getEntityEjbClassPrefix() + entityClass.getClassName() + ejbNames.getEntityEjbClassSuffix();
        FileObject ejbClassFO = entityClass.getPackageFileObject().getFileObject(EjbGenerationUtil.getBaseName(className), "java"); // NOI18N
        CmFieldGenerator generator = CmFieldGenerator.create(ejbClass, ejbClassFO);
        for (EntityMember m : entityClass.getFields()) {
            generator.addCmpField(
                    MethodModel.Variable.create(m.getMemberType(), m.getMemberName()),
                    true,
                    true,
                    false,
                    false,
                    null
                    );
        }
    }
    
    /**
     * Doesn't write entry to deployment descriptor
     */
    private void addRelationshipFields(String ejbClass, EntityClass entityClass) throws IOException {
        FileObject ejbClassFO = entityClass.getPackageFileObject().getFileObject(EjbGenerationUtil.getBaseName(ejbClass), "java"); // NOI18N
        CmFieldGenerator generator = CmFieldGenerator.create(ejbClass, ejbClassFO);
        for (RelationshipRole role : entityClass.getRoles()) {
            String cmrFieldType = getCmrFieldType(role, entityClass.getPackage());
            MethodModel.Variable field = MethodModel.Variable.create(cmrFieldType, role.getFieldName());
            generator.addFieldToClass(field, true, true, false, false);
        }
    }
    
    private Entity findEntityForEjbClass(String className) throws IOException {
        if (ejbJar != null) {
            EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
            if (enterpriseBeans != null) {
                return (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, className);
            }
        }
        return null;
    }
    
    public static interface ProgressNotifier {
        
        void switchToDeterminate(int workunits);
        
        void progress(int workunit);
        
        void progress(String message);
        
    }
    
}
