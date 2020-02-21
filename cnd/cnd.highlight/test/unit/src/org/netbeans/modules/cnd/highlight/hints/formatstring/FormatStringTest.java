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
package org.netbeans.modules.cnd.highlight.hints.formatstring;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.highlight.error.ErrorHighlightingBaseTestCase;

/**
 * Tests for the format string checks
 */
public class FormatStringTest extends ErrorHighlightingBaseTestCase {
    
    public FormatStringTest(String testName) {
        super(testName);
    }
    
    @Override
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        File srcDir = new File(projectDir, "src"); // NOI18N
        File incl1 = new File(projectDir, "sys_includes"); // NOI18N
        List<String> sysIncludes = Arrays.asList(incl1.getAbsolutePath());
        super.setSysIncludes(srcDir.getAbsolutePath(), sysIncludes);
        return new File[]{srcDir};
    }
    
    public void testGeneralPositive() throws Exception {
        performStaticTest("src/general_positive_test.cpp"); // NOI18N
    }
    
    public void testGeneralNegative() throws Exception {
        performStaticTest("src/general_negative_test.cpp"); // NOI18N
    }
    
    public void testBug254435() throws Exception {
        performStaticTest("src/bug254435.c"); // NOI18N
    }
    
    public void testBug254472() throws Exception {
        performStaticTest("src/bug254472.c"); // NOI18N
    }
    
    public void testBug254469() throws Exception {
        performStaticTest("src/bug254469.cpp"); // NOI18N
    }
    
    public void testBug254476() throws Exception {
        performStaticTest("src/bug254476.c"); // NOI18N
    }
    
    public void testBug254475() throws Exception {
        performStaticTest("src/bug254475.cpp"); // NOI18N
    }
    
    public void testBug254508() throws Exception {
        performStaticTest("src/bug254508.c"); // NOI18N
    }
    
    public void testBug254500() throws Exception {
        performStaticTest("src/bug254500.c"); // NOI18N
    }
    
    public void testBug254545() throws Exception {
        performStaticTest("src/bug254545.c"); // NOI18N
    }
    
    public void testBug254803() throws Exception {
        performStaticTest("src/bug254803.cpp"); // NOI18N
    }
    
    public void testBug255083() throws Exception {
        performStaticTest("src/bug255083.c"); // NOI18N
    }
    
    public void testBug255270() throws Exception {
        performStaticTest("src/bug255270.c"); // NOI18N
    }
    
    public void testBug255378() throws Exception {
        performStaticTest("src/bug255378.c"); // NOI18N
    }
    
    public void testBug254580() throws Exception {
        performStaticTest("src/bug254580.c"); // NOI18N
    }
    
    public void testBug256254() throws Exception {
        performStaticTest("src/bug256254.c"); // NOI18N
    }
    
    public void testBug255693() throws Exception {
        performStaticTest("src/bug255693.c"); // NOI18N
    }
    
    public void testBug256321() throws Exception {
        performStaticTest("src/bug256321.c"); // NOI18N
    }
    
    public void testBug257545() throws Exception {
        performStaticTest("src/bug257545.c"); // NOI18N
    }
    
    public void testBug259130() throws Exception {
        performStaticTest("src/bug259130.cpp"); // NOI18N
    }
    
    public void testBug267505() throws Exception {
        performFixesTest("src/bug267505.cpp"); // NOI18N
    }
}
