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
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
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

/**
 *
 */
public class NavigatorSourceFactoryTask extends IndexingAwareParserResultTask<Parser.Result> {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private final CancelSupport cancel = CancelSupport.create(this);
    private AtomicBoolean canceled = new AtomicBoolean(false);
    
    public NavigatorSourceFactoryTask() {
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
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        if (doc != null && doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
            return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "NavigatorSourceFactoryTask started"); //NOI18N
            time = System.currentTimeMillis();
        }
        final NavigatorContent content = NavigatorComponent.getContent();
        NavigatorModel model = content.getModel();
        if (model != null) {
            CursorMovedSchedulerEvent cursorEvent = (CursorMovedSchedulerEvent) event;
            JTextComponent comp = EditorRegistry.lastFocusedComponent();
            if (comp instanceof JEditorPane) {
                model.setSelection(cursorEvent.getCaretOffset(), (JEditorPane) comp, canceled, result.getSnapshot().getText());
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "NavigatorSourceFactoryTask finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
        }
    }

    @Override
    public int getPriority() {
        // this tasks depends on model created by NavigatorNodeFactoryTask
        // make sure we are called after it
        return NavigatorNodeFactoryTask.PRIORITY + 10;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final void cancel() {
        synchronized(this) {
            canceled.set(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "NavigatorSourceFactoryTask canceled"); //NOI18N
        }
    }
    
    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.FORTRAN_MIME_TYPE, service = TaskFactory.class)
    })
    public static class NavigatorSourceFactory extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new NavigatorSourceFactoryTask());
        }
    }
}
