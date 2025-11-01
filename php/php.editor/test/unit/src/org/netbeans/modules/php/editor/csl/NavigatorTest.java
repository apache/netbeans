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

/**
 *
 * @author Petr Pisl
 */
public class NavigatorTest extends PhpNavigatorTestBase {

    public NavigatorTest(String testName) {
        super(testName);
    }

    public void testNamespace() throws Exception {
        performTest("structure/php53/namespace");
    }

    public void testMultiple_namespaces() throws Exception {
        performTest("structure/php53/multiple_namespaces");
    }

    public void testBracketedMultipleNamespaces() throws Exception {
        performTest("structure/php53/bracketed_multiple_namespaces");
    }

    public void testBracketedMultipleNamespacesWithDefaultOne() throws Exception {
        performTest("structure/php53/bracketed_multiple_namespaces_with_default_one");
    }

    public void testBracketedMultipleNamespacesWithMultipleDefault() throws Exception {
        performTest("structure/php53/bracketed_multiple_namespaces_with_multiple_default");
    }

    public void testScan() throws Exception {
        performTest("structure/interface_001");
    }

    public void test133484() throws Exception {
        performTest("structure/referenceParameter_001");
    }

    public void testClass() throws Exception {
        performTest("structure/class005");
    }

    public void testIssue142644() throws Exception {
        performTest("structure/issue142644");
    }

    public void testIssue148558() throws Exception {
        performTest("structure/issue148558");
    }

    public void testPHPDocTagProperty() throws Exception {
        performTest("structure/propertyTag");
    }

    public void testIssue205886_01() throws Exception {
        performTest("structure/issue205886_01");
    }

    public void testTraits_01() throws Exception {
        performTest("structure/traitsStructure_01");
    }

    public void testTraits_02() throws Exception {
        performTest("structure/traitsStructure_02");
    }

    public void testIssue170712() throws Exception {
        performTest("structure/issue170712");
    }

    public void testAnonymousClassInNamespaceScope() throws Exception {
        performTest("structure/anonymousClassInNamespaceScope");
    }

    public void testAnonymousClassInClassScope() throws Exception {
        performTest("structure/anonymousClassInClassScope");
    }

    public void testAnonymousClassInTraitScope() throws Exception {
        performTest("structure/anonymousClassInTraitScope");
    }

    public void testNullableTypes_01() throws Exception {
        performTest("structure/nullableTypes_01");
    }

    public void testNullableTypes_02() throws Exception {
        performTest("structure/nullableTypes_02");
    }

    public void testMagicMethods_01() throws Exception {
        performTest("structure/magicMethods");
    }

    public void testPHP80ConstructorPropertyPromotion() throws Exception {
        performTest("structure/php80ConstructorPropertyPromotion");
    }

    public void testPureIntersectionTypes() throws Exception {
        performTest("structure/pureIntersectionTypes");
    }

    public void testEnumerations() throws Exception {
        performTest("structure/enumerations");
    }

    public void testEnumCasesWithError01() throws Exception {
        performTest("structure/enumCasesWithError01");
    }

    public void testEnumCasesWithError02() throws Exception {
        performTest("structure/enumCasesWithError02");
    }

    public void testStandAloneTrueType() throws Exception {
        performTest("structure/standAloneTrueType");
    }

    public void testConstantsInTraits() throws Exception {
        performTest("structure/php82/constantsInTraits");
    }

    public void testDNFReturnTypes() throws Exception {
        performTest("structure/php82/dnfReturnTypes");
    }

    public void testDNFParameterTypes() throws Exception {
        performTest("structure/php82/dnfParameterTypes");
    }

    public void testDNFFieldTypes() throws Exception {
        performTest("structure/php82/dnfFieldTypes");
    }

    public void testFunctionGuessingArrayReturnType() throws Exception {
        performTest("structure/functionGuessingArrayReturnType");
    }

    public void testTypedClassConstants() throws Exception {
        performTest("structure/php83/typedClassConstants");
    }

