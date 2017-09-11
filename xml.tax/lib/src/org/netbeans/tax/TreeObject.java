/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.tax;

import java.io.PrintStream;
import java.beans.PropertyChangeListener;

import org.netbeans.tax.event.TreeEventManager;
import org.netbeans.tax.event.TreeEventModel;
import org.netbeans.tax.event.TreeEvent;
import org.netbeans.tax.event.TreeEventChangeSupport;

/**
 * Tree objects base class with support for firing <b>events</b> and <b>merging</b>.
 * <p>
 * It also prescribes that each subclass MUST have <b>copy constuctor</b>
 * calling its superclass copy constructor. The copy constructor MUST be  then called
 * during <b>cloning</b>.
 * <p>
 * All TreeObject subclasses should not have public contructors and therefore
 * should be created just by factory methods.
 * <p>
 * Pending: validation on request, invalidation
 *
 * @author  Libor Kramolis
 * @author  Petr Kuzel
 * @version 0.1
 */
public abstract class TreeObject implements TreeEventModel {
    
    /** */
    public static final String PROP_READ_ONLY = "readOnly"; // NOI18N
    
    /** */
    private boolean readOnly;
    
    /** */
    transient private TreeEventChangeSupport eventChangeSupport;
    
    
    //
    // init
    //
    
    
    /** Creates new TreeObject. */
    protected TreeObject () {
        this.readOnly           = false;
        this.eventChangeSupport = null;
    }
    
    /**
     * Creates new TreeObject - copy constructor.
     * (it does not copy eventChangeSupport)
     */
    protected TreeObject (TreeObject object) {
        this.readOnly = object.readOnly;
        this.eventChangeSupport = null;
    }
    
    
    //
    // clone
    //
    
    /**
     * Cloning must use copy constructors!
     */
    public abstract Object clone ();
    
    
    //
    // util
    //
    
    /**
     */
    protected final boolean isInstance (Object object) {
        return ( this.getClass ().isInstance (object) );
    }
    
    
    //
    // context
    //
    
    /**
     */
    abstract public boolean isInContext ();
    
    /**
     */
    abstract public void removeFromContext () throws ReadOnlyException;
    
    
    //
    // equals
    //
    
    /**
     */
    public /*final*/ boolean equals (Object object) {
        return super.equals (object);
        
        // when TreeObjectList will compare inserted object by 'instance' instead of 'equals' we should final this method and use this impl:
        //          return equals (object, true);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! isInstance (object))
            return false;
        
        TreeObject peer = (TreeObject) object;
        
        return (this.readOnly == peer.readOnly);
    }
    
    
    //
    // merge
    //
    
    /**
     * <p>Update algorithm pattern that <b>reuses original tree instances</b>:
     * <pre>
     *    // 1. optimalization
     *    if (this == treeObject) return;
     *
     *    // 2. can merge just my instances (so no cross implemetation merge allowed)
     *    if (getClass().isAssignablFrom(treeObject.getClass())) throw CannotMergeException;
     *
     *    // 3. let superclass do its merge
     *    super.merge(treeObject);
     *
     *    // 4. cast to myself (see step 2)
     *    {getClass()} peer = ({getClass()}) treeObject;
     *
     *    // 5. merge all fields at THIS CLASS HIEARCHY LEVEL but
     *    // fields that references object "parents"
     *    // use setters that just fires property changes, i.e. such that never fails
     *    // due to read-only or other constrains checks
     *
     *    foreach field in suitableClassFields
     *         if field is simple
     *            set{field}Impl( peer.get{field}() )
     *         if field is collection or TreeObject
     *            {field}.merge(peer.{field})
     *    next field
     *
     * </pre>
     * @param treeobject merge peer
     * @throws CannotMergeException if can not merge with given node (invalid class)
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        if (treeObject == this)
            return;
        
        checkMergeObject (treeObject);
        
        TreeObject peer = treeObject;
        
        setReadOnly (peer.isReadOnly ());
    }
    
    /**
     */
    protected final void checkMergeObject (TreeObject treeObject) throws CannotMergeException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeObject::checkMergeObject: this        = " + this); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("          ::checkMergeObject: treeObject  = " + treeObject); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("            checkMergeObject: isSameClass ? " + isInstance (treeObject)); // NOI18N
        
