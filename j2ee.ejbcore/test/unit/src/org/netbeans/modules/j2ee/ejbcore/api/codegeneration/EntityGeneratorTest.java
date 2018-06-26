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
