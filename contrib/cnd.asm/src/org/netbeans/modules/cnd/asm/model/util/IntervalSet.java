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



package org.netbeans.modules.cnd.asm.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;

public class IntervalSet<E extends AsmOffsetable> implements Iterable<E> {
       
    private List<E> intervals;
    
    public IntervalSet() {
        intervals = new ArrayList<E>();        
    }
    
    public IntervalSet(int cap) {
        intervals = new ArrayList<E>(cap);        
    }
    
    public boolean isEmpty() {
        return intervals.isEmpty();
    }
    
    public int getLowerBound() {
        if (isEmpty())
            throw new IndexOutOfBoundsException("Empty"); // NOI18N
        
        return intervals.get(0).getStartOffset();
    }
    
    public int getUpperBound() {
        if (isEmpty())
            throw new IndexOutOfBoundsException("Empty"); // NOI18N
        
        return intervals.get(intervals.size() - 1).getEndOffset();
    }
    
    public IntervalSet<E> getFromBounds(int start, int end) {
        IntervalSet<E> result = new IntervalSet<E>();
        AsmOffsetable off = new DefaultOffsetable(start, end);
        
        for (E cur : this) {
            int res = 
                 IntersectionComparator.getInstance().compare(off, cur);
            
            if (res == 0) {
                result.add(cur);
            } else if (res < 0 ) {
                break;
            }
        }
        
        return result;
    }        
    
    public void add(E interval) {
                   
        int res = Collections.binarySearch(intervals, interval, 
                                           IntersectionComparator.getInstance());
        
        if (res >= 0)
            throw new IllegalArgumentException("Intersection"); // NOI18N
               
        intervals.add(-res - 1, interval);
    }
            
    public E getElementAtPosition(int pos) {   
        int res = Collections.binarySearch(intervals, new DummyOffsetable(pos), 
                                  IntersectionComparator.getInstance());
        
        return (res < 0) ? null : intervals.get(res);
    }               
    
    public void clear() {
        intervals.clear(); 
    }
    
    public List<E> getList() {
        return Collections.<E>unmodifiableList(intervals);
    }
    
    public Iterator<E> iterator() {
        return getList().iterator();
    }
       
    private static class IntersectionComparator implements Comparator<AsmOffsetable> {

        private final static Comparator<AsmOffsetable>  instance = new IntersectionComparator();

        public static Comparator<AsmOffsetable>  getInstance() {
            return instance;
        }

        public int compare(AsmOffsetable o1, AsmOffsetable o2) {
            if (o1.getEndOffset() < o2.getStartOffset())
                return -1;

            if (o2.getEndOffset() < o1.getStartOffset()) {
                return 1;
            }

            return 0;
        }  

        private IntersectionComparator() { }
     } 
    
     private static class DummyOffsetable implements AsmOffsetable {

        private final int pos;

        public DummyOffsetable(int pos) {
            super();
            this.pos = pos;
        }

        public int getStartOffset() {
            return pos;
        }

        public int getEndOffset() {
            return pos;
        }
    }     
}
