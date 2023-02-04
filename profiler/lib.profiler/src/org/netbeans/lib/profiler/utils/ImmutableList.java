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

package org.netbeans.lib.profiler.utils;

import java.util.Iterator;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ImmutableList implements Iterable {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class InnerIterator implements Iterator {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private int counter;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public InnerIterator() {
            counter = 0;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public boolean hasNext() {
            return counter < size;
        }

        public Object next() {
            return get(counter++);
        }

        public void remove() {
            // do nothing
        }
    }

    private static final class LoadFactor {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        public static final float DEFAULT_FACTOR = 0.01f;

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        public final float factor;
        public final int upperLimit;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public LoadFactor(final int upperLimit, final float loadFactor) {
            this.upperLimit = upperLimit;
            this.factor = loadFactor;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final LoadFactor[] distributionMap = new LoadFactor[] {
                                                            new LoadFactor(2, 3f), new LoadFactor(6, 0.3f),
                                                            new LoadFactor(10, 0.5f), new LoadFactor(50, 0.1f)
                                                        };

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Object slotsGuard = new Object();

    // @GuardedBy slotsGuard
    private int[] slotLimits;

    // @GuardedBy slotsGuard
    private Object[] storageSlots;
    private float loadFactor;

    // @GuardedBy slotsGuard
    private int availableSize;

    // @GuardedBy slotsGuard
    private int currentIndex;

    // @GuardedBy slotsGuard
    private int currentSlot;
    private int initialSize;
    private int size;

    // @GuardedBy slotsGuard
    private int slotCount;
    private int slotInitialSize;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of DynamicList */
    public ImmutableList() {
        initialSize = 1;
        slotInitialSize = 1;
        loadFactor = 1.75f;

        reset();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void add(Object item) {
        synchronized (slotsGuard) {
            accomodate(++size);
            ((Object[]) storageSlots[currentSlot])[currentIndex] = item;
        }
    }

    public void clear() {
        reset();
    }

    public Object get(int index) {
        synchronized (slotsGuard) {
            Object[] slot = null;
            int lowerLimit = 0;

            for (int i = 0; i < slotCount; i++) {
                if (slotLimits[i] > index) {
                    slot = (Object[]) storageSlots[i];

                    break;
                }

                lowerLimit = slotLimits[i];
            }

            if (slot == null) {
                return null;
            }

            return ((Object[]) slot)[index - lowerLimit];
        }
    }

    public Object get(final Object template) {
        for (Iterator iter = iterator(); iter.hasNext();) {
            Object obj = iter.next();

            if (obj.equals(template)) {
                return obj;
            }
        }

        return null;
    }

    public Iterator iterator() {
        return new InnerIterator();
    }

    public static void main(String[] args) {
        ImmutableList list = new ImmutableList();

        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }

        System.out.println("ready"); // NOI18N

        for (int i = 0; i < 10000; i++) {
            System.out.println(i + " = " + list.get(i)); // NOI18N
        }

        list.clear();

        for (int i = 0; i < 100000; i++) {
            list.add(i);
        }

        System.out.println("ready"); // NOI18N

        for (int i = 0; i < 100000; i++) {
            System.out.println(i + " = " + list.get(i)); // NOI18N
        }
    }

    public int size() {
        return size;
    }

    private void accomodate(int newSize) {
        synchronized (slotsGuard) {
            if (slotCount == 0) {
                slotLimits = new int[initialSize];
                storageSlots = new Object[initialSize];
                storageSlots[0] = new Object[slotInitialSize];
                availableSize = slotLimits[0] = slotInitialSize;
                slotCount = 1;
                currentSlot = 0;
                currentIndex = 0;

                return;
            }

            if (newSize > availableSize) {
                int newSlotSize = (int) (((float) availableSize * findLoadFactor(availableSize)) + 0.5f);
                newSlotSize = (newSlotSize > 0) ? newSize : 1;

                Object[] newSlot = new Object[newSlotSize];
                availableSize += newSlotSize;

                if (slotCount == storageSlots.length) { // all slots taken

                    int newSlotsSize = (int) (((float) slotCount * loadFactor) + 0.5f);
                    Object[] newStorageSlots = new Object[newSlotsSize];
                    int[] newSlotLimits = new int[newSlotsSize];
                    System.arraycopy(storageSlots, 0, newStorageSlots, 0, storageSlots.length);
                    System.arraycopy(slotLimits, 0, newSlotLimits, 0, slotLimits.length);
                    storageSlots = newStorageSlots;
                    slotLimits = newSlotLimits;
                }

                currentSlot = slotCount;
                currentIndex = 0;
                storageSlots[slotCount] = newSlot;
                slotLimits[slotCount] = availableSize;

                slotCount++;

                return;
            }

            if (slotCount > 1) {
                currentIndex = newSize - slotLimits[slotCount - 2] - 1; // use the slot size of the previously filled-up slot
            } else {
                currentIndex = newSize - 1;
            }
        }
    }

    private float findLoadFactor(int origSize) {
        for (int i = 0; i < distributionMap.length; i++) {
            if (distributionMap[i].upperLimit > origSize) {
                return distributionMap[i].factor;
            }
        }

        return LoadFactor.DEFAULT_FACTOR;
    }

    private void reset() {
        synchronized (slotsGuard) {
            storageSlots = null;
            slotLimits = null;

            slotCount = 0;
            size = 0;
        }
    }
}