    // PHP 8.4
    public void testNewWithoutParentheses() throws Exception {
        performTest("structure/php84/newWithoutParentheses");
    }

    public void testDeprecatedAttributeFunctionsInGlobal() throws Exception {
        performTest("structure/php84/deprecatedAttributeFunctionsInGlobal");
    }

    public void testDeprecatedAttributeFunctionsInNamespace01() throws Exception {
        performTest("structure/php84/deprecatedAttributeFunctionsInNamespace01");
    }

    public void testDeprecatedAttributeFunctionsInNamespace02() throws Exception {
        performTest("structure/php84/deprecatedAttributeFunctionsInNamespace02");
    }

    public void testDeprecatedAttributeFunctionsInNamespace03() throws Exception {
        performTest("structure/php84/deprecatedAttributeFunctionsInNamespace03");
    }

    public void testDeprecatedAttributeFunctionsInNamespaceWithUse01() throws Exception {
        performTest("structure/php84/deprecatedAttributeFunctionsInNamespaceWithUse01");
    }

    public void testDeprecatedAttributeFunctionsInNamespaceWithUse02() throws Exception {
        performTest("structure/php84/deprecatedAttributeFunctionsInNamespaceWithUse02");
    }

    public void testDeprecatedAttributeFunctionsInNamespaceWithUse03() throws Exception {
        performTest("structure/php84/deprecatedAttributeFunctionsInNamespaceWithUse03");
    }

    public void testDeprecatedAttributeMethodsInGlobal() throws Exception {
        performTest("structure/php84/deprecatedAttributeMethodsInGlobal");
    }

    public void testDeprecatedAttributeMethodsInNamespace() throws Exception {
        performTest("structure/php84/deprecatedAttributeMethodsInNamespace");
    }

    public void testDeprecatedAttributeMethodsInNamespaceWithUse() throws Exception {
        performTest("structure/php84/deprecatedAttributeMethodsInNamespaceWithUse");
    }

    public void testDeprecatedAttributeEnumCaseInGlobal() throws Exception {
        performTest("structure/php84/deprecatedAttributeEnumCaseInGlobal");
    }

    public void testDeprecatedAttributeEnumCaseInNamespace() throws Exception {
        performTest("structure/php84/deprecatedAttributeEnumCaseInNamespace");
    }

    public void testDeprecatedAttributeEnumCaseInNamespaceWithUse() throws Exception {
        performTest("structure/php84/deprecatedAttributeEnumCaseInNamespaceWithUse");
    }

    public void testDeprecatedAttributeTypeConstantInGlobal() throws Exception {
        performTest("structure/php84/deprecatedAttributeTypeConstantInGlobal");
    }

    public void testDeprecatedAttributeTypeConstantInNamespace() throws Exception {
        performTest("structure/php84/deprecatedAttributeTypeConstantInNamespace");
    }

    public void testDeprecatedAttributeTypeConstantInNamespaceWithUse() throws Exception {
        performTest("structure/php84/deprecatedAttributeTypeConstantInNamespaceWithUse");
    }

    public void testAsymmetricVisibilityClass() throws Exception {
        performTest("structure/php84/asymmetricVisibilityClass");
    }

    public void testAsymmetricVisibilityTrait() throws Exception {
        performTest("structure/php84/asymmetricVisibilityTrait");
    }

    public void testAsymmetricVisibilityAnonClass() throws Exception {
        performTest("structure/php84/asymmetricVisibilityAnonClass");
    }

    public void testAsymmetricVisibilityConstructorPropertyPromotion() throws Exception {
        performTest("structure/php84/asymmetricVisibilityConstructorPropertyPromotion");
    }

    public void testFinalFieldsClass() throws Exception {
        performTest("structure/php84/finalFieldsClass");
    }

    public void testFinalFieldsTrait() throws Exception {
        performTest("structure/php84/finalFieldsTrait");
    }

    public void testFinalFieldsAnonClass() throws Exception {
        performTest("structure/php84/finalFieldsAnonClass");
    }

    public void testGH8527() throws Exception {
        performTest("structure/gh8527");
    }
}
