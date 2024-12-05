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
package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import static com.sun.source.tree.Tree.Kind.BLOCK;
import static com.sun.source.tree.Tree.Kind.LAMBDA_EXPRESSION;
import static com.sun.source.tree.Tree.Kind.METHOD;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lsp.InlineValue;
import org.netbeans.api.lsp.Position;
import org.netbeans.api.lsp.Range;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.lsp.InlineValuesProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;


@DebuggerServiceRegistration(path="netbeans-JPDASession/inlineValue", types=InlineValueComputer.class)
public class InlineValueComputerImpl implements InlineValueComputer, PropertyChangeListener {

    private static final RequestProcessor EVALUATOR = new RequestProcessor(InlineValueProviderImpl.class.getName(), 1, false, false);
    private static final String JAVA_STRATUM = "Java"; //XXX: this is probably already defined somewhere
    private final JPDADebuggerImpl debugger;
    private Document lastDocument;

    public InlineValueComputerImpl(ContextProvider contextProvider) {
        debugger = (JPDADebuggerImpl) contextProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(evt.getPropertyName())) {
            CallStackFrame frame = debugger.getCurrentCallStackFrame();

            FileObject frameFile = null;
            int frameLineNumber = -1;
            Document frameDocument = null;

            if (frame != null && !frame.isObsolete() &&
                frame.getThread().isSuspended() &&
                JAVA_STRATUM.equals(frame.getDefaultStratum())) {
                //TODO: more checks...
                try {
                    String url = debugger.getEngineContext().getURL(frame, JAVA_STRATUM);
                    frameFile = url != null ? URLMapper.findFileObject(URI.create(url).toURL()) : null;
                    if (frameFile != null) {
                        frameLineNumber = frame.getLineNumber(JAVA_STRATUM);
                        EditorCookie ec = frameFile.getLookup().lookup(EditorCookie.class);
                        frameDocument = ec.getDocument(); //TODO: might be null!!!
                    }
                } catch (InternalExceptionWrapper | InvalidStackFrameExceptionWrapper | ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper | MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            synchronized (this) {
                if (lastDocument != frameDocument) {
                    if (lastDocument != null) {
                        getHighlightsBag(lastDocument).clear();
                    }

                    lastDocument = frameDocument;
                }
            }

            if (frameFile != null && frameDocument != null) {
                //TODO: cancel any already running computation if the configuration is different:
                OffsetsBag currentDocumentBag = getHighlightsBag(frameDocument);
                OffsetsBag runningBag = new OffsetsBag(frameDocument);

                Lookup.getDefault().lookup(ComputeInlineVariablesFactory.class).set(frameFile, frameLineNumber, variables -> {
                    Map<String, Variable> expression2Value = new HashMap<>();
                    Map<Integer, Map<String, String>> line2Values = new HashMap<>();

                    for (InlineVariable v : variables) {
                        EVALUATOR.post(() -> {
                            Variable value = expression2Value.computeIfAbsent(v.expression, expr -> {
                                try {
                                    return debugger.evaluate(expr);
                                } catch (InvalidExpressionException ex) {
                                    Exceptions.printStackTrace(ex);
                                    return null; //TODO: avoid re-evaluation(!)
                                }
                            });
                            if (value != null) {
                                line2Values.computeIfAbsent(v.lineEnd, __ -> new LinkedHashMap<>())
                                           .putIfAbsent(v.expression, v.expression + " = " + value.getValue());
                                runningBag.addHighlight(v.lineEnd, v.lineEnd + 1, AttributesUtilities.createImmutable("virtual-text-prepend", "  " + line2Values.get(v.lineEnd).values().stream().collect(Collectors.joining(", "))));
                                currentDocumentBag.setHighlights(runningBag);
                            }
                        });
                    }
                });
            }
        }
    }

    @DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
    public static final class Init extends DebuggerManagerAdapter {
        @Override
        public void sessionAdded(Session session) {
            for (InlineValueComputer v : session.lookup("inlineValue", InlineValueComputer.class)) {
            }
        }
    }

    @ServiceProviders({
        @ServiceProvider(service=JavaSourceTaskFactory.class),
        @ServiceProvider(service=ComputeInlineVariablesFactory.class)
    })
    public static final class ComputeInlineVariablesFactory extends JavaSourceTaskFactory {

        private FileObject currentFile;
        private int currentLineNumber;
        private Consumer<Collection<InlineVariable>> currentTarget;

        public ComputeInlineVariablesFactory() {
            super(Phase.UP_TO_DATE, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new CancellableTask<CompilationInfo>() {
                @Override
                public void cancel() {
                    //TODO
                }

                @Override
                public void run(CompilationInfo info) throws Exception {
                    int line;
                    Consumer<Collection<InlineVariable>> target;

                    synchronized (ComputeInlineVariablesFactory.this) {
                        if (!info.getFileObject().equals(currentFile)) {
                            return ;
                        }
                        line = currentLineNumber;
                        target = currentTarget;
                    }

                    Collection<InlineVariable> variables = computeVariables(info, line, 1);

                    target.accept(variables);
                }
            };
        }

        @Override
        protected synchronized Collection<FileObject> getFileObjects() {
            return currentFile != null ? List.of(currentFile) : List.of();
        }

        public synchronized void set(FileObject currentFile, int lineNumber, Consumer<Collection<InlineVariable>> target) {
            this.currentFile = currentFile;
            this.currentLineNumber = lineNumber;
            this.currentTarget = target;
            fileObjectsChanged();
            if (currentFile != null) {
                reschedule(currentFile);
            }
        }
    }

    //TODO: cancel
    private static Collection<InlineVariable> computeVariables(CompilationInfo info, int stackLine, int stackCol) {
        Collection<InlineVariable> result = new ArrayList<>();
        int donePos = (int) info.getCompilationUnit().getLineMap().getPosition(stackLine, stackCol);
        int upcomingPos = (int) info.getCompilationUnit().getLineMap().getStartPosition(stackLine + 1);
        TreePath relevantPoint = info.getTreeUtilities().pathFor(donePos);
        OUTER: while (relevantPoint != null) {
            Tree leaf = relevantPoint.getLeaf();
            switch (leaf.getKind()) {
                case METHOD: case LAMBDA_EXPRESSION: break OUTER;
                case BLOCK:
                    if (relevantPoint.getParentPath() != null && TreeUtilities.CLASS_TREE_KINDS.contains(relevantPoint.getParentPath().getLeaf().getKind())) {
                        break OUTER;
                    }
            }
            relevantPoint = relevantPoint.getParentPath();
        }
        LineMap lm = info.getCompilationUnit().getLineMap();
        new CancellableTreePathScanner<Void, Void>() {
            @Override
            public Void visitVariable(VariableTree node, Void p) {
                int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), node);
                if (end < donePos) {
                    int[] span = info.getTreeUtilities().findNameSpan(node); //TODO: might return null
                    int lineEnd = (int) (lm.getStartPosition(lm.getLineNumber(span[1]) + 1) - 1);

                    result.add(new InlineVariable(span[0], span[1], lineEnd, node.getName().toString()));
                }
                return super.visitVariable(node, p);
            }

            //TODO: visitIdent
            //TODO: fields?

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                //XXX: check the element kind!
                Element el = info.getTrees().getElement(getCurrentPath());

                if (el != null && el.getKind().isVariable()) {
                    int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), node);
                    int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), node);

                    if (start != (-1) && end != (-1)) {
                        int lineEnd = (int) (lm.getStartPosition(lm.getLineNumber(end) + 1) - 1);

                        result.add(new InlineVariable(start, end, lineEnd, node.getName().toString()));
                    }
                }

                return super.visitIdentifier(node, p);
            }

            @Override
            public Void scan(Tree tree, Void p) {
                if (tree != null) {
                    int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);

                    if (start > upcomingPos) {
                        return null;
                    }
                }
                return super.scan(tree, p);
            }

        }.scan(relevantPoint, null);

        return result;
    }

    public static final class InlineVariable {
        public final int start;
        public final int end;
        public final int lineEnd;
        public final String expression;

        public InlineVariable(int start, int end, int lineEnd, String expression) {
            this.start = start;
            this.end = end;
            this.lineEnd = lineEnd;
            this.expression = expression;
        }

    }

    @MimeRegistration(mimeType="text/x-java", service=HighlightsLayerFactory.class)
    public static HighlightsLayerFactory createHighlightsLayerFactory() {
        return new HighlightsLayerFactory() {
            @Override
            public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
                return new HighlightsLayer[] {
                    HighlightsLayer.create(InlineValueProviderImpl.class.getName(), ZOrder.SYNTAX_RACK.forPosition(1400), false, getHighlightsBag(context.getDocument()))
                };
            }
        };
    }

    private static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(InlineValueProviderImpl.class);
        if (bag == null) {
            doc.putProperty(InlineValueProviderImpl.class, bag = new OffsetsBag(doc, true));
        }
        return bag;
    }

    @MimeRegistration(mimeType="text/x-java", service=InlineValuesProvider.class)
    public static final class InlineValueProviderImpl implements InlineValuesProvider {

        @Override
        public CompletableFuture<List<? extends InlineValue>> inlineValues(FileObject file, int currentExecutionPosition) {
            //TODO: proper cancellability
            JavaSource js = JavaSource.forFileObject(file);
            CompletableFuture<List<? extends InlineValue>> result = new CompletableFuture<>();
            List<InlineValue> resultValues = new ArrayList<>();
            if (js != null) {
                try {
                    js.runUserActionTask(cc -> {
                        cc.toPhase(JavaSource.Phase.RESOLVED);
                        int stackLine = (int) cc.getCompilationUnit().getLineMap().getLineNumber(currentExecutionPosition);
                        int stackCol = (int) cc.getCompilationUnit().getLineMap().getColumnNumber(currentExecutionPosition);

                        for (InlineVariable var : computeVariables(cc, stackLine, stackCol)) {
                            resultValues.add(InlineValue.createInlineVariable(new Range(var.start, var.end), var.expression));
                        }
                    }, true);
                } catch (IOException ex) {
                    result.completeExceptionally(ex);
                    return result;
                }
            }
            result.complete(resultValues);
            return result;
        }

    }
}
