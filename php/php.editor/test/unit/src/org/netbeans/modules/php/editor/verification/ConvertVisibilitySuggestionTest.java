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
package org.netbeans.modules.php.editor.verification;

public class ConvertVisibilitySuggestionTest extends PHPHintsTestBase {

    public ConvertVisibilitySuggestionTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "ConvertVisibilitySuggestion/";
    }

    public void testConvertVisibilitySuggestionProperty_01() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public $public = ^\"public\";");
    }

    public void testConvertVisibilitySuggestionProperty_02() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public $public1 = \"public\", ^$public2 = \"public\";");
    }

    public void testConvertVisibilitySuggestionProperty_03() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    private $privat^e = \"private\";");
    }

    public void testConvertVisibilitySuggestionProperty_04() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    protected $prote^cted = \"protected\";");
    }

    public void testConvertVisibilitySuggestionProperty_05() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public static $^publicStatic = \"public\";");
    }

    public void testConvertVisibilitySuggestionProperty_06() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public static ^$publicStatic1 = \"public\", $publicStatic2 = \"public\";");
    }

    public void testConvertVisibilitySuggestionProperty_07() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    private static $privateStatic = \"privat^e\";");
    }

    public void testConvertVisibilitySuggestionProperty_08() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    protected static $protectedStatic = \"protect^ed\";");
    }

    public void testConvertVisibilitySuggestionProperty_09() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    var $var ^= \"public\";");
    }

    public void testConvertVisibilitySuggestionConst_01() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    const IMPLICIT_CO^NST = \"implicit\";");
    }

    public void testConvertVisibilitySuggestionConst_02() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public const PUBLIC_CONST = \"pub^lic\";");
    }

    public void testConvertVisibilitySuggestionConst_03() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    private const PRIVATE_CONST = \"priva^te\";");
    }

    public void testConvertVisibilitySuggestionConst_04() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    protected const ^PROTECTED_CONST = \"protected\";");
    }

    public void testConvertVisibilitySuggestionMethod_01() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    function implicitMethod($param)^ {");
    }

    public void testConvertVisibilitySuggestionMethod_02() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public function publicMethod($para^m) {");
    }

    public void testConvertVisibilitySuggestionMethod_03() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    private function privateMethod($param) {^");
    }

    public void testConvertVisibilitySuggestionMethod_04() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    protected ^function protectedMethod($param) {");
    }

    public void testConvertVisibilitySuggestionMethod_05() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    static function implicitStaticMethod($pa^ram) {");
    }

    public void testConvertVisibilitySuggestionMethod_06() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public static function publicStaticMe^thod($param) {");
    }

    public void testConvertVisibilitySuggestionMethod_07() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    private static ^function privateStaticMethod($param) {");
    }

    public void testConvertVisibilitySuggestionMethod_08() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    prote^cted static function protectedStaticMethod($param) {");
    }

    public void testConvertVisibilitySuggestionInterfaceConst_01() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    const INTERFACE_IMPLICIT_CONST = \"implici^t\";");
    }

    public void testConvertVisibilitySuggestionInterfaceConst_02() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public const INTERFACE_P^UBLIC_CONST = \"implicit\";");
    }

    public void testConvertVisibilitySuggestionInterfaceMethod_01() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    function interfaceImplicitMethod($para^m);");
    }

    public void testConvertVisibilitySuggestionInterfaceMethod_02() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public ^function interfacePublicMethod($param);");
    }

    public void testConvertVisibilitySuggestionInterfaceMethod_03() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    static function interfaceImplicitStaticMeth^od();");
    }

    public void testConvertVisibilitySuggestionInterfaceMethod_04() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    public ^static function interfaceImplicitPublicStaticMethod();");
    }

    public void testConvertVisibilitySuggestionGlobalConst_01() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "const CON^STANT = \"global\";");
    }

    public void testConvertVisibilitySuggestionAbstractMethod_01() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    abstract function abstractImplici^tPublic();");
    }

    public void testConvertVisibilitySuggestionAbstractMethod_02() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    abstract public function abstractP^ublic();");
    }

    public void testConvertVisibilitySuggestionAbstractMethod_03() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    abstract protected ^function abstractProtected();");
    }

    public void testConvertVisibilitySuggestionAbstractMethod_04() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    abstract static function abstractImplicitPu^blicStatic();");
    }

    public void testConvertVisibilitySuggestionAbstractMethod_05() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    abstract ^public static function abstractPublicStatic();");
    }

    public void testConvertVisibilitySuggestionAbstractMethod_06() throws Exception {
        checkHints(new ConvertVisibilitySuggestion(), "testConvertVisibility.php", "    abstract protected static function abstractProtectedStat^ic();");
    }

    // Fix
    public void testConvertVisibilitySuggestionPropertyFix_01() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    var ^$var = \"public\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionPropertyFix_02() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    public ^$public = \"public\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionPropertyFix_03() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    public $public1 = \"public\", $public2^ = \"public\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionPropertyFix_04() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    private ^$private = \"private\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionPropertyFix_05() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    protected $protected = \"pr^otected\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionPropertyFix_06() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    public static $publicStat^ic = \"public\";", "protected");
    }

    public void testConvertVisibilitySuggestionPropertyFix_07() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    public static $publicStatic1 = \"public\", $publicStatic2 ^= \"public\";", "private");
    }

    public void testConvertVisibilitySuggestionPropertyFix_08() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    private static $privateStatic = \"priva^te\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionPropertyFix_09() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityProperty.php", "    protected ^static $protectedStatic = \"protected\";", "private");
    }

    public void testConvertVisibilitySuggestionConstFix_01() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityConst.php", "    const IMPLICIT_CO^NST = \"implicit\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionConstFix_02() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityConst.php", "    public const PUBLIC_CONST = ^\"public\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionConstFix_03() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityConst.php", "    private const PRIVATE_CONST^ = \"private\";", "protected");
    }

    public void testConvertVisibilitySuggestionConstFix_04() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityConst.php", "    protected const PROTECTED_^CONST = \"protected\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionMethodFix_01() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    function implicitMeth^od($param) {", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionMethodFix_02() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    public function publicMethod($^param) {", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionMethodFix_03() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    private function privat^eMethod($param) {", "protected");
    }

    public void testConvertVisibilitySuggestionMethodFix_04() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    protected fu^nction protectedMethod($param) {", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionMethodFix_05() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    static function implicitStaticMethod($para^m) {", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionMethodFix_06() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    public static fun^ction publicStaticMethod($param) {", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionMethodFix_07() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    private static function privateStat^icMethod($param) {", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionMethodFix_08() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityMethod.php", "    protected static function protecte^dStaticMethod($param) {", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionInterfaceFix_01() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityInterface.php", "    const INTERFACE_IMPLICIT_CONST = \"impli^cit\";", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionInterfaceFix_02() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityInterface.php", "    function interfaceImp^licitMethod($param);", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionInterfaceFix_03() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityInterface.php", "    static function interfaceImplicitStaticMetho^d();", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionAbstractClassFix_01() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityAbstractClass.php", "    abstract function abst^ractImplicitPublic();", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionAbstractClassFix_02() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityAbstractClass.php", "    abstract public ^function abstractPublic();", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionAbstractClassFix_03() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityAbstractClass.php", "    abstract protected function ^abstractProtected();", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionAbstractClassFix_04() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityAbstractClass.php", "    abstract ^static function abstractImplicitPublicStatic();", "protected");
    }

    public void testConvertVisibilitySuggestionAbstractClassFix_05() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityAbstractClass.php", "    abstract public s^tatic function abstractPublicStatic();", "Convert Visibility");
    }

    public void testConvertVisibilitySuggestionAbstractClassFix_06() throws Exception {
        applyHint(new ConvertVisibilitySuggestion(), "testConvertVisibilityAbstractClass.php", "    abstract protected static function abs^tractProtectedStatic();", "Convert Visibility");
    }

}
