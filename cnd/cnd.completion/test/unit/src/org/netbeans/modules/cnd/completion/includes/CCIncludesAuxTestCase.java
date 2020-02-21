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
package org.netbeans.modules.cnd.completion.includes;

import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 *
 *
 */
public class CCIncludesAuxTestCase extends CndBaseTestCase {

    private static final boolean TRACE = false;

    /**
     * Creates a new instance of CCIncludesAuxTestCase
     */
    public CCIncludesAuxTestCase(String testName) {
        super(testName);
    }

    public void testTextShrinking() throws Exception {
        String text = "/very/long/path/to/include/dir";
        CsmIncludeCompletionItem item = new CsmIncludeCompletionItem(0, 0, 0, text, "on/Unix/system", "", false, true, false);
        String shrinked = item.getRightText(true, "/");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("/very/long.../Unix/system", shrinked);
        text = "C:\\very\\long\\path\\to\\include\\dir";
        item = new CsmIncludeCompletionItem(0, 0, 0, text, "on\\Windows\\system", "", false, true, false);
        shrinked = item.getRightText(true, "\\");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("C:\\very\\long...\\Windows\\system", shrinked);
        text = "C:\\very\\long\\path\\to\\mixed/include/dir";
        item = new CsmIncludeCompletionItem(0, 0, 0, text, "on/Windows//mixed", "", false, true, false);
        shrinked = item.getRightText(true, "\\");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("C:\\very\\long...\\\\mixed", shrinked);
        text = "/very/long/path/to\\include\\mixed\\dir";
        item = new CsmIncludeCompletionItem(0, 0, 0, text, "on/unix/mixed", "", false, true, false);
        shrinked = item.getRightText(true, "/");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("/very/long.../unix/mixed", shrinked);
    }
}
