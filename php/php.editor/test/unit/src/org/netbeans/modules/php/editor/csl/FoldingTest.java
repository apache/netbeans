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
package org.netbeans.modules.php.editor.csl;

import org.netbeans.modules.php.editor.PHPTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FoldingTest extends PHPTestBase {

    public FoldingTest(String testName) {
        super(testName);
    }

    public void testFoldingMethod() throws Exception {
        checkFolds("testfiles/parser/foldingMethod.php");
    }

    public void testFoldingConditionalStatements() throws Exception {
        checkFolds("testfiles/parser/foldingConditionalStatements.php");
    }

    public void testFoldingCycles() throws Exception {
        checkFolds("testfiles/parser/foldingCycles.php");
    }

    public void testFoldingMethod_1() throws Exception {
        checkFolds("testfiles/parser/foldingMethod_1.php");
    }

    public void testFoldingConditionalStatements_1() throws Exception {
        checkFolds("testfiles/parser/foldingConditionalStatements_1.php");
    }

    public void testFoldingCycles_1() throws Exception {
        checkFolds("testfiles/parser/foldingCycles_1.php");
    }

    public void testIssue213616() throws Exception {
        checkFolds("testfiles/parser/issue213616.php");
    }

    public void testIssue216088() throws Exception {
        checkFolds("testfiles/parser/issue216088.php");
    }

    public void testIssue232884() throws Exception {
        checkFolds("testfiles/parser/issue232884.php");
    }

    public void testFinally_01() throws Exception {
        checkFolds("testfiles/parser/finally_01.php");
    }

    public void testFinally_02() throws Exception {
        checkFolds("testfiles/parser/finally_02.php");
    }

    public void testAnonymousClass01() throws Exception {
        checkFolds("testfiles/parser/anonymousClass_01.php");
    }

    // #262471
    public void testArrays() throws Exception {
        checkFolds("testfiles/parser/foldingArrays.php");
    }

    // #254432
    public void testUses() throws Exception {
        checkFolds("testfiles/parser/foldingUses.php");
    }

    // #232600
    public void testPHPTags() throws Exception {
        checkFolds("testfiles/parser/foldingPHPTags.php");
    }

}
