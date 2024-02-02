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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.ejbcore.test.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
//import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Tests for <code>EjbFacadeWizardIterator</code>.
 *
 * @author Erno Mononen
 */
public class EjbFacadeWizardIteratorTest extends TestBase {

    public EjbFacadeWizardIteratorTest(String testName) {
        super(testName);
    }


    @Override
    public void setUp() throws IOException{
        super.setUp();
        ClassPathProviderImpl cppr=(ClassPathProviderImpl) Lookup.getDefault().lookup(ClassPathProvider.class);
        cppr.setClassPath(new FileObject[]{FileUtil.toFileObject(getWorkDir())});
//        File javaxEjb = new File(getWorkDir(), "javax" + File.separator + "ejb");
//        javaxEjb.mkdirs();
//        TestUtilities.copyStringToFile(new File(javaxEjb, "Stateless.java"), "package javax.ejb; public @interface Stateless{}");
//        TestUtilities.copyStringToFile(new File(javaxEjb, "Local.java"), "package javax.ejb; public @interface Local{}");
//        TestUtilities.copyStringToFile(new File(javaxEjb, "Remote.java"), "package javax.ejb; public @interface Remote{}");
    }

    /**
     * sme problem with annotation creation
     * TODO: need additional investigation
     * @throws Exception
     */
    public void testCreateInterface() throws Exception {

        final String name = "Test";
        final String annotationType = "javax.ejb.Remote";
        final String pkgName = "foo";
        File pkg = new File(getWorkDir(), pkgName);
        pkg.mkdir();
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        String author=System.getProperty("user.name");

        String golden =
        "/*\n"+
        " * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license\n" +
        " * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template\n"+
        " */\n"+
        "\n"+
        "package " + pkgName + ";\n"+
        "\n"+
        "import " + annotationType + ";\n"+
        "\n"+
        "/**\n"+
        " *\n"+
        " * @author "+author+"\n"+
        " */\n"+
        "@" + JavaIdentifiers.unqualify(annotationType) + "\n"+
        "public interface " + name + " {\n"+
        "\n"+
        "}\n"+
        "";

        FileObject result = wizardIterator.createInterface(name, annotationType, FileUtil.toFileObject(pkg));
        assertEquals(golden, TestUtilities.copyFileObjectToString(result));
    }

    public void testAddMethodToInterface() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String originalContent =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public interface Test {\n" +
                "}";

        TestUtilities.copyStringToFile(testFile, originalContent);

        String golden =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public interface Test {\n\n" +
                "    void testMethod(Object entity);\n" +
                "}";

        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("testMethod");
        options.setReturnType("void");
        options.setParameterName("entity");
        options.setParameterType("Object");
        wizardIterator.addMethodToInterface(Collections.<GenerationOptions>singletonList(options), FileUtil.toFileObject(testFile));
        assertEquals(golden, TestUtilities.copyFileToString(testFile));

    }

    /**
     * sme problem with annotation creation
     * TODO: need additional investigation
     * @throws Exception
     */
//    public void testGenerate() throws Exception {
//        File testFile = new File(getWorkDir(), "Test.java");
//        String originalContent =
//                "package org.netbeans.test;\n\n" +
//                "import java.util.*;\n\n" +
//                "@javax.persistence.Entity\n" +
//                "public class Test {\n" +
//                "}";
//
//        final String pkgName = "foo";
//        File pkg = new File(getWorkDir(), pkgName);
//        pkg.mkdir();
//
//        TestUtilities.copyStringToFile(testFile, originalContent);
//        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
//        Set<FileObject> result = wizardIterator.generate(
//                FileUtil.toFileObject(pkg), "Test", pkgName,
//                true, true, ContainerManagedJTAInjectableInEJB.class);
//
//        assertEquals(3, result.size());
//
//        for (FileObject each : result){
//            assertFile(FileUtil.toFile(each), getGoldenFile(each.getNameExt()));
//        }
//
//    }

}
