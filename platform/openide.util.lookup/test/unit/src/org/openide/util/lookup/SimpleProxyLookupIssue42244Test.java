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

import java.lang.ref.WeakReference;
import java.util.*;
import org.openide.util.Lookup;

/** To simulate issue 42244.
 */
@SuppressWarnings("unchecked") // XXX ought to be corrected, just a lot of them
public class SimpleProxyLookupIssue42244Test extends AbstractLookupBaseHid implements AbstractLookupBaseHid.Impl {
    public SimpleProxyLookupIssue42244Test (java.lang.String testName) {
        super(testName, null);
    }

    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (final Lookup lookup) {
        class C implements Lookup.Provider {
            public Lookup getLookup () {
                return lookup;
            }
        }
        return Lookups.proxy (new C ());
    }
    
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new KeepResultsProxyLookup (new AbstractLookup (ic));
    }
    
    public void clearCaches () {
        KeepResultsProxyLookup k = (KeepResultsProxyLookup)this.instanceLookup;
        
        ArrayList toGC = new ArrayList ();
        Iterator it = k.allQueries.iterator ();
        while (it.hasNext ()) {
            Lookup.Result r = (Lookup.Result)it.next ();
            toGC.add (new WeakReference (r));
        }
        
        k.allQueries = null;
        
        it = toGC.iterator ();
        while (it.hasNext ()) {
            WeakReference r = (WeakReference)it.next ();
            assertGC ("Trying to release all results from memory", r);
        }
    }
    
    class KeepResultsProxyLookup extends ProxyLookup {
        private ArrayList allQueries = new ArrayList ();
        private ThreadLocal in = new ThreadLocal ();
        
        public KeepResultsProxyLookup (Lookup delegate) {
            super (new Lookup[] { delegate });
        }
        
        @Override
        protected void beforeLookup (org.openide.util.Lookup.Template template) {
            super.beforeLookup (template);
            if (allQueries != null && in.get () == null) {
                in.set (this);
                Lookup.Result res = lookup (template);
                allQueries.add (res);
                in.set (null);
            }
        }
        
    }
}
