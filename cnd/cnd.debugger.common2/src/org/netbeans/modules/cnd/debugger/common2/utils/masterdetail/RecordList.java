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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

/**
 * This interface defines the methods components for a picklist list (List).
 *
 * It seems to have evolved fron javax.swing.ListModel (combinded with
 * ListDataListener and ListDataEvent ... but it doesn't completely follow it.
 */

public interface RecordList<R extends Record> extends Iterable<R> {
    /**
     * Appends the specified component to the end of this list,
     * increasing its size by one (if not already in the list).
     * @param   record   the component to be added.
     */
    public void appendRecord(R record);


    /**
     * Adds the specified component to the beginning of this list, 
     * increasing its size by one (if not already in the list).
     * @param   record   the component to be added.
     */
    public void addRecord(R record);

    /**
     * Adds the specified component to the beginning of this list, 
     * increasing its size by one (if not already inlist and check is true).
     * @param   record   the component to be added.
     */
    public void addRecord(R record, boolean check);

    /**
     * Adds the specified component after 'after'.
     * If 'record' is already in the list this is effectively a record move.
     * @param   record   the component to be added.
     * @param   after   index to add after.
     */
    public void addRecordAfter(R record, int after);

    /**
     * Tests if the specified record is a component in this list.
     *
     * @param   record   an object.
     * @return  index of the specified record in the list if it exists
     * as determined by the <tt>equals</tt> method; <code>-1</code> otherwise.
     */
    public boolean contains(R record);

    public int indexOf(R record);

    public int recordByKey(String key);

    /**
     * Returns the value at the specified index.  
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public R getRecordAt(int index);
    
    public int getCurrentRecordIndex();

    public String[] getRecordsDisplayName();

    public String[] getRecordsName();

    /** 
     * Returns the max length of the list.
     * @return the max length of the list
     */
    public int getMaxSize();

    /** 
     * Returns the top-most record in the list if not empty, othervise null.
     * @return the top-most record in the list
     */
    public R getMostRecentUsedRecord();

    /** 
     * Returns the length of the list.
     * @return the length of the list
     */
    public int getSize();

    /**
     * Removes all records from this list and sets its size to zero.
     */
    public void removeAllRecords();

    /**
     * Removes all records except the archetypal one from this list and sets
     * its size to one.
     */
    public void removeAllButArchetype();

    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this list.
     *
     * @param   record   the record to be removed.
     * @return  the record removed if found, null othervise.
     */
    public R removeRecord(R record);

    /**
     * Removes the record at the specified index from the list. Each record in 
     * this list with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. The size of this list 
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the list. <p>
     *
     * @param      index   the index of the object to remove.
     * @return  the record removed if index valid, null othervise.
     */
    public R removeRecordAt(int index);

    /**
     * Replaces the record at the specified index with the one
     * specified.
     *
     * @param      index   the index of the object to remove.
     * @return  the record replaced if index valid, null othervise.
     */
    public R replaceRecordAt(R record, int index);

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     * @param l the <code>RecordListListener</code> to be added
     */  
    public void addRecordListListener(RecordListListener l);

    /**
     * Removes a listener from the list that's notified each time a 
     * change to the data model occurs.
     * @param l the <code>DataListener</code> to be removed
     */  
    public void removeRecordListListener(RecordListListener l);

    /**
     * Fire a RecordListEvent signifying that the contents of Record at 'i'.
     * hanged.
     * @param      i   The index of the object which changed.
     */
    public void fireRecordChanged(int i);

    /**
     * Clones a picklist
     */
    public RecordList cloneList();

    /**
     * First removes all records, then copies all records from 'picklist'.
     */
    public void copyList(RecordList<R> picklist, Object obj);

    /**
     * Dumps all list records to std err (for debugging...)
     */
    public void dumpRecords();

    /**
     * Return a new unique (or not) key to be used when records are duplicated.
     * May return null in which case the duplicated Records key is unchanged.
     */
    public String newKey();
}
