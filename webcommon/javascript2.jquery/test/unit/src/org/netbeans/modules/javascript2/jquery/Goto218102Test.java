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
package org.netbeans.modules.javascript2.jquery;

import java.util.Collections;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Petr Pisl
 */
public class Goto218102Test extends JsTestBase {
    
    public Goto218102Test(String testName) {
        super(testName);
    }
    // TODO disable, because it's failing in teh continues build: server. Should be corrected.
    public void testIssue218102_01() throws Exception {
//        checkOffsetRange("testfiles/jquery/218102/issue218102.js", "jQuery(\".do^g\").get(1);", 8, 12);
    }
    
    protected void checkOffsetRange(String file, String caretLine, int start, int end) throws Exception {
        OffsetRange computed = findReferenceSpan(file, caretLine);
        assertEquals(new OffsetRange(start, end), computed);
    }
    
    protected OffsetRange findReferenceSpan(String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        final int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
        enforceCaretOffset(testSource, caretOffset);

        final OffsetRange [] location = new OffsetRange[] { OffsetRange.NONE };
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                DeclarationFinder finder = getFinder();
                location[0] = finder.getReferenceSpan(resultIterator.getSnapshot().getSource().getDocument(false), caretOffset);
            }
        });

        return location[0];
    }

    
}
