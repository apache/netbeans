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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.PhpVersion;
import org.openide.filesystems.FileObject;

public class IntroduceSuggestionTest extends PHPHintsTestBase {

    private static final List<String> USING_FIX_CONTENT_METHOD_TEST_NAMES = Arrays.asList(
            "testIntroduceSuggestion_01"
    );

    public IntroduceSuggestionTest(String testName) {
        super(testName);
    }

    @Override
    protected File getDataFile(String relFilePath) {
        if (USING_FIX_CONTENT_METHOD_TEST_NAMES.contains(getName())) {
            // Overriden because CslTestBase loads file from different location.
            // only when fixContent() method is used
            File inputFile = new File(getDataDir(), relFilePath);
            return inputFile;
        }
        return super.getDataFile(relFilePath);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "IntroduceSuggestion/" + getTestName() + "/";
    }

    private String getTestFileName() {
        return getTestName() + ".php";
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    public void testIntroduceSuggestion_01() throws Exception {
        // Needs to replace directory separators in expected result.
        fixContent(new File(getDataDir(), getTestDirectory() + "testIntroduceSuggestion.php.testIntroduceSuggestion_01.hints"));
        checkHints("new MyClass();^");
    }

    public void testIntroduceSuggestion_02() throws Exception {
        checkHints("$foo->bar;^");
    }

    public void testIntroduceSuggestion_03() throws Exception {
        checkHints("$foo->method();^");
    }

    public void testIntroduceSuggestion_04() throws Exception {
        checkHints("Omg::CON;^");
    }

    public void testIntroduceSuggestion_05() throws Exception {
        checkHints("Omg::stMeth();^");
    }

    public void testIntroduceSuggestion_06() throws Exception {
        checkHints("Omg::$stFld;^");
    }

    public void testIntroduceSuggestion_07() throws Exception {
        checkHints("new class {};^");
    }

    // #257264
    public void testIntroduceSuggestion_Fix01() throws Exception {
        // in case of class, a new file is created
        // applyHint("new MyClass();^", "Create Class");
    }

    public void testIntroduceSuggestion_Fix02() throws Exception {
        applyHint("$foo->bar;^", "Create Field");
    }

    public void testIntroduceSuggestion_Fix03() throws Exception {
        applyHint("$foo->method();^", "Create Method");
    }

    public void testIntroduceSuggestion_Fix04() throws Exception {
        applyHint("Omg::CON;^", "Create Constant");
    }

    public void testIntroduceSuggestion_Fix05() throws Exception {
        applyHint("Omg::stMeth();^", "Create Method");
    }

    public void testIntroduceSuggestion_Fix06() throws Exception {
        applyHint("Omg::$stFld;^", "Create Field");
    }

    public void testSpecialTypes_01() throws Exception {
        checkHints("        return new static;^");
    }

    public void testSpecialTypes_02() throws Exception {
        checkHints("        return new self;^");
    }

    public void testSpecialTypes_03() throws Exception {
        checkHints("        return new parent;^");
    }

    // #257296
    public void testTrait_Field() throws Exception {
        checkHints("$this->field;^");
    }

    public void testTrait_StaticField_01() throws Exception {
        checkHints("self::$staticField1;^");
    }

    public void testTrait_StaticField_02() throws Exception {
        checkHints("TraitA::$staticField2;^");
    }

    public void testTrait_Method() throws Exception {
        checkHints("$this->method();^");
    }

    public void testTrait_Constant() throws Exception {
        // don't show the hint because a trait can't have constants
        checkHints("TraitA::CONSTANT^", PhpVersion.PHP_81);
    }

    public void testTrait_StaticMethod_01() throws Exception {
        checkHints("self::staticMethod1();^");
    }

    public void testTrait_StaticMethod_02() throws Exception {
        checkHints("TraitA::staticMethod2();^");
    }

    public void testTrait_FixField() throws Exception {
        applyHint("$this->field;^", "Create Field");
    }

    public void testTrait_FixStaticField_01() throws Exception {
        applyHint("self::$staticField1;^", "Create Field");
    }

    public void testTrait_FixStaticField_02() throws Exception {
        applyHint("TraitA::$staticField2;^", "Create Field");
    }

    public void testTrait_FixStaticField_03() throws Exception {
        applyHint("TraitB::$staticTraitBField;^", "Create Field");
    }

    public void testTrait_FixStaticField_04() throws Exception {
        applyHint("TraitC::$staticTraitCField;^", "Create Field");
    }

    public void testTrait_FixMethod() throws Exception {
        applyHint("$this->method();^", "Create Method");
    }

    public void testTrait_FixStaticMethod_01() throws Exception {
        applyHint("self::staticMethod1();^", "Create Method");
    }

    public void testTrait_FixStaticMethod_02() throws Exception {
        applyHint("TraitA::staticMethod2();^", "Create Method");
    }

    public void testTrait_FixStaticMethod_03() throws Exception {
        applyHint("TraitB::staticTraitBMethod();^", "Create Method");
    }

    public void testTrait_FixStaticMethod_04() throws Exception {
        applyHint("TraitC::staticTraitCMethod();^", "Create Method");
    }

    public void testIssue223842() throws Exception {
        checkHints("Foo::{\"\"}();^");
    }

    public void testIssue239277_01() throws Exception {
        checkHints("Foo::ahoj(^);");
    }

    public void testIssue239277_02() throws Exception {
        checkHints("Bat::$bar^z;");
    }

    public void testIssue241824_01() throws Exception {
        checkHints("(new \\MyFoo(\"Whatever can be here\"))->myFnc()^;");
    }

    public void testIssue241824_02() throws Exception {
        checkHints("(new \\MyFoo(\"Whatever can be here\"))->notMyFnc()^;");
    }

    public void testEnumCase_01() throws Exception {
        checkHints("ExampleEnum::Ca^se3;");
    }

    public void testEnumCase_02() throws Exception {
        checkHints("BackedEnumInt::Case^3;");
    }

    public void testEnumCase_03() throws Exception {
        checkHints("BackedEnumString::C^ase3;");
    }

    public void testEnumCase_Fix01a() throws Exception {
        applyHint("ExampleEnum::Ca^se3;", "Create Enum Case");
    }

    public void testEnumCase_Fix01b() throws Exception {
        applyHint("ExampleEnum::Ca^se3;", "Create Constant");
    }

    public void testEnumCase_Fix02a() throws Exception {
        applyHint("BackedEnumInt::Case^3;", "Create Enum Case");
    }

    public void testEnumCase_Fix02b() throws Exception {
        applyHint("BackedEnumInt::Case^3;", "Create Constant");
    }

    public void testEnumCase_Fix03a() throws Exception {
        applyHint("BackedEnumString::C^ase3;", "Create Enum Case");
    }

    public void testEnumCase_Fix03b() throws Exception {
        applyHint("BackedEnumString::C^ase3;", "Create Constant");
    }

    public void testEnumMethods_01() throws Exception {
        checkHints("ExampleEnum::introduceStat^icMethod();");
    }

    public void testEnumMethods_02() throws Exception {
        checkHints("        $this->introduceMeth^od();");
    }

    public void testEnumMethods_03() throws Exception {
        checkHints("        self::introduceStaticMet^hod();");
    }

    public void testEnumMethods_04() throws Exception {
        checkHints("        static::introduceStaticMeth^od();");
    }

    public void testEnumMethods_05() throws Exception {
        checkHints("BackedEnumInt::Case1->introduceMet^hod();");
    }

    public void testEnumMethods_06() throws Exception {
        checkHints("BackedEnumString::Case2::introdu^ceStaticMethod();");
    }

    public void testEnumMethods_Fix01() throws Exception {
        applyHint("ExampleEnum::introduceStat^icMethod();", "Create Method");
    }

    public void testEnumMethods_Fix02() throws Exception {
        applyHint("        $this->introduceMeth^od();", "Create Method");
    }

    public void testEnumMethods_Fix03() throws Exception {
        applyHint("        self::introduceStaticMet^hod();", "Create Method");
    }

    public void testEnumMethods_Fix04() throws Exception {
        applyHint("        static::introduceStaticMeth^od();", "Create Method");
    }

    public void testEnumMethods_Fix05() throws Exception {
        applyHint("BackedEnumInt::Case1->introduceMet^hod();", "Create Method");
    }

    public void testEnumMethods_Fix06() throws Exception {
        applyHint("BackedEnumString::Case2::introdu^ceStaticMethod();", "Create Method");
    }

    public void testGH6258_01() throws Exception {
        checkHints("testGH6258_01.php", "TestClass::generatedMeth^od();");
    }

    public void testGH6258_01Fix() throws Exception {
        // no changes because the method is created into the TestClass
        // check that NPE doesn't occur
        applyHint("testGH6258_01.php", "TestClass::generatedMeth^od();", "Create Method");
    }

    public void testGH6258_02() throws Exception {
        checkHints("testGH6258_02.php", "$test->generatedMet^hod();");
    }

    public void testGH6258_02Fix() throws Exception {
        // no changes because the method is created into the TestClass
        // check that NPE doesn't occur
        applyHint("testGH6258_02.php", "$test->generatedMetho^d();", "Create Method");
    }

    public void testGH6266_Const() throws Exception {
        checkHints("TestClass::CONSTA^NT;");
    }

    public void testGH6266_ConstFix() throws Exception {
        // no changes because the const is created into the TestClass
        // check that NPE doesn't occur
        applyHint("TestClass::CONSTAN^T;", "Create Constant");
    }

    public void testGH6266_StaticField() throws Exception {
        checkHints("TestClass::$staticFiel^d;");
    }

    public void testGH6266_StaticFieldFix() throws Exception {
        // no changes because the field is created into the TestClass
        // check that NPE doesn't occur
        applyHint("TestClass::$staticFiel^d;", "Create Field");
    }

    public void testGH6266_Field() throws Exception {
        checkHints("$test->fie^ld;");
    }

    public void testGH6266_FieldFix() throws Exception {
        // no changes because the field is created into the TestClass
        // check that NPE doesn't occur
        applyHint("$test->fiel^d;", "Create Field");
    }

    public void testConstantsInTraits01_PHP81() throws Exception {
        checkHints("        echo self::CONST^ANT;", PhpVersion.PHP_81);
    }

    public void testConstantsInTraits01_PHP82() throws Exception {
        checkHints("        echo self::CONST^ANT;", PhpVersion.PHP_82);
    }

    public void testConstantsInTraits02_PHP82() throws Exception {
        checkHints("        echo self::CONST^ANT;", PhpVersion.PHP_82);
    }

    public void testConstantsInTraits03_PHP82() throws Exception {
        checkHints("        echo self::CONST^ANT;", PhpVersion.PHP_82);
    }

    public void testConstantsInTraits01_PHP82Fix() throws Exception {
        applyHint("        echo self::CONST^ANT;", "Create Constant", PhpVersion.PHP_82);
    }

    public void testDynamicClassConstantFetch_01() throws Exception {
        checkHints("    public const A = self::{'B^AR'};");
    }

    public void testDynamicClassConstantFetch_02() throws Exception {
        checkHints("    public const B = self::{self::{'B^A'} . 'R'};");
    }

    public void testDynamicClassConstantFetch_03() throws Exception {
        checkHints("    public const C = self::{self::BA . self::^R};");
    }

    public void testDynamicClassConstantFetch_03Fix() throws Exception {
        applyHint("    public const C = self::{self::BA . self::^R};", "Create Constant");
    }

    public void testDynamicClassConstantFetch_04() throws Exception {
        checkHints("Test::{\"BA^R\"};");
    }

    public void testDynamicClassConstantFetch_05() throws Exception {
        checkHints("$test::{\"BA^R\"};");
    }

    public void testDynamicClassConstantFetch_06() throws Exception {
        checkHints("Test::{test()}::{tes^t($bar)};");
    }

    public void testDynamicClassConstantFetch_07() throws Exception {
        checkHints("Test::{test('foo')}::FO^O;");
    }

    public void testDynamicClassConstantFetch_Trait01() throws Exception {
        checkHints("    public const TRAIT_B = self::{self::{'B^A'} . 'R'};");
    }

    public void testDynamicClassConstantFetch_Trait02() throws Exception {
        checkHints("    public const TRAIT_C = self::{self::TRAIT_BA . self::TRA^IT_R};");
    }

    public void testDynamicClassConstantFetch_Trait02Fix() throws Exception {
        applyHint("    public const TRAIT_C = self::{self::TRAIT_BA . self::TRAI^T_R};", "Create Constant");
    }

    public void testDynamicClassConstantFetch_EnumCase01() throws Exception {
        checkHints("    case A = self::{'BA^R'};");
    }

    public void testDynamicClassConstantFetch_EnumCase02() throws Exception {
        checkHints("    case B = self::{self::{'B^A'} . 'R'};");
    }

    public void testDynamicClassConstantFetch_EnumCase03() throws Exception {
        checkHints("    case C = self::{self::BA . self::^R};");
    }

    public void testDynamicClassConstantFetch_EnumCase03aFix() throws Exception {
        applyHint("    case C = self::{self::BA . self::^R};", "Create Constant");
    }

    public void testDynamicClassConstantFetch_EnumCase03bFix() throws Exception {
        applyHint("    case C = self::{self::BA . self::^R};", "Create Enum Case");
    }

    public void testDynamicClassConstantFetch_EnumCase04() throws Exception {
        checkHints("EnumTest::{\"BA^R\"};");
    }

    public void testDynamicClassConstantFetch_EnumCase05() throws Exception {
        checkHints("EnumTest::{test()}::{tes^t($bar)};");
    }

    public void testDynamicClassConstantFetch_EnumCase06() throws Exception {
        checkHints("EnumTest::{tes^t('foo')}::FOO;");
    }

    private void checkHints(String caretLine) throws Exception {
        checkHints(new IntroduceSuggestion(), getTestFileName(), caretLine);
    }

    private void checkHints(String caretLine, PhpVersion phpVersion) throws Exception {
        checkHints(new IntroduceSuggestionStub(phpVersion), getTestFileName(), caretLine);
    }

    private void checkHints(String fileName, String caretLine) throws Exception {
        checkHints(new IntroduceSuggestion(), fileName, caretLine);
    }

    private void applyHint(String caretLine, String fixDesc) throws Exception {
        applyHint(new IntroduceSuggestion(), getTestFileName(), caretLine, fixDesc);
    }

    private void applyHint(String caretLine, String fixDesc, PhpVersion phpVersion) throws Exception {
        applyHint(new IntroduceSuggestionStub(phpVersion), getTestFileName(), caretLine, fixDesc);
    }

    private void applyHint(String fileName, String caretLine, String fixDesc) throws Exception {
        applyHint(new IntroduceSuggestion(), fileName, caretLine, fixDesc);
    }

    private void fixContent(File file) throws Exception {
        Path path = file.toPath();
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replace("%SEP%", File.separator);
        Files.write(path, content.getBytes(charset));
    }

    private static class IntroduceSuggestionStub extends IntroduceSuggestion {

        private final PhpVersion phpVersion;

        public IntroduceSuggestionStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject fileObject) {
            return phpVersion;
        }
    }

}
