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

import org.netbeans.core.NbLoaderPool;
import org.openide.util.Lookup;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest6 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest6(String name) {
        super(name);
    }

    public void testReloadSettingsCausesLookupResultChange() throws Exception {
        ERR.log("before twidle enabled");
        twiddle(m2, TWIDDLE_ENABLE);
        ERR.log("Ok twidle enable");
        try {
            ClassLoader l1 = m2.getClassLoader();
            Class<?> c1 = l1.loadClass("test2.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            Lookup.Result r = Lookup.getDefault().lookupResult(c1);
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1, r));
            ERR.log("Action successfully checked, reload"); 
            
            
            LookupL l = new LookupL();
            r.addLookupListener(l);
            ERR.log("Listener attached"); 
            twiddle(m2, TWIDDLE_RELOAD);
            ERR.log("Reload done");
            assertTrue("Got a result change after module reload", l.gotSomething());

            ERR.log("wait for loader pool");
            NbLoaderPool.waitFinished();
            ERR.log("Pool refreshed");
            
            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1, r));
        } finally {
            ERR.log("finally disable");
            twiddle(m2, TWIDDLE_DISABLE);
            ERR.log("finally disable done");
        }
    }
    
}
