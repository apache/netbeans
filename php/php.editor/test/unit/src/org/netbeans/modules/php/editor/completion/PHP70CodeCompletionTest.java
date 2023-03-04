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
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHP70CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP70CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php70/base/"))
            })
        );
    }

    public void testIntTypeHint01() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function intTypeHint(^int $arg) {", false);
    }

    public void testIntTypeHint02() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function intTypeHint(in^t $arg) {", false);
    }

    public void testIntTypeHint03() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(^int $arg) {", false);
    }

    public void testIntTypeHint04() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(in^t $arg) {", false);
    }

    public void testFloatTypeHint01() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function floatTypeHint(^float $arg) {", false);
    }

    public void testFloatTypeHint02() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function floatTypeHint(flo^at $arg) {", false);
    }

    public void testFloatTypeHint03() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(^float $arg) {", false);
    }

    public void testFloatTypeHint04() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(flo^at $arg) {", false);
    }

    public void testStringTypeHint01() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function stringTypeHint(^string $arg) {", false);
    }

    public void testStringTypeHint02() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function stringTypeHint(strin^g $arg) {", false);
    }

    public void testStringTypeHint03() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(^string $arg) {", false);
    }

    public void testStringTypeHint04() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(strin^g $arg) {", false);
    }

    public void testBoolTypeHint01() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function boolTypeHint(^bool $arg) {", false);
    }

    public void testBoolTypeHint02() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function boolTypeHint(boo^l $arg) {", false);
    }

    public void testBoolTypeHint03() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(^bool $arg) {", false);
    }

    public void testBoolTypeHint04() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/scalarTypeHints.php", "function __construct(boo^l $arg) {", false);
    }

    public void testReturnType01() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function current(): ^Comment;", false);
    }

    public void testReturnType02() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function current(): Comm^ent;", false);
    }

    public void testReturnType03() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function &my_array_sort(array &$data): ^array {", false);
    }

    public void testReturnType04() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function &my_array_sort(array &$data): arr^ay {", false);
    }

    public void testReturnType05() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function foo(): ^Comment {", false);
    }

    public void testReturnType06() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function foo(): Comm^ent {", false);
    }

    public void testReturnType07() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function &bar(): ^\\My\\Firm\\Comment {", false);
    }

    public void testReturnType08() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function &bar(): \\^My\\Firm\\Comment {", false);
    }

    public void testReturnType09() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function &bar(): \\My\\Fi^rm\\Comment {", false);
    }

    public void testReturnType10() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function &bar(): \\My\\Firm\\^Comment {", false);
    }

    public void testReturnType11() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function &bar(): \\My\\Firm\\Comm^ent {", false);
    }

    public void testReturnType12() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function baz($data): ^Comment {", false);
    }

    public void testReturnType13() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function bazz(...$data): ^Comment {", false);
    }

    public void testReturnType14() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypes.php", "function bazz(&...$data): ^Comment {", false);
    }

    public void testReturnTypesTyping01() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping01.php", "function mytest():^", false);
    }

    public void testReturnTypesTyping02() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping02.php", "function mytest(): ^", false);
    }

    public void testReturnTypesTyping03() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping03.php", "function mytest():^{", false);
    }

    public void testReturnTypesTyping04a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping04.php", "function mytest():^ {", false);
    }

    public void testReturnTypesTyping04b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping04.php", "function mytest(): ^{", false);
    }

    public void testReturnTypesTyping05() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping05.php", "    function current():^", false);
    }

    public void testReturnTypesTyping06() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping06.php", "    function current(): ^", false);
    }

    public void testReturnTypesTyping07() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping07.php", "    function current():^;", false);
    }

    public void testReturnTypesTyping08a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping08.php", "    function current():^ ;", false);
    }

    public void testReturnTypesTyping08b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping08.php", "    function current(): ^;", false);
    }

    public void testReturnTypesTyping09() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping09.php", "    function current():^", false);
    }

    public void testReturnTypesTyping10() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping10.php", "    function current(): ^", false);
    }

    public void testReturnTypesTyping11() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping11.php", "    function current():^{", false);
    }

    public void testReturnTypesTyping12a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping12.php", "    function current():^ {", false);
    }

    public void testReturnTypesTyping12b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/returnTypesTyping12.php", "    function current(): ^{", false);
    }

    public void testGroupUse01a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse01.php", "use A\\{^", false);
    }

    public void testGroupUse01b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse01.php", "    ClsA,^", false);
    }

    public void testGroupUse01c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse01.php", "    B\\Cls^AB,", false);
    }

    public void testGroupUse01d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse01.php", "    B\\^ClsAB,", false);
    }

    public void testGroupUse02a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse02.php", "use \\A\\{^", false);
    }

    public void testGroupUse02b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse02.php", "    ClsA,^", false);
    }

    public void testGroupUse02c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse02.php", "    B\\Cls^AB,", false);
    }

    public void testGroupUse02d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse02.php", "    B\\^ClsAB,", false);
    }

    public void testGroupUse03a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse03.php", "use A\\ {^", false);
    }

    public void testGroupUse03b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse03.php", "    ClsA,^", false);
    }

    public void testGroupUse03c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse03.php", "    B\\Cls^AB,", false);
    }

    public void testGroupUse03d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse03.php", "    B\\^ClsAB,", false);
    }

    public void testGroupUse04a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUse04.php", "use A\\ {^", false);
    }

    public void testGroupUseConst01a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseConst01.php", "use const A\\ {^", false);
    }

    public void testGroupUseConst01b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseConst01.php", "    B\\^C_B,", false);
    }

    public void testGroupUseConst01c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseConst01.php", "    B\\C\\C_^C", false);
    }

    public void testGroupUseConst02a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseConst02.php", "use const A\\{^", false);
    }

    public void testGroupUseConst03a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseConst03.php", "use const \\A\\{^", false);
    }

    public void testGroupUseFunction01a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseFunction01.php", "use function A\\ {^", false);
    }

    public void testGroupUseFunction01b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseFunction01.php", "    B\\^fb,", false);
    }

    public void testGroupUseFunction01c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseFunction01.php", "    B\\C\\f^c", false);
    }

    public void testGroupUseFunction02a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseFunction02.php", "use function A\\{^", false);
    }

    public void testGroupUseFunction03a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseFunction03.php", "use function \\A\\{^", false);
    }

    public void testGroupUseMixed01a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "use A\\{^", false);
    }

    public void testGroupUseMixed01b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "    cons^t CONSTANTA,", false);
    }

    public void testGroupUseMixed01c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "    const ^CONSTANTA,", false);
    }

    public void testGroupUseMixed01d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "    const CON^STANTA,", false);
    }

    public void testGroupUseMixed01e() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "    function ^testA,", false);
    }

    public void testGroupUseMixed01f() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "    function te^stA,", false);
    }

    public void testGroupUseMixed01g() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "    function ^ testA AS mytestA,", false);
    }

    public void testGroupUseMixed01h() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "    ^MyAA", false);
    }

    public void testGroupUseMixed01i() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "use function A\\{^", false);
    }

    public void testGroupUseMixed01j() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "new My^A();", false);
    }

    // XXX
    public void testGroupUseMixed01k() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "echo CONST^ANTA; // CONSTANTA", false);
    }

    public void testGroupUseMixed01l() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "test^A(); // testA", false);
    }

    public void testGroupUseMixed01m() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "myte^stA(); // testA", false);
    }

    public void testGroupUseMixed01n() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/groupUseMixed01.php", "new MyA^A();", false);
    }

    public void testAnonymousClass01a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "$an^on->testAnon();", false);
    }

    public void testAnonymousClass01b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "$anon->^testAnon();", false);
    }

    public void testAnonymousClass01c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "$anon = new class ^{", false);
    }

    public void testAnonymousClass01d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "    p^ublic function testAnon() {", false);
    }

    public void testAnonymousClass01e() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "    public f^unction testAnon() {", false);
    }

    public void testAnonymousClass01f() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "        $thi^s->testBnon();", false);
    }

    public void testAnonymousClass01g() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "        $this->test^Bnon();", false);
    }

    public void testAnonymousClass01h() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass01.php", "^// magic methods", false);
    }

    public void testAnonymousClass02a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass02.php", "$anon = new class(^$int, foo()) {", false);
    }

    public void testAnonymousClass02b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass02.php", "$anon = new class($^int, foo()) {", false);
    }

    public void testAnonymousClass02c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass02.php", "$anon = new class($int, foo()) ^{", false);
    }

    public void testAnonymousClass02d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass02.php", "$anon = new class($int, ^foo()) {", false);
    }

    public void testAnonymousClass02e() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass02.php", "$anon = new class($int, foo(^)) {", false);
    }

    public void testAnonymousClass02f() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass02.php", "        $this->pr^op1 = $number;", false);
    }

    public void testAnonymousClass02g() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass02.php", "        $this->prop1 = $num^ber;", false);
    }

    public void testAnonymousClass03a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass03.php", "        return new class($^this->prop) extends Outer {", false);
    }

    public void testAnonymousClass03b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass03.php", "        return new class($this->^prop) extends Outer {", false);
    }

    public void testAnonymousClass03c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass03.php", "                return $this->^prop2 + $this->prop3 + $this->func1();", false);
    }

    public void testAnonymousClass03d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/anonymousClass03.php", "                return $t^his->prop2 + $this->prop3 + $this->func1();", false);
    }

    public void testUniformVariableSyntax01() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax01.php", "fnc()->getNumbers()::^MAX;", false);
    }

    // XXX did not work properly even before php7
