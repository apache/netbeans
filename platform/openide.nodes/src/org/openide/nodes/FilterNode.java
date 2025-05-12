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

import java.lang.ref.Reference;
import java.lang.reflect.Method;
import org.openide.nodes.Children.Entry;
import org.openide.nodes.EntrySupportLazyState.EntryInfo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;

import java.awt.Image;
import java.awt.datatransfer.Transferable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.lang.ref.WeakReference;

import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Item;
import org.openide.util.Parameters;


/** A proxy for another node.
* Unless otherwise mentioned, all methods of the original node are delegated to.
* If desired, you can disable delegation of certain methods which are concrete in <code>Node</code>
* by calling {@link #disableDelegation}.
*
* <p><strong>Note:</strong> it is fine to subclass this class and use
* it to filter things. But please do not ever try to cast a node to
* <code>FilterNode</code>: it probably means you are doing something
* wrong. Instead, ask whatever <code>Node</code> you have for a proper
* kind of cookie (e.g. <code>DataObject</code>).
*
* @author Jaroslav Tulach
*/
public class FilterNode extends Node {
    /** Whether to delegate <code>setName</code>. */
    protected static final int DELEGATE_SET_NAME = 1 << 0;

    /** Whether to delegate <code>getName</code>. */
    protected static final int DELEGATE_GET_NAME = 1 << 1;

    /** Whether to delegate <code>setDisplayName</code>. */
    protected static final int DELEGATE_SET_DISPLAY_NAME = 1 << 2;

    /** Whether to delegate <code>getDisplayName</code>. */
    protected static final int DELEGATE_GET_DISPLAY_NAME = 1 << 3;

    /** Whether to delegate <code>setShortDescription</code>. */
    protected static final int DELEGATE_SET_SHORT_DESCRIPTION = 1 << 4;

    /** Whether to delegate <code>getShortDescription</code>. */
    protected static final int DELEGATE_GET_SHORT_DESCRIPTION = 1 << 5;

    /** Whether to delegate <code>destroy</code>. */
    protected static final int DELEGATE_DESTROY = 1 << 6;

    /** Whether to delegate <code>getActions</code>. */
    protected static final int DELEGATE_GET_ACTIONS = 1 << 7;

    /** Whether to delegate <code>getContextActions</code>. */
    protected static final int DELEGATE_GET_CONTEXT_ACTIONS = 1 << 8;

    /** Whether to delegate <code>setValue</code>.
     * @since 4.25
     */
    protected static final int DELEGATE_SET_VALUE = 1 << 9;

    /** Whether to delegate <code>getValue</code>.
     * @since 4.25
     */
    protected static final int DELEGATE_GET_VALUE = 1 << 10;

    /** Mask indicating delegation of all possible methods. */
    private static final int DELEGATE_ALL = DELEGATE_SET_NAME | DELEGATE_GET_NAME | DELEGATE_SET_DISPLAY_NAME |
        DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_SHORT_DESCRIPTION | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_DESTROY |
        DELEGATE_GET_ACTIONS | DELEGATE_GET_CONTEXT_ACTIONS | DELEGATE_SET_VALUE | DELEGATE_GET_VALUE;
    private static final Map<Class<?>,Boolean> overridesGetDisplayNameCache = new WeakHashMap<Class<?>,Boolean>(27);
    private static final Map<Class<?>,Boolean> replaceProvidedLookupCache = new WeakHashMap<Class<?>,Boolean>(27);

    /** Depth of stack trace.
     */
    private static volatile int hashCodeDepth;

    /** node to delegate to */
    private Node original;

    /** listener to property changes,
    * accessible thru getPropertyChangeListener
    */
    private PropertyChangeListener propL;

    /** listener to node changes
    * Accessible thru get node listener
    */
    private NodeListener nodeL;

    // Note: int (not long) to avoid need to ever synchronize when accessing it
    // (Java VM spec does not guarantee that long's will be stored atomically)

    /** @see #delegating */
    private int delegateMask;

    /** Is PropertyChangeListener attached to original node */
    private boolean pchlAttached = false;

    /** children provided or created the default ones? */
    private boolean childrenProvided;

    /** lookup provided or created the default ones? */
    private boolean lookupProvided;

    /** lock used to sync property listener */
    private final Object LISTENER_LOCK = new Object();
    
    static final Logger LOGGER = Logger.getLogger(FilterNode.class.getName());

    /** Create proxy.
    * @param original the node to delegate to
    */
    public FilterNode(Node original) {
        this(original, null);
    }

    /** Create proxy with a different set of children.
    *
    * @param original the node to delegate to
    * @param children a set of children for this node
    */
    public FilterNode(Node original, org.openide.nodes.Children children) {
        this(original, children, new FilterLookup());
    }

    /** Constructs new filter node with a provided children and lookup.
     * The lookup is used to implement {@link FilterNode#getCookie} calls that just call
     * <code>lookup.lookup(clazz)</code>. If this constructor is used,
     * the code shall not override {@link FilterNode#getCookie} method, but do all
     * its state manipulation in the lookup. Look at {@link Node#Node}
     * constructor for best practices usage of this constructor.
     *
     * @param original the node we delegate to
     * @param children the children to use for the filter node or <code>null</code> if
     *    default children should be provided
     * @param lookup lookup to use. Do not pass <CODE>orginal.getLookup()</CODE> into this parameter.
     *        In such case use the {@link #FilterNode(Node, Children)} constructor.
     *
     * @since 4.4
     */
    public FilterNode(Node original, org.openide.nodes.Children children, Lookup lookup) {
        super(
            (children == null) ? (original.isLeaf() ? org.openide.nodes.Children.LEAF : new Children(original)) : children,
            lookup
        );

        Parameters.notNull("original", original);

        this.childrenProvided = children != null;
        this.lookupProvided = lookup != null && !(lookup instanceof FilterLookup);
        this.original = original;
        init();

        Lookup lkp = internalLookup(false);

        if (lkp instanceof FilterLookup) {
            ((FilterLookup) lkp).ownNode(this);
        } else {
            if (lkp == null) {
                // rely on default NodeLookup around getCookie. 
                getNodeListener();
            }
        }
    }

    /** Overrides package private method of a node that allows us to say
     * that the lookup provided in the constructor should be replaced by
     * something else
     *
     * @param lookup
     * @return lookup or null
     */
    @Override
    final Lookup replaceProvidedLookup(Lookup lookup) {
        synchronized (replaceProvidedLookupCache) {
            Boolean b = replaceProvidedLookupCache.get(getClass());

            if (b == null) {
                b = !overridesAMethod("getCookie", Class.class); // NOI18N
                replaceProvidedLookupCache.put(getClass(), b);
            }

            return b ? lookup : null;
        }
    }

