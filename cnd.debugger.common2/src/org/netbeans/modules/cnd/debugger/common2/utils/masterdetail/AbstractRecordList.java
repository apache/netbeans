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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.EventListenerList;

/**
 * Basic implementation of RecordList.
 */
public abstract class AbstractRecordList<R extends Record>
    implements RecordList<R> {

    /** Default max size of List */
    private int maxSize = 10;

    /** pointer to last accessed Record */
    private int currentRecordIndex;

    /** The list itself */
    private ArrayList<R> list = new ArrayList<R>();
//    private static boolean dirty = false;

    /** Holds list of event listeners */
    protected EventListenerList listenerList = null;

    /**
     * Creates a new variable sized List
     */
    public AbstractRecordList() {
	initialize(0);
    }

    /**
     * Creates a new List with maximux 'max' records. 'max' <= 0 means variable sized list.
     */
    public AbstractRecordList(int max) {
	initialize(max);
    }

    /**
     * Cloning constructor
     */
    protected AbstractRecordList(int max, RecordList<R> that) {
	initialize(max);
	for (R r : that) {
	    @SuppressWarnings("unchecked")
	    R clone = (R) r.cloneRecord();
	    appendRecord(clone);
        }
    }


    private EventListenerList getListenerList() {
	if (listenerList == null) {
	    listenerList = new EventListenerList();
	}
	return listenerList;
    }


    /**
     * Initializing data
     */
    private void initialize(int max) {
	if (max <= 0) {
	    maxSize = 0; // Open-ended ...
	}
	else {
	    maxSize = max; // Fixed sized
	}
    }

    /**
     * Appends the specified component to the end of this list,
     * increasing its size by one.
     * @param   record   the component to be added.
     */
    @Override
    public void appendRecord(R record) {
	list.add(record);
//        dirty = true;
    }

    /**
     * Adds the specified component to the beginning of this list, 
     * increasing its size by one.
     * @param   record   the component to be added.
     */
    @Override
    public void addRecord(R record) {
	addRecord(record, true);
    }


    /**
     * Adds the specified component to the beginning of this list, 
     * increasing its size by one.
     * @param   record   the component to be added.
     */
    @Override
    public synchronized void addRecord(R record, boolean reuse) {
	// Is this the only method that needs to synchronized??? FIXUP

	// first check if record already in the list
	int existing = indexOf(record);

	if (reuse && existing > 0) {
	    // effectively move it to the front of the list
	    list.remove(existing);
	}

	list.add(0, record);

	// if fixed-sizes list, check size and possily delete last record
	if (maxSize > 0 && list.size() > maxSize)
	    list.remove(list.size() - 1);

	// notify listener of a change
	fireContentsChanged(this, null);
    }

    /**
     * Move the Record at 'rx' to the front of the list.
     */
    public void moveToFront(int rx) {
	R record = list.remove(rx);
	list.add(0, record);

	// notify listener of a change
	fireContentsChanged(this, null);
    }

    @Override
    public void addRecordAfter(R record, int after) {

	// Protect against bad indices
	if (after < 0)
	    after = 0;
	if (after >= list.size())
	    after = list.size() - 1;

	int existing = indexOf(record);
	assert existing < 0 :
       	       "AbstractRecordList.addRecordAfter() record already in list"; // NOI18N

	if (list.size() == 0) {
	    list.add(0, record);
	} else {
	    list.add(after+1, record);
	}

	// notify listener of a change
	fireContentsChanged(this, null);
    }

    /**
     * Tests if the specified record is a component in this list.
     *
     * @param   record   an object.
     * @return  true if it exists as determined by the <tt>equals</tt> method;
     *          <code>false</code> otherwise.
     */
    @Override
    public boolean contains(R record) {
	return list.contains(record);
    }

    @Override
    public int indexOf(R record) {
	return list.indexOf(record);
    }

    /*
     * Return the index of the record whose key matches 'key' or -1 if
     * no match was found.
     */
    @Override
    public int recordByKey(String key) {
	int i = 0;
	for (R r : this) {
	    if (r.matches(key))
		return i;
	    i++;
	}
	return -1;
    }

    /**
     * Returns the index of current Record
     * @return the index of <code>R</code>
     */
    @Override
    public int getCurrentRecordIndex() {
        return currentRecordIndex;
    }

    /**
     * Returns the value at the specified index.  
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    @Override
    public R getRecordAt(int index) {
	try {
	    currentRecordIndex = index;
	    return list.get(index);
	} catch (IndexOutOfBoundsException x) {
	    return null;
	}
    }

    @Override
    public Iterator<R> iterator() {
	return list.iterator();
    }

    @Override
    public String[] getRecordsName() {
        String[] recordsName = new String[getSize()];
        int i = 0;
        for (R r : this)
            recordsName[i++] = r.getHostName();
        return recordsName;
    }

    @Override
    public String[] getRecordsDisplayName() {
        String[] recordsDisplayName = new String[getSize()];
	int i = 0;
	for (R r : this)
	    recordsDisplayName[i++] = r.displayName();
	return recordsDisplayName;
    }

    /** 
     * Returns the max length of the list, 0 being variable sized.
     * @return the max length of the list, 0 being variable sized
     */
    @Override
    public int getMaxSize() {
	return maxSize;
    }

    /** 
     * Returns the top-most record in the list if not empty, othervise null.
     * @return the top-most record in the list
     */
    @Override
    public R getMostRecentUsedRecord() {
	if (getSize() > 0)
	    return list.get(0);
	else
	    return null;
    }

    /** 
     * Returns the length of the list.
     * @return the length of the list
     */
    @Override
    public int getSize() {
	return list.size();
    }

    /**
     * Removes all records from this list and sets its size to zero.
     */
    @Override
    public void removeAllRecords() {
	list.clear();
	
	// notify listener of a change
	fireContentsChanged(this, null);
    }

    private R findArchetype() {

	// This is best-case for lists with archetypes because the archetype
	// is contrained to be the first element
	// It is worst-case for lists w/o archetypes.

	for (R r : this) {
	    if (r.isArchetype())
		return r;
	}
	return null;
    }

    @Override
    public void removeAllButArchetype() {
	R archetype = findArchetype();
	list.clear();
	if (archetype != null)
	    list.add(archetype);
	
	// notify listener of a change
	fireContentsChanged(this, null);
    }

    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this list.
     *
     * @param   record   the record to be removed.
     * @return  the record removed if found, null othervise.
     */
    @Override
    public R removeRecord(R record) {
	return removeRecordAt(indexOf(record));
    }

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
    @Override
    public R removeRecordAt(int index) {
	R toBeRemoved = null;
	if (index < 0 || index >= getSize()) {
	    return toBeRemoved;
	}
	toBeRemoved = list.remove(index);
	
	// notify listener of a change
	fireContentsChanged(this, null);

	return toBeRemoved;
    }


    /**
     * Replaces the record at the specified index with the one
     * specified.
     *
     * @param      index   the index of the object to remove.
     * @return  the record replaced if index valid, null othervise.
     */
    @Override
    public R replaceRecordAt(R record, int index) {
	if (index < 0 || index >= getSize()) {
	    return null;
	}
	R toBeReplaced = list.set(index, record);

	// notify listener of a change
	fireContentsChanged(this, null);

	return toBeReplaced;
    }

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     * @param l the <code>RecordListListener</code> to be added
     */  
    @Override
    public void addRecordListListener(RecordListListener l) {
	getListenerList().add(RecordListListener.class, l);
    }

    /**
     * Removes a listener from the list that's notified each time a 
     * change to the data model occurs.
     * @param l the <code>RecordListListener</code> to be removed
     */  
    @Override
    public void removeRecordListListener(RecordListListener l) {
	getListenerList().remove(RecordListListener.class, l);
    }

    /**
     * Notify that the contents of record 'i' changed.
     * For now overkills by claiming the whole RecordList changed.
     */
    @Override
    public void fireRecordChanged(int i) {
	fireContentsChanged(this, null);
    }

    /**
     * Clones a list deeply.
     *
     * Implementation should use a cloning constructor which should in turn
     * use our cloning constructor:
     * protected AbstractRecordList(int max, RecordList<R> that)
     */
    @Override
    public abstract RecordList<R> cloneList();


    /**
     * Shallow copy all records from 'that' to 'this'. (aka assign())
     */
    @Override
    public void copyList(RecordList<R> that, Object obj) {
	list.clear();
	// list.addAll(that); 'that' needs to be a Collection
	for (R r : that)
	    list.add(r);
	fireContentsChanged(this, obj);
    }

    /**
     * Dumps all list records to std out (for debugging...)
     */
    @Override
    public void dumpRecords() {
	for (int i = 0; i < getSize(); i++)
	    System.out.println("[" + i + "] " + list.get(i).displayName()); // NOI18N
	System.out.println();
    }

    /*
     * Default implementation.
     * Can be overriden in subclass.
     */
    @Override
    public String newKey() {
	return null;
    }


    protected void fireContentsChanged(Object source, Object obj) {
	Object[] listeners = getListenerList().getListenerList();
	RecordListEvent e = null;

	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == RecordListListener.class) {
		if (e == null) {
		    e = new RecordListEvent(source,
					    RecordListEvent.CONTENTS_CHANGED, 
					    (String) obj);
		}
		((RecordListListener)listeners[i+1]).contentsChanged(e);
	    }	       
	}
    }

//    protected static boolean isDirty() {
//        if (dirty)
//            return true;
//        return false;
//    }
//
//    protected static void clearDirty() {
//        dirty = false;
//    }
}
