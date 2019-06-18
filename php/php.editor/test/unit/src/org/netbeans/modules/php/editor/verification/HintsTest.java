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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class HintsTest extends PHPHintsTestBase {

    public HintsTest(String testName) {
        super(testName);
    }

    public void testModifiersCheckHint() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testModifiersCheckHint.php");
    }

    public void testAbstractClassInstantiationHint() throws Exception {
        checkHints(new AbstractClassInstantiationHintError(), "testAbstractClassInstantiationHint.php");
    }

    public void testAbstractClassInstantiationHint_02() throws Exception {
        checkHints(new AbstractClassInstantiationHintError(), "testAbstractClassInstantiationHint_02.php");
    }

    public void testImplementAbstractMethodsHint() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHint.php");
    }

    public void testIssue257898() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testIssue257898.php");
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

    public void testMethodRedeclarationHint() throws Exception {
        checkHints(new MethodRedeclarationHintError(), "testMethodRedeclarationHint.php");
    }

    public void testTypeRedeclarationHint() throws Exception {
        checkHints(new TypeRedeclarationHintError(), "testTypeRedeclarationHint.php");
    }

    public void testFieldRedeclarationHint() throws Exception {
        checkHints(new FieldRedeclarationHintError(), "testFieldRedeclarationHint.php");
    }

    public void testConstantRedeclarationHint() throws Exception {
        checkHints(new ConstantRedeclarationHintError(), "testConstantRedeclarationHint.php");
    }

    public void testWrongOrderOfArgsHint() throws Exception {
        checkHints(new WrongOrderOfArgsHint(), "testWrongOrderOfArgsHint.php");
    }

    public void testUnusedUsesHint() throws Exception {
        checkHints(new UnusedUsesHint(), "testUnusedUsesHint.php");
    }

    public void testUnusedUsesHintWithTypedProperties() throws Exception {
        // PHP 7.4
        checkHints(new UnusedUsesHint(), "testUnusedUsesHintWithTypedProperties.php");
    }

    public void testAmbiguousComparisonHint() throws Exception {
        checkHints(new AmbiguousComparisonHint(), "testAmbiguousComparisonHint.php");
    }

    public void testVarDocSuggestion() throws Exception {
        checkHints(new VarDocSuggestion(), "testVarDocSuggestion.php", "$foo^Bar;");
    }

    public void testAssignVariableSuggestion() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "myFnc();^");
    }

    public void testAssignVariableSuggestion_02() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "die('message');^");
    }

    public void testAssignVariableSuggestion_03() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "exit('message');^");
    }

    public void testAssignVariableSuggestion_04() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "new class {};^");
    }

    public void testIdenticalComparisonSuggestion() throws Exception {
        checkHints(new IdenticalComparisonSuggestion(), "testIdenticalComparisonSuggestion.php", "if ($a == true)^ {}");
    }

    public void testIntroduceSuggestion_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "new MyClass();^");
    }

    public void testIntroduceSuggestion_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->bar;^");
    }

    public void testIntroduceSuggestion_03() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->method();^");
    }

    public void testIntroduceSuggestion_04() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::CON;^");
    }

    public void testIntroduceSuggestion_05() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::stMeth();^");
    }

    public void testIntroduceSuggestion_06() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::$stFld;^");
    }

    public void testIntroduceSuggestion_07() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "new class {};^");
    }

    // #257264
    public void testIntroduceSuggestionFix_01() throws Exception {
        // in case of class, a new file is created
        // applyHint(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "new MyClass();^", "Create Class");
    }

    public void testIntroduceSuggestionFix_02() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->bar;^", "Create Field");
    }

    public void testIntroduceSuggestionFix_03() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->method();^", "Create Method");
    }

    public void testIntroduceSuggestionFix_04() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::CON;^", "Create Constant");
    }

    public void testIntroduceSuggestionFix_05() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::stMeth();^", "Create Method");
    }

    public void testIntroduceSuggestionFix_06() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::$stFld;^", "Create Field");
    }

    // #257296
    public void testIntroduceSuggestionForTraitField() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "$this->field;^");
    }

    public void testIntroduceSuggestionForTraitStaticField_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "self::$staticField1;^");
    }

    public void testIntroduceSuggestionForTraitStaticField_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitA::$staticField2;^");
    }

    public void testIntroduceSuggestionForTraitMethod() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "$this->method();^");
    }

    public void testIntroduceSuggestionForTraitConstant() throws Exception {
        // don't show the hint because a trait can't have constants
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitA::CONSTANT^");
    }

    public void testIntroduceSuggestionForTraitStaticMethod_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "self::staticMethod1();^");
    }

    public void testIntroduceSuggestionForTraitStaticMethod_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitA::staticMethod2();^");
    }

    public void testIntroduceSuggestionFixForTraitField() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "$this->field;^", "Create Field");
    }

    public void testIntroduceSuggestionFixForTraitStaticField_01() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "self::$staticField1;^", "Create Field");
    }

    public void testIntroduceSuggestionFixForTraitStaticField_02() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitA::$staticField2;^", "Create Field");
    }

    public void testIntroduceSuggestionFixForTraitStaticField_03() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitB::$staticTraitBField;^", "Create Field");
    }

    public void testIntroduceSuggestionFixForTraitStaticField_04() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitC::$staticTraitCField;^", "Create Field");
    }

    public void testIntroduceSuggestionFixForTraitMethod() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "$this->method();^", "Create Method");
    }

    public void testIntroduceSuggestionFixForTraitStaticMethod_01() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "self::staticMethod1();^", "Create Method");
    }

    public void testIntroduceSuggestionFixForTraitStaticMethod_02() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitA::staticMethod2();^", "Create Method");
    }

    public void testIntroduceSuggestionFixForTraitStaticMethod_03() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitB::staticTraitBMethod();^", "Create Method");
    }

    public void testIntroduceSuggestionFixForTraitStaticMethod_04() throws Exception {
        applyHint(new IntroduceSuggestion(), "testIntroduceSuggestionTrait.php", "TraitC::staticTraitCMethod();^", "Create Method");
    }

    public void testAddUseImportSuggestion_01() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testAddUseImportSuggestion_01.php", "new Foo\\Bar();^");
    }

    public void testAddUseImportSuggestion_02() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testAddUseImportSuggestion_02.php", "new Foox\\Barx();^");
    }

    public void testIssue258480_1() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testIssue258480_1.php", "$x = date2();^");
    }

    public void testIssue258480_2() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testIssue258480_2.php", "$x = date2();^");
    }

    public void testIssue223842() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue223842.php", "Foo::{\"\"}();^");
    }

    public void testIfBracesHint_01() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_01.php");
    }

    public void testIfBracesHint_02() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_02.php");
    }

    public void testIfBracesHint_03() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_03.php");
    }

    public void testDoWhileBracesHint() throws Exception {
        checkHints(new BracesHint.DoWhileBracesHint(), "testDoWhileBracesHint.php");
    }

    public void testWhileBracesHint() throws Exception {
        checkHints(new BracesHint.WhileBracesHint(), "testWhileBracesHint.php");
    }

    public void testForBracesHint() throws Exception {
        checkHints(new BracesHint.ForBracesHint(), "testForBracesHint.php");
    }

    public void testForEachBracesHint() throws Exception {
        checkHints(new BracesHint.ForEachBracesHint(), "testForEachBracesHint.php");
    }

    public void testGetSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.GetSuperglobalHint(), "testGetSuperglobalsHint.php");
    }

    public void testPostSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.PostSuperglobalHint(), "testPostSuperglobalsHint.php");
    }

    public void testCookieSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.CookieSuperglobalHint(), "testCookieSuperglobalsHint.php");
    }

    public void testServerSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.ServerSuperglobalHint(), "testServerSuperglobalsHint.php");
    }

    public void testEnvSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.EnvSuperglobalHint(), "testEnvSuperglobalsHint.php");
    }

    public void testRequestSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.RequestSuperglobalHint(), "testRequestSuperglobalsHint.php");
    }

    public void testEmptyStatementHint() throws Exception {
        checkHints(new EmptyStatementHint(), "testEmptyStatementHint.php");
    }

    public void testUnreachableStatementHint() throws Exception {
        checkHints(new UnreachableStatementHint(), "testUnreachableStatementHint.php");
    }

    public void testUnreachableStatementHint_02() throws Exception {
        checkHints(new UnreachableStatementHint(), "testUnreachableStatementHint_02.php");
    }

    public void testParentConstructorCallHint() throws Exception {
        checkHints(new ParentConstructorCallHint(), "testParentConstructorCallHint.php");
    }

    public void testIssue224940() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testIssue224940.php", "echo \"foo $whatever \\n^\";");
    }

    public void testErrorControlOperatorHint() throws Exception {
        checkHints(new ErrorControlOperatorHint(), "testErrorControlOperatorHint.php");
    }

    public void testIssue226494() throws Exception {
        checkHints(new MethodRedeclarationHintError(), "testIssue226494.php");
    }

    public void testClosingDelimUseCase01() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase01.php");
    }

    public void testClosingDelimUseCase02() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase02.php");
    }

    public void testClosingDelimUseCase03() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase03.php");
    }

    public void testIssue227081() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue227081.php");
    }

    public void testIssue229529() throws Exception {
        checkHints(new AmbiguousComparisonHint(), "testIssue229529.php");
    }

    public void testIssue234983() throws Exception {
        checkHints(new ParentConstructorCallHint(), "testIssue234983.php");
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

    public void testTooManyReturnStatements() throws Exception {
        checkHints(new TooManyReturnStatementsHint(), "testTooManyReturnStatements.php");
    }

    public void testIssue229522() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue229522.php", "function __construct($par^am) {");
    }

    public void testIssue237726_01() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237726_01.php");
    }

    public void testIssue237726_02() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237726_02.php");
    }

    public void testIssue237768() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237768.php");
    }

    public void testWrongParamNameHint() throws Exception {
        checkHints(new WrongParamNameHint(), "testWrongParamNameHint.php");
    }

    public void testIssue239277_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue239277.php", "Foo::ahoj(^);");
    }

    public void testIssue239277_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue239277.php", "Bat::$bar^z;");
    }

    public void testIssue239640() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue239640.php", "public function __construct(array $get = array(), array $post = array()^);");
    }

    public void testIssue239640_01() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue239640.php", "public function __construct(array $get = array(), array $post2 = array()^);");
    }

    public void testIssue241824_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue241824.php", "(new \\MyFoo(\"Whatever can be here\"))->myFnc()^;");
    }

    public void testIssue241824_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue241824.php", "(new \\MyFoo(\"Whatever can be here\"))->notMyFnc()^;");
    }

    public void testArraySyntaxSuggestion_01() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "$foo = ar^ray(");
    }

    public void testArraySyntaxSuggestion_02() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "11, ^22,");
    }

    public void testArraySyntaxSuggestion_03() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "2, ^3);");
    }

    public void testArraySyntaxSuggestion_04() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "$boo = a^rray(");
    }

    public void testArraySyntaxSuggestion_05() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "\"sdf\" => array(^1, 2, 3)");
    }

    public void testArraySyntaxSuggestion_06() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", ")^; //huhu");
    }

    public void testIssue248013_01() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "$foo = ar^ray(");
    }

    public void testIssue248013_02() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "11, ^22,");
    }

    public void testIssue248013_03() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "2, ^3);");
    }

    public void testIssue248013_04() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "$boo = a^rray(");
    }

    public void testIssue248013_05() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "\"sdf\" => array(^1, 2, 3)");
    }

    public void testIssue248013_06() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", ")^; //huhu");
    }

    public void testIssue248213() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue248213.php", "function __construct(&...$f^oo) {");
    }

    public void testIssue249306() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue249306.php", "function __construct(...$f^oo) {");
    }

    public void testIssue259026_01() throws Exception {
        checkHints(new EmptyStatementHint(), "testIssue259026_01.php");
    }

    public void testIssue259026_02() throws Exception {
        checkHints(new EmptyStatementHint(), "testIssue259026_02.php");
    }

    public void testIssue259026_03() throws Exception {
        checkHints(new EmptyStatementHint(), "testIssue259026_03.php");
    }

    public void testIssue259026Fix_02() throws Exception {
        applyHint(new EmptyStatementHint(), "testIssue259026_02.php", "declare(strict_types=1);;^", "Empty Statement");
    }

    public void testIssue259026Fix_03() throws Exception {
        applyHint(new EmptyStatementHint(), "testIssue259026_03.php", "$test1 = 1;;^", "Empty Statement");
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

    public void testIssue268557() throws Exception {
        checkHints(new FieldRedeclarationHintError(), "testIssue268557.php");
    }

    public void testConstantModifiersCheckHint() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testConstantModifiersCheckHint.php");
    }

    public void testConstantModifiersCheckFix_01() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testConstantModifiersCheckFix.php", "private const PRIVATE_INT^ERFACE_CONST = 2;", "Remove modifier");
    }

    public void testConstantModifiersCheckFix_02() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testConstantModifiersCheckFix.php", "protected const P^ROTECTED_INTERFACE_CONST = 3;", "Remove modifier");
    }

    public void testVoidReturnTypeHint_01() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_70), "testVoidReturnTypeHint.php");
    }

    public void testVoidReturnTypeHint_02() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_71), "testVoidReturnTypeHint.php");
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

    public void testDeclareStrictTypes_01a() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_01.php", "^<?php");
    }

    public void testDeclareStrictTypes_01b() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_01.php", "^<?php");
    }

    public void testDeclareStrictTypes_02a() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_02.php", "<?p^hp // first line");
    }

    public void testDeclareStrictTypes_02b() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_02.php", "<?ph^p // first line");
    }

    public void testDeclareStrictTypes_02c() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_02.php", "<?php ec^ho \"multiple open tags\" ?>");
    }

    public void testDeclareStrictTypes_02d() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_02.php", "<?php echo \"multiple open tags\"^ ?>");
    }

    public void testDeclareStrictTypes_03a() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_03.php", "<?p^hp");
    }

    public void testDeclareStrictTypes_03b() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_03.php", "<?p^hp");
    }

    public void testDeclareStrictTypesFix_01() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_01.php", "<?p^hp", "Add declare(strict_types=1)");
    }

    public void testDeclareStrictTypesFix_02a() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_02.php", "<?p^hp // first line", "Add declare(strict_types=1)");
    }

    public void testDeclareStrictTypesFix_02b() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_02.php", "<?php ec^ho \"multiple open tags\" ?>", "Add declare(strict_types=1)");
    }

    public void testDeclareStrictTypesFix_03() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_03.php", "<?p^hp", "Add declare(strict_types=1)");
    }

    public void testIssue270368_01() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue270368_01.php", "function __construct(?string $tes^t) {");
    }

    public void testIssue270368_02() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue270368_02.php", "function __construct(?\\Foo\\Bar $^test) {");
    }

    public void testIssue270368Fix_01() throws Exception {
        applyHint(new InitializeFieldSuggestion(), "testIssue270368_01.php", "function __construct(?string $tes^t) {", "Initialize Field");
    }

    public void testIssue270368Fix_02() throws Exception {
        applyHint(new InitializeFieldSuggestion(), "testIssue270368_02.php", "function __construct(?\\Foo\\Bar $^test) {", "Initialize Field");
    }

    public void testIssue270368Fix_03() throws Exception {
        applyHint(new InitializeFieldSuggestion(), "testIssue270368_03.php", "function __construct($^test) {", "Initialize Field");
    }

    public void testIssue270368Fix_04() throws Exception {
        applyHint(new InitializeFieldSuggestion(), "testIssue270368_04.php", "function __construct(\\Foo\\Bar $^test) {", "Initialize Field");
    }

    public void testFieldRedeclarationTypedProperties20Hint_01() throws Exception {
        // PHP 7.4
        checkHints(new FieldRedeclarationHintError(), "testFieldRedeclarationTypedProperties20Hint_01.php");
    }

    public void testFieldRedeclarationTypedProperties20Hint_02() throws Exception {
        // PHP 7.4
        checkHints(new FieldRedeclarationHintError(), "testFieldRedeclarationTypedProperties20Hint_02.php");
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

    private static final class ArraySyntaxSuggestionStub extends ArraySyntaxSuggestion {

        private final PhpVersion phpVersion;


        ArraySyntaxSuggestionStub(PhpVersion phpVersion) {
            assert phpVersion != null;
            this.phpVersion = phpVersion;
        }

        @Override
        protected boolean isAtLeastPhp54(FileObject fileObject) {
            return phpVersion.compareTo(PhpVersion.PHP_54) >= 0;
        }

    }

    private static final class ReturnTypeHintErrorStub extends ReturnTypeHintError {

        private final PhpVersion phpVersion;

        public ReturnTypeHintErrorStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject file) {
            return phpVersion;
        }

    }

    private static final class DeclareStrictTypesSuggestionStub extends DeclareStrictTypesSuggestion {

        private final PhpVersion phpVersion;

        public DeclareStrictTypesSuggestionStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject fileObject) {
            return phpVersion;
        }

    }

}
