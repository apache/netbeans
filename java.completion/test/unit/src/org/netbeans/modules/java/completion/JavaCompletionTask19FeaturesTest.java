/**
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

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTask19FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask19FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.9 try-with-resources tests -------------------------------------------
    
    public void testEmptyFileBeforeTypingExistingVarResouce() throws Exception {
        performTest("SimpleTWRStart", 949, "try (", "resourcesAndAutoCloseables.pass", "1.9");
    }

    public void testBeforeTypingExistingVarResouce() throws Exception {
        performTest("SimpleTWRNoRes", 954, null, "resourcesAndAutoCloseables.pass", "1.9");
    }

    public void testBeforeExistingVarResouce() throws Exception {
        performTest("SimpleTWR", 982, null, "resourcesAndAutoCloseables.pass", "1.9");
    }

    public void testEmptyFileTypingExistingVarResouce() throws Exception {
        performTest("SimpleTWRStart", 949, "try (f", "finalKeyword.pass", "1.9");
    }

    public void testTypingExistingVarResouce() throws Exception {
        performTest("SimpleTWRNoRes", 954, "f", "finalKeyword.pass", "1.9");
    }

    public void testOnExistingVarResouce() throws Exception {
        performTest("SimpleTWR", 983, null, "finalKeyword.pass", "1.9");
    }

    public void testEmptyFileAfterTypingExistingVarResouce() throws Exception {
        performTest("SimpleTWRStart", 949, "try (fw ", "empty.pass", "1.9");
    }

    public void testAfterTypingExistingVarResouce() throws Exception {
        performTest("SimpleTWRNoRes", 954, "fw ", "empty.pass", "1.9");
    }

    public void testAfterExistingVarResouce() throws Exception {
        performTest("SimpleTWR", 984, " ", "empty.pass", "1.9");
    }

    public void testEmptyFileBeforeTypingNewVarResouce() throws Exception {
        performTest("SimpleTWRStart", 949, "try (fw;", "resourcesAndAutoCloseables.pass", "1.9");
    }

    public void testBeforeTypingNewVarResouce() throws Exception {
        performTest("SimpleTWRNoRes", 954, "fw;", "resourcesAndAutoCloseables.pass", "1.9");
    }

    public void testBeforeNewVarResouce() throws Exception {
        performTest("SimpleTWR", 985, null, "resourcesAndAutoCloseables.pass", "1.9");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
