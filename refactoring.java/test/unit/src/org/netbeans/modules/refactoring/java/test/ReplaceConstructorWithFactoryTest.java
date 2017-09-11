/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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