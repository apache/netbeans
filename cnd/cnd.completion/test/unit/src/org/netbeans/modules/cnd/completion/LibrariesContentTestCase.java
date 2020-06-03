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
package org.netbeans.modules.cnd.completion;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 *159170
 */
public class LibrariesContentTestCase extends CompletionBaseTestCase {
    
    public LibrariesContentTestCase(String testName) {
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
        return new File[] {srcDir};
    }

    public void testGlobalFunsWithPrefix_1() throws Exception {
        super.performTest("src/main.cc", 6, 5, "f");
    }
    
    public void testGlobalFunsWithPrefix_2() throws Exception {
        super.performTest("src/main.cc", 6, 5, "::f");
    }    

    public void testLibraryClassStaticFunctions_1() throws Exception {
        super.performTest("src/main.cc", 6, 5, "AAA::f");
    }
    
    public void testLibraryClassStaticFunctions_2() throws Exception {
        super.performTest("src/main.cc", 6, 5, "BBB::f");
    }    

    public void testMergeOfLibrariesNamespaces() throws Exception {
        super.performTest("src/main.cc", 6, 5, "common::decl_from");
        super.performTest("src/include_sys2_file.h", 5, 5, "common::decl_from");
        super.performTest("src/include_sys1_file.h", 5, 5, "common::decl_from");
    }

    public void testStdSizeT() throws Exception {
        super.performTest("src/main.cc", 13, 5, "ns_sttdd::s");
    }
    
    public void test202486_NS1() throws Exception {
        super.performTest("src/iz202486.cc", 11, 5, "NS1::");
    }

    public void test202486_NS2() throws Exception {
        super.performTest("src/iz202486.cc", 11, 5, "NS1::NS2::");
    }

    public void test202486_NS3() throws Exception {
        super.performTest("src/iz202486.cc", 11, 5, "NS1::NS3::");
    }

    public void test202486_NS4() throws Exception {
        super.performTest("src/iz202486.cc", 11, 5, "NS1::NS4::");
    }

    public void test202486_NS5() throws Exception {
        super.performTest("src/iz202486.cc", 11, 5, "NS5::");
    }

    public void test202486_NS51() throws Exception {
        super.performTest("src/iz202486.cc", 11, 5, "NS5::NS51::");
    }
    
    public void testBug254671_1() throws Exception {
        super.performTest("src/bug254671.cpp", 5, 23);
    }
    
    public void testBug254671_2() throws Exception {
        super.performTest("src/bug254671.cpp", 7, 10);
    }
    
    public void testBug254671_3() throws Exception {
        super.performTest("src/bug254671.cpp", 8, 10);
    }

//    public void testStdSizeTGlob() throws Exception {
//        super.performTest("sys_include/sys1_incl_sys2.h", 5, 14);
//    }
}
