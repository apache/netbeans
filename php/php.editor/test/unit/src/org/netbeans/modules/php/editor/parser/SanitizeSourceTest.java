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

package org.netbeans.modules.php.editor.parser;

import java.util.Collections;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class SanitizeSourceTest extends ParserTestBase {

    public SanitizeSourceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSanitizeClass01() throws Exception {
        performTest("sanitize/sanitize001");
    }

    public void testSanitizeClass02() throws Exception {
        performTest("sanitize/sanitize002");
    }

    public void testSanitizeClass03() throws Exception {
        performTest("sanitize/sanitize004");
    }
    public void testSanitizeTopContext() throws Exception {
        performTest("sanitize/sanitize003");
    }

    public void testMissingEndCurly() throws Exception {
        // one } at the end
        performTest("sanitize/curly01");
    }

    public void testMissingEndCurly2() throws Exception {
        // more } at the end
        performTest("sanitize/curly02");
    }

    public void testMissingEndCurly3() throws Exception {
        // more } at the end without end php ?>
        performTest("sanitize/curly03");
    }

    public void testMissingEndCurly4() throws Exception {
        // inner functions
        performTest("sanitize/curly04");
    }

    public void testMissingEndCurly5() throws Exception {
        // non finished class and method and blog
        performTest("sanitize/curly05");
    }

    public void testMissingEndCurly6() throws Exception {
        // non finished class and method and blog 2
        performTest("sanitize/curly06", "if ($a) {^");
    }

    public void testMissingEndCurly7() throws Exception {
        // non finieshed class and method
        performTest("sanitize/curly07");
    }

    public void testMissingEndCurly8() throws Exception {
        // non finished class
        performTest("sanitize/curly08");
    }

    public void testMissingEndCurly9() throws Exception {
        // non finished class
        performTest("sanitize/sanitize005");
    }

    public void testUnfinishedVar() throws Exception {
        // non finished class
        performTest("sanitize/sanitize006");
    }

    // testing when class declaration is in class declaration
    public void testCDInCD01() throws Exception {
        performTest("sanitize/sanitize007");
    }
    public void testCDInCD02() throws Exception {
        performTest("sanitize/sanitize008");
    }
    public void testCDInCD03() throws Exception {
        performTest("sanitize/sanitize009");
    }
    public void testCDInCD04() throws Exception {
        performTest("sanitize/sanitize010");
    }
    public void testCDInCD05() throws Exception {
        performTest("sanitize/sanitize011");
    }
    public void testCDInCD06() throws Exception {
        performTest("sanitize/sanitize012", "/*marker*/public functio^");
    }
    public void testCDInCD07() throws Exception {
        performTest("sanitize/sanitize013","/*marker*/public function^");
    }
    public void testCDInCD08() throws Exception {
        performTest("sanitize/sanitize014", "/*marker*/public function ^");
    }
    public void testCDInCD09() throws Exception {
        performTest("sanitize/sanitize015", "/*marker*/public function name(^");
    }
    public void testCDInCD10() throws Exception {
        performTest("sanitize/sanitize016", "/*marker*/public function name(){^");
    }

    public void testIssue204588() throws Exception {
        performTest("sanitize/issue204588", "function __con^");
    }

    // disabling the test, unitl I find out what is wrong
//    public void test145494() throws Exception {
//        performTest("sanitize/sanitize145494");
//    }

    public void testDoNotDeleteCurly01() throws Exception {
        performTest("sanitize/sanitize017");
    }

    public void testCase01() throws Exception {
        performTest("sanitize/case01", "/*marker*/case self::^");
    }

    public void test149424() throws Exception {
        performTest("sanitize/issue149424");
    }

    public void testNullableTypePrefix01() throws Exception {
        performTest("sanitize/nullableTypes01");
    }

    public void testNullableTypePrefix02() throws Exception {
        performTest("sanitize/nullableTypes02");
    }

    public void testNullableTypePrefix03() throws Exception {
        performTest("sanitize/nullableTypes03");
    }

    public void testNullableTypePrefix04() throws Exception {
        performTest("sanitize/nullableTypes04");
    }

    public void testNullableTypePrefix05() throws Exception {
        performTest("sanitize/nullableTypes05");
    }

    public void testNullableTypePrefix06() throws Exception {
        performTest("sanitize/nullableTypes06");
    }

    public void testNullableTypePrefix07() throws Exception {
        performTest("sanitize/nullableTypes07");
    }

    public void testNullableTypePrefix08() throws Exception {
        performTest("sanitize/nullableTypes08");
    }

    public void testNullableTypePrefix09() throws Exception {
        performTest("sanitize/nullableTypes09");
    }

    public void testNullableTypePrefix10() throws Exception {
        performTest("sanitize/nullableTypes10");
    }

    public void testNullableTypePrefix11() throws Exception {
        performTest("sanitize/nullableTypes11");
    }

    public void testNullableTypePrefix12() throws Exception {
        performTest("sanitize/nullableTypes12");
    }

    public void testNullableTypePrefix13() throws Exception {
        performTest("sanitize/nullableTypes13");
    }

    public void testNullableTypePrefix14() throws Exception {
        performTest("sanitize/nullableTypes14");
    }

    public void testNullableTypePrefix15() throws Exception {
        performTest("sanitize/nullableTypes15");
    }

    public void testNullableTypePrefix16() throws Exception {
        performTest("sanitize/nullableTypes16");
    }

    public void testUnionTypeParameter01() throws Exception {
        performTest("sanitize/unionTypesParameter01");
    }

    public void testUnionTypeParameter02() throws Exception {
        performTest("sanitize/unionTypesParameter02");
    }

    public void testUnionTypeParameter03() throws Exception {
        performTest("sanitize/unionTypesParameter03");
    }

    public void testUnionTypeParameter04() throws Exception {
        performTest("sanitize/unionTypesParameter04");
    }

    public void testUnionTypeParameter05() throws Exception {
        performTest("sanitize/unionTypesParameter05");
    }

    public void testConstructorPropertyPromotionParameter01() throws Exception {
        performTest("sanitize/constructorPropertyPromotionParameter01");
    }

    protected String getTestResult(String filename) throws Exception {
        return getTestResult(filename, null);
    }

    protected String getTestResult(String filename, String caretLine) throws Exception {
        FileObject testFile = getTestFile("testfiles/" + filename + ".php");

        Source testSource = getTestSource(testFile);
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        final StringBuffer textresult = new StringBuffer();
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                if (r != null) {
                    assertTrue(r instanceof ParserResult);
                    PHPParseResult phpResult = (PHPParseResult)r;
                    Program program = phpResult.getProgram();

                    if (program != null) {
                        textresult.append((new PrintASTVisitor()).printTree(program, 0));
                    } else {
                        textresult.append("Program is null");
                    }
                }
            }
        });

        return textresult.toString();
    }


    /*protected String getTestResult(String filename) throws Exception {
        return null;
        GsfTestCompilationInfo info = getInfo("testfiles/" + filename + ".php");
        StringBuffer textresult = new StringBuffer();
        int offset = info.getText().indexOf('^');
        if (offset > -1) {
            String content = info.getText();
            content = content.substring(0, offset) + content.substring(offset+1, content.length()-1);
            info = getInfoForText(content, "testFile.php");
            info.setCaretOffset(offset);
        }

        ParserResult result = info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);

        if (result == null) {
            textresult.append("Not possible to parse");
        } else {
            PHPParseResult phpResult = (PHPParseResult)result;
            Program program = phpResult.getProgram();

            if (program != null){
                textresult.append((new PrintASTVisitor()).printTree(program, 0));
            }
            else {
                textresult.append("Program is null");
            }
        }
        return textresult.toString();
    }*/
}
