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
package org.netbeans.modules.debugger.jpda.projects;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Martin Entlicher
 */
public class WeakHashMapActiveTest extends NbTestCase {
    
    public WeakHashMapActiveTest(String name) {
        super(name);
    }
    
    public void testRelease() throws Exception {
        Object k1 = new Object();
        Object k2 = new Object();
        Object v1 = new Object();
        Object v2 = new Object();
        Reference rv1 = new WeakReference(v1);
        Reference rv2 = new WeakReference(v2);
        WeakHashMapActive whma = new WeakHashMapActive();
        whma.put(k1, v1);
        whma.put(k2, v2);
        assertEquals("Size", 2, whma.size());
        assertEquals(v1, whma.get(k1));
        assertEquals(v2, whma.get(k2));
        Reference rk1 = new WeakReference(k1);
        Reference rk2 = new WeakReference(k2);
        k1 = k2 = null;
        System.gc();
        int[] arr = new int[10000000];
        arr[1000000] = 10;
        arr = null;
        System.gc();
        assertGC("WeakHashMapActive's key is not released from memory", rk1);
        assertGC("WeakHashMapActive's key is not released from memory", rk2);
        assertEquals("Size", 0, whma.size());
        v1 = v2 = null;
        System.gc();
        arr = new int[20000000];
        arr[1000000] = 10;
        arr = null;
        System.gc();
        assertGC("WeakHashMapActive's value is not released from memory", rv1);
        assertGC("WeakHashMapActive's value is not released from memory", rv2);
    }
}
