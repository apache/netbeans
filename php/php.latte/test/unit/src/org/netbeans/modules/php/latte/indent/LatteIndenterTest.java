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
package org.netbeans.modules.php.latte.indent;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteIndenterTest extends LatteIndenterTestBase {

    public LatteIndenterTest(String testName) {
        super(testName);
    }

    public void testInlineLatte() throws Exception {
        indent("testInlineLatte");
    }

    public void testSimpleIfBlock() throws Exception {
        indent("testSimpleIfBlock");
    }

    public void testIfElseBlock() throws Exception {
        indent("testIfElseBlock");
    }

    public void testRealFile_01() throws Exception {
        indent("testRealFile_01");
    }

    public void testRealFile_02() throws Exception {
        indent("testRealFile_02");
    }

    public void testRealFile_03() throws Exception {
        indent("testRealFile_03");
    }

    public void testRealFile_04() throws Exception {
        indent("testRealFile_04");
    }

    public void testShortedBlockMacro() throws Exception {
        indent("shortedBlockMacro");
    }

    public void testIssue237326_01() throws Exception {
        indent("testIssue237326_01");
    }

    public void testIssue237326_02() throws Exception {
        indent("testIssue237326_02");
    }

    public void testIssue237326_03() throws Exception {
        indent("testIssue237326_03");
    }

    public void testIssue241118_01() throws Exception {
        indent("testIssue241118_01");
    }

    public void testIssue241118_02() throws Exception {
        indent("testIssue241118_02");
    }

}
