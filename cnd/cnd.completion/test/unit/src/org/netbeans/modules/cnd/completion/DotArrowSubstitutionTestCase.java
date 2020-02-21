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

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 *
 */
public class DotArrowSubstitutionTestCase extends CompletionBaseTestCase {

    public DotArrowSubstitutionTestCase(String testName) {
        super(testName, true);
    }
    
    public void testDotToArrowAutoFix_1() throws Exception {
        super.performTest("bug97120.cc", 29, 13, "pA.f");
    }

    public void testDotToArrowAutoFix_2() throws Exception {
        super.performTest("bug97120.cc", 29, 13, "c.f");
    }

    public void testDotToArrowAutoFix_3() throws Exception {
        super.performTest("bug97120.cc", 29, 13, "c.p");
    }

    public void testDotToArrowAutoFix_4() throws Exception {
        super.performTest("bug97120.cc", 29, 13, "ppA.f");
    }

    public void testDotToArrowAutoFix_5() throws Exception {
        super.performTest("bug97120.cc", 29, 13, "pppA.f");
    }

    public void testDotToArrowAutoFix_6() throws Exception {
        super.performTest("bug97120.cc", 29, 13, "d.ptr().");
    }

    public void testDotToArrowAutoFix_7() throws Exception {
        super.performTest("bug97120.cc", 29, 13, "(*pA).");
    }
    
    public void testDotToArrowAutoFix_8() throws Exception {
        super.performTest("bug230101.cpp", 36, 43, ".T");
    }
}
