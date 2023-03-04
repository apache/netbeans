/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
public class JavaCompletionTask17FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask17FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.7 try-with-resources tests ---------------------------------------
    
    public void testEmptyFileBeforeTypingVarResouce() throws Exception {
        performTest("TWRStart", 923, "try (", "finalAndAutoCloseables.pass", "1.7");
    }

    public void testBeforeTypingVarResouce() throws Exception {
        performTest("TWRNoRes", 936, null, "finalAndAutoCloseables.pass", "1.7");
    }

    public void testBeforeVarResouce() throws Exception {
        performTest("TWR", 936, null, "finalAndAutoCloseables.pass", "1.7");
    }

    public void testEmptyFileTypingVarResouce() throws Exception {
        performTest("TWRStart", 923, "try (f", "finalKeyword.pass", "1.7");
    }

    public void testTypingVarResouce() throws Exception {
        performTest("TWRNoRes", 936, "f", "finalKeyword.pass", "1.7");
    }

    public void testOnVarResouce() throws Exception {
        performTest("TWR", 937, null, "finalKeyword.pass", "1.7");
    }

    public void testEmptyFileAfterFinalInResource() throws Exception {
        performTest("TWRStart", 923, "try (final ", "autoCloseables.pass", "1.7");
    }

    public void testTypingAfterFinalInResouce() throws Exception {
        performTest("TWRNoRes", 936, "final ", "autoCloseables.pass", "1.7");
    }

    public void testAfterFinalInResouce() throws Exception {
        performTest("TWR", 942, null, "autoCloseables.pass", "1.7");
    }

    public void testEmptyFileTypingTypeInVarResouce() throws Exception {
        performTest("TWRStart", 923, "try (final F", "autoCloseablesStartingWithF.pass", "1.7");
    }

    public void testTypingTypeInVarResouce() throws Exception {
        performTest("TWRNoRes", 936, "final F", "autoCloseablesStartingWithF.pass", "1.7");
    }

    public void testOnTypeInVarResouce() throws Exception {
        performTest("TWR", 943, null, "autoCloseablesStartingWithF.pass", "1.7");
    }

    public void testEmptyFileBeforeTypingNameInVarResouce() throws Exception {
        performTest("TWRStart", 923, "try (final FileWriter ", "resourceNames.pass", "1.7");
    }

    public void testBeforeTypingNameInVarResouce() throws Exception {
        performTest("TWRNoRes", 936, "final FileWriter ", "resourceNames.pass", "1.7");
    }

    public void testBeforeNameInVarResouce() throws Exception {
        performTest("TWR", 953, null, "resourceNames.pass", "1.7");
    }

    public void testEmptyFileAfterTypingNameInVarResouce() throws Exception {
        performTest("TWRStart", 923, "try (final FileWriter fw ", "empty.pass", "1.7");
    }

    public void testAfterTypingNameInVarResouce() throws Exception {
        performTest("TWRNoRes", 936, "final FileWriter fw ", "empty.pass", "1.7");
    }

    public void testAfterNameInVarResouce() throws Exception {
        performTest("TWR", 956, null, "empty.pass", "1.7");
    }

    public void testEmptyFileBeforeVarResouceInit() throws Exception {
        performTest("TWRStart", 923, "try (final FileWriter fw = ", "resourceInit.pass", "1.7");
    }

    public void testBeforeTypingVarResouceInit() throws Exception {
        performTest("TWRNoRes", 936, "final FileWriter fw = ", "resourceInit.pass", "1.7");
    }

    public void testBeforeVarResouceInit() throws Exception {
        performTest("TWR", 958, null, "resourceInit.pass", "1.7");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