        if ( (treeObject == null) || (!!! isInstance (treeObject)) ) {
            throw new CannotMergeException (treeObject);
        }
    }
    
    //
    // read only
    //
    
    /**
     */
    public final boolean isReadOnly () {
        return readOnly;
    }
    
    /**
     */
    protected void setReadOnly (boolean newReadOnly) {
        if (readOnly == newReadOnly)
            return;
        
        boolean oldReadOnly = this.readOnly;
        this.readOnly = newReadOnly;
        firePropertyChange (getEventChangeSupport ().createEvent (PROP_READ_ONLY, oldReadOnly ? Boolean.TRUE : Boolean.FALSE, newReadOnly ? Boolean.TRUE : Boolean.FALSE));
    }
    
    /**
     */
    protected final void checkReadOnly () throws ReadOnlyException {
        if (readOnly == true) {
            throw new ReadOnlyException (this);
        }
    }
    
    
    
    //
    // event model
    //
    
    /**
     * @return support that delegates to TreeEventManager
     */
    protected final TreeEventChangeSupport getEventChangeSupport () {
        if (eventChangeSupport == null) {
            eventChangeSupport = new TreeEventChangeSupport (this);
        }
        return eventChangeSupport;
    }
    
    /**
     * Get assigned event manager.
     * Whole document should have only one and same EventManager. When there is not
     * available manager, it returns null.
     *
     * @return assigned event manager (may be null).
     */
    public abstract TreeEventManager getEventManager ();
    
    
    /**
     */
    //    protected final void addEventManagerChangeListener (PropertyChangeListener listener) {
    //        getEventChangeSupport().addPropertyChangeListener (PROP_EVENT_MANAGER, listener);
    //    }
    
    /**
     */
    //    protected final void removeEventManagerChangeListener (PropertyChangeListener listener) {
    //        getEventChangeSupport().removePropertyChangeListener (PROP_EVENT_MANAGER, listener);
    //    }
    
    /**
     */
    public final void addReadonlyChangeListener (PropertyChangeListener listener) {
        getEventChangeSupport ().addPropertyChangeListener (PROP_READ_ONLY, listener);
    }
    
    /**
     */
    public final void removeReadonlyChangeListener (PropertyChangeListener listener) {
        getEventChangeSupport ().removePropertyChangeListener (PROP_READ_ONLY, listener);
    }
    
    
    /**
     * Add a PropertyChangeListener to the listener list.
     * @param listener The listener to add.
     */
    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Tree " + this + "attached listener" + listener); // NOI18N

        getEventChangeSupport ().addPropertyChangeListener (listener);
    }
    
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to remove.
     */
    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        getEventChangeSupport ().removePropertyChangeListener (listener);
    }
    
    /**
     * Fire an existing TreeEvent to any registered listeners.
     * No event is fired if the given event's old and new values are
     * equal and non-null.
     * @param evt  The TreeEvent object.
     */
    protected final void firePropertyChange (TreeEvent evt) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeObject firing " + evt); // NOI18N
        
        getEventChangeSupport ().firePropertyChange (evt);
        bubblePropertyChange (evt);
    }
    
    
    /** Add a PropertyChangeListener for a specific property to the listener list.
     * @param propertyname Name of the property to listen on.
     * @param listener The listener to add.
     */
    public final void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        getEventChangeSupport ().addPropertyChangeListener (propertyName, listener);
    }
    
    /** Removes a PropertyChangeListener for a specific property from the listener list.
     * @param propertyname Name of the property that was listened on.
     * @param listener The listener to remove.
     */
    public final void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        getEventChangeSupport ().removePropertyChangeListener (propertyName, listener);
    }
    
    
    /**
     * Check if there are any listeners for a specific property.
     *
     * @param propertyName  the property name.
     * @return true if there are ore or more listeners for the given property
     */
    public final boolean hasPropertyChangeListeners (String propertyName) {
        return getEventChangeSupport ().hasPropertyChangeListeners (propertyName);
    }
    
    
    /**
     * Report a bound property update to any registered listeners.
     * No event is fired if old and new are equal and non-null.
     *
     * @param propertyName The programmatic name of the property that was changed.
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     */
    protected final void firePropertyChange (String propertyName, Object oldValue, Object newValue) {
        firePropertyChange (getEventChangeSupport ().createEvent (propertyName, oldValue, newValue));
    }
    
    
    /**
     * Propagate event to parents' listeners.
     */
    protected final void bubblePropertyChange (TreeEvent origEvt) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeObject [ " + this + " ]::bubblePropertyChange: origEvt = " + origEvt.getPropertyName ()); // NOI18N
        
        TreeObject source = (TreeObject)origEvt.getSource ();
        if ( source instanceof TreeAttribute ) {
            TreeAttribute attr = (TreeAttribute)source;
            TreeElement ownElem = attr.getOwnerElement ();
            if ( ownElem != null ) {
                ownElem.firePropertyChange (TreeElement.PROP_ATTRIBUTES, attr, null);
            }
        } else if ( source instanceof TreeChild ) {
            while ( source != null ) {
                TreeChild child = (TreeChild)source;
                TreeParentNode parent = child.getParentNode ();
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    ::bubblePropertyChange::parentNode = " + parent); // NOI18N
                
                if ( parent != null ) {
                    parent.getEventChangeSupport ().firePropertyChange (origEvt.createBubbling (parent));
                }
                source = parent;
            }
        }
    }
    
    
    //
    // debug
    //
    
    /**
     * For debugging purposes.
     */
    public final String listListeners () {
        return getEventChangeSupport ().listListeners ();
    }
    
}
