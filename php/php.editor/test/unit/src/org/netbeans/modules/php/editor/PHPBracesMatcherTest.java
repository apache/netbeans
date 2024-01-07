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

package org.netbeans.modules.php.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseDocument;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.bracesmatching.BraceContext;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Test for PHPBracesMatcher
 *
 * @author Marek Slama
 */
public class PHPBracesMatcherTest extends PHPTestBase {

    private static final String TEST_DIRECTORY = "testfiles/bracematching/"; //NOI18N

    public PHPBracesMatcherTest(String testName) {
        super(testName);
    }

    /**
     * Test for BracesMatcher, first ^ gives current caret position,
     * second ^ gives matching caret position. Test is done in forward and backward direction.
     */
    private void match2(String original) throws BadLocationException {
        super.assertMatches2(wrapAsPhp(original));
    }

    private static String wrapAsPhp(String s) {
        // XXX: remove \n
        return "<?php\n" + s + "\n?>";
    }

    public void testFindMatching2() throws Exception {
        match2("x=^(true^)\ny=5");
    }

    public void testFindMatching3() throws Exception {
        match2("x=^(true || (false)^)\ny=5");
    }


    public void testIssue164495_01() throws Exception {
        match2("foreach ^($q['answers'] as $a^)\n{\n $tag=\"{value_$a[id]}\";\n}");
    }

    public void testIssue164495_02() throws Exception {
        match2("foreach ($q^['answers'^] as $a)\n{\n $tag=\"{value_$a[id]}\";\n}");
    }

    public void testIssue164495_03() throws Exception {
        match2("foreach ($q['answers'] as $a)\n^{\n $tag=\"{value_$a[id]}\";\n^}");
    }


    public void testIssue197709_01() throws Exception {
        match2("if (true) ^{\n"
                + "    echo \"Some string with braced ${variables[ $index ]} in it.\";\n"
                + "^}");
    }

    public void testIssue197709_02() throws Exception {
        match2("if (true) {\n"
                + "    echo \"Some string with braced ^${variables[ $index ]^} in it.\";\n"
                + "}");
    }

    public void testAlternativeSyntax_01() throws Exception {
        match2(
                "if ($i == 0) :\n"
                + "    if ($j == 0) :\n"
                + "    endif;\n"
                + "elseif ($i == 1)^:\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "^endif;\n"
                + "\n");
    }

