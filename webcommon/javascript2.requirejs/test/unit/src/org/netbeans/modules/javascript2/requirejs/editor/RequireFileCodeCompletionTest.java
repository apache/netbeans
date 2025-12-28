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

package org.netbeans.modules.javascript2.requirejs.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.requirejs.RequireJsPreferences;
import org.netbeans.modules.javascript2.requirejs.TestProjectSupport;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class RequireFileCodeCompletionTest extends JsCodeCompletionBase {
    
    public RequireFileCodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileObject folder = getTestFile("TestProject1");
        Project tp = new TestProjectSupport.TestProject(folder, null);
        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(tp));
        MockLookup.setInstances(lookupAll.toArray());
        
        Map<String, String> mappings = new HashMap<>();
        mappings.put("utils", "js/folder1/api/utils.js");
        mappings.put("api", "js/folder1/api");
        mappings.put("lib/api", "js/folder1/api");
        RequireJsPreferences.storeMappings(tp, mappings);
    }
    
    public void testFSCompletion01() throws Exception {
        checkCompletion("TestProject1/js/main.js", "'fo^lder1/module1'", false);
    }
    
    public void testFSCompletion02() throws Exception {
        checkCompletion("TestProject1/js/folder2/fs.js", "'^'", false);
    }
    
    public void testFSCompletion03() throws Exception {
        checkCompletion("TestProject1/js/main2.js", "requirejs(['^']);", false);
    }
    
    public void testFSCompletion04() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/main3.js", "requirejs(['^']);", "requirejs(['./folder1/^']);", "folder1", false);
        
    }
    
    public void testFSCompletion05() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/main4.js", "requirejs(['fold^']);", "requirejs(['folder1/^']);", "folder1", false);        
    }
    
    public void testFSCompletion06() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/main5.js", "requirejs(['folder1/^']);", "requirejs(['folder1/module2^']);", "module2", false);        
    }
    
    public void testFSCompletion07() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/main6.js", "requirejs(['folder1/mo^']);", "requirejs(['folder1/module2^']);", "module2", false);        
    }
    
    public void testMappingCompletion01() throws Exception {
        checkCompletion("TestProject1/js/folder2/fs.js", "'lib/a^',", false);        
    }
    
    public void testMappingCompletion02() throws Exception {
        checkCompletion("TestProject1/js/folder2/fs.js", "'lib/api/^',", false);        
    }
    
    public void testMappingCompletion03() throws Exception {
        checkCompletion("TestProject1/js/folder2/fs.js", "'lib/api/v0.1/^',", false);        
    }
    
    public void testIssue245041() throws Exception {
        checkCompletion("TestProject1/js/fileCC/folder2/issue245041.js", "define([\"f^\"], function(e){", false);        
    }
    
    public void testMappingCompletion04() throws Exception {
        checkCompletion("TestProject1/js/folder2/fs.js", "'lib/api/v0.1/OM^',", false);        
    }
    
    public void testIssue248197_01() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue248197/mainTest01.js", "var a = require('^');", "var a = require('./aaa^');", "aaa", false);        
    }
    
    public void testIssue248197_02() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue248197/mainTest02.js", "var a2 = require('./^');", "var a2 = require('./aaa^');", "aaa", false);        
    }
    
    public void testIssue248197_03() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue248197/mainTest03.js", "var a3 = require('./a^');", "var a3 = require('./aaa^');", "aaa", false);        
    }
    
    public void testIssue248197_04() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue248197/mainTest04.js", "var l1 = require('^');", "var l1 = require('./lib/^');", "lib", false);        
    }

    public void testIssue248197_05() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue248197/mainTest05.js", "var l2 = require('./l^');", "var l2 = require('./lib/^');", "lib", false);        
    }

    public void testIssue248197_06() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue248197/mainTest06.js", "var l3 = require('./lib/^');", "var l3 = require('./lib/bbb^');", "bbb", false);        
    }
    
    public void testIssue248197_07() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue248197/mainTest07.js", "var l4 = require('./lib/b^');", "var l4 = require('./lib/bbb^');", "bbb", false);        
    }
    
    public void testIssue248198_01() throws Exception {
        checkCompletion("TestProject1/js/fileCC/folder1/module2.js", "var a2 = require('./^');", false);        
    }
    
    public void testIssue247759_01() throws Exception {
        checkCompletion("TestProject1/js/folder1/api/v0.1/issue247759.js", "define(['^'], function () {});", false);        
    }
    
    public void testIssue247729_01() throws Exception {
        checkAppliedCompletion("TestProject1/js/main3.js", "'text!fileCC/folder2/template/^',", "'text!fileCC/folder2/template/body.html^',", "body", false);        
    }
    
    public void testIssue249026_01() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue249026/base.dt/js/issue249026_1.js", "'^'", "'base.dt/^'", "base.dt", false);        
    }
    
    public void testIssue249026_02() throws Exception {
        checkAppliedCompletion("TestProject1/js/fileCC/issue249026/base.dt/js/issue249026_2.js", "'b^'", "'base.dt/^'", "base.dt", false);        
    }
    
    public void checkAppliedCompletion(final String file, final String caretLine, final String expectedLine, final String itemToComplete, final boolean includeModifiers) throws Exception {
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
                final String fileName = file.substring(0, file.lastIndexOf(".")) + ".js." + getName() + ".expected.js";
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
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>();
        
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/TestProject1")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return false;
    }

    @Override
    protected boolean cleanCacheDir() {
        return false;
    }
}
