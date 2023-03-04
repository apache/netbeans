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


public class PHP80CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP80CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        PHPCodeCompletion.PHP_VERSION = PhpVersion.getDefault();
        super.setUp();
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php80/" + getTestDirName()))
            })
        );
    }

    private String getTestDirName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php80/%s/%s.php", getTestDirName(), fileName);
    }

    public void testNonCapturingCatches_01() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (Ex^ception) { // test1", false);
    }

    public void testNonCapturingCatches_02() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (Exception^A | ExceptionB) { // test2 test3", false);
    }

    public void testNonCapturingCatches_03() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (ExceptionA | Excep^tionB) { // test2 test3", false);
    }

    public void testNonCapturingCatches_04() throws Exception {
        checkCompletion(getTestPath("nonCapturingCatches"), "} catch (^) { // test4", false);
    }

    // Allow ::class on Objects
    public void testClassNameLiteralOnObjects_01() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($test::^class);", false);
    }

    public void testClassNameLiteralOnObjects_02() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($reference::cl^ass);", false);
    }

    public void testClassNameLiteralOnObjects_03() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump((new Test)::^class);", false);
    }

    public void testClassNameLiteralOnObjects_04() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump(test()::^class);", false);
    }

    public void testClassNameLiteralOnObjects_05() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($test->newInstance()::c^lass)", false);
    }

    public void testClassNameLiteralOnObjects_06() throws Exception {
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($this::^class);", false);
    }

    public void testClassNameLiteralOnObjects_07() throws Exception {
        // No completion items
        checkCompletion(getTestPath("classNameLiteralOnObjects"), "var_dump($test->noReturnTypes()::^class)", false);
    }

    public void testMatchExpressionSimple01() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple01"), "$result = match^", false);
    }

    public void testMatchExpressionSimple02() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple02"), "$result = match (^)", false);
    }

    public void testMatchExpressionSimple03() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple03"), "    ^", false);
    }

    public void testMatchExpressionSimple04() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple04"), "    \"match test\", ^", false);
    }

    public void testMatchExpressionSimple05_01() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple05"), "    \"match test\" => ^", false);
    }

    public void testMatchExpressionSimple05_02() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple05"), "    \"match test\" => matchT^est(),", false);
    }

    public void testMatchExpressionSimple05_03() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple05"), "    \"match test\" => matchT^", false);
    }

    public void testMatchExpressionSimple06() throws Exception {
        checkCompletion(getTestPath("matchExpressionSimple06"), "    MATCH_^", false);
    }

    public void testMatchExpressionInClass_01() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "        return match (^$state) {", false);
    }

    public void testMatchExpressionInClass_02() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            ^MatchExpression::START => self::$start,", false);
    }

    public void testMatchExpressionInClass_03() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::^START => self::$start,", false);
    }

    public void testMatchExpressionInClass_04() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::START => ^self::$start,", false);
    }

    public void testMatchExpressionInClass_05() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::START => self::^$start,", false);
    }

    public void testMatchExpressionInClass_06() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::SUSP^END => $this->suspend,", false);
    }

    public void testMatchExpressionInClass_07() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::SUSPEND => $this->^suspend,", false);
    }

    public void testMatchExpressionInClass_08() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClass"), "            MatchExpression::STOP,^ => $this->stopState(),", false);
    }

    public void testMatchExpressionInClassSimple01() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClassSimple01"), "        return match (^)", false);
    }

    public void testMatchExpressionInClassSimple02() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClassSimple02"), "            ^", false);
    }

    public void testMatchExpressionInClassSimple03() throws Exception {
        checkCompletion(getTestPath("matchExpressionInClassSimple03"), "            $this->suspend => ^", false);
    }

    public void testUnionTypesImplementMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesImplementMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesImplementMethod02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesImplementMethod02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesImplementMethod03() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesImplementMethod03"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesImplementMethod04() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesImplementMethod04"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesOverrideMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesOverrideMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesOverrideMethod02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesOverrideMethod02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesOverrideMethod03() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesOverrideMethod03"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesOverrideMethodSpecialTypes01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesOverrideMethodSpecialTypes01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesOverrideMethodSpecialTypes02() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesOverrideMethodSpecialTypes02"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesOverrideMethodSpecialTypes03() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesOverrideMethodSpecialTypes03"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesOverrideMethodSpecialTypes04() throws Exception {
        // default namespace
        checkCompletionCustomTemplateResult(getTestPath("testUnionTypesOverrideMethodSpecialTypes04"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testUnionTypesFunctionParameterType01() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType01"), "function union_types(int|^)", false);
    }

    public void testUnionTypesFunctionParameterType02() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType02"), "function union_types(int|\\Test\\U^)", false);
    }

    public void testUnionTypesFunctionParameterType03() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType03"), "function union_types(int|\\Test\\UnionTypes|^)", false);
    }

    public void testUnionTypesFunctionParameterType04() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType04"), "function union_types(int|^|\\Test\\UnionTypes)", false);
    }

    public void testUnionTypesFunctionParameterType05() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType05"), "function union_types(int|^) {", false);
    }

    public void testUnionTypesFunctionParameterType06() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType06"), "function union_types(int|Uni^) {", false);
    }

    public void testUnionTypesFunctionParameterType07() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType07"), "function union_types(int|UnionTypes $param, int|float|string|^) {", false);
    }

    public void testUnionTypesFunctionParameterType08() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType08"), "function union_types(int|UnionTypes $param, int|float|string|\\Test\\^ ) {", false);
    }

    public void testUnionTypesFunctionParameterType09() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionParameterType09"), "function union_types(int|UnionTypes $param, int|float|string|\\Test\\Uni^ ) {", false);
    }

    public void testUnionTypesFunctionReturnType01() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionReturnType01"), "function union_types(int|\\Test\\UnionTypes|null $param): null|^", false);
    }

    public void testUnionTypesFunctionReturnType02() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionReturnType02"), "function union_types(int|\\Test\\UnionTypes|null $param): null|\\Test\\^", false);
    }

    public void testUnionTypesFunctionReturnType03() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionReturnType03"), "function union_types(int|\\Test\\UnionTypes|null $param): null|\\Test\\UnionTyes1|U^", false);
    }

    public void testUnionTypesFunctionReturnType04() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctionReturnType04"), "function union_types(int|\\Test\\UnionTypes|null $param): null|^|\\Test\\UnionTyes1", false);
    }

    public void testUnionTypesFunctions_01() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "function union_types(n^ull|int|float $param1, string|object $param): UnionTypes1| UnionTypes2 {", false);
    }

    public void testUnionTypesFunctions_02() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "function union_types(null|int|float $param1, string|^object $param): UnionTypes1| UnionTypes2 {", false);
    }

    public void testUnionTypesFunctions_03() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "function union_types(null|int|float $param1, string|obj^ect $param): UnionTypes1| UnionTypes2 {", false);
    }

    public void testUnionTypesFunctions_04() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "function union_types(null|int|float $param1, string|object $param): ^UnionTypes1| UnionTypes2 {", false);
    }

    public void testUnionTypesFunctions_05() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "function union_types(null|int|float $param1, string|object $param): UnionTypes1| ^UnionTypes2 {", false);
    }

    public void testUnionTypesFunctions_06() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "function union_types(null|int|float $param1, string|object $param): UnionTypes1| Uni^onTypes2 {", false);
    }

    public void testUnionTypesFunctions_07() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$closure = function(null|int|float $param1, st^ring| object $param): iterable|false {", false);
    }

    public void testUnionTypesFunctions_08() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$closure = function(null|int|float $param1, string| ^object $param): iterable|false {", false);
    }

    public void testUnionTypesFunctions_09() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$closure = function(null|int|float $param1, string| object $param): ^iterable|false {", false);
    }

    public void testUnionTypesFunctions_10() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$closure = function(null|int|float $param1, string| object $param): iterable|^false {", false);
    }

    public void testUnionTypesFunctions_11() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$closure = function(null|int|float $param1, string| object $param): iterable|f^alse {", false);
    }

    public void testUnionTypesFunctions_12() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|^float $param1, string|object $param): \\Test\\UnionTypes1|\\Test\\UnionTypes2|null {", false);
    }

    public void testUnionTypesFunctions_13() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|^object $param): \\Test\\UnionTypes1|\\Test\\UnionTypes2|null {", false);
    }

    public void testUnionTypesFunctions_14() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|object^ $param): \\Test\\UnionTypes1|\\Test\\UnionTypes2|null {", false);
    }

    public void testUnionTypesFunctions_15() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|object $param): ^\\Test\\UnionTypes1|\\Test\\UnionTypes2|null {", false);
    }

    public void testUnionTypesFunctions_16() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|object $param): \\Test\\Un^ionTypes1|\\Test\\UnionTypes2|null {", false);
    }

    public void testUnionTypesFunctions_17() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|object $param): \\Test\\UnionTypes1|^\\Test\\UnionTypes2|null {", false);
    }

    public void testUnionTypesFunctions_18() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|object $param): \\Test\\UnionTypes1|\\Test\\^UnionTypes2|null {", false);
    }

    public void testUnionTypesFunctions_19() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|object $param): \\Test\\UnionTypes1|\\Test\\UnionTypes2|^null {", false);
    }

    public void testUnionTypesFunctions_20() throws Exception {
        checkCompletion(getTestPath("unionTypesFunctions"), "$arrow = fn(null|int|float $param1, string|object $param): \\Test\\UnionTypes1|\\Test\\UnionTypes2|n^ull {", false);
    }

    public void testUnionTypesFields01() throws Exception {
        checkCompletion(getTestPath("unionTypesFields01"), "    private int|^", false);
    }

    public void testUnionTypesFields02() throws Exception {
        checkCompletion(getTestPath("unionTypesFields02"), "    private int|\\Test1\\^", false);
    }

    public void testUnionTypesFields03() throws Exception {
        checkCompletion(getTestPath("unionTypesFields03"), "    private int|\\Test1\\UnionTypes2|^", false);
    }

    public void testUnionTypesFields04() throws Exception {
        checkCompletion(getTestPath("unionTypesFields04"), "    private int|\\Test1\\UnionTypes2|nu^ll $test;", false);
    }

    public void testUnionTypesFields05() throws Exception {
        checkCompletion(getTestPath("unionTypesFields05"), "    private int|\\Test1\\UnionTypes2|^|null $test;", false);
    }

    public void testUnionTypesFields06() throws Exception {
        checkCompletion(getTestPath("unionTypesFields06"), "    private static int|\\Test1\\UnionTypes2|^null $test;", false);
    }

    public void testUnionTypesFieldsStaticKeyword() throws Exception {
        checkCompletion(getTestPath("unionTypesFieldsStaticKeyword"), "    private stat^", false);
    }

    public void testUnionTypesMethods_01() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            ^\\Test1\\UnionTypes1|TestClass $object,", false);
    }

    public void testUnionTypesMethods_02() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            \\Test1\\^UnionTypes1|TestClass $object,", false);
    }

    public void testUnionTypesMethods_03() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            \\Test1\\UnionTypes1|^TestClass $object,", false);
    }

    public void testUnionTypesMethods_04() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            \\Test1\\UnionTypes1|Test^Class $object,", false);
    }

    public void testUnionTypesMethods_05() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            ^string|int|float|null $param,", false);
    }

    public void testUnionTypesMethods_06() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            string|int|float|n^ull $param,", false);
    }

    public void testUnionTypesMethods_07() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    ): ^\\Test1\\UnionTypes2|\\Test1\\UnionTypes1|null {", false);
    }

    public void testUnionTypesMethods_08() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    ): \\Test1\\UnionTypes2|^\\Test1\\UnionTypes1|null {", false);
    }

    public void testUnionTypesMethods_09() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    ): \\Test1\\UnionTypes2|\\Test1\\Un^ionTypes1|null {", false);
    }

    public void testUnionTypesMethods_10() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            \\Test1\\UnionTypes1|Test^Class $object, // static", false);
    }

    public void testUnionTypesMethods_11() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "            string|int|float|^null $param, // static", false);
    }

    public void testUnionTypesMethods_12() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    ): \\Test1\\UnionTypes2|^self|null {", false);
    }

    public void testUnionTypesMethods_13() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    ): \\Test1\\UnionTypes2|se^lf|null {", false);
    }

    public void testUnionTypesMethods_14() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    public function childClassMethod(parent|self|null $object, self|^parent|null $object2): self|parent|null {", false);
    }

    public void testUnionTypesMethods_15() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    public function childClassMethod(parent|self|null $object, self|par^ent|null $object2): self|parent|null {", false);
    }

    public void testUnionTypesMethods_16() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    public function childClassMethod(parent|self|null $object, self|parent|null $object2): self|p^arent|null {", false);
    }

    public void testUnionTypesMethods_17() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    public function interfaceMethod(\\Test1\\UnionTypes1|TestClass $object, string|int|float|^null $param, ): int|float|null;", false);
    }

    public void testUnionTypesMethods_18() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    public function interfaceMethod(\\Test1\\UnionTypes1|TestClass $object, string|int|float|null $param, ): int|^float|null;", false);
    }

    public void testUnionTypesMethods_19() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    abstract protected function abstractClassMethod(array|object $param, TestInerface|Test^Class|null $object,): iterable|false;", false);
    }

    public void testUnionTypesMethods_20() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    abstract protected function abstractClassMethod(array|object $param, TestInerface|TestClass|null $object,): iterable|^false;", false);
    }

    public void testUnionTypesMethods_21() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    private function traitMethod(TestInterface|\\Test1\\TestUnionTypes2 $object, bool|ca^llable $param): TestInterface|false|null {", false);
    }

    public void testUnionTypesMethods_22() throws Exception {
        checkCompletion(getTestPath("unionTypesMethods"), "    private function traitMethod(TestInterface|\\Test1\\TestUnionTypes2 $object, bool|callable $param): TestInterface|^false|null {", false);
    }

    public void testUnionTypes_01() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $this->^publicFieldInterfaceImpl->publicMethodClass1();", false);
    }

    public void testUnionTypes_02() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $this->publicFieldInterfaceImpl->^publicMethodClass1();", false);
    }

    public void testUnionTypes_03() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $this->publicFieldTrait1->^publicMethodClass1();", false);
    }

    public void testUnionTypes_04() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        self::^$publicStaticFieldInterfaceImpl->publicMethodClass1();", false);
    }

    public void testUnionTypes_05() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        self::$publicStaticFieldInterfaceImpl->^publicMethodClass1();", false);
    }

    public void testUnionTypes_06() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        self::$publicStaticFieldInterfaceImpl::^CONST_CLASS1;", false);
    }

    public void testUnionTypes_07() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object->^publicMethodClass1()->publicMethodClass2();", false);
    }

    public void testUnionTypes_08() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object->publicMethodClass1()->^publicMethodClass2();", false);
    }

    public void testUnionTypes_09() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object->publicMethodClass1()::^CONST_CLASS2;", false);
    }

    public void testUnionTypes_10() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object::^$publicFieldClass1->publicMethodClass1();", false);
    }

    public void testUnionTypes_11() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object::$publicFieldClass1->^publicMethodClass1();", false);
    }

    public void testUnionTypes_12() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object::$publicFieldClass1::^publicStaticMethodClass1();", false);
    }

    public void testUnionTypes_13() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object->^publicMethodClass2(); // phpdoc", false);
    }

    public void testUnionTypes_14() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object::^publicStaticMethodClass2(); // phpdoc", false);
    }

    public void testUnionTypes_15() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $this::^$publicStaticFieldTrait1->publicMethodClass1();", false);
    }

    public void testUnionTypes_16() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $this::$publicStaticFieldTrait1->^publicMethodClass1();", false);
    }

    public void testUnionTypes_17() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $this::publicStaticMethodTrait1()::^publicStaticMethodClass1();", false);
    }

    public void testUnionTypes_18() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object->^publicMethodClass1(); // with whitespaces", false);
    }

    public void testUnionTypes_19() throws Exception {
        checkCompletion(getTestPath("unionTypes"), "        $object::^publicStaticMethodClass1(); // with whitespaces", false);
    }

    // self & parent types
    public void testUnionTypesWithSpecialTypes_01() throws Exception {
        checkCompletion(getTestPath("unionTypesWithSpecialTypes"), "$param->^testMethod();", false);
    }

    public void testUnionTypesWithSpecialTypes_02() throws Exception {
        checkCompletion(getTestPath("unionTypesWithSpecialTypes"), "$param->^parentMethod(); // parent", false);
    }

    public void testUnionTypesWithSpecialTypes_03() throws Exception {
        checkCompletion(getTestPath("unionTypesWithSpecialTypes"), "$this->union->^parentMethod();", false);
    }

    public void testUnionTypesWithSpecialTypes_04() throws Exception {
        checkCompletion(getTestPath("unionTypesWithSpecialTypes"), "$this->childMethod($this)->^parentMethod();", false);
    }

    public void testUnionTypesWithSpecialTypes_05() throws Exception {
        checkCompletion(getTestPath("unionTypesWithSpecialTypes"), "$this->childMethodParent(null)->^parentMethod();", false);
    }

    public void testUnionTypesWithSpecialTypes_06() throws Exception {
        checkCompletion(getTestPath("unionTypesWithSpecialTypes"), "$this->traitMethod($this)->^parentMethod();", false);
    }

    public void testStaticReturnTypeInReturnType_01() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeInReturnType"), "    public function testReturnType(): sta^tic {", false);
    }

    public void testStaticReturnTypeInReturnType_02() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeInReturnType"), "    public function testReturnNullableType(): ?stat^ic {", false);
    }

    public void testStaticReturnTypeInReturnType_03() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeInReturnType"), "    public static function testReturnUnionType(): self|\\Foo\\Bar|stat^ic|null {", false);
    }

    public void testStaticReturnType_01() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnTypeParent()->^testStaticReturnTypeChild(); // all", false);
    }

    public void testStaticReturnType_02() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testSelfReturnTypeParent()->^testStaticReturnTypeParent(); // parent items", false);
    }

    public void testStaticReturnType_03() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnNullableTypeParent()->^testStaticReturnTypeChild(); // all", false);
    }

    public void testStaticReturnType_04() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testSelfReturnNullableTypeParent()->^testStaticReturnTypeParent(); // parent items", false);
    }

    public void testStaticReturnType_05() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnUnionTypeParent()->^testStaticReturnTypeChild(); // all", false);
    }

    public void testStaticReturnType_06() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testSelfReturnUnionTypeParent()->^testStaticReturnTypeParent(); // parent items", false);
    }

    public void testStaticReturnType_07() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticSelfReturnUnionTypeParent()->^testStaticReturnTypeChild(); // all", false);
    }

    public void testStaticReturnType_08() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnTypeTrait()->^testStaticReturnTypeChild(); // all", false);
    }

    public void testStaticReturnType_09() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnNullableTypeTrait()->^testStaticReturnTypeChild(); // all", false);
    }

    public void testStaticReturnType_10() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnUnionTypeTrait()->^testStaticReturnTypeChild(); // all", false);
    }

    public void testStaticReturnType_11() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnTypeChild()->^testSelfReturnNullableTypeParent(); // all", false);
    }

    public void testStaticReturnType_12() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnNullableTypeChild()->^testSelfReturnNullableTypeParent(); // all", false);
    }

    public void testStaticReturnType_13() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "$child->testStaticReturnUnionTypeChild()->^testStaticReturnTypeTrait(); // all", false);
    }

    public void testStaticReturnType_14() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "                ->^testSelfReturnNullableTypeParent() // parent", false);
    }

    public void testStaticReturnType_15() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "                ->^testSelfReturnUnionTypeParent(); // parent", false);
    }

    public void testStaticReturnType_16() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "                ->^testSelfReturnTypeParent() // all test16", false);
    }

    public void testStaticReturnType_17() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "                ->^testStaticReturnNullableTypeParent() // parent test17", false);
    }

    public void testStaticReturnType_18() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "                ->^testStaticReturnTypeParent(); // parent test 18", false);
    }

    public void testStaticReturnType_19() throws Exception {
        checkCompletion(getTestPath("staticReturnType"), "                ->^testStaticReturnUnionTypeTrait(); // trait test19", false);
    }

    public void testStaticReturnTypeOfStaticMethod_01() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "ChildClass::testStaticReturnTypeParentStatic()::^testStaticReturnTypeChildStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_02() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "ChildClass::testSelfReturnTypeParentStatic()::^testStaticReturnTypeParentStatic(); // parent items", false);
    }

    public void testStaticReturnTypeOfStaticMethod_03() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "ChildClass::testStaticReturnNullableTypeParentStatic()::^testStaticReturnTypeChildStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_04() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "ChildClass::testSelfReturnNullableTypeParentStatic()::^testStaticReturnTypeParentStatic(); // parent items", false);
    }

    public void testStaticReturnTypeOfStaticMethod_05() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "ChildClass::testStaticReturnUnionTypeParentStatic()::^testStaticReturnTypeChildStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_06() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "ChildClass::testSelfReturnUnionTypeParentStatic()::^testStaticReturnTypeParentStatic(); // parent items", false);
    }

    public void testStaticReturnTypeOfStaticMethod_07() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "$child::testStaticSelfReturnUnionTypeParentStatic()::^testStaticReturnTypeChildStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_08() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "$child::testStaticReturnTypeTraitStatic()::^testStaticReturnTypeChildStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_09() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "$child::testStaticReturnNullableTypeTraitStatic()::^testStaticReturnTypeChildStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_10() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "$child::testStaticReturnUnionTypeTraitStatic()::^testStaticReturnTypeChildStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_11() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "$child::testStaticReturnTypeChildStatic()::^testSelfReturnNullableTypeParentStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_12() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "$child::testStaticReturnNullableTypeChildStatic()::^testSelfReturnNullableTypeParentStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_13() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "$child::testStaticReturnUnionTypeChildStatic()::^testStaticReturnTypeTraitStatic(); // all", false);
    }

    public void testStaticReturnTypeOfStaticMethod_14() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "                ::^testSelfReturnNullableTypeParentStatic() // parent", false);
    }

    public void testStaticReturnTypeOfStaticMethod_15() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "                ::^testSelfReturnUnionTypeParentStatic(); // parent", false);
    }

    public void testStaticReturnTypeOfStaticMethod_16() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "            ::^testSelfReturnTypeParentStatic() // all test16", false);
    }

    public void testStaticReturnTypeOfStaticMethod_17() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "            ::^testStaticReturnNullableTypeParentStatic() // parent test17", false);
    }

    public void testStaticReturnTypeOfStaticMethod_18() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "            ::^testStaticReturnTypeParentStatic(); // parent test 18", false);
    }

    public void testStaticReturnTypeOfStaticMethod_19() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeOfStaticMethod"), "                ::^testStaticReturnUnionTypeTraitStatic(); // trait test19", false);
    }

    public void testStaticReturnTypeMixed_01() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeMixed"), "                ->^testSelfReturnNullableTypeParent() // parent test1", false);
    }

    public void testStaticReturnTypeMixed_02() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeMixed"), "                ::^testSelfReturnUnionTypeParentStatic(); // parent test2", false);
    }

    public void testStaticReturnTypeMixed_03() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeMixed"), "                ->^testSelfReturnTypeParent() // all test3", false);
    }

    public void testStaticReturnTypeMixed_04() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeMixed"), "                ::^testStaticReturnNullableTypeParentStatic() // parent test4", false);
    }

    public void testStaticReturnTypeMixed_05() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeMixed"), "                ->^testStaticReturnTypeParent(); // parent test5", false);
    }

    public void testStaticReturnTypeMixed_06() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeMixed"), "                ->^testStaticReturnUnionTypeTrait() // trait test6", false);
    }

    public void testStaticReturnTypeMixed_07() throws Exception {
        checkCompletion(getTestPath("staticReturnTypeMixed"), "                ::^testStaticReturnTypeTraitStatic(); // trait test7", false);
    }

    public void testMixedType_01() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    private ^mixed $mixed;", false);
    }

    public void testMixedType_02() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    private mixe^d $mixed;", false);
    }

    public void testMixedType_03() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    public function mixed(^mixed $mixed): mixed {", false);
    }

    public void testMixedType_04() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    public function mixed(mi^xed $mixed): mixed {", false);
    }

    public void testMixedType_05() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    public function mixed(mixed $mixed): ^mixed {", false);
    }

    public void testMixedType_06() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    public function mixed(mixed $mixed): mix^ed {", false);
    }

    public void testMixedType_07() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    private int|^null $union;", false);
    }

    public void testMixedType_08() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    public function union(int|^string $mixed): object|array {", false);
    }

    public void testMixedType_09() throws Exception {
        checkCompletion(getTestPath("mixedType"), "    public function union(int|string $mixed): object|^array {", false);
    }

    public void testNullsafeOperator_01() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "        return $this?->^address;", false);
    }

    public void testNullsafeOperator_02() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session?->^user?->getAddress()?->country;", false);
    }

    public void testNullsafeOperator_03() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session?->user?->^getAddress()?->country;", false);
    }

    public void testNullsafeOperator_04() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session?->user?->getAddress()?->^country;", false);
    }

    public void testNullsafeOperator_05() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session?->user::^$test;", false);
    }

    public void testNullsafeOperator_06() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session?->user->^id;", false);
    }

    public void testNullsafeOperator_07() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session?->user?->getAddress()::^ID;", false);
    }

    public void testNullsafeOperator_08() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = User::create(\"test\")?->^getAddress()?->country;", false);
    }

    public void testNullsafeOperator_09() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = User::create(\"test\")?->getAddress()?->^country;", false);
    }

    public void testNullsafeOperator_10() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session->getUser()::create(\"test\")?->^getAddress()->country;", false);
    }

    public void testNullsafeOperator_11() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = $session->getUser()::create(\"test\")?->getAddress()->^country;", false);
    }

    public void testNullsafeOperator_12() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = (new User(\"test\"))?->^getAddress()->country;", false);
    }

    public void testNullsafeOperator_13() throws Exception {
        checkCompletion(getTestPath("nullsafeOperator"), "$country = (new User(\"test\"))?->getAddress()->^country;", false);
    }

    public void testNullsafeOperatorWithComments_01() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->^foo();", false);
    }

    public void testNullsafeOperatorWithComments_02() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj /**/?->^foo();", false);
    }

    public void testNullsafeOperatorWithComments_03() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/ ?->^foo();", false);
    }

    public void testNullsafeOperatorWithComments_04() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj /* aa */ ?->^foo();", false);
    }

    public void testNullsafeOperatorWithComments_05() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?-> /**/^foo();", false);
    }

    public void testNullsafeOperatorWithComments_06() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?-> /**/ ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_07() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->/**/ ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_08() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->/**/^foo();", false);
    }

    public void testNullsafeOperatorWithComments_09() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?-> /* aa */^foo();", false);
    }

    public void testNullsafeOperatorWithComments_10() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?-> /* aa */ ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_11() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->/* aa */^foo();", false);
    }

    public void testNullsafeOperatorWithComments_12() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->/* aa */ ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_13() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->\n /* aa */\n ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_14() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->\n /** aa */\n ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_15() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->\n /**/\n ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_16() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->\n // aa\n ^foo();", false);
    }

    public void testNullsafeOperatorWithComments_17() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->\n // aa\n^foo();", false);
    }

    public void testNullsafeOperatorWithComments_18() throws Exception {
        checkCompletion(getTestPath("nullsafeOperatorWithComments"), "$obj/**/?->\n// aa\n^foo();", false);
    }

    public void testConstructorPropertyPromotion01a() throws Exception {
        // one line
        checkCompletion(getTestPath("constructorPropertyPromotion01a"), "    public function __construct(^", false);
    }

    public void testConstructorPropertyPromotion01b() throws Exception {
        // multiple lines
        checkCompletion(getTestPath("constructorPropertyPromotion01b"), "            ^", false);
    }

    public void testConstructorPropertyPromotion02a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion02a"), "    public function __construct(pr^", false);
    }

    public void testConstructorPropertyPromotion02b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion02b"), "            pr^", false);
    }

    public void testConstructorPropertyPromotion03a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion03a"), "    public function __construct(private ^) {", false);
    }

    public void testConstructorPropertyPromotion03b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion03b"), "            private ^", false);
    }

    public void testConstructorPropertyPromotion04a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion04a"), "    public function __construct(private ?^) {", false);
    }

    public void testConstructorPropertyPromotion04b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion04b"), "            private ?^", false);
    }

    public void testConstructorPropertyPromotion05a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion05a"), "    public function __construct(private string|^) {", false);
    }

    public void testConstructorPropertyPromotion05b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion05b"), "            private string|^", false);
    }

    public void testConstructorPropertyPromotion06a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion06a"), "    public function __construct(private string|int $param1, ^) {", false);
    }

    public void testConstructorPropertyPromotion06b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion06b"), "            ^// test", false);
    }

    public void testConstructorPropertyPromotion07a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion07a"), "    public function __construct(private string|int $param1, protected ^) {", false);
    }

    public void testConstructorPropertyPromotion07b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion07b"), "            protected ^", false);
    }

    public void testConstructorPropertyPromotion08a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion08a"), "    public function __construct(private string|int $param1, protected ?string $param2 = ^) {", false);
    }

    public void testConstructorPropertyPromotion08b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion08b"), "            protected ?string $param2 = ^", false);
    }

    public void testConstructorPropertyPromotion09a() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion09a"), "    public function __construct(private string|int $param1, protected ?string $param2 = ^,) {", false);
    }

    public void testConstructorPropertyPromotion09b() throws Exception {
        checkCompletion(getTestPath("constructorPropertyPromotion09b"), "            protected ?string $param2 = ^,", false);
    }

    public void testNamedArgumentsFunction01() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction01"), "test(^)", false);
    }

    public void testNamedArgumentsFunction02() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction02"), "test(ar^);", false);
    }

    public void testNamedArgumentsFunction03() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction03"), "test(1, ^);", false);
    }

    public void testNamedArgumentsFunction04() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction04"), "test(1, defa^);", false);
    }

    public void testNamedArgumentsFunction05_a() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction05"), "test(param1: 1,^ default: \"test\");", false);
    }

    public void testNamedArgumentsFunction05_b() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction05"), "test(param1: 1, ^default: \"test\");", false);
    }

    public void testNamedArgumentsFunction05_c() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction05"), "test(param1: 1, arr^default: \"test\");", false);
    }

    public void testNamedArgumentsFunction06() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunction06"), "test(param1: ^);", false);
    }

    public void testNamedArgumentsFunctionNested01() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunctionNested01"), "test(1, default: test(^));", false);
    }

    public void testNamedArgumentsFunctionNested02() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunctionNested02"), "test(1, default: test(param^));", false);
    }

    public void testNamedArgumentsFunctionNested03() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunctionNested03"), "test(1, default: test(param1: 1, ^));", false);
    }

    public void testNamedArgumentsFunctionNested04() throws Exception {
        checkCompletion(getTestPath("namedArgumentsFunctionNested04"), "test(1, default: test(param1: 1, arr^));", false);
    }

    public void testNamedArgumentsMethod01() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod01"), "$this->test(^)", false);
    }

    public void testNamedArgumentsMethod02_a() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod02"), "$this->test(arr^);", false);
    }

    public void testNamedArgumentsMethod02_b() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod02"), "$instance->test(^)", false);
    }

    public void testNamedArgumentsMethod03() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod03"), "$this->test(1, ^)", false);
    }

    public void testNamedArgumentsMethod04() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod04"), "$this->test(1, defa^);", false);
    }

    public void testNamedArgumentsMethod05_a() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod05"), "$this->test(param1: 1,^ default: \"test\");", false);
    }

    public void testNamedArgumentsMethod05_b() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod05"), "$this->test(param1: 1, ^default: \"test\");", false);
    }

    public void testNamedArgumentsMethod05_c() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod05"), "$this->test(param1: 1, arr^default: \"test\");", false);
    }

    public void testNamedArgumentsMethod06() throws Exception {
        checkCompletion(getTestPath("namedArgumentsMethod06"), "$this->test(^)", false);
    }

    public void testNamedArgumentsStaticMethod01() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod01"), "self::test(^)", false);
    }

    public void testNamedArgumentsStaticMethod02_a() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod02"), "static::test(arr^);", false);
    }

    public void testNamedArgumentsStaticMethod02_b() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod02"), "NamedArguments::test(^)", false);
    }

    public void testNamedArgumentsStaticMethod03() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod03"), "self::test(1, ^);", false);
    }

    public void testNamedArgumentsStaticMethod04() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod04"), "self::test(1, defa^);", false);
    }

    public void testNamedArgumentsStaticMethod05_a() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod05"), "self::test(param1: 1,^ default: \"test\");", false);
    }

    public void testNamedArgumentsStaticMethod05_b() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod05"), "self::test(param1: 1, ^default: \"test\");", false);
    }

    public void testNamedArgumentsStaticMethod05_c() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod05"), "self::test(param1: 1, arr^default: \"test\");", false);
    }

    public void testNamedArgumentsStaticMethod06() throws Exception {
        checkCompletion(getTestPath("namedArgumentsStaticMethod06"), "self::test(^)", false);
    }

    public void testStaticReturnTypeOverrideMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testStaticReturnTypeOverrideMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testStaticReturnTypeImplementMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testStaticReturnTypeImplementMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testMixedTypeOverrideMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testMixedTypeOverrideMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }

    public void testMixedTypeImplementMethod01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testMixedTypeImplementMethod01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_80, "test"), true);
    }
}
