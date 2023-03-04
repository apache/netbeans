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
package org.openide.nodes;

import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.*;

import java.awt.Image;
import java.awt.datatransfer.Transferable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.text.MessageFormat;

import java.util.*;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;


/** A basic implementation of a node.
*
* <p>It simplifies creation of the display name, based on a message
* format and the system name. It also simplifies working with icons:
* one need only specify the base name and all icons will be loaded
* when needed. Other common requirements are handled as well.
*
* @author Jaroslav Tulach */
public class AbstractNode extends Node {
    /** messages to create a resource identification for each type of
    * icon from the base name for the icon.
    */
    private static final String[] icons = {
        
        // color 16x16
        "", // NOI18N

        // color 32x32
        "32", // NOI18N

        // mono 16x16
        "", // NOI18N

        // mono 32x32
        "32", // NOI18N

        // opened color 16x16
        "Open", // NOI18N

        // opened color 32x32
        "Open32", // NOI18N

        // opened mono 16x16
        "Open", // NOI18N

        // opened mono 32x32
        "Open32" // NOI18N
    };

    /** To index normal icon from previous array use
    *  + ICON_BASE.
    */
    private static final int ICON_BASE = -1;

    /** for indexing opened icons */
    private static final int OPENED_ICON_BASE = 3;

    /** empty array of paste types */
    private static final PasteType[] NO_PASTE_TYPES = {  };

    /** empty array of new types */
    private static final NewType[] NO_NEW_TYPES = {  };

    /** default icon base for all nodes */
    private static final String DEFAULT_ICON_BASE = "org/openide/nodes/defaultNode"; // NOI18N
    private static final String DEFAULT_ICON_EXTENSION = ".gif"; // NOI18N
    private static final String DEFAULT_ICON = DEFAULT_ICON_BASE + ".png"; // NOI18N
    
    // maps class either to Boolean or to this
    private static final WeakHashMap<Class, Object> overridesGetDefaultAction = new WeakHashMap<Class, Object>(32);

    /** Message format to use for creation of the display name.
    * It permits conversion of text from
    * {@link #getName} to the one sent to {@link #setDisplayName}. The format can take
    * one parameter, <code>{0}</code>, which will be filled by a value from <CODE>getName()</CODE>.
    *
    * <p>The default format just uses the simple name; subclasses may
    * change it, though it will not take effect until the next {@link #setName} call.
    *
    * <p>Can be set to <CODE>null</CODE>. Then there is no connection between the
    * name and display name; they may be independently modified.  */
    protected MessageFormat displayFormat;

    /** Preferred action */
    private Action preferredAction;

    /** Resource base for icons (without suffix denoting right icon)  */
    private String iconBase = DEFAULT_ICON_BASE;

    /** Resource extension for icons  */
    private String iconExtension = ".png"; // NOI18N
    
    /** array of cookies for this node */
    private Object lookup;

    /** set of properties to use */
    private Sheet sheet;

    /** Actions for the node. They are used only for the pop-up menus
     * of this node.
     * @deprecated Override {@link #getActions(boolean)} instead of using
     * this field.
     */
    @Deprecated protected SystemAction[] systemActions;
    private SheetAndCookieListener sheetCookieL = null;

    /** Create a new abstract node with a given child set.
    * @param children the children to use for this node
    */
    public AbstractNode(Children children) {
        this(children, null);
    }

    /** Create a new abstract node with a given child set and associated
    * lookup. If you use this constructor, please do not call methods
    * {@link #getCookieSet} and {@link #setCookieSet} they will throw an
    * exception.
    * <p>
    * More info on the correct usage of constructor with Lookup can be found
    * in the {@link Node#Node(org.openide.nodes.Children, org.openide.util.Lookup)}
    * javadoc.
    *
    * @param children the children to use for this node
    * @param lookup the lookup to provide content of {@link #getLookup}
    *   and also {@link #getCookie}
    * @since 3.11
    */
    public AbstractNode(Children children, Lookup lookup) {
        super(children, lookup);

        // Setting the name to non-null value for the node
        // to return "reasonable" name and displayName
        // not using this.setName since the descendants
        // can override it and might assume that it is
        // not called from constructor (see e.g. DataNode)
        super.setName(""); // NOI18N
    }
    
