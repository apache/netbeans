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
package org.netbeans.modules.refactoring.java.callhierarchy;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ScanUtils;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.lsp.CallHierarchyEntry;
import org.netbeans.api.lsp.Range;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lsp.CallHierarchyProvider;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Implementation of LSP call hierarchy for the java Mime type.
 * @author sdedic
 */
@MimeRegistration(mimeType = "text/x-java", service = CallHierarchyProvider.class)
public class LspCallHierarchyProvider implements CallHierarchyProvider {
    private static final RequestProcessor HIERARCHY_RP = new RequestProcessor();
    
    private static final Element findEclosingExecutable(CompilationInfo ci, TreePath tp) {
        while (tp != null && tp.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
            switch (tp.getLeaf().getKind()) {
                case METHOD:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    return ci.getTrees().getElement(tp);
            }
            tp = tp.getParentPath();
        }
        return null;
    }
    
    @Override
    public CompletableFuture<List<CallHierarchyEntry>> findCallOrigin(Document doc, int offset) {
        class OriginT extends UserTask {
            private final CompletableFuture<List<CallHierarchyEntry>> control;

            public OriginT(CompletableFuture<List<CallHierarchyEntry>> control) {
                this.control = control;
            }
            
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult(offset);
                if ("text/x-java".equals(r.getSnapshot().getMimeType())) {
                    CompilationInfo ci = CompilationInfo.get(r);
                    if (ci == null || r.getSnapshot().getSource().getFileObject() == null) {
                        control.complete(null);
                        return;
                    }
                    TreePath tp = ci.getTreeUtilities().pathFor(offset);
                    if (tp == null) {
                        control.complete(null);
                        return;
                    }
                    
                    Element e = findEclosingExecutable(ci, tp);
                    if (e == null) {
                        return;
                    }
                    
                    StructureElement se = ElementHeaders.toStructureElement(ci, e, null);
                    CallHierarchyEntry item = new CallHierarchyEntry(se, signature(e));
                    control.complete(Collections.singletonList(item));
                    return;
                }
                for (Embedding e : resultIterator.getEmbeddings()) {
                    // interrupt embedding search, results are already reported or the future cancelled.
                    if (control.isDone()) {
                        return;
                    }
                    run(resultIterator.getResultIterator(e));
                }
            }
        }
        
