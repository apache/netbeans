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

/**
 *
 *
 */
public class MultiParseHyperlinkTestCase extends HyperlinkBaseTestCase {
    public MultiParseHyperlinkTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        // check multiparse in one thread mode for stability
//        System.setProperty("cnd.modelimpl.parser.threads", "1");
//        System.setProperty("parser.log.parse", "true");
        super.setUp();
    }

    public void testIZ157907() throws Exception {
        // IZ#151881: Unresolved ids in #ifdef and #ifndef
        performTest("source.c", 6, 10, "shared.h", 12, 9);
        performTest("source.cpp", 6, 10, "shared.h", 10, 9);
    }
}
