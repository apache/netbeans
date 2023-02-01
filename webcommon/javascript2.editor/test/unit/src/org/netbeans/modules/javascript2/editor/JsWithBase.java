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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsWithBase extends JsCodeCompletionBase{

    public JsWithBase(String testName) {
        super(testName);
    }
    
    @Override
    protected DeclarationFinder.DeclarationLocation findDeclaration(String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        final int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
        enforceCaretOffset(testSource, caretOffset);

        final DeclarationFinder.DeclarationLocation [] location = new DeclarationFinder.DeclarationLocation[] { null };
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof JsParserResult);
                JsParserResult pr = (JsParserResult) r;
                Model.getModel(pr, false).getGlobalObject();
                DeclarationFinder finder = getFinder();
                location[0] = finder.findDeclaration(pr, caretOffset);
            }
        });

        return location[0];
    }
    
    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
    
    @Override
    protected void checkOccurrences(String relFilePath, String caretLine, final boolean symmetric) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        Document doc = testSource.getDocument(true);
        final int caretOffset = getCaretOffset(doc.getText(0, doc.getLength()), caretLine);

        final OccurrencesFinder finder = getOccurrencesFinder();
        assertNotNull("getOccurrencesFinder must be implemented", finder);
        finder.setCaretPosition(caretOffset);

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult(caretOffset);
                if (r instanceof JsParserResult) {
                    Model.getModel(((JsParserResult)r), false).getGlobalObject();
                    finder.run((ParserResult) r, null);
                    Map<OffsetRange, ColoringAttributes> occurrences = finder.getOccurrences();
                    if (occurrences == null) {
                        occurrences = Collections.emptyMap();
                    }

                    String annotatedSource = annotateFinderResult(resultIterator.getSnapshot(), occurrences, caretOffset);
                    assertDescriptionMatches(resultIterator.getSnapshot().getSource().getFileObject(), annotatedSource, true, ".occurrences");

                    if (symmetric) {
                        // Extra check: Ensure that occurrences are symmetric: Placing the caret on ANY of the occurrences
                        // should produce the same set!!
                        for (OffsetRange range : occurrences.keySet()) {
                            int midPoint = range.getStart() + range.getLength() / 2;
                            finder.setCaretPosition(midPoint);
                            finder.run((ParserResult) r, null);
                            Map<OffsetRange, ColoringAttributes> alternates = finder.getOccurrences();
                            assertEquals("Marks differ between caret positions - failed at " + midPoint, occurrences, alternates);
                        }
                    }
                }
            }
        });
    }
    
    @Override
    protected void checkSemantic(final String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        if (caretLine != null) {
            int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                JsParserResult pr = (JsParserResult) r;
                
                Model.getModel(pr, false).getGlobalObject();
                
                SemanticAnalyzer analyzer = getSemanticAnalyzer();
                assertNotNull("getSemanticAnalyzer must be implemented", analyzer);

                analyzer.run(pr, null);
                Map<OffsetRange, Set<ColoringAttributes>> highlights = analyzer.getHighlights();

                if (highlights == null) {
                    highlights = Collections.emptyMap();
                }

                Document doc = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                checkNoOverlaps(highlights.keySet(), doc);

                String annotatedSource = annotateSemanticResults(doc, highlights);
                assertDescriptionMatches(relFilePath, annotatedSource, false, ".semantic");
            }
        });
    }
}
