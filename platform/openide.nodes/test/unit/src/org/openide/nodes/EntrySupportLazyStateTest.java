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
package org.openide.nodes;

import java.util.Collection;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class EntrySupportLazyStateTest extends NbTestCase {

    public EntrySupportLazyStateTest(String name) {
        super(name);
    }

    public void testCreatingThreadIsAlwaysNulled() {
        Children ch = new Children.Array();
        EntrySupportLazy lazy = new EntrySupportLazy(ch);
        
        class BrokenEntry implements Children.Entry {
            @Override
            public Collection<Node> nodes(Object source) {
                throw new InternalError();
            }
        }
        BrokenEntry entry = new BrokenEntry();
        EntrySupportLazyState.EntryInfo ei = new EntrySupportLazyState.EntryInfo(lazy, entry);
        
        try {
            ei.getNode(true, this);
        } catch (InternalError err) {
            // ok
        }
        
        assertNull("Creating thread has to be always null", ei.creatingNodeThread);
    }
}
