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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

    private static final Set<String> MODULAR_TESTS = new HashSet<>(Arrays.asList("testModuleOpens"));

    public RenamePackageTest(String name) {
        super(name, MODULAR_TESTS.contains(name) ? "9" : "1.8");
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
    
    public void testModuleOpens() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("module-info.java", "module m {\n"
                + "exports t;\n"
                + "opens t;\n"
                + "}"),
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"));
        performRenameFolder(src.getFileObject("t"), "u", false);
        verifyContent(src,
                new File("module-info.java", "module m {\n"
                + "exports u;\n"
                + "opens u;\n"
                + "}"),
                new File("u/A.java", "package u;\n"
                + "public class A {\n"
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
