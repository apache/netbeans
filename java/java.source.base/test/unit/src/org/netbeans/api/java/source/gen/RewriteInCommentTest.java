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
        //System.err.println(res);
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

        assertEquals(code.replace("test", "foo"), mr.getResultingSource(fo));
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
