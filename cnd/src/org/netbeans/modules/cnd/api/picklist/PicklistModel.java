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

package org.netbeans.modules.cnd.api.picklist;

/**
 * This interface defines the methods components for a picklist list (Picklist).
 */
public interface PicklistModel {

    /**
     * Adds the specified component to the beginning of this list,
     * increasing its size by one (if not already in the list).
     * @param   elem   the component to be added.
     */
    public void addElement(PicklistElement elem);

    /**
     * Adds the specified component to the beginning of this list, 
     * increasing its size by one (if not already inlist and check is true).
     * @param   elem   the component to be added.
     */
    public void addElement(PicklistElement elem, boolean check);

    /**
     * Tests if the specified element is a component in this list.
     *
     * @param   elem   an object.
     * @return  index of the specified element in the list if it exists
     * as determined by the <tt>equals</tt> method; <code>-1</code> otherwise.
     */
    public int contains(PicklistElement elem);

    /**
     * Returns the value at the specified index.  
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public PicklistElement getElementAt(int index);

    /** 
     * Returns all the elements in the list (possibly size 0 array)
     * @return all the elements in the list (possibly size 0 array).
     */
    public PicklistElement[] getElements();
    
    public String[] getElementsDisplayName();

    /** 
     * Returns the max length of the list.
     * @return the max length of the list
     */
    public int getMaxSize();

    /** 
     * Returns the top-most element in the list if not empty, othervise null.
     * @return the top-most element in the list
     */
    public PicklistElement getMostRecentUsedElement();

    /** 
     * Returns the length of the list.
     * @return the length of the list
     */
    public int getSize();

    /**
     * Removes all elements from this list and sets its size to zero.
     */
    public void removeAllElements();

    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this list.
     *
     * @param   elem   the element to be removed.
     * @return  the element removed if found, null otherwise.
     */
    public PicklistElement removeElement(PicklistElement elem);

    /**
     * Removes the element at the specified index from the list. Each element in 
     * this list with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. The size of this list 
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the list. <p>
     *
     * @param      index   the index of the object to remove.
     * @return  the element removed if index valid, null otherwise.
     */
    public PicklistElement removeElementAt(int index);

    /**
     * Replaces the element at the specified index with the one
     * specified.
     *
     * @param      index   the index of the object to remove.
     * @return  the element replaced if index valid, null otherwise.
     */
    public PicklistElement replaceElementAt(PicklistElement elem, int index);

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     * @param l the <code>PicklistDataListener</code> to be added
     */  
    public void addPicklistDataListener(PicklistDataListener l);

    /**
     * Removes a listener from the list that's notified each time a 
     * change to the data model occurs.
     * @param l the <code>PicklistDataListener</code> to be removed
     */  
    public void removePicklistDataListener(PicklistDataListener l);

    /**
     * Clones a picklist
     */
    public PicklistModel clonePicklist();

    /**
     * First removes all elements, then copies all elements from 'picklist'.
     */
    public void copyPicklist(PicklistModel picklist);

    /**
     * Dumps all list elements to std err (for debugging...)
     */
    public void dumpElements();
}
