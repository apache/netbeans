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

package org.netbeans.modules.j2ee.ejbcore.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodParams;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class FinderMethodGenerator extends AbstractMethodGenerator {

    private FinderMethodGenerator(String ejbClass, FileObject ejbClassFileObject) {
        super(ejbClass, ejbClassFileObject);
    }

    public static FinderMethodGenerator create(String ejbClass, FileObject ejbClassFileObject) {
        return new FinderMethodGenerator(ejbClass, ejbClassFileObject);
    }

    /**
     * Generates finder method.
     * <p>
     * <b>Should be called outside EDT.</b>
     */
    public void generate(MethodModel methodModel, boolean generateLocal, boolean generateRemote,
            boolean isOneReturn, String ejbql) throws IOException {
        String persistenceType = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                Entity entity = (Entity) metadata.findByEjbClass(ejbClass);
                return entity != null ? entity.getPersistenceType() : null;
            }
        });
        if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(persistenceType)) {
            generateCmp(methodModel, generateLocal, generateRemote, isOneReturn, ejbql);
        } else if (Entity.PERSISTENCE_TYPE_BEAN.equals(persistenceType)) {
            generateBmp(methodModel, generateLocal, generateRemote, isOneReturn, ejbql);
        }
    }

    private void generateCmp(MethodModel methodModel, boolean generateLocal, boolean generateRemote,
            boolean isOneReturn, String ejbql) throws IOException {

        if (!methodModel.getName().startsWith("find")) {
            throw new IllegalArgumentException("The finder method name must have find as its prefix.");
        }

        Map<String, String> interfaces = getInterfaces();
        String local = interfaces.get(EntityAndSession.LOCAL);
        String localHome = interfaces.get(EntityAndSession.LOCAL_HOME);
        String remote = interfaces.get(EntityAndSession.REMOTE);
        String remoteHome = interfaces.get(EntityAndSession.HOME);

        // local interface EJB 2.1 spec 10.6.12
        if (generateLocal && local != null && localHome != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    isOneReturn ? local : "java.util.Collection",
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, localHome);
            addMethod(methodModelCopy, fileObject, localHome);
        }

        // remote interface
        if (generateRemote && remote != null && remoteHome != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    isOneReturn ? remote : "java.util.Collection",
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, remoteHome);
            addMethod(methodModelCopy, fileObject, remoteHome);
        }

        // write query to deplyment descriptor
        addQueryToXml(methodModel, ejbql);

    }

    private void generateBmp(MethodModel methodModel, boolean generateLocal, boolean generateRemote,
            boolean isOneReturn, String ejbql) throws IOException {

        if (!methodModel.getName().startsWith("find")) {
            throw new IllegalArgumentException("The finder method name must have find as its prefix.");
        }

        Map<String, String> interfaces = getInterfaces();
        String local = interfaces.get(EntityAndSession.LOCAL);
        String localHome = interfaces.get(EntityAndSession.LOCAL_HOME);
        String remote = interfaces.get(EntityAndSession.REMOTE);
        String remoteHome = interfaces.get(EntityAndSession.HOME);

        // local interface EJB 2.1 spec 10.6.12
        if (generateLocal && local != null && localHome != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            // find method in LocalHome interface
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                isOneReturn ? local : "java.util.Collection",
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, localHome);
            addMethod(methodModelCopy, fileObject, localHome);
        }

        String primKeyClass = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                Entity entity = (Entity) metadata.findByEjbClass(ejbClass);
                return entity != null ? entity.getPrimKeyClass() : null;
            }
        });

        // remote interface
        if (generateRemote && remote != null && remoteHome != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            // find method in RemoteHome interface
            MethodModel methodModelCopy = MethodModel.create(
                    'c' + methodModel.getName().substring(4),
                    isOneReturn ? remote : "java.util.Collection",
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, remoteHome);
            addMethod(methodModelCopy, fileObject, remoteHome);

            // ejbFind method in ejb class
            methodModelCopy = MethodModel.create(
                    "ejbF" + methodModelCopy.getName().substring(1),
                    isOneReturn ? primKeyClass : "java.util.Collection",
                    methodModelCopy.getBody(),
                    methodModelCopy.getParameters(),
                    methodModelCopy.getExceptions(),
                    Collections.singleton(Modifier.PUBLIC)
                    );
            addMethod(methodModelCopy, ejbClassFileObject, ejbClass);

        }

        // ejbFind method in ejb class
        List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
        if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
            exceptions.add("javax.ejb.FinderException");
        }
        MethodModel methodModelCopy = MethodModel.create(
                "ejbF" + methodModel.getName().substring(1),
                isOneReturn ? primKeyClass : "java.util.Collection",
                "return null;",
                methodModel.getParameters(),
                exceptions,
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejbClass);

    }

    private void addQueryToXml(MethodModel methodModel, String ejbql) throws IOException {
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ejbModule.getDeploymentDescriptor()); // EJB 2.1
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Entity entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, ejbClass);
        Query query = entity.newQuery();
        QueryMethod queryMethod = query.newQueryMethod();
        queryMethod.setMethodName(methodModel.getName());
        MethodParams methodParams = queryMethod.newMethodParams();
        for (MethodModel.Variable parameter : methodModel.getParameters()) {
            methodParams.addMethodParam(parameter.getType());
        }
        queryMethod.setMethodParams(methodParams);
        query.setQueryMethod(queryMethod);
        query.setEjbQl(ejbql);
        entity.addQuery(query);
        saveXml();
    }

}
