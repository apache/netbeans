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

package org.netbeans.modules.editor.completion;

import java.util.BitSet;
import javax.swing.*;
import javax.swing.event.*;

/** Model that can compute its values lazily and moreover handle some
 * kind of filtering.
 *
 * Made public just to test an impl of Children.EntrySource based on this
 * filtering list.
*/
public final class LazyListModel extends Object 
implements ListModel, Runnable, javax.swing.event.ListDataListener {
    /** means that the value has not yet been assigned */
    private static int NOT_TESTED = Short.MIN_VALUE - 1;
    private static int EMPTY_VALUE = Short.MIN_VALUE - 2;
    /** skips extensive asserts - needed for performance tests */
    private static final boolean skipExpensiveAsserts = Boolean.getBoolean ("org.openide.explorer.view.LazyListModel.skipExpensiveAsserts"); // NOI18N
    
    
    private boolean log;
    private ListModel listModel;
    private Filter filter;
    /** the value to return when nothing else can be returned */
    private Object defaultValue;
    /** simple event listener list */
    private javax.swing.event.EventListenerList list = new javax.swing.event.EventListenerList ();

    /** the size of the original list we now know it has */
    private int originalSize;
    /** the size we currently pretend to have */
    private int size;
    /** maps an external index to our internal one 
     * (NOT_TESTED, means it has not been tested yet, EMPTY_VALUE means for this external index
     * we have returned default value)
     */
    private int[] external;
    /** set with marked values where external is different than EMPTY_VALUE or NOT_TESTED */
    private BitSet checked;
    /** counts number of refused elements */
    private int refused;
    /** contains 1 if the bit mask if the index in listModel has already been tested */
    private BitSet tested;
    /** dirty means that we should really update assumptions */
    private boolean markDirty;
    
    private LazyListModel (ListModel m, Filter f, double expectedRadio, Object defaultValue) {
        this.listModel = m;
        this.filter = f;
        this.defaultValue = defaultValue;
        
        // JST-PENDING: Weak or not?
        m.addListDataListener (this);
    }
    
    final Filter getFilter () {
        return filter;
    }
    
    final boolean isComputed (int index) {
        return tested != null && tested.get (index);
    }
    
    /** Makes itself dirty and schedules an update.
     */
    private void markDirty () {
        this.markDirty = true;
        getFilter ().scheduleUpdate (this);
    }
    
    /** When executed, updateYourAssumeptions.
     */
    public void run () {
        if (!markDirty) {
            return;
        }

        markDirty = false;
        if (log) {
            System.err.println("updateYourAssumeptions ();"); // NOI18N
        }
        updateYourAssumeptions ();
    }
    
    /** Notifies removal of inteval from (inclusive) to (exclusive) and
     * updates its structures.
     * 
     * !!! as a side effect updates size !!!
     *
     * @return s - number of removals
     */
    private void notifyRemoval (int from, int to) {
        ListDataEvent ev = new ListDataEvent (
            this, ListDataEvent.INTERVAL_REMOVED, from, to - 1
        );
        removeInterval (external, from, to);
        int cnt = to - from;
        size -= cnt;
        
        regenerateCheckedBitSet ();
        fireChange (ev);
    }
    
    private void regenerateCheckedBitSet () {
        checked = new BitSet (size);
        for (int i = 0; i < size; i++) {
            if (external[i] >= 0) {
                checked.set (i);
            }
        }
    }
    
    private int getExternal (int index) {
        if (index == size) {
            return originalSize;
        }
        if (index < 0) {
            return -1;
        }
        return external[index];
    }
    
    /** Can be called to ask the LazyListModel to update its assumptions, 
     * especially assumptions about the size to match its current knowledge.
     */
    final void updateYourAssumeptions () {
        if (external == null) {
            return;
        }
        
        int sizeBefore = size;
        int notifiedRemovals = 0;
        
        int i = 0;
        LOOP: while (i < size) {
            while (getExternal (i) >= 0 && i < size) {
                i++;
            }
            if (i == size) {
                break;
            }
            
            if (getExternal (i) == NOT_TESTED) {
                int minusOneIndex = i - 1;
                while (i < size && getExternal (i) == NOT_TESTED) { 
                    i++;
                }
                
                int count = i - minusOneIndex - 1;
                int from = getExternal (minusOneIndex) + 1;
                int to = getExternal (i);
                
                assert from >= 0 : "Value at " + minusOneIndex + "(" + from + ") must be greater than minus one"; // NOI18N
                assert to >= 0 : "Value at " + i + "must be greater than minus one but was: " + to; // NOI18N
                assert to >= from : "Must be true: " + to + " >= " + from; // NOI18N

                int howMuch = count - (to - from);
                if (howMuch > 0) {
                    // we need to notify some kind of removal
                    notifyRemoval (i - howMuch, i);
                    i -= howMuch;
                }
            } else {
                int minusTwoIndex = i;
                while (i < size && getExternal (i) == EMPTY_VALUE) {
                    i++;
                }
                notifyRemoval (minusTwoIndex, i);
                i = minusTwoIndex;
            }
        }
        
        assert externalContraints () : "Constraints failed"; // NOI18N
    }
    
    private boolean externalContraints () {
        assert external != null : "Not null"; // NOI18N
        assert external.length >= size : "Length " + external.length + " >= " + size; // NOI18N
        if (!skipExpensiveAsserts) {
            for (int i = 1; i < size; i++) {
                assert external[i - 1] != NOT_TESTED || external[i] != EMPTY_VALUE : "There cannot be empty value after not tested value"; // NOI18N
                assert external[i - 1] != EMPTY_VALUE || external[i] != NOT_TESTED : "Not tested cannot immediatelly follow empty value"; // NOI18N
                assert external[i] < 0 || external[i] > external[i - 1] : "If valid index it has to be greater: " + i; // NOI18N
                assert external[i] < 0 == !checked.get (i) : "external and checked must be consistent: " + i; // NOI18N
            }
        }
        return true;
    }
    
    /** Removes an interval from array */
    private static void removeInterval (int[] array, int index0, int index1) {
        assert index0 < index1 : "Index1 must be bigger than index0: " + index1 + " > " + index0; // NOI18N
        System.arraycopy (array, index1, array, index0, array.length - index1);
    }
    
    /** Factory method to create new filtering lazy model.
     */
    public static LazyListModel create (ListModel listModel, Filter f, double expectedRadio, Object defValue) {
        return create (listModel, f, expectedRadio, defValue, false);
    }
    
    /** Model with enabled logging.
     */
    static LazyListModel create (ListModel listModel, Filter f, double expectedRadio, Object defValue, boolean log) {
        LazyListModel lazy = new LazyListModel (listModel, f, expectedRadio, defValue);
        lazy.log = log;
        return lazy;
    }
    
    //
    // Model methods.
    //

    public void addListDataListener(ListDataListener l) {
        list.add (ListDataListener.class, l);
    }
    
    public void removeListDataListener(ListDataListener l) {
        list.remove (ListDataListener.class, l);
    }
    
    private void fireChange (ListDataEvent ev) {
        if (list.getListenerCount () == 0) return ;
        
        Object[] arr = list.getListenerList ();
        for (int i = arr.length - 1; i >= 0; i -= 2) {
            ListDataListener l = (ListDataListener)arr[i];
            switch (ev.getType ()) {
                case ListDataEvent.CONTENTS_CHANGED: l.contentsChanged (ev); break;
                case ListDataEvent.INTERVAL_ADDED: l.intervalAdded (ev); break;
                case ListDataEvent.INTERVAL_REMOVED: l.intervalRemoved (ev); break;
                default:
                    throw new IllegalArgumentException  ("Unknown type: " + ev.getType ());
            }
        }
    }
    
    /** Is this index accepted.
     */
    private boolean accepted (int indx, Object[] result) {
        Object v = listModel.getElementAt (indx);
        tested.set (indx);
        if (filter.accept (v)) {
            result[0] = v;
            return true;
        }
        
        markDirty ();
        return false;
    }
    
    /** Initialize the bitsets to sizes of the listModel.
     */
    private void initialize () {
        assert SwingUtilities.isEventDispatchThread() : "ListModel methods should be called form AWT event thread"; //NOI18N
        if (tested == null) {
            originalSize = listModel.getSize ();
            size = listModel.getSize ();
            tested = new BitSet (size);
            external = new int[size];
            for (int i = 0; i < size; i++) {
                external[i] = NOT_TESTED;
            }
            checked = new BitSet (size);
            assert externalContraints () : "Constraints failed"; // NOI18N
        }
    }
    
    /** this variable is used from tests to prevent creation of elements in
     * certain cases.
     */
    static Boolean CREATE;
    /** If value is not know for given index and CREATE.get() is Boolean.FALSE it returns defaultValue.
     */
    public Object getElementAt(int index) {
        initialize ();
        
        if (log) {
            System.err.println("model.getElementAt (" + index + ");"); // NOI18N
        }
        
        if (external[index] >= 0) {
            // we have computed the index, so return it
            return listModel.getElementAt (external[index]);
        }
        
        if (external[index] == EMPTY_VALUE) {
            // default value needs to be used
            return defaultValue;
        }
        
        if (CREATE != null && !CREATE.booleanValue()) {
            assert Thread.holdsLock(CREATE) : "Only one thread (from tests) can access this"; // NOI18N
            return defaultValue;
        }
        
        
        // JST: Why there is no BitSet.previousSetBit!!!???
        int minIndex = index;
        while (minIndex >= 0 && getExternal (minIndex) < 0) {
            minIndex--;
        }
        int maxIndex;
        if (checked.get (index)) {
            maxIndex = index; 
        } else {
            maxIndex = checked.nextSetBit (index);
            if (maxIndex == -1 || maxIndex > size) {
                maxIndex = size;
            }
        }

        int myMinIndex = getExternal (minIndex) + 1; // one after the index of the first non-1 index
        int myMaxIndex = getExternal (maxIndex);
        
        assert myMaxIndex >= myMaxIndex : "Must be greater"; // NOI18N        
        if (myMaxIndex != myMinIndex) {
            int myIndex = myMinIndex + (index - minIndex) - 1;
            if (myIndex >= myMaxIndex) {
                myIndex = myMaxIndex - 1;
            }

            Object[] result = new Object[1];
            if (accepted (myIndex, result)) {
                assert external[index] == NOT_TESTED : "External index " + index + " still needs to be unset: " + external[index];
                external[index] = myIndex;
                checked.set (index);
                return result[0];
            }

            boolean checkBefore = true;
            boolean checkAfter = true;
            for (int i = 1; checkAfter || checkBefore; i++) {
                if (checkBefore) {
                    checkBefore = index - i >= minIndex && myIndex - i >= myMinIndex && getExternal (index - i) == NOT_TESTED;
                    if (checkBefore && accepted (myIndex - i, result)) {
                        external[index] = myIndex - i;
                        checked.set (index);
                        return result[0];
                    }
                }
                if (checkAfter) {
                    checkAfter = index + i < maxIndex && myIndex + i < myMaxIndex && getExternal (index + i) == NOT_TESTED;
                    if (checkAfter && accepted (myIndex + i, result)) {
                        external[index] = myIndex + i;
                        checked.set (index);
                        return result[0];
                    }
                }
            }
        }
        
        markDirty ();

        // set default value for all objects in the interval
        for (int i = minIndex + 1; i < maxIndex; i++) {
            assert external[i] == NOT_TESTED : i + " should not be set: " + external[i]; // NOI18N
            external[i] = EMPTY_VALUE;
        }
        checked.clear (minIndex + 1, maxIndex);
        
        assert external[index] == EMPTY_VALUE : "Should be asigned in the cycle above"; // NOI18N
        return defaultValue;
    }

    public int getSize() {
        initialize ();
        return size;
    }
    
    //
    // Notifications from the underlaying model
    //
    
    /** Inserts specified amount of bits into the set.
     */
    private static BitSet insertAt (BitSet b, int at, int len, int size) {
        BitSet before = b.get (0, at);
        
        BitSet res = new BitSet (size);
        res.or (before);
        
        int max = b.length ();
        while (at < max) {
            res.set (at + len, b.get (at));
            at++;
        }
        return res;
    }
    /** Removes specified amount of bits from the set.
     */
    private static BitSet removeAt (BitSet b, int at, int len, int newSize) {
        BitSet clone = (BitSet)b.clone ();
        
        int max = b.length ();
        while (at < max) {
            clone.set (at, b.get (at + len));
            at++;
        }
        clone.set (newSize, b.size (), false);
        return clone;
    }

    public void contentsChanged (ListDataEvent listDataEvent) {
        throw new java.lang.UnsupportedOperationException ("Not yet implemented");
    }

    public void intervalAdded (ListDataEvent listDataEvent) {
        if (external == null) {
            return;
        }
        
        updateYourAssumeptions ();
        
        int first = listDataEvent.getIndex0 ();
        int end = listDataEvent.getIndex1 () + 1; 
        int len = end - first;
        
        int newOriginalSize = originalSize + len;
        int newSize = size + len;
        
        tested = insertAt (tested, first, len, newOriginalSize);
        
        int insert = findExternalIndex (first);
        int[] newExternal = new int[newSize];
        System.arraycopy (external, 0, newExternal, 0, insert);
        for (int i = 0; i < len; i++) {
            newExternal[insert + i] = NOT_TESTED;
        }
        for (int i = insert + len; i < newExternal.length; i++) {
            int v = external[i - len];
            newExternal[i] = v < 0 ? v : v + len;
        }
        external = newExternal;
        size = newSize;
        originalSize = newOriginalSize;

        regenerateCheckedBitSet ();

        fireChange (new ListDataEvent (this, ListDataEvent.INTERVAL_ADDED, insert, insert + len - 1));
        assert externalContraints () : "Constraints failed"; // NOI18N
    }
    
    /** Finds the appropriate index of given internal index. The state is 
     * supposed to be after updateYourAssumeptions => no EMPTY_VALUE
     */
    public int findExternalIndex (int myIndex) {
        initialize();
        int outIndex = 0;
        for (int i = -1; i < size; i++) {
            if (getExternal (i) == NOT_TESTED) {
                outIndex++;
            } else {
                outIndex = getExternal (i);
            }
            
            if (outIndex >= myIndex) {
                return i;
            }
        }
        return size;
    }

    public void intervalRemoved (ListDataEvent listDataEvent) {
        if (external == null) {
            return;
        }
        
        updateYourAssumeptions ();
        
        int first = listDataEvent.getIndex0 ();
        int end = listDataEvent.getIndex1 () + 1; 
        int len = end - first;

        int newOriginalSize = originalSize - len;
        
        int f = findExternalIndex (first);
        int e = findExternalIndex (end);
        
        assert f >= 0 : "First index must be above zero: " + f; // NOI18N
        assert e >= f : "End index must be above first: " + f + " <= " + e; // NOI18N
        
        int outLen = e - f;
        
        int[] newExternal = (int[])external.clone ();
        for (int i = e; i < size; i++) {
            int v = external[i];
            newExternal[i - outLen] = v < 0 ? v : v - len;
            checked.set (i - outLen, v >= 0);
        }
        external = newExternal;
        size -= outLen;
        originalSize = newOriginalSize;
        
        if (outLen != 0) {
            fireChange (new ListDataEvent (this, ListDataEvent.INTERVAL_REMOVED, f, e - 1));
        }
        assert externalContraints () : "Constraints failed"; // NOI18N
   }

    
    /** Interface for those that wish to filter content of the list.
     * This filter is expected to always return the same result for
     * the same object - e.g. either always exclude or include it.
     */
    public interface Filter {
        public boolean accept (Object obj);
        /** This method is called when the list needs update. It's goal is
         * usually to do SwingUtilities.invokeLater, even more rafined 
         * methods are allowed.
         */
        public void scheduleUpdate (Runnable run);
    }
}
