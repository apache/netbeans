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
package org.netbeans.modules.cnd.highlight.error;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider.EditorEvent;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
public final class HighlightProvider  {
    
    /** for test purposes only! */
    public interface Hook {
        void highlightingDone(String absoluteFileName, List<ErrorDescription> descriptions);
    }
    private static final boolean TRACE_TASKS = false;
    
    private Hook hook;
    
    public static final boolean TRACE_ANNOTATIONS = Boolean.getBoolean("cnd.highlight.trace.annotations"); // NOI18N
    
    private static final HighlightProvider instance = new HighlightProvider();
    
    /** for test purposes only! */
    public synchronized  void setHook(Hook hook) {
        this.hook = hook;
    }
    
    public static HighlightProvider getInstance(){
        return instance;
    }
    private final Lookup.Result<CsmErrorProvider> res;
    private final RequestProcessor RP;
    private final Map<CsmErrorProvider, MyTask> tasks;
    private final AtomicInteger processingLevel = new AtomicInteger(0);
    
    /** Creates a new instance of HighlightProvider */
    private HighlightProvider() {
        res = Lookup.getDefault().lookupResult(CsmErrorProvider.class);
        RP = new RequestProcessor("HighlightProvider", 1); // NOI18N
        tasks = new ConcurrentHashMap<>();
    }
    
    /* package */ void update(CsmFile file, Document doc, DataObject dao, InterrupterImpl interrupter) {
        assert doc!=null || file==null;
        if (doc instanceof BaseDocument){
            addAnnotations((BaseDocument)doc, file, dao, interrupter);
        }
    }
    
    /* package */ void clear(Document doc) {
        assert doc!=null;
        if (doc instanceof BaseDocument){
            for(final CsmErrorProvider provider : res.allInstances() ) {
                removeAnnotations(doc, provider.getName());
            }
            CppUpToDateStatusProvider.get((BaseDocument) doc).setUpToDate(UpToDateStatus.UP_TO_DATE_OK);
        }
    }
    
    private static org.netbeans.spi.editor.hints.Severity getSeverity(CsmErrorInfo info) {
        switch( info.getSeverity() ) {
            case ERROR:     return org.netbeans.spi.editor.hints.Severity.ERROR;
            case WARNING:   return org.netbeans.spi.editor.hints.Severity.WARNING;
            case HINT:   return org.netbeans.spi.editor.hints.Severity.HINT;
            default:        throw new IllegalArgumentException("Unexpected severity: " + info.getSeverity()); //NOI18N
        }
    }
    
