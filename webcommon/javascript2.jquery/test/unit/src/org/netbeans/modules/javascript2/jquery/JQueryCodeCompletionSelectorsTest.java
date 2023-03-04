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
package org.netbeans.modules.javascript2.jquery;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JQueryCodeCompletionSelectorsTest extends JsCodeCompletionBase {

    public JQueryCodeCompletionSelectorsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestLanguageProvider.register(CssTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
    }
    
    public void testSelectorTag01() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$(\"ta^ble\").css(\"border\",\"3px solid red\");", false);
    }

    public void testSelectorTag02() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$(ta^ska).text(\"fadsfads\");", false);
    }

    public void testSelectorId01() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$(my^)", false);
    }

    public void testSelectorId02() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('#^myIdTable')", false);
    }

    public void testSelectorClass01() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('div.^notMe')", false);
    }

    public void testSelectorClass02() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('.^notMe')", false);
    }

    public void testSelectorTagAttr01() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('div[t^itle]')", false);
    }

    public void testSelectorTagAttr02() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('div[t^itle=\"hello\"]')", false);
    }

    public void testCodeCompletionSelectorInsert() throws Exception {
        assertComplete("$(':b|')", "$(':button|')", ":button");
        assertComplete("$('h|')", "$(':has(|)')", ":has()");
        assertComplete("$(':|')", "$(':focus|')", ":focus");
        assertComplete("$('|')", "$(':odd|')", ":odd");
    }

    public void testIssue217013_1() throws Exception {
        // complete $(di|)
        assertComplete("$('di|')", "$('div|')", "div");
        assertComplete("$('di|)", "$('div|)", "div");
    }

    public void testIssue217019_1() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('[^]')", false);
    }

    public void testIssue217019_2() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('[^')", false);
    }

    public void testIssue211769_1() throws Exception {
        checkAppliedCompletion("testfiles/completion/jQuery/selectors/basic/issue211769.js", "$('.^')", "$('.notMe^')", ".notMe", false);
    }

    public void testIssue211769_2() throws Exception {
        checkAppliedCompletion("testfiles/completion/jQuery/selectors/basic/issue211769_1.js", "$('^')", "$('.notMe^')", ".notMe", false);
    }

    public void testIssue221481() throws Exception {
        checkAppliedCompletion("testfiles/completion/jQuery/selectors/basic/issue221481.js", "$(document).find('a #foo .^');", "$(document).find('a #foo .myClass^');", ".myClass", false);
    }

    public void testIssue221481_1() throws Exception {
        checkAppliedCompletion("testfiles/completion/jQuery/selectors/basic/issue221481_1.js", "$(document).find('a #foo i^');", "$(document).find('a #foo img^');", "img", false);
    }

    public void testIssue221481_2() throws Exception {
        checkAppliedCompletion("testfiles/completion/jQuery/selectors/basic/issue221481_2.js", "$(document).find('a #foo img[^');", "$(document).find('a #foo img[title^');", "title", false);
    }

    public void testIssue229750() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/basic/selectors.js", "$('[h^]')", false);
    }
    
    public void testPropertyNames_01 () throws Exception {
        checkCompletion("testfiles/completion/propertyNames.js", "    e^rror: function(jqXHR, textStatus, errorThrown) {", false);
    }
    
    public void testPropertyNames_02 () throws Exception {
        checkCompletion("testfiles/completion/propertyNames.js", "    c^ontents: ss", false);
    }
    
    public void testPropertyNames_03() throws Exception {
        checkAppliedCompletion("testfiles/completion/propertyNames.js", "    ^    // here cc 01", "    data: ,^", "data", false);
    }
    
    public void testPropertyNames_04() throws Exception {
        checkAppliedCompletion("testfiles/completion/propertyNames02.js", "    ^    // here cc 02", "                        ^", "success", false);
    }
    
    public void testAttributeName_01() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/issue223964.js", "$(document).find('a[^]')", false);
    }
    
    public void testAttributeName_02() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/issue223964.js", "$(document).find('[^]')", false);
    }
    
    public void testAttributeName_03() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/issue223964.js", "$(document).find('a.add-bottom[^]')", false);
    }
    
    public void testAttributeName_04() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/issue223964.js", "$(document).find('(a#add-bottom[^])')", false);
    }
    
    public void testAttributeName_05() throws Exception {
        checkCompletion("testfiles/completion/jQuery/selectors/issue223964.js", "$(document).find('.add-bottom[^]')", false);
    }
    
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
                JS_SOURCE_ID,
                ClassPathSupport.createClassPath(new FileObject[]{
                    FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/jQuery/selectors/basic"))
                }));
    }

    public void checkAppliedCompletion(final String file, final String caretLine, final String expectedLine, final String itemToComplete, final boolean includeModifiers) throws Exception {
        RepositoryUpdater.getDefault().waitUntilFinished(10000);
        final CodeCompletionHandler.QueryType type = CodeCompletionHandler.QueryType.COMPLETION;
        final boolean caseSensitive = true;

        Source testSource = getTestSource(getTestFile(file));
        final AtomicReference<CompletionProposal> found = new AtomicReference<CompletionProposal>();

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        final Document[] doc = new Document[1];
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override
            void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler cc = getCodeCompleter();
                assertNotNull("getCodeCompleter must be implemented", cc);

                doc[0] = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                boolean upToOffset = type == CodeCompletionHandler.QueryType.COMPLETION;
                String prefix = cc.getPrefix(pr, caretOffset, upToOffset);
                if (prefix == null) {
                    if (prefix == null) {
                        int[] blk =
                                org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc[0], caretOffset);

                        if (blk != null) {
                            int start = blk[0];
                            if (start < caretOffset) {
                                if (upToOffset) {
                                    prefix = doc[0].getText(start, caretOffset - start);
                                } else {
                                    prefix = doc[0].getText(start, blk[1] - start);
                                }
                            }
                        }
                    }
                }

                final int finalCaretOffset = caretOffset;
                final String finalPrefix = prefix;
                final ParserResult finalParserResult = pr;
                CodeCompletionContext context = new CodeCompletionContext() {
                    @Override
                    public int getCaretOffset() {
                        return finalCaretOffset;
                    }

                    @Override
                    public ParserResult getParserResult() {
                        return finalParserResult;
                    }

                    @Override
                    public String getPrefix() {
                        return finalPrefix;
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
                        return caseSensitive;
                    }
                };

                CodeCompletionResult completionResult = cc.complete(context);
                List<CompletionProposal> proposals = completionResult.getItems();

                final boolean deprecatedHolder[] = new boolean[1];
                final HtmlFormatter formatter = new HtmlFormatter() {
                    private StringBuilder sb = new StringBuilder();

                    @Override
                    public void reset() {
                        sb.setLength(0);
                    }

                    @Override
                    public void appendHtml(String html) {
                        sb.append(html);
                    }

                    @Override
                    public void appendText(String text, int fromInclusive, int toExclusive) {
                        sb.append(text, fromInclusive, toExclusive);
                    }

                    @Override
                    public void emphasis(boolean start) {
                    }

                    @Override
                    public void active(boolean start) {
                    }

                    @Override
                    public void name(ElementKind kind, boolean start) {
                    }

                    @Override
                    public void parameters(boolean start) {
                    }

                    @Override
                    public void type(boolean start) {
                    }

                    @Override
                    public void deprecated(boolean start) {
                        deprecatedHolder[0] = true;
                    }

                    @Override
                    public String getText() {
                        return sb.toString();
                    }
                };

                assertCompletionItemNames(new String[]{itemToComplete}, completionResult, Match.CONTAINS);

                for (CompletionProposal ccp : completionResult.getItems()) {
                    if (itemToComplete.equals(ccp.getName())) {
                        //complete the item
                        found.set(ccp);
                        break;
                    }
                }

                CompletionProposal proposal = found.get();
                assertNotNull(proposal);

                final String text = proposal.getCustomInsertTemplate() != null ? proposal.getCustomInsertTemplate() : proposal.getInsertPrefix();
                final int offset = proposal.getAnchorOffset();
                final int len = caretOffset - offset;
                final int[] resultPipeOffset = new int[1];

                final BaseDocument bd = (BaseDocument) doc[0];

                //since there's no access to the GsfCompletionItem.defaultAction() I've copied important code below:
                bd.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int semiPos = -2;
                            String textToReplace = bd.getText(offset, len);
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
                                    bd.insertString(semiPos, ";", null); //NOI18N
                                }
                                return;
                            }
                            int common = 0;
                            while (rtext.regionMatches(0, textToReplace, 0, ++common)) {
                                //no-op
                            }
                            common--;
                            Position position = bd.createPosition(offset + common);
                            Position semiPosition = semiPos > -1 ? bd.createPosition(semiPos) : null;
                            bd.remove(offset + common, len - common);
                            bd.insertString(position.getOffset(), rtext.substring(common), null);
                            if (semiPosition != null) {
                                bd.insertString(semiPosition.getOffset(), ";", null);
                            }

                        } catch (BadLocationException e) {
                            // Can't update
                        }
                    }
                });

                assertDescriptionMatches(file, bd.getText(0, bd.getLength()), true, ".expected.js");
                final String fileName = file.substring(0, file.indexOf(".")) + ".js." + getName() + ".expected.js";
                assertTrue("File not found: " + getTestFile(fileName).getPath(), getTestFile(fileName).isValid());
                Source expectedSource = getTestSource(getTestFile(fileName));
                final StringBuilder expectedContent = new StringBuilder(expectedSource.getDocument(false).getText(0, expectedSource.getDocument(false).getLength()));
                final int expectedOffset;
                if (expectedLine != null) {
                    expectedOffset = getCaretOffset(expectedSource.createSnapshot().getText().toString(), expectedLine);
                    enforceCaretOffset(expectedSource, expectedOffset);
                } else {
                    expectedOffset = -1;
                }
                assertEquals(expectedContent.toString(), bd.getText(0, bd.getLength()));
                assertEquals(expectedOffset, resultPipeOffset[0]);
            }
        });
    }
}
