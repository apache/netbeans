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

public class InitializeFieldsSuggestionTest extends PHPHintsTestBase {

    public InitializeFieldsSuggestionTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "InitializeFieldsSuggestion/";
    }

    public void testInitializeFieldSuggestion_01() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_01.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testInitializeFieldSuggestion_02() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_02.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testInitializeFieldSuggestion_03() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_03.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testInitializeFieldSuggestion_04() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_04.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testInitializeFieldSuggestion_05() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_05.php", "    function __construct(\\Bar\\Baz $^foo) {");
    }

    public void testInitializeFieldSuggestion_06() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_06.php", "    function __construct(\\Bar\\Baz $^foo) {");
    }

    public void testInitializeFieldSuggestion_07() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_07.php", "    function __construct(\\Bar\\Baz $^foo) {");
    }

    public void testInitializeFieldSuggestion_08() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_08.php", "    function __construct(\\Bar\\Baz $^foo) {");
    }

    public void testIssue229522() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue229522.php", "function __construct($par^am) {");
    }

    public void testIssue239640() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue239640.php", "public function __construct(array $get = array(), array $post = array()^);");
    }

    public void testIssue239640_01() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue239640.php", "public function __construct(array $get = array(), array $post2 = array()^);");
    }

    public void testIssue248213() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue248213.php", "function __construct(&...$f^oo) {");
    }

    public void testIssue249306() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue249306.php", "function __construct(...$f^oo) {");
    }

    public void testIssue270368_01() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue270368_01.php", "function __construct(?string $tes^t) {");
    }

    public void testIssue270368_02() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue270368_02.php", "function __construct(?\\Foo\\Bar $^test) {");
    }

    // NETBEANS-4443 PHP 8.0
    public void testUnionTypes_01a() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "unionTypes_01.php", "            int|float $n^umber,");
    }

    public void testUnionTypes_01b() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "unionTypes_01.php", "            ?Foo $nullabl^e,");
    }

    public void testUnionTypes_01c() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "unionTypes_01.php", "            Bar $b^ar,");
    }

    public void testUnionTypes_01d() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "unionTypes_01.php", "            \\Test\\Foo|Bar $mix^ed,");
    }

    public void testConstructorPropertyPromotion_01_Class() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_01.php", "private int $^x, // class");
    }

    public void testConstructorPropertyPromotion_01_Trait() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_01.php", "public int $^x, // trait");
    }

    public void testConstructorPropertyPromotion_01_AnonClass() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_01.php", "private int|string $p^aram, // anon class");
    }

    public void testConstructorPropertyPromotion_02_Class() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_02.php", "private ?int $^x = null, // class");
    }

    public void testConstructorPropertyPromotion_02_Trait() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_02.php", "private ?int $^x = null, // trait");
    }

    public void testConstructorPropertyPromotion_02_AnonClass() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_02.php", "private int|string $p^aram = \"default value\", // anon class");
    }

    public void testConstructorPropertyPromotion_03_Class() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_03.php", "$^y, // class");
    }

    public void testConstructorPropertyPromotion_03_Trait() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_03.php", "$^y, // trait");
    }

    public void testConstructorPropertyPromotion_03_AnonClass() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "constructorPropertyPromotion_03.php", "$^y, // anon class");
    }

    // Fix
    public void testIssue270368Fix_01() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_73), "testIssue270368_01.php", "function __construct(?string $tes^t) {", "Initialize Field");
    }

    public void testIssue270368Fix_01_php74() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_74), "testIssue270368_01.php", "function __construct(?string $tes^t) {", "Initialize Field");
    }

    public void testIssue270368Fix_02() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_73), "testIssue270368_02.php", "function __construct(?\\Foo\\Bar $^test) {", "Initialize Field");
    }

    public void testIssue270368Fix_02_php74() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_74), "testIssue270368_02.php", "function __construct(?\\Foo\\Bar $^test) {", "Initialize Field");
    }

    public void testIssue270368Fix_03() throws Exception {
        applyHint(new InitializeFieldSuggestion(), "testIssue270368_03.php", "function __construct($^test) {", "Initialize Field");
    }

    public void testIssue270368Fix_04() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_73), "testIssue270368_04.php", "function __construct(\\Foo\\Bar $^test) {", "Initialize Field");
    }

    public void testIssue270368Fix_04_php74() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_74), "testIssue270368_04.php", "function __construct(\\Foo\\Bar $^test) {", "Initialize Field");
    }

    // NETBEANS-4443 PHP 8.0
    public void testUnionTypesFix_01a_php80() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_80), "unionTypes_01.php", "            int|float $n^umber,", "Initialize Field");
    }

    public void testUnionTypesFix_01a_php73() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_73), "unionTypes_01.php", "            int|float $n^umber,", "Initialize Field");
    }

    public void testUnionTypesFix_01b_php80() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_80), "unionTypes_01.php", "            ?Foo $nullabl^e,", "Initialize Field");
    }

    public void testUnionTypesFix_01b_php73() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_73), "unionTypes_01.php", "            ?Foo $nullabl^e,", "Initialize Field");
    }

    public void testUnionTypesFix_01c_php80() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_80), "unionTypes_01.php", "            Bar $^bar,", "Initialize Field");
    }

    public void testUnionTypesFix_01c_php73() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_73), "unionTypes_01.php", "            Bar $^bar,", "Initialize Field");
    }

    public void testUnionTypesFix_01d_php80() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_80), "unionTypes_01.php", "            \\Test\\Foo|Bar $mi^xed,", "Initialize Field");
    }

    public void testUnionTypesFix_01d_php73() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_73), "unionTypes_01.php", "            \\Test\\Foo|Bar $mi^xed,", "Initialize Field");
    }

    public void testIntersectionTypes_01a() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "intersectionTypes_01.php", "            Foo&Bar $para^m1,");
    }

    public void testIntersectionTypes_01b() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "intersectionTypes_01.php", "            \\Test\\Foo&Bar $par^am2,");
    }

    public void testIntersectionTypesFix_01a() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_81), "intersectionTypes_01.php", "            Foo&Bar $par^am1,", "Initialize Field");
    }

    public void testIntersectionTypesFix_01b() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_81), "intersectionTypes_01.php", "            \\Test\\Foo&Bar $par^am2,", "Initialize Field");
    }

    public void testDnfTypes_01a() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "dnfTypes_01.php", "            (Foo&Bar)|Baz $para^m1,");
    }

    public void testDnfTypes_01b() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "dnfTypes_01.php", "            (\\Test\\Foo&Bar)|(Foo&Baz) $par^am2,");
    }

    public void testDnfTypes_01c() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "dnfTypes_01.php", "            Foo|(Foo&Baz) $para^m3,");
    }

    public void testDnfTypes_01d() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "dnfTypes_01.php", "            Foo|(Foo&Baz)|null $para^m4,");
    }

    public void testDnfTypesFix_01a() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_82), "dnfTypes_01.php", "            (Foo&Bar)|Baz $par^am1,", "Initialize Field");
    }

    public void testDnfTypesFix_01b() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_82), "dnfTypes_01.php", "            (\\Test\\Foo&Bar)|(Foo&Baz) $pa^ram2,", "Initialize Field");
    }

    public void testDnfTypesFix_01c() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_82), "dnfTypes_01.php", "            Foo|(Foo&Baz) $para^m3,", "Initialize Field");
    }

    public void testDnfTypesFix_01d() throws Exception {
        applyHint(new InitializeFieldSuggestionStub(PhpVersion.PHP_82), "dnfTypes_01.php", "            Foo|(Foo&Baz)|null $para^m4,", "Initialize Field");
    }

    //~ Inner classes
    private static class InitializeFieldSuggestionStub extends InitializeFieldSuggestion {

        private final PhpVersion phpVersion;

        public InitializeFieldSuggestionStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject fileObject) {
            return phpVersion;
        }
    }
}
