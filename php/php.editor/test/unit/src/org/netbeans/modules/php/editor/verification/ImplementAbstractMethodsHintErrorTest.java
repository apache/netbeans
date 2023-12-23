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
        checkHints("testImplementAbstractMethodsHint.php", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_01() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Extendin^gClass1", "Implement", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_02() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Extendin^gClass2", "Implement", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_03() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Extendin^gClass3", "Implement", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_04() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Extendin^gClass4", "Implement", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_05() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Extendin^gClass5", "Implement", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_06() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Implementi^ngClass", "Implement", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_07() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Extendin^gClass1", "Declare", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix_08() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix.php", "Implementi^ngClass", "Declare", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix02_01() throws Exception {
        applyHint("testImplementAbstractMethodsHintFix02.php", "$a = new cl^ass implements Iface {", "Implement", PhpVersion.PHP_82);
    }

    public void testImplementAbstractMethodsHintFix02_02() throws Exception {
        checkHints("testImplementAbstractMethodsHintFix02.php", PhpVersion.PHP_82);
    }

    public void testIssue257898() throws Exception {
        checkHints("testIssue257898.php", PhpVersion.PHP_82);
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
        checkHints("testUnionTypesImplementMethod01.php", PhpVersion.PHP_82);
    }

    public void testUnionTypes_02() throws Exception {
        checkHints("testUnionTypesImplementMethod02.php", PhpVersion.PHP_82);
    }

    public void testUnionTypes_03() throws Exception {
        checkHints("testUnionTypesImplementMethod03.php", PhpVersion.PHP_82);
    }

    public void testUnionTypes_04() throws Exception {
        checkHints("testUnionTypesImplementMethod04.php", PhpVersion.PHP_82);
    }

    public void testStaticReturnType_01() throws Exception {
        checkHints("testStaticReturnTypeImplementMethod01.php", PhpVersion.PHP_82);
    }

    public void testMixedType_01() throws Exception {
        checkHints("testMixedTypeImplementMethod01.php", PhpVersion.PHP_82);
    }

    public void testUnionTypesFix_01() throws Exception {
        applyHint("testUnionTypesImplementMethod01.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testUnionTypesFix_02() throws Exception {
        applyHint("testUnionTypesImplementMethod02.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testUnionTypesFix_03() throws Exception {
        applyHint("testUnionTypesImplementMethod03.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testUnionTypesFix_04() throws Exception {
        applyHint("testUnionTypesImplementMethod04.php", "class Impleme^nt extends ImplementMethodTest {", "Implement", PhpVersion.PHP_82);
    }

    public void testUnionTypesWithSpecialTypesFix_01() throws Exception {
        applyHint("testUnionTypesImplementMethodSpecialTypes01.php", "class Gr^andchild extends Child {", "Implement", PhpVersion.PHP_82);
    }

    public void testUnionTypesWithSpecialTypesFix_02() throws Exception {
        applyHint("testUnionTypesImplementMethodSpecialTypes02.php", "class Gr^andchild extends Child {", "Implement", PhpVersion.PHP_82);
    }

    public void testUnionTypesWithSpecialTypesFix_03() throws Exception {
        applyHint("testUnionTypesImplementMethodSpecialTypes03.php", "class Gr^andchild extends Child {", "Implement", PhpVersion.PHP_82);
    }

    public void testUnionTypesWithSpecialTypesFix_04() throws Exception {
        applyHint("testUnionTypesImplementMethodSpecialTypes04.php", "class Gr^andchild extends Child {", "Implement", PhpVersion.PHP_82);
    }

    public void testStaticReturnTypeFix_01() throws Exception {
        applyHint("testStaticReturnTypeImplementMethod01.php", "class TestC^lass implements TestInterface {", "Implement", PhpVersion.PHP_82);
    }

    public void testMixedTypeFix_01() throws Exception {
        applyHint("testMixedTypeImplementMethod01.php", "class Chil^d implements MixedType {", "Implement", PhpVersion.PHP_82);
    }

    public void testNetbeans5370() throws Exception {
        checkHints("testNetbeans5370.php", PhpVersion.PHP_82);
    }

    public void testNetbeans5370Fix_01() throws Exception {
        applyHint("testNetbeans5370.php", "class Test^Class1 extends TestAbstractClass", "Implement", PhpVersion.PHP_82);
    }

    public void testNetbeans5370Fix_02() throws Exception {
        applyHint("testNetbeans5370.php", "class TestCl^ass2 implements TestInterface", "Implement", PhpVersion.PHP_82);
    }

    public void testIntersectionTypes_01() throws Exception {
        checkHints("testIntersectionTypesImplementMethod01.php", PhpVersion.PHP_82);
    }

    public void testIntersectionTypes_02() throws Exception {
        checkHints("testIntersectionTypesImplementMethod02.php", PhpVersion.PHP_82);
    }

    public void testIntersectionTypes_03() throws Exception {
        checkHints("testIntersectionTypesImplementMethod03.php", PhpVersion.PHP_82);
    }

    public void testIntersectionTypes_04() throws Exception {
        checkHints("testIntersectionTypesImplementMethod04.php", PhpVersion.PHP_82);
    }

    public void testIntersectionTypesFix_01() throws Exception {
        applyHint("testIntersectionTypesImplementMethod01.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testIntersectionTypesFix_02() throws Exception {
        applyHint("testIntersectionTypesImplementMethod02.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testIntersectionTypesFix_03() throws Exception {
        applyHint("testIntersectionTypesImplementMethod03.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testIntersectionTypesFix_04() throws Exception {
        applyHint("testIntersectionTypesImplementMethod04.php", "class Impleme^nt extends ImplementMethodTest {", "Implement", PhpVersion.PHP_82);
    }

    public void testEnumerations_01() throws Exception {
        checkHints("testEnumerations_01.php", PhpVersion.PHP_82);
    }

    public void testEnumerationssFix_01() throws Exception {
        applyHint("testEnumerations_01.php", "enum Test^Enum implements TestEnumInterface {", "Implement", PhpVersion.PHP_82);
    }

    public void testEnumerationssFix_02() throws Exception {
        applyHint("testEnumerations_01.php", "enum TestEnumWith^Trait {", "Implement", PhpVersion.PHP_82);
    }

    public void testDNFTypes_01() throws Exception {
        checkHints("testDNFTypesImplementMethod01.php", PhpVersion.PHP_82);
    }

    public void testDNFTypes_02() throws Exception {
        checkHints("testDNFTypesImplementMethod02.php", PhpVersion.PHP_82);
    }

    public void testDNFTypes_03() throws Exception {
        checkHints("testDNFTypesImplementMethod03.php", PhpVersion.PHP_82);
    }

    public void testDNFTypes_04() throws Exception {
        checkHints("testDNFTypesImplementMethod04.php", PhpVersion.PHP_82);
    }

    public void testDNFTypesFix_01() throws Exception {
        applyHint("testDNFTypesImplementMethod01.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testDNFTypesFix_02() throws Exception {
        applyHint("testDNFTypesImplementMethod02.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testDNFTypesFix_03() throws Exception {
        applyHint("testDNFTypesImplementMethod03.php", "class Impleme^nt", "Implement", PhpVersion.PHP_82);
    }

    public void testDNFTypesFix_04() throws Exception {
        applyHint("testDNFTypesImplementMethod04.php", "class Impleme^nt extends ImplementMethodTest {", "Implement", PhpVersion.PHP_82);
    }

    public void testOverrideAttribute_01() throws Exception {
        checkHints("testOverrideAttribute01.php", PhpVersion.PHP_83);
    }

    public void testOverrideAttribute_Fix01() throws Exception {
        applyHint("testOverrideAttribute01.php", "class Impleme^nt extends AbstractClass implements TestInterface {", "Implement All Abstract Methods", PhpVersion.PHP_83);
    }

    public void testOverrideAttribute_PHP82_01() throws Exception {
        checkHints("testOverrideAttribute01.php", PhpVersion.PHP_82);
    }

    public void testOverrideAttribute_PHP82_Fix01() throws Exception {
        applyHint("testOverrideAttribute01.php", "class Impleme^nt extends AbstractClass implements TestInterface {", "Implement All Abstract Methods", PhpVersion.PHP_82);
    }

    public void testOverrideAttributeAnonClass_01() throws Exception {
        checkHints("testOverrideAttributeAnonClass.php", PhpVersion.PHP_83);
    }

    public void testOverrideAttributeAnonClass_Fix01() throws Exception {
        applyHint("testOverrideAttributeAnonClass.php", "$anon = new cla^ss () extends AbstractClass implements TestInterface {", "Implement All Abstract Methods", PhpVersion.PHP_83);
    }

    public void testOverrideAttributeEnum_01() throws Exception {
        checkHints("testOverrideAttributeEnum.php", PhpVersion.PHP_83);
    }

    public void testOverrideAttributeEnum_Fix01() throws Exception {
        applyHint("testOverrideAttributeEnum.php", "enum Enum^1 implements TestInterface {", "Implement All Abstract Methods", PhpVersion.PHP_83);
    }

    private void checkHints(String fileName, PhpVersion phpVersion) throws Exception {
        checkHints(new ImplementAbstractMethodsHintErrorStub(phpVersion), fileName);
    }

    private void applyHint(String fileName, String caretLine, String fixDesc, PhpVersion phpVersion) throws Exception {
        applyHint(new ImplementAbstractMethodsHintErrorStub(phpVersion), fileName, caretLine, fixDesc);
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
