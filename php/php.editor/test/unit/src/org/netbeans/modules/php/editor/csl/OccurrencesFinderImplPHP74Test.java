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


public class OccurrencesFinderImplPHP74Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP74Test(String testName) {
        super(testName);
    }

    // class
    public void testTypedProperties20Class_01() throws Exception {
        checkOccurrences(getTestPath(), "use Bar\\My^Class;", true);
    }

    public void testTypedProperties20Class_02() throws Exception {
        checkOccurrences(getTestPath(), "    public My^Class $myClass;", true);
    }

    public void testTypedProperties20Class_03() throws Exception {
        checkOccurrences(getTestPath(), "    public ?MyCla^ss $myClass2;", true);
    }

    public void testTypedProperties20Class_04() throws Exception {
        checkOccurrences(getTestPath(), "    public \\Bar\\MyCla^ss $myClass3;", true);
    }

    public void testTypedProperties20Class_05() throws Exception {
        checkOccurrences(getTestPath(), "    public ?\\Bar\\^MyClass $myClass4;", true);
    }

    public void testTypedProperties20Class_06() throws Exception {
        checkOccurrences(getTestPath(), "class MyCl^ass {", true);
    }

    public void testTypedProperties20Class_07() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass->publicTe^stMethod();", true);
    }

    public void testTypedProperties20Class_08() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2->pu^blicTestMethod();", true);
    }

    public void testTypedProperties20Class_09() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", true);
    }

    public void testTypedProperties20Class_10() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4->^publicTestMethod();", true);
    }

    public void testTypedProperties20Class_11() throws Exception {
        checkOccurrences(getTestPath(), "    public function publicTestMe^thod(): void {", true);
    }

    public void testTypedProperties20Class_12() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass::publicStati^cTestMethod();", true);
    }

    public void testTypedProperties20Class_13() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2::p^ublicStaticTestMethod();", true);
    }

    public void testTypedProperties20Class_14() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Class_15() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Class_16() throws Exception {
        checkOccurrences(getTestPath(), "    public static function publicStaticTestMe^thod(): void {", true);
    }

    // trait
    public void testTypedProperties20Trait_01() throws Exception {
        checkOccurrences(getTestPath(), "use Bar\\My^Class;", true);
    }

    public void testTypedProperties20Trait_02() throws Exception {
        checkOccurrences(getTestPath(), "    public My^Class $myClass;", true);
    }

    public void testTypedProperties20Trait_03() throws Exception {
        checkOccurrences(getTestPath(), "    public ?MyCla^ss $myClass2;", true);
    }

    public void testTypedProperties20Trait_04() throws Exception {
        checkOccurrences(getTestPath(), "    public \\Bar\\MyCla^ss $myClass3;", true);
    }

    public void testTypedProperties20Trait_05() throws Exception {
        checkOccurrences(getTestPath(), "    public ?\\Bar\\^MyClass $myClass4;", true);
    }

    public void testTypedProperties20Trait_06() throws Exception {
        checkOccurrences(getTestPath(), "class MyCl^ass {", true);
    }

    public void testTypedProperties20Trait_07() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass->publicTe^stMethod();", true);
    }

    public void testTypedProperties20Trait_08() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2->pu^blicTestMethod();", true);
    }

    public void testTypedProperties20Trait_09() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", true);
    }

    public void testTypedProperties20Trait_10() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4->^publicTestMethod();", true);
    }

    public void testTypedProperties20Trait_11() throws Exception {
        checkOccurrences(getTestPath(), "    public function publicTestMe^thod(): void {", true);
    }

    public void testTypedProperties20Trait_12() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass::publicStati^cTestMethod();", true);
    }

    public void testTypedProperties20Trait_13() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2::p^ublicStaticTestMethod();", true);
    }

    public void testTypedProperties20Trait_14() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Trait_15() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Trait_16() throws Exception {
        checkOccurrences(getTestPath(), "    public static function publicStaticTestMe^thod(): void {", true);
    }

}
