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
import java.util.Collections;

import org.openide.filesystems.FileUtil;

import com.sun.source.tree.*;

import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;

import org.netbeans.junit.NbTestSuite;

/**
 * Regression tests.
 * 
 * @author Pavel Flaska
 */
public class DuplicatedCommentsTest extends GeneratorTestMDRCompat {

    public DuplicatedCommentsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(DuplicatedCommentsTest.class);
//        suite.addTest(new DuplicatedCommentsTest("testLineAtTopLevel"));
        return suite;
    }
    
    public void testLineAtTopLevela() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package tohle;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "// TODO:\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "public class NewClass {\n" +
            "    \n" +
            "    public NewClass() {\n" +
            "        List l = new ArrayList();\n" +
            "    }\n" +
            "}\n");
        String golden = 
            "package tohle;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.List;\n" +
            "\n" +
            "// TODO:\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "public class NewClass {\n" +
            "    \n" +
            "    public NewClass() {\n" +
            "        List l = new ArrayList();\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                // hucky staff, correct memberSelectTree should be provided.
                // for testing reason this hacky stuff is enough.
                ImportTree importt = make.Import(make.Identifier("java.util.ArrayList"), false);
                CompilationUnitTree copy = make.insertCompUnitImport(cut, 0, importt);
                workingCopy.rewrite(cut, copy);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLineAtTopLevelb() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package tohle;\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "// XXX:\n" +
            "public class OldClass {\n" +
            "    \n" +
            "}\n");
        String golden = 
            "package tohle;\n" +
            "\n" +
            "/**\n" +
            " * Alois\n" +
            " */\n" +
            "// XXX:\n" +
            "public class NewClass {\n" +
            "    \n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                Tree type = workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(type, make.setLabel(type, "NewClass"));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testLineAtMethoda() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package tohle;\n"
                + "\n"
                + "public class OldClass {\n"
                + "    /**\n"
                + "     * Alois\n"
                + "     */\n"
                + "    // XXX:\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "}\n");
        String golden
                = "package tohle;\n"
                + "\n"
                + "public class OldClass {\n"
                + "    /**\n"
                + "     * Alois\n"
                + "     */\n"
                + "    // XXX:\n"
                + "    public void bar() {\n"
                + "    }\n"
                + "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                Tree foo = ((ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0)).getMembers().get(1);
                workingCopy.rewrite(foo, make.setLabel(foo, "bar"));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
