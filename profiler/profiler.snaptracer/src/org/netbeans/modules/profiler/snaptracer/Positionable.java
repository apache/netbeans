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

package org.netbeans.modules.profiler.snaptracer;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Common interface for entities that can specify their position within other entities.
 *
 * @author Jiri Sedlacek
 */
public interface Positionable {
    
    /**
     * Entity will be placed before first entity with POSITION_LAST position or at the current last position if there's no entity with POSITION_LAST position.
     */
    public static final int POSITION_AT_THE_END = Integer.MAX_VALUE - 1;
    
    /**
     * Entity will be placed at the current last position.
     */
    public static final int POSITION_LAST = Integer.MAX_VALUE;
    
    /**
     * Comparator based on <code>getPreferredPosition()</code> value.
     * <code>COMPARATOR.compare(Positionable p1, Positionable p2)</code> returns
     * <code>0</code> only if <code>p1.getPreferredPosition() == p2.getPreferredPosition</code>,
     * not to be used as a comparator for <code>TreeSet</code> or <code>TreeMap</code>.
     */
    public static final Comparator COMPARATOR = new PositionableComparator();
    
    /**
     * Comparator based on <code>getPreferredPosition()</code> value.
     * <code>COMPARATOR.compare(Positionable p1, Positionable p2)</code> returns
     * <code>0</code> only if <code>p1.equals(p2)</code>, safe to be used as a
     * comparator for <code>TreeSet</code> or <code>TreeMap</code>.
     * 
     * @since VisualVM 1.3
     */
    public static final Comparator STRONG_COMPARATOR = new StrongPositionableComparator();
    
    /**
     * Returns preferred position of this entity within other entities.
     * 
     * @return preferred position of this entity within other entities.
     */
    public int getPreferredPosition();
    
    /**
     * Implementation of Comparator based on <code>getPreferredPosition()</code> value.
     * <code>PositionableComparator.compare(Positionable p1, Positionable p2)</code> returns
     * <code>0</code> only if <code>p1.getPreferredPosition() == p2.getPreferredPosition</code>,
     * not to be used as a comparator for <code>TreeSet</code> or <code>TreeMap</code>.
     */
    static final class PositionableComparator implements Comparator, Serializable {
        
        public int compare(Object o1, Object o2) {
            Positionable p1 = (Positionable)o1;
            Positionable p2 = (Positionable)o2;
            
            int position1 = p1.getPreferredPosition();
            int position2 = p2.getPreferredPosition();
            
            if (position1 == position2) return 0;
            if (position1 > position2) return 1;
            return -1;
        }
        
    }
    
    /**
     * Implementation of Comparator based on <code>getPreferredPosition()</code> value.
     * <code>StrongPositionableComparator.compare(Positionable p1, Positionable p2)</code>
     * returns <code>0</code> only if <code>p1.equals(p2)</code>, safe to be used
     * as a comparator for <code>TreeSet</code> or <code>TreeMap</code>.
     * 
     * @since VisualVM 1.3
     */
    static final class StrongPositionableComparator implements Comparator, Serializable {
        
        public int compare(Object o1, Object o2) {
            Positionable p1 = (Positionable)o1;
            Positionable p2 = (Positionable)o2;
            
            int position1 = p1.getPreferredPosition();
            int position2 = p2.getPreferredPosition();
            
            // Compare using getPreferredPosition()
            if (position1 > position2) return 1;
            else if (position1 < position2) return -1;
            
            // Make sure to return 0 for o1.equals(o2)
            if (o1.equals(o2)) return 0;
            
            // Compare using classname
            int result = ClassNameComparator.INSTANCE.compare(o1, o2);
            if (result != 0) return result;
            
            // Compare using System.identityHashCode(o)
            result = Integer.valueOf(System.identityHashCode(o1)).compareTo(
                     Integer.valueOf(System.identityHashCode(o2)));
            if (result != 0) return result;
            
            // Compare using o.hashCode()
            result = Integer.valueOf(o1.hashCode()).compareTo(
                     Integer.valueOf(o2.hashCode()));
            if (result != 0) return result;
            
            // Give up, pretend that second number is greater
            return -1;
        }
        
    }

}
