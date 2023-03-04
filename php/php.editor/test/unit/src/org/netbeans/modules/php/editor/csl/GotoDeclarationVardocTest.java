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

public class GotoDeclarationVardocTest extends GotoDeclarationTestBase {

    public GotoDeclarationVardocTest(String testName) {
        super(testName);
    }

    public void testVardoc_01() throws Exception {
        checkDeclaration(getTestPath(), "/** @var ^VarType $varType */", "class ^VarType");
    }

    public void testVardoc_02() throws Exception {
        checkDeclaration(getTestPath(), "/* @var  $varType2   VarT^ype  */", "class ^VarType");
    }

    public void testVardoc_03() throws Exception {
        checkDeclaration(getTestPath(), "/** @var    VarTy^pe    $value */", "class ^VarType");
    }

    public void testVardoc_04() throws Exception {
        checkDeclaration(getTestPath(), "$varType->tes^t();", "    public function ^test()");
    }

    public void testVardoc_05() throws Exception {
        checkDeclaration(getTestPath(), "$varType2->^test();", "    public function ^test()");
    }

    public void testVardoc_06() throws Exception {
        checkDeclaration(getTestPath(), "    $value->te^st();", "    public function ^test()");
    }

    public void testVardoc_07() throws Exception {
        checkDeclaration(getTestPath(), "/** @var VarType $var^Type */", "$^varType = getVarType();");
    }

    public void testVardoc_08() throws Exception {
        checkDeclaration(getTestPath(), "$var^Type->test();", "$^varType = getVarType();");
    }

    public void testVardoc_09() throws Exception {
        checkDeclaration(getTestPath(), "/* @var  $varT^ype2   VarType  */", "$^varType2 = getVarType();");
    }

    public void testVardoc_10() throws Exception {
        checkDeclaration(getTestPath(), "$varT^ype2->test();", "$^varType2 = getVarType();");
    }

    public void testVardoc_11() throws Exception {
        checkDeclaration(getTestPath(), "/** @var    VarType    $va^lue */", "foreach ($array as $^value) {");
    }

    public void testVardoc_12() throws Exception {
        checkDeclaration(getTestPath(), "    $va^lue->test();", "foreach ($array as $^value) {");
    }

}
