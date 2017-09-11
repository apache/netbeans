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
