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

package org.openide.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;

/** A <em>node</em> represents one element in a hierarchy of objects (beans).
* It provides all methods that are needed for communication between
* an explorer view and the bean.
* <P>
* The node has three purposes:
* <OL>
*   <LI>visually represent the object in the tree hierarchy (i.e. Explorer)
*   <LI>provide sets of properties for that object (i.e. Component Inspector, Property Sheet)
*   <LI>offer actions to perform on itself
* </OL>
* <P>
* Frequently nodes are created to represent <code>DataObject</code>s.
* But they may also represent anything to be displayed to the user or manipulated programmatically,
* even if they have no data directly stored behind them; for example, a control panel or debugger
* breakpoint.
* <P>
* There are two listeners in this class: {@link PropertyChangeListener}
* and {@link NodeListener} (which extends <code>PropertyChangeListener</code>). The first
* is designed to listen on properties that can be returned from
* {@link #getPropertySets}, the later for listening on changes in the
* node itself (including the name, children, parent, set of properties,
* icons, etc.). Be sure to distinguish between these two.
* <P>
* The node is cloneable. When a node is cloned, it is initialized
* with an empty set of listeners and no parent. The display name and short description
* are copied to the new node. The set of properties is <em>shared</em>.
* <P>
* Implements {@link org.openide.util.Lookup.Provider} since 3.11.
*
* @author Jaroslav Tulach,
*/
public abstract class Node extends FeatureDescriptor implements Lookup.Provider, HelpCtx.Provider {
    /** An empty leaf node. */
    public static final Node EMPTY = new AbstractNode(Children.LEAF);

    /* here is list of property names that can be changed in the Node object.
    * These properties can be notified to the <CODE>NodeListener</CODE>.
    */

    /** Property for node display name. */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N

    /** Property for internal (not displayable) name of a node. This name is often used to
    * look up a node in the hierarchy, so it should be chosen to be simple.
    */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Property for short description of a node. */
    public static final String PROP_SHORT_DESCRIPTION = "shortDescription"; // NOI18N

    /** Property for the normal (closed) icon of a node. */
    public static final String PROP_ICON = "icon"; // NOI18N

    /** Property for the opened icon of a node. */
    public static final String PROP_OPENED_ICON = "openedIcon"; // NOI18N

    /** Property for a node's parent. */
    public static final String PROP_PARENT_NODE = "parentNode"; // NOI18N

    /** Property for a node's list of property sets. */
    public static final String PROP_PROPERTY_SETS = "propertySets"; // NOI18N

    /** Property for a node's cookie set. */
    public static final String PROP_COOKIE = "cookie"; // NOI18N

    /** Property saying whether the Node is Leaf
     *@since 3.1
     */
    public static final String PROP_LEAF = "leaf"; // NOI18N

    /** Error manager used for logging and its precomputed err.isLoggable(Level.FINE) 
     */
    private static final Logger err = Logger.getLogger("org.openide.nodes.Node"); //NOI18N;

    /** cache of all created lookups */
    private static Map<EventListenerList,Reference<Lookup>> lookups = new WeakHashMap<EventListenerList,Reference<Lookup>>(37);

    /** class.property names we have warned about for #31413 */
    private static final Set<String> warnedBadProperties = new HashSet<String>(100);

    /** template for changes in cookies */
    private static final Lookup.Template<Node.Cookie> TEMPL_COOKIE = new Lookup.Template<Node.Cookie>(Node.Cookie.class);

    /** Lock for initialization */
    private static final Object INIT_LOCK = new Object();

    /** private lock to avoid synchronize on this */
    private static final Object LOCK = new Object();

    /** children representing parent node (Children or ChildrenArray),
    * for synchronization reasons must be changed only
    * under the Children.MUTEX lock
    */
    private Object parent;

    /** children list, for synch. reasons change only under Children.MUTEX
    * lock
    */
    Children hierarchy;

    /** listeners for changes in hierarchy.
    */
    private transient EventListenerList listeners;

    /** Creates a new node with a given hierarchy of children.
    * @param h the children to use for this node
    * @exception IllegalStateException if the children object is already in use by
    *   a different node
    */
    protected Node(Children h) throws IllegalStateException {
        this(h, null);
    }

    /** Creates a new node with a given hierarchy of children and a lookup
    * providing content for {@link #getCookie} and {@link #getLookup} methods.
    * <p>
    * As the lookup needs to be constructed before Node's constructor is called,
    * it might not be obvious how to add Node or other objects into it without
    * type casting. Here is the recommended suggestion that uses public/private
    * pair of constructors:
    * <PRE>
    <span class="keyword">public</span> <span class="function-name">MyNode</span><span class="constant">(</span><span class="variable-name">Children</span> <span class="variable-name">ch</span><span class="constant">,</span> <span class="variable-name">FileObject</span> <span class="variable-name">fil</span><span class="variable-name">e</span><span class="constant">)</span> <span class="constant">{</span>
        <span class="keyword">this</span><span class="constant">(</span><span class="variable-name">ch</span><span class="constant">, </span><span class="variable-name">file</span><span class="constant">,</span> <span class="keyword">new</span> <span class="function-name">InstanceContent</span><span class="constant">(</span><span class="constant">)</span><span class="constant">)</span><span class="constant">;</span>
    <span class="constant">}</span>

    <span class="comment">/** A private constructor that takes an InstanceContent and</span>
    <span class="comment">     * uses it as internals for the Node lookup and also allow us</span>
    <span class="comment">     * to modify the content, for example by adding a reference </span>
    <span class="comment">     * to the node itself or any other object we want to represent.</span>
    <span class="comment">     *</span>
    <span class="comment">     * @param ch children we wish to use</span>
    <span class="comment">     * @param file sample object we wish to have in lookup</span>
    <span class="comment">     * @param content the content created by the first constructor</span>
    <span class="comment">     *<b>/</b></span>
    <span class="keyword">private</span> <span class="function-name">MyNode</span><span class="constant">(</span><span class="variable-name">Children</span> <span class="variable-name">ch</span><span class="constant">,</span> <span class="variable-name">FileObject</span> <span class="variable-name">file</span><span class="constant">,</span> <span class="variable-name">InstanceContent</span> <span class="variable-name">content</span><span class="constant">)</span> <span class="constant">{</span>
        <span class="keyword">super</span><span class="constant">(</span><span class="variable-name">ch</span><span class="constant">,</span> <span class="keyword">new</span> <span class="function-name">AbstractLookup</span><span class="constant">(</span><span class="variable-name">content</span><span class="constant">)</span><span class="constant">)</span><span class="constant">;</span>
        <span class="comment">// adds the node to our own lookup</span>
        <span class="variable-name">content</span><span class="constant">.</span><span class="function-name">add</span> <span class="constant">(</span><span class="keyword">this</span><span class="constant">)</span><span class="constant">;</span>
        <span class="comment">// adds additional items to the lookup</span>
        <span class="variable-name">content</span><span class="constant">.</span><span class="function-name">add</span> <span class="constant">(</span><span class="variable-name">file</span><span class="constant">)</span><span class="constant">;</span>
    <span class="constant">}</span>
    </PRE>
    *
    * @since 3.11
    * @param h the children to use for this node
    * @param lookup the lookup to provide content of {@link #getLookup}
    *   and also {@link #getCookie}
    * @exception IllegalStateException if the children object is already in use by
    *   a different node
    */
    protected Node(Children h, Lookup lookup) throws IllegalStateException {
        this.hierarchy = h;

        // allow subclasses (FilterNode) to update the lookup
        lookup = replaceProvidedLookup(lookup);

        if (lookup != null) {
            this.listeners = new LookupEventList(lookup);
        } else {
            this.listeners = new EventListenerList();
        }

        // attaches to this node
        h.attachTo(this);
    }

