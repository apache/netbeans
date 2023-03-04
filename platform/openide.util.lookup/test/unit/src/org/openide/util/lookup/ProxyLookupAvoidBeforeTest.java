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
package org.openide.util.lookup;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProxyLookupAvoidBeforeTest extends NbTestCase implements LookupListener {

    public ProxyLookupAvoidBeforeTest(String name) {
        super(name);
    }
    
    public void testDontCallBeforeLookup() throws Exception {
        InstanceContent ic = new InstanceContent();
        ic.add(1);
        ic.add(2L);
        
        ABefore one = new ABefore(ic);
        
        ProxyLookup lkp = new ProxyLookup(one);
        
        Result<Long> longResult = lkp.lookupResult(Long.class);
        longResult.addLookupListener(this);
        Result<Integer> intResult = lkp.lookupResult(Integer.class);
        intResult.addLookupListener(this);
        Result<Number> numResult = lkp.lookupResult(Number.class);
        numResult.addLookupListener(this);

        one.queryAllowed = true;
        assertEquals("Two", Long.valueOf(2L), longResult.allInstances().iterator().next());
        assertEquals("One", Integer.valueOf(1), intResult.allInstances().iterator().next());
        assertEquals("Two numbers", 2, numResult.allInstances().size());
        assertEquals("Two number items", 2, numResult.allItems().size());
        one.queryAllowed = false;
        
        NoBefore nob = new NoBefore();
        lkp.setLookups(one, nob);
        
        nob.queryAllowed = true;
        one.queryAllowed = true;
        assertEquals("Again Two", Long.valueOf(2L), lkp.lookup(Long.class));
        assertEquals("Again One", Integer.valueOf(1), lkp.lookup(Integer.class));
        assertEquals("Again Two numbers", 2, lkp.lookupAll(Number.class).size());
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        fail("No changes expected");
    }
    
    class NoBefore extends ProxyLookup {
        boolean queryAllowed;

        public NoBefore(Lookup... lookups) {
            super(lookups);
        }

        @Override
        protected void beforeLookup(Template<?> template) {
            assertTrue("Please don't call beforeLookup from changes: " + template.getType(), queryAllowed);
        }
    }
    
    class ABefore extends AbstractLookup {
        boolean queryAllowed;

        public ABefore(Content content) {
            super(content);
        }
        
        @Override
        protected void beforeLookup(Template<?> template) {
            assertTrue("Please don't call beforeLookup from changes: " + template.getType(), queryAllowed);
        }
        
    }
}
