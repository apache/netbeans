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
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
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

/**
 *
 */
public class NavigatorNodeFactoryTask extends IndexingAwareParserResultTask<Parser.Result> {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private final CancelSupport cancel = CancelSupport.create(this);
    private AtomicBoolean canceled = new AtomicBoolean(false);
    
    public NavigatorNodeFactoryTask() {
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
        boolean navigatorEnabled = NavigatorComponent.getInstance().isNavigatorEnabled();
        Source source = result.getSnapshot().getSource();
        if (!navigatorEnabled) {
            // check if need any activity at all
            Document doc = source.getDocument(false);
            if (doc != null && !BreadcrumbsController.areBreadCrumsEnabled(doc)) {
                // no navigator and no document or breadcrumbs is disabled
                return;
            }
        }
        FileObject fo = source.getFileObject();
        if (fo == null) {
            return;
        }
        DataObject cdo = null;
        try {
            cdo = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
        }
        if (cdo == null) {
            return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "NavigatorNodeFactoryTask started"); //NOI18N
            time = System.currentTimeMillis();
        }
        try {
            final NavigatorPanelUI panelUI;
            final NavigatorContent content;
            if (!navigatorEnabled) {
                panelUI = null;
                content = NavigatorComponent.getContent();
            } else {
                panelUI = NavigatorComponent.getInstance().getPanelUI();
                content = panelUI.getContent();
            }
            String mimeType = result.getSnapshot().getMimePath().getPath();
            CsmFile csmFile = CsmFileInfoQuery.getDefault().getCsmFile(result);
            if (csmFile != null) {
                NavigatorModel oldModel = content.getModel();
                if (oldModel != null) {
                    DataObject oldCdo = oldModel.getDataObject();
                    CsmFile oldCsmFile = oldModel.getCsmFile();
                    if (oldCsmFile != null && oldCsmFile.isValid()) {
                        if (cdo.equals(oldCdo) && csmFile.equals(oldCsmFile) && csmFile.isValid()) {
                            oldModel.update(canceled, false);
                            return;
                        }
                    }
                }
            }
            final NavigatorModel model = new NavigatorModel(cdo, fo, panelUI, mimeType, csmFile);
            content.setModel(model);
            model.update(canceled, true);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "NavigatorNodeFactoryTask finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
            }
        }
    }

    static final int PRIORITY = 90;

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final synchronized void cancel() {
        synchronized(this) {
            canceled.set(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "NavigatorNodeFactoryTask cancelled"); //NOI18N
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
            return Collections.singletonList(new NavigatorNodeFactoryTask());
        }
    }
}
