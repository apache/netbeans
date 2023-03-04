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
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PHP82CodeCompletionTest extends PHPCodeCompletionTestBase {

    public PHP82CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[]{
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/php82/" + getTestDirName()))
            })
        );
    }

    private String getTestDirName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    private String getTestPath(String fileName) {
        return String.format("testfiles/completion/lib/php82/%s/%s.php", getTestDirName(), fileName);
    }

    private void checkCompletion(String fileName, String caretPosition) throws Exception {
        checkCompletion(getTestPath(fileName), caretPosition, false);
    }

    public void testNullAndFalseType_01() throws Exception {
        checkCompletion("nullAndFalseType", "    public nu^ll $null = null; // PHP 8.2: OK");
    }

    public void testNullAndFalseType_02() throws Exception {
        checkCompletion("nullAndFalseType", "    public fal^se $false = false; // PHP 8.2: OK");
    }

    public void testNullAndFalseType_03() throws Exception {
        checkCompletion("nullAndFalseType", "    public ?fals^e $false2 = null; // PHP 8.2: OK");
    }

    public void testNullAndFalseType_04() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNull(nu^ll $null): null {");
    }

    public void testNullAndFalseType_05() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNull(null $null): nu^ll {");
    }

    public void testNullAndFalseType_06() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testFalse(fal^se $false): false {");
    }

    public void testNullAndFalseType_07() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testFalse(false $false): fal^se {");
    }

    public void testNullAndFalseType_08() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNullableFalse(?fal^se $false): ?false {");
    }

    public void testNullAndFalseType_09() throws Exception {
        checkCompletion("nullAndFalseType", "    public function testNullableFalse(?false $false): ?fal^se {");
    }

    public void testTrueType_01() throws Exception {
        checkCompletion("trueType", "    public tru^e $true = true; // PHP 8.2: OK");
    }

    public void testTrueType_02() throws Exception {
        checkCompletion("trueType", "    public ?tru^e $true2 = true; // PHP 8.2: OK");
    }

    public void testTrueType_03() throws Exception {
        checkCompletion("trueType", "    public int|tr^ue $true3 = true; // line comment");
    }

    public void testTrueType_04() throws Exception {
        checkCompletion("trueType", "    public tru^e|int $true4 = true; // line comment");
    }

    public void testTrueType_05() throws Exception {
        checkCompletion("trueType", "    public function test(tr^ue $true): true {");
    }

    public void testTrueType_06() throws Exception {
        checkCompletion("trueType", "    public function test(true $true): tru^e {");
    }

    public void testTrueType_07() throws Exception {
        checkCompletion("trueType", "    public function testNullable(?tr^ue $true): ?true {");
    }

    public void testTrueType_08() throws Exception {
        checkCompletion("trueType", "    public function testNullable(?true $true): ?tr^ue {");
    }

    public void testTrueType_09() throws Exception {
        checkCompletion("trueType", "    public function testUnionType(tr^ue|string $true): string|true {");
    }

    public void testTrueType_10() throws Exception {
        checkCompletion("trueType", "    public function testUnionType(true|string $true): string|tru^e {");
    }

    public void testReadonlyClasses_01() throws Exception {
        checkCompletion("readonlyClasses", "readon^ly class ReadonlyClass {");
    }

    public void testReadonlyClasses_02() throws Exception {
        checkCompletion("readonlyClasses", "readonl^y final class ReadonlyFinalClass {");
    }

    public void testReadonlyClasses_03() throws Exception {
        checkCompletion("readonlyClasses", "final read^only class FinalReadonlyClass {");
    }

    public void testReadonlyClasses_04() throws Exception {
        checkCompletion("readonlyClasses", "rea^donly abstract class ReadonlyAbstractClass {");
    }

    public void testReadonlyClasses_05() throws Exception {
        checkCompletion("readonlyClasses", "abstract reado^nly class AbstractReadonlyClass {");
    }

    public void testReadonlyClassesTyping01() throws Exception {
        checkCompletion("readonlyClassesTyping01", "readon^");
    }

    public void testReadonlyClassesTyping02() throws Exception {
        checkCompletion("readonlyClassesTyping02", "final readon^");
    }

    public void testReadonlyClassesTyping03() throws Exception {
        checkCompletion("readonlyClassesTyping03", "abstract readon^");
    }

    public void testFetchPropertiesInConstExpressions_01a() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const C1 = [self::Case->^value => self::Case];");
    }

    public void testFetchPropertiesInConstExpressions_01b() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const C1 = [self::Case->va^lue => self::Case];");
    }

    public void testFetchPropertiesInConstExpressions_02() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const C2 = [self::Case?->val^ue => self::Case];");
    }

    public void testFetchPropertiesInConstExpressions_03() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "const NAME = E::Case->na^me;");
    }

    public void testFetchPropertiesInConstExpressions_04() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "const VALUE_NULLSAFE = E::Case?->valu^e;");
    }

    public void testFetchPropertiesInConstExpressions_05() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const VALUE = E::Case->va^lue;");
    }

    public void testFetchPropertiesInConstExpressions_06() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    const NAME_NULLSAFE = E::Case?->nam^e;");
    }

    public void testFetchPropertiesInConstExpressions_07() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    public string $name = E::Case->na^me;");
    }

    public void testFetchPropertiesInConstExpressions_08() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    $valueNullsafe = E::Case?->va^lue,");
    }

    public void testFetchPropertiesInConstExpressions_09() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    static $staticName = E::Case->na^me;");
    }

    public void testFetchPropertiesInConstExpressions_10() throws Exception {
        checkCompletion("fetchPropertiesInConstExpressions", "    case VALUE = E::Case->valu^e;");
    }

    public void testConstantsInTraits_01() throws Exception {
        // no constant items because T::CONSTANT is invalid
        checkCompletion("constantsInTraits", "echo ExampleTrait::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // fatal error");
    }

    public void testConstantsInTraits_02a() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_02b() throws Exception {
        checkCompletion("constantsInTraits", "echo self::IMPLICIT_PUBLIC^_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_03a() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PRIVATE_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_03b() throws Exception {
        checkCompletion("constantsInTraits", "echo static::PRIVATE^_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_04a() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_04b() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::PROTECTED_TR^AIT . PHP_EOL;");
    }

    public void testConstantsInTraits_05() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT2 . PHP_EOL;");
    }

    public void testConstantsInTraits_06() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PRIVATE_TRAIT2 . PHP_EOL;");
    }

    public void testConstantsInTraits_07() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT2 . PHP_EOL;");
    }

    public void testConstantsInTraits_08() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // class");
    }

    public void testConstantsInTraits_09() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PRIVATE_TRAIT . PHP_EOL; // class");
    }

    public void testConstantsInTraits_10() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT . PHP_EOL; // class");
    }

    public void testConstantsInTraits_11() throws Exception {
        checkCompletion("constantsInTraits", "echo self::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_12() throws Exception {
        checkCompletion("constantsInTraits", "echo static::^PUBLIC_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_13() throws Exception {
        checkCompletion("constantsInTraits", "echo $this::^PROTECTED_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_14() throws Exception {
        checkCompletion("constantsInTraits", "echo parent::^PUBLIC_TRAIT . PHP_EOL; // child");
    }

    public void testConstantsInTraits_15() throws Exception {
        checkCompletion("constantsInTraits", "echo ExampleClass::^IMPLICIT_PUBLIC_TRAIT . PHP_EOL;");
    }

    public void testConstantsInTraits_16() throws Exception {
        checkCompletion("constantsInTraits", "$i::^PUBLIC_TRAIT;");
    }

    public void testConstantsInTraits_17() throws Exception {
        checkCompletion("constantsInTraits", "$c::^PUBLIC_TRAIT;");
    }

}
