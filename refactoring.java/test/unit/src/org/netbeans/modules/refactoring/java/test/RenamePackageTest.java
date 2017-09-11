/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import static org.netbeans.modules.refactoring.java.test.RefactoringTestBase.addAllProblems;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author ralph
 */
public class RenamePackageTest extends RefactoringTestBase {

    public RenamePackageTest(String name) {
        super(name, "1.8");
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
    
    public void testRenameCasePackage() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"));
        performRenameFolder(src.getFileObject("t"), "T", false);
        verifyContent(src,
                new File("T/A.java", "package T;\n"
                + "public class A {\n"
                + "}"));
    }
    
    public void testRenameJavadoc() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "/**\n"
                        + " * @see t.B\n"
                        + " */\n"
                        + "public class A {\n"
                        + "}"),
                new File("t/B.java", "package t;\n"
                        + "public class B {\n"
                        + "}"));
        performRenameFolder(src.getFileObject("t"), "v", true);
        verifyContent(src,
                new File("v/A.java", "package v;\n"
                        + "/**\n"
                        + " * @see v.B\n"
                        + " */\n"
                        + "public class A {\n"
                        + "}"),
                new File("v/B.java", "package v;\n"
                        + "public class B {\n"
                        + "}"));
    }
    
    public void testRenameJavadoc2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "/**\n"
                        + " * @see t\n"
                        + " */\n"
                        + "public class A {\n"
                        + "}"),
                new File("t/B.java", "package t;\n"
                        + "public class B {\n"
                        + "}"));
        performRenameFolder(src.getFileObject("t"), "v", true);
        verifyContent(src,
                new File("v/A.java", "package v;\n"
                        + "/**\n"
                        + " * @see v\n"
                        + " */\n"
                        + "public class A {\n"
                        + "}"),
                new File("v/B.java", "package v;\n"
                        + "public class B {\n"
                        + "}"));
    }
    
    public void test218766() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"));
        writeFilesAndWaitForScan(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "}"));
        performRenameFolder(src.getFileObject("t"), "u", false);
        verifyContent(src,
                new File("u/A.java", "package u;\n"
                + "public class A {\n"
                + "}"));
        verifyContent(test,
                new File("t/ATest.java", "package t;\n"
                + "import junit.framework.TestCase;\n"
                + "\n"
                + "public class ATest extends TestCase {\n"
                + "}"));
    }
    
        
    private void performRenameFolder(FileObject source, final String newname, boolean searchInComments, Problem... expectedProblems) throws Exception {
        final RenameRefactoring[] r = new RenameRefactoring[1];
        r[0] = new RenameRefactoring(Lookups.singleton(source));
        r[0].setNewName(newname);
        r[0].setSearchInComments(searchInComments);
        RefactoringSession rs = RefactoringSession.create("Rename");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
}
