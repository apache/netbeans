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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.mimelookup.TestUtilities;

/**
 *
 * @author vita
 */
public class MimePathMemoryTest extends NbTestCase {
    
    /** Creates a new instance of DummyMemoryTest */
    public MimePathMemoryTest(String name) {
        super(name);
    }

    public void testSimple() {
        MimePath pathA = MimePath.get("text/x-java");
        MimePath pathB = MimePath.get("text/x-java");
        assertSame("MimePath instances are not cached and reused", pathA, pathB);
    }
    
    public void testListOfRecentlyUsed() {
        int idA = System.identityHashCode(MimePath.get("text/x-java"));

        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimePath.get("text/x-java"));
        
        // The same instance of MimePath should still be in the cache
        assertEquals("The MimePath instance was lost", idA, idB);
        
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }
        
        // Now the original text/x-java MimePath should be discarded
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idC = System.identityHashCode(MimePath.get("text/x-java"));
        assertTrue("The MimePath instance was not release", idA != idC);
    }
}
