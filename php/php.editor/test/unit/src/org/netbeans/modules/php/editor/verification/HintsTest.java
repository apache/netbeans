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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class HintsTest extends PHPHintsTestBase {

    public HintsTest(String testName) {
        super(testName);
    }

    public void testAmbiguousComparisonHint() throws Exception {
        checkHints(new AmbiguousComparisonHint(), "testAmbiguousComparisonHint.php");
    }

    public void testVarDocSuggestion() throws Exception {
        checkHints(new VarDocSuggestion(), "testVarDocSuggestion.php", "$foo^Bar;");
    }

    public void testAssignVariableSuggestion() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "myFnc();^");
    }

    public void testAssignVariableSuggestion_02() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "die('message');^");
    }

    public void testAssignVariableSuggestion_03() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "exit('message');^");
    }

    public void testAssignVariableSuggestion_04() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "new class {};^");
    }

    public void testIdenticalComparisonSuggestion() throws Exception {
        checkHints(new IdenticalComparisonSuggestion(), "testIdenticalComparisonSuggestion.php", "if ($a == true)^ {}");
    }

    public void testAddUseImportSuggestion_01() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testAddUseImportSuggestion_01.php", "new Foo\\Bar();^");
    }

    public void testAddUseImportSuggestion_02() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testAddUseImportSuggestion_02.php", "new Foox\\Barx();^");
    }

    public void testIssue258480_1() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testIssue258480_1.php", "$x = date2();^");
    }

    public void testIssue258480_2() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testIssue258480_2.php", "$x = date2();^");
    }

    public void testIfBracesHint_01() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_01.php");
    }

    public void testIfBracesHint_02() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_02.php");
    }

    public void testIfBracesHint_03() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_03.php");
    }

    public void testDoWhileBracesHint() throws Exception {
        checkHints(new BracesHint.DoWhileBracesHint(), "testDoWhileBracesHint.php");
    }

    public void testWhileBracesHint() throws Exception {
        checkHints(new BracesHint.WhileBracesHint(), "testWhileBracesHint.php");
    }

    public void testForBracesHint() throws Exception {
        checkHints(new BracesHint.ForBracesHint(), "testForBracesHint.php");
    }

    public void testForEachBracesHint() throws Exception {
        checkHints(new BracesHint.ForEachBracesHint(), "testForEachBracesHint.php");
    }

    public void testGetSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.GetSuperglobalHint(), "testGetSuperglobalsHint.php");
    }

    public void testPostSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.PostSuperglobalHint(), "testPostSuperglobalsHint.php");
    }

    public void testCookieSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.CookieSuperglobalHint(), "testCookieSuperglobalsHint.php");
    }

    public void testServerSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.ServerSuperglobalHint(), "testServerSuperglobalsHint.php");
    }

    public void testEnvSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.EnvSuperglobalHint(), "testEnvSuperglobalsHint.php");
    }

    public void testRequestSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.RequestSuperglobalHint(), "testRequestSuperglobalsHint.php");
    }

    public void testEmptyStatementHint() throws Exception {
        checkHints(new EmptyStatementHint(), "testEmptyStatementHint.php");
    }

    public void testUnreachableStatementHint() throws Exception {
        checkHints(new UnreachableStatementHint(), "testUnreachableStatementHint.php");
    }

    public void testUnreachableStatementHint_02() throws Exception {
        checkHints(new UnreachableStatementHint(), "testUnreachableStatementHint_02.php");
    }

    public void testParentConstructorCallHint() throws Exception {
        checkHints(new ParentConstructorCallHint(), "testParentConstructorCallHint.php");
    }

    public void testIssue224940() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testIssue224940.php", "echo \"foo $whatever \\n^\";");
    }

    public void testErrorControlOperatorHint() throws Exception {
        checkHints(new ErrorControlOperatorHint(), "testErrorControlOperatorHint.php");
    }

    public void testClosingDelimUseCase01() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase01.php");
    }

    public void testClosingDelimUseCase02() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase02.php");
    }

    public void testClosingDelimUseCase03() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase03.php");
    }

    public void testIssue227081() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue227081.php");
    }

    public void testIssue229529() throws Exception {
        checkHints(new AmbiguousComparisonHint(), "testIssue229529.php");
    }

    public void testIssue234983() throws Exception {
        checkHints(new ParentConstructorCallHint(), "testIssue234983.php");
    }

    public void testTooManyReturnStatements() throws Exception {
        checkHints(new TooManyReturnStatementsHint(), "testTooManyReturnStatements.php");
    }

    public void testIssue237726_01() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237726_01.php");
    }

    public void testIssue237726_02() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237726_02.php");
    }

    public void testIssue237768() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237768.php");
    }

    public void testWrongParamNameHint() throws Exception {
        checkHints(new WrongParamNameHint(), "testWrongParamNameHint.php");
    }

    public void testArraySyntaxSuggestion_01() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "$foo = ar^ray(");
    }

    public void testArraySyntaxSuggestion_02() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "11, ^22,");
    }

    public void testArraySyntaxSuggestion_03() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "2, ^3);");
    }

    public void testArraySyntaxSuggestion_04() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "$boo = a^rray(");
    }

    public void testArraySyntaxSuggestion_05() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", "\"sdf\" => array(^1, 2, 3)");
    }

    public void testArraySyntaxSuggestion_06() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_54), "testArraySyntaxSuggestion.php", ")^; //huhu");
    }

    public void testIssue248013_01() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "$foo = ar^ray(");
    }

    public void testIssue248013_02() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "11, ^22,");
    }

    public void testIssue248013_03() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "2, ^3);");
    }

    public void testIssue248013_04() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "$boo = a^rray(");
    }

    public void testIssue248013_05() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", "\"sdf\" => array(^1, 2, 3)");
    }

    public void testIssue248013_06() throws Exception {
        checkHints(new ArraySyntaxSuggestionStub(PhpVersion.PHP_53), "testArraySyntaxSuggestion.php", ")^; //huhu");
    }

    public void testIssue259026_01() throws Exception {
        checkHints(new EmptyStatementHint(), "testIssue259026_01.php");
    }

    public void testIssue259026_02() throws Exception {
        checkHints(new EmptyStatementHint(), "testIssue259026_02.php");
    }

    public void testIssue259026_03() throws Exception {
        checkHints(new EmptyStatementHint(), "testIssue259026_03.php");
    }

    public void testIssue259026Fix_02() throws Exception {
        applyHint(new EmptyStatementHint(), "testIssue259026_02.php", "declare(strict_types=1);;^", "Empty Statement");
    }

    public void testIssue259026Fix_03() throws Exception {
        applyHint(new EmptyStatementHint(), "testIssue259026_03.php", "$test1 = 1;;^", "Empty Statement");
    }

    public void testDeclareStrictTypes_01a() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_01.php", "^<?php");
    }

    public void testDeclareStrictTypes_01b() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_01.php", "^<?php");
    }

    public void testDeclareStrictTypes_02a() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_02.php", "<?p^hp // first line");
    }

    public void testDeclareStrictTypes_02b() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_02.php", "<?ph^p // first line");
    }

    public void testDeclareStrictTypes_02c() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_02.php", "<?php ec^ho \"multiple open tags\" ?>");
    }

    public void testDeclareStrictTypes_02d() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_02.php", "<?php echo \"multiple open tags\"^ ?>");
    }

    public void testDeclareStrictTypes_03a() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_03.php", "<?p^hp");
    }

    public void testDeclareStrictTypes_03b() throws Exception {
        checkHints(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_56), "testDeclareStrictTypesSuggestion_03.php", "<?p^hp");
    }

    public void testDeclareStrictTypesFix_01() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_01.php", "<?p^hp", "Add declare(strict_types=1)");
    }

    public void testDeclareStrictTypesFix_02a() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_02.php", "<?p^hp // first line", "Add declare(strict_types=1)");
    }

    public void testDeclareStrictTypesFix_02b() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_70), "testDeclareStrictTypesSuggestion_02.php", "<?php ec^ho \"multiple open tags\" ?>", "Add declare(strict_types=1)");
    }

    public void testDeclareStrictTypesFix_03() throws Exception {
        applyHint(new DeclareStrictTypesSuggestionStub(PhpVersion.PHP_71), "testDeclareStrictTypesSuggestion_03.php", "<?p^hp", "Add declare(strict_types=1)");
    }

    //~ Inner classes
    private static final class ArraySyntaxSuggestionStub extends ArraySyntaxSuggestion {

        private final PhpVersion phpVersion;


        ArraySyntaxSuggestionStub(PhpVersion phpVersion) {
            assert phpVersion != null;
            this.phpVersion = phpVersion;
        }

        @Override
        protected boolean isAtLeastPhp54(FileObject fileObject) {
            return phpVersion.compareTo(PhpVersion.PHP_54) >= 0;
        }

    }

    private static final class DeclareStrictTypesSuggestionStub extends DeclareStrictTypesSuggestion {

        private final PhpVersion phpVersion;

        public DeclareStrictTypesSuggestionStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject fileObject) {
            return phpVersion;
        }

    }

}
