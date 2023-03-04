/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.editor.fold;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Helps to automate folding based on parser result. Derives from {@link ParserResultTask}, register the implementation
 * using {@link MimeRegistration} for the appropriate MIME type. Because of instantiation protocol limitation, you MUST
 * repeat the MIME type in the constructor - you MUST define a no-arg constructor, calling {@code} super() with the
 * appropriate MIME type.
 * 
 * @author sdedic
 */
public abstract class ParsingFoldSupport extends TaskFactory implements FoldManagerFactory {
    private static final RequestProcessor RP = new RequestProcessor(ParsingFoldSupport.class);
    
    /**
     * Initializes parsing fold support.
     * @param mimeType the handled MIME type.
     */
    protected ParsingFoldSupport() {
    }
    
    protected abstract FoldProcessor createTask(FileObject f);
    
    protected abstract static class FoldProcessor  {
        private final FileObject        file;
        private FileData                fileData;
        
        private Updater                 updater;
        private String                  mimeType;
        private RequestProcessor.Task   refreshTask = RP.create(new R());
        
        protected FoldProcessor(FileObject f, String mimeType) {
            this.mimeType = mimeType;
            this.file = f;
        }
        
        protected FileObject getFile() {
            return file;
        }
        
        protected Runnable runInEDT() {
            return null;
        }
        
        /**
         * Forces a refresh of the folds. The refresh may  based on an external event, e.g. a referened file,
         * whose content was used to create fold presentation has changed.
         */
        protected final void performRefresh() {
            refreshTask.schedule(300);
        }
        
        class R extends UserTask implements Runnable {
            public void run() {
                try {
                    Source source = Source.create(file);
                    if (source != null) {
                        ParserManager.parse(Collections.singleton(source), this);
                    }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                if (!mimeType.equals(r.getSnapshot().getMimeType())) {
                    return;
                }
                Document doc = r.getSnapshot().getSource().getDocument(false);
                if (doc == null) {
                    return;
                }
                Updater theUpdater = new Updater(fileData, doc);
                runWith(theUpdater, r, doc);
            }
        }
        
        protected abstract boolean processResult(Parser.Result result);

        protected int getPriority() {
            return 0;
        }

        /**
         * Defines a fold together with starting symbol position. 
         * Some folds collapse e.g. body of method or class, but the symbol header itself is not collapsed. Still if a
         * caret is positioned on the header, it may be sometimes useful to expand the entire fold, e.g. when opening a file.
         * 
         * @param info
         * @param anchor 
         */
        protected final void addFold(FoldInfo info, int anchor) {
            if (isCancelled() || updater == null) {
                throw new Stop();
            }
            updater.foldInfos.add(info);
            if (anchor == -1) {
                anchor = info.getStart();
            }
            updater.anchors.add(anchor);
        }
        
        protected final boolean isCancelled() {
            return updater == null || fileData.getStamp() != updater.initialStamp;
        }
        
