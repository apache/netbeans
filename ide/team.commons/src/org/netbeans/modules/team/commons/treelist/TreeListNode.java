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
package org.netbeans.modules.team.commons.treelist;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Representation of a single row in TreeList.<br> If the node is expandable
 * then its children are created asynchronously in a separate thread to avoid
 * blocking of AWT queue.<br> Each node provides its own renderer component.
 *
 * @author S. Aubrecht
 */
public abstract class TreeListNode extends ListNode {

    /**
     * Time in milliseconds to wait for children creation to finish. When the
     * interval elapses then node's renderer shows an error message.
     */
    public static final long TIMEOUT_INTERVAL_MILLIS =
            NbPreferences.forModule(TreeListNode.class).getInt("node.expand.timeoutmillis", 5 * 60 * 1000); //NOI18N
    private final boolean expandable;
    private final TreeListNode parent;
    private TreeListListener listener;
    private boolean expanded = false;
    private ArrayList<TreeListNode> children = null;
    private final Object LOCK = new Object();
    private RendererPanel renderer;
    private ChildrenLoader loader;
    private Type type;
    private boolean indentChildren = true;
    private static RequestProcessor rp = new RequestProcessor("Tree List Node - Load Children", 5); // NOI18N

    protected static void post(Runnable run) {
        rp.post(run);
    }
    private int lastRowWidth = -1;
    private final boolean renderGradient;

    /**
     * C'tor
     *
     * @param expandable True if the node provides some children
     * @param parent Node's parent or null if this node is root.
     */
    public TreeListNode(boolean expandable, TreeListNode parent) {
        this(expandable, true, parent);
    }
    
    /**
     * C'tor
     *
     * @param expandable True if the node provides some children
     * @param renderGradient False in case the expanded/collapsed icon 
     *        shouldn't be shown even though the node is expandable
     * @param parent Node's parent or null if this node is root.
     */
    public TreeListNode(boolean expandable, boolean renderGradient, TreeListNode parent) {
        this.expandable = expandable;
        this.renderGradient = renderGradient;
        this.parent = parent;
    }

    public boolean isRenderedWithGradient() {
        return renderGradient;
    }
    
    public final boolean isExpandable() {
        return expandable;
    }

    public final TreeListNode getParent() {
        return parent;
    }

    public final List<TreeListNode> getChildren() {
        synchronized (LOCK) {
            if (null == children) {
                return Collections.emptyList();
            }
            return new ArrayList<TreeListNode>(children);
        }
    }

    /**
     * This method is called outside AWT thread and may block indefinetely. The
     * list of children is cached until the call of refreshChildren() method.
     *
     * @return Node's children or an empty list if no children are available,
     * never returns null.
     */
    protected abstract List<TreeListNode> createChildren();

