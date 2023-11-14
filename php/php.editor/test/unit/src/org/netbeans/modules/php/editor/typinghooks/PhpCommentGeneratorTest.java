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

package org.netbeans.modules.php.editor.typinghooks;

import java.util.concurrent.Future;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.php.editor.csl.PHPNavTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpCommentGeneratorTest extends PHPNavTestBase {

    public PhpCommentGeneratorTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testFunctionDocumentationParam() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo($i) {\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * \n" +
                            " * @param " + PhpCommentGenerator.TYPE_PLACEHOLDER + " $i^\n" +
                            " */\n" +
                            "function foo($i) {\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionDocumentationGlobalVar() throws Exception {
        insertBreak( "<?php\n" +
                            "$r = 1;\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    global $r;\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "$r = 1;\n" +
                            "/**\n" +
                            " * \n" +
                            " * @global int $r^\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    global $r;\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionDocumentationStaticVar() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    static $r;\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * \n" +
                            " * @staticvar " + PhpCommentGenerator.TYPE_PLACEHOLDER + " $r^\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    static $r;\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionDocumentationReturn() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    return \"\";\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * \n" +
                            " * @return string^\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    return \"\";\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testGlobalVariableDocumentation() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "$GLOBALS['test'] = \"\";\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " *\n" +
                            " * @global string $GLOBALS['test']\n" +
                            " * @name $test ^\n" +
                            " */\n" +
                            "$GLOBALS['test'] = \"\";\n" +
                            "?>\n");
    }

    public void testFieldDocumentation() throws Exception {
        insertBreak( "<?php\n" +
                            "class foo {\n" +
                            "    /**^\n" +
                            "    var $bar = \"\";\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "class foo {\n" +
                            "    /**\n" +
                            "     * \n" +
                            "     * @var " + PhpCommentGenerator.TYPE_PLACEHOLDER + "^\n" +
                            "     */\n" +
                            "    var $bar = \"\";\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testMethodDocumentation() throws Exception {
        insertBreak( "<?php\n" +
                            "class foo {\n" +
                            "    /**^\n" +
                            "    function bar($par) {\n" +
                            "    }\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "class foo {\n" +
                            "    /**\n" +
                            "     * \n" +
                            "     * @param " + PhpCommentGenerator.TYPE_PLACEHOLDER + " $par^\n" +
                            "     */\n" +
                            "    function bar($par) {\n" +
                            "    }\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testIssue235110() throws Exception {
        insertBreak( "<?php\n" +
                            "class Prdel {\n" +
                            "    /**^\n" +
                            "    function functionName() {\n" +
                            "        return $this;\n" +
                            "    }\n" +
                            "}\n" +
                            "?>",
                            "<?php\n" +
                            "class Prdel {\n" +
                            "    /**\n" +
                            "     * \n" +
                            "     * @return $this^\n" +
                            "     */\n" +
                            "    function functionName() {\n" +
                            "        return $this;\n" +
                            "    }\n" +
                            "}\n" +
                            "?>");
    }

    public void testIssue236311() throws Exception {
        insertBreak("<?php\n" +
                "class MyCls {\n" +
                "    /**^\n" +
                "    public static function beginRequest()\n" +
                "    {\n" +
                "        foreach(array_keys($_GET) as $key) {\n" +
                "            array_filter($_GET[$key], function($var){\n" +
                "                return isset($var) && $var !== '';\n" +
                "            });\n" +
                "        }\n" +
                "        $_GET = array_filter($_GET);\n" +
                "    }\n" +
                "}\n" +
                "?>", "<?php\n" +
                "class MyCls {\n" +
                "    /**\n" +
                "     * ^\n" +
                "     */\n" +
                "    public static function beginRequest()\n" +
                "    {\n" +
                "        foreach(array_keys($_GET) as $key) {\n" +
                "            array_filter($_GET[$key], function($var){\n" +
                "                return isset($var) && $var !== '';\n" +
                "            });\n" +
                "        }\n" +
                "        $_GET = array_filter($_GET);\n" +
                "    }\n" +
                "}\n" +
                "?>");
    }

    public void testIssue242356() throws Exception {
        insertBreak("<?php\n" +
                "\n" +
                "interface Iface1 {\n" +
                "    /**^\n" +
                "    public function faceFnc($param);\n" +
                "}\n" +
                "?>", "<?php\n" +
                "\n" +
                "interface Iface1 {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param type $param^\n" +
                "     */\n" +
                "    public function faceFnc($param);\n" +
                "}\n" +
                "?>");
    }

    public void testResolveProperType_01() throws Exception {
        String original = "<?php\n" +
                "namespace foo\\bar;\n" +
                "use baz\\SomeClass;\n" +
                "class Test {\n" +
                "    /**^\n" +
                "    public function getSomething(SomeClass $someClass) {}\n" +
                "}";
        String expected = "<?php\n" +
                "namespace foo\\bar;\n" +
                "use baz\\SomeClass;\n" +
                "class Test {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param SomeClass $someClass^\n" +
                "     */\n" +
                "    public function getSomething(SomeClass $someClass) {}\n" +
                "}";
        insertBreak(original, expected);
    }

    public void testResolveProperType_02() throws Exception {
        String original = "<?php\n" +
                "namespace foo\\bar;\n" +
                "use baz\\SomeClass as SomeClassAlias;\n" +
                "class Test {\n" +
                "    /**^\n" +
                "    public function getSomething(SomeClassAlias $someClass) {}\n" +
                "}";
        String expected = "<?php\n" +
                "namespace foo\\bar;\n" +
                "use baz\\SomeClass as SomeClassAlias;\n" +
                "class Test {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param SomeClassAlias $someClass^\n" +
                "     */\n" +
                "    public function getSomething(SomeClassAlias $someClass) {}\n" +
                "}";
        insertBreak(original, expected);
    }

    public void testIssue248213Variadic() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "/**^\n"
                + "function foo(...$i) {\n"
                + "}\n"
                + "?>\n",

                // expected
                "<?php\n"
                + "/**\n"
                + " * \n"
                + " * @param " + PhpCommentGenerator.TYPE_PLACEHOLDER + " $i^\n"
                + " */\n"
                + "function foo(...$i) {\n"
                + "}\n"
                + "?>\n"
        );
    }

    public void testIssue248213ReferenceVariadic() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "class foo {\n"
                + "    /**^\n"
                + "    function bar(&...$par) {\n"
                + "    }\n"
                + "}\n"
                + "?>\n",

                // expected
                "<?php\n"
                + "class foo {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param " + PhpCommentGenerator.TYPE_PLACEHOLDER + " $par^\n"
                + "     */\n"
                + "    function bar(&...$par) {\n"
                + "    }\n"
                + "}\n"
                + "?>\n"
        );
    }

    public void testIssue269104_01() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "\n"
                + "namespace Foo;\n"
                + "\n"
                + "class Bar {\n"
                + "    /**^\n"
                + "    function callableType(callable $callable) {\n"
                + "        //...\n"
                + "    }\n"
                + "\n"
                + "}\n",

                // expected
                "<?php\n"
                + "\n"
                + "namespace Foo;\n"
                + "\n"
                + "class Bar {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param callable $callable^\n"
                + "     */\n"
                + "    function callableType(callable $callable) {\n"
                + "        //...\n"
                + "    }\n"
                + "\n"
                + "}\n"
        );
    }

     public void testIssue269104_02() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "\n"
                + "namespace Foo;\n"
                + "\n"
                + "class Bar {\n"
                + "    /**^\n"
                + "    function intType(int $int) {\n"
                + "        //...\n"
                + "    }\n"
                + "\n"
                + "}\n",

                // expected
                "<?php\n"
                + "\n"
                + "namespace Foo;\n"
                + "\n"
                + "class Bar {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param int $int^\n"
                + "     */\n"
                + "    function intType(int $int) {\n"
                + "        //...\n"
                + "    }\n"
                + "\n"
                + "}\n"
        );
    }

     public void testIssue269104_03() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "\n"
                + "namespace Foo;\n"
                + "\n"
                + "class Bar {\n"
                + "    /**^\n"
                + "    function iterableType(iterable $iterable) {\n"
                + "        //...\n"
                + "    }\n"
                + "\n"
                + "}\n",

                // expected
                "<?php\n"
                + "\n"
                + "namespace Foo;\n"
                + "\n"
                + "class Bar {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param iterable $iterable^\n"
                + "     */\n"
                + "    function iterableType(iterable $iterable) {\n"
                + "        //...\n"
                + "    }\n"
                + "\n"
                + "}\n"
        );
    }

    // Nullable Types
    public void testIssue270235_01() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "class Foo {}\n"
                + "/**^\n"
                + "function test(?string $string): ?Foo {\n"
                + "    \n"
                + "}",

                // expected
                "<?php\n"
                + "class Foo {}\n"
                + "/**\n"
                + " * \n"
                + " * @param string|null $string\n"
                + " * @return Foo|null^\n"
                + " */\n"
                + "function test(?string $string): ?Foo {\n"
                + "    \n"
                + "}"
        );
    }

    public void testIssue270235_02() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "class Foo {}\n"
                + "interface TestInterface {\n"
                + "    /**^\n"
                + "    public function test(?int $int) : ?Foo;\n"
                + "}",

                // expected
                "<?php\n"
                + "class Foo {}\n"
                + "interface TestInterface {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param int|null $int\n"
                + "     * @return Foo|null^\n"
                + "     */\n"
                + "    public function test(?int $int) : ?Foo;\n"
                + "}"
        );
    }

    public void testVoidReturnType() throws Exception {
        insertBreak(
                // original
                "<?php\n"
                + "/**^\n"
                + "function test(?string $string): void {\n"
                + "    \n"
                + "}",

                // expected
                "<?php\n"
                + "/**\n"
                + " * \n"
                + " * @param string|null $string\n"
                + " * @return void^\n"
                + " */\n"
                + "function test(?string $string): void {\n"
                + "    \n"
                + "}"
        );
    }

    public void testThrowExpression() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "/**^\n"
                + "function test(?string $string): void {\n"
                + "    throw new Exception();\n"
                + "}",

                // expected
                ""
                + "<?php\n"
                + "/**\n"
                + " * \n"
                + " * @param string|null $string\n"
                + " * @return void\n"
                + " * @throws Exception^\n"
                + " */\n"
                + "function test(?string $string): void {\n"
                + "    throw new Exception();\n"
                + "}"
        );
    }

    public void testUnionTypes01() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "class Class1{}\n"
                + "class Class2{}\n"
                + "/**^\n"
                + "function test(Class1|Class2|false|null $param): Class1|Class2|false|null {\n"
                + "}",

                // expected
                ""
                + "<?php\n"
                + "class Class1{}\n"
                + "class Class2{}\n"
                + "/**\n"
                + " * \n"
                + " * @param Class1|Class2|false|null $param\n"
                + " * @return Class1|Class2|false|null^\n"
                + " */\n"
                + "function test(Class1|Class2|false|null $param): Class1|Class2|false|null {\n"
                + "}"
        );
    }

    public void testUnionTypes02() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "/**^\n"
                + "function test(Class1|Class2|false|null $param): Class1|Class2|false|null {\n"
                + "}\n\n"
                + "namespace Test2;\n"
                + "class Class1{}\n"
                + "class Class2{}\n",
                // expected
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "/**\n"
                + " * \n"
                + " * @param Class1|Class2|false|null $param\n"
                + " * @return Class1|Class2|false|null^\n"
                + " */\n"
                + "function test(Class1|Class2|false|null $param): Class1|Class2|false|null {\n"
                + "}\n\n"
                + "namespace Test2;\n"
                + "class Class1{}\n"
                + "class Class2{}\n"
        );
    }

    public void testUnionTypes03() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "use Test0\\Class0;\n"
                + "\n"
                + "class Class1 extends Class0 {\n"
                + "\n"
                + "    /**^\n"
                + "    public function create(parent|self $object): self|parent {\n"
                + "        return new Class1;\n"
                + "    }\n"
                + "\n"
                + "}\n"
                + "\n"
                + "namespace Test0;\n"
                + "class Class0 {}",
                // expected
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "use Test0\\Class0;\n"
                + "\n"
                + "class Class1 extends Class0 {\n"
                + "\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param parent|self $object\n"
                + "     * @return self|parent^\n"
                + "     */\n"
                + "    public function create(parent|self $object): self|parent {\n"
                + "        return new Class1;\n"
                + "    }\n"
                + "\n"
                + "}\n"
                + "\n"
                + "namespace Test0;\n"
                + "class Class0 {}"
        );
    }

    public void testUnionTypes04() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "use Test2\\Class2;\n"
                + "\n"
                + "class Class1 {\n"
                + "\n"
                + "    /**^\n"
                + "    public Class2|string|int $test;\n"
                + "\n"
                + "}\n"
                + "\n"
                + "\n"
                + "namespace Test2;\n"
                + "class Class2 {}",
                // expected
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "use Test2\\Class2;\n"
                + "\n"
                + "class Class1 {\n"
                + "\n"
                + "    /**\n"
                + "     * \n"
                + "     * @var Class2|string|int^\n"
                + "     */\n"
                + "    public Class2|string|int $test;\n"
                + "\n"
                + "}\n"
                + "\n"
                + "\n"
                + "namespace Test2;\n"
                + "class Class2 {}"
        );
    }

    public void testStaicReturnType() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "class StaticReturnType {\n"
                + "    /**^\n"
                + "    public function test(): static {\n"
                + "    }\n"
                + "}",
                // expected
                ""
                + "<?php\n"
                + "class StaticReturnType {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @return static^\n"
                + "     */\n"
                + "    public function test(): static {\n"
                + "    }\n"
                + "}"
        );
    }

    public void testStaicReturnTypeWithNullableType() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "class StaticReturnType {\n"
                + "    /**^\n"
                + "    public function testNullable(): ?static {\n"
                + "    }\n"
                + "}",
                // expected
                ""
                + "<?php\n"
                + "class StaticReturnType {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @return static|null^\n"
                + "     */\n"
                + "    public function testNullable(): ?static {\n"
                + "    }\n"
                + "}"
        );
    }

    public void testStaicReturnTypeWithUnionType() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "class StaticReturnType {\n"
                + "    /**^\n"
                + "    public function testUnionType(): static|null|string {\n"
                + "    }\n"
                + "}",
                // expected
                ""
                + "<?php\n"
                + "class StaticReturnType {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @return static|null|string^\n"
                + "     */\n"
                + "    public function testUnionType(): static|null|string {\n"
                + "    }\n"
                + "}"
        );
    }

    public void testIntersectionTypes01() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "class Class1{}\n"
                + "class Class2{}\n"
                + "/**^\n"
                + "function test(Class1&Class2 $param): Class1&Class2 {\n"
                + "}",

                // expected
                ""
                + "<?php\n"
                + "class Class1{}\n"
                + "class Class2{}\n"
                + "/**\n"
                + " * \n"
                + " * @param Class1&Class2 $param\n"
                + " * @return Class1&Class2^\n"
                + " */\n"
                + "function test(Class1&Class2 $param): Class1&Class2 {\n"
                + "}"
        );
    }

    public void testIntersectionTypes02() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "/**^\n"
                + "function test(Class1&Class2 $param): Class1&Class2 {\n"
                + "}\n\n"
                + "namespace Test2;\n"
                + "class Class1{}\n"
                + "class Class2{}\n",
                // expected
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "/**\n"
                + " * \n"
                + " * @param Class1&Class2 $param\n"
                + " * @return Class1&Class2^\n"
                + " */\n"
                + "function test(Class1&Class2 $param): Class1&Class2 {\n"
                + "}\n\n"
                + "namespace Test2;\n"
                + "class Class1{}\n"
                + "class Class2{}\n"
        );
    }

    public void testIntersectionTypes03() throws Exception {
        insertBreak(
                // original
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "use Test2\\Class2;\n"
                + "\n"
                + "class Class1 {\n"
                + "\n"
                + "    /**^\n"
                + "    public Class1&Class2 $test;\n"
                + "\n"
                + "}\n"
                + "\n"
                + "\n"
                + "namespace Test2;\n"
                + "class Class2 {}",
                // expected
                ""
                + "<?php\n"
                + "namespace Test1;\n"
                + "use Test2\\Class2;\n"
                + "\n"
                + "class Class1 {\n"
                + "\n"
                + "    /**\n"
                + "     * \n"
                + "     * @var Class1&Class2^\n"
                + "     */\n"
                + "    public Class1&Class2 $test;\n"
                + "\n"
                + "}\n"
                + "\n"
                + "\n"
                + "namespace Test2;\n"
                + "class Class2 {}"
        );
    }

    public void testFunctionGuessingBoolReturnType() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    return true;\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * \n" +
                            " * @return bool^\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    return true;\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionGuessingNullReturnType() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "function testFunction() {\n" +
                            "    if ($a) {\n" +
                            "        return 1;\n" +
                            "    }\n" +
                            "    return null;\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * \n" +
                            " * @return null|int^\n" +
                            " */\n" +
                             "function testFunction() {\n" +
                            "    if ($a) {\n" +
                            "        return 1;\n" +
                            "    }\n" +
                            "    return null;\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionGuessingArrayReturnType() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    return [1, 2];\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * \n" +
                            " * @return array^\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    return [1, 2];\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionGuessingArrayReturnTypeWithUnionType() throws Exception {
        insertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    if (true) {\n" +
                            "       return 'str';\n" +
                            "    }\n" +
                            "    return [1, 2];\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * \n" +
                            " * @return string|array^\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    if (true) {\n" +
                            "       return 'str';\n" +
                            "    }\n" +
                            "    return [1, 2];\n" +
                            "}\n" +
                            "?>\n");
    }

    @Override
    public void insertNewline(String source, String reformatted, IndentPrefs preferences) throws Exception {
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos + 1);
        Formatter formatter = getFormatter(null);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos + 1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        // wait for generating comment
        Future<?> future = PhpCommentGenerator.RP.submit(() -> {});
        future.get();

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

}
