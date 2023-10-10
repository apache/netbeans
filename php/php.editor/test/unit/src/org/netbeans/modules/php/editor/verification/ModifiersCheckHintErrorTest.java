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

}
