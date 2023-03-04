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
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

import static org.netbeans.api.java.source.gen.GeneratorTestMDRCompat.getJavaSource;

/**
 * This test produces the defect described in issue #227677.
 * @author sdedic
 */
public class ParallelModificationTest extends GeneratorTestBase {

    /** Creates a new instance of ClassMemberTest */
    public ParallelModificationTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ParallelModificationTest.class);
//        suite.addTest(new ParallelModificationTest("testClassRename"));
//        suite.addTest(new ParallelModificationTest("testClassRename"));
//        suite.addTest(new ParallelModificationTest("testEnumRename"));
        return suite;
    }
    

    public void testClassRename() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "// very very very long comment, which will be removed and the edit operation will fail miserably\n" +
            "// very very very long comment, which will be removed and the edit operation will fail miserably\n" +
            "// very very very long comment, which will be removed and the edit operation will fail miserably\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test1234567890 {\n" +
            "    Test1234567890(int a, long c, String s) {\n" +
            "    }\n" +
            "\n" +
            "    Test1234567890() {}\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class T {\n" +
            "    T(int a, long c, String s) {\n" +
            "    }\n" +
            "\n" +
            "    T() {}\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        FileObject fob = FileUtil.toFileObject(testFile);
        DataObject dob = DataObject.find(fob);
        EditorCookie edit = dob.getCookie(EditorCookie.class);
        final Document doc = edit.openDocument();
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree copy = make.setLabel((ClassTree) typeDecl, "T");
                        workingCopy.rewrite(typeDecl, copy);
                    }
                }
                try {
                    doc.remove(30, 321-30);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        };
        try {
            src.runModificationTask(task).commit();
            /*
            String res = TestUtilities.copyFileToString(testFile);
            //System.err.println(res);
            assertEquals(golden, res);
            */
            fail("Exception expected");
        } catch (IOException ex) {
            // thi sis expected
        }
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
