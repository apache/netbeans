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

package org.netbeans.modules.java.hints.declarative.debugging;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.hints.declarative.Condition;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.FixTextDescription;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;
import org.netbeans.modules.java.hints.declarative.conditionapi.Matcher;
import org.netbeans.modules.java.hints.declarative.test.TestTokenId;
import org.netbeans.modules.java.hints.spiimpl.SPIAccessor;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.*;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class EvaluationSpanTask extends JavaParserResultTask<Result> {

    public EvaluationSpanTask() {
        super(Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return ;
        }

        CursorMovedSchedulerEvent evt = (CursorMovedSchedulerEvent) event;
        final CompilationInfo[] info = new CompilationInfo[] {CompilationInfo.get(result)};
        int start = evt.getMarkOffset();
        int end   = evt.getCaretOffset();

        if (info[0] == null) {
            TokenSequence<TestTokenId> ts = result.getSnapshot().getTokenHierarchy().tokenSequence(TestTokenId.language());

            if (ts == null) return ;

            ts.move(evt.getCaretOffset());
            if (!ts.moveNext() || ts.token().id() != TestTokenId.JAVA_CODE) return ;

            int tokenStart = ts.offset();
            int tokenEnd = ts.offset() + ts.token().length();

            if (evt.getCaretOffset() < tokenStart || tokenEnd < evt.getCaretOffset()) return ;
            if (evt.getMarkOffset() < tokenStart || tokenEnd < evt.getMarkOffset()) return ;

            start -= ts.offset();
            end -= ts.offset();

            Writer out;

            try {
                FileObject file = FileUtil.createMemoryFileSystem().getRoot().createData("Test.java");
                out = new OutputStreamWriter(file.getOutputStream(), "UTF-8");
                out.write(ts.token().text().toString());
                out.close();//XXX: finally!
                ClasspathInfo cpInfo = ClasspathInfo.create(file);
                JavaSource.create(cpInfo, file).runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.RESOLVED);
                        info[0] = parameter;
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
        }

        if (info[0] == null) {
            return ;//??
        }

        Document[] documents = ToggleDebuggingAction.debuggingEnabled.toArray(new Document[0]);
        
        for (final Document doc : documents) {
            assert doc != null;

            List<int[]> passed = new LinkedList<int[]>();
            List<int[]> failed = new LinkedList<int[]>();
            final String[] text = new String[1];

            doc.render(new Runnable() {
                @Override public void run() {
                    text[0] = DocumentUtilities.getText(doc).toString();
                }
            });

            Collection<? extends HintWrapper> hints = HintWrapper.parse(NbEditorUtilities.getFileObject(doc), text[0]);

            computeHighlights(info[0],
                              start,
                              end,
                              hints,
                              passed,
                              failed);

            OffsetsBag bag = new OffsetsBag(doc);

            for (int[] span : passed) {
                bag.addHighlight(span[0], span[1], PASSED);
            }

            for (int[] span : failed) {
                bag.addHighlight(span[0], span[1], FAILED);
            }

            DebuggingHighlightsLayerFactory.getBag(doc).setHighlights(bag);
        }
    }

    static void computeHighlights(CompilationInfo info,
                                  int selectionStart,
                                  int selectionEnd,
                                  Collection<? extends HintWrapper> hints,
                                  List<int[]> passed,
                                  List<int[]> failed) {
        if (hints.isEmpty()) return ;

        if (selectionStart == selectionEnd)
            return ;

        int t = Math.min(selectionStart, selectionEnd);

        selectionEnd = skipWhitespaces(info, Math.max(selectionStart, selectionEnd), false);
        selectionStart = skipWhitespaces(info, t, true);

        TreePath tp = validateSelection(info, selectionStart, selectionEnd);

        if (tp == null) {
            return ;
        }
        
        for (HintWrapper d : hints) {
            Map<String, TreePath> variables = new HashMap<String, TreePath>();
            Map<String, Collection<? extends TreePath>> multiVariables = new HashMap<String, Collection<? extends TreePath>>();
            Map<String, String> variableNames = new HashMap<String, String>();

            variables.put("$_", tp);

            HintContext ctx = SPIAccessor.getINSTANCE().createHintContext(info, HintsSettings.getSettingsFor(info.getFileObject()), null, tp, variables, multiVariables, variableNames);
            String pattern = d.spec.substring(d.desc.textStart, d.desc.textEnd);
            Context context = new Context(ctx);

            context.enterScope();
 
            boolean matches = new Matcher(context).matchesWithBind(context.variableForName("$_"), pattern);

            List<int[]> target = matches ? passed : failed;

            target.add(trim(d.spec, new int[] {d.desc.textStart, d.desc.textEnd}));

            if (matches) {
                evaluateConditions(d.desc.conditions, d.desc.conditionSpans, context, passed, failed, d);

                context.enterScope();

                for (FixTextDescription f : d.desc.fixes) {
                    evaluateConditions(f.conditions, f.conditionSpans, context, passed, failed, d);
                }

                context.leaveScope();
            }
        }
    }

    private static void evaluateConditions(Iterable<Condition> conditions, Iterable<int[]> conditionSpans, Context ctx, List<int[]> passed, List<int[]> failed, HintWrapper d) {
        Iterator<Condition> cond = conditions.iterator();
        Iterator<int[]> span = conditionSpans.iterator();

        while (cond.hasNext() && span.hasNext()) {
            boolean holds = cond.next().holds(ctx, true);
            List<int[]> condTarget = holds ? passed : failed;
            
            condTarget.add(trim(d.spec, span.next()));
        }
    }

    private static int[] trim(String spec, int[] span) {
        while (Character.isWhitespace(spec.charAt(span[0])))
            span[0]++;
        while (Character.isWhitespace(spec.charAt(span[1] - 1)))
            span[1]--;
        return span;
    }

    private static final AttributeSet PASSED = AttributesUtilities.createImmutable(StyleConstants.Background, Color.GREEN);
    private static final AttributeSet FAILED = AttributesUtilities.createImmutable(StyleConstants.Background, Color.RED);
    private static final Set<JavaTokenId> WHITESPACES = EnumSet.of(JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.WHITESPACE);

    private static int skipWhitespaces(CompilationInfo info, int pos, boolean forward) {
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        ts.move(pos);

        boolean moveSucceeded = false;
        
        while (forward ? ts.moveNext() : ts.movePrevious()) {
            moveSucceeded = true;
            if (!WHITESPACES.contains(ts.token().id())) {
                break;
            }
        }

        if (moveSucceeded) {
            return forward ? ts.offset() : ts.offset() + ts.token().length();
        } else {
            return pos;
        }
    }
    
    private static TreePath validateSelection(CompilationInfo ci, int start, int end) {
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);

        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();

            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);

            if (treeStart != start || treeEnd != end) {
                continue;
            }

            return tp;
        }

        return null;
    }

    public void cancel() {
        //XXX
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType=TestTokenId.MIME_TYPE, service=TaskFactory.class),
        @MimeRegistration(mimeType="text/x-java", service=TaskFactory.class)
    })
    public static final class FactoryImpl extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new EvaluationSpanTask());
        }
    }
}