    /** Checks whether subclass overrides a method
     */
    private boolean overridesAMethod(String name, Class... arguments) {
        if (getClass() == FilterNode.class) {
            return false;
        }

        // we are subclass of FilterNode
        try {
            Method m = getClass().getMethod(name, arguments);

            if (m.getDeclaringClass() != FilterNode.class) {
                // ok somebody overriden getCookie method
                return true;
            }
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    /** Initializes the node.
    */
    private void init() {
        delegateMask = DELEGATE_ALL;
    }

    @Override
    void notifyPropertyChangeListenerAdded(PropertyChangeListener l) {
        if (!pchlAttached) {
            original.addPropertyChangeListener(getPropertyChangeListener());
            pchlAttached = true;
        }
    }

    @Override
    void notifyPropertyChangeListenerRemoved(PropertyChangeListener l) {
        if (getPropertyChangeListenersCount() == 0) {
            original.removePropertyChangeListener(getPropertyChangeListener());
            pchlAttached = false;
        }
    }

    /** Enable delegation of a set of methods.
    * These will be delegated to the original node.
    * Since all available methods are delegated by default, normally you will not need to call this.
    * @param mask bitwise disjunction of <code>DELEGATE_XXX</code> constants
    * @throws IllegalArgumentException if the mask is invalid
    */
    protected final void enableDelegation(int mask) {
        if ((mask & ~DELEGATE_ALL) != 0) {
            throw new IllegalArgumentException("Bad delegation mask: " + mask); // NOI18N
        }

        delegateMask |= mask;
    }

    /** Disable delegation of a set of methods.
    * The methods will retain their behavior from {@link Node}.
    * <p>For example, if you wish to subclass <code>FilterNode</code>, giving your
    * node a distinctive display name and tooltip, and performing some special
    * action upon deletion, you may do so without risk of affecting the original
    * node as follows:
    * <br><pre>{@code
    * public MyNode extends FilterNode {
    *   public MyNode (Node orig) {
    *     super (orig, new MyChildren (orig));
    *     disableDelegation (DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME |
    *                        DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION |
    *                        DELEGATE_DESTROY);
    *     // these will affect only the filter node:
    *     setDisplayName ("Linking -> " + orig.getDisplayName ());
    *     setShortDescription ("Something different.");
    *   }
    *   public boolean canRename () { return false; }
    *   public void destroy () throws IOException {
    *     doMyCleanup ();
    *     super.destroy (); // calls Node.destroy(), not orig.destroy()
    *   }
    * }
    * }</pre>
    * <br>You may still manually delegate where desired using {@link #getOriginal}.
    * Other methods abstract in <code>Node</code> may simply be overridden without
    * any special handling.
    * @param mask bitwise disjunction of <code>DELEGATE_XXX</code> constants
    * @throws IllegalArgumentException if the mask is invalid
    */
    protected final void disableDelegation(int mask) {
        if ((mask & ~DELEGATE_ALL) != 0) {
            throw new IllegalArgumentException("Bad delegation mask: " + mask); // NOI18N
        }

        delegateMask &= ~mask;
    }

    /** Test whether we are currently delegating to some method. */
    private final boolean delegating(int what) {
        return (delegateMask & what) != 0;
    }

    /** Create new filter node for the original.
    * Subclasses do not have to override this, but if they do not,
    * the default implementation will filter the subclass filter, which is not
    * very efficient.
    * @return copy of this node
    */
    public Node cloneNode() {
        if (isDefault()) {
            // this is realy filter node without changed behaviour
            // with the normal children => use normal constructor for the
            // original node
            return new FilterNode(original);
        } else {
            // create filter node for this node to reflect changed
            // behaviour
            return new FilterNode(this);
        }
    }

    /** Tries to prevent issue 46993 by checking whether a node
     * to be set as original is actually pointing to this node.
     * @exception IllegalArgumentException if the check fails
     * @return always true
     */
    private boolean checkIfIamAccessibleFromOriginal(Node original) {
        if (this == original) {
            throw new IllegalArgumentException("Node cannot be its own original (even thru indirect chain)"); // NOI18N
        }

        if (original instanceof FilterNode) {
            FilterNode f = (FilterNode) original;
            checkIfIamAccessibleFromOriginal(f.original);
        }

        return true;
    }

    /** Changes the original node for this node.
     *@param original The new original node.
     *@param changeChildren If set to <CODE>true</CODE> changes children
     * of this node according to the new original node. If you pass
     * children which are not instance of class
     * {@link FilterNode.Children} into the constructor set this
     * parameter to <CODE>false</CODE>. Be aware
     * that this method aquires
     * write lock on the nodes hierarchy ({@link Children#MUTEX}). Take care not to call this method
     * under read lock.
     *
     *@throws java.lang.IllegalStateException if children which are not
     * instance of <CODE>FilterNode.Children</CODE> were passed
     * into the constructor and the method was called with the parameter
     * <CODE>changeChildren</CODE> set to <CODE>true</CODE>.
     *@since 1.39
     */
    protected final void changeOriginal(Node original, boolean changeChildren) {
        Parameters.notNull("original", original);

        if (
            changeChildren && !(getChildren() instanceof FilterNode.Children) &&
                !(getChildren() == Children.LEAF /* && original.isLeaf () */)
        ) {
            throw new IllegalStateException("Can't change implicitly defined Children on FilterNode"); // NOI18N
        }

        assert checkIfIamAccessibleFromOriginal(original) : ""; // NOI18N

        try {
            Children.PR.enterWriteAccess();

            // First remove the listeners from current original node
            this.original.removeNodeListener(getNodeListener());

            if (pchlAttached) {
                this.original.removePropertyChangeListener(getPropertyChangeListener());
            }

            // Set the new original node
            this.original = original;

            // attach listeners to new original node
            this.original.addNodeListener(getNodeListener());

            if (pchlAttached) {
                this.original.addPropertyChangeListener(getPropertyChangeListener());
            }

            // Reset children's original node.
            if (changeChildren /* && !original.isLeaf () */) {
                if (original.isLeaf() && (getChildren() != Children.LEAF)) {
                    setChildren(Children.LEAF);
                } else if (!original.isLeaf() && (getChildren() == Children.LEAF)) {
                    setChildren(new Children(original));
                } else if (!original.isLeaf() && (getChildren() != Children.LEAF)) {
                    ((FilterNode.Children) getChildren()).changeOriginal(original);
                }
            }
        } finally {
            Children.PR.exitWriteAccess();
        }

        // Fire all sorts of events (everything gets changed after we
        // reset the original node.)
        Lookup lkp = internalLookup(false);

        if (lkp instanceof FilterLookup) {
            ((FilterLookup) lkp).checkNode();
        }

        fireCookieChange();
        fireNameChange(null, null);
        fireDisplayNameChange(null, null);
        fireShortDescriptionChange(null, null);
        fireIconChange();
        fireOpenedIconChange();
        firePropertySetsChange(null, null);
    }

    // ------------- START OF DELEGATED METHODS ------------
    @Override
    public void setValue(String attributeName, Object value) {
        if (delegating(DELEGATE_SET_VALUE)) {
            original.setValue(attributeName, value);
        } else {
            super.setValue(attributeName, value);
        }
    }

    @Override
    public Object getValue(String attributeName) {
        if (delegating(DELEGATE_GET_VALUE)) {
            return original.getValue(attributeName);
        } else {
            return super.getValue(attributeName);
        }
    }

    /* Setter for system name. Fires info about property change.
    * @param s the string
    */
    @Override
    public void setName(String s) {
        if (delegating(DELEGATE_SET_NAME)) {
            original.setName(s);
        } else {
            super.setName(s);
        }
    }

    /* @return the name of the original node
    */
    @Override
    public String getName() {
        if (delegating(DELEGATE_GET_NAME)) {
            return original.getName();
        } else {
            return super.getName();
        }
    }

    /* Setter for display name. Fires info about property change.
    * @param s the string
    */
    @Override
    public void setDisplayName(String s) {
        if (delegating(DELEGATE_SET_DISPLAY_NAME)) {
            original.setDisplayName(s);
        } else {
            super.setDisplayName(s);
        }
    }

    /* @return the display name of the original node
    */
    @Override
    public String getDisplayName() {
        if (delegating(DELEGATE_GET_DISPLAY_NAME)) {
            return original.getDisplayName();
        } else {
            return super.getDisplayName();
        }
    }

    /* Setter for short description. Fires info about property change.
    * @param s the string
    */
    @Override
    public void setShortDescription(String s) {
        if (delegating(DELEGATE_SET_SHORT_DESCRIPTION)) {
            original.setShortDescription(s);
        } else {
            super.setShortDescription(s);
        }
    }

    /* @return the description of the original node
    */
    @Override
    public String getShortDescription() {
        if (delegating(DELEGATE_GET_SHORT_DESCRIPTION)) {
            return original.getShortDescription();
        } else {
            return super.getShortDescription();
        }
    }

    /* Finds an icon for this node. Delegates to the original.
    *
    * @see java.bean.BeanInfo
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean
    */
    public @Override Image getIcon(int type) {
        Image icon = original.getIcon(type);
        if (icon != null) {
            return icon;
        } else {
            LOGGER.log(Level.WARNING, "Cannot return null from {0}.getIcon", original.getClass().getName());
            return Node.EMPTY.getIcon(type);
        }
    }

    /* Finds an icon for this node. This icon should represent the node
    * when it is opened (if it can have children). Delegates to original.
    *
    * @see java.bean.BeanInfo
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon(int type) {
        return original.getOpenedIcon(type);
    }

    public HelpCtx getHelpCtx() {
        return original.getHelpCtx();
    }

    /* Can the original node be renamed?
    *
    * @return true if the node can be renamed
    */
    public boolean canRename() {
        return original.canRename();
    }

    /* Can the original node be deleted?
    * @return <CODE>true</CODE> if can, <CODE>false</CODE> otherwise
    */
    public boolean canDestroy() {
        return original.canDestroy();
    }

    /* Degelates the delete operation to original.
    */
    @Override
    public void destroy() throws java.io.IOException {
        if (delegating(DELEGATE_DESTROY)) {
            original.destroy();
        } else {
            super.destroy();
        }
    }

    /** Used to access the destroy method when original nodes
    * has been deleted
    */
    private final void originalDestroyed() {
        try {
            super.destroy();
        } catch (IOException ex) {
            Logger.getLogger(FilterNode.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    /* Getter for the list of property sets. Delegates to original.
    *
    * @return the array of property sets.
    */
    public PropertySet[] getPropertySets() {
        return original.getPropertySets();
    }

    /* Called when an object is to be copied to clipboard.
    * @return the transferable object dedicated to represent the
    *    content of clipboard
    * @exception IOException is thrown when the
    *    operation cannot be performed
    */
    public Transferable clipboardCopy() throws IOException {
        return original.clipboardCopy();
    }

    /* Called when an object is to be cut to clipboard.
    * @return the transferable object dedicated to represent the
    *    content of clipboard
    * @exception IOException is thrown when the
    *    operation cannot be performed
    */
    public Transferable clipboardCut() throws IOException {
        return original.clipboardCut();
    }

    /* Returns true if this object allows copying.
    * @returns true if this object allows copying.
    */
    public boolean canCopy() {
        return original.canCopy();
    }

    /* Returns true if this object allows cutting.
    * @returns true if this object allows cutting.
    */
    public boolean canCut() {
        return original.canCut();
    }

    public Transferable drag() throws IOException {
        return original.drag();
    }

    /* Default implementation that tries to delegate the implementation
    * to the createPasteTypes method. Simply calls the method and
    * tries to take the first provided argument. Ignores the action
    * argument and index.
    *
    * @param t the transferable
    * @param action the drag'n'drop action to do DnDConstants.ACTION_MOVE, ACTION_COPY, ACTION_LINK
    * @param index index between children the drop occured at or -1 if not specified
    * @return null if the transferable cannot be accepted or the paste type
    *    to execute when the drop occures
    */
    public PasteType getDropType(Transferable t, int action, int index) {
        return original.getDropType(t, action, index);
    }

    /* Which paste operations are allowed when transferable t is in clipboard?
    * @param t the transferable in clipboard
    * @return array of operations that are allowed
    */
    public PasteType[] getPasteTypes(Transferable t) {
        return original.getPasteTypes(t);
    }

    /* Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes() {
        return original.getNewTypes();
    }

    /* Delegates to original.
    *
    * @return array of system actions that should be in popup menu
    */
    @Override
    @Deprecated
    public SystemAction[] getActions() {
        if (delegating(DELEGATE_GET_ACTIONS)) {
            return original.getActions();
        } else {
            return super.getActions();
        }
    }

    /* Delegates to original
    */
    @Override
    @Deprecated
    public SystemAction[] getContextActions() {
        if (delegating(DELEGATE_GET_CONTEXT_ACTIONS)) {
            return original.getContextActions();
        } else {
            return super.getContextActions();
        }
    }

    /*
    * @return default action of the original node or null
    */
    @Override
    @Deprecated
    public SystemAction getDefaultAction() {
        return original.getDefaultAction();
    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        if (context) {
            if (!delegating(DELEGATE_GET_ACTIONS) || overridesAMethod("getContextActions")) { // NOI18N

                return super.getActions(context);
            }
        } else {
            if (!delegating(DELEGATE_GET_CONTEXT_ACTIONS) || overridesAMethod("getActions")) { // NOI18N

                return super.getActions(context);
            }
        }

        javax.swing.Action[] retValue;
        retValue = original.getActions(context);

        return retValue;
    }

    @Override
    public javax.swing.Action getPreferredAction() {
        javax.swing.Action retValue;

        if (overridesAMethod("getDefaultAction")) { // NOI18N
            retValue = super.getPreferredAction();
        } else {
            retValue = original.getPreferredAction();
        }

        return retValue;
    }

    /** Get a display name containing HTML markup.  <strong><b>Note:</b> If you subclass
     * FilterNode and override <code>getDisplayName()</code>, this method will
     * always return null unless you override it as well (assuming that if you're
     * changing the display name, you don't want an HTML display name constructed
     * from the original node's display name to be what shows up in views of
     * this node).</strong>  If <code>getDisplayName()</code> is not overridden,
     * this method will return whatever the original node returns from this
     * method.
     * <p>
     * Note that if you do override <code>getDisplayName</code>, you should also override
     * this method to return null.
     *
     *
     *
     * @see org.openide.nodes.Node#getHtmlDisplayName
     * @return An HTML display name, if available, or null if no display name
     * is available   */
    @Override
    public String getHtmlDisplayName() {
        if (overridesGetDisplayName()) {
            return null;
        } else {
            return delegating(DELEGATE_GET_DISPLAY_NAME) ? original.getHtmlDisplayName() : super.getHtmlDisplayName();
        }
    }

    private boolean overridesGetDisplayName() {
        synchronized (overridesGetDisplayNameCache) {
            Boolean b = overridesGetDisplayNameCache.get(getClass());

            if (b == null) {
                b = overridesAMethod("getDisplayName"); // NOI18N
                overridesGetDisplayNameCache.put(getClass(), b);
            }

            return b;
        }
    }

    /*
    * @return <CODE>true</CODE> if the original has a customizer.
    */
    public boolean hasCustomizer() {
        return original.hasCustomizer();
    }

    /* Returns the customizer component.
    * @return the component or <CODE>null</CODE> if there is no customizer
    */
    public java.awt.Component getCustomizer() {
        return original.getCustomizer();
    }

    /** Delegates to original, if no special lookup provided in constructor,
    * Otherwise it delegates to the lookup. Never override this method
    * if the lookup is provided in constructor.
    *
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    * @see Node#getCookie
    */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        Lookup l = internalLookup(true);

        if (l != null) {
            Object res = l.lookup(type);
            return type.isInstance(res) && res instanceof Node.Cookie ? type.cast(res) : null;
        }

        return original.getCookie(type);
    }

    /** If this is FilterNode without any changes (subclassed, changed children)
    * and the original provides handle, stores them and
    * returns a new handle for the proxy.
    * <p>Subclasses <strong>must</strong> override this if they wish for their nodes to be
    * properly serializable.
    *
    * @return the handle, or <code>null</code> if this node is subclassed or
    *    uses changed children
    */
    public Node.Handle getHandle() {
        if (!isDefault()) {
            // subclasses has to implement the method by its own
            return null;
        }

        Node.Handle original = this.original.getHandle();

        if (original == null) {
            // no original handle => no handle here
            return null;
        }

        return new FilterHandle(original);
    }

    /** Test equality of original nodes.
    * Note that for subclasses of <code>FilterNode</code>, or filter nodes with non-default children,
    * the test reverts to object identity.
    * <strong>Note:</strong> if you wish that the {@link Index} cookie works correctly on
    * filtered nodes and their subnodes, and you are subclassing <code>FilterNode</code> or
    * using non-default children, you will probably want to override this method to test
    * equality of the specified node with this filter node's original node; otherwise Move Up
    * and Move Down actions may be disabled.
    * <p>Note though that it is often better to provide your own index cookie from a filter
    * node. Only then it is possible to change the number of children relative to the original.
    * And in many cases this is easier anyway, as for example with
    * <code>DataFolder.Index</code> for data folders.
    * @param o something to compare to, presumably a node or <code>FilterNode</code> of one
    * @return true if this node's original node is the same as the parameter (or original node of parameter)
    */
    @Override
    public boolean equals(Object o) {
        // VERY DANGEROUS! Completely messes up visualizers and often original node is displayed rather than filter.
        // Jst: I know that it is dangerous, but some code probably depends on it
        if (!(o instanceof Node)) {
            return false; // something else or null
        }

        if (this == o) {
            return true; // shortcut
        }

        // get the "most original" ones....
        Node left = getRepresentation(this);
        Node right = getRepresentation((Node) o);

        // cover nondefault FilterNodes (possibly) deep in the stack
        if ((left instanceof FilterNode) || (right instanceof FilterNode)) {
            return left == right;
        }

        return left.equals(right);
    }

    private static Node getRepresentation(Node n) {
        while (n instanceof FilterNode) {
            FilterNode fn = (FilterNode) n;

            if (!fn.isDefault()) {
                return n;
            }

            n = fn.original;
        }

        return n; // either node or nondefault FilterNode
    }

    /** Hash by original nodes.
    * Note that for subclasses of <code>FilterNode</code>, or filter nodes with non-default children,
    * the hash reverts to the identity hash code.
    * @return the delegated hash code
    */
    @Override
    public int hashCode() {
        try {
            assert hashCodeLogging(true) : ""; // NOI18N

            int result = isDefault() ? original.hashCode() : System.identityHashCode(this);
            assert hashCodeLogging(false) : ""; // NOI18N

            return result;
        } catch (StackError err) {
            err.add(this);
            throw err;
        }
    }

    /** Method for tracing the issue 46993. Counts the depth of execution
     * and if larger than 1000 throws debugging exception.
     */
    private static boolean hashCodeLogging(boolean enter) {
        if (hashCodeDepth > 1000) {
            hashCodeDepth = 0;
            throw new StackError();
        }

        if (enter) {
            hashCodeDepth++;
        } else {
            hashCodeDepth--;
        }

        return true;
    }

    //  public String toString () {
    //    return super.toString () + " original has children: " + original.getChildren ().getNodesCount (); // NOI18N
    //  }
    // ----------- END OF DELEGATED METHODS ------------

    /** Get the original node.
    * <p><strong>Yes</strong> this is supposed to be protected! If you
    * are not subclassing <code>FilterNode</code> yourself, you should
    * not be calling it (nor casting to <code>FilterNode</code>). Use
    * cookies instead.
    * @return the node proxied to
    */
    protected Node getOriginal() {
        return original;
    }

    /** Create a property change listener that allows listening on the
    * original node properties (contained in property sets) and propagating
    * them to the proxy.
    * <P>
    * This method is called during initialization and allows subclasses
    * to modify the default behaviour.
    *
    * @return a {@link PropertyChangeAdapter} in the default implementation
    */
    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeAdapter(this);
    }

    /** Creates a node listener that allows listening on the
    * original node and propagating events to the proxy.
    * <p>Intended for overriding by subclasses, as with {@link #createPropertyChangeListener}.
    *
    * @return a {@link FilterNode.NodeAdapter} in the default implementation
    */
    protected NodeListener createNodeListener() {
        return new NodeAdapter(this);
    }

    /** Getter for property change listener.
    */
    PropertyChangeListener getPropertyChangeListener() {
        synchronized (LISTENER_LOCK) {
            if (propL == null) {
                propL = createPropertyChangeListener();
            }

            return propL;
        }
    }

    /** Getter for node listener.
    */
    NodeListener getNodeListener() {
        synchronized (LISTENER_LOCK) {
            if (nodeL == null) {
                nodeL = createNodeListener();
                getOriginal().addNodeListener(nodeL);
            }

            return nodeL;
        }
    }

    /** Notified from Node that a listener has been added.
     * Thus we force initialization of listeners.
     */
    @Override
    final void listenerAdded() {
        getNodeListener();
    }

    /** Check method whether the node has default behavior or
    * if it is either subclass or uses different children or lookup.
    * @return true if it is default
    */
    private boolean isDefault() {
        //System.err.print ("FilterNode.isDefault: ");
        if (getClass() != FilterNode.class) {
            //System.err.println("false\n\tsubclass of FilterNode");
            return false;
        }

        return !childrenProvided && !lookupProvided;
    }

    /**
     * This method is used to change the Children from Children.LEAF to Children
     * typically used to when there is a setChildren() on the original node
     * setChildren will fire the appropriate events
     */
    @Override
    final void updateChildren() {
        if (isDefault()) {
            org.openide.nodes.Children newChildren = null;

            try {
                Children.PR.enterReadAccess();

                if ((original.hierarchy == Children.LEAF) && (hierarchy != Children.LEAF)) {
                    newChildren = Children.LEAF;
                } else if ((original.hierarchy != Children.LEAF) && (hierarchy == Children.LEAF)) {
                    newChildren = new Children(original);
                }
            } finally {
                Children.PR.exitReadAccess();
            }

            if (newChildren != null) {
                setChildren(newChildren);
            }
        } else {
            super.updateChildren();
        }
    }

    /** An exception to be thrown from hashCode() to debug issue 46993.
     */
    private static class StackError extends StackOverflowError {
        private IdentityHashMap<FilterNode,FilterNode> nodes;

        public void add(FilterNode n) {
            if (nodes == null) {
                nodes = new IdentityHashMap<FilterNode,FilterNode>();
            }

            if (!nodes.containsKey(n)) {
                nodes.put(n, n);
            }
        }

        @Override
        public String getMessage() {
            StringBuffer sb = new StringBuffer();
            sb.append("StackOver in FilterNodes:\n"); // NOI18N

            for (FilterNode f : nodes.keySet()) {
                sb.append("  class: "); // NOI18N
                sb.append(f.getClass().getName());
                sb.append(" id: "); // NOI18N
                sb.append(Integer.toString(System.identityHashCode(f), 16));
                sb.append("\n"); // NOI18N
            }

            return sb.toString();
        }
    }

    /** Adapter that listens on changes in an original node
    * and refires them in a proxy.
    * This adapter is created during
    * initialization in  {@link FilterNode#createPropertyChangeListener}. The method
    * can be overridden and this class used as the super class for the
    * new implementation.
    * <P>
    * A reference to the proxy is stored by weak reference, so it does not
    * prevent the node from being finalized.
    */
    protected static class PropertyChangeAdapter extends Object implements PropertyChangeListener {
        private Reference<FilterNode> fnRef;

        /** Create a new adapter.
        * @param fn the proxy
        */
        public PropertyChangeAdapter(FilterNode fn) {
            this.fnRef = new WeakReference<FilterNode>(fn);
        }

        /* Find the node we are attached to. If it is not null call property
        * change method with two arguments.
        */
        public final void propertyChange(PropertyChangeEvent ev) {
            FilterNode fn = this.fnRef.get();

            if (fn == null) {
                return;
            }

            propertyChange(fn, ev);
        }

        /** Actually propagate the event.
        * Intended for overriding.
        * @param fn the proxy
        * @param ev the event
        */
        protected void propertyChange(FilterNode fn, PropertyChangeEvent ev) {
            fn.firePropertyChange(ev.getPropertyName(), ev.getOldValue(), ev.getNewValue());
        }
        
        static Set<PropertyChangeListener> checkDormant(PropertyChangeListener l, Set<PropertyChangeListener> in) {
            if (l instanceof PropertyChangeAdapter && ((PropertyChangeAdapter)l).fnRef.get() == null) {
                Set<PropertyChangeListener> ret = new HashSet<PropertyChangeListener>();
                if (in != null) {
                    ret.addAll(in);
                }
                ret.add(l);
                return ret;
            }
            return in;
        }
    }

    /** Adapter that listens on changes in an original node and refires them
    * in a proxy. Created in {@link FilterNode#createNodeListener}.
    * @see FilterNode.PropertyChangeAdapter
    */
    protected static class NodeAdapter extends Object implements NodeListener {
        private Reference<FilterNode> fnRef;

        /** Create an adapter.
        * @param fn the proxy
        */
        public NodeAdapter(FilterNode fn) {
            this.fnRef = new WeakReference<FilterNode>(fn);
        }

        /* Tests if the reference to the node provided in costructor is
        * still valid (it has not been finalized) and if so, calls propertyChange (Node, ev).
        */
        public final void propertyChange(PropertyChangeEvent ev) {
            FilterNode fn = this.fnRef.get();

            if (fn == null) {
                return;
            }

            propertyChange(fn, ev);
        }

        /** Actually refire the change event in a subclass.
        * The default implementation ignores changes of the <code>parentNode</code> property but refires
        * everything else.
        *
        * @param fn the filter node
        * @param ev the event to fire
        */
        protected void propertyChange(FilterNode fn, PropertyChangeEvent ev) {
            String n = ev.getPropertyName();

            if (n.equals(Node.PROP_PARENT_NODE)) {
                // does nothing
                return;
            }

            if (n.equals(Node.PROP_DISPLAY_NAME)) {
                fn.fireOwnPropertyChange(PROP_DISPLAY_NAME, (String) ev.getOldValue(), (String) ev.getNewValue());

                return;
            }

            if (n.equals(Node.PROP_NAME)) {
                fn.fireOwnPropertyChange(PROP_NAME, (String) ev.getOldValue(), (String) ev.getNewValue());

                return;
            }

            if (n.equals(Node.PROP_SHORT_DESCRIPTION)) {
                fn.fireOwnPropertyChange(PROP_SHORT_DESCRIPTION, (String) ev.getOldValue(), (String) ev.getNewValue());

                return;
            }

            if (n.equals(Node.PROP_ICON)) {
                fn.fireIconChange();

                return;
            }

            if (n.equals(Node.PROP_OPENED_ICON)) {
                fn.fireOpenedIconChange();

                return;
            }

            if (n.equals(Node.PROP_PROPERTY_SETS)) {
                fn.firePropertySetsChange((PropertySet[]) ev.getOldValue(), (PropertySet[]) ev.getNewValue());

                return;
            }

            if (n.equals(Node.PROP_COOKIE)) {
                fn.fireCookieChange();

                return;
            }

            if (n.equals(Node.PROP_LEAF)) {
                fn.updateChildren();

                /*
                fn.fireOwnPropertyChange(
                    Node.PROP_LEAF, ev.getOldValue(), ev.getNewValue()
                );
                 */
            }
        }

        /** Does nothing.
        * @param ev event describing the action
        */
        public void childrenAdded(NodeMemberEvent ev) {
        }

        /** Does nothing.
        * @param ev event describing the action
        */
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        /** Does nothing.
        * @param ev event describing the action
        */
        public void childrenReordered(NodeReorderEvent ev) {
        }

        /* Does nothing.
        * @param ev event describing the node
        */
        public final void nodeDestroyed(NodeEvent ev) {
            FilterNode fn = this.fnRef.get();

            if (fn == null) {
                return;
            }

            fn.originalDestroyed();
        }
        static Set<NodeListener> checkDormant(NodeListener l, Set<NodeListener> in) {
            if (l instanceof NodeAdapter && ((NodeAdapter)l).fnRef.get() == null) {
                Set<NodeListener> ret = new HashSet<NodeListener>();
                if (in != null) {
                    ret.addAll(in);
                }
                ret.add(l);
                return ret;
            }
            return in;
        }
    }

    /** Children for a filter node. Listens on changes in subnodes of
    * the original node and asks this filter node to creates representatives for
    * these subnodes.
    * <P>
    * This class is used as the default for subnodes of filter node, but
    * subclasses may modify it or provide a totally different implementation.
     * <p><code>FilterNode.Children</code> is not well suited to cases where you need to insert
     * additional nodes at the beginning or end of the list, or where you may need
     * to merge together multiple original children lists, or reorder them, etc.
     * That is because the keys are of type <code>Node</code>, one for each original
     * child, and the keys are reset during {@link #addNotify}, {@link #filterChildrenAdded},
     * {@link #filterChildrenRemoved}, and {@link #filterChildrenReordered}, so it is
     * not trivial to use different keys: you would need to override <code>addNotify</code>
     * (calling super first!) and the other three update methods. For such complex cases
     * you will do better by creating your own <code>Children.Keys</code> subclass, setting
     * keys that are useful to you, and keeping a <code>NodeListener</code> on the original
     * node to handle changes.
    */
    public static class Children extends org.openide.nodes.Children.Keys<Node> implements Cloneable {
        /** Original node. Should not be modified. */
        protected Node original;

        /** node listener on original */
        private ChildrenAdapter nodeL;

        /** Create children.
         * @param or original node to take children from */
        public Children(Node or) {
            this(or, or.getChildren().isLazy());
        }
        
        private Children(Node or, boolean lazy) {
            super(lazy);
            original = or;
        }
        
        @Override
        EntrySupport entrySupport() {
            FilterChildrenSupport support = null;
            synchronized (org.openide.nodes.Children.class) {
                if (getEntrySupport() != null && !getEntrySupport().isInitialized()) {
                    // support is not initialized, it should be checked against original
                    support = (FilterChildrenSupport) getEntrySupport();
                }
            }

            if (support != null) {
                // get original support without lock
                assert !Thread.holdsLock(org.openide.nodes.Children.class);
                EntrySupport origSupport = original.getChildren().entrySupport();
                synchronized (org.openide.nodes.Children.class) {
                    if (getEntrySupport() == support && support.originalSupport() != origSupport) {
                        // original support was changed, force new support creation
                        setEntrySupport(null);
                    }
                }
            }

            synchronized (org.openide.nodes.Children.class) {
                if (getEntrySupport() != null) {
                    return getEntrySupport();
                }
            }

            // access without lock
            org.openide.nodes.Children origChildren = original.getChildren();
            EntrySupport os = origChildren.entrySupport();
            boolean osIsLazy = origChildren.isLazy();

            synchronized (org.openide.nodes.Children.class) {
                if (getEntrySupport() != null) {
                    return getEntrySupport();
                }
                lazySupport = osIsLazy;
                EntrySupport es = lazySupport ? new LazySupport(this, (EntrySupportLazy) os) : new DefaultSupport(this, (EntrySupportDefault) os);
                setEntrySupport(es);
                postInitializeEntrySupport(es);
                return getEntrySupport();
            }
        }
        
        /** Sets the original children for this children. 
         * Be aware that this method aquires
         * write lock on the nodes hierarchy ({@link Children#MUTEX}). 
         * Take care not to call this method under read lock.
         * @param original The new original node.
         * @since 1.39
         */
        protected final void changeOriginal(Node original) {
            try {
                PR.enterWriteAccess();

                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("changeOriginal() " + this); // NOI18N
                    LOGGER.finer("    old original children: " + this.original.getChildren()); // NOI18N
                    LOGGER.finer("    old original lazy support: " + this.original.getChildren().lazySupport); // NOI18N
                    LOGGER.finer("    new original: " + original); // NOI18N
                    LOGGER.finer("    new original children: " + original.getChildren()); // NOI18N
                    LOGGER.finer("    new original lazy support: " + original.getChildren().lazySupport); // NOI18N
                    LOGGER.finer("    Children adapter: " + nodeL); // NOI18N
                }

                boolean wasAttached = nodeL != null;

                // uregister from the original node
                if (wasAttached) {
                    this.original.removeNodeListener(nodeL);
                    nodeL = null;
                }

                changeSupport(original);

                if (wasAttached) {
                    addNotifyImpl();
                }
            } finally {
                PR.exitWriteAccess();
            }
        }
        
        private void changeSupport(Node newOriginal) {
            final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
            boolean init = entrySupport().isInitialized();
            boolean changed = false;

            if (LOG_ENABLED) {
                LOGGER.finer("changeSupport() " + this); // NOI18N
                LOGGER.finer("    newOriginal: " + newOriginal); // NOI18N
                LOGGER.finer("    entrySupport().isInitialized(): " + init); // NOI18N
                LOGGER.finer("    parent: " + parent); // NOI18N
            }

            if (init && parent != null) {
                List<Node> snapshot = entrySupport().snapshot();
                if (snapshot.size() > 0) {
                    int[] idxs = getSnapshotIdxs(snapshot);
                    if (newOriginal != null) {
                        this.original = newOriginal;
                    }
                    changed = true;
                    synchronized (org.openide.nodes.Children.class) {
                        setEntrySupport(null);
                    }

                    if (LOG_ENABLED) {
                        LOGGER.finer("   firing node removal: " + snapshot); // NOI18N
                    }
                    parent.fireSubNodesChangeIdx(false, idxs, null, Collections.<Node>emptyList(), snapshot);
                }
            }
            if (!changed) {
                if (newOriginal != null) {
                    this.original = newOriginal;
                }
                synchronized (org.openide.nodes.Children.class) {
                    setEntrySupport(null);
                }
            }

            if (init || newOriginal == null) {
                entrySupport().notifySetEntries();

                if (LOG_ENABLED) {
                    LOGGER.log(Level.FINER, "    initializing new support"); // NOI18N
                }
                // force initialization
                entrySupport().getNodesCount(false);
            }
        }

        /** Closes the listener, if any, on the original node.
        */
        @Override
        protected void finalize() {
            if (nodeL != null) {
                original.removeNodeListener(nodeL);
            }

            nodeL = null;
        }

        /* Clones the children object.
        */
        @Override
        public Object clone() {
            return new Children(original);
        }

        /** Initializes listening to changes in original node.
        */
        @Override
        protected void addNotify() {
            addNotifyImpl();
        }

        private void addNotifyImpl() {
            // add itself to reflect to changes children of original node
            nodeL = new ChildrenAdapter(this);
            original.addNodeListener(nodeL);
            filterSupport().update();
        }

        /** Clears current keys, because all mirrored nodes disappeared.
        */
        @Override
        protected void removeNotify() {
            setKeys(Collections.<Node>emptySet());

            if (nodeL != null) {
                original.removeNodeListener(nodeL);
                nodeL = null;
            }
        }

        /** Allows subclasses to override
        * creation of node representants for nodes in the mirrored children
        * list. The default implementation simply uses {@link Node#cloneNode}.
        * <p>Note that this method is only suitable for a 1-to-1 mirroring.
        *
        * @param node node to create copy of
        * @return copy of the original node
        */
        protected Node copyNode(Node node) {
            return node.cloneNode();
        }

        /* Implements find of child by finding the original child and then [PENDING]
        * @param name of node to find
        * @return the node or null
        */
        @Override
        public Node findChild(String name) {
            return filterSupport().findChild(name);
        }
               

        /** Create nodes representing copies of the original node's children.
        * The default implementation returns exactly one representative for each original node,
        * as returned by {@link #copyNode}.
        * Subclasses may override this to avoid displaying a copy of an original child at all,
        * or even to display multiple nodes representing the original.
        * @param key the original child node
        * @return zero or more nodes representing the original child node
        */
        protected Node[] createNodes(Node key) {
            // is run under read access lock so nobody can change children
            return new Node[] { copyNode(key) };
        }

        /* Delegates to children of the original node.
        *
        * @param arr nodes to add
        * @return true/false
        */
        @Override
        @Deprecated
        public boolean add(Node[] arr) {
            return original.getChildren().add(arr);
        }

        private FilterChildrenSupport filterSupport() {
            return (FilterChildrenSupport) entrySupport();
        }

        private boolean checkSupportChanged() {
            FilterChildrenSupport support = (FilterChildrenSupport) entrySupport();
            EntrySupport origSupport = original.getChildren().entrySupport();

            if (support.originalSupport() != origSupport) {
                assert Children.MUTEX.isWriteAccess() : "Should be called only under write access"; // NOI18N
                changeSupport(null);
                return true;
            } else {
                return false;
            }
        }

        /* Delegates to filter node.
        * @param arr nodes to remove
        * @return true/false
        */
        @Override
        @Deprecated
        public boolean remove(Node[] arr) {
            return original.getChildren().remove(arr);
        }

        /** Called when the filter node adds a new child.
        * The default implementation makes a corresponding change.
        * @param ev info about the change
        */
        protected void filterChildrenAdded(NodeMemberEvent ev) {
            if (checkSupportChanged()) {
                // support was changed, we should be already updated
                return;
            }            
            filterSupport().filterChildrenAdded(ev);
        }

        /** Called when the filter node removes a child.
        * The default implementation makes a corresponding change.
        * @param ev info about the change
        */
        protected void filterChildrenRemoved(NodeMemberEvent ev) {
            if (checkSupportChanged()) {
                // support was changed, we should be already updated
                return;
            }
            filterSupport().filterChildrenRemoved(ev);
        }

        /** Called when the filter node reorders its children.
        * The default implementation makes a corresponding change.
        * @param ev info about the change
        */
        protected void filterChildrenReordered(NodeReorderEvent ev) {
            filterSupport().filterChildrenReordered(ev);
        }

        /**
         * Implementation that ensures the original node is fully initialized
         * if optimal result is requested.
         *
         * @param optimalResult if <code>true</code>, the method will block
         * until the original node is fully initialized.
         * @since 3.9
         */
        @Override
        public Node[] getNodes(boolean optimalResult) {
            return filterSupport().callGetNodes(optimalResult);
        }

        @Override
        public int getNodesCount(boolean optimalResult) {
            return filterSupport().callGetNodesCount(optimalResult);
        }
        
        @Override
        Entry createEntryForKey(Node key) {
            return filterSupport().createEntryForKey(key);
        }
        
        @Override
        void switchSupport(boolean toLazy) {        
            try {
                Children.PR.enterWriteAccess();
                ((Children.Keys) original.getChildren()).switchSupport(toLazy);
                super.switchSupport(toLazy);
            } finally {
                Children.PR.exitWriteAccess();
            }
        }
        
        private class DefaultSupport extends EntrySupportDefault implements FilterChildrenSupport {
            
            EntrySupportDefault origSupport;

            public DefaultSupport(org.openide.nodes.Children ch, EntrySupportDefault origSupport) {
                super(ch);
                this.origSupport = origSupport;
            }

            @Override
            protected DefaultSnapshot createSnapshot() {
                DefaultSnapshot snapshot = super.createSnapshot();
                Object[] newHolder = new Object[]{snapshot.holder, origSupport.createSnapshot()};
                snapshot.holder = newHolder;
                return snapshot;
            }

            public Node[] callGetNodes(boolean optimalResult) {
                Node[] hold = null;
                if (optimalResult) {
                    hold = original.getChildren().getNodes(true);
                }
                hold = Children.this.getNodes();
                return hold;
            }

            @Override
            public int callGetNodesCount(boolean optimalResult) {
                int cnt = 0;
                if (optimalResult) {
                    cnt = original.getChildren().getNodesCount(true);
                }
                int ret = Children.this.getNodesCount();
                LOG.log(Level.FINEST, "Count {1} gives {2}", new Object[]{cnt, ret});
                return ret;
            }

            public Node findChild(String name) {
                Node dontGC = original.getChildren().findChild(name);
                return Children.super.findChild(name);
            }

            public void filterChildrenAdded(NodeMemberEvent ev) {
                updateKeys();
            }

            public void filterChildrenRemoved(NodeMemberEvent ev) {
                updateKeys();
            }

            public void filterChildrenReordered(NodeReorderEvent ev) {
                updateKeys();
            }

            public void update() {
                updateKeys();
            }

            private void updateKeys() {
                final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
                if (LOG_ENABLED) {
                    LOGGER.finer("updateKeys() " + this); // NOI18N
                }
                ChildrenAdapter cha = nodeL;
                if (cha != null) {
                    if (LOG_ENABLED) {
                        LOGGER.finer("    getting original nodes"); // NOI18N
                    }
                    Node[] arr = original.getChildren().getNodes();

                    if (LOG_ENABLED) {
                        LOGGER.finer("    setKeys(), keys: " + Arrays.toString(arr)); // NOI18N
                    }
                    setKeys(arr);
                    if (!origSupport.isInitialized()) {
                        origSupport.notifySetEntries();
                    }
                }
            }

            public Entry createEntryForKey(Node key) {
                return new KE(key);
            }

            public EntrySupport originalSupport() {
                return origSupport;
            }            
        }

        private class LazySupport extends EntrySupportLazy implements FilterChildrenSupport {
            EntrySupportLazy origSupport;

            public LazySupport(org.openide.nodes.Children ch, EntrySupportLazy origSupport) {
                super(ch);
                this.origSupport = origSupport;
            }

            class FilterLazySnapshot extends LazySnapshot {
                private LazySnapshot origSnapshot;

                public FilterLazySnapshot(List<Entry> entries, java.util.Map<Entry, EntryInfo> e2i) {
                    super(entries, e2i);
                    origSnapshot = origSupport.createSnapshot();
                }

                @Override
                public Node get(Entry entry) {
                    EntryInfo info = entryToInfo.get(entry);
                    Node node = info.currentNode();
                    if (node == null) {
                        node = info.getNode(false, origSnapshot);
                    }
                    if (isDummyNode(node)) {
                        // force new snapshot
                        hideEmpty(null, entry);
                    }
                    return node;
                }
            }

            final class FilterDelayedLazySnapshot extends FilterLazySnapshot {

                public FilterDelayedLazySnapshot(List<Entry> entries, java.util.Map<Entry, EntryInfo> e2i) {
                    super(entries, e2i);
                }
            }

            @Override
            protected LazySnapshot createSnapshot(List<Entry> entries, java.util.Map<Entry, EntryInfo> e2i, boolean delayed) {
                synchronized (LOCK) {
                    return delayed ? new FilterDelayedLazySnapshot(entries, e2i) : new FilterLazySnapshot(entries, e2i);
                }
            }
        
            public Node[] callGetNodes(boolean optimalResult) {
                Node[] hold = null;
                if (optimalResult) {
                    hold = original.getChildren().getNodes(true);
                }
                hold = Children.this.getNodes();
                return hold;
            }

            @Override
            public int callGetNodesCount(boolean optimalResult) {
                return original.getChildren().getNodesCount(optimalResult);
            }

            public Node findChild(String name) {
                original.getChildren().findChild(name);
                return Children.super.findChild(name);
            }

            public void filterChildrenAdded(NodeMemberEvent ev) {
                if (ev.sourceEntry == null) {
                    update();
                } else {
                    doRefreshEntry(ev.sourceEntry);
                }
            }

            public void filterChildrenRemoved(NodeMemberEvent ev) {
                if (ev.sourceEntry == null) {
                    update();
                } else {
                    doRefreshEntry(ev.sourceEntry);
                }
            }

            public void filterChildrenReordered(NodeReorderEvent ev) {
                update();
            }

            @Override
            public void update() {
                final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
                if (LOG_ENABLED) {
                    LOGGER.finer("updateEntries() " + this); // NOI18N
                }
                ChildrenAdapter cha = nodeL;
                if (cha != null) {
                    final int count = origSupport.getNodesCount(false);
                    Children.MUTEX.postWriteRequest(new Runnable() {
                        @Override
                        public void run() {
                            updateEntries(count);
                        }
                    });
                }
            }

            private void updateEntries(int count) {
                final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
                if (LOG_ENABLED) {
                    LOGGER.finer("updateEntries() " + this); // NOI18N
                }
                if (LOG_ENABLED) {
                    LOGGER.finer("    origSupport.getNodesCount(): " + count); // NOI18N
                }

                List<Entry> origEntries = origSupport.getEntries();
                if (LOG_ENABLED) {
                    LOGGER.finer("    origSupport.getEntries() - size: " + origEntries.size() + " data: " + origEntries); // NOI18N
                }

                ArrayList<Entry> filtEntries = new ArrayList<Entry>(origEntries.size());
                for (Entry e : origEntries) {
                    filtEntries.add(new FilterNodeEntry(e));
                }

                setEntries(filtEntries);

                if (!origSupport.isInitialized()) {
                    origSupport.notifySetEntries();
                    return;
                }
            }

            private void doRefreshEntry(Entry entry) {
                refreshEntry(new FilterNodeEntry(entry));
            }

            public Entry createEntryForKey(Node key) {
                Entry entry = origSupport.entryForNode(key);
                return new FilterNodeEntry(entry);
            }

            public EntrySupport originalSupport() {
                return origSupport;
            }

            /** Substitution for Node to use it as key instead of Node */
            private final class FilterNodeEntry extends Children.Keys<Object>.KE {

                Entry origEntry;

                public FilterNodeEntry(Entry origEntry) {
                    this.origEntry = origEntry;
                }

                @Override
                public Object getKey() {
                    return this;
                }

                @Override
                public int getCnt() {
                    return 0;
                }

                @Override
                public Collection<Node> nodes(Object source) {
                    Node node;
                    if (source != null) {
                        LazySnapshot origSnapshot = (LazySnapshot) source;
                        node = origSnapshot.get(origEntry);
                    } else {
                        node = origSupport.getNode(origEntry);
                    }

                    key = node;
                    if (node == null || isDummyNode(node)) {
                        return Collections.emptyList();
                    }
                    Node[] nodes = createNodes(node);
                    if (nodes == null) {
                        return Collections.emptyList();
                    }
                    return Arrays.asList(nodes);
                }

                @Override
                public int hashCode() {
                    return origEntry.hashCode();
                }

                @Override
                public boolean equals(Object o) {
                    return o instanceof FilterNodeEntry ? origEntry.equals(((FilterNodeEntry) o).origEntry) : false;
                }

                @Override
                public String toString() {
                    return "FilterNodeEntry[" + origEntry + "]@" + Integer.toString(hashCode(), 16);
                }
            }
        }

        interface FilterChildrenSupport {
            Node[] callGetNodes(boolean optimalResult);

            int callGetNodesCount(boolean optimalResult);
            
            Node findChild(String name);

            void filterChildrenAdded(NodeMemberEvent ev);

            void filterChildrenRemoved(NodeMemberEvent ev);

            void filterChildrenReordered(NodeReorderEvent ev);

            void update();

            Entry createEntryForKey(Node key);
            
            EntrySupport originalSupport();
        }
    }

    /** Adapter that listens on changes in the original node and fires them
    * in this node.
    * Used as the default listener in {@link FilterNode.Children},
    * and is intended for refinement by its subclasses.
    */
    private static class ChildrenAdapter extends Object implements NodeListener {
        /** children object to notify about addition of children.
        * Can be null. Set from Children's initNodes method.
        */
        private Reference<Children> childrenRef;

        /** Create a new adapter.
        * @param ch the children list
        */
        public ChildrenAdapter(Children ch) {
            this.childrenRef = new WeakReference<Children>(ch);
        }

        /** Does nothing.
        * @param ev the event
        */
        public void propertyChange(PropertyChangeEvent ev) {
        }

        /* Informs that a set of new children has been added.
        * @param ev event describing the action
        */
        public void childrenAdded(NodeMemberEvent ev) {
            Children children = this.childrenRef.get();

            if (children == null) {
                return;
            }

            children.filterChildrenAdded(ev);
        }

        /* Informs that a set of children has been removed.
        * @param ev event describing the action
        */
        public void childrenRemoved(NodeMemberEvent ev) {
            Children children = this.childrenRef.get();

            if (children == null) {
                return;
            }

            children.filterChildrenRemoved(ev);
        }

        /* Informs that a set of children has been reordered.
        * @param ev event describing the action
        */
        public void childrenReordered(NodeReorderEvent ev) {
            Children children = this.childrenRef.get();

            if (children == null) {
                return;
            }

            children.filterChildrenReordered(ev);
        }

        /** Does nothing.
        * @param ev the event
        */
        public void nodeDestroyed(NodeEvent ev) {
        }
    }

    /** Filter node handle.
    */
    private static final class FilterHandle implements Node.Handle {
        static final long serialVersionUID = 7928908039428333839L;
        private Node.Handle original;

        public FilterHandle(Node.Handle original) {
            this.original = original;
        }

        public Node getNode() throws IOException {
            return new FilterNode(original.getNode());
        }

        @Override
        public String toString() {
            return "FilterHandle[" + original + "]"; // NOI18N
        }
    }

    /** Special ProxyLookup
     */
    private static final class FilterLookup extends Lookup {
        /** node we belong to */
        private FilterNode node;

        /** lookup we delegate too */
        private Lookup delegate;

        /** set of all results associated to this lookup */
        private Set<ProxyResult> results;

        FilterLookup() {
        }

        /** Registers own node.
         */
        public void ownNode(FilterNode n) {
            this.node = n;
        }

        /** A method that replaces instance of original node
         * with a new one
         */
        private <T> T replaceNodes(T orig, Class<T> clazz) {
            if (isNodeQuery(clazz) && (orig == node.getOriginal()) && clazz.isInstance(node)) {
                return clazz.cast(node);
            } else {
                return orig;
            }
        }

        /** Changes the node we delegate to if necessary.
         * @param n the node to delegate to
         */
        public Lookup checkNode() {
            Lookup l = node.getOriginal().getLookup();

            if (delegate == l) {
                return l;
            }

            Iterator<ProxyResult> toCheck = null;

            synchronized (this) {
                if (l != delegate) {
                    this.delegate = l;

                    if (results != null) {
                        toCheck = new ArrayList<>(results).iterator();
                    }
                }
            }

            if (toCheck != null) {
                // update

                while (toCheck.hasNext()) {
                    ProxyResult p = toCheck.next();

                    if (p.updateLookup(l)) {
                        p.resultChanged(null);
                    }
                }
            }

            return delegate;
        }

        public <T> Result<T> lookup(Template<T> template) {
            ProxyResult<T> p = new ProxyResult<T>(template);

            synchronized (this) {
                if (results == null) {
                    results = Collections.newSetFromMap(new WeakHashMap<>());
                }

                results.add(p);
            }

            return p;
        }

        public <T> T lookup(Class<T> clazz) {
            T result = checkNode().lookup(clazz);

            if (result == null && clazz.isInstance(node)) {
                result = clazz.cast(node);
            }

            return replaceNodes(result, clazz);
        }

        /** Finds out whether a query for a class can be influenced
         * by a state of the "nodes" lookup and whether we should
         * initialize listening
         */
        private static boolean isNodeQuery(Class<?> c) {
            return Node.class.isAssignableFrom(c) || c.isAssignableFrom(Node.class);
        }

        @Override
        public <T> Item<T> lookupItem(Template<T> template) {
            boolean nodeQ = isNodeQuery(template.getType());
            Item<T> i = checkNode().lookupItem(template);

            if (
                nodeQ && 
                i == null && 
                template.getType().isInstance(node) &&
                (template.getInstance() == null || template.getInstance() == node)
            ) {
                i = checkNode().lookupItem(wackohacko(template.getId(), template.getInstance()));
            }

            return nodeQ && i != null ? new FilterItem<T>(i, template.getType()) : i;
        }
        
        @SuppressWarnings("unchecked") // cannot type-check this but ought to be safe
        private static <T> Lookup.Template<T> wackohacko(String id, T instance) {
            return new Lookup.Template(Node.class, id, instance);
        }

        /**
         * Result used in SimpleLookup. It holds a reference to the collection
         * passed in constructor. As the contents of this lookup result never
         * changes the addLookupListener and removeLookupListener are empty.
         */
        private final class ProxyResult<T> extends Result<T> implements LookupListener {
            /** Template used for this result. It is never null.*/
            private Template<T> template;

            /** result to delegate to */
            private Lookup.Result<T> delegate;

            /** listeners set */
            private javax.swing.event.EventListenerList listeners;

            /** Just remembers the supplied argument in variable template.*/
            ProxyResult(Template<T> template) {
                this.template = template;
            }

            /** Checks state of the result
             */
            private Result<T> checkResult() {
                updateLookup(checkNode());

                return this.delegate;
            }

            /** Updates the state of the lookup.
             * @return true if the lookup really changed
             */
            public boolean updateLookup(Lookup l) {
                Collection<? extends Item<T>> oldPairs = (delegate != null) ? delegate.allItems() : null;

                synchronized (this) {
                    if (delegate != null) {
                        delegate.removeLookupListener(this);
                    }

                    delegate = l.lookup(template);

                    if (template.getType().isAssignableFrom(node.getClass()) && delegate.allItems().isEmpty()) {
                        delegate = l.lookup(wackohacko(template.getId(), template.getInstance()));
                    }

                    delegate.addLookupListener(this);
                }

                if (oldPairs == null) {
                    // nobody knows about a change
                    return false;
                }

                Collection<? extends Item<T>> newPairs = delegate.allItems();

                return !oldPairs.equals(newPairs);
            }

            public synchronized void addLookupListener(LookupListener l) {
                if (listeners == null) {
                    listeners = new javax.swing.event.EventListenerList();
                }

                listeners.add(LookupListener.class, l);
            }

            public synchronized void removeLookupListener(LookupListener l) {
                if (listeners != null) {
                    listeners.remove(LookupListener.class, l);
                }
            }

            public Collection<? extends T> allInstances() {
                Collection<? extends T> c = checkResult().allInstances();

                if (isNodeQuery(template.getType())) {
                    List<T> ll = new ArrayList<T>(c.size());
                    for (T o : c) {
                        ll.add(replaceNodes(o, template.getType()));
                    }
                    if (ll.isEmpty() && template.getType().isInstance(node)) {
                        if (template.getInstance() == null || template.getInstance() == node) {
                            ll.add(template.getType().cast(node));
                        }
                    }

                    return ll;
                } else {
                    return c;
                }
            }

            @Override
            public Set<Class<? extends T>> allClasses() {
                return checkResult().allClasses();
            }

            @Override
            public Collection<? extends Item<T>> allItems() {
                return checkResult().allItems();
            }

            /** A change in lookup occured.
             * @param ev event describing the change
             *
             */
            public void resultChanged(LookupEvent anEvent) {
                javax.swing.event.EventListenerList l = this.listeners;

                if (l == null) {
                    return;
                }

                Object[] listeners = l.getListenerList();

                if (listeners.length == 0) {
                    return;
                }

                LookupEvent ev = new LookupEvent(this);

                for (int i = listeners.length - 1; i >= 0; i -= 2) {
                    LookupListener ll = (LookupListener) listeners[i];
                    ll.resultChanged(ev);
                }
            }
        }
         // end of ProxyResult

        /** Item that exchanges the original node for the FilterNode */
        private final class FilterItem<T> extends Lookup.Item<T> {
            private Item<T> delegate;
            private Class<T> clazz;

            FilterItem(Item<T> d, Class<T> clazz) {
                this.delegate = d;
                this.clazz = clazz;
            }

            public String getDisplayName() {
                return delegate.getDisplayName();
            }

            public String getId() {
                return delegate.getId();
            }

            public T getInstance() {
                return replaceNodes(delegate.getInstance(), clazz);
            }

            public Class<? extends T> getType() {
                return delegate.getType();
            }
        }
    }
     // end of FilterLookup
}
