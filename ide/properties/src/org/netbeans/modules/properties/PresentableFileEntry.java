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

package org.netbeans.modules.properties;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/**
 * Object that represents one <code>FileEntry</code> and has support
 * for presentation of this entry as a node. I.&thinsp;i. it contains methods
 * required for a node, so that a node for the entry may just delegate to it.
 *
 * @author Jaroslav Tulach, Petr Jiricka
 */
public abstract class PresentableFileEntry extends FileEntry
                                           implements Node.Cookie {
    
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3328227388376142699L;
    
    /** The node delegate for this data object. */
    private transient Node nodeDelegate;
    
    /** Modified flag */
    private boolean modif = false;
    
    /** property change listener support */
    private transient PropertyChangeSupport changeSupport;
    
    /** listener for changes in the cookie set */
    private ChangeListener cookieL = new ChangeListener () {
        public void stateChanged (ChangeEvent ev) {
            firePropertyChange (Node.PROP_COOKIE, null, null);
        }
    };
    
    /** array of cookies for this entry */
    private transient CookieSet cookieSet;

    // guard used in getNodeDelegate
    private transient Object nodeDelegateMutex = new Object();
    
    /**
     * Creates a new presentable file entry initially attached
     * to a given file object.
     *
     * @param obj the data object this entry belongs to
     * @param fo the file object for the entry
     */
    public PresentableFileEntry(MultiDataObject obj, FileObject fo) {
        super (obj, fo);
    }
    

    /** Creates a node delegate for this entry. */
    protected abstract Node createNodeDelegate();
    
    /**
     * Gets a node delegate for this data object entry. Either
     * {@linkplain #createNodeDelegate creates it} (if it does not exist yet)
     * or returns a previously created instance of it.
     *
     * @return  the node delegate (without parent) for this data object
     */
    public final Node getNodeDelegate () {
        synchronized (nodeDelegateMutex) {
            if (nodeDelegate == null) {
                nodeDelegate = createNodeDelegate();
            }
            return nodeDelegate;
        }
    }
    
    /**
     * Sets value of attribute &quot;is template?&quot; for a given file.
     * Used also from FileEntry.
     *
     * @param  fo  file to assign the attribute to
     * @param  newValue  new value of the attribute
     * @return  <code>true</code> if the value was changed;
     *          <code>false</code> if the new value was the same
     *              as the old value
     * @exception  java.io.IOException  if the operation failed
     */
    private static boolean setTemplate(FileObject fo,
                                       boolean newValue) throws IOException {
        Object old = fo.getAttribute(DataObject.PROP_TEMPLATE);
        boolean oldValue = Boolean.TRUE.equals(old);
        if (newValue == oldValue) {
            return false;
        }
        fo.setAttribute(DataObject.PROP_TEMPLATE,
                        newValue ? Boolean.TRUE : null);
        return true;
    }
    
    /**
     * Sets value of attribute &quot;is template?&quot; for this entry's file.
     *
     * @param  newValue  new value of the attribute
     * @exception  java.io.IOException  if setting the template state fails
     */
    public final void setTemplate(boolean newValue) throws IOException {
        if (!setTemplate(getFile(), newValue)) {
            // no change in state
            return;
        }
        firePropertyChange(DataObject.PROP_TEMPLATE,
                           Boolean.valueOf(!newValue),
                           Boolean.valueOf(newValue));
    }
    
    /**
     * Get the template status of this data object entry.
     *
     * @return  <code>true</code> if it is a template;
     *          <code>false</code> otherwise
     */
    public boolean isTemplate() {
        Object o = getFile().getAttribute(DataObject.PROP_TEMPLATE);
        return Boolean.TRUE.equals(o);
    }
    
    /** 
     * Renames underlying fileobject. This implementation returns the
     * same file. Fires property change. Called when the DO is renamed, not the entry
     *
     * @param name new name
     * @return file object with renamed file
     * @see  #renameEntry
     */
    @Override
    public FileObject rename (String name) throws IOException {
        String oldName = getName();
        FileObject fo = super.rename(name);
        firePropertyChange(DataObject.PROP_NAME, oldName, name);
        return fo;
    }
    
    /** Renames underlying fileobject. This implementation return the
     * same file. Fires property change. Called when the file entry is renamed, not the DO
     *
     * @param name new name
     * @return file object with renamed file
     * @see  #rename
     */
    public FileObject renameEntry (String name) throws IOException {
        return rename(name);
    }
    
    /** Deletes file object and fires property change. */
    @Override
    public void delete () throws IOException {
        super.delete();
        
        firePropertyChange(DataObject.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
    }
    
    
    /** Test whether the object may be deleted.
     * @return <code>true</code> if it may
     */
    public abstract boolean isDeleteAllowed ();
    
    /** Test whether the object may be copied.
     * @return <code>true</code> if it may
     */
    public abstract boolean isCopyAllowed ();
    
    /** Test whether the object may be moved.
     * @return <code>true</code> if it may
     */
    public abstract boolean isMoveAllowed ();
    
    /** Test whether the object may create shadows.
     * <p>The default implementation returns <code>true</code>.
     * @return <code>true</code> if it may
     */
    public boolean isShadowAllowed () {
        return true;
    }
    
    /** Test whether the object may be renamed.
     * @return <code>true</code> if it may
     */
    public abstract boolean isRenameAllowed ();
    
    
    /** Test whether the object is modified.
     * @return <code>true</code> if it is modified
     */
    public boolean isModified() {
        return modif;
    }
    
    /** Set whether the object is considered modified.
     * Also fires a change event.
     * If the new value is <code>true</code>, the data object is added into
     * a {@link #getRegistry registry} of opened data objects.
     * If the new value is <code>false</code>,
     * the data object is removed from the registry.
     */
    public void setModified(boolean modif) {
        if (this.modif != modif) {
            this.modif = modif;
            firePropertyChange(DataObject.PROP_MODIFIED,
                               Boolean.valueOf(!modif),
                               Boolean.valueOf(modif));
        }
    }
    
    /**
     * Get help context for this object.
     *
     * @return the help context
     */
    public abstract HelpCtx getHelpCtx ();
    
    /**
     * Returns name of the data object.
     * <p>
     * The default implementation returns name of the primary file.
     *
     * @return  the name of the data object
     */
    public String getName () {
        return getFile ().getName ();
    }
    
    /**
     * Returns a folder this data object is stored in.
     *
     * @return  folder this data object is stored in;
     *          or <code>null</code> if the data object's primary file
     *          is the {@linkplain FileObject#isRoot root} of its filesystem
     */
    public final DataFolder getFolder () {
        FileObject fo = getFile ().getParent ();
        // could throw IllegalArgumentException but only if fo is not folder
        // => then there is a bug in filesystem implementation
        return fo == null ? null : DataFolder.findFolder (fo);
    }
    
    
    //
    // Property change support
    //
    
    /** @param l the listener
     */
    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        getChangeSupport ().addPropertyChangeListener (l);
    }
    
    /** @param l the listener
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        getChangeSupport ().removePropertyChangeListener (l);
    }
    
    /** Fires property change notification to all listeners registered via
     * {@link #addPropertyChangeListener}.
     *
     * @param name of property
     * @param oldValue old value
     * @param newValue new value
     */
    protected final void firePropertyChange (String name, Object oldValue, Object newValue) {
        getChangeSupport ().firePropertyChange (name, oldValue, newValue);
    }
    
    /** Getter for standard property change support. This is used in
     * this class and by this method provided to subclasses.
     *
     * @return support
     */
    private final synchronized PropertyChangeSupport getChangeSupport () {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport (this);
        }
        return changeSupport;
    }

    /** Set the set of cookies.
     * To the provided cookie set a listener is attached,
     * and any change to the set is propagated by
     * firing a change on {@link #PROP_COOKIE}.
     *
     * @param s the cookie set to use
     * @deprecated
     */
    @Deprecated
    protected final synchronized void setCookieSet (CookieSet s) {
        if (cookieSet != null) {
            cookieSet.removeChangeListener (cookieL);
    }

        s.addChangeListener (cookieL);
        cookieSet = s;

        firePropertyChange (Node.PROP_COOKIE, null, null);
    }
    
    /** Get the set of cookies.
     * If the set had been
     * previously set by {@link #setCookieSet}, that set
     * is returned. Otherwise an empty set is
     * returned.
     *
     * @return the cookie set (never <code>null</code>)
     */
    protected final CookieSet getCookieSet () {
        CookieSet s = cookieSet;
        if (s != null) {
            return s;
        }
        synchronized (this) {
            if (cookieSet != null) {
                return cookieSet;
            }
            // sets an empty sheet and adds a listener to it
            setCookieSet (CookieSet.createGeneric(null));
            cookieSet.assign(FileObject.class, getFile());
            return cookieSet;
        }
    }
    
    /**
     * Looks for a cookie in the current cookie set matching the requested class.
     *
     * @param type the class to look for
     * @return an instance of that class, or <code>null</code> if this class of cookie
     *    is not supported
     */
    @SuppressWarnings("unchecked")
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        CookieSet c = cookieSet;
        if (c != null) {
            T cookie = c.getCookie(type);
            if (cookie != null) {
                return cookie;
            }
        }
        
        if (type.isInstance (this)) {
            return (T) this;
        }
        return null;
    }
}
