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
public class MimeLookupMemoryTest extends NbTestCase {

    public MimeLookupMemoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new String[] {
                "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance"
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );
    }
    
    public void testSimple1() {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookupA = MimeLookup.getLookup(path);
        Lookup lookupB = MimeLookup.getLookup(path);
        assertSame("Lookups are not reused", lookupA, lookupB);
    }

    public void testSimple2() {
        MimePath path = MimePath.get("text/x-java");
        int idA = System.identityHashCode(MimeLookup.getLookup(path));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(path));
        assertEquals("Lookup instance was lost", idA, idB);
    }

    public void testSimple3() {
        int idA = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertEquals("Lookup instance was lost", idA, idB);
    }
  
    public void testLookupResultHoldsTheLookup() {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        Lookup.Result lr = lookup.lookupResult(Object.class);
        
        int pathIdA = System.identityHashCode(path);
        int lookupIdA = System.identityHashCode(lookup);
        
        path = null;
        lookup = null;
        
        // Force the MimePath instance to be dropped from the list of recently used
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();

        int pathIdB = System.identityHashCode(MimePath.get("text/x-java"));
        int lookupIdB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        assertEquals("MimePath instance was lost", pathIdA, pathIdB);
        assertEquals("Lookup instance was lost", lookupIdA, lookupIdB);
    }
}
