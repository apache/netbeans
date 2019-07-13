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

import org.netbeans.modules.php.api.PhpVersion;
import org.openide.filesystems.FileObject;

public class ArrowFunctionSuggestionTest extends PHPHintsTestBase {

    public ArrowFunctionSuggestionTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "ArrowFunctionSuggestion/";
    }

    public void testArrowFunctionSuggestionSimple_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsSimple.php", "$fn = function (int $x)^ use ($y) {");
    }

    public void testArrowFunctionSuggestionSimple_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_73), "testArrowFunctionsSimple.php", "$fn = function (int $x)^ use ($y) {");
    }

    public void testArrowFunctionSuggestionSimple_02a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsSimple.php", "$fn = static function ($x) use ($y):^ int {");
    }

    public void testArrowFunctionSuggestionSimple_02b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsSimple.php", "    return $x +^ $y; // 02");
    }

    public void testArrowFunctionSuggestionSimple_02c() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_72), "testArrowFunctionsSimple.php", "    return $x +^ $y; // 02");
    }

    public void testArrowFunctionSuggestionParameterType_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsParameterType.php", "function(array ^$x) use ($y) {");
    }

    public void testArrowFunctionSuggestionParameterType_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsParameterType.php", "^};");
    }

    public void testArrowFunctionSuggestionReturnType_01() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReturnType.php", "    return $x^ + $y;");
    }

    public void testArrowFunctionSuggestionParameterReturnTypes_01() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsParameterReturnTypes.php", "funct^ion(?array $z, int $x) use ($y): ?int {");
    }

    public void testArrowFunctionSuggestionDefaultValue_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsDefaultValue.php", "    return $x + ^$y;");
    }

    public void testArrowFunctionSuggestionDefaultValue_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_73), "testArrowFunctionsDefaultValue.php", "    return $x + ^$y;");
    }

    public void testArrowFunctionSuggestionReference_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReference.php", "function(&$x) use ($y) {return $^x + $y;};");
    }

    public void testArrowFunctionSuggestionReference_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_73), "testArrowFunctionsReference.php", "function(&$x) use ($y) {return $^x + $y;};");
    }

    public void testArrowFunctionSuggestionReference_02a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReference.php", "    return $x * $y * ^CONSTANT_INT;");
    }

    public void testArrowFunctionSuggestionReference_03a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReference.php", "    return $x +  ^  $y;");
    }

    public void testArrowFunctionSuggestionVariadic_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsVariadic.php", "function($x, ...$reset) use ^($y) {");
    }

    public void testArrowFunctionSuggestionVariadic_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_73), "testArrowFunctionsVariadic.php", "function($x, ...$reset) use ^($y) {");
    }

    public void testArrowFunctionSuggestionVariadic_02a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsVariadic.php", "    return $x + count($reset) ^+ $y; // 02");
    }

    public void testArrowFunctionSuggestionExamples_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "    return array_map(function ($x) use ($array^) { return $array[$x]; }, $keys);");
    }

    public void testArrowFunctionSuggestionExamples_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_73), "testArrowFunctionsExamples.php", "    return array_map(function ($x) use ($array^) { return $array[$x]; }, $keys);");
    }

    public void testArrowFunctionSuggestionExamples_02a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "$test = function ($param) use (^$callable1, $callable2) {");
    }

    public void testArrowFunctionSuggestionExamples_03a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "    return in_array($ne^edle, $array);");
    }

    public void testArrowFunctionSuggestionExamples_04a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "        return !$callable(...$ar^gs);");
    }

    public void testArrowFunctionSuggestionExamples_04b() throws Exception {
        // not lambda function
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "function test(callable $call^able) {");
    }

    public void testArrowFunctionSuggestionExamples_05a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "    ->test1(function ^($param) {");
    }

    public void testArrowFunctionSuggestionExamples_05b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "        return $param1 + ^$param2;");
    }

    public void testArrowFunctionSuggestionInMethod_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "        $test = function($test) ^use ($param) {");
    }

    public void testArrowFunctionSuggestionInMethod_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "            return $test^ + $param;");
    }

    public void testArrowFunctionSuggestionInMethod_01c() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "        ^}; // 01");
    }

    public void testArrowFunctionSuggestionInMethod_01d() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_73), "testArrowFunctionsInMethod.php", "        ^}; // 01");
    }

    public void testArrowFunctionSuggestionInMethod_02a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "        $test = function&(array $test1, &...$test2, ...$test3, &$test4)^ use ($param): ?int {");
    }

    public void testArrowFunctionSuggestionInMethod_02b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "            return count($test1)+ count($test2) + count($test3) + count($test4) + $param;^");
    }

    public void testArrowFunctionSuggestionInMethod_03() throws Exception {
        // normal method declaration
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "    public function test($param)^ {");
    }

    public void testArrowFunctionSuggestionNested_01a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "    return fn($param2) => $param1 + $param2 + ^$variable1;");
    }

    public void testArrowFunctionSuggestionNested_01b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(PhpVersion.PHP_73), "testArrowFunctionsNested.php", "    return fn($param2) => $param1 + $param2 + ^$variable1;");
    }

    public void testArrowFunctionSuggestionNested_02a() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "function($param1) use ($variable1): ?^int { // 02");
    }

    public void testArrowFunctionSuggestionNested_02b() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "    return function ($param2) use ($param1, $variable1)^ {");
    }

    public void testArrowFunctionSuggestionNested_02c() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "        return $param1 + $param2 + $variab^le1;");
    }

    public void testArrowFunctionSuggestionNested_02d() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "    ^};");
    }

    public void testArrowFunctionSuggestionNested_02e() throws Exception {
        checkHints(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "}^; // 02");
    }

    // Fix
    public void testArrowFunctionSuggestionSimpleFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsSimple.php", "$fn = function (int $x)^ use ($y) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionSimpleFix_02() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsSimple.php", "    return $x + $y^; // 02", "Arrow Function");
    }

    public void testArrowFunctionSuggestionParameterTypeFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsParameterType.php", "    return $x +^ $y;", "Arrow Function");
    }

    public void testArrowFunctionSuggestionReturnTypeFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReturnType.php", "    return $x +^ $y;", "Arrow Function");
    }

    public void testArrowFunctionSuggestionParameterReturnTypesFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsParameterReturnTypes.php", "function(?array $z, int $x) use^ ($y): ?int {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionDefaultValueFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsDefaultValue.php", "    return $x^ + $y;", "Arrow Function");
    }

    public void testArrowFunctionSuggestionReferenceFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReference.php", "function(&$x) use ($y) {return ^$x + $y;};", "Arrow Function");
    }

    public void testArrowFunctionSuggestionReferenceFix_02() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReference.php", "function&($x) use ^($y) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionReferenceFix_03() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsReference.php", "function&(&$x) use^ ($y) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionVariadicFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsVariadic.php", "    return $x + count($reset) +^ $y;", "Arrow Function");
    }

    public void testArrowFunctionSuggestionVariadicFix_02() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsVariadic.php", "function($x, &...$reset^) use ($y) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionExamplesFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "    return array_map(function ($x) use ($ar^ray) { return $array[$x]; }, $keys);", "Arrow Function");
    }

    public void testArrowFunctionSuggestionExamplesFix_02() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "    return $callable1($callab^le2($param), $param);", "Arrow Function");
    }

    public void testArrowFunctionSuggestionExamplesFix_03() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "$instance->filed = array_filter($tests, function ($need^le) use ($array) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionExamplesFix_04() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "    return function (...$args) use ^($callable) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionExamplesFix_05a() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "        ^return $param * 100;", "Arrow Function");
    }

    public void testArrowFunctionSuggestionExamplesFix_05b() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsExamples.php", "    ->test2(function ($param1, ^$param2) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionInMethodFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "        $test = function($test) use ($param)^ {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionInMethodFix_02() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsInMethod.php", "            return ^count($test1)+ count($test2) + count($test3) + count($test4) + $param;", "Arrow Function");
    }

    public void testArrowFunctionSuggestionNestedFix_01() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "function($param1) use ($variable1): ^?int {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionNestedFix_02a() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "function($param1) use ($variable1): ?int ^{ // 02", "Arrow Function");
    }

    public void testArrowFunctionSuggestionNestedFix_02b() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "    return function ($param2) use ($param1, $variable1^) {", "Arrow Function");
    }

    public void testArrowFunctionSuggestionNestedFix_02c() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "        return $param1 + $param2 + $varia^ble1;", "Arrow Function");
    }

    public void testArrowFunctionSuggestionNestedFix_02d() throws Exception {
        applyHint(new ArrowFunctionSuggestionStub(), "testArrowFunctionsNested.php", "^}; // 02", "Arrow Function");
    }

    private static class ArrowFunctionSuggestionStub extends ArrowFunctionSuggestion {

        private final PhpVersion phpVersion;

        public ArrowFunctionSuggestionStub() {
            this.phpVersion = PhpVersion.PHP_74;
        }

        public ArrowFunctionSuggestionStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected boolean isAtLeastPhp74(FileObject fileObject) {
            return phpVersion.compareTo(PhpVersion.PHP_74) >= 0;
        }
    }
}
