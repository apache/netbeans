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

public class GotoDeclarationPHP83Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP83Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php83/";
    }

    public void testDynamicClassConstantFetch_01a() throws Exception {
        checkDeclaration(getTestPath(), "    public const TEST3 = self::{self::T^ES . self::T};", "    public const ^TES = 'TES';");
    }

    public void testDynamicClassConstantFetch_02a() throws Exception {
        checkDeclaration(getTestPath(), "    public const TEST3 = self::{self::TES . self::^T};", "    public const ^T = 'T';");
    }

    public void testDynamicClassConstantFetch_03a() throws Exception {
        checkDeclaration(getTestPath(), "    case TEST3 = self::{self::TE^S . self::T};", "    case ^TES = 'TES';");
    }

    public void testDynamicClassConstantFetch_04a() throws Exception {
        checkDeclaration(getTestPath(), "    case TEST3 = self::{self::TES . self::^T};", "    case ^T = 'T';");
    }

    public void testDynamicClassConstantFetch_05a() throws Exception {
        checkDeclaration(getTestPath(), "Tes^t::{\"TEST\"};", "class ^Test {");
    }

    public void testDynamicClassConstantFetch_06a() throws Exception {
        checkDeclaration(getTestPath(), "$te^st::{\"TEST\"};", "$^test = \"Test\";");
    }

    public void testDynamicClassConstantFetch_07a() throws Exception {
        checkDeclaration(getTestPath(), "Test::{$vari^able};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07b() throws Exception {
        checkDeclaration(getTestPath(), "$test::{$vari^able};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07c() throws Exception {
        checkDeclaration(getTestPath(), "Test::{$vari^able . $e};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07d() throws Exception {
        checkDeclaration(getTestPath(), "$test::{$vari^able . $e};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07e() throws Exception {
        checkDeclaration(getTestPath(), "Test::{test()}::{test($varia^ble)};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_08a() throws Exception {
        checkDeclaration(getTestPath(), "Test::{$variable . $^e};", "$^e = 'e';");
    }

    public void testDynamicClassConstantFetch_08b() throws Exception {
        checkDeclaration(getTestPath(), "$test::{$variable . $^e};", "$^e = 'e';");
    }

    public void testDynamicClassConstantFetch_09a() throws Exception {
        checkDeclaration(getTestPath(), "Test::{tes^t()}::{test($variable)};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_09b() throws Exception {
        checkDeclaration(getTestPath(), "Test::{test()}::{te^st($variable)};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_09c() throws Exception {
        checkDeclaration(getTestPath(), "Test::{te^st('test1')}::{test('test2')};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_09d() throws Exception {
        checkDeclaration(getTestPath(), "Test::{test('test1')}::{tes^t('test2')};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_10a() throws Exception {
        checkDeclaration(getTestPath(), "EnumT^est::{[]};", "enum ^EnumTest: string {");
    }
}
