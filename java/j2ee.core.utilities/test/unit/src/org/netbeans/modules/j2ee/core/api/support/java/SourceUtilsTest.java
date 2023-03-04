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

package org.netbeans.modules.j2ee.core.api.support.java;

import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea, Martin Adamek
 */
public class SourceUtilsTest extends NbTestCase {

    private FileObject testFO;

    public SourceUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class/*, RepositoryImpl.class*/);

        clearWorkDir();
        TestUtilities.setCacheFolder(getWorkDir());
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }

    public void testGetPublicTopLevelElement() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}" +
                "class AnotherClass {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertTrue(typeElement.getQualifiedName().contentEquals("foo.TestClass"));
            }
        });

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class AnotherClass {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                assertNull(SourceUtils.getPublicTopLevelElement(controller));
            }
        });
    }

    public void testGetNoArgConstructor() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() {" +
                "   }" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                ExecutableElement constructor = SourceUtils.getNoArgConstructor(controller, typeElement);
                assertNotNull(constructor);
                assertFalse(controller.getElementUtilities().isSynthetic(constructor));
            }
        });

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertNull(SourceUtils.getNoArgConstructor(controller, typeElement));
            }
        });
    }

    public void testIsSubtype() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass implements java.io.Serializable {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertTrue(SourceUtils.isSubtype(controller, typeElement, "java.io.Serializable"));
                assertFalse(SourceUtils.isSubtype(controller, typeElement, "java.lang.Cloneable"));
            }
        });
    }

    public void testIsSubtypeGenerics() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "import java.util.Enumeration;" +
                "public class TestClass implements Enumeration<String> {" +
                "    public boolean hasMoreElement() {" +
                "        return false;" +
                "    }" +
                "    public String nextElement() {" +
                "        return null;" +
                "    }" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertTrue(SourceUtils.isSubtype(controller, typeElement, "java.util.Enumeration<String>"));
                assertFalse(SourceUtils.isSubtype(controller, typeElement, "java.util.Enumeration<Object>"));
            }
        });
    }

    private static void runUserActionTask(FileObject javaFile, final Task<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(Phase.RESOLVED);
                taskToTest.run(controller);
            }
        }, true);
    }
}
