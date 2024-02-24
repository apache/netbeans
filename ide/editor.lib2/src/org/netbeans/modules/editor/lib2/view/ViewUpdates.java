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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.swing.BlockCompare;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Update paragraph views by document and view factory changes.
 * 
 * @author Miloslav Metelka
 */

public final class ViewUpdates implements DocumentListener, EditorViewFactoryListener {
    
    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewUpdates.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewUpdates.class.getName());
    
    /**
     * Delay between view factory reports a change and the actual view(s) rebuild.
     */
    private static final int REBUILD_DELAY = 5;
    
    /**
     * Maximum number of attempts to rebuild the views after some of the view factories
     * reported stale creation.
     */
    private static final int MAX_VIEW_REBUILD_ATTEMPTS = 10;

    private static final RequestProcessor rebuildRegionRP = 
            new RequestProcessor("ViewHierarchy-Region-Rebuilding", 1, false, false); // NOI18N

    private final Object rebuildRegionsLock = new String("rebuild-region-lock"); // NOI18N
    
    private boolean rebuildRegionsScheduled;

    private final DocumentView docView;

    private EditorViewFactory[] viewFactories;

    private DocumentListener incomingModificationListener;

    /**
     * Rebuild region where character attributes changed (or null for no change).
     */
    private OffsetRegion charRebuildRegion;
    
    /**
     * Paragraph views rebuild region.
     * Paragraph views rebuilding has priority over character-level rebuilding
     * and requires pViews rebuilding (compared to char-level rebuilding
     * which only invalidates PV's children).
     */
    private OffsetRegion paragraphRebuildRegion;
    
    /**
     * Rebuild all paragraph views from scratch (without matching to original pViews).
     */
    private boolean rebuildAll;
    
    private DocumentEvent incomingEvent;
    
    private final RequestProcessor.Task rebuildRegionTask = rebuildRegionRP.create(new RebuildViews());
    
    private boolean listenerPriorityAwareDoc;
    
    public ViewUpdates(DocumentView docView) {
        this.docView = docView;
        incomingModificationListener = new IncomingModificationListener();
        Document doc = docView.getDocument();
        // View hierarchy uses a pair of its own document listeners and DocumentView ignores
        // document change notifications sent from BasicTextUI.RootView.
        // First listener - incomingModificationListener at DocumentListenerPriority.FIRST notifies the hierarchy
        // about incoming document modification.
        // Second listener is "this" at DocumentListenerPriority.VIEW updates the view hierarchy structure
        // according to the document modification.
        // These two listeners avoid situation when a document modification modifies line structure
        // and so the view hierarchy (which uses swing Positions for line view statrts) is inconsistent
        // since e.g. with insert there may be gaps between views and with removal there may be overlapping views
        // but the document listeners that are just being notified include a highlighting layer's document listener
        // BEFORE the BasicTextUI.RootView listener. At that point the highlighting layer would fire a highlighting
        // change and the view hierarchy would attempt to rebuild itself but that would fail.
        listenerPriorityAwareDoc = DocumentUtilities.addPriorityDocumentListener(doc, 
                WeakListeners.create(DocumentListener.class, incomingModificationListener, null),
                DocumentListenerPriority.FIRST);
        // Add the second listener in all cases.
        DocumentUtilities.addDocumentListener(doc,
                WeakListeners.create(DocumentListener.class, this, doc),
                DocumentListenerPriority.VIEW);
    }
    
    void initFactories() {
        // Init view factories
        List<EditorViewFactory.Factory> factoryFactories = EditorViewFactory.factories();
        int size = factoryFactories.size();
        List<EditorViewFactory> factoryList = new ArrayList<EditorViewFactory>(size);
        for (int i = 0; i < size; i++) {
            EditorViewFactory.Factory factoryFactory = factoryFactories.get(i);
            if (factoryFactories != null) {
                EditorViewFactory factory = factoryFactory.createEditorViewFactory(docView);
                if (factory != null) {
                    factory.addEditorViewFactoryListener(WeakListeners.create(
                            EditorViewFactoryListener.class, this, factory));
                    factoryList.add(factory);
                }
            }
        }
        viewFactories = factoryList.toArray(new EditorViewFactory[0]);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "ViewUpdates initializing for {0}, factories: {1}", 
                    new Object[] { docView.getTextComponent(), Arrays.asList(viewFactories) });
        }
    }
    
    void released() {
        if (viewFactories != null) {
            for (EditorViewFactory viewFactory : viewFactories) {
                viewFactory.releaseAll();
            }
        }
    }

    /**
     * Start view building process (it must be followed by finishBuildViews() in try-finally).
     */
    private ViewBuilder startBuildViews() {
//        assert (DocumentUtilities.isReadLocked(documentView.getDocument())) :
//                "Document NOT READ-LOCKED: " + documentView.getDocument(); // NOI18N
        ViewBuilder viewBuilder = new ViewBuilder(docView, viewFactories);
        docView.checkMutexAcquiredIfLogging();
        return viewBuilder;
    }
    
    private void finishBuildViews(ViewBuilder viewBuilder, boolean allowCheck) {
        viewBuilder.finish(); // Includes factory.finish() in each factory
        if (allowCheck) {
            // Since checkIntegrityIfLoggable() may throw an exception
            // the client should only allow check if an exception on client's level
            // is not being currently thrown - otherwise it would be hidden.
            docView.checkIntegrityIfLoggable();
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finer("ViewUpdates.buildViews(): UPDATED-DOC-VIEW:\n" + docView); // NOI18N
        }
    }

    void reinitAllViews() {
        // Build views lazily; boundaries may differ from start/end of doc e.g. for fold preview
        for (int i = MAX_VIEW_REBUILD_ATTEMPTS; i >= 0; i--) {
            ViewBuilder viewBuilder = startBuildViews();
            boolean noException = false;
            try {
                // Possibly clear rebuild region - all the views will be re-inited anyway
                fetchCharRebuildRegion();
                
                viewBuilder.initFullRebuild();
                boolean replaceSuccessful = viewBuilder.createReplaceRepaintViews(i == 0);
                noException = true;
                if (replaceSuccessful) {
                    break; // Creation finished successfully
                }
            } finally {
                finishBuildViews(viewBuilder, noException);
            }
        }
    }
    
    void initParagraphs(int startIndex, int endIndex) {
        for (int i = MAX_VIEW_REBUILD_ATTEMPTS; i >= 0; i--) {
            ViewBuilder viewBuilder = startBuildViews();
            boolean noException = false;
            try {
                viewBuilder.initParagraphs(startIndex, endIndex);
                boolean replaceSuccessful = viewBuilder.createReplaceRepaintViews(i == 0);
                noException = true;
                if (replaceSuccessful) {
                    break;
                }
            } finally {
                finishBuildViews(viewBuilder, noException);
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        clearIncomingEvent(evt);
        if (docView.lock()) {
            docView.checkDocumentLockedIfLogging();
            try { // No return prior this "try" to properly unset incomingModification
                if (!docView.op.isUpdatable()) {
                    return;
                }
                Document doc = docView.getDocument();
                assert (doc == evt.getDocument()) : "Invalid document";
                int insertOffset = evt.getOffset();
                int insertLength = evt.getLength();
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("\nDOCUMENT-INSERT-evt: offset=" + insertOffset + ", length=" + insertLength + // NOI18N
                            ", cRegion=" + charRebuildRegion + // NOI18N
                            ", current-docViewEndOffset=" + (evt.getDocument().getLength()+1) + '\n'); // NOI18N
                }
                updateViewsByModification(insertOffset, insertLength, evt);

            } finally {
                docView.op.clearIncomingModification();
                docView.unlock();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        clearIncomingEvent(evt);
        if (docView.lock()) {
            docView.checkDocumentLockedIfLogging();
            try { // No return prior this "try" to properly unset incomingModification
                if (!docView.op.isUpdatable() || docView.getViewCount() == 0) {
                    // For viewCount zero - it would later fail on paragraphViewIndex == -1
                    // Even for empty doc there should be a single paragraph view for extra ending '\n'
                    // so this should only happen when no views were created yet.
                    return;
                }
                Document doc = docView.getDocument();
                assert (doc == evt.getDocument()) : "Invalid document";
                int removeOffset = evt.getOffset();
                int removeLength = evt.getLength();
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("\nDOCUMENT-REMOVE-evt: offset=" + removeOffset + ", length=" + removeLength + // NOI18N
                            ", cRegion=" + charRebuildRegion + // NOI18N
                            ", current-docViewEndOffset=" + (evt.getDocument().getLength()+1) + '\n'); // NOI18N
                }
                updateViewsByModification(removeOffset, -removeLength, evt);

            } finally {
                docView.op.clearIncomingModification();
                docView.unlock();
            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        clearIncomingEvent(evt);
        if (docView.lock()) {
            docView.checkDocumentLockedIfLogging();
            try {
                if (!docView.op.isUpdatable()) {
                    return;
                }
                // TODO finish
                docView.checkIntegrityIfLoggable();
            } finally {
                docView.unlock();
            }
        }
    }
    
    private void updateViewsByModification(int modOffset, int modLength, DocumentEvent evt) {
        // Update views by modification. For faster operation ignore stale creation
        // and check it afterwards and possibly invalidate the created views (by ViewBuilder).
        ViewBuilder viewBuilder = startBuildViews();
        boolean success = false;
        try {
            OffsetRegion cRegion = fetchCharRebuildRegion();
            if (viewBuilder.initModUpdate(modOffset, modLength, cRegion)) {
                boolean replaced = viewBuilder.createReplaceRepaintViews(true);
                assert (replaced) : "Views replace failed"; // NOI18N
                docView.validChange().documentEvent = evt;
                int startCreationOffset = viewBuilder.getStartCreationOffset();
                int matchOffset = viewBuilder.getMatchOffset();
                Document doc = docView.getDocument();
                if (cRegion != null) {
                    BlockCompare bc = BlockCompare.get(cRegion.startOffset(), cRegion.endOffset(),
                            startCreationOffset, matchOffset);
                    if (bc.inside()) {
                        cRegion = null; // Created area fully contains cRegion
                    } else if (bc.overlapStart()) {
                        cRegion = OffsetRegion.create(cRegion.startPosition(),
                                ViewUtils.createPosition(doc, startCreationOffset));
                    } else if (bc.overlapEnd()) {
                        cRegion = OffsetRegion.create(ViewUtils.createPosition(doc, matchOffset),
                                cRegion.endPosition());
                    }
                }
            }
            if (cRegion != null) { // cRegion area that remains "dirty"
                extendCharRebuildRegion(cRegion);
            }
            success = true;
        } finally {
            finishBuildViews(viewBuilder, success);
        }
    }

    @Override
    public void viewFactoryChanged(EditorViewFactoryEvent evt) {
        // Do not build views directly (when event arrives) since it could damage
        // view hierarchy processing. For example painting requires paint highlights
        // and fetching them could trigger the change event
        // and a synchronous rebuild of views would affect paint operation processing.
        synchronized (rebuildRegionsLock) {
            // Check whether the pane's document is still equal to docView's document.
            // If not then ignore upcoming event since the event likely comes
            // from an obsolete weak listener.
            JTextComponent c = docView.getTextComponent();
            if (c == null || c.getDocument() != docView.getDocument()) {
                return;
            }

            docView.checkDocumentLockedIfLogging();
            // Post the task only if the region is null (if it would be non-null
            // a pending task would be started for it previously)
            for (EditorViewFactoryChange change : evt.getChanges()) {
                int startOffset = change.getStartOffset();
                int endOffset = change.getEndOffset();
                // Ignore empty <startOffset,endOffset> regions
                Document doc = docView.getDocument();
                int docTextLen = doc.getLength() + 1;
                startOffset = Math.min(startOffset, docTextLen);
                endOffset = Math.min(endOffset, docTextLen);
                switch (change.getType()) {
                    case CHARACTER_CHANGE:
                        charRebuildRegion = OffsetRegion.union(charRebuildRegion, doc, startOffset, endOffset, true);
                        break;
                    case PARAGRAPH_CHANGE:
                        paragraphRebuildRegion = OffsetRegion.union(paragraphRebuildRegion, doc, startOffset, endOffset, true);
                        break;
                    case REBUILD:
                        rebuildAll = true;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected type=" + change.getType());
                }
            }
            if (!rebuildRegionsScheduled) { // If rebuilding scheduled do not reschedule
                rebuildRegionsScheduled = true;
                rebuildRegionTask.schedule(REBUILD_DELAY);
            } // Otherwise the task is scheduled already
        }

    }

    void viewsRebuildOrMarkInvalidNeedsLock() { // It should be called with acquired mutex only
        docView.checkDocumentLockedIfLogging();
        if (docView.op.isActive()) {
            boolean rebuildAllLocal;
            synchronized (rebuildRegionsLock) {
                rebuildAllLocal = rebuildAll;
                rebuildAll = false;
            }
            if (rebuildAllLocal) {
                for (int i = MAX_VIEW_REBUILD_ATTEMPTS; i >= 0; i--) {
                    ViewBuilder viewBuilder = startBuildViews();
                    boolean noException = false;
                    try {
                        viewBuilder.initFullRebuild();
                        boolean replaceSuccessful = (viewBuilder.createReplaceRepaintViews(i == 0));
                        noException = true;
                        if (replaceSuccessful) {
                            break; // Creation finished successfully
                        }
                    } finally {
                        finishBuildViews(viewBuilder, noException);
                    }
                }
            }

            OffsetRegion pRegion = fetchParagraphRebuildRegion();
            if (pRegion != null && !pRegion.isEmpty()) {
                for (int i = MAX_VIEW_REBUILD_ATTEMPTS; i >= 0; i--) {
                    // Do nothing if docView is not active. Once becomes active a full rebuild will be done.
                    ViewBuilder viewBuilder = startBuildViews();
                    boolean noException = false;
                    try {
                        if (viewBuilder.initRebuildParagraphs(pRegion)) {
                            if (viewBuilder.createReplaceRepaintViews(i == 0)) {
                                noException = true; // Ensure checking in finally { ... } gets done in this case
                                break; // Creation finished successfully
                            } else {
                                // There could be additional changes in the meantime
                                OffsetRegion newPRegion = fetchParagraphRebuildRegion();
                                if (newPRegion != null) {
                                    pRegion = pRegion.union(newPRegion, true);
                                }
                            }
                        }
                        noException = true;
                    } finally {
                        finishBuildViews(viewBuilder, noException);
                    }
                }
            }
            
            OffsetRegion cRegion = fetchCharRebuildRegion();
            if (cRegion != null && !cRegion.isEmpty()) {
                int pCount = docView.getViewCount();
                int startOffset = cRegion.startOffset();
                int endOffset = cRegion.endOffset();
                if (pCount > 0 && endOffset > docView.getStartOffset() && startOffset < docView.getEndOffset()) {
                    int startPIndex = docView.getViewIndex(startOffset);
                    int pIndex = startPIndex;
                    ParagraphView pView = docView.getParagraphView(pIndex);
                    double startY = docView.getY(pIndex);
                    int pViewStartOffset = pView.getStartOffset();
                    Rectangle2D repaintRect = null;
                    boolean localRepaint = false;
                    while (startOffset < endOffset) {
                        int pViewLength = pView.getLength();
                        if (!pView.isChildrenNull()) { // Compute local offsets
                            int lStartOffset = Math.max(startOffset - pViewStartOffset, 0);
                            int lEndOffset = Math.min(endOffset - pViewStartOffset,pViewLength);
                            if (pView.isChildrenValid()) {
                                pView.markChildrenInvalid();
                            } else { // Already an invalid range
                                lStartOffset = Math.min(lStartOffset, pView.children.getStartInvalidChildrenLocalOffset());
                                lEndOffset = Math.max(lEndOffset, pView.children.getEndInvalidChildrenLocalOffset());
                            }
                            pView.children.setInvalidChildrenLocalRange(lStartOffset, lEndOffset);
                            // When change is local within first paragraph => compute precise repaint bounds
                            if (pIndex == startPIndex && lEndOffset < pViewLength) {
                                localRepaint = true;
                                Shape pAlloc = docView.getChildAllocation(pIndex, docView.getAllocation());
                                if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                                    pAlloc = docView.getChildAllocation(pIndex, docView.getAllocation());
                                }
                                Shape s = pView.modelToViewChecked(startOffset, Position.Bias.Forward,
                                        endOffset, Position.Bias.Forward, pAlloc);
                                repaintRect = ViewUtils.shapeAsRect(s);
                            }
                        } // else: Null children already invalid
                        pViewStartOffset += pViewLength;
                        pIndex++;
                        if (pIndex >= pCount) {
                            break;
                        }
                        pView = docView.getParagraphView(pIndex);
                        startOffset = pViewStartOffset;
                    }

                    if (!localRepaint) {
                        Rectangle2D.Double r = docView.getAllocationCopy();
                        r.y += startY;
                        r.height = docView.getY(pIndex) - startY;
                        docView.op.extendToVisibleWidth(r);
                        repaintRect = r;
                    }
                    assert (repaintRect != null) : "Null repaintRect"; // NOI18N
                    docView.op.notifyRepaint(repaintRect);
                } // No pViews or update does not touch docView -> do not rebuild anything
                // Now just wait until a paint request will rebuild the local views
            }
        }
    }

    OffsetRegion fetchCharRebuildRegion() {
        synchronized (rebuildRegionsLock) {
            OffsetRegion region = charRebuildRegion;
            charRebuildRegion = null;
            return region;
        }
    }
    
    void extendCharRebuildRegion(OffsetRegion newRegion) {
        synchronized (rebuildRegionsLock) {
            charRebuildRegion = OffsetRegion.union(charRebuildRegion, newRegion, true);
        }
    }
    
    OffsetRegion fetchParagraphRebuildRegion() {
        synchronized (rebuildRegionsLock) {
            OffsetRegion region = paragraphRebuildRegion;
            paragraphRebuildRegion = null;
            return region;
        }
    }
    
    /*private*/ void incomingEvent(DocumentEvent evt) {
        if (incomingEvent != null) {
            // Rebuild the view hierarchy: temporary solution until the real cause is found.
            docView.op.releaseChildrenNeedsLock();
            LOG.log(Level.INFO, "View hierarchy rebuild due to pending document event", // NOI18N
                    new Exception("Pending incoming event: " + incomingEvent)); // NOI18N
        }
        incomingEvent = evt;
    }
    
    private void clearIncomingEvent(DocumentEvent evt) {
        if (listenerPriorityAwareDoc) {
            if (incomingEvent == null) {
                throw new IllegalStateException("Incoming event already cleared"); // NOI18N
            }
            if (incomingEvent != evt) {
                throw new IllegalStateException("Invalid incomingEvent=" + incomingEvent + " != evt=" + evt); // NOI18N
            }
            incomingEvent = null;
        }
    }

    StringBuilder appendInfo(StringBuilder sb) {
        sb.append("Regions:");
        int len = sb.length();
        synchronized (rebuildRegionsLock) {
            if (charRebuildRegion != null) {
                sb.append(" C").append(charRebuildRegion);
            }
            if (paragraphRebuildRegion != null) {
                sb.append(" P").append(paragraphRebuildRegion);
            }
            if (rebuildAll) {
                sb.append(" A");
            }
        }
        if (sb.length() == len) {
            sb.append(" <NONE>");
        }
        return sb;
    }

    @Override
    public String toString() {
        return appendInfo(new StringBuilder(200)).toString();
    }
    
    private final class IncomingModificationListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            incomingEvent(e);
            docView.op.markIncomingModification();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            incomingEvent(e);
            docView.op.markIncomingModification();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            incomingEvent(e);
        }

    }

    private final class RebuildViews implements Runnable {
        
        public @Override void run() {
            if (docView.testRun != null) {
                return;
            }
            synchronized (rebuildRegionsLock) {
                rebuildRegionsScheduled = false;
            }
            docView.op.viewsRebuildOrMarkInvalid();
        }

    }
}
