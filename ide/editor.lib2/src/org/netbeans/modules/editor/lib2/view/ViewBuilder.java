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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.ArrayUtilities;


/**
 * View building support.
 * <br>
 * When building new views they must have "enough space" so the old view(s) that occupy
 * area of the new view must be removed. When firstReplace is non-null
 * then the views can be replaced locally in a paragraph view.
 * However if the replace exceeds a local replace then full paragraph views
 * are being removed and recreated. This is because otherwise the remaining
 * local views would have to be re-parented because new paragraph view instances
 * are being created and used.
 * 
 * @author Miloslav Metelka
 */

final class ViewBuilder {

    /**
     * Maximum number of scanned characters for which the view building will produce local views.
     */
    /*private*/ static final int MAX_CHARS_FOR_CREATE_LOCAL_VIEWS = 2000;

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewBuilder.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewBuilder.class.getName());

    /**
     * Replace of paragraph views in a document view.
     */
    private final ViewReplace<DocumentView,ParagraphView> docReplace;
    
    private final EditorViewFactory[] viewFactories;

    private FactoryState[] factoryStates;

    private boolean createLocalViews; // Whether children of paragraph views are created
    
    private boolean forceCreateLocalViews;

    private int startCreationOffset;
    
    /**
     * Offset where the views creation start.
     * During views building it's offset of a next view that will be created.
     */
    private int creationOffset;

    /**
     * Offset (in after-mod offset space) where the views creation can possibly match.
     * This (unlike endCreationOffset) may be within local views of a paragraph view.
     */
    private int matchOffset;
    
    /**
     * Current end creation offset that was reported to factories.
     * If rebuilding goes past this value then it will be reported to factories.
     */
    private int endCreationOffset;
    
    /**
     * Start offset of a before-modOffset local view that may be reused.
     * <br>
     * If set to Integer.MAX_VALUE then no more before-modOffset reusing possible
     * or bmReusePView has no children.
     */
    private int bmReuseOffset;

    /**
     * PView containing bmReuseOffset or null.
     */
    private ParagraphView bmReusePView;

    /**
     * Local index inside bmReusePView that corresponds to bmReuseOffset.
     */
    private int bmReuseLocalIndex;

    /**
     * Start offset of an above-modOffset local view that may be reused
     * (or if no local views exist inside 
     * <br>
     * If set to Integer.MAX_VALUE then no more before-modOffset reusing possible
     * or bmReusePView has no children.
     */
    private int amReuseOffset;
    
    private int amReusePIndex;
    
    private ParagraphView amReusePView;
    
    private int amReuseLocalIndex;
    
    /**
     * Modification offset when updating after modification.
     */
    private int modOffset;

    /**
     * "modOffset+modLength" for insertion and "modOffset" for removal.
     */
    private int endModOffset;

    /**
     * Whether an insert was performed right at a pView begining and so pView's start position
     * must be updated.
     */
    private boolean insertAtPViewStart;
    
    private Element lineRoot;

    private int lineIndex;

    private int lineEndOffset;
    
    /**
     * Line element to be possibly used for a paragraph view's start position.
     */
    private Element lineForParagraphView;
    
    /**
     * First local replace or null if none.
     * It's in ParagraphView[docReplace.index-1] and it is attempted in certain cases
     * (see checkLocalRebuild()) to make the view rebuilds as small as possible.
     * However if the rebuild continues across lines then the granularity extends to whole
     * paragraph views (matchOffset is increased by whole paragraph views).
     */
    private ViewReplace<ParagraphView,EditorView> firstReplace;

    /**
     * Actual local views replace inside currently served paragraph view.
     * <br>
     * It may be equal to firstReplace when replacing inside firstly updated paragraph view.
     */
    private ViewReplace<ParagraphView,EditorView> localReplace;

    /**
     * List of all paragraph replaces done so far in docReplace (firstReplace is not part
     * of allReplaces).
     */
    private List<ViewReplace<ParagraphView,EditorView>> allReplaces;
    
    private volatile boolean staleCreation;
    
    static enum RebuildCause {
        FULL_REBUILD, // Full rebuild of all paragraphs
        REBUILD_PARAGRAPHS, // Rebuild a paragraphs region that has changed
        INIT_PARAGRAPHS, // Initialize children of one or more paragraphs (for painting etc.)
        MOD_UPDATE, // Update after modification in the document
    }
    
    /** Cause of the rebuild for logging purposes. */
    private RebuildCause rebuildCause;

    /**
     * Construct view builder.
     * @param docView non-null doc view for which view building is performed.
     * @param viewFactories view factories that should be sorted with increasing priority.
     */
    ViewBuilder(DocumentView docView, EditorViewFactory[] viewFactories) {
        // Always do document-replace since the built views can extend beyond firstReplace even for very local changes
        this.docReplace = new ViewReplace<DocumentView, ParagraphView>(docView);
        this.viewFactories = viewFactories;
        this.forceCreateLocalViews = docView.op.isAccurateSpan();
    }

    int getStartCreationOffset() {
        return startCreationOffset;
    }
    
    int getMatchOffset() {
        return matchOffset;
    }

    boolean isStaleCreation() {
        return staleCreation;
    }

    /**
     * Init for build/rebuild of all paragraph views in the document view.
     */
    void initFullRebuild() {
        rebuildCause = RebuildCause.FULL_REBUILD;
        DocumentView docView = docReplace.view;
        modOffset = endModOffset = Integer.MIN_VALUE; // No mod
        startCreationOffset = docView.getStartOffset();
        matchOffset = docView.getEndOffset();
        endCreationOffset = matchOffset;
        int buildLen = (endCreationOffset - startCreationOffset);
        createLocalViews = forceCreateLocalViews || (buildLen <= MAX_CHARS_FOR_CREATE_LOCAL_VIEWS);
        docReplace.removeTillEnd();
        amReuseOffset = Integer.MAX_VALUE; // No reusal (need fresh views)
        // No local rebuild => leave firstReplace == null
    }

    /**
     * Init requested paragraphs views. First and last paragraphs will children==null
     * the rest is unknown.
     *
     * @param startRebuildIndex index of first paragraph where the rebuilding occurs.
     * @param endRebuildIndex
     */
    void initParagraphs(int startRebuildIndex, int endRebuildIndex) {
        DocumentView docView = docReplace.view;
        int startOffset = docView.getParagraphView(startRebuildIndex).getStartOffset();
        int endOffset = docView.getParagraphView(endRebuildIndex - 1).getEndOffset();
        rebuildCause = RebuildCause.INIT_PARAGRAPHS;
        modOffset = endModOffset = Integer.MIN_VALUE; // No mod
        forceCreateLocalViews = true;
        createLocalViews = forceCreateLocalViews;
        docReplace.index = startRebuildIndex;
        docReplace.setRemoveCount(endRebuildIndex - startRebuildIndex);
        startCreationOffset = startOffset;
        matchOffset = endOffset;
        endCreationOffset = endOffset;
        amReuseOffset = startOffset;
        amReusePIndex = startRebuildIndex;
        amReusePView = docReplace.view.getParagraphView(amReusePIndex);
        amReuseLocalIndex = 0;
    }

    /**
     * Initialize rebuild of a given changed region where structure of paragraph views changed.
     *
     * @param pRegion non-null changed region
     * @return true if the change affects the view hierarchy and the createViews()
     *  method should be called. If false is returned createViews() should not be called
     *  and just finish() should be called.
     */
    boolean initRebuildParagraphs(OffsetRegion pRegion) {
        rebuildCause = RebuildCause.REBUILD_PARAGRAPHS;
        DocumentView docView = docReplace.view;
        int startAffectedOffset = pRegion.startOffset();
        int endAffectedOffset = pRegion.endOffset();
        int startCreationIndex; // Index of first paragraph view to be rebuilt or -1
        int startOffset = docView.getStartOffset();
        if (endAffectedOffset < startOffset) {
            // Affected area completely below docView's start
            startCreationIndex = -1;
        } else if (startAffectedOffset >= docView.getEndOffset()) {
            // Affected area completely above docView's end
            startCreationIndex = -1;
        } else { // Change affects docView
            startCreationIndex = docView.getViewIndex(startAffectedOffset);
            // would be -1 in case pViewCount == 0 - but there should be full rebuild performed first
            // so this state is ok.
        }
        if (startCreationIndex != -1) {
            ParagraphView pView = docView.getParagraphView(startCreationIndex);
            startCreationOffset = pView.getStartOffset();
            int endCreationIndex = docView.getViewIndex(endAffectedOffset);
            ParagraphView endPView = docView.getParagraphView(endCreationIndex);
            // Either endAffectedOffset points to begining of paragraph or inside it
            endCreationOffset = endPView.getStartOffset();
            if (endCreationOffset < endAffectedOffset) { // Inside paragraph
                endCreationOffset += endPView.getLength();
                endCreationIndex++;
            }
            modOffset = endModOffset = Integer.MIN_VALUE; // No mod
            matchOffset = endCreationOffset;
            int buildLen = (endCreationOffset - startCreationOffset);
            createLocalViews = forceCreateLocalViews || (buildLen <= MAX_CHARS_FOR_CREATE_LOCAL_VIEWS);
            docReplace.index = startCreationIndex;
            docReplace.setRemoveCount(endCreationIndex - startCreationIndex);
            amReuseOffset = startCreationOffset;
            amReusePIndex = startCreationIndex;
            amReusePView = docReplace.view.getParagraphView(amReusePIndex);
            amReuseLocalIndex = 0;
            return true;
        }
        return false;
    }

    /**
     * Initialize view builder after document modification.
     *
     * @param modOffset it must be endOffset for no-mod; insertOffset for inserts
     *  and removeOffset for removals.
     * @param modLength it's 0 for no-mod; +insertLength for inserts; -removeLength for removals.
     * @param cRegion character rebuild region in after-mod offset space or null if no extra
     *  affected area.
     * @return true if the change affects the view hierarchy and the createViews()
     *  method should be called. If false is returned createViews() should not be called
     *  and just finish() should be called.
     */
    boolean initModUpdate(int modOffset, int modLength, OffsetRegion cRegion) {
        rebuildCause = RebuildCause.MOD_UPDATE;
        // 1.Since the modifications are typically local do not fully add modified region
        //   to the rebuilt area since it could increase the rebuilt region dramatically
        //   and from a modification that would normally be local-rebuild
        //   (assuming non-null children in the corresponding pView) it could become
        //   non-local rebuild which would drop valuable TextLayout instances
        //   from local views.
        // 2.Paragraph views affected by modification are computed first.
        //   Then if currently maintained start offfset of damaged characters region
        //   is lower than the modification offset then the lower offset is taken into accound
        //   (but the starting paragraph view remains - due to item 1).
        //   The same is done for ending bound.
        // 3.Affected area must be pre-computed in order to give correct assumed ending offset
        //   estimate to view factories.
        // 4.First prepare before-mod part of rebuild. Once modOffset will be reached
        //   during doing view rebuild then the modLength will be skipped and reuse* and other
        //   variables will be updated.
        this.modOffset = modOffset;
        DocumentView docView = docReplace.view;
        int pViewCount = docView.getViewCount();
        int startOffset = docView.getStartOffset(); // Start offset not yet updated by current modification
        int endOffset = docView.getEndOffset(); // End offset not yet updated by current modification
        int startCreationPIndex; // Index of first pView to be rebuilt
        ParagraphView startCreationPView = null;
        int startCreationPViewOffset = -1;
        int startCreationLocalOffset = 0;
        // Index of last pView (in original indexes) affected by modification
        //  -1 remains if there are no pViews (pViewCount == 0)
        int lastAffectedPIndex = -1;
        ParagraphView lastAffectedPView = null;
        int lastAffectedLocalOffset = -1;

        if (modLength > 0) { // INSERTION
            createLocalViews = forceCreateLocalViews || (modLength < MAX_CHARS_FOR_CREATE_LOCAL_VIEWS);
            endModOffset = modOffset + modLength;
            // When inserting right at view's begining a previous pView is found by bin-search
            // since insertion shifted affected next pView's start position.
            // But the change in fact belongs to the next view. However the next view's start position
            // is incorrect (shifted by insert) so whole pView must be reconstructed
            // (that will be checked later in common part code).
            // This does not in fact apply for pView at offset==0 (such position does not shift)
            // but let's treat it as an implementation detail of AbstractDocument-based doc impls.
            if (modOffset < startOffset) {
                docView.setStartOffset(startOffset + modLength);
                docView.setEndOffset(endOffset + modLength);
                // Ignore (mod below VH) => leave startRebuildIndex == -1
                startCreationPIndex = -1;
            } else if (modOffset == startOffset) { // Insert right at VH begining
                // Include inserted text into docView => leave docView.startOffset unchanged
                startCreationPIndex = 0;
                startCreationPView = (pViewCount > 0) ? docView.getParagraphView(0) : null;
                startCreationPViewOffset = startOffset;

            } else if (modOffset <= endOffset) {
                // docView.startOffset stays at current value
                startCreationPIndex = docView.getViewIndex(modOffset);
                startCreationPView = docView.getParagraphView(startCreationPIndex);
                startCreationPViewOffset = startCreationPView.getStartOffset();
                // Check for insert right at pView's begining => should rebuild next pView since
                // it's affected by the insertion
                if (startCreationPIndex + 1 < pViewCount &&
                        startCreationPViewOffset + startCreationPView.getLength() == modOffset)
                {
                    startCreationPIndex++;
                    startCreationPView = docView.getParagraphView(startCreationPIndex);
                    startCreationPViewOffset = modOffset;
                }
            } else { // above VH's end no-need to update docView.startOffset or endOffset
                // docView.startOffset stays at current value
                startCreationPIndex = -1;
            }

            if (startCreationPIndex != -1) {
                startCreationLocalOffset = modOffset - startCreationPViewOffset;
                lastAffectedPIndex = startCreationPIndex;
                lastAffectedPView = startCreationPView;
                lastAffectedLocalOffset = startCreationLocalOffset;
                docView.setEndOffset(endOffset + modLength);
            }
            
        } else { // REMOVAL: modLength < 0
            createLocalViews = true; // By default build local views (later may be corrected)
            int removeLen = -modLength;
            int endRemoveOffset = modOffset + removeLen;
            endModOffset = modOffset;
            int newEndOffset = (endOffset > modOffset)
                    ? Math.max(modOffset, endOffset - removeLen)
                    : endOffset;
            if (pViewCount == 0) { // startOffset == endOffset
                docView.setStartOffset(newEndOffset);
                // docView.setEndOffset(newEndOffset) done later
                startCreationPIndex = -1;

            } else if (modOffset < startOffset) { // Removal that starts below start of docView
                if (endRemoveOffset > startOffset) { // Removal hits start of VH (or includes whole VH)
                    docView.setStartOffset(modOffset);
                    startCreationPViewOffset = startOffset;
                    startCreationPView = docView.getParagraphView(0);
                    startCreationPIndex = 0;
                    startCreationLocalOffset = 0;

                } else { // Removal completely below VH
                    docView.setStartOffset(startOffset - removeLen);
                    startCreationPIndex = -1;
                }
                
            } else { // modOffset >= startOffset
                if (endOffset <= modOffset) { // Removal above docView's end
                    // docView.startOffset and endOffset stays at current value
                    startCreationPIndex = -1;
                } else { // inside or past docView's end
                    // docView.startOffset stays at current value
                    if (modOffset == startOffset) { // Removal right at docView's start offset
                        startCreationPIndex = 0;
                    } else { // Removal starts inside docView
                        // Search for modOffset - 1 since (multiple) positions might get moved to modOffset
                        // due to removal and bin-search would find first one of them for modOffset
                        startCreationPIndex = docView.getViewIndex(modOffset - 1);
                        startCreationPView = docView.getParagraphView(startCreationPIndex);
                        startCreationPViewOffset = startCreationPView.getStartOffset();
                        startCreationLocalOffset = modOffset - startCreationPViewOffset;
                        if (startCreationLocalOffset >= startCreationPView.getLength()) {
                            startCreationPIndex++;
                            startCreationPView = null;
                        }
                    }
                    if (startCreationPView == null) {
                        startCreationPView = docView.getParagraphView(startCreationPIndex);
                        startCreationPViewOffset = startCreationPView.getStartOffset();
                        startCreationLocalOffset = modOffset - startCreationPViewOffset;
                    }
                }
            }
            docView.setEndOffset(newEndOffset);

            if (startCreationPIndex != -1) { // REMOVAL: compute lastAffected stuff
                // Must go through pViews and examine their lengths (since position are moved to modOffset)
                lastAffectedPIndex = startCreationPIndex;
                lastAffectedPView = startCreationPView;
                int lastAffectedPViewOffset = startCreationPViewOffset;
                // If local offset not within view then goto next pView if it exists
                int pViewLen = lastAffectedPView.getLength();
                lastAffectedLocalOffset = endRemoveOffset - lastAffectedPViewOffset;
                while (lastAffectedLocalOffset > pViewLen && lastAffectedPIndex + 1 < pViewCount) {
                    lastAffectedPViewOffset += pViewLen;
                    lastAffectedPIndex++;
                    lastAffectedPView = docView.getParagraphView(lastAffectedPIndex);
                    pViewLen = lastAffectedPView.getLength();
                    lastAffectedLocalOffset = endRemoveOffset - lastAffectedPViewOffset;
                }
                lastAffectedLocalOffset = Math.min(lastAffectedLocalOffset, pViewLen);
            }
        } // END-of-REMOVAL handling
        
        // Common for insert and remove - determine local offsets and indices in affected pViews
        // And set boundaries for creation and views reuse.
        if (startCreationPIndex != -1) { // Modification needs to be processed
            docReplace.index = startCreationPIndex;
            if (startCreationPView != null) {
                // Check whether cRegion would affect rebuilding
                int cRegionStartLocalOffset;
                int cRegionEndLocalOffset;
                if (cRegion != null) {
                    cRegionStartLocalOffset = cRegion.startOffset() - startCreationPViewOffset;
                    cRegionEndLocalOffset = cRegion.endOffset() - startCreationPViewOffset;
                    if (cRegionEndLocalOffset == cRegionStartLocalOffset || // Empty
                            cRegionEndLocalOffset <= 0 || // End before pView start
                            cRegionStartLocalOffset >= startCreationPView.getLength()) // Start above pView's end
                    {
                        cRegion = null;
                    }
                } else {
                    cRegionStartLocalOffset = cRegionEndLocalOffset = 0;
                }

                docReplace.setRemoveCount(lastAffectedPIndex + 1 - docReplace.index);
                int lastAffectedPViewLength = lastAffectedPView.getLength();
                int tillLastAffectedEnd = (lastAffectedPViewLength - lastAffectedLocalOffset);
                // End creation is paragraph end (in after-mod offstes)
                endCreationOffset = endModOffset + tillLastAffectedEnd;
                // When removing till end of line (including newline) the next pView will be affected too
                if (tillLastAffectedEnd == 0) {
                    if (lastAffectedPIndex + 1 < pViewCount) {
                        endCreationOffset += docView.getParagraphView(lastAffectedPIndex + 1).getLength();
                    }
                }
                if (startCreationPView.isChildrenNull()) { // Must rebuild from pView start
                    // For null children only build local views if forced - prevents building
                    // of local views for e.g. reformatting (many small changes throughout document).
                    createLocalViews = forceCreateLocalViews;
                    bmReuseOffset = modOffset; // No before-mod reusal
                    startCreationOffset = modOffset - startCreationLocalOffset;

                } else { // Valid children
                    int shiftBack = 0;
                    bmReusePView = startCreationPView;
                    if (startCreationLocalOffset > 0) {
                        if (!createLocalViews) {
                            shiftBack = startCreationLocalOffset;
                        } else {
                            if (cRegion != null) {
                                shiftBack = Math.min(Math.max(cRegionStartLocalOffset, 0), // Could be < 0
                                        startCreationLocalOffset);
                            }
                            if (!startCreationPView.isChildrenValid()) {
                                int startInvalidOffset = startCreationPView.children.getStartInvalidChildrenLocalOffset();
                                shiftBack = Math.max(shiftBack, startCreationLocalOffset - startInvalidOffset);
                            }
                        }
                        startCreationLocalOffset -= shiftBack;
                        bmReuseLocalIndex = startCreationPView.getViewIndexLocalOffset(startCreationLocalOffset);
                        int viewLocalStartOffset = startCreationPView.getLocalOffset(bmReuseLocalIndex);
                        int viewStartShiftBack = startCreationLocalOffset - viewLocalStartOffset;
                        if (viewStartShiftBack == 0 && bmReuseLocalIndex > 0) {
                            // Insert/remove at local view's start => rebuild prev view too
                            // since it may incorporate just inserted chars (or beyond-remove chars).
                            bmReuseLocalIndex--;
                            viewStartShiftBack += startCreationPView.getEditorView(bmReuseLocalIndex).getLength();
                        }
                        // startCreationLocalOffset -= viewStartShiftBack; but startCreationLocalOffset no longer used
                        shiftBack += viewStartShiftBack;
                        startCreationOffset = modOffset - shiftBack;
                        bmReuseOffset = startCreationOffset;

                    } else { // startCreationLocalOffset == 0
                        if (modLength > 0) { // Insert at pView's begining
                            insertAtPViewStart = true; // Marker value for insert-at-pView-begining
                        }
                        bmReuseLocalIndex = 0;
                        bmReuseOffset = modOffset;
                        startCreationOffset = modOffset;
                    }

                    if (createLocalViews) {
                        firstReplace = new ViewReplace<ParagraphView, EditorView>(startCreationPView);
                        firstReplace.index = bmReuseLocalIndex;
                        localReplace = firstReplace;
                        // pView at docReplace.index will be processed by firstReplace
                        docReplace.index++;
                        docReplace.setRemoveCount(docReplace.getRemoveCount() - 1);
                    }
                }

                // Extra affected area above lastAffectedLocalOffset (which locally corresponds to endModOffset).
                // StartOffset of local view at amReuseLocalIndex will correspond to (lastAffectedLocalOffset + shift)
                // Note: lastAffectedLocalOffset locally corresponds to endModOffset.
                int shift;
                amReusePIndex = lastAffectedPIndex;
                amReusePView = lastAffectedPView; // non-null
                int endRemoveLocalIndex;
                boolean reuseNextPView;
                if (lastAffectedPView.isChildrenNull() || !createLocalViews) {
                    shift = tillLastAffectedEnd;
                    amReuseOffset = endModOffset + tillLastAffectedEnd;
                    endRemoveLocalIndex = lastAffectedPView.getViewCount();
                    reuseNextPView = true;

                } else { // Valid children of lastAffectedPView (amReusePView)
                    // Compute after-mod area reusal offset and local index
                    endRemoveLocalIndex = lastAffectedPView.getViewIndexLocalOffset(lastAffectedLocalOffset);
                    int startLocalOffset = lastAffectedPView.getLocalOffset(endRemoveLocalIndex);
                    if (lastAffectedLocalOffset > startLocalOffset) { // Reuse at the next index
                        int localViewLength = lastAffectedPView.getEditorView(endRemoveLocalIndex).getLength();
                        shift = (startLocalOffset + localViewLength) - lastAffectedLocalOffset;
                        endRemoveLocalIndex++;
                        amReuseOffset = endModOffset + shift;
                        reuseNextPView = (endRemoveLocalIndex == lastAffectedPView.getViewCount());
                    } else { // Local view's start boundary right at endModOffset
                        shift = 0;
                        amReuseOffset = endModOffset;
                        reuseNextPView = false;
                    }
                    amReuseLocalIndex = endRemoveLocalIndex;
                }
                if (reuseNextPView) {
                    amReuseLocalIndex = 0; // means viewCount (children==null)
                    amReusePIndex++;
                    if (amReusePIndex < pViewCount) {
                        amReusePView = docView.getParagraphView(amReusePIndex);
                    } else {
                        amReusePView = null;
                        amReuseOffset = Integer.MAX_VALUE;
                    }
                }

                // Possibly extend shift by cRegion and children that were marked as invalid
                int origShift = shift;
                if (shift < tillLastAffectedEnd) {
                    if (startCreationPView != lastAffectedPView) { // Will create views till end of lastAffectedPView
                        shift = tillLastAffectedEnd;
                    } else {
                        if (cRegion != null) {
                            shift = Math.max(shift, Math.min(cRegion.endOffset() - endModOffset, tillLastAffectedEnd));
                        }
                        if (!lastAffectedPView.isChildrenValid()) { // Some children invalidated
                            int endInvalidLocalOffset = lastAffectedPView.children.getEndInvalidChildrenLocalOffset();
                            shift = Math.max(shift, endInvalidLocalOffset - lastAffectedLocalOffset);
                        }
                    }
                }

                if (shift != origShift) {
                    if (shift < tillLastAffectedEnd) { // Within startCreationPView
                        int endALocalOffset = lastAffectedLocalOffset + shift;
                        endRemoveLocalIndex = lastAffectedPView.getViewIndexLocalOffset(endALocalOffset);
                        int startLocalOffset = lastAffectedPView.getLocalOffset(endRemoveLocalIndex);
                        if (endALocalOffset > startLocalOffset) { // Reuse at the next index
                            int localViewLength = lastAffectedPView.getEditorView(endRemoveLocalIndex).getLength();
                            shift = (startLocalOffset + localViewLength) - lastAffectedLocalOffset;
                            endRemoveLocalIndex++;
                        }
                    } else { // shift == tillLastAffectedEnd
                        endRemoveLocalIndex = lastAffectedPView.getViewCount();
                    }
                }
                matchOffset = endModOffset + shift;

                if (firstReplace != null) {
                    int endIndex = (startCreationPView == lastAffectedPView)
                            ? endRemoveLocalIndex
                            : startCreationPView.getViewCount();
                    firstReplace.setRemoveCount(endIndex - firstReplace.index);
                }

            } else { // Should only be insert when no pViews are present
                docReplace.setRemoveCount(0);
                startCreationOffset = startOffset;
                endCreationOffset = docView.getEndOffset(); // Do not use endOffset (use updated end offset)
                matchOffset = endCreationOffset;
                bmReuseOffset = modOffset;
                amReuseOffset = Integer.MAX_VALUE; // No reusal; amReusePView == null
            }
            assert (amReuseOffset >= endModOffset) :
                    "amReuseOffset=" + amReuseOffset + " < endModOffset=" + endModOffset; // NOI18N
            
            return true;
        }
        return false;
    }
    
    boolean createReplaceRepaintViews(boolean force) {
        if (!createViews(force)) {
            return false;
        }
        replaceRepaintViews();
        return true;
    }
    
    boolean createViews(boolean force) {
        if (startCreationOffset > matchOffset) {
            throw new IllegalStateException(
                    "startCreationOffset=" + startCreationOffset + " > matchOffset=" + matchOffset); // NOI18N
        }
        if (firstReplace != null && !createLocalViews) {
            throw new IllegalStateException("firstReplace != null && !createLocalViews"); // NOI18N
        }
        
        DocumentView docView = docReplace.view;
        if (docView.testRun != null) {
            docView.testValues = new Object[] {
                rebuildCause, createLocalViews,
                startCreationOffset, matchOffset, endCreationOffset,
                bmReuseOffset, bmReusePView, bmReuseLocalIndex,
                amReuseOffset, amReusePIndex, amReusePView, amReuseLocalIndex
            };
            docView.testRun.run();
        }

        Document doc = docView.getDocument();
        lineRoot = doc.getDefaultRootElement();
        lineIndex = lineRoot.getElementIndex(startCreationOffset);
        Element line = lineRoot.getElement(lineIndex);
        lineEndOffset = line.getEndOffset();
        lineForParagraphView = line;

        this.factoryStates = new FactoryState[viewFactories.length];
        for (int i = 0; i < viewFactories.length; i++) {
            FactoryState state = new FactoryState(viewFactories[i]);
            factoryStates[i] = state;
            // Note: if init() fails with exception state.finish() will be called.
            state.init(this, startCreationOffset, endCreationOffset, createLocalViews);
        }
        allReplaces = new ArrayList<ViewReplace<ParagraphView, EditorView>>(2);

        creationOffset = startCreationOffset;
        if (creationOffset < matchOffset) {
            // Create all new views
            while (createNextView()) {
                if (staleCreation && !force) {
                    ViewStats.incrementStaleViewCreations();
                    if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
                        ViewHierarchyImpl.BUILD_LOG.fine("STALE-CREATION notified => View Rebuild Terminated\n"); // NOI18N
                    }
                    return false;
                }
            }
        }

        if (localReplace != null && localReplace != firstReplace) {
            // The "unclosed" localReplace already added to allReplaces.
            int length = creationOffset - localReplace.view.getStartOffset();
            // Since localReplace != null when pView is not closed by NewlineView do setLength() here
            localReplace.view.setLength(length);
            localReplace = null;
        }

        // Check whether firstReplace replaces all views in the paragraph view with no added views.
        // In such case remove whole pView since it would otherwise stay empty which would be wrong.
        if (firstReplace != null && firstReplace.isMakingViewEmpty()) {
            // Remove whole pView
            docReplace.index--;
            docReplace.setRemoveCount(docReplace.getRemoveCount() + 1);
            firstReplace = null;
        }

        if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
            if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINEST)) {
                // Log original docView state
                // Use separate string builder to at least log original state if anything goes wrong.
                ViewHierarchyImpl.BUILD_LOG.finer("ViewBuilder: DocView-Original-Content:\n" + // NOI18N
                        docView.toStringDetailNeedsLock() + '\n'); // NOI18N
            }
            StringBuilder sb = new StringBuilder(200);
            sb.append("ViewBuilder.createViews(): in <").append(startCreationOffset); // NOI18N
            sb.append(",").append(creationOffset).append("> cause: ").append(rebuildCause).append("\n"); // NOI18N
            sb.append("Document:").append(doc).append('\n'); // NOI18N
            if (firstReplace != null) {
                sb.append("FirstReplace[").append(docReplace.index - 1).append("]: ").append(firstReplace); // NOI18N
            } else {
                sb.append("No-FirstReplace\n"); // NOI18N
            }
            sb.append("DocReplace: ").append(docReplace); // NOI18N
            sb.append("allReplaces:\n"); // NOI18N
            int digitCount = ArrayUtilities.digitCount(allReplaces.size());
            for (int i = 0; i < allReplaces.size(); i++) {
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
                sb.append(allReplaces.get(i));
            }
            sb.append("-------------END-OF-VIEW-REBUILD-------------\n"); // NOI18N
            ViewUtils.log(ViewHierarchyImpl.BUILD_LOG, sb.toString());
        }
        return true;
    }

    /**
     * Create next view.
     * @return true if the creation of views should continue or false if it should end.
     */
    boolean createNextView() {
        // Check if creation continues past current endCreationOffset
        if (creationOffset >= endCreationOffset) {
            endCreationOffset = matchOffset;
            for (int i = factoryStates.length - 1; i >= 0; i--) {
                FactoryState state = factoryStates[i];
                state.factory.continueCreation(creationOffset, endCreationOffset);
                
            }
        }

        // Go through factories from top one to first one and ask for view creation
        DocumentView docView = docReplace.view;
        int limitOffset = endCreationOffset;
        boolean forcedLimit = false;
        for (int i = factoryStates.length - 1; i >= 0; i--) {
            FactoryState state = factoryStates[i];
            int cmp = state.nextViewStartOffset - creationOffset;
            if (cmp < 0) { // Next view starting below
                state.updateNextViewStartOffset(creationOffset);
                cmp = state.nextViewStartOffset - creationOffset;
            }
            if (cmp == 0) { // Candidate for the next view
                // Create new view. Note that the limitOffset is only a suggestion.
                // Only the bottommost highlights-view-factory should always respect the the limitOffset.
                EditorView createdView = null;
                int createdViewEndOffset;
                if (createLocalViews) { // Regular views creation
                    // Prepare original view if possible
                    EditorView origView; // null means no view to reuse
                    int nextOrigViewOffset; // Integer.MAX_VALUE means next local view is not known
                    if (creationOffset >= endModOffset) { // Possible reusal above modified area
                        while (creationOffset > amReuseOffset) {
                            boolean reuseNextPView = true;
                            if (amReusePView.isChildrenNull()) { // Skip whole pView
                                amReuseOffset += amReusePView.getLength();
                            } else {
                                int viewCount = amReusePView.getViewCount();
                                while (amReuseLocalIndex < viewCount) {
                                    EditorView view = amReusePView.getEditorView(amReuseLocalIndex);
                                    amReuseOffset += view.getLength();
                                    amReuseLocalIndex++;
                                    if (amReuseOffset >= creationOffset) {
                                        reuseNextPView = (amReuseLocalIndex == viewCount);
                                        break;
                                    }
                                }
                            }
                            if (reuseNextPView) {
                                amReuseLocalIndex = 0; // means viewCount (children==null)
                                amReusePIndex++;
                                if (amReusePIndex < docView.getViewCount()) {
                                    amReusePView = docView.getParagraphView(amReusePIndex);
                                } else {
                                    amReusePView = null;
                                    amReuseOffset = Integer.MAX_VALUE;
                                }
                            }
                        }
                        // Assign origView and nextOrigViewOffset
                        if (creationOffset == amReuseOffset) { // Reuse if local views
                            int viewCount = amReusePView.getViewCount();
                            if (amReuseLocalIndex < viewCount) {
                                origView = amReusePView.getEditorView(amReuseLocalIndex);
                            } else { // 
                                origView = null;
                            }
                        } else { // amReuseOffset > creationOffset
                            origView = null;
                        }
                        nextOrigViewOffset = amReuseOffset;

                    } else if (creationOffset < modOffset) { // Possible reusal below modified area
                        while (creationOffset > bmReuseOffset) {
                            EditorView view = bmReusePView.getEditorView(bmReuseLocalIndex);
                            bmReuseOffset += view.getLength();
                            bmReuseLocalIndex++;
                        }
                        // Assign origView and nextOrigViewOffset
                        if (creationOffset == bmReuseOffset) {
                            EditorView view = bmReusePView.getEditorView(bmReuseLocalIndex);
                            bmReuseOffset += view.getLength();
                            bmReuseLocalIndex++;
                            if (bmReuseOffset <= modOffset) { // end of view below modOffset
                                origView = view;
                                nextOrigViewOffset = bmReuseOffset;
                            } else { // Cannot reuse since modOffset inside the view
                                origView = null;
                                nextOrigViewOffset = amReuseOffset;
                            }
                        } else { // bmReuseOffset > creationOffset
                            origView = null;
                            nextOrigViewOffset = (bmReuseOffset <= modOffset) ? bmReuseOffset : amReuseOffset;
                        }

                    } else { // No reusal
                        origView = null;
                        nextOrigViewOffset = Integer.MAX_VALUE;
                    }
                    
                    
                    createdView = state.factory.createView(creationOffset, limitOffset, forcedLimit, origView, nextOrigViewOffset);
                    if (createdView == null) { // Creation refused
                        createdViewEndOffset = -1; // Ignored; will not reach updateLine()
                    } else {
                        int viewLength = createdView.getLength();
                        createdViewEndOffset = creationOffset + viewLength;
                        assert (viewLength > 0) : "viewLength=" + viewLength + " < 0"; // NOI18N
                    }

                } else { // Do not create local views
                    // createdViewEndOffset may be -1 to signal that factory does not want to create view here.
                    createdViewEndOffset = state.factory.viewEndOffset(creationOffset, limitOffset, forcedLimit);
                }

                if (createdViewEndOffset == -1) { // View creation refused by this factory
                    // Rescan the factory at next offset since it may request another view creation
                    // within the area which may lower limitOffset
                    state.updateNextViewStartOffset(creationOffset + 1);
                    if (state.nextViewStartOffset <= limitOffset) {
                        forcedLimit = true;
                        limitOffset = state.nextViewStartOffset;
                    }
                    continue; // Continue with next factory to possibly create a view
                }

                updateLine(createdViewEndOffset);
                boolean eolView = (createdViewEndOffset == lineEndOffset);
                boolean inFirstReplace = (localReplace == firstReplace && firstReplace != null);
                // Make space for new views by replacing old ones.
                // When firstReplace is active then only local removals are done unless
                // a NewlineView gets created in which case the views till the end
                // of a firstReplace's view must be removed (they would have to be re-parented otherwise).
                // If firstReplace is not active then remove full paragraph views
                // (again to avoid re-parenting of local views to new paragraph views).
                if (eolView && inFirstReplace) { // Rest of views on first pagaraph view will be thrown away
                    // Ensure that local views till end of first paragraph view will be thrown away
                    if (!firstReplace.isRemovedTillEnd()) {
                        // Increase matchOffset by remaining views on firstReplace's view
                        int remainingLenOnParagraph = firstReplace.view.getLength() - 
                                firstReplace.view.getLocalOffset(firstReplace.removeEndIndex());
                        matchOffset += remainingLenOnParagraph;
                        firstReplace.removeTillEnd();
                    }
                }
                if (createdViewEndOffset > matchOffset) {
                    boolean matchOffsetValid = false;
                    if (inFirstReplace) { // Replacing in firstReplace
                        while (!firstReplace.isRemovedTillEnd()) {
                            // Use getLength() instead of getEndOffset() since for intra-line mods
                            // with modLength != 0 the views do not have updated offsets
                            matchOffset += localReplace.view.getEditorView(firstReplace.removeEndIndex()).getLength();
                            localReplace.setRemoveCount(localReplace.getRemoveCount() + 1);
                            // For eolView remove all till end; otherwise only until matchOffset is ok
                            if (createdViewEndOffset <= matchOffset) {
                                matchOffsetValid = true;
                                break;
                            }
                        }
                    }
                    if (!matchOffsetValid) {
                        while (!docReplace.isRemovedTillEnd()) {
                            matchOffset += docView.getParagraphView(docReplace.removeEndIndex()).getLength();
                            docReplace.setRemoveCount(docReplace.getRemoveCount() + 1);
                            if (createdViewEndOffset <= matchOffset) {
                                break;
                            }
                        }
                    }

                } else if (createdViewEndOffset == matchOffset) {
                    // Check for condition in ViewUpdatesTest.testInsertAndRemoveNewline()
                    // when backspace pressed on begining of an empty line then rebuilding
                    // could end up with a line not ended by NewlineView.
                    if (inFirstReplace && !eolView && localReplace.isRemovedTillEnd()) {
                        // Rebuild next paragraph
                        if (!docReplace.isRemovedTillEnd()) {
                            matchOffset += docView.getParagraphView(docReplace.removeEndIndex()).getLength();
                            docReplace.setRemoveCount(docReplace.getRemoveCount() + 1);
                        }
                    }
                }

                if (localReplace == null) { // Finished a paragraph view previously
                    Position startPos;
                    if (lineForParagraphView instanceof Position &&
                            creationOffset == lineForParagraphView.getStartOffset())
                    { // Reuse element as position
                        startPos = (Position) lineForParagraphView;
                    } else { // Create pos
                        try {
                            startPos = docView.getDocument().createPosition(creationOffset);
                        } catch (BadLocationException e) {
                            throw new IllegalStateException("Cannot create position at offset=" + creationOffset, e);
                        }
                    }
                    ParagraphView paragraphView = new ParagraphView(startPos);
                    docReplace.add(paragraphView);
                    localReplace = new ViewReplace<ParagraphView, EditorView>(paragraphView);
                    // pReplace.index = 0;   <= already set by constructor
                    if (createLocalViews) {
                        allReplaces.add(localReplace);
                    }
                }
                if (createLocalViews) {
                    localReplace.add(createdView);
                }

                if (eolView) {
                    // Init view's length except for first replace where it's updated by ParagraphViewChildren.replace()
                    if (localReplace != firstReplace) {
                        int length = createdViewEndOffset - localReplace.view.getStartOffset();
                        localReplace.view.setLength(length);
                    }
                    localReplace = null;
                    // Attempt to reuse line element as a start position for paragraph view
                    lineForParagraphView = (lineIndex + 1 < lineRoot.getElementCount())
                            ? lineRoot.getElement(lineIndex + 1)
                            : null;
                }

                creationOffset = createdViewEndOffset;
                // Continue creation until matchOffset is reached
                // but also in case when it was reached but the created views do not
                // finish a paragraph view (pReplace is non-null and it's not a first-replace
                // where it's allowed to finish without newline-view creation).
                return (creationOffset < matchOffset);

            } else { // cmp > 0 => next view starting somewhere above last view's end offset
                // Remember the nextViewStartOffset as a limit offset for factories
                // that lay below this factory
                if (state.nextViewStartOffset <= limitOffset) {
                    forcedLimit = true;
                    limitOffset = state.nextViewStartOffset;
                }
            }
        }
        // The code should not get there since the highlights-view-factory (at index 0)
        // should always provide a view.
        throw new IllegalStateException("No factory returned view for offset=" + creationOffset);
    }
    
    private void transcribe(ParagraphView origPView, ParagraphView newPView) {
        float origWidth = origPView.getWidth();
        newPView.setWidth(origWidth);
        float origHeight = origPView.getHeight();
        newPView.setHeight(origHeight);
    }

    private void replaceRepaintViews() {
        // Compute repaint region as area of views being removed
        DocumentView docView = docReplace.view;
        TextLayoutCache tlCache = docView.op.getTextLayoutCache();
        final JTextComponent textComponent = docView.getTextComponent();
        assert (textComponent != null) : "Null textComponent"; // NOI18N
        // Check firstReplace (in PV at (docReplace.index - 1))
        boolean firstReplaceValid = firstReplace != null && firstReplace.isChanged();
        if (firstReplaceValid) {
            ParagraphView pView = firstReplace.view;
            if (insertAtPViewStart) {
                pView.setStartPosition(ViewUtils.createPosition(docView.getDocument(), modOffset));
            }
            if (!pView.isChildrenValid()) {
                // Check that rebuild fixes currently invalid children
                int invalidLOffset = pView.children.getStartInvalidChildrenLocalOffset();
                assert pView.children.startOffset(firstReplace.index) <= invalidLOffset :
                        "rebuildLOffset=" + pView.children.startOffset(firstReplace.index) + // NOI18N
                        " > invalidLOffset=" + invalidLOffset; // NOI18N
                int invalidEndLOffset = pView.children.getEndInvalidChildrenLocalOffset();
                assert pView.children.startOffset(firstReplace.removeEndIndex()) >= invalidEndLOffset :
                        "rebuildEndLOffset=" + pView.children.startOffset(firstReplace.removeEndIndex()) + // NOI18N
                        " < invalidEndIndex=" + invalidEndLOffset; // NOI18N
                pView.markChildrenValid();
            }
            pView.replace(firstReplace.index, firstReplace.getRemoveCount(), firstReplace.addedViews());
            tlCache.activate(pView);
        }
        
        // Possibly retain vertical spans from original views
        // Algorithm first goes from first replaced pView checking whether new pView has the same offset span
        // (and if it does then its width and height are copied from original pView)
        // until first pView with different span is found.
        // If there are any changed pView(s) the algorithm goes from last pView back doing the same thing.
        // The remaining really "new" pView(s) are assigned default row height
        // and their y-span is reported as a changed area into the view hierarchy listener.
        // Since the pViews are based on swing positions it make sense to go from the end back
        // since for document modifications the original position-based pViews will be shifted forward/back
        // accordingly.
        List<ParagraphView> addedPViews = docReplace.added();
        int addedCount = docReplace.addedSize();
        int removeCount = docReplace.getRemoveCount(); // Number of pViews to be removed from docView
        int index = docReplace.index;
        // Area between i0 and i1 will be set to default row height
        int i0 = 0; // first changed pView
        int i1 = addedCount; // next after last changed view among added views
        int i1Orig = removeCount; // i1 in current pViews
        int commonCount = Math.min(addedCount, removeCount);
        if (commonCount > 0) {
            ParagraphView origPView = docView.getParagraphView(index);
            ParagraphView newPView = addedPViews.get(0);
            if (origPView.getStartOffset() == newPView.getStartOffset()) {
                while (true) {
                    if (origPView.getLength() != newPView.getLength()) {
                        break;
                    }
                    transcribe(origPView, newPView);
                    i0++;
                    if (i0 >= commonCount) {
                        break;
                    }
                    origPView = docView.getParagraphView(index + i0);
                    newPView = addedPViews.get(i0);
                }
            }
            int endCount = commonCount - i0;
            if (endCount > 0) {
                int i = 1; // subtract index
                origPView = docView.getParagraphView(index + removeCount - i);
                newPView = addedPViews.get(addedCount - i);
                if (origPView.getEndOffset() == newPView.getEndOffset()) { // could in fact compare start offsets too
                    while (true) {
                        if (origPView.getLength() != newPView.getLength()) {
                            i--;
                            break;
                        }
                        transcribe(origPView, newPView);
                        if (i >= commonCount - i0) {
                            break;
                        }
                        i++;
                        origPView = docView.getParagraphView(index + removeCount - i);
                        newPView = addedPViews.get(addedCount - i);
                    }
                    i1 = addedCount - i;
                    i1Orig = removeCount - i;
                }
            }
        }
        // Fill the rest with default row height
        if (i0 != i1) {
            float defaultRowHeight = docView.op.getDefaultRowHeight();
            for (int i = i0; i < i1; i++) {
                ParagraphView addedPView = addedPViews.get(i);
                addedPView.setHeight(defaultRowHeight);
                // Do not set initial width (let it be computed) since it also requires
                // possible notifying of width change. And there also may be line wrapping
                // involved so the estimated width would not comply.
            }
        }
        if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
            ViewHierarchyImpl.BUILD_LOG.fine("Non-Retained Views: " + // NOI18N
                    ((i0 != i1) ? "<" + i0 + "," + i1 + ">" : "NONE") + " of " + addedCount + " new pViews\n"); // NOI18N
        }
        
        
        // New paragraph views are currently not measured (they use spans
        // that were retained from old views or they use defaults).
        ViewHierarchyChange change = docView.validChange();
        // When change is local inside pView then report change till end of pView
        // since the views that follow modification will shift very likely.
        // It's important that the firstReplace replace was already processed
        // so the firstReplace.view has correct end offset.
        int changeEndOffset = (firstReplaceValid && !docReplace.isChanged())
                ? firstReplace.view.getEndOffset()
                : matchOffset; // replace ends at pView boundary
        change.addChange(startCreationOffset, changeEndOffset);

        // firstReplace generally does not affect y although it can but it's determined later
        double startY;
        double endY;
        double deltaY;
        if (docReplace.isChanged()) {
            boolean changeY = false;
            double y0 = 0d;
            double y1 = 0d;
            if (removeCount != addedCount || i0 != i1) {
                changeY = true;
                // Do actual replace AFTER determining y coordinates in order to get original y values
                // Children may be uninitialized (subsequent docView.replace() will init them.
                y0 = (docView.children != null) ? docView.getY(index + i0) : 0d;
                y1 = (i0 != i1Orig) ? docView.getY(index + i1Orig) : y0;
            }
            // Replace views in docView (includes possible call to notifyHeightChange())
            double[] startEndDeltaY = docView.replaceViews(
                    docReplace.index, docReplace.getRemoveCount(), docReplace.addedViews());
            startY = startEndDeltaY[0];
            endY = startEndDeltaY[1];
            deltaY = startEndDeltaY[2];
            int allReplacesSize = allReplaces.size();
            if (forceCreateLocalViews) { // Ensure there will be enough space in TLCache
                // Respect any previously set limit
                if (allReplacesSize > tlCache.capacity()) {
                    tlCache.setCapacityOrDefault(allReplacesSize);
                }
            }
            // Replace contents of each added paragraph view (if the contents are built too).
            for (int pIndex = 0; pIndex < allReplacesSize; pIndex++) {
                ViewReplace<ParagraphView, EditorView> replace = allReplaces.get(pIndex);
                if (replace.isChanged()) {
                    ParagraphView pView = replace.view;
                    pView.replace(replace.index, replace.getRemoveCount(), replace.addedViews());
                    pView.markChildrenValid();
                    tlCache.activate(pView);
                }
            }
            // Add y change if necessary
            if (changeY) {
                boolean realChange = true;
                if (deltaY == 0d) { // Attempt extra check
                    // There might be a modification in a single line and since
                    // newPView.getLength() != origPView.getLength() the line was not transcribed.
                    // However if (i1 - i0) == 1 and deltaY == 0d then there was not a physical change in fact.
                    if (i1 - i0 == 1) {
                        realChange = false;
                    }
                }
                if (realChange) {
                    change.addChangeY(y0, y1, deltaY);
                }
            }
        } else { // docReplace empty
            int startIndex = firstReplaceValid ? docReplace.index - 1 : docReplace.index;
            startY = docView.getY(startIndex);
            endY = docView.getY(docReplace.index);
            deltaY = 0d;
        }
        // Update end offset of the docView since it might extend beyond boundary computed
        // before view building process.
        docView.updateEndOffset();
        
        // For accurate span force computation of text layouts
        Rectangle2D.Double docViewRect = docView.getAllocationCopy();
        if (docView.op.isAccurateSpan()) { // All pViews should already have children
            int pIndex = docReplace.index;
            int endIndex = docReplace.addEndIndex();
            if (firstReplaceValid) { // Include pView of firstReplace too
                pIndex--;
            }
            for (; pIndex < endIndex; pIndex++) {
                ParagraphView pView = docView.getParagraphView(pIndex);
                Shape pAlloc = docView.getChildAllocation(pIndex, docViewRect);
                if (pView.isChildrenNull()) {
                    LOG.info("Null children for accurate span at pIndex=" + // NOI18N
                        pIndex + "\nviewBuilder:\n" + this); // NOI18N
//                        "\n\ndocView:\n" + docView.toStringDetailNeedsLock()); // NOI18N
                    break;
                }
                pView.checkLayoutUpdate(pIndex, ViewUtils.shapeAsRect(pAlloc));
            }
        }
        
        // Schedule repaints based on current docView allocation.
        // For valid firstReplace the current impl repaints whole line.
        // When deltaY > 0 it must repaint the newly added area too
        docViewRect.y = startY;
        double endRepaintY = (deltaY != 0d) 
                ? docViewRect.getMaxY() + Math.max(0d, deltaY)
                : endY;
        docViewRect.height = endRepaintY - docViewRect.y;
        docView.op.extendToVisibleWidth(docViewRect);
        docView.op.notifyRepaint(docViewRect);
    }
    
    void finish() {
        // Finish factories
        if (factoryStates != null) {
            for (FactoryState factoryState : factoryStates) {
                if (factoryState != null) { // Prevents NPE in case of early failure of state.init()
                    factoryState.finish();
                }
            }
        }
    }

    /**
     * Update line so that it "contains" the offset or the <code>offset == lineEndOffset</code>
     * @param offset
     */
    void updateLine(int offset) {
        while (offset > lineEndOffset) {
            lineIndex++;
            Element line = lineRoot.getElement(lineIndex);
            lineEndOffset = line.getEndOffset();
        }
    }
    
    void notifyStaleCreation() {
        staleCreation = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("-------- ViewBuilder dump -------\n"). // NOI18N
                append("docLen=").append(docReplace.view.getDocument().getLength()).append('\n'). // NOI18N
                append("start/endCreationOffset:<").append(startCreationOffset). // NOI18N
                append(",").append(endCreationOffset).append(">\n").
                append("creationOffset=").append(creationOffset).append('\n').
                append("matchOffset=").append(matchOffset).append('\n').  // NOI18N
                append("modOffset=").append(modOffset).append('\n').  // NOI18N
                append("endModOffset=").append(endModOffset).append('\n').  // NOI18N
                append("bmReuseOffset=").append(bmReuseOffset).append('\n').  // NOI18N
                append("bmReusePView=").append(bmReusePView).append('\n').  // NOI18N
                append("bmReuseLocalIndex=").append(bmReuseLocalIndex).append('\n').  // NOI18N
                append("amReuseOffset=").append(amReuseOffset).append('\n').  // NOI18N
                append("amReusePIndex=").append(amReusePIndex).append('\n').  // NOI18N
                append("amReusePView=").append(amReusePView).append('\n').  // NOI18N
                append("amReuseLocalIndex=").append(amReuseLocalIndex).append('\n').  // NOI18N
                append("docView <").append(docReplace.view.getStartOffset()).append(','). // NOI18N
                append(docReplace.view.getEndOffset()).append(">\n"). // NOI18N
                append("lineIndex=").append(lineIndex).append('\n'). // NOI18N
                append("lineEndOffset=").append(lineEndOffset).append('\n').  // NOI18N
                append("firstReplace=").append((firstReplace != null) ? firstReplace : "<NULL>\n").  // NOI18N
                append("docReplace=").append(docReplace).  // NOI18N
                append("localReplace=").append((localReplace != null) ? localReplace : "<NULL>\n").  // NOI18N
                append("allocation=").append(allReplaces).
                append("\n-------- End of ViewBuilder dump -------\n");  // NOI18N
        return sb.toString();
    }
    
    private static final class FactoryState {

        final EditorViewFactory factory;

        int nextViewStartOffset;

        FactoryState(EditorViewFactory factory) {
            this.factory = factory;
        }

        void init(ViewBuilder viewBuilder, int startOffset, int matchOffset, boolean createViews) {
            factory.setViewBuilder(viewBuilder);
            factory.restart(startOffset, matchOffset, createViews);
            updateNextViewStartOffset(startOffset);
        }

        void updateNextViewStartOffset(int offset) {
            nextViewStartOffset = factory.nextViewStartOffset(offset);
            if (nextViewStartOffset < offset) {
                throw new IllegalStateException("Editor view factory " + factory + // NOI18N
                        " returned nextViewStartOffset=" + nextViewStartOffset + // NOI18N
                        " < offset=" + offset); // NOI18N
            }
        }

        void finish() {
            factory.finishCreation();
            factory.setViewBuilder(null);
        }

    }

}
