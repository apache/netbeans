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

package org.netbeans.modules.php.latte.parser;

import org.netbeans.modules.php.latte.LatteTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteParserErrorTest extends LatteTestBase {

    public LatteParserErrorTest(String testName) {
        super(testName);
    }

    public void testIssue245728_01() throws Exception {
        checkErrors("testfiles/parser/issue245728_01.latte");
    }

    public void testIssue245728_02() throws Exception {
        checkErrors("testfiles/parser/issue245728_02.latte");
    }

    public void testIssue245728_03() throws Exception {
        checkErrors("testfiles/parser/issue245728_03.latte");
    }

    /*
    Tests are disabled because test files were not donated.
    public void testIssue245728_04() throws Exception {
        checkErrors("testfiles/parser/issue245728_04.latte");
    }

    public void testIssue245728_05() throws Exception {
        checkErrors("testfiles/parser/issue245728_05.latte");
    }

    public void testIssue245728_06() throws Exception {
        checkErrors("testfiles/parser/issue245728_06.latte");
    }
    */

    public void testIssue245728_07() throws Exception {
        checkErrors("testfiles/parser/issue245728_07.latte");
    }

    public void testIssue245728_08() throws Exception {
        checkErrors("testfiles/parser/issue245728_08.latte");
    }

    public void testIssue245728_09() throws Exception {
        checkErrors("testfiles/parser/issue245728_09.latte");
    }

    public void testIssue245728_10() throws Exception {
        checkErrors("testfiles/parser/issue245728_10.latte");
    }

    public void testIssue245728_11() throws Exception {
        checkErrors("testfiles/parser/issue245728_11.latte");
    }

    public void testIssue245728_12() throws Exception {
        checkErrors("testfiles/parser/issue245728_12.latte");
    }

    public void testIssue245728_13() throws Exception {
        checkErrors("testfiles/parser/issue245728_13.latte");
    }

    public void testIssue245728_14() throws Exception {
        checkErrors("testfiles/parser/issue245728_14.latte");
    }

    public void testIssue245728_15() throws Exception {
        checkErrors("testfiles/parser/issue245728_15.latte");
    }

    public void testIssue245728_16() throws Exception {
        checkErrors("testfiles/parser/issue245728_16.latte");
    }

    public void testIssue245728_17() throws Exception {
        checkErrors("testfiles/parser/issue245728_17.latte");
    }

    public void testIssue245728_18() throws Exception {
        checkErrors("testfiles/parser/issue245728_18.latte");
    }

    public void testIssue245728_19() throws Exception {
        checkErrors("testfiles/parser/issue245728_19.latte");
    }

    public void testIssue245728_20() throws Exception {
        checkErrors("testfiles/parser/issue245728_20.latte");
    }

    public void testIssue245728_21() throws Exception {
        checkErrors("testfiles/parser/issue245728_21.latte");
    }

    public void testIssue245728_22() throws Exception {
        checkErrors("testfiles/parser/issue245728_22.latte");
    }

    public void testIssue245728_23() throws Exception {
        checkErrors("testfiles/parser/issue245728_23.latte");
    }

    public void testIssue245728_24() throws Exception {
        checkErrors("testfiles/parser/issue245728_24.latte");
    }

    public void testIssue245728_25() throws Exception {
        checkErrors("testfiles/parser/issue245728_25.latte");
    }

    public void testIssue245728_26() throws Exception {
        checkErrors("testfiles/parser/issue245728_26.latte");
    }

    public void testIssue245728_27() throws Exception {
        checkErrors("testfiles/parser/issue245728_27.latte");
    }

    public void testIssue245728_28() throws Exception {
        checkErrors("testfiles/parser/issue245728_28.latte");
    }

}