        CompletableFuture<List<CallHierarchyEntry>> res = new CompletableFuture<List<CallHierarchyEntry>>();
        HIERARCHY_RP.post(() -> {
            try {
                ParserManager.parse(Collections.singletonList(Source.create(doc)), new OriginT(res));
            } catch (ParseException ex) {
                res.completeExceptionally(ex);
            }
        });
        return res;
    }
    
    /**
     * Special implementation of Future that relays {@code cancel()} to a {@link Cancellable}.
     * @param <T> 
     */
    private static class CancellableF<T> extends CompletableFuture<T> {
        private volatile Cancellable c;

        public CancellableF() {
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            Cancellable c2 = this.c;
            if (mayInterruptIfRunning && c2 != null) {
                return c2.cancel();
            } else {
                return false;
            }
        }
    }
    
    private static String signature(Element e) {
        ElementHandle h = ElementHandle.create(e);
        String extra = h.getKind().name() + "/" + String.join("/", SourceUtils.getJVMSignature(h));
        return extra;
    }
    
    static abstract class CallTask implements Task<CompilationController>, Cancellable {
        final CallHierarchyModel.HierarchyType type;
        final CancellableF<List<CallHierarchyEntry.Call>> res = new CancellableF<>();
        final CallHierarchyEntry callTarget;
        final AtomicBoolean cancelled = new AtomicBoolean();
        final FileObject fo;
        protected volatile CompletableFuture toCancel;

        public CallTask(CallHierarchyEntry callTarget, CallHierarchyModel.HierarchyType type) {
            this.callTarget = callTarget;
            this.type = type;
            this.fo = callTarget.getElement().getFile();

            res.c = this;
        }

        @Override
        public boolean cancel() {
            CompletableFuture tc = toCancel;
            return cancelled.getAndSet(true) && (tc == null || tc.cancel(true));
        }
        
        public void run(CompilationController parameter) throws Exception {
            List<CallHierarchyEntry.Call> calls = new ArrayList<>();
            int s = callTarget.getElement().getSelectionStartOffset();
            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TreePath p = parameter.getTreeUtilities().pathFor(s);
            Element e = null;

            if (p != null) {
                Element candidate = parameter.getTrees().getElement(p);
                if (candidate != null) {
                    // just do a sanity check
                    if (callTarget.getCustomData() == null ||
                        callTarget.getCustomData().equals(signature(candidate))) {
                        e = candidate;
                    }
                }
            }

            if (e == null) {
                res.complete(null);
                return;
            }
            TreePathHandle tph = TreePathHandle.create(e, parameter);

            CallHierarchyTasks.RootResolver rr = new CallHierarchyTasks.RootResolver(tph, true, true);
            rr.run(parameter);

            CallHierarchyModel m = CallHierarchyModel.create(tph, 
                    EnumSet.of(CallHierarchyModel.Scope.ALL, CallHierarchyModel.Scope.BASE), type);
            m.replaceRoot(rr.getRoot());

            Call rootCall = m.getRoot();
            m.computeCalls(m.getRoot(), () -> processComputedCall(parameter, rootCall));
        }
        
        protected abstract CallHierarchyEntry.Call createCall(StructureElement se, Call c, String signature);
        
        protected CompletableFuture<List<CallHierarchyEntry.Call>> processAsync(CompilationInfo info, List<Call> refs, List<CallHierarchyEntry.Call> calls) {
            List<CompletableFuture<StructureElement>> delayed = new ArrayList<>();
            List<String> signatures = new ArrayList<>();
            List<Call> delayedRefs = new ArrayList<>();
            
            for (Call c : refs) {
                TreePathHandle targetH = c.selection;
                Element target = targetH.getElementHandle().resolve(info);

                CompletableFuture<StructureElement> elementFuture = ElementHeaders.resolveStructureElement(info, target, true);
                if (elementFuture.isDone()) {
                    try {
                        calls.add(createCall(elementFuture.get(), c, signature(target)));
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof CancellationException) {
                            throw (CancellationException)cause;
                        }
                        throw new IllegalStateException(ex);
                    } catch (InterruptedException ex) {
                        CancellationException t = new CancellationException();
                        t.initCause(ex);
                        throw t;
                    }
                } else {
                    signatures.add(signature(target));
                    delayedRefs.add(c);
                    delayed.add(elementFuture);
                }
            }
            if (delayed.isEmpty()) {
                return CompletableFuture.completedFuture(calls); 
            }

            return CompletableFuture.allOf(delayed.stream().filter(Objects::nonNull).toArray((i) -> new CompletableFuture[i])).
                thenApply(x -> {
                    int index = 0; 
                    for (CompletableFuture<StructureElement> f : delayed) {
                        if (f != null) {
                            StructureElement se = f.getNow(null);
                            if (se != null) {
                                calls.add(createCall(se, refs.get(index), signatures.get(index)));
                            }
                        }
                        index++;
                    }
                    return calls;
                });
        }

        public CompletableFuture<List<CallHierarchyEntry.Call>> process() {
            JavaSource js = JavaSource.forFileObject(fo);
            if (js == null) {
                return null;
            }
            HIERARCHY_RP.post(() -> {
                try {
                    ScanUtils.waitUserActionTask(js, this);
                } catch (IOException | RuntimeException ex) {
                    res.completeExceptionally(ex);
                }
            });
            return toCancel != null ? toCancel : res;
        }

        protected void processComputedCall(CompilationInfo info, Call rootCall) {
            List<CallHierarchyEntry.Call> calls = new ArrayList<>();
            List<Call> refs = rootCall.getReferences();

            if (cancelled.get()) {
                return;
            }
            toCancel = processAsync(info, refs, calls);
            toCancel.handle((r, ex) -> {
                if (ex != null) { 
                    res.completeExceptionally((Throwable)ex);
                } else {
                    res.complete(calls);
                }
                return null;
            });
        }
    }
    
    @Override
    public CompletableFuture<List<CallHierarchyEntry.Call>> findIncomingCalls(CallHierarchyEntry callTarget) {
        
        class T extends CallTask {

            public T(CallHierarchyEntry callTarget) {
                super(callTarget, CallHierarchyModel.HierarchyType.CALLER);
            }

            @Override
            protected CallHierarchyEntry.Call createCall(StructureElement se, Call c, String signature) {
                CallHierarchyEntry i = new CallHierarchyEntry(se, signature);

                List<Range> ranges = new ArrayList<>();
                for (CallOccurrence oc : c.getOccurrences()) {
                    PositionBounds pb = oc.getSelectionBounds();
                    ranges.add(new Range(pb.getBegin().getOffset(), pb.getEnd().getOffset()));
                }
                return new CallHierarchyEntry.Call(i, ranges);
            }
        }
        return new T(callTarget).process();
    }

    @Override
    public CompletableFuture<List<CallHierarchyEntry.Call>> findOutgoingCalls(CallHierarchyEntry callSource) {
        class T extends CallTask {
            
            public T(CallHierarchyEntry callTarget) {
                super(callTarget, CallHierarchyModel.HierarchyType.CALLEE);
            }
            
            protected CallHierarchyEntry.Call createCall(StructureElement se, Call c, String signature) {
                CallHierarchyEntry i = new CallHierarchyEntry(se, signature);

                List<Range> ranges = new ArrayList<>();
                for (CallOccurrence oc : c.getOccurrences()) {
                    PositionBounds pb = oc.getSelectionBounds();
                    ranges.add(new Range(pb.getBegin().getOffset(), pb.getEnd().getOffset()));
                }
                return new CallHierarchyEntry.Call(i, ranges);
            }
            
            /*
            @Override
            protected void processComputedCall(CompilationInfo info, Call rootCall) {
                List<CallHierarchyEntry.Call> calls = new ArrayList<>();
                List<Call> refs = rootCall.getReferences();
                
                if (cancelled.get()) {
                    return;
                }
                toCancel = processAsync(info, refs, calls);
                toCancel.handle((r, ex) -> {
                    if (ex != null) { 
                        res.completeExceptionally((Throwable)ex);
                    } else {
                        res.complete(calls);
                    }
                    return null;
                });
            }
            */
        }
        return new T(callSource).process();
    } 
}
