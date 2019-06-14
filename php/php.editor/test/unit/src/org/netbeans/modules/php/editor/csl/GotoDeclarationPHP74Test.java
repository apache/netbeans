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

public class GotoDeclarationPHP74Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP74Test(String testName) {
        super(testName);
    }

    // class
    public void testTypedProperties20Class_01() throws Exception {
        checkDeclaration(getTestPath(), "use Bar\\My^Class;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_02() throws Exception {
        checkDeclaration(getTestPath(), "    public My^Class $myClass;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_03() throws Exception {
        checkDeclaration(getTestPath(), "    public ?^MyClass $myClass2;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_04() throws Exception {
        checkDeclaration(getTestPath(), "    public \\Bar\\MyCl^ass $myClass3;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_05() throws Exception {
        checkDeclaration(getTestPath(), "    public ?\\Bar\\My^Class $myClass4;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_06() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass->pub^licTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_07() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2->publicTest^Method();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_08() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_09() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4->publicTestMe^thod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_10() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass::publicS^taticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Class_11() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2::publ^icStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Class_12() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3::publicSt^aticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Class_13() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    // trait
    public void testTypedProperties20Trait_01() throws Exception {
        checkDeclaration(getTestPath(), "use Bar\\My^Class;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_02() throws Exception {
        checkDeclaration(getTestPath(), "    public My^Class $myClass;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_03() throws Exception {
        checkDeclaration(getTestPath(), "    public ?^MyClass $myClass2;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_04() throws Exception {
        checkDeclaration(getTestPath(), "    public \\Bar\\MyCl^ass $myClass3;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_05() throws Exception {
        checkDeclaration(getTestPath(), "    public ?\\Bar\\My^Class $myClass4;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_06() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass->pub^licTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_07() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2->publicTest^Method();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_08() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_09() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4->publicTestMe^thod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_10() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass::publicS^taticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Trait_11() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2::publ^icStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Trait_12() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3::publicSt^aticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Trait_13() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }
}
