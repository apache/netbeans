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
package org.netbeans.modules.web.el.hints;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.csl.hints.infrastructure.HintsSettings;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.el.ELLanguage;
import org.netbeans.modules.web.el.ELParser;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ELTestBaseForTestProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class HintTestBase extends ELTestBaseForTestProject {

    public HintTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected HintsProvider getHintsProvider() {
        return new ELHintsProvider();
    }

    protected abstract Rule createRule();

    /* Next lines are copied from the CslTestBase - we need to be able to use EL parser */
    protected void checkHints(NbTestCase test, Rule hint, String relFilePath, String caretLine) throws Exception {
        findHints(test, hint, relFilePath, null, caretLine);
    }

    protected void findHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints r = getHints2(test, hint, relFilePath, fileObject, caretLine);
        ParserResult info = r.info;
        List<Hint> result = r.hints;
        int caretOffset = r.caretOffset;

        String annotatedSource = annotateHints((BaseDocument) info.getSnapshot().getSource().getDocument(true), result, caretOffset);

        if (fileObject != null) {
            assertDescriptionMatches(fileObject, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        } else {
            assertDescriptionMatches(relFilePath, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        }
    }

    protected ComputedHints getHints2(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints hints = computeHints2(test, hint, relFilePath, fileObject, caretLine, ChangeOffsetType.NONE);

        if (checkAllHintOffsets()) {
            // Run alternate hint computation AFTER the real computation above since we will destroy the document...
            Logger.global.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    if (record.getThrown() != null) {
                        StringWriter sw = new StringWriter();
                        record.getThrown().printStackTrace(new PrintWriter(sw));
                        fail("Encountered error: " + sw.toString());
                    }
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }

            });
            for (ChangeOffsetType type : new ChangeOffsetType[] { ChangeOffsetType.OUTSIDE, ChangeOffsetType.OVERLAP }) {
                computeHints2(test, hint, relFilePath, fileObject, caretLine, type);
            }
        }

        return hints;
    }

    protected ComputedHints computeHints2(final NbTestCase test, final Rule hint, String relFilePath, FileObject fileObject, final String lineWithCaret, final ChangeOffsetType changeOffsetType) throws Exception {
        assertTrue(relFilePath == null || fileObject == null);

        initializeRegistry();

        if (fileObject == null) {
            fileObject = getTestFile(relFilePath);
        }

        Source testSource = getTestSource(fileObject);

        final int caretOffset;
        final String caretLine;
        if (lineWithCaret != null) {
            CaretLineOffset caretLineOffset = getCaretOffsetInternal2(testSource.createSnapshot().getText().toString(), lineWithCaret);
            caretOffset = caretLineOffset.offset;
            caretLine = caretLineOffset.caretLine;
            enforceCaretOffset(testSource, caretLineOffset.offset);
        } else {
            caretOffset = -1;
            caretLine = lineWithCaret;
        }

        final ComputedHints [] result = new ComputedHints[] { null };
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                try {
                    ELParser p = new ELParser();
                    p.parse(r.getSnapshot(), null, null);
                    r = (ELParserResult) p.getResult(new Task() {});
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }


                ParserResult pr = (ParserResult) r;

                Document document = pr.getSnapshot().getSource().getDocument(true);
                assert document != null : test;

                // remember the original document content, we are going to destroy it
                // and then restore
                String originalDocumentContent = null;
                if (changeOffsetType != ChangeOffsetType.NONE) {
                    customizeHintInfo(pr, changeOffsetType);

                    // Also: Delete te contents from the document!!!
                    // This ensures that the node offsets will be out of date by the time the rules run
                    originalDocumentContent = document.getText(0, document.getLength());
                    document.remove(0, document.getLength());
                }

                try {
                    if (!(hint instanceof Rule.ErrorRule)) { // only expect testcase source errors in error tests
                        if (parseErrorsOk) {
                            int caretOffset = 0;
                            result[0] = new ComputedHints(pr, new ArrayList<Hint>(), caretOffset);
                            return;
                        } else {
                            boolean errors = false;
                            for(org.netbeans.modules.csl.api.Error e : pr.getDiagnostics()) {
                                if (e.getSeverity() == Severity.ERROR) {
                                    errors = true;
                                    break;
                                }
                            }
                            assertTrue("Unexpected parse error in test case " +
                                    FileUtil.getFileDisplayName(pr.getSnapshot().getSource().getFileObject()) + "\nErrors = " +
                                    pr.getDiagnostics(), !errors);
                        }
                    }

                    String text = pr.getSnapshot().getText().toString();

                    org.netbeans.modules.csl.core.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
                    HintsProvider provider = getHintsProvider();
                    GsfHintsManager manager = getHintsManager(language);

                    Rule.UserConfigurableRule ucr = null;
                    if (hint instanceof Rule.UserConfigurableRule) {
                        ucr = (Rule.UserConfigurableRule)hint;
                    }

                    // Make sure the hint is enabled
                    if (ucr != null && !HintsSettings.isEnabled(manager, ucr)) {
                        Preferences p = HintsSettings.getPreferences(manager, ucr, HintsSettings.getCurrentProfileId());
                        HintsSettings.setEnabled(p, true);
                    }

                    List<Hint> hints = new ArrayList<Hint>();
                    if (hint instanceof Rule.ErrorRule) {
                        RuleContext context = manager.createRuleContext(pr, language, -1, -1, -1);
                        // It's an error!
                        // Create a hint registry which contains ONLY our hint (so other registered
                        // hints don't interfere with the test)
                        Map<Object, List<? extends Rule.ErrorRule>> testHints = new HashMap<Object, List<? extends Rule.ErrorRule>>();
                        if (hint.appliesTo(context)) {
                            Rule.ErrorRule ErrorRule = (Rule.ErrorRule)hint;
                            for (Object key : ErrorRule.getCodes()) {
                                testHints.put(key, Collections.singletonList(ErrorRule));
                            }
                        }
                        manager.setTestingRules(testHints, null, null, null);
                        provider.computeErrors(manager, context, hints, new ArrayList<org.netbeans.modules.csl.api.Error>());
                    } else if (hint instanceof Rule.SelectionRule) {
                        Rule.SelectionRule rule = (Rule.SelectionRule)hint;
                        List<Rule.SelectionRule> testHints = new ArrayList<Rule.SelectionRule>();
                        testHints.add(rule);

                        manager.setTestingRules(null, null, null, testHints);

                        if (caretLine != null) {
                            int start = text.indexOf(caretLine.toString());
                            int end = start+caretLine.length();
                            RuleContext context = manager.createRuleContext(pr, language, -1, start, end);
                            provider.computeSelectionHints(manager, context, hints, start, end);
                        }
                    } else {
                        assertTrue(hint instanceof Rule.AstRule && ucr != null);
                        Rule.AstRule AstRule = (Rule.AstRule)hint;
                        // Create a hint registry which contains ONLY our hint (so other registered
                        // hints don't interfere with the test)
                        Map<Object, List<? extends Rule.AstRule>> testHints = new HashMap<Object, List<? extends Rule.AstRule>>();
                        RuleContext context = manager.createRuleContext(pr, language, caretOffset, -1, -1);
                        if (hint.appliesTo(context)) {
                            for (Object nodeId : AstRule.getKinds()) {
                                testHints.put(nodeId, Collections.singletonList(AstRule));
                            }
                        }
                        if (HintsSettings.getSeverity(manager, ucr) == HintSeverity.CURRENT_LINE_WARNING) {
                            manager.setTestingRules(null, Collections.EMPTY_MAP, testHints, null);
                            provider.computeSuggestions(manager, context, hints, caretOffset);
                        } else {
                            manager.setTestingRules(null, testHints, null, null);
                            context.caretOffset = -1;
                            provider.computeHints(manager, context, hints);
                        }
                    }

                    result[0] = new ComputedHints(pr, hints, caretOffset);
                } finally {
                    if (originalDocumentContent != null) {
                        assert document != null;
                        document.remove(0, document.getLength());
                        document.insertString(0, originalDocumentContent, null);
                    }
                }
            }
        });

        return result[0];
    }

    private GsfHintsManager getHintsManager(org.netbeans.modules.csl.core.Language language) {
        return new GsfHintsManager(getPreferredMimeType(), getHintsProvider(), language);
    }

    private void customizeHintInfo(ParserResult result, ChangeOffsetType changeOffsetType) {
        if (changeOffsetType == ChangeOffsetType.NONE) {
            return;
        }
        if (result == null) {
            return;
        }
        // Test offset handling to make sure we can handle bogus node positions

//        Document doc = GsfUtilities.getDocument(result.getSnapshot().getSource().getFileObject(), true);
//        int docLength = doc.getLength();
        int docLength = result.getSnapshot().getText().length();

        // Replace errors with offsets
        List<org.netbeans.modules.csl.api.Error> errors = new ArrayList<org.netbeans.modules.csl.api.Error>();
        List<? extends org.netbeans.modules.csl.api.Error> oldErrors = result.getDiagnostics();
        for (org.netbeans.modules.csl.api.Error error : oldErrors) {
            int start = error.getStartPosition();
            int end = error.getEndPosition();

            // Modify document position to be off
            int length = end - start;
            if (changeOffsetType == ChangeOffsetType.OUTSIDE) {
                start = docLength + 1;
            } else {
                start = docLength - 1;
            }
            end = start + length;
            if (end <= docLength) {
                end = docLength + 1;
            }

            DefaultError newError = new DefaultError(
                    error.getKey(), error.getDisplayName(),
                    error.getDescription(), error.getFile(), start,
                    end, error.getSeverity()
            );
            newError.setParameters(error.getParameters());
            customizeHintError(error, start); // XXX: should not this be newError ??
            errors.add(newError);
        }
// XXX: there is no way to fiddle with parser errors
//        oldErrors.clear();
//        oldErrors.addAll(errors);
    }

    private static CaretLineOffset getCaretOffsetInternal2(String text, String caretLine) {
        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue("No occurrence of caretLine " + caretLine + " in text '" + text + "'", lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        return new CaretLineOffset(caretOffset, caretLine);
    }

    private static class CaretLineOffset {
        private final int offset;
        private final String caretLine;

        public CaretLineOffset(int offset, String caretLine) {
            this.offset = offset;
            this.caretLine = caretLine;
        }

    }

    protected static class ComputedHints {
        ComputedHints(ParserResult info, List<Hint> hints, int caretOffset) {
            this.info = info;
            this.hints = hints;
            this.caretOffset = caretOffset;
        }

        @Override
        public String toString() {
            return "ComputedHints(caret=" + caretOffset + ",info=" + info + ",hints=" + hints + ")";
        }

        public ParserResult info;
        public List<Hint> hints;
        public int caretOffset;
    }

}
