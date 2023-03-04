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

package org.netbeans.api.editor.mimelookup;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.mimelookup.EditorTestLookup;
import org.netbeans.modules.editor.mimelookup.TestUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Roskanin, Vita Stejskal
 */
public class MimeLookupMemoryGCTest extends NbTestCase {

    public MimeLookupMemoryGCTest(String testName) {
        super(testName);
    }

    public void testLookupsRelease() {
        int idA = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));

        TestUtilities.consumeAllMemory();
        TestUtilities.gc();

        MimePath mp = MimePath.get("text/x-java");
        Object obj = MimeLookup.getLookup(mp);
        int idB = System.identityHashCode(obj);
        assertEquals("Lookup instance was lost", idA, idB);
        
        // Force the MimePath instance to be dropped from the list of recently used
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }

        Reference<Object> ref = new WeakReference<Object>(obj);
        obj = null;
        mp = null;
        assertGC("Can disappear", ref);
        
        int idC = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertTrue("Lookup instance was not released", idA != idC);
    }
}
