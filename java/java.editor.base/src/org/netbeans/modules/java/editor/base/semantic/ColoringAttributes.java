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
package org.netbeans.modules.java.editor.base.semantic;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Jan Lahoda
 */
public enum ColoringAttributes {

    UNUSED,

    ABSTRACT,

    FIELD,
    LOCAL_VARIABLE,
    PARAMETER,
    METHOD,
    CONSTRUCTOR,
    MODULE,
    CLASS,
    INTERFACE,
    ANNOTATION_TYPE,
    ENUM,
    DEPRECATED,
    STATIC,

    DECLARATION,
    
    PRIVATE,
    PACKAGE_PRIVATE,
    PROTECTED,
    PUBLIC,

    TYPE_PARAMETER_DECLARATION,
    TYPE_PARAMETER_USE,

    UNDEFINED,

    MARK_OCCURRENCES,
    
    KEYWORD,
    
    JAVADOC_IDENTIFIER,

    UNINDENTED_TEXT_BLOCK;
    
    public static Coloring empty() {
        return new Coloring();
    }
    
    public static Coloring add(Coloring c, ColoringAttributes a) {
        Coloring ci = new Coloring();
        
        ci.value = c.value | (1 << a.ordinal());
        
        return ci;
    }

    public static final class Coloring implements Collection<ColoringAttributes> {

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
        
        
    }
}
