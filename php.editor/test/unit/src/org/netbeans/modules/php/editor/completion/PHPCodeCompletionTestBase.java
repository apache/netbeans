/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tor Norbye
 */
public abstract class PHPCodeCompletionTestBase extends PHPTestBase {

    public PHPCodeCompletionTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "testfiles/completion/lib"))
            })
        );
    }

    protected void checkCompletionCustomTemplateResult(final String file, final String caretLine, CompletionProposalFilter filter, boolean checkAllItems) throws Exception {
        final CodeCompletionHandler.QueryType type = CodeCompletionHandler.QueryType.COMPLETION;
        final boolean caseSensitive = true;

        Source testSource = getTestSource(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        final CompletionProposalFilter completionProposalFilter = filter == null ? CompletionProposalFilter.ACCEPT_ALL : filter;

        String[] results = new String[1];
        UserTask task = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler cc = getCodeCompleter();
                assertNotNull("getCodeCompleter must be implemented", cc);

                Document doc = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                boolean upToOffset = type == CodeCompletionHandler.QueryType.COMPLETION;
                String prefix = cc.getPrefix(pr, caretOffset, upToOffset);
                if (prefix == null) {
                    if (prefix == null) {
                        int[] blk =
                            org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);

                        if (blk != null) {
                            int start = blk[0];
                            if (start < caretOffset ) {
                                if (upToOffset) {
                                    prefix = doc.getText(start, caretOffset - start);
                                } else {
                                    prefix = doc.getText(start, blk[1] - start);
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
                StringBuilder sb = new StringBuilder();
                List<CompletionProposal> completionProposals = new LinkedList<>();
                List<CompletionProposal> proposals = completionResult.getItems();
                for (CompletionProposal proposal : proposals) {
                    if (completionProposalFilter.accept(proposal)) {
                        completionProposals.add(proposal);
                        if (!checkAllItems) {
                            break;
                        }
                    }
                }

                completionProposals.sort((o1, o2) -> {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                });

                for (CompletionProposal completionProposal : completionProposals) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append("Name: ").append(completionProposal.getName()).append("\n");
                    sb.append(completionProposal.getCustomInsertTemplate()).append("\n");
                }

                results[0] = sb.toString();
            }
        };
        Map<String, ClassPath> classPaths = createClassPathsForTest();
        if (classPaths == null || classPaths.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
        }
        assertDescriptionMatches(file, results[0], true, ".cccustomtpl");
    }

    //~ Inner class
    public interface CompletionProposalFilter {

        CompletionProposalFilter ACCEPT_ALL = proposal -> {
            return true;
        };

        boolean accept(CompletionProposal proposal);
    }

}
