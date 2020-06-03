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

package org.netbeans.modules.cnd.highlight.semantic;

/**
 *
 */
public class FunctionsHighlightingTest extends SemanticHighlightingNewTestBase {

    public FunctionsHighlightingTest(String testName) {
        super(testName);
    }

    public void testClassFieldsInItsMethodsBody() throws Exception {
        performTest("functions_hl.cpp"); // NOI18N
    }

//    @Override
//    protected List<? extends CsmOffsetable> getBlocks(FileImpl testFile, int offset) {
//        List<? extends CsmOffsetable> list = ModelUtils.collect(
//                testFile, new ModelUtils.FunctionReferenceCollector());
//        assertTrue(list != null && list.size() > 0);
//        return list;
//    }
}
