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
package org.netbeans.tax;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.LinkedList;

import java.beans.PropertyChangeListener;

import org.netbeans.tax.event.TreeEventManager;
import org.netbeans.tax.event.TreeNodeContentEventModel;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeObjectList extends TreeObject implements TreeNodeContentEventModel, List {
    // toDo:
    // + set inserted object readOnly value as object list has

    /** Name of property for insert node to node content. */
    public static final String PROP_CONTENT_INSERT = "contentInsert"; // NOI18N
    
    /** Name of property for remove node from node content. */
    public static final String PROP_CONTENT_REMOVE = "contentRemove"; // NOI18N
    
    /** Name of property for change order in node content. */
    public static final String PROP_CONTENT_ORDER  = "contentOrder"; // NOI18N
    
    
    /** */
    private ContentManager contentManager;
    
    /** */
    private List list;
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeObjectList.
     */
    protected TreeObjectList (ContentManager contentManager) {
        super ();
        
        this.contentManager = contentManager;
        this.list           = new LinkedList ();
    }
    
    /** Creates new TreeObjectList -- copy constructor. */
    protected TreeObjectList (TreeObjectList objectList) {
        super (objectList);
        
        this.contentManager = null;
        
        this.list = new LinkedList ();
        Iterator it = objectList.iterator ();
        
        boolean wasReadOnly = this.isReadOnly ();
        if ( wasReadOnly ) {
            this.setReadOnly (false);
        }
        while ( it.hasNext () ) {
            this.add (((TreeObject)it.next ()).clone ());
        }
        if ( wasReadOnly ) {
            this.setReadOnly (true);
        }
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeObjectList (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeObjectList peer = (TreeObjectList) object;
        if ( this.list.size () != peer.list.size () )
            return false;
        
        Iterator thisIt = this.list.iterator ();
        Iterator peerIt = peer.list.iterator ();
        while ( thisIt.hasNext () ) {
            Object thisNext = thisIt.next ();
            Object peerNext = peerIt.next ();
            if (!!! Util.equals (thisNext, peerNext))
                return false;
        }
        
        return true;
    }
    
    /*
     * Merge list content by merging instances using findMergeCandidate().
     * Let list's ContentManagers update structural links.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeObjectList peer = (TreeObjectList) treeObject;
        
        // merge nodeList;
        // try to reuse old instances by type in FIFO order
        // note that in tree editor (and anyone manipulating this structure directly)
        // it is ok so no explorer node collapsing would occure
        // see children package from related details
        
        // I think no optimalization can be performend since
        // there is not uniq id of element
        
        boolean wasReadOnly = this.isReadOnly ();
        if ( wasReadOnly ) {
            this.setReadOnly (false);
        }
        
        TreeObject[] backupArray = (TreeObject[]) list.toArray (new TreeObject[0]);
        list.clear ();  //do not use content manager, do not fire
        
        short policy = TreeEventManager.FIRE_LATER;
        TreeEventManager manager = getEventManager ();
        if (manager != null) {
            policy = manager.getFirePolicy ();
            manager.setFirePolicy (TreeEventManager.FIRE_LATER);
        }
        
        // if the size is the same => order may be changed
        
        boolean reordered = true;
        int permutation[] = null;
        int originalIndex = 0;
        
        if (backupArray.length == peer.list.size ()) {
            permutation = new int[backupArray.length];
        } else {
            reordered = false;
        }
        
        // MERGE
        
        for (Iterator it = peer.list.iterator (); it.hasNext (); originalIndex++) {
            TreeObject peerNode = (TreeObject) it.next ();
            TreeObject suitableNode = null;
            
            // search backup array for suitable element and place it in suitableNode
            // then mark the backup array entry by null as used
            
            int suitableIndex = findMergeCandidate (peerNode, backupArray);
            if (suitableIndex >= 0) {
                suitableNode = backupArray[suitableIndex];
                backupArray[suitableIndex] = null;
            }
            
            // add to current list and let list's ContentManagers
            // update their structural references
            
            if (suitableNode != null) { // if exist merge it
                
                suitableNode.merge (peerNode);
                addNoFire (suitableNode);  // we were there before clear()
                if (permutation != null) permutation[originalIndex] = suitableIndex;
                
            } else {
                
                suitableNode = peerNode;
                add (suitableNode);
                reordered = false;
                
            }
            
        }
        
        // cache events to be fired
        
        if (reordered) {
            
            // check the permutation, ignore identical one
            
            reordered = false;
            
            for (int i = 0; i<permutation.length; i++) {
                if (permutation[i] != i) {
                    reordered = true;
                    break;
                }
            }
            if (reordered) firePropertyOrder (permutation);
            
        } else {
            
            // fire removal events for rest of backup array
            
            for (int i = 0; i<backupArray.length; i++) {
                if (backupArray[i] == null)
                    continue;
                contentManagerObjectRemoved (backupArray[i]);
                firePropertyRemove (backupArray[i]);
            }
        }
        
        if ( wasReadOnly ) {
            this.setReadOnly (true);
        }
        
        if (manager != null)
            manager.setFirePolicy (policy);
        
    }
    
    
    /**
     * Defines algorithm used for searching for must suitable merge peer.
     * @param original we search suitable candidate for this
     * @param candidates array of candidates and nulls
     * @return index of suitable candidate or -1
     */
    protected int findMergeCandidate (final TreeObject original, final TreeObject[] candidates) {
        
        // suitable if first member of the same class
        
        //        System.err.println("Looking for peer candidate:" + System.identityHashCode(original) + " " + original.getClass()); // NOI18N
        
        for (int i = 0; i<candidates.length; i++) {
            TreeObject candidate = candidates[i];
            if (candidate == null) continue;
            
            //            System.err.println("Inspecting: " + System.identityHashCode(candidate) + " " + candidate.getClass()); // NOI18N
            if (original.getClass ().equals (candidate.getClass ())) {
                return i;
            }
        }
        
        return -1;
    }
    
    
    
    //
    // read only
    //
    
    
    /**
     */
    protected void setReadOnly (boolean newReadOnly) {
        super.setReadOnly (newReadOnly);
        
        Iterator it = this.list.iterator ();
        while ( it.hasNext () ) {
            TreeObject obj = (TreeObject) it.next ();
            obj.setReadOnly (newReadOnly);
        }
    }
    
    
    //
    // context
    //
    
    /**
     */
    public final boolean isInContext () {
        return true; //???
    }
    
    /**
     */
    public final void removeFromContext () throws ReadOnlyException {
        if ( isInContext () ) {
            this.clear (); //???
        }
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final ContentManager getContentManager () {
        return contentManager;
    }
    
    //      /**
    //       */
    //      protected final void setContentManager (ContentManager newContentManager) {
    //  	this.contentManager = newContentManager;
    //      }
    
    
    /**
     */
    public final boolean isAssignableObject (Object obj) {
        try {
            getContentManager ().checkAssignableObject (obj);
            return true;
        } catch (ClassCastException exc) {
            return false;
        }
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void reorder (int[] perm) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if (equals (perm)) {
            return;
        }
        checkReadOnly ();
        checkReorder (perm);
        
        List newList = new LinkedList ();
        int len = size ();
        int[] newPerm = new int [len];
        for (int i = 0; i < len; i++) {
            newPerm[perm[i]] = i;
        }
        for (int i = 0; i < len; i++) {
            newList.add (list.get (newPerm[i]));
        }
        list = newList;
        
        contentManagerOrderChanged (perm);
        firePropertyOrder (perm);
    }
    
    /**
     */
    protected final void checkReorder (int[] perm) throws InvalidArgumentException {
        if (perm == null) {
            throw new InvalidArgumentException
            (Util.THIS.getString ("EXC_invalid_reorder_permutation"),
            new NullPointerException ());
        }
        if (perm.length != size ()) {
            throw new InvalidArgumentException
            (perm.length + " != " + size (), Util.THIS.getString ("EXC_invalid_reorder_permutation")); // NOI18N
        }
    }
    
    /** @return true if <code>perm</code> is identical permutaion. */
    protected final boolean equals (int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            if (perm[i] != i)
                return false;
        }
        return true;
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void switchObjects (int fromIndex, int toIndex) throws ReadOnlyException, InvalidArgumentException {
        int len = size ();
        int[] perm = new int [len];
        
        for (int i = 0; i < perm.length; i++) {
            perm[i] = i;
        }
        perm[fromIndex] = toIndex;
        perm[toIndex]   = fromIndex;
        
        reorder (perm);
    }
    
    /**
     */
    protected final void checkUnsupportedOperation () throws TreeUnsupportedOperationException {
        try {
            checkReadOnly ();
        } catch (TreeException exc) {
            throw new TreeUnsupportedOperationException (exc);
        }
    }
    
    
    //
    // java.util.List
    //
    
    /**
     */
    public final void clear () {
        checkUnsupportedOperation ();
        
        //          list.clear();
        Iterator it = (new LinkedList (list)).iterator (); // new LinkedList => fixed ConcurentModificationException
        while (it.hasNext ()) {
            Object obj = it.next ();
            remove (obj);
        }
    }
    
    /**
     */
    public final boolean removeAll (Collection collection) throws UnsupportedOperationException {
        checkUnsupportedOperation ();
        
        //  	return list.removeAll (collection);
        throw new UnsupportedOperationException ();
    }
    
    /**
     */
    public final Object get (int index) {
        return list.get (index);
    }
    
    /**
     */
    public final int hashCode () {
        return list.hashCode ();
    }
    
    /**
     */
    public final int size () {
        return list.size ();
    }
    
    /**
     */
    public final boolean retainAll (Collection collection) throws UnsupportedOperationException {
        checkUnsupportedOperation ();
        
        //  	return list.retainAll (collection);
        throw new UnsupportedOperationException ();
    }
    
    /**
     */
    protected boolean removeImpl (Object obj) {
        return list.remove (obj);
    }
    
    /**
     */
    public final boolean remove (Object obj) {
        checkUnsupportedOperation ();
        
        boolean removed = removeImpl (obj);
        if (removed) {
            contentManagerObjectRemoved ((TreeObject)obj);
            firePropertyRemove ((TreeObject)obj);
        }
        return removed;
    }
    
    /**
     */
    public final int indexOf (Object obj) {
        return list.indexOf (obj);
    }
    
    /**
     */
    public final boolean contains (Object obj) {
        return list.contains (obj);
    }
    
    /**
     */
    public final int lastIndexOf (Object obj) {
        return list.lastIndexOf (obj);
    }
    
    /**
     */
    protected Object setImpl (int index, Object obj) {
        return list.set (index, obj);
    }
    
    /**
     */
    public final Object set (int index, Object obj) {
        checkUnsupportedOperation ();
        contentManagerCheckAssignableObject (obj);
        
        Object oldObj = setImpl (index, obj);
        //          if (obj != oldObj) {
        contentManagerObjectRemoved ((TreeObject)oldObj);
        firePropertyRemove ((TreeObject)oldObj);
        //          }
        contentManagerObjectInserted ((TreeObject)obj);
        firePropertyInsert ((TreeObject)obj);
        
        return oldObj;
    }
    
    /**
     */
    public final ListIterator listIterator (int index) throws UnsupportedOperationException {
        //  	return list.listIterator (index);
        throw new UnsupportedOperationException ();
    }
    
    /**
     */
    public final boolean containsAll (Collection collection) {
        return list.containsAll (collection);
    }
    
    /**
     */
    public final Iterator iterator () {
        return (new LinkedList (list)).iterator (); //!!! TEMPORARY : fixed ConcurrentModificationException but iterator is not active
    }
    
    /**
     */
    public final boolean addAll (Collection collection) {
        checkUnsupportedOperation ();
        
        boolean changed = false;
        Iterator it = collection.iterator ();
        while ( it.hasNext () ) {
            this.add (it.next ());
            changed = true;
        }
        return changed;
    }
    
    /**
     */
    protected Object removeImpl (int index) {
        return list.remove (index);
    }
    
    /**
     */
    public final Object remove (int index) {
        checkUnsupportedOperation ();
        
        Object oldObj = removeImpl (index);
        if (oldObj != null) {
            contentManagerObjectRemoved ((TreeObject)oldObj);
            firePropertyRemove ((TreeObject)oldObj);
        }
        return (oldObj);
    }
    
    /**
     */
    public final boolean isEmpty () {
        return list.isEmpty ();
    }
    
    /**
     */
    protected void addImpl (int index, Object obj) {
        list.add (index, obj);
    }
    
    /**
     */
    public final void add (int index, Object obj) {
        checkUnsupportedOperation ();
        contentManagerCheckAssignableObject (obj);
        
        addImpl (index, obj);
        contentManagerObjectInserted ((TreeObject)obj);
        firePropertyInsert ((TreeObject)obj);
    }
    
    /**
     */
    public final boolean equals (Object obj) {
        if (!!! ( obj instanceof TreeObjectList ))
            return false;
        return list.equals (((TreeObjectList)obj).list);
    }
    
    /**
     */
    public final boolean addAll (int index, Collection collection) throws UnsupportedOperationException {
        checkUnsupportedOperation ();
        
        //  	return list.addAll (index, collection);
        throw new UnsupportedOperationException ();
    }
    
    /**
     */
    protected boolean addImpl (Object obj) {
        return list.add (obj);
    }
    
    /*
     * Add to list, Do NOT notify ContentManager and do not fire
     * the added object already was in the list.
     * Used during merge process.
     */
    private boolean addNoFire (Object obj) {
        checkUnsupportedOperation ();
        contentManagerCheckAssignableObject (obj);
        
        return addImpl (obj);
    }
    
    /**
     */
    public final boolean add (Object obj) {
        boolean added = addNoFire (obj);
        if ( added ) {
            contentManagerObjectInserted ((TreeObject)obj);
            firePropertyInsert ((TreeObject)obj);
        }
        return added;
    }
    
    /**
     */
    public final Object[] toArray (Object[] array) {
        return list.toArray (array);
    }
    
    /**
     */
    public final Object[] toArray () {
        return list.toArray ();
    }
    
    /**
     */
    public final ListIterator listIterator () throws UnsupportedOperationException {
        //  	return list.listIterator();
        throw new UnsupportedOperationException ();
    }
    
    /**
     */
    public final List subList (int fromIndex, int toIndex) throws UnsupportedOperationException {
        //  	return list.subList (fromIndex, toIndex);
        throw new UnsupportedOperationException ();
    }
    
    
    //
    // toString
    //
    
    /**
     */
    public String toString () {
        return list.toString ();
    }
    
    
    //
    // event model
    //
    
    /** Get assigned event manager which is assigned to ownerNode.
     * @return assigned event manager (may be null).
     */
    public final TreeEventManager getEventManager () {
        TreeNode ownerNode = contentManagerGetOwnerNode ();
        
        if (ownerNode == null)
            return null;
        
        return ownerNode.getEventManager ();
    }
    
    /**
     * @param listener The listener to add.
     */
    public final void addContentChangeListener (PropertyChangeListener listener) {
        getEventChangeSupport ().addPropertyChangeListener (PROP_CONTENT_INSERT, listener);
        getEventChangeSupport ().addPropertyChangeListener (PROP_CONTENT_REMOVE, listener);
        getEventChangeSupport ().addPropertyChangeListener (PROP_CONTENT_ORDER, listener);
    }
    
    /**
     * @param listener The listener to remove.
     */
    public final void removeContentChangeListener (PropertyChangeListener listener) {
        getEventChangeSupport ().removePropertyChangeListener (PROP_CONTENT_INSERT, listener);
        getEventChangeSupport ().removePropertyChangeListener (PROP_CONTENT_REMOVE, listener);
        getEventChangeSupport ().removePropertyChangeListener (PROP_CONTENT_ORDER, listener);
    }
    
    /**
     */
    public final boolean hasContentChangeListeners () {
        return getEventChangeSupport ().hasPropertyChangeListeners (PROP_CONTENT_INSERT)
        || getEventChangeSupport ().hasPropertyChangeListeners (PROP_CONTENT_REMOVE)
        || getEventChangeSupport ().hasPropertyChangeListeners (PROP_CONTENT_ORDER);
    }
    
    /**
     */
    public final void firePropertyInsert (TreeObject newNode) {
        firePropertyChange (getEventChangeSupport ().createEvent (PROP_CONTENT_INSERT, null, newNode));
    }
    
    /**
     */
    public final void firePropertyRemove (TreeObject oldNode) {
        firePropertyChange (getEventChangeSupport ().createEvent (PROP_CONTENT_REMOVE, oldNode, null));
    }
    
    /**
     */
    public final void firePropertyOrder (int [] permutation) {
        firePropertyChange (getEventChangeSupport ().createEvent (PROP_CONTENT_ORDER, null, permutation));
    }
    
    
    //
    // class ContentManager
    //
    
    
    /**
     */
    protected final TreeNode contentManagerGetOwnerNode () {
        if (contentManager != null) {
            return contentManager.getOwnerNode ();
        } else {
            return null;
        }
    }
    
    /**
     */
    protected final void contentManagerCheckAssignableObject (Object object) {
        if (contentManager != null) {
            contentManager.checkAssignableObject (object);
        }
    }
    
    /**
     */
    protected final void contentManagerObjectInserted (TreeObject treeObject) {
        if (contentManager != null) {
            contentManager.objectInserted (treeObject);
        }
    }
    
    /**
     */
    protected final void contentManagerObjectRemoved (TreeObject treeObject) {
        if (contentManager != null) {
            contentManager.objectRemoved (treeObject);
        }
    }
    
    /**
     */
    protected final void contentManagerOrderChanged (int[] permutation) {
        if (contentManager != null) {
            contentManager.orderChanged (permutation);
        }
    }
    
    
    /**
     *
     */
    public abstract static class ContentManager {
        
        /**
         * @return reference to the particular object list owner that created us
         */
        public abstract TreeNode getOwnerNode ();
        
        /** @throws ClassCastException
         */
        public void checkAssignableObject (Object obj) throws ClassCastException {
            if (!!! (obj instanceof TreeObject)) {
                String msg = Util.THIS.getString ("EXC_invalid_instance_of_TreeObject"); //,obj.getClass().getName());
                throw new ClassCastException (msg);
            }
        }
        
        /** @throws ClassCastException
         */
        protected final void checkAssignableClass (Class cls, Object obj) throws ClassCastException {
            if (!!! cls.isInstance (obj)) {
                String msg = Util.THIS.getString ("EXC_is_not_assignable_to", cls.getName ()); //,obj.getClass().getName(), cls.getName());
                throw new ClassCastException (msg);
            }
        }
        
        /**
         * Called AFTER object insertion.
         */
        public abstract void objectInserted (TreeObject obj);
        
        /**
         * Called AFTER object removal before firing.
         */
        public abstract void objectRemoved (TreeObject obj);
        
        /** */
        public abstract void orderChanged (int[] permutation);
        
    } // end: interface ContentManager
    
}
