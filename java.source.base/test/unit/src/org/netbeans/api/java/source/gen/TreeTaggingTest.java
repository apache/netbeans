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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source.gen;

import java.io.File;
import java.util.Collections;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Unit tests for Tree tagging / span suport
 *
 * @author Max Sauer
 */
public class TreeTaggingTest extends GeneratorTestMDRCompat {

    public TreeTaggingTest(String name) {
        super(name);
    }

    /**
     * Adds 'System.err.println(true);' statement to the method body,
     * tags the tree and checks the marks are valid
     */
    public void testTaggingOfGeneratedMethodBody() throws Exception {

        // the tag
        final String methodBodyTag = "mbody"; //NOI18N

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    }\n" +
            "}\n"
            );

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(Boolean.TRUE)
                        )
                    )
                );
                //tag
                workingCopy.tag(statement, methodBodyTag);

                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }

        };
        ModificationResult diff = testSource.runModificationTask(task);
        diff.commit();
        int[] span = diff.getSpan(methodBodyTag);
        int delta = span[1] - span[0];
        //lenghth of added statement has to be the same as the length of span
        assertEquals(delta, new String("System.err.println(true);").length());
        //absolute position of span beginning
        assertEquals(115, span[0]);

        assertEquals("System.err.println(true);", diff.getResultingSource(FileUtil.toFileObject(testFile)).substring(span[0], span[1]));
    }

    /**
     * Adds 'System.err.println(true);' statement to the method body,
     * tags the tree and checks the marks are valid inside an inner class
     */
    public void testTaggingOfGeneratedMethodBodyInInnerClass() throws Exception {

        // the tag
        final String methodBodyTag = "mbody"; //NOI18N

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    class foo {\n" +
            "        public void taragui() {\n" +
            "            ;\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                // finally, find the correct body and rewrite it.
                ClassTree topClazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ClassTree clazz = (ClassTree) topClazz.getMembers().get(1);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(Boolean.TRUE)
                        )
                    )
                );
                //tag
                workingCopy.tag(statement, methodBodyTag);

                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }

        };
        ModificationResult diff = testSource.runModificationTask(task);
        diff.commit();
        int[] span = diff.getSpan(methodBodyTag);
        int delta = span[1] - span[0];
        //lenghth of added statement has to be the same as the length of span
        assertEquals(delta, new String("System.err.println(true);").length());
        //absolute position of span beginning
        assertEquals(143, span[0]);

        assertEquals("System.err.println(true);", diff.getResultingSource(FileUtil.toFileObject(testFile)).substring(span[0], span[1]));
    }

    /**
     * Adds 'System.err.println(true);' statement to the method body,
     * tags the tree and checks the marks are valid inside an annonymous class
     */
    public void testTaggingOfGeneratedMethodBodyInAnnonymousClass() throws Exception {

        // the tag
        final String methodBodyTag = "mbody"; //NOI18N

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void foo() {\n" +
            "        new Runnable() {\n" +
            "            public void run() {\n" +
            "                ;\n" +
            "            }\n" +
            "        };\n" +
            "    }" +
            "}\n"
            );

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                // finally, find the correct body and rewrite it.
                ClassTree topClazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                MethodTree meth = (MethodTree) topClazz.getMembers().get(1);
                NewClassTree clazz = (NewClassTree) ((ExpressionStatementTree) meth.getBody().getStatements().get(0)).getExpression();
                MethodTree method = (MethodTree) clazz.getClassBody().getMembers().get(1);
                ExpressionStatementTree statement = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "err"
                            ),
                            "println"
                        ),
                        Collections.singletonList(
                            make.Literal(Boolean.TRUE)
                        )
                    )
                );
                //tag
                workingCopy.tag(statement, methodBodyTag);

                BlockTree copy = make.addBlockStatement(method.getBody(), statement);
                workingCopy.rewrite(method.getBody(), copy);
            }

        };
        ModificationResult diff = testSource.runModificationTask(task);
        diff.commit();
        int[] span = diff.getSpan(methodBodyTag);
        int delta = span[1] - span[0];
        //lenghth of added statement has to be the same as the length of span
        assertEquals(delta, new String("System.err.println(true);").length());
        //absolute position of span beginning
        assertEquals(184, span[0]);

        assertEquals("System.err.println(true);", diff.getResultingSource(FileUtil.toFileObject(testFile)).substring(span[0], span[1]));
    }


    public void testTaggingOfSuperCall() throws Exception {

        // the tag
        final String methodBodyTag = "mbody"; //NOI18N

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**/" +
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        ;\n" +
            "    }\n" +
            "}\n"
            );

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                // finally, find the correct body and rewrite it.
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);

                MethodInvocationTree inv = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("super"), "print"), Collections.<ExpressionTree>emptyList()); //NOI18N
                ReturnTree ret = make.Return(inv);

                //tag
                workingCopy.tag(ret, methodBodyTag);

                BlockTree copy = make.addBlockStatement(method.getBody(), ret);
                workingCopy.rewrite(method.getBody(), copy);
            }

        };
        ModificationResult diff = testSource.runModificationTask(task);
        diff.commit();

        int[] span = diff.getSpan(methodBodyTag);
        int delta = span[1] - span[0];
        //lenghth of added statement has to be the same as the length of span
        assertEquals(delta, new String("return super.print();").length());
        //absolute position of span beginning
        assertEquals(119, span[0]);

        assertEquals("return super.print();", diff.getResultingSource(FileUtil.toFileObject(testFile)).substring(span[0], span[1]));
    }

    public void testAnnotationTagging() throws Exception {
        // the tag
        final String tag = "tag"; //NOI18N

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );

        FileObject testFileObject = FileUtil.toFileObject(testFile);
        JavaSource testSource = JavaSource.forFileObject(testFileObject);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                String annotations = "@A @B({@C, @D, @E}) @F";
                String toParse = "new Object() {" + annotations + " void test() {} }";
                NewClassTree nct = (NewClassTree) workingCopy.getTreeUtilities().parseExpression(toParse, new SourcePositions[1]);
                MethodTree method = ((MethodTree) nct.getClassBody().getMembers().get(0));

                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));

                Tree toTag = ((NewArrayTree) method.getModifiers().getAnnotations().get(1).getArguments().get(0)).getInitializers().get(1);

                workingCopy.tag(toTag, tag);
            }

        };
        ModificationResult diff = testSource.runModificationTask(task);
        diff.commit();

        int[] span = diff.getSpan(tag);
        String newCode = diff.getResultingSource(testFileObject);

        assertNotNull(span);
        assertEquals("@D", newCode.substring(span[0], span[1]));
    }

    @Override
    String getGoldenPckg() {
        return "";
    }

    @Override
    String getSourcePckg() {
        return "";
    }
}
