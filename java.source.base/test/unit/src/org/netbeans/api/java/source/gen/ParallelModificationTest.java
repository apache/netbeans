/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
            System.err.println(res);
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
