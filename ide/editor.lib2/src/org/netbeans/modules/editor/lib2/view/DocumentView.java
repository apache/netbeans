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

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.TextUI;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.TabExpander;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.lib.editor.util.PriorityMutex;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.SplitOffsetHighlightsSequence;

/**
 * View representing the whole document.
 * <br>
 * It consists of individual paragraph views that typically map to line elements
 * but e.g. code folding may cause multiple line elements correspond to one line view.
 * 
 * @author Miloslav Metelka
 */

public final class DocumentView extends EditorView implements EditorView.Parent {

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentView.class.getName());

    // True to log real source chars
    // -J-Dorg.netbeans.editor.log.source.text=true
    static final boolean LOG_SOURCE_TEXT = Boolean.getBoolean("org.netbeans.editor.log.source.text"); // NOI18N

    // True to disable extra virtual space of 1/3 of height of the visible viewport
    // -J-Dorg.netbeans.editor.disable.end.virtual.space=true
    static final boolean DISABLE_END_VIRTUAL_SPACE =
            Boolean.getBoolean("org.netbeans.editor.disable.end.virtual.space"); // NOI18N

    /**
     * Text component's client property for the mutex doing synchronization
     * for view's operation. The mutex is physically the same like the one
     * for the fold hierarchy otherwise deadlocks could occur.
     */
    private static final String MUTEX_CLIENT_PROPERTY = "foldHierarchyMutex"; //NOI18N

    /**
     * Component's client property that contains swing position - start of document's area
     * to be displayed by the view.
     * Value of the property is only examined at time of view.setParent().
     */
    static final String START_POSITION_PROPERTY = "document-view-start-position";

    /**
     * Component's client property that contains swing position - end of document's area
     * to be displayed by the view.
     * Value of the property is only examined at time of view.setParent().
     */
    static final String END_POSITION_PROPERTY = "document-view-end-position";

    /**
     * Component's client property that defines whether accurate width and height should be computed
     * by the view or whether the view can estimate its width and improve the estimated
     * upon rendering of the concrete region.
     * Value of the property is only examined at time of view.setParent().
     */
    static final String ACCURATE_SPAN_PROPERTY = "document-view-accurate-span";
    
    /**
     * Component's client property (containing Integer) that defines by how many points
     * the default font size should be increased/decreased.
     */
    static final String TEXT_ZOOM_PROPERTY = "text-zoom";

    public static DocumentView get(JTextComponent component) {
        TextUI textUI = component.getUI();
        if (textUI != null) {
            View rootView = textUI.getRootView(component);
            if (rootView != null && rootView.getViewCount() > 0) {
                View view = rootView.getView(0);
                if (view instanceof DocumentView) {
                    return (DocumentView)view;
                }
            }
        }
        return null;
    }

    static DocumentView get(View view) {
        while (view != null && !(view instanceof DocumentView)) {
            view = view.getParent();
        }
        return (DocumentView) view;
    }

    static {
        EditorViewFactory.registerFactory(new HighlightsViewFactory.HighlightsFactory());
    }
    
    final DocumentViewOp op;

    private PriorityMutex pMutex;
    
    final DocumentViewChildren children;

    private JTextComponent textComponent;
    
    final ViewRenderContext viewRenderContext;
    
    /**
     * Start offset of the document view after last processed modification.
     * <br>
     * This is used during updating of the document view by document modification.
     * For undo of removal the position of first paragraph may restore inside the inserted area
     * and holding integer offset allows to know the original start offset.
     */
    private int startOffset;

    /**
     * Start offset of the document view.
     * <br>
     * This is used during updating of the document view by document modification.
     * For undo of removal the position of last paragraph may restore inside the inserted area
     * and holding integer offset allows to know the original end offset of the document view.
     */
    private int endOffset;
    
    /**
     * Preferred width of the whole view is set by updatePreferredWidth().
     */
    private float preferredWidth;
    
    private float preferredHeight;

    /**
     * Current allocation of document view.
     */
    private Rectangle2D.Float allocation = new Rectangle2D.Float();
    
    private final TabExpander tabExpander;
    
    /**
     * Change that occurred in the view hierarchy.
     */
    private ViewHierarchyChange change;

    private Map<TextLayout,String> textLayoutVerifier;
    
    static Runnable testRun; // Used for testing - controlling proper values in ViewBuilder etc.
    static Object[] testValues; // Complete state to be tested (depends on situation)
    
    public DocumentView(Element elem) {
        super(elem);
        assert (elem != null) : "Expecting non-null element"; // NOI18N
        this.op = new DocumentViewOp(this);
        this.tabExpander = new EditorTabExpander(this);
        this.children = new DocumentViewChildren(1);
        this.viewRenderContext = new ViewRenderContext(this);
    }

    /**
     * Run transaction over locked view hierarchy.
     * The document must be read-locked prior calling this method.
     * 
     * @param r non-null runnable to be executed over locked view hierarchy.
     */
    public void runTransaction(Runnable r) {
        if (lock()) {
            try {
                r.run();
            } finally {
                unlock();
            }
        } else { // If no mutex present run without mutex (not running at all would be more serious)
            r.run();
        }
    }
    
    public void runReadLockTransaction(final Runnable r) {
        getDocument().render(new Runnable() {
            @Override
            public void run() {
                runTransaction(r);
            }
        });
    }
    
    @Override
    public float getPreferredSpan(int axis) {
        // Since this may be called e.g. from BasicTextUI.getPreferredSize()
        // this method needs to acquire mutex
        if (lock()) {
            try {
                checkDocumentLockedIfLogging(); // Should only be called with read-locked document
                op.checkViewsInited();
                // Ensure the width and height are updated before unlock() gets called (which is too late)
                op.checkRealSpanChange();
                if (!op.isChildrenValid()) {
                    return 1f; // Return 1f until parent and etc. gets initialized
                }
                float span;
                if (axis == View.X_AXIS) {
                    span = preferredWidth;
                } else { // Y_AXIS
                    span = preferredHeight + op.getExtraVirtualHeight();
                }
                return span;
            } finally {
                unlock();
            }
        } else {
            return 1f;
        }
    }

    boolean lock() {
        if (pMutex != null) {
            pMutex.lock();
            boolean success = false;
            try {
                op.lockCheck();
                success = true;
            } finally {
                if (!success) {
                    pMutex.unlock();
                }
            }
            return success;
        }
        return false;
    }
    
    void unlock() {
        try {
            op.unlockCheck();
            checkFireEvent();
        } finally {
            pMutex.unlock();
        }
    }

    @Override
    public Document getDocument() {
        return getElement().getDocument();
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }
    
    void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }
    
    @Override
    public int getEndOffset() {
        return endOffset;
    }
    
    void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }
    
    void updateEndOffset() {
        int viewCount = getViewCount();
        int endOffset = (viewCount > 0)
                ? getParagraphView(viewCount - 1).getEndOffset()
                : getStartOffset();
        setEndOffset(endOffset);
    }
    
    @Override
    public int getViewCount() {
        return children.size();
    }

    @Override
    public View getView(int index) {
        checkDocumentLockedIfLogging();
        checkMutexAcquiredIfLogging();
        return (index < children.size()) ? children.get(index) : null;
    }

    public ParagraphView getParagraphView(int index) {
        if (index >= getViewCount()) {
            throw new IndexOutOfBoundsException("View index=" + index + " >= " + getViewCount()); // NOI18N
        }
        return children.get(index);
    }
    
    void ensureLayoutValidForInitedChildren() { // For tests to ensure layout update for all pViews with valid children
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                op.checkViewsInited(); // First check whether pViews are valid
                children.ensureLayoutValidForInitedChildren(DocumentView.this);
            }
        });
    }

    void ensureAllParagraphsChildrenAndLayoutValid() { // For tests to initialize all pViews
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                op.checkViewsInited(); // First check whether pViews are valid
                children.ensureParagraphsChildrenAndLayoutValid(DocumentView.this, 0, getViewCount(), 0, 0);
            }
        });

    }

    Shape getChildAllocation(int index) {
        return getChildAllocation(index, getAllocation());
    }

    @Override
    public Shape getChildAllocation(int index, Shape alloc) {
        return children.getChildAllocation(this, index, alloc);
    }

    @Override
    public int getViewIndex(int offset, Position.Bias b) {
        if (b == Position.Bias.Backward) {
            offset--;
        }
        return getViewIndex(offset);
    }

    public int getViewIndex(int offset) {
        return children.viewIndexFirstByStartOffset(offset, 0);
    }
    
    public int getViewIndex(double y) {
        if (op.isActive()) {
            Shape alloc = getAllocation();
            return children.viewIndexAtY(y, alloc);
        }
        return -1;
    }

    public double getY(int pViewIndex) {
        return children.getY(pViewIndex);
    }

    @Override
    public int getViewEndOffset(int rawChildEndOffset) {
        throw new IllegalStateException("Raw end offsets storage not maintained for DocumentView."); // NOI18N
    }

    @Override
    public void replace(int index, int length, View[] views) {
        replaceViews(index, length, views);
    }

    ViewHierarchyChange validChange() {
        if (change == null) {
            change = new ViewHierarchyChange();
        }
        return change;
    }

    void checkFireEvent() {
        if (change != null) {
            op.viewHierarchyImpl().fireChange(change);
            change = null;
        }
    }

    public TabExpander getTabExpander() {
        return tabExpander;
    }
    
    double[] replaceViews(int index, int length, View[] views) {
        return children.replace(this, index, length, views);
    }
    
    @Override
    public int getRawEndOffset() {
        return -1;
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        throw new IllegalStateException("Unexpected"); // NOI18N
    }

    @Override
    public void setSize(float width, float height) {
        // This method is called outside of VH lock
        if (width != allocation.width) {
            op.markAllocationWidthChange(width);
            // Update visible dimension early to avoid a visible "double resizing"
            if (SwingUtilities.isEventDispatchThread()) {
                op.updateVisibleDimension(false);
            }
        }
        if (height != allocation.height) {
            op.markAllocationHeightChange(height);
        }
    }
    
    void setAllocationWidth(float width) {
        allocation.width = width;
    }
    
    void setAllocationHeight(float height) {
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DV.setAllocationHeight(): " + // NOI18N
                    allocation.height + " to " + height + '\n'); // NOI18N
        }
        allocation.height = height;
        updateBaseY();
    }

    /**
     * Get current allocation of the document view by using size from last call
     * to {@link #setSize(float, float)}.
     * <br>
     * Returned instance may not be mutated (use getAllocationMutable()).
     *
     * @return current allocation of document view.
     */
    public Rectangle2D getAllocation() {
        return allocation;
    }
    
    /**
     * Get mutable document view allocation rectangle.
     */
    public Rectangle2D.Double getAllocationCopy() {
        return new Rectangle2D.Double(0d, 0d, allocation.getWidth(), allocation.getHeight());
    }

    /**
     * Get paint highlights for the given view.
     *
     * @param view view for which the highlights are obtained.
     * @param shift shift inside the view where the returned highlights should start.
     * @return highlights sequence containing the merged highlights of the view and painting highlights.
     */
    public SplitOffsetHighlightsSequence getPaintHighlights(EditorView view, int shift) {
        return children.getPaintHighlights(view, shift);
    }

    @Override
    public void preferenceChanged(View childView, boolean widthChange, boolean heightChange) {
        if (childView == null) { // This docView
            if (widthChange) {
                op.notifyWidthChange();
            }
            if (heightChange) {
                op.notifyHeightChange();
            }
        } else { // pViews should not notify here (they should make appropriate changes by themselves)
        }
    }
    
    void superPreferenceChanged(boolean widthChange, boolean heightChange) {
        super.preferenceChanged(this, widthChange, heightChange);
    }

    boolean updatePreferredWidth() {
        float newWidth = children.width();
        if (newWidth != preferredWidth) {
            preferredWidth = newWidth;
            return true;
        }
        return false;
    }

    boolean updatePreferredHeight() {
        float newHeight = children.height();
        if (newHeight != preferredHeight) {
            preferredHeight = newHeight;
            updateBaseY();
            return true;
        }
        return false;
    }

    void updateBaseY() {
        float baseY = op.asTextField
                ? (float) Math.floor((allocation.height - preferredHeight) / 2.0f)
                : 0f;
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DV.updateBaseY(): " + // NOI18N
                    children.getBaseY() + " to " + baseY + ", asTextField=" + op.asTextField + '\n'); // NOI18N
        }
        children.setBaseY(baseY);
    }
            
    void markChildrenLayoutInvalid() {
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG,
                    "Component width differs => children.recomputeChildrenWidths()\n"); // NOI18N
        }
        children.markChildrenLayoutInvalid();
    }

    @Override
    public ViewRenderContext getViewRenderContext() {
        return viewRenderContext;
    }

    public FontRenderContext getFontRenderContext() {
        return op.getFontRenderContext();
    }

    void offsetRepaint(int startOffset, int endOffset) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging(); // Should only be called with read-locked document
                if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINE)) {
                    ViewHierarchyImpl.REPAINT_LOG.fine("OFFSET-REPAINT: <" + startOffset + "," + endOffset + ">\n");
                }
                if (op.isActive() && startOffset < endOffset && getViewCount() > 0) {
                    Rectangle2D repaintRect;
                    Rectangle2D.Double docViewRect = getAllocationCopy();
                    int pIndex = getViewIndex(startOffset);
                    ParagraphView pView = getParagraphView(pIndex);
                    int pViewEndOffset = pView.getEndOffset();
                    if (endOffset <= pViewEndOffset) {
                        Shape pAlloc = getChildAllocation(pIndex, docViewRect);
                        if (pView.children != null) { // Do local repaint
                            if (pView.checkLayoutUpdate(pIndex, pAlloc)) {
                                pAlloc = getChildAllocation(pIndex, docViewRect);
                            }
                            Shape s = pView.modelToViewChecked(startOffset, Bias.Forward,
                                    endOffset, Bias.Forward, pAlloc);
                            // In case it ends right at the paragraph's end extend to visible width
                            if (endOffset == pViewEndOffset) {
                                Rectangle2D.Double r = ViewUtils.shape2Bounds(s);
                                op.extendToVisibleWidth(r);
                                repaintRect = r;
                            } else {
                                repaintRect = ViewUtils.shapeAsRect(s);
                            }

                        } else { // Repaint single paragraph
                            Rectangle2D.Double r = ViewUtils.shape2Bounds(pAlloc);
                            op.extendToVisibleWidth(r);
                            repaintRect = r;
                        }
                    } else { // Spans paragraphs
                        docViewRect.y = getY(pIndex);
                        int endIndex = getViewIndex(endOffset) + 1;
                        docViewRect.height = getY(endIndex) - docViewRect.y;
                        op.extendToVisibleWidth(docViewRect);
                        repaintRect = docViewRect;
                    }
                    if (repaintRect != null) {
                        op.notifyRepaint(repaintRect);
                    }
                }
            } finally {
                unlock();
            }
        }
        
    }
    
    @Override
    public void setParent(final View parent) {
        if (parent != null) {
            Container container = parent.getContainer();
            assert (container != null) : "Container is null"; // NOI18N
            assert (container instanceof JTextComponent) : "Container not JTextComponent"; // NOI18N
            final JTextComponent tc = (JTextComponent) container;
            pMutex = (PriorityMutex) tc.getClientProperty(MUTEX_CLIENT_PROPERTY);
            if (pMutex == null) {
                pMutex = new PriorityMutex();
                tc.putClientProperty(MUTEX_CLIENT_PROPERTY, pMutex);
            }

            runReadLockTransaction(new Runnable() {
                @Override
                public void run() {
                    DocumentView.super.setParent(parent);
                    textComponent = tc;
                    op.parentViewSet();
                    updateStartEndOffsets();
                }
            });

        } else { // Setting null parent
            // Set the textComponent to null under mutex
            // so that children suddenly don't see a null textComponent
            runReadLockTransaction(new Runnable() {
                @Override
                public void run() {
                    if (textComponent != null) {
                        op.parentCleared();
                        textComponent = null; // View services stop working and propagating to children
                    }
                    DocumentView.super.setParent(null);
                }
            });
        }
    }
    
    Position getExtraStartPosition() {
        return (Position) textComponent.getClientProperty(START_POSITION_PROPERTY);
    }
    
    Position getExtraEndPosition() {
        return (Position) textComponent.getClientProperty(END_POSITION_PROPERTY);
    }
    
    void updateStartEndOffsets() {
        Position startPos = getExtraStartPosition();
        Position endPos = getExtraEndPosition();
        Document doc = getDocument();
        startOffset = (startPos != null) ? startPos.getOffset() : 0;
        Position docEndPos;
        endOffset = Math.max(startOffset,
                (endPos != null)
                    ? endPos.getOffset()
                    : ((doc != null && (docEndPos = doc.getEndPosition()) != null)
                        ? docEndPos.getOffset()
                        : 0));
    }
    
    @Override
    public String getToolTipTextChecked(double x, double y, Shape alloc) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    return children.getToolTipTextChecked(this, x, y, alloc);
                }
            } finally {
                unlock();
            }
        }
        return null;
    }

    @Override
    public JComponent getToolTip(double x, double y, Shape alloc) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    return children.getToolTip(this, x, y, alloc);
                }
            } finally {
                unlock();
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    op.updateFontRenderContext(g, true); // Includes setting of rendering hints
                    children.paint(this, g, alloc, clipBounds);
                }
            } finally {
                unlock();
            }
        }
    }
    
    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Bias bias) {
        if (lock()) {
            try {
                return modelToViewNeedsLock(offset, alloc, bias);
            } finally {
                unlock();
            }
        }
        return null;
    }

    public Shape modelToViewNeedsLock(int offset, Shape alloc, Bias bias) {
        Rectangle2D.Double rect = ViewUtils.shape2Bounds(alloc);
        Shape retShape = null;
        checkDocumentLockedIfLogging();
        op.checkViewsInited();
        if (op.isActive()) {
            retShape = children.modelToViewChecked(this, offset, alloc, bias);
        } else {
            // Not active but attempt to find at least a reasonable y
            // The existing line views may not be updated for a longer time
            // but the binary search should find something and end in finite time.
            int index = getViewIndex(offset); // Must work without children inited
            if (index >= 0) {
                rect.y = getY(index); // Must work without children inited
                // Let the height to possibly be set to default line height later
            }
        }
        if (retShape == null) {
            // Attempt to just return height of line since otherwise e.g. caret
            // would have height of the whole doc which is undesirable.
            float defaultRowHeight = op.getDefaultRowHeight();
            if (defaultRowHeight > 0f) {
                rect.height = defaultRowHeight;
            }
            retShape = rect;
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "modelToView(" + offset + // NOI18N
                    ")=" + retShape + "\n"); // NOI18N
        }
        return retShape;
    }

    public double modelToY(int offset) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                return modelToYNeedsLock(offset);
            } finally {
                unlock();
            }
        }
        return children.getBaseY();
    }
    
    public double modelToYNeedsLock(int offset) {
        double retY = children.getBaseY();
        op.checkViewsInited();
        if (op.isActive()) {
            int index = getViewIndex(offset);
            if (index >= 0) {
                retY = getY(index);
            }
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "modelToY(" + offset + ")=" + retY + "\n"); // NOI18N
        }
        return retY;
    }
    
    public double[] modelToYNeedsLock(int[] offsets) {
        double[] retYs = new double[offsets.length];
        op.checkViewsInited();
        if (op.isActive()) { // Otherwise leave 0d for all retYs
            if (offsets.length > 0) {
                // Can in fact assume lastOffset == 0 corresponds to retY == 0d even if the view hierarchy
                // covers only portion of document since offset == 0 should be covered and it falls into first pView.
                int lastOffset = 0;
                int lastIndex = 0;
                double lastY = children.getBaseY();
                for (int i = 0; i < offsets.length; i++) {
                    int offset = offsets[i];
                    double y;
                    if (offset == lastOffset) {
                        y = lastY;
                    } else {
                        int startIndex = (offset > lastOffset) ? lastIndex : 0;
                        int index = children.viewIndexFirstByStartOffset(offset, startIndex);
                        y = getY(index);
                    }
                    retYs[i] = y;
                }
            }
        }
        return retYs;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        if (lock()) {
            try {
                return viewToModelNeedsLock(x, y, alloc, biasReturn);
            } finally {
                unlock();
            }
        }
        return 0;
    }
    
    public int viewToModelNeedsLock(double x, double y, Shape alloc, Bias[] biasReturn) {
        int retOffset = 0;
        checkDocumentLockedIfLogging();
        op.checkViewsInited();
        if (op.isActive()) {
            retOffset = children.viewToModelChecked(this, x, y, alloc, biasReturn);
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "viewToModel: [x,y]=" + x + "," + y + // NOI18N
                    " => " + retOffset + "\n"); // NOI18N
        }
        return retOffset;
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc,
            int direction, Bias[] biasRet)
    {
        int retOffset = offset;
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    switch (direction) {
                        case SwingConstants.EAST:
                            if (offset == -1) {
                                retOffset = getEndOffset() - 1;
                                break;
                            }
                            // Pass to SwingConstants.WEST
                        case SwingConstants.WEST:
                            if (offset == -1) {
                                retOffset = getStartOffset();
                            } else {
                                retOffset = children.getNextVisualPositionX(this, offset, bias, alloc,
                                        direction == SwingConstants.EAST, biasRet);
                            }
                            break;
                        case SwingConstants.NORTH:
                            if (offset == -1) {
                                retOffset = getEndOffset() - 1;
                                break;
                            }
                            // Pass to SwingConstants.SOUTH
                        case SwingConstants.SOUTH:
                            if (offset == -1) {
                                retOffset = getStartOffset();
                            } else {
                                retOffset = children.getNextVisualPositionY(this, offset, bias, alloc,
                                        direction == SwingConstants.SOUTH, biasRet);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Bad direction " + direction); // NOI18N
                    }
                }
            } finally {
                unlock();
            }
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "nextVisualPosition(" + offset + "," + // NOI18N
                    ViewUtils.toStringDirection(direction) + ")=" + retOffset + "\n"); // NOI18N
        }
        return retOffset;
    }
    
    /**
     * It should be called with +1 once it's detected that there's a lengthy atomic edit
     * in progress and with -1 when such edit gets finished.
     * @param delta +1 or -1 when entering/leaving lengthy atomic edit.
     */
    public void updateLengthyAtomicEdit(int delta) {
        op.updateLengthyAtomicEdit(delta);
    }

    @Override
    public void insertUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        // Do nothing here - see ViewUpdates constructor
    }

    @Override
    public void removeUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        // Do nothing here - see ViewUpdates constructor
    }

    @Override
    public void changedUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        // Do nothing here - see ViewUpdates constructor
    }

    JTextComponent getTextComponent() {
        return textComponent;
    }

    void checkDocumentLockedIfLogging() {
        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
            checkDocumentLocked();
        }
    }
    
    void checkDocumentLocked() {
        Document doc = getDocument();
        if (!DocumentUtilities.isReadLocked(doc)) {
            String msg = "Document " + // NOI18N
                    doc.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(doc)) + // NOI18N
                    " not locked"; // NOI18N
            ViewHierarchyImpl.CHECK_LOG.log(Level.INFO, msg, new Exception(msg)); // NOI18N
        }
    }
    
    boolean isDocumentLocked() {
        return DocumentUtilities.isReadLocked(getDocument());
    }
    
    void checkMutexAcquiredIfLogging() {
        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
            checkLocked();
        }
    }

    void checkLocked() {
        PriorityMutex mutex = pMutex;
        if (mutex != null) {
            Thread mutexThread = mutex.getLockThread();
            if (mutexThread != Thread.currentThread()) {
                String msg = (mutexThread == null)
                        ? "Mutex not acquired" // NOI18N
                        : "Mutex already acquired for different thread: " + mutexThread; // NOI18N
                ViewHierarchyImpl.CHECK_LOG.log(Level.INFO, msg + " for textComponent=" + textComponent, new Exception()); // NOI18N
            }
        }
    }

    boolean isLocked() {
        PriorityMutex mutex = pMutex;
        return (mutex != null && mutex.getLockThread() == Thread.currentThread());
    }
    
    public Map<TextLayout,String> getTextLayoutVerifier() {
        if (textLayoutVerifier == null) {
            textLayoutVerifier = new WeakHashMap<TextLayout, String>(256);
        }
        return textLayoutVerifier;
    }

    @Override
    protected String getDumpName() {
        return "DV";
    }

    @Override
    public String findIntegrityError() {
        String err = super.findIntegrityError();
        if (err == null) {
            int viewCount = getViewCount();
            if (viewCount > 0) {
                ParagraphView firstView = getParagraphView(0);
                if (firstView.getStartOffset() != startOffset) {
                    err = "firstView.getStartOffset()=" + firstView.getStartOffset() + // NOI18N
                            " != startOffset=" + startOffset; // NOI18N
                }
                ParagraphView lastView = getParagraphView(viewCount - 1);
                int lastViewEndOffset = lastView.getEndOffset();
                if (err == null && lastViewEndOffset != endOffset) {
                    err = "lastView.endOffset=" + lastViewEndOffset + " != endOffset=" + endOffset; // NOI18N
                }
                int docTextEndOffset = getDocument().getLength() + 1;
                if (err == null && lastViewEndOffset > docTextEndOffset) {
                    err = "lastViewEndOffset=" + lastViewEndOffset + " > docTextEndOffset=" + docTextEndOffset; // NOI18N
                }
                if (err == null) {
                    err = children.findIntegrityError(this);
                }
                if (err == null) {
                    // Check TextLayoutCache correctness - all PVs with non-null children
                    // should be present in the cache
                    TextLayoutCache tlCache = op.getTextLayoutCache();
                    err = tlCache.findIntegrityError();
                    if (err == null) {
                        for (int i = 0; i < viewCount; i++) {
                            ParagraphView pView = getParagraphView(i);
                            boolean inCache = tlCache.contains(pView);
                            if (!pView.isChildrenNull() != inCache) {
                                err = "Invalid TLCaching for pView[" + i + "]: inCache=" + inCache; // NOI18N
                                break;
                            }
                        }
                    }
                }
            } // else { do not check startOffset == endOffset since they may differ (pViews not created yet)
            if (err == null && startOffset > endOffset) {
                err = "startOffset=" + startOffset + " > endOffset=" + endOffset; // NOI18N
            }
        }

        if (err != null) {
            err = getDumpName() +  ": " + err;
        }
        return err;
    }

    @Override
    public String findTreeIntegrityError() {
        final String[] ret = new String[1];
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                ret[0] = op.isChildrenValid()
                        ? DocumentView.super.findTreeIntegrityError()
                        : null; // No checks when children are invalid (likely startOffset != firstPView.getStartOffset() etc.)
            }
        });
        return ret[0];
    }

    @Override
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, String xyInfo, int importantChildIndex) {
        DocumentView.super.appendViewInfo(sb, indent, xyInfo, importantChildIndex);
        if (getParent() == null) {
            sb.append("; NULL-PARENT");
        }
        if (!op.isChildrenValid()) {
            sb.append("; INVALID-CHILDREN");
        }
        Position startPos = getExtraStartPosition();
        Position endPos = getExtraEndPosition();
        if (startPos != null || endPos != null) {
            sb.append("; ExtraBounds:<");
            sb.append((startPos != null) ? startPos.getOffset() : "START");
            sb.append(","); // NOI18N
            sb.append((endPos != null) ? endPos.getOffset() : "END");
            sb.append(">, ");
        }
        op.appendInfo(sb);
        if (LOG_SOURCE_TEXT) {
            Document doc = getDocument();
            sb.append("\nDoc: ").append(ViewUtils.toString(doc)); // NOI18N
        }
        if (importantChildIndex != -1) {
            children.appendChildrenInfo(DocumentView.this, sb, indent, importantChildIndex);
        }
        return sb;
    }

    @Override
    public String toString() {
        final String[] s = new String[1];
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                s[0] = toStringNeedsLock();
            }
        });
        return s[0];
    }
    
    public String toStringNeedsLock() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString();
    }

    public String toStringDetail() {
        final String[] s = new String[1];
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                s[0] = toStringDetailNeedsLock();
            }
        });
        return s[0];
    }
    
    public String toStringDetailNeedsLock() { // Dump everything
        return appendViewInfo(new StringBuilder(200), 0, "", -2).toString();
    }

}
