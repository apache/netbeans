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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