    /**
     * Invoke this method when node's children must be reloaded.
     */
    protected final void refreshChildren() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean hasChildren;
                synchronized (LOCK) {
                    hasChildren = null != children;
                    if (hasChildren) {
                        for (TreeListNode node : children) {
                            node.dispose();
                        }
                        children = null;
                    }

                    if (expanded) {
                        startLoadingChildren();
                    }
                }
                if (hasChildren && null != listener) {
                    listener.childrenRemoved(TreeListNode.this);
                }
            }
        });
    }

    final JComponent getRenderer(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowHeight, int rowWidth) {
        RendererPanel res = null;
        synchronized (this) {
            //hack - in case of resizing TC fire content changed to repaint
            if (lastRowWidth > rowWidth) {
                fireContentChanged();
            }
            this.lastRowWidth = rowWidth;
            if (null == renderer) {
                renderer = new RendererPanel(this);
            }
            res = renderer;
        }

        res.configure(foreground, background, isSelected, hasFocus, getNestingDepth(), rowHeight, rowWidth);

        return res;
    }

    /**
     * Notification that the loading of this node's children has started. The
     * method may get called several times without corresponding
     * childrenLoadingFinished() or childrenLoadingTimedout() calls as the
     * loading thread may get cancelled.
     */
    protected void childrenLoadingStarted() {
    }

    /**
     * Notification that the loading of this node's children is finished.
     */
    protected void childrenLoadingFinished() {
    }

    /**
     * Notification that the loading of this node's children has timed out.
     */
    protected void childrenLoadingTimedout() {
    }

    /**
     * Determines whether children of this node should be indented. Considered
     * when calculating the nesting depth. By default true.
     * @return true if the children should be indented (add one depth level),
     *         false otherwise
     */
    public boolean getIndentChildren() {
        return indentChildren;
    }

    /**
     * Sets whether children of this node should be indented. By default this is
     * true. Makes sense to set to false if this node is not painted in the list
     * (likely root that is not displayed itself) and so should not add indent
     * level (depth) to its children.
     * @param indent whether to indent children nodes of this node
     */
    public void setIndentChildren(boolean indent) {
        indentChildren = indent;
    }

    public final void setListener(TreeListListener listener) {
        synchronized(LOCK) {
            super.setListener(listener);
            this.listener = listener;
        }
    }

    /**
     * Invoked when the node is removed from the model. All listeners should be
     * removed here. Always call super implementation to ensure that children
     * node's (if any) get disposed properly as well.
     */
    protected void dispose() {
        synchronized (LOCK) {
            this.listener = null;
            if (null != children) {
                for (TreeListNode node : children) {
                    node.dispose();
                }
            }
        }
    }

    /**
     * Invoked when the node is added to the model. All listeners should be
     * added here.
     */
    protected void attach() {
    }

    /**
     * Returned type defines the rendering of the node.
     */
    protected Type getType() {
        return Type.NORMAL;
    }

    final boolean isDescendantOf(TreeListNode grandParent) {
        if (null == parent) {
            return false;
        }
        if (parent.equals(grandParent)) {
            return true;
        }
        return parent.isDescendantOf(grandParent);
    }

    public final boolean isExpanded() {
        return expanded && isExpandable();
    }

    public final void setExpanded(boolean expanded) {
        if (!isExpandable()) {
            throw new IllegalStateException();
        }
//        if( this.expanded == expanded )
//            return;
        this.expanded = expanded;
        if (null != listener) {
            if (this.expanded) {
                boolean childrenLoaded = true;
                synchronized (LOCK) {
                    if (null == children) {
                        childrenLoaded = false;
                        startLoadingChildren();
                    }
                }
                if (childrenLoaded) {
                    listener.childrenAdded(this);
                }
            } else {
                synchronized (LOCK) {
                    if (null != loader) {
                        loader.cancel();
                        childrenLoadingFinished();
                    }
                }
                listener.childrenRemoved(this);
            }
        }
    }

    @Override
    final protected void fireContentChanged() {
        synchronized (this) {
            renderer = null;
        }
        super.fireContentChanged();
    }

    @Override
    final protected void fireContentSizeChanged() {
        synchronized (this) {
            renderer = null;
        }        
        super.fireContentSizeChanged(); 
    }

    final protected ProgressLabel createProgressLabel() {
        return createProgressLabel(NbBundle.getMessage(TreeListNode.class, "LBL_LoadingInProgress")); //NOI18N
    }

    final protected ProgressLabel createProgressLabel(String text) {
        return new ProgressLabel(text, this);
    }

    final int getNestingDepth() {
        if (null == getParent()) {
            return 0;
        }
        return getParent().getNestingDepth() + (getParent().getIndentChildren() ? 1 : 0);
    }

    boolean isLoaded() {
        return true;
    }

    private void startLoadingChildren() {
        childrenLoadingStarted();
        if (null != loader) {
            loader.cancel();
        }
        loader = new ChildrenLoader();
        post(loader);
    }

    private class ChildrenLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        public void run() {
            final List<TreeListNode>[] res = new List[1];
            Runnable r = new Runnable() {
                public void run() {
                    res[0] = createChildren();
                }
            };
            t = new Thread(r);
            t.start();
            try {
                t.join(TIMEOUT_INTERVAL_MILLIS);
            } catch (InterruptedException iE) {
                //ignore
            }

            if (cancelled) {
                return;
            }

            if (null == res[0]) {
                childrenLoadingTimedout();
                return;
            }

            synchronized (LOCK) {
                children = new ArrayList<TreeListNode>(res[0]);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (null != listener) {
                        listener.childrenAdded(TreeListNode.this);
                    }
                }
            });
            childrenLoadingFinished();
        }

        public boolean cancel() {
            cancelled = true;
            if (null != t) {
                t.interrupt();
            }
            return true;
        }
    }

    /**
     * Type of the node - each type has a specific background
     */
    protected static enum Type {
        NORMAL,
        CLOSED,
        TITLE;
    }
}