    /** Fake node constructor with given CookieSet
     */
    AbstractNode(CookieSet set) {
        super(Children.LEAF);
        lookup = set;
    }

    /** Clone the node. If the object implements {@link Cloneable},
    * that is used; otherwise a {@link FilterNode filter node}
    * is created.
    *
    * @return copy of this node
    */
    public Node cloneNode() {
        try {
            if (this instanceof Cloneable) {
                return (Node) clone();
            }
        } catch (CloneNotSupportedException ex) {
        }

        return new FilterNode(this);
    }

    /** Set the system name. Fires a property change event.
    * Also may change the display name according to {@link #displayFormat}.
    *
    * @param s the new name
    */
    public void setName(String s) {
        super.setName(s);

        MessageFormat mf = displayFormat;

        if (mf != null) {
            setDisplayName(mf.format(new Object[] { s }));
        } else {
            // additional hack, because if no display name is set, then it
            // is taken from the getName, that means calling setName can
            // also change display name
            // fix of 10665
            fireDisplayNameChange(null, null);
        }
    }

    /** Change the icon.
    * One need only specify the base resource name without extension;
    * the real name of the icon is obtained by the applying icon message
    * formats.
    *
    * The method effectively behaves as if it was just delegating
    * to {@link #setIconBaseWithExtension(java.lang.String)}
    * using <code>base + ".gif"</code> as parameter.
    *
    * @param base base resouce name (no initial slash)
    * @deprecated Use {@link #setIconBaseWithExtension(java.lang.String)}
    */
    @Deprecated
    public void setIconBase(String base) {
        setIconBaseWithExtension(base, DEFAULT_ICON_EXTENSION);
    }

    /** Change the icon.
    * One need only specify the base name of the icon resource,
    * including the resource extension; the real name of the icon is obtained
    * by inserting proper infixes into the resource name.
    *
    * <p>For example, for the base <code>org/foo/resource/MyIcon.png</code>
    * the following images may be used according to the icon
    * state and {@link java.beans.BeanInfo#getIcon presentation type}:
    *
    * <ul>
    * <li><code>org/foo/resource/MyIcon.png</code>
    * <li><code>org/foo/resource/MyIconOpen.png</code>
    * <li><code>org/foo/resource/MyIcon32.png</code>
    * <li><code>org/foo/resource/MyIconOpen32.png</code></ul>
    *
    * <P>
    * This method may be used to dynamically switch between different sets
    * of icons for different configurations. If the set is changed,
    * an icon property change event is fired.
    *
    * @param baseExt base resouce name with extension (no initial slash)
    * @since org.openide.nodes 6.5
    */
    public final void setIconBaseWithExtension(String baseExt) {
        int lastDot = baseExt.lastIndexOf('.');
        int lastSlash = baseExt.lastIndexOf('/');
        
        if ((lastSlash > lastDot) || (lastDot == -1)) { // no .extension
            setIconBaseWithExtension(baseExt, "");
        } else {
            String base = baseExt.substring(0, lastDot);
            String ext = baseExt.substring(lastDot);
            setIconBaseWithExtension(base, ext);
        }

    }

    /** Change the icon. */
    private final void setIconBaseWithExtension(String base, String extension) {
        if (base.equals(iconBase) && extension.equals(iconExtension)) {
            return;
        }

        this.iconBase = base;
        this.iconExtension = extension;
        fireIconChange();
        fireOpenedIconChange();
    }

    

    /** Find an icon for this node. Uses an {@link #setIconBase icon set}.
    *
    * @param type constants from {@link java.beans.BeanInfo}
    *
    * @return icon to use to represent the bean
    */
    public Image getIcon(int type) {
        return findIcon(type, ICON_BASE);
    }

