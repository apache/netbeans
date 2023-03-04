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

public class GotoDeclaration269647Test extends GotoDeclarationTestBase {

    public GotoDeclaration269647Test(String testName) {
        super(testName);
    }

    public void testIssue269647_01() throws Exception {
        checkDeclaration(getTestPath(), "@param ^I $interface Description", "interface ^I {");
    }

    public void testIssue269647_02() throws Exception {
        checkDeclaration(getTestPath(), "@return ^I interface", "interface ^I {");
    }

    public void testIssue269647_03() throws Exception {
        checkDeclaration(getTestPath(), "@method ^I testClass2(I $class) Description", "interface ^I {");
    }

    public void testIssue269647_04() throws Exception {
        checkDeclaration(getTestPath(), "@method I testClass2(^I $class) Description", "interface ^I {");
    }

    public void testIssue269647_05() throws Exception {
        checkDeclaration(getTestPath(), "@property ^I $prop Description", "interface ^I {");
    }

    public void testIssue269647_06() throws Exception {
        checkDeclaration(getTestPath(), "@param ^I $class Description", "interface ^I {");
    }

    public void testIssue269647_07() throws Exception {
        checkDeclaration(getTestPath(), "@return ^I class", "interface ^I {");
    }

    public void testIssue269647_08() throws Exception {
        checkDeclaration(getTestPath(), "@param ^I $trait Description", "interface ^I {");
    }

    public void testIssue269647_09() throws Exception {
        checkDeclaration(getTestPath(), "@return ^I trait", "interface ^I {");
    }

}
