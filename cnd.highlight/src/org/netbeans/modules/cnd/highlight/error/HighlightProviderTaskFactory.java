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

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
    @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
    @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class)
})
public final class HighlightProviderTaskFactory extends TaskFactory {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N

    @Override
    public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
        return Collections.singletonList(new ErrorsHighlighter());
    }

    private static final class ErrorsHighlighter extends IndexingAwareParserResultTask<Parser.Result> {
        private final CancelSupport cancel = CancelSupport.create(this);
        private InterrupterImpl interrupter = new InterrupterImpl();
        private Parser.Result lastParserResult;

        public ErrorsHighlighter() {
            super(TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        public void run(Parser.Result result, SchedulerEvent event) {
            synchronized(this) {
                if (lastParserResult == result) {
                    return;
                }
                interrupter.cancel();
                this.interrupter = new InterrupterImpl();
                if (cancel.isCancelled()) {
                    lastParserResult = null;
                    return;
                } else {
                    this.lastParserResult = result;
                }
            }
            long time = 0;
            try {
                final FileObject fo = result.getSnapshot().getSource().getFileObject();
                if (fo == null) {
                    return;
                }
                final CsmFile csmFile = CsmFileInfoQuery.getDefault().getCsmFile(result);
                if (csmFile == null) {
                    return;
                }
                final Document doc = result.getSnapshot().getSource().getDocument(false);
                if (doc == null) {
                    return;
                }
                DataObject dobj = DataObject.find(fo);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "HighlightProviderTaskFactory started"); //NOI18N
                    time = System.currentTimeMillis();
                }
                HighlightProvider.getInstance().update(csmFile, doc, dobj, interrupter);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "HighlightProviderTaskFactory finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
            }
        }

        @Override
        public void cancel() {
            synchronized(this) {
                interrupter.cancel();
                lastParserResult = null;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "HighlightProviderTaskFactory canceled"); //NOI18N
            }
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public int getPriority() {return 3000;}
    }
}