    /** Finds an icon for this node when opened. This icon should represent the node
    * only when it is opened (when it can have children).
    *
    * @param type as in {@link #getIcon}
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon(int type) {
        return findIcon(type, OPENED_ICON_BASE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Tries to find the right icon for the iconbase.
    * @param type type of icon (from BeanInfo constants)
    * @param ib base where to scan in the array
    */
    private Image findIcon(int type, int ib) {
        String res = iconBase + icons[type + ib] + iconExtension;
        Image im = ImageUtilities.loadImage(res, true);

        if (im != null) {
            return im;
        }

        // try the first icon
        res = iconBase + icons[java.beans.BeanInfo.ICON_COLOR_16x16 + ib] + iconExtension;

        im = ImageUtilities.loadImage(res, true);

        if (im != null) {
            return im;
        }

        if (ib == OPENED_ICON_BASE) {
            // try closed icon also
            return findIcon(type, ICON_BASE);
        }

        // if still not found return default icon
        return getDefaultIcon();
    }

    Image getDefaultIcon() {
        Image i = ImageUtilities.loadImage(DEFAULT_ICON, true);

        if (i == null) {
            throw new MissingResourceException("No default icon", "", DEFAULT_ICON); // NOI18N
        }

        return i;
    }

    /** Can this node be renamed?
    * @return <code>false</code>
    */
    public boolean canRename() {
        return false;
    }

    /** Can this node be destroyed?
    * @return <CODE>false</CODE>
    */
    public boolean canDestroy() {
        return false;
    }

    /** Set the set of properties.
    * A listener is attached to the provided sheet
    * and any change of the sheet is propagated to the node by
    * firing a {@link #PROP_PROPERTY_SETS} change event.
    *
    * @param s the sheet to use
    */
    protected final synchronized void setSheet(Sheet s) {
        setSheetImpl(s);
        firePropertySetsChange(null, null);
    }

    private synchronized void setSheetImpl(Sheet s) {
        if (sheetCookieL == null) {
            sheetCookieL = new SheetAndCookieListener();
        }

        if (sheet != null) {
            sheet.removePropertyChangeListener(sheetCookieL);
        }

        s.addPropertyChangeListener(sheetCookieL);
        sheet = s;
    }

    /** Initialize a default
    * property sheet; commonly overridden. If {@link #getSheet}
    * is called and there is not yet a sheet,
    * this method is called to allow a subclass
    * to specify its properties.
    * <P>
    * <em>Warning:</em> Do not call <code>getSheet</code> in this method.
    * <P>
    * The default implementation returns an empty sheet.
    *
    * @return the sheet with initialized values (never <code>null</code>)
    */
    protected Sheet createSheet() {
        return new Sheet();
    }

    /** Get the current property sheet. If the sheet has been
     * previously set by a call to {@link #setSheet}, that sheet
     * is returned. Otherwise {@link #createSheet} is called.
     *
     * @return the sheet (never <code>null</code>)
     */
    protected final synchronized Sheet getSheet() {
        if (sheet != null) {
            return sheet;
        }

        Sheet s = createSheet();
        if (s == null) {
            // #150503
            throw new IllegalStateException("createSheet returns null in " + this.getClass().getName()); // NOI18N
        }
        setSheetImpl(s);

        return s;
    }

    /** Get a list of property sets.
    *
    * @return the property sets for this node
    * @see #getSheet
    */
    public PropertySet[] getPropertySets() {
        Sheet s = getSheet();

        return s.toArray();
    }

    boolean propertySetsAreKnown() {
        return (sheet != null);
    }

