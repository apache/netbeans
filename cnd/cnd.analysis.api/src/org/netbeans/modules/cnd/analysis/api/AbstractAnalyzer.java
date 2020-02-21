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
package org.netbeans.modules.cnd.analysis.api;

import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.cnd.analysis.api.options.HintsPanel;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 */
public abstract class AbstractAnalyzer implements Analyzer {

    private final Context ctx;
    private final AtomicBoolean cancel = new AtomicBoolean(false);
    private Thread processingThread;
    private final AtomicInteger count = new AtomicInteger(0);
    private int total;

    protected AbstractAnalyzer(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public Iterable<? extends ErrorDescription> analyze() {
        WorkSet set = new WorkSet();
        Set<String> antiLoop = new HashSet<String>();
        for (FileObject sr : ctx.getScope().getSourceRoots()) {
            doDryRun(sr, set, antiLoop);
        }
        for (NonRecursiveFolder nrf : ctx.getScope().getFolders()) {
            doDryRun(nrf, set);
        }
        for (FileObject file : ctx.getScope().getFiles()) {
            doDryRun(file, set, antiLoop);
        }
        set.processHeaders(cancel, isCompileUnitBased());
        total = set.compileUnits.size();
        ctx.start(total);
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        try {
            CsmErrorProvider errorProvider = getErrorProvider(ctx.getSettings());
            for(NativeFileItem item : set.compileUnits) {
                if (cancel.get()) {
                    break;
                }
                if (count.incrementAndGet() < total) {
                    ctx.progress(count.get());
                }
                try {
                    result.addAll(doRunImpl(item.getFileObject(), ctx, errorProvider, cancel));
                } catch (Throwable ex){
                    CndUtils.printStackTraceOnce(ex);
                }
            }
        } finally {
            result.addAll(done());
            ctx.finish();
        }
        return result;
    }

    @Override
    public boolean cancel() {
        cancel.set(true);
        synchronized (this) {
            if (processingThread != null) {
                processingThread.interrupt();
            }
        }
        return false;
    }

    private void doDryRun(NonRecursiveFolder nrf, WorkSet set) {
        FileObject sr = nrf.getFolder();
        for (FileObject fo : sr.getChildren()) {
            if (cancel.get()) {
                break;
            }
            if (fo.isData()) {
                set.add(fo);
            }
        }
    }

    private void doDryRun(final FileObject sr, WorkSet set, Set<String> antiLoop) {
        if (sr.isData()) {
            set.add(sr);
        } else {
            String canonicalPath;
            try {
                canonicalPath = FileSystemProvider.getCanonicalPath(sr);
                if (!antiLoop.contains(canonicalPath)) {
                    antiLoop.add(canonicalPath);
                    for (FileObject fo : sr.getChildren()) {
                        if (cancel.get()) {
                            break;
                        }
                        doDryRun(fo, set, antiLoop);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * This callback notifies analyzer about the end of analysis
     * and returns errors which can only be computed after analysis.
     */
    protected Collection<? extends ErrorDescription> done() {
        return Collections.<ErrorDescription>emptyList();
    }

    protected abstract boolean isCompileUnitBased();

    protected abstract CsmErrorProvider getErrorProvider(final Preferences context);
    
    protected abstract Collection<? extends ErrorDescription> doRunImpl(final FileObject sr, final Context ctx, final CsmErrorProvider provider, final AtomicBoolean cancel);

    protected static AbstractHintsPanel createComponent(CodeAuditProvider provider) {
        return new HintsPanel(null, provider, provider.getMimeType());
    }

    protected static class RequestImpl implements CsmErrorProvider.Request, AnalyzerRequest {
        private final CsmFile csmFile;
        private final AtomicBoolean cancel;
        private final Context ctx;
        public RequestImpl(CsmFile csmFile, Context ctx, AtomicBoolean cancel) {
            this.csmFile = csmFile;
            this.cancel = cancel;
            this.ctx = ctx;
        }

        @Override
        public CsmFile getFile() {
            return csmFile;
        }

        @Override
        public boolean isCancelled() {
            return cancel.get();
        }

        @Override
        public Document getDocument() {
            return null;
        }

        @Override
        public String getSingleAuditId() {
            String singleWarningId = ctx.getSingleWarningId();
            if (singleWarningId != null && singleWarningId.indexOf('-') > 0) {
                singleWarningId = singleWarningId.substring( singleWarningId.indexOf('-')+1);
            }
            return singleWarningId;
        }

        @Override
        public CsmErrorProvider.EditorEvent getEvent() {
            return CsmErrorProvider.EditorEvent.FileBased;
        }
    }

    protected abstract static class AbstractResponse implements CsmErrorProvider.Response, AnalyzerResponse {

        private final FileObject sr;
        private final ArrayList<ErrorDescription> res;
        private final AtomicBoolean cancel;

        public AbstractResponse(FileObject sr, ArrayList<ErrorDescription> res, final AtomicBoolean cancel) {
            this.sr = sr;
            this.res = res;
            this.cancel = cancel;
        }

        @Override
        public void addError(CsmErrorInfo errorInfo) {
            ErrorDescription error = addErrorImpl(errorInfo, sr);
            if (error != null) {
                res.add(error);
            }
        }

        @Override
        public void addError(AnalyzerSeverity severity, String message, FileObject file, CsmErrorInfo errorInfo) {
            switch (severity) {
                case FileError:
                    // probably file has compile errors
                    break;
                case ProjectError:
                    // project cannot start analyzer
                    // stop analyzing
                    cancel.set(true);
                    break;
                case ToolError:
                    // analyzer tool is not found
                    // stop analyzing
                    cancel.set(true);
                    break;
                case DetectedError:
                    // problem was detected
                    break;
            }
            ErrorDescription error = addErrorImpl(errorInfo, file);
            if (error != null) {
                res.add(error);
            }
        }

        @Override
        public void done() {
        }

        protected abstract ErrorDescription addErrorImpl(CsmErrorInfo errorInfo, FileObject fo);
    }

    protected static class LazyFixListImpl implements LazyFixList {

        public LazyFixListImpl() {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public boolean probablyContainsFixes() {
            return false;
        }

        @Override
        public List<Fix> getFixes() {
            return Collections.<Fix>emptyList();
        }

        @Override
        public boolean isComputed() {
            return false;
        }
    }
    
    private static class WorkSet {
        LinkedHashSet<NativeFileItem> compileUnits = new LinkedHashSet<NativeFileItem>();
        LinkedHashSet<FileObject> headers = new LinkedHashSet<FileObject>();
        
        private WorkSet() {
        }
        
        void add(FileObject fo) {
            String mimeType = fo.getMIMEType();
            if (MIMENames.isCppOrC(mimeType)) {
                final Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    NativeProject np = project.getLookup().lookup(NativeProject.class);
                    if (np != null) {
                        NativeFileItem item = np.findFileItem(fo);
                        if (item != null && !item.isExcluded()) {
                            compileUnits.add(item);
                        }
                    }
                }
            } else if (MIMENames.isHeader(mimeType)) {
                headers.add(fo);
            }
        }
        
        private void processHeaders(AtomicBoolean cancel, boolean isCompileUnitBased){
            for(FileObject fo : headers) {
                if (cancel.get()) {
                    break;
                }
                CsmFile csmFile = CsmUtilities.getCsmFile(fo, false, false);
                if (csmFile != null) {
                    if (isCompileUnitBased) {
                        for(CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(csmFile, 0)) {
                            CsmFile startFile = cu.getStartFile();
                            if (startFile != null) {
                                NativeFileItem findItem = findItem(startFile);
                                if (findItem != null) {
                                    if (!compileUnits.contains(findItem)) {
                                        compileUnits.add(findItem);
                                    }
                                    break;
                                }
                            }

                        }
                    } else {
                        NativeFileItem findItem = findItem(csmFile);
                        if (findItem != null) {
                            if (!compileUnits.contains(findItem)) {
                                compileUnits.add(findItem);
                            }
                        }
                    }
                }
            }
            headers.clear();
        }
        
        private NativeFileItem findItem(CsmFile file) {
            Object platformProject = file.getProject().getPlatformProject();
            if (platformProject instanceof NativeProject) {
                return ((NativeProject)platformProject).findFileItem(file.getFileObject());
            }
            return null;
        }
    }
}
