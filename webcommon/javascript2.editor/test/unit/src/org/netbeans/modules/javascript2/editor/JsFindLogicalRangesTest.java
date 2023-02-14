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
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Petr Pisl
 */
public class JsFindLogicalRangesTest extends JsTestBase {

    public JsFindLogicalRangesTest(String testName) {
        super(testName);
    }

    public void testFLR_01() throws Exception {
        checkFindLogicalRanges("/testfiles/keyStrokeHandler/simple01.js", "var listdirect^ory = require('./listdirectory');");
    }
    
    public void testFLR_02() throws Exception {
        checkFindLogicalRanges("/testfiles/keyStrokeHandler/simple01.js", "console.log('Listin^g of', dir, ':');");
    }
    
    public void testFLR_03() throws Exception {
        checkFindLogicalRanges("/testfiles/keyStrokeHandler/simple01.js", "listdirectory(dir, extension, function(err, li^st) {");
    }
    
    public void testFLR_04() throws Exception {
        checkFindLogicalRanges("/testfiles/keyStrokeHandler/simple01.js", "return console.error('Error occurred:', e^rr);");
    }

    private void checkFindLogicalRanges(final String filePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(filePath));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override
            void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult info = (ParserResult) r;

                JsKeyStrokeHandler handler = new JsKeyStrokeHandler();
                List<OffsetRange> ranges = handler.findLogicalRanges(info, caretOffset);
                List<OffsetRange> reverse = new ArrayList<>(ranges);
                Collections.reverse(reverse);
                int offset = 0;
                int rangesIndex = 0;
                OffsetRange currentRange = ranges.get(ranges.size() - rangesIndex - 1);
                StringBuilder annotatedSource = new StringBuilder();
                String text = info.getSnapshot().getText().toString();
                HashMap<Integer, String> annotations = new HashMap<>();
                for (OffsetRange range : reverse) {
                    rangesIndex++;
                    String annotation = annotations.get(range.getStart());
                    if (annotation == null) {
                        annotation = "[LR #" + rangesIndex + " Start]";
                    } else {
                        annotation = annotation + "[LR #" + rangesIndex + " Start]";
                    }
                    annotations.put(range.getStart(), annotation);

                    annotation = annotations.get(range.getEnd());
                    if (annotation == null) {
                        annotation = "[LR #" + rangesIndex + " End]";
                    } else {
                        annotation = "[LR #" + rangesIndex + " End]" + annotation;
                    }
                    annotations.put(range.getEnd(), annotation);
                }
                while (offset < text.length()) {
                    String annotation = annotations.get(offset);
                    if (annotation != null) {
                        annotatedSource.append(annotation);
                    }
                    annotatedSource.append(text.charAt(offset));
                    offset++;
                }
                String annotation = annotations.get(offset);
                if (annotation != null) {
                    annotatedSource.append(annotation);
                }
                assertDescriptionMatches(filePath, annotatedSource.toString(), true, ".logicalRange");
            }

        });
    }

}
