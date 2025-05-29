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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.formatters.Formatters;
import org.netbeans.modules.debugger.jpda.expr.formatters.FormattersLoopControl;
import org.netbeans.modules.debugger.jpda.expr.formatters.VariablesFormatter;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.ui.values.ComputeInlineValues;
import org.netbeans.modules.debugger.jpda.ui.values.ComputeInlineValues.InlineVariable;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;


public class InlineValueComputerImpl implements PreferenceChangeListener, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(InlineValueComputerImpl.class.getName());
    private static final RequestProcessor EVALUATOR = new RequestProcessor(InlineValueComputerImpl.class.getName(), 1, false, false);
    private static final String JAVA_STRATUM = "Java"; //XXX: this is probably already defined somewhere
    private final JPDADebuggerImpl debugger;
    private final Preferences prefs;
    private TaskDescription currentTask;

    private InlineValueComputerImpl(Session session) {
        debugger = (JPDADebuggerImpl) session.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(this);
        prefs = MimeLookup.getLookup("text/x-java").lookup(Preferences.class);
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName()) &&
            debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            setNewTask(null);
        }

        if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(evt.getPropertyName())) {
            refreshVariables();
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        refreshVariables();
    }

    private void refreshVariables() {
        CallStackFrame frame = debugger.getCurrentCallStackFrame();

        FileObject frameFile = null;
        int frameLineNumber = -1;
        Document frameDocument = null;

        if (prefs.getBoolean(Constants.KEY_INLINE_VALUES, Constants.DEF_INLINE_VALUES) &&
            frame != null && !frame.isObsolete() &&
            frame.getThread().isSuspended() &&
            JAVA_STRATUM.equals(frame.getDefaultStratum())) {
            try {
                String url = debugger.getEngineContext().getURL(frame, JAVA_STRATUM);
                frameFile = url != null ? URLMapper.findFileObject(URI.create(url).toURL()) : null;
                if (frameFile != null) {
                    frameLineNumber = frame.getLineNumber(JAVA_STRATUM);
                    EditorCookie ec = frameFile.getLookup().lookup(EditorCookie.class);
                    frameDocument = ec != null ? ec.getDocument() : null;
                }
            } catch (InternalExceptionWrapper | InvalidStackFrameExceptionWrapper | ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper | MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        TaskDescription newTask;

        if (frameFile != null && frameDocument != null) {
            newTask = new TaskDescription(frameFile, frameLineNumber, frameDocument);
        } else {
            newTask = null;
        }

        if (setNewTask(newTask)) {
            return;
        }

        if (newTask != null) {
            CountDownLatch computationDone = new CountDownLatch(1);

            newTask.addCancelCallback(computationDone::countDown);

            AtomicReference<Collection<InlineVariable>> values = new AtomicReference<>();

            EVALUATOR.post(() -> {
                OffsetsBag runningBag = new OffsetsBag(newTask.frameDocument);

                Lookup.getDefault().lookup(ComputeInlineVariablesFactory.class).set(newTask.frameFile, newTask.frameLineNumber, variables -> {
                    values.set(variables);
                    computationDone.countDown();
                });

                try {
                    computationDone.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (newTask.isCancelled()) {
                    return ;
                }

                Collection<InlineVariable> variables = values.get();

                if (variables == null) {
                    return ;
                }

                Map<String, Variable> expression2Value = new HashMap<>();
                Map<Integer, Map<String, String>> line2Values = new HashMap<>();

                for (InlineVariable v : variables) {
                    if (newTask.isCancelled()) {
                        return ;
                    }

                    Variable value = expression2Value.computeIfAbsent(v.expression(), expr -> {
                        try {
                            return debugger.evaluate(expr);
                        } catch (InvalidExpressionException ex) {
                            //the variable may not exist
                            LOG.log(Level.FINE, null, ex);
                            return null;
                        }
                    });
                    if (value != null) {
                        String valueText;
                        if (value instanceof ObjectVariable ov) {
                            valueText = toValue(ov).replace("\n", "\\n");
                        } else {
                            valueText = value.getValue();
                        }
                        line2Values.computeIfAbsent(v.lineEnd(), __ -> new LinkedHashMap<>())
                                   .putIfAbsent(v.expression(), v.expression() + " = " + valueText);
                        String mergedValues = line2Values.get(v.lineEnd()).values().stream().collect(Collectors.joining(", ", "  ", ""));
                        AttributeSet attrs = AttributesUtilities.createImmutable("virtual-text-prepend", mergedValues);

                        runningBag.addHighlight(v.lineEnd(), v.lineEnd() + 1, attrs);

                        setHighlights(newTask, runningBag);
                    }
                }
            });
        }
    }

    private String toValue(ObjectVariable variable) {
        //mostly copied from the VariablesFormatterFilter.getValueAt:
        FormattersLoopControl formattersLoop = new FormattersLoopControl();
        String type = variable.getType ();
        ObjectVariable ov = (ObjectVariable) variable;
        JPDAClassType ct = ov.getClassType();
        if (ct == null) {
            return ov.getValue();
        }
        VariablesFormatter f = Formatters.getFormatterForType(ct, formattersLoop.getFormatters());
        String[] formattersInLoopRef = new String[] { null };
        if (f != null && formattersLoop.canUse(f, ct.getName(), formattersInLoopRef)) {
            String code = f.getValueFormatCode();
            if (code != null && code.length() > 0) {
                try {
                    java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                    evaluateMethod.setAccessible(true);
                    Variable ret = (Variable) evaluateMethod.invoke(ov, code);
                    if (ret == null) {
                        return null;
                    }
                    return ret.getValue();
                } catch (java.lang.reflect.InvocationTargetException itex) {
                    Throwable t = itex.getTargetException();
                    if (t instanceof InvalidExpressionException) {
                        return ov.getValue();
                    } else {
                        Exceptions.printStackTrace(t);
                    }
                } catch (ReflectiveOperationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else if (formattersInLoopRef[0] != null) {
            //ignore loops(?)
        }
        if (VariablesFormatterFilter.isToStringValueType(type)) {
            try {
                return "\""+ov.getToStringValue ()+"\"";
            } catch (InvalidExpressionException ex) {
                // Not a supported operation (e.g. J2ME, see #45543)
                // Or missing context or any other reason
                Logger.getLogger(VariablesFormatterFilter.class.getName()).fine("getToStringValue() "+ex.getLocalizedMessage());
                if (ex.getTargetException () instanceof UnsupportedOperationException) {
                    // PATCH for J2ME. see 45543
                    return ov.getValue();
                }
                return ex.getLocalizedMessage ();
            }
        }
        return ov.getValue();
    }

    private synchronized boolean setNewTask(TaskDescription newTask) {
        if (Objects.equals(currentTask, newTask)) {
            return true; //nothing changed, nothing to do
        }

        if (currentTask != null) {
            currentTask.cancel();
            getHighlightsBag(currentTask.frameDocument).clear();
        }

        currentTask = newTask;

        return false;
    }

    private synchronized void setHighlights(TaskDescription task, OffsetsBag highlights) {
        if (!task.isCancelled()) {
            getHighlightsBag(currentTask.frameDocument).setHighlights(highlights);
        }
    }

    @DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
    public static final class Init extends DebuggerManagerAdapter {
        @Override
        public void sessionAdded(Session session) {
            new InlineValueComputerImpl(session);
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
                private final AtomicBoolean cancel = new AtomicBoolean();

                @Override
                public void cancel() {
                    cancel.set(true);
                }

                @Override
                public void run(CompilationInfo info) throws Exception {
                    cancel.set(false);

                    int line;
                    Consumer<Collection<InlineVariable>> target;

                    synchronized (ComputeInlineVariablesFactory.this) {
                        if (!info.getFileObject().equals(currentFile)) {
                            return ;
                        }
                        line = currentLineNumber;
                        target = currentTarget;
                    }

                    Collection<InlineVariable> variables = ComputeInlineValues.computeVariables(info, line, 1, cancel);

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

    private static final class TaskDescription {
        public final FileObject frameFile;
        public final int frameLineNumber;
        public final Document frameDocument;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final List<Runnable> cancelCallbacks =
                Collections.synchronizedList(new ArrayList<>());

        public TaskDescription(FileObject frameFile, int frameLineNumber, Document frameDocument) {
            this.frameFile = frameFile;
            this.frameLineNumber = frameLineNumber;
            this.frameDocument = frameDocument;
        }

        public void cancel() {
            cancelled.set(true);

            List<Runnable> callbacks;

            synchronized (cancelCallbacks) {
                callbacks = new ArrayList<>(cancelCallbacks);
            }

            for (Runnable r : callbacks) {
                r.run();
            }
        }

        public void addCancelCallback(Runnable r) {
            cancelCallbacks.add(r);

            if (cancelled.get()) {
                r.run();
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.frameFile);
            hash = 53 * hash + this.frameLineNumber;
            hash = 53 * hash + System.identityHashCode(this.frameDocument);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TaskDescription other = (TaskDescription) obj;
            if (this.frameLineNumber != other.frameLineNumber) {
                return false;
            }
            if (!Objects.equals(this.frameFile, other.frameFile)) {
                return false;
            }
            return this.frameDocument == other.frameDocument;
        }

        public boolean isCancelled() {
            return cancelled.get();
        }

    }
    @MimeRegistration(mimeType="text/x-java", service=HighlightsLayerFactory.class)
    public static HighlightsLayerFactory createHighlightsLayerFactory() {
        return new HighlightsLayerFactory() {
            @Override
            public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
                return new HighlightsLayer[] {
                    HighlightsLayer.create(InlineValueComputerImpl.class.getName(), ZOrder.SYNTAX_RACK.forPosition(1400), false, getHighlightsBag(context.getDocument()))
                };
            }
        };
    }

    private static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(InlineValueComputerImpl.class);
        if (bag == null) {
            doc.putProperty(InlineValueComputerImpl.class, bag = new OffsetsBag(doc, true));
        }
        return bag;
    }

}
