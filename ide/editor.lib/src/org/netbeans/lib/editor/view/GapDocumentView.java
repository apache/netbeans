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

package org.netbeans.lib.editor.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.editor.view.spi.EstimatedSpanView;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.editor.view.spi.ViewLayoutQueue;
import org.netbeans.editor.view.spi.ViewLayoutState;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.WeakListeners;

/**
 * View responsible for holding all line views for a particular document.
 * <br>
 * There is one instance of this view per document.
 *
 * <p>
 * It is expected that this view will not act as an active
 * layout state i.e. that it will not be hosted by a view
 * implementing <code>ViewLayoutState.Parent</code>.
 * <br>
 * The implementation tries to optimize calls to updateLayout()
 * so that if there are multiple changes in children then
 * they will all be serviced at once.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class GapDocumentView extends GapBoxView {
    
    private static final boolean debugPaint = Boolean.getBoolean(
        "netbeans.debug.editor.view.paint"); // NOI18N
    private static final boolean debugRepaint = Boolean.getBoolean(
        "netbeans.debug.editor.view.repaint"); // NOI18N
    
    /**
     * The minimum number of children serviced
     * during a particular operation necessary for a asynchronous
     * task to be scheduled for updating of the children.
     */
    private static final int ASYNC_CHILDREN_UPDATE_COUNT = 20;
    
    /**
     * If the estimated span flag is being changed in all the children
     * (in response to estimated span change in the parent)
     * this constant determines into how many subtasks should
     * the total task be divided. At least one child will be done in each
     * subtask but it can be more if there is many children.
     * <br>
     * For example if the constant is 100 and there is 6000 children
     * then 6000 / 100 = 60 children will be serviced in each subtask.
     */
    private static final int CHILDREN_UPDATE_SUBTASK_COUNT = 50;
    
    /**
     * Task that updates estimated spans in children
     * to false sequentially when large view replaces are done
     * or when estimated span of the parent view changes
     * from true to false.
     * <br>
     * If a task is run it's remembered in this variable.
     * If there is an additional requirement for a task
     * (e.g. another large replace) the existing running task
     * is reused. Although it consumes another four bytes
     * in the variable space the potential two or more
     * such tasks running in parallel and the resulting view
     * preferred span/size updates at two or more "places"
     * in the view could cause the offset gap to move
     * back and forth affecting the view performance.
     */
    private ChildrenUpdateTask childrenUpdateTask;
    
    /**
     * Last allocation assigned to this view.
     */
    private int lastAllocationX;
    private int lastAllocationY;
    private int lastAllocationWidth;
    private int lastAllocationHeight;
    
    /**
     * Sub-offset along the major axis inside the first child
     * from which a repaint request has came.
     */
    private double firstRepaintChildYSubOffset;
    private double firstRepaintChildYSubSpan;
    private float firstRepaintChildXSubOffset;
    
    /**
     * Depth of the layout lock used to defer the updateLayout() call.
     */
    private int layoutLockDepth;
    
    private boolean pendingUpdate;
    
    private DocumentListener earlyDocListener;
    
    private int damageRangeStartOffset;
    private int damageRangeEndOffset;
    
    /** Set to true to avoid adding bottom padding */
    private final boolean hideBottomPadding;

    /**
     * Cached viewport instance or null.
     */
    private JViewport viewport;

    /**
     * Construct a view intended to cover the whole document.
     *
     * @param elem the element of the model to represent.
     */
    public GapDocumentView(Element elem) {
        this(elem, false);
    }

    /**
     * Construct a view intended to cover the whole document.
     *
     * @param elem the element of the model to represent.
     * @param hideBottomPadding to avoid adding bottom padding to view
     */
    public GapDocumentView(Element elem, boolean hideBottomPadding) {
        super(elem, View.Y_AXIS);
        this.hideBottomPadding = hideBottomPadding;
        clearDamageRangeBounds();
    }
    
    @Override
    public float getPreferredSpan(int axis) {
        float span = super.getPreferredSpan(axis);
        if (!hideBottomPadding && (axis == View.Y_AXIS)) {
            // Add an extra span to avoid typing at the bottom of the screen
            // by adding virtual height
            if (viewport == null) {
                Container c = getContainer();
                if (c != null) {
                    viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, c);
                }
            }
            if (viewport != null) {
                int viewportHeight = viewport.getExtentSize().height;
                span += viewportHeight / 3;
            }

        }
        return span;
    }

    GapBoxViewChildren createChildren() {
        return new GapDocumentViewChildren(this);
    }

    protected Rectangle reallocate(Shape a) {
        Rectangle alloc = super.reallocate(a);
        
        lastAllocationX = alloc.x;
        lastAllocationY = alloc.y;
        lastAllocationWidth = alloc.width;
        lastAllocationHeight = alloc.height;
        
        return alloc;
    }

    protected void directUpdateLayout() {
        // assert (layoutLockDepth >= 0);
        if (layoutLockDepth == 0) {
            super.directUpdateLayout();
        }
    }
    
    protected final void layoutLock() {
        layoutLockDepth++;
    }
    
    protected final void layoutUnlock() {
        layoutLockDepth--;
    }

    public void renderWithUpdateLayout(Runnable r) {
        layoutLockDepth++;
        try {
            r.run();
        } finally {
            updateLayout();
            layoutLockDepth--;
        }
    }

    public void setParent(View parent) {
        layoutLockDepth++;
        try {
            super.setParent(parent);
            if (parent != null) {
                Document doc = getDocument();
                if (doc != null) {
                    earlyDocListener = new DocumentListener() {
                        public void insertUpdate(DocumentEvent e) {
                            markPendingUpdate();
                        }

                        public void removeUpdate(DocumentEvent e) {
                            markPendingUpdate();
                        }

                        public void changedUpdate(DocumentEvent e) {
                            // Do nothing since this is likely invoked by BaseDocument.repaintBlock()
                        }
                    };
                    if (!DocumentUtilities.addPriorityDocumentListener(doc,
                            WeakListeners.document(earlyDocListener, doc),
                            DocumentListenerPriority.FIRST)
                    ) { // Priority listening not supported on the given document
                        // The early listener was not added and markPendingUpdate() will never be called
                        earlyDocListener = null;
                    }
                }
            }
        } finally {
            updateLayout();
            layoutLockDepth--;
        }
    }

    public void setSize(float width, float height) {
        layoutLockDepth++;
        try {
            super.setSize(width, height);
        } finally {
            updateLayout();
            layoutLockDepth--;
        }
    }

    public void insertUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        layoutLockDepth++;
        try {
            pendingUpdate = false; // Reset before so that child view do not estimate
            super.insertUpdate(evt, a, f);
        } finally {
            updateLayout();
            checkPendingDamageRange();
            layoutLockDepth--;
        }
    }
    
    public void removeUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        layoutLockDepth++;
        try {
            pendingUpdate = false; // Reset before so that child view do not estimate
            super.removeUpdate(evt, a, f);
        } finally {
            updateLayout();
            checkPendingDamageRange();
            layoutLockDepth--;
        }
    }
    
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        layoutLockDepth++;
        try {
            // Do not check pendingUpdate since changedUpdate() is likely invoked by BaseDocument.repaintBlock()
            super.changedUpdate(e, a, f);
        } finally {
            updateLayout();
            layoutLockDepth--;
        }
    }

    /**
     * Check whether damage range should be performed in a regular way
     * or whether the view hierarchy is not updated yet and so just the boundaries
     * of the damage should just be remembered until the insertUpdate() etc.
     * arrives.
     * 
     * @return true if the regular <code>JTextComponent.getUI().damageRange()</code>
     *  should be performed or false if there are pending view updates and the damageRange
     *  will be invoked later by the view itself.
     */
    public boolean checkDamageRange(int startOffset, int endOffset, Bias startBias, Bias endBias) {
        if (pendingUpdate) {
            damageRangeStartOffset = Math.min(damageRangeStartOffset, startOffset);
            damageRangeEndOffset = Math.max(damageRangeEndOffset, endOffset);
            return false;
        }
        return true;
    }

    /**
     * Return true if this view (and its subviews) need to be updated after a document modification.
     * This flag is set automatically because of an early document listener being notified
     * before all other document listeners that possibly call
     * <code>JTextComponent.getUI().damageRange()</code>.
     */
    public boolean isPendingUpdate() {
        return pendingUpdate;
    }

    void markPendingUpdate() {
        this.pendingUpdate = true;
    }

    void checkPendingDamageRange() {
        if (damageRangeStartOffset != Integer.MAX_VALUE) {
            Document doc = getDocument();
            if (doc != null) {
                damageRangeStartOffset = Math.max(damageRangeStartOffset, doc.getLength());
                damageRangeEndOffset = Math.max(damageRangeEndOffset, doc.getLength());
                JTextComponent component = (JTextComponent)getContainer();
                if (component != null) {
                    component.getUI().damageRange(component, damageRangeStartOffset, damageRangeEndOffset);
                }
            }
        }
        clearDamageRangeBounds();
    }
    
    private void clearDamageRangeBounds() {
        damageRangeStartOffset = Integer.MAX_VALUE;
        damageRangeEndOffset = -1;
        pendingUpdate = false; // Ensure that the flag is false
    }

    public void paint(Graphics g, Shape a) {
        if (debugPaint) {
            System.err.println("VIEW-PAINT: clip=" + g.getClipBounds() + ", alloc=" + a); // NOI18N
        }

        // During paint the estimated spans of children may be reset to exact measurements
        // causing the layout to be updated
        layoutLockDepth++;
        try {
            super.paint(g, a);
        } finally {
            updateLayout();
            layoutLockDepth--;
        }
    }

    public void repaint(ViewLayoutState child,
    double majorAxisOffset, double majorAxisSpan,
    float minorAxisOffset, float minorAxisSpan) {

        int childIndex = getChildIndexNoCheck(child);
        if (markRepaint(childIndex, false)) { // lower index was marked
            firstRepaintChildYSubOffset = majorAxisOffset;
            firstRepaintChildXSubOffset = minorAxisOffset;
        }
    }

    protected boolean markRepaint(int childIndex, boolean repaintTillEnd) {
        boolean lowerIndexMarked = super.markRepaint(childIndex, repaintTillEnd);
        if (lowerIndexMarked) {
            firstRepaintChildYSubOffset = 0d;
            firstRepaintChildXSubOffset = 0f;
        }
        return lowerIndexMarked;
    }
        
    protected void processRepaint(ViewLayoutState.Parent lsParent) {
        int firstRepaintChildIndex = getChildren().getFirstRepaintChildIndex();
        if (firstRepaintChildIndex >= 0 && firstRepaintChildIndex < getViewCount()) {
            double repY = getChildren().getMajorAxisOffset(firstRepaintChildIndex);
            repY += firstRepaintChildYSubOffset;
            int repaintY = (int)Math.floor(repY);

            int repaintX;
            int repaintHeight;
            if (isRepaintTillEnd()) {
                repaintX = 0; // till end should always be since begining
                repaintHeight = lastAllocationHeight;
            } else { // repaint only inside one child
                repaintX = (int)Math.floor(firstRepaintChildXSubOffset);
                double repYEnd = repY
                    + getChild(firstRepaintChildIndex).getLayoutMajorAxisPreferredSpan();
                repaintHeight = (int)Math.ceil(repYEnd) - repaintY;
            }

            int repaintWidth = lastAllocationWidth - repaintX;
            // Shift repaintX by lastAllocationX
            repaintX += lastAllocationX;

            if (debugRepaint) {
                System.err.println("REPAINT(childIndex=" + firstRepaintChildIndex // NOI18N
                    + ", rect(" + repaintX + ", " + repaintY // NOI18N
                    + ", " + repaintWidth + ", " + repaintHeight + "))" // NOI18N
                ); // NOI18N
            }

            Component c = getContainer();
            if (c != null) {
                c.repaint(repaintX, repaintY, repaintWidth, repaintHeight);
            }
        }
    }
    
    ChildrenUpdateTask getChildrenUpdateTask() {
        if (childrenUpdateTask == null) {
            childrenUpdateTask = new ChildrenUpdateTask();
        }
        return childrenUpdateTask;
    }
    
    protected void resetEstimatedSpan(int childIndex, int count) {
        if (count >= ASYNC_CHILDREN_UPDATE_COUNT) {
            ChildrenUpdateTask updateTask = getChildrenUpdateTask();
            updateTask.markResetChildEstimatedSpan();
            updateTask.setChildIndex(childIndex);
            if (!updateTask.isRunning()) {
                updateTask.start();
            }
            
        } else { // small count => do synchronously
            super.resetEstimatedSpan(childIndex, count);
        }
    }

    protected void markSizeInvalid(int childIndex, int count) {
        if (count >= ASYNC_CHILDREN_UPDATE_COUNT) {
            ChildrenUpdateTask updateTask = getChildrenUpdateTask();
            updateTask.markUpdateChildSize();
            updateTask.setChildIndex(0);
            if (!updateTask.isRunning()) {
                updateTask.start();
            }
            
        } else { // small count => do synchronously
            super.markSizeInvalid(childIndex, count);
        }
    }

    protected final int getLastAllocationX() {
        return lastAllocationX;
    }

    protected final int getLastAllocationY() {
        return lastAllocationY;
    }

    protected final int getLastAllocationWidth() {
        return lastAllocationWidth;
    }

    protected final int getLastAllocationHeight() {
        return lastAllocationHeight;
    }

    /**
     * Fetch the queue to use for layout.
     */
    protected ViewLayoutQueue getLayoutQueue() {
//        return ViewLayoutQueue.getSynchronousQueue();
        return ViewLayoutQueue.getDefaultQueue();
    }
    

    /**
     * Task that crawls through children and sets their estimated span
     * to false.
     * <br>
     * It's used when a large replace is done or when the view changes
     * its estimated span from true to false.
     * <br>
     * The task gets initial child index and processes everything
     * till the last child.
     */
    final class ChildrenUpdateTask implements Runnable {
        
        private int childIndex = Integer.MAX_VALUE;
        
        private boolean running;
        
        private boolean updateChildSize;
        
        private boolean resetChildEstimatedSpan;
        
        ChildrenUpdateTask() {
        }
        
        void markUpdateChildSize() {
            updateChildSize = true;
        }
        
        void markResetChildEstimatedSpan() {
            resetChildEstimatedSpan = true;
        }

        void start() {
            running = true;
            getLayoutQueue().addTask(this);
        }

        boolean isRunning() {
            return running;
        }
        
        private void finish() {
            running = false;
            updateChildSize = false;
            resetChildEstimatedSpan = false;
            childIndex = Integer.MAX_VALUE;
        }
        
        void setChildIndex(int childIndex) {
            if (childIndex < this.childIndex) {
                this.childIndex = childIndex;
            }
        }
        
        public void run() {
            AbstractDocument doc = (AbstractDocument)getDocument();
            if (doc!=null){
                doc.readLock();
                try {
                    LockView lockView = LockView.get(GapDocumentView.this);
                    if (lockView != null) {
                        lockView.lock();
                        try {
                            layoutLock();
                            try {
                                updateView(lockView);
                            } finally {
                                updateLayout();
                                layoutUnlock();
                            }
                        } finally {
                            lockView.unlock();
                        }
                    } // missing lock view => likely disconnected from hierarchy
                } finally {
                    doc.readUnlock();
                }
            }
        }

        private void updateView(LockView lockView) {
            if (getContainer() == null) { // view disconnected from component
                finish();
                return;
            }

            int viewCount = getViewCount();
            int updateCount = Math.max(1,
                viewCount / CHILDREN_UPDATE_SUBTASK_COUNT);

            while (updateCount > 0 && childIndex < viewCount
                && !lockView.isPriorityThreadWaiting()
            ) {
                ViewLayoutState child = getChild(childIndex);
                if (!child.isFlyweight()) {
                    View childView = child.getView();

                    // Posibly reset child's estimated span
                    if (resetChildEstimatedSpan) {
                        if (childView instanceof EstimatedSpanView) {
                            ((EstimatedSpanView)childView).setEstimatedSpan(false);
                        }
                    }

                    // Possibly mark the child as invalid
                    if (updateChildSize) {
                        child.markViewSizeInvalid();
                    }

                    // Update child's layout
                    child.updateLayout();
                    // assert (child.isLayoutValid());
                    
                    updateCount--;
                }

                childIndex++;
            }
            
            if (childIndex < viewCount) { // not finished yet
                // Schedule this runnable again to layout thread
                // to continue the ongoing work
                getLayoutQueue().addTask(this);

            } else { // no more children
                finish();
            }
        }
        
    }
    
}