    /** Method for subclasses to modify provided lookup before its use.
     * This implementation does nothing.
     */
    Lookup replaceProvidedLookup(Lookup l) {
        return l;
    }

    /** Method that gives access to internal lookup.
     * @param init should it be really initialized (ready for queries) or need not be
     * @return lookup or null
     */
    final Lookup internalLookup(boolean init) {
        if (listeners instanceof LookupEventList) {
            return ((LookupEventList) listeners).init(init);
        } else {
            return null;
        }
    }

    /** Implements {@link Object#clone} to behave correctly if cloning is desired.
    * But {@link Cloneable} is <em>not</em> declared by default.
    * <P>
    * The default implementation checks whether the child list implements
    * <code>Cloneable</code>, and if so, it clones the children.
    * If the child list does not support cloning, {@link Children#LEAF} is used
    * instead for the children. The parent of this node is set to <code>null</code> and an empty set
    * of listeners is attached to the node.
    *
    * @return the cloned version of the node
    * @exception CloneNotSupportedException if the children cannot be cloned
    *    in spite of implementing <code>Cloneable</code>
    */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Node n = (Node) super.clone();
        Children hier2;

        if (hierarchy instanceof Cloneable) {
            hier2 = (Children) hierarchy.cloneHierarchy();
        } else {
            hier2 = Children.LEAF;
        }

        // attach the hierarchy
        n.hierarchy = hier2;
        hier2.attachTo(n);

        // no parent
        n.parent = null;

        // empty set of listeners
        if (listeners instanceof LookupEventList) {
            n.listeners = new LookupEventList(internalLookup(false));
        } else {
            n.listeners = new EventListenerList();
        }

