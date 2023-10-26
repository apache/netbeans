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

public class ImplementAbstractMethodsHintErrorTest extends PHPHintsTestBase {

    public ImplementAbstractMethodsHintErrorTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "ImplementAbstractMethodsHintError/";
    }

    public void testImplementAbstractMethodsHint() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHint.php");
    }

    public void testImplementAbstractMethodsHintFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Extendin^gClass1", "Implement");
    }

    public void testImplementAbstractMethodsHintFix_02() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Extendin^gClass2", "Implement");
    }

    public void testImplementAbstractMethodsHintFix_03() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Extendin^gClass3", "Implement");
    }

    public void testImplementAbstractMethodsHintFix_04() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Extendin^gClass4", "Implement");
    }

    public void testImplementAbstractMethodsHintFix_05() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Extendin^gClass5", "Implement");
    }

    public void testImplementAbstractMethodsHintFix_06() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Implementi^ngClass", "Implement");
    }

    public void testImplementAbstractMethodsHintFix_07() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Extendin^gClass1", "Declare");
    }

    public void testImplementAbstractMethodsHintFix_08() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix.php", "Implementi^ngClass", "Declare");
    }

    public void testImplementAbstractMethodsHintFix02_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix02.php", "$a = new cl^ass implements Iface {", "Implement");
    }

    public void testImplementAbstractMethodsHintFix02_02() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHintFix02.php");
    }

    public void testIssue257898() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testIssue257898.php");
    }

    public void testIssue262838Fix01a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_70), "testIssue262838Fix01.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue262838Fix01b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_55), "testIssue262838Fix01.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue262838Fix02a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_70), "testIssue262838Fix02.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue262838Fix02b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_55), "testIssue262838Fix02.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue262838Fix03a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_70), "testIssue262838Fix03.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue262838Fix03b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_55), "testIssue262838Fix03.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue267563Fix01a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_70), "testIssue267563Fix01.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue267563Fix01b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_55), "testIssue267563Fix01.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue267563Fix02a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_70), "testIssue267563Fix02.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue267563Fix02b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_55), "testIssue267563Fix02.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix01a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_71), "testIssue270237Fix01.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix01b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_56), "testIssue270237Fix01.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix02a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_71), "testIssue270237Fix02.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix02b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_56), "testIssue270237Fix02.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix03a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_71), "testIssue270237Fix03.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix03b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_56), "testIssue270237Fix03.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix04a() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_71), "testIssue270237Fix04.php", "class Fo^o implements FooInterface", "Implement");
    }

    public void testIssue270237Fix04b() throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(PhpVersion.PHP_56), "testIssue270237Fix04.php", "class Fo^o implements FooInterface", "Implement");
    }

    // NETBEANS-4443 PHP 8.0
    public void testUnionTypes_01() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod01.php");
    }

    public void testUnionTypes_02() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod02.php");
    }

    public void testUnionTypes_03() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod03.php");
    }

    public void testUnionTypes_04() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod04.php");
    }

    public void testStaticReturnType_01() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testStaticReturnTypeImplementMethod01.php");
    }

    public void testMixedType_01() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testMixedTypeImplementMethod01.php");
    }

    public void testUnionTypesFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod01.php", "class Impleme^nt", "Implement");
    }

    public void testUnionTypesFix_02() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod02.php", "class Impleme^nt", "Implement");
    }

    public void testUnionTypesFix_03() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod03.php", "class Impleme^nt", "Implement");
    }

    public void testUnionTypesFix_04() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethod04.php", "class Impleme^nt extends ImplementMethodTest {", "Implement");
    }

    public void testUnionTypesWithSpecialTypesFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethodSpecialTypes01.php", "class Gr^andchild extends Child {", "Implement");
    }

    public void testUnionTypesWithSpecialTypesFix_02() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethodSpecialTypes02.php", "class Gr^andchild extends Child {", "Implement");
    }

    public void testUnionTypesWithSpecialTypesFix_03() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethodSpecialTypes03.php", "class Gr^andchild extends Child {", "Implement");
    }

    public void testUnionTypesWithSpecialTypesFix_04() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testUnionTypesImplementMethodSpecialTypes04.php", "class Gr^andchild extends Child {", "Implement");
    }

    public void testStaticReturnTypeFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testStaticReturnTypeImplementMethod01.php", "class TestC^lass implements TestInterface {", "Implement");
    }

    public void testMixedTypeFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testMixedTypeImplementMethod01.php", "class Chil^d implements MixedType {", "Implement");
    }

    public void testNetbeans5370() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testNetbeans5370.php");
    }

    public void testNetbeans5370Fix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testNetbeans5370.php", "class Test^Class1 extends TestAbstractClass", "Implement");
    }

    public void testNetbeans5370Fix_02() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testNetbeans5370.php", "class TestCl^ass2 implements TestInterface", "Implement");
    }

    public void testIntersectionTypes_01() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod01.php");
    }

    public void testIntersectionTypes_02() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod02.php");
    }

    public void testIntersectionTypes_03() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod03.php");
    }

    public void testIntersectionTypes_04() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod04.php");
    }

    public void testIntersectionTypesFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod01.php", "class Impleme^nt", "Implement");
    }

    public void testIntersectionTypesFix_02() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod02.php", "class Impleme^nt", "Implement");
    }

    public void testIntersectionTypesFix_03() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod03.php", "class Impleme^nt", "Implement");
    }

    public void testIntersectionTypesFix_04() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testIntersectionTypesImplementMethod04.php", "class Impleme^nt extends ImplementMethodTest {", "Implement");
    }

    public void testEnumerations_01() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testEnumerations_01.php");
    }

    public void testEnumerationssFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testEnumerations_01.php", "enum Test^Enum implements TestEnumInterface {", "Implement");
    }

    public void testEnumerationssFix_02() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testEnumerations_01.php", "enum TestEnumWith^Trait {", "Implement");
    }

    public void testDNFTypes_01() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod01.php");
    }

    public void testDNFTypes_02() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod02.php");
    }

    public void testDNFTypes_03() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod03.php");
    }

    public void testDNFTypes_04() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod04.php");
    }

    public void testDNFTypesFix_01() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod01.php", "class Impleme^nt", "Implement");
    }

    public void testDNFTypesFix_02() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod02.php", "class Impleme^nt", "Implement");
    }

    public void testDNFTypesFix_03() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod03.php", "class Impleme^nt", "Implement");
    }

    public void testDNFTypesFix_04() throws Exception {
        applyHint(new ImplementAbstractMethodsHintError(), "testDNFTypesImplementMethod04.php", "class Impleme^nt extends ImplementMethodTest {", "Implement");
    }

    //~ Inner classes
    private static final class ImplementAbstractMethodsHintErrorStub extends ImplementAbstractMethodsHintError {

        private final PhpVersion phpVersion;

        ImplementAbstractMethodsHintErrorStub(PhpVersion phpVersion) {
            assert phpVersion != null;
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject file) {
            return phpVersion;
        }

    }

}