        private synchronized void runWith(final Updater u, final Parser.Result r, final Document doc) {
            if (doc == null) {
                return;
            }
            assert this.updater == null;
            try {
                this.updater = u;
                if (!processResult(r)) {
                    return;
                }
                final List<FoldManager> fms = fileData.getManagers();
                final int[] carets = new int[fms.size()];
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // do not use isCancelled, updater is going to be cleared.
                        if (fileData.getStamp() != u.initialStamp) {
                            return;
                        }
                        int index = -1;
                        for (FoldManager fm : fms) {
                            index++;
                            
                            FoldOperation op = getFoldOperation(fm);
                            if (op == null) {
                                continue;
                            }
                            carets[index] = getCaretPos(op.getHierarchy());
                        }
                        
                        u.setManagersAndCarets(fms, carets);
                        
                        // todo: offload into a RP  -- document lock from AWT ?
                        doc.render(u);
                    }
                });
            } catch (Stop stop) {
                // OK, wait for the next round
            } finally {
                this.updater = null;
            }
        }
    }
    
    private static FoldOperation getFoldOperation(FoldManager m) {
        if (!(m instanceof FM)) {
            return null;
        } else {
            return ((FM)m).operation;
        }
    }
    
    private static class Updater implements Runnable {
        private Document          snapshotDoc;
        private FileData          fileData;
        private final int               initialStamp;
        private List<FoldInfo>          foldInfos = new ArrayList<>();
        private List<Integer>           anchors = new ArrayList<>();
        private Iterator<FoldManager>   mgrsToUpdate;
        private FoldOperation           oper;
        private int[]                   caretPositions;
        
        public Updater(FileData fileData, Document doc) {
            this.fileData = fileData;
            this.initialStamp = fileData.initStamp();
            this.snapshotDoc = doc;
        }
        
        protected final FoldOperation getOperation() {
            return oper;
        }
        
        synchronized void setManagersAndCarets(List<FoldManager> mgrs, int[] poss) {
            this.mgrsToUpdate = mgrs.iterator();
            this.caretPositions = poss;
        }
        
        private int findFoldIndex(int caretPos, boolean useAnchors) {
            if (caretPos == -1) {
                return -1;
            }
            int expandIndex = -1;
            if (caretPos >= 0) {
                for (int i = 0; i < anchors.size(); i++) {
                    int a = anchors.get(i);
                    if (a > caretPos) {
                        continue;
                    }
                    FoldInfo fi = foldInfos.get(i);
                    if (a == caretPos) {
                        // do not expand comments if the pos is at the start, not within
                        FoldType ft = fi.getType();
                        if (ft.isKindOf(FoldType.INITIAL_COMMENT) || ft.isKindOf(FoldType.COMMENT) ||
                            ft.isKindOf(FoldType.DOCUMENTATION)) {
                            continue;
                        }
                    }
                    if (fi.getEnd() > caretPos) {
                        expandIndex = i;
                        break;
                    }
                }
            }
            return expandIndex;
        }
        
        private synchronized void processManagers() {
            try {
                while (mgrsToUpdate.hasNext()) {
                    oper = getFoldOperation(mgrsToUpdate.next());
                    if (isCancelled()) {
                        return;
                    }
                    if (oper == null || getDocument(oper.getHierarchy()) != snapshotDoc) {
                        continue;
                    }
                    oper.getHierarchy().render(this);
                }
            } finally {
                mgrsToUpdate = null;
                snapshotDoc = null;
                fileData = null;
            }
        }

        protected final boolean isCancelled() {
            return fileData.getStamp() != initialStamp;
        }

        private FoldInfo expanded(FoldInfo info) {
            FoldInfo ex = FoldInfo.range(info.getStart(), info.getEnd(), info.getType());
            if (info.getTemplate() != info.getType().getTemplate()) {
                ex = ex.withTemplate(info.getTemplate());
            }
            if (info.getDescriptionOverride() != null) {
                ex = ex.withDescription(info.getDescriptionOverride());
            }
            ex.attach(info.getExtraInfo());
            return ex.collapsed(false);
        }

        List<FoldInfo> expandCaretFold() {
            if (!fileData.first) {
                return foldInfos;
            }
            int expandIndex = findFoldIndex(getCaretPos(oper.getHierarchy()), true);
            if (expandIndex == -1) {
                return foldInfos;
            }
            List<FoldInfo> aa = new ArrayList<>(foldInfos);
            aa.set(expandIndex, expanded(foldInfos.get(expandIndex)));
            return aa;
        }

        @Override
        public void run() {
            if (oper == null) {
                processManagers();
                return;
            } else {
                List<FoldInfo>  infos = expandCaretFold();
                try {
                    if (oper.update(infos, null, null) != null) {
                        fileData.first = false;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    protected static final class Stop extends Error {}

    @Override
    public final Collection<? extends SchedulerTask> create(Snapshot snapshot) {
        FileObject f = snapshot.getSource().getFileObject();
        FileData fdata = null;
        FoldProcessor processor = null;
        if (f != null) {
            fdata = getRegistrar(this).getFileData(f);
            processor = fdata.createProcessor(this, f);
        }
        return f == null || processor == null ? 
                Collections.<SchedulerTask>emptyList() : 
                Collections.singleton(createParserTask(f, processor)
        );
    }
    
    protected ParserResultTask createParserTask(FileObject file, FoldProcessor processor) {
        FileData fd = getRegistrar(this).getFileData(file);
        return new ParserTask(file, fd, fd.createProcessor(this, file));
    }
    
    private static class ParserTask extends IndexingAwareParserResultTask<Parser.Result> {
        private final FileData   fileData;
        private final FileObject file;
        private final FoldProcessor processor;

        public ParserTask(FileObject file, FileData fd, FoldProcessor proc) {
            super(TaskIndexingMode.ALLOWED_DURING_SCAN);
            this.file = file;
            this.processor = proc;
            this.fileData = fd;
        }
        
        @Override
        public void run(Parser.Result r, SchedulerEvent event) {
            final Document doc = r.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return;
            }
            Updater theUpdater = new Updater(fileData, doc);
            processor.runWith(theUpdater, r, doc);
        }

        @Override
        public int getPriority() {
            return processor.getPriority();
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            fileData.invalidate();
        }
    }
    
    
    @Override
    public final FoldManager createFoldManager() {
        return new FM(getRegistrar(this));
    }
    
    private static Document getDocument(FoldHierarchy h) {
        JTextComponent c = h.getComponent();
        if (c == null) {
            return null;
        }
        return c.getDocument();
    }
    
    private static FileObject getFileObject(FoldHierarchy h) {
        JTextComponent c = h.getComponent();
        if (c == null) {
            return null;
        }
        Document d = c.getDocument();
        Object o = d.getProperty(Document.StreamDescriptionProperty);
        if (o instanceof FileObject) {
            return (FileObject)o;
        } else if (o instanceof DataObject) {
            return ((DataObject)o).getPrimaryFile();
        }
        
        return null;
    }
    
    private static int getCaretPos(FoldHierarchy h) {
        int caretPos = -1;
        JTextComponent c = h.getComponent();
        if (c == null) {
            return -1;
        }
        Document doc = getDocument(h);
        Object od = doc.getProperty(Document.StreamDescriptionProperty);
        if (od instanceof DataObject) {
            DataObject d = (DataObject)od;
            EditorCookie cake = d.getCookie(EditorCookie.class);
            JEditorPane[] panes = cake.getOpenedPanes();
            int idx = panes == null ? -1 : Arrays.asList(panes).indexOf(c);
            if (idx != -1) {
                caretPos = c.getCaret().getDot();
            }
        }
        return caretPos;
    }
    
    private final class FM implements FoldManager {
        private final FileManagerRegistrar reg;
        private FoldOperation   operation;
        private FileData  fileData;
        private FoldProcessor   processor;

        public FM(FileManagerRegistrar reg) {
            this.reg = reg;
        }
        
        @Override
        public void init(FoldOperation operation) {
            this.operation = operation;
            FileObject f = getFileObject(operation.getHierarchy());
            if (f != null) {
                fileData = reg.addFoldManager(f, this);
                processor = fileData.createProcessor(ParsingFoldSupport.this, f);
            }
        }
        
        private void invalidate() {
            if (fileData != null) {
                fileData.invalidate();
            }
        }

        @Override
        public void initFolds(FoldHierarchyTransaction transaction) {
        }

        @Override
        public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
            invalidate();
        }

        @Override
        public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
            invalidate();
        }

        @Override
        public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        }

        @Override
        public void removeEmptyNotify(Fold epmtyFold) {
        }

        @Override
        public void removeDamagedNotify(Fold damagedFold) {
        }

        @Override
        public void expandNotify(Fold expandedFold) {
        }

        @Override
        public void release() {
            if (fileData != null) {
                fileData.removeManager(this);
            }
        }
    }
    
    private static final Map<String, FileManagerRegistrar>  regs = new HashMap<>();
    
    private FileManagerRegistrar getRegistrar(ParsingFoldSupport instance) {
        Class c = instance.getClass();
        synchronized (regs) {
            String s = c.getName();
            FileManagerRegistrar r = regs.get(s);
            if (r == null) {
                r = new FileManagerRegistrar();
                regs.put(s, r);
            }
            return r;
        }
    }
    
    static class FileData {
        private Collection<Reference<FoldManager>>  managers = new ArrayList<>();
        private Reference<FoldProcessor>            processor;
        private AtomicInteger   stamp = new AtomicInteger();
        boolean first = true;
        
        void invalidate() {
            stamp.incrementAndGet();
        }
        
        int getStamp() {
            return stamp.get();
        }
        
        int initStamp() {
            return stamp.incrementAndGet();
        }
        
        synchronized FoldProcessor createProcessor(ParsingFoldSupport factory, FileObject f) {
            FoldProcessor p = null;
            Reference<FoldProcessor> rp = processor;
            if (rp != null) {
                p = rp.get();
            }
            if (p == null) {
                p = factory.createTask(f);
                if (p == null) {
                    return null;
                }
                p.fileData = this;
                processor = new WeakReference(p);
            }
            return p;
        }
        
        synchronized void addManager(FoldManager m) {
            managers.add(new WeakReference(m));
        }
        
        synchronized void removeManager(FoldManager m) {
            for (Iterator<Reference<FoldManager>> it = managers.iterator(); it.hasNext(); ) {
                Reference<FoldManager> ref = it.next();
                FoldManager x = ref.get();
                if (x == null) {
                    it.remove();
                } else if (x == m) {
                    it.remove();
                    break;
                }
            }
        }
        
        synchronized List<FoldManager> getManagers() {
            List<FoldManager> live = new ArrayList<>(managers.size());
            for (Iterator<Reference<FoldManager>> it = managers.iterator(); it.hasNext(); ) {
                Reference<FoldManager> ref = it.next();
                FoldManager x = ref.get();
                if (x == null) {
                    it.remove();
                } else {
                    live.add(x);
                }
            }
            return live;
        }
    }
    
    /**
     * Maintains a mapping from a FileObject to set of active FoldManagers. The set of active managers is used
     * when parsing is over, and the folds are to be committed, since each manager (view) creates its own set
     * out of FoldInfos.
     * <p/>
     * Each of supports need to have its own registry.
     */
    private static class FileManagerRegistrar {
        private Map<FileObject, FileData> data = new WeakHashMap<>(5);
        
        FileData getFileData(FileObject f) {
            if (f == null) {
                return new FileData();
            }
            synchronized(this) {
                FileData d = data.get(f);
                if (d == null) {
                    data.put(f, d = new FileData());
                }
                return d;
            }
        }
        
        public FileData addFoldManager(FileObject f, FoldManager m) {
            if (f == null) {
                return null;
            }
            FileData fd = getFileData(f);
            fd.addManager(m);
            return fd;
        }
        
        public void removeFoldManager(FileObject f, FoldManager m) {
            if (f == null) {
                return;
            }
            getFileData(f).removeManager(m);
        }
        
        public Collection<FoldManager> getFoldManagers(FileObject f) {
            return getFileData(f).getManagers();
        }
    }
}
