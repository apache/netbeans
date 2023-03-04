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


public class OccurrencesFinderImplVardocTest extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplVardocTest(String testName) {
        super(testName);
    }

    public void testVardoc_01() throws Exception {
        checkOccurrences(getTestPath(), "class V^arType", true);
    }

    public void testVardoc_02() throws Exception {
        checkOccurrences(getTestPath(), "/** @var VarT^ype $varType */", true);
    }

    public void testVardoc_03() throws Exception {
        checkOccurrences(getTestPath(), "/** @var    Var^Type    $value */", true);
    }

    public void testVardoc_04() throws Exception {
        checkOccurrences(getTestPath(), "/* @var  $varType2   VarT^ype  */", true);
    }

    public void testVardoc_05() throws Exception {
        checkOccurrences(getTestPath(), "    public function tes^t()", true);
    }

    public void testVardoc_06() throws Exception {
        checkOccurrences(getTestPath(), "$varType->tes^t();", true);
    }

    public void testVardoc_07() throws Exception {
        checkOccurrences(getTestPath(), "$varType2->te^st();", true);
    }

    public void testVardoc_08() throws Exception {
        checkOccurrences(getTestPath(), "    $value->^test();", true);
    }

    public void testVardoc_09() throws Exception {
        checkOccurrences(getTestPath(), "/** @var VarType $va^rType */", true);
    }

    public void testVardoc_10() throws Exception {
        checkOccurrences(getTestPath(), "$^varType = getVarType();", true);
    }

    public void testVardoc_11() throws Exception {
        checkOccurrences(getTestPath(), "$varT^ype->test();", true);
    }

    public void testVardoc_12() throws Exception {
        checkOccurrences(getTestPath(), "/** @var    VarType    $val^ue */", true);
    }

    public void testVardoc_13() throws Exception {
        checkOccurrences(getTestPath(), "foreach ($array as $^value) {", true);
    }

    public void testVardoc_14() throws Exception {
        checkOccurrences(getTestPath(), "    $val^ue->test();", true);
    }

    public void testVardoc_15() throws Exception {
        checkOccurrences(getTestPath(), "/* @var  $var^Type2   VarType  */", true);
    }

    public void testVardoc_16() throws Exception {
        checkOccurrences(getTestPath(), "$varT^ype2 = getVarType();", true);
    }

    public void testVardoc_17() throws Exception {
        checkOccurrences(getTestPath(), "$varTyp^e2->test();", true);
    }

}
