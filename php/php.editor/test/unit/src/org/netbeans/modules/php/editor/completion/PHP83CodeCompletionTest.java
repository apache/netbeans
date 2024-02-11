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
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHP83CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP83CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php83/" + getTestDirName()))
            })
        );
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php83/%s/%s.php", getTestDirName(), fileName);
    }

    private void checkCompletion(String fileName, String caretPosition) throws Exception {
        checkCompletion(getTestPath(fileName), caretPosition, false);
    }

    public void testDynamicClassConstantFetchInConstDeclTyping_01() throws Exception {
        checkCompletion("inConstantDeclarationTyping", "    public const TEST1 = self::{^};");
    }

    public void testDynamicClassConstantFetchInEnumCaseTyping_01() throws Exception {
        checkCompletion("inEnumCaseTyping", "    case TEST1 = self::{^};");
    }

    public void testDynamicClassConstantFetchTyping_01() throws Exception {
        checkCompletion("dynamicClassConstantFetchTyping", "Test::{^};");
    }

    public void testDynamicClassConstantFetch_01() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    public const TEST3 = self::{self::T^ES . self::T};");
    }

    public void testDynamicClassConstantFetch_02() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{$^variable};");
    }

    public void testDynamicClassConstantFetch_03() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{$var^iable};");
    }

    public void testDynamicClassConstantFetch_04() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "$test::{$variable . $^e};");
    }

    public void testDynamicClassConstantFetch_05() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "$test::{$variable . $e^};");
    }

    public void testDynamicClassConstantFetch_06a() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Tes^t::method()}::{test($variable)};");
    }

    public void testDynamicClassConstantFetch_06b() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Test::met^hod()}::{test($variable)};");
    }

    public void testDynamicClassConstantFetch_06c() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Test::method()}::{te^st($variable)};");
    }

    public void testDynamicClassConstantFetch_06d() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "Test::{Test::method()}::{test($var^iable)};");
    }

    public void testDynamicClassConstantFetch_07a() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST3 = self::^{self::TES . self::T};");
    }

    public void testDynamicClassConstantFetch_07b() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST3 = self::{sel^f::TES . self::T};");
    }

    public void testDynamicClassConstantFetch_07c() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST3 = self::{self::TE^S . self::T};");
    }

    public void testDynamicClassConstantFetch_08a() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTe^st::{self::{self::TES} . self::T};");
    }

    public void testDynamicClassConstantFetch_08b() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{se^lf::{self::TES} . self::T};");
    }

    public void testDynamicClassConstantFetch_08c() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{self::{sel^f::TES} . self::T};");
    }

    public void testDynamicClassConstantFetch_08d() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{self::{self::TE^S} . self::T};");
    }

    public void testDynamicClassConstantFetch_08e() throws Exception {
        checkCompletion("dynamicClassConstantFetch", "    case TEST4 = EnumTest::{self::{self::TES} . self::T^};");
    }

    public void testTypedClassConstants_Class01() throws Exception {
        checkCompletion("typedClassConstants", "    public const ^WITHOUT_TYPE = 1;");
    }

    public void testTypedClassConstants_Class02() throws Exception {
        checkCompletion("typedClassConstants", "    const ?^A NULLABLE = null;");
    }

    public void testTypedClassConstants_Class03() throws Exception {
        checkCompletion("typedClassConstants", "    public const ?in^t NULLABLE2 = 1;");
    }

    public void testTypedClassConstants_Class04a() throws Exception {
        checkCompletion("typedClassConstants", "    private const ^A|B UNION = A;");
    }

    public void testTypedClassConstants_Class04b() throws Exception {
        checkCompletion("typedClassConstants", "    private const A|^B UNION = A;");
    }

    public void testTypedClassConstants_Class04c() throws Exception {
        checkCompletion("typedClassConstants", "    private const A|B^ UNION = A;");
    }

    public void testTypedClassConstants_Class05a() throws Exception {
        checkCompletion("typedClassConstants", "    protected const ^A&B INTERSECTION = B;");
    }

    public void testTypedClassConstants_Class05b() throws Exception {
        checkCompletion("typedClassConstants", "    protected const A^&B INTERSECTION = B;");
    }

    public void testTypedClassConstants_Class05c() throws Exception {
        checkCompletion("typedClassConstants", "    protected const A&^B INTERSECTION = B;");
    }

    public void testTypedClassConstants_Class06a() throws Exception {
        checkCompletion("typedClassConstants", "    public const (^A&B)|C DNF = C;");
    }

    public void testTypedClassConstants_Class06b() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A^&B)|C DNF = C;");
    }

    public void testTypedClassConstants_Class06c() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&^B)|C DNF = C;");
    }

    public void testTypedClassConstants_Class06d() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B^)|C DNF = C;");
    }

    public void testTypedClassConstants_Class06e() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|^C DNF = C;");
    }

    public void testTypedClassConstants_Class06f() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|C^ DNF = C;");
    }

    public void testTypedClassConstants_Class07() throws Exception {
        checkCompletion("typedClassConstants", "    const ?A NULLABLE = ^null;");
    }

    public void testTypedClassConstants_Interface01() throws Exception {
        checkCompletion("typedClassConstants", "    public const ^WITHOUT_TYPE = 1; // interface");
    }

    public void testTypedClassConstants_Interface02() throws Exception {
        checkCompletion("typedClassConstants", "    public const ?^A NULLABLE = null;  // interface");
    }

    public void testTypedClassConstants_Interface03() throws Exception {
        checkCompletion("typedClassConstants", "    public const ?in^t NULLABLE2 = 1; // interface");
    }

    public void testTypedClassConstants_Interface04a() throws Exception {
        checkCompletion("typedClassConstants", "    public const ^A|B UNION = A; // interface");
    }

    public void testTypedClassConstants_Interface04b() throws Exception {
        checkCompletion("typedClassConstants", "    public const A^|B UNION = A; // interface");
    }

    public void testTypedClassConstants_Interface04c() throws Exception {
        checkCompletion("typedClassConstants", "    public const A|^B UNION = A; // interface");
    }

    public void testTypedClassConstants_Interface04d() throws Exception {
        checkCompletion("typedClassConstants", "    public const A|B^ UNION = A; // interface");
    }

    public void testTypedClassConstants_Interface05a() throws Exception {
        checkCompletion("typedClassConstants", "    public const ^A&B INTERSECTION = B; // interface");
    }

    public void testTypedClassConstants_Interface05b() throws Exception {
        checkCompletion("typedClassConstants", "    public const A^&B INTERSECTION = B; // interface");
    }

    public void testTypedClassConstants_Interface05c() throws Exception {
        checkCompletion("typedClassConstants", "    public const A&^B INTERSECTION = B; // interface");
    }

    public void testTypedClassConstants_Interface05d() throws Exception {
        checkCompletion("typedClassConstants", "    public const A&B^ INTERSECTION = B; // interface");
    }

    public void testTypedClassConstants_Interface06a() throws Exception {
        checkCompletion("typedClassConstants", "    public const (^A&B)|C DNF = C; // interface");
    }

    public void testTypedClassConstants_Interface06b() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A^&B)|C DNF = C; // interface");
    }

    public void testTypedClassConstants_Interface06c() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&^B)|C DNF = C; // interface");
    }

    public void testTypedClassConstants_Interface06d() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B^)|C DNF = C; // interface");
    }

    public void testTypedClassConstants_Interface06e() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|^C DNF = C; // interface");
    }

    public void testTypedClassConstants_Interface06f() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|C^ DNF = C; // interface");
    }

    public void testTypedClassConstants_Interface07() throws Exception {
        checkCompletion("typedClassConstants", "    public const A|B UNION = ^A; // interface");
    }

    public void testTypedClassConstants_Trait01() throws Exception {
        checkCompletion("typedClassConstants", "    public const ^WITHOUT_TYPE = 1; // trait");
    }

    public void testTypedClassConstants_Trait02() throws Exception {
        checkCompletion("typedClassConstants", "    public const ?^A NULLABLE = null; // trait");
    }

    public void testTypedClassConstants_Trait03() throws Exception {
        checkCompletion("typedClassConstants", "    public const ?in^t NULLABLE2 = 1; // trait");
    }

    public void testTypedClassConstants_Trait04a() throws Exception {
        checkCompletion("typedClassConstants", "    private const ^A|B UNION = A; // trait");
    }

    public void testTypedClassConstants_Trait04b() throws Exception {
        checkCompletion("typedClassConstants", "    private const A|^B UNION = A; // trait");
    }

    public void testTypedClassConstants_Trait04c() throws Exception {
        checkCompletion("typedClassConstants", "    private const A|B^ UNION = A; // trait");
    }

    public void testTypedClassConstants_Trait05a() throws Exception {
        checkCompletion("typedClassConstants", "    protected const ^A&B INTERSECTION = B; // trait");
    }

    public void testTypedClassConstants_Trait05b() throws Exception {
        checkCompletion("typedClassConstants", "    protected const A^&B INTERSECTION = B; // trait");
    }

    public void testTypedClassConstants_Trait05c() throws Exception {
        checkCompletion("typedClassConstants", "    protected const A&^B INTERSECTION = B; // trait");
    }

    public void testTypedClassConstants_Trait06a() throws Exception {
        checkCompletion("typedClassConstants", "    public const (^A&B)|C DNF = C; // trait");
    }

    public void testTypedClassConstants_Trait06b() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A^&B)|C DNF = C; // trait");
    }

    public void testTypedClassConstants_Trait06c() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&^B)|C DNF = C; // trait");
    }

    public void testTypedClassConstants_Trait06d() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B^)|C DNF = C; // trait");
    }

    public void testTypedClassConstants_Trait06e() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|^C DNF = C; // trait");
    }

    public void testTypedClassConstants_Trait06f() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|C^ DNF = C; // trait");
    }

    public void testTypedClassConstants_Trait07() throws Exception {
        checkCompletion("typedClassConstants", "    protected const A&B INTERSECTION = ^B; // trait");
    }

    public void testTypedClassConstants_Enum01() throws Exception {
        checkCompletion("typedClassConstants", "    public const ^WITHOUT_TYPE = 1; // enum");
    }

    public void testTypedClassConstants_Enum02() throws Exception {
        checkCompletion("typedClassConstants", "    public const ?^A NULLABLE = null; // enum");
    }

    public void testTypedClassConstants_Enum03() throws Exception {
        checkCompletion("typedClassConstants", "    public const ?in^t NULLABLE2 = 1; // enum");
    }

    public void testTypedClassConstants_Enum04a() throws Exception {
        checkCompletion("typedClassConstants", "    private const ^A|B UNION = A; // enum");
    }

    public void testTypedClassConstants_Enum04b() throws Exception {
        checkCompletion("typedClassConstants", "    private const A|^B UNION = A; // enum");
    }

    public void testTypedClassConstants_Enum04c() throws Exception {
        checkCompletion("typedClassConstants", "    private const A|B^ UNION = A; // enum");
    }

    public void testTypedClassConstants_Enum05a() throws Exception {
        checkCompletion("typedClassConstants", "    protected const ^A&B INTERSECTION = B; // enum");
    }

    public void testTypedClassConstants_Enum05b() throws Exception {
        checkCompletion("typedClassConstants", "    protected const A^&B INTERSECTION = B; // enum");
    }

    public void testTypedClassConstants_Enum05c() throws Exception {
        checkCompletion("typedClassConstants", "    protected const A&^B INTERSECTION = B; // enum");
    }

    public void testTypedClassConstants_Enum06a() throws Exception {
        checkCompletion("typedClassConstants", "    public const (^A&B)|C DNF = C; // enum");
    }

    public void testTypedClassConstants_Enum06b() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A^&B)|C DNF = C; // enum");
    }

    public void testTypedClassConstants_Enum06c() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&^B)|C DNF = C; // enum");
    }

    public void testTypedClassConstants_Enum06d() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B^)|C DNF = C; // enum");
    }

    public void testTypedClassConstants_Enum06e() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|^C DNF = C; // enum");
    }

    public void testTypedClassConstants_Enum06f() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|C^ DNF = C; // enum");
    }

    public void testTypedClassConstants_Enum07() throws Exception {
        checkCompletion("typedClassConstants", "    public const (A&B)|C DNF = ^C; // enum");
    }

    public void testTypedClassConstantsTypingClass01() throws Exception {
        checkCompletion("typedClassConstantsTypingClass01", "    public const ^");
    }

    public void testTypedClassConstantsTypingClass02() throws Exception {
        checkCompletion("typedClassConstantsTypingClass02", "    public const string^");
    }

    public void testTypedClassConstantsTypingClass03() throws Exception {
        checkCompletion("typedClassConstantsTypingClass03", "    public const string|^");
    }

    public void testTypedClassConstantsTypingClass04() throws Exception {
        checkCompletion("typedClassConstantsTypingClass04", "    public const A&^");
    }

    public void testTypedClassConstantsTypingClass05() throws Exception {
        checkCompletion("typedClassConstantsTypingClass05", "    public const A&B&Te^");
    }

    public void testTypedClassConstantsTypingClass06() throws Exception {
        checkCompletion("typedClassConstantsTypingClass06", "    public const (A&^)");
    }

    public void testTypedClassConstantsTypingClass07() throws Exception {
        checkCompletion("typedClassConstantsTypingClass07", "    public const (A&B)|^");
    }

    public void testTypedClassConstantsTypingClass08() throws Exception {
        checkCompletion("typedClassConstantsTypingClass08", "    public const (A&B)|(^)");
    }

    public void testTypedClassConstantsTypingClass09() throws Exception {
        checkCompletion("typedClassConstantsTypingClass09", "    public const (A&B)|(A&C) C^");
    }

    public void testTypedClassConstantsTypingClass10() throws Exception {
        checkCompletion("typedClassConstantsTypingClass10", "    public const (A&B)|(A&C) CONST_NAME = ^");
    }

    public void testTypedClassConstantsTypingInterface01() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface01", "    public const ^");
    }

    public void testTypedClassConstantsTypingInterface02() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface02", "    public const string^");
    }

    public void testTypedClassConstantsTypingInterface03() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface03", "    public const string|^");
    }

    public void testTypedClassConstantsTypingInterface04() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface04", "    public const A&^");
    }

    public void testTypedClassConstantsTypingInterface05() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface05", "    public const A&B&C^");
    }

    public void testTypedClassConstantsTypingInterface06() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface06", "    public const (A&^)");
    }

    public void testTypedClassConstantsTypingInterface07() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface07", "    public const (A&B)|^");
    }

    public void testTypedClassConstantsTypingInterface08() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface08", "    public const (A&B)|(^)");
    }

    public void testTypedClassConstantsTypingInterface09() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface09", "    public const (A&B)|(A&C) C^");
    }

    public void testTypedClassConstantsTypingInterface10() throws Exception {
        checkCompletion("typedClassConstantsTypingInterface10", "    public const (A&B)|(A&C) CONST_NAME = ^");
    }

    public void testOverrideAttribute01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testOverrideAttribute01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_83, "test"), true);
    }

    public void testOverrideAttribute01_PHP82() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testOverrideAttribute01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testOverrideAttributeAnonClass01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testOverrideAttributeAnonClass01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_83, "test"), true);
    }

    public void testOverrideAttributeAnonClass01_PHP82() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testOverrideAttributeAnonClass01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

    public void testOverrideAttributeEnum01() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testOverrideAttributeEnum01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_83, "test"), true);
    }

    public void testOverrideAttributeEnum01_PHP82() throws Exception {
        checkCompletionCustomTemplateResult(getTestPath("testOverrideAttributeEnum01"), "    test^",
                new DefaultFilter(PhpVersion.PHP_82, "test"), true);
    }

}
