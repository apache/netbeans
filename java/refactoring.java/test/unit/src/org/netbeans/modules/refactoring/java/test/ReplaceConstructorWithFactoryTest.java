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

package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.ReplaceConstructorWithFactoryRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class ReplaceConstructorWithFactoryTest extends RefTestBase {

    public ReplaceConstructorWithFactoryTest(String name) {
        super(name);
    }
    
    public void testReplaceGenericWithFactory() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test<T> {\n public Test(int i, java.util.List<String> aa) {}\n private void t() {\n Test<String> t = new Test<String>(1, null);\n }\n }\n"),
                                 new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test<Boolean> t = new Test<Boolean>(-1, ll); } }")
                                 );

        performTest("create");

        assertContent(src,
                      new File("test/Test.java", "package test; public class Test<T> { public static <T> Test<T> create(int i, java.util.List<String> aa) { return new Test<T>(i, aa); } private Test(int i, java.util.List<String> aa) {} private void t() { Test<String> t = Test.<String>create(1, null); } } "),
                      new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test<Boolean> t = Test.<Boolean>create(-1, ll); } }")
                      //new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")
                     );
    }

    public void testReplaceWithFactory() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public Test(int i, java.util.List<String> aa) {}\n private void t() {\n Test t = new Test(1, null);\n }\n }\n"),
                                 new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = new Test(-1, ll); } }")
                                 );

        performTest("create");

        assertContent(src,
                      new File("test/Test.java", "package test; public class Test { public static Test create(int i, java.util.List<String> aa) { return new Test(i, aa); } private Test(int i, java.util.List<String> aa) {} private void t() { Test t = Test.create(1, null); } } "),
                      new File("test/Use.java", "package test; public class Use { private void t(java.util.List<String> ll) { Test t = Test.create(-1, ll); } }")
                      //new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")
                     );
    }


    private void performTest(final String factoryName) throws Exception {
        final ReplaceConstructorWithFactoryRefactoring[] r = new ReplaceConstructorWithFactoryRefactoring[1];
        FileObject testFile = src.getFileObject("test/Test.java");
        
        JavaSource.forFileObject(testFile).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree var = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(0);

                TreePath tp = TreePath.getPath(cut, var);
                r[0] = new ReplaceConstructorWithFactoryRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setFactoryName(factoryName);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        Thread.sleep(1000);
        r[0].prepare(rs);
        rs.doRefactoring(true);

        IndexingManager.getDefault().refreshIndex(src.getURL(), null);
        SourceUtils.waitScanFinished();
        //assertEquals(false, TaskCache.getDefault().isInError(src, true));
    }

}