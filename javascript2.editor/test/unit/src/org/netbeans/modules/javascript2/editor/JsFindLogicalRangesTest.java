/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static junit.framework.Assert.assertTrue;
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
                List<OffsetRange> reverse = new ArrayList(ranges);
                Collections.reverse(reverse);
                int offset = 0;
                int rangesIndex = 0;
                OffsetRange currentRange = ranges.get(ranges.size() - rangesIndex - 1);
                StringBuilder annotatedSource = new StringBuilder();
                String text = info.getSnapshot().getText().toString();
                HashMap<Integer, String> annotations = new HashMap();
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