    public void testAlternativeSyntax_02() throws Exception {
        match2(
                "if ($i == 0) :\n"
                + "    if ($j == 0) ^:\n"
                + "    ^endif;\n"
                + "elseif ($i == 1):\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "endif;\n");
    }

    public void testAlternativeSyntax_03() throws Exception {
        match2(   "for ($i = 0; $i < count($array); $i++) ^:\n"
                + "    for ($i = 0; $i < count($array); $i++) :\n"
                + "    endfor;\n"
                + "^endfor;\n");
    }

    public void testAlternativeSyntax_04() throws Exception {
        match2(   "for ($i = 0; $i < count($array); $i++) :\n"
                + "    for ($i = 0; $i < count($array); $i++) ^:\n"
                + "    ^endfor;\n"
                + "endfor;\n");
    }

    public void testAlternativeSyntax_05() throws Exception {
        match2(   "while (true)^:\n"
                + "    while(false):\n"
                + "        if ($a == 1):\n"
                + "\n            "
                + "        endif;\n"
                + "    endwhile;\n"
                + "^endwhile;\n");
    }

    public void testAlternativeSyntax_06() throws Exception {
        match2(   "while (true):\n"
                + "    while(false)^:\n"
                + "        if ($a == 1):\n"
                + "\n            "
                + "        endif;\n"
                + "    ^endwhile;\n"
                + "endwhile;\n");
    }

    public void testAlternativeSyntax_07() throws Exception {
        match2(   "switch ($i)^:\n"
                + "    case 22:\n"
                + "        $i = 44;\n"
                + "        break;\n"
                + "    case 33:\n"
                + "    case 44:\n"
                + "        $i = 55;\n"
                + "        break;\n"
                + "    default:\n"
                + "        $i = 66;\n"
                + "^endswitch;\n");
    }

    public void testIssue240157_01() throws Exception {
        matchesBackward("if (isSomething@(^~)){}");
        matchesForward("if (isSomething~(^@)){}");
    }

    public void testIssue240157_02() throws Exception {
        matchesBackward("if (isSomething~(@)^){}");
        matchesForward("if ~(isSomething~()^@){}");
    }

    public void testIssue240157_03() throws Exception {
        matchesBackward("if ~(isSomething()@)^{}");
        matchesForward("if (isSomething())^@{~}");
    }

    public void testIssue240157_04() throws Exception {
        matchesBackward("if (isSomething())@{^~}");
        matchesForward("if (isSomething())~{^@}");
    }

    public void testIssue240157_05() throws Exception {
        matchesBackward("if (isSomething())~{@}^");
    }

    public void testIssue240157_06() throws Exception {
        matchesBackward("if (isSomething^@(~)){}");
        matchesForward("if (isSomething^@(~)){}");
    }

    public void testIssue240157_07() throws Exception {
        matchesBackward("if @(^isSomething()~){}");
    }

    public void testIssue240157_08() throws Exception {
        matchesBackward("if @^(isSomething()~){}");
        matchesForward("if @^(isSomething()~){}");
    }

    public void testIssue240157_09() throws Exception {
        matchesBackward("if (isSomething()) ^@{~}");
        matchesForward("if (isSomething()) ^@{~}");
    }

    public void testIssue240157_10() throws Exception {
        matchesBackward("echo \"Some string with braced @${^variables[ $index ]~} in it.\";");
    }

    public void testIssue240157_11() throws Exception {
        matchesBackward("echo \"Some string with braced ${variables~[ $index @]^} in it.\";");
    }

    public void testIssue240157_AlternativeSyntax_01() throws Exception {
        matchesBackward(
                "if ($i == 0) @:^\n"
                + "    if ($j == 0) :\n"
                + "    endif;\n"
                + "~elseif ($i == 1):\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "endif;\n"
                + "\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_02() throws Exception {
        matchesBackward(
                "if ($i == 0) ~:\n"
                + "    if ($j == 0) :\n"
                + "    endif;\n"
                + "@elseif^ ($i == 1):\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "endif;\n"
                + "\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_03() throws Exception {
        matchesBackward(
                "if ($i == 0) :\n"
                + "    if ($j == 0) :\n"
                + "    endif;\n"
                + "elseif ($i == 1):\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else~:\n"
                + "        $l = 22;\n"
                + "    @endif^;\n"
                + "endif;\n"
                + "\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_04() throws Exception {
        matchesBackward(
                "if ($i == 0) ^@:\n"
                + "    if ($j == 0) :\n"
                + "    endif;\n"
                + "~elseif ($i == 1):\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "endif;\n"
                + "\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_05() throws Exception {
        matchesBackward(
                "if ($i == 0) :\n"
                + "    if ($j == 0) :\n"
                + "    endif;\n"
                + "elseif ($i == 1)~:\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "@en^dif;\n"
                + "\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_06() throws Exception {
        matchesBackward(
                "for ($i = 0; $i < count($array); $i++) ~:\n"
                + "    for ($i = 0; $i < count($array); $i++) :\n"
                + "    endfor;\n"
                + "@endfor^;"
                + "\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_07() throws Exception {
        matchesBackward(
                "for ($i = 0; $i < count($array); $i++) :\n"
                + "    for ($i = 0; $i < count($array); $i++) ~:\n"
                + "    ^@endfor;\n"
                + "endfor;"
                + "\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_08() throws Exception {
        matchesBackward(
                "while (true)~:\n"
                + "    while(false):\n"
                + "        if ($a == 1):\n"
                + "\n            "
                + "        endif;\n"
                + "    endwhile;\n"
                + "@endwhile^;\n");
    }

    public void testIssue240157_AlternativeSyntax_09() throws Exception {
        matchesBackward(
                "while (true)~:\n"
                + "    while(false):\n"
                + "        if ($a == 1):\n"
                + "\n            "
                + "        endif;\n"
                + "    endwhile;\n"
                + "^@endwhile;\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_10() throws Exception {
        matchesBackward(
                "switch ($i)~:\n"
                + "    case 22:\n"
                + "        $i = 44;\n"
                + "        break;\n"
                + "    case 33:\n"
                + "    case 44:\n"
                + "        $i = 55;\n"
                + "        break;\n"
                + "    default:\n"
                + "        $i = 66;\n"
                + "@endswitch^;\n"
        );
    }

    public void testIssue240157_AlternativeSyntax_11() throws Exception {
        matchesBackward(
                "switch ($i)~:\n"
                + "    case 22:\n"
                + "        $i = 44;\n"
                + "        break;\n"
                + "    case 33:\n"
                + "    case 44:\n"
                + "        $i = 55;\n"
                + "        break;\n"
                + "    default:\n"
                + "        $i = 66;\n"
                + "^@endswitch;\n"
        );
    }

    // PHP 8.0
    public void testMatchExpression_01() throws Exception {
        matchesBackward(""
                + "$x = 2;\n"
                + "$result = match ($x) ~{\n"
                + "    1 => '1',\n"
                + "    2 => '2',\n"
                + "    default => 10,\n"
                + "@}^;\n"
        );
        matchesForward(""
                + "$x = 2;\n"
                + "$result = match ($x) @^{\n"
                + "    1 => '1',\n"
                + "    2 => '2',\n"
                + "    default => 10,\n"
                + "~};\n"
        );
    }

    public void testAttributeSyntax_01() throws Exception {
        matchesBackward(""
                + "~#[A(1)@]^\n"
                + "class AttributeSyntax {}"
        );
        matchesForward(""
                + "@^#[A(1)~]\n"
                + "class AttributeSyntax {}"
        );
    }

    public void testAttributeSyntax_02() throws Exception {
        matchesBackward(""
                + "~#[A(1), B(\"param\"), \\C\\Test()@]^\n"
                + "class AttributeSyntax {}"
        );
        matchesForward(""
                + "@#^[A(1), B(\"param\"), \\C\\Test()~]\n"
                + "class AttributeSyntax {}"
        );
    }

    public void testAttributeSyntax_03() throws Exception {
        matchesBackward(""
                + "~#[\n"
                + "    A(1),\n"
                + "    A(2),\n"
                + "    A(3),\n"
                + "    A(4),\n"
                + "@]^\n"
                + "class AttributeSyntax {}"
        );
        matchesForward(""
                + "@^#[\n"
                + "    A(1),\n"
                + "    A(2),\n"
                + "    A(3),\n"
                + "    A(4),\n"
                + "~]\n"
                + "class AttributeSyntax {}"
        );
    }

    public void testAttributeSyntax_04a() throws Exception {
        matchesBackward(""
                + "~#[\n"
                + "    A([\n"
                + "        1,\n"
                + "        2,\n"
                + "    ]),\n"
                + "@]^\n"
                + "class AttributeSyntax {}"
        );
        matchesForward(""
                + "@^#[\n"
                + "    A([\n"
                + "        1,\n"
                + "        2,\n"
                + "    ]),\n"
                + "~]\n"
                + "class AttributeSyntax {}"
        );
    }

    public void testAttributeSyntax_04b() throws Exception {
        matchesBackward(""
                + "#[\n"
                + "    A(~[\n"
                + "        1,\n"
                + "        2,\n"
                + "    @]^),\n"
                + "]\n"
                + "class AttributeSyntax {}"
        );
        matchesForward(""
                + "#[\n"
                + "    A(@^[\n"
                + "        1,\n"
                + "        2,\n"
                + "    ~]),\n"
                + "]\n"
                + "class AttributeSyntax {}"
        );
    }

    public void testAttributeSyntax_05a() throws Exception {
        matchesBackward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "~{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method(#[Class1(6)] $param1, #[Class1('foo', 'bar', 7)] int $pram2) {}\n"
                + "\n"
                + "@}^"
        );
        matchesForward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "@^{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method(#[Class1(6)] $param1, #[Class1('foo', 'bar', 7)] int $pram2) {}\n"
                + "\n"
                + "~}"
        );
    }

    public void testAttributeSyntax_05b() throws Exception {
        matchesBackward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method~(#[Class1(6)] $param1, #[Class1('foo', 'bar', 7)] int $pram2@)^ {}\n"
                + "\n"
                + "}"
        );
        matchesForward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method@^(#[Class1(6)] $param1, #[Class1('foo', 'bar', 7)] int $pram2~) {}\n"
                + "\n"
                + "}"
        );
    }

    public void testAttributeSyntax_05c() throws Exception {
        matchesBackward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method(#[Class1(6)] $param1, ~#[Class1('foo', 'bar', 7)@]^ int $pram2) {}\n"
                + "\n"
                + "}"
        );
        matchesForward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method(#[Class1(6)] $param1, @^#[Class1('foo', 'bar', 7)~] int $pram2) {}\n"
                + "\n"
                + "}"
        );
    }

    public void testAttributeSyntax_05d() throws Exception {
        matchesBackward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method(#[Class1(6)] $param1, #[Class1~('foo', 'bar', 7@)^] int $pram2) {}\n"
                + "\n"
                + "}"
        );
        matchesForward(""
                + "#[Class1(1)]\n"
                + "class AttributeSyntax\n"
                + "{\n"
                + "    #[Class1(4), Class2(4)] // group\n"
                + "    public $staticField;\n"
                + "\n"
                + "    #[Class1(5)]\n"
                + "    public function method(#[Class1(6)] $param1, #[Class1@^('foo', 'bar', 7~)] int $pram2) {}\n"
                + "\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_01a() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::~{self::TES . self::T@}^;\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::@^{self::TES . self::T~};\n"
                + "}"
        );
    }


    public void testDynamicClassConstantFetch_01b() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::@{^self::TES . self::T~};\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::~{self::TES . self::T@^};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_02a() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::~{self::{self::TES} . self::T@}^;\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::@^{self::{self::TES} . self::T~};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_02b() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::@{^self::{self::TES} . self::T~};\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::~{self::{self::TES} . self::T@^};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_02c() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::~{self::TES@}^ . self::T};\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::@^{self::TES~} . self::T};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_02d() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::@{^self::TES~} . self::T};\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::~{self::TES@^} . self::T};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_03a() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::~{self::{self::TES}@}^;\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::@^{self::{self::TES}~};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_03b() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::@{^self::{self::TES}~};\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::~{self::{self::TES}@^};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_03c() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::~{self::TES@}^};\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::@^{self::TES~}};\n"
                + "}"
        );
    }

    public void testDynamicClassConstantFetch_03d() throws Exception {
        matchesBackward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::@{^self::TES~}};\n"
                + "}"
        );
        matchesForward(""
                + "class Test {\n"
                + "    public const TEST = self::{self::~{self::TES@^}};\n"
                + "}"
        );
    }

    public void testFindContext_01() throws Exception {
        checkBraceContext("braceContextTest.php", "^} elseif ($i == 1) { // if", true);
    }

    public void testFindContext_02() throws Exception {
        checkBraceContext("braceContextTest.php", "^} else { // elseif", true);
    }

    public void testFindContext_03() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // else", true);
    }

    public void testFindContext_04() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // if2", true);
    }

    public void testFindContext_05() throws Exception {
        checkBraceContext("braceContextTest.php", "^elseif ($i == 1) : // alternative if", true);
    }

    public void testFindContext_06() throws Exception {
        checkBraceContext("braceContextTest.php", "^else : // alternative elseif", true);
    }

    public void testFindContext_07() throws Exception {
        checkBraceContext("braceContextTest.php", "^endif; // alternative else", true);
    }

    public void testFindContext_08() throws Exception {
        checkBraceContext("braceContextTest.php", "^else : // alternative nested if", true);
    }

    public void testFindContext_09() throws Exception {
        checkBraceContext("braceContextTest.php", "^endif; // alternative nested else", true);
    }

    public void testFindContext_10() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // while", true);
    }

    public void testFindContext_11() throws Exception {
        checkBraceContext("braceContextTest.php", "^endwhile; // alternative while", true);
    }

    public void testFindContext_12() throws Exception {
        checkBraceContext("braceContextTest.php", "^} while (true); // do", true);
    }

    public void testFindContext_13() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // for", true);
    }

    public void testFindContext_14() throws Exception {
        checkBraceContext("braceContextTest.php", "^endfor; // alternative for", true);
    }

    public void testFindContext_15() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // foreach", true);
    }

    public void testFindContext_16() throws Exception {
        checkBraceContext("braceContextTest.php", "^endforeach; // alternative foreach", true);
    }

    public void testFindContext_17() throws Exception {
        checkBraceContext("braceContextTest.php", "^endswitch; // alternative switch", true);
    }

    public void testFindContext_18() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // foo method", true);
    }

    public void testFindContext_19() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // Foo class", true);
    }

    public void testFindContext_20() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // bar method", true);
    }

    public void testFindContext_21() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // Bar class", true);
    }

    public void testFindContext_22() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // FooInterface", true);
    }

    public void testFindContext_23() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // AbstractClass", true);
    }

    public void testFindContext_24() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // FooTrait", true);
    }

    public void testFindContext_25() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // complex syntax", true);
    }

    public void testFindContext_26() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // try", true);
    }

    public void testFindContext_27() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // catch", true);
    }

    public void testFindContext_28() throws Exception {
        checkBraceContext("braceContextTest.php", "^} // finally", true);
    }

    public void testFindContext_29() throws Exception {
        checkBraceContext("braceContextUseTraitTest.php", "^} // use", true);
    }

    // PHP 8.0
    public void testFindContextForMatchExpression_01() throws Exception {
        checkBraceContext("php80/matchExpression_01.php", "^}; // match", true);
    }

    public void testFindContextForMatchExpression_02a() throws Exception {
        checkBraceContext("php80/matchExpression_02.php", "^}; // match1", true);
    }

    public void testFindContextForMatchExpression_02b() throws Exception {
        checkBraceContext("php80/matchExpression_02.php", "    ^}, // match2", true);
    }

    public void testFindContextForMatchExpression_03() throws Exception {
        checkBraceContext("php80/matchExpression_03.php", "^}; // match", true);
    }

    public void testFindContextForMatchExpression_04() throws Exception {
        checkBraceContext("php80/matchExpression_04.php", "        ^}; // match", true);
    }

    public void testFindContextForEnumerations_01() throws Exception {
        checkBraceContext("php81/enumerations.php", "^} // enum 1", true);
    }

    public void testFindContextForEnumerations_02() throws Exception {
        checkBraceContext("php81/enumerations.php", "^} // enum 2", true);
    }

    public void testFindContextForEnumerations_03() throws Exception {
        checkBraceContext("php81/enumerations.php", "^} // enum 3", true);
    }

    public void testFindContextForEnumerations_04() throws Exception {
        checkBraceContext("php81/enumerations.php", "^} // enum 4", true);
    }

    public void testFindContextForEnumerations_05() throws Exception {
        checkBraceContext("php81/enumerations.php", "^} // enum 5", true);
    }

    public void testFindContextForEnumerations_06() throws Exception {
        checkBraceContext("php81/enumerations.php", "^} // enum 6", true);
    }

    private void matchesBackward(String original) throws BadLocationException {
        matches(original, true);
    }

    private void matchesForward(String original) throws BadLocationException {
        matches(original, false);
    }

    /**
     * "^": a caret position, "@": an origin position, "~": a matching position.
     *
     * @param original code
     * @param backward {@code true} if search backward, otherwise {@code false}
     * @throws BadLocationException
     */
    private void matches(final String original, boolean backward) throws BadLocationException {
        BracesMatcherFactory factory = MimeLookup.getLookup(getPreferredMimeType()).lookup(BracesMatcherFactory.class);
        String wrappedOriginal = wrapAsPhp(original);
        int caretPosition = wrappedOriginal.replaceAll("(@|~)", "").indexOf('^');
        int originPosition = wrappedOriginal.replaceAll("(\\^|~)", "").indexOf('@');
        int matchingPosition = wrappedOriginal.replaceAll("(\\^|@)", "").indexOf('~');
        wrappedOriginal = wrappedOriginal.replaceAll("(\\^|@|~)", "");

        BaseDocument doc = getDocument(wrappedOriginal);

        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, caretPosition, backward, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int[] origin = null, matches = null;
        try {
            origin = matcher.findOrigin();
            matches = matcher.findMatches();
        } catch (InterruptedException ex) {
        }

        assertNotNull("Did not find origin for " + " position " + originPosition, origin);
        assertNotNull("Did not find matches for " + " position " + matchingPosition, matches);

        assertEquals("Incorrect origin", originPosition, origin[0]);
        assertEquals("Incorrect matches", matchingPosition, matches[0]);
    }

    /**
     * Check BraceContexts. To check brace context reanges, Please wrap them in
     * {@code |>MARK_BC: <|} or {@code |>MARK_RELATED_BC: <|} in a
     * testFileName.testCaseName.bracecontext file.
     *
     * @param filePath Path of the file which is in testfiles/bracemacthing/
     * directory.
     * @param caretLine The text contained in the line which has the caret.
     * @param backward {@code true} if searching barckward in the BraceMatcher
     * class, otherwise {@code false}.
     * @throws Exception
     */
    private void checkBraceContext(String filePath, String caretLine, boolean backward) throws Exception {
        Source testSource = getTestSource(getTestFile(TEST_DIRECTORY + filePath));

        Document doc = testSource.getDocument(true);
        final int caretOffset = getCaretOffset(doc.getText(0, doc.getLength()), caretLine);

        BracesMatcherFactory factory = MimeLookup.getLookup(getPreferredMimeType()).lookup(BracesMatcherFactory.class);
        MatcherContext matcherContext = BracesMatchingTestUtils.createMatcherContext(doc, caretOffset, backward, 1);
        PHPBracesMatcher matcher = (PHPBracesMatcher) factory.createMatcher(matcherContext);
        int[] origin = null, matches = null;
        try {
            origin = matcher.findOrigin();
            matches = matcher.findMatches();
        } catch (InterruptedException ex) {
        }
        assertNotNull(origin);
        assertNotNull(matches);

        BraceContext context = matcher.findContext(origin[0]);
        assertNotNull(context);

        String result = annoteteBraceContextRanges(doc, context);
        assertDescriptionMatches(testSource.getFileObject(), result, true, ".bracecontext", true);
    }

    private String annoteteBraceContextRanges(Document document, final BraceContext context) throws BadLocationException {
        List<BraceContext> relatedContexts = new ArrayList<>();
        BraceContext relatedContext = context.getRelated();
        while (relatedContext != null) {
            relatedContexts.add(relatedContext);
            relatedContext = relatedContext.getRelated();
        }
        Collections.reverse(relatedContexts);

        StringBuilder sb = new StringBuilder();
        int index = 0;
        int contextStart = context.getStart().getOffset();
        int contextEnd = context.getEnd().getOffset();

        // related context
        for (BraceContext related : relatedContexts) {
            int start = related.getStart().getOffset();
            int end = related.getEnd().getOffset();
            sb.append(document.getText(index, start - index));
            sb.append("|>MARK_RELATED_BC:");
            sb.append(document.getText(start, end - start));
            sb.append("<|");
            index = end;
            assertTrue("Related context offset > context offset", end <= contextStart);
        }

        // context
        sb.append(document.getText(index, contextStart - index));
        sb.append("|>MARK_BC:");
        sb.append(document.getText(contextStart, contextEnd - contextStart));
        sb.append("<|");
        index = contextEnd;
        sb.append(document.getText(index, document.getLength() - index));
        return sb.toString();
    }

}
