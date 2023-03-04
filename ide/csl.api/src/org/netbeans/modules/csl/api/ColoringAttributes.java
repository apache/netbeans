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
package org.netbeans.modules.csl.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Jan Lahoda
 */
public enum ColoringAttributes {
    ABSTRACT,
    ANNOTATION_TYPE,
    CLASS,
    CUSTOM1,
    CUSTOM2,
    CUSTOM3,
    CONSTRUCTOR,
    DECLARATION,
    DEPRECATED,
    ENUM,
    FIELD,
    GLOBAL,
    INTERFACE,
    LOCAL_VARIABLE,
    LOCAL_VARIABLE_DECLARATION,
    MARK_OCCURRENCES,
    METHOD,
    PACKAGE_PRIVATE,
    PARAMETER,
    PRIVATE,
    PROTECTED,
    PUBLIC,
    REGEXP,
    STATIC,
    TYPE_PARAMETER_DECLARATION,
    TYPE_PARAMETER_USE,
    UNDEFINED,
    UNUSED;
    //ATTRIBUTE_DECLARATION,
    //ATTRIBUTE_USE,
    //FUNCTION_DECLARATION,
    //FUNCTION_USE,
    //OPERATION_DECLARATION,
    //OPERATION_USE,
    //JAVA_FIELD_USE,
    //LOCAL_VARIABLE_USE,
    //LOCAL_VARIABLE_DECLARATION,
    //PARAMETER_DECLARATION,
    //PARAMETER_USE,
    //JAVA_METHOD_USE,
    //JAVA_CONSTRUCTOR_USE,
    //CLASS_USE,
    //CLASS_DECLARATION,
    //JAVA_INTERFACE_USE,
            
    public static Coloring empty() {
        return new Coloring();
    }
    
    public static Coloring add(Coloring c, ColoringAttributes a) {
        Coloring ci = new Coloring();
        
        ci.value = c.value | (1 << a.ordinal());
        
        return ci;
    }
    // TODO - figure out why the Java Editor had this, instead of a normal EnumSet<ColoringAttributes>
    public static final class Coloring implements Collection<ColoringAttributes> {
        static {
            // The bit vector here doesn't work for a large set of values!!
            assert ColoringAttributes.values().length < 32;
        } 

        private int value;
        
        private Coloring() {}
        
        public int size() {
            return Integer.bitCount(value);
        }

        public boolean isEmpty() {
            return value == 0;
        }

        public boolean contains(Object o) {
            if (o instanceof ColoringAttributes) {
                return (value & (1 << ((ColoringAttributes) o).ordinal())) !=0;
            } else {
                return false;
            }
        }

        public Iterator<ColoringAttributes> iterator() {
            Set<ColoringAttributes> s = EnumSet.noneOf(ColoringAttributes.class);
            for (ColoringAttributes c : ColoringAttributes.values()) {
                if (contains(c))
                    s.add(c);
            }
            
            return s.iterator();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean add(ColoringAttributes o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o))
                    return false;
            }
            
            return true;
        }

        public boolean addAll(Collection<? extends ColoringAttributes> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Coloring) {
                //XXX:
                return ((Coloring) obj).value == value;
            }
            
            return false;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (ColoringAttributes a : this) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(a.name());
            }
            
            return sb.toString();
        }
    }

        public static final EnumSet<ColoringAttributes> UNUSED_SET = EnumSet.of(UNUSED);
        public static final EnumSet<ColoringAttributes> FIELD_SET = EnumSet.of(FIELD);
        public static final EnumSet<ColoringAttributes> STATIC_FIELD_SET = EnumSet.of(FIELD, STATIC);
        public static final EnumSet<ColoringAttributes> PARAMETER_SET = EnumSet.of(PARAMETER);
        public static final EnumSet<ColoringAttributes> CUSTOM1_SET = EnumSet.of(CUSTOM1);
        public static final EnumSet<ColoringAttributes> CUSTOM2_SET = EnumSet.of(CUSTOM2);
        public static final EnumSet<ColoringAttributes> CUSTOM3_SET = EnumSet.of(CUSTOM3);
        public static final EnumSet<ColoringAttributes> CONSTRUCTOR_SET = EnumSet.of(CONSTRUCTOR);
        public static final EnumSet<ColoringAttributes> METHOD_SET = EnumSet.of(METHOD);
        public static final EnumSet<ColoringAttributes> CLASS_SET = EnumSet.of(CLASS);
        public static final EnumSet<ColoringAttributes> GLOBAL_SET = EnumSet.of(GLOBAL);
        public static final EnumSet<ColoringAttributes> REGEXP_SET = EnumSet.of(REGEXP);
        public static final EnumSet<ColoringAttributes> STATIC_SET = EnumSet.of(STATIC);
        // When you update this, also look at the performance mapping cache, ColoringManager.COLORING_MAP
}
