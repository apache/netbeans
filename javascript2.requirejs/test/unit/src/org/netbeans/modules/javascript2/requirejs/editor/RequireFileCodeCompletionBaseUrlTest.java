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

package org.netbeans.modules.javascript2.requirejs.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
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
public class RequireFileCodeCompletionBaseUrlTest extends JsCodeCompletionBase {
    
    public RequireFileCodeCompletionBaseUrlTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileObject folder = getTestFile("TestProject2");
        Project tp = new TestProjectSupport.TestProject(folder, null);
        List lookupAll = new ArrayList();
        lookupAll.addAll(MockLookup.getDefault().lookupAll(Object.class));
        lookupAll.add(new TestProjectSupport.FileOwnerQueryImpl(tp));
        MockLookup.setInstances(lookupAll.toArray());
    }
    
    public void testFSCompletion01() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "requirejs(['modul^eLib1', ", false);
    }
    
    public void testFSCompletion02() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "'js/^lib/moduleLib2.js',", false);
    }
    
    public void testFSCompletion03() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "'js/lib/moduleL^ib2.js',", false);
    }
    
    public void testFSCompletion04() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "'app/module^App1',", false);
    }
    
    public void testFSCompletion06() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "'p^roto/localization',", false);
    }
    
    public void testFSCompletion07() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "'proto/loc^alization',", false);
    }
    
    public void testFSCompletion08() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "'pikn^ic'", false);
    }
    
    public void testFSBaseUrl01() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "baseUrl: 'js/^lib',", false);
    }
    
    public void testIssue245156_01() throws Exception {
        checkCompletion("TestProject2/public_html/js/app/issue245156.js", "var module1 = require(\"app/mo^duleApp1\");", false);
    }
    
    public void testIssue245156_02() throws Exception {
        checkCompletion("TestProject2/public_html/js/app/issue245156.js", "var module2 = require(\"lib/mod^uleLib1\");", false);
    }
    
    public void testPathWithPlugin_01() throws Exception {
        checkCompletion("TestProject2/public_html/js/app/issue245184.js", "'text!^app/issue245156',", false);
    }
    
    public void testPathWithPlugin_02() throws Exception {
        checkCompletion("TestProject2/public_html/js/app/issue245184.js", "'text!app/i^ssue245156',", false);
    }
    
    public void testPathWithPlugin_03() throws Exception {
        checkCompletion("TestProject2/public_html/js/app/issue245184.js", "'order!lib/^proto/localization'", false);
    }
    
    public void testLoaderPluginName_01() throws Exception {
        checkCompletion("TestProject2/public_html/js/app/issue245184.js", "'^order!lib/proto/localization'", false);
    }
    
    public void testLoaderPluginName_02() throws Exception {
        checkCompletion("TestProject2/public_html/js/main.js", "'^proto/localization',", false);
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
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>();
        
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/TestProject2/public_html")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
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
