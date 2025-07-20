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

public class ModifiersCheckHintErrorTest extends PHPHintsTestBase {

    public ModifiersCheckHintErrorTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "ModifiersCheckHintError/";
    }

    public void testModifiersCheckHint() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testModifiersCheckHint.php");
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

    public void testTraitMethods_01() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testTraitMethods_01.php");
    }

    public void testTraitMethodsFix_01() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testTraitMethodsFix_01.php", "abstract public function testAbstract^WithBody() {", "Remove body of the method");
    }

    public void testReadonlyProperties_01() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testReadonlyProperties_01.php");
    }

    public void testClassModifiers_01() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testClassModifiers_01.php");
    }

    public void testClassModifiersFix_01a() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "abstract final class Abstra^ctFinalClass {}", "Remove modifier: abstract");
    }

    public void testClassModifiersFix_01b() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "abstract final class Abstra^ctFinalClass {}", "Remove modifier: final");
    }

    public void testClassModifiersFix_02a() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "final abstract class FinalAbstrac^tClass {}", "Remove modifier: abstract");
    }

    public void testClassModifiersFix_02b() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "final abstract class FinalAbstrac^tClass {}", "Remove modifier: final");
    }

    public void testClassModifiersFix_03() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "readonly read^only class DuplicatedReadonlyClass {}", "Remove modifier: readonly");
    }

    public void testClassModifiersFix_04() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "final fi^nal class DuplicatedFinalClass {}", "Remove modifier: final");
    }

    public void testClassModifiersFix_05() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "abstract abst^ract class DuplicatedAbstractClass {}", "Remove modifier: abstract");
    }

    public void testClassModifiersFix_06a() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "final readonly fin^al readonly class DuplicatedModifiersClass {}", "Remove modifier: final");
    }

    public void testClassModifiersFix_06b() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testClassModifiersFix.php", "final readonly final rea^donly class DuplicatedModifiersClass {}", "Remove modifier: readonly");
    }

    public void testReadonlyClasses_01() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testReadonlyClasses_01.php");
    }

    public void testReadonlyClassesFix_01() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testReadonlyClassesFix.php", "readonly class Readonly^ChildClass extends NonReadonlyParentClass implements Iface {}", "Remove modifier: readonly");
    }

    public void testReadonlyClassesFix_02() throws Exception {
        applyHint(new ModifiersCheckHintError(), "testReadonlyClassesFix.php", "class NonReadonlyChildC^lass extends ReadonlyParentClass {}", "Add modifier: readonly");
    }

    // asymmetric visibility
    public void testAsymmetricVisibilityClass() throws Exception {
        checkHints();
    }

    public void testAsymmetricVisibilityClassConstructorPropertyPromotion() throws Exception {
        checkHints();
    }

    public void testAsymmetricVisibilityTrait() throws Exception {
        checkHints();
    }

    public void testAsymmetricVisibilityTraitConstructorPropertyPromotion() throws Exception {
        checkHints();
    }

    public void testAsymmetricVisibilityAnonClass() throws Exception {
        checkHints();
    }

    public void testAsymmetricVisibilityAnonClassConstructorPropertyPromotion() throws Exception {
        checkHints();
    }

    // const class
    public void testConstModifiersClassUnsupported_PHP80() throws Exception {
        checkHints("testConstModifiersClassNoError.php", PhpVersion.PHP_80);
    }

    public void testConstModifiersClassFinalPrivate_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersClassMultipleInvalid_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersClassNoError_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix01a() throws Exception {
        applyHint("    final private const PRIV^ATE_CONST1 = \"foo\"; // error1", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix01b() throws Exception {
        applyHint("    final private const PRIV^ATE_CONST1 = \"foo\"; // error1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix02a() throws Exception {
        applyHint("    private final const PRIVATE^_CONST2 = \"foo\"; // error2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix02b() throws Exception {
        applyHint("    private final const PRIVATE^_CONST2 = \"foo\"; // error2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix03a() throws Exception {
        applyHint("    final private const PRIVATE_^CONST3 = \"foo\", PRIVATE_CONST4 = \"foo\"; // error3", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix03b() throws Exception {
        applyHint("    final private const PRIVATE_^CONST3 = \"foo\", PRIVATE_CONST4 = \"foo\"; // error3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix04a() throws Exception {
        applyHint("    private final const PRIVATE_C^ONST5 = \"foo\", PRIVATE_CONST6 = \"foo\"; // error4", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix04b() throws Exception {
        applyHint("    private final const PRIVATE_C^ONST5 = \"foo\", PRIVATE_CONST6 = \"foo\"; // error4", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix05a() throws Exception {
        applyHint("    final private const string|int PRIVATE_CON^ST_TYPE_1 = \"foo\"; // error5", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix05b() throws Exception {
        applyHint("    final private const string|int PRIVATE_CON^ST_TYPE_1 = \"foo\"; // error5", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix06a() throws Exception {
        applyHint("    private final const string|int PRIVATE_CONST^_TYPE_2 = \"foo\"; // error6", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix06b() throws Exception {
        applyHint("    private final const string|int PRIVATE_CONST^_TYPE_2 = \"foo\"; // error6", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix07a() throws Exception {
        applyHint("    final private const string|int PRIVATE_CON^ST_TYPE_3 = \"foo\", PRIVATE_CONST_TYPE_4 = \"foo\"; // error7", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix07b() throws Exception {
        applyHint("    final private const string|int PRIVATE_CON^ST_TYPE_3 = \"foo\", PRIVATE_CONST_TYPE_4 = \"foo\"; // error7", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix08a() throws Exception {
        applyHint("    private final const string|int PRIVATE_CON^ST_TYPE_5 = \"foo\", PRIVATE_CONST_TYPE_6 = \"foo\"; // error8", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassFinalPrivate_PHP84_Fix08b() throws Exception {
        applyHint("    private final const string|int PRIVATE_CON^ST_TYPE_5 = \"foo\", PRIVATE_CONST_TYPE_6 = \"foo\"; // error8", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix01a() throws Exception {
        applyHint("    private public const int|string INVAL^ID_PRIVATE_PUBLIC = 1; // error1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix01b() throws Exception {
        applyHint("    private public const int|string INVAL^ID_PRIVATE_PUBLIC = 1; // error1", "Remove modifier: public", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix02a() throws Exception {
        applyHint("    static private const int INVALI^D_STATIC = 1; // error2", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix03a() throws Exception {
        applyHint("    abstract const int INVALID^_ABSTRACT = 1; // error3", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix04a() throws Exception {
        applyHint("    public(set) const int INVALID_SET_VIS^IBILITY = 1, INVALID_SET_VISIBILITY2 = 1; // error4", "Remove modifier: public(set)", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix05a() throws Exception {
        applyHint("    readonly const INVAL^ID_READONLY = 1; // error5", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix06a() throws Exception {
        applyHint("    public public const INVALID^_MULTIPLE1 = 1; // error6", "Remove modifier: public", PhpVersion.PHP_84);
    }

    public void testConstModifiersClassMultipleInvalid_PHP84_Fix07a() throws Exception {
        applyHint("    final final public const int INVALID_MULTI^PLE2 = 1, INVALID_MULTIPLE3 = 1; // error7", "Remove modifier: final", PhpVersion.PHP_84);
    }

    // const interface
    public void testConstModifiersInterfaceUnsupported_PHP80() throws Exception {
        checkHints("testConstModifiersInterfaceNoError.php", PhpVersion.PHP_80);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersInterfaceNoError_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix01a() throws Exception {
        applyHint("    final private const PRIVATE_C^ONST1 = \"foo\"; // error1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix02a() throws Exception {
        applyHint("    private final const PRIVATE_^CONST2 = \"foo\"; // error2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix03a() throws Exception {
        applyHint("    final private const PRIVATE_^CONST3 = \"foo\", PRIVATE_CONST4 = \"foo\"; // error3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix04a() throws Exception {
        applyHint("    private final const PRIVAT^E_CONST5 = \"foo\", PRIVATE_CONST6 = \"foo\"; // error4", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix05a() throws Exception {
        applyHint("    final private const string|int PRIVAT^E_CONST_TYPE_1 = \"foo\"; // error5", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix06a() throws Exception {
        applyHint("    private final const string|int PRIVATE_^CONST_TYPE_2 = \"foo\"; // error6", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix07a() throws Exception {
        applyHint("    final private const string|int PRIVAT^E_CONST_TYPE_3 = \"foo\", PRIVATE_CONST_TYPE_4 = \"foo\"; // error7", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceFinalPrivate_PHP84_Fix08a() throws Exception {
        applyHint("    private final const string|int PRIVATE_C^ONST_TYPE_5 = \"foo\", PRIVATE_CONST_TYPE_6 = \"foo\"; // error8", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix01a() throws Exception {
        applyHint("    private public const int|string INVALID_^PRIVATE_PUBLIC = 1, INVALID_PRIVATE_PUBLIC2 = 1; // error1", "Remove modifier: ", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix02a() throws Exception {
        applyHint("    static const int INVALID^_STATIC = 1; // error2", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix03a() throws Exception {
        applyHint("    abstract const int INVALID_AB^STRACT = 1; // error3", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix04a() throws Exception {
        applyHint("    public(set) const int INVALI^D_SET_VISIBILITY = 1; // error4", "Remove modifier: public(set)", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix05a() throws Exception {
        applyHint("    readonly const INVALID_RE^ADONLY = 1; // error5", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix06a() throws Exception {
        applyHint("    public public const INVALID_MUL^TIPLE1 = 1; // error6", "Remove modifier: public", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix07a() throws Exception {
        applyHint("    final final public const INVALID_MULTIP^LE2 = 1, INVALID_MULTIPLE3 = 1; // error7", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix08a() throws Exception {
        applyHint("    private const int|string INVAL^ID_PRIVATE = 1; // error8", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testConstModifiersInterfaceMultipleInvalid_PHP84_Fix09a() throws Exception {
        applyHint("    protected const int|string INVALID^_PROTECTED = 1; // error9", "Remove modifier: protected", PhpVersion.PHP_84);
    }

    // const trait
    public void testConstModifiersTraitUnsupported_PHP80() throws Exception {
        checkHints("testConstModifiersTraitNoError.php", PhpVersion.PHP_80);
    }

    public void testConstModifiersTraitFinalPrivate_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersTraitMultipleInvalid_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersTraitNoError_PHP82() throws Exception {
        checkHints(PhpVersion.PHP_82);
    }

    public void testConstModifiersTraitFinalPrivate_PHP82() throws Exception {
        checkHints(PhpVersion.PHP_82);
    }

    public void testConstModifiersTraitMultipleInvalid_PHP82() throws Exception {
        checkHints(PhpVersion.PHP_82);
    }

    public void testConstModifiersTraitNoError_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersTraitFinalPrivate_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersTraitMultipleInvalid_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    // const enum
    public void testConstModifiersEnumUnsupported_PHP80() throws Exception {
        checkHints("testConstModifiersEnumNoError.php", PhpVersion.PHP_80);
    }

    public void testConstModifiersEnumFinalPrivate_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersEnumMultipleInvalid_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersEnumNoError_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersEnumFinalPrivate_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersEnumMultipleInvalid_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    // const anonymous class
    public void testConstModifiersAnonClassUnsupported_PHP80() throws Exception {
        checkHints("testConstModifiersAnonClassNoError.php", PhpVersion.PHP_80);
    }

    public void testConstModifiersAnonClassFinalPrivate_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersAnonClassMultipleInvalid_PHP80() throws Exception {
        checkHints(PhpVersion.PHP_80);
    }

    public void testConstModifiersAnonClassNoError_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersAnonClassFinalPrivate_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testConstModifiersAnonClassMultipleInvalid_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    // method class
    public void testMethodModifiersClassNoHint_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersClassFinalPrivate_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersClassFinalPrivate_Fix01a() throws Exception {
        applyHint("    final private function finalPrivate^ClassMethod(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersClassFinalPrivate_Fix02a() throws Exception {
        applyHint("    final private static function finalPrivateStaticClassMe^thod(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersClassFinalAbstract_01() throws Exception {
        checkHints("testMethodModifiersClassFinalAbstract.php");
    }

    public void testMethodModifiersClassFinalAbstract_Fix01a() throws Exception {
        applyHint("    final abstract function finalAbstractClass^Method(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersClassFinalAbstract_Fix01b() throws Exception {
        applyHint("    final abstract function finalAbstractClass^Method(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersClassFinalAbstract_Fix02a() throws Exception {
        applyHint("    final abstract public function finalAbstractP^ublicClassMethod(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersClassFinalAbstract_Fix02b() throws Exception {
        applyHint("    final abstract public function finalAbstractP^ublicClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersClassFinalAbstract_Fix03a() throws Exception {
        applyHint("    final abstract protected function finalAbs^tractProtectedClassMethod(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersClassFinalAbstract_Fix03b() throws Exception {
        applyHint("    final abstract protected function finalAbs^tractProtectedClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersClassFinalAbstract_Fix04a() throws Exception {
        applyHint("    final abstract private function finalAbstractP^rivateClassMethod(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersClassFinalAbstract_Fix04b() throws Exception {
        applyHint("    final abstract private function finalAbstractP^rivateClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersClassAbstract_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersClassAbstract_Fix01a() throws Exception {
        applyHint("    abstract function abstra^ctClassMethod(): void {} // method error class", "Remove body of the method");
    }

    public void testMethodModifiersClassAbstract_Fix02a() throws Exception {
        applyHint("    abstract public function abstractPublicCla^ssMethod(): void {} // method error class", "Remove body of the method");
    }

    public void testMethodModifiersClassAbstract_Fix03a() throws Exception {
        applyHint("    abstract protected function abstractProtec^tedClassMethod(): void {} // method error class", "Remove body of the method");
    }

    public void testMethodModifiersClassAbstract_Fix04a() throws Exception {
        applyHint("    abstract private function abstractPrivateCl^assMethod(): void {} // method error class", "Remove body of the method");
    }

    public void testMethodModifiersClassAbstract_Fix05a() throws Exception {
        applyHint("    abstract private function abstractPrivateClas^sMethod2(): void; // method error class", "Remove modifier: private");
    }

    public void testMethodModifiersClassReadonlySetVisibility_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersClassReadonlySetVisibility_Fix01a() throws Exception {
        applyHint("    readonly function readonlyClas^sMethod(): void {} // method error class", "Remove modifier: readonly");
    }

    public void testMethodModifiersClassReadonlySetVisibility_Fix02a() throws Exception {
        applyHint("    public(set) function publicSetC^lassMethod(): void {} // method error class", "Remove modifier: public(set)");
    }

    public void testMethodModifiersClassReadonlySetVisibility_Fix03a() throws Exception {
        applyHint("    private(set) function privateSetClas^sMethod(): void {} // method error class", "Remove modifier: private(set)");
    }

    public void testMethodModifiersClassReadonlySetVisibility_Fix04a() throws Exception {
        applyHint("    protected(set) function protectedSetCl^assMethod(): void {} // method error class", "Remove modifier: protected(set)");
    }

    public void testMethodModifiersClassReadonlySetVisibility_Fix05a() throws Exception {
        applyHint("    public public(set) function publicPublicSe^tClassMethod(): void {} // method error class", "Remove modifier: public(set)");
    }

    public void testMethodModifiersClassReadonlySetVisibility_Fix06a() throws Exception {
        applyHint("    public private(set) function publicPrivat^eSetClassMethod(): void {} // method error class", "Remove modifier: private(set)");
    }

    public void testMethodModifiersClassReadonlySetVisibility_Fix07a() throws Exception {
        applyHint("    public protected(set) function publicPr^otectedSetClassMethod(): void {} // method error class", "Remove modifier: protected(set)");
    }

    public void testMethodModifiersClassMultiple_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersClassMultiple_Fix01a() throws Exception {
        applyHint("    final final function finalFinalClas^sMethod(): void {", "Remove modifier: ");
    }

    public void testMethodModifiersClassMultiple_Fix02a() throws Exception {
        applyHint("    public public function publicPubli^cClassMethod(): void {", "Remove modifier: ");
    }

    public void testMethodModifiersClassMultiple_Fix03a() throws Exception {
        applyHint("    public private function publicPrivate^ClassMethod(): void {", "Remove modifier: public");
    }

    public void testMethodModifiersClassMultiple_Fix03b() throws Exception {
        applyHint("    public private function publicPrivate^ClassMethod(): void {", "Remove modifier: private");
    }

    public void testMethodModifiersClassMultiple_Fix04a() throws Exception {
        applyHint("    public protected function publicProtect^edClassMethod(): void {", "Remove modifier: public");
    }

    public void testMethodModifiersClassMultiple_Fix04b() throws Exception {
        applyHint("    public protected function publicProtect^edClassMethod(): void {", "Remove modifier: protected");
    }

    public void testMethodModifiersClassMultiple_Fix05a() throws Exception {
        applyHint("    static static function staticStaticC^lassMethod(): void {", "Remove modifier: ");
    }

    // method interface
    public void testMethodModifiersInterfaceNoError_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersInterfaceAbstract_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersInterfaceAbstract_Fix01a() throws Exception {
        applyHint("    abstract function abstractImplicitPubli^cMethodError(): void;", "Remove modifier: abstract");
    }

    public void testMethodModifiersInterfaceAbstract_Fix02a() throws Exception {
        applyHint("    abstract static function abstractImpli^citPublicStaticMethodError(): void;", "Remove modifier: abstract");
    }

    public void testMethodModifiersInterfaceAbstract_Fix03a() throws Exception {
        applyHint("    abstract public function abstractPubli^cMethodError(): void;", "Remove modifier: abstract");
    }

    public void testMethodModifiersInterfaceAbstract_Fix04a() throws Exception {
        applyHint("    abstract public static function abstractPublicSta^ticMethodError(): void;", "Remove modifier: abstract");
    }

    public void testMethodModifiersInterfaceFinal_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersInterfaceFinal_Fix01a() throws Exception {
        applyHint("    final function finalImplicitPub^licMethodError(): void;", "Remove modifier: final");
    }

    public void testMethodModifiersInterfaceFinal_Fix02a() throws Exception {
        applyHint("    final static function fina^lImplicitPublicStaticMethodError(): void;", "Remove modifier: final");
    }

    public void testMethodModifiersInterfaceFinal_Fix03a() throws Exception {
        applyHint("    final public function finalPubli^cMethodError(): void;", "Remove modifier: final");
    }

    public void testMethodModifiersInterfaceFinal_Fix04a() throws Exception {
        applyHint("    final public static function finalPubli^cStaticMethodError(): void;", "Remove modifier: final");
    }

    public void testMethodModifiersInterfaceMultiple_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersInterfaceMultiple_Fix01a() throws Exception {
        applyHint("    public public function publi^cPublicMethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceMultiple_Fix02a() throws Exception {
        applyHint("    public private function publicPrivate^MethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceMultiple_Fix03a() throws Exception {
        applyHint("    protected private function protectedP^rivateMethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceReadonly_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersInterfaceReadonly_Fix01a() throws Exception {
        applyHint("    readonly function readonlyImplicitPublic^MethodError(): void;", "Remove modifier: readonly");
    }

    public void testMethodModifiersInterfaceReadonly_Fix02a() throws Exception {
        applyHint("    readonly static function readonlyImplicitP^ublicStaticMethodError(): void;", "Remove modifier: readonly");
    }

    public void testMethodModifiersInterfaceReadonly_Fix03a() throws Exception {
        applyHint("    readonly public function readonlyPublicM^ethodError(): void;", "Remove modifier: readonly");
    }

    public void testMethodModifiersInterfaceReadonly_Fix04a() throws Exception {
        applyHint("    readonly public static function readonlyPubli^cStaticMethodError(): void;", "Remove modifier: readonly");
    }

    public void testMethodModifiersInterfaceSetVisibility_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersInterfaceSetVisibility_Fix01a() throws Exception {
        applyHint("    private(set) function privateSetImplicitPublic^MethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceSetVisibility_Fix02a() throws Exception {
        applyHint("    protected(set) static function protectedSe^tImplicitPublicStaticMethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceSetVisibility_Fix03a() throws Exception {
        applyHint("    public(set) public function publicSetPub^licMethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceSetVisibility_Fix04a() throws Exception {
        applyHint("    private(set) public static function privateSetPublic^StaticMethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceVisibility_01() throws Exception {
        checkHints("testMethodModifiersInterfaceVisibility.php");
    }

    public void testMethodModifiersInterfaceVisibility_Fix01a() throws Exception {
        applyHint("    private function privateM^ethodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceVisibility_Fix02a() throws Exception {
        applyHint("    protected function protectedMet^hodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceVisibility_Fix03a() throws Exception {
        applyHint("    private static function privateStaticMe^thodError(): void;", "Remove modifier: ");
    }

    public void testMethodModifiersInterfaceVisibility_Fix04a() throws Exception {
        applyHint("    protected static function prot^ectedStaticMethodError(): void;", "Remove modifier: ");
    }

    // method trait
    public void testMethodModifiersTraitAbstract_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersTraitAbstract_Fix01a() throws Exception {
        applyHint("    abstract function abstractTra^itMethodError(): void {} // method error trait", "Remove body of the method:");
    }

    public void testMethodModifiersTraitAbstract_Fix02a() throws Exception {
        applyHint("    abstract public function abstractPubli^cTraitMethodError(): void {} // method error trait", "Remove body of the method:");
    }

    public void testMethodModifiersTraitAbstract_Fix03a() throws Exception {
        applyHint("    abstract protected function abstractPr^otectedTraitMethodError(): void {} // method error trait", "Remove body of the method:");
    }

    public void testMethodModifiersTraitAbstract_Fix04a() throws Exception {
        applyHint("    abstract private function abstractPrivate^TraitMethodError(): void {} // method error trait", "Remove body of the method:");
    }

    public void testMethodModifiersTraitFinalAbstract_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersTraitFinalAbstract_Fix01a() throws Exception {
        applyHint("    final abstract function finalAbstractTrai^tMethodError(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersTraitFinalAbstract_Fix01b() throws Exception {
        applyHint("    final abstract function finalAbstractTrai^tMethodError(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersTraitFinalAbstract_Fix02a() throws Exception {
        applyHint("    final abstract public function finalAbstract^PublicTraitMethodError(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersTraitFinalAbstract_Fix02b() throws Exception {
        applyHint("    final abstract public function finalAbstract^PublicTraitMethodError(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersTraitFinalAbstract_Fix03a() throws Exception {
        applyHint("    final abstract protected function finalAbstractPr^otectedTraitMethodError(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersTraitFinalAbstract_Fix03b() throws Exception {
        applyHint("    final abstract protected function finalAbstractPr^otectedTraitMethodError(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersTraitFinalAbstract_Fix04a() throws Exception {
        applyHint("    final abstract private function finalAbs^tractPrivateTraitMethodError(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersTraitFinalAbstract_Fix04b() throws Exception {
        applyHint("    final abstract private function finalAbs^tractPrivateTraitMethodError(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersTraitFinalPrivate_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersTraitFinalPrivate_Fix01a() throws Exception {
        applyHint("    final private function finalPrivateTra^itMethodError(): void {} // warning trait", "Remove modifier: final");
    }

    public void testMethodModifiersTraitFinalPrivate_Fix02a() throws Exception {
        applyHint("    final private static function finalPrivateStati^cTraitMethodError(): void {} // warning trait", "Remove modifier: final");
    }

    public void testMethodModifiersTraitMultiple_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersTraitMultiple_Fix01a() throws Exception {
        applyHint("    final final function finalFinal^TraitMethodError(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersTraitMultiple_Fix02a() throws Exception {
        applyHint("    public public function publicPubl^icTraitMethodError(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersTraitMultiple_Fix03a() throws Exception {
        applyHint("    public private function publicPrivate^TraitMethodError(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersTraitMultiple_Fix03b() throws Exception {
        applyHint("    public private function publicPrivateTrait^MethodError(): void {}", "Remove modifier: private");
    }

    public void testMethodModifiersTraitMultiple_Fix04a() throws Exception {
        applyHint("    public protected function publicPr^otectedTraitMethodError(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersTraitMultiple_Fix04b() throws Exception {
        applyHint("    public protected function publicProtectedT^raitMethodError(): void {}", "Remove modifier: protected");
    }

    public void testMethodModifiersTraitMultiple_Fix05a() throws Exception {
        applyHint("    static static function staticStaticTraitM^ethodError(): void {}", "Remove modifier: static");
    }

    public void testMethodModifiersTraitNoError_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersTraitReadonlySetVisibility_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersTraitReadonlySetVisibility_Fix01a() throws Exception {
        applyHint("    readonly function readonly^TraitMethodError(): void {}", "Remove modifier: readonly");
    }

    public void testMethodModifiersTraitReadonlySetVisibility_Fix02a() throws Exception {
        applyHint("    public(set) function publicSetT^raitMethodError(): void {}", "Remove modifier: public(set)");
    }

    public void testMethodModifiersTraitReadonlySetVisibility_Fix03a() throws Exception {
        applyHint("    private(set) function privateSetTra^itMethodError(): void {}", "Remove modifier: private(set)");
    }

    public void testMethodModifiersTraitReadonlySetVisibility_Fix04a() throws Exception {
        applyHint("    protected(set) function protected^SetTraitMethodError(): void {}", "Remove modifier: protected(set)");
    }

    public void testMethodModifiersTraitReadonlySetVisibility_Fix05a() throws Exception {
        applyHint("    public public(set) function pub^licPublicSetTraitMethodError(): void {}", "Remove modifier: public(set)");
    }

    public void testMethodModifiersTraitReadonlySetVisibility_Fix06a() throws Exception {
        applyHint("    public private(set) function publicPrivateSet^TraitMethodError(): void {}", "Remove modifier: private(set)");
    }

    public void testMethodModifiersTraitReadonlySetVisibility_Fix07a() throws Exception {
        applyHint("    public protected(set) static function publicProtectedSe^tStaticTraitMethodError(): void {}", "Remove modifier: protected(set)");
    }

    // method enum
    public void testMethodModifiersEnumAbstract_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersEnumAbstract_Fix01a() throws Exception {
        applyHint("    abstract function abstractImplicitPublic^BodyEnumMethod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumAbstract_Fix02a() throws Exception {
        applyHint("    abstract public function abstractPublicBo^dyEnumMethod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumAbstract_Fix03a() throws Exception {
        applyHint("    abstract protected function abstractProt^ectedBodyEnumMethod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumAbstract_Fix04a() throws Exception {
        applyHint("    abstract private function abstract^PrivateBodyEnumMethod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumFinalAbstract_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersEnumFinalAbstract_Fix01a() throws Exception {
        applyHint("    final abstract function finalAbstr^actEnumMethod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumFinalAbstract_Fix02a() throws Exception {
        applyHint("    final abstract public function finalAbstract^PublicEnumMethod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumFinalAbstract_Fix03a() throws Exception {
        applyHint("    final abstract protected function finalAbstract^ProtectedEnumMethod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumFinalAbstract_Fix04a() throws Exception {
        applyHint("    final abstract private function finalAbstractPrivateEnumMe^thod(): void {} // method error enum", "Remove modifier: abstract");
    }

    public void testMethodModifiersEnumFinalPrivate_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersEnumFinalPrivate_Fix01a() throws Exception {
        applyHint("    final private function finalPrivate^EnumMethod(): void {} // warning enum", "Remove modifier: final");
    }

    public void testMethodModifiersEnumFinalPrivate_Fix02a() throws Exception {
        applyHint("    final private static function finalPr^ivateStaticEnumMethod(): void {} // warning enum", "Remove modifier: final");
    }

    public void testMethodModifiersEnumMultiple_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersEnumMultiple_Fix01a() throws Exception {
        applyHint("    final final function finalFin^alEnumMethod(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersEnumMultiple_Fix02a() throws Exception {
        applyHint("    public public function publicPubli^cEnumMethod(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersEnumMultiple_Fix03a() throws Exception {
        applyHint("    public private function publicPriva^teEnumMethod(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersEnumMultiple_Fix03b() throws Exception {
        applyHint("    public private function publicPrivat^eEnumMethod(): void {}", "Remove modifier: private");
    }

    public void testMethodModifiersEnumMultiple_Fix04a() throws Exception {
        applyHint("    public protected function publicProte^ctedEnumMethod(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersEnumMultiple_Fix04b() throws Exception {
        applyHint("    public protected function publicPro^tectedEnumMethod(): void {}", "Remove modifier: protected");
    }

    public void testMethodModifiersEnumMultiple_Fix05a() throws Exception {
        applyHint("    static static function staticStatic^EnumMethod(): void {}", "Remove modifier: static");
    }

    public void testMethodModifiersEnumNoError_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersEnumReadonlySetVisibility_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersEnumReadonlySetVisibility_Fix01a() throws Exception {
        applyHint("    readonly function readonlyEn^umMethod(): void {}", "Remove modifier: readonly");
    }

    public void testMethodModifiersEnumReadonlySetVisibility_Fix02a() throws Exception {
        applyHint("    public(set) function publicSetEn^umMethod(): void {}", "Remove modifier: public(set)");
    }

    public void testMethodModifiersEnumReadonlySetVisibility_Fix03a() throws Exception {
        applyHint("    private(set) function privateSetEnum^Method(): void {}", "Remove modifier: private(set)");
    }

    public void testMethodModifiersEnumReadonlySetVisibility_Fix04a() throws Exception {
        applyHint("    protected(set) function protectedSetEn^umMethod(): void {}", "Remove modifier: protected(set)");
    }

    public void testMethodModifiersEnumReadonlySetVisibility_Fix05a() throws Exception {
        applyHint("    public public(set) function publicPu^blicSetEnumMethod(): void {}", "Remove modifier: public(set)");
    }

    public void testMethodModifiersEnumReadonlySetVisibility_Fix06a() throws Exception {
        applyHint("    public private(set) function publicPrivate^SetEnumMethod(): void {}", "Remove modifier: private(set)");
    }

    public void testMethodModifiersEnumReadonlySetVisibility_Fix07a() throws Exception {
        applyHint("    public protected(set) function publicProtecte^dSetEnumMethod(): void {}", "Remove modifier: protected(set)");
    }

    // method anonymous class
    public void testMethodModifiersAnonClassAbstract_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersAnonClassAbstract_Fix01a() throws Exception {
        applyHint("    final abstract function finalAbstractAno^nClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassAbstract_Fix02a() throws Exception {
        applyHint("    final abstract public function finalAbstractPu^blicAnonClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassAbstract_Fix03a() throws Exception {
        applyHint("    final abstract protected function finalAbstr^actProtectedAnonClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassAbstract_Fix04a() throws Exception {
        applyHint("    final abstract private function finalAbstr^actPrivateAnonClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassAbstract_Fix05a() throws Exception {
        applyHint("    abstract function abstractBodyAnonC^lassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassAbstract_Fix06a() throws Exception {
        applyHint("    abstract public function abstractPublicB^odyAnonClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassAbstract_Fix07a() throws Exception {
        applyHint("    protected abstract function abstractProtect^edBodyAnonClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassAbstract_Fix08a() throws Exception {
        applyHint("    abstract private function abstractPrivateBodyAn^onClassMethod(): void {}", "Remove modifier: abstract");
    }

    public void testMethodModifiersAnonClassFinalPrivate_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersAnonClassFinalPrivate_Fix01a() throws Exception {
        applyHint("    final private function finalPrivateAno^nClassMethod(): void {} // warning anon class", "Remove modifier: final");
    }

    public void testMethodModifiersAnonClassFinalPrivate_Fix02a() throws Exception {
        applyHint("    final private static function finalPrivateStaticAno^nClassMethod(): void {} // warning anon class", "Remove modifier: final");
    }

    public void testMethodModifiersAnonClassMultiple_01() throws Exception {
        checkHints("testMethodModifiersAnonClassMultiple.php");
    }

    public void testMethodModifiersAnonClassMultiple_Fix01a() throws Exception {
        applyHint("    final final function finalFinalAnon^ClassMethod(): void {}", "Remove modifier: final");
    }

    public void testMethodModifiersAnonClassMultiple_Fix02a() throws Exception {
        applyHint("    public public function publicPublicAn^onClassMethod(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersAnonClassMultiple_Fix03a() throws Exception {
        applyHint("    public private function publicPrivateAnonC^lassMethod(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersAnonClassMultiple_Fix03b() throws Exception {
        applyHint("    public private function publicPrivateAnon^ClassMethod(): void {}", "Remove modifier: private");
    }

    public void testMethodModifiersAnonClassMultiple_Fix04a() throws Exception {
        applyHint("    public protected function publicProtectedAnon^ClassMethod(): void {}", "Remove modifier: public");
    }

    public void testMethodModifiersAnonClassMultiple_Fix04b() throws Exception {
        applyHint("    public protected function publicProtecte^dAnonClassMethod(): void {}", "Remove modifier: protected");
    }

    public void testMethodModifiersAnonClassMultiple_Fix05a() throws Exception {
        applyHint("    public static static function staticStaticAn^onClassMethod(): void {}", "Remove modifier: static");
    }

    public void testMethodModifiersAnonClassNoError_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_01() throws Exception {
        checkHints();
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_Fix01a() throws Exception {
        applyHint("    public readonly function rea^donlyAnonClassMethod(): void {}", "Remove modifier: readonly");
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_Fix02a() throws Exception {
        applyHint("    public(set) function publ^icSetAnonClassMethod(): void {}", "Remove modifier: public(set)");
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_Fix03a() throws Exception {
        applyHint("    private(set) function privateSetA^nonClassMethod(): void {}", "Remove modifier: private(set)");
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_Fix04a() throws Exception {
        applyHint("    protected(set) function protectedSetAnon^ClassMethod(): void {}", "Remove modifier: protected(set)");
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_Fix05a() throws Exception {
        applyHint("    public public(set) function publicPublicSetAn^onClassMethod(): void {}", "Remove modifier: public(set)");
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_Fix06a() throws Exception {
        applyHint("    public private(set) static function publicPri^vateSetAnonClassMethod(): void {}", "Remove modifier: private(set)");
    }

    public void testMethodModifiersAnonClassReadonlySetVisibility_Fix07a() throws Exception {
        applyHint("    public protected(set) function publicProtecte^dSetAnonClassMethod(): void {}", "Remove modifier: protected(set)");
    }

    // final field class
    public void testFinalFieldModifiersClassAbstract_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassAbstract_FixPHP84_01a() throws Exception {
        applyHint("    final abstract string $finalAbstra^ct = \"final static\"; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassAbstract_FixPHP84_02a() throws Exception {
        applyHint("    abstract final string $abstrac^tFinal = \"final static\"; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassAbstractReadonly_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassAbstractReadonly.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassAbstractReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final abstract readonly $fina^lAbstractReadonly1; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassAbstractReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final abstract readonly int $finalAbs^tractReadonly2; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassSetVisibility_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassSetVisibility.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStatic_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassStatic.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticAbstract_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassStaticAbstract.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticAbstract_FixPHP84_01a() throws Exception {
        applyHint("    final abstract static $finalAbstra^ctStatic1; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticAbstract_FixPHP84_02a() throws Exception {
        applyHint("    final abstract static int $finalAbstr^actStatic2; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticReadonly_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassStaticReadonly.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final static readonly $finalStati^cReadonly1; // error1", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticReadonly_FixPHP84_01b() throws Exception {
        applyHint("    final static readonly $finalStatic^Readonly1; // error1", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final static readonly string $fina^lStaticReadonly2; // error2", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticReadonly_FixPHP84_02b() throws Exception {
        applyHint("    final static readonly string $fin^alStaticReadonly2; // error2", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticSetVisibility_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassStaticSetVisibility.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticSetVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final static public(set) string $finalStatic^PublicSet2; // error2 fix1", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticSetVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final static public(set) string $finalSta^ticPublicSet2; // error2 fix1", "Remove modifier: public(set)", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassStaticVisibility_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassStaticVisibility.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibility_PHP84() throws Exception {
        checkHints("testFinalFieldModifiersClassVisibility.php", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final private string $fina^lPrivate = \"final visibility\"; // error1", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final private string $finalPr^ivate = \"final visibility\"; // error1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibility_FixPHP84_02a() throws Exception {
        applyHint("    private final string $priva^teFinal = \"final visibility\"; // error2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibility_FixPHP84_02b() throws Exception {
        applyHint("    private final string $privat^eFinal = \"final visibility\"; // error2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final private public(set) string|int $finalPrivat^ePublicSet1; // error3 fix1 PHP Fatal error:  Property cannot be both final and private", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final private public(set) string|int $finalPriva^tePublicSet1; // error3 fix1 PHP Fatal error:  Property cannot be both final and private", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_02a() throws Exception {
        applyHint("    final private private(set) string|int $finalPri^vatePrivateSet1; // error4 fix2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_02b() throws Exception {
        applyHint("    final private private(set) string|int $finalPrivat^ePrivateSet1; // error4 fix2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_03a() throws Exception {
        applyHint("    final private protected(set) string|int $finalPrivate^ProtectedSet1; // error5 fix3", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_03b() throws Exception {
        applyHint("    final private protected(set) string|int $finalPrivatePr^otectedSet1; // error5 fix3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_04a() throws Exception {
        applyHint("    final public(set) private string|int $finalPub^licSetPrivate1; // error8 fix4", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_04b() throws Exception {
        applyHint("    final public(set) private string|int $finalPublicSet^Private1; // error8 fix4", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_05a() throws Exception {
        applyHint("    final private(set) private string|int $finalPrivate^SetPrivate1; // error10 fix5", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_05b() throws Exception {
        applyHint("    final private(set) private string|int $finalPrivat^eSetPrivate1; // error10 fix5", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_06a() throws Exception {
        applyHint("    final protected(set) private string|int $finalProte^ctedSetPrivate1; // error11 fix6", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibility_FixPHP84_06b() throws Exception {
        applyHint("    final protected(set) private string|int $finalProtected^SetPrivate1; // error11 fix6", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibilityReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibilityReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final private public(set) readonly string|int $finalPrivateP^ublicSetReadonly; // error3 fix1", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibilityReadonly_FixPHP84_01b() throws Exception {
        applyHint("    final private public(set) readonly string|int $finalPrivatePubl^icSetReadonly; // error3 fix1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibilityReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final private protected(set) readonly string|int $finalPrivateProt^ectedSetReadonly; // error4 fix2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibilityReadonly_FixPHP84_02b() throws Exception {
        applyHint("    final private protected(set) readonly string|int $finalPriva^teProtectedSetReadonly; // error4 fix2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibilityReadonly_FixPHP84_03a() throws Exception {
        applyHint("    final private private(set) readonly string|int $finalPri^vatePrivateSetReadonly; // error5 fix3", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClassVisibilitySetVisibilityReadonly_FixPHP84_03b() throws Exception {
        applyHint("    final private private(set) readonly string|int $finalPrivatePri^vateSetReadonly; // error5 fix3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersClass_PHP83() throws Exception {
        checkHints(PhpVersion.PHP_83);
    }

    // final field trait
    public void testFinalFieldModifiersTraitAbstract_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitAbstract_FixPHP84_01a() throws Exception {
        applyHint("    final abstract string $finalAbstra^ct = \"final static\"; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitAbstract_FixPHP84_02a() throws Exception {
        applyHint("    abstract final string $abstrac^tFinal = \"final static\"; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitAbstractReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitAbstractReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final abstract readonly $fina^lAbstractReadonly1; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitAbstractReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final abstract readonly int $finalAbs^tractReadonly2; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitSetVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStatic_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticAbstract_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticAbstract_FixPHP84_01a() throws Exception {
        applyHint("    final abstract static $finalAbstra^ctStatic1; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticAbstract_FixPHP84_02a() throws Exception {
        applyHint("    final abstract static int $finalAbstr^actStatic2; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final static readonly $finalStati^cReadonly1; // error1", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticReadonly_FixPHP84_01b() throws Exception {
        applyHint("    final static readonly $finalStatic^Readonly1; // error1", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final static readonly string $fina^lStaticReadonly2; // error2", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticReadonly_FixPHP84_02b() throws Exception {
        applyHint("    final static readonly string $fin^alStaticReadonly2; // error2", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticSetVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticSetVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final static public(set) string $finalStatic^PublicSet2; // error2 fix1", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticSetVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final static public(set) string $finalSta^ticPublicSet2; // error2 fix1", "Remove modifier: public(set)", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitStaticVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final private string $fina^lPrivate = \"final visibility\"; // error1", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final private string $finalPr^ivate = \"final visibility\"; // error1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibility_FixPHP84_02a() throws Exception {
        applyHint("    private final string $priva^teFinal = \"final visibility\"; // error2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibility_FixPHP84_02b() throws Exception {
        applyHint("    private final string $privat^eFinal = \"final visibility\"; // error2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final private public(set) string|int $finalPrivat^ePublicSet1; // error3 fix1 PHP Fatal error:  Property cannot be both final and private", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final private public(set) string|int $finalPriva^tePublicSet1; // error3 fix1 PHP Fatal error:  Property cannot be both final and private", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_02a() throws Exception {
        applyHint("    final private private(set) string|int $finalPri^vatePrivateSet1; // error4 fix2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_02b() throws Exception {
        applyHint("    final private private(set) string|int $finalPrivat^ePrivateSet1; // error4 fix2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_03a() throws Exception {
        applyHint("    final private protected(set) string|int $finalPrivate^ProtectedSet1; // error5 fix3", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_03b() throws Exception {
        applyHint("    final private protected(set) string|int $finalPrivatePr^otectedSet1; // error5 fix3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_04a() throws Exception {
        applyHint("    final public(set) private string|int $finalPub^licSetPrivate1; // error8 fix4", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_04b() throws Exception {
        applyHint("    final public(set) private string|int $finalPublicSet^Private1; // error8 fix4", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_05a() throws Exception {
        applyHint("    final private(set) private string|int $finalPrivate^SetPrivate1; // error10 fix5", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_05b() throws Exception {
        applyHint("    final private(set) private string|int $finalPrivat^eSetPrivate1; // error10 fix5", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_06a() throws Exception {
        applyHint("    final protected(set) private string|int $finalProte^ctedSetPrivate1; // error11 fix6", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibility_FixPHP84_06b() throws Exception {
        applyHint("    final protected(set) private string|int $finalProtected^SetPrivate1; // error11 fix6", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibilityReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibilityReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final private public(set) readonly string|int $finalPrivateP^ublicSetReadonly; // error3 fix1", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibilityReadonly_FixPHP84_01b() throws Exception {
        applyHint("    final private public(set) readonly string|int $finalPrivatePubl^icSetReadonly; // error3 fix1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibilityReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final private protected(set) readonly string|int $finalPrivateProt^ectedSetReadonly; // error4 fix2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibilityReadonly_FixPHP84_02b() throws Exception {
        applyHint("    final private protected(set) readonly string|int $finalPriva^teProtectedSetReadonly; // error4 fix2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibilityReadonly_FixPHP84_03a() throws Exception {
        applyHint("    final private private(set) readonly string|int $finalPri^vatePrivateSetReadonly; // error5 fix3", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTraitVisibilitySetVisibilityReadonly_FixPHP84_03b() throws Exception {
        applyHint("    final private private(set) readonly string|int $finalPrivatePri^vateSetReadonly; // error5 fix3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersTrait_PHP83() throws Exception {
        checkHints(PhpVersion.PHP_83);
    }

    // final field anonymous class
    public void testFinalFieldModifiersAnonClassAbstract_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassAbstract_FixPHP84_01a() throws Exception {
        applyHint("    final abstract string $finalAbstra^ct = \"final static\"; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassAbstract_FixPHP84_02a() throws Exception {
        applyHint("    abstract final string $abstrac^tFinal = \"final static\"; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassAbstractReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassAbstractReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final abstract readonly $fina^lAbstractReadonly1; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassAbstractReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final abstract readonly int $finalAbs^tractReadonly2; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassSetVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStatic_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticAbstract_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticAbstract_FixPHP84_01a() throws Exception {
        applyHint("    final abstract static $finalAbstra^ctStatic1; // error1", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticAbstract_FixPHP84_02a() throws Exception {
        applyHint("    final abstract static int $finalAbstr^actStatic2; // error2", "Remove modifier: abstract", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final static readonly $finalStati^cReadonly1; // error1", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticReadonly_FixPHP84_01b() throws Exception {
        applyHint("    final static readonly $finalStatic^Readonly1; // error1", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final static readonly string $fina^lStaticReadonly2; // error2", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticReadonly_FixPHP84_02b() throws Exception {
        applyHint("    final static readonly string $fin^alStaticReadonly2; // error2", "Remove modifier: readonly", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticSetVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticSetVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final static public(set) string $finalStatic^PublicSet2; // error2 fix1", "Remove modifier: static", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticSetVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final static public(set) string $finalSta^ticPublicSet2; // error2 fix1", "Remove modifier: public(set)", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassStaticVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final private string $fina^lPrivate = \"final visibility\"; // error1", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final private string $finalPr^ivate = \"final visibility\"; // error1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibility_FixPHP84_02a() throws Exception {
        applyHint("    private final string $priva^teFinal = \"final visibility\"; // error2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibility_FixPHP84_02b() throws Exception {
        applyHint("    private final string $privat^eFinal = \"final visibility\"; // error2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_01a() throws Exception {
        applyHint("    final private public(set) string|int $finalPrivat^ePublicSet1; // error3 fix1 PHP Fatal error:  Property cannot be both final and private", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_01b() throws Exception {
        applyHint("    final private public(set) string|int $finalPriva^tePublicSet1; // error3 fix1 PHP Fatal error:  Property cannot be both final and private", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_02a() throws Exception {
        applyHint("    final private private(set) string|int $finalPri^vatePrivateSet1; // error4 fix2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_02b() throws Exception {
        applyHint("    final private private(set) string|int $finalPrivat^ePrivateSet1; // error4 fix2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_03a() throws Exception {
        applyHint("    final private protected(set) string|int $finalPrivate^ProtectedSet1; // error5 fix3", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_03b() throws Exception {
        applyHint("    final private protected(set) string|int $finalPrivatePr^otectedSet1; // error5 fix3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_04a() throws Exception {
        applyHint("    final public(set) private string|int $finalPub^licSetPrivate1; // error8 fix4", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_04b() throws Exception {
        applyHint("    final public(set) private string|int $finalPublicSet^Private1; // error8 fix4", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_05a() throws Exception {
        applyHint("    final private(set) private string|int $finalPrivate^SetPrivate1; // error10 fix5", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_05b() throws Exception {
        applyHint("    final private(set) private string|int $finalPrivat^eSetPrivate1; // error10 fix5", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_06a() throws Exception {
        applyHint("    final protected(set) private string|int $finalProte^ctedSetPrivate1; // error11 fix6", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibility_FixPHP84_06b() throws Exception {
        applyHint("    final protected(set) private string|int $finalProtected^SetPrivate1; // error11 fix6", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibilityReadonly_PHP84() throws Exception {
        checkHints(PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibilityReadonly_FixPHP84_01a() throws Exception {
        applyHint("    final private public(set) readonly string|int $finalPrivateP^ublicSetReadonly; // error3 fix1", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibilityReadonly_FixPHP84_01b() throws Exception {
        applyHint("    final private public(set) readonly string|int $finalPrivatePubl^icSetReadonly; // error3 fix1", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibilityReadonly_FixPHP84_02a() throws Exception {
        applyHint("    final private protected(set) readonly string|int $finalPrivateProt^ectedSetReadonly; // error4 fix2", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibilityReadonly_FixPHP84_02b() throws Exception {
        applyHint("    final private protected(set) readonly string|int $finalPriva^teProtectedSetReadonly; // error4 fix2", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibilityReadonly_FixPHP84_03a() throws Exception {
        applyHint("    final private private(set) readonly string|int $finalPri^vatePrivateSetReadonly; // error5 fix3", "Remove modifier: final", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClassVisibilitySetVisibilityReadonly_FixPHP84_03b() throws Exception {
        applyHint("    final private private(set) readonly string|int $finalPrivatePri^vateSetReadonly; // error5 fix3", "Remove modifier: private", PhpVersion.PHP_84);
    }

    public void testFinalFieldModifiersAnonClass_PHP83() throws Exception {
        checkHints(PhpVersion.PHP_83);
    }

    public void testInterfaceInvalidMethodsWithBody() throws Exception {
        checkHints();
    }

    public void testInterfaceInvalidMethodsWithBody_Fix_01() throws Exception {
        applyHint("    function implicitP^ublicMethodWithBody() {}", "Remove body of the method: implicitPublicMethodWithBody");
    }

    public void testInterfaceInvalidMethodsWithBody_Fix_02() throws Exception {
        applyHint("    function implicitP^ublicMethodWithBodyAndReturnType(): void {}", "Remove body of the method: implicitPublicMethodWithBodyAndReturnType");
    }

    private void checkHints() throws Exception {
        checkHints(String.format("%s.php", getTestName()));
    }

    private void checkHints(PhpVersion phpVersion) throws Exception {
        checkHints(String.format("%s.php", getTestName()), phpVersion);
    }

    private void checkHints(String fileName) throws Exception {
        checkHints(new ModifiersCheckHintError(), fileName);
    }

    private void checkHints(String fileName, PhpVersion phpVersion) throws Exception {
        checkHints(new ModifiersCheckHintErrorStub(phpVersion), fileName);
    }

    private void applyHint(String caretLine, String fixDesc) throws Exception {
        applyHint(String.format("%s.php", getTestName()), caretLine, fixDesc);
    }

    private void applyHint(String caretLine, String fixDesc, PhpVersion phpVersion) throws Exception {
        applyHint(String.format("%s.php", getTestName()), caretLine, fixDesc, phpVersion);
    }

    private void applyHint(String fileName, String caretLine, String fixDesc) throws Exception {
        applyHint(new ModifiersCheckHintError(), fileName, caretLine, fixDesc);
    }

    private void applyHint(String fileName, String caretLine, String fixDesc, PhpVersion phpVersion) throws Exception {
        applyHint(new ModifiersCheckHintErrorStub(phpVersion), fileName, caretLine, fixDesc);
    }

    private static class ModifiersCheckHintErrorStub extends ModifiersCheckHintError {

        private final PhpVersion phpVersion;

        public ModifiersCheckHintErrorStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion() {
            return phpVersion;
        }
    }
}
