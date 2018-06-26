/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.modules.php.editor.csl;

public class GotoDeclarationPHP71Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP71Test(String testName) {
        super(testName);
    }

    public void testNullableTypes_01() throws Exception {
        checkDeclaration(getTestPath(), "function testReturnType(): ?Tes^tClass {", "class ^TestClass {}");
    }

    public void testNullableTypes_02() throws Exception {
        checkDeclaration(getTestPath(),
                "function testParameterType(?Te^stClass $testClass, ?TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypes_03() throws Exception {
        checkDeclaration(getTestPath(),
                "function testParameterType(?TestClass $testClass, ?TestIn^terface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypes_04() throws Exception {
        checkDeclaration(getTestPath(), "public function testClassReturnType(): ?TestCla^ss {", "class ^TestClass {}");
    }

    public void testNullableTypes_05() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testClassParameterType(?TestCla^ss $testClass, ?TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypes_06() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testClassParameterType(?TestClass $testClass, ?^TestInterface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypes_07() throws Exception {
        checkDeclaration(getTestPath(), "public static function testStaticClassReturnType(): ?T^estInterface {", "interface ^TestInterface {}");
    }

    public void testNullableTypes_08() throws Exception {
        checkDeclaration(getTestPath(),
                "public static function testStaticClassParameterType(?^TestClass $testClass, ?TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypes_09() throws Exception {
        checkDeclaration(getTestPath(),
                "public static function testStaticClassParameterType(?TestClass $testClass, ?TestInte^rface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypes_10() throws Exception {
        checkDeclaration(getTestPath(), "public function testTraitReturnType(): ?Te^stClass {", "class ^TestClass {}");
    }

    public void testNullableTypes_11() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testTraitParameterType(?TestClas^s $testClass, ?TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypes_12() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testTraitParameterType(?TestClass $testClass, ?Test^Interface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypes_13() throws Exception {
        checkDeclaration(getTestPath(), "public function testInterfaceReturnType(): ?TestC^lass;", "class ^TestClass {}");
    }

    public void testNullableTypes_14() throws Exception {
        checkDeclaration(getTestPath(), "public function testInterfaceParameterType(?Test^Class $testClass, ?TestInterface $testInterface);", "class ^TestClass {}");
    }

    public void testNullableTypes_15() throws Exception {
        checkDeclaration(getTestPath(), "public function testInterfaceParameterType(?TestClass $testClass, ?TestInterf^ace $testInterface);", "interface ^TestInterface {}");
    }

    public void testNullableTypesFQN_01() throws Exception {
        checkDeclaration(getTestPath(), "function testReturnType(): ?\\Test\\Sub\\Te^stInterface {", "interface ^TestInterface {}");
    }

    public void testNullableTypesFQN_02() throws Exception {
        checkDeclaration(getTestPath(),
                "function testParameterType(?\\Test\\Sub\\Te^stClass $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypesFQN_03() throws Exception {
        checkDeclaration(getTestPath(),
                "function testParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestIn^terface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypesFQN_04() throws Exception {
        checkDeclaration(getTestPath(), "public function testClassReturnType(): ?\\Test\\Sub\\TestCla^ss {", "class ^TestClass {}");
    }

    public void testNullableTypesFQN_05() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testClassParameterType(?\\Test\\Sub\\TestCla^ss $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypesFQN_06() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testClassParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\^TestInterface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypesFQN_07() throws Exception {
        checkDeclaration(getTestPath(), "public static function testStaticClassReturnType(): ?\\Test\\Sub\\T^estClass {", "class ^TestClass {}");
    }

    public void testNullableTypesFQN_08() throws Exception {
        checkDeclaration(getTestPath(),
                "public static function testStaticClassParameterType(?\\Test\\Sub\\^TestClass $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypesFQN_09() throws Exception {
        checkDeclaration(getTestPath(),
                "public static function testStaticClassParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestInte^rface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypesFQN_10() throws Exception {
        checkDeclaration(getTestPath(), "public function testTraitReturnType(): ?\\Test\\Sub\\Te^stClass {", "class ^TestClass {}");
    }

    public void testNullableTypesFQN_11() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testTraitParameterType(?\\Test\\Sub\\TestClas^s $testClass, ?\\Test\\Sub\\TestInterface $testInterface) {",
                "class ^TestClass {}");
    }

    public void testNullableTypesFQN_12() throws Exception {
        checkDeclaration(getTestPath(),
                "public function testTraitParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\Test^Interface $testInterface) {",
                "interface ^TestInterface {}");
    }

    public void testNullableTypesInPhpDoc_01() throws Exception {
        checkDeclaration(getTestPath(),
                " * @method ?\\PHPD^ocTags testMethod2(?PHPDocTags $tags) Description",
                "class ^PHPDocTags {");
    }

    public void testNullableTypesInPhpDoc_02() throws Exception {
        checkDeclaration(getTestPath(),
                " * @method ?\\PHPDocTags testMethod2(?^PHPDocTags $tags) Description",
                "class ^PHPDocTags {");
    }

    public void testNullableTypesInPhpDoc_03() throws Exception {
        checkDeclaration(getTestPath(),
                " * @property ?PH^PDocTags $test Description",
                "class ^PHPDocTags {");
    }

    public void testNullableTypesInPhpDoc_04() throws Exception {
        checkDeclaration(getTestPath(),
                "@param ?PHP^DocTags $tags",
                "class ^PHPDocTags {");
    }

    public void testNullableTypesInPhpDoc_05() throws Exception {
        checkDeclaration(getTestPath(),
                "@return ?PHPDocTags^",
                "class ^PHPDocTags {");
    }

    public void testNullableTypesFQN_13() throws Exception {
        checkDeclaration(getTestPath(), "public function testInterfaceReturnType(): ?\\Test\\Sub\\TestC^lass;", "class ^TestClass {}");
    }

    public void testNullableTypesFQN_14() throws Exception {
        checkDeclaration(getTestPath(), "public function testInterfaceParameterType(?\\Test\\Sub\\Test^Class $testClass, ?\\Test\\Sub\\TestInterface $testInterface);", "class ^TestClass {}");
    }

    public void testNullableTypesFQN_15() throws Exception {
        checkDeclaration(getTestPath(), "public function testInterfaceParameterType(?\\Test\\Sub\\TestClass $testClass, ?\\Test\\Sub\\TestInterf^ace $testInterface);", "interface ^TestInterface {}");
    }

    public void testMultiCatch_01() throws Exception {
        checkDeclaration(getTestPath(), "} catch (Exceptio^nType1 | ExceptionType2 $ex) {", "class ^ExceptionType1 extends \\Exception {");
    }

    public void testMultiCatch_02() throws Exception {
        checkDeclaration(getTestPath(), "} catch (ExceptionType1 | ExceptionT^ype2 $ex) {", "class ^ExceptionType2 extends \\Exception {");
    }

    public void testMultiCatch_03() throws Exception {
        checkDeclaration(getTestPath(), "} catch (^ExceptionType3 $ex) {", "class ^ExceptionType3 extends \\Exception {");
    }

    public void testMultiCatch_04() throws Exception {
        checkDeclaration(getTestPath(), "} catch (E^xceptionType1 | ExceptionType2 | ExceptionType3 $ex) {", "class ^ExceptionType1 extends \\Exception {");
    }

    public void testMultiCatch_05() throws Exception {
        checkDeclaration(getTestPath(), "} catch (ExceptionType1 | ^ExceptionType2 | ExceptionType3 $ex) {", "class ^ExceptionType2 extends \\Exception {");
    }

    public void testMultiCatch_06() throws Exception {
        checkDeclaration(getTestPath(), "} catch (ExceptionType1 | ExceptionType2 | ExceptionTyp^e3 $ex) {", "class ^ExceptionType3 extends \\Exception {");
    }

    public void testMultiCatch_07() throws Exception {
        checkDeclaration(getTestPath(), "} catch (ExceptionTyp^e1 $ex) {", "class ^ExceptionType1 extends \\Exception {");
    }

    public void testMultiCatch_08() throws Exception {
        checkDeclaration(getTestPath(), "} catch (Exceptio^nType2 | ExceptionType3 $ex) {", "class ^ExceptionType2 extends \\Exception {");
    }

    public void testMultiCatch_09() throws Exception {
        checkDeclaration(getTestPath(), "} catch (ExceptionType2 | Excepti^onType3 $ex) {", "class ^ExceptionType3 extends \\Exception {");
    }

    public void testMultiCatchFQN_01() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\Exceptio^nType1 | \\Test\\Sub\\ExceptionType2 $ex) {", "class ^ExceptionType1 extends \\Exception {");
    }

    public void testMultiCatchFQN_02() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\ExceptionT^ype2 $ex) {", "class ^ExceptionType2 extends \\Exception {");
    }

    public void testMultiCatchFQN_03() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\^ExceptionType3 $ex) {", "class ^ExceptionType3 extends \\Exception {");
    }

    public void testMultiCatchFQN_04() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\E^xceptionType1 | \\Test\\Sub\\ExceptionType2 | \\Test\\Sub\\ExceptionType3 $ex) {", "class ^ExceptionType1 extends \\Exception {");
    }

    public void testMultiCatchFQN_05() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\^ExceptionType2 | \\Test\\Sub\\ExceptionType3 $ex) {", "class ^ExceptionType2 extends \\Exception {");
    }

    public void testMultiCatchFQN_06() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType1 | \\Test\\Sub\\ExceptionType2 | \\Test\\Sub\\ExceptionTyp^e3 $ex) {", "class ^ExceptionType3 extends \\Exception {");
    }

    public void testMultiCatchFQN_07() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\ExceptionTyp^e1 $ex) {", "class ^ExceptionType1 extends \\Exception {");
    }

    public void testMultiCatchFQN_08() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\Exceptio^nType2 | \\Test\\Sub\\ExceptionType3 $ex) {", "class ^ExceptionType2 extends \\Exception {");
    }

    public void testMultiCatchFQN_09() throws Exception {
        checkDeclaration(getTestPath(), "} catch (\\Test\\Sub\\ExceptionType2 | \\Test\\Sub\\Excepti^onType3 $ex) {", "class ^ExceptionType3 extends \\Exception {");
    }

    public void testClassConstantVisibility_01() throws Exception {
        checkDeclaration(getTestPath(), "ParentClass::IMPLICIT_PUBLIC^_PARENT_CONST;", "const ^IMPLICIT_PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_02() throws Exception {
        checkDeclaration(getTestPath(), "self::PUBLIC_PARENT_CO^NST;", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_03() throws Exception {
        checkDeclaration(getTestPath(), "static::PRI^VATE_PARENT_CONST;", "private const ^PRIVATE_PARENT_CONST = \"private\";");
    }

    public void testClassConstantVisibility_04() throws Exception {
        checkDeclaration(getTestPath(), "ParentClass::PROTECTED_PARENT_C^ONST[0];", "protected const ^PROTECTED_PARENT_CONST = [0, 1];");
    }

    public void testClassConstantVisibility_05() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::IMPLICIT_P^UBLIC_PARENT_CONST;", "const ^IMPLICIT_PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_06() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::PUBLIC_PAR^ENT_CONST;", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_07() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::PROTECTED_PAR^ENT_CONST[0];", "protected const ^PROTECTED_PARENT_CONST = [0, 1];");
    }

    public void testClassConstantVisibility_08() throws Exception {
        checkDeclaration(getTestPath(), "TestInterfaceImpl::IMPLICIT_PUBLIC_INTE^RFACE_CONST;", "const ^IMPLICIT_PUBLIC_INTERFACE_CONST = 1;");
    }

    public void testClassConstantVisibility_09() throws Exception {
        checkDeclaration(getTestPath(), "TestInterfaceImpl::PUBLIC_INTERFA^CE_CONST;", "const ^PUBLIC_INTERFACE_CONST = 0;");
    }

    public void testClassConstantVisibility_10() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass2::IMPLICIT_PU^BLIC_PARENT_CONST;", "const ^IMPLICIT_PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_11() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass2::PUBLIC_PA^RENT_CONST;", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_12() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass2::PROTECTED_PAREN^T_CONST[1];", "protected const ^PROTECTED_PARENT_CONST = [0, 1];");
    }

    public void testClassConstantVisibility_13() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass2::IMPLICIT_PUBLIC_INT^ERFACE_CONST;", "const ^IMPLICIT_PUBLIC_INTERFACE_CONST = 1;");
    }

    public void testClassConstantVisibility_14() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass2::PUBLIC_IN^TERFACE_CONST;", "const ^PUBLIC_INTERFACE_CONST = 0;");
    }

    public void testClassConstantVisibility_15() throws Exception {
        checkDeclaration(getTestPath(), "ParentClass::IMPLICIT_PU^BLIC_PARENT_CONST; // global", "const ^IMPLICIT_PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_16() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::^IMPLICIT_PUBLIC_PARENT_CONST; // global", "const ^IMPLICIT_PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_17() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass2::^IMPLICIT_PUBLIC_PARENT_CONST; // global", "const ^IMPLICIT_PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_18() throws Exception {
        checkDeclaration(getTestPath(), "ParentClass::^PUBLIC_PARENT_CONST; // global", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_19() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::PUBLIC_PAR^ENT_CONST; // global", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_20() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass2::PUBLIC_PA^RENT_CONST; // global", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibility_21() throws Exception {
        checkDeclaration(getTestPath(), "TestInterface::IMPLICIT_PUBL^IC_INTERFACE_CONST; // global", "const ^IMPLICIT_PUBLIC_INTERFACE_CONST = 1;");
    }

    public void testClassConstantVisibility_22() throws Exception {
        checkDeclaration(getTestPath(), "TestInterfaceImpl::IMPLICIT^_PUBLIC_INTERFACE_CONST; // global", "const ^IMPLICIT_PUBLIC_INTERFACE_CONST = 1;");
    }

    public void testClassConstantVisibility_23() throws Exception {
        checkDeclaration(getTestPath(), "TestInterface::PUBLIC_INTE^RFACE_CONST; // global", "const ^PUBLIC_INTERFACE_CONST = 0;");
    }

    public void testClassConstantVisibility_24() throws Exception {
        checkDeclaration(getTestPath(), "TestInterfaceImpl::PUBLIC_IN^TERFACE_CONST; // global", "const ^PUBLIC_INTERFACE_CONST = 0;");
    }

    public void testClassConstantVisibilityFQN_01() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\ParentClass::^IMPLICIT_PUBLIC_PARENT_CONST[1];", "const ^IMPLICIT_PUBLIC_PARENT_CONST = [0, 1];");
    }

    public void testClassConstantVisibilityFQN_02() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\ParentClass::PUBLIC_PARENT_CON^ST;", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibilityFQN_03() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\ChildClass::IMPLICIT_PUBLIC_P^ARENT_CONST[0];", "const ^IMPLICIT_PUBLIC_PARENT_CONST = [0, 1];");
    }

    public void testClassConstantVisibilityFQN_04() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\ChildClass::PUBLIC_PARENT_CO^NST;", "public const ^PUBLIC_PARENT_CONST = 0;");
    }

    public void testClassConstantVisibilityFQN_05() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\TestInterface::IMPLICIT_PUBLIC_I^NTERFACE_CONST;", "const ^IMPLICIT_PUBLIC_INTERFACE_CONST = 1;");
    }

    public void testClassConstantVisibilityFQN_06() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\TestInterface::PUBLIC_INTERFA^CE_CONST;", "public const ^PUBLIC_INTERFACE_CONST = 0;");
    }

    public void testClassConstantVisibilityFQN_07() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\TestInterfaceImpl::IMPLICIT_PU^BLIC_INTERFACE_CONST;", "const ^IMPLICIT_PUBLIC_INTERFACE_CONST = 1;");
    }

    public void testClassConstantVisibilityFQN_08() throws Exception {
        checkDeclaration(getTestPath(), "\\Test\\Sub\\TestInterfaceImpl::PUBLIC_INTER^FACE_CONST;", "public const ^PUBLIC_INTERFACE_CONST = 0;");
    }

}
