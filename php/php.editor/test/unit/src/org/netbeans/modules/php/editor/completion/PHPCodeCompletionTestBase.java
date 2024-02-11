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
import org.netbeans.api.annotations.common.NullAllowed;
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
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.options.CodeCompletionPanel;
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
        FileObject[] sourceDirectories = createSourceClassPathsForTest();
        if (sourceDirectories == null) {
            sourceDirectories = new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "testfiles/completion/lib"))
            };
        }
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(sourceDirectories)
        );
    }

    protected FileObject[] createSourceClassPathsForTest() {
        return null;
    }

    protected String getTestDirName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    protected void checkCompletionCustomTemplateResult(final String file, final String caretLine, CompletionProposalFilter filter, boolean checkAllItems) throws Exception {
        checkCompletionCustomTemplateResult(file, caretLine, filter, checkAllItems, null);
    }

    protected void checkCompletionCustomTemplateResult(final String file, final String caretLine, CompletionProposalFilter filter, boolean checkAllItems, @NullAllowed AutoImportOptions autoImportOptions) throws Exception {
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

        // just get the ParserResult in the UserTask
        // because ParserManager.parse() is called in completionProposal.getCustomInsertTemplate()
        ParserResult[] parserResults = new ParserResult[1];
        UserTask task = new UserTask() {
            public @Override
            void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;
                parserResults[0] = pr;
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

        final ParserResult parserResult = parserResults[0];
        CodeCompletionHandler cc = getCodeCompleter();
        assertNotNull("getCodeCompleter must be implemented", cc);

        Document doc = GsfUtilities.getADocument(parserResult.getSnapshot().getSource().getFileObject(), true);
        boolean upToOffset = type == CodeCompletionHandler.QueryType.COMPLETION;
        String prefix = cc.getPrefix(parserResult, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk = org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);
                if (blk != null) {
                    int start = blk[0];
                    if (start < caretOffset) {
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
        CodeCompletionContext context = new CodeCompletionContext() {

            @Override
            public int getCaretOffset() {
                return finalCaretOffset;
            }

            @Override
            public ParserResult getParserResult() {
                return parserResult;
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
                if (proposal instanceof PHPCompletionItem) {
                    PHPCompletionItem phpCompletionItem = (PHPCompletionItem) proposal;
                    if (autoImportOptions != null) {
                        phpCompletionItem.setAutoImport(autoImportOptions.isAutoImport());
                        phpCompletionItem.setCodeCompletionType(autoImportOptions.getCodeCompletionType());
                        phpCompletionItem.setGlobalItemImportable(autoImportOptions.isGlobalItemImportable());
                    }
                }
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

        assertDescriptionMatches(file, sb.toString(), true, ".cccustomtpl");
    }

    //~ Inner class
    public static interface CompletionProposalFilter {

        CompletionProposalFilter ACCEPT_ALL = proposal -> true;

        boolean accept(CompletionProposal proposal);
    }

    public static final class DefaultFilter implements CompletionProposalFilter {

        private final PhpVersion phpVersion;
        private final String prefix;

        public DefaultFilter(PhpVersion phpVersion, String prefix) {
            this.phpVersion = phpVersion;
            this.prefix = prefix;
        }

        public DefaultFilter(String prefix) {
            this.phpVersion = PhpVersion.getDefault();
            this.prefix = prefix;
        }

        @Override
        public boolean accept(CompletionProposal proposal) {
            if (proposal instanceof PHPCompletionItem.MethodDeclarationItem) {
                PHPCompletionItem.MethodDeclarationItem item = (PHPCompletionItem.MethodDeclarationItem) proposal;
                String name = item.getName();
                if (name.startsWith(prefix)) {
                    item.setPhpVersion(phpVersion);
                    return true;
                }
            }
            if (proposal instanceof PHPCompletionItem.KeywordItem) {
                // ignore KeywordItem because it invokes EditorRegistry.lastFocusedComponent().getDocument() (NPE)
                return false;
            }
            if (proposal != null) {
                String name = proposal.getName();
                return name.startsWith(prefix);
            }
            return false;
        }
    }

    static final class AutoImportOptions {

        private CodeCompletionPanel.CodeCompletionType codeCompletionType = CodeCompletionPanel.CodeCompletionType.SMART;
        private boolean autoImport = false;
        private boolean globalItemImportable = false;

        public CodeCompletionPanel.CodeCompletionType getCodeCompletionType() {
            return codeCompletionType;
        }

        public boolean isAutoImport() {
            return autoImport;
        }

        public boolean isGlobalItemImportable() {
            return globalItemImportable;
        }

        public AutoImportOptions codeCompletionType(CodeCompletionPanel.CodeCompletionType codeCompletionType) {
            this.codeCompletionType = codeCompletionType;
            return this;
        }

        public AutoImportOptions autoImport(boolean autoImport) {
            this.autoImport = autoImport;
            return this;
        }

        public AutoImportOptions globalItemImportable(boolean globalItemImportable) {
            this.globalItemImportable = globalItemImportable;
            return this;
        }
    }
}
