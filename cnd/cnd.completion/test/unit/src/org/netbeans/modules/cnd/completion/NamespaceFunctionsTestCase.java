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
 *159170
 */
public class NamespaceFunctionsTestCase extends CompletionBaseTestCase {

    /**
     * Creates a new instance of NamespacesTestCase
     */
    public NamespaceFunctionsTestCase(String testName) {
        super(testName, true);
    }

    public void testInNamespaceMethod() throws Exception {
        // IZ#125043: Code completion & hyperlink take into account namespace context in function definitions
        super.performTest("ns_functions.cc", 18, 13);
    }

    public void testInNamespaceMethod2() throws Exception {
        // IZ#125043: Code completion & hyperlink take into account namespace context in function definitions
        super.performTest("ns_functions.cc", 24, 5);
    }

    public void testInClassMethod() throws Exception {
        // IZ#125043: Code completion & hyperlink take into account namespace context in function definitions
        super.performTest("ns_functions.cc", 28, 5);
    }
}
