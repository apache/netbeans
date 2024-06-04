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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UsedNamesCollectorTest extends PHPTestBase {

    public UsedNamesCollectorTest(String testName) {
        super(testName);
    }

    public void testUseCase_01() throws Exception {
        performTest("useCase_01", "Homepage^Presenter");
    }

    public void testUseCase_02() throws Exception {
        performTest("useCase_02", "Homepage^Presenter");
    }

    public void testUseCase_03() throws Exception {
        performTest("useCase_03", "Homepage^Presenter");
    }

    public void testUseCase_04() throws Exception {
        performTest("useCase_04", "Homepage^Presenter");
    }

    public void testUseCase_05() throws Exception {
        performTest("useCase_05", "Homepage^Presenter");
    }

    public void testUseCase_06() throws Exception {
        performTest("useCase_06", "Homepage^Presenter");
    }

    public void testUseCase_07() throws Exception {
        performTest("useCase_07", "class Class^Name {");
    }

    public void testUseCase_08() throws Exception {
        performTest("useCase_08", "class Class^Name {");
    }

    public void testUseCase_09() throws Exception {
        performTest("useCase_09", "class Class^Name {");
    }

    public void testUseCase_10() throws Exception {
        performTest("useCase_10", "class Class^Name {");
    }

    public void testIssue209755() throws Exception {
        performTest("issue209755", "class Class^Name {");
    }

    public void testNB4978_01() throws Exception {
        performTest("nb4978_01", "// test^");
    }

    public void testNB4978_02() throws Exception {
        performTest("nb4978_02", "// test^");
    }

    public void testNB4978_03() throws Exception {
        performTest("nb4978_03", "// test^");
    }

    public void testNB4978_04() throws Exception {
        performTest("nb4978_04", "// test^");
    }

    public void testNB4978_05() throws Exception {
        performTest("nb4978_05", "public funct^ion test(?Foo $foo): ?Foo");
    }

    public void testNB4978_06() throws Exception {
        performTest("nb4978_06", "public funct^ion test(?Foo $foo): ?Foo");
    }

    public void testNB4978_07() throws Exception {
        performTest("nb4978_07", "// test^");
    }

    public void testNB4978_08() throws Exception {
        performTest("nb4978_08", "// test^");
    }

    public void testGH6075_01() throws Exception {
        performTest("gh6075_01", "function test(): void ^{");
    }

    public void testGH6075_02() throws Exception {
        performTest("gh6075_02", "function test(): void ^{");
    }

    public void testGH6162_01() throws Exception {
        performTest("gh6162_01", "class Test ^{}");
    }

    public void testGH7123_01() throws Exception {
        performTest("gh7123_01", "class GH7123_01^{}");
    }

    public void testGH7123_02() throws Exception {
        performTest("gh7123_02", "class GH7123_02^{");
    }

    protected void performTest(String fileName, String caretLine) throws Exception {
        String exactFileName = "testfiles/actions/" + fileName + ".php";
        Map<String, List<UsedNamespaceName>> testResult = getTestResult(exactFileName, caretLine);
        String target = createResultString(testResult);
        assertDescriptionMatches(exactFileName, target, false, ".usedNames");
    }

    protected Map<String, List<UsedNamespaceName>> getTestResult(String fileName, String caretLine) throws Exception {
        FileObject testFile = getTestFile(fileName);

        Source testSource = getTestSource(testFile);
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final Map<String, List<UsedNamespaceName>> result = new HashMap<>();
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                if (r != null) {
                    assertTrue(r instanceof ParserResult);
                    PHPParseResult phpResult = (PHPParseResult) r;
                    result.putAll(new UsedNamesCollector(phpResult, caretOffset).collectNames());
                }
            }
        });

        return result;
    }

    private String createResultString(Map<String, List<UsedNamespaceName>> testResult) {
        StringBuilder sb = new StringBuilder();
        SortedSet<String> keys = new TreeSet<>(testResult.keySet());
        for (String key : keys) {
            sb.append("Name: ").append(key).append("\n");
            for (UsedNamespaceName usedNamespaceName : testResult.get(key)) {
                sb.append(" ").append(usedNamespaceName.getName()).append(" --> ").append(usedNamespaceName.getReplaceName()).append(":").append(usedNamespaceName.getOffset()).append("\n");
            }
        }

        return sb.toString();
    }
}
