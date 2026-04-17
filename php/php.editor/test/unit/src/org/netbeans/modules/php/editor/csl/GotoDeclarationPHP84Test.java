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

public class GotoDeclarationPHP84Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP84Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php84/";
    }

    public void testNewWithoutParentheses_01a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::IMPLICI^T_PUBLIC_CONST;",
                "    const ^IMPLICIT_PUBLIC_CONST = \"implicit public const\";"
        );
    }

    public void testNewWithoutParentheses_01b() throws Exception {
        checkDeclaration(
                "new Example()::IMPLICIT_PUBLIC_CO^NST; // test",
                "    const ^IMPLICIT_PUBLIC_CONST = \"implicit public const\";"
        );
    }

    public void testNewWithoutParentheses_01c() throws Exception {
        checkDeclaration(
                "new Example()->returnThis()::IMPLICIT_PUBLIC_C^ONST; // test",
                "    const ^IMPLICIT_PUBLIC_CONST = \"implicit public const\";"
        );
    }

    public void testNewWithoutParentheses_02a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::PUBLI^C_CONST;",
                "    public const string ^PUBLIC_CONST = \"public const\";"
        );
    }

    public void testNewWithoutParentheses_02b() throws Exception {
        checkDeclaration(
                "new Example()::PUBLIC_C^ONST; // test",
                "    public const string ^PUBLIC_CONST = \"public const\";"
        );
    }

    public void testNewWithoutParentheses_02c() throws Exception {
        checkDeclaration(
                "new Example()->returnThis()::PUBLIC_^CONST; // test",
                "    public const string ^PUBLIC_CONST = \"public const\";"
        );
    }

    public void testNewWithoutParentheses_03a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::PROTECTED^_CONST;",
                "    protected const string ^PROTECTED_CONST = \"protected const\";"
        );
    }

    public void testNewWithoutParentheses_04a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::PRIVATE_CO^NST;",
                "    private const string ^PRIVATE_CONST = \"private const\";"
        );
    }

    public void testNewWithoutParentheses_04b() throws Exception {
        checkDeclaration(
                "        $example = new Example()?->returnThis()::PRIVATE_^CONST;",
                "    private const string ^PRIVATE_CONST = \"private const\";"
        );
    }

    public void testNewWithoutParentheses_05a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->publicF^ield;",
                "    public int $^publicField = 1;"
        );
    }

    public void testNewWithoutParentheses_05b() throws Exception {
        checkDeclaration(
                "new Example()->public^Field; // test",
                "    public int $^publicField = 1;"
        );
    }

    public void testNewWithoutParentheses_05c() throws Exception {
        checkDeclaration(
                "new Example()->returnThis()->publicF^ield; // test",
                "    public int $^publicField = 1;"
        );
    }

    public void testNewWithoutParentheses_06a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->protecte^dField;",
                "    protected int $^protectedField = 2;"
        );
    }

    public void testNewWithoutParentheses_07a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->pri^vateField;",
                "    private int $^privateField = 3;"
        );
    }

    public void testNewWithoutParentheses_07b() throws Exception {
        checkDeclaration(
                "        $example = new Example()->returnThis()?->privateFie^ld;",
                "    private int $^privateField = 3;"
        );
    }

    public void testNewWithoutParentheses_08a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::$publicSt^aticField;",
                "    public static string $^publicStaticField = \"public static field\";"
        );
    }

    public void testNewWithoutParentheses_08b() throws Exception {
        checkDeclaration(
                "new Example()::$publicStati^cField; // test",
                "    public static string $^publicStaticField = \"public static field\";"
        );
    }

    public void testNewWithoutParentheses_08c() throws Exception {
        checkDeclaration(
                "new Example()->returnThis()::$publicStat^icField; // test",
                "    public static string $^publicStaticField = \"public static field\";"
        );
    }

    public void testNewWithoutParentheses_09a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::$protectedSta^ticField;",
                "    protected static string $^protectedStaticField = \"protected static field\";"
        );
    }

    public void testNewWithoutParentheses_09b() throws Exception {
        checkDeclaration(
                "        $example = new Example()->returnThis()::$protectedStaticF^ield;",
                "    protected static string $^protectedStaticField = \"protected static field\";"
        );
    }

    public void testNewWithoutParentheses_10a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::$privateSta^ticField;",
                "    private static string $^privateStaticField = \"private static field\";"
        );
    }

    public void testNewWithoutParentheses_11a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->returnTh^is()?->privateField;",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11b() throws Exception {
        checkDeclaration(
                "        $example = new Example()->return^This()->publicMethod();",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11c() throws Exception {
        checkDeclaration(
                "        $example = new Example()->retur^nThis()::$protectedStaticField;",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11d() throws Exception {
        checkDeclaration(
                "        $example = new Example()?->returnT^his()::PRIVATE_CONST;",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11e() throws Exception {
        checkDeclaration(
                "        $example = new Example()->return^This()::privateStaticMethod();",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11f() throws Exception {
        checkDeclaration(
                "new Example()->returnT^his()->publicField; // test",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11g() throws Exception {
        checkDeclaration(
                "new Example()->return^This()::$publicStaticField; // test",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11h() throws Exception {
        checkDeclaration(
                "new Example()->returnT^his()::IMPLICIT_PUBLIC_CONST; // test",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11i() throws Exception {
        checkDeclaration(
                "new Example()->return^This()::PUBLIC_CONST; // test",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11j() throws Exception {
        checkDeclaration(
                "new Example()->returnTh^is()->publicMethod(); // test",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_11k() throws Exception {
        checkDeclaration(
                "new Example()->returnThi^s()::publicStaticMethod(); // test",
                "    public function ^returnThis(): self {"
        );
    }

    public void testNewWithoutParentheses_12a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->publicMe^thod();",
                "    public function ^publicMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_12b() throws Exception {
        checkDeclaration(
                "        $example = new Example()->returnThis()->publicMet^hod();",
                "    public function ^publicMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_12c() throws Exception {
        checkDeclaration(
                "new Example()->publicM^ethod(); // test",
                "    public function ^publicMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_12d() throws Exception {
        checkDeclaration(
                "new Example()->returnThis()->publicM^ethod(); // test",
                "    public function ^publicMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_13a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->protectedM^ethod();",
                "    protected function ^protectedMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_14a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->privateMeth^od();",
                "    private function ^privateMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_15a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::publicStaticMeth^od();",
                "    public static function ^publicStaticMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_15b() throws Exception {
        checkDeclaration(
                "new Example()::publicStatic^Method(); // test",
                "    public static function ^publicStaticMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_15c() throws Exception {
        checkDeclaration(
                "new Example()->returnThis()::publicStaticMet^hod(); // test",
                "    public static function ^publicStaticMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_16a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::protectedStatic^Method();",
                "    protected static function ^protectedStaticMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_17a() throws Exception {
        checkDeclaration(
                "        $example = new Example()::privateStaticM^ethod();",
                "    private static function ^privateStaticMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_17b() throws Exception {
        checkDeclaration(
                "        $example = new Example()->returnThis()::privateStaticM^ethod();",
                "    private static function ^privateStaticMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_18a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->te^st->publicTestField;",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18b() throws Exception {
        checkDeclaration(
                "        $example = new Example()->tes^t->publicTestMethod();",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18c() throws Exception {
        checkDeclaration(
                "        $example = new Example()->t^est::PUBLIC_TEST_CONST;",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18d() throws Exception {
        checkDeclaration(
                "        $example = new Example()->t^est::$publicStaticTestField;",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18() throws Exception {
        checkDeclaration(
                "        $example = new Example()->tes^t::publicStaticTestMethod();",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18e() throws Exception {
        checkDeclaration(
                "new Example()->t^est->publicTestField; // test",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18f() throws Exception {
        checkDeclaration(
                "new Example()->te^st->publicTestMethod(); // test",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18g() throws Exception {
        checkDeclaration(
                "new Example()->tes^t::PUBLIC_TEST_CONST; // test",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18h() throws Exception {
        checkDeclaration(
                "new Example()->tes^t::$publicStaticTestField; // test",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_18i() throws Exception {
        checkDeclaration(
                "new Example()->te^st::publicStaticTestMethod(); // test",
                "    public Test $^test;"
        );
    }

    public void testNewWithoutParentheses_19a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->test->publicTes^tField;",
                "    public int $^publicTestField = 1;"
        );
    }

    public void testNewWithoutParentheses_19b() throws Exception {
        checkDeclaration(
                "new Example()->test->publicTestFie^ld; // test",
                "    public int $^publicTestField = 1;"
        );
    }

    public void testNewWithoutParentheses_20a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->test->publicTestM^ethod();",
                "    public function ^publicTestMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_20b() throws Exception {
        checkDeclaration(
                "new Example()->test->publicTestMe^thod(); // test",
                "    public function ^publicTestMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_21a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->test::PUBLIC_TEST_C^ONST;",
                "    public const string ^PUBLIC_TEST_CONST = \"public test const\";"
        );
    }

    public void testNewWithoutParentheses_21b() throws Exception {
        checkDeclaration(
                "new Example()->test::PUBLIC_TEST^_CONST; // test",
                "    public const string ^PUBLIC_TEST_CONST = \"public test const\";"
        );
    }

    public void testNewWithoutParentheses_22a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->test::$publicStat^icTestField;",
                "    public static string $^publicStaticTestField = \"public static test field\";"
        );
    }

    public void testNewWithoutParentheses_22b() throws Exception {
        checkDeclaration(
                "new Example()->test::$publicStaticTest^Field; // test",
                "    public static string $^publicStaticTestField = \"public static test field\";"
        );
    }

    public void testNewWithoutParentheses_23a() throws Exception {
        checkDeclaration(
                "        $example = new Example()->test::publicStaticTes^tMethod();",
                "    public static function ^publicStaticTestMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_23b() throws Exception {
        checkDeclaration(
                "new Example()->test::publicStaticTest^Method(); // test",
                "    public static function ^publicStaticTestMethod(): string {"
        );
    }

    public void testNewWithoutParentheses_24a() throws Exception {
        checkDeclaration(
                "        $example = new Exam^ple()::IMPLICIT_PUBLIC_CONST;",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24b() throws Exception {
        checkDeclaration(
                "        $example = new Exam^ple()->publicField;",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24c() throws Exception {
        checkDeclaration(
                "        $example = new E^xample()::$publicStaticField;",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24d() throws Exception {
        checkDeclaration(
                "        $example = new Exam^ple()->publicMethod();",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24e() throws Exception {
        checkDeclaration(
                "        $example = new Exam^ple()::publicStaticMethod();",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24f() throws Exception {
        checkDeclaration(
                "        $example = new Exam^ple()->returnThis()?->privateField;",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24g() throws Exception {
        checkDeclaration(
                "        $example = new Exa^mple()->returnThis()::$protectedStaticField;",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24h() throws Exception {
        checkDeclaration(
                "        $example = new Exam^ple()->test->publicTestField;",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24i() throws Exception {
        checkDeclaration(
                "        $example = new Ex^ample()->test::PUBLIC_TEST_CONST;",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24j() throws Exception {
        checkDeclaration(
                "new Ex^ample()->publicField; // test",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24k() throws Exception {
        checkDeclaration(
                "new Exam^ple()::publicStaticMethod(); // test",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24l() throws Exception {
        checkDeclaration(
                "new Exa^mple()->returnThis()->publicField; // test",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24m() throws Exception {
        checkDeclaration(
                "new Exa^mple()->returnThis()::PUBLIC_CONST; // test",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24n() throws Exception {
        checkDeclaration(
                "new Exam^ple()->test->publicTestMethod(); // test",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_24o() throws Exception {
        checkDeclaration(
                "new Examp^le()->test::$publicStaticTestField; // test",
                "    public function ^__construct() {"
        );
    }

    public void testNewWithoutParentheses_25a() throws Exception {
        checkDeclaration(
                "}::PUBLIC_CO^NSTANT;",
                "    public const string ^PUBLIC_CONSTANT = 'public constant'; // anon1"
        );
    }

    public void testNewWithoutParentheses_26a() throws Exception {
        checkDeclaration(
                "}->pu^blicField;",
                "    public string $^publicField = 'public field'; // anon2"
        );
    }

    public void testNewWithoutParentheses_27a() throws Exception {
        checkDeclaration(
                "}::$publicStati^cField;",
                "    public static string $^publicStaticField = 'public static field'; // anon3"
        );
    }

    public void testNewWithoutParentheses_28a() throws Exception {
        checkDeclaration(
                "}->publicMet^hod();",
                "    public function ^publicMethod(): void {} // anon4"
        );
    }

    public void testNewWithoutParentheses_29a() throws Exception {
        checkDeclaration(
                "}::publicStati^cMethod();",
                "    public static function ^publicStaticMethod(): void {} // anon5"
        );
    }

    public void testNewWithoutParentheses_30a() throws Exception {
        checkDeclaration(
                "}::publicStaticMethod()->publicMethod()->publ^icField;",
                "    public string $^publicField = 'public field'; // anon6"
        );
    }

    public void testNewWithoutParentheses_31a() throws Exception {
        checkDeclaration(
                "}::publicStaticMethod()->publicMe^thod()->publicField;",
                "    public function ^publicMethod(): self {} // anon6"
        );
    }

    public void testNewWithoutParentheses_32a() throws Exception {
        checkDeclaration(
                "}::publicStat^icMethod()->publicMethod()->publicField;",
                "    public static function ^publicStaticMethod(): self {} // anon6"
        );
    }

    public void testNewWithoutParentheses_33a() throws Exception {
        checkDeclaration(
                "}->publicMethod()::publicStaticMethod()->publi^cField;",
                "    public string $^publicField = 'public field'; // anon7"
        );
    }

    public void testNewWithoutParentheses_34a() throws Exception {
        checkDeclaration(
                "}->public^Method()::publicStaticMethod()->publicField;",
                "    public function ^publicMethod(): self {} // anon7"
        );
    }

    public void testNewWithoutParentheses_35a() throws Exception {
        checkDeclaration(
                "}->publicMethod()::publicSt^aticMethod()->publicField;",
                "    public static function ^publicStaticMethod(): self {} // anon7"
        );
    }

    public void testNewWithoutParentheses_36a() throws Exception {
        checkDeclaration(
                "}->publicMethod()::publicStaticMethod()::PUBLIC_^CONSTANT;",
                "    public const string ^PUBLIC_CONSTANT = 'public constant'; // anon8"
        );
    }

    public void testNewWithoutParentheses_37a() throws Exception {
        checkDeclaration(
                "}->publicM^ethod()::publicStaticMethod()::PUBLIC_CONSTANT;",
                "    public function ^publicMethod(): self {} // anon8"
        );
    }

    public void testNewWithoutParentheses_38a() throws Exception {
        checkDeclaration(
                "}->publicMethod()::publicStat^icMethod()::PUBLIC_CONSTANT;",
                "    public static function ^publicStaticMethod(): self {} // anon8"
        );
    }
}
