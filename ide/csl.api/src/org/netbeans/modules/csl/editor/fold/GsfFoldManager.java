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
package org.netbeans.modules.csl.editor.fold;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.*;
import org.openide.util.NbBundle;

import static org.netbeans.modules.csl.editor.fold.Bundle.*;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * 
 * Copied from both JavaFoldManager and JavaElementFoldManager.
 * <p/>
 * 
 * From introduction of {@link FoldType}, this Manager accepts these Strings
 * from {@link StructureScanner#folds}:<ul>
 * <li>codes of FoldTypes registered for the language, or
 * <li>legacy FoldType codes.
 * </ul>
 * The registered FoldTypes take precedence; that is if the SPI returns
 * "code-block" and a FoldType is registered with that code, that FoldType (and its FoldTemplate)
 * will be used to create the Fold. If no such FoldType exists, the GsfFoldManager will still
 * create the fold in a bw compatible mode with an <b>unregistered</b> fold type. But because
 * of implementation peculiarities, the FoldType will eventually default to one of the generic
 * settings.
 * <p/>
 * If a code other than registered FoldTypes and bw compatible constants is used, 
 * a warning will be printed - to detect programmer errors.
 * 
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public class GsfFoldManager implements FoldManager {

    static final Logger LOG = Logger.getLogger(GsfFoldManager.class.getName());
    
    private static final FoldTemplate TEMPLATE_CODEBLOCK = new org.netbeans.api.editor.fold.FoldTemplate(1, 1, "{...}"); // NOI18N
   
    /**
     * This definition and all Fold types defined in CSL are deprecated and for backward
     * compatibility only. 
     */
    @Deprecated
    public static final FoldType CODE_BLOCK_FOLD_TYPE = FoldType.CODE_BLOCK;
    
    @Deprecated
    public static final FoldType INITIAL_COMMENT_FOLD_TYPE = FoldType.INITIAL_COMMENT;
    
    /**
     * Note: this FoldType's code was changed from 'imports' to 'import' to match the used preference key.
     */
    @NbBundle.Messages("FT_label_imports=Imports or Includes")
    @Deprecated
    public static final FoldType IMPORTS_FOLD_TYPE = FoldType.IMPORT;

    @Deprecated
    public static final FoldType JAVADOC_FOLD_TYPE = FoldType.DOCUMENTATION;
    
    @Deprecated
    public static final FoldType TAG_FOLD_TYPE = FoldType.TAG;
    
    /**
     * This type's code was renamed from inner-class to innerclass to match the preference value
     */
    @NbBundle.Messages("FT_label_innerclass=Inner Classes")
    @Deprecated
    public static final FoldType INNER_CLASS_FOLD_TYPE = FoldType.create("innerclass", FT_label_innerclass(), TEMPLATE_CODEBLOCK);
    
    @NbBundle.Messages("FT_label_othercodeblocks=Other code blocks")
    @Deprecated
    public static final FoldType OTHER_CODEBLOCKS_FOLD_TYPE = FoldType.TAG.derive("othercodeblocks", FT_label_othercodeblocks(), 
            TEMPLATE_CODEBLOCK);
    
    private static final Set<String> LEGACY_FOLD_TAGS = new HashSet<String>(11);
    
    static {
        LEGACY_FOLD_TAGS.add(OTHER_CODEBLOCKS_FOLD_TYPE.code());
        LEGACY_FOLD_TAGS.add(INNER_CLASS_FOLD_TYPE.code());
        LEGACY_FOLD_TAGS.add(TAG_FOLD_TYPE.code());
        LEGACY_FOLD_TAGS.add(JAVADOC_FOLD_TYPE.code());
        LEGACY_FOLD_TAGS.add(IMPORTS_FOLD_TYPE.code());
        LEGACY_FOLD_TAGS.add(INITIAL_COMMENT_FOLD_TYPE.code());
        LEGACY_FOLD_TAGS.add(CODE_BLOCK_FOLD_TYPE.code());
    }
    
    private FoldOperation operation;
    private FileObject    file;
    private volatile JavaElementFoldTask task;
    
    private volatile Preferences prefs;
    
    /** Creates a new instance of GsfFoldManager */
    public GsfFoldManager() {
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Created FM: {0}\n\t\t, doc: {1}\n\t\t, comp: {2}", new Object[] {
               this, operation.getHierarchy().getComponent().getDocument(),
               Integer.toHexString(System.identityHashCode(operation.getHierarchy().getComponent()))
            });
        }
        String mimeType = DocumentUtilities.getMimeType(operation.getHierarchy().getComponent());
        if (prefs == null) {
            prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        }
    }

    @Override
    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        file = DataLoadersBridge.getDefault().getFileObject(doc);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Initializing, document {0}\n\t\t, file {1}\n\t\t, component {2}\n\t\t, FM {3}", new Object[] { 
                doc, file, Integer.toHexString(System.identityHashCode(operation.getHierarchy().getComponent())), this });
        }
        
        if (file != null) {
            task = JavaElementFoldTask.getTask(file);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "registering FM to task: {0}, {1}", new Object[] { this, task });
            }
            task.setGsfFoldManager(GsfFoldManager.this, file);
        }
    }
    
    @Override
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    @Override
    public void removeEmptyNotify(Fold emptyFold) {
        removeDamagedNotify(emptyFold);
    }

    @Override
    public void removeDamagedNotify(Fold damagedFold) {
        if (importsFold == damagedFold) {
            importsFold = null;//not sure if this is correct...
        }
        if (initialCommentFold == damagedFold) {
            initialCommentFold = null;//not sure if this is correct...
        }
    }

    public void expandNotify(Fold expandedFold) {
    }

    public synchronized void release() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Releasing FM {0}, task {1}", new Object[] { this, task });
        }
        if (task != null) {
            task.setGsfFoldManager(this, null);
        }
        
        task         = null;
        file         = null;
        importsFold  = null;
        initialCommentFold = null;
    }
    
    static final class JavaElementFoldTask extends IndexingAwareParserResultTask<ParserResult> {
        
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        
        private FoldInfo initComment;
        
        private FoldInfo imports;
        
        public JavaElementFoldTask() {
            super(TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        //XXX: this will hold JavaElementFoldTask as long as the FileObject exists:
        private static final Map<FileObject, JavaElementFoldTask> file2Task = new WeakHashMap<FileObject, JavaElementFoldTask>();
        
        static JavaElementFoldTask getTask(FileObject file) {
            synchronized (file2Task) {
                JavaElementFoldTask task = file2Task.get(file);

                if (task == null) {
                    file2Task.put(file, task = new JavaElementFoldTask());
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Task for file {0} -> {1}", new Object[] { file, task });
                }
                return task;
            }
        }
        
        private Collection<Reference<GsfFoldManager>> managers = new ArrayList<Reference<GsfFoldManager>>(2);
        
        synchronized void setGsfFoldManager(GsfFoldManager manager, FileObject file) {
            if (file == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Got null file, unregistering {0}, task {1}", new Object[] { manager, this });
                }
                for (Iterator<Reference<GsfFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                    Reference<GsfFoldManager> ref = it.next();
                    GsfFoldManager fm = ref.get();
                    if (fm == null || fm == manager) {
                        it.remove();
                        break;
                    }
                }
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Registering manager {0} for file {1}, task {2} ", new Object[] { manager, file, this} );
                }
                managers.add(new WeakReference<GsfFoldManager>(manager));
                GsfFoldScheduler.reschedule();
            }
        }
        
        private synchronized Object findLiveManagers() {
            GsfFoldManager oneMgr = null;
            List<GsfFoldManager> result = null;
            
            for (Iterator<Reference<GsfFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                Reference<GsfFoldManager> ref = it.next();
                GsfFoldManager fm = ref.get();
                if (fm == null) {
                    it.remove();
                    continue;
                }
                if (result != null) {
                    result.add(fm);
                } else if (oneMgr != null) {
                    result = new ArrayList<GsfFoldManager>(2);
                    result.add(oneMgr);
                    result.add(fm);
                } else {
                    oneMgr = fm;
                }
            }
            return result != null ? result : oneMgr;
        }
        
        
        public void run(final ParserResult info, SchedulerEvent event) {
            cancelled.set(false);
            final CancelSupportImplementation cs = SchedulerTaskCancelSupportImpl.create(this);
            SpiSupportAccessor.getInstance().setCancelSupport(cs);
            try {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "GSF fold task {0} called for: {1}", new Object[] { this, info.getSnapshot().getSource()});
                }
                final Object mgrs = findLiveManagers();

                if (mgrs == null) {
                    LOG.log(Level.FINE, "No live FoldManagers found for {0}", this);
                    return;
                }

                long startTime = System.currentTimeMillis();

                // Don't update folds, if there is an invalid result
                // It should be solved per lenguages, but then there has to be remembered
                // lates folds and transformed to the new possition.
                if (hasErrors(info)) {
                    LOG.log(Level.FINE, "File has errors, not updating: {0}", this);
                    return;
                }

                final Collection<FoldInfo> folds = new HashSet<FoldInfo>();
                final Document doc = info.getSnapshot().getSource().getDocument(false);
                if (doc == null) {
                    LOG.log(Level.FINE, "Could not open document: {0}", this);
                    return;
                }
                boolean success = gsfFoldScan(doc, info, folds);
                if (!success || cancelled.get()) {
                    LOG.log(Level.FINER, "Fold scan cancelled or unsuccessful: {0}, {1}", new Object[] {
                        success, cancelled.get()
                    });
                    return;
                }

                // pending: refactor!!
                if (mgrs instanceof GsfFoldManager) {
                    ((GsfFoldManager)mgrs).new CommitFolds(folds,
                            initComment, imports, doc, info.getSnapshot().getSource(), cancelled).run();
                } else {
                    Collection<GsfFoldManager> jefms = (Collection<GsfFoldManager>)mgrs;
                    for (GsfFoldManager jefm : jefms) {
                        jefm.new CommitFolds(folds,
                            initComment, imports, doc, info.getSnapshot().getSource(), cancelled).run();
                    }
                }

                long endTime = System.currentTimeMillis();

                Logger.getLogger("TIMER").log(Level.FINE, "Folds - 1", //NOI18N
                        new Object[] {info.getSnapshot().getSource().getFileObject(), endTime - startTime});
            } finally {
                SpiSupportAccessor.getInstance().removeCancelSupport(cs);
            }
        }
        
        /**
         * Ask the language plugin to scan for folds. 
         * 
         * @return true If folds were found, false if cancelled
         */
        private boolean gsfFoldScan(final Document doc, ParserResult info, final Collection<FoldInfo> folds) {
            final boolean [] success = new boolean [] { false };
            Source source = info.getSnapshot().getSource();

            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    public @Override void run(ResultIterator resultIterator) throws Exception {
                        String mimeType = resultIterator.getSnapshot().getMimeType();
                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
                        if (language == null) {
                            return;
                        }

                        StructureScanner scanner = language.getStructure();
                        if (scanner == null) {
                            return;
                        }

                        Parser.Result r = resultIterator.getParserResult();
                        if (!(r instanceof ParserResult)) {
                            return;
                        }

                        scan((ParserResult) r, folds, doc, scanner);

                        if (cancelled.get()) {
                            return;
                        }

                        for(Embedding e : resultIterator.getEmbeddings()) {
                            run(resultIterator.getResultIterator(e));

                            if (cancelled.get()) {
                                return;
                            }
                        }

                        success[0] = true;
                    }
                });
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }

            if (success[0]) {
                //check for initial fold:
                success[0] = checkInitialFold(doc, folds);
            }

            return success[0];
        }

        private boolean checkInitialFold(final Document doc, final Collection<FoldInfo> folds) {
            final boolean[] ret = new boolean[1];
            ret[0] = true;
            final TokenHierarchy<?> th = TokenHierarchy.get(doc);
            if (th == null) {
                return false;
            }
            doc.render(new Runnable() {
                @Override
                public void run() {
                    TokenSequence<?> ts = th.tokenSequence();
                    if (ts == null) {
                        return;
                    }
                    while (ts.moveNext()) {
                        Token<?> token = ts.token();
                        String category = token.id().primaryCategory();
                        if ("comment".equals(category)) { // NOI18N
                            int startOffset = ts.offset();
                            int endOffset = startOffset + token.length();

                            // Find end - could be a block of single-line statements
                            while (ts.moveNext()) {
                                token = ts.token();
                                category = token.id().primaryCategory();
                                if ("comment".equals(category)) { // NOI18N
                                    endOffset = ts.offset() + token.length();
                                } else if (!"whitespace".equals(category)) { // NOI18N
                                    break;
                                }
                            }

                            try {
                                // Start the fold at the END of the line
                                startOffset = LineDocumentUtils.getLineEndOffset((BaseDocument) doc, startOffset);
                                if (startOffset >= endOffset) {
                                    return;
                                }
                            } catch (BadLocationException ex) {
                                LOG.log(Level.WARNING, null, ex);
                            }

                            folds.add(initComment = FoldInfo.range(startOffset, endOffset, INITIAL_COMMENT_FOLD_TYPE));
                            return;
                        }
                        if (!"whitespace".equals(category)) { // NOI18N
                            break;
                        }
                    }
                }
            });
            return ret[0];
        }
        
        private void scan(final ParserResult info,
            final Collection<FoldInfo> folds, final Document doc, final
            StructureScanner scanner) {
            
            // #217322, disabled folding -> no folds will be created
            String mime = info.getSnapshot().getMimeType();
            
            if (!FoldUtilities.isFoldingEnabled(mime)) {
                LOG.log(Level.FINER, "Folding is not enabled for MIME: {0}", mime);
                return;
            }
            final Map<String,List<OffsetRange>> collectedFolds = scanner.folds(info);
            final Collection<? extends FoldType> ftypes = FoldUtilities.getFoldTypes(mime).values();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    addTree(folds, info, collectedFolds, ftypes);
                }
            });
        }
        
        private boolean addFoldsOfType(
                    String type, Map<String,List<OffsetRange>> folds,
                    Collection<FoldInfo> result,
                    FoldType foldType) {
            
            List<OffsetRange> ranges = folds.get(type); //NOI18N
            if (ranges != null) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Creating folds {0}", new Object[] {
                        type
                    });
                }
                for (OffsetRange range : ranges) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(Level.FINEST, "Fold: {0}", range);
                    }
                    addFold(range, result, foldType);
                }
                folds.remove(type);
                return true;
            } else {
                LOG.log(Level.FINEST, "No folds of type {0}", type);
                return false;
            }
        }
        
        private void addTree(Collection<FoldInfo> result, ParserResult info, 
                Map<String,List<OffsetRange>> folds, Collection<? extends FoldType> ftypes) {
            if (cancelled.get()) {
                return;
            }
            // make a copy, since addFoldsOfType will remove pieces:
            folds = new HashMap<String, List<OffsetRange>>(folds);
            for (FoldType ft : ftypes) {
                addFoldsOfType(ft.code(), folds, result,  ft);
            }
            // fallback: transform the legacy keys into FoldInfos
            addFoldsOfType("codeblocks", folds, result, 
                    CODE_BLOCK_FOLD_TYPE);
            addFoldsOfType("comments", folds, result, 
                    JAVADOC_FOLD_TYPE);
            addFoldsOfType("initial-comment", folds, result, 
                    INITIAL_COMMENT_FOLD_TYPE);
            addFoldsOfType("imports", folds, result, 
                    IMPORTS_FOLD_TYPE);
            addFoldsOfType("tags", folds, result, 
                    TAG_FOLD_TYPE);
            addFoldsOfType("othercodeblocks", folds, result, 
                    CODE_BLOCK_FOLD_TYPE);
            addFoldsOfType("inner-classes", folds, result, 
                    INNER_CLASS_FOLD_TYPE);
            
            if (folds.size() > 0) {
                LOG.log(Level.WARNING, "Undefined fold types used in {0}: {1}", new Object[] {
                    info, folds.keySet()
                });
            }
        }
        
        private void addFold(OffsetRange range, Collection<FoldInfo> folds, FoldType type) {
            if (range != OffsetRange.NONE) {
                int start = range.getStart();
                int end = range.getEnd();
                // the readlock will be interrupted before FoldInfos are committed to fold hierarchy,
                // so we don't need to check against doc length here. FoldOp.update checks+ignores under read lock.
                if (start != (-1) && end != (-1)) {
                    FoldInfo fi = FoldInfo.range(start, end, type);
                    // hack for singular imports fold
                    if (fi.getType() == IMPORTS_FOLD_TYPE && imports == null) {
                        imports = fi;
                    }
                    folds.add(fi);
                }
            }
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return GsfFoldScheduler.class;
        }

        @Override
        public void cancel() {
            cancelled.set(true);
        }

    }
    
    private class CommitFolds implements Runnable {
        private final Document scannedDocument;
        private Source  scanSource;
        
        private boolean insideRender;
        private Collection<FoldInfo> infos;
        private long startTime;
        private FoldInfo    initComment;
        private FoldInfo    imports;
        private final AtomicBoolean cancel;
        
        public CommitFolds(Collection<FoldInfo> infos, FoldInfo initComment, FoldInfo imports, Document scannedDocument, Source s,
                AtomicBoolean cancel) {
            this.infos = infos;
            this.initComment = initComment;
            this.imports = imports;
            this.scannedDocument = scannedDocument;
            this.scanSource = s;
            this.cancel = cancel;
        }
        
        /**
         * For singular folds, if they exist in the FoldManager already
         * ignores the default state, and takes it from the actual state of
         * existing fold.
         */
        private void mergeSpecialFoldState(FoldInfo fi, Fold f) {
            if (fi != null && f != null) {
                fi.collapsed(f.isCollapsed());
            }
        }

        public void run() {
            final Document document = operation.getHierarchy().getComponent().getDocument();
            if (!insideRender) {
                startTime = System.currentTimeMillis();
                insideRender = true;
                document.render(this);
                
                return;
            }
            if (cancel.get() || task == null) {
                return;
            }
            operation.getHierarchy().lock();
            try {
                Document newDoc = operation.getHierarchy().getComponent().getDocument();
                if (newDoc != this.scannedDocument) {
                    // prevent folding, bad offsets, see issue #223800
                    return;
                }
                if (newDoc != document) {
                    LOG.log(Level.WARNING, "Locked different document than the component: currentDoc: {0}, lockedDoc: {1}", new Object[] {
                        newDoc, document
                    });
                }
                try {
                    mergeSpecialFoldState(imports, importsFold);
                    mergeSpecialFoldState(initComment, initialCommentFold);
                    
                    Map<FoldInfo, Fold> newState = operation.update(infos, null, null);
                    if (newState == null) {
                        // manager has been released, no further folds should be created.
                        return;
                    }
                    if (imports != null) {
                        importsFold = newState.get(imports);
                    }
                    if (initComment != null) {
                        initialCommentFold = newState.get(initComment);
                    }
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, null, e);
                }
            } finally {
                operation.getHierarchy().unlock();
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 2",
                    new Object[] {file, endTime - startTime});
        }
    }
    
    private Fold initialCommentFold;
    private Fold importsFold;

    private static boolean hasErrors(ParserResult r) {
        for(org.netbeans.modules.csl.api.Error e : r.getDiagnostics()) {
            if (e.getSeverity() == Severity.FATAL) {
                return true;
            }
        }
        return false;
    }
}
