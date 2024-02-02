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

public class AddOverrideAttributeHintTest extends PHPHintsTestBase {

    public AddOverrideAttributeHintTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "AddOverrideAttributeHint/";
    }

    public void testImplementsInterface_01() throws Exception {
        checkHints("testImplementsInterface.php");
    }

    public void testImplementsInterface_PHP82_01() throws Exception {
        checkHints("testImplementsInterface.php", PhpVersion.PHP_82);
    }

    public void testImplementsInterface_Fix01() throws Exception {
        applyHint("testImplementsInterface.php", "    public function interfaceMe^thod1(): void {", "Add \"#[\\Override]\" Attribute");
    }

    public void testImplementsInterface_Fix02() throws Exception {
        applyHint("testImplementsInterface.php", "    #[\\Overri^de]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testExtendsClass_01() throws Exception {
        checkHints("testExtendsClass.php");
    }

    public void testExtendsClass_PHP82_01() throws Exception {
        checkHints("testExtendsClass.php", PhpVersion.PHP_82);
    }

    public void testExtendsClass_Fix01() throws Exception {
        applyHint("testExtendsClass.php", "    public function parentMetho^d3(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClass_Fix02() throws Exception {
        applyHint("testExtendsClass.php", "    public function parentMethod^4(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClass_Fix03() throws Exception {
        applyHint("testExtendsClass.php", "    public function parentM^ethod5(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClass_Fix04() throws Exception {
        applyHint("testExtendsClass.php", "    public static function parentPublicStaticMe^thod1(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClass_Fix05() throws Exception {
        applyHint("testExtendsClass.php", "    protected static function parentProtectedStaticMet^hod1(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClass_Fix06() throws Exception {
        applyHint("testExtendsClass.php", "    protected function parentProtectedMe^thod1(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClass_Fix07() throws Exception {
        applyHint("testExtendsClass.php", "    public function interfaceMet^hod1(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClassInNamespace_01() throws Exception {
        checkHints("testExtendsClassInNamespace_01.php");
    }

    public void testExtendsClassInNamespace01_Fix01() throws Exception {
        applyHint("testExtendsClassInNamespace_01.php", "    public function parentMeth^od2(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClassInNamespace01_Fix02() throws Exception {
        applyHint("testExtendsClassInNamespace_01.php", "    public function parentMetho^d3(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClassInNamespace01_Fix03() throws Exception {
        applyHint("testExtendsClassInNamespace_01.php", "    public function parentMetho^d5(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClassInNamespace01_Fix04() throws Exception {
        applyHint("testExtendsClassInNamespace_01.php", "    public static function parentPublicStaticMet^hod1(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClassInNamespace01_Fix05() throws Exception {
        applyHint("testExtendsClassInNamespace_01.php", "    protected function pare^ntProtectedMethod1(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClassInNamespace_02() throws Exception {
        checkHints("testExtendsClassInNamespace_02.php");
    }

    public void testExtendsClassInNamespace02_Fix01() throws Exception {
        applyHint("testExtendsClassInNamespace_02.php", "    public function parent^Method3(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsClassInNamespace02_Fix02() throws Exception {
        applyHint("testExtendsClassInNamespace_02.php", "    public function paren^tMethod5(): void {} // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testEnumImplementsInterface_01() throws Exception {
        checkHints("testEnumImplementsInterface.php");
    }

    public void testEnumImplementsInterface_Fix01() throws Exception {
        applyHint("testEnumImplementsInterface.php", "    public function interfaceMet^hod1(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testEnumImplementsInterface_Fix02() throws Exception {
        applyHint("testEnumImplementsInterface.php", "    public function interfaceM^ethod2(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testEnumImplementsInterface_Fix03() throws Exception {
        applyHint("testEnumImplementsInterface.php", "    public function interfaceStaticMetho^d1(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testEnumImplementsInterface_Fix04() throws Exception {
        applyHint("testEnumImplementsInterface.php", "    public function interfaceSt^aticMethod2(): void { // hints", "Add \"#[\\Override]\" Attribute");
    }

    public void testAnonymousClass_01() throws Exception {
        checkHints("testAnonymousClass_01.php");
    }

    public void testAnonymousClass_PHP82_01() throws Exception {
        checkHints("testAnonymousClass_01.php", PhpVersion.PHP_82);
    }

    public void testAnonymousClass_Fix01() throws Exception {
        applyHint("testAnonymousClass_01.php", "    public function interfaceMe^thod2(): void { // hints class", "Add \"#[\\Override]\" Attribute");
    }

    public void testAnonymousClass_Fix02() throws Exception {
        applyHint("testAnonymousClass_01.php", "            public function interfaceMet^hod1(): void { // hints anon1", "Add \"#[\\Override]\" Attribute");
    }

    public void testAnonymousClass_Fix03() throws Exception {
        applyHint("testAnonymousClass_01.php", "            public function interfaceMet^hod2(): void { // hints anon1", "Add \"#[\\Override]\" Attribute");
    }

    public void testAnonymousClass_Fix04() throws Exception {
        applyHint("testAnonymousClass_01.php", "                    public function interface^Method2(): void { // hints anon2", "Add \"#[\\Override]\" Attribute");
    }

    public void testAnonymousClass_Fix05() throws Exception {
        applyHint("testAnonymousClass_01.php", "                    public function interfaceStatic^Method1(): void { // hints anon2", "Add \"#[\\Override]\" Attribute");
    }

    public void testAnonymousClass_Fix06() throws Exception {
        applyHint("testAnonymousClass_01.php", "            public function interfaceStaticMe^thod1(): void { // hints anon1", "Add \"#[\\Override]\" Attribute");
    }

    public void testAnonymousClass_Fix07() throws Exception {
        applyHint("testAnonymousClass_01.php", "    public function interface^StaticMethod1(): void { // hints class", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsAbstractClass_01() throws Exception {
        checkHints("testExtendsAbstractClass.php");
    }

    public void testExtendsAbstractClass_Fix01() throws Exception {
        applyHint("testExtendsAbstractClass.php", "    public function abstractPublicMet^hod(): void {", "Add \"#[\\Override]\" Attribute");
    }

    public void testExtendsAbstractClass_Fix02() throws Exception {
        applyHint("testExtendsAbstractClass.php", "    protected function abstractProtecte^dMethod(): void {", "Add \"#[\\Override]\" Attribute");
    }

    public void testAbstractClass_01() throws Exception {
        checkHints("testAbstractClass.php");
    }

    public void testAbstractClass_Fix01() throws Exception {
        applyHint("testAbstractClass.php", "    public function interfaceMetho^d1(): void {}", "Add \"#[\\Override]\" Attribute");
    }

    public void testTrait_01() throws Exception {
        // no hints
        checkHints("testTrait.php");
    }

    public void testAbstractTraitMethods_01() throws Exception {
        checkHints("testAbstractTraitMethods_01.php");
    }

    public void testAbstractTraitMethods_Fix01() throws Exception {
        applyHint("testAbstractTraitMethods_01.php", "    public function abstractTraitPubl^icMethod(): int {", "Add \"#[\\Override]\" Attribute");
    }

    public void testAbstractTraitMethods_Fix02() throws Exception {
        applyHint("testAbstractTraitMethods_01.php", "    protected function abstractTraitProtecte^dMethod(): int {", "Add \"#[\\Override]\" Attribute");
    }

    public void testAbstractTraitMethods_Fix03() throws Exception {
        applyHint("testAbstractTraitMethods_01.php", "    private function abstractTraitPrivateM^ethod(): string {", "Add \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_01() throws Exception {
        checkHints("testRemoveOverride_01.php");
    }

    public void testRemoveOverride_02() throws Exception {
        checkHints("testRemoveOverride_02.php");
    }

    public void testRemoveOverride_03() throws Exception {
        checkHints("testRemoveOverride_03.php");
    }

    public void testRemoveOverride_04() throws Exception {
        checkHints("testRemoveOverride_04.php");
    }

    public void testRemoveOverride_05() throws Exception {
        checkHints("testRemoveOverride_05.php");
    }

    public void testRemoveOverride_06() throws Exception {
        checkHints("testRemoveOverride_06.php");
    }

    public void testRemoveOverride_07() throws Exception {
        checkHints("testRemoveOverride_07.php");
    }

    public void testRemoveOverride_08() throws Exception {
        checkHints("testRemoveOverride_08.php");
    }

    public void testRemoveOverride_09() throws Exception {
        checkHints("testRemoveOverride_09.php");
    }

    public void testRemoveOverride_10() throws Exception {
        checkHints("testRemoveOverride_10.php");
    }

    public void testRemoveOverride_11() throws Exception {
        checkHints("testRemoveOverride_11.php");
    }

    public void testRemoveOverride_12() throws Exception {
        checkHints("testRemoveOverride_12.php");
    }

    public void testRemoveOverride_13() throws Exception {
        checkHints("testRemoveOverride_13.php");
    }

    public void testRemoveOverride_14() throws Exception {
        checkHints("testRemoveOverride_14.php");
    }

    public void testRemoveOverride_15() throws Exception {
        checkHints("testRemoveOverride_15.php");
    }

    public void testRemoveOverride_16() throws Exception {
        checkHints("testRemoveOverride_16.php");
    }

    public void testRemoveOverride_17() throws Exception {
        checkHints("testRemoveOverride_17.php");
    }

    public void testRemoveOverride_18() throws Exception {
        checkHints("testRemoveOverride_18.php");
    }

    public void testRemoveOverride_19() throws Exception {
        checkHints("testRemoveOverride_19.php");
    }

    public void testRemoveOverride_20() throws Exception {
        checkHints("testRemoveOverride_20.php");
    }

    public void testRemoveOverride_Fix01() throws Exception {
        applyHint("testRemoveOverride_01.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix02() throws Exception {
        applyHint("testRemoveOverride_02.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix03() throws Exception {
        applyHint("testRemoveOverride_03.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix04() throws Exception {
        applyHint("testRemoveOverride_04.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix05() throws Exception {
        applyHint("testRemoveOverride_05.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix06() throws Exception {
        applyHint("testRemoveOverride_06.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix07() throws Exception {
        applyHint("testRemoveOverride_07.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix08() throws Exception {
        applyHint("testRemoveOverride_08.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix09() throws Exception {
        applyHint("testRemoveOverride_09.php", "        \\Overri^de,", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix10() throws Exception {
        applyHint("testRemoveOverride_10.php", "    #[Attr1, \\Overri^de]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix11() throws Exception {
        applyHint("testRemoveOverride_11.php", "\\Overri^de", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix12() throws Exception {
        applyHint("testRemoveOverride_12.php", "\\Overri^de", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix13() throws Exception {
        applyHint("testRemoveOverride_13.php", "\\Overri^de", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix14() throws Exception {
        applyHint("testRemoveOverride_14.php", "\\Overri^de", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix15() throws Exception {
        applyHint("testRemoveOverride_15.php", "#[\\Overri^de]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix16() throws Exception {
        applyHint("testRemoveOverride_16.php", "#[Overri^de]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix17() throws Exception {
        applyHint("testRemoveOverride_17.php", "    #[\\Overr^ide]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix18() throws Exception {
        applyHint("testRemoveOverride_18.php", "    #[\\Overr^ide]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix19() throws Exception {
        applyHint("testRemoveOverride_19.php", "    #[\\Overr^ide]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverride_Fix20() throws Exception {
        applyHint("testRemoveOverride_20.php", "    #[\\Overri^de] // comment", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInNamespace_01() throws Exception {
        checkHints("testRemoveOverrideInNamespace_01.php");
    }

    public void testRemoveOverrideInNamespace_02() throws Exception {
        // no hints
        checkHints("testRemoveOverrideInNamespace_02.php");
    }

    public void testRemoveOverrideInNamespace_03() throws Exception {
        checkHints("testRemoveOverrideInNamespace_03.php");
    }

    public void testRemoveOverrideInNamespace_04() throws Exception {
        checkHints("testRemoveOverrideInNamespace_04.php");
    }

    public void testRemoveOverrideInNamespace_05() throws Exception {
        checkHints("testRemoveOverrideInNamespace_05.php");
    }

    public void testRemoveOverrideInNamespace_06() throws Exception {
        checkHints("testRemoveOverrideInNamespace_06.php");
    }

    public void testRemoveOverrideInNamespace_07() throws Exception {
        checkHints("testRemoveOverrideInNamespace_07.php");
    }

    public void testRemoveOverrideInNamespace_08() throws Exception {
        checkHints("testRemoveOverrideInNamespace_08.php");
    }

    public void testRemoveOverrideInNamespace_09() throws Exception {
        // no hints
        checkHints("testRemoveOverrideInNamespace_09.php");
    }

    public void testRemoveOverrideInNamespace_Fix01() throws Exception {
        applyHint("testRemoveOverrideInNamespace_01.php", "#[\\Overri^de]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInNamespace_Fix03() throws Exception {
        applyHint("testRemoveOverrideInNamespace_03.php", "    #[\\Over^ride, Attr1]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInNamespace_Fix04() throws Exception {
        applyHint("testRemoveOverrideInNamespace_04.php", "    #[Overrid^e]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInNamespace_Fix05() throws Exception {
        applyHint("testRemoveOverrideInNamespace_05.php", "    #[Overri^de, Attr(1)]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInNamespace_Fix06() throws Exception {
        applyHint("testRemoveOverrideInNamespace_06.php", "    #[Attr(1), Over^ride]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInNamespace_Fix07() throws Exception {
        applyHint("testRemoveOverrideInNamespace_07.php", "    #[Attr(1), /*comment */ Ov^erride,]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInNamespace_Fix08() throws Exception {
        applyHint("testRemoveOverrideInNamespace_08.php", "    #[Overr^ide]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideEnum_01() throws Exception {
        checkHints("testRemoveOverrideEnum_01.php");
    }

    public void testRemoveOverrideEnum_02() throws Exception {
        checkHints("testRemoveOverrideEnum_02.php");
    }

    public void testRemoveOverrideEnum_Fix01() throws Exception {
        applyHint("testRemoveOverrideEnum_01.php", "    #[\\Overr^ide]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideEnum_Fix02() throws Exception {
        applyHint("testRemoveOverrideEnum_02.php", "    #[\\Overr^ide] // comment", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInterface_01() throws Exception {
        checkHints("testRemoveOverrideInterface_01.php");
    }

    public void testRemoveOverrideInterface_02() throws Exception {
        checkHints("testRemoveOverrideInterface_02.php");
    }

    public void testRemoveOverrideInterface_Fix01() throws Exception {
        applyHint("testRemoveOverrideInterface_01.php", "    #[\\Overr^ide]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideInterface_Fix02() throws Exception {
        applyHint("testRemoveOverrideInterface_02.php", "    #[Over^ride, Attr(1)]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideAnonymousClass_01() throws Exception {
        checkHints("testRemoveOverrideAnonymousClass_01.php");
    }

    public void testRemoveOverrideAnonymousClass_02() throws Exception {
        checkHints("testRemoveOverrideAnonymousClass_02.php");
    }

    public void testRemoveOverrideAnonymousClass_03() throws Exception {
        checkHints("testRemoveOverrideAnonymousClass_03.php");
    }

    public void testRemoveOverrideAnonymousClass_Fix01() throws Exception {
        applyHint("testRemoveOverrideAnonymousClass_01.php", "            #[\\Ove^rride]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideAnonymousClass_Fix02() throws Exception {
        applyHint("testRemoveOverrideAnonymousClass_02.php", "                    #[Overri^de]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideAnonymousClass_Fix03() throws Exception {
        applyHint("testRemoveOverrideAnonymousClass_03.php", "            #[\\Overr^ide]", "Remove \"#[\\Override]\" Attribute");
    }

    public void testRemoveOverrideAbstractClass_01() throws Exception {
        checkHints("testRemoveOverrideAbstractClass_01.php");
    }

    public void testRemoveOverrideAbstractClass_Fix01() throws Exception {
        applyHint("testRemoveOverrideAbstractClass_01.php", "    #[\\Overr^ide] // test", "Remove \"#[\\Override]\" Attribute");
    }

    private void checkHints(String fileName, PhpVersion phpVersion) throws Exception {
        checkHints(new AddOverrideAttributeHintStub(phpVersion), fileName);
    }

    private void checkHints(String fileName) throws Exception {
        checkHints(fileName, PhpVersion.PHP_83);
    }

    private void applyHint(String fileName, String caretLine, String fixDesc, PhpVersion phpVersion) throws Exception {
        applyHint(new AddOverrideAttributeHintStub(phpVersion), fileName, caretLine, fixDesc);
    }

    private void applyHint(String fileName, String caretLine, String fixDesc) throws Exception {
        applyHint(fileName, caretLine, fixDesc, PhpVersion.PHP_83);
    }

    private static final class AddOverrideAttributeHintStub extends AddOverrideAttributeHint {

        private final PhpVersion phpVersion;

        public AddOverrideAttributeHintStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected boolean hasOverrideAttribute(FileObject fileObject) {
            return phpVersion.hasOverrideAttribute();
        }
    }
}
