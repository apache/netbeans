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
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHP82CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP82CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php82/" + getTestDirName()))
            })
        );
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php82/%s/%s.php", getTestDirName(), fileName);
    }

    private void checkCompletion(String fileName, String caretPosition) throws Exception {
        checkCompletion(getTestPath(fileName), caretPosition, false);
    }

    public void testNullAndFalseType_01() throws Exception {
        checkCompletion("nullAndFalseType", "    public nu^ll $null = null; // PHP 8.2: OK");
    }

    public void testNullAndFalseType_02() throws Exception {
        checkCompletion("nullAndFalseType", "    public fal^se $false = false; // PHP 8.2: OK");
    }

    public void testNullAndFalseType_03() throws Exception {
        checkCompletion("nullAndFalseType", "    public ?fals^e $false2 = null; // PHP 8.2: OK");
    }

    public void testNullAndFalseType_04() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNull(nu^ll $null): null {");
    }

    public void testNullAndFalseType_05() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNull(null $null): nu^ll {");
    }

    public void testNullAndFalseType_06() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testFalse(fal^se $false): false {");
    }

    public void testNullAndFalseType_07() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testFalse(false $false): fal^se {");
    }

    public void testNullAndFalseType_08() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNullableFalse(?fal^se $false): ?false {");
    }

    public void testNullAndFalseType_09() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNullableFalse(?false $false): ?fal^se {");
    }

    public void testTrueType_01() throws Exception {
        checkCompletion("trueType", "    public tru^e $true = true; // PHP 8.2: OK");
    }

    public void testTrueType_02() throws Exception {
        checkCompletion("trueType", "    public ?tru^e $true2 = true; // PHP 8.2: OK");
    }

    public void testTrueType_03() throws Exception {
        checkCompletion("trueType", "    public int|tr^ue $true3 = true; // line comment");
    }

    public void testTrueType_04() throws Exception {
        checkCompletion("trueType", "    public tru^e|int $true4 = true; // line comment");
    }

    public void testTrueType_05() throws Exception {
        checkCompletion("trueType", "    public function test(tr^ue $true): true {");
    }

    public void testTrueType_06() throws Exception {
        checkCompletion("trueType", "    public function test(true $true): tru^e {");
    }

    public void testTrueType_07() throws Exception {
        checkCompletion("trueType", "    public function testNullable(?tr^ue $true): ?true {");
    }

    public void testTrueType_08() throws Exception {
        checkCompletion("trueType", "    public function testNullable(?true $true): ?tr^ue {");
    }

    public void testTrueType_09() throws Exception {
        checkCompletion("trueType", "    public function testUnionType(tr^ue|string $true): string|true {");
    }

    public void testTrueType_10() throws Exception {
        checkCompletion("trueType", "    public function testUnionType(true|string $true): string|tru^e {");
    }

    public void testReadonlyClasses_01() throws Exception {
        checkCompletion("readonlyClasses", "readon^ly class ReadonlyClass {");
    }

    public void testReadonlyClasses_02() throws Exception {
        checkCompletion("readonlyClasses", "readonl^y final class ReadonlyFinalClass {");
    }

    public void testReadonlyClasses_03() throws Exception {
        checkCompletion("readonlyClasses", "final read^only class FinalReadonlyClass {");
    }

    public void testReadonlyClasses_04() throws Exception {
        checkCompletion("readonlyClasses", "rea^donly abstract class ReadonlyAbstractClass {");
    }

    public void testReadonlyClasses_05() throws Exception {
        checkCompletion("readonlyClasses", "abstract reado^nly class AbstractReadonlyClass {");
    }

    public void testReadonlyClassesTyping01() throws Exception {
        checkCompletion("readonlyClassesTyping01", "readon^");
    }

    public void testReadonlyClassesTyping02() throws Exception {
        checkCompletion("readonlyClassesTyping02", "final readon^");
    }

    public void testReadonlyClassesTyping03() throws Exception {
        checkCompletion("readonlyClassesTyping03", "abstract readon^");
    }

    public void testFetchPropertiesInConstExpressions_01a() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const C1 = [self::Case->^value => self::Case];");
    }

    public void testFetchPropertiesInConstExpressions_01b() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const C1 = [self::Case->va^lue => self::Case];");
    }

    public void testFetchPropertiesInConstExpressions_02() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const C2 = [self::Case?->val^ue => self::Case];");
    }

    public void testFetchPropertiesInConstExpressions_03() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "const NAME = E::Case->na^me;");
    }

    public void testFetchPropertiesInConstExpressions_04() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "const VALUE_NULLSAFE = E::Case?->valu^e;");
    }

    public void testFetchPropertiesInConstExpressions_05() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const VALUE = E::Case->va^lue;");
    }

    public void testFetchPropertiesInConstExpressions_06() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const NAME_NULLSAFE = E::Case?->nam^e;");
    }

    public void testFetchPropertiesInConstExpressions_07() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    public string $name = E::Case->na^me;");
    }

    public void testFetchPropertiesInConstExpressions_08() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    $valueNullsafe = E::Case?->va^lue,");
    }

    public void testFetchPropertiesInConstExpressions_09() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    static $staticName = E::Case->na^me;");
    }

    public void testFetchPropertiesInConstExpressions_10() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    case VALUE = E::Case->valu^e;");
    }

    public void testConstantsInTraits_01() throws Exception {
        // no constant items because T::CONSTANT is invalid
        checkCompletion("constantsInTraits", "echo ExampleTrait::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // fatal error");
    }

    public void testConstantsInTraits_02a() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_02b() throws Exception {
        checkCompletion("constantsInTraits", "echo self::IMPLICIT_PUBLIC^_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_03a() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PRIVATE_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_03b() throws Exception {
        checkCompletion("constantsInTraits", "echo static::PRIVATE^_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_04a() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_04b() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::PROTECTED_TR^AIT . PHP_EOL;");
    }

    public void testConstantsInTraits_05() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT2 . PHP_EOL;");
    }

    public void testConstantsInTraits_06() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PRIVATE_TRAIT2 . PHP_EOL;");
    }

    public void testConstantsInTraits_07() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT2 . PHP_EOL;");
    }

    public void testConstantsInTraits_08() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // class");
    }

    public void testConstantsInTraits_09() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PRIVATE_TRAIT . PHP_EOL; // class");
    }

    public void testConstantsInTraits_10() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT . PHP_EOL; // class");
    }

    public void testConstantsInTraits_11() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_12() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PUBLIC_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_13() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_14() throws Exception {
        checkCompletion("constantsInTraits", "echo parent::^PUBLIC_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_15() throws Exception {
        checkCompletion("constantsInTraits", "echo ExampleClass::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_16() throws Exception {
        checkCompletion("constantsInTraits", "$i::^PUBLIC_TRAIT;");
    }

    public void testConstantsInTraits_17() throws Exception {
        checkCompletion("constantsInTraits", "$c::^PUBLIC_TRAIT;");
    }

    public void testDNFTypes_MethodReturnType01() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()->^publicXField;");
    }

    public void testDNFTypes_MethodReturnType02a() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()->publicXMethod()->^publicYField;");
    }

    public void testDNFTypes_MethodReturnType02b() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()->publicXMethod()->publicY^Field;");
    }

    public void testDNFTypes_MethodReturnType03() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()?->publicYMethod()?->^publicXField;");
    }

    public void testDNFTypes_MethodReturnType04() throws Exception {
        checkCompletion("dnfTypes", "        $this->privateTraitMethod()->^publicXField;");
    }

    public void testDNFTypes_MethodReturnType05() throws Exception {
        checkCompletion("dnfTypes", "        $this->protectedTraitMethod()->publicYMethod()->^publicXField;");
    }

    public void testDNFTypes_MethodReturnType06() throws Exception {
        checkCompletion("dnfTypes", "        $this->phpdocReturnType()->^publicXField;");
    }

    public void testDNFTypes_MethodReturnType07() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()::^IMPLICIT_X_CONSTANT;");
    }

    public void testDNFTypes_MethodReturnType08() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()::publicStaticXMethod()::^$publicStaticXField;");
    }

    public void testDNFTypes_MethodReturnType09a() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()::publicStaticXMethod()->^publicXField;");
    }

    public void testDNFTypes_MethodReturnType09b() throws Exception {
        checkCompletion("dnfTypes", "        $this->returnType()::publicStaticXMethod()->publicXField^;");
    }

    public void testDNFTypes_MethodReturnType10() throws Exception {
        checkCompletion("dnfTypes", "        $this->privateTraitMethod()::^publicStaticXMethod();");
    }

    public void testDNFTypes_MethodReturnType11() throws Exception {
        checkCompletion("dnfTypes", "        $this->methodTag()->^publicXMethod();");
    }

    public void testDNFTypes_MethodReturnType12() throws Exception {
        checkCompletion("dnfTypes", "        self::publicStaticMethod()::^IMPLICIT_X_CONSTANT;");
    }

    public void testDNFTypes_MethodReturnType13a() throws Exception {
        checkCompletion("dnfTypes", "        self::publicStaticMethod()::publicStaticXMethod()::^publicStaticYMethod();");
    }

    public void testDNFTypes_MethodReturnType13b() throws Exception {
        checkCompletion("dnfTypes", "        self::publicStaticMethod()::publicStaticXMethod()::publicStaticYM^ethod();");
    }

    public void testDNFTypes_MethodReturnType14() throws Exception {
        checkCompletion("dnfTypes", "        self::publicStaticMethod()::publicStaticXMethod()->^publicXMethod();");
    }

    public void testDNFTypes_MethodReturnType15() throws Exception {
        checkCompletion("dnfTypes", "        static::publicStaticMethod()->^publicZMethod();");
    }

    public void testDNFTypes_MethodReturnType16() throws Exception {
        checkCompletion("dnfTypes", "        static::publicStaticMethod()->publicZMethod()::^$publicStaticXField;");
    }

    public void testDNFTypes_MethodReturnType17() throws Exception {
        checkCompletion("dnfTypes", "        static::publicStaticMethod()->publicZMethod()->^publicYMethod();");
    }

    public void testDNFTypes_MethodReturnType18() throws Exception {
        checkCompletion("dnfTypes", "        static::publicStaticTraitMethod()->publicXMethod()->^publicXField;");
    }

    public void testDNFTypes_FieldType01() throws Exception {
        checkCompletion("dnfTypes", "        $this->privateFiled->^publicXMethod();");
    }

    public void testDNFTypes_FieldType02a() throws Exception {
        checkCompletion("dnfTypes", "        $this->publicFiled->publicXMethod()->^publicYField;");
    }

    public void testDNFTypes_FieldType02b() throws Exception {
        checkCompletion("dnfTypes", "        $this->publicFiled->publicXMethod()->publicY^Field;");
    }

    public void testDNFTypes_FieldType03() throws Exception {
        checkCompletion("dnfTypes", "        $this->protectedFiled::^PUBLIC_Z_CONSTANT;");
    }

    public void testDNFTypes_FieldType04() throws Exception {
        checkCompletion("dnfTypes", "        $this->publicPhpdocField->^publicXMethod();");
    }

    public void testDNFTypes_FieldType05() throws Exception {
        checkCompletion("dnfTypes", "        $this->publicPhpdocField::^$publicStaticXField;");
    }

    public void testDNFTypes_FieldType06() throws Exception {
        checkCompletion("dnfTypes", "        $this->protectedTraitField->^publicXMethod();");
    }

    public void testDNFTypes_FieldType07() throws Exception {
        checkCompletion("dnfTypes", "        $this->privatePromotedFiled->^publicYMethod();");
    }

    public void testDNFTypes_FieldType08() throws Exception {
        checkCompletion("dnfTypes", "        $this->propertyTag->^publicYMethod();");
    }

    public void testDNFTypes_FieldType09() throws Exception {
        checkCompletion("dnfTypes", "        static::$privateStaticField::^IMPLICIT_Y_CONSTANT;");
    }

    public void testDNFTypes_FieldType10a() throws Exception {
        checkCompletion("dnfTypes", "        static::$privateStaticField?->^publicStaticZMethod();");
    }

    public void testDNFTypes_FieldType10b() throws Exception {
        checkCompletion("dnfTypes", "        static::$privateStaticField?->publicStaticZ^Method();");
    }

    public void testDNFTypes_FieldType11() throws Exception {
        checkCompletion("dnfTypes", "        self::$publicPhpdocStaticField::^$publicStaticXField;");
    }

    public void testDNFTypes_FieldType12() throws Exception {
        checkCompletion("dnfTypes", "        self::$publicPhpdocStaticField->^publicZField;");
    }

    public void testDNFTypes_FieldType13() throws Exception {
        checkCompletion("dnfTypes", "        self::$publicStaticTraitField->^publicXMethod();");
    }

    public void testDNFTypes_FunctionReturnType01() throws Exception {
        checkCompletion("dnfTypes", "testFunctionReturnType()->^publicXField;");
    }

    public void testDNFTypes_FunctionReturnType02() throws Exception {
        checkCompletion("dnfTypes", "testFunctionReturnType()::^PUBLIC_X_CONSTANT;");
    }

    public void testDNFTypes_FunctionReturnType03() throws Exception {
        checkCompletion("dnfTypes", "$testFunctionReturnType->^publicXMethod();");
    }

    public void testDNFTypes_FunctionReturnType04a() throws Exception {
        checkCompletion("dnfTypes", "$testFunctionReturnType::^IMPLICIT_Z_CONSTANT();");
    }

    public void testDNFTypes_FunctionReturnType04b() throws Exception {
        checkCompletion("dnfTypes", "$testFunctionReturnType::IMPLICIT_Z_^CONSTANT();");
    }

    public void testDNFTypes_VarDocType01a() throws Exception {
        checkCompletion("dnfTypes", "$vardoc1->^publicXField;");
    }

    public void testDNFTypes_VarDocType01b() throws Exception {
        checkCompletion("dnfTypes", "$vardoc1::^$publicStaticXField;");
    }

    public void testDNFTypes_VarDocType02a() throws Exception {
        checkCompletion("dnfTypes", "$vardoc2->^publicXField;");
    }

    public void testDNFTypes_VarDocType02b() throws Exception {
        checkCompletion("dnfTypes", "$vardoc2::^$publicStaticXField;");
    }

    public void testDNFTypes_VarDocType03a() throws Exception {
        checkCompletion("dnfTypes", "$vardoc3->^publicXField;");
    }

    public void testDNFTypes_VarDocType03b() throws Exception {
        checkCompletion("dnfTypes", "$vardoc3::^$publicStaticXField;");
    }

    public void testDNFTypes_VarDocType04a() throws Exception {
        checkCompletion("dnfTypes", "$vardoc4->^publicXField;");
    }

    public void testDNFTypes_VarDocType04b() throws Exception {
        checkCompletion("dnfTypes", "$vardoc4::^$publicStaticXField;");
    }

    public void testDNFTypes_VarDocType05a() throws Exception {
        checkCompletion("dnfTypes", "$vardoc5->^publicXField;");
    }

    public void testDNFTypes_VarDocType05b() throws Exception {
        checkCompletion("dnfTypes", "$vardoc5::^$publicStaticXField;");
    }

    public void testDNFTypes_VarDocType06a() throws Exception {
        checkCompletion("dnfTypes", "$vardoc6->^publicXField;");
    }

    public void testDNFTypes_VarDocType06b() throws Exception {
        checkCompletion("dnfTypes", "$vardoc6::^$publicStaticXField;");
    }

    public void testDNFTypes_ParameterType01() throws Exception {
        checkCompletion("dnfTypes", "$publicPromotedFiled->^publicYMethod();");
    }

    public void testDNFTypes_ParameterType02() throws Exception {
        checkCompletion("dnfTypes", "$publicPromotedFiled->publicYMethod()->^publicXMethod();");
    }

    public void testDNFTypes_ParameterType03() throws Exception {
        checkCompletion("dnfTypes", "$publicPromotedFiled->publicYMethod()::^$publicStaticXField;");
    }

    public void testDNFTypes_ParameterType04() throws Exception {
        checkCompletion("dnfTypes", "$publicPromotedFiled::^publicStaticYMethod();");
    }

    public void testDNFTypes_ParameterType05() throws Exception {
        checkCompletion("dnfTypes", "$publicPromotedFiled::$publicStaticYField->^publicYField;");
    }

    public void testDNFTypes_ParameterType06() throws Exception {
        checkCompletion("dnfTypes", "$publicPromotedFiled::$publicStaticYField::^PUBLIC_Z_CONSTANT;");
    }

    public void testDNFTypes_ParameterType07() throws Exception {
        checkCompletion("dnfTypes", "$privatePromotedFiled->^publicXField;");
    }

    public void testDNFTypes_ParameterType08() throws Exception {
        checkCompletion("dnfTypes", "$privatePromotedFiled->publicXField->^publicZMethod();");
    }

    public void testDNFTypes_ParameterType09() throws Exception {
        checkCompletion("dnfTypes", "$protectedPromotedFiled->^publicXMethod();");
    }

    public void testDNFTypes_ParameterType10() throws Exception {
        checkCompletion("dnfTypes", "$protectedPromotedFiled::^$publicStaticZField()");
    }

    public void testDNFTypes_ParameterType11() throws Exception {
        checkCompletion("dnfTypes", "$param1->^publicXMethod();");
    }

    public void testDNFTypes_ParameterType12() throws Exception {
        checkCompletion("dnfTypes", "$param1->publicYMethod()->^publicZMethod();");
    }

    public void testDNFTypes_ParameterType13() throws Exception {
        checkCompletion("dnfTypes", "$param1->publicXMethod()::^PUBLIC_Y_CONSTANT;");
    }

    public void testDNFTypes_ParameterType14() throws Exception {
        checkCompletion("dnfTypes", "$param1::^publicStaticYMethod();");
    }

    public void testDNFTypes_ParameterType15() throws Exception {
        checkCompletion("dnfTypes", "$param1::publicStaticYMethod()::^$publicStaticYField;");
    }

    public void testDNFTypes_ParameterType16() throws Exception {
        checkCompletion("dnfTypes", "$param2->^publicYField;");
    }

    public void testDNFTypes_ParameterType17() throws Exception {
        checkCompletion("dnfTypes", "$param2::^IMPLICIT_Y_CONSTANT;");
    }

    public void testDNFTypes_ParameterType18() throws Exception {
        checkCompletion("dnfTypes", "$param3?->^publicZField;");
    }

    public void testDNFTypes_ParameterType19() throws Exception {
        checkCompletion("dnfTypes", "$param3::^$publicStaticZField;");
    }

    public void testDNFTypes_ParameterType20() throws Exception {
        checkCompletion("dnfTypes", "$phpdoc1->^publicXMethod();");
    }

    public void testDNFTypes_ParameterType21() throws Exception {
        checkCompletion("dnfTypes", "$phpdoc1::^publicStaticZMethod();");
    }

    public void testDNFTypes_ParameterType22() throws Exception {
        checkCompletion("dnfTypes", "$phpdoc2->^publicYField;");
    }

    public void testDNFTypes_ParameterType23a() throws Exception {
        checkCompletion("dnfTypes", "$phpdoc2::^PUBLIC_Y_CONSTANT;");
    }

    public void testDNFTypes_ParameterType23b() throws Exception {
        checkCompletion("dnfTypes", "$phpdoc2::PUBLIC_Y_^CONSTANT;");
    }

    public void testDNFTypes_ParameterType24() throws Exception {
        checkCompletion("dnfTypes", "$closure1->^publicXField;");
    }

    public void testDNFTypes_ParameterType25() throws Exception {
        checkCompletion("dnfTypes", "$closure1::^PUBLIC_Y_CONSTANT;");
    }

    public void testDNFTypes_ParameterType26() throws Exception {
        checkCompletion("dnfTypes", "$closure2->publicYMethod()->^publicXField;");
    }

    public void testDNFTypes_ParameterType27() throws Exception {
        checkCompletion("dnfTypes", "$closure2::$publicStaticYField->^publicZField;");
    }

    public void testDNFTypes_ParameterType28() throws Exception {
        checkCompletion("dnfTypes", "$arrow1 = fn((ClassX&ClassY)|ClassX $test) => $test->^publicXField;");
    }

    public void testDNFTypes_ParameterType29() throws Exception {
        checkCompletion("dnfTypes", "$arrow2 = fn((ClassX&ClassY)|ClassX $test) => $test::publicStaticXMethod()->^publicZField;");
    }

    public void testDNFTypes_ParameterTypeInCCList01() throws Exception {
        checkCompletion("dnfTypes", "$this->param^(null);");
    }

    public void testDNFTypesImplementMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesImplementMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesImplementMethod02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesImplementMethod02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesImplementMethod03a() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesImplementMethod03a"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesImplementMethod03b() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesImplementMethod03b"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesImplementMethod04() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesImplementMethod04"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesOverrideMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesOverrideMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesOverrideMethod02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesOverrideMethod02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesOverrideMethod03() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesOverrideMethod03"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesOverrideMethodSpecialTypes01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesOverrideMethodSpecialTypes01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesOverrideMethodSpecialTypes02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesOverrideMethodSpecialTypes02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesOverrideMethodSpecialTypes03() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesOverrideMethodSpecialTypes03"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testDNFTypesOverrideMethodSpecialTypes04() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testDNFTypesOverrideMethodSpecialTypes04"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

}
