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

import java.lang.reflect.Field;
import java.util.logging.Level;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.NamedServicesProvider;

/** 
 * @author Jaroslav Tulach
 */
public class PathInLookupTest extends NbTestCase {
    public PathInLookupTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        Lookup.getDefault();
        Field f = Lookup.class.getDeclaredField("defaultLookup");
        f.setAccessible(true);
        f.set(null, null);
        System.setProperty("org.openide.util.Lookup.paths", "MyServices:YourServices");
        MockServices.setServices(P.class);
        Lookup.getDefault();
    }
    
    public void testInterfaceFoundInMyServices() throws Exception {
        assertNull("not found", Lookup.getDefault().lookup(Shared.class));
        Shared v = new Shared();
        P.ic1.add(v);
        assertNotNull("found", Lookup.getDefault().lookup(Shared.class));
        P.ic1.remove(v);
        assertNull("not found again", Lookup.getDefault().lookup(Shared.class));
    }
    public void testInterfaceFoundInMyServices2() throws Exception {
        assertNull("not found", Lookup.getDefault().lookup(Shared.class));
        Shared v = new Shared();
        P.ic2.add(v);
        assertNotNull("found", Lookup.getDefault().lookup(Shared.class));
        P.ic2.remove(v);
        assertNull("not found again", Lookup.getDefault().lookup(Shared.class));
    }

    static final class Shared extends Object {}

    public static final class P extends NamedServicesProvider {
        static InstanceContent ic1 = new InstanceContent();
        static InstanceContent ic2 = new InstanceContent();
        static AbstractLookup[] arr = {
            new AbstractLookup(ic1), new AbstractLookup(ic2)
        };

        public Lookup create(String path) {
            int indx = -1;
            if (path.equals("MyServices/")) {
                indx = 0;
            }
            if (path.equals("YourServices/")) {
                indx = 1;
            }
            if (indx == -1) {
                fail("Unexpected lookup query: " + path);
            }
            return arr[indx];
        }
    }

}
