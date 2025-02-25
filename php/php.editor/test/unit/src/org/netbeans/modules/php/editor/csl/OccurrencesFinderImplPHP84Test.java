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

public class OccurrencesFinderImplPHP84Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP84Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php84/";
    }

    public void testNewWithoutParentheses_01a() throws Exception {
        checkOccurrences("    public const string PUBLIC_TEST_^CONST = \"public test const\";");
    }

    public void testNewWithoutParentheses_01b() throws Exception {
        checkOccurrences("        $example = new Example()->test::PUBLIC_TEST^_CONST;");
    }

    public void testNewWithoutParentheses_01c() throws Exception {
        checkOccurrences("new Example()->test::PUBLIC_TEST^_CONST; // test");
    }

    public void testNewWithoutParentheses_02a() throws Exception {
        checkOccurrences("    public int $publicTestFie^ld = 1;");
    }

    public void testNewWithoutParentheses_02b() throws Exception {
        checkOccurrences("        $example = new Example()->test->publicTestF^ield;");
    }

    public void testNewWithoutParentheses_02c() throws Exception {
        checkOccurrences("new Example()->test->publicTestFi^eld; // test");
    }

    public void testNewWithoutParentheses_03a() throws Exception {
        checkOccurrences("    public static string $publicStaticTestF^ield = \"public static test field\";");
    }

    public void testNewWithoutParentheses_03b() throws Exception {
        checkOccurrences("        $example = new Example()->test::$publicStati^cTestField;");
    }

    public void testNewWithoutParentheses_03c() throws Exception {
        checkOccurrences("new Example()->test::$publicStati^cTestField; // test");
    }

    public void testNewWithoutParentheses_04a() throws Exception {
        checkOccurrences("    public function publicTestMeth^od(): string {");
    }

    public void testNewWithoutParentheses_04b() throws Exception {
        checkOccurrences("        $example = new Example()->test->pu^blicTestMethod();");
    }

    public void testNewWithoutParentheses_04c() throws Exception {
        checkOccurrences("new Example()->test->publicTest^Method(); // test");
    }

    public void testNewWithoutParentheses_05a() throws Exception {
        checkOccurrences("    public static function publicStaticTes^tMethod(): string {");
    }

    public void testNewWithoutParentheses_05b() throws Exception {
        checkOccurrences("        $example = new Example()->test::publicStaticTes^tMethod();");
    }

    public void testNewWithoutParentheses_05c() throws Exception {
        checkOccurrences("new Example()->test::pu^blicStaticTestMethod(); // test");
    }

    public void testNewWithoutParentheses_06a() throws Exception {
        checkOccurrences("    const IMPLICIT_PUBLIC_^CONST = \"implicit public const\";");
    }

    public void testNewWithoutParentheses_06b() throws Exception {
        checkOccurrences("        $example = new Example()::IMPLICIT_PUBLIC^_CONST;");
    }

    public void testNewWithoutParentheses_06c() throws Exception {
        checkOccurrences("new Example()::IMPLICIT_PUBLI^C_CONST; // test");
    }

    public void testNewWithoutParentheses_06d() throws Exception {
        checkOccurrences("new Example()->returnThis()::IMPLICIT_PUBLI^C_CONST; // test");
    }

    public void testNewWithoutParentheses_07a() throws Exception {
        checkOccurrences("    public const string PUBLIC_CO^NST = \"public const\";");
    }

    public void testNewWithoutParentheses_07b() throws Exception {
        checkOccurrences("        $example = new Example()::PUBLIC_CON^ST;");
    }

    public void testNewWithoutParentheses_07c() throws Exception {
        checkOccurrences("new Example()::PUBLI^C_CONST; // test");
    }

    public void testNewWithoutParentheses_07d() throws Exception {
        checkOccurrences("new Example()->returnThis()::PUBL^IC_CONST; // test");
    }

    public void testNewWithoutParentheses_08a() throws Exception {
        checkOccurrences("    protected const string PROTECTED_CO^NST = \"protected const\";");
    }

    public void testNewWithoutParentheses_08b() throws Exception {
        checkOccurrences("        $example = new Example()::PROTECTE^D_CONST;");
    }

    public void testNewWithoutParentheses_09a() throws Exception {
        checkOccurrences("    private const string PRIVAT^E_CONST = \"private const\";");
    }

    public void testNewWithoutParentheses_09b() throws Exception {
        checkOccurrences("        $example = new Example()::PRIVATE_CON^ST;");
    }

    public void testNewWithoutParentheses_09c() throws Exception {
        checkOccurrences("        $example = new Example()?->returnThis()::PRIV^ATE_CONST;");
    }

    public void testNewWithoutParentheses_10a() throws Exception {
        checkOccurrences("    public int $publ^icField = 1;");
    }

    public void testNewWithoutParentheses_10b() throws Exception {
        checkOccurrences("        $example = new Example()->publicFi^eld;");
    }

    public void testNewWithoutParentheses_10c() throws Exception {
        checkOccurrences("new Example()->public^Field; // test");
    }

    public void testNewWithoutParentheses_10d() throws Exception {
        checkOccurrences("new Example()->returnThis()->publicFi^eld; // test");
    }

    public void testNewWithoutParentheses_11a() throws Exception {
        checkOccurrences("    protected int $protectedFie^ld = 2;");
    }

    public void testNewWithoutParentheses_11b() throws Exception {
        checkOccurrences("        $example = new Example()->protectedFiel^d;");
    }

    public void testNewWithoutParentheses_12a() throws Exception {
        checkOccurrences("    private int $private^Field = 3;");
    }

    public void testNewWithoutParentheses_12b() throws Exception {
        checkOccurrences("        $example = new Example()->priva^teField;");
    }

    public void testNewWithoutParentheses_12c() throws Exception {
        checkOccurrences("        $example = new Example()->returnThis()?->priva^teField;");
    }

    public void testNewWithoutParentheses_13a() throws Exception {
        checkOccurrences("    public Test $t^est;");
    }

    public void testNewWithoutParentheses_13b() throws Exception {
        checkOccurrences("        $example = new Example()->te^st->publicTestField;");
    }

    public void testNewWithoutParentheses_13c() throws Exception {
        checkOccurrences("        $example = new Example()->te^st->publicTestMethod();");
    }

    public void testNewWithoutParentheses_13d() throws Exception {
        checkOccurrences("        $example = new Example()->^test::PUBLIC_TEST_CONST;");
    }

    public void testNewWithoutParentheses_13e() throws Exception {
        checkOccurrences("        $example = new Example()->t^est::$publicStaticTestField;");
    }

    public void testNewWithoutParentheses_13f() throws Exception {
        checkOccurrences("        $example = new Example()->tes^t::publicStaticTestMethod();");
    }

    public void testNewWithoutParentheses_13g() throws Exception {
        checkOccurrences("new Example()->tes^t->publicTestField; // test");
    }

    public void testNewWithoutParentheses_13h() throws Exception {
        checkOccurrences("new Example()->t^est->publicTestMethod(); // test");
    }

    public void testNewWithoutParentheses_13i() throws Exception {
        checkOccurrences("new Example()->tes^t::PUBLIC_TEST_CONST; // test");
    }

    public void testNewWithoutParentheses_13j() throws Exception {
        checkOccurrences("new Example()->te^st::$publicStaticTestField; // test");
    }

    public void testNewWithoutParentheses_13k() throws Exception {
        checkOccurrences("new Example()->te^st::publicStaticTestMethod(); // test");
    }

    public void testNewWithoutParentheses_14a() throws Exception {
        checkOccurrences("    public static string $publicStaticFie^ld = \"public static field\";");
    }

    public void testNewWithoutParentheses_14b() throws Exception {
        checkOccurrences("        $example = new Example()::$publicSt^aticField;");
    }

    public void testNewWithoutParentheses_14c() throws Exception {
        checkOccurrences("new Example()::$publicS^taticField; // test");
    }

    public void testNewWithoutParentheses_14d() throws Exception {
        checkOccurrences("new Example()->returnThis()::$publicStat^icField; // test");
    }

    public void testNewWithoutParentheses_15a() throws Exception {
        checkOccurrences("    protected static string $protectedSta^ticField = \"protected static field\";");
    }

    public void testNewWithoutParentheses_15b() throws Exception {
        checkOccurrences("        $example = new Example()::$protecte^dStaticField;");
    }

    public void testNewWithoutParentheses_15c() throws Exception {
        checkOccurrences("        $example = new Example()->returnThis()::$protected^StaticField;");
    }

    public void testNewWithoutParentheses_16a() throws Exception {
        checkOccurrences("    private static string $private^StaticField = \"private static field\";");
    }

    public void testNewWithoutParentheses_16b() throws Exception {
        checkOccurrences("        $example = new Example()::$privateSta^ticField;");
    }

    public void testNewWithoutParentheses_17a() throws Exception {
        checkOccurrences("        $example = new Example()->returnT^his()?->privateField;");
    }

    public void testNewWithoutParentheses_17b() throws Exception {
        checkOccurrences("        $example = new Example()->returnTh^is()->publicMethod();");
    }

    public void testNewWithoutParentheses_17c() throws Exception {
        checkOccurrences("        $example = new Example()->returnTh^is()::$protectedStaticField;");
    }

    public void testNewWithoutParentheses_17d() throws Exception {
        checkOccurrences("        $example = new Example()?->returnT^his()::PRIVATE_CONST;");
    }

    public void testNewWithoutParentheses_17e() throws Exception {
        checkOccurrences("        $example = new Example()->returnT^his()::privateStaticMethod();");
    }

    public void testNewWithoutParentheses_17f() throws Exception {
        checkOccurrences("    public function returnT^his(): self {");
    }

    public void testNewWithoutParentheses_17g() throws Exception {
        checkOccurrences("new Example()->return^This()->publicField; // test");
    }

    public void testNewWithoutParentheses_17h() throws Exception {
        checkOccurrences("new Example()->returnT^his()::$publicStaticField; // test");
    }

    public void testNewWithoutParentheses_17i() throws Exception {
        checkOccurrences("new Example()->re^turnThis()::IMPLICIT_PUBLIC_CONST; // test");
    }

    public void testNewWithoutParentheses_17j() throws Exception {
        checkOccurrences("new Example()->r^eturnThis()::PUBLIC_CONST; // test");
    }

    public void testNewWithoutParentheses_17k() throws Exception {
        checkOccurrences("new Example()->retur^nThis()->publicMethod(); // test");
    }

    public void testNewWithoutParentheses_17l() throws Exception {
        checkOccurrences("new Example()->returnT^his()::publicStaticMethod(); // test");
    }

    public void testNewWithoutParentheses_18a() throws Exception {
        checkOccurrences("        $example = new Example()->pu^blicMethod();");
    }

    public void testNewWithoutParentheses_18b() throws Exception {
        checkOccurrences("        $example = new Example()->returnThis()->pub^licMethod();");
    }

    public void testNewWithoutParentheses_18c() throws Exception {
        checkOccurrences("    public function pu^blicMethod(): string {");
    }

    public void testNewWithoutParentheses_18d() throws Exception {
        checkOccurrences("new Example()->publicM^ethod(); // test");
    }

    public void testNewWithoutParentheses_18e() throws Exception {
        checkOccurrences("new Example()->returnThis()->publicM^ethod(); // test");
    }

    public void testNewWithoutParentheses_19a() throws Exception {
        checkOccurrences("        $example = new Example()->protectedMeth^od();");
    }

    public void testNewWithoutParentheses_19b() throws Exception {
        checkOccurrences("    protected function protectedMet^hod(): string {");
    }

    public void testNewWithoutParentheses_20a() throws Exception {
        checkOccurrences("        $example = new Example()->privat^eMethod();");
    }

    public void testNewWithoutParentheses_20b() throws Exception {
        checkOccurrences("    private function privateMeth^od(): string {");
    }

    public void testNewWithoutParentheses_21a() throws Exception {
        checkOccurrences("        $example = new Example()::publicSta^ticMethod();");
    }

    public void testNewWithoutParentheses_21b() throws Exception {
        checkOccurrences("    public static function publicSt^aticMethod(): string {");
    }

    public void testNewWithoutParentheses_21c() throws Exception {
        checkOccurrences("new Example()::pub^licStaticMethod(); // test");
    }

    public void testNewWithoutParentheses_21d() throws Exception {
        checkOccurrences("new Example()->returnThis()::publicStaticMe^thod(); // test");
    }

    public void testNewWithoutParentheses_22a() throws Exception {
        checkOccurrences("        $example = new Example()::protectedStatic^Method();");
    }

    public void testNewWithoutParentheses_22b() throws Exception {
        checkOccurrences("    protected static function protectedStat^icMethod(): string {");
    }

    public void testNewWithoutParentheses_23a() throws Exception {
        checkOccurrences("        $example = new Example()::privateStat^icMethod();");
    }

    public void testNewWithoutParentheses_23b() throws Exception {
        checkOccurrences("        $example = new Example()->returnThis()::priva^teStaticMethod();");
    }

    public void testNewWithoutParentheses_23c() throws Exception {
        checkOccurrences("    private static function priv^ateStaticMethod(): string {");
    }

    public void testNewWithoutParentheses_24a() throws Exception {
        checkOccurrences("class Examp^le {");
    }

    public void testNewWithoutParentheses_24b() throws Exception {
        checkOccurrences("        $example = new Exam^ple()::IMPLICIT_PUBLIC_CONST;");
    }

    public void testNewWithoutParentheses_24c() throws Exception {
        checkOccurrences("        $example = new Exam^ple()->publicField;");
    }

    public void testNewWithoutParentheses_24d() throws Exception {
        checkOccurrences("        $example = new Examp^le()::$protectedStaticField;");
    }

    public void testNewWithoutParentheses_24e() throws Exception {
        checkOccurrences("        $example = new Exampl^e()->privateMethod();");
    }

    public void testNewWithoutParentheses_24f() throws Exception {
        checkOccurrences("        $example = new Ex^ample()::publicStaticMethod();");
    }

    public void testNewWithoutParentheses_24g() throws Exception {
        checkOccurrences("        $example = new Exam^ple()->returnThis()->publicMethod();");
    }

    public void testNewWithoutParentheses_24h() throws Exception {
        checkOccurrences("        $example = new Ex^ample()?->returnThis()::PRIVATE_CONST;");
    }

    public void testNewWithoutParentheses_24i() throws Exception {
        checkOccurrences("        $example = new Exa^mple()->test->publicTestField;");
    }

    public void testNewWithoutParentheses_24j() throws Exception {
        checkOccurrences("        $example = new Exam^ple()->test::$publicStaticTestField;");
    }

    public void testNewWithoutParentheses_24k() throws Exception {
        checkOccurrences("new Exa^mple()::$publicStaticField; // test");
    }

    public void testNewWithoutParentheses_24l() throws Exception {
        checkOccurrences("new Examp^le()->publicMethod(); // test");
    }

    public void testNewWithoutParentheses_24m() throws Exception {
        checkOccurrences("new Examp^le()->returnThis()::IMPLICIT_PUBLIC_CONST; // test");
    }

    public void testNewWithoutParentheses_24n() throws Exception {
        checkOccurrences("new Exa^mple()->returnThis()::publicStaticMethod(); // test");
    }

    public void testNewWithoutParentheses_24o() throws Exception {
        checkOccurrences("new E^xample()->test->publicTestMethod(); // test");
    }

    public void testNewWithoutParentheses_24p() throws Exception {
        checkOccurrences("new Exam^ple()->test::$publicStaticTestField; // test");
    }

    public void testNewWithoutParentheses_24q() throws Exception {
        checkOccurrences("echo new Exa^mple()->test();");
    }

    public void testNewWithoutParentheses_25a() throws Exception {
        checkOccurrences("    public const string PUBL^IC_CONSTANT = 'public constant'; // anon1");
    }

    public void testNewWithoutParentheses_25b() throws Exception {
        checkOccurrences("}::PUBLIC_CONSTA^NT;");
    }

    public void testNewWithoutParentheses_26a() throws Exception {
        checkOccurrences("    public string $publ^icField = 'public field'; // anon2");
    }

    public void testNewWithoutParentheses_26b() throws Exception {
        checkOccurrences("}->publicF^ield;");
    }

    public void testNewWithoutParentheses_27a() throws Exception {
        checkOccurrences("    public static string $publicS^taticField = 'public static field'; // anon3");
    }

    public void testNewWithoutParentheses_27b() throws Exception {
        checkOccurrences("}::$publicStat^icField;");
    }

    public void testNewWithoutParentheses_28a() throws Exception {
        checkOccurrences("    public function public^Method(): void {} // anon4");
    }

    public void testNewWithoutParentheses_28b() throws Exception {
        checkOccurrences("}->public^Method();");
    }

    public void testNewWithoutParentheses_29a() throws Exception {
        checkOccurrences("    public static function publicStaticMetho^d(): void {} // anon5");
    }

    public void testNewWithoutParentheses_29b() throws Exception {
        checkOccurrences("}::pub^licStaticMethod();");
    }

    public void testNewWithoutParentheses_30a() throws Exception {
        checkOccurrences("    public string $publicF^ield = 'public field'; // anon6");
    }

    public void testNewWithoutParentheses_30b() throws Exception {
        checkOccurrences("}::publicStaticMethod()->publicMethod()->public^Field;");
    }

    public void testNewWithoutParentheses_31a() throws Exception {
        checkOccurrences("    public function publi^cMethod(): self {} // anon6");
    }

    public void testNewWithoutParentheses_31b() throws Exception {
        checkOccurrences("}::publicStaticMethod()->publicMet^hod()->publicField;");
    }

    public void testNewWithoutParentheses_32a() throws Exception {
        checkOccurrences("    public static function publicStatic^Method(): self {} // anon6");
    }

    public void testNewWithoutParentheses_32b() throws Exception {
        checkOccurrences("}::publicSta^ticMethod()->publicMethod()->publicField;");
    }

    public void testNewWithoutParentheses_33a() throws Exception {
        checkOccurrences("    public string $public^Field = 'public field'; // anon7");
    }

    public void testNewWithoutParentheses_33b() throws Exception {
        checkOccurrences("}->publicMethod()::publicStaticMethod()->publicF^ield;");
    }

    public void testNewWithoutParentheses_34a() throws Exception {
        checkOccurrences("    public function publicMe^thod(): self {} // anon7");
    }

    public void testNewWithoutParentheses_34b() throws Exception {
        checkOccurrences("}->publicM^ethod()::publicStaticMethod()->publicField;");
    }

    public void testNewWithoutParentheses_35a() throws Exception {
        checkOccurrences("    public static function publicSta^ticMethod(): self {} // anon7");
    }

    public void testNewWithoutParentheses_35b() throws Exception {
        checkOccurrences("}->publicMethod()::publicStat^icMethod()->publicField;");
    }

    public void testNewWithoutParentheses_36a() throws Exception {
        checkOccurrences("    public const string PUBLIC_CO^NSTANT = 'public constant'; // anon8");
    }

    public void testNewWithoutParentheses_36b() throws Exception {
        checkOccurrences("}->publicMethod()::publicStaticMethod()::PUBLIC_C^ONSTANT;");
    }

    public void testNewWithoutParentheses_37a() throws Exception {
        checkOccurrences("    public function public^Method(): self {} // anon8");
    }

    public void testNewWithoutParentheses_37b() throws Exception {
        checkOccurrences("}->publicMe^thod()::publicStaticMethod()::PUBLIC_CONSTANT;");
    }

    public void testNewWithoutParentheses_38a() throws Exception {
        checkOccurrences("    public static function publicStati^cMethod(): self {} // anon8");
    }

    public void testNewWithoutParentheses_38b() throws Exception {
        checkOccurrences("}->publicMethod()::publicStatic^Method()::PUBLIC_CONSTANT;");
    }
}
