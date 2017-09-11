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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.java.source.gen;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests WorkingCopy.rewriteInComment
 */
public class RewriteInCommentTest extends GeneratorTestBase {

    public RewriteInCommentTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RewriteInCommentTest.class);
//        suite.addTest(new IfTest("testRewriteInComment"));
        return suite;
    }

    private void shouldThrowException(WorkingCopy wc, int offset, int length, String replaceWith) {
        boolean wasException = false;
        
        try {
            wc.rewriteInComment(offset, length, replaceWith);
        } catch (IllegalArgumentException e) {
            wasException = true;
        }
        
        assertTrue("Should throw an exception", wasException);
    }
    
    /**
     * Test replacing then statement with empty block.
     */
    public void testRewriteInComment() throws Exception {
        testFile = new File(getWorkDir(), "RewriteInCommentTest.java");        
        TestUtilities.copyStringToFile(testFile, 
            "package foo.bar; public class RewriteInCommentTest {/* test */ /** test */ //test\n /* test */ /**/ /***/ //\n}");
        String golden =
            "package foo.bar; public class RewriteInCommentTest {/* nue */ /** nue */ //nue\n /*nue*/ /*nue*/ /**nue*/ //nue\n}";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.PARSED);
                shouldThrowException(workingCopy, 44 - 14, 4, "xxxx");
                shouldThrowException(workingCopy, 91 - 14, 6, "xxxx");
                shouldThrowException(workingCopy, 66 - 14, 2, "");
                shouldThrowException(workingCopy, 67 - 14, 1, "");
                shouldThrowException(workingCopy, 74 - 14, 2, "");
                shouldThrowException(workingCopy, 77 - 14, 1, "");
                shouldThrowException(workingCopy, 78 - 14, 1, "");
                shouldThrowException(workingCopy, 79 - 14, 1, "");
                shouldThrowException(workingCopy, 85 - 14, 2, "");
                shouldThrowException(workingCopy, 89 - 14, 2, "");
                shouldThrowException(workingCopy, 90 - 14, 2, "");
                
                workingCopy.rewriteInComment(69 - 14, 4, "nue");
                workingCopy.rewriteInComment(81 - 14, 4, "nue");
                workingCopy.rewriteInComment(91 - 14, 4, "nue");
                
                workingCopy.rewriteInComment(99 - 14, 6, "nue");
                
                workingCopy.rewriteInComment(110 - 14, 0, "nue");
                workingCopy.rewriteInComment(116 - 14, 0, "nue");
                workingCopy.rewriteInComment(121 - 14, 0, "nue");
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLastRewriteInComment() throws Exception {
        File f = new File(getWorkDir(), "TestClass.java");
        String code = "package foo;\n" +
                      "public class TestClass{\n" +
                      "   public void foo() {\n" +
                      "   }\n" +
                      "}//test";
        TestUtilities.copyStringToFile(f, code);
        FileObject fo = FileUtil.toFileObject(f);
        JavaSource javaSource = JavaSource.forFileObject(fo);
        ModificationResult mr = javaSource.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);

                TokenSequence ts = copy.getTokenHierarchy().tokenSequence();

                ts.moveEnd();
                assertTrue(ts.movePrevious());

                int off = ts.offset() + ts.token().length();

                copy.rewriteInComment(off - "test".length(), "test".length(), "foo");
            }
        });

        assertEquals(code.replaceAll("test", "foo"), mr.getResultingSource(fo));
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
