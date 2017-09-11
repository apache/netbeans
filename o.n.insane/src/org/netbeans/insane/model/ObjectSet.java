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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.insane.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** A set of objects with reflexive map behavior, using possibly
 * an external hash implementation. Tries to minimize memory consumption.
 *
 * @author Nenik
 */
class ObjectSet {

    private Hash hash;
    private Object[] table;
    private float loadFactor = 0.75f;
    private int limit;
    private int size;

    public ObjectSet() {
        this (new Hash() {
            public boolean equals(Object o1, Object o2) {
                return o1.equals(o2);
            }

            public int hashCodeFor(Object o) {
                return o.hashCode();
            }
        });
    }
    
    /** Creates a new instance of ObjectSet */
    public ObjectSet(Hash hash) {
        this.hash = hash;
        table = new Object[11];
        limit = (int)(table.length * loadFactor);
    }

    public interface Hash {
        public int hashCodeFor(Object o);
        public boolean equals(Object o1, Object o2);
    }
    
    
    public Object get(Object key) {
        int bucket = (hash.hashCodeFor(key) & 0x7FFFFFFF)  % table.length;
        
        while (table[bucket] != null) {
            if (hash.equals(key, table[bucket])) return table[bucket];
            bucket = (bucket + 1) % table.length;
        }
        
        return null; // XXX
    }
    
    /* Always replaces exiting equals object */
    public void put(Object key) {
//System.err.println("put: size=" + size + ", limit=" + limit + ", len=" + table.length);
        if ((size+1) > limit) rehash(table.length*2);

        size++;
        int bucket = (hash.hashCodeFor(key) & 0x7FFFFFFF) % table.length;
            
        while (table[bucket] != null && !hash.equals(key, table[bucket])) {
            bucket = (bucket + 1) % table.length;
        }
        
        table[bucket] = key;
    }
    
    public boolean contains(Object key) {
        return get(key) != null;
    }
    
    public Iterator iterator() {
        return new Iterator() {
            int ptr = 0;
            Object next;
            public boolean hasNext() {
                while (next == null && ptr < table.length) {
                    next = table[ptr++];
                }
                return next != null;
            }
            
            public Object next() {
                if (!hasNext()) throw new NoSuchElementException();
                Object ret = next;
                next = null;
                return ret;
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    private void rehash(int newSize) {
        Object[] newTable = new Object[newSize];
        for (int i=0; i<table.length; i++) {
            Object act = table[i];
            if (act != null) {
                int bucket = (hash.hashCodeFor(act) & 0x7FFFFFFF) % newTable.length;
                while (newTable[bucket] != null) { // find an empty slot
                    bucket = (bucket + 1) % newTable.length;
                }
                
                newTable[bucket] = act;
            }
        }
            
        table = newTable;
        limit = (int)(table.length * loadFactor);
    }
}
