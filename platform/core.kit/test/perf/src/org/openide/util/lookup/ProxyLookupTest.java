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

import org.netbeans.performance.Benchmark;
import java.util.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;

/**
 * Comparison beween tree lookup structure and flat lookup structure.
 * Uses three different Lookup configurations:
 * <UL><LI>ProxyLookup containing 16 InstanceLookups
 *     <LI>ProxyLookup containing 2 ProxyLookups of 8 InstanceLookups each
 *     <LI>ProxyLookup of 2 ProxyLookups of 2 ProxyLookups of 4 InstanceLookups each
 * </UL>
 */
public class ProxyLookupTest extends Benchmark {

    public ProxyLookupTest(String name) {
        super( name, new String[] {"tree3", "tree2", "flat"} );
    }

    private Lookup lookup;
    
    protected void setUp() {
        String type = (String)getArgument();
	if("tree3".equals(type)) {
	    lookup = new ProxyLookup( new Lookup[] {
		new ProxyLookup( new Lookup[] {
		    createProxy(4),
		    createProxy(4)
		}),
		new ProxyLookup( new Lookup[] {
		    createProxy(4),
		    createProxy(4)
		})
	    });
	} else if("tree2".equals(type)) {
	    lookup = new ProxyLookup( new Lookup[] {
		createProxy(8),
		createProxy(8)
	    });
	} else {
	    lookup = createProxy(16);
	}
    }
    
    private Lookup createOne() {
	InstanceContent ic = new InstanceContent();
	ic.add(new Object());
	ic.add("");
	return new AbstractLookup(ic);
    }
    
    private Lookup createProxy(int subs) {
    	Lookup[] delegates = new Lookup[subs];
	for(int i=0; i<subs; i++) delegates[i] = createOne();
	return new ProxyLookup(delegates);
    }
    
    protected void tearDown() {
        lookup=null;
    }

    public void testLookupObject() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
            // do the stuff here, 
	    lookup.lookup(Object.class);
        }
    }    

    public void testLookupString() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
            // do the stuff here, 
	    lookup.lookup(String.class);
        }
    }    

    public void testAllInstances() throws Exception {
        int count = getIterationCount();
	Lookup.Result result = lookup.lookup(new Lookup.Template(String.class));

        while( count-- > 0 ) {
            // do the stuff here, 
	    Collection c = result.allInstances();
        }
    }    

    public void testIterateInstances() throws Exception {
        int count = getIterationCount();
	Lookup.Result result = lookup.lookup(new Lookup.Template(String.class));

        while( count-- > 0 ) {
            // do the stuff here, 
	    Iterator i = result.allInstances().iterator();
	    while (i.hasNext()) i.next();
        }
    }    

    public static void main( String[] args ) {
	junit.textui.TestRunner.run( new junit.framework.TestSuite( ProxyLookupTest.class ) );
    }
}
