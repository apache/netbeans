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

public class GotoDeclarationGH5371Test extends GotoDeclarationTestBase {

    public GotoDeclarationGH5371Test(String testName) {
        super(testName);
    }

    public void testIssueGH5371_01a() throws Exception {
        checkDeclaration(getTestPath(), "class TestClass implements TestI^nterface {", "interface ^TestInterface {");
    }

    public void testIssueGH5371_01b() throws Exception {
        checkDeclaration(getTestPath(), "if ($a instanceof TestInt^erface) {", "interface ^TestInterface {");
    }

    public void testIssueGH5371_02a() throws Exception {
        checkDeclaration(getTestPath(), "$a = new TestCla^ss();", "class ^TestClass implements TestInterface {");
    }

    public void testIssueGH5371_02b() throws Exception {
        checkDeclaration(getTestPath(), "if ($a instanceof Tes^tClass) {", "class ^TestClass implements TestInterface {");
    }

}
