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

package org.openide.loaders;
import java.util.logging.Logger;
import junit.framework.TestResult;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.test.MockLookup;


/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 * @deprecated Please use {@link MockLookup} instead.
 */
public abstract class LoggingTestCaseHid extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.LoggingTestCaseHid$Lkp");
    }

    protected LoggingTestCaseHid (String name) {
        super (name);
    }
    
    @Override
    public void run(TestResult result) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.reset();
        
        super.run(result);
    }
    
    /** Allows subclasses to register content for the lookup. Can be used in 
     * setUp and test methods, after that the content is cleared.
     */
    protected final void registerIntoLookup(Object instance) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.ic.add(instance);
    }

    /** @deprecated Just call {@link Log#controlFlow} directly. */
    protected void registerSwitches(String switches, int timeOut) {
        Log.controlFlow(Logger.getLogger(""), null, switches, timeOut);
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends ProxyLookup {
        InstanceContent ic;
        
        public Lkp () {
            super(new Lookup[0]);
        }
    
        public void reset() {
            this.ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);
            setLookups(new Lookup[] { al, Lookups.metaInfServices(getClass().getClassLoader()) });
        }
    }
}
