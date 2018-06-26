/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.ServiceLocatorStrategy;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Adamek
 */
public final class UseDatabaseGenerator {

    private final static Logger LOGGER = Logger.getLogger(UseDatabaseGenerator.class.getName());

    public UseDatabaseGenerator() {
    }

    @Messages({
        "server.resource.exists.message=Data Source in project for JNDI name \"{0}\" already exists."
    })
    public void generate(final FileObject fileObject, String className,
                         final J2eeModuleProvider j2eeModuleProvider, final String datasourceReferenceName, 
                         final Datasource datasource, final boolean createServerResources, String serviceLocator) 
                         throws IOException, ConfigurationException {
        
        Project project = FileOwnerQuery.getOwner(fileObject);
        ServiceLocatorStrategy serviceLocatorStrategy = (serviceLocator == null) ? null : 
            ServiceLocatorStrategy.create(project, fileObject, serviceLocator);
        EnterpriseReferenceContainer erc = project.getLookup().lookup(EnterpriseReferenceContainer.class);
        if (ProjectUtil.isJavaEE5orHigher(project) && serviceLocatorStrategy == null &&
                InjectionTargetQuery.isInjectionTarget(fileObject, className)) {
            boolean isStatic = InjectionTargetQuery.isStaticReferenceRequired(fileObject, className);
            String fieldName = Utils.makeJavaIdentifierPart(Utils.jndiNameToCamelCase(datasourceReferenceName, true, null));
            _RetoucheUtil.generateAnnotatedField(fileObject, className, "javax.annotation.Resource", fieldName, // NOI18N
                    "javax.sql.DataSource", Collections.singletonMap("name", datasourceReferenceName), isStatic); // NOI18N
        } else {
            String jndiName = generateJNDILookup(datasourceReferenceName, erc, fileObject, className, datasource.getUrl());
            if (jndiName != null) {
                generateLookupMethod(fileObject, className, datasourceReferenceName, serviceLocatorStrategy);
            }
        }
        
        if (createServerResources) {
            try {
                if (findProjectDatasource(j2eeModuleProvider, datasource.getJndiName()) != null) {
                    NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                            Bundle.server_resource_exists_message(datasource.getJndiName()));
                    DialogDisplayer.getDefault().notify(descriptor);
                } else {
                    j2eeModuleProvider.getConfigSupport().createDatasource(
                        datasource.getJndiName(),
                        datasource.getUrl(),
                        datasource.getUsername(),
                        datasource.getPassword(),
                        datasource.getDriverClassName()
                        );
                }
            } catch (DatasourceAlreadyExistsException daee) {
                LOGGER.log(Level.WARNING, null, daee);
            }
        }

        J2eeModule module = j2eeModuleProvider.getJ2eeModule();
        if (isWebOrAppClientModule(module)) {
            bindDataSourceReference(j2eeModuleProvider, datasourceReferenceName, datasource);
        }
        else if (isEjbModule(module)) {
            bindDataSourceReferenceForEjb(j2eeModuleProvider, datasourceReferenceName, datasource, fileObject, className);
        }
        
        if (serviceLocator != null) {
            erc.setServiceLocatorName(serviceLocator);
        }
    }

    private Datasource findProjectDatasource(J2eeModuleProvider j2eeModuleProvider, String jndiName) throws ConfigurationException {
        Set<Datasource> datasources = j2eeModuleProvider.getConfigSupport().getDatasources();
        for (Datasource ds : datasources) {
            if (ds.getJndiName().equals(jndiName)) {
                return ds;
            }
        }
        return null;
    }
    
    private void bindDataSourceReference(J2eeModuleProvider j2eeModuleProvider, String dsRefName, Datasource datasource) 
    throws ConfigurationException {

        String dsJndiName = datasource.getJndiName();
        j2eeModuleProvider.getConfigSupport().bindDatasourceReference(dsRefName, dsJndiName);
    }
    
    private void bindDataSourceReferenceForEjb(J2eeModuleProvider j2eeModuleProvider, String dsRefName, Datasource datasource,
            FileObject fileObject, final String className) throws ConfigurationException, IOException {

        final String[] ejbName = new String[1];
        final String[] ejbType = new String[1];

        MetadataModel<EjbJarMetadata> metadataModel = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject).getMetadataModel();
        metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws Exception {
                Ejb ejb = metadata.findByEjbClass(className);
                if (ejb != null) {
                    ejbName[0] = ejb.getEjbName();
                    if (ejb instanceof Session) {
                        ejbType[0] = EnterpriseBeans.SESSION;
                    } else if (ejb instanceof MessageDriven) {
                        ejbType[0] = EnterpriseBeans.MESSAGE_DRIVEN;
                    } else if (ejb instanceof Entity) {
                        ejbType[0] = EnterpriseBeans.ENTITY;
                    }
                }
                return null;
            }
        });
        
        if (ejbName[0] != null && ejbType[0] != null) {
            String dsJndiName = datasource.getJndiName();
            j2eeModuleProvider.getConfigSupport().bindDatasourceReferenceForEjb(ejbName[0], ejbType[0], dsRefName, dsJndiName);
        }
    }
    
    private boolean isWebOrAppClientModule(J2eeModule module) {
        J2eeModule.Type moduleType = module.getType();
        return J2eeModule.Type.WAR.equals(moduleType) || J2eeModule.Type.CAR.equals(moduleType);
    }
    
    private boolean isEjbModule(J2eeModule module) {
        return module.getType().equals(J2eeModule.Type.EJB);
    }
    
    private String generateJNDILookup(String datasourceReferenceName, EnterpriseReferenceContainer enterpriseReferenceContainer, 
            FileObject fileObject, String className, String nodeName) throws IOException {
        ResourceReference resourceReference = ResourceReference.create(
                datasourceReferenceName,
                javax.sql.DataSource.class.getName(),
                ResourceRef.RES_AUTH_CONTAINER,
                ResourceRef.RES_SHARING_SCOPE_SHAREABLE,
                nodeName
                );
        return enterpriseReferenceContainer.addResourceRef(resourceReference, fileObject, className);
    }
    
    private void generateLookupMethod(FileObject fileObject, final String className, final String datasourceReferenceName, 
            final ServiceLocatorStrategy slStrategy) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final String body = slStrategy == null ? getLookupCode(datasourceReferenceName) : getLookupCode(datasourceReferenceName, slStrategy, fileObject, className);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                String methodName = "get" + Utils.makeJavaIdentifierPart(Utils.jndiNameToCamelCase(datasourceReferenceName, false, null)); //NO18N
                MethodModel methodModel = MethodModel.create(
                        methodName,
                        javax.sql.DataSource.class.getName(),
                        body,
                        Collections.<MethodModel.Variable>emptyList(),
                        Collections.singletonList(javax.naming.NamingException.class.getName()),
                        Collections.singleton(Modifier.PRIVATE)
                        );
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree modifiedClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();
    }
    
    private String getLookupCode(String jndiName, ServiceLocatorStrategy serviceLocatorStrategy, FileObject fileObject, String className) {
        String jdbcLookupString = serviceLocatorStrategy.genDataSource(jndiName, fileObject, className);
        return "return (javax.sql.DataSource) " + jdbcLookupString + ";\n"; // NOI18N
    }
    
    private String getLookupCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.sql.DataSource) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] { jndiName });
    }
    
}