        return n;
    }

    /** Clone the node. The newly created node should reference the same
    * object as this node does, but it may be added as a child
    * to a different parent node. Also it should have an empty set of listeners.
    * In all other respects the node should behave exactly as the
    * original one does.
    *
    * @return copy of this node
    */
    public abstract Node cloneNode();

    /** Finds the children we are attached to.
     * @return children
     */
    private Children getParentChildren() {
        Object p = this.parent;
        return (p instanceof ChildrenArray) ? ((ChildrenArray)p).getChildren() : (Children)p;
    }

    /** Method that allows Children to change the parent children of
    * the node when the node is add to a children.
    *
    * @param parent the children that wants to contain this node
    * @param index index that will be assigned to this node
    * @exception IllegalStateException if this node already belongs to a children
    */
    final void assignTo(Children parent, int index) {
        synchronized (LOCK) {
            Children ch = getParentChildren();

            if ((ch != null) && (ch != parent)) {
                String parentNodes = null;
                String chNodes = null;
                Throwable t = null;
                try {
                    parentNodes = Arrays.toString(parent.getNodes());
                    chNodes = Arrays.toString(ch.getNodes());
                } catch (StackOverflowError e) {
                    t = e;
                    StackTraceElement[] from = t.getStackTrace();
                    StackTraceElement[] arr = new StackTraceElement[Math.min(50, from.length)];
                    System.arraycopy(from, 0, arr, 0, arr.length);
                    t.setStackTrace(arr);
                } catch (RuntimeException e) {
                    t = e;
                }
                IllegalStateException ex = new IllegalStateException(
                    "Cannot initialize " + index + "th child of node " + parent.getNode() +
                    "; it already belongs to node " + ch.getNode() + " (did you forgot to use cloneNode?)\nChildren of new node: " +
                    parentNodes + "\nChildren of old node: " +
                    chNodes
                ); // NOI18N
                if (t != null) {
                    ex.initCause(t);
                }
                throw ex;
            }

            if (!(this.parent instanceof ChildrenArray)) {
                this.parent = parent;
            }
        }
    }

    /** Code that reassigns the reference from to parent from its
     * Children to its ChildrenArray.
     */
    final void reassignTo(Children currentParent, ChildrenArray itsArray) {
        synchronized (LOCK) {
            if ((this.parent != currentParent) && (this.parent != itsArray)) {
                throw new IllegalStateException(
                    "Unauthorized call to change parent: " + this.parent + " and should be: " + currentParent
                );
            }

            this.parent = itsArray;
        }
    }

    /** Deassigns the node from a children, when it is removed from
    * a children.
    */
    final void deassignFrom(Children parent) {
        synchronized (LOCK) {
            Children p = getParentChildren();

            if (parent != p) {
                throw new IllegalArgumentException("Deassign from wrong parent. Old: " + p + " Caller: " + parent); //NOI18N
            }

            this.parent = null;
        }
    }

    /** Set the system name. Fires a property change event.
    * @param s the new name
    * @exception IllegalArgumentException if the new name cannot represent
    *    a valid node name
    */
    @Override
    public void setName(String s) {
        String name = super.getName();

        if ((name == null) || !name.equals(s)) {
            super.setName(s);
            fireNameChange(name, s);
        }
    }

    /** Set the display name. Fires a property change event.
    * @param s the new name
    */
    @Override
    public void setDisplayName(String s) {
        String displayName = super.getDisplayName();

        if ((displayName == null) || !displayName.equals(s)) {
            super.setDisplayName(s);
            fireDisplayNameChange(displayName, s);
        }
    }

    /** Set the short description of the node. Fires a property change event.
    * <p>This description may be used for tool tips, etc.
    * @param s the new description
    */
    @Override
    public void setShortDescription(String s) {
        String descr = super.getShortDescription();

        if ((descr == null) || !descr.equals(s)) {
            super.setShortDescription(s);
            fireShortDescriptionChange(descr, s);
        }
    }

    /**
     * @deprecated Has no effect. To make a node disappear, simply remove it from the
     *             children of its parent. For example, you might call
     *             {@link Children.Keys#setKeys(Collection)} with a smaller collection.
     */
    @Deprecated
    @Override
    public void setHidden(boolean hidden) {
        super.setHidden(hidden);
    }

    /** Find an icon for this node (in the closed state).
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node
    */
    public abstract Image getIcon(int type);

    /** Find an icon for this node (in the open state).
    * This icon is used when the node may have children and is expanded.
    *
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node when open
    */
    public abstract Image getOpenedIcon(int type);

    /** Get context help associated with this node.
    * @return the context help object (could be <code>null</code> or {@link HelpCtx#DEFAULT_HELP})
    */
    public abstract HelpCtx getHelpCtx();

    /** Get the list of children.
    * @return the children
    */
    public final Children getChildren() {
        updateChildren();

        return hierarchy;
    }

    /** Can be overridden in subclasses (probably in FilterNode) to check
     * whether children are of the right subclass
     */
    void updateChildren() {
        Children ch = hierarchy;
        if (ch instanceof Children.LazyChildren) {
            // Replace the children with the ones provided lazily:
            ch = ((Children.LazyChildren) ch).getOriginal();
            setChildren(ch);
        }
    }

    /** Allows to change Children of the node. Call to this method aquires
     * write lock on the nodes hierarchy. Take care not to call this method
     * under read lock.<BR>
     *
     * @param ch New children to be set on the node.
     * @since 3.1
     */
    protected final void setChildren(final Children ch) {
        Parameters.notNull("ch", ch);
        Children.MUTEX.postWriteRequest(new Runnable() { public void run() {

            List<Node> prevSnapshot = null;
            boolean wasInited = hierarchy.isInitialized();
            boolean wasLeaf = hierarchy == Children.LEAF;

            if (wasInited && !wasLeaf) {
                prevSnapshot = hierarchy.snapshot();
            }

            hierarchy.detachFrom();

            if (prevSnapshot != null && prevSnapshot.size() > 0) {
                // set children to LEAF during firing 
                // (cur. snapshot is empty and we should be consistent with children)
                hierarchy = Children.LEAF;
                int[] idxs = Children.getSnapshotIdxs(prevSnapshot);
                // fire remove event
                fireSubNodesChangeIdx(false, idxs, null, Collections.<Node>emptyList(), prevSnapshot);
            }

            hierarchy = ch;
            hierarchy.attachTo(Node.this);

            if (wasInited && !wasLeaf && hierarchy != Children.LEAF) {
                // init new children if old was inited
                hierarchy.getNodesCount();

                // fire add event
                List<Node> snapshot = hierarchy.snapshot();
                if (snapshot.size() > 0) {
                    int[] idxs = Children.getSnapshotIdxs(snapshot);
                    fireSubNodesChangeIdx(true, idxs, null, snapshot, null);
                }
            }

            if (wasLeaf != (hierarchy == Children.LEAF)) {
                fireOwnPropertyChange(PROP_LEAF, wasLeaf, hierarchy == Children.LEAF);
            }
        }});
    }

    /** Test whether the node is a leaf, or may contain children.
    * @return <code>true</code> if the children list is actually {@link Children#LEAF}
    */
    public final boolean isLeaf() {
        updateChildren();

        return hierarchy == Children.LEAF;
    }

    /** Get the parent node.
    * @return the parent node, or <CODE>null</CODE> if this node is the root of a hierarchy
    */
    public final Node getParentNode() {
        // if contained in a list return its parent node
        Children ch = getParentChildren();

        return (ch == null) ? null : ch.getNode();
    }

    /** Test whether this node can be renamed.
    * If true, one can use {@link #getName} to obtain the current name and
    * {@link #setName} to change it.
    *
    * @return <code>true</code> if the node can be renamed
    */
    public abstract boolean canRename();

    /** Test whether this node can be deleted.
    * @return <CODE>true</CODE> if can
    */
    public abstract boolean canDestroy();

    /**
     * Called when a node is deleted.
     * Generally you would never call this method yourself (only override it);
     * perform modifications on the underlying model itself.
     * <p>
    * The default
    * implementation obtains write access to
    * {@link Children#MUTEX}, and removes
    * the node from its parent (if any). Also fires a property change.
     * <p>Subclasses which return true from {@link #canDestroy} should override
     * this method to remove the associated model object from its parent. There
     * is no need to call the super method in this case.
     * <p>There is no guarantee that after this method has been called, other
     * methods such as {@link #getIcon} will not also be called for a little while.
    * @exception IOException if something fails
    */
    public void destroy() throws IOException {
        Children.MUTEX.postWriteRequest(
            new Runnable() {
                public void run() {
                    Children p = getParentChildren();

                    if (p != null) {
                        // remove itself from parent
                        p.remove(new Node[] { Node.this });
                    }

                    // sets the valid flag to false and fires prop. change
                    fireNodeDestroyed();
                }
            }
        );
    }

    /** Get the list of property sets for this node.
    * E.g. typically there may be one for normal Bean properties, one for expert
    * properties, and one for hidden properties.
    *
    * @return the property sets
    */
    public abstract PropertySet[] getPropertySets();

    /** Called when a node is to be copied to the clipboard.
    * @return the transferable object representing the
    *    content of the clipboard
    * @exception IOException when the
    *    copy cannot be performed
    */
    public abstract Transferable clipboardCopy() throws IOException;

    /** Called when a node is to be cut to the clipboard.
    * @return the transferable object representing the
    *    content of the clipboard
    * @exception IOException when the
    *    cut cannot be performed
    */
    public abstract Transferable clipboardCut() throws IOException;

    /** Called when a drag is started with this node.
    * The node can attach a transfer listener to ExTransferable and
    * will be then notified about progress of the drag (accept/reject).
    *
    * @return transferable to represent this node during a drag
    * @exception IOException if a drag cannot be started
    */
    public abstract Transferable drag() throws IOException;

    /** Test whether this node permits copying.
    * @return <code>true</code> if so
    */
    public abstract boolean canCopy();

    /** Test whether this node permits cutting.
    * @return <code>true</code> if so
    */
    public abstract boolean canCut();

    /** Determine which paste operations are allowed when a given transferable is in the clipboard.
    * For example, a node representing a Java package will permit classes to be pasted into it.
    * @param t the transferable in the clipboard
    * @return array of operations that are allowed
    */
    public abstract PasteType[] getPasteTypes(Transferable t);

    /** Determine if there is a paste operation that can be performed
    * on provided transferable. Used by drag'n'drop code to check
    * whether the drop is possible.
    *
    * @param t the transferable
    * @param action the drag'n'drop action to do DnDConstants.ACTION_MOVE, ACTION_COPY, ACTION_LINK
    * @param index index between children the drop occurred at or -1 if not specified
    * @return null if the transferable cannot be accepted or the paste type
    *    to execute when the drop occurs
    */
    public abstract PasteType getDropType(Transferable t, int action, int index);

    /** Get the new types that can be created in this node.
    * For example, a node representing a Java package will permit classes to be added.
    * @return array of new type operations that are allowed
    */
    public abstract NewType[] getNewTypes();

    /** Get the set of actions that are associated with this node.
     * This set is used to construct the context menu for the node.
     *
     * <P>
     * By default this method delegates to the deprecated getActions or getContextActions
     * method depending on the value of supplied argument.
     * <P>
     * It is supposed to be overridden by subclasses accordingly.
     *
     * @param context whether to find actions for context meaning or for the
     *   node itself
     * @return a list of actions (you may include nulls for separators)
     * @since 3.29
     */
    public Action[] getActions(boolean context) {
        return context ? getContextActions() : getActions();
    }

    /** Get the set of actions associated with this node.
    * This may be used e.g. in constructing a {@link #getContextMenu context menu}.
    *
    * <P>
    * By default returns the actions in {@link NodeOp#getDefaultActions}.
    *
    * @return system actions appropriate to the node
    * @deprecated Use getActions (false) instead.
    */
    @Deprecated
    public SystemAction[] getActions() {
        return NodeOp.getDefaultActions();
    }

    /** Get a special set of actions
    * for situations when this node is displayed as a context.
    * <p>For example, right-clicking on a parent node in a hierarchical view (such as
    * the normal Explorer) should use <code>getActions</code>. However, if this node
    * is serving as the parent of a (say) a window tab full of icons (e.g., in
    * <code>IconView</code>), and the users right-clicks on
    * the empty space in this pane, then this method should be used to get
    * the appropriate actions for a context menu.
    * <p>Note that in the Windows UI system, e.g., these action sets are quite different.
    *
    * @return actions for a context. In the default implementation, same as {@link #getActions}.
    * @deprecated Use getActions (true) instead.
    */
    @Deprecated
    public SystemAction[] getContextActions() {
        return getActions();
    }

    /** Gets the default action for this node.
     * @return <code>null</code> indicating there should be none default action
     * @deprecated Use {@link #getPreferredAction} instead.
     */
    @Deprecated
    public SystemAction getDefaultAction() {
        return null;
    }

    /** Gets the preferred action for this node.
     * This action can but need not to be one from the action array returned
     * from {@link #getActions(boolean)}.
     * In case it is, the context menu created from those actions
     * is encouraged to highlight the preferred action.
     * Override in subclasses accordingly.
     *
     * @return the preferred action, or <code>null</code> if there is none
     * @since 3.29
     */
    public Action getPreferredAction() {
        return getDefaultAction();
    }

    /**
     * Makes a context menu for this node.
     * <p>Component action maps are not taken into consideration.
     * {@link Utilities#actionsToPopup(Action[], Component)} is a better choice
     * if you want to use actions such as "Paste" which look at action maps.
    * @return the context menu as per {@link NodeOp#findContextMenu}
    */
    public final JPopupMenu getContextMenu() {
        return NodeOp.findContextMenu(new Node[] {this});
    }

    /** Test whether there is a customizer for this node. If true,
    * the customizer can be obtained via {@link #getCustomizer}.
    *
    * @return <CODE>true</CODE> if there is a customizer
    */
    public abstract boolean hasCustomizer();

    /** Get the customizer component.
    * @return the component, or <CODE>null</CODE> if there is no customizer
    */
    public abstract java.awt.Component getCustomizer();

    /** Get a cookie for this node.
    * <P>
    * The set of cookies can change. If a node changes its set of
    * cookies, it fires a property change event with {@link #PROP_COOKIE}.
    * <P>
    * If the Node was constructed with a <code>Lookup</code> in constructor
    * than this method delegates to the provided lookup object.
    *
    * @param type the representation class of the cookie
    * @return a cookie assignable to that class, or <code>null</code> if this node has no such cookie
    * @see Lookup
    */
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        Lookup l = internalLookup(true);

        if (l != null) {
            Object obj = l.lookup(type);
            if (Node.Cookie.class.isInstance(obj)) {
                return type.cast(obj);
            }
            CookieSet.enhancedQueryMode(l, type);
        }

        return null;
    }

    /** Obtains a Lookup representing additional content of this Node.
     * If the lookup was provided in a constructor, it is returned here,
     * if not, a lookup based on the content of {@link #getCookie(java.lang.Class)}
     * method is provided.
     *
     * @return lookup for this node
     * @since 3.11
     */
    public final Lookup getLookup() {
        synchronized (listeners) {
            Lookup l = internalLookup(true);

            if (l != null) {
                return l;
            }

            l = findDelegatingLookup();

            if (l != null) {
                return l;
            }

            // create new lookup and use it
            NodeLookup nl = new NodeLookup(this);
            registerDelegatingLookup(nl);

            return nl;
        }
    }

    /** Return a variant of the display name containing HTML markup
     * conforming to the limited subset of font-markup HTML supported by
     * the lightweight HTML renderer <code>org.openide.awt.HtmlRenderer</code>
     * (font color, bold, italic and strike-through supported; font
     * colors can be UIManager color keys if they are prefixed with
     * a ! character, i.e. <code>&lt;font color='!controlShadow'&gt;</code>).
     * Enclosing <code>&lt;html&gt;</code> tags are not needed. If returning non-null, HTML
     * markup characters that should be literally rendered must be
     * escaped (<code>&gt;</code> becomes <code>&amp;gt;</code> and so forth).
     * <p><strong>This method should return either an HTML display name
     * or null; it should not return the non-HTML display name.</strong>
     * <p>
     * Note the specified foreground color has to have a high luminescence difference
     * to the background color in order to be used (displayed).
     * If not, the specified foreground color will be ignored and the default color will be used.
     * Luminescence of a color is calculated as following:
     * <code>(Red value * 299 + Green value * 587 + Blue value * 114) / 1000</code>
     * and the difference has to be greater or equal to 80.
     * <p>
     * Note there is no property corresponding to the HTML display name -
     * if it should change, a change in the display name should be fired; this
     * should not be a mechanism for returning anything other than a marked
     * up version of the return value of <code>getDisplayName</code>.
     *
     * @see org.openide.awt.HtmlRenderer
     * @since 4.30
     * @return a String containing conformant HTML markup which
     *  represents the display name, or null.  The default implementation
     *  returns null.  */
    public String getHtmlDisplayName() {
        return null;
    }

    /** Register delegating lookup so it can always be found.
     */
    final void registerDelegatingLookup(NodeLookup l) {
        // to have just one thread accessing the static lookups variable
        synchronized (lookups) {
            lookups.put(listeners, new WeakReference<Lookup>(l));
        }
    }

    /** Finds delegating lookup that was previously registered
     * @return the lookup or null if nothing was registered or the
     *    lookup was GCed.
     */
    final Lookup findDelegatingLookup() {
        Reference<Lookup> ref = lookups.get(listeners);

        return (ref == null) ? null : ref.get();
    }

    /** Obtain handle for this node (for serialization).
    * The handle can be serialized and {@link Handle#getNode} used after
    * deserialization to obtain the original node.
    *
    * @return the handle, or <code>null</code> if this node is not persistable
    */
    public abstract Node.Handle getHandle();

    /** Add a listener to changes in the node's intrinsic properties (name, cookies, etc.).
     * <P>
     * The listener is not notified about changes in subnodes until the
     * method <CODE>getChildren().getNodes()</CODE> is called.
     * @param l the listener to add
     */
    public final void addNodeListener(NodeListener l) {
        listeners.add(NodeListener.class, l);
        listenerAdded();
    }

    /** A method to notify FilterNode that a listenerAdded has been added */
    void listenerAdded() {
    }
    
    final int getNodeListenerCount() {
        return listeners.getListenerCount(NodeListener.class);
    }

    /** Remove a node listener.
     * @param l the listener
    */
    public final void removeNodeListener(NodeListener l) {
        listeners.remove(NodeListener.class, l);
    }

    /** Add a listener to the node's computed Bean properties.
     * @param l the listener
    */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        int count = -1;

        if (err.isLoggable(Level.FINE)) {
            count = getPropertyChangeListenersCount();
        }

        listeners.add(PropertyChangeListener.class, l);

        if (err.isLoggable(Level.FINE)) {
            err.log(
                Level.FINE,
                "ADD - " + getName() + " [" + count + "]->[" + getPropertyChangeListenersCount() + "] " + l
            );
        }

        notifyPropertyChangeListenerAdded(l);
    }

    /** Called to notify subclasses (FilterNode) about addition of
     * PropertyChangeListener.
     */
    void notifyPropertyChangeListenerAdded(PropertyChangeListener l) {
    }

    /** Returns the number of property change listeners attached to this node
     */
    int getPropertyChangeListenersCount() {
        return listeners.getListenerCount(PropertyChangeListener.class);
    }

    /** Allows to figure out, whether the node has any
     * PropertyChangeListeners attached.
     * @return True if node has one or more PropertyChangeListeners attached.
     * @since 1.36
     */
    protected final boolean hasPropertyChangeListener() {
        return getPropertyChangeListenersCount() > 0;
    }

    /** Remove a Bean property change listener.
     * @param l the listener
    */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        int count = -1;

        if (err.isLoggable(Level.FINE)) {
            count = getPropertyChangeListenersCount();
        }

        listeners.remove(PropertyChangeListener.class, l);

        if (err.isLoggable(Level.FINE)) {
            err.log(
                Level.FINE,
                "RMV - " + getName() + " [" + count + "]->[" + getPropertyChangeListenersCount() + "] " + l
            );
        }

        notifyPropertyChangeListenerRemoved(l);
    }

    /** Called to notify subclasses (FilterNode) about removal of
     * PropertyChangeListener.
     */
    void notifyPropertyChangeListenerRemoved(PropertyChangeListener l) {
    }

    /** Fire a property change event.
    *
    * @param name name of changed property (from {@link #getPropertySets}); may be null
    * @param o old value; may be null
    * @param n new value; may be null
    * @see PropertyChangeEvent
    */
    protected final void firePropertyChange(String name, Object o, Object n) {
        // First check if this property actually exists - if not warn! See #31413.
        if (err.isLoggable(Level.WARNING) && (name != null) && propertySetsAreKnown()) {
            Node.PropertySet[] pss = getPropertySets();
            boolean exists = false;

            for (int i = 0; i < pss.length; i++) {
                Node.Property[] ps = pss[i].getProperties();

                for (int j = 0; j < ps.length; j++) {
                    if (ps[j].getName().equals(name)) {
                        exists = true;

                        break;
                    }
                }
            }

            if (!exists) {
                synchronized (warnedBadProperties) {
                    String clazz = getClass().getName();

                    if (warnedBadProperties.add(clazz + "." + name)) {
                        StringWriter w = new StringWriter();
                        IllegalStateException ise = new IllegalStateException("Warning - the node \"" +
                            getDisplayName() +
                            "\" [" +
                            clazz +
                            "] is trying to fire the property " +
                            name +
                            " which is not included in its property sets. This is illegal. See IZ #31413 for details."
                        ); // NOI18N
                        ise.printStackTrace(new PrintWriter(w));
                        Logger.getLogger(Node.class.getName()).warning(w.toString());
                    }
                }
            }
        }

        // do not fire if the values are the same
        if ((o != null) && (n != null) && ((o == n) || o.equals(n))) {
            return;
        }

        PropertyChangeEvent ev = null;

        Object[] listeners = this.listeners.getListenerList();

        Set<PropertyChangeListener> dormant = Collections.emptySet();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PropertyChangeListener.class) {
                // Lazily create the event:
                if (ev == null) {
                    ev = new PropertyChangeEvent(this, name, o, n);
                }
                final PropertyChangeListener l = (PropertyChangeListener) listeners[i + 1];
                l.propertyChange(ev);
                
                dormant = FilterNode.PropertyChangeAdapter.checkDormant(l, dormant);
            }
        }
        removeDormant(dormant, PropertyChangeListener.class);
    }

    /**
     * If true, property sets have definitely been computed, and it is fine
     * to call {@link #getPropertySets} without fear of killing laziness.
     * Used from {@link #firePropertyChange} to only check for bad properties
     * if the set of properties has already been computed. Otherwise, don't
     * bother. Subclasses may override - {@link AbstractNode} does.
     */
    boolean propertySetsAreKnown() {
        return false;
    }

    /** Allow subclasses that override the getName method to fire
    * the changes of the name by itself. Please notice that default
    * implementation of setName will fire the change by itself.
    */
    protected final void fireNameChange(String o, String n) {
        fireOwnPropertyChange(PROP_NAME, o, n);
    }

    /** Allow subclasses that override the getDisplayName method to fire
    * the changes of the name by itself. Please notice that default
    * implementation of setDisplayName will fire the change by itself.
    */
    protected final void fireDisplayNameChange(String o, String n) {
        fireOwnPropertyChange(PROP_DISPLAY_NAME, o, n);
    }

    /** Allow subclasses that override the getShortDescription method to fire
    * the changes of the description by itself. Please notice that default
    * implementation of setShortDescription will fire the change by itself.
    */
    protected final void fireShortDescriptionChange(String o, String n) {
        fireOwnPropertyChange(PROP_SHORT_DESCRIPTION, o, n);
    }

    /** Fire a change event for {@link #PROP_ICON}.
    */
    protected final void fireIconChange() {
        fireOwnPropertyChange(PROP_ICON, null, null);
    }

    /** Fire a change event for {@link #PROP_OPENED_ICON}.
    */
    protected final void fireOpenedIconChange() {
        fireOwnPropertyChange(PROP_OPENED_ICON, null, null);
    }

    /** Fires info about some structural change in children. Providing
    * type of operation and set of children changed generates event describing
    * the change.
    *
    *
    * @param addAction <CODE>true</CODE> if the set of children has been added,
    *   false if it has been removed
    * @param delta the array with changed children
    * @param from the array of nodes to take indices from.
    *   Can be null if one should find indices from current set of nodes
    */
    final void fireSubNodesChange(boolean addAction, Node[] delta, Node[] from) {
        Set<NodeListener> dormant = Collections.emptySet();
        try {
            // enter to readAccess to prevent firing another event before all listeners receive current event
            Children.PR.enterReadAccess();
            if (err.isLoggable(Level.FINER)) {
                err.finer("fireSubNodesChange() " + this); // NOI18N
                err.finer("    added: " + addAction); // NOI18N
                err.finer("    delta: " + Arrays.toString(delta)); // NOI18N
                err.finer("    from: " + Arrays.toString(from)); // NOI18N
            }

            NodeMemberEvent ev = null;
            Object[] listeners = this.listeners.getListenerList();

            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == NodeListener.class) {
                    // Lazily create the event:
                    if (ev == null) {
                        ev = new NodeMemberEvent(this, addAction, delta, from);
                    }
                    final NodeListener l = (NodeListener) listeners[i + 1];

                    if (addAction) {
                        l.childrenAdded(ev);
                    } else {
                        l.childrenRemoved(ev);
                    }
                    dormant = FilterNode.NodeAdapter.checkDormant(l, dormant);
                }
            }
        } finally {
            Children.PR.exitReadAccess();
        }
        removeDormant(dormant, NodeListener.class);
    }
    
    /** Fires that some indexes has been removed.
     *
     * @param added
     * @param idxs removed indicies
     */
    final void fireSubNodesChangeIdx(boolean added, int[] idxs, Children.Entry sourceEntry, List<Node> current, List<Node> previous) {
        Set<NodeListener> dormant = Collections.emptySet();
        try {
            // enter to readAccess to prevent firing another event before all listeners receive current event
            Children.PR.enterReadAccess();
            if (err.isLoggable(Level.FINER)) {
                err.finer("fireSubNodesChangeIdx() " + this); // NOI18N
                err.finer("    added: " + added); // NOI18N
                err.finer("    idxs: " + Arrays.toString(idxs)); // NOI18N
                err.finer("    sourceEntry: " + sourceEntry); // NOI18N
                err.finer("    current size: " + current.size() + "    current: " + current); // NOI18N
                err.finer(previous != null ? ("    previous size: " + previous.size() + "    previous: " + previous) : "    null"); // NOI18N
            }

            NodeMemberEvent ev = null;
            Object[] tmpListeners = this.listeners.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = tmpListeners.length - 2; i >= 0; i -= 2) {
                if (tmpListeners[i] == NodeListener.class) {
                    // Lazily create the event:
                    if (ev == null) {
                        ev = new NodeMemberEvent(this, added, idxs, current, previous);
                        ev.sourceEntry = sourceEntry;
                    }
                    final NodeListener l = (NodeListener) tmpListeners[i + 1];
                    if (added) {
                        l.childrenAdded(ev);
                    } else {
                        l.childrenRemoved(ev);
                    }
                    dormant = FilterNode.NodeAdapter.checkDormant(l, dormant);
                }
            }
        } finally {
            Children.PR.exitReadAccess();
        }
        removeDormant(dormant, NodeListener.class);
    }     

    /** Fires info about reordering of some children.
    *
    * @param indices array of integers describing the permutation
    */
    final void fireReorderChange(int[] indices) {
        Set<NodeListener> dormant = Collections.emptySet();
        NodeReorderEvent ev = null;

        Object[] listeners = this.listeners.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == NodeListener.class) {
                // Lazily create the event:
                if (ev == null) {
                    ev = new NodeReorderEvent(this, indices);
                }
                final NodeListener l = (NodeListener) listeners[i + 1];

                l.childrenReordered(ev);
                dormant = FilterNode.NodeAdapter.checkDormant(l, dormant);
            }
        }
        removeDormant(dormant, NodeListener.class);
    }

    /** To all node listeners fire node destroyed notification.
    */
    protected final void fireNodeDestroyed() {
        Set<NodeListener> dormant = Collections.emptySet();
        NodeEvent ev = null;

        Object[] listeners = this.listeners.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == NodeListener.class) {
                // Lazily create the event:
                if (ev == null) {
                    ev = new NodeEvent(this);
                }
                final NodeListener l = (NodeListener) listeners[i + 1];

                l.nodeDestroyed(ev);
                dormant = FilterNode.NodeAdapter.checkDormant(l, dormant);
            }
        }
        removeDormant(dormant, NodeListener.class);
    }

    /** Fires info about change of parent node.
    * @param o old node
    * @param n new parent
    */
    final void fireParentNodeChange(Node o, Node n) {
        fireOwnPropertyChange(PROP_PARENT_NODE, o, n);
    }

    /** Fires a (Bean) property change event (for {@link #PROP_PROPERTY_SETS}).
    * @param o the old set
    * @param n the new set
    */
    protected final void firePropertySetsChange(PropertySet[] o, PropertySet[] n) {
        fireOwnPropertyChange(PROP_PROPERTY_SETS, o, n);
    }

    /** Fires a change event for {@link #PROP_COOKIE}.
    * The old and new values are set to null.
    */
    protected final void fireCookieChange() {
        Lookup l = findDelegatingLookup();

        if (l instanceof NodeLookup && updateNow(this)) {
            Set<Node> prev = blockEvents();
            try {
                ((NodeLookup) l).updateLookupAsCookiesAreChanged(null);
            } finally {
                unblockEvents(prev);
            }
        }

        fireOwnPropertyChange(PROP_COOKIE, null, null);
    }
    
    private static final ThreadLocal<Set<Node>> BLOCK_EVENTS = new ThreadLocal<Set<Node>>();
    static Set<Node> blockEvents() {
        Set<Node> prev = BLOCK_EVENTS.get();
        if (prev != null) {
            return prev;
        }
        BLOCK_EVENTS.set(new HashSet<Node>());
        return null;
    }
    
    private static boolean updateNow(Node n) {
        final Set<Node> set = BLOCK_EVENTS.get();
        if (set == null) {
            return true;
        }
        set.add(n);
        return false;
    }
    
    static void unblockEvents(Set<Node> prev) {
        final Set<Node> set = BLOCK_EVENTS.get();
        if (prev == null) {
            while (!set.isEmpty()) {
                Node[] arr = set.toArray(new Node[0]);
                for (Node n : arr) {
                    Lookup l = n.findDelegatingLookup();
                    if (l instanceof NodeLookup) {
                        ((NodeLookup) l).updateLookupAsCookiesAreChanged(null);
                    }
                }
                set.removeAll(Arrays.asList(arr));
            }
        }
        BLOCK_EVENTS.set(prev);
    }

    /** Fires info about change of own property.
    * @param name name of property
    * @param o old value
    * @param n new value
    */
    final void fireOwnPropertyChange(String name, Object o, Object n) {
        // do not fire if the values are the same
        if ((o != null) && (n != null) && ((o == n) || o.equals(n))) {
            return;
        }

        Set<NodeListener> dormant = Collections.emptySet();
        PropertyChangeEvent ev = null;

        Object[] listeners = this.listeners.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == NodeListener.class) {
                // Lazily create the event:
                if (ev == null) {
                    ev = new PropertyChangeEvent(this, name, o, n);
                }
                final NodeListener l = (NodeListener) listeners[i + 1];
                l.propertyChange(ev);
                dormant = FilterNode.NodeAdapter.checkDormant(l, dormant);
            }
        }
        removeDormant(dormant, NodeListener.class);
    }

    /** Compares for equality. Does special treatment of
     * FilterNodes. If argument is FilterNode then this node can be
     * equal with it if it is its original.
     *
     * @param obj object to compare
     * @return true if the obj is <code>==</code> or is filter node of this node
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FilterNode) {
            return ((FilterNode) obj).equals(this);
        }

        return this == obj;
    }

    // For the benefit of FindBugs.
    public @Override int hashCode() {
        return super.hashCode();
    }

    /** Obtains a resource string from bundle.
    * @param resName resource name
    * @return the string
    */
    static String getString(final String resName) {
        return NbBundle.getBundle(Node.class).getString(resName);
    }

    @Override
    public String toString() {
        return super.toString() + "[Name=" + getName() + ", displayName=" + getDisplayName() + "]"; // NOI18N
    }

    private <T extends PropertyChangeListener> void removeDormant(Set<T> dormant, Class<T> c) {
        for (T l : dormant) {
            listeners.remove(c, l);
        }
    }

    /** Marker interface for all cookies.
    * <P>
    * Most examples are present in {@link org.openide.cookies}.
    */
    public static interface Cookie {
    }

    /** Serializable node reference. The node should not
    * be serialized directly but via this handle. One can obtain a handle
    * by a call to {@link Node#getHandle}.
    * <P>
    * If that methods returns a non-<code>null</code> value, one can serialize it,
    * and after deserialization
    * use {@link #getNode} to obtain the original node.
    * 
    * <p><b>Related documentation</b>
    * 
    * <ul>
    * <li><a href="https://netbeans.apache.org/blogs/geertjan/serializing_nodes.html">Serializing Nodes</a> 
    * <li><a href="https://netbeans.apache.org/blogs/geertjan/multiple_nodes_serialization.html">Serializing Multiple Nodes</a> 
    * </ul>
    * 
    */
    public static interface Handle extends java.io.Serializable {
        /** @deprecated Only public by accident. */
        @Deprecated
        /* public static final */ long serialVersionUID = -4518262478987434353L;

        /** Reconstitute the node for this handle.
        *
        * @return the node for this handle
        * @exception IOException if the node cannot be created
        */
        public Node getNode() throws java.io.IOException;
    }

    /** Class that represents one set of properties. A usual bean has three
    * sets of properties: normal, expert, and events.
    * <p>You may associate context help with this object, if desired, by setting
    * a {@link FeatureDescriptor#setValue custom property} with the name <code>helpID</code>
    * and value of type <code>String</code> giving a help ID.
    * Normally this is unnecessary as help for the whole {@link Node} will be used by default.
    */
    public abstract static class PropertySet extends FeatureDescriptor {
        /** Default constructor. */
        public PropertySet() {
        }

        /** Create a property set.
         * @param name system name of the property set
        * @param displayName human presentable name
        * @param shortDescription description for the set
        */
        public PropertySet(String name, String displayName, String shortDescription) {
            super.setName(name);
            super.setDisplayName(displayName);
            super.setShortDescription(shortDescription);
        }

        /** Get the list of contained properties.
        * This list can contain both {@link Node.Property} and {@link Node.IndexedProperty} elements.
        *
        * @return the properties
        */
        public abstract Property<?>[] getProperties();

        /* Compares just the names.
         * @param propertySet The object to compare to
         */
        @Override
        public boolean equals(Object propertySet) {
            if (!(propertySet instanceof PropertySet)) {
                return false;
            }
            final String n1 = ((PropertySet) propertySet).getName();
            if (n1 == null) {
                return getName() == null;
            }
            return n1.equals(getName());
        }

        /* Returns a hash code value for the object.
         *
         * @return int hashcode
         */
        @Override
        public int hashCode() {
            final String n = getName();
            return n == null ? 0 : n.hashCode();
        }

        /** Return a variant of the display name containing HTML markup
         * conforming to the limited subset of font-markup HTML supported by
         * the lightweight HTML renderer org.openide.awt.HtmlRenderer
         * (font color, bold, italic and strikethrough supported; font
         * colors can be UIManager color keys if they are prefixed with
         * a ! character, i.e. &lt;font color=&amp;'controlShadow'&gt;).
         * Enclosing HTML tags are not needed.
         * <p><strong>This method should return either an HTML display name
         * or null; it should not return the non-HTML display name if no
         * markup is needed.</strong>
         *
         * @see org.openide.awt.HtmlRenderer
         * @since 4.30
         * @return a String containing conformant, legal HTML markup which
         *  represents the display name, or null.  The default implementation
         *  returns null.  */
        public String getHtmlDisplayName() {
            return null;
        }
    }

    /** Description of a Bean property on a node, and operations on it.
    * <p>You may associate context help with this object, if desired, by setting
    * a {@link FeatureDescriptor#setValue custom property} with the name <code>helpID</code>
    * and value of type <code>String</code> giving a help ID.
    * Normally this is unnecessary as help for the whole {@link Node} will be used by default.
     * <p><strong>Important:</strong> the {@link FeatureDescriptor#getName code name} you use for the
     * property is relevant not only for making properties of a node unique, but also for
     * {@link Node#firePropertyChange firing property changes}.
     * @param <T> the type of bean
    */
    public abstract static class Property<T> extends FeatureDescriptor {
        /**
         * Contains classNames of incorrectly implemented properties which have
         * been already logged by an ErrorManager.<br>
         * For more information see the
         * <a href="http://openide.netbeans.org/issues/show_bug.cgi?id=51907">
         * discussion in issuezilla</a>
         */
        private static final Set<String> warnedNames = new HashSet<String>();

        /** type that this property works with */
        private Class<T> type;

        //Soft caching of property editor references to improve JTable
        //property sheet performance
        private PropertyEditorRef edRef = null;

        /** Constructor.
        * @param valueType type of the property
        */
        public Property(Class<T> valueType) {
            this.type = valueType;
            super.setName(""); // NOI18N
        }

        /** Get the value type. This is the representation class of the property.
        * Remember that e.g. {@link Boolean Boolean.class} means that values are <code>Boolean</code>
        * objects; to specify the primitive type, use e.g. {@link Boolean#TYPE}.
        * In the latter case, {@link #getValue} and {@link #setValue} will still operate on the wrapper object.
        * @return the type
        */
        public Class<T> getValueType() {
            return type;
        }

        /** Test whether the property is readable.
        * @return <CODE>true</CODE> if it is
        */
        public abstract boolean canRead();

        /** Get the value.
        * @return the value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception InvocationTargetException an exception during invocation
        */
        public abstract T getValue() throws IllegalAccessException, InvocationTargetException;

        /** Test whether the property is writable.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        public abstract boolean canWrite();

        /** Set the value.
        * @param val the new value of the property
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public abstract void setValue(T val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

        /** Test whether the property had a default value.
        * @return <code>true</code> if it does (<code>false</code> by default)
        */
        public boolean supportsDefaultValue() {
            return false;
        }

        /** Restore this property to its default value, if supported.
        * In the default implementation, does nothing.
        * Typically you would just call e.g. <code>setValue(default)</code>.
        * Note that it is not permitted for this call to throw {@link IllegalArgumentException},
        * though the other two exceptions from {@link #setValue} may be passed through.
        * @exception IllegalAccessException cannot access the called method
        * @exception InvocationTargetException an exception during invocation
        */
        public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
        }

        /**
         * This method indicates whether the current value is the same as
         * the value that would otherwise be restored by calling
         * <code>restoreDefaultValue()</code> (if <code>supportsDefaultValue()</code>
         * returns true). The default implementation returns true and
         * it is recommended to also return true when <code>supportsDefaultValue()</code>
         * returns false (if we do not support default value any value can
         * be considered as the default). If <code>supportsDefaultValue()</code>
         * returns false this method will not be called by the default
         * implementation of property sheet.
         * @since 3.19
         */
        public boolean isDefaultValue() {
            String name = getClass().getName();

            // Issue 51907 backward compatibility
            if (supportsDefaultValue() && warnedNames.add(name)) {
                Logger.getLogger(Node.Property.class.getName()).log(
                    Level.WARNING,
                    "Class " + name + " must override isDefaultValue() since it " +
                    "overrides supportsDefaultValue() to be true"
                );
            }

            return true;
        }

        /** Get a property editor for this property.
         * The default implementation uses standard Java
         * {@link java.beans.PropertyEditorManager}. If a property
         * editor is found, its instance is cached using {@link SoftReference}.
         * Caching happens per thread - e.g. it is guaranteed that multiple 
         * threads accessing the editor will get different instance.
         * 
         * @return the property editor, or <CODE>null</CODE> if there is no editor
         */
        public PropertyEditor getPropertyEditor() {
            if (type == null) {
                return null;
            }

            PropertyEditor result = null;

            if (edRef != null) {
                result = edRef.get();
            }

            if (result == null) {
                result = java.beans.PropertyEditorManager.findEditor(type);
                if (result != null && (
                    result.getClass().getName().equals("sun.beans.editors.EnumEditor") // NOI18N
                    ||
                    result.getClass().getName().equals("com.sun.beans.editors.EnumEditor") // NOI18N
                )) { 
                    result = null;
                }
                edRef = new PropertyEditorRef(result);
            }

            return result;
        }

        /* Standard equals implementation for all property
        * classes.
        * @param property The object to compare to
        */
        @Override
        public boolean equals(Object property) {
            // fix #32845 - check for non-matching types and also for null values
            // coming in input parameter 'property'
            if (!(property instanceof Property)) {
                return false;
            }

            Class<?> propValueType = ((Property) property).getValueType();
            Class<?> valueType = getValueType();

            if (((propValueType == null) && (valueType != null)) || ((propValueType != null) && (valueType == null))) {
                return false;
            }

            return ((Property) property).getName().equals(getName()) &&
            (((propValueType == null) && (valueType == null)) || propValueType.equals(valueType));
        }

        /* Returns a hash code value for the object.
        *
        * @return int hashcode
        */
        @Override
        public int hashCode() {
            Class<?> valueType = getValueType();

            return getName().hashCode() * ((valueType == null) ? 1 : valueType.hashCode());
        }

        /** Return a variant of the display name containing HTML markup
         * conforming to the limited subset of font-markup HTML supported by
         * the lightweight HTML renderer {@link org.openide.awt.HtmlRenderer}
         * (font color, bold, italic and strike-through supported; font
         * colors can be UIManager color keys if they are prefixed with
         * a ! character, i.e. &lt;font color=&amp;'controlShadow'&gt;).
         * Enclosing HTML tags are not needed.
         * <p>
         * <strong>This method should return either an HTML display name
         * or null; it should not return the non-HTML display name.</strong>
         *
         * @see org.openide.awt.HtmlRenderer
         * @since 4.30
         * @return a String containing conformant, legal HTML markup which
         *  represents the display name, or null.  The default implementation
         *  returns null.  */
        public String getHtmlDisplayName() {
            return null;
        }
    }

    /** Description of an indexed property and operations on it.
     * @param <T> type of the whole property
     * @param <E> type of one element
    */
    public abstract static class IndexedProperty<T,E> extends Node.Property<T> {
        /** type of element that this property works with */
        private Class<E> elementType;

        /** Constructor.
        * @param valueType type of the property
        */
        public IndexedProperty(Class<T> valueType, Class<E> elementType) {
            super(valueType);
            this.elementType = elementType;
        }

        /** Test whether the property is readable by index.
        * @return <CODE>true</CODE> if so
        */
        public abstract boolean canIndexedRead();

        /** Get the element type of the property (not the type of the whole property).
        * @return the type
        */
        public Class<E> getElementType() {
            return elementType;
        }

        /** Get the value of the property at an index.
        *
        * @param index the index
        * @return the value at that index
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public abstract E getIndexedValue(int index)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

        /** Test whether the property is writable by index.
        * @return <CODE>true</CODE> if so
        */
        public abstract boolean canIndexedWrite();

        /** Set the value of the property at an index.
        *
        * @param indx the index
        * @param val the value to set
        * @exception IllegalAccessException cannot access the called method
        * @exception IllegalArgumentException wrong argument
        * @exception InvocationTargetException an exception during invocation
        */
        public abstract void setIndexedValue(int indx, E val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

        /** Get a property editor for individual elements in this property.
        * @return the property editor for elements
        */
        public PropertyEditor getIndexedPropertyEditor() {
            return java.beans.PropertyEditorManager.findEditor(elementType);
        }

        /* Standard equals implementation for all property
        * classes.
        * @param property The object to compare to
        */
        @Override
        public boolean equals(Object property) {
            try {
                if (!super.equals(property)) {
                    return false;
                }

                Class<?> propElementType = ((IndexedProperty) property).getElementType();
                Class<?> elementType = getElementType();

                if (
                    ((propElementType == null) && (elementType != null)) ||
                        ((propElementType != null) && (elementType == null))
                ) {
                    return false;
                }

                return (((propElementType == null) && (elementType == null)) || propElementType.equals(elementType));
            } catch (ClassCastException e) {
                return false;
            }
        }

        /* Returns a hash code value for the object.
        *
        * @return int hashcode
        */
        @Override
        public int hashCode() {
            Class<?> ementType = getElementType();

            return super.hashCode() * ((elementType == null) ? 1 : elementType.hashCode());
        }
    }

    /** Special subclass of EventListenerList that can also listen on changes in
     * a lookup.
     */
    private final class LookupEventList extends EventListenerList implements LookupListener {
        public final Lookup lookup;
        private Lookup.Result<Node.Cookie> result;

        public LookupEventList(Lookup l) {
            this.lookup = l;
        }

        public Lookup init(boolean init) {
            boolean doInit = false;

            synchronized (INIT_LOCK) {
                if (init && (result == null)) {
                    result = lookup.lookup(TEMPL_COOKIE);
                    assert result != null : "Null lookup result from " + lookup + " in " + Node.this;
                    result.addLookupListener(this);
                    doInit = true;
                }
            }

            if (doInit) {
                result.allItems();
            }

            return lookup;
        }

        public void resultChanged(LookupEvent ev) {
            if (Node.this instanceof FilterNode) {
                FilterNode f = (FilterNode) Node.this;

                // See #40734 and NodeLookupTest and CookieActionIsTooSlowTest. 
                if (f.getOriginal() == NodeLookup.NO_COOKIE_CHANGE.get()) {
                    // this is not real cookie change, do not fire it
                    // issue 40734
                    return;
                }
            }

            fireCookieChange();
        }
    }
    
    private static final class PropertyEditorRef extends SoftReference<PropertyEditor> {
        private final Thread createdBy;
        
        public PropertyEditorRef(PropertyEditor referent) {
            super(referent);
            createdBy = Thread.currentThread();
        }

        @Override
        public PropertyEditor get() {
            if (Thread.currentThread() != createdBy) {
                return null;
            }
            return super.get();
        }
    }
}

