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

import com.sun.source.tree.CompilationUnitTree;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.junit.NbTestSuite;

/**
 * Makes source changes in broken sources.
 * 
 * @author Pavel Flaska
 */
public class BrokenSourceTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of BrokenSourceTest 
     
     * @param name  test name
     */
    public BrokenSourceTest(String name) {
        super(name);
    }
    
    /**
     * Return suite.
     * 
     * @return  suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(BrokenSourceTest.class);
//        suite.addTest(new BrokenSourceTest("testAddImportWhenClosingCurlyMissing"));
        return suite;
    }

    /**
     * Regression test for #97901.
     * 
     * @throws java.lang.Exception 
     */
    public void testAddImportWhenClosingCurlyMissing() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class User {\n" +
            "\n" +
            "    public User(Object node) {\n" +
            "        if (node instanceof Object) {\n" +
            "        } else if (node instanceof ArrayList)\n" +
            "        } else if (node instanceof LinkedList) {\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        System.err.println(\"nafink\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "public class User {\n" +
            "\n" +
            "    public User(Object node) {\n" +
            "        if (node instanceof Object) {\n" +
            "        } else if (node instanceof ArrayList)\n" +
            "        } else if (node instanceof LinkedList) {\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    void method() {\n" +
            "        System.err.println(\"nafink\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree cutCopy = make.addCompUnitImport(
                       cut, 
                       make.Import(make.Identifier("java.util.ArrayList"), false)
                );
                workingCopy.rewrite(cut, cutCopy);
            }

        };
        src.runModificationTask(task).commit();
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
