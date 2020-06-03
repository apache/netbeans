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
package org.netbeans.modules.cnd.navigation.docview;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmDocProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class DocViewCaretAwareFactory extends IndexingAwareParserResultTask<Parser.Result> {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("DocViewCaretAwareFactory runner", 1); //NOI18N"
    private static final int TASK_DELAY = getInt("cnd.docview.delay", 500); // NOI18N
    private final CancelSupport cancel = CancelSupport.create(this);
    private AtomicBoolean canceled = new AtomicBoolean(false);

    public DocViewCaretAwareFactory(String mimeType) {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);

    }
    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        synchronized (this) {
            canceled.set(true);
            canceled = new AtomicBoolean(false);
        }
        if (cancel.isCancelled()) {
            return;
        }
        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }
        if (!isDocViewActive()) {
            return;
        }
        Document doc = result.getSnapshot().getSource().getDocument(false);
        if (!(doc instanceof StyledDocument)) {
            return;
        }
        CsmFile csmFile = CsmFileInfoQuery.getDefault().getCsmFile(result);
        if (csmFile == null) {
            csmFile = (CsmFile) doc.getProperty(CsmFile.class);
        }
        if (csmFile == null) {
            return;
        }
        if (canceled.get()) {
          return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "DocViewCaretAwareFactory started"); //NOI18N
            time = System.currentTimeMillis();
        }
        RP.post(new RunnerImpl(csmFile, (StyledDocument)doc, (CursorMovedSchedulerEvent)event, canceled, time+TASK_DELAY), TASK_DELAY);
    }

    @Override
    public void cancel() {
        synchronized(this) {
            canceled.set(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "DocViewCaretAwareFactory canceled"); //NOI18N
        }
    }

    @Override
    public int getPriority() {return 1000;}

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    private static int getInt(String name, int result){
        String text = System.getProperty(name);
        if( text != null ) {
            try {
                result = Integer.parseInt(text);
            } catch(NumberFormatException e){
                // default value
            }
        }
        return result;
    }

    private boolean isDocViewActive() {
        DocViewTopComponent instance = DocViewTopComponent.getInstance();
        return instance != null && instance.isActivated();
    }

    private static final class RunnerImpl implements Runnable {

        private final CsmFile file;
        private final StyledDocument doc;
        private final AtomicBoolean canceled;
        private final CursorMovedSchedulerEvent event;
        private final long time;

        private RunnerImpl(CsmFile file, StyledDocument doc, CursorMovedSchedulerEvent event, AtomicBoolean canceled, long time){
            this.file = file;
            this.doc = doc;
            this.event = event;
            this.canceled = canceled;
            this.time = time;
        }

        @Override
        public void run() {
            try {
                CsmCacheManager.enter();
                CsmReference ref = CsmReferenceResolver.getDefault().findReference(doc, event.getCaretOffset());
                if (ref == null) {
                    return;
                }
                if (canceled.get()) {
                    return;
                }
                CsmObject csmObject = ref.getReferencedObject();
                if (csmObject == null) {
                    return;
                }
                if (canceled.get()) {
                    return;
                }
                CsmDocProvider p = Lookup.getDefault().lookup(CsmDocProvider.class);
                if (p == null) {
                    return;
                }
                CharSequence documentation = p.getDocumentation(csmObject, file);
                if (documentation == null) {
                    return;
                }
                if (canceled.get()) {
                    return;
                }
                CharSequence selfDoc = CsmDisplayUtilities.getTooltipText(csmObject);
                if (selfDoc != null) {
                    documentation = selfDoc.toString() + documentation.toString();
                }
                if (canceled.get()) {
                    return;
                }
                final CharSequence toShow = documentation;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DocViewTopComponent topComponent = DocViewTopComponent.findInstance();
                        if (topComponent != null && topComponent.isOpened()) {
                            topComponent.setDoc(toShow);
                        }
                    }
                });
            } finally {
                CsmCacheManager.leave();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "DocViewCaretAwareFactory finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
                }
            }
        }

        @Override
        public String toString() {
            if (file == null) {
                return "DocViewCaretAwareFactory runner"; //NOI18N
            } else {
                return "DocViewCaretAwareFactory runner for "+file.getAbsolutePath(); //NOI18N
            }
        }
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class),
    })
    public static final class DocViewCaretAwareFactoryImpl extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new DocViewCaretAwareFactory(snapshot.getMimeType()));
        }
    }
}
