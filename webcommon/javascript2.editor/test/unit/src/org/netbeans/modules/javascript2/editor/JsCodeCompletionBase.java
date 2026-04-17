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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionBase extends JsTestBase {

    public JsCodeCompletionBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
// //        MockLookup.init();
//        List lookupAll = new ArrayList();
//        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
//        MockLookup.setInstances(new ClassPathProviderImpl());
//        MockLookup.setInstances(lookupAll.toArray());
        OpenProjects.getDefault().getOpenProjects();
        OptionsUtils.forLanguage(getPreferredLanguage().getLexerLanguage()).autoCompletionTypeResolution();
        OptionsUtils.forLanguage(getPreferredLanguage().getLexerLanguage()).setTestTypeResolution(true);
    }

    public static enum Match {
        EXACT, CONTAINS, EMPTY, NOT_EMPTY, DOES_NOT_CONTAIN;
    }

    @Override
    public void checkCompletion(String file, String caretLine, boolean includeModifiers) throws Exception {
        // waits for registration into the GlobalPathRegistry
        RepositoryUpdater.getDefault().waitUntilFinished(10000);
        super.checkCompletion(file, caretLine, includeModifiers);
    }

    public void assertComplete(String documentText, String expectedDocumentText, final String itemToComplete) throws ParseException, BadLocationException {
        StringBuilder content = new StringBuilder(documentText);
        StringBuilder expectedContent = new StringBuilder(expectedDocumentText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0;
        final int expPipeOffset = expectedContent.indexOf("|");
        assert expPipeOffset >= 0;

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        expectedContent.deleteCharAt(expPipeOffset);

        final BaseDocument doc = getDocument(content.toString());
        Source source = Source.create(doc);
        final AtomicReference<CompletionProposal> found = new AtomicReference<>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertNotNull(result);
                assertTrue(result instanceof JsParserResult);

                JsParserResult jsResult = (JsParserResult) result;

                CodeCompletionHandler cc = getPreferredLanguage().getCompletionHandler();
                String prefix = cc.getPrefix(jsResult, pipeOffset, false);
                CodeCompletionResult ccresult = cc.complete(createContext(pipeOffset, jsResult, prefix));

                assertCompletionItemNames(new String[]{itemToComplete}, ccresult, Match.CONTAINS);

                for (CompletionProposal ccp : ccresult.getItems()) {
                    if (itemToComplete.equals(ccp.getName())) {
                        //complete the item
                        found.set(ccp);
                        break;
                    }
                }

            }
        });

        CompletionProposal proposal = found.get();
        assertNotNull(proposal);

        final String text = proposal.getCustomInsertTemplate() != null ? proposal.getCustomInsertTemplate() : proposal.getInsertPrefix();
        final int offset = proposal.getAnchorOffset();
        final int len = pipeOffset - offset;
        final int[] resultPipeOffset = new int[1];

        //since there's no access to the GsfCompletionItem.defaultAction() I've copied important code below:
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    int semiPos = -2;
                    String textToReplace = doc.getText(offset, len);
                    int newCaretPos = text.indexOf("${cursor}");
                    String rtext = text;
                    if (newCaretPos > -1) {
                        rtext = text.substring(0, newCaretPos) + text.substring(newCaretPos + 9);
                        resultPipeOffset[0] = offset + newCaretPos;
                    } else {
                        resultPipeOffset[0] = offset + rtext.length();
                    }
                    if (rtext.equals(textToReplace)) {
                        if (semiPos > -1) {
                            doc.insertString(semiPos, ";", null); //NOI18N
                        }
                        return;
                    }
                    int common = 0;
                    while (rtext.regionMatches(0, textToReplace, 0, ++common)) {
                        //no-op
                    }
                    common--;
                    Position position = doc.createPosition(offset + common);
                    Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                    doc.remove(offset + common, len - common);
                    doc.insertString(position.getOffset(), rtext.substring(common), null);
                    if (semiPosition != null) {
                        doc.insertString(semiPosition.getOffset(), ";", null);
                    }

                } catch (BadLocationException e) {
                    // Can't update
                }
            }
        });

        assertEquals(expectedContent.toString(), doc.getText(0, doc.getLength()));
        assertEquals(expPipeOffset, resultPipeOffset[0]);
    }

    protected void assertCompletionItemNames(String[] expected, CodeCompletionResult ccresult, Match type) {
        Collection<String> real = new ArrayList<>();
        for (CompletionProposal ccp : ccresult.getItems()) {
            real.add(ccp.getName());
        }
        Collection<String> exp = new ArrayList<>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            assertEquals(exp, Collections.emptyList());
        } else if (type == Match.EMPTY) {
            assertEquals(0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals("The unexpected element(s) '" + arrayToString(expected) + "' are present in the completion items list", originalRealSize, real.size());
        }
    }

    private String arrayToString(String[] elements) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            buf.append(elements[i]);
            if (i < elements.length - 1) {
                buf.append(',');
                buf.append(' ');
            }
        }
        return buf.toString();
    }

    private static TestCodeCompletionContext createContext(int offset, ParserResult result, String prefix) {
        return new TestCodeCompletionContext(offset, result, prefix, CodeCompletionHandler.QueryType.COMPLETION, false);
    }

    private static class TestCodeCompletionContext extends CodeCompletionContext {

        private final int caretOffset;
        private final ParserResult result;
        private final String prefix;
        private final CodeCompletionHandler.QueryType type;
        private final boolean isCaseSensitive;

        public TestCodeCompletionContext(int caretOffset, ParserResult result, String prefix, CodeCompletionHandler.QueryType type, boolean isCaseSensitive) {
            this.caretOffset = caretOffset;
            this.result = result;
            this.prefix = prefix;
            this.type = type;
            this.isCaseSensitive = isCaseSensitive;
        }

        @Override
        public int getCaretOffset() {
            return caretOffset;
        }

        @Override
        public ParserResult getParserResult() {
            return result;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public boolean isPrefixMatch() {
            return true;
        }

        @Override
        public CodeCompletionHandler.QueryType getQueryType() {
            return type;
        }

        @Override
        public boolean isCaseSensitive() {
            return isCaseSensitive;
        }
    }
}
