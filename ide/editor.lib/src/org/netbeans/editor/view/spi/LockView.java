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

package org.netbeans.editor.view.spi;

import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.lib.editor.util.PriorityMutex;

/**
 * View that allow to lock the view hierarchy.
 * It's a filter view that is being installed under the root view.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LockView extends View {
    
    private static final String PROPERTY_VIEW_HIERARCHY_MUTEX = "viewHierarchyMutex"; // NOI18N
    // Note: FoldHierarchyExecution has the same property
    private static final String PROPERTY_FOLD_HIERARCHY_MUTEX = "foldHierarchyMutex"; // NOI18N
    
    private View view;
    
    private PriorityMutex mutex;
    
    private AbstractDocument doc;
    
    /**
     * Get mutex used to lock the view hierarchy.
     * All the services manipulating the view hierarchy
     * or providing data for the view hierarchy
     * may choose to lock on this mutex
     * rather having its own locking mechanism
     * to simplify their locking model
     * and eliminate possibility of deadlocks
     * because of counter-locking.
     * <br>
     * The <code>LockView</code> itself uses this mutex
     * as well as the code folding hierarchy.
     */
    public static synchronized PriorityMutex getViewHierarchyMutex(JTextComponent component) {
        // A single mutex instance must be shared by view and fold hierarchies
        PriorityMutex mutex = (PriorityMutex)component.getClientProperty(PROPERTY_FOLD_HIERARCHY_MUTEX);
        if (mutex == null) {
            mutex = new PriorityMutex();
            component.putClientProperty(PROPERTY_FOLD_HIERARCHY_MUTEX, mutex);
        }
        component.putClientProperty(PROPERTY_VIEW_HIERARCHY_MUTEX, mutex);
        
        return mutex;
    }

    /**
     * Find the <code>LockView</code> instance in a view hierarchy
     * by traversing it up to the root view.
     *
     * @param view view in the view hierarchy. <code>null</code> is accepted too.
     * @return valid instance of <code>LockView</code> or null
     *  if there is no <code>LockView</code> instance present
     *  in the view hierarchy of the view is no longer
     *  part of the view hierarchy.
     */
    public static LockView get(View view) {
        while (view != null && !(view instanceof LockView)) {
            view = view.getParent();
        }
        
        return (LockView)view;
    }
    
    public LockView(View view) {
        super(null);

        this.view = view;
//        System.out.println("LockView instance created " + System.identityHashCode(this));
    }
    
    public void setParent(View parent) {
    
        View origParent = getParent();
        if (origParent != null && parent != null) {
            /* This is not truly an errorneous situation
             * but this does not normally happen
             * as for complex changes in the text component (e.g. a document replacement)
             * the whole view hierarchy starting with RootView in TextUI
             * is being thrown away and recreated.
             * So this special state is reported
             * to make sure that this situation will be handled
             * before this constraint will be removed.
             */
            throw new IllegalStateException("Unexpected state occurred when" // NOI18N
                + " trying to set non-null parent to LockView with non-null" // NOI18N
                + " parent already set." // NOI18N
            );
        }

        // Assign the mutex variable if necessary
        // May be desirable to be synced with getMutex() if necessary
        if (mutex == null && parent != null) {
            JTextComponent c = (JTextComponent)parent.getContainer();
            if (c != null) {
                mutex = getViewHierarchyMutex(c);
            }
        }

        if (parent != null) {
            // Check that AbstractDocument is being used.
            Document maybeAbstractDoc = parent.getDocument();
            if (!(maybeAbstractDoc instanceof AbstractDocument)) {
                /**
                 * Although LockView could possibly be changed
                 * to work with just the Document interface
                 * there are bunch of other view implementations
                 * in the editor that expect the AbstractDocument as well
                 * mainly due to the presence of AbstractDocument.readLock()
                 * instead of just the Document.render().
                 * If working with non-AbstractDocument instances
                 * would be a strong requirement the editor's
                 * view implementations would have to be reviewed
                 * before this constraint can be removed.
                 */
                throw new IllegalStateException("Currently the LockView" // NOI18N
                    + " is designed to work with AbstractDocument instances only." // NOI18N
                );
            }
            
            /**
             * Remember the document for which this LockView and underlying
             * view hierarchy was created.
             * If any of the childviews would delegate its getDocument()
             * to parent and it would end up here then the remembered
             * document will be returned instead of possibly delegating
             * to the parent RootView which delegates to component.getDocument().
             * The component.getDocument() always brings the most fresh
             * document. However that can be problematic e.g. in the following
             * case:
             * <ol>
             *    <li> ViewLayoutQueue executes a task in Layout-Thread
             *    <li> The task properly locks document and then view hierarchy
             *    <li> In AWT thread someone calls JTextComponent.setDocument()
             *         before the task in Layout-Tread finishes
             *    <li> Task calls JTextComponent.getDocument()
             *         and attempts to do doc.readLock().
             *         Normally it should be noop as the document
             *         was already read-locked previously however
             *         here it's a different document so looking from
             *         the new document's perspective the locking order
             *         is exactly opposite than it should be
             *         i.e. first the view hierarchy is locked
             *         and then the (new) document is locked.
             *         This situation may lead to deadlock from counter-locking.
             *         <br>
             *         Remembering of the document here and consistent
             *         use of the view.getDocument() instead of
             *         component.getDocument() in the tasks
             *         executed in the Layout-Thread
             *         should avoid this type of deadlock.
             * </ol>
             */
            this.doc = (AbstractDocument)maybeAbstractDoc;
        }

        /* First read-lock the document to prevent deadlocks
         * from counter-locking.
         */
        this.doc.readLock();
        try {
            lock();
            try {

                setParentLocked(parent);

            } finally {
                unlock();
            }
                
        } finally {
            this.doc.readUnlock();
        }
    }
    
    protected void setParentLocked(View parent) {
        // possibly first clear parent in child than in this view
        // so that getContainer() remains usable
        if (parent == null && view != null) {
            view.setParent(null);
        }
        
        super.setParent(parent);

        // Update child for non-null parent here
        if (parent != null && view != null) {
            view.setParent(this);
        }
    }

    /**
     * Set a new single child of this view.
     */
    public void setView(View v) {
        lock();
        try {
            
            if (view != null) {
                // get rid of back reference so that the old
                // hierarchy can be garbage collected.
                view.setParent(null);
            }
            view = v;
            if (view != null) {
                view.setParent(this);
            }
            
        } finally {
            unlock();
        }
    }
    
    public void lock() {
        if (mutex != null) {
            mutex.lock();
        }
    }
    
    public void unlock() {
        mutex.unlock(); // should always proceed if a previous lock() succeeded
    }
    
    public boolean isPriorityThreadWaiting() {
        return mutex.isPriorityThreadWaiting();
    }
    
    /**
     * Return the thread that holds a lock on the view hierarchy.
     * <br>
     * This method is intended for diagnostic purposes only to determine
     * an intruder thread that entered the view hierarchy without obtaining
     * the lock first.
     *
     * @return thread that currently holds a lock on the hierarchy or null
     *  if there is currently no thread holding a lock on the hierarchy.
     */
    public Thread getLockThread() {
        return mutex.getLockThread();
    }
    
    public void render(Runnable r) {
        lock();
        try {
            
            r.run();
            
        } finally {
            unlock();
        }
    }

    /**
     * Fetches the attributes to use when rendering.  At this level
     * there are no attributes.  If an attribute is resolved
     * up the view hierarchy this is the end of the line.
     */
    public AttributeSet getAttributes() {
        return null;
    }

    public float getPreferredSpan(int axis) {
        lock();
        try {
            
            if (view != null) {
                return view.getPreferredSpan(axis);
            }
            return 10;

        } finally {
            unlock();
        }
    }

    public float getMinimumSpan(int axis) {
        lock();
        try {
            
            if (view != null) {
                return view.getMinimumSpan(axis);
            }
            return 10;

        } finally {
            unlock();
        }
    }

    public float getMaximumSpan(int axis) {
        lock();
        try {
            
            if (view != null) {
                return view.getMaximumSpan(axis);
            }
            return Integer.MAX_VALUE;

        } finally {
            unlock();
        }
    }

    public void preferenceChanged(View child, boolean width, boolean height) {
        View parent = getParent();
        if (parent != null) {
            parent.preferenceChanged(this, width, height);
        }
    }

    public float getAlignment(int axis) {
        lock();
        try {
            
            if (view != null) {
                return view.getAlignment(axis);
            }
            return 0;

        } finally {
            unlock();
        }
    }

    public void paint(Graphics g, Shape allocation) {
        if (g == null)
            return; // #131797 - seems like a null graphics could be passed by swing

        lock();
        try {
            
            if (view != null) {
                view.paint(g, allocation);
            }

        } finally {
            unlock();
        }
    }

    public int getViewCount() {
        return 1;
    }

    /** 
     * Gets the n-th view in this container.
     *
     * @param n the number of the view to get
     * @return the view
     */
    public View getView(int n) {
        return view;
    }

    public int getViewIndex(int pos, Position.Bias b) {
        return 0;
    }

    public Shape getChildAllocation(int index, Shape a) {
        return a;
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        lock();
        try {
            
            if (view != null) {
                return view.modelToView(pos, a, b);
            }
            return null;

        } finally {
            unlock();
        }
    }

    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
        lock();
        try {
            
            if (view != null) {
                return view.modelToView(p0, b0, p1, b1, a);
            }
            return null;

        } finally {
            unlock();
        }
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        lock();
        try {
            
            if (view != null) {
                return view.viewToModel(x, y, a, bias);
            }
            return -1;

        } finally {
            unlock();
        }
    }

    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, 
    int direction, Position.Bias[] biasRet) throws BadLocationException {

        lock();
        try {
            
            if(view != null) {
                return view.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
            } 
            return -1;

        } finally {
            unlock();
        }
    }

    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        lock();
        try {
            
            if (view != null) {
                view.insertUpdate(e, a, f);
            }

        } finally {
            unlock();
        }
    }

    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        lock();
        try {
            
            if (view != null) {
                view.removeUpdate(e, a, f);
            }

        } finally {
            unlock();
        }
    }

    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        lock();
        try {
            
            if (view != null) {
                view.changedUpdate(e, a, f);
            }

        } finally {
            unlock();
        }
    }

    public String getToolTipText(float x, float y, Shape allocation) {
        lock();
        try {
            
            return (view != null)
                ? view.getToolTipText(x, y, allocation)
                : null;

        } finally {
            unlock();
        }
    }

    public Document getDocument() {
        return doc;
    }

    public int getStartOffset() {
        if (view != null) {
            return view.getStartOffset();
        }
        Element elem = getElement();
        return (elem != null) ? elem.getStartOffset() : 0;
    }

    public int getEndOffset() {
        if (view != null) {
            return view.getEndOffset();
        }
        Element elem = getElement();
        return (elem != null) ? elem.getEndOffset() : 0;
    }

    public Element getElement() {
        if (view != null) {
            return view.getElement();
        }
        Document doc = getDocument();
        return (doc != null) ? doc.getDefaultRootElement() : null;
    }

    public View breakView(int axis, float len, Shape a) {
        throw new Error("Can't break lock view"); // NOI18N
    }

    public int getResizeWeight(int axis) {
        lock();
        try {
            
            if (view != null) {
                return view.getResizeWeight(axis);
            }
            return 0;

        } finally {
            unlock();
        }
    }

    public void setSize(float width, float height) {
        lock();
        try {
            
            if (view != null) {
                view.setSize(width, height);
            }

        } finally {
            unlock();
        }
    }

}
