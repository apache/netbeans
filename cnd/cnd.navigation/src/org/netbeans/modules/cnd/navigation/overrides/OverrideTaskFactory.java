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

package org.netbeans.modules.cnd.navigation.overrides;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedOption;
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
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class OverrideTaskFactory extends IndexingAwareParserResultTask<Parser.Result> {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("OverrideTaskFactory runner", 1); //NOI18N"
    private static final int TASK_DELAY = getInt("cnd.overrides.delay", 500); // NOI18N
    private final CancelSupport cancel = CancelSupport.create(this);
    private AtomicBoolean canceled = new AtomicBoolean(false);
    private Parser.Result lastParserResult;

    public OverrideTaskFactory(String mimeType) {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        synchronized (this) {
            if (lastParserResult == result) {
                return;
            }
            canceled.set(true);
            canceled = new AtomicBoolean(false);
            if (cancel.isCancelled()) {
                lastParserResult = null;
                return;
            } else {
                lastParserResult = result;
            }
        }
        FileObject fo = result.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return;
        }
        Document doc = result.getSnapshot().getSource().getDocument(false);
        if (!(doc instanceof StyledDocument)) {
            return;
        }
        if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null) {
            return;
        }
        CsmFile csmFile = CsmFileInfoQuery.getDefault().getCsmFile(result);
        if (csmFile == null) {
            return;
        }
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace(System.err);
        }
        if (dobj == null) {
            return;
        }
        if (canceled.get()) {
            return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "OverrideTaskFactory started"); //NOI18N
            time = System.currentTimeMillis();
        }
        RP.post(new RunnerImpl(dobj, csmFile, (StyledDocument)doc, canceled, time+TASK_DELAY), TASK_DELAY);
    }

    @Override
    public int getPriority() {return 2000;}

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final void cancel() {
        synchronized(this) {
            canceled.set(true);
            lastParserResult = null;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "OverrideTaskFactory cancelled"); //NOI18N
        }
    }

    private static boolean isEnabled() {
        return NamedOption.getAccessor().getBoolean(OverrideOptions.NAME);
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

    private static final class RunnerImpl implements Runnable {

        private final DataObject dobj;
        private final CsmFile file;
        private final Reference<StyledDocument> weakDoc;
        private final AtomicBoolean canceled;
        private final long time;

        private RunnerImpl(DataObject dobj, CsmFile file, StyledDocument doc, AtomicBoolean canceled, long time){
            this.dobj = dobj;
            this.file = file;
            weakDoc = new WeakReference<StyledDocument>(doc);
            this.canceled = canceled;
            this.time = time;
        }

        @Override
        public void run() {
            try {
            if (!isEnabled()) {
                AnnotationsHolder.clearIfNeed(dobj);
                return;
            }
            if (canceled.get()) {
                return;
            }
            StyledDocument doc = weakDoc.get();
            if (doc != null) {
                addAnnotations(file, doc, dobj, canceled);
            }
            } finally {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "OverrideTaskFactory finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
                }
            }
        }

        private void addAnnotations(CsmFile file, StyledDocument doc, DataObject dobj, AtomicBoolean canceled) {
            final Collection<BaseAnnotation> toAdd = new ArrayList<BaseAnnotation>();
            BaseAnnotation.LOGGER.log(Level.FINE, ">> Computing annotations for {0}", file);
            long time = System.currentTimeMillis();
            ComputeAnnotations.getInstance(file, doc, canceled).computeAnnotations(toAdd);
            time = System.currentTimeMillis() - time;
            BaseAnnotation.LOGGER.log(Level.FINE, "<< Computed annotations for {0} in {1} ms", new Object[] { file, time });
            if (canceled.get()) {
                return;
            }
            AnnotationsHolder.get(dobj).setNewAnnotations(toAdd);
        }

        @Override
        public String toString() {
            if (file == null) {
                return "OverrideTaskFactory runner"; //NOI18N
            } else {
                return "OverrideTaskFactory runner for "+file.getAbsolutePath(); //NOI18N
            }
        }
    }

    @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=2000)
    public static final class OverrideOptions extends NamedOption {
        private static final String NAME = "overrides-annotations"; //NOI18N

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public OptionKind getKind() {
            return OptionKind.Boolean;
        }

        @Override
        public Object getDefaultValue() {
            return true;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(OverrideTaskFactory.class, "Show-overrides-annotations");
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(OverrideTaskFactory.class, "Show-overrides-annotations-AD");
        }
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class)
    })
    public static final class OverrideTaskFactoryImpl extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new OverrideTaskFactory(snapshot.getMimeType()));
        }
    }
}
