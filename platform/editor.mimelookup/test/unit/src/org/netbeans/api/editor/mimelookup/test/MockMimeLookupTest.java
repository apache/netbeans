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
package org.netbeans.api.editor.mimelookup.test;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class MockMimeLookupTest extends NbTestCase {

    public MockMimeLookupTest(String name) {
        super(name);
    }

    public void testSetInstances() {
        MimePath mimePath = MimePath.parse("text/x-whatever");
        MockMimeLookup.setInstances(mimePath, "hi!");
        assertEquals("setInstances works", "hi!", MimeLookup.getLookup(mimePath).lookup(String.class));
        MockMimeLookup.setInstances(mimePath, "bye!");
        assertEquals("modified lookup works", "bye!", MimeLookup.getLookup(mimePath).lookup(String.class));
        MockMimeLookup.setInstances(mimePath);
        assertEquals("cleared lookup works", null, MimeLookup.getLookup(mimePath).lookup(String.class));
        
    }
    
    public void testComposition() {
        MimePath mimePath = MimePath.parse("text/x-whatever");
        MockMimeLookup.setInstances(mimePath, "inherited");
        
        mimePath = MimePath.parse("text/x-something-else/text/x-whatever");
        MockMimeLookup.setInstances(mimePath, "top");
        
        Collection<? extends String> all = MimeLookup.getLookup(mimePath).lookupAll(String.class);
        assertEquals("Wrong number of instances", 2, all.size());
        
        Iterator<? extends String> iterator = all.iterator();
        assertEquals("Wrong top item", "top", iterator.next());
        assertEquals("Wrong inherited item", "inherited", iterator.next());
    }
}
