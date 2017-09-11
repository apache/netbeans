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

package org.netbeans.lib.editor.util;

import java.util.Iterator;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 * Test of CompactMap correctness.
 *
 * @author mmetelka
 */
public class CompactMapTest extends NbTestCase {

    public CompactMapTest(java.lang.String testName) {
        super(testName);
    }

    @SuppressWarnings("unchecked")
    public void test() {
        CompactMap testMap = new CompactMap();
        Object key1 = new NamedObject("key1");
        Object key2 = new NamedObject("key2");
        Object val1 = new NamedObject("val1");
        Object val2 = new NamedObject("val2");
        
        assertEquals(0, testMap.size());
        assertEquals(null, testMap.get(key1));
        testMap.put(key1, val1);
        assertEquals(1, testMap.size());
        assertEquals(val1, testMap.get(key1));
        testMap.put(key2, val2);
        assertEquals(2, testMap.size());
        assertEquals(val2, testMap.get(key2));
        assertEquals(val1, testMap.get(key1));
        
        Iterator it = testMap.entrySet().iterator();
        assertTrue(it.hasNext());
        Map.Entry entry = (Map.Entry)it.next();
        boolean wasKey2;
        if (entry.getKey() == key2) {
            assertEquals(key2, entry.getKey());
            assertEquals(val2, entry.getValue());
            wasKey2 = true;
        } else { // key1
            assertEquals(key1, entry.getKey());
            assertEquals(val1, entry.getValue());
            wasKey2 = false;
        }
        assertTrue(it.hasNext());
        entry = (Map.Entry)it.next();
        if (wasKey2) {
            assertEquals(key1, entry.getKey());
            assertEquals(val1, entry.getValue());
        } else {
            assertEquals(key2, entry.getKey());
            assertEquals(val2, entry.getValue());
        }

        assertEquals(val1, testMap.put(key1, val2));
        assertEquals(val2, testMap.get(key2));
        assertEquals(val2, testMap.remove(key1));
        assertEquals(val2, testMap.get(key2));

        it = testMap.entrySet().iterator();
        assertTrue(it.hasNext());
        entry = (Map.Entry)it.next();
        assertEquals(key2, entry.getKey());
        assertEquals(val2, entry.getValue());

        Entry e1 = new Entry(key1);
        assertEquals(null, e1.setValue(val1));
        assertEquals(null, testMap.putEntry(e1));
        assertEquals(val1, testMap.get(key1));

        Entry e2 = new Entry(key2);
        e2.setValue(val1);
        CompactMap.MapEntry e = testMap.putEntry(e2);
        assertEquals(key2, e.getKey());
        assertEquals(val2, e.getValue());
        
        CompactMap.MapEntry mapEntry = testMap.getFirstEntry(key1.hashCode());
        assertEquals(e1, mapEntry);
        
        // Test MapEntry.hashCode() and equals()
        Entry ehe = new Entry(mapEntry.getKey());
        ehe.setValue(mapEntry.getValue());
        assertEquals(mapEntry.hashCode(), ehe.hashCode());
        assertEquals(mapEntry, ehe);
        
        // Clear the map
        testMap.clear();
        assertEquals(0, testMap.size());

        // Insert 5 mappings to check extending of the bucket table
        Object key3 = new NamedObject("key3");
        Object key4 = new NamedObject("key4");
        Object key5 = new NamedObject("key5");
        Object val3 = new NamedObject("val3");
        Object val4 = new NamedObject("val4");
        Object val5 = new NamedObject("val5");
        testMap.put(key1, val1);
        testMap.put(key2, val2);
        testMap.put(key3, val3);
        testMap.put(key4, val4);
        testMap.put(key5, val5);
        assertEquals(5, testMap.size());
        assertEquals(val2, testMap.get(key2));
        assertEquals(val1, testMap.get(key1));
        assertEquals(val3, testMap.get(key3));
        assertEquals(val4, testMap.get(key4));
        assertEquals(val5, testMap.get(key5));

    }
    
    private static final class Entry extends CompactMap.MapEntry {
        
        private final Object key;
        
        private Object value;
        
        public Entry(Object key) {
            this.key = key;
        }

        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public Object getValue() {
            return value;
        }

        public Object getKey() {
            return key;
        }

        protected int valueHashCode() {
            return (value != null) ? value.hashCode() : 0;
        }
        
        protected boolean valueEquals(Object value2) {
            return (value == value2 || (value != null && value.equals(value2)));
        }
        
    }
    
    private static final class NamedObject extends Object {
        
        private final String name;
        
        public NamedObject(String name) {
            this.name = name;
        }
        
        public String toString() {
            return name;
        }

    }
    
}
