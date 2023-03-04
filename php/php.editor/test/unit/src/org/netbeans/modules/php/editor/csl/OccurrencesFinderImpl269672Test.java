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

public class OccurrencesFinderImpl269672Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImpl269672Test(String testName) {
        super(testName);
    }

    public void testIssue269672_01() throws Exception {
        checkOccurrences(getTestPath(), "$tes^ts = [];", true);
    }

    public void testIssue269672_02() throws Exception {
        checkOccurrences(getTestPath(), "$anon = function() use ($tes^ts) {", true);
    }

    public void testIssue269672_03() throws Exception {
        checkOccurrences(getTestPath(), "$t^ests[0] = [1, 2, 3];", true);
    }

    public void testIssue269672_04() throws Exception {
        checkOccurrences(getTestPath(), "var_dump($test^s);", true);
    }

    public void testIssue269672_05() throws Exception {
        checkOccurrences(getTestPath(), "$referenc^es = [];", true);
    }

    public void testIssue269672_06() throws Exception {
        checkOccurrences(getTestPath(), "$anon2 = function() use (&$reference^s) {", true);
    }

    public void testIssue269672_07() throws Exception {
        checkOccurrences(getTestPath(), "$reference^s[0] = [1, 2, 3];", true);
    }

    public void testIssue269672_08() throws Exception {
        checkOccurrences(getTestPath(), "var_dump($referenc^es);", true);
    }

    public void testIssue269672_09() throws Exception {
        checkOccurrences(getTestPath(), "    $tes^ts;", true);
    }

    public void testIssue269672_10() throws Exception {
        checkOccurrences(getTestPath(), "$fo^o = new Foo();", true);
    }

    public void testIssue269672_11() throws Exception {
        checkOccurrences(getTestPath(), "$anon3 = function() use (&$f^oo) {", true);
    }

    public void testIssue269672_12() throws Exception {
        checkOccurrences(getTestPath(), "if($a instanceof $^foo) {", true);
    }

    public void testIssue269672_13() throws Exception {
        checkOccurrences(getTestPath(), "echo get_class($f^oo) . PHP_EOL;", true);
    }

}
