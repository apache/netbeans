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
package org.netbeans.modules.php.latte.completion;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteCompletionTest extends LatteCompletionTestBase {

    public LatteCompletionTest(String testName) {
        super(testName);
    }

    public void testCompletion_01() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_01.latte", "{^}", false);
    }

    public void testCompletion_02() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_02.latte", "{fo^}", false);
    }

    public void testCompletion_03() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_03.latte", "{$item|^}", false);
    }

    public void testCompletion_04() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_04.latte", "{$item|l^}", false);
    }

    public void testCompletion_05() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_05.latte", "{^} <!-- first -->", false);
    }

    public void testCompletion_06() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.latte", "{^}{$bar}{}", false);
    }

    public void testCompletion_07() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.latte", "{}{$bar}{^}", false);
    }

    public void testCompletion_08() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_08.latte", "<!-- second --> {^}", false);
    }

    public void testIterator_01() throws Exception {
        checkCompletion("testfiles/completion/testIterator_01.latte", "{$iterator->^}", false);
    }

    public void testIterator_02() throws Exception {
        checkCompletion("testfiles/completion/testIterator_02.latte", "{$iterator->f^}", false);
    }

    public void testVariable_01() throws Exception {
        checkCompletion("testfiles/completion/testVariable_01.latte", "{$^}", false);
    }

    public void testVariable_02() throws Exception {
        checkCompletion("testfiles/completion/testVariable_02.latte", "{$b^}", false);
    }

    public void testVariable_03() throws Exception {
        checkCompletion("testfiles/completion/testVariable_03.latte", "{$x^}", false);
    }

    public void testVariable_04() throws Exception {
        checkCompletion("testfiles/completion/testVariable_04.latte", "{foreach $b^ as item}", false);
    }

    public void testEndMacro_01() throws Exception {
        checkCompletion("testfiles/completion/testEndMacro_01.latte", "{/^}", false);
    }

    public void testEndMacro_02() throws Exception {
        checkCompletion("testfiles/completion/testEndMacro_02.latte", "{/c^}", false);
    }

}
