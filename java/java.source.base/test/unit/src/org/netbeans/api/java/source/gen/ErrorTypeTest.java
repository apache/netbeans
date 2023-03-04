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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Tests correct error types.
 */
public class ErrorTypeTest extends GeneratorTestMDRCompat {

    public ErrorTypeTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ErrorTypeTest.class);
        return suite;
    }

    public void testType() throws Exception {
        File libSrc = new File(getWorkDir(), "libsrc");
        libSrc.mkdirs();
        File lib = new File(libSrc, "Lib.java");
        TestUtilities.copyStringToFile(lib,
            "package lib;\n" +
            "public class Lib extends Base {\n" +
            "}\n" +
            "class Base {\n" +
            "}\n"
            );
        File libClass = new File(getWorkDir(), "libclass");
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        try (StandardJavaFileManager fm = javaCompiler.getStandardFileManager(null, null, null)) {
            assertTrue(javaCompiler.getTask(null, fm, null, Arrays.asList("-d", libClass.getAbsolutePath(), "-source", "8"), null, fm.getJavaFileObjects(lib))
                                   .call());
        }

        Files.delete(libClass.toPath().resolve("lib").resolve("Base.class"));
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import lib.Base;\n\n" +
            "public class Test extends Base {\n" +
            "}\n";
        ClassPath compile = ClassPathSupport.createClassPath(libClass.toURI().toURL());

        MockLookup.setInstances(new ClassPathProvider() {
            @Override
            public ClassPath findClassPath(FileObject file, String type) {
                if (ClassPath.COMPILE.equals(type) && FileUtil.toFileObject(testFile).equals(file)) {
                    return compile;
                }
                return null;
            }
        });

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            TypeElement type = workingCopy.getElements().getTypeElement("lib.Lib");
            assertNotNull(type);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            workingCopy.rewrite(clazz, make.setExtends(clazz, (ExpressionTree) make.Type(type.getSuperclass())));
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }


}
