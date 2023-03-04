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
package org.netbeans.modules.refactoring.php.findusages;

import java.util.Collections;
import java.util.concurrent.Future;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.refactoring.php.RefactoringTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class FindUsagesTestBase extends RefactoringTestBase {

    public FindUsagesTestBase(String testName) {
        super(testName);
    }

    @Override
    protected String getTestFolderPathSuffix() {
        return "findusages/" + getTestName();
    }

    protected void findUsages(final String caretLine) throws Exception {
        final String exactFileName = getTestPath();
        final String result = getTestResult(exactFileName, caretLine);
        assertDescriptionMatches(exactFileName, result, true, ".findUsages");
    }

    protected void findUsages(final String caretLine, String filePath) throws Exception {
        final String exactFileName = getTestFolderPath() + "/" + filePath;
        final String result = getTestResult(exactFileName, caretLine);
        assertDescriptionMatches(exactFileName, result, true, ".findUsages");
    }

    private String getTestResult(final String exactFileName, final String caretLine) throws Exception {
        FileObject testFile = getTestFile(exactFileName);
        Source testSource = getTestSource(testFile);
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }
        final String[] result = new String[1];
        // wait for the scan to finish
        Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), new UserTask() {

            @Override
            public void run(final ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof PHPParseResult);
                PHPParseResult phpResult = (PHPParseResult)r;
                StringBuilder sb = new StringBuilder();
                WhereUsedSupport wus = WhereUsedSupport.getInstance(phpResult, caretOffset);
                for (FileObject fileObject : wus.getRelevantFiles()) {
                    wus.collectUsages(fileObject);
                }
                WhereUsedSupport.Results results = wus.getResults();
                for (WhereUsedElement whereUsedElement : results.getResultElements()) {
                    sb.append("Display text: ");
                    sb.append(whereUsedElement.getDisplayText());
                    sb.append("\n");
                    sb.append("File name: ");
                    sb.append(whereUsedElement.getFile().getNameExt());
                    sb.append("\n");
                    sb.append("Name: ");
                    sb.append(whereUsedElement.getName());
                    sb.append("\n");
                    sb.append("Position: ");
                    sb.append("BEGIN: ").append(whereUsedElement.getPosition().getBegin().getOffset()).append(" END: ").append(whereUsedElement.getPosition().getEnd().getOffset());
                    sb.append("\n\n");
                }
                result[0] = sb.toString().trim();
            }
        });
        if (!future.isDone()) {
            future.get();
        }
        return result[0];
    }

}
