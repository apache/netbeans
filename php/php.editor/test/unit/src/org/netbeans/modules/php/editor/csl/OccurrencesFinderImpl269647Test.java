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
package org.netbeans.modules.php.editor.csl;

public class OccurrencesFinderImpl269647Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImpl269647Test(String testName) {
        super(testName);
    }

    public void testIssue269647_01() throws Exception {
        checkOccurrences(getTestPath(), "interface ^I {", true);
    }

    public void testIssue269647_02() throws Exception {
        checkOccurrences(getTestPath(), "     * @param ^I $interface Description", true);
    }

    public void testIssue269647_03() throws Exception {
        checkOccurrences(getTestPath(), "     * @return ^I interface", true);
    }

    public void testIssue269647_04() throws Exception {
        checkOccurrences(getTestPath(), "    function testInterface(^I $interface);", true);
    }

    public void testIssue269647_05() throws Exception {
        checkOccurrences(getTestPath(), " * @method ^I testClass2(I $class) Description", true);
    }

    public void testIssue269647_06() throws Exception {
        checkOccurrences(getTestPath(), " * @method I testClass2(^I $class) Description", true);
    }

    public void testIssue269647_07() throws Exception {
        checkOccurrences(getTestPath(), " * @property ^I $prop Description", true);
    }

    public void testIssue269647_08() throws Exception {
        checkOccurrences(getTestPath(), "     * @param ^I $class Description", true);
    }

    public void testIssue269647_09() throws Exception {
        checkOccurrences(getTestPath(), "     * @return ^I class", true);
    }

    public void testIssue269647_10() throws Exception {
        checkOccurrences(getTestPath(), "    function testClass(^I $class){", true);
    }

    public void testIssue269647_11() throws Exception {
        checkOccurrences(getTestPath(), "     * @param ^I $trait Description", true);
    }

    public void testIssue269647_12() throws Exception {
        checkOccurrences(getTestPath(), "     * @return ^I trait", true);
    }

    public void testIssue269647_13() throws Exception {
        checkOccurrences(getTestPath(), "    function testTrait(^I $trait){", true);
    }

}
