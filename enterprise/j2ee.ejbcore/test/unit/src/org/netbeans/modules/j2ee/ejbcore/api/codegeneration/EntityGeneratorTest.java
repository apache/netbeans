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
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class EntityGeneratorTest extends TestBase {
    
    public EntityGeneratorTest(String testName) {
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

        // CMP Entity EJB in Java EE 1.4
        
        EntityGenerator entityGenerator = new EntityGenerator("TestCmp", packageFileObject, true, true, true, "java.lang.Long", null, true);
        entityGenerator.generate();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Entity entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_NAME, "TestCmp");

        assertNotNull(entity);
        assertEquals("TestCmpEB", entity.getDefaultDisplayName());
        assertEquals("TestCmp", entity.getEjbName());
        assertEquals("testGenerateJavaEE14.TestCmpRemoteHome", entity.getHome());
        assertEquals("testGenerateJavaEE14.TestCmpRemote", entity.getRemote());
        assertEquals("testGenerateJavaEE14.TestCmpLocalHome", entity.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestCmpLocal", entity.getLocal());
        assertEquals("testGenerateJavaEE14.TestCmp", entity.getEjbClass());
        assertEquals("Container", entity.getPersistenceType());
        assertEquals("java.lang.Long", entity.getPrimKeyClass());
        assertFalse(entity.isReentrant());
        assertEquals("TestCmp", entity.getAbstractSchemaName());
        assertEquals(1, entity.getCmpField().length);
        assertEquals("pk", entity.getCmpField()[0].getFieldName());
        assertEquals("pk", entity.getPrimkeyField());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmp.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmp.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpRemote.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpRemote.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestCmpRemoteHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestCmpRemoteHome.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // BMP Entity EJB in Java EE 1.4
        
        entityGenerator = new EntityGenerator("TestBmp", packageFileObject, false, true, false, "java.lang.Long", null, true);
        entityGenerator.generate();
        entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_NAME, "TestBmp");

        assertNotNull(entity);
        assertEquals("TestBmpEB", entity.getDefaultDisplayName());
        assertEquals("TestBmp", entity.getEjbName());
        assertNull(entity.getHome());
        assertNull(entity.getRemote());
        assertEquals("testGenerateJavaEE14.TestBmpLocalHome", entity.getLocalHome());
        assertEquals("testGenerateJavaEE14.TestBmpLocal", entity.getLocal());
        assertEquals("testGenerateJavaEE14.TestBmp", entity.getEjbClass());
        assertEquals("Bean", entity.getPersistenceType());
        assertEquals("java.lang.Long", entity.getPrimKeyClass());
        assertFalse(entity.isReentrant());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestBmp.java")), 
                getGoldenFile("testGenerateJavaEE14/TestBmp.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestBmpLocal.java")), 
                getGoldenFile("testGenerateJavaEE14/TestBmpLocal.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestBmpLocalHome.java")), 
                getGoldenFile("testGenerateJavaEE14/TestBmpLocalHome.java"), 
                FileUtil.toFile(packageFileObject)
                );
        assertNull(packageFileObject.getFileObject("TestBmpRemote.java"));
        assertNull(packageFileObject.getFileObject("TestBmpRemoteHome.java"));
    }
    
}
