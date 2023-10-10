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
package org.netbeans.modules.php.editor.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.actions.FixUsesAction.Options;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FixUsesPerformerTest extends PHPTestBase {
    private static final String CAN_NOT_BE_RESOLVED = "<CAN-NOT-BE-RRESOLVED>"; //NOI18N

    public FixUsesPerformerTest(String testName) {
        super(testName);
    }

    public void testIssue210093_01() throws Exception {
        String[] selections = new String[] {"\\Issue\\Martin\\Pondeli"};
        Options options = new Options(false, false, true, false, false);
        performTest("function testFail(\\Issue\\Martin\\Pond^eli $param) {}", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testIssue210093_02() throws Exception {
        String[] selections = new String[] {"\\Issue\\Martin\\Pondeli"};
        Options options = new Options(false, false, false, false, false);
        performTest("function testFail(\\Issue\\Martin\\Pond^eli $param) {}", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testIssue211566_01() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\Baz"};
        Options options = new Options(false, false, false, false, false);
        performTest("new \\Foo\\Bar\\B^az(); //HERE", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testIssue211566_02() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\Baz"};
        Options options = new Options(false, false, true, false, false);
        performTest("new \\Foo\\Bar\\B^az(); //HERE", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testIssue214699() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\ClassName", "\\Baz\\Bat\\ClassName", "\\Fom\\Bom\\ClassName"};
        performTest("$a = new ClassName();^//HERE", createSelections(selections, ItemVariant.Type.CLASS));
    }

    public void testIssue211585_01() throws Exception {
        String[] selections = new String[] {"\\Foo\\Bar\\ClassName", "\\Baz\\Bat\\ClassName", "\\Fom\\Bom\\ClassName"};
        Options options = new Options(false, false, true, true, false);
        performTest("$a = new ClassName();^//HERE", createSelections(selections, ItemVariant.Type.CLASS), false, options);
    }

    public void testIssue211585_02() throws Exception {
        String[] selections = new String[] {"\\Baz\\Bat\\ClassName", "\\Baz\\Bat\\ClassName", "\\Fom\\Bom\\ClassName"};
        Options options = new Options(false, false, true, true, false);
        performTest("$a = new ClassName();^//HERE", createSelections(selections, ItemVariant.Type.CLASS), false, options);
    }

    public void testIssue211585_03() throws Exception {
        String[] selections = new String[] {"\\Fom\\Bom\\ClassName", "\\Baz\\Bat\\ClassName", "\\Fom\\Bom\\ClassName"};
        Options options = new Options(false, false, true, true, false);
        performTest("$a = new ClassName();^//HERE", createSelections(selections, ItemVariant.Type.CLASS), false, options);
    }

    public void testIssue233527() throws Exception {
        String[] selections = new String[] {"\\NS1\\NS2\\SomeClass", "\\NS1\\NS2\\SomeClass"};
        Options options = new Options(false, false, true, true, false);
        performTest("public function test(SomeClass $a) {^", createSelections(selections, ItemVariant.Type.CLASS), false, options);
    }

    public void testIssue222595_01() throws Exception {
        String[] selections = new String[] {"\\pl\\dagguh\\people\\Person"};
        Options options = new Options(false, false, false, true, false);
        performTest("function assignRoom(Room $room, Person $roomOwner);^", createSelections(selections, ItemVariant.Type.INTERFACE), false, options);
    }

    public void testIssue222595_02() throws Exception {
        String[] selections = new String[] {"\\pl\\dagguh\\people\\Person"};
        Options options = new Options(true, false, true, true, false);
        performTest("function assignRoom(Room $room, Person $roomOwner);^", createSelections(selections, ItemVariant.Type.INTERFACE), false, options);
    }

    public void testIssue222595_03() throws Exception {
        String[] selections = new String[] {};
        Options options = new Options(false, false, true, true, false);
        performTest("function addRoom(\\pl\\dagguh\\buildings\\Room $room);^", createSelections(selections, ItemVariant.Type.NONE), false, options);
    }

    public void testIssue222595_04() throws Exception {
        String[] selections = new String[] {};
        Options options = new Options(true, false, true, true, false);
        performTest("function addRoom(\\pl\\dagguh\\buildings\\Room $room);^", createSelections(selections, ItemVariant.Type.NONE), false, options);
    }

    public void testIssue238828() throws Exception {
        String[] selections = new String[] {"\\First\\Second\\Util", CAN_NOT_BE_RESOLVED, CAN_NOT_BE_RESOLVED, CAN_NOT_BE_RESOLVED};
        Options options = new Options(true, false, true, true, false);
        performTest("function functionName3($param) {}^", createSelections(selections, ItemVariant.Type.CLASS), false, options);
    }

    public void testUseFuncAndConst_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("Name\\Space\\Bar2", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\FOO", ItemVariant.Type.CONST));
        selections.add(new Selection("Name\\Space\\Bar", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\FOO2", ItemVariant.Type.CONST));
        selections.add(new Selection("Name\\Space\\fnc2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("Name\\Space\\fnc", ItemVariant.Type.FUNCTION));
        Options options = new Options(false, false, false, false, true);
        performTest("Name\\Space\\fnc2();^", selections, false, options);
    }

    public void testUseFuncAndConst_02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("Name\\Space\\Bar2", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\FOO", ItemVariant.Type.CONST));
        selections.add(new Selection("Name\\Space\\Bar", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\FOO2", ItemVariant.Type.CONST));
        selections.add(new Selection("Name\\Space\\fnc2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("Name\\Space\\fnc", ItemVariant.Type.FUNCTION));
        Options options = new Options(false, true, false, false, true);
        performTest("Name\\Space\\fnc2();^", selections, false, options);
    }

    public void testUseFuncAndConst_03() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("Name\\Space\\Bar2", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\Bar", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\fnc2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("Name\\Space\\fnc", ItemVariant.Type.FUNCTION));
        Options options = new Options(false, true, false, false, true);
        performTest("function __construct() {^", selections, false, options);
    }

    public void testUseFuncAndConst_04() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("Name\\Space\\Bar2", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\FOO", ItemVariant.Type.CONST));
        selections.add(new Selection("Name\\Space\\Bar", ItemVariant.Type.CLASS));
        selections.add(new Selection("Name\\Space\\FOO2", ItemVariant.Type.CONST));
        Options options = new Options(false, true, false, false, true);
        performTest("function __construct() {^", selections, false, options);
    }

    public void testUseFuncAndConst_05() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("Name\\Space\\FOO", ItemVariant.Type.CONST));
        selections.add(new Selection("Name\\Space\\FOO2", ItemVariant.Type.CONST));
        selections.add(new Selection("Name\\Space\\fnc2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("Name\\Space\\fnc", ItemVariant.Type.FUNCTION));
        Options options = new Options(false, true, false, false, true);
        performTest("function __construct() {^", selections, false, options);
    }

    public void testIssue243271_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("SomeClassAlias", ItemVariant.Type.CLASS, true));
        Options options = new Options(false, false, false, false, false);
        performTest("public function getSomething(SomeClassAlias $someClass) {}^", selections, false, options);
    }

    public void testIssue243271_02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, false, false, false, false);
        performTest("public function getSomething(SomeClassAlias $someClass) {}^", selections, false, options);
    }

    public void testGroupUse_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, false, false, false, false, PhpVersion.PHP_70);
        performTest("$a = new ClsA();^", selections, false, options);
    }

    public void testGroupUse_02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, false, false, false, false, PhpVersion.PHP_70);
        performTest("$a = new ClsA();^", selections, true, options);
    }

    public void testGroupUse_03() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, false, false, false, false, PhpVersion.PHP_70);
        performTest("$a = new ClsA();^", selections, true, options);
    }

    public void testGroupUse_04() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, false, false, false, false, PhpVersion.PHP_70);
        performTest("$a = new ClsA();^", selections, true, options);
    }

    public void testGroupUse_05() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, true, true, false, false);
        performTest("$a = new ClsA();^", selections, true, options);
    }

    public void testGroupUse_06() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, true, true, true, false, PhpVersion.PHP_70);
        performTest("$a = new ClsA();^", selections, true, options);
    }

    public void testGroupUse_07() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, true, true, true, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, true, options);
    }

    public void testGroupUse_08() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, false, true, true, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, true, options);
    }

    public void testGroupUse_09() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, true, true, false, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, true, options);
    }

    public void testGroupUse_10() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, true, true, true, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, true, options);
    }

    public void testGroupUse_11() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, true, true, true, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, true, options);
    }

    public void testGroupUse_12() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, true, true, true, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, true, options);
    }

    public void testGroupUse_13() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, true, true, true, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, true, options);
    }

    public void testGroupUse_14() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(true, true, true, true, false, PhpVersion.PHP_70);
        performTest("fa();^", selections, false, options);
    }

    public void testGroupUseComplex_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, true, true, true, false, PhpVersion.PHP_70);
        performTest("$a = new ClsA();^", selections, true, options);
    }

    public void testIssue249140_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, false, false, false, true);
        performTest("$d = d($someVar);^", selections, false, options);
    }

    public void testIssue249140_02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        Options options = new Options(false, false, false, false, true);
        performTest("$f = f($someVar);^", selections, false, options);
    }

    public void testNB4978_01() throws Exception {
        String[] selections = new String[] {"\\Foo\\Foo\\Foo"};
        Options options = new Options(false, false, false, false, true);
        performTest("// test^", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testNB4978_02() throws Exception {
        String[] selections = new String[] {"\\Foo\\Foo\\Foo"};
        Options options = new Options(false, false, false, false, true);
        performTest("// test^", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testNB4978_03() throws Exception {
        String[] selections = new String[] {"\\Foo\\Foo\\Foo"};
        Options options = new Options(false, false, false, false, true);
        performTest("// test^", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testNB4978_04() throws Exception {
        String[] selections = new String[] {"\\Foo\\Foo\\Foo"};
        Options options = new Options(false, false, false, false, true);
        performTest("// test^", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testNB4978_05() throws Exception {
        String[] selections = new String[] {"\\Foo\\Foo\\Foo"};
        Options options = new Options(false, false, false, false, true);
        performTest("public funct^ion test(?Foo $foo): ?Foo", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testNB4978_06() throws Exception {
        String[] selections = new String[] {"\\Foo\\Foo\\Foo"};
        Options options = new Options(false, false, false, false, true);
        performTest("public funct^ion test(?Foo $foo): ?Foo", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testNB4978_07() throws Exception {
        String[] selections = new String[] {"\\Foo\\Foo\\Foo"};
        Options options = new Options(false, false, false, false, true);
        performTest("// test^", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testGH6056_01() throws Exception {
        String[] selections = new String[] {"\\Test\\TestClass1", "\\Test\\TestClass2"};
        Options options = new Options(false, false, false, false, true);
        performTest("        <?php if(1 === 1):^ ?>", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testGH6082_01() throws Exception {
        String[] selections = new String[] {"\\Vendor\\Test"};
        Options options = new Options(false, false, false, false, true);
        performTest("function one(): ^Test {", createSelections(selections, ItemVariant.Type.CLASS), true, options);
    }

    public void testGH5330_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Test\\SameName\\SameNamePart", ItemVariant.Type.CLASS));
        Options options = new Options(false, false, false, false, false, PhpVersion.PHP_81);
        performTest("class Exam^ple {", selections, true, options);
    }

    public void testGH4609PSR12_GroupUses01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUses02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12a01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12a02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12b01() throws Exception {
        // no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12b02() throws Exception {
        // no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12c01() throws Exception {
        // no classes, no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12c02() throws Exception {
        // no classes, no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12d01() throws Exception {
        // no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12d02() throws Exception {
        // no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12e01() throws Exception {
        // no functions, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12e02() throws Exception {
        // no functions, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12f01() throws Exception {
        // no classes
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("CONSTAN^T_A;", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12f02() throws Exception {
        // no classes
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("CONSTAN^T_A;", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12g01() throws Exception {
        // no classes, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("funct^ionA();", selections, true, options);
    }

    public void testGH4609PSR12_GroupUsesWithPSR12g02() throws Exception {
        // no classes, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("funct^ionA();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUses01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUses02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12a01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12a02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12b01() throws Exception {
        // no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12b02() throws Exception {
        // no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12c01() throws Exception {
        // no classes, no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12c02() throws Exception {
        // no classes, no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12d01() throws Exception {
        // no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12d02() throws Exception {
        // no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12e01() throws Exception {
        // no functions, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12e02() throws Exception {
        // no functions, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12f01() throws Exception {
        // no classes
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12f02() throws Exception {
        // no classes
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12g01() throws Exception {
        // no classes, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("functio^nA();", selections, true, options);
    }

    public void testGH4609PSR12_SingleLineUsesWithPSR12g02() throws Exception {
        // no classes, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("functio^nA();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUses01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUses02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12a01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12a02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12b01() throws Exception {
        // no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12b02() throws Exception {
        // no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12c01() throws Exception {
        // no classes, no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12c02() throws Exception {
        // no classes, no functions
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("CONST^ANT_A;", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12d01() throws Exception {
        // no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12d02() throws Exception {
        // no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12e01() throws Exception {
        // no functions, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12e02() throws Exception {
        // no functions, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Classes\\MyClass", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\Vendor\\Package\\Enums\\MyEnum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\Vendor\\Package\\Traits\\MyTrait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\Vendor\\Package\\Interfaces\\MyInterface", ItemVariant.Type.INTERFACE));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("        $a = new MyCla^ss();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12f01() throws Exception {
        // no classes
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("CONSTAN^T_A;", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12f02() throws Exception {
        // no classes
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_A", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Constants\\CONSTANT_B", ItemVariant.Type.CONST));
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("CONSTAN^T_A;", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12g01() throws Exception {
        // no classes, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(1)
                .build();
        performTest("funct^ionA();", selections, true, options);
    }

    public void testGH4609PSR12_MultipleUsesWithPSR12g02() throws Exception {
        // no classes, no constants
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vendor\\Package\\Functions\\functionA", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .setBlankLinesBetweenUseTypes(0)
                .build();
        performTest("funct^ionA();", selections, true, options);
    }

    public void testGH6075_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vender\\User", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .build();
        performTest("function test(): void ^{", selections, true, options);
    }

    public void testGH6162_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\Vender\\User", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .build();
        performTest("class Test ^{}", selections, true, options);
    }

    public void testGH5578_inHtml01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\HtmlNamespace\\InHtml", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81).build();
        performTest("        ^$test = new InHtml();", selections, true, options);
    }

    public void testGH5578_declare01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\B", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81).build();
        performTest("^// test", selections, true, options);
    }

    public void testGH5578_declare02() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\B", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81).build();
        performTest("^// test", selections, true, options);
    }

    public void testGH5578_declare03() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\DeclareTest2", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81).build();
        performTest("class DeclareTest1 ^{", selections, true, options);
    }

    public void testGH5578_declare04() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\DeclareTest2", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81).build();
        performTest("class DeclareTest1 ^{", selections, true, options);
    }

    public void testGH5578_declare05() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\DeclareTest2", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81).build();
        performTest("class DeclareTest1 ^{", selections, true, options);
    }

    public void testNoChanges_01() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\TestClass", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81).build();
        performTest("    class ^Test {", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single01a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single01b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single02a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single02b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single03a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single03b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single04a() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single04b() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single05a() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Single05b() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR01a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR01b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR02a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR02b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR03a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR03b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR04a() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR04b() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR05a() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_SinglePSR05b() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group01a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group01b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group02a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group02b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group03a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group03b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group04a() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group04b() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group05a() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Group05b() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR01a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR01b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR02a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR02b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR03a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR03b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR04a() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR04b() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR05a() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_GroupPSR05b() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferGroupUses(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple01a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple01b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple02a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple02b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple03a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple03b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple04a() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple04b() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple05a() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_Multiple05b() throws Exception {
        // default order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR01a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR01b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS1\\NS1Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS1\\NS1Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS1\\NS1Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS1\\NS1Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        selections.add(new Selection("\\NS2\\NS2Enum", ItemVariant.Type.ENUM));
        selections.add(new Selection("\\NS2\\NS2Interface", ItemVariant.Type.INTERFACE));
        selections.add(new Selection("\\NS2\\NS2Trait", ItemVariant.Type.TRAIT));
        selections.add(new Selection("\\NS1\\ns1Function", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS2\\ns2Function2", ItemVariant.Type.FUNCTION));
        selections.add(new Selection("\\NS1\\NS1_CONSTANT", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT1", ItemVariant.Type.CONST));
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("NS1\\ns1Functi^on();", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR02a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR02b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2_CONSTANT2", ItemVariant.Type.CONST));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR03a() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR03b() throws Exception {
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("NS2\\ns2Function1", ItemVariant.Type.FUNCTION));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR04a() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR04b() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR05a() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(true)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    public void testKeepExistingUseTypeOrder_MultiplePSR05b() throws Exception {
        // PSR-12 order is used
        List<Selection> selections = new ArrayList<>();
        selections.add(new Selection("\\NS2\\NS2Class", ItemVariant.Type.CLASS));
        Options options = new Options.Builder(PhpVersion.PHP_81)
                .preferMultipleUseStatementsCombined(true)
                .putInPSR12Order(true)
                .keepExistingUseTypeOrder(false)
                .build();
        performTest("$const1 = NS1_CONST^ANT;", selections, true, options);
    }

    private String getTestResult(final String fileName, final String caretLine, final List<Selection> selections, final boolean removeUnusedUses, final Options options) throws Exception {
        FileObject testFile = getTestFile(fileName);

        Source testSource = getTestSource(testFile);

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final String[] result = new String[1];
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(final ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                if (r != null) {
                    assertTrue(r instanceof ParserResult);
                    PHPParseResult phpResult = (PHPParseResult)r;
                    Map<String, List<UsedNamespaceName>> usedNames = new UsedNamesCollector(phpResult, caretOffset).collectNames();
                    FileScope fileScope = phpResult.getModel().getFileScope();
                    NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, caretOffset);
                    Options currentOptions = options;
                    Document document = phpResult.getSnapshot().getSource().getDocument(false);
                    if (document == null) {
                        // it can sometimes randomly happen
                        document = phpResult.getSnapshot().getSource().getDocument(true);
                    }
                    assertNotNull("Document should be found for: " + phpResult.getSnapshot().getSource().getFileObject(), document);
                    if (currentOptions == null) {
                        CodeStyle codeStyle = CodeStyle.get(document);
                        currentOptions = new FixUsesAction.Options(codeStyle, fileScope.getFileObject());
                    }
                    ImportData importData = new ImportDataCreator(
                            usedNames,
                            ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(phpResult)),
                            namespaceScope.getNamespaceName(),
                            currentOptions).create();
                    final List<ItemVariant> properSelections = new ArrayList<>();
                    for (Selection selection : selections) {
                        properSelections.add(new ItemVariant(
                                selection.getSelection(),
                                CAN_NOT_BE_RESOLVED.equals(selection.getSelection()) ? ItemVariant.UsagePolicy.CAN_NOT_BE_USED : ItemVariant.UsagePolicy.CAN_BE_USED,
                                selection.getType(),
                                selection.isAlias()));
                    }
                    importData.caretPosition = caretOffset;
                    FixUsesPerformer fixUsesPerformer = new FixUsesPerformer(phpResult, importData, properSelections, removeUnusedUses, currentOptions);
                    fixUsesPerformer.perform();
                    result[0] = document.getText(0, document.getLength());
                }
            }
        });
        return result[0];
    }

    private void performTest(final String caretLine, final List<Selection> selections) throws Exception {
        performTest(caretLine, selections, true, null);
    }

    private void performTest(final String caretLine, final List<Selection> selections, final boolean removeUnusedUses) throws Exception {
        performTest(caretLine, selections, removeUnusedUses, null);
    }

    private void performTest(final String caretLine, final List<Selection> selections, final boolean removeUnusedUses, final Options options) throws Exception {
        String exactFileName = getTestPath();
        String result = getTestResult(exactFileName, caretLine, selections, removeUnusedUses, options);
        assertDescriptionMatches(exactFileName, result, false, ".fixUses");
    }

    protected FileObject[] createSourceClassPathsForTest() {
        final File folder = new File(getDataDir(), getTestFolderPath());
        return new FileObject[]{FileUtil.toFileObject(folder)};
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))
            })
        );
    }

    private String getTestFolderPath() {
        return "testfiles/actions/" + transformTestMethodNameToDirectory();//NOI18N
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getName() + ".php";//NOI18N
    }

    private String transformTestMethodNameToDirectory() {
        return getName().replace('_', '/');
    }

    private static final class Selection {
        private final String selection;
        private final ItemVariant.Type type;
        private final boolean isAlias;

        public Selection(String selection, ItemVariant.Type type) {
            this(selection, type, false);
        }

        public Selection(String selection, ItemVariant.Type type, boolean isAlias) {
            this.selection = selection;
            this.type = type;
            this.isAlias = isAlias;
        }

        public String getSelection() {
            return selection;
        }

        public ItemVariant.Type getType() {
            return type;
        }

        public boolean isAlias() {
            return isAlias;
        }

    }

    private static List<Selection> createSelections(String[] selections, ItemVariant.Type type) {
        List<Selection> result = new ArrayList<>();
        for (String selection : selections) {
            result.add(new Selection(selection, type));
        }
        return result;
    }

}