//    public void testUniformVariableSyntax02() throws Exception {
//        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax02.php", "fnc()->getNumbers()[0]::^MAX;", false);
//    }

    public void testUniformVariableSyntax03a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax03.php", "UVS3::^myStatic3()::myStatic2()::myStatic1()::MAX;", false);
    }

    public void testUniformVariableSyntax03b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax03.php", "UVS3::myStatic3()::^myStatic2()::myStatic1()::MAX;", false);
    }

    public void testUniformVariableSyntax03c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax03.php", "UVS3::myStatic3()::myStatic2()::^myStatic1()::MAX;", false);
    }

    public void testUniformVariableSyntax03d() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax03.php", "UVS3::myStatic3()::myStatic2()::myStatic1()::^MAX;", false);
    }

    public void testUniformVariableSyntax03e() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax03.php", "UVS3::myStatic3()::myStatic2()::$IN^STANCE::myStatic1();", false);
    }

    public void testUniformVariableSyntax03f() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/uniformVariableSyntax03.php", "UVS3::myStatic3()::myStatic2()::$INSTANCE::mySta^tic1();", false);
    }

    public void testIssue259074a() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/issue259074.php", "    C^2", false);
    }

    public void testIssue259074b() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/issue259074.php", "    C^ONST_2", false);
    }

    public void testIssue259074c() throws Exception {
        checkCompletion("testfiles/completion/lib/php70/base/issue259074.php", "    test^2", false);
    }

    // XXX add tests for CC after NS aliases!

}
