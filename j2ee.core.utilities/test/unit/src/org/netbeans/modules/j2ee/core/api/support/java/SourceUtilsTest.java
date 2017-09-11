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
