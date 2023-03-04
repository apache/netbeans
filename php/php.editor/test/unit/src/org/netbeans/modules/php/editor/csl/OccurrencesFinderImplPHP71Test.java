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
package org.netbeans.modules.php.editor.csl;

public class OccurrencesFinderImplPHP71Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP71Test(String testName) {
        super(testName);
    }

    public void testNullableTypes_01() throws Exception {
        checkOccurrences(getTestPath(), "class TestC^lass {}", true);
    }

    public void testNullableTypes_02() throws Exception {
        checkOccurrences(getTestPath(), "function testReturnType(): ?Test^Class {", true);
    }

    public void testNullableTypes_03() throws Exception {
        checkOccurrences(getTestPath(), "function testParameterType(?TestCl^ass $testClass, ?TestInterface $testInterface) {", true);
    }

    public void testNullableTypes_04() throws Exception {
        checkOccurrences(getTestPath(), "public function testClassReturnType(): ?TestC^lass {", true);
    }

    public void testNullableTypes_05() throws Exception {
        checkOccurrences(getTestPath(), "public function testClassParameterType(?TestCl^ass $testClass, ?TestInterface $testInterface) {", true);
    }

    public void testNullableTypes_06() throws Exception {
        checkOccurrences(getTestPath(), "public static function testStaticClassParameterType(?TestC^lass $testClass, ?TestInterface $testInterface) {", true);
    }

    public void testNullableTypes_07() throws Exception {
        checkOccurrences(getTestPath(), "public function testTraitReturnType(): ?Tes^tClass {", true);
    }

    public void testNullableTypes_08() throws Exception {
        checkOccurrences(getTestPath(), "public function testTraitParameterType(?TestC^lass $testClass, ?TestInterface $testInterface) {", true);
    }

    public void testNullableTypes_09() throws Exception {
        checkOccurrences(getTestPath(), "public function testInterfaceReturnType(): ?TestCl^ass;", true);
    }

    public void testNullableTypes_10() throws Exception {
        checkOccurrences(getTestPath(), "public function testInterfaceParameterType(?TestC^lass $testClass, ?TestInterface $testInterface);", true);
    }

    public void testNullableTypes_11() throws Exception {
        checkOccurrences(getTestPath(), "interface TestInterfa^ce {}", true);
    }

    public void testNullableTypes_12() throws Exception {
        checkOccurrences(getTestPath(), "function testParameterType(?TestClass $testClass, ?TestInte^rface $testInterface) {", true);
    }

    public void testNullableTypes_13() throws Exception {
        checkOccurrences(getTestPath(), "public function testClassParameterType(?TestClass $testClass, ?TestIn^terface $testInterface) {", true);
    }

    public void testNullableTypes_14() throws Exception {
        checkOccurrences(getTestPath(), "public static function testStaticClassReturnType(): ?TestIn^terface {", true);
    }

    public void testNullableTypes_15() throws Exception {
        checkOccurrences(getTestPath(), "public static function testStaticClassParameterType(?TestClass $testClass, ?TestInt^erface $testInterface) {", true);
    }

    public void testNullableTypes_16() throws Exception {
        checkOccurrences(getTestPath(), "public function testTraitParameterType(?TestClass $testClass, ?TestInt^erface $testInterface) {", true);
    }

    public void testNullableTypes_17() throws Exception {
        checkOccurrences(getTestPath(), "public function testInterfaceParameterType(?TestClass $testClass, ?TestInter^face $testInterface);", true);
    }

    public void testNullableTypesFQN_01() throws Exception {
        checkOccurrences(getTestPath(), "class TestC^lass {}", true);
    }

    public void testNullableTypesFQN_02() throws Exception {
        checkOccurrences(getTestPath(), "function testReturnType(): ?\\Test\\Sub\\Test^Class {", true);
    }

    public void testNullableTypesFQN_03() throws Exception {
        checkOccurrences(getTestPath(), "function testParameterType(?\\Test\\Sub\\TestCl^ass $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {", true);
    }

    public void testNullableTypesFQN_04() throws Exception {
        checkOccurrences(getTestPath(), "public function testClassReturnType(): ?\\Test\\Sub\\TestC^lass {", true);
    }

    public void testNullableTypesFQN_05() throws Exception {
        checkOccurrences(getTestPath(), "public function testClassParameterType(?\\Test\\Sub\\TestCl^ass $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {", true);
    }

    public void testNullableTypesFQN_06() throws Exception {
        checkOccurrences(getTestPath(), "public static function testStaticClassParameterType(?\\Test\\Sub\\TestC^lass $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {", true);
    }

    public void testNullableTypesFQN_07() throws Exception {
        checkOccurrences(getTestPath(), "public function testTraitReturnType(): ?\\Test\\Sub\\Tes^tClass {", true);
    }

    public void testNullableTypesFQN_08() throws Exception {
        checkOccurrences(getTestPath(), "public function testTraitParameterType(?\\Test\\Sub\\TestC^lass $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {", true);
    }

    public void testNullableTypesFQN_09() throws Exception {
        checkOccurrences(getTestPath(), "public function testInterfaceReturnType(): ?\\Test\\Sub\\TestCl^ass;", true);
    }

    public void testNullableTypesFQN_10() throws Exception {
        checkOccurrences(getTestPath(), "public function testInterfaceParameterType(?\\Test\\Sub\\TestC^lass $testClass, ?\\Test\\Sub\\TestInterface $testInterface);", true);
    }

    public void testNullableTypesFQN_11() throws Exception {
        checkOccurrences(getTestPath(), "interface TestInterfa^ce {}", true);
    }

    public void testNullableTypesFQN_12() throws Exception {
        checkOccurrences(getTestPath(), "function testParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestInte^rface $testInterface) {", true);
    }

    public void testNullableTypesFQN_13() throws Exception {
        checkOccurrences(getTestPath(), "public function testClassParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestIn^terface $testInterface) {", true);
    }

    public void testNullableTypesFQN_14() throws Exception {
        checkOccurrences(getTestPath(), "public static function testStaticClassReturnType(): ?\\Test\\Sub\\TestIn^terface {", true);
    }

    public void testNullableTypesFQN_15() throws Exception {
        checkOccurrences(getTestPath(), "public static function testStaticClassParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestInt^erface $testInterface) {", true);
    }

    public void testNullableTypesFQN_16() throws Exception {
        checkOccurrences(getTestPath(), "public function testTraitParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestInt^erface $testInterface) {", true);
    }

    public void testNullableTypesFQN_17() throws Exception {
        checkOccurrences(getTestPath(), "public function testInterfaceParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestInter^face $testInterface);", true);
    }

    public void testNullableTypesInPhpDoc_01() throws Exception {
        checkOccurrences(getTestPath(), " * @method ?\\PHP^DocTags testMethod2(?PHPDocTags $tags) Description", true);
    }

    public void testNullableTypesInPhpDoc_02() throws Exception {
        checkOccurrences(getTestPath(), " * @method ?\\PHPDocTags testMethod2(?PHPDo^cTags $tags) Description", true);
    }

    public void testNullableTypesInPhpDoc_03() throws Exception {
        checkOccurrences(getTestPath(), " * @property ?P^HPDocTags $test Description", true);
    }

    public void testNullableTypesInPhpDoc_04() throws Exception {
        checkOccurrences(getTestPath(), "class PHPDocT^ags {", true);
    }

    public void testNullableTypesInPhpDoc_05() throws Exception {
        checkOccurrences(getTestPath(), "@param ?PHPDoc^Tags $tags", true);
    }

    public void testNullableTypesInPhpDoc_06() throws Exception {
        checkOccurrences(getTestPath(), "@return ?PHPDocT^ags", true);
    }

    public void testNullableTypesInPhpDoc_07() throws Exception {
        checkOccurrences(getTestPath(), "public function testMethod(?^PHPDocTags $tags) {", true);
    }

    public void testMultiCatch_01() throws Exception {
        checkOccurrences(getTestPath(), "class ^ExceptionType1 extends \\Exception {", true);
    }

    public void testMultiCatch_02() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Exceptio^nType1 | ExceptionType2 $ex) {", true);
    }

    public void testMultiCatch_03() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Except^ionType1 | ExceptionType2 | ExceptionType3 $ex) {", true);
    }

    public void testMultiCatch_04() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Excepti^onType1 $ex) {", true);
    }

    public void testMultiCatch_05() throws Exception {
        checkOccurrences(getTestPath(), "class Exceptio^nType2 extends \\Exception {", true);
    }

    public void testMultiCatch_06() throws Exception {
        checkOccurrences(getTestPath(), "} catch (ExceptionType1 | ^ExceptionType2 $ex) {", true);
    }

    public void testMultiCatch_07() throws Exception {
        checkOccurrences(getTestPath(), "} catch (ExceptionType1 | Exc^eptionType2 | ExceptionType3 $ex) {", true);
    }

    public void testMultiCatch_08() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Exceptio^nType2 | ExceptionType3 $ex) {", true);
    }

    public void testMultiCatch_09() throws Exception {
        checkOccurrences(getTestPath(), "class Ex^ceptionType3 extends \\Exception {", true);
    }

    public void testMultiCatch_10() throws Exception {
        checkOccurrences(getTestPath(), "} catch (^ExceptionType3 $ex) {", true);
    }

    public void testMultiCatch_11() throws Exception {
        checkOccurrences(getTestPath(), "} catch (ExceptionType1 | ExceptionType2 | Exceptio^nType3 $ex) {", true);
    }

    public void testMultiCatch_12() throws Exception {
        checkOccurrences(getTestPath(), "} catch (ExceptionType2 | ^ExceptionType3 $ex) {", true);
    }

    public void testMultiCatchFQN_01() throws Exception {
        checkOccurrences(getTestPath(), "class ^ExceptionType1 extends \\Exception {", true);
    }

    public void testMultiCatchFQN_02() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\Exceptio^nType1 | \\Test\\Sub\\ExceptionType2 $ex) {", true);
    }

    public void testMultiCatchFQN_03() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\Except^ionType1 | \\Test\\Sub\\ExceptionType2 | \\Test\\Sub\\ExceptionType3 $ex) {", true);
    }

    public void testMultiCatchFQN_04() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\Excepti^onType1 $ex) {", true);
    }

    public void testMultiCatchFQN_05() throws Exception {
        checkOccurrences(getTestPath(), "class Exceptio^nType2 extends \\Exception {", true);
    }

    public void testMultiCatchFQN_06() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\^ExceptionType2 $ex) {", true);
    }

    public void testMultiCatchFQN_07() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\Exc^eptionType2 | \\Test\\Sub\\ExceptionType3 $ex) {", true);
    }

    public void testMultiCatchFQN_08() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\Exceptio^nType2 | \\Test\\Sub\\ExceptionType3 $ex) {", true);
    }

    public void testMultiCatchFQN_09() throws Exception {
        checkOccurrences(getTestPath(), "class Ex^ceptionType3 extends \\Exception {", true);
    }

    public void testMultiCatchFQN_10() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\^ExceptionType3 $ex) {", true);
    }

    public void testMultiCatchFQN_11() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\ExceptionType2 | \\Test\\Sub\\Exceptio^nType3 $ex) {", true);
    }

    public void testMultiCatchFQN_12() throws Exception {
        checkOccurrences(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType2 | \\Test\\Sub\\^ExceptionType3 $ex) {", true);
    }

    public void testClassConstantVisibility_01() throws Exception {
        checkOccurrences(getTestPath(), "const IMPLICIT_PUBL^IC_PARENT_CONST = 0;", true);
    }

    public void testClassConstantVisibility_02() throws Exception {
        checkOccurrences(getTestPath(), "ParentClass::^IMPLICIT_PUBLIC_PARENT_CONST;", true);
    }

    public void testClassConstantVisibility_03() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::IMPLICIT_P^UBLIC_PARENT_CONST;", true);
    }

    public void testClassConstantVisibility_04() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass2::IMPLICIT_PUBLIC_PARENT_CON^ST;", true);
    }

    public void testClassConstantVisibility_05() throws Exception {
        checkOccurrences(getTestPath(), "ParentClass::IM^PLICIT_PUBLIC_PARENT_CONST; // global", true);
    }

    public void testClassConstantVisibility_06() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::IMPLICIT_PUB^LIC_PARENT_CONST; // global", true);
    }

    public void testClassConstantVisibility_07() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass2::I^MPLICIT_PUBLIC_PARENT_CONST; // global", true);
    }

    public void testClassConstantVisibility_08() throws Exception {
        checkOccurrences(getTestPath(), "public const ^PUBLIC_PARENT_CONST = 0;", true);
    }

    public void testClassConstantVisibility_09() throws Exception {
        checkOccurrences(getTestPath(), "self::^PUBLIC_PARENT_CONST;", true);
    }

    public void testClassConstantVisibility_10() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::PUBLI^C_PARENT_CONST;", true);
    }

    public void testClassConstantVisibility_11() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass2::PUBLIC^_PARENT_CONST;", true);
    }

    public void testClassConstantVisibility_12() throws Exception {
        checkOccurrences(getTestPath(), "ParentClass::PUBLIC_PA^RENT_CONST; // global", true);
    }

    public void testClassConstantVisibility_13() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::PUBLIC_PAR^ENT_CONST; // global", true);
    }

    public void testClassConstantVisibility_14() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass2::PUBL^IC_PARENT_CONST; // global", true);
    }

    public void testClassConstantVisibility_15() throws Exception {
        checkOccurrences(getTestPath(), "private const PRI^VATE_PARENT_CONST = \"private\";", true);
    }

    public void testClassConstantVisibility_16() throws Exception {
        checkOccurrences(getTestPath(), "static::^PRIVATE_PARENT_CONST;", true);
    }

    public void testClassConstantVisibility_17() throws Exception {
        checkOccurrences(getTestPath(), "protected const PROT^ECTED_PARENT_CONST = [0, 1];", true);
    }

    public void testClassConstantVisibility_18() throws Exception {
        checkOccurrences(getTestPath(), "ParentClass::PROT^ECTED_PARENT_CONST[0];", true);
    }

    public void testClassConstantVisibility_19() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::^PROTECTED_PARENT_CONST[0];", true);
    }

    public void testClassConstantVisibility_20() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass2::^PROTECTED_PARENT_CONST[1];", true);
    }

    public void testClassConstantVisibility_21() throws Exception {
        checkOccurrences(getTestPath(), "const ^IMPLICIT_PUBLIC_INTERFACE_CONST = 1;", true);
    }

    public void testClassConstantVisibility_22() throws Exception {
        checkOccurrences(getTestPath(), "TestInterfaceImpl::IMPLICIT_PU^BLIC_INTERFACE_CONST;", true);
    }

    public void testClassConstantVisibility_23() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass2::IMPLICIT_P^UBLIC_INTERFACE_CONST;", true);
    }

    public void testClassConstantVisibility_24() throws Exception {
        checkOccurrences(getTestPath(), "TestInterface::IMPLICIT_PUBLIC_^INTERFACE_CONST; // global", true);
    }

    public void testClassConstantVisibility_25() throws Exception {
        checkOccurrences(getTestPath(), "TestInterfaceImpl::IMPLICIT_PUBLIC_INT^ERFACE_CONST; // global", true);
    }

    public void testClassConstantVisibility_26() throws Exception {
        checkOccurrences(getTestPath(), "public const PUBLIC_INTE^RFACE_CONST = 0;", true);
    }

    public void testClassConstantVisibility_27() throws Exception {
        checkOccurrences(getTestPath(), "TestInterfaceImpl::PUBLIC_I^NTERFACE_CONST;", true);
    }

    public void testClassConstantVisibility_28() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass2::PUBLIC_INTE^RFACE_CONST;", true);
    }

    public void testClassConstantVisibility_29() throws Exception {
        checkOccurrences(getTestPath(), "TestInterface::PUBLIC_INT^ERFACE_CONST; // global", true);
    }

    public void testClassConstantVisibility_30() throws Exception {
        checkOccurrences(getTestPath(), "TestInterfaceImpl::^PUBLIC_INTERFACE_CONST; // global", true);
    }

    public void testClassConstantVisibilityFQN_01() throws Exception {
        checkOccurrences(getTestPath(), "const IMPLICIT^_PUBLIC_PARENT_CONST = [0, 1];", true);
    }

    public void testClassConstantVisibilityFQN_02() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\ParentClass::^IMPLICIT_PUBLIC_PARENT_CONST[1];", true);
    }

    public void testClassConstantVisibilityFQN_03() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\ChildClass::IMPLICIT_PU^BLIC_PARENT_CONST[0];", true);
    }

    public void testClassConstantVisibilityFQN_04() throws Exception {
        checkOccurrences(getTestPath(), "public const PUBLI^C_PARENT_CONST = 0;", true);
    }

    public void testClassConstantVisibilityFQN_05() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\ParentClass::^PUBLIC_PARENT_CONST;", true);
    }

    public void testClassConstantVisibilityFQN_06() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\ChildClass::PUBLIC_PA^RENT_CONST;", true);
    }

    public void testClassConstantVisibilityFQN_07() throws Exception {
        checkOccurrences(getTestPath(), "const IMPLICIT_PUBLIC_INTERFAC^E_CONST = 1;", true);
    }

    public void testClassConstantVisibilityFQN_08() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\TestInterface::^IMPLICIT_PUBLIC_INTERFACE_CONST;", true);
    }

    public void testClassConstantVisibilityFQN_09() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\TestInterfaceImpl::IMPLICIT_PU^BLIC_INTERFACE_CONST;", true);
    }

    public void testClassConstantVisibilityFQN_10() throws Exception {
        checkOccurrences(getTestPath(), "public const PUBLIC_INT^ERFACE_CONST = 0;", true);
    }

    public void testClassConstantVisibilityFQN_11() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\TestInterface::^PUBLIC_INTERFACE_CONST;", true);
    }

    public void testClassConstantVisibilityFQN_12() throws Exception {
        checkOccurrences(getTestPath(), "\\Test\\Sub\\TestInterfaceImpl::PUBL^IC_INTERFACE_CONST;", true);
    }

}
