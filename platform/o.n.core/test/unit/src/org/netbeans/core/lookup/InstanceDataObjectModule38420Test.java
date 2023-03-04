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

package org.netbeans.core.lookup;

import java.lang.ref.WeakReference;
import javax.swing.Action;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;

/** A test.
 */
public class InstanceDataObjectModule38420Test extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModule38420Test (String name) {
        super(name);
    }

    @RandomlyFails // NB-Core-Build #8196
    public void testEnableDisableOfModulePreservesExistingInstances () throws Exception {
        Lookup.Result res = Lookup.getDefault ().lookupResult(Action.class);
        Action found = null;
        twiddle(m1, TWIDDLE_ENABLE);
        try {
            twiddle(m2, TWIDDLE_ENABLE);
            StringBuffer foundLog = new StringBuffer ();
            try {
                java.util.Iterator it = res.allInstances ().iterator ();
                while (it.hasNext ()) {
                    Action a = (Action)it.next ();
                    if ("test1.SomeAction".equals (a.getClass ().getName ())) {
                        found = a;
                    } else {
                        foundLog.append ("Found: ");
                        foundLog.append (a.getClass ().getName ());
                        foundLog.append ("\n");
                    }
                }
                assertNotNull ("Action from module m1 has been found. Only found:\n" + foundLog, found);

            } finally {
                twiddle (m2, TWIDDLE_DISABLE);
            }

            Action again = Lookup.getDefault().lookup(found.getClass());
            assertSame ("The instance remains the same", found, again);
            
            WeakReference ref = new WeakReference(found);
            found = null;
            again = null;
            res = null;
            assertGC ("Content of lookup is hold by a weak reference", ref);

        } finally {
            twiddle(m1, TWIDDLE_DISABLE);
        }
    }
    
}