    /** Copy this node to the clipboard.
    *
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one copy flavor
    * @throws IOException if it could not copy
    * @see NodeTransfer
    */
    public Transferable clipboardCopy() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.CLIPBOARD_COPY);
    }

    /** Cut this node to the clipboard.
    *
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one cut flavor
    * @throws IOException if it could not cut
    * @see NodeTransfer
    */
    public Transferable clipboardCut() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.CLIPBOARD_CUT);
    }

    /**
    * This implementation only calls clipboardCopy supposing that
    * copy to clipboard and copy by d'n'd are similar.
    *
    * @return transferable to represent this node during a drag
    * @exception IOException when the
    *    cut cannot be performed
    */
    public Transferable drag() throws IOException {
        return clipboardCopy();
    }

    /** Can this node be copied?
    * @return <code>true</code>
    */
    public boolean canCopy() {
        return true;
    }

    /** Can this node be cut?
    * @return <code>false</code>
    */
    public boolean canCut() {
        return false;
    }

    /** Accumulate the paste types that this node can handle
    * for a given transferable.
    * <P>
    * The default implementation simply tests whether the transferable supports
    * intelligent pasting via {@link NodeTransfer#findPaste}, and if so, it obtains the paste types
    * from the {@link NodeTransfer.Paste transfer data} and inserts them into the set.
    * <p>Subclass implementations should typically call super (first or last) so that they
    * add to, rather than replace, a superclass's available paste types; especially as the
    * default implementation in <code>AbstractNode</code> is generally desirable to retain.
    *
    * @param t a transferable containing clipboard data
    * @param s a list of {@link PasteType}s that will have added to it all types
    *    valid for this node (ordered as they will be presented to the user)
    */
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        NodeTransfer.Paste p = NodeTransfer.findPaste(t);

        if (p != null) {
            // adds all its types into the set
            s.addAll(Arrays.asList(p.types(this)));
        }
    }

    /** Determine which paste operations are allowed when a given transferable is in the clipboard.
    * Subclasses should override {@link #createPasteTypes}.
    *
    * @param t the transferable in the clipboard
    * @return array of operations that are allowed
    */
    public final PasteType[] getPasteTypes(Transferable t) {
        List<PasteType> s = new LinkedList<PasteType>();
        createPasteTypes(t, s);

        return s.toArray(NO_PASTE_TYPES);
    }

    /** Default implementation that tries to delegate the implementation
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
        java.util.List<PasteType> s = new LinkedList<PasteType>();
        createPasteTypes(t, s);

        return s.isEmpty() ? null : s.get(0);
    }

    /* List new types that can be created in this node.
    * @return new types
    */
    public NewType[] getNewTypes() {
        return NO_NEW_TYPES;
    }

    /** Checks whether subclass overrides a method
     */
    private boolean overridesAMethod(String name, Class[] arguments) {
        // we are subclass of AbstractNode
        try {
            java.lang.reflect.Method m = getClass().getMethod(name, arguments);

            if (m.getDeclaringClass() != AbstractNode.class) {
                // ok somebody overriden the method
                return true;
            }
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    /** Gets preferred action.
      * By default, null.
      * @return preferred action
      * @see Node#getPreferredAction
      * @since 3.29
      */
    public Action getPreferredAction() {
        boolean delegate = false;

        Class c = getClass();

        if (c != AbstractNode.class) {
            synchronized (overridesGetDefaultAction) {
                Object in = overridesGetDefaultAction.get(c);

                if (in == this) {
                    // catched in a loop of overriding getDefaultAction and
                    // calling super.getDefaultAction
                    // pretend that we do not override
                    overridesGetDefaultAction.put(c, Boolean.FALSE);

                    return preferredAction;
                }

                Boolean b;

                if (in == null) {
                    b = overridesAMethod("getDefaultAction", new Class[0]) ? Boolean.TRUE : Boolean.FALSE; // NOI18N

                    if (b.booleanValue()) {
                        // check whether it is safe to call the getDefaultAction
                        overridesGetDefaultAction.put(c, this);
                        getDefaultAction();

                        if (overridesGetDefaultAction.get(c) == this) {
                            // value unchanged, we have not been cought in a loop
                            overridesGetDefaultAction.put(c, b);
                        }
                    } else {
                        overridesGetDefaultAction.put(c, b);
                    }
                } else {
                    b = (Boolean) in;
                }

                delegate = b.booleanValue();
            }
        }

        return delegate ? getDefaultAction() : preferredAction;
    }

    /** Gets the default action. Overrides superclass method.
    * @return if there is a default action set, then returns it
     * @deprecated Use {@link #getPreferredAction} instead.
    */
    @Deprecated
    public SystemAction getDefaultAction() {
        Action a = getPreferredAction();

        if (a instanceof SystemAction) {
            return (SystemAction) a;
        }

        return null;
    }

    /** Set a default action for the node.
    * @param action the new default action, or <code>null</code> for none
    * @deprecated Override {@link #getPreferredAction} instead.
    */
    @Deprecated
    public void setDefaultAction(SystemAction action) {
        preferredAction = action;
    }

    /** Get all actions for the node.
    * Initialized with {@link #createActions}, or with the superclass's list.
    *
    * @return actions for the node
     * @deprecated Override {@link #getActions(boolean)} instead.
    */
    @Deprecated
    public SystemAction[] getActions() {
        if (systemActions == null) {
            systemActions = createActions();

            if (systemActions == null) {
                systemActions = super.getActions();
            }
        }

        return systemActions;
    }

    /** Lazily initialize set of node's actions (overridable).
    * The default implementation returns <code>null</code>.
    * <p><em>Warning:</em> do not call {@link #getActions} within this method.
    * @return array of actions for this node, or <code>null</code> to use the default node actions
     * @deprecated Override {@link #getActions(boolean)} instead.
    */
    @Deprecated
    protected SystemAction[] createActions() {
        return null;
    }

    /** Does this node have a customizer?
    * @return <CODE>false</CODE>
    */
    public boolean hasCustomizer() {
        return false;
    }

    /** Get the customizer.
    * @return <code>null</code> in the default implementation
    */
    public java.awt.Component getCustomizer() {
        return null;
    }

    /** Set the cookie set.
    * A listener is attached to the provided cookie set,
    * and any change of the sheet is propagated to the node by
    * firing {@link #PROP_COOKIE} change events.
    *
    * @param s the cookie set to use
    * @deprecated just use getCookieSet().add(...) instead
    * @exception IllegalStateException If you pass a Lookup instance into the constructor, this
    *   method cannot be called.
    */
    @Deprecated
    protected final synchronized void setCookieSet(CookieSet s) {
        if (internalLookup(false) != null) {
            throw new IllegalStateException("CookieSet cannot be used when lookup is associated with the node"); // NOI18N
        }

        if (sheetCookieL == null) {
            sheetCookieL = new SheetAndCookieListener();
        }

        CookieSet cookieSet = (CookieSet) lookup;

        if (cookieSet != null) {
            cookieSet.removeChangeListener(sheetCookieL);
        }

        s.addChangeListener(sheetCookieL);
        lookup = s;

        fireCookieChange();
    }

    /** Get the cookie set.
    *
    * @return the cookie set created by {@link #setCookieSet}, or an empty set (never <code>null</code>)
    * @exception IllegalStateException If you pass a Lookup instance into the constructor, this
    *   method cannot be called.
    */
    protected final CookieSet getCookieSet() {
        if (internalLookup(false) != null) {
            throw new IllegalStateException("CookieSet cannot be used when lookup is associated with the node"); // NOI18N
        }

        CookieSet s = (CookieSet) lookup;

        if (s != null) {
            return s;
        }

        synchronized (this) {
            if (lookup != null) {
                return (CookieSet) lookup;
            }

            // sets empty sheet and adds a listener to it
            setCookieSet(new CookieSet());

            return (CookieSet) lookup;
        }
    }

    /** Get a cookie from the node.
    * Uses the cookie set as determined by {@link #getCookieSet}.
    *
    * @param type the representation class
    * @return the cookie or <code>null</code>
    */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        if (lookup instanceof CookieSet) {
            CookieSet c = (CookieSet) lookup;

            return c.getCookie(type);
        } else {
            return super.getCookie(type);
        }
    }

    /** Get a serializable handle for this node.
    * @return a {@link DefaultHandle} in the default implementation
    */
    public Handle getHandle() {
        return DefaultHandle.createHandle(this);
    }

    /** Listener for changes in the sheet and the cookie set. */
    private final class SheetAndCookieListener implements PropertyChangeListener, ChangeListener {
        SheetAndCookieListener() {
        }

        public void propertyChange(PropertyChangeEvent ev) {
            AbstractNode.this.firePropertySetsChange(null, null);
        }

        public void stateChanged(ChangeEvent ev) {
            AbstractNode.this.fireCookieChange();
        }
    }
}
