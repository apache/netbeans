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

import java.util.concurrent.Executor;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class AbstractLookupExecutorTest extends AbstractLookupBaseHid 
implements AbstractLookupBaseHid.Impl, Executor, LookupListener {
    Lookup.Result<?> res;
    
    
    public AbstractLookupExecutorTest(java.lang.String testName) {
        super(testName, null);
    }
    
    //
    // Impl of AbstractLookupBaseHid.Impl
    //

    /** Creates the initial abstract lookup.
     */
    @Override
    public Lookup createInstancesLookup (InstanceContent ic) {
        ic.attachExecutor(this);
        Lookup l = new AbstractLookup (ic, new InheritanceTree ());
        return l;
    }
    
    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    @Override
    public Lookup createLookup (Lookup lookup) {
        res = lookup.lookupResult(Object.class);
        res.addLookupListener(this);
        return lookup;
    }

    @Override
    public void clearCaches () {
        res = null;
    }    

    ThreadLocal<Object> ME = new ThreadLocal<Object>();
    @Override
    public void execute(Runnable command) {
        assertEquals("Not yet set", null, ME.get());
        ME.set(this);
        try {
            command.run();
        } finally {
            ME.set(null);
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        assertEquals("Changes delivered only from execute method", this, ME.get());
    }
    
    //
    // need to clean the res field
    @Override
    public void testDoubleAddIssue35274() throws Exception {
        res = null;
        super.testDoubleAddIssue35274();
    }
}
