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

public class OccurrencesFinderImplPHP83Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP83Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php83/";
    }

    public void testDynamicClassConstantFetch_01a() throws Exception {
        checkOccurrences(getTestPath(), "class Te^st {", true);
    }

    public void testDynamicClassConstantFetch_01b() throws Exception {
        checkOccurrences(getTestPath(), "Te^st::{\"TEST\"};", true);
    }

    public void testDynamicClassConstantFetch_01c() throws Exception {
        checkOccurrences(getTestPath(), "T^est::{$variable};", true);
    }

    public void testDynamicClassConstantFetch_01d() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{$variable . $e};", true);
    }

    public void testDynamicClassConstantFetch_01e() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{strtoupper(\"test\")};", true);
    }

    public void testDynamicClassConstantFetch_01f() throws Exception {
        checkOccurrences(getTestPath(), "^Test::{'$variablee'};", true);
    }

    public void testDynamicClassConstantFetch_01g() throws Exception {
        checkOccurrences(getTestPath(), "T^est::{strtolower(\"CLASS\")};", true);
    }

    public void testDynamicClassConstantFetch_01h() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{1000};", true);
    }

    public void testDynamicClassConstantFetch_01i() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{Test::method()}::{test($variable)};", true);
    }

    public void testDynamicClassConstantFetch_01j() throws Exception {
        checkOccurrences(getTestPath(), "Test::{T^est::method()}::{test($variable)};", true);
    }

    public void testDynamicClassConstantFetch_01k() throws Exception {
        checkOccurrences(getTestPath(), "Te^st::{test('test1')}::{test('test2')};", true);
    }

    public void testDynamicClassConstantFetch_01l() throws Exception {
        checkOccurrences(getTestPath(), "$test = new Te^st();", true);
    }

    public void testDynamicClassConstantFetch_02a() throws Exception {
        checkOccurrences(getTestPath(), "enum EnumT^est: string {", true);
    }

    public void testDynamicClassConstantFetch_02b() throws Exception {
        checkOccurrences(getTestPath(), "EnumTe^st::{[]};", true);
    }

    public void testDynamicClassConstantFetch_03a() throws Exception {
        checkOccurrences(getTestPath(), "    public const TE^S = 'TES';", true);
    }

    public void testDynamicClassConstantFetch_03b() throws Exception {
        checkOccurrences(getTestPath(), "    public const TEST3 = self::{self::T^ES . self::T};", true);
    }

    public void testDynamicClassConstantFetch_04a() throws Exception {
        checkOccurrences(getTestPath(), "    public const ^T = 'T';", true);
    }

    public void testDynamicClassConstantFetch_04b() throws Exception {
        checkOccurrences(getTestPath(), "    public const TEST3 = self::{self::TES . self::^T};", true);
    }

    public void testDynamicClassConstantFetch_05a() throws Exception {
        checkOccurrences(getTestPath(), "    public static function metho^d(): void {}", true);
    }

    public void testDynamicClassConstantFetch_05b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{Test::met^hod()}::{test($variable)};", true);
    }

    public void testDynamicClassConstantFetch_06a() throws Exception {
        checkOccurrences(getTestPath(), "    case TE^S = 'TES';", true);
    }

    public void testDynamicClassConstantFetch_06b() throws Exception {
        checkOccurrences(getTestPath(), "    case TEST3 = self::{self::TE^S . self::T};", true);
    }

    public void testDynamicClassConstantFetch_07a() throws Exception {
        checkOccurrences(getTestPath(), "    case ^T = 'T';", true);
    }

    public void testDynamicClassConstantFetch_07b() throws Exception {
        checkOccurrences(getTestPath(), "    case TEST3 = self::{self::TES . self::^T};", true);
    }

    public void testDynamicClassConstantFetch_08a() throws Exception {
        checkOccurrences(getTestPath(), "function t^est(string $param = \"test\"): void {", true);
    }

    public void testDynamicClassConstantFetch_08b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{Test::method()}::{tes^t($variable)};", true);
    }

    public void testDynamicClassConstantFetch_08c() throws Exception {
        checkOccurrences(getTestPath(), "Test::{te^st('test1')}::{test('test2')};", true);
    }

    public void testDynamicClassConstantFetch_08d() throws Exception {
        checkOccurrences(getTestPath(), "Test::{test('test1')}::{te^st('test2')};", true);
    }

    public void testDynamicClassConstantFetch_09a() throws Exception {
        checkOccurrences(getTestPath(), "$vari^able = 'TEST';", true);
    }

    public void testDynamicClassConstantFetch_09b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{$var^iable};", true);
    }

    public void testDynamicClassConstantFetch_09c() throws Exception {
        checkOccurrences(getTestPath(), "$test::{$v^ariable};", true);
    }

    public void testDynamicClassConstantFetch_09d() throws Exception {
        checkOccurrences(getTestPath(), "Test::{$vari^able . $e};", true);
    }

    public void testDynamicClassConstantFetch_09e() throws Exception {
        checkOccurrences(getTestPath(), "$test::{$varia^ble . $e};", true);
    }

    public void testDynamicClassConstantFetch_09f() throws Exception {
        checkOccurrences(getTestPath(), "Test::{Test::method()}::{test($variab^le)};", true);
    }

    public void testDynamicClassConstantFetch_10a() throws Exception {
        checkOccurrences(getTestPath(), "$^e = 'e';", true);
    }

    public void testDynamicClassConstantFetch_10b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{$variable . $^e};", true);
    }

    public void testDynamicClassConstantFetch_10c() throws Exception {
        checkOccurrences(getTestPath(), "$test::{$variable . $^e};", true);
    }

    public void testDynamicClassConstantFetch_11a() throws Exception {
        checkOccurrences(getTestPath(), "$t^est = \"Test\";", true);
    }

    public void testDynamicClassConstantFetch_11b() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{\"TEST\"};", true);
    }

    public void testDynamicClassConstantFetch_11c() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{$variable};", true);
    }

    public void testDynamicClassConstantFetch_11d() throws Exception {
        checkOccurrences(getTestPath(), "$tes^t::{$variable . $e};", true);
    }

    public void testDynamicClassConstantFetch_11e() throws Exception {
        checkOccurrences(getTestPath(), "$te^st::{strtoupper(\"test\")};", true);
    }

    public void testDynamicClassConstantFetch_11f() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{'$variablee'};", true);
    }

    public void testDynamicClassConstantFetch_11g() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{strtolower(\"CLASS\")};", true);
    }

    public void testDynamicClassConstantFetch_11h() throws Exception {
        checkOccurrences(getTestPath(), "$te^st::{1000};", true);
    }

    public void testDynamicClassConstantFetch_11i() throws Exception {
        checkOccurrences(getTestPath(), "$tes^t::{[]};", true);
    }

    public void testDynamicClassConstantFetch_11j() throws Exception {
        checkOccurrences(getTestPath(), "$tes^t = new Test();", true);
    }
}
