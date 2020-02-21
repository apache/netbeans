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