    private void addAnnotations(final BaseDocument doc, final CsmFile file, final DataObject dao, final InterrupterImpl interrupter) {
        EditorEvent event;
        if (CsmFileInfoQuery.getDefault().isDocumentBasedFile(file)) {
            event = EditorEvent.DocumentBased;
        } else {
            event = EditorEvent.FileBased;
        }
        List<CsmErrorProvider> list = new ArrayList<>();
        for(final CsmErrorProvider provider : res.allInstances() ) {
            if (interrupter.cancelled()) {
                return;
            }
            if (provider.isSupportedEvent(event)) {
                list.add(provider);
            }
        }
        for(final CsmErrorProvider provider : list) {
            if (!tasks.containsKey(provider)) {
                tasks.put(provider, new MyTask(provider));
            }
        }
        if (TRACE_ANNOTATIONS) System.err.printf("\nSetting annotations for %s\n", file);

        if (interrupter.cancelled()) {
            return;
        }
        synchronized (processingLevel) {
            if (processingLevel.getAndIncrement() == 0) {
                CppUpToDateStatusProvider.get(doc).setUpToDate(UpToDateStatus.UP_TO_DATE_PROCESSING);
            }
        }
        
        final List<ResponseImpl> responces = new ArrayList<>();
        final RequestImpl request = new RequestImpl(file, doc, event, interrupter);
        final CountDownLatch wait = new CountDownLatch(list.size());
        for(final CsmErrorProvider provider : list) {
            if (interrupter.cancelled()) {
                wait.countDown();
                continue;
            }
            final ResponseImpl response = new ResponseImpl(provider, interrupter, dao, doc);
            responces.add(response);
            MyTask myTask = tasks.get(provider);
            myTask.post(request, response, wait);
        }
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    wait.await();
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.err);
                }

                synchronized (processingLevel) {
                    if (processingLevel.decrementAndGet() == 0) {
                        CppUpToDateStatusProvider.get(doc).setUpToDate(UpToDateStatus.UP_TO_DATE_OK);
                    }
                }
                Hook theHook = HighlightProvider.this.hook;
                if( theHook != null ) {
                    List<ErrorDescription> descriptions = new ArrayList<>();
                    for(ResponseImpl responce : responces) {
                        descriptions.addAll(responce.descriptions);
                    }
                    theHook.highlightingDone(file.getAbsolutePath().toString(), descriptions);
                }
            }
        });
    }
    
    private static PositionBounds createPositionBounds(DataObject dao, int start, int end) {
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(dao);
        if (ces != null) {
            PositionRef posBeg = ces.createPositionRef(start, Position.Bias.Forward);
            PositionRef posEnd = ces.createPositionRef(end, Position.Bias.Backward);
            return new PositionBounds(posBeg, posEnd);
        }
        return null;
    }
    
    private void removeAnnotations(Document doc, String layer) {
        HintsController.setErrors(doc, layer, Collections.<ErrorDescription>emptyList());
    }

    // package-local for test purposes
    static final class RequestImpl implements CsmErrorProvider.Request {

        private final CsmFile file;
        private final Interrupter interrupter;
        private final Document document;
        private final EditorEvent event;
        
        public RequestImpl(CsmFile file, Document doc, EditorEvent event, Interrupter interrupter) {
            this.file = file;
            this.interrupter = interrupter;
            this.document = doc;
            this.event = event;
        }

        @Override
        public CsmFile getFile() {
            return file;
        }

        @Override
        public boolean isCancelled() {
            return interrupter.cancelled();
        }

        @Override
        public Document getDocument() {
            return document;
        }

        @Override
        public EditorEvent getEvent() {
            return event;
        }
    }

    private static final class ResponseImpl implements CsmErrorProvider.Response {

        private final List<ErrorDescription> descriptions = new ArrayList<>();
        private final CsmErrorProvider provider;
        private final InterrupterImpl interrupter;
        private final DataObject dao;
        private final BaseDocument doc;

        public ResponseImpl(CsmErrorProvider provider, InterrupterImpl interrupter, DataObject dao, BaseDocument doc) {
            this.provider = provider;
            this.interrupter = interrupter;
            this.dao = dao;
            this.doc = doc;
        }

        @Override
        public void addError(CsmErrorInfo info) {
            if (interrupter.cancelled()) {
                return;
            }
            List<Fix> fixes = CsmErrorInfoHintProvider.getFixes(info);
            ErrorDescription desc = ErrorDescriptionFactory.createErrorDescription(
                    null, getSeverity(info), info.getCustomType(), info.getMessage(), null, fixes, doc,
                    info.getStartOffsets(), info.getEndOffsets());
            descriptions.add(desc);
            if (TRACE_ANNOTATIONS) {
                System.err.printf("\tadded to a bag %s\n", desc.toString());
            }
        }

        @Override
        public void done() {
            if (TRACE_ANNOTATIONS) {
                System.err.printf("Showing %d errors\n", descriptions.size());
            }
            HintsController.setErrors(doc, provider.getName(), descriptions);
        }
    }

    private static final class RunnableImpl implements Runnable {

        private final CsmErrorProvider provider;
        private RequestImpl request;
        private ResponseImpl response;
        private CountDownLatch wait;

        public RunnableImpl(CsmErrorProvider provider) {
            this.provider = provider;
        }

        public void setWork(RequestImpl request, ResponseImpl response, CountDownLatch wait) {
            synchronized(this) {
                if (this.wait != null) {
                    this.wait.countDown();
                }
                this.request = request;
                this.response = response;
                this.wait = wait;
            }
        }

        @Override
        public void run() {
            RequestImpl aRequest;
            ResponseImpl aResponse;
            CountDownLatch aWait;
            synchronized(this) {
                aRequest = request;
                aResponse = response;
                aWait = wait;
                this.request = null;
                this.response = null;
                this.wait = null;
            }
            if (aWait == null) {
                // if sequence is setWork-setWork-run-run,
                // the second run has already cleaned wait.
                // In this case first wait is count downed in second setWork method,
                // second wait count downed in first run method,
                // there is no needs to do second run.
                return;
            }
            try {
                if (!aRequest.isCancelled()){
                    try {
                        provider.getErrors(aRequest, aResponse);
                        if (TRACE_TASKS) {System.err.println("finish "+provider);} //NOI18N
                    } catch (Throwable ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            } finally {
                aWait.countDown();
            }
        }
    }
    
    private static final class MyTask {
        private final RunnableImpl runnable;
        private final Task task;
        private final RequestProcessor RP;
        
        private MyTask(CsmErrorProvider provider) {
            this.RP = new RequestProcessor("Error Provider "+provider.getName(), 1); // NOI18N
            runnable = new RunnableImpl(provider);
            task = RP.create(runnable);
        }
        
        private void post(RequestImpl request, ResponseImpl response, CountDownLatch wait) {
            runnable.setWork(request, response, wait);
            task.schedule(0);
        }
    }
    
}
