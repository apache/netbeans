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

package org.netbeans.modules.java.completion;

import org.netbeans.modules.java.source.parsing.JavacParser;

public class JavaCompletionTask116FeaturesTest extends CompletionTestBase {

    private static final String SOURCE_LEVEL = "16"; //NOI18N

    public JavaCompletionTask116FeaturesTest(String testName) {
        super(testName);
    }

    public void testInstanceofPatternCompletion_1() throws Exception {
        performTest("InstanceofPattern", 907, null, "stringVarName.pass", SOURCE_LEVEL);
    }

    public void testInstanceofPatternCompletion_2() throws Exception {
        performTest("InstanceofPattern", 908, " ", "empty.pass", SOURCE_LEVEL);
    }

    public void testInstanceofPatternCompletion_3() throws Exception {
        performTest("InstanceofPattern", 926, null, "stringContent.pass", SOURCE_LEVEL);
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
