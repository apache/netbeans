/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.declarative.test;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.java.hints.declarative.test.TestParser.TestCase;
import org.netbeans.modules.java.hints.declarative.test.TestParser.TestResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class EditorTestPerformer extends ParserResultTask<TestResult>{

    private static final Logger LOG = Logger.getLogger(EditorTestPerformer.class.getName());
    private static final AttributeSet PASSED = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.GREEN);
    private static final AttributeSet FAILED = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.RED);
    
    private final AtomicBoolean cancel = new AtomicBoolean();
    
    @Override
    public void run(TestResult result, SchedulerEvent event) {
        TestCase[] tests = result.getTests();
        FileObject file = result.getSnapshot().getSource().getFileObject();
        FileObject ruleFile = TestLocatorImpl.findOpposite(file, false);

        if (ruleFile == null) {
                return ;
        }
        
        Document doc = result.getSnapshot().getSource().getDocument(false);

        if (!(doc instanceof StyledDocument)) {
            return ;
        }

        StyledDocument sdoc = (StyledDocument) doc;
        
        try {
            List<ErrorDescription> errors = new LinkedList<ErrorDescription>();
            OffsetsBag bag = new OffsetsBag(doc);
            Map<TestCase, Collection<String>> testResults = TestPerformer.performTest(ruleFile, file, tests, cancel);

            if (testResults == null || cancel.get()) return ;
            
            for (Entry<TestCase, Collection<String>> e : testResults.entrySet()) {
                if (cancel.get()) return ;
                
                TestCase tc = e.getKey();
                String[] golden = tc.getResults();
                String[] real = e.getValue().toArray(new String[0]);
                boolean passed = true;

                if (golden.length != real.length) {
                    int line = NbDocument.findLineNumber(sdoc, tc.getTestCaseStart()) + 1;
                    ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "Incorrect number of outputs, expected: " + golden.length + ", was: " + real.length, doc, line);

                    errors.add(ed);
                    passed = false;
                }
                
                for (int cntr = 0; cntr < Math.min(golden.length, real.length); cntr++) {
                    String goldenText = golden[cntr];
                    String realText   = real[cntr];

                    if (!TestPerformer.normalize(goldenText).equals(TestPerformer.normalize(realText))) {
                        int line = NbDocument.findLineNumber(sdoc, tc.getResultsStart()[cntr]);
                        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(tc.getResultsStart()[cntr], tc.getResultsEnd()[cntr], sdoc, realText));
                        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "Incorrect output", fixes, doc, line);

                        errors.add(ed);
                        passed = false;
                    }
                }

                bag.addHighlight(tc.getTestCaseStart() + "%%TestCase ".length(), tc.getCodeStart() - 1, passed ? PASSED : FAILED);
            }

            getBag(doc).setHighlights(bag);
            HintsController.setErrors(doc, EditorTestPerformer.class.getName(), errors);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

    static @NonNull OffsetsBag getBag(@NonNull Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(EditorTestPerformer.class);

        if (bag == null) {
            doc.putProperty(EditorTestPerformer.class, bag = new OffsetsBag(doc));
        }

        return bag;
    }

    @MimeRegistration(mimeType=TestTokenId.MIME_TYPE, service=TaskFactory.class)
    public static final class FactoryImpl extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new EditorTestPerformer());
        }
        
    }

    private static final class FixImpl implements Fix {

        //XXX: Position!!
        private final int start;
        private final int end;
        private final StyledDocument doc;
        private final String text;

        public FixImpl(int start, int end, StyledDocument doc, String text) {
            this.start = start;
            this.end = end;
            this.doc = doc;
            this.text = text;
        }

        public String getText() {
            return "Put actual output into golden section";
        }

        public ChangeInfo implement() throws Exception {
            NbDocument.runAtomic(doc, new Runnable() {
                public void run() {
                    try {
                        doc.remove(start, end - start);
                        doc.insertString(start, text, null);
                    } catch (BadLocationException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            });
            
            return null;
        }
        
    }
}
