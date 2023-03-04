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

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class SessionGeneratorTest extends TestBase {
    
    public SessionGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateJavaEE14() throws IOException {
        TestModule testModule = createEjb21Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE14");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE14");

        // Stateless EJB in Java EE 1.4
        
        SessionGenerator sessionGenerator = new SessionGenerator("TestStatelessLR", packageFileObject, true, true, Session.SESSION_TYPE_STATELESS, false, false, true, null, false, false, true);
        sessionGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Session session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, "TestStatelessLR");

        assertNotNull(session);
        assertEquals("TestStatelessLRSB", session.getDefaultDisplayName());
        assertEquals("TestStatelessLR", session.getEjbName());
        assertEquals("testGenerateJavaEE14.TestStatelessLRRemoteHome", session.getHome());
        assertEquals("testGenerateJavaEE14.TestStatelessLRRemote", session.getRemote());
        assertEquals("testGenerateJavaEE14.TestStatelessLRLocalHome", session.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestStatelessLRLocal", session.getLocal());
        assertEquals("testGenerateJavaEE14.TestStatelessLR", session.getEjbClass());
        assertEquals("Stateless", session.getSessionType());
        assertEquals("Container", session.getTransactionType());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLR.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLR.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRRemote.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLRRemoteHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatelessLRRemoteHome.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 1.4
        
        sessionGenerator = new SessionGenerator("TestStatefulLR", packageFileObject, false, true, Session.SESSION_TYPE_STATEFUL, false, false, true, null, false, false, true);
        sessionGenerator.generate();
        session = (Session) enterpriseBeans.findBeanByName(EnterpriseBeans.SESSION, Session.EJB_NAME, "TestStatefulLR");

        assertNotNull(session);
        assertEquals("TestStatefulLRSB", session.getDefaultDisplayName());
        assertEquals("TestStatefulLR", session.getEjbName());
        assertNull(session.getHome());
        assertNull(session.getRemote());
        assertEquals("testGenerateJavaEE14.TestStatefulLRLocalHome", session.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestStatefulLRLocal", session.getLocal());
        assertEquals("testGenerateJavaEE14.TestStatefulLR", session.getEjbClass());
        assertEquals("Stateful", session.getSessionType());
        assertEquals("Container", session.getTransactionType());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLR.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLR.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulLRLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestStatefulLRLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestStatefulLRRemote.java"));
        assertNull(packageFileObject.getFileObject("TestStatefulLRRemoteHome.java"));
    }
    
    public void testGenerateJavaEE50() throws IOException {
        TestModule testModule = createEjb30Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE50");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE50");

        // Stateless EJB in Java EE 5.0 defined in annotations
        
        SessionGenerator sessionGenerator = new SessionGenerator("TestStateless", packageFileObject, true, true, Session.SESSION_TYPE_STATELESS, true, false, false, null, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStateless.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStateless.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLocal.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessRemote.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatelessRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 5.0 defined in annotations
        
        sessionGenerator = new SessionGenerator("TestStateful", packageFileObject, true, false, Session.SESSION_TYPE_STATEFUL, true, false, false, null, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStateful.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStateful.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulRemote.java")), 
                getGoldenFile("testGenerateJavaEE50/TestStatefulRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestStatefulLocal.java"));
    }

    public void testGenerateJavaEE60() throws IOException {
        TestModule testModule = createEjb31Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE60");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE60");

        // Stateless EJB in Java EE 6.0 defined in annotations

        SessionGenerator sessionGenerator = new SessionGenerator("TestStateless", packageFileObject, true, true, Session.SESSION_TYPE_STATELESS, true, false, false, null, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStateless.java")),
                getGoldenFile("testGenerateJavaEE60/TestStateless.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessLocal.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatelessLocal.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatelessRemote.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatelessRemote.java"),
                FileUtil.toFile(packageFileObject)
                );

        // Stateful EJB in Java EE 6.0 defined in annotations

        sessionGenerator = new SessionGenerator("TestStateful", packageFileObject, true, false, Session.SESSION_TYPE_STATEFUL, true, false, false, null, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStateful.java")),
                getGoldenFile("testGenerateJavaEE60/TestStateful.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestStatefulRemote.java")),
                getGoldenFile("testGenerateJavaEE60/TestStatefulRemote.java"),
                FileUtil.toFile(packageFileObject)
                );

        assertNull(packageFileObject.getFileObject("TestStatefulLocal.java"));

        // Singleton EJB in Java EE 6.0 defined in annotations

        sessionGenerator = new SessionGenerator("TestSingleton", packageFileObject, false, false, Session.SESSION_TYPE_SINGLETON, true, false, false, null, false, false, true);
        sessionGenerator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestSingleton.java")),
                getGoldenFile("testGenerateJavaEE60/TestSingleton.java"),
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestSingletonLocal.java"));
        assertNull(packageFileObject.getFileObject("TestSingletonRemote.java"));

    }

}
