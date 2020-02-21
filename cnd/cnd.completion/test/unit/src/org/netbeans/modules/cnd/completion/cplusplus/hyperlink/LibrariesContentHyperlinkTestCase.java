/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Test case for hyperlink to library content
 *
 */
public class LibrariesContentHyperlinkTestCase extends HyperlinkBaseTestCase {

    public LibrariesContentHyperlinkTestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        // we have following structure for this test
        // test-folder
        //  --src\
        //        main.cc
        //  --sys_include1\
        //        include1.h
        //  --sys_include2\
        //        include2.h
        //
        // so, adjust used folders

        File srcDir = new File(projectDir, "src");
        File incl1 = new File(projectDir, "sys_include");
        File incl2 = new File(projectDir, "sys_include2");
        checkDir(srcDir);
        checkDir(incl1);
        checkDir(incl2);
        List<String> sysIncludes = Arrays.asList(incl1.getAbsolutePath(), incl2.getAbsolutePath());
        super.setSysIncludes(srcDir.getAbsolutePath(), sysIncludes);
        List<String> usrIncludes = Arrays.asList(srcDir.getAbsolutePath());
        super.setUsrIncludes(srcDir.getAbsolutePath(), usrIncludes);
        return new File[] {srcDir};
    }

    public void testTwoLevelsStructRedirection() throws Exception {
        performTest("src/format1.c", 17, 15, "src/format1.c", 7, 5);
        performTest("src/format1.c", 18, 15, "src/format1.c", 8, 5);
        performTest("src/format1.c", 19, 15, "src/format1.c", 12, 5);
        performTest("src/format1.c", 20, 15, "src/format1.c", 13, 5);
        
        performTest("src/format2.c", 17, 15, "src/format2.c", 7, 5);
        performTest("src/format2.c", 18, 15, "src/format2.c", 8, 5);
        performTest("src/format2.c", 19, 15, "src/format2.c", 12, 5);
        performTest("src/format2.c", 20, 15, "src/format2.c", 13, 5);
    }

    public void testIZ157797() throws Exception {
        // IZ#157797: no hyperlink on macro reference
        performTest("src/macro_incl_ok.h", 2, 25, "src/macro_def.cc", 3, 1);
        performNullTargetTest("src/macro_incl_err.h", 2, 25);
    }

    public void testStructFromSystemDir() throws Exception {
        performTest("src/testTdClassFwdResolve1.c", 7, 15, "sys_include2/addrinfo.h", 5, 5);
    }

    public void testStructFromUserDir() throws Exception {
        performTest("src/testTdClassFwdResolve2.c", 7, 12, "src/audio_format.h", 4, 5);
        performTest("src/testTdClassFwdResolve2.c", 8, 20, "src/audio_format.h", 4, 5);
    }

    public void testDuplicationConstructions_0() throws Exception {
        // IZ#145982: context of code changes unexpectedly
        performTest("src/testDup1.cc", 5, 15, "src/dup1.h", 12, 5); // duplicationFoo
        performTest("src/testDup1.cc", 7, 15, "src/dup1.h", 5, 5); // classElementDup
        performTest("src/testDup1.cc", 4, 10, "src/dup1.h", 10, 1); // Duplication
        performTest("src/testDup1.cc", 6, 10, "src/dup1.h", 3, 1); // ElementDup
    }

    public void testDuplicationConstructions_1() throws Exception {
        // IZ#145982: context of code changes unexpectedly
        performTest("src/testSys1Dup.cc", 5, 15, "sys_include/sys1dup.h", 4, 5); // duplicationSys1
        performTest("src/testSys1Dup.cc", 7, 15, "sys_include/sys1dup.h", 11, 5); // structMethod
        performTest("src/testSys1Dup.cc", 4, 10, "sys_include/sys1dup.h", 2, 1); // Duplication
        performTest("src/testSys1Dup.cc", 6, 10, "sys_include/sys1dup.h", 10, 1); // ElementDup
    }

    public void testDuplicationConstructions_2() throws Exception {
        // IZ#145982: context of code changes unexpectedly
        performTest("src/testSys2Dup.cc", 5, 15, "sys_include2/sys2dup.h", 4, 5); // duplicationSys2
        performTest("src/testSys2Dup.cc", 7, 15, "sys_include2/sys2dup.h", 4, 5); // duplicationSys2
        performTest("src/testSys2Dup.cc", 4, 10, "sys_include2/sys2dup.h", 2, 1); // Duplication
        performTest("src/testSys2Dup.cc", 6, 10, "sys_include2/sys2dup.h", 9, 1); // ElementDup
    }

    public void testTypedefClassFwd() throws Exception {
        // IZ#146289: REGRESSTION: inaccuracy tests show significant regressions
        performTest("src/testTdClassFwdResolve.cc", 5, 25, "src/outer.h", 3, 5); // outerFunction
    }

    public void testLibraryClass() throws Exception {
        performTest("src/main.cc", 7, 6, "sys_include2/include2.h", 9, 1);
    }

    public void testLibraryClassConstructor() throws Exception {
        // IZ 137971 : library class name after "new" is not resolved
        performTest("src/main.cc", 7, 20, "sys_include2/include2.h", 9, 1);
    }

    public void testLibraryClassConstructor2() throws Exception {
        performTest("src/main.cc", 9, 20, "sys_include/include1.h", 12, 5);
    }

    public void testNsAliases() throws Exception {
        // IZ 131914: Code completion should work for namespace aliases
        performTest("src/main.cc", 18, 16, "sys_include/include1.h", 32, 5);
        performTest("src/main.cc", 19, 16, "src/include.h", 4, 5);
    }

    public void testNamespaceOverride() throws Exception {
        // Main project has namespace std overriding std from library.
        // This should not break hyperlinks for original std members.
        performTest("src/main.cc", 20, 13, "sys_include/include1.h", 37, 1);
    }

    public void testGlobalNamespaceInLibrary() throws Exception {
        // Library has declaration of size_t and namespace std with
        // "using ::size_t". For hyperlink to work in this declaration
        // the global namespace must be resolved in library project,
        // not in the main project.
        performTest("sys_include/include1.h", 40, 15, "sys_include/include1.h", 37, 1);
    }

    public void testEndl() throws Exception {
        performTest("src/main2.cc", 7, 8, "sys_include/iostream_ours", 20, 5);
        performTest("src/main2.cc", 7, 26, "sys_include/iostream_ours", 14, 5);
    }

    public void testNamespaceInDifferentFolders() throws Exception {
        performTest("src/main.cc", 26, 8, "sys_include/include1.h", 44, 5);
        performTest("src/main.cc", 27, 8, "sys_include2/include2.h", 28, 5);
    }

    public void testIZ140787_cout() throws Exception {
        // iz #140787 cout, endl unresolved in some Loki files
        performTest("src/iz140787_cout.cc", 9, 9, "sys_include/include1.h", 44, 5);
        performTest("src/iz140787_cout.cc", 10, 10, "sys_include2/include2.h", 28, 5);
    }

    public void testQtUsage() throws Exception {
        // IZ#155122: Completion doesn't work for Qt examples
        performTest("src/qt_usage.cc", 8, 25, "sys_include/QtDecls.h", 4, 5);
        performTest("src/qt_usage.cc", 12, 25, "sys_include2/QObject.h", 4, 5);
        performTest("sys_include/QtDecls.h", 2, 30, "sys_include2/QObject.h", 2, 1);
    }

    public void test154851() throws Exception {
        // IZ#154851 : Code completion (assistant) failed if using forward reference
        performTest("src/iz154851.cc", 6, 9, "sys_include/iz154851_2.h", 2, 1);
        performTest("src/iz154851.cc", 7, 7, "sys_include/iz154851_2.h", 4, 5);
    }

    public void test160829() throws Exception {
        // IZ#160829 : [code model, navigation] Unresolved types
        performTest("sys_include/iz160829_2.h", 4, 11, "sys_include2/iz160829.h", 2, 1);
    }

    public void test167200() throws Exception {
        // IZ#167200: Class forward declaration is confusing code completion
        performTest("src/iz154851.h", 4, 10, "src/iz167200.h", 2, 1);
        performTest("src/iz154851.h", 2, 10, "sys_include/iz154851_2.h", 2, 1);
        performTest("src/iz154851.cc", 6, 10, "sys_include/iz154851_2.h", 2, 1);
        performTest("src/iz154851.cc", 8, 10, "src/iz167200.h", 2, 1);
    }

    public void test175505() throws Exception {
        // IZ#175505 : Unable to resolve namespace
        performTest("src/iz175505.cc", 10, 38, "sys_include/iz175505.h", 3, 5);
        performTest("src/iz175505.cc", 13, 6, "sys_include/iz175505.h", 4, 9);
    }
    
    public void testBug179048() throws Exception {
        // Bug 179048 - Unable to resolve using of global function in namespace
        performTest("src/bug179048.cpp", 10, 22, "src/bug179048.cpp", 2, 1);
    }
    
    public void testBug229990() throws Exception {
        // Bug 229990 - stability tests: unresolved identifier in clucene-core-0.9.11
        performTest("src/bug229990.cpp", 8, 35, "sys_include/sys_stat_h.h", 5, 1);
        performTest("src/bug229990.cpp", 10, 50, "sys_include/sys_stat_h.h", 7, 9);
        performTest("src/bug229990.cpp", 13, 40, "src/bug229990.h", 15, 5);
        performTest("src/bug229990.cpp", 12, 35, "src/bug229990.h", 14, 1);
    }    
    
    public void testBug244777() throws Exception {
        // Bug 244777 - Code assistance sometimes flags valid C++11 as errors
        performTest("src/bug244777.cpp", 15, 32, "src/bug244777.cpp", 6, 9);
    }    
    
    public void testBug257032() throws Exception {
        performTest("src/bug257032.cpp", 6, 10, "sys_include/bug257032.h", 2, 3);
    }
    
    public void testBug257028() throws Exception {
        performTest("src/bug257028.cpp", 9, 18, "sys_include2/bug257028_2.h", 5, 17);
    }
    
    public void testBug267668() throws Exception {
        performTest("src/bug267668.cpp", 7, 18, "sys_include2/bug267668.h", 6, 9);
    }
    
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class<?> getTestCaseDataClass() {
            return BasicHyperlinkTestCase.class;
        }

        public Failed(String testName) {
            super(testName, true);
        }
    }
}
