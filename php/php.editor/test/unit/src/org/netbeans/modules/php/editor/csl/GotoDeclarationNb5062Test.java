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


public class GotoDeclarationNb5062Test extends GotoDeclarationTestBase {

    public GotoDeclarationNb5062Test(String testName) {
        super(testName);
    }

    public void testNb5062_Static01() throws Exception {
        checkDeclaration(getTestPath(), "$static1->te^stMethod();", "    public function ^testMethod() { // Test2");
    }

    public void testNb5062_Static02() throws Exception {
        checkDeclaration(getTestPath(), "$static2->testMet^hod();", "    public function ^testMethod() { // Test2");
    }

    public void testNb5062_Static03() throws Exception {
        checkDeclaration(getTestPath(), "$static3->^testMethod();", "    public function ^testMethod() { // Test2");
    }

    public void testNb5062_Self01() throws Exception {
        checkDeclaration(getTestPath(), "$self1->te^stMethod();", "    public function ^testMethod() { // Test2");
    }

    public void testNb5062_Self02() throws Exception {
        checkDeclaration(getTestPath(), "$self2->testMet^hod();", "    public function ^testMethod() { // Test2");
    }

    public void testNb5062_Self03() throws Exception {
        checkDeclaration(getTestPath(), "$self3->^testMethod();", "    public function ^testMethod() { // Test2");
    }
}
