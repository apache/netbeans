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

public class GotoDeclaration269672Test extends GotoDeclarationTestBase {

    public GotoDeclaration269672Test(String testName) {
        super(testName);
    }

    public void testIssue269672_01() throws Exception {
        checkDeclaration(getTestPath(), "$anon = function() use ($te^sts) {", "$^tests = [];");
    }

    public void testIssue269672_02() throws Exception {
        checkDeclaration(getTestPath(), "$te^sts[0] = [1, 2, 3];", "$^tests = [];");
    }

    public void testIssue269672_03() throws Exception {
        checkDeclaration(getTestPath(), "$^tests[0] = [1, 2, 3];", "$^tests = [];");
    }

    public void testIssue269672_04() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($tes^ts);", "$^tests = [];");
    }

    public void testIssue269672_05() throws Exception {
        checkDeclaration(getTestPath(), "$anon2 = function() use (&$refere^nces) {", "$^references = [];");
    }

    public void testIssue269672_06() throws Exception {
        checkDeclaration(getTestPath(), "$^references[0] = [1, 2, 3];", "$^references = [];");
    }

    public void testIssue269672_07() throws Exception {
        checkDeclaration(getTestPath(), "$refer^ences[0] = [1, 2, 3];", "$^references = [];");
    }

    public void testIssue269672_08() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($reference^s);", "$^references = [];");
    }

    public void testIssue269672_09() throws Exception {
        checkDeclaration(getTestPath(), "$anon3 = function() use (&$f^oo) {", "$^foo = new Foo();");
    }

    public void testIssue269672_10() throws Exception {
        checkDeclaration(getTestPath(), "if($a instanceof $^foo) {", "$^foo = new Foo();");
    }

    public void testIssue269672_11() throws Exception {
        checkDeclaration(getTestPath(), "if($a instanceof $fo^o) {", "$^foo = new Foo();");
    }

    public void testIssue269672_12() throws Exception {
        checkDeclaration(getTestPath(), "echo get_class($f^oo) . PHP_EOL;", "$^foo = new Foo();");
    }

}
