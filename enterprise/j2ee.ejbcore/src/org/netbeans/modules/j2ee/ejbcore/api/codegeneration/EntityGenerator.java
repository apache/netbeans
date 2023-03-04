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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EntityGenerator {

    private static final String BMP_EJBCLASS = "Templates/J2EE/EJB21/BmpEjbClass.java"; // NOI18N
    private static final String BMP_LOCAL = "Templates/J2EE/EJB21/BmpLocal.java"; // NOI18N
    private static final String BMP_LOCALHOME = "Templates/J2EE/EJB21/BmpLocalHome.java"; // NOI18N
    private static final String BMP_REMOTE = "Templates/J2EE/EJB21/BmpRemote.java"; // NOI18N
    private static final String BMP_REMOTEHOME = "Templates/J2EE/EJB21/BmpRemoteHome.java"; // NOI18N

    private static final String CMP_EJBCLASS = "Templates/J2EE/EJB21/CmpEjbClass.java"; // NOI18N
    private static final String CMP_LOCAL = "Templates/J2EE/EJB21/CmpLocal.java"; // NOI18N
    private static final String CMP_LOCALHOME = "Templates/J2EE/EJB21/CmpLocalHome.java"; // NOI18N
    private static final String CMP_REMOTE = "Templates/J2EE/EJB21/CmpRemote.java"; // NOI18N
    private static final String CMP_REMOTEHOME = "Templates/J2EE/EJB21/CmpRemoteHome.java"; // NOI18N

    // informations collected in wizard
    private final FileObject pkg;
    private final boolean hasRemote;
    private final boolean hasLocal;
    private final boolean isCMP;
    private final String primaryKeyClassName;
    private final String wizardTargetName;
    
    // EJB naming options
    private final EJBNameOptions ejbNameOptions;
    private final String ejbName;
    private final String ejbClassName;
    private final String remoteName;
    private final String remoteHomeName;
    private final String localName;
    private final String localHomeName;
    private final String displayName;
    
    private final String packageName;
    private final String packageNameWithDot;
    
    private final Map<String, String> templateParameters;
    private final String primaryKeyName;

    public static EntityGenerator create(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal, 
            boolean isCMP, String primaryKeyClassName, String primaryKeyName) {
        return new EntityGenerator(wizardTargetName, pkg, hasRemote, hasLocal, isCMP, primaryKeyClassName, primaryKeyName, false);
    }
    
    protected EntityGenerator(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal, 
            boolean isCMP, String primaryKeyClassName, String primaryKeyName, boolean isTest) {
        this.pkg = pkg;
        this.hasRemote = hasRemote;
        this.hasLocal = hasLocal;
        this.isCMP = isCMP;
        this.primaryKeyClassName = primaryKeyClassName;
        this.primaryKeyName = primaryKeyName;
        this.wizardTargetName = wizardTargetName;
        this.ejbNameOptions = new EJBNameOptions();
        this.ejbName = ejbNameOptions.getEntityEjbNamePrefix() + wizardTargetName + ejbNameOptions.getEntityEjbNameSuffix();
        this.ejbClassName = ejbNameOptions.getEntityEjbClassPrefix() + wizardTargetName + ejbNameOptions.getEntityEjbClassSuffix();
        this.remoteName = ejbNameOptions.getEntityRemotePrefix() + wizardTargetName + ejbNameOptions.getEntityRemoteSuffix();
        this.remoteHomeName = ejbNameOptions.getEntityRemoteHomePrefix() + wizardTargetName + ejbNameOptions.getEntityRemoteHomeSuffix();
        this.localName = ejbNameOptions.getEntityLocalPrefix() + wizardTargetName + ejbNameOptions.getEntityLocalSuffix();
        this.localHomeName = ejbNameOptions.getEntityLocalHomePrefix() + wizardTargetName + ejbNameOptions.getEntityLocalHomeSuffix();
        this.displayName = ejbNameOptions.getEntityDisplayNamePrefix() + wizardTargetName + ejbNameOptions.getEntityDisplayNameSuffix();
        this.packageName = EjbGenerationUtil.getSelectedPackageName(pkg);
        this.packageNameWithDot = packageName + ".";
        this.templateParameters = new HashMap<String, String>();
        // fill all possible template parameters
        this.templateParameters.put("package", packageName);
        this.templateParameters.put("primaryKey", primaryKeyClassName);
        this.templateParameters.put("primaryKeyName", primaryKeyName);
        this.templateParameters.put("localInterface", packageNameWithDot + localName);
        this.templateParameters.put("remoteInterface", packageNameWithDot + remoteName);
        if (isTest) {
            // set date, time and user to values used in goldenfiles
            this.templateParameters.put("date", "{date}");
            this.templateParameters.put("time", "{time}");
            this.templateParameters.put("user", "{user}");
        }
    }

    public FileObject generate() throws IOException {
        FileObject resultFileObject = null;
        if (isCMP) {
            resultFileObject = generateCmpClasses();
        } else {
            resultFileObject = generateBmpClasses();
        }

        //put these lines in a common function at the appropriate place after EA1
        //something like public EjbJar getEjbJar()
        //This method will be used whereever we construct/get DD object graph to ensure
        //corresponding config listners attached to it.
        Project project = FileOwnerQuery.getOwner(pkg);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();

        generateXml();
        
        return resultFileObject;
    }
    
    private FileObject generateBmpClasses() throws IOException {
        FileObject ejbClassFO = GenerationUtils.createClass(BMP_EJBCLASS,  pkg, ejbClassName, null, templateParameters);
        if (hasRemote) {
            GenerationUtils.createClass(BMP_REMOTE,  pkg, remoteName, null, templateParameters);
            GenerationUtils.createClass(BMP_REMOTEHOME, pkg, remoteHomeName, null, templateParameters);
        }
        if (hasLocal) {
            GenerationUtils.createClass(BMP_LOCAL, pkg, localName, null, templateParameters);
            GenerationUtils.createClass(BMP_LOCALHOME, pkg, localHomeName, null, templateParameters);
        }
        return ejbClassFO;
    }

    private FileObject generateCmpClasses() throws IOException {
        FileObject ejbClassFO = GenerationUtils.createClass(CMP_EJBCLASS,  pkg, ejbClassName, null, templateParameters);
        if (hasRemote) {
            GenerationUtils.createClass(CMP_REMOTE,  pkg, remoteName, null, templateParameters);
            GenerationUtils.createClass(CMP_REMOTEHOME, pkg, remoteHomeName, null, templateParameters);
        }
        if (hasLocal) {
            GenerationUtils.createClass(CMP_LOCAL, pkg, localName, null, templateParameters);
            GenerationUtils.createClass(CMP_LOCALHOME, pkg, localHomeName, null, templateParameters);
        }
        return ejbClassFO;
    }

    private void generateXml() throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ejbModule.getDeploymentDescriptor()); // EJB 2.1
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans == null) {
            enterpriseBeans = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(enterpriseBeans);
        }
        Entity entity = enterpriseBeans.newEntity();
        entity.setEjbName(ejbName);
        entity.setEjbClass(packageNameWithDot + ejbClassName);
        entity.setPrimKeyClass(primaryKeyClassName);
        entity.setReentrant(false);
        entity.setDisplayName(displayName);
        if (hasRemote) {
            entity.setRemote(packageNameWithDot + remoteName);
            entity.setHome(packageNameWithDot + remoteHomeName);
        }
        if (hasLocal) {
            entity.setLocal(packageNameWithDot + localName);
            entity.setLocalHome(packageNameWithDot + localHomeName);
        }
        if (isCMP) {
            entity.setPersistenceType(Entity.PERSISTENCE_TYPE_CONTAINER);
            entity.setAbstractSchemaName(wizardTargetName);
            if (primaryKeyName == null) {
                CmpField cmpField = entity.newCmpField();
                cmpField.setFieldName("pk");
                entity.addCmpField(cmpField);
                entity.setPrimkeyField("pk");
            } else {
                entity.setPrimkeyField(primaryKeyName);
            }
        } else {
            entity.setPersistenceType(Entity.PERSISTENCE_TYPE_BEAN);
        }
        enterpriseBeans.addEntity(entity);
        // add transaction requirements
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor == null) {
            assemblyDescriptor = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        }
        ContainerTransaction containerTransaction = assemblyDescriptor.newContainerTransaction();
        containerTransaction.setTransAttribute("Required"); //NOI18N;
        org.netbeans.modules.j2ee.dd.api.ejb.Method method = containerTransaction.newMethod();
        method.setEjbName(ejbName);
        method.setMethodName("*"); //NOI18N;
        containerTransaction.addMethod(method);
        assemblyDescriptor.addContainerTransaction(containerTransaction);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
    }

}
