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

package org.netbeans.modules.debugger.jpda.projectsui;

import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class LambdaBreakpointManager extends DebuggerManagerAdapter {

    private static volatile JPDADebugger currentDebugger = null; //XXX: static???

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS, DebuggerManager.PROP_DEBUGGER_ENGINES };
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        refreshAfterChange(breakpoint);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        refreshAfterChange(breakpoint);
    }

    private void refreshAfterChange(Breakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint lb) {
            try {
                URL currentURL = new URL(lb.getURL());
                FileObject fo = URLMapper.findFileObject(currentURL);

                if (fo != null) {
                    FactoryImpl.doRefresh(fo);
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName ();
        if (propertyName == null) {
            return;
        }
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(propertyName)) {
            setCurrentDebugger(DebuggerManager.getDebuggerManager().getCurrentEngine());
        }
        if ( (!LineBreakpoint.PROP_URL.equals (propertyName)) &&
             (!LineBreakpoint.PROP_LINE_NUMBER.equals (propertyName))
        ) {
            return;
        }
        JPDABreakpoint b = (JPDABreakpoint) evt.getSource ();
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        Breakpoint[] bkpts = manager.getBreakpoints();
        boolean found = false;
        for (int x = 0; x < bkpts.length; x++) {
            if (b == bkpts[x]) {
                found = true;
                break;
            }
        }
        if (!found) {
            // breakpoint has been removed
            return;
        }
    }

    private void setCurrentDebugger(DebuggerEngine engine) {
        JPDADebugger oldDebugger = currentDebugger;
        if (oldDebugger != null) {
            oldDebugger.removePropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
        }
        boolean active = true;
        JPDADebugger debugger = null;
        if (engine != null) {
            debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger != null) {
                debugger.addPropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
                active = debugger.getBreakpointsActive();
            }
        }
        currentDebugger = debugger;
    }

    //TODO: requires dependency on java.source - maybe try to rewrite to Schedulers!
    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class FactoryImpl extends EditorAwareJavaSourceTaskFactory {

        private BreakpointAnnotationProvider bap;

        public FactoryImpl() {
            super(Phase.PARSED, Priority.BELOW_NORMAL);
        }

        private BreakpointAnnotationProvider getAnnotationProvider() {
            if (bap == null) {
                bap = BreakpointAnnotationProvider.getInstance();
            }
            return bap;
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new CancellableTask<CompilationInfo>() {
                private final AtomicBoolean canceled = new AtomicBoolean();

                @Override
                public void cancel() {
                    canceled.set(true);
                }

                @Override
                public void run(CompilationInfo info) throws Exception {
                    canceled.set(false);

                    String currentFile = info.getFileObject().toURL().toString();
                    Map<Integer, Map<Integer, LineBreakpoint>> lines2LambdaIndexes = new HashMap<>();

                    for (Breakpoint b : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                        if (b instanceof LineBreakpoint lb && currentFile.equals(lb.getURL())) {
                            lines2LambdaIndexes.computeIfAbsent(lb.getLineNumber(), x -> new HashMap<>())
                                               .put(lb.getLambdaIndex(), lb);
                        }
                    }

                    LineMap lineMap = info.getCompilationUnit().getLineMap();

                    new CancellableTreePathScanner<Void, Void>(canceled) {
                        int currentLine = -1;
                        int currentLambdaIndex = -1;
                        public Void scan(Tree tree, Void v) {
                            if (tree != null && tree.getKind() != Tree.Kind.COMPILATION_UNIT) {
                                long startPos = info.getTrees().getSourcePositions().getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                                if (startPos != (-1)) {
                                    int line = (int) lineMap.getLineNumber(startPos);

                                    if (line != currentLine) {
                                        currentLine = line;
                                        currentLambdaIndex = 0;
                                    }
                                }
                            }
                            return super.scan(tree, v);
                        }
                        public Void visitLambdaExpression(LambdaExpressionTree tree, Void v) {
                            long startPos = info.getTrees().getSourcePositions().getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                            int startLine = (int) lineMap.getLineNumber(startPos);
                            Map<Integer, LineBreakpoint> existingLineBreakpoints = lines2LambdaIndexes.get(startLine);
                            LineBreakpoint existingLineBreakpoint = existingLineBreakpoints != null ? existingLineBreakpoints.remove(currentLambdaIndex) : null;

                            if (existingLineBreakpoints != null && existingLineBreakpoints.containsKey(-1)) {
                                //the line breakpoint exists, ensure the lambda breakpoint exists and is updated
                                if (existingLineBreakpoint == null) {
                                    LineBreakpoint lb = LineBreakpoint.create(currentFile, startLine);
                                    lb.setLambdaIndex(currentLambdaIndex);
                                    lb.disable();
                                    DebuggerManager.getDebuggerManager().addBreakpoint(lb);
                                } else {
                                    long endPos = info.getTrees().getSourcePositions().getEndPosition(getCurrentPath().getCompilationUnit(), tree);
                                    int endLine = (int) lineMap.getLineNumber(endPos);
                                    int startColumn = (int) lineMap.getColumnNumber(startPos) - 1;
                                    int endColumn;
                                    if (startLine == endLine) {
                                        endColumn = (int) lineMap.getColumnNumber(endPos) - 1;
                                    } else {
                                        endColumn = (int) lineMap.getStartPosition(startLine + 1) - 1;
                                    }

                                    int length = endColumn - startColumn;

                                    Set<Annotation> annotations = getAnnotationProvider().getAnnotationsForBreakpoint(existingLineBreakpoint);

                                    for (Annotation ann : annotations) {
                                        if (!(ann.getAttachedAnnotatable() instanceof Part p) || p.getLine().getLineNumber() != startLine || p.getColumn() != startColumn || p.getLength() != length) {
                                            LineCookie lc = info.getFileObject().getLookup().lookup(LineCookie.class);

                                            if (lc == null) {
                                                continue;
                                            }

                                            Line line = lc.getLineSet().getCurrent(startLine - 1);
                                            Part part = line.createPart(startColumn, length);

                                            if (canceled.get()) {
                                                //don't set the part if cancelled - the positions may be wrong
                                                return null;
                                            }

                                            ann.detach();
                                            ann.attach(part);
                                        }
                                    }
                                }
                            }

                            currentLambdaIndex++;

                            return super.visitLambdaExpression(tree, v);
                        }
                    }.scan(info.getCompilationUnit(), null);

                    //remove any stale lambda breakpoints:
                    for (Breakpoint b : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                        if (b instanceof LineBreakpoint lb && currentFile.equals(lb.getURL()) && lb.getLambdaIndex() >=0) {
                            Map<Integer, LineBreakpoint> staleLambdaBreakpoints = lines2LambdaIndexes.getOrDefault(lb.getLineNumber(), Map.of());
                            if (staleLambdaBreakpoints.containsKey(lb.getLambdaIndex()) || !staleLambdaBreakpoints.containsKey(-1)) {
                                DebuggerManager.getDebuggerManager().removeBreakpoint(lb);
                            }
                        }
                    }
                }
            };
        }

        public static void doRefresh(FileObject file) {
            for (JavaSourceTaskFactory f : Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
                if (f instanceof FactoryImpl impl) {
                    impl.reschedule(file);
                }
            }
        }
    }

}
